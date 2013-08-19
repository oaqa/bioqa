package edu.cmu.lti.oaqa.bio.core.ie.overlap;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;

import edu.cmu.lti.oaqa.bio.core.ie.overlap.OverlapDetector.Interval;
import edu.cmu.lti.oaqa.cse.basephase.ie.AbstractPassageUpdater;
import edu.cmu.lti.oaqa.framework.UimaContextHelper;
import edu.cmu.lti.oaqa.framework.data.Keyterm;
import edu.cmu.lti.oaqa.framework.data.PassageCandidate;
import edu.cmu.lti.oaqa.framework.data.RetrievalResult;

/**
 * A component to either merge or remove partially overlapping passages or identical passages from
 * the returned list, based on {@link PassageOverlapDetector} and {@link OverlapDetector}, specified
 * in the {@link #mode} parameter.
 * 
 * @author Zi Yang <ziy@cs.cmu.edu>
 * 
 */
public class OverlappingPassageFilter extends AbstractPassageUpdater {

  private enum FilterMode {
    FILTER_OVERLAP, FILTER_IDENTICAL, MERGE_OVERLAP
  }

  private int limit;

  private FilterMode mode;

  private int slack;

  @Override
  public void initialize(UimaContext c) throws ResourceInitializationException {
    super.initialize(c);
    limit = UimaContextHelper.getConfigParameterIntValue(c, "limit", 1000);
    mode = FilterMode.valueOf(UimaContextHelper.getConfigParameterStringValue(c, "FilterMode",
            "FILTER_OVERLAP"));
    slack = UimaContextHelper.getConfigParameterIntValue(c, "slack", 0);
  }

  @Override
  protected List<PassageCandidate> updatePassages(String question, List<Keyterm> keyterms,
          List<RetrievalResult> documents, List<PassageCandidate> passages) {
    List<PassageCandidate> filteredPassages = new ArrayList<PassageCandidate>();
    PassageOverlapDetector detector;
    switch (mode) {
      case FILTER_IDENTICAL:
        Set<PassageCandidate> passageSet = new HashSet<PassageCandidate>();
        for (PassageCandidate passage : passages) {
          if (filteredPassages.size() >= limit) {
            break;
          }
          if (!passageSet.contains(passage)) {
            filteredPassages.add(passage);
            passageSet.add(passage);
          }
        }
        return filteredPassages;
      case FILTER_OVERLAP:
        detector = new PassageOverlapDetector();
        for (PassageCandidate passage : passages) {
          if (filteredPassages.size() >= limit) {
            break;
          }
          if (detector.isNonOverlapping(passage)) {
            filteredPassages.add(passage);
            detector.addNonOverlappingPassage(passage);
          }
        }
        return filteredPassages;
      case MERGE_OVERLAP:
        detector = new PassageOverlapDetector();
        for (PassageCandidate passage : passages) {
          detector.addOverlappingPassage(passage);
        }
        for (PassageCandidate passage : passages) {
          if (filteredPassages.size() >= limit) {
            break;
          }
          List<Interval> intervals = detector.getOverlappingPassage(passage, slack);
          assert intervals.size() == 1;
          passage.setStart(intervals.get(0).begin);
          passage.setEnd(intervals.get(0).end);
          if (!filteredPassages.contains(passage)) {
            filteredPassages.add(passage);
          }
        }
      default:
        return passages;
    }
  }
}
