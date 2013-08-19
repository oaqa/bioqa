package edu.cmu.lti.oaqa.bio.core.ie.span;

import java.util.ArrayList;
import java.util.List;

import com.aliasi.sentences.MedlineSentenceModel;
import com.aliasi.sentences.SentenceModel;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;

import edu.cmu.lti.oaqa.framework.data.TextSpan;

public class LingPipeSentenceSplitter {

  // The inputs to the SentenceModel method boundaryIndices are an array of tokens and an array of
  // whitespaces. Therefore we must first process the text into token and whitespace arrays, then
  // identify sentence boundaries. The SentenceBoundaryDemo.java program uses the class
  // com.aliasi.tokenizer.IndoEuropeanTokenizerFactory to provide a tokenizer, and a
  // com.aliasi.sentences.MedlineSentenceModel to do the sentence boundary detection:
  static final TokenizerFactory TOKENIZER_FACTORY = IndoEuropeanTokenizerFactory.INSTANCE;

  static final SentenceModel SENTENCE_MODEL = new MedlineSentenceModel();

  public static List<TextSpan> split(String text) {
    // The TokenizerFactory method tokenizer returns a a com.aliasi.tokenizer.Tokenizer. The
    // tokenize method parses the text into tokens and whitespaces, adding them to their respective
    // lists:
    List<String> tokenList = new ArrayList<String>();
    List<String> whiteList = new ArrayList<String>();
    Tokenizer tokenizer = TOKENIZER_FACTORY.tokenizer(text.toCharArray(), 0, text.length());
    tokenizer.tokenize(tokenList, whiteList);

    // The tokenList and whiteList arrays produced by the tokenizer are parallel arrays. The
    // whitespace at index [i] is that which precedes the token at index [i]. The tokenizer returns
    // elements for the whitespace preceding the first token and the whitespace following the last
    // token. Therefore in the above example we see that the whitespace array contains 151 elements,
    // while the token array contains 150 elements.

    // We convert the ArrayList objects into their corresponding String arrays, and then invoke the
    // boundaryIndices method:
    String[] tokens = new String[tokenList.size()];
    String[] whites = new String[whiteList.size()];
    tokenList.toArray(tokens);
    whiteList.toArray(whites);
    int[] sentenceBoundaries = SENTENCE_MODEL.boundaryIndices(tokens, whites);

    // The boundaryIndices method returns an array whose values are the indices of the elements in
    // the tokens array which are sentence final tokens. To extract the sentences we iterate through
    // the sentence bounaries array, keeping track of the indices of the sentence start and end
    // tokens, and printing out the correct elements from the tokens and whitespaces arrays. Here is
    // the code to print out the sentences found in the abstract, one per line:
    int sentStartTok = 0;
    int sentEndTok = 0;
    int sentStartOffset = 0;
    int sentEndOffset = 0;
    List<TextSpan> sentences = new ArrayList<TextSpan>();
    for (int i = 0; i < sentenceBoundaries.length; ++i) {
      sentEndTok = sentenceBoundaries[i];
      for (int j = sentStartTok; j <= sentEndTok; j++) {
        sentEndOffset += tokens[j].length() + whites[j + 1].length();
      }
      sentStartTok = sentEndTok + 1;
      sentences.add(new TextSpan(sentStartOffset, sentEndOffset));
      sentStartOffset = sentEndOffset + 1;
    }
    return sentences;
  }

  public static List<TextSpan> split(String text, int offset) {
    List<TextSpan> shiftedSpans = new ArrayList<TextSpan>();
    for (TextSpan span : split(text)) {
      shiftedSpans.add(new TextSpan(span.begin + offset, span.end + offset));
    }
    return shiftedSpans;
  }

  public static void main(String[] args) {
    String text = "<br>The BRCA1 polypeptides are marked with an asterisk.<br> In each case that included BARD1, "
            + "BRCA1 and BARD1 were co-expressed in insect cells and purified via an epitope tag on BRCA1. "
            + "B, purified RNAPII, as in Fig. 1A, was tested as a substrate for ubiquitination using the "
            + "following BRCA1 preparations: <sup>none (lane 1) </sup>, full-length BRCA1 plus BARD1 (lane 2), full-length "
            + "BRCA1 alone (lane 3), BRCA1(1–1852) plus BARD1 (lane 4), BRCA1(1–1527) plus BARD1 (lane 5), "
            + "BRCA1(1–1000) plus BARD1 (lane 6), BRCA1 (1–500) plus BARD1 (lane 7), and BRCA1(301–1863) (lane 8). "
            + "C, reactions as in panel B were repeated replacing the RNAPII complex with GST·CTD that had been "
            + "labeled using TFIIH.";
    System.out.println(text.length());
    System.out.println(split(text));
  }
}
