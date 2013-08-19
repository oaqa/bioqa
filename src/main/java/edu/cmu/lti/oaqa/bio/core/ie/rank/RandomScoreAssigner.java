package edu.cmu.lti.oaqa.bio.core.ie.rank;

import java.util.List;

import edu.cmu.lti.oaqa.cse.basephase.ie.AbstractPassageUpdater;
import edu.cmu.lti.oaqa.framework.data.Keyterm;
import edu.cmu.lti.oaqa.framework.data.PassageCandidate;
import edu.cmu.lti.oaqa.framework.data.RetrievalResult;

/**
 * Random scores between 0 and 1 will be assigned to each passage.
 * 
 * @author Zi Yang <ziy@cs.cmu.edu>
 *
 */
public class RandomScoreAssigner extends AbstractPassageUpdater {

  @Override
  protected List<PassageCandidate> updatePassages(String question, List<Keyterm> keyterms,
          List<RetrievalResult> documents, List<PassageCandidate> passages) {
    for (PassageCandidate passage : passages) {
      passage.setProbablity((float) Math.random());
    }
    return passages;
  }
}
