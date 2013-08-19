package edu.cmu.lti.oaqa.bio.core.keyterm.ner;

import java.util.List;

import edu.cmu.lti.oaqa.bio.framework.data.BioKeyterm;
import edu.cmu.lti.oaqa.bio.utils.retrieval.tools.BioNameLexicalVariants;
import edu.cmu.lti.oaqa.cse.basephase.keyterm.AbstractKeytermUpdater;
import edu.cmu.lti.oaqa.framework.data.Keyterm;

/**
 * Adds the lexical variants to the external resources type. The lexical variants plays the same
 * role as the synonyms in the external resource type.
 * 
 * @author yanfang (yanfang@cmu.edu)
 */
public class LexicalVariantsResolver extends AbstractKeytermUpdater {

  // TODO add lexical variants to the phrases

  @Override
  protected List<Keyterm> updateKeyterms(String question, List<Keyterm> keyterms) {

    for (Keyterm keyterm : keyterms) {
      BioKeyterm bk = (BioKeyterm) keyterm;
      // work for the single term
      if (!keyterm.getText().contains(" ")) {
        List<String> syns;
        if ((syns = BioNameLexicalVariants.getLexicalVariants(keyterm.getText())) != null) {
          bk.addExternalResource("", "", syns, "LexicalVariants");
        }
        keyterm = bk;
      }
    }
    return keyterms;
  }
}
