package edu.cmu.lti.oaqa.bio.core.keyterm.kb;

import java.util.ArrayList;
import java.util.List;

import edu.cmu.lti.oaqa.bio.framework.data.BioKeyterm;
import edu.cmu.lti.oaqa.bio.umls_wrapper.TermRelationship;
import edu.cmu.lti.oaqa.bio.umls_wrapper.UmlsTermsDAO;
import edu.cmu.lti.oaqa.bio.utils.SqlUtils;
import edu.cmu.lti.oaqa.cse.basephase.keyterm.AbstractKeytermUpdater;
import edu.cmu.lti.oaqa.framework.data.Keyterm;

/**
 * Synonym expansion from UMLS wrapper.
 * 
 * @author Zi Yang <ziy@cs.cmu.edu>
 * 
 */
public class UmlsSynonymLookupper extends AbstractKeytermUpdater {

  private UmlsTermsDAO lookupper = new UmlsTermsDAO();

  @Override
  protected List<Keyterm> updateKeyterms(String question, List<Keyterm> keyterms) {
    for (Keyterm keyterm : keyterms) {
      try {
        // TODO: Umls Wrapper should handle the escape of invalid sql queries
        ArrayList<TermRelationship> synonyms = lookupper.getTermSynonyms(
                SqlUtils.escape(keyterm.getText()), false);
        // ArrayList<TermRelationship> synonyms =
        // lookupper.getProteinGeneDiseaseSynonyms(SqlUtils.escape(keyterm
        // .getText()), false);
        for (TermRelationship relation : synonyms) {
          ((BioKeyterm) keyterm).addConcept(relation.getToTermDefinition(), relation.getSource());
          ((BioKeyterm) keyterm).addSynonym(relation.getToTerm(), relation.getSource());
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return keyterms;
  }

  public static void main(String[] args) {
    UmlsSynonymLookupper lookupper = new UmlsSynonymLookupper();
    List<Keyterm> keyterms = lookupper.updateKeyterms(null, Vocabulary.keyterms);
    for (Keyterm keyterm : keyterms) {
      for (String source : ((BioKeyterm) keyterm).getAllResourceSources()) {
        System.out.println("Keyterm > " + keyterm.getText());
        System.out.println("Concept [" + source + "] > "
                + ((BioKeyterm) keyterm).getConceptBySource(source));
        System.out.println("Category[" + source + "] > "
                + ((BioKeyterm) keyterm).getCategoryBySource(source));
        System.out.println("Synonyms[" + source + "] > "
                + ((BioKeyterm) keyterm).getSynonymsBySource(source));
      }
    }
  }

}
