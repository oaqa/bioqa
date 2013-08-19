package edu.cmu.lti.oaqa.bio.utils.retrieval.tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * This class is used to clean and remove unnecessary content for terms, such as duplicated synonyms
 * and illegal characters for Indri
 * 
 * @author yanfang <yanfang@cmu.edu>
 */

public class CleanTerms {

  CheckTerms checker = new CheckTerms();

  /**
   * Gets rid of special characters that will blow up Indri
   * 
   * @param term
   *          The term probably has special characters
   * @return modified term without Indri unexpected characters
   */
  public static String removeIndriSpeCha(String term) {
    term = term.replaceAll("[\\+\\?\\(\\)\\[\\]\\,/<>\\:;]", " ");
    term = term.replace(".", "");
    term = term.replace("_", "");
    term = term.replaceAll("[\'\\\\@\\*]", "");
    return term;
  }

  /**
   * Removes the hyphens at the beginning or the end of a string
   * 
   * @param s
   *          The string has the hyphen
   * @return the string without hyphen at the beginning or the end
   */
  public static String removeBeginingEndingHyphen(String s) {
    s = s.trim();
    while (s.startsWith("-") && s.length() >= 2)
      s = s.substring(1);
    while (s.endsWith("-") && s.length() >= 2)
      s = s.substring(0, s.length() - 1);
    if (s.equals("-"))
      return "";
    return s;
  }

  /**
   * remove the duplicated synonyms, not case sensitivity
   * 
   * @param synsList
   *          the list of synonyms
   * @param original
   *          the original term in the question
   * @return new string list without duplicated synonyms
   */
  public List<String> removeDuplicatedSynonyms(String original, List<String> synsList) {

    List<String> newList = new ArrayList<String>();

    if (CleanTerms.removeIndriSpeCha(synsList.toString()).isEmpty())
      return null;

    // add the original term to the HashMap
    HashMap<String, String> hm = new HashMap<String, String>();
    String stemmedOriginal = getStemmedTerm(original.toLowerCase());

    hm.put(stemmedOriginal, original);

    for (String s : synsList) {

      // System.out.println("*****  " + s);

      boolean filter = false;
      String queryString;
      StringBuilder syns = new StringBuilder();
      String[] sList;
      s = CleanTerms.removeIndriSpeCha(s);

      if (s.length() <= 2)
        continue;
      if (s.trim().isEmpty())
        continue;
      // when the phrase has more than two terms and is part of the original term, remove it
      if (s.toLowerCase().indexOf(original.toLowerCase()) > -1 && original.trim().contains(" "))
        continue;

      sList = s.split("\\s+");
      String newS = "";

      // remove unexpected hyphens in the synonyms
      for (String s1 : sList) {
        newS = newS + " " + removeBeginingEndingHyphen(s1);

        if (CheckTerms.isAllNumeric(s1.replaceAll("-", "")))
          continue;
        else if (CheckTerms.hasNumeric(s1))
          s1 = s1.toLowerCase(); // stemming will remove all the numbers following the alphabeta,
                                 // which is not we want
        else
          s1 = getStemmedTerm(s1.toLowerCase());
        // remove the synonyms which contains the original term
        if (s1.equals(stemmedOriginal)) {
          filter = true;
          break;
        }

        syns.append(s1);
      }

      if (filter)
        continue;

      // System.out.println("****   " + syns);

      // with and withough hyphen are synonyms
      if (newS.contains("-"))
        queryString = "#syn(" + "#od1(" + newS + ")" + " #od1(" + newS.replaceAll("-", "") + ")"
                + ")";
      else
        queryString = "#od1( " + newS + " ) ";

      if (hm.get(syns.toString()) == null) {
        hm.put(syns.toString(), queryString);

        newList.add(newS);

        if (newS.contains("-"))
          newList.add(newS.replaceAll("-", ""));
      }
    }

    return newList;
  }

  /**
   * Removes the duplicated synonyms in a synonyms list, not case sensitivity.
   * 
   * @param synsList
   *          The list of synonyms
   * @param original
   *          The original term in the question
   * @return new string without duplicated synonyms
   */
  public String removeDuplicatedSynonymsToString(String original, List<String> synsList) {

    ArrayList<String> newList = (ArrayList<String>) removeDuplicatedSynonyms(original, synsList);
    String result = "";

    for (String s : newList) {
      result = result + " " + s;
    }

    return result;
  }

  /**
   * For the content in bracket, add it as the synonym.
   * 
   * @param resources
   *          the list of all the synonyms
   * @return the new synonyms list
   */
  public List<String> processBrackets(List<String> resources) {

    List<String> newResources = new ArrayList<String>();

    for (String s : resources) {
      newResources.add(s);
      if (s.contains("(") || s.contains(")")) {
        String[] split;
        split = s.split("[()]");
        if (split.length > 1) {
          if (!split[0].equals("human") && !split[0].equals("mouse")
                  && !split[0].equals("diagnosis"))
            newResources.add(split[0]);
        }
      }
    }
    return newResources;
  }

  /**
   * Gets stem for a term
   * 
   * @param str
   *          the term
   * @return stemmed term
   */
  public static String getStemmedTerm(String str) {
    Stemmer s = new Stemmer();
    for (int i = 0; i < str.length(); i++) {
      if (Character.isLetter(str.charAt(i))) {
        s.add(Character.toLowerCase(str.charAt(i)));
      }
    }
    s.stem();
    return s.toString();
  }
}
