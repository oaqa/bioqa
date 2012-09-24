package edu.cmu.lti.oaqa.bio.framework.retrieval;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.CASRuntimeException;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.CasCreationUtils;
import org.uimafit.factory.TypeSystemDescriptionFactory;

import edu.cmu.lti.bio.Sentence;
import edu.cmu.lti.bio.trec.LegalSpan;
import edu.cmu.lti.oaqa.framework.data.Article;
import edu.cmu.lti.oaqa.framework.data.TextSpan;

public class DocumentRetrieverWrapper {

  /**
 * @uml.property  name="retriever"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private DocumentRetriever retriever;

  /**
 * @uml.property  name="prefix"
 */
private String prefix;

  /**
 * @uml.property  name="tempCas"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private CAS tempCas;

  public DocumentRetrieverWrapper(boolean gzip, boolean annotated) {
    this("http://peace.isri.cs.cmu.edu:9080/", gzip, annotated);
  }

  public DocumentRetrieverWrapper(String url, boolean gzip, boolean annotated) {
    if (gzip) {
      this.retriever = new GZipURLDocumentRetriever(url);
      if (annotated) {
        prefix = "GZipTRECGenomics/annotated_xmigz";
      } else {
        prefix = "GZipTRECGenomics/xmigz";
      }
    } else {
      this.retriever = new URLDocumentRetriever(url);
      if (annotated) {
        prefix = "TRECGenomics/Trec06_annotated_xmi";
      } else {
        prefix = "TRECGenomics/xmi";
      }
    }
    try {
      tempCas = CasCreationUtils.createCas(
              TypeSystemDescriptionFactory.createTypeSystemDescription(), null, null);
    } catch (ResourceInitializationException e) {
      e.printStackTrace();
    }
  }

  public String getDocumentText(String id) {
    retriever.retrieveCAS(prefix, id, tempCas);
    return tempCas.getDocumentText();
  }

  public Article getDocument(String id) {
    retriever.retrieveCAS(prefix, id, tempCas);
    Article article = new Article(id, tempCas.getDocumentText());
    try {
      AnnotationIndex<Annotation> legalspans = tempCas.getJCas().getAnnotationIndex(LegalSpan.type);
      for (Annotation legalspan : legalspans) {
        article.addLegalSpan(legalspan.getBegin(), legalspan.getEnd());
      }
      AnnotationIndex<Annotation> sentences = tempCas.getJCas().getAnnotationIndex(Sentence.type);
      for (Annotation sentence : sentences) {
        article.addSentence(sentence.getBegin(), sentence.getEnd());
      }
      // TODO: richer annotation should be read to a data model.
    } catch (CASRuntimeException e) {
      e.printStackTrace();
    } catch (CASException e) {
      e.printStackTrace();
    }
    return article;
  }

  public void releaseCas() {
    tempCas.release();
  }

  public static void main(String[] args) {
    String id = "11925436";
    DocumentRetrieverWrapper drw = new DocumentRetrieverWrapper(true, true);
    String text = drw.getDocumentText(id);
    System.out.println(text);
    drw = new DocumentRetrieverWrapper("file:/Users/elmer/Downloads/", true, true);
    Article article = drw.getDocument(id);
    System.out.println(article.getLegalSpans());
    for (TextSpan sentence : article.getSentences()) {
      System.out.println(sentence + " " + article.isSpanLegal(sentence) + " "
              + article.getSpanText(sentence).replaceAll("[\\n\\r]", " "));
    }
  }
}
