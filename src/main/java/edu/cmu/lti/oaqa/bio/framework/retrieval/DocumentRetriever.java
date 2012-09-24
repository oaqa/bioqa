package edu.cmu.lti.oaqa.bio.framework.retrieval;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.impl.XmiCasDeserializer;
import org.xml.sax.SAXException;

/**
 * Document Retriever Interface
 * 
 * Given a Docno, look up and retrieve the document
 * 
 * May be extended to do this in various ways: - From a URL - From a Database - Compressed versions
 * of the above
 */
public abstract class DocumentRetriever {

  /**
 * @uml.property  name="prefix"
 */
protected String prefix;

  /**
   * @deprecated Should explicitly identify the prefix of the url, use
   *             {@link #DocumentRetriever(String)} instead.
   */
  @Deprecated
  public DocumentRetriever() {
    prefix = "http://peace.isri.cs.cmu.edu:9080/";
  }

  public DocumentRetriever(String prefix) {
    this.prefix = prefix;
  }

  // docurl of the form http://peace.isri.cs.cmu.edu:9080/TRECGenomics/xmi/1234565.xmi
  protected abstract CAS retrieveCAS(URL url, CAS aCAS);

  protected abstract URL urlify(String CorpusIdentifier, String docno) throws MalformedURLException;

  // CorpusIdentifier of the form TrecGenomics/xmi
  // docno in the form of 1234565.xmi
  public CAS retrieveCAS(String CorpusIdentifier, String docno, CAS aCAS) {
    try {
      return retrieveCAS(urlify(CorpusIdentifier, docno), aCAS);
    } catch (MalformedURLException e) {
      e.printStackTrace();
    }
    return null;
  }

  protected CAS retrieveCAS(InputStream is, CAS aCAS) throws SAXException, IOException {
    XmiCasDeserializer.deserialize(is, aCAS, true);
    return aCAS;
  }
}