package edu.cmu.lti.oaqa.bio.core.keyterm.pos;

import java.util.ArrayList;
import java.util.List;

import edu.cmu.lti.oaqa.bio.framework.data.BioKeyterm;
import edu.cmu.lti.oaqa.cse.basephase.keyterm.AbstractKeytermUpdater;
import edu.cmu.lti.oaqa.framework.data.Keyterm;

/**
 * A generic POS tagger wrapper.
 * 
 * @author Zi Yang <ziy@cs.cmu.edu>
 * 
 */
public abstract class AbstractPosTagger extends AbstractKeytermUpdater {

  public abstract List<String> getPosTag(List<String> tokens);

  @Override
  protected final List<Keyterm> updateKeyterms(String question, List<Keyterm> keyterms) {
    List<String> tokens = new ArrayList<String>();
    for (Keyterm keyterm : keyterms) {
      tokens.add(keyterm.getText());
    }
    List<String> tags = getPosTag(tokens);
    int i = 0;
    for (Keyterm keyterm : keyterms) {
      BioKeyterm temp = (BioKeyterm) keyterm;
      temp.addTag(tags.get(i++), getClass().getSimpleName());
      keyterm = temp;
    }
    return keyterms;
  }

}
