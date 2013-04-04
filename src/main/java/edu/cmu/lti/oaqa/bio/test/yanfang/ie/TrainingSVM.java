package edu.cmu.lti.oaqa.bio.test.yanfang.ie;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.CharArrayMap.EntrySet;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.oaqa.model.Passage;

import similarity.Similarity;

import edu.cmu.lti.oaqa.bio.framework.data.BioKeyterm;
import edu.cmu.lti.oaqa.bio.framework.retrieval.DocumentRetrieverWrapper;
import edu.cmu.lti.oaqa.bio.retrieval.tools.CleanTerms;
import edu.cmu.lti.oaqa.bio.test.eric.passagescore.CompositeKeytermWindowScorer;
import edu.cmu.lti.oaqa.bio.test.yanfang.keyterm.LingPipeAndAbbreviation;
import edu.cmu.lti.oaqa.bio.test.ziy.ie.ContentAwarePassageUpdater;
import edu.cmu.lti.oaqa.bio.test.ziy.ie.span.LingPipeSentenceChunker;
import edu.cmu.lti.oaqa.bio.test.ziy.keyterm.pos.LingPipeHmmPosTagger;
import edu.cmu.lti.oaqa.ecd.log.AbstractLoggedComponent;
import edu.cmu.lti.oaqa.framework.BaseJCasHelper;
import edu.cmu.lti.oaqa.framework.UimaContextHelper;
import edu.cmu.lti.oaqa.framework.ViewManager;
import edu.cmu.lti.oaqa.framework.ViewManager.ViewType;
import edu.cmu.lti.oaqa.framework.data.Article;
import edu.cmu.lti.oaqa.framework.data.Keyterm;
import edu.cmu.lti.oaqa.framework.data.KeytermList;
import edu.cmu.lti.oaqa.framework.data.PassageCandidate;
import edu.cmu.lti.oaqa.framework.data.PassageCandidateArray;
import edu.cmu.lti.oaqa.framework.data.TextSpan;
import edu.cmu.lti.oaqa.framework.eval.passage.PassageHelper;
import edu.cmu.lti.oaqa.framework.types.InputElement;

public class TrainingSVM extends AbstractLoggedComponent {

  private int limit;

  protected DocumentRetrieverWrapper retriever;

  private CompositeKeytermWindowScorer scorer;

  @Override
  public void initialize(UimaContext c) throws ResourceInitializationException {
    super.initialize(c);
    limit = UimaContextHelper.getConfigParameterIntValue(c, "limit", 10);
    boolean zipped = UimaContextHelper.getConfigParameterBooleanValue(c, "Zipped", true);
    try {
      retriever = new DocumentRetrieverWrapper((String) c.getConfigParameterValue("Prefix"),
              zipped, true);
    } catch (NullPointerException e) {
      retriever = new DocumentRetrieverWrapper(zipped, true);
    }

  }

