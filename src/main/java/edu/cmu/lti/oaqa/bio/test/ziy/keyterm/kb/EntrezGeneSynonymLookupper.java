package edu.cmu.lti.oaqa.bio.test.ziy.keyterm.kb;

import java.util.ArrayList;
import java.util.List;

import edu.cmu.lti.oaqa.bio.entrezgene_wrapper.EntrezGeneWrapper;
import edu.cmu.lti.oaqa.bio.resource_wrapper.Entity;
import edu.cmu.lti.oaqa.cse.basephase.keyterm.AbstractKeytermUpdater;
import edu.cmu.lti.oaqa.framework.data.Keyterm;
import edu.cmu.lti.oaqa.bio.framework.data.BioKeyterm;

public class EntrezGeneSynonymLookupper extends AbstractKeytermUpdater {

  private EntrezGeneWrapper lookupper = new EntrezGeneWrapper();

  @Override
  protected List<Keyterm> updateKeyterms(String question, List<Keyterm> keyterms) {
    for (Keyterm keyterm : keyterms) {
      for (Entity entity : lookupper.getEntities(keyterm.getText(), true)) {         
      //for (Entity entity : lookupper.getEntities(keyterm.getText(), true)) {  
        ((BioKeyterm) keyterm).addExternalResource(entity.getDefinition(), entity.getName(), entity.getSynonyms(), entity.getSource());
      }
    }
    return keyterms;
  }

  public static void main(String[] args) {
    EntrezGeneSynonymLookupper lookupper = new EntrezGeneSynonymLookupper();
    List<Keyterm> keyterms = lookupper.updateKeyterms(null, Vocabulary.keyterms);
    for (Keyterm keyterm : keyterms) {
      for (String source : ((BioKeyterm)keyterm).getAllResourceSources()) {
        System.out.println("Keyterm > " + keyterm.getText());
        System.out.println("Concept [" + source + "] > " + ((BioKeyterm)keyterm).getConceptBySource(source));
        System.out.println("Category[" + source + "] > " + ((BioKeyterm)keyterm).getCategoryBySource(source));
        System.out.println("Synonyms[" + source + "] > " + ((BioKeyterm)keyterm).getSynonymsBySource(source));
      }
    }
  }

}
