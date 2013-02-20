package edu.cmu.lti.oaqa.bio.test.yanfang.ie;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.oaqa.model.Passage;

import edu.cmu.lti.oaqa.bio.framework.retrieval.DocumentRetrieverWrapper;
import edu.cmu.lti.oaqa.bio.retrieval.tools.CleanTerms;
import edu.cmu.lti.oaqa.bio.test.ziy.ie.span.LingPipeSentenceChunker;
import edu.cmu.lti.oaqa.bio.test.ziy.keyterm.pos.LingPipeHmmPosTagger;
import edu.cmu.lti.oaqa.ecd.log.AbstractLoggedComponent;
import edu.cmu.lti.oaqa.framework.BaseJCasHelper;
import edu.cmu.lti.oaqa.framework.UimaContextHelper;
import edu.cmu.lti.oaqa.framework.ViewManager;
import edu.cmu.lti.oaqa.framework.ViewManager.ViewType;
import edu.cmu.lti.oaqa.framework.data.Article;
import edu.cmu.lti.oaqa.framework.data.Keyterm;
import edu.cmu.lti.oaqa.framework.data.KeytermList;
import edu.cmu.lti.oaqa.framework.data.PassageCandidate;
import edu.cmu.lti.oaqa.framework.data.PassageCandidateArray;
import edu.cmu.lti.oaqa.framework.data.TextSpan;
import edu.cmu.lti.oaqa.framework.eval.passage.PassageHelper;
import edu.cmu.lti.oaqa.framework.types.InputElement;

public class TrainingSVM extends AbstractLoggedComponent {

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
  public final void process(JCas jcas) throws AnalysisEngineProcessException {
    super.process(jcas);
    try {
      // collect gold standard
      List<Passage> gs = PassageHelper.loadDocumentSet(ViewManager.getView(jcas,
              ViewType.DOCUMENT_GS));
      // collect retrieved passages
      List<PassageCandidate> passages = PassageCandidateArray.retrievePassageCandidates(ViewManager
              .getCandidateView(jcas));
      // get the question
      String questionText = ((InputElement) BaseJCasHelper.getAnnotation(jcas, InputElement.type))
              .getQuestion();
      // get keyterms
      KeytermList keytermList = new KeytermList(jcas);
      List<Keyterm> keyterms = keytermList.getKeyterms();
      int count = 0;
      HashMap<Integer, List<String>> features = new HashMap<Integer, List<String>>();
      List<String> labels = new ArrayList<String>();
      Map<String, List<Passage>> id2gsPassages = new HashMap<String, List<Passage>>();
      for (Passage gs_passage : gs) {
        String id = gs_passage.getUri();
        if (!id2gsPassages.containsKey(id)) {
          id2gsPassages.put(id, new ArrayList<Passage>());
        }
        id2gsPassages.get(id).add(gs_passage);
      }
      
      // get the labels by comparing retrieved passages and golden standard. Regard the passage as
      // relevant as long as it have overlap with the gs
      // this should be gotten from the DB, instead of comparing the results and golden standard
   
      for (PassageCandidate passage : passages) {
        // get the labels
        String passage_id = passage.getDocID();
        if (!id2gsPassages.containsKey(passage_id)) {
          labels.add("0");
          continue;
        }        
        for (Passage gs_passage_in_same_doc : id2gsPassages.get(passage_id)){
          if (getOverlapLength(gs_passage_in_same_doc, passage) > 0) {
            labels.add("1");
            continue;
          }
        }
        labels.add("0");
        
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
        String keytermCount = Integer.toString(this.getKeytermCount(psg, keyterms));
        String ranking = Integer.toString(passage.getRank());
        String score = Double.toString(passage.getProbability());
        
        features.put(count, new ArrayList<String>());
        features.get(count).add(keytermCount); 
        features.get(count).add(ranking); 
        features.get(count).add(score); 
        
        count++;
        if(count > limit) break;
      }
      
      System.out.println("<><><><><>" + labels);
      System.out.println("<><><><><>" + features);
     
      // write the features and labels into the SVMlib format file
      outputAsSVMLibFormat(features, labels, "SVMtrain");
      
      // train the model based on SVMlib
      String[] arguments_for_training = {"-m","100", "SVMtrain","SVMmodel"}; 
      svm_train train = new svm_train();
      train.run(arguments_for_training);
      
      /*
       * Map<String, List<PassageCandidate>> id2gsPassages = new HashMap<String,
       * List<PassageCandidate>>(); for (PassageCandidate gsPassage : gsPassages) { String id =
       * gsPassage.getDocID(); if (!id2gsPassages.containsKey(id)) { id2gsPassages.put(id, new
       * ArrayList<PassageCandidate>()); } id2gsPassages.get(id).add(gsPassage); }
       */

    } catch (Exception e) {
      throw new AnalysisEngineProcessException(e);
    }
  }
  
  /*
   * Return Integer.MIN_VALUE if p1 and p2 come from different documents, positive value if p1 and
   * p2 are overlapped, and the value is the number of overlapped length, negative (or 0) value if
   * p1 and p2 are not overlapped, and the absolute value is the distance between two passages.
   */
  private static int getOverlapLength(Passage p1, PassageCandidate p2) {
    // different documents
    if (!p1.getUri().equals(p2.getDocID())) {
      return Integer.MIN_VALUE;
    }
    // overlapped passages
    if (p1.getBegin() < p2.getEnd() && p1.getEnd() > p2.getStart()) {
      return Math.min(p1.getEnd(), p2.getEnd()) - Math.max(p1.getBegin(), p2.getStart());
    }
    // non-overlapped passages
    return -Math.min(Math.abs(p1.getBegin() - p2.getEnd()), Math.abs(p1.getEnd() - p2.getStart()));
  }
  
  public static int getKeytermCount(String psg, List<Keyterm> keyterms) {
    int result = 0;
    Map<String, Keyterm> keytermSet = new HashMap<String, Keyterm>();
    for (Keyterm keyterm : keyterms) {
      String stemmed = CleanTerms.getStemmedTerm(keyterm.getText());
      if (!keytermSet.containsKey(stemmed)) {
        keytermSet.put(CleanTerms.getStemmedTerm(keyterm.getText()), keyterm);
      }
    }
    
    List<TextSpan> sentences = LingPipeSentenceChunker.split(psg);
    for (TextSpan sentence : sentences) {
      List<String> tokens = LingPipeHmmPosTagger.tokenize(psg.substring(sentence.begin, sentence.end));

      for (String token : tokens) {
        if (keytermSet.containsKey(CleanTerms.getStemmedTerm(token)))
          result++;
      }
    }
    
    return result;
  }
  
  public static void outputAsSVMLibFormat(HashMap<Integer, List<String>> features, List<String> labels, String fileName) {
    try {

      File file = new File(fileName);
      
      if (!file.exists()) {
        file.createNewFile();
      }
 
      FileWriter fw = new FileWriter(file.getAbsoluteFile());
      BufferedWriter bw = new BufferedWriter(fw);
      
      String content = "";
      for (int i = 0; i< Math.min(features.size(), labels.size()); i++) {
        content = labels.get(i) + " ";
        int feature_number = 1;
        for (String feature : features.get(i)) {
          content += Integer.toString(feature_number) + ":" + feature + " ";
          feature_number++;
        }
        bw.write(content);
        bw.newLine();
      }
      
      bw.close();
 
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}
