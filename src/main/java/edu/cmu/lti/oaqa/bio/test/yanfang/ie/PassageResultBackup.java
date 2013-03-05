package edu.cmu.lti.oaqa.bio.test.yanfang.ie;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.resource.ResourceInitializationException;

import edu.cmu.lti.oaqa.cse.basephase.ie.AbstractPassageUpdater;
import edu.cmu.lti.oaqa.cse.basephase.retrieval.AbstractRetrievalUpdater;
import edu.cmu.lti.oaqa.framework.data.Keyterm;
import edu.cmu.lti.oaqa.framework.data.PassageCandidate;
import edu.cmu.lti.oaqa.framework.data.RetrievalResult;

public class PassageResultBackup extends AbstractPassageUpdater {

  private File retrievalResultDir;

  private Map<String, Map<PassageCandidate, Float>> question2passages = new HashMap<String, Map<PassageCandidate, Float>>();

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
  protected List<PassageCandidate> updatePassages(String question, List<Keyterm> keyterms,
          List<RetrievalResult> documents, List<PassageCandidate> passages) {
    Map<PassageCandidate, Float> temp = new HashMap<PassageCandidate, Float>();
    for (PassageCandidate passage : passages) {
      System.out.println(passage.getDocID());
      System.out.println(passage.getStart());
      System.out.println(passage.getProbability());
      temp.put(passage, passage.getProbability());
    }
 
    question2passages.put(question, temp);
    return passages;
  }

  @Override
  public void collectionProcessComplete() throws AnalysisEngineProcessException {
    super.collectionProcessComplete();
    try {
      File retrievalResultFile = new File(retrievalResultDir, UUID.randomUUID().toString());
      ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(retrievalResultFile));
      oos.writeObject(question2passages);
      oos.close();
    } catch (Exception e) {
      e.printStackTrace();
      throw new AnalysisEngineProcessException(e);
    }
  }

}
