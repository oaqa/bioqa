package edu.cmu.lti.oaqa.bio.core.retrieval.io;

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

import edu.cmu.lti.oaqa.cse.basephase.retrieval.AbstractRetrievalUpdater;
import edu.cmu.lti.oaqa.framework.data.Keyterm;
import edu.cmu.lti.oaqa.framework.data.RetrievalResult;

public class RetrievalResultBackuper extends AbstractRetrievalUpdater {

  private File retrievalResultDir;

  private Map<String, List<RetrievalResult>> question2documents = new HashMap<String, List<RetrievalResult>>();

  @Override
  public void initialize(UimaContext aContext) throws ResourceInitializationException {
    super.initialize(aContext);
    retrievalResultDir = new File(
            (String) aContext.getConfigParameterValue("RetrievalResultDirPath"));
    if (retrievalResultDir.exists()) {
      assert retrievalResultDir.isDirectory();
    } else {
      retrievalResultDir.mkdir();
    }
    File[] files = retrievalResultDir.listFiles();
    for (File file : files) {
      file.delete();
    }
  }

  @Override
  protected List<RetrievalResult> updateDocuments(String question, List<Keyterm> keyterms,
          List<RetrievalResult> documents) {
    question2documents.put(question, documents);
    return documents;
  }

  @Override
  public void collectionProcessComplete() throws AnalysisEngineProcessException {
    super.collectionProcessComplete();
    try {
      File retrievalResultFile = new File(retrievalResultDir, UUID.randomUUID().toString());
      ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(retrievalResultFile));
      oos.writeObject(question2documents);
      oos.close();
    } catch (Exception e) {
      e.printStackTrace();
      throw new AnalysisEngineProcessException(e);
    }
  }

}
