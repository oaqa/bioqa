package edu.cmu.lti.oaqa.bio.test.ziy.ie.rank;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;

import edu.cmu.lti.oaqa.bio.core.ie.AbstractPassageUpdater;
import edu.cmu.lti.oaqa.framework.UimaContextHelper;
import edu.cmu.lti.oaqa.framework.data.Keyterm;
import edu.cmu.lti.oaqa.framework.data.PassageCandidate;
import edu.cmu.lti.oaqa.framework.data.RetrievalResult;


public class ScoreCombiner extends AbstractPassageUpdater {

  private enum TransformMode {
    no_transform, exponential, logarithmic, reciprocal_of_rank, normalized, normalized_of_exponential, exponential_of_normalized, logarithmic_of_normalized, normalized_of_logarithmic,
  }

  private double docWeight;

  private TransformMode mode;

  private Map<Double, Double> transform(List<Double> scores) {
    Map<Double, Double> ret = new HashMap<Double, Double>();
    double maxScore = Collections.max(scores);
    switch (mode) {
      case exponential:
        for (double score : scores) {
          if (!ret.containsKey(score)) {
            ret.put(score, Math.exp(score));
          }
        }
        break;
      case logarithmic:
        for (double score : scores) {
          if (!ret.containsKey(score)) {
            ret.put(score, safeLog(score));
          }
        }
        break;
      case normalized:
        for (double score : scores) {
          if (!ret.containsKey(score)) {
            ret.put(score, score / maxScore);
          }
        }
        break;
      case exponential_of_normalized:
        for (double score : scores) {
          if (!ret.containsKey(score)) {
            ret.put(score, Math.exp(score / maxScore));
          }
        }
        break;
      case normalized_of_exponential:
        for (double score : scores) {
          if (!ret.containsKey(score)) {
            ret.put(score, Math.exp(score));
          }
        }
        double maxExpScore = Collections.max(ret.values());
        for (double score : ret.keySet()) {
          ret.put(score, ret.get(score) / maxExpScore);
        }
        break;
      case logarithmic_of_normalized:
        for (double score : scores) {
          if (!ret.containsKey(score)) {
            ret.put(score, safeLog(score / maxScore));
          }
        }
        break;
      case normalized_of_logarithmic:
        for (double score : scores) {
          if (!ret.containsKey(score)) {
            ret.put(score, safeLog(score));
          }
        }
        double maxLogScore = Collections.max(ret.values());
        for (double score : ret.keySet()) {
          ret.put(score, ret.get(score) / maxLogScore);
        }
        break;
      case reciprocal_of_rank:
        int i = 1;
        for (double score : scores) {
          if (!ret.containsKey(score)) {
            ret.put(score, 1.0 / i);
          }
          i++;
        }
        break;
      case no_transform:
      default:
        for (double score : scores) {
          ret.put(score, score);
        }
        break;
    }
    return ret;
  }

  private static double safeLog(double value) {
    return value > 0 ? Math.log(value) : Double.NEGATIVE_INFINITY;
  }

  @Override
  public void initialize(UimaContext aContext) throws ResourceInitializationException {
    super.initialize(aContext);
    docWeight = UimaContextHelper.getConfigParameterDoubleValue(aContext, "DocWeight", 0.5);
    mode = TransformMode.valueOf(UimaContextHelper.getConfigParameterStringValue(aContext,
            "TransformMode", "no_transform"));
  }

  @Override
  protected List<PassageCandidate> updatePassages(String question, List<Keyterm> keyterms,
          List<RetrievalResult> documents, List<PassageCandidate> passages) {
    if (passages.size() == 0) {
      return passages;
    }
    // transform document scores
    Map<String, Double> id2score = new HashMap<String, Double>();
    List<Double> docScores = new ArrayList<Double>();
    for (RetrievalResult document : documents) {
      id2score.put(document.getDocID(), document.getScore());
      docScores.add(document.getScore());
    }
    Map<Double, Double> docScoreMap = transform(docScores);
    double minDocScore = docScoreMap.get(documents.get(documents.size() - 1).getScore());
    // transform passage scores
    List<Double> passageScores = new ArrayList<Double>();
    for (PassageCandidate passage : passages) {
      passageScores.add(passage.getScore());
    }
    Map<Double, Double> passageScoreMap = transform(passageScores);
    // combine scores
    for (PassageCandidate passage : passages) {
      String id = passage.getDocID();
      double docScore = id2score.containsKey(id) ? docScoreMap.get(id2score.get(id)) : minDocScore;
      // System.out.println(passage + "\t" + docScore + "\t" +
      // passageScoreMap.get(passage.getScore())
      // + "\t"
      // + (passageScoreMap.get(passage.getScore()) * (1 - docWeight) + docScore * docWeight));
      passage.setScore(passageScoreMap.get(passage.getScore()) * (1 - docWeight) + docScore
              * docWeight);
    }
    return passages;
  }
}
