/**
 * 
 */
package edu.cmu.lti.oaqa.bio.retrieval.query.strategy;

import java.util.HashMap;
import java.util.List;

import edu.cmu.lti.oaqa.bio.framework.data.BioKeyterm;
import edu.cmu.lti.oaqa.bio.retrieval.query.structure.QueryComponent;
import edu.cmu.lti.oaqa.bio.retrieval.query.structure.QueryComponentContainer;
import edu.cmu.lti.oaqa.bio.retrieval.tools.CleanTerms;
import edu.cmu.lti.oaqa.framework.data.Keyterm;

/**
 * QueryGenerater is the class to generate different queries for different search engines.
 * 
 * @author yanfang (yanfang@cmu.edu)
 */
public class QueryGenerator {

  private List<BioKeyterm> keyTerms;
  private HashMap<String, Boolean> resourceFilter;

  public QueryGenerator(List<BioKeyterm> keyterm) {
    this.keyTerms = keyterm;
    resourceFilter = new HashMap<String, Boolean>();
  }

  /**
   * Generates the Indri query, considering boolean-filter and resouce selection.
   * 
   * @param filter Whether to use the boolean-filter in Indri or not
   * @param map The map contains all the resource selections
   * @return formed Indri query
   */
  public String generateIndriQuery(boolean filter, HashMap<String, Boolean> map) {
    return this.generateIndriQuery(filter, map, "");
  }
  
  /**
   * Generates the Indri query, considering boolean-filter, resouce selection and which field to search.
   * 
   * @param filter Whether to use the boolean-filter in Indri or not
   * @param map The map contains all the resource selections
   * @param field The field to search in Indri. Possible options are "[legalspan]", "[sentence], etc.
   * @return formed Indri query
   */
  public String generateIndriQuery(boolean filter, HashMap<String, Boolean> map, String field) {

    QueryStrategy refiner = new QueryStrategy(this.keyTerms);
    
    if(map.isEmpty()) {
      this.setDefaultResourceFilter();
      map = this.resourceFilter;
    }
    else {
      this.resourceFilter.putAll(map);
    }
    
    refiner.hasUMLS(resourceFilter.get("umls"));
    refiner.hasEntrez(resourceFilter.get("entrez"));
    refiner.hasMESH(resourceFilter.get("mesh"));
    refiner.hasLexicalVariants(resourceFilter.get("lexical_variants"));
    refiner.hasPOSTagger(resourceFilter.get("postagger"));
    refiner.hasUMLSAcronym(resourceFilter.get("acronym_umls"));
    refiner.hasEntrezAcronym(resourceFilter.get("acronym_entrez"));
    refiner.hasMESHAcronym(resourceFilter.get("acronym_mesh"));
  
    QueryComponentContainer qc = refiner.getAllQueryComponents();
    
    //System.out.println("All");
    //qc.printOut();
    //System.out.println("Concept");
    //qc.printOutConceptPart();
    //System.out.println("non-Concept");
    //qc.printOutNotConceptPart();
    
    String query = ""; // general query
    String mainPart = ""; // boolean-filter part

    for (QueryComponent q : qc.getQueryComponent()) {

      String keyterm = "";

      // wraps the keyterm if it is a phrase with "#od2". Using "2" here is based on experiment.
      keyterm = q.getKeyterm().getText().contains(" ") ? "#od2(" + q.getKeyterm().getText() + ")"
              : q.getKeyterm().getText();

      // wraps the keyterm and synonyms
      String tempMain = q.getSynonyms().isEmpty() ? " " + keyterm : " #syn( " + keyterm
              + q.getSynonymsToString("#od1(", ")") + ")";

      query = query + " " + q.getWeight() + tempMain; // the general query

      if (q.isConcept())
        mainPart = mainPart + " " + tempMain; // content in boolean-filter

    }

    // forms the query
    String s = mainPart.isEmpty() ? "#weight" + field + "( " + query + " ) " : "#filreq( #band (" + mainPart
            + ")" + "#weight" + field + "( " + query + " ) " + ")";

    // returns the general query or boolean-filter query
    if (!filter)
      return "#weight" + field + "( " + query + " ) ";
    else
      return s;
  }
  
  public static String generateIndriQuery (List<Keyterm> keyterms, String field, boolean filter) {
    String query = ""; // general query
    String mainPart = ""; // boolean-filter part

    for(Keyterm keyterm2 : keyterms) {
    
      BioKeyterm bk = (BioKeyterm) keyterm2;

      if(bk.getProbability() == 0) continue;

      String keyterm = "";

      // wraps the keyterm if it is a phrase with "#od2". Using "2" here is based on experiment.
      keyterm = bk.getText().contains(" ") ? "#od2(" + CleanTerms.removeIndriSpeCha(bk.getText()) + ")"
              : CleanTerms.removeIndriSpeCha(bk.getText());

      // wraps the keyterm and synonyms
      String tempMain = bk.getSynonymsBySource("RefinedSynonyms").isEmpty() ? " " + keyterm : " #syn( " + keyterm
              + QueryComponent.getSynonymsToString("#od1(", ")", bk.getSynonymsBySource("RefinedSynonyms")) + ")";

      if(bk.getProbability() == 1) {
     // content in boolean-filter
        mainPart = mainPart + " " + tempMain;
        query = query + " " + "0.6" + tempMain;
      }
      else
        query = query + " " + Float.toString(bk.getProbability()) + tempMain; // the general query
    }
    // forms the query
    String s = mainPart.isEmpty() ? "#weight" + field + "( " + query + " ) " : "#filreq( #band (" + mainPart
            + ")" + "#weight" + field + "( " + query + " ) " + ")";

    // returns the general query or boolean-filter query
    if (!filter)
      return "#weight" + field + "( " + query + " ) ";
    else
      return s;
    
    
    
  }
  
  
  
  private void setDefaultResourceFilter() {
    this.resourceFilter.put("umls", false);
    this.resourceFilter.put("entrez", true);
    this.resourceFilter.put("mesh", true);
    this.resourceFilter.put("lexical_variants", true);
    this.resourceFilter.put("postagger", true);
    this.resourceFilter.put("acronym_umls", true);
    this.resourceFilter.put("acronym_entrez", false);
    this.resourceFilter.put("acronym_mesh", false);
  }
  
}
