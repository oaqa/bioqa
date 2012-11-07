package edu.cmu.lti.oaqa.bio.test.yanfang.retrieval;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import lemurproject.indri.ScoredExtentResult;

import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;

import edu.cmu.lti.oaqa.bio.core.retrieval.DefaultRetrievalStrategist;
import edu.cmu.lti.oaqa.bio.retrieval.query.strategy.QueryGenerator;
import edu.cmu.lti.oaqa.bio.retrieval.query.strategy.QueryStrategy;
import edu.cmu.lti.oaqa.bio.retrieval.query.structure.QueryComponentContainer;
import edu.cmu.lti.oaqa.framework.UimaContextHelper;
import edu.cmu.lti.oaqa.framework.data.Keyterm;
import edu.cmu.lti.oaqa.framework.data.RetrievalResult;

/**
 * this strategy integrated synonyms, lexical variants, etc.
 * 
 * @author yanfang <yanfang@cmu.edu>
 */
public class IndriRetrievalStrategist extends DefaultRetrievalStrategist {

  private Integer hitListSize;

  private String smoothing;

  private String smoothingMu;

  private String smoothingLambda;

  private String backupQuery;

  @Override
  public void initialize(UimaContext aContext) throws ResourceInitializationException {
    super.initialize(aContext);
    
    // Gets values from the yaml files
    this.hitListSize = (Integer) aContext.getConfigParameterValue("hit-list-size");
    this.smoothing = (String) aContext.getConfigParameterValue("smoothing");
    this.smoothingMu = (String) aContext.getConfigParameterValue("smoothing-mu");
    this.smoothingLambda = (String) aContext.getConfigParameterValue("smoothing-lambda");

  }

  @Override
  protected String formulateQuery(List<Keyterm> keyterms) {

    //HashMap<String, Boolean> map = new HashMap<String, Boolean>();
    
    /*
    map.put("umls", useUMLS);
    map.put("entrez", useENTREZ);
    map.put("mesh", useMESH);
    map.put("lexical_variants", useLexicalVariants);
    map.put("postagger", usePosTagger);
    map.put("acronym_umls", useUMLSAcronym);
    map.put("acronym_entrez", useENTREZAcronym);
    map.put("acronym_mesh", useMESHAcronym);
    */
    this.backupQuery = QueryGenerator.generateIndriQuery(keyterms,"",false);

    String s2 = QueryGenerator.generateIndriQuery(keyterms,"",true);
    System.out.println("Query~~~:" + s2);

    return s2;
  }

