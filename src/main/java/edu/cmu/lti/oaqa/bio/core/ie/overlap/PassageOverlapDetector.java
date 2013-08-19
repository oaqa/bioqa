package edu.cmu.lti.oaqa.bio.core.ie.overlap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.cmu.lti.oaqa.bio.core.ie.overlap.OverlapDetector.Interval;
import edu.cmu.lti.oaqa.framework.data.PassageCandidate;

/**
 * An extension to the {@link OverlapDetector} to also incorporate comparison between document ids
 * of passages, where each passage is represented by a (doc id, offsets) pair.
 * 
 * @author Zi Yang <ziy@cs.cmu.edu>
 * 
 */
public class PassageOverlapDetector {

  private Map<String, OverlapDetector> id2detectors = new HashMap<String, OverlapDetector>();

  public void addNonOverlappingPassage(PassageCandidate passage) {
    String id = passage.getDocID();
    if (!id2detectors.containsKey(id)) {
      id2detectors.put(id, new OverlapDetector());
    }
    id2detectors.get(id).addNonOverlappingInterval(passage.getStart(), passage.getEnd());
  }

  public boolean isNonOverlapping(PassageCandidate passage) {
    String id = passage.getDocID();
    if (!id2detectors.containsKey(id)) {
      return true;
    }
    return id2detectors.get(id).isNonOverlapping(passage.getStart(), passage.getEnd());
  }

  public void addOverlappingPassage(PassageCandidate passage) {
    String id = passage.getDocID();
    if (!id2detectors.containsKey(id)) {
      id2detectors.put(id, new OverlapDetector());
    }
    id2detectors.get(id).addOverlappingInterval(passage.getStart(), passage.getEnd());
  }

  public List<Interval> getOverlappingPassage(PassageCandidate passage, int slack) {
    String id = passage.getDocID();
    if (!id2detectors.containsKey(id)) {
      id2detectors.put(id, new OverlapDetector());
    }
    return id2detectors.get(id).getIntervalsOverlappingWith(passage.getStart(), passage.getEnd(),
            slack);
  }

}
