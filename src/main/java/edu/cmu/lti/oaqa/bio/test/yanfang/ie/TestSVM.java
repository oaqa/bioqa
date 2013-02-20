package edu.cmu.lti.oaqa.bio.test.yanfang.ie;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;

import edu.cmu.lti.oaqa.bio.framework.retrieval.DocumentRetrieverWrapper;
import edu.cmu.lti.oaqa.cse.basephase.ie.AbstractPassageUpdater;
import edu.cmu.lti.oaqa.framework.UimaContextHelper;
import edu.cmu.lti.oaqa.framework.data.Article;
import edu.cmu.lti.oaqa.framework.data.Keyterm;
import edu.cmu.lti.oaqa.framework.data.PassageCandidate;
import edu.cmu.lti.oaqa.framework.data.RetrievalResult;
import edu.cmu.lti.oaqa.framework.data.TextSpan;

public class TestSVM extends AbstractPassageUpdater {
  
  private int limit;

  protected DocumentRetrieverWrapper retriever;

  @Override
  public void initialize(UimaContext c) throws ResourceInitializationException {
    super.initialize(c);
    limit = UimaContextHelper.getConfigParameterIntValue(c, "limit", 10);
    boolean zipped = UimaContextHelper.getConfigParameterBooleanValue(c, "Zipped", true);
    try {
      retriever = new DocumentRetrieverWrapper((String) c.getConfigParameterValue("Prefix"),
              zipped, true);
    } catch (NullPointerException e) {
      retriever = new DocumentRetrieverWrapper(zipped, true);
    }
  }
  @Override
  protected List<PassageCandidate> updatePassages(String question, List<Keyterm> keyterms,
          List<RetrievalResult> documents, List<PassageCandidate> passages) {
    // TODO Auto-generated method stub
    
    HashMap<Integer, List<String>> features = new HashMap<Integer, List<String>>();
    List<String> labels = new ArrayList<String>();
    int count = 0;
    
    for(PassageCandidate passage : passages) {
      
      // extract features: ranking score, ranking, keyterm counts,
      //passage.getProbability()
      Article article = retriever.getDocument(passage.getDocID());
      // sanity check
      if (passage.getStart() > article.getText().length() - 1) {
        passage.setStart(article.getText().length() - 1);
      }
      if (passage.getEnd() > article.getText().length()) {
        passage.setEnd(article.getText().length());
      }
      // get all sentences from the retrieved paragraph
      TextSpan origSpan = new TextSpan(passage.getStart(), passage.getEnd());
      String psg = article.getSpanText(origSpan);
      // keyterm counts
      String keytermCount = Integer.toString(TrainingSVM.getKeytermCount(psg, keyterms));
      String ranking = Integer.toString(passage.getRank());
      String score = Double.toString(passage.getProbability());
      
      features.put(count, new ArrayList<String>());
      features.get(count).add(keytermCount); 
      features.get(count).add(ranking); 
      features.get(count).add(score);
      
      labels.add(Integer.toString(count));
      
      count++;
    }
    
    // write the features and labels into the SVMlib format file
    TrainingSVM.outputAsSVMLibFormat(features, labels, "SVMtest");
    
    // predict 
    String[] arguments_for_prediction = {"-b", "0", "SVMtest", "SVMmodel", "SVMoutput"};
    try {
      svm_predict.run(arguments_for_prediction);
    } catch (IOException e) {
      e.printStackTrace();
    } 
    
    // read predict results
    BufferedReader br = null;
    List<String> predict_results = new ArrayList<String>();
    try {
 
      String sCurrentLine;
      br = new BufferedReader(new FileReader("SVMoutput"));
      while ((sCurrentLine = br.readLine()) != null) {
        predict_results.add(sCurrentLine);
      }
      br.close();
    } catch (IOException e) {
      e.printStackTrace();
    }    
       
    // recalculate the score based on predicted results
    for (int i = 0; i<Math.min(predict_results.size(), passages.size()); i++) {
      passages.get(i).setProbablity(passages.get(i).getProbability() +  Float.parseFloat(predict_results.get(i)));
    }
    
    System.out.println(">>>>>" + passages.size());
    
    return passages;
  }

}