  @Override
  public final void process(JCas jcas) throws AnalysisEngineProcessException {
    super.process(jcas);
    try {
      // collect gold standard
      List<Passage> gs = PassageHelper.loadDocumentSet(ViewManager.getView(jcas,
              ViewType.DOCUMENT_GS));
      // collect retrieved passages
      List<PassageCandidate> passages = PassageCandidateArray.retrievePassageCandidates(ViewManager
              .getCandidateView(jcas));
      // get the question
      String questionText = ((InputElement) BaseJCasHelper.getAnnotation(jcas, InputElement.type))
              .getQuestion();
      // String qID = ((InputElement) BaseJCasHelper.getAnnotation(jcas,
      // InputElement.type)).getSequenceId();
      // System.out.println("*********" + qID);

      // get keyterms
      KeytermList keytermList = new KeytermList(jcas);
      List<Keyterm> keyterms = keytermList.getKeyterms();
      int count = 0;
      HashMap<Integer, List<String>> features = new HashMap<Integer, List<String>>();
      List<String> labels = new ArrayList<String>();
      Map<String, List<Passage>> id2gsPassages = new HashMap<String, List<Passage>>();
      for (Passage gs_passage : gs) {
        String id = gs_passage.getUri();
        if (!id2gsPassages.containsKey(id)) {
          id2gsPassages.put(id, new ArrayList<Passage>());
        }
        id2gsPassages.get(id).add(gs_passage);
      }

      // get the labels by comparing retrieved passages and golden standard. Regard the passage as
      // relevant as long as it have overlap with the gs
      // this should be gotten from the DB, instead of comparing the results and golden standard
      @SuppressWarnings("unchecked")
      List<PassageCandidate> passage_for_features = (List<PassageCandidate>) ((ArrayList<PassageCandidate>) passages)
              .clone();

      // File file = new File("qID");

      // FileWriter fw = new FileWriter(file.getAbsoluteFile());
      // BufferedWriter bw = new BufferedWriter(fw);

      loop: for (PassageCandidate passage : passages) {

        Article article = retriever.getDocument(passage.getDocID());
        TextSpan origSpan = new TextSpan(passage.getStart(), passage.getEnd());
        String psg = article.getSpanText(origSpan);
        // bw.write(psg);

        // bw.newLine();

        // get the labels
        String passage_id = passage.getDocID();
        if (!id2gsPassages.containsKey(passage_id)) {
          labels.add("0");
          // bw.write("============false");
          // bw.newLine();
          // bw.newLine();
          continue loop;
        }
        for (Passage gs_passage_in_same_doc : id2gsPassages.get(passage_id)) {
          if (getOverlapLength(gs_passage_in_same_doc, passage) > 0) {
            labels.add("1");
            // bw.write("============true");
            // bw.newLine();
            // bw.newLine();
            continue loop;
          }
        }

        labels.add("0");
        // bw.write("============false");
        // features.get(count).add(ranking);
        // features.get(count).add(score);

        // bw.newLine();
        // bw.newLine();

        count++;
        if (count > limit)
          break;
      }

      // bw.close();

      features = extractFeatures(questionText, passage_for_features, keyterms, limit, retriever,
              false);

      /*
       * HashMap<Integer, List<String>> fake_features = new HashMap<Integer, List<String>>();
       * 
       * 
       * int fake_count = 0; for(String s : labels) { fake_features.put(fake_count, new
       * ArrayList<String>()); fake_features.get(fake_count).add(s); fake_count++; }
       */
      System.out.println("labels" + labels);
      System.out.println("features" + features);

      // write the features and labels into the SVMlib format file
      // the last parameter is to change the one model for all questions (when true) or each
      // question has its own model (when false)
      //outputAsSVMLibFormat(features, labels, "SVMtrain", false);
      outputAsSVMLibFormat(features, labels, "SVMtrain", true);

      // train the model based on SVMlib
      String[] arguments_for_training = { "-s", "3", "-t", "0", "-b", "1", "SVMtrain", "SVMmodel" };
      svm_train train = new svm_train();
      train.run(arguments_for_training);

      /*
       * Map<String, List<PassageCandidate>> id2gsPassages = new HashMap<String,
       * List<PassageCandidate>>(); for (PassageCandidate gsPassage : gsPassages) { String id =
       * gsPassage.getDocID(); if (!id2gsPassages.containsKey(id)) { id2gsPassages.put(id, new
       * ArrayList<PassageCandidate>()); } id2gsPassages.get(id).add(gsPassage); }
       */

    } catch (Exception e) {
      throw new AnalysisEngineProcessException(e);
    }
  }

  /*
   * Return Integer.MIN_VALUE if p1 and p2 come from different documents, positive value if p1 and
   * p2 are overlapped, and the value is the number of overlapped length, negative (or 0) value if
   * p1 and p2 are not overlapped, and the absolute value is the distance between two passages.
   */
  private static int getOverlapLength(Passage p1, PassageCandidate p2) {
    // different documents
    if (!p1.getUri().equals(p2.getDocID())) {
      return Integer.MIN_VALUE;
    }
    // overlapped passages
    if (p1.getBegin() < p2.getEnd() && p1.getEnd() > p2.getStart()) {
      return Math.min(p1.getEnd(), p2.getEnd()) - Math.max(p1.getBegin(), p2.getStart());
    }
    // non-overlapped passages
    return -Math.min(Math.abs(p1.getBegin() - p2.getEnd()), Math.abs(p1.getEnd() - p2.getStart()));
  }

