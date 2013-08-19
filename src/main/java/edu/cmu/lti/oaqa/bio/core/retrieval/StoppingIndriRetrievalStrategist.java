package edu.cmu.lti.oaqa.bio.core.retrieval;

import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import edu.cmu.lti.oaqa.framework.data.Keyterm;

public class StoppingIndriRetrievalStrategist extends DefaultRetrievalStrategist {

  private String[] stopWordList;

  @Override
  public void initialize(UimaContext aContext) throws ResourceInitializationException {
    super.initialize(aContext);
    this.stopWordList = (String[]) aContext.getConfigParameterValue("stop-word-list");
  }

  @Override
  protected String formulateQuery(List<Keyterm> keyterms) {
    List<String> kterms = Lists.transform(keyterms, new Function<Keyterm, String>() {
      @Override
      public String apply(Keyterm keyterm) {
        return keyterm.getText();
      }
    });
    applyStopWordList(kterms);
    StringBuffer result = new StringBuffer();
    for (String keyterm : kterms) {
      result.append(keyterm + " ");
    }
    return result.toString();
  }

  private void applyStopWordList(List<String> keyterms) {
    System.out.println("applyStopWordList(): input = " + keyterms);
    for (String stopWord : stopWordList) {
      int match = keyterms.indexOf(stopWord);
      while (match > -1) {
        keyterms.remove(match);
        match = keyterms.indexOf(stopWord);
      }
    }
    System.out.println("applyStopWordList(): output = " + keyterms);
  }

}
