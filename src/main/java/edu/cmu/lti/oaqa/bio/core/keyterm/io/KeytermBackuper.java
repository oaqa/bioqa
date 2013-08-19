package edu.cmu.lti.oaqa.bio.core.keyterm.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.resource.ResourceInitializationException;

import edu.cmu.lti.oaqa.bio.framework.data.BioKeyterm;
import edu.cmu.lti.oaqa.cse.basephase.keyterm.AbstractKeytermUpdater;
import edu.cmu.lti.oaqa.framework.data.Keyterm;

public class KeytermBackuper extends AbstractKeytermUpdater {

  private File keytermDir;

  private Map<String, BioKeyterm> text2keyterm = new HashMap<String, BioKeyterm>();

  @Override
  public void initialize(UimaContext aContext) throws ResourceInitializationException {
    super.initialize(aContext);
    keytermDir = new File((String) aContext.getConfigParameterValue("KeytermDirPath"));
    if (keytermDir.exists()) {
      assert keytermDir.isDirectory();
    } else {
      keytermDir.mkdir();
    }
    File[] files = keytermDir.listFiles();
    for (File file : files) {
      file.delete();
    }
  }

  @Override
  protected List<Keyterm> updateKeyterms(String question, List<Keyterm> keyterms) {
    for (Keyterm keyterm : keyterms) {
      text2keyterm.put(keyterm.getText(), (BioKeyterm) keyterm);
    }
    return keyterms;
  }

  @Override
  public void collectionProcessComplete() throws AnalysisEngineProcessException {
    super.collectionProcessComplete();
    try {
      File keytermListFile = new File(keytermDir, UUID.randomUUID().toString());
      ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(keytermListFile));
      oos.writeObject(text2keyterm);
      oos.close();
    } catch (Exception e) {
      e.printStackTrace();
      throw new AnalysisEngineProcessException(e);
    }
  }

}
