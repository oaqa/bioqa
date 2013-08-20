package edu.cmu.lti.oaqa.bio.framework.retrieval;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.zip.GZIPInputStream;

import org.apache.uima.cas.CAS;

/**
 * Read the GZipped XMI files. The CorpusIdentifier should be <code>GZipTRECGenomics/xmigz</code>.
 * The {@link #retrieveCAS(String, CAS)} method is migrated from
 * {@link URLDocumentRetriever#retrieveCAS(String, CAS)} .
 * <p>
 * Note that if the offset is an important attribute for the following process, Be aware that the
 * current xmi does not store the line breaks from the original file.
 * 
 * @author Zi Yang <ziy@cs.cmu.edu>
 * 
 */
public class GZipURLDocumentRetriever extends DocumentRetriever {

  public GZipURLDocumentRetriever(String prefix) {
    super(prefix);
  }

  @Override
  protected URL urlify(String docno) throws MalformedURLException {
    return new URL(prefix + "/" + docno + ".xmi.gz");
  }

  @Override
  public CAS retrieveCAS(URL url, CAS aCAS) {
    try {
      GZIPInputStream gis = new GZIPInputStream(url.openStream());
      super.retrieveCAS(gis, aCAS);
      gis.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return aCAS;
  }

}