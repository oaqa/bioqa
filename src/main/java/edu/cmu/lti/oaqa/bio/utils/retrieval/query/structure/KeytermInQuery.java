package edu.cmu.lti.oaqa.bio.utils.retrieval.query.structure;

/**
 * KeytermInQuery is the class for the keyterms that are used to form query. It contains
 * <ul>
 * <li>the keyterm itself
 * <li>weights of the keyterm in the query
 * </ul>
 * 
 * @author yanfang (yanfang@cmu.edu)
 */
public class KeytermInQuery {

  private String keyterm;

  private String weight;

  public KeytermInQuery() {
  }

  public KeytermInQuery(String keyterm, String weight) {
    this.keyterm = keyterm;
    this.weight = weight;
  }

  public KeytermInQuery(String keyterm) {
    this.keyterm = keyterm;
    this.weight = "1.0";
  }

  public void setWeight(String w) {
    this.weight = w;
  }

  public void changeKeyterm(String k) {
    this.keyterm = k;
  }

  public String getWeight() {
    return this.weight;
  }

  public String getText() {
    return this.keyterm;
  }

}