  public static int getKeytermCount(String psg, List<Keyterm> keyterms, float threshold,
          boolean countDuplicates) {
    int result = 0;
    int result_with_duplicates = 0;
    Map<String, Keyterm> keytermSet = new HashMap<String, Keyterm>();
    Map<Keyterm, Boolean> markList = new HashMap<Keyterm, Boolean>();

    for (Keyterm keyterm : keyterms) {
      if (keyterm.getProbability() < threshold)
        continue;

      // mark keyterms
      if (!markList.containsKey(keyterm)) {
        markList.put(keyterm, false);
      }

      // store synonyms as the keys
      for (String synonym : ((BioKeyterm) keyterm).getSynonymsBySource("RefinedSynonyms")) {
        if (!keytermSet.containsKey(synonym.toLowerCase())) {
          synonym = CleanTerms.getStemmedTerm(synonym);
          keytermSet.put(synonym, keyterm);
        }
      }

      // store the text of the keyterm
      if (!keytermSet.containsKey(keyterm.getText())) {
        keytermSet.put(keyterm.getText().toLowerCase(), keyterm);
      }
    }

    List<TextSpan> sentences = LingPipeSentenceChunker.split(psg);
    for (TextSpan sentence : sentences) {

      // use chunk
      List<String> tokens = LingPipeAndAbbreviation.tokenize(psg.substring(sentence.begin,
              sentence.end));

      // divide the sentence with chunk
      for (String token : tokens) {
        token = CleanTerms.getStemmedTerm(token.toLowerCase());
        // the token is part of the token
        for (Map.Entry<String, Keyterm> entry : keytermSet.entrySet()) {
          if (entry.getKey().contains(token) && token.length() > 4) {
            if (markList.containsKey(entry.getValue())) {
              result_with_duplicates++;
              // check if this one has been founded once
              if (!markList.get(entry.getValue())) {
                markList.put(entry.getValue(), true);
                result++;
              }
            }
          }
        }

        // keyterm and token are the same
        if (keytermSet.containsKey(token)) {
          if (markList.containsKey(keytermSet.get(token))) {
            result_with_duplicates++;
            // check if this one has been founded once
            if (!markList.get(keytermSet.get(token))) {
              markList.put(keytermSet.get(token), true);
              result++;
            }
          }
        }
      }

      // use Pos tagger
      List<String> tokens2 = LingPipeHmmPosTagger.tokenize(psg.substring(sentence.begin,
              sentence.end));

      // divide the psg with pos tagger
      for (String token : tokens2) {
        token = CleanTerms.getStemmedTerm(token.toLowerCase());
        if (keytermSet.containsKey(token)) {
          if (markList.containsKey(keytermSet.get(token))) {
            // check if this one has been founded once
            if (!markList.get(keytermSet.get(token))) {
              markList.put(keytermSet.get(token), true);
              result++;
            }
          }
        }
      }

    }
    return countDuplicates ? result_with_duplicates : result;
  }

