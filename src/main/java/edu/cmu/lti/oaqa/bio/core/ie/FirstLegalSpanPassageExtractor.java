package edu.cmu.lti.oaqa.bio.core.ie;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.resource.ResourceInitializationException;

import edu.cmu.lti.oaqa.bio.framework.retrieval.DocumentRetrieverWrapper;
import edu.cmu.lti.oaqa.cse.basephase.ie.AbstractPassageExtractor;
import edu.cmu.lti.oaqa.framework.UimaContextHelper;
import edu.cmu.lti.oaqa.framework.data.Keyterm;
import edu.cmu.lti.oaqa.framework.data.PassageCandidate;
import edu.cmu.lti.oaqa.framework.data.RetrievalResult;

/**
 * A base passage extractor by returning the first legal span from document.
 * 
 * @author Zi Yang <ziy@cs.cmu.edu>
 * 
 */
public class FirstLegalSpanPassageExtractor extends AbstractPassageExtractor {

  private int hitListSize = 0;

  private DocumentRetrieverWrapper retriever;

  @Override
  public void initialize(UimaContext aContext) throws ResourceInitializationException {
    super.initialize(aContext);
    Integer hlsValue = (Integer) aContext.getConfigParameterValue("hit-list-size");
    if (hlsValue != null) {
      this.hitListSize = hlsValue.intValue();
    }
    String prefix = (String) aContext.getConfigParameterValue("prefix");
    boolean zipped = UimaContextHelper.getConfigParameterBooleanValue(aContext, "zipped", true);
    retriever = new DocumentRetrieverWrapper(prefix, zipped);
  }

  @Override
  protected List<PassageCandidate> extractPassages(String question, List<Keyterm> keyterms,
          List<RetrievalResult> documents) {
    List<PassageCandidate> candidates = new ArrayList<PassageCandidate>();
    int rank = 1;
    for (RetrievalResult doc : documents) {
      String text = retriever.getDocumentText(doc.getDocID());
      int begin = text.indexOf("<P>") + 3;
      if (begin < 3)
        begin = 0;
      int end = Math.min(text.indexOf("<P>", begin + 1) > 0 ? text.indexOf("<P>", begin + 1)
              : Integer.MAX_VALUE,
              text.indexOf("</P>", begin + 1) > 0 ? text.indexOf("</P>", begin + 1)
                      : Integer.MAX_VALUE);
      if (end == Integer.MAX_VALUE)
        end = text.length();
      double score = 1.0 / rank;
      try {
        PassageCandidate candidate = new PassageCandidate(doc.getDocID(), begin, end,
                (float) score, question);
        candidates.add(candidate);
      } catch (AnalysisEngineProcessException e) {
        e.printStackTrace();
      }
      if (hitListSize > 0 && rank > hitListSize) {
        break;
      }
    }
    return candidates;
  }
}
