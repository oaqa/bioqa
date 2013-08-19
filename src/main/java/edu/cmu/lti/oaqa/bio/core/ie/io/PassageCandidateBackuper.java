package edu.cmu.lti.oaqa.bio.core.ie.io;

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

import edu.cmu.lti.oaqa.cse.basephase.ie.AbstractPassageUpdater;
import edu.cmu.lti.oaqa.framework.data.Keyterm;
import edu.cmu.lti.oaqa.framework.data.PassageCandidate;
import edu.cmu.lti.oaqa.framework.data.RetrievalResult;

public class PassageCandidateBackuper extends AbstractPassageUpdater {

  private File passageCandidateDir;

  private Map<String, List<PassageCandidate>> question2passages = new HashMap<String, List<PassageCandidate>>();

  @Override
  public void initialize(UimaContext aContext) throws ResourceInitializationException {
    super.initialize(aContext);
    passageCandidateDir = new File(
            (String) aContext.getConfigParameterValue("PassageCandidateDirPath"));
    if (passageCandidateDir.exists()) {
      assert passageCandidateDir.isDirectory();
    } else {
      passageCandidateDir.mkdir();
    }
    File[] files = passageCandidateDir.listFiles();
    for (File file : files) {
      file.delete();
    }
  }

  @Override
  protected List<PassageCandidate> updatePassages(String question, List<Keyterm> keyterms,
          List<RetrievalResult> documents, List<PassageCandidate> passages) {
    question2passages.put(question, passages);
    return passages;
  }

  @Override
  public void collectionProcessComplete() throws AnalysisEngineProcessException {
    super.collectionProcessComplete();
    try {
      File passageCandidateFile = new File(passageCandidateDir, UUID.randomUUID().toString());
      ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(passageCandidateFile));
      oos.writeObject(question2passages);
      oos.close();
    } catch (Exception e) {
      e.printStackTrace();
      throw new AnalysisEngineProcessException(e);
    }
  }

}
