package edu.cmu.lti.oaqa.bio.core.ie;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.jcas.JCas;

import edu.cmu.lti.oaqa.bio.framework.retrieval.DocumentRetrieverWrapper;
import edu.cmu.lti.oaqa.ecd.log.AbstractLoggedComponent;
import edu.cmu.lti.oaqa.framework.JCasHelper;
import edu.cmu.lti.oaqa.framework.ViewManager;
import edu.cmu.lti.oaqa.framework.data.PassageCandidate;

public class PassageTextAppender extends AbstractLoggedComponent {

  @Override
  public final void process(JCas jcas) throws AnalysisEngineProcessException {
    super.process(jcas);
    try {
      JCas candidateView = ViewManager.getCandidateView(jcas);
      JCas finalView = ViewManager.getFinalAnswerView(jcas);
      List<PassageCandidate> passages = JCasHelper.getPassages(candidateView);
      List<String> texts = appendTexts(passages);
      JCasHelper.storePassageTexts(finalView, passages, texts);
    } catch (CASException e) {
      throw new AnalysisEngineProcessException(e);
    }
  }

  protected List<String> appendTexts(List<PassageCandidate> passages) {
    DocumentRetrieverWrapper retriever = new DocumentRetrieverWrapper(true, true);
    List<String> texts = new ArrayList<String>();
    for (PassageCandidate passage : passages) {
      String text = retriever.getDocumentText(passage.getDocID());
      int start = passage.getStart();
      int end = passage.getEnd();
      texts.add(text.substring(start, end));
    }
    retriever.releaseCas();
    return texts;
  }

}
