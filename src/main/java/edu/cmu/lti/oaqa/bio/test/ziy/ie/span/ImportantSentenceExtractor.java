package edu.cmu.lti.oaqa.bio.test.ziy.ie.span;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.resource.ResourceInitializationException;

import similarity.Similarity;
import edu.cmu.lti.oaqa.bio.test.ziy.ie.ContentAwarePassageUpdater;
import edu.cmu.lti.oaqa.framework.UimaContextHelper;
import edu.cmu.lti.oaqa.framework.data.Article;
import edu.cmu.lti.oaqa.framework.data.Keyterm;
import edu.cmu.lti.oaqa.framework.data.PassageCandidate;
import edu.cmu.lti.oaqa.framework.data.RetrievalResult;
import edu.cmu.lti.oaqa.framework.data.TextSpan;

/**
 * Used by [Tari:06]
 * 
 * @author Zi Yang <ziy@cs.cmu.edu>
 * 
 */
public class ImportantSentenceExtractor extends ContentAwarePassageUpdater {

  private int maxNumSentInPassage;

  private boolean considerSynonyms;

  private int maxNumPassageInParagragh;

  private double importantSentSimThreshold;

  private Similarity importantSentSim;

  private double neighborSentSimThreshold;

  private Similarity neighborSentSim;

  private enum SentenceType {
    important, neighbor, other
  };

  private boolean countReplicated = false;

  @Override
  public void initialize(UimaContext c) throws ResourceInitializationException {
    super.initialize(c);
    // 3 is the default value in [Tari:06]
    maxNumSentInPassage = UimaContextHelper.getConfigParameterIntValue(c, "MaxNumSentInPassage", 3);
    // false is the default value in [Tari:06]
    considerSynonyms = UimaContextHelper.getConfigParameterBooleanValue(c, "ConsiderSynonyms",
            false);
    // 5 is the default value in [Tari:06]
    maxNumPassageInParagragh = UimaContextHelper.getConfigParameterIntValue(c,
            "MaxNumPassageInParagraph", 5);
    // 0.5 is the default value in [Tari:06] (Sentences that have at least half of the keywords in
    // the article)
    importantSentSimThreshold = UimaContextHelper.getConfigParameterFloatValue(c,
            "ImportantSentSimThreshold", 0.5F);
    importantSentSim = (Similarity) UimaContextHelper.getConfigParameterClassInstance(c,
            "ImportantSentSim", "similarity.ModifiedOverlapCoefficient");
    // 1 is the default value in [Tari:06] (Neighboring sentences of the important sentences with at
    // least one keyword are merged to form a passage.)
    neighborSentSimThreshold = UimaContextHelper.getConfigParameterFloatValue(c,
            "NeighborSentSimThreshold", 1F);
    neighborSentSim = (Similarity) UimaContextHelper.getConfigParameterClassInstance(c,
            "NeighborSentSim", "similarity.MatchingCoefficient");
  }

  @Override
  protected List<PassageCandidate> updatePassages(String question, List<Keyterm> keyterms,
          List<RetrievalResult> documents, List<PassageCandidate> passages) {
    // collect keyterms to calculate similarities
    Map<String, Double> keytermCount = countReplicated ? getLowerCasedKeytermCount(keyterms)
            : getLowerCasedKeytermTypes(keyterms);
    // generate synonym to keyterm mapping
    Map<String, String> synonym2keyterm = null;
    if (considerSynonyms) {
      synonym2keyterm = getLowerCasedSynonymKeytermMapping(keyterms);
    }
    // System.out.println(keytermCount);
    // System.out.println(synonym2keyterm);
    // generate new passages
    List<PassageCandidate> newPassages = new ArrayList<PassageCandidate>();
    for (PassageCandidate passage : passages) {
      for (PassageCandidate newPassage : extractImportantSentences(keytermCount, passage,
              synonym2keyterm)) {
        testAliveness();
        if (!newPassages.contains(newPassage)) {
          newPassages.add(newPassage);
        }
      }
    }
    return newPassages;
  }

