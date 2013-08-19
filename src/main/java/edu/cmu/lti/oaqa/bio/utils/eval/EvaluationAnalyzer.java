package edu.cmu.lti.oaqa.bio.utils.eval;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EvaluationAnalyzer {

  private int numPhases;

  private int numScorers;

  private Map<String[], double[]> trace2scores = new HashMap<String[], double[]>();

  private static DecimalFormat df = new DecimalFormat("0.0000");

  public EvaluationAnalyzer(String logFile, String evaluator) throws NumberFormatException,
          IOException {
    BufferedReader br = new BufferedReader(new FileReader(logFile));
    String line;
    while ((line = br.readLine()) != null) {
      String[] segs = line.split(",");
      if (!segs[0].equals(evaluator)) {
        continue;
      }
      String[] trace = segs[1].split(">");
      numPhases = trace.length;
      numScorers = segs.length - 2;
      double[] scores = new double[numScorers];
      for (int i = 0; i < numScorers; i++) {
        scores[i] = Double.parseDouble(segs[i + 2]);
      }
      trace2scores.put(trace, scores);
    }
    br.close();
  }

  public void analyzeMinMaxAvg() {
    for (int i = 0; i < numPhases; i++) {
      System.out.println("Phase " + i);
      Map<String, List<Double>[]> component2scores = getComponentLevelScoresSummary(i);
      for (Map.Entry<String, List<Double>[]> entry : component2scores.entrySet()) {
        System.out.print("\t" + entry.getKey());
        List<Double>[] scores = entry.getValue();
        for (List<Double> score : scores) {
          System.out.print("\t" + df.format(Collections.min(score)) + "\t"
                  + df.format(Collections.max(score)) + "\t" + df.format(avg(score)));
        }
        System.out.println();
      }
    }
  }

  private double avg(List<Double> values) {
    double sum = 0.0;
    for (double value : values) {
      sum += value;
    }
    return sum / values.size();
  }

  private Map<String, List<Double>[]> getComponentLevelScoresSummary(int i) {
    Map<String, List<Double>[]> component2scores = new HashMap<String, List<Double>[]>();
    for (Map.Entry<String[], double[]> entry : trace2scores.entrySet()) {
      String component = entry.getKey()[i];
      @SuppressWarnings("unchecked")
      List<Double>[] scores = component2scores.containsKey(component) ? component2scores
              .get(component) : new List[numScorers];
      for (int j = 0; j < numScorers; j++) {
        if (scores[j] == null) {
          scores[j] = new ArrayList<Double>();
        }
        scores[j].add(entry.getValue()[j]);
      }
      component2scores.put(component, scores);
    }
    return component2scores;
  }

  public static void main(String[] args) throws NumberFormatException, IOException {
    EvaluationAnalyzer ea = new EvaluationAnalyzer("/home/yangzi/Desktop/evaluation.log",
            "PassageMAPMeasuresEvaluator");
    ea.analyzeMinMaxAvg();
  }
}
