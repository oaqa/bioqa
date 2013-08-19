package edu.cmu.lti.oaqa.bio.core.ie.span;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunking;
import com.aliasi.sentences.MedlineSentenceModel;
import com.aliasi.sentences.SentenceChunker;
import com.aliasi.sentences.SentenceModel;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.Strings;

import edu.cmu.lti.oaqa.framework.data.TextSpan;

public class LingPipeSentenceChunker {

  // The inputs to the SentenceModel method boundaryIndices are an array of tokens and an array of
  // whitespaces. Therefore we must first process the text into token and whitespace arrays, then
  // identify sentence boundaries. The SentenceBoundaryDemo.java program uses the class
  // com.aliasi.tokenizer.IndoEuropeanTokenizerFactory to provide a tokenizer, and a
  // com.aliasi.sentences.MedlineSentenceModel to do the sentence boundary detection.
  // A SentenceChunker is constructed from a TokenizerFactory and a SentenceModel:
  static final TokenizerFactory TOKENIZER_FACTORY = HtmlTokenizerFactory.INSTANCE;

  // static final TokenizerFactory TOKENIZER_FACTORY = IndoEuropeanTokenizerFactory.INSTANCE;

  static final SentenceModel SENTENCE_MODEL = new MedlineSentenceModel();

  static final SentenceChunker SENTENCE_CHUNKER = new SentenceChunker(TOKENIZER_FACTORY,
          SENTENCE_MODEL);

  public static List<TextSpan> split(String text) {
    // The SentenceChunker method chunk produces a com.aliasi.chunk.Chunking over the text. A
    // Chunking is a set of com.aliasi.chunk.Chunk objects over a shared CharSequence. The chunkSet
    // method returns the set of (sentence) chunks, and the charSequence method returns the
    // underlying character sequence.
    Chunking chunking = SENTENCE_CHUNKER.chunk(text.toCharArray(), 0, text.length());
    Set<Chunk> sentences = chunking.chunkSet();
    List<TextSpan> spans = new ArrayList<TextSpan>();
    for (Chunk sentence : sentences) {
      spans.add(new TextSpan(sentence.start(), sentence.end()));
    }
    return spans;
  }

  public static List<TextSpan> split(String text, int offset) {
    List<TextSpan> shiftedSpans = new ArrayList<TextSpan>();
    for (TextSpan span : split(text)) {
      shiftedSpans.add(new TextSpan(span.begin + offset, span.end + offset));
    }
    return shiftedSpans;
  }

  public static void main(String[] args) throws IOException {
    testFile();
  }

  public static void testParagraph() {
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

  public static void testFile() throws IOException {
    BufferedReader br = new BufferedReader(new FileReader("data/15133046.html"));
    String line;
    StringBuffer sb = new StringBuffer();
    while ((line = br.readLine()) != null) {
      sb.append(line + "\r\n");
    }
    br.close();
    String text = sb.toString();
    System.out.println(text.length());
    System.out.println(split(text));

    TokenizerFactory tokFact = HtmlTokenizerFactory.INSTANCE;
    char[] cs = Strings.toCharArray(text);
    Tokenizer tokenizer = tokFact.tokenizer(cs, 0, cs.length);
    for (String token : tokenizer) {
      int start = tokenizer.lastTokenStartPosition();
      int end = tokenizer.lastTokenEndPosition();
      System.out.println(start + " " + end + " " + token);
    }
  }
}

class HtmlTokenizerFactory extends IndoEuropeanTokenizerFactory {

  private static final long serialVersionUID = 1L;

  public static final HtmlTokenizerFactory INSTANCE = new HtmlTokenizerFactory();

  static final HtmlTokenizerFactory FACTORY = INSTANCE;

  @Override
  public Tokenizer tokenizer(char[] ch, int offset, int length) {
    removeHtmlTables(ch, offset, length);
    removeHtmlTags(ch, offset, length);
    unescapeHtmlCodes(ch, offset, length);
    return super.tokenizer(ch, offset, length);
  }

  protected static Pattern getHtmlBlockPattern(String tag) {
    return Pattern.compile("<" + tag + ".*?>.*?</" + tag + ">|^.*?</" + tag + ">|<" + tag
            + ".*?>.*?$", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
  }

  protected void removeHtmlTables(char[] ch, int offset, int length) {
    Matcher matcher = getHtmlBlockPattern("TABLE").matcher(CharBuffer.wrap(ch, offset, length));
    while (matcher.find()) {
      Arrays.fill(ch, matcher.start(), matcher.end(), ' ');
    }
  }

  protected static void removeHtmlTags(char[] ch, int offset, int length) {
    boolean filter = false;
    for (int i = offset; i < offset + length; i++) {
      if (ch[i] == '<') {
        filter = true;
        ch[i] = ' ';
      } else if (ch[i] == '>') {
        filter = false;
        ch[i] = ' ';
      } else if (filter) {
        ch[i] = ' ';
      }
    }
  }

  private static Pattern HTML_CODE_PATTERN = Pattern.compile("&#?\\w+;");

  protected static void unescapeHtmlCodes(char[] ch, int offset, int length) {
    Matcher matcher = HTML_CODE_PATTERN.matcher(CharBuffer.wrap(ch, offset, length));
    while (matcher.find()) {
      int start = matcher.start();
      int end = matcher.end();
      String htmlCode = CharBuffer.wrap(ch, start, end - start).toString();
      String unescape = StringEscapeUtils.unescapeHtml(htmlCode);
      assert unescape.length() == 1;
      ch[start] = ch[start + 1] == '#' || htmlCode.equals("&nbsp;") ? ' ' : unescape.charAt(0);
      Arrays.fill(ch, start + 1, end, ' ');
    }
  }
}
