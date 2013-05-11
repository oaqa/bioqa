package edu.cmu.lti.oaqa.bio.test.ziy.keyterm.kb;

import java.util.List;

import edu.cmu.lti.oaqa.bio.framework.data.BioKeyterm;
import edu.cmu.lti.oaqa.bio.mesh_wrapper.MeshWrapper;
import edu.cmu.lti.oaqa.bio.resource_wrapper.Entity;

import edu.cmu.lti.oaqa.cse.basephase.keyterm.AbstractKeytermUpdater;
import edu.cmu.lti.oaqa.framework.data.Keyterm;

public class ResourceWrapperSynonymLookupper extends AbstractKeytermUpdater {

  private MeshWrapper lookupper = new MeshWrapper();

  @Override
  protected List<Keyterm> updateKeyterms(String question, List<Keyterm> keyterms) {
    for (Keyterm keyterm : keyterms) {
      for (Entity entity : lookupper.getEntities(keyterm.getText())) {
     // for (Entity entity : lookupper.getEntities(keyterm.getText())) {
        ((BioKeyterm)keyterm).addExternalResource(entity.getDefinition(), entity.getName(), entity.getSynonyms(),
                entity.getSource());
      }
    }
    return keyterms;
  }

  public static void main(String[] args) {
    ResourceWrapperSynonymLookupper lookupper = new ResourceWrapperSynonymLookupper();
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
