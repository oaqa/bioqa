package edu.cmu.lti.oaqa.bio.framework.retrieval;

import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.CasMultiplier_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.AbstractCas;
import org.apache.uima.cas.CAS;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.oaqa.model.Passage;

import edu.cmu.lti.oaqa.framework.ViewManager;
import edu.cmu.lti.oaqa.framework.ViewManager.ViewType;
import edu.cmu.lti.oaqa.framework.eval.passage.PassageHelper;

public class DocumentRetrievalAnnotator extends CasMultiplier_ImplBase {

  private String corpusId;

  private String url;

  public boolean hasNext() {
    return false;
  }

  public AbstractCas next() {
    return null;
  }

  public void initialize(UimaContext aContext) throws ResourceInitializationException {
    super.initialize(aContext);
    this.corpusId = (String) aContext.getConfigParameterValue("corpusId");
  }

  @Override
  public void process(CAS aCas) throws AnalysisEngineProcessException {
    try {
      JCas jcas = aCas.getJCas();
      JCas gsView = ViewManager.getView(jcas, ViewType.DOCUMENT_GS);
      List<Passage> gs = PassageHelper.loadDocumentSet(gsView);
      DocumentRetriever docRetriever = new URLDocumentRetriever(url);
      for (Passage passage : gs) {
        CAS tempCas = getEmptyCAS();
        try {
          docRetriever.retrieveCAS(corpusId, passage.getUri(), tempCas);
          String text = tempCas.getDocumentText().substring(passage.getBegin(),
                  passage.getEnd() + 1);
          passage.setText(text);
        } finally {
          tempCas.release();
        }
      }
      System.out.println("RETRIEVED: " + gs.size());
    } catch (Exception e) {
      e.printStackTrace();
      // throw new AnalysisEngineProcessException( e );
    }
  }

}