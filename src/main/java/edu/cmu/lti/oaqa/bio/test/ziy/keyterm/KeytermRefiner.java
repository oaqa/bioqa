package edu.cmu.lti.oaqa.bio.test.ziy.keyterm;

import java.util.ArrayList;
import java.util.List;

import edu.cmu.lti.oaqa.bio.test.yanfang.QueryItem;
import edu.cmu.lti.oaqa.bio.test.yanfang.QueryStrategy;
import edu.cmu.lti.oaqa.framework.data.Keyterm;
import edu.cmu.lti.oaqa.mergeqa.keyterm.AbstractKeytermUpdater;

public class KeytermRefiner extends AbstractKeytermUpdater {

  @Override
  protected List<Keyterm> updateKeyterms(String question, List<Keyterm> keyterms) {
    QueryStrategy qs = new QueryStrategy();
    qs.setKeyterms(keyterms);
    qs.originalAdvancedSynonymsQuery();
    ArrayList<QueryItem> items = new ArrayList<QueryItem>();
    items.addAll(qs.getConceptPartQuery().getQueryItems());
    items.addAll(qs.getRegularPartQuery().getQueryItems());
    List<Keyterm> newKeyterms = new ArrayList<Keyterm>();
    for (QueryItem item : items) {
      Keyterm newKeyterm = new Keyterm(item.getKeyterm().getText());
      for (String synonym : item.getSynonymsList()) {
        newKeyterm.addSynonym(synonym.trim(), QueryStrategy.class.getSimpleName());
      }
      newKeyterms.add(newKeyterm);
    }
    return newKeyterms;
  }
}
