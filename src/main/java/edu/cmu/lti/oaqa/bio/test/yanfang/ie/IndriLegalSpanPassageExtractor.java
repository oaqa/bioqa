package edu.cmu.lti.oaqa.bio.test.yanfang.ie;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import lemurproject.indri.ParsedDocument;
import lemurproject.indri.ScoredExtentResult;

import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;

import edu.cmu.lti.oaqa.bio.core.ie.DefaultPassageExtractor;
import edu.cmu.lti.oaqa.bio.retrieval.query.strategy.QueryGenerator;
import edu.cmu.lti.oaqa.framework.UimaContextHelper;
import edu.cmu.lti.oaqa.framework.data.Keyterm;
import edu.cmu.lti.oaqa.framework.data.PassageCandidate;

/**
 * @author Zi Yang <ziy@cs.cmu.edu>
 */
public class IndriLegalSpanPassageExtractor extends DefaultPassageExtractor {

  private String smoothing;

  private String smoothingMu;

  private String smoothingLambda;

  private String backupQuery = "";
  
  @Override
  public void initialize(UimaContext aContext) throws ResourceInitializationException {
    super.initialize(aContext);

    this.smoothing = aContext.getConfigParameterValue("smoothing").toString();
    this.smoothingMu = aContext.getConfigParameterValue("smoothing-mu").toString();
    this.smoothingLambda = aContext.getConfigParameterValue("smoothing-lambda").toString();
  }

  @Override
  protected String formulateQuery(List<Keyterm> keyterms) {

    this.backupQuery = QueryGenerator.generateIndriQuery(keyterms,"[legalspan]",false);

    String s2 = QueryGenerator.generateIndriQuery(keyterms,"[legalspan]",true);
    System.out.println("Query~~~:" + s2);

    return s2;
  }

  @Override
  protected List<PassageCandidate> extractPassages(String query) {

    String rule = "";
    if (this.smoothing.startsWith("j"))
      rule = "method:" + this.smoothing + "," + "collectionLambda:" + this.smoothingLambda;
    if (this.smoothing.startsWith("d"))
      rule = "method:" + this.smoothing + "," + "mu:" + this.smoothingMu;
    if (this.smoothing.startsWith("t"))
      rule = "method:" + this.smoothing + "," + "lambda:" + this.smoothingLambda + "," + "mu:"
              + this.smoothingMu;

    String[] rules = {rule};

    List<PassageCandidate> result = new ArrayList<PassageCandidate>();

    try {
      wrapper.getQueryEnvironment().setScoringRules(rules);

      ScoredExtentResult[] sers = wrapper.getQueryEnvironment().runQuery(query,
              hitListSize);
      String[] ids = wrapper.getQueryEnvironment().documentMetadata(sers, "docno");

      System.out.println("SERS: " + sers.length);

      ParsedDocument[] texts = null;

      int count = 0;

      for (int i = 0; i < ids.length; i++) {

        testAliveness();
        if (i % batchSize == 0) {
          ScoredExtentResult[] subSers = Arrays.copyOfRange(sers, i,
                  Math.min(i + batchSize, ids.length));
          texts = wrapper.getQueryEnvironment().documents(subSers);

        }
        int begin = texts[i % batchSize].positions[sers[i].begin].begin;
        int end = texts[i % batchSize].positions[sers[i].end - 1].end;
        int offset = texts[i % batchSize].text.indexOf("<TEXT>") + 6;
        assert offset >= 6;

        // TODO FIX THIS
        
        PassageCandidate r = new PassageCandidate(ids[i], begin - offset, end - offset,
                (float) Math.exp(sers[i].score), query);
        result.add(r);
      }

      // to retrieve enough passages/documents
      if (Math.max(count, ids.length) < hitListSize) {
        // sers = wrapper.getQueryEnvironment().runQuery(backupQuery, hitListSize - Math.max(count,
        // ids.length));
        sers = wrapper.getQueryEnvironment().runQuery(backupQuery, hitListSize);
        String[] ids2 = wrapper.getQueryEnvironment().documentMetadata(sers, "docno");
        texts = null;
        for (int i = 0; i < ids2.length; i++) {
          testAliveness();
          if (i % batchSize == 0) {
            ScoredExtentResult[] subSers = Arrays.copyOfRange(sers, i,
                    Math.min(i + batchSize, ids2.length));
            texts = wrapper.getQueryEnvironment().documents(subSers);
          }
          int begin = texts[i % batchSize].positions[sers[i].begin].begin;
          int end = texts[i % batchSize].positions[sers[i].end - 1].end;
          int offset = texts[i % batchSize].text.indexOf("<TEXT>") + 6;
          assert offset >= 6;
          
          // TODO FIX THIS
          
          PassageCandidate r = new PassageCandidate(ids2[i], begin - offset, end - offset,
                  (float) Math.exp(sers[i].score) / 10, query);
          result.add(r);
        }
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
    return result;
  }
}