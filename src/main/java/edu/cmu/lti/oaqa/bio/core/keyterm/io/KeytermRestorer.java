package edu.cmu.lti.oaqa.bio.core.keyterm.io;

import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;

import edu.cmu.lti.oaqa.bio.framework.data.BioKeyterm;
import edu.cmu.lti.oaqa.cse.basephase.keyterm.AbstractKeytermUpdater;
import edu.cmu.lti.oaqa.framework.data.Keyterm;

public class KeytermRestorer extends AbstractKeytermUpdater {

  private Map<String, BioKeyterm> text2keyterm = new HashMap<String, BioKeyterm>();

  @SuppressWarnings("unchecked")
  @Override
  public void initialize(UimaContext aContext) throws ResourceInitializationException {
    super.initialize(aContext);
    String keytermFilePath = (String) aContext.getConfigParameterValue("KeytermFilePath");
    try {
      ObjectInputStream oos = new ObjectInputStream(getClass().getResourceAsStream(keytermFilePath));
      text2keyterm.putAll((Map<String, BioKeyterm>) oos.readObject());
      oos.close();
    } catch (Exception e) {
      e.printStackTrace();
      throw new ResourceInitializationException(e);
    }
  }

  @Override
  protected List<Keyterm> updateKeyterms(String question, List<Keyterm> keyterms) {
    for (Keyterm keyterm : keyterms) {
      BioKeyterm backup = text2keyterm.get(keyterm.getText());
      if (backup == null)
        continue;
      for (String source : backup.getAllResourceSources()) {
        ((BioKeyterm) keyterm).addExternalResource(backup.getConceptBySource(source),
                backup.getCategoryBySource(source), backup.getSynonymsBySource(source), source);
      }
    }
    return keyterms;
  }

}
