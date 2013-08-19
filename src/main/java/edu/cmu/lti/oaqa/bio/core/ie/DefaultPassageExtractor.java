package edu.cmu.lti.oaqa.bio.core.ie;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lemurproject.indri.ParsedDocument;
import lemurproject.indri.ScoredExtentResult;

import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;

import edu.cmu.lti.oaqa.bio.core.provider.indri.IndriWrapper;
import edu.cmu.lti.oaqa.cse.basephase.ie.AbstractPassageExtractor;
import edu.cmu.lti.oaqa.framework.data.Keyterm;
import edu.cmu.lti.oaqa.framework.data.PassageCandidate;
import edu.cmu.lti.oaqa.framework.data.RetrievalResult;

/**
 * Passage extractor by retrieving relevant passage (specified by a given extend:
 * {@link #passageSpan}) from Indri search engine.
 * 
 * @author Zi Yang <ziy@cs.cmu.edu>
 * 
 */
public class DefaultPassageExtractor extends AbstractPassageExtractor {

  protected static enum PassageSpanType {
    legalspan, sentence
  };

  protected int hitListSize;

  protected int batchSize;

  protected PassageSpanType passageSpan;

  protected static IndriWrapper wrapper;

  @Override
  public void initialize(UimaContext aContext) throws ResourceInitializationException {
    super.initialize(aContext);
    try {
      this.hitListSize = (Integer) aContext.getConfigParameterValue("hit-list-size");
    } catch (ClassCastException e) {
      this.hitListSize = Integer.parseInt((String) aContext
              .getConfigParameterValue("hit-list-size"));
    }
    try {
      this.batchSize = (Integer) aContext.getConfigParameterValue("BatchSize");
    } catch (ClassCastException e) {
      this.batchSize = Integer.parseInt((String) aContext.getConfigParameterValue("BatchSize"));
    } catch (NullPointerException e) {
      this.batchSize = 50;
    }
    this.passageSpan = PassageSpanType.valueOf((String) aContext
            .getConfigParameterValue("PassageSpan"));
    String serverUrl = (String) aContext.getConfigParameterValue("server");
    Integer serverPort = (Integer) aContext.getConfigParameterValue("port");
    try {
      if (wrapper == null) {
        wrapper = new IndriWrapper(serverUrl, serverPort);
      }
    } catch (Exception e) {
      throw new ResourceInitializationException(e);
    }
  }

  @Override
  protected final List<PassageCandidate> extractPassages(String question, List<Keyterm> keyterms,
          List<RetrievalResult> documents) {
    String query = formulateQuery(keyterms);
    log(query);
    query = wrapper.removeSpecialChar(query);
    return extractPassages(query);
  }

  protected String formulateQuery(List<Keyterm> keyterms) {
    StringBuffer result = new StringBuffer();
    result.append("#combine[" + passageSpan + "](");
    for (Keyterm keyterm : keyterms)
      result.append(keyterm.getText() + " ");
    result.append(")");
    String query = result.toString();
    return query;
  }

  /**
   * @see <a href="http://www.lemurproject.org/phorum/read.php?11,4406">
   *      http://www.lemurproject.org/phorum/read.php?11,4406</a> for details about Indri's
   *      retrieval of character-based offsets (rather than term-based offsets).
   */
  protected List<PassageCandidate> extractPassages(String query) {
    List<PassageCandidate> result = new ArrayList<PassageCandidate>();
    try {
      ScoredExtentResult[] sers = wrapper.getQueryEnvironment().runQuery(query, hitListSize);
      String[] ids = wrapper.getQueryEnvironment().documentMetadata(sers, "docno");
      ParsedDocument[] texts = null;
      for (int i = 0; i < ids.length; i++) {
        if (i % batchSize == 0) {
          ScoredExtentResult[] subSers = Arrays.copyOfRange(sers, i,
                  Math.min(i + batchSize, ids.length));
          texts = wrapper.getQueryEnvironment().documents(subSers);
        }
        int begin = texts[i % batchSize].positions[sers[i].begin].begin;
        int end = texts[i % batchSize].positions[sers[i].end - 1].end;
        // TODO: A better way to get the offset of <TEXT> tag in the indexed corpus is needed.
        int offset = texts[i % batchSize].text.indexOf("<TEXT>") + 6;
        assert offset >= 6;
        // Use begin and end directly from Indri to obtain the content of extent from indexed
        // corpus, e.g., <code>texts[i % batchSize].text.substring(begin, end)</code>, but if one
        // wants to get the string from original document, i.e., the text field of indexed corpus,
        // the begin and end should be shifted by <code>offset</code>.
        PassageCandidate r = new PassageCandidate(ids[i], begin - offset, end - offset,
                (float) Math.exp(sers[i].score), query);
        result.add(r);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return result;
  }
}
