package edu.cmu.lti.oaqa.bio.core.ie;

import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;

import edu.cmu.lti.oaqa.cse.basephase.ie.AbstractPassageUpdater;
import edu.cmu.lti.oaqa.framework.data.Keyterm;
import edu.cmu.lti.oaqa.framework.data.PassageCandidate;
import edu.cmu.lti.oaqa.framework.data.RetrievalResult;

public class PassageResultRestore extends AbstractPassageUpdater {

  private Map<String, Map<PassageCandidate, Float>> question2passages = new HashMap<String, Map<PassageCandidate, Float>>();

  @SuppressWarnings("unchecked")
  @Override
  public void initialize(UimaContext aContext) throws ResourceInitializationException {
    super.initialize(aContext);
    String retrievalResultFilePath = (String) aContext
            .getConfigParameterValue("RetrievalResultFilePath");
    try {
      ObjectInputStream oos = new ObjectInputStream(getClass().getResourceAsStream(
              retrievalResultFilePath));
      question2passages.putAll((Map<String, Map<PassageCandidate, Float>>) oos.readObject());
      oos.close();
    } catch (Exception e) {
      e.printStackTrace();
      throw new ResourceInitializationException(e);
    }
  }

  @Override
  protected List<PassageCandidate> updatePassages(String question, List<Keyterm> keyterms,
          List<RetrievalResult> documents, List<PassageCandidate> passages) {
    // TODO Auto-generated method stub
    passages = new ArrayList<PassageCandidate>();
    for (Map.Entry<PassageCandidate, Float> pairs : question2passages.get(question).entrySet()) {
      pairs.getKey().setProbablity(pairs.getValue());
      passages.add(pairs.getKey());
    }

    return passages;
  }
}