  public static void outputAsSVMLibFormat(HashMap<Integer, List<String>> features,
          List<String> labels, String fileName, boolean append) {
    try {
      File file = new File(fileName);
      if (!file.exists()) {
        file.createNewFile();
      }

      FileWriter fw = new FileWriter(file.getAbsoluteFile(), append);
      BufferedWriter bw = new BufferedWriter(fw);

      String content = "";
      for (int i = 0; i < Math.min(features.size(), labels.size()); i++) {
        content = labels.get(i) + " ";
        int feature_number = 1;
        for (String feature : features.get(i)) {
          content += Integer.toString(feature_number) + ":" + feature + " ";
          feature_number++;
        }
        bw.write(content);
        bw.newLine();
      }
      bw.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static List<String> getKeytermsAndSynonyms(List<Keyterm> keyterms, float threshold) {

    List<String> result = new ArrayList<String>();
    for (Keyterm k : keyterms) {
      if (k.getProbability() >= threshold) {
        result.add(k.getText());
        result.addAll(((BioKeyterm) k).getSynonyms());
      }
    }
    return result;
  }

  public static int totalKeytermsinPassge(String psg) {
    int count = 0;

    List<TextSpan> sentences = LingPipeSentenceChunker.split(psg);
    for (TextSpan sentence : sentences) {

      // use chunk
      List<String> tokens = LingPipeAndAbbreviation.tokenize(psg.substring(sentence.begin,
              sentence.end));
      count += tokens.size();
    }
    return count;
  }

  public static int windowInfor(String psg, List<Keyterm> keyterms, float threshold, String infor) {
    int begin = Integer.MIN_VALUE;
    int end = Integer.MIN_VALUE;
    int termCount = 0;
    Map<String, Keyterm> keytermSet = new HashMap<String, Keyterm>();

    for (Keyterm keyterm : keyterms) {
      if (keyterm.getProbability() < threshold)
        continue;

      // store synonyms as the keys
      for (String synonym : ((BioKeyterm) keyterm).getSynonymsBySource("RefinedSynonyms")) {
        if (!keytermSet.containsKey(synonym.toLowerCase())) {
          synonym = CleanTerms.getStemmedTerm(synonym);
          keytermSet.put(synonym, keyterm);
        }
      }
      // store the text of the keyterm
      if (!keytermSet.containsKey(keyterm.getText())) {
        keytermSet.put(keyterm.getText().toLowerCase(), keyterm);
      }
    }

    List<TextSpan> sentences = LingPipeSentenceChunker.split(psg);
    for (TextSpan sentence : sentences) {

      String s = psg.substring(sentence.begin, sentence.end);
      
      // use chunk
      List<String> tokens = LingPipeAndAbbreviation.tokenize(s);

      // divide the sentence with chunk
      for (String token : tokens) {
        token = CleanTerms.getStemmedTerm(token.toLowerCase());
        // the token is part of the token
        for (Map.Entry<String, Keyterm> entry : keytermSet.entrySet()) {
          if (entry.getKey().contains(token) && token.length() > 4) {
            begin = begin == Integer.MIN_VALUE ? s.indexOf(token) : begin;
            end = end < s.indexOf(token) ? s.indexOf(token) : end;
          }

          // keyterm and token are the same
          if (keytermSet.containsKey(token)) {
            begin = begin == Integer.MIN_VALUE ? s.indexOf(token) : begin;
            end = end < s.indexOf(token) ? s.indexOf(token) : end;
          }
        }
      }
      // use Pos tagger
      List<String> tokens2 = LingPipeHmmPosTagger.tokenize(s);

      // divide the psg with pos tagger
      for (String token : tokens2) {
        termCount++;
        token = CleanTerms.getStemmedTerm(token.toLowerCase());
        if (keytermSet.containsKey(token)) {
          begin = begin == Integer.MIN_VALUE ? s.indexOf(token) : begin;
          end = end < s.indexOf(token) ? s.indexOf(token) : end;
        }
      }
    }

    if (begin == Integer.MIN_VALUE)
      begin = 0;
    if (end == Integer.MIN_VALUE)
      end = 0;

    if (infor.equals("begin"))
      return begin;
    if (infor.equals("end"))
      return end;
    if (infor.equals("size"))
      return end - begin + 1;
    if (infor.equals("termCount"))
      return termCount;
    return 0;
  }

  public static int keytermCountInQuestion(List<Keyterm> keyterms, float threshold) {
    int result = 0;
    
    for (Keyterm k : keyterms) {
      if(k.getProbability() >= threshold)
        result++;
    }
    
    return result;
  }
  
  public static HashMap<Integer, List<String>> extractFeatures(String question,
          List<PassageCandidate> passages, List<Keyterm> keyterms, int limit,
          DocumentRetrieverWrapper retriever, boolean print) {
    HashMap<Integer, List<String>> features = new HashMap<Integer, List<String>>();
    int count = 0;
    limit = limit == 0 ? passages.size() : limit;

    // get the global information for each question
    ArrayList<PassageCandidate> passages2 = (ArrayList<PassageCandidate>) ((ArrayList) passages).clone();
    int sumLength = 0;
    int sumKeytermsInPsg = 0;
    int sumImportantKeytermsInPsg = 0;
    int sumImportantKeytermsInPsgWithDuplicates = 0;
    int sumRegularKeyterms = 0;
    HashMap<Integer, Double> standardizedLength = new HashMap<Integer, Double>();
    HashMap<Integer, Double> standardizedKeytermsInPsg = new HashMap<Integer, Double>();
    HashMap<Integer, Double> standardizedImportantKeytermsInPsg = new HashMap<Integer, Double>();
    HashMap<Integer, Double> standardizedImportantKeytermsInPsgWithDuplicates = new HashMap<Integer, Double>();
    HashMap<Integer, Double> standardizedRegularKeyterms = new HashMap<Integer, Double>();
    
    for (PassageCandidate passage2 : passages) {
   // extract features: ranking score, ranking, keyterm counts,
      // passage.getProbability()
      Article article = retriever.getDocument(passage2.getDocID());
      // sanity check
      if (passage2.getStart() > article.getText().length() - 1) {
        passage2.setStart(article.getText().length() - 1);
      }
      if (passage2.getEnd() > article.getText().length()) {
        passage2.setEnd(article.getText().length());
      }
      // get all sentences from the retrieved paragraph
      TextSpan origSpan = new TextSpan(passage2.getStart(), passage2.getEnd());
      String psg = article.getSpanText(origSpan);
      
      sumLength += psg.length();
      standardizedLength.put(count, (double)psg.length());
      
      int termCountInPsg = windowInfor(psg, keyterms, (float)0.6, "termCount");
      sumKeytermsInPsg += termCountInPsg;
      standardizedKeytermsInPsg.put(count, (double)termCountInPsg);
      
      int importantKeytermCount = getKeytermCount(psg, keyterms, (float) 0.6, false);
      sumImportantKeytermsInPsg += importantKeytermCount; 
      standardizedImportantKeytermsInPsg.put(count, (double)importantKeytermCount);
      
      int importantKeytermCountWithDuplicates = getKeytermCount(psg, keyterms, (float) 0.6, true);
      sumImportantKeytermsInPsgWithDuplicates += importantKeytermCountWithDuplicates;
      standardizedImportantKeytermsInPsgWithDuplicates.put(count, (double)importantKeytermCountWithDuplicates);
      
      int regularKeytermCount = getKeytermCount(psg, keyterms, (float) 0.1, false);
      sumRegularKeyterms += regularKeytermCount;
      standardizedRegularKeyterms.put(count, (double)regularKeytermCount);
      
      count++;
      if (count > limit)
        break;
    }
    
    double meanLength = (double)sumLength/limit;
    double meanTermCountInPsg = (double)sumKeytermsInPsg/limit;
    double meanImportantKeytermsInPsg = (double)sumImportantKeytermsInPsg/limit;
    double meanImportantKeytermsInPsgWithDuplicates = (double) sumImportantKeytermsInPsgWithDuplicates/limit;
    double meanRegularKeyterms = (double)sumRegularKeyterms/limit;
    
    double devLength = 0;
    double devTermCountInPsg = 0;
    double devImportantKeytermsInPsg = 0;
    double devImportantKeytermsInPsgWithDuplicates = 0;
    double devRegularKeyterms = 0;
    double largestLength = 10;  // set as the minimun length
    
    for(int i = 0; i< count; i++) {
      if (standardizedLength.containsKey(i))  {
        largestLength = largestLength > standardizedLength.get(i) ? largestLength : standardizedLength.get(i);
        devLength += Math.pow((standardizedLength.get(i) - meanLength), 2);   
      }
        
      if (standardizedKeytermsInPsg.containsKey(i))
        devTermCountInPsg += Math.pow((standardizedKeytermsInPsg.get(i) - meanTermCountInPsg), 2);      
  
      if (standardizedImportantKeytermsInPsg.containsKey(i))
        devImportantKeytermsInPsg += Math.pow((standardizedImportantKeytermsInPsg.get(i) - meanImportantKeytermsInPsg), 2);      
  
      if (standardizedImportantKeytermsInPsgWithDuplicates.containsKey(i))
        devImportantKeytermsInPsgWithDuplicates += Math.pow((standardizedImportantKeytermsInPsgWithDuplicates.get(i) - meanImportantKeytermsInPsgWithDuplicates), 2);      
      
      if (standardizedRegularKeyterms.containsKey(i))
        devRegularKeyterms += Math.pow((standardizedRegularKeyterms.get(i) - meanRegularKeyterms), 2);      
  
    }
    
    devLength = Math.sqrt(devLength/limit);
    devTermCountInPsg = Math.sqrt(devTermCountInPsg/limit);
    devImportantKeytermsInPsg = Math.sqrt(devImportantKeytermsInPsg/limit);
    devImportantKeytermsInPsgWithDuplicates = Math.sqrt(devImportantKeytermsInPsgWithDuplicates/limit);
    devRegularKeyterms = Math.sqrt(devRegularKeyterms/limit);
    
    count = 0;
    for (PassageCandidate passage : passages) {
      // extract features: ranking score, ranking, keyterm counts,
      // passage.getProbability()
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
      String psg = article.getSpanText(origSpan);
      int importantKeytermCount = getKeytermCount(psg, keyterms, (float) 0.6, false);

      int importantKeytermCountWithDuplicates = getKeytermCount(psg, keyterms, (float) 0.6, true);

      int regularKeytermCount = getKeytermCount(psg, keyterms, (float) 0.1, false);
      
      int windowSize = windowInfor(psg,keyterms, (float)0.6, "size");
      int windowBegin = windowInfor(psg,keyterms, (float)0.6, "begin");       
      int keytermCountInQuestion = keytermCountInQuestion(keyterms, (float) 0.4);
      int termCountInPsg = windowInfor(psg, keyterms, (float)0.6, "termCount");
      
      features.put(count, new ArrayList<String>());

      features.get(count).add(Integer.toString(importantKeytermCount));
      features.get(count).add(Double.toString(psg.length()/100));
      features.get(count).add(Double.toString((psg.length() - meanLength)/devLength));
      //features.get(count).add(Integer.toString(importantKeytermCountWithDuplicates));
      features.get(count).add(Integer.toString(regularKeytermCount));
     
      //features.get(count).add(Double.toString((termCountInPsg - meanTermCountInPsg)/devTermCountInPsg));
      
      features.get(count).add(Double.toString((importantKeytermCount - meanImportantKeytermsInPsg)/devImportantKeytermsInPsg));
      
      features.get(count).add(Double.toString((importantKeytermCountWithDuplicates - meanImportantKeytermsInPsgWithDuplicates)/devImportantKeytermsInPsgWithDuplicates));
      
      features.get(count).add(Double.toString((regularKeytermCount - meanRegularKeyterms)/devRegularKeyterms));
      
      /*Percent Matches*/
      features.get(count).add(
              Double.toString(importantKeytermCountWithDuplicates == 0 ? 0 : importantKeytermCount
                      / (double) importantKeytermCountWithDuplicates));
      //features.get(count).add(
        //      Double.toString(1 - (double)windowSize/psg.length()));
      // passage offset score
      
      //features.get(count).add(Double.toString((psg.length()-windowBegin)/(double)psg.length()));
      
      //features.get(count).add(Double.toString((double)importantKeytermCount/keyterms.size()));
      
      /*Keyterm Matches Score*/
      features.get(count).add(Double.toString((double)importantKeytermCount/keytermCountInQuestion));
      
      /* keyterm found / passage length(density of keyterms) */
      features.get(count).add(Double.toString((double) importantKeytermCount/termCountInPsg));
      
      count++;
      if (count > limit)
        break;
    }
   
    /*
    double meanLength = (double)sumLength/limit;
    double meanTermCountInPsg = (double)sumKeytermsInPsg/limit;
    double meanImportantKeytermsInPsg = (double)sumImportantKeytermsInPsg/limit;
    double meanImportantKeytermsInPsgWithDuplicates = (double) sumImportantKeytermsInPsgWithDuplicates/limit;
   
    for(int i = 0; i<count; i++) {
      features.get
    }
    */

    return features;
  }

}