  @Override
  protected List<RetrievalResult> retrieveDocuments(String query) {

    query = "#filreq( #band (  #syn( Pes #od1( CAPOS) #od1( Rocker-bottom foot) #od1( Reppal PES100) #od1( Krauss Herman Holmes syndrome) #od1( Reppal PES-100) #od1( Rockerbottom foot))" +
    		"" +
    		")" +
    		"" +
    		"" +
    		"#weight(  0.6 #od2(cell growth) " +
    		"0.6 #syn( growth #od1( postnatal development) #od1( Endo-GF) #od1( EndoGF) #od1( Humatrope) #od1( Zomacton) #od1( r-hGH m) #od1( rhGH m) #od1( Somatotropin Human) #od1( Omnitrope) #od1( CryoTropin) #od1( Norditropin) #od1( Maxomat) #od1( Norditropin Simplexx) #od1( Genotropin) #od1( Somatropin Human) #od1( development) #od1( ECDGF) #od1( Saizen) #od1( Umatrope) #od1( Serostim) #od1( Nutropin) #od1( Genotonorm) #od1( Somatropin)) 0.6 #syn( cell #od1( dmTAF1) #od1( TAF250) #od1( TFIID) #od1( dTAF II 250) #od1( TAF II 250 230) #od1( cel) #od1( l 3 84Ab) #od1( dmTAF II 230) #od1( dTAF250) #od1( p230) #od1( BG DS0000413) #od1( EfW1) #od1( SR3-5) #od1( SR35) #od1( CG17603) #od1( zgc 112377) #od1( TFIID TAF250) #od1( Taf1p) #od1( DmelCG17603) #od1( d230) #od1( T-Cell Leukemia-Lymphoma Human) #od1( TCell LeukemiaLymphoma Human) #od1( T-Cell Leukemia-Lymphomas Adult) #od1( TCell LeukemiaLymphomas Adult) #od1( Adult T-Cell Leukemia-Lymphoma) #od1( Adult TCell LeukemiaLymphoma) #od1( Leukemia-Lymphomas HTLV-Associated) #od1( LeukemiaLymphomas HTLVAssociated) #od1( HTLV Associated Leukemia Lymphoma) #od1( NKG2-E Receptor) #od1( NKG2E Receptor) #od1( T-Cell Leukemias Adult) #od1( TCell Leukemias Adult) #od1( HTLV-I-Associated T-Cell Leukemia-Lymphomas) #od1( HTLVIAssociated TCell LeukemiaLymphomas) #od1( Adult T-Cell Leukemia) #od1( Adult TCell Leukemia) #od1( NKG2A Receptor) #od1( Human T Lymphotropic Virus-Associated Leukemia-Lymphoma) #od1( Human T Lymphotropic VirusAssociated LeukemiaLymphoma) #od1( Human T-Cell Leukemia-Lymphomas) #od1( Human TCell LeukemiaLymphomas) #od1( Receptor NKG2A) #od1( Receptor NKG2B) #od1( Receptor NKG2C) #od1( Receptor NKG2E) #od1( Leukemias Adult T-Cell) #od1( Leukemias Adult TCell) #od1( Leukemia-Lymphomas HTLV-I-Associated T-Cell) #od1( LeukemiaLymphomas HTLVIAssociated TCell) #od1( NKG2-C Receptor) #od1( NKG2C Receptor) #od1( Receptor NKG2-F) #od1( Receptor NKG2F) #od1( Human T Lymphotropic Virus Associated Leukemia Lymphoma) #od1( NKG2 F Receptor) #od1( NKG2B Receptor) #od1( ATLL) #od1( Leukemia-Lymphomas Human T-Cell) #od1( LeukemiaLymphomas Human TCell) #od1( Leukemia-Lymphomas Adult T-Cell) #od1( LeukemiaLymphomas Adult TCell) #od1( T-Cell Leukemia-Lymphomas HTLV-I-Associated) #od1( TCell LeukemiaLymphomas HTLVIAssociated) #od1( Leukemia-Lymphoma T-Cell Acute HTLV-I-Associated) #od1( LeukemiaLymphoma TCell Acute HTLVIAssociated) #od1(  carboxyl ester lipase like)) 0.2 affect " +
    		"" +
    		"0.6 #syn( Pes #od1( CAPOS) #od1( Rocker-bottom foot) #od1( Reppal PES100) #od1( Krauss Herman Holmes syndrome) #od1( Reppal PES-100) #od1( Rockerbottom foot)) " +
    		"" +
    		"" +
    		"0.2 mutations ) )";
    
    
    ArrayList<RetrievalResult> result = new ArrayList<RetrievalResult>();

    // set smoothing parameters for Indri here
    String rule = "";
    if (this.smoothing.startsWith("j"))
      rule = "method:" + this.smoothing + "," + "collectionLambda:" + this.smoothingLambda;
    if (this.smoothing.startsWith("d"))
      rule = "method:" + this.smoothing + "," + "mu:" + this.smoothingMu;
    if (this.smoothing.startsWith("t"))
      rule = "method:" + this.smoothing + "," + "lambda:" + this.smoothingLambda + "," + "mu:"
              + this.smoothingMu;
    String[] rules = { rule };

    try {

      // set retrieval rules for Indri
      wrapper.getQueryEnvironment().setScoringRules(rules);

      ScoredExtentResult[] sers = wrapper.getQueryEnvironment().runQuery(query, hitListSize);
      String[] docnos = wrapper.getQueryEnvironment().documentMetadata(sers, "docno");
      String[] docnos2 = new String[hitListSize];

      for (int i = 0; i < docnos.length; i++) {
        RetrievalResult r = new RetrievalResult(docnos[i], (float)Math.exp(sers[i].score), query);
        result.add(r);
      }

      /*
       * If there are not enough documents retrieved from boolean complex query, use general complex
       * query to guarantee enough documents
       */
      if (docnos.length < hitListSize) {
        sers = wrapper.getQueryEnvironment().runQuery(backupQuery, hitListSize - docnos.length);
        docnos2 = wrapper.getQueryEnvironment().documentMetadata(sers, "docno");
        for (int j = 0; j < docnos2.length; j++) {
         RetrievalResult r = new RetrievalResult(docnos2[j], (float) Math.exp(sers[j].score) / 10,
                  backupQuery);
          result.add(r);
        }
      }
      
    } catch (Exception e) {
      System.err.println("Error retrieving documents from Indri: " + e);
    }
    return result;
  }

}