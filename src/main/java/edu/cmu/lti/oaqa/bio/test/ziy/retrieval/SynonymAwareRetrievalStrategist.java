package edu.cmu.lti.oaqa.bio.test.ziy.retrieval;

import java.util.List;

import edu.cmu.lti.oaqa.bio.core.retrieval.DefaultRetrievalStrategist;
import edu.cmu.lti.oaqa.bio.framework.data.BioKeyterm;
import edu.cmu.lti.oaqa.framework.data.Keyterm;

public class SynonymAwareRetrievalStrategist extends DefaultRetrievalStrategist {

  @Override
  protected String formulateQuery(List<Keyterm> keyterms) {
    StringBuffer queryBuffer = new StringBuffer();
    queryBuffer.append("#combine(");
    for (Keyterm keyterm : keyterms) {
      BioKeyterm bioKeyterm = (BioKeyterm) keyterm;
      System.out.println(bioKeyterm.getTags());
      if (bioKeyterm.isToken()) {
        queryBuffer.append(escape(keyterm.getText())).append(" ");
      } else if (bioKeyterm.isPhrase()) {
        queryBuffer.append("#1(").append(escape(keyterm.getText())).append(") ");
      }
      for (String synonym : bioKeyterm.getSynonyms()) {
        queryBuffer.append("#1(").append(escape(synonym)).append(") ");
      }
    }
    queryBuffer.append(")");
    String query = queryBuffer.toString();
    return query;
  }

  /**
   * Solve the issue of
   * <code>../src/QueryEnvironment.cpp(874): Couldn't understand this query: NoViableAlt</code>
   * 
   * It replaces all unsafe characters with space according to the safe character list provided from
   * the developers. Moreover, it removes dash at either the beginning or the end of the query
   * string or next to a space.
   * 
   * @see <a
   *      href="http://www.lemurproject.org/phorum/read.php?11,2592">http://www.lemurproject.org/phorum/read.php?11,2592</a>
   * @param str
   * @return
   */
  private static String escape(String str) {
    String ret = str.replaceAll("[^a-zA-Z0-9-_]+", " ");
    ret = ret.replaceAll("-* +-*", " ");
    ret = ret.replaceAll("^-+", "");
    ret = ret.replaceAll("-+$", "");
    return ret.trim();
  }

}
