package edu.cmu.lti.oaqa.bio.test.ziy.ie.rank;

import java.util.List;

import edu.cmu.lti.oaqa.framework.data.Keyterm;
import edu.cmu.lti.oaqa.framework.data.PassageCandidate;
import edu.cmu.lti.oaqa.framework.data.RetrievalResult;
import edu.cmu.lti.oaqa.mergeqa.ie.AbstractPassageUpdater;

public class RandomScoreAssigner extends AbstractPassageUpdater {

  @Override
  protected List<PassageCandidate> updatePassages(String question, List<Keyterm> keyterms,
          List<RetrievalResult> documents, List<PassageCandidate> passages) {
    for (PassageCandidate passage : passages) {
      passage.setScore(Math.random());
    }
    return passages;
  }
}
