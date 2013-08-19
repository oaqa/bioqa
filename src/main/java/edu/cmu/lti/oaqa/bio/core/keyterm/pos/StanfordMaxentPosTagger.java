package edu.cmu.lti.oaqa.bio.core.keyterm.pos;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

/**
 * Part of speech tagger by wrapping Stanford CoreNLP's {@link MaxentTagger} and white space
 * tokenizer.
 * 
 * @author Zi Yang <ziy@cs.cmu.edu>
 * 
 */
public class StanfordMaxentPosTagger extends AbstractModelBasedPosTagger {

  private MaxentTagger tagger;

  @Override
  protected void initialize(URL url) throws IOException, ClassNotFoundException {
    tagger = new MaxentTagger(url.getFile());
  }

  @Override
  public List<String> getPosTag(List<String> tokenList) {
    List<Word> words = new ArrayList<Word>();
    for (String token : tokenList) {
      words.add(new Word(token));
    }
    ArrayList<TaggedWord> taggedWords = tagger.tagSentence(words);
    List<String> tags = new ArrayList<String>();
    for (TaggedWord word : taggedWords) {
      tags.add(word.tag());
    }
    return tags;
  }

  public static void main(String[] args) throws IOException, ClassNotFoundException {
    String sentence = "What are the known drug side effects associated with the different alleles of CYP2C19's?";
    StanfordMaxentPosTagger posTagger = new StanfordMaxentPosTagger();
    posTagger.initialize("wsj-0-18-left3words.tagger");
    posTagger.initialize("src/main/resources/wsj-0-18-left3words.tagger");
    List<String> posTags = posTagger.getPosTag(Arrays.asList(sentence.split("\\s+")));
    System.out.println(posTags);
  }
}
