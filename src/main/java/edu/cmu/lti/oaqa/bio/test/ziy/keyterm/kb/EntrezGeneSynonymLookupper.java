package edu.cmu.lti.oaqa.bio.test.ziy.keyterm.kb;

import java.util.List;

import edu.cmu.lti.oaqa.bio.entrezgene_wrapper.EntrezGeneWrapper;
import edu.cmu.lti.oaqa.bio.resource_warpper.Entity;
import edu.cmu.lti.oaqa.framework.data.Keyterm;
import edu.cmu.lti.oaqa.mergeqa.keyterm.AbstractKeytermUpdater;

public class EntrezGeneSynonymLookupper extends AbstractKeytermUpdater {

  private EntrezGeneWrapper lookupper = new EntrezGeneWrapper();

  @Override
  protected List<Keyterm> updateKeyterms(String question, List<Keyterm> keyterms) {
    for (Keyterm keyterm : keyterms) {
      for (Entity entity : lookupper.getEntities(keyterm.getText(), true)) {
        keyterm.addExternalResource(entity.getDefinition(), entity.getName(), entity.getSynonyms(),
                entity.getSource());
      }
    }
    return keyterms;
  }

  public static void main(String[] args) {
    EntrezGeneSynonymLookupper lookupper = new EntrezGeneSynonymLookupper();
    List<Keyterm> keyterms = lookupper.updateKeyterms(null, Vocabulary.keyterms);
    for (Keyterm keyterm : keyterms) {
      for (String source : keyterm.getAllResourceSources()) {
        System.out.println("Keyterm > " + keyterm.getText());
        System.out.println("Concept [" + source + "] > " + keyterm.getConceptBySource(source));
        System.out.println("Category[" + source + "] > " + keyterm.getCategoryBySource(source));
        System.out.println("Synonyms[" + source + "] > " + keyterm.getSynonymsBySource(source));
      }
    }
  }

}
