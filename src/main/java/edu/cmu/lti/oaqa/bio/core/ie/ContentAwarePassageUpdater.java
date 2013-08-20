package edu.cmu.lti.oaqa.bio.core.ie;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;

import util.SimilarityUtils;
import edu.cmu.lti.oaqa.bio.core.keyterm.pos.LingPipeHmmPosTagger;
import edu.cmu.lti.oaqa.bio.framework.data.BioKeyterm;
import edu.cmu.lti.oaqa.bio.framework.retrieval.DocumentRetrieverWrapper;
import edu.cmu.lti.oaqa.cse.basephase.ie.AbstractPassageUpdater;
import edu.cmu.lti.oaqa.framework.UimaContextHelper;
import edu.cmu.lti.oaqa.framework.data.Keyterm;

/**
 * A passage extractor with static methods provided to process retrieved texts and enabling data
 * source of the contents specified from a configuration file.
 * 
 * @author Zi Yang <ziy@cs.cmu.edu>
 * 
 */
public abstract class ContentAwarePassageUpdater extends AbstractPassageUpdater {

  protected DocumentRetrieverWrapper retriever;

  @Override
  public void initialize(UimaContext c) throws ResourceInitializationException {
    super.initialize(c);
    String prefix = (String) c.getConfigParameterValue("prefix");
    boolean zipped = UimaContextHelper.getConfigParameterBooleanValue(c, "zipped", true);
    retriever = new DocumentRetrieverWrapper(prefix, zipped);
  }

  public static Map<String, Double> getLowerCasedKeytermCount(List<Keyterm> keyterms) {
    List<String> keytermStrs = new ArrayList<String>();
    for (Keyterm keyterm : keyterms) {
      // only consider the keyterms whose weights >= 0.4
      // this is only for TREC 2006
      // this can be set as a parameter
      // TODO
      if (keyterm.getProbability() >= 0.4)
        keytermStrs.add(keyterm.getText().toLowerCase());
    }
    return SimilarityUtils.countWord(keytermStrs.toArray(new String[0]));
  }

  public static Map<String, Double> getLowerCasedKeytermCount(List<Keyterm> keyterms,
          float threshold) {
    List<String> keytermStrs = new ArrayList<String>();
    for (Keyterm keyterm : keyterms) {
      // only consider the keyterms whose weights >= 0.4
      // this is only for TREC 2006
      // this can be set as a parameter
      // TODO
      if (keyterm.getProbability() >= threshold)
        keytermStrs.add(keyterm.getText().toLowerCase());
    }
    return SimilarityUtils.countWord(keytermStrs.toArray(new String[0]));
  }

  public static Map<String, Double> getLowerCasedKeytermTypes(List<Keyterm> keyterms,
          float threshold) {
    Map<String, Double> keytermCount = getLowerCasedKeytermCount(keyterms, threshold);
    for (String keyterm : keytermCount.keySet()) {
      keytermCount.put(keyterm, 1.0);
    }
    return keytermCount;
  }

  public static Map<String, Double> getLowerCasedKeytermTypes(List<Keyterm> keyterms) {
    Map<String, Double> keytermCount = getLowerCasedKeytermCount(keyterms);
    for (String keyterm : keytermCount.keySet()) {
      keytermCount.put(keyterm, 1.0);
    }
    return keytermCount;
  }

  public static Map<String, Double> getLowerCasedPassageTokenCount(List<String> tokens) {
    List<String> lowerCasedTokens = new ArrayList<String>();
    for (String token : tokens) {
      lowerCasedTokens.add(token.toLowerCase());
    }
    return SimilarityUtils.countWord(lowerCasedTokens.toArray(new String[0]));
  }

  public static Map<String, Double> getLowerCasedPassageTokenTypes(List<String> tokens) {
    Map<String, Double> tokenCount = getLowerCasedPassageTokenCount(tokens);
    for (String token : tokenCount.keySet()) {
      tokenCount.put(token, 1.0);
    }
    return tokenCount;
  }

  public static Map<String, String> getLowerCasedSynonymKeytermMapping(List<Keyterm> keyterms) {
    Map<String, String> synonym2keyterm = new HashMap<String, String>();
    for (Keyterm keyterm : keyterms) {
      // only consider keyterms whose weights >= 0.4
      if (keyterm.getProbability() >= 0.4) {
        String text = keyterm.getText();
        BioKeyterm biokeyterm = (BioKeyterm) keyterm;
        // use refined synonyms
        for (String synonym : biokeyterm.getSynonymsBySource("RefinedSynonyms")) {
          synonym2keyterm.put(synonym.toLowerCase(), text);
        }
      }
    }
    return synonym2keyterm;
  }

  protected static int[] getCharacterOffsets(List<String> tokens) {
    int[] charOffsets = new int[tokens.size()];
    int offset = 0;
    for (int i = 0; i < tokens.size(); i++) {
      charOffsets[i] = (offset += tokens.size() + 1);
    }
    return charOffsets;
  }

  public static List<String> getResolvedTokens(List<String> originalTokens,
          Map<String, String> synonym2keyterm) {
    List<String> resolvedTokens = new ArrayList<String>();
    for (String orignalToken : originalTokens) {
      if (synonym2keyterm.containsKey(orignalToken.toLowerCase())) {
        resolvedTokens.add(synonym2keyterm.get(orignalToken.toLowerCase()));
      } else {
        resolvedTokens.add(orignalToken);
      }
    }
    return resolvedTokens;
  }

  public static List<String> tokenize(String spanText, Collection<String> keyterms,
          Collection<String> synonyms) {
    Set<String> lowerCasedKeyterms = new HashSet<String>();
    if (keyterms != null) {
      for (String keyterm : keyterms)
        lowerCasedKeyterms.add(keyterm.toLowerCase());
    }
    if (synonyms != null) {
      for (String synonym : synonyms) {
        lowerCasedKeyterms.add(synonym.toLowerCase());
      }
    }
    return tokenize(spanText, lowerCasedKeyterms);
  }

  public static List<String> tokenize(String spanText, Collection<String> lowerCasedKeyterms) {
    String lowerCasedText = spanText.toLowerCase();
    List<String> tokens = new ArrayList<String>();
    int pos = 0;
    while (true) {
      Map<Integer, String> nextPosList = new HashMap<Integer, String>();
      for (String keyterm : lowerCasedKeyterms) {
        int nextPos;
        if ((nextPos = lowerCasedText.indexOf(keyterm, pos)) > 0) {
          nextPosList.put(nextPos, keyterm);
        }
      }
      if (nextPosList.size() > 0) {
        int minNextPos = Collections.min(nextPosList.keySet());
        tokens.addAll(LingPipeHmmPosTagger.tokenize(spanText.substring(pos, minNextPos)));
        pos = minNextPos + nextPosList.get(minNextPos).length();
        tokens.add(spanText.substring(minNextPos, pos));
      } else {
        tokens.addAll(LingPipeHmmPosTagger.tokenize(spanText.substring(pos)));
        break;
      }
    }
    return tokens;
  }
}
