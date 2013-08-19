package edu.cmu.lti.oaqa.bio.core.ie.rank;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;

import edu.cmu.lti.oaqa.cse.basephase.ie.AbstractPassageUpdater;
import edu.cmu.lti.oaqa.framework.UimaContextHelper;
import edu.cmu.lti.oaqa.framework.data.Keyterm;
import edu.cmu.lti.oaqa.framework.data.PassageCandidate;
import edu.cmu.lti.oaqa.framework.data.RetrievalResult;

/**
 * Combine scores from various sources (document, passages) stored in different CAS views, where
 * users may specify what transformation function should be applied ({@link #mode}) and what weight
 * should be used to combine ({@link #docWeight}).
 * 
 * @author Zi Yang <ziy@cs.cmu.edu>
 * 
 */
public class ScoreCombiner extends AbstractPassageUpdater {

  private enum TransformMode {
    no_transform, exponential, logarithmic, reciprocal_of_rank, normalized, normalized_of_exponential, exponential_of_normalized, logarithmic_of_normalized, normalized_of_logarithmic,
  }

  private double docWeight;

  private TransformMode mode;

  private Map<Float, Float> transform(List<Float> scores) {
    Map<Float, Float> ret = new HashMap<Float, Float>();

    float maxScore = Collections.max(scores);
    switch (mode) {
      case exponential:
        for (float score : scores) {
          if (!ret.containsKey(score)) {
            ret.put(score, (float) Math.exp(score));
          }
        }
        break;
      case logarithmic:
        for (float score : scores) {
          if (!ret.containsKey(score)) {
            ret.put(score, (float) safeLog(score));
          }
        }
        break;
      case normalized:
        for (float score : scores) {
          if (!ret.containsKey(score)) {
            ret.put(score, (float) score / maxScore);
          }
        }
        break;
      case exponential_of_normalized:
        for (float score : scores) {
          if (!ret.containsKey(score)) {
            ret.put(score, (float) Math.exp(score / maxScore));
          }
        }
        break;
      case normalized_of_exponential:
        for (float score : scores) {
          if (!ret.containsKey(score)) {
            ret.put(score, (float) Math.exp(score));
          }
        }
        float maxExpScore = Collections.max(ret.values());
        for (float score : ret.keySet()) {
          ret.put(score, (float) ret.get(score) / maxExpScore);
        }
        break;
      case logarithmic_of_normalized:
        for (float score : scores) {
          if (!ret.containsKey(score)) {
            ret.put(score, (float) safeLog(score / maxScore));
          }
        }
        break;
      case normalized_of_logarithmic:
        for (float score : scores) {
          if (!ret.containsKey(score)) {
            ret.put(score, (float) safeLog(score));
          }
        }
        float maxLogScore = Collections.max(ret.values());
        for (float score : ret.keySet()) {
          ret.put(score, (float) ret.get(score) / maxLogScore);
        }
        break;
      case reciprocal_of_rank:
        int i = 1;
        for (float score : scores) {
          if (!ret.containsKey(score)) {
            ret.put(score, (float) 1.0 / i);
          }
          i++;
        }
        break;
      case no_transform:
      default:
        for (float score : scores) {
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
    docWeight = UimaContextHelper.getConfigParameterFloatValue(aContext, "DocWeight", 0.5F);
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
    Map<String, Float> id2score = new HashMap<String, Float>();
    List<Float> docScores = new ArrayList<Float>();
    for (RetrievalResult document : documents) {
      id2score.put(document.getDocID(), document.getProbability());
      docScores.add(document.getProbability());
    }
    Map<Float, Float> docScoreMap = transform(docScores);

    double minDocScore = docScoreMap.get(documents.get(documents.size() - 1).getProbability());
    // transform passage scores
    List<Float> passageScores = new ArrayList<Float>();
    for (PassageCandidate passage : passages) {
      passageScores.add((float) passage.getProbability());
    }
    Map<Float, Float> passageScoreMap = transform(passageScores);
    // combine scores
    for (PassageCandidate passage : passages) {
      String id = passage.getDocID();
      double docScore = id2score.containsKey(id) ? docScoreMap.get(id2score.get(id)) : minDocScore;
      passage.setProbablity((float) (passageScoreMap.get(passage.getProbability())
              * (1 - docWeight) + docScore * docWeight));
    }

    return passages;
  }
}
