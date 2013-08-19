package edu.cmu.lti.oaqa.bio.core.retrieval;

import java.util.ArrayList;
import java.util.List;

import lemurproject.indri.ScoredExtentResult;

import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;

import edu.cmu.lti.oaqa.bio.core.provider.indri.IndriWrapper;
import edu.cmu.lti.oaqa.cse.basephase.retrieval.AbstractRetrievalStrategist;
import edu.cmu.lti.oaqa.framework.data.Keyterm;
import edu.cmu.lti.oaqa.framework.data.RetrievalResult;

/**
 * Default retrieval strategist based on Indri search engine.
 * 
 * @author Zi Yang <ziy@cs.cmu.edu>
 * 
 */
public class DefaultRetrievalStrategist extends AbstractRetrievalStrategist {

  protected int hitListSize;

  protected static IndriWrapper wrapper;

  @Override
  public void initialize(UimaContext aContext) throws ResourceInitializationException {
    super.initialize(aContext);
    try {
      this.hitListSize = (Integer) aContext.getConfigParameterValue("hit-list-size");
    } catch (ClassCastException e) { // all cross-opts are strings?
      this.hitListSize = Integer.parseInt((String) aContext
              .getConfigParameterValue("hit-list-size"));
    }
    String serverUrl = (String) aContext.getConfigParameterValue("server");
    Integer serverPort = (Integer) aContext.getConfigParameterValue("port");
    try {
      if (wrapper == null) {
        wrapper = new IndriWrapper(serverUrl, serverPort);
      }
    } catch (Exception e) {
      throw new ResourceInitializationException(e);
    }
  }

  public final List<RetrievalResult> retrieveDocuments(String questionText, List<Keyterm> keyterms) {
    String query = formulateQuery(keyterms);
    log(query);
    query = wrapper.removeSpecialChar(query);
    return retrieveDocuments(query);
  };

  protected String formulateQuery(List<Keyterm> keyterms) {
    StringBuffer result = new StringBuffer();
    for (Keyterm keyterm : keyterms)
      result.append(keyterm.getText() + " ");
    String query = result.toString();
    return query;
  }

  protected List<RetrievalResult> retrieveDocuments(String query) {
    List<RetrievalResult> result = new ArrayList<RetrievalResult>();
    try {
      ScoredExtentResult[] sers = wrapper.getQueryEnvironment().runQuery(query, hitListSize);
      String[] ids = wrapper.getQueryEnvironment().documentMetadata(sers, "docno");
      for (int i = 0; i < ids.length; i++) {
        RetrievalResult r = new RetrievalResult(ids[i], (float) Math.exp(sers[i].score), query);
        result.add(r);
      }
    } catch (Exception e) {
      System.err.println("Error retrieving documents from Indri: " + e);
    }
    return result;
  }

}
