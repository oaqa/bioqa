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

public class DocumentRetrieverWrapper {

  private DocumentRetriever retriever;

  private CAS tempCas;

  public DocumentRetrieverWrapper(String prefix, boolean gzip) {
    if (gzip) {
      this.retriever = new GZipURLDocumentRetriever(prefix);
    } else {
      this.retriever = new URLDocumentRetriever(prefix);
    }
    try {
      tempCas = CasCreationUtils.createCas(
              TypeSystemDescriptionFactory.createTypeSystemDescription(), null, null);
    } catch (ResourceInitializationException e) {
      e.printStackTrace();
    }
  }

  public String getDocumentText(String id) {
    retriever.retrieveCAS(id, tempCas);
    return tempCas.getDocumentText();
  }

  public Article getDocument(String id) {
    retriever.retrieveCAS(id, tempCas);
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
}
