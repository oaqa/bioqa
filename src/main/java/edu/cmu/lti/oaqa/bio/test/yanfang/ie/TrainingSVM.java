package edu.cmu.lti.oaqa.bio.test.yanfang.ie;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.oaqa.model.Passage;

import edu.cmu.lti.oaqa.bio.framework.retrieval.DocumentRetrieverWrapper;
import edu.cmu.lti.oaqa.ecd.log.AbstractLoggedComponent;
import edu.cmu.lti.oaqa.framework.UimaContextHelper;
import edu.cmu.lti.oaqa.framework.ViewManager;
import edu.cmu.lti.oaqa.framework.ViewManager.ViewType;
import edu.cmu.lti.oaqa.framework.data.Article;
import edu.cmu.lti.oaqa.framework.data.PassageCandidate;
import edu.cmu.lti.oaqa.framework.data.PassageCandidateArray;
import edu.cmu.lti.oaqa.framework.data.TextSpan;
import edu.cmu.lti.oaqa.framework.eval.passage.PassageHelper;

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
      int count = 0;
      HashMap<String, List<String>> features = new HashMap<String, List<String>>();
      List<String> labels = new ArrayList<String>();

      // display retrieved passages
      for (PassageCandidate passage : passages) {
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

        System.out.println(">>>>>>" + article.getSpanText(origSpan));
        count++;
        if (count > limit)
          break;
      }

      count = 0;
      // display golden standard
      for (Passage gs_passage : gs) {
        Article article = retriever.getDocument(gs_passage.getUri());
        TextSpan origSpan = new TextSpan(gs_passage.getBegin(), gs_passage.getEnd());
        System.out.println("<<<<<<<" + article.getSpanText(origSpan));
        count++;
        if (count > limit)
          break;
      }

      // get the labels by comparing retrieved passages and golden standard. Regard the passage as
      // relevant as long as it have overlap with the gs
      // this should be gotten from the DB, instead of comparing the results and golden standard

      
      
      
      // extract features from retrieved passages

      
      
      
      
      // write the features and labels into the SVMlib format file

      
      
      
      // train the model based on SVMlib

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

}
