package edu.cmu.lti.oaqa.bio.framework.retrieval;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.uima.cas.CAS;

/**
 * Document Retriever Interface
 * 
 * Given a Docno, look up and retrieve the document
 * 
 * May be extended to do this in various ways: - From a URL - From a Database - Compressed versions
 * of the above
 */
public class URLDocumentRetriever extends DocumentRetriever {

  /**
   * @deprecated Should explicitly identify the prefix of the url, use
   *             {@link #URLDocumentRetriever(String)} instead.
   */
  @Deprecated
  public URLDocumentRetriever() {
    super();
  }

  public URLDocumentRetriever(String prefix) {
    super(prefix);
  }

  @Override
  protected URL urlify(String CorpusIdentifier, String docno) throws MalformedURLException {
    return new URL(prefix + CorpusIdentifier + "/" + docno + ".xmi");
  }

  @Override
  public CAS retrieveCAS(URL url, CAS aCAS) {
    try {
      InputStream is = url.openStream();
      super.retrieveCAS(is, aCAS);
      is.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return aCAS;
  }

}