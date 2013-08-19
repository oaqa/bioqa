package edu.cmu.lti.oaqa.bio.core.ie.rank;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;

import edu.cmu.lti.oaqa.bio.core.ie.ContentAwarePassageUpdater;
import edu.cmu.lti.oaqa.framework.UimaContextHelper;
import edu.cmu.lti.oaqa.framework.data.Keyterm;
import edu.cmu.lti.oaqa.framework.data.PassageCandidate;
import edu.cmu.lti.oaqa.framework.data.RetrievalResult;

/**
 * Used by [Tari:06], [Bergler:06]
 * 
 * Formula derived from "Inquirus, the NECI meta search engine" by Steve Lawrence and C. Lee Giles.
 * See http://www7.scu.edu.au/1906/com1906.htm for details. There is a typo in the paper: the c3 is
 * also a multiplier for the last term instead of a denominator. Moreover, the denominator of the
 * second term should be the combinatorial of numQueryTerms, instead of factorial of
 * (numQueryTerms-1).
 * 
 * @author Zi Yang <ziy@cs.cmu.edu>
 * 
 */
public class TermProximityRanker extends ContentAwarePassageUpdater {

  /**
   * a constant which controls the overall magnitude of R (currently in terms of the number of
   * characters, default is 100)
   */
  private double c1;

  /**
   * a constant specifying the maximum distance between query terms which is considered useful
   * (currently in terms of the number of characters, default is 5000)
   */
  private double c2;

  /**
   * a constant specifying the importance of term frequency (currently in terms of the number of
   * characters, default is 10 * 100)
   */
  private double c3;

  private boolean combineOriginalScores;

  private boolean considerSynonyms;

  private double originalWeight;

  @Override
  public void initialize(UimaContext c) throws ResourceInitializationException {
    super.initialize(c);
    // a constant which controls the overall magnitude of R (currently in terms of the number of
    // characters, default is 100 from [Lawrence:98])
    c1 = UimaContextHelper.getConfigParameterFloatValue(c, "C1", 100);
    // the maximum distance between query terms which is considered useful
    // (currently in terms of the number of characters, default is 5000 from [Lawrence:98])
    c2 = UimaContextHelper.getConfigParameterFloatValue(c, "C2", 5000);
    // the importance of term frequency (currently in terms of the number of
    // characters, default is 10 * 100 from [Lawrence:98])
    c3 = UimaContextHelper.getConfigParameterFloatValue(c, "C3", 1000);
    // false is the default value in [Tari:06] and [Bergler:06]
    considerSynonyms = UimaContextHelper.getConfigParameterBooleanValue(c, "ConsiderSynonyms",
            false);
    // false is the default value in [Tari:06] and [Bergler:06]
    combineOriginalScores = UimaContextHelper.getConfigParameterBooleanValue(c,
            "CombineOriginalScores", false);
    originalWeight = UimaContextHelper.getConfigParameterFloatValue(c, "OriginalWeight", 0.5F);
  }

  @Override
  protected List<PassageCandidate> updatePassages(String question, List<Keyterm> keyterms,
          List<RetrievalResult> documents, List<PassageCandidate> passages) {
    // collect keyterms to calculate similarities
    Map<String, Double> keytermCount = getLowerCasedKeytermCount(keyterms);
    // generate synonym to keyterm mapping
    Map<String, String> synonym2keyterm = null;
    if (considerSynonyms) {
      synonym2keyterm = getLowerCasedSynonymKeytermMapping(keyterms);
    }
    // update passages
    for (PassageCandidate passage : passages) {
      testAliveness();
      String docText = retriever.getDocumentText(passage.getDocID());
      // sanity check
      if (passage.getStart() > docText.length() - 1) {
        passage.setStart(docText.length() - 1);
      }
      if (passage.getEnd() > docText.length()) {
        passage.setEnd(docText.length());
      }
      String passageText = docText.substring(passage.getStart(), passage.getEnd());
      // List<String> originalTokens = LingPipeHmmPosTagger.tokenize(passageText);
      List<String> originalTokens = tokenize(passageText, keytermCount == null ? null
              : keytermCount.keySet(), synonym2keyterm == null ? null : synonym2keyterm.keySet());
      int[] originalCharOffsets = getCharacterOffsets(originalTokens);
      List<String> resolvedTokens = null;
      if (considerSynonyms) {
        resolvedTokens = getResolvedTokens(originalTokens, synonym2keyterm);
      } else {
        resolvedTokens = originalTokens;
      }
      Map<String, Double> tokenCount = getLowerCasedPassageTokenCount(resolvedTokens);
      double score = updatePassageScore(keytermCount, tokenCount, resolvedTokens,
              originalCharOffsets);
      if (combineOriginalScores) {
        passage.setProbablity((float) (originalWeight * passage.getProbability() + (1.0 - originalWeight)
                * score));
      } else {
        passage.setProbablity((float) score);
      }
    }
    return passages;
  }

  private double updatePassageScore(Map<String, Double> keytermCount,
          Map<String, Double> tokenCount, List<String> resolvedTokens, int[] originalCharOffsets) {
    Map<String, Double> commonCount = getCommonTerms(keytermCount, tokenCount);
    int numQueryTerms = commonCount.size();
    int totalNumQueryTerms = 0;
    for (double value : commonCount.values()) {
      totalNumQueryTerms += value;
    }
    double R;
    if (numQueryTerms > 1) {
      int sumDists = getSumOfMinDists(commonCount.keySet(), resolvedTokens, originalCharOffsets);
      R = c1 * numQueryTerms + (c2 - sumDists / combinatorial(numQueryTerms)) / (c2 / c1)
              + totalNumQueryTerms * c3;
    } else {
      R = c1 * numQueryTerms + totalNumQueryTerms * c3;
    }
    return R;
  }

  private int getSumOfMinDists(Set<String> keyterms, List<String> resolvedTokens,
          int[] originalCharOffsets) {
    int sumDists = 0;
    String[] tokenArray = keyterms.toArray(new String[0]);
    for (int i = 0; i < tokenArray.length - 1; i++) {
      for (int j = i + 1; j < tokenArray.length; j++) {
        sumDists += Math.min(
                getMinDist(tokenArray[i], tokenArray[j], resolvedTokens, originalCharOffsets), c2);
      }
    }
    return sumDists;
  }

  private int getMinDist(String token1, String token2, List<String> resolvedTokens,
          int[] originalCharOffsets) {
    int pos1 = -1, pos2 = -1;
    int minDist = Integer.MAX_VALUE;
    for (int i = 0; i < resolvedTokens.size(); i++) {
      String token = resolvedTokens.get(i).toLowerCase();
      if (token.equals(token1)) {
        pos1 = i;
        if (pos2 != -1) {
          assert pos1 > pos2;
          minDist = Math.min(minDist, originalCharOffsets[pos1] - originalCharOffsets[pos2]);
        }
      } else if (token.equals(token2)) {
        pos2 = i;
        if (pos1 != -1) {
          assert pos2 > pos1;
          minDist = Math.min(minDist, originalCharOffsets[pos2] - originalCharOffsets[pos1]);
        }
      }
    }
    return minDist;
  }

  private static Map<String, Double> getCommonTerms(Map<String, Double> m1, Map<String, Double> m2) {
    Map<String, Double> ret = new HashMap<String, Double>();
    for (String key : m1.keySet()) {
      if (m2.containsKey(key)) {
        ret.put(key, m1.get(key) * m2.get(key));
      }
    }
    return ret;
  }

  private static int combinatorial(int n) {
    return n * (n - 1) / 2;
  }

}
