package edu.cmu.lti.oaqa.bio.core.retrieval.io;

import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;

import edu.cmu.lti.oaqa.cse.basephase.retrieval.AbstractRetrievalStrategist;
import edu.cmu.lti.oaqa.framework.data.Keyterm;
import edu.cmu.lti.oaqa.framework.data.RetrievalResult;

public class RetrievalResultRestorer extends AbstractRetrievalStrategist {

  private Map<String, List<RetrievalResult>> question2documents = new HashMap<String, List<RetrievalResult>>();

  @SuppressWarnings("unchecked")
  @Override
  public void initialize(UimaContext aContext) throws ResourceInitializationException {
    super.initialize(aContext);
    String retrievalResultFilePath = (String) aContext
            .getConfigParameterValue("RetrievalResultFilePath");
    try {
      ObjectInputStream oos = new ObjectInputStream(getClass().getResourceAsStream(
              retrievalResultFilePath));
      question2documents.putAll((Map<String, List<RetrievalResult>>) oos.readObject());
      oos.close();
    } catch (Exception e) {
      e.printStackTrace();
      throw new ResourceInitializationException(e);
    }
  }

  @Override
  protected List<RetrievalResult> retrieveDocuments(String question, List<Keyterm> keyterms) {
    return question2documents.get(question);
  }

}
