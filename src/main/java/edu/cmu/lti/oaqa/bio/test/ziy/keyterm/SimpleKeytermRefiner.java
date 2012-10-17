package edu.cmu.lti.oaqa.bio.test.ziy.keyterm;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.cmu.lti.oaqa.framework.data.Keyterm;
import edu.cmu.lti.oaqa.mergeqa.keyterm.AbstractKeytermUpdater;

public class SimpleKeytermRefiner extends AbstractKeytermUpdater {

  private static String[] stoplist = {
      "what",
      "how",
      "does",
      "is",
      "the",
      "of",
      "in",
      "do",
      "s",
      "and",
      "to",
      "or",
      "with",
      "disease",
      "role" };

  private static Set<String> stopwords = new HashSet<String>();

  static {
    for (String stopword : stoplist) {
      stopwords.add(stopword);
    }
  }

  @Override
  protected List<Keyterm> updateKeyterms(String question, List<Keyterm> keyterms) {
    List<Keyterm> newKeyterms = new ArrayList<Keyterm>();
    for (Keyterm keyterm : keyterms) {
      if (!keyterm.isToken()) {
        continue;
      }
      String text = keyterm.getText();
      if (stopwords.contains(text.toLowerCase())) {
        continue;
      }
      Keyterm newKeyterm = new Keyterm(text, keyterm.getSequenceId(), keyterm.getType());
      if (text.matches("[A-Z0-9\\-]+")) {
        String umlsName = null;
        for (String source : keyterm.getAllResourceSources()) {
          if (source.toLowerCase().startsWith("umls")) {
            umlsName = source;
          }
        }
        if (umlsName != null) {
          for (String synonym : keyterm.getSynonymsBySource(umlsName)) {
            newKeyterm.addSynonym(synonym, "UMLS");
          }
        }
      } else {
        for (String synonym : keyterm.getSynonymsBySource("MeSH")) {
          newKeyterm.addSynonym(synonym, "MeSH");
        }
        for (String synonym : keyterm.getSynonymsBySource("EntrezGene")) {
          newKeyterm.addSynonym(synonym, "EntrezGene");
        }
      }
      newKeyterms.add(newKeyterm);
    }
    return newKeyterms;
  }
}
