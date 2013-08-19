package edu.cmu.lti.oaqa.bio.utils.retrieval.tools;

import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * This class contains all the necessary check functions for terms. e.g. numeric, protein name, etc.
 * 
 * @author yanfang <yanfang@cmu.edu>
 */

public class CheckTerms {

  /**
   * Checks whether it is alphanumeric
   * 
   * @param term
   *          the key term word
   * @return <code>true</code> if it is key word, otherwise, <code>false</code>
   */
  public static boolean hasNumeric(String term) {
    for (int i = 0; i < term.length(); i++) {
      if (Character.isDigit(term.charAt(i)))
        return true;
    }
    return false;
  }

  /**
   * Checks if the string is consisted by all numeric
   * 
   * @param term
   * @return <code>true</code> if the term is combined with all numbers, otherwise,
   *         <code>false</code>
   */
  public static boolean isAllNumeric(String term) {
    for (int i = 0; i < term.length(); i++) {
      if (!Character.isDigit(term.charAt(i)))
        return false;
    }
    return true;
  }

  /**
   * Checks if the term only has upper case or numerical characters
   */
  public static boolean isOnlyUpperCaseOrNumerical(String term) {
    for (int i = 0; i < term.length(); i++) {
      if (!Character.isUpperCase(term.charAt(i)) && !Character.isDigit(term.charAt(i)))
        return false;
    }
    return true;
  }

  /**
   * Checks if the term is acronym
   */
  public static boolean isAcronym(String term) {

    // these words should be removed
    String[] notFit = { "PROTEINS", "GENES", "PATHWAYS", "BIOLOGICAL", "SUBSTANCES", "TUMOR",
        "TYPES", "DRUGS", "OR", "SYMPTOMS", "SIGNS", "MOLECULAR", "FUNCTIONS", "DISEASES",
        "ANTIBODIES", "TOXICITIES", "CELL", "TISSUE", "MUTATIONS", "gene", "role", "genes", "GENE",
        "ROLE" };

    // ignore frequency words
    for (String s : notFit) {
      if (term.equals(s))
        return false;
    }

    return isOnlyUpperCaseOrNumerical(term);
  }

  /**
   * Checks the stop word based on the stop word list.
   * 
   * @param word
   *          the word wanted to be checked
   * @return <code>true</code> if it is the stop word; <code>false</code> if it is not the stop word
   */
  public static boolean isStopwords(String word) {
    ArrayList<String> stopwordList = new ArrayList<String>();
    String stopwords = "about again all almost also although always among an and another "
            + "any are as at be because been before being between both but by can could did do does done "
            + "due during each either enough especially etc for found from further had has have having here how "
            + "however i if in into is it its itself just kg km made mainly make may mg might ml mm most mostly "
            + "must nearly neither no nor obtained of often on our overall perhaps pmid quite rather really "
            + "regarding seem seen several should show showed shown shows significantly since so some such than that "
            + "the their theirs them then there therefore these they this those through thus to upon use used "
            + "using various very was we were what when which while with within without would ";
    StringTokenizer stopwordsTokens = new StringTokenizer(stopwords);
    while (stopwordsTokens.hasMoreTokens()) {
      stopwordList.add(stopwordsTokens.nextToken());
    }
    for (String s : stopwordList) {
      if (word.equals(s))
        return true;
    }
    return false;
  }

  /**
   * Checks whether the term is the concept term or not
   * 
   * @param term
   *          the key term word
   * @return <code>true</code> when it is concept terms; <code>false</code> otherwise
   */
  public static boolean isConceptTerm(String term) {

    // if the term is the list of notFit, they should not be thought as protein/gene
    String[] notFit = { "PROTEINS", "GENES", "PATHWAYS", "BIOLOGICAL", "SUBSTANCES", "TUMOR",
        "TYPES", "DRUGS", "OR", "SYMPTOMS", "SIGNS", "MOLECULAR", "FUNCTIONS", "DISEASES",
        "ANTIBODIES", "TOXICITIES", "CELL", "TISSUE", "MUTATIONS", "gene", "role", "genes", "GENE",
        "ROLE" };

    // ignore frequency words
    for (String s : notFit) {
      if (term.equals(s))
        return false;
    }

    // if there is an uppercase and numerical in the term, treat it as the acronym of
    // protein/gene
    for (int i = 0; i < term.length(); i++) {
      if (Character.isUpperCase(term.charAt(i)))
        return true;
    }
    return false;
  }

  public static boolean isProtein(String term) {
    return isConceptTerm(term);
  }

  public static boolean isGene(String term) {
    return isConceptTerm(term);
  }
}
