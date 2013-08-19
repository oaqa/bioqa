package edu.cmu.lti.oaqa.bio.core.keyterm;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;

import edu.cmu.lti.oaqa.bio.framework.data.BioKeyterm;
import edu.cmu.lti.oaqa.cse.basephase.keyterm.AbstractKeytermExtractor;
import edu.cmu.lti.oaqa.framework.data.Keyterm;

/**
 * A simple keyterm extraction based on white spaces, with punctuations removed.
 * 
 * @author Zi Yang <ziy@cs.cmu.edu>
 * 
 */
public class DefaultKeytermExtractor extends AbstractKeytermExtractor {

  @Override
  public void initialize(UimaContext aContext) throws ResourceInitializationException {
    super.initialize(aContext);
  }

  @Override
  protected List<Keyterm> getKeyterms(String question) {

    question = question.replace('?', ' ');
    question = question.replace('(', ' ');
    question = question.replace('[', ' ');
    question = question.replace(')', ' ');
    question = question.replace(']', ' ');
    question = question.replace('/', ' ');
    question = question.replace('\'', ' ');

    String[] questionTokens = question.split("\\s+");
    List<Keyterm> keyterms = new ArrayList<Keyterm>();
    for (int i = 0; i < questionTokens.length; i++) {
      keyterms.add(new BioKeyterm(questionTokens[i], i, BioKeyterm.TOKEN));
    }

    return keyterms;
  }
}
