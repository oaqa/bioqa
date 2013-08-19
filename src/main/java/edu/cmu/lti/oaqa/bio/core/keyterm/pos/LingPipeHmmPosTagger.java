package edu.cmu.lti.oaqa.bio.core.keyterm.pos;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.aliasi.classify.ConditionalClassification;
import com.aliasi.hmm.HiddenMarkovModel;
import com.aliasi.hmm.HmmDecoder;
import com.aliasi.tag.ScoredTagging;
import com.aliasi.tag.TagLattice;
import com.aliasi.tag.Tagging;
import com.aliasi.tokenizer.RegExTokenizerFactory;
import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.Pair;
import com.aliasi.util.Streams;

/**
 * Part of speech tagger by wrapping LingPipe's {@link HiddenMarkovModel} and
 * {@link com.aliasi.tokenizer.RegExTokenizerFactory$RegExTokenizer}.
 * 
 * @author Zi Yang <ziy@cs.cmu.edu>
 * 
 */
public class LingPipeHmmPosTagger extends AbstractModelBasedPosTagger {

  private static final TokenizerFactory TOKENIZER = new RegExTokenizerFactory(
          "(-|'|\\d|\\p{L})+|\\S");

  private HmmDecoder decoder;

  @Override
  protected void initialize(URL url) throws IOException, ClassNotFoundException {
    ObjectInputStream ois = new ObjectInputStream(url.openStream());
    HiddenMarkovModel hmm = (HiddenMarkovModel) ois.readObject();
    decoder = new HmmDecoder(hmm);
    Streams.closeQuietly(ois);
  }

  @Override
  public List<String> getPosTag(List<String> tokenList) {
    List<String> tags = new ArrayList<String>();
    Tagging<String> tagging = decoder.tag(tokenList);
    for (int i = 0; i < tagging.size(); ++i) {
      tags.add(tagging.tag(i));
    }
    return tags;
  }

  public static List<String> tokenize(String sentence) {
    char[] cs = sentence.toCharArray();
    Tokenizer tokenizer = TOKENIZER.tokenizer(cs, 0, cs.length);
    String[] tokens = tokenizer.tokenize();
    return Arrays.asList(tokens);
  }

  public List<Pair<List<String>, Double>> getNBestPosTag(List<String> tokenList, int nBest) {
    List<Pair<List<String>, Double>> tagScorePairs = new ArrayList<Pair<List<String>, Double>>();
    Iterator<ScoredTagging<String>> nBestIt = decoder.tagNBest(tokenList, nBest);
    for (int n = 0; n < nBest && nBestIt.hasNext(); ++n) {
      ScoredTagging<String> scoredTagging = nBestIt.next();
      List<String> tags = new ArrayList<String>();
      for (int i = 0; i < tokenList.size(); ++i) {
        tags.add(scoredTagging.tag(i));
      }
      tagScorePairs.add(new Pair<List<String>, Double>(tags, scoredTagging.score()));
    }
    return tagScorePairs;
  }

  public List<Pair<String, Double>> getPosTagConfidence(List<String> tokenList) {
    List<Pair<String, Double>> tagConfPairs = new ArrayList<Pair<String, Double>>();
    TagLattice<String> lattice = decoder.tagMarginal(tokenList);
    for (int tokenIndex = 0; tokenIndex < tokenList.size(); ++tokenIndex) {
      ConditionalClassification tagScores = lattice.tokenClassification(tokenIndex);
      String tag = tagScores.bestCategory();
      double conf = tagScores.conditionalProbability(tag);
      tagConfPairs.add(new Pair<String, Double>(tag, conf));
    }
    return tagConfPairs;
  }

  public static void main(String[] args) throws IOException, ClassNotFoundException {
    String sentence = "What are the known drug side effects associated with the different alleles of CYP2C19's?";
    LingPipeHmmPosTagger posTagger = new LingPipeHmmPosTagger();
    posTagger.initialize("pos-en-bio-medpost.HiddenMarkovModel");
    posTagger.initialize("src/main/resources/pos-en-bio-medpost.HiddenMarkovModel");
    List<String> posTags = posTagger.getPosTag(tokenize(sentence));
    System.out.println(posTags);
  }
}
