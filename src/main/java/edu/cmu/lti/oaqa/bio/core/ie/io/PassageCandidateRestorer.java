package edu.cmu.lti.oaqa.bio.core.ie.io;

import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;

import edu.cmu.lti.oaqa.cse.basephase.ie.AbstractPassageExtractor;
import edu.cmu.lti.oaqa.framework.data.Keyterm;
import edu.cmu.lti.oaqa.framework.data.PassageCandidate;
import edu.cmu.lti.oaqa.framework.data.RetrievalResult;

public class PassageCandidateRestorer extends AbstractPassageExtractor {

  private Map<String, List<PassageCandidate>> question2passages = new HashMap<String, List<PassageCandidate>>();

  @SuppressWarnings("unchecked")
  @Override
  public void initialize(UimaContext aContext) throws ResourceInitializationException {
    super.initialize(aContext);
    String passageCandidateFilePath = (String) aContext
            .getConfigParameterValue("PassageCandidateFilePath");
    try {
      ObjectInputStream oos = new ObjectInputStream(getClass().getResourceAsStream(
              passageCandidateFilePath));
      question2passages.putAll((Map<String, List<PassageCandidate>>) oos.readObject());
      oos.close();
    } catch (Exception e) {
      e.printStackTrace();
      throw new ResourceInitializationException(e);
    }
  }

  @Override
  protected List<PassageCandidate> extractPassages(String question, List<Keyterm> keyterms,
          List<RetrievalResult> documents) {
    return question2passages.get(question);
  }
}
