package edu.cmu.lti.oaqa.bio.test.ziy.ie;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.oaqa.model.Passage;

import edu.cmu.lti.oaqa.ecd.log.AbstractLoggedComponent;
import edu.cmu.lti.oaqa.framework.UimaContextHelper;
import edu.cmu.lti.oaqa.framework.ViewManager;
import edu.cmu.lti.oaqa.framework.ViewManager.ViewType;
import edu.cmu.lti.oaqa.framework.data.PassageCandidate;
import edu.cmu.lti.oaqa.framework.eval.passage.PassageHelper;

public class PassageCandidateAnalyzer extends AbstractLoggedComponent {

  private boolean analyzeRecall;

  private boolean analyzePrecision;

  private int hitSize;

  @Override
  public void initialize(UimaContext c) throws ResourceInitializationException {
    super.initialize(c);
    analyzeRecall = UimaContextHelper.getConfigParameterBooleanValue(c, "AnalyzeRecall", true);
    analyzePrecision = UimaContextHelper.getConfigParameterBooleanValue(c, "AnalyzePrecision",
            false);
    hitSize = UimaContextHelper.getConfigParameterIntValue(c, "AnalyzeHitSize", 1000);
  }

  @Override
  public final void process(JCas jcas) throws AnalysisEngineProcessException {
    super.process(jcas);
    try {
      // collect gold standard
      List<Passage> gs = PassageHelper.loadDocumentSet(ViewManager.getView(jcas,
              ViewType.DOCUMENT_GS));
      List<PassageCandidate> gsPassages = new ArrayList<PassageCandidate>();
      for (Passage passage : gs) {
        gsPassages.add(new PassageCandidate(passage));
      }
      Map<String, List<PassageCandidate>> id2gsPassages = new HashMap<String, List<PassageCandidate>>();
      for (PassageCandidate gsPassage : gsPassages) {
        String id = gsPassage.getDocID();
        if (!id2gsPassages.containsKey(id)) {
          id2gsPassages.put(id, new ArrayList<PassageCandidate>());
        }
        id2gsPassages.get(id).add(gsPassage);
      }
      // collect extracted passages
      
      List<PassageCandidate> candidates = JCasHelper.getPassages(ViewManager.getCandidateView(jcas));
      Map<String, List<PassageCandidate>> id2candidates = new HashMap<String, List<PassageCandidate>>();
      for (PassageCandidate candidate : candidates) {
        String id = candidate.getDocID();
        if (!id2candidates.containsKey(id)) {
          id2candidates.put(id, new ArrayList<PassageCandidate>());
        }
        id2candidates.get(id).add(candidate);
      }
      // analyze recall
      if (analyzeRecall) {
        System.out.println("Recall analysis: total of gold-standard passages:" + gsPassages.size());
        for (PassageCandidate gsPassage : gsPassages) {
          System.out.println(" - " + gsPassage + ":\t");
          String id = gsPassage.getDocID();
          if (!id2candidates.containsKey(id)) {
            System.out.println("\t> document not retrieved");
            continue;
          }
          boolean found = false;
          int maxLength = Integer.MIN_VALUE;
          for (PassageCandidate candidate : id2candidates.get(id)) {
            int length = getOverlapLength(gsPassage, candidate);
            if (length > maxLength) {
              maxLength = length;
            }
            if (length > 0) {
              found = true;
              System.out.println("\t> overlapped passage " + candidate + " found at rank "
                      + candidate.getRank() + ": " + length + " (candidate: "
                      + (candidate.getEnd() - candidate.getStart()) + ", gs: "
                      + (gsPassage.getEnd() - gsPassage.getStart()) + ")");
            }
          }
          if (!found) {
            System.out.println("\t> document retrieved but not overlapped: " + maxLength);
          }
        }
      }
      // analyze precision
      if (analyzePrecision) {
        candidates = candidates.subList(0, Math.min(candidates.size(), hitSize));
        System.out.println("Precision analysis: total of extracted passages:" + candidates.size());
        for (PassageCandidate candidate : candidates) {
          System.out.println(" - " + candidate + ":\t");
          String id = candidate.getDocID();
          if (!id2gsPassages.containsKey(id)) {
            System.out.println("\t> is irrelevant");
            continue;
          }
          boolean found = false;
          int maxLength = Integer.MIN_VALUE;
          PassageCandidate nearestGs = null;
          for (PassageCandidate gsPassage : id2gsPassages.get(id)) {
            int length = getOverlapLength(gsPassage, candidate);
            if (length > maxLength) {
              maxLength = length;
              nearestGs = gsPassage;
            }
            if (length > 0) {
              found = true;
              System.out.println("\t> relevant passage " + gsPassage + " ranked "
                      + candidate.getRank() + ": " + length + " (candidate: "
                      + (candidate.getEnd() - candidate.getStart()) + ", gs: "
                      + (gsPassage.getEnd() - gsPassage.getStart()) + ")");
            }
          }
          if (!found) {
            System.out.println("\t> document relevant but passage " + nearestGs + " not: "
                    + maxLength);
          }
        }
      }
    } catch (CASException e) {
      throw new AnalysisEngineProcessException(e);
    }
  }

  /*
   * Return Integer.MIN_VALUE if p1 and p2 come from different documents, positive value if p1 and
   * p2 are overlapped, and the value is the number of overlapped length, negative (or 0) value if
   * p1 and p2 are not overlapped, and the absolute value is the distance between two passages.
   */
  private static int getOverlapLength(PassageCandidate p1, PassageCandidate p2) {
    // different documents
    if (!p1.getDocID().equals(p2.getDocID())) {
      return Integer.MIN_VALUE;
    }
    // overlapped passages
    if (p1.getStart() < p2.getEnd() && p1.getEnd() > p2.getStart()) {
      return Math.min(p1.getEnd(), p2.getEnd()) - Math.max(p1.getStart(), p2.getStart());
    }
    // non-overlapped passages
    return -Math.min(Math.abs(p1.getStart() - p2.getEnd()), Math.abs(p1.getEnd() - p2.getStart()));
  }

}
