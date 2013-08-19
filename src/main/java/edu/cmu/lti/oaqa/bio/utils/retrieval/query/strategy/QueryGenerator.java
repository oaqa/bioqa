/**
 * 
 */
package edu.cmu.lti.oaqa.bio.utils.retrieval.query.strategy;

import java.util.List;

import edu.cmu.lti.oaqa.bio.framework.data.BioKeyterm;
import edu.cmu.lti.oaqa.bio.utils.retrieval.query.structure.QueryComponent;
import edu.cmu.lti.oaqa.bio.utils.retrieval.tools.CleanTerms;
import edu.cmu.lti.oaqa.framework.data.Keyterm;

/**
 * QueryGenerater is the class to generate different queries for different search engines.
 * 
 * @author yanfang (yanfang@cmu.edu)
 */
public class QueryGenerator {

  public static String identifyAnswerType(List<Keyterm> keyterm) {
    for (Keyterm k : keyterm) {

      if (k.getText().equals("GENES")) {
        return "gene";
      }

      if (k.getText().equals("PROTEINS")) {
        return "protein";
      }
    }

    return "";
  }

  /**
   * Generates the Indri query, considering boolean-filter, resouce selection and which field to
   * search.
   * 
   * @param filter
   *          Whether to use the boolean-filter in Indri or not
   * @param map
   *          The map contains all the resource selections
   * @param field
   *          The field to search in Indri. Possible options are "[legalspan]", "[sentence], etc.
   * @return formed Indri query
   * 
   */
  public static String generateIndriQuery(List<Keyterm> keyterms, String field, boolean filter,
          String answerTypeWeight) {
    String query = ""; // general query
    String mainPart = ""; // boolean-filter part

    for (Keyterm keyterm2 : keyterms) {

      BioKeyterm bk = (BioKeyterm) keyterm2;

      if (bk.getProbability() == 0)
        continue;

      String keyterm = "";

      // wraps the keyterm if it is a phrase with "#od2". Using "2" here is based on experiment.
      keyterm = bk.getText().contains(" ") ? "#od2(" + CleanTerms.removeIndriSpeCha(bk.getText())
              + ")" : CleanTerms.removeIndriSpeCha(bk.getText());

      // wraps the keyterm and synonyms
      String tempMain = bk.getSynonymsBySource("RefinedSynonyms").isEmpty() ? " " + keyterm
              : " #syn( "
                      + keyterm
                      + QueryComponent.getSynonymsToString("#od1(", ")",
                              bk.getSynonymsBySource("RefinedSynonyms")) + ")";

      if (bk.getProbability() > 1) {
        // content in boolean-filter
        mainPart = mainPart + " " + tempMain;
        query = query + " " + Float.toString(bk.getProbability() - 1) + tempMain;
      } else if (bk.getProbability() == 1) {
        mainPart = mainPart + " " + tempMain;
        query = query + " " + "0.6" + tempMain;
      } else
        query = query + " " + Float.toString(bk.getProbability()) + tempMain; // the general query
    }

    // add answer type information into the query

    if (!answerTypeWeight.equals("0")) {
      if (identifyAnswerType(keyterms).equals("gene"))
        query = query + " " + answerTypeWeight + " #any:gene_ontology" + " ";

      if (identifyAnswerType(keyterms).equals("protein"))
        query = query + " " + answerTypeWeight + " #any:protein" + " ";
    }

    // forms the query
    String s = mainPart.isEmpty() ? "#weight" + field + "( " + query + " ) " : "#filreq( #band ("
            + mainPart + ")" + "#weight" + field + "( " + query + " ) " + ")";

    // returns the general query or boolean-filter query
    if (!filter)
      return "#weight" + field + "( " + query + " ) ";
    else
      return s;

  }

}
