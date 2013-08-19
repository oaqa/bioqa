package edu.cmu.lti.oaqa.bio.utils.retrieval.query.structure;

import java.util.ArrayList;
import java.util.List;

/**
 * QueryCompoenent is the class having the information to form one component of a query. It contains
 * <ul>
 * <li>{@link KeytermInQuery}
 * <li>synonyms of the keyterm
 * <li>is concept or not
 * </ul>
 * Some manipulations in terms of synonyms and keyterms can be found in the class as well.
 * 
 * @author yanfang (yanfang@cmu.edu)
 */
public class QueryComponent {

  private KeytermInQuery keyterm;

  private List<String> synonyms;

  private Boolean isConcept;

  private String weight = "";

  public QueryComponent() {
    this.synonyms = new ArrayList<String>();
  }

  public QueryComponent(KeytermInQuery query, boolean b) {
    this.synonyms = new ArrayList<String>();
    this.keyterm = query;
    this.isConcept = b;
  }

  public QueryComponent(KeytermInQuery query) {
    this.synonyms = new ArrayList<String>();
    this.keyterm = query;
    this.isConcept = true;
  }

  public QueryComponent(KeytermInQuery query, List<String> synAll) {
    this.synonyms = new ArrayList<String>();
    if (synAll != null)
      this.synonyms.addAll(synAll);
    this.keyterm = query;
    this.isConcept = true;
  }

  public QueryComponent(KeytermInQuery query, boolean b, List<String> synAll) {
    this.synonyms = new ArrayList<String>();
    if (synAll != null)
      this.synonyms.addAll(synAll);
    this.keyterm = query;
    this.isConcept = b;
  }

  public void setKeytermInQuery(KeytermInQuery query) {
    this.keyterm = query;
  }

  public void setConcept(boolean b) {
    this.isConcept = b;
  }

  public boolean isConcept() {
    if (isConcept == null)
      return false;
    else
      return this.isConcept;
  }

  // adds one synonym
  public void addSynonyms(String synTerm) {
    this.synonyms.add(synTerm);
  }

  // adds many synonyms at one time
  public void addAllSynonyms(List<String> synAll) {
    this.synonyms.addAll(synAll);
  }

  public void setWeight(String w) {
    this.weight = w;
  }

  public KeytermInQuery getKeyterm() {
    return this.keyterm;
  }

  public List<String> getSynonyms() {
    return this.synonyms;
  }

  /**
   * Wraps each synonym with leftWrapper and rightWrapper
   * 
   * @param leftWrapper
   * @param rightWrapper
   * @return a String of synonyms that have been wrapped
   */
  public String getSynonymsToString(String leftWrapper, String rightWrapper) {
    String result = "";
    if (this.synonyms != null) {
      for (String s : this.synonyms) {
        result = result + " " + leftWrapper + s + rightWrapper;
      }
    } else
      return "";
    return result;
  }

  public static String getSynonymsToString(String leftWrapper, String rightWrapper,
          List<String> synonyms) {
    String result = "";
    if (synonyms != null) {
      for (String s : synonyms) {
        result = result + " " + leftWrapper + s + rightWrapper;
      }
    } else
      return "";
    return result;
  }

  public String getWeight() {
    if (this.weight.isEmpty())
      return this.keyterm.getWeight();
    else
      return this.weight;
  }

  // only consider if the class has keyterm (no matter it has synonyms or not)
  public boolean isEmpty() {
    if (this.keyterm == null)
      return true;
    else
      return false;
  }

  /**
   * Replaces a String in all synonyms with a new String
   * 
   * @param a
   *          The string will be replaced
   * @param b
   *          The string will replace a
   */
  public void replaceStringInSynonyms(String a, String b) {
    ArrayList<String> newList = new ArrayList<String>();
    for (String s : this.synonyms) {
      newList.add(s.replace(a, b));
    }
    this.synonyms = newList;
  }

  /**
   * Replaces a String in all synonyms with a new String. At the same time, the original synonym
   * whose string will be replaced is left on.
   * 
   * @param a
   *          The string will be replaced
   * @param b
   *          The string will replace a
   */

  public void replaceStringKeepingOriginalInSynonyms(String a, String b) {
    ArrayList<String> newList = new ArrayList<String>();
    for (String s : this.synonyms) {
      newList.add(s);
      if (s.contains(a))
        newList.add(s.replace(a, b));
    }
    this.synonyms = newList;
  }

}