  private List<PassageCandidate> extractImportantSentences(Map<String, Double> keytermCount,
          PassageCandidate passage, Map<String, String> synonym2keyterm) {
    Article article = retriever.getDocument(passage.getDocID());
    // sanity check
    if (passage.getStart() > article.getText().length() - 1) {
      passage.setStart(article.getText().length() - 1);
    }
    if (passage.getEnd() > article.getText().length()) {
      passage.setEnd(article.getText().length());
    }
    // get all sentences from the retrieved paragraph
    TextSpan origSpan = new TextSpan(passage.getStart(), passage.getEnd());
    /*
     * List<TextSpan> sentences = new ArrayList<TextSpan>(); for (TextSpan sentence :
     * article.getSentences()) { if (TextSpan.getOverlapLength(origSpan, sentence) > 0) {
     * sentences.add(TextSpan.getOverlapTextSpan(origSpan, sentence)); } }
     */
    List<TextSpan> sentences = LingPipeSentenceChunker.split(article.getSpanText(origSpan),
            origSpan.begin);
    // identify important sentences and neighboring sentences
    List<SentenceType> types = new ArrayList<SentenceType>();
    List<Double> sims = new ArrayList<Double>();
    /* int importantNum = 0, neighborNum = 0; */
    for (TextSpan sentence : sentences) {
      testAliveness();
      // List<String> originalTokens = LingPipeHmmPosTagger.tokenize(article.getSpanText(sentence));
      List<String> originalTokens = tokenize(article.getSpanText(sentence),
              keytermCount == null ? null : keytermCount.keySet(), synonym2keyterm == null ? null
                      : synonym2keyterm.keySet());
      List<String> resolvedTokens = null;
      if (considerSynonyms) {
        resolvedTokens = getResolvedTokens(originalTokens, synonym2keyterm);
      } else {
        resolvedTokens = originalTokens;
      }
      Map<String, Double> tokenCount = countReplicated ? getLowerCasedPassageTokenCount(resolvedTokens)
              : getLowerCasedPassageTokenTypes(resolvedTokens);
      double sim = 0;
      if ((sim = importantSentSim.getSimilarity((HashMap<String, Double>) tokenCount,
              (HashMap<String, Double>) keytermCount)) >= importantSentSimThreshold) {
        types.add(SentenceType.important);
        /* importantNum++; */
      } else if ((sim = neighborSentSim.getSimilarity((HashMap<String, Double>) tokenCount,
              (HashMap<String, Double>) keytermCount)) >= neighborSentSimThreshold) {
        types.add(SentenceType.neighbor);
        /* neighborNum++; */
      } else {
        types.add(SentenceType.other);
      }
      sims.add(sim);
      // System.out.println(passage + " " + sentence);
      // System.out.println(tokenCount);
      // System.out.println(originalTokens);
      // System.out.println(resolvedTokens);
      // System.out.println(types.get(types.size()-1));
    }
    /*
     * System.out.println("Found " + importantNum + " important sentences and " + neighborNum +
     * " neighboring sentences.");
     */
    // extract new passages
    List<PassageCandidate> newPassages = new ArrayList<PassageCandidate>();
    List<TextSpan> passageStack = new ArrayList<TextSpan>();
    for (int i = 0; i < types.size(); i++) {
      testAliveness();
      if (types.get(i) != SentenceType.important) {
        continue;
      }
      passageStack.add(sentences.get(i));
      boolean backward = true, forward = true;
      int skip = 0;
      for (int j = 1; j < maxNumSentInPassage; j++) {
        if (i - j < 0 || types.get(i - j) == SentenceType.other) {
          // if (i - j < 0 || types.get(i - j) != SentenceType.neighbor) {
          backward = false;
        }
        if (backward) {
          passageStack.add(sentences.get(i - j));
        }
        if (i + j >= types.size() || types.get(i + j) == SentenceType.other) {
          // if (i + j >= types.size() || types.get(i + j) != SentenceType.neighbor) {
          forward = false;
        }
        if (forward) {
          passageStack.add(sentences.get(i + j));
          skip = j;
        }
      }
      i += skip;
      TextSpan newSpan = TextSpan.getBoundingTextSpan(passageStack.subList(0,
              Math.min(passageStack.size(), maxNumSentInPassage)));
      try {
        newPassages.add(new PassageCandidate(passage.getDocID(), newSpan.begin, newSpan.end, sims
                .get(i).floatValue(), passage.getQueryString()));
      } catch (AnalysisEngineProcessException e) {
        e.printStackTrace();
      }
      passageStack.clear();
    }
    // limit the number of new passages from the paragraph
    Collections.sort(newPassages, Collections.reverseOrder());
    newPassages = newPassages.subList(0, Math.min(newPassages.size(), maxNumPassageInParagragh));
    for (PassageCandidate newPassage : newPassages) {
      newPassage.setProbablity(passage.getProbability());
    }
    return newPassages;
  }

}
