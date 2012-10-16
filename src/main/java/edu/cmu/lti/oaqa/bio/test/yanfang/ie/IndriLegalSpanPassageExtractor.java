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

  private String conceptTermWeight;

  private String regularTermWeight;

  private String backupQuery = "";
  
  private boolean useUMLS;

  private boolean useENTREZ;

  private boolean useMESH;
  
  private boolean useUMLSAcronym;

  private boolean useENTREZAcronym;

  private boolean useMESHAcronym;

  private boolean useLexicalVariants;

  private boolean usePosTagger;

  @Override
  public void initialize(UimaContext aContext) throws ResourceInitializationException {
    super.initialize(aContext);

    this.smoothing = (String) aContext.getConfigParameterValue("smoothing");
    this.smoothingMu = (String) aContext.getConfigParameterValue("smoothing-mu");
    this.smoothingLambda = (String) aContext.getConfigParameterValue("smoothing-lambda");
    this.conceptTermWeight = (String) aContext.getConfigParameterValue("concept-term-weight");
    this.regularTermWeight = (String) aContext.getConfigParameterValue("regular-term-weight");
    this.useENTREZ = UimaContextHelper.getConfigParameterBooleanValue(aContext, "ENTREZ", false);
    this.useMESH = UimaContextHelper.getConfigParameterBooleanValue(aContext, "MESH", false);
    this.useUMLS = UimaContextHelper.getConfigParameterBooleanValue(aContext, "UMLS", false);
    this.useENTREZAcronym = UimaContextHelper.getConfigParameterBooleanValue(aContext, "ENTREZ-Acronym", false);
    this.useMESHAcronym = UimaContextHelper.getConfigParameterBooleanValue(aContext, "MESH-Acronym", false);
    this.useUMLSAcronym = UimaContextHelper.getConfigParameterBooleanValue(aContext, "UMLS-Acronym", false);
    this.useLexicalVariants = UimaContextHelper.getConfigParameterBooleanValue(aContext,
            "LexicalVariants", false);
    this.usePosTagger = UimaContextHelper.getConfigParameterBooleanValue(aContext, "PosTagger",
            false);
  }

  @Override
  protected String formulateQuery(List<Keyterm> keyterms) {

    /*
    QueryGenerator qs = new QueryGenerator(keyterms);

    HashMap<String, Boolean> map = new HashMap<String, Boolean>();
    
    map.put("umls", useUMLS);
    map.put("entrez", useENTREZ);
    map.put("mesh", useMESH);
    map.put("lexical_variants", useLexicalVariants);
    map.put("postagger", usePosTagger);
    map.put("acronym_umls", useUMLSAcronym);
    map.put("acronym_entrez", useENTREZAcronym);
    map.put("acronym_mesh", useMESHAcronym);
    */
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

    String[] rules = { rule };

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
        
        //PassageCandidate r = new PassageCandidate(ids[i], begin - offset, end - offset,
          //      Math.exp(sers[i].score), query);
        //result.add(r);
      }

      // to retrieve enough passages/documents
      if (Math.max(count, ids.length) < hitListSize) {
        // sers = wrapper.getQueryEnvironment().runQuery(backupQuery, hitListSize - Math.max(count,
        // ids.length));
        sers = wrapper.getQueryEnvironment().runQuery(backupQuery, hitListSize);
        String[] ids2 = wrapper.getQueryEnvironment().documentMetadata(sers, "docno");
        texts = null;
        for (int i = 0; i < ids2.length; i++) {
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
          
          //PassageCandidate r = new PassageCandidate(ids2[i], begin - offset, end - offset,
            //      Math.exp(sers[i].score) / 10, query);
          //result.add(r);
        }
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
    return result;
  }
}