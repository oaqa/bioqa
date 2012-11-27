package edu.cmu.lti.oaqa.bio.test.ziy.keyterm.kb;

import java.util.List;

import edu.cmu.lti.oaqa.bio.annotate.graph.ConceptBundle;
import edu.cmu.lti.oaqa.bio.annotate.umls.GraphQueryEngine;
import edu.cmu.lti.oaqa.bio.framework.data.BioKeyterm;
import edu.cmu.lti.oaqa.cse.basephase.keyterm.AbstractKeytermUpdater;
import edu.cmu.lti.oaqa.framework.data.Keyterm;


public class UmlsGraphSynonymLookupper extends AbstractKeytermUpdater {

  /**
 * @uml.property  name="lookupper"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private GraphQueryEngine lookupper = new GraphQueryEngine();

  @Override
  protected List<Keyterm> updateKeyterms(String question, List<Keyterm> keyterms) {
    for (Keyterm keyterm : keyterms) {
      ConceptBundle synonyms = lookupper.search(keyterm.getText());
      if (synonyms == null) {
        continue;
      }
      for (String synonym : synonyms.getSynonyms()) {
        ((BioKeyterm)keyterm).addSynonym(synonym, "UMLS-GRAPH");
      }
      for (String definition : synonyms.getDefinitions()) {
        ((BioKeyterm)keyterm).addConcept(definition, "UMLS-GRAPH");
      }
    }
    return keyterms;
  }

  public static void main(String[] args) {
    UmlsGraphSynonymLookupper lookupper = new UmlsGraphSynonymLookupper();
    List<Keyterm> keyterms = lookupper.updateKeyterms(null, Vocabulary.keyterms);
    for (Keyterm keyterm : keyterms) {
      for (String source : ((BioKeyterm) keyterm).getAllResourceSources()) {
        System.out.println("Keyterm > " + keyterm.getText());
        System.out.println("Concept [" + source + "] > " + ((BioKeyterm)keyterm).getConceptBySource(source));
        System.out.println("Category[" + source + "] > " + ((BioKeyterm)keyterm).getCategoryBySource(source));
        System.out.println("Synonyms[" + source + "] > " + ((BioKeyterm)keyterm).getSynonymsBySource(source));
      }
    }
  }

}
