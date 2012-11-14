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

    /*
    String x = "#syn(  #od1(transforming growth factor beta 1) TGF-beta1 #od1( DNA segment Chr X Brigham and Womens Genetics 1396 expressed protein mouse) #od1( DENTT protein mouse) #od1( TGF-beta1 Latency-Associated Protein) #od1( TGF-beta-1) #od1( transforming growth factor beta1 binding protein 1 human) #od1( differentially expressed nucleolar TGFbeta1 target protein mouse) #od1( DENTT protein human) #od1( P144 peptide) #od1(TGF-beta I) #od1( Latency-Associated Protein TGF-beta1) #od1(TGF beta 1) #od1( differentially expressed nucleolar TGF-beta1 target protein mouse) #od1( TGF-beta1LAP) #od1( TGFbeta1BP1 protein human) #od1( latent transforming growth factor beta binding protein 1 human) #od1( TGFbeta1) #od1(TGF-beta-1) #od1( TGF beta1 Latency Associated Protein) #od1(TGF-beta 1) #od1( CINAP protein human) #od1(TGF beta I) #od1( cutaneous T-cell lymphoma-associated tumor antigen se20-4 protein human) #od1( differentially expressed nucleolar TGFbeta1 target protein human) #od1(TGF beta1) #od1( differentially expressed nucleolar TGF-beta1 target protein human) #od1(TGFbetaI) #od1(TGFbeta-1) #od1(TGF beta-I) #od1(TGF-beta-I) #od1(TGFbeta-I) #od1( hCINAP protein human) #od1(TGF beta-1) #od1(TGFbeta I) #od1(TGFbeta1) #od1( Transforming Growth Factor beta 1 Latency Associated Peptide) #od1( TGFbeta1LAP) #od1(TGFbeta 1) #od1(TGF-beta1) #od1(TGFp) #od1( beta-glycan 730-743 human) #od1( LatencyAssociated Protein TGFbeta1) #od1( cutaneous Tcell lymphomaassociated tumor antigen se204 protein human) #od1( TGFbeta1 LatencyAssociated Protein) #od1( transforming growth factor beta-1 binding protein 1 human) #od1(TGF) #od1( TSPX protein human) #od1( TGF beta1LAP) #od1( DXBwg1396e protein mouse) #od1(TGF betaI) #od1(TGF-betaI) #od1( TGF-beta1-BP-1 protein human) #od1( Transforming Growth Factor beta I) #od1( betaglycan 730743 human) #od1( TGF-beta1))";
    //String x = "#syn( TGF-beta1 #od1( Tgfb) #od1( TGF-beta1-BP-1 protein human) #od1( TGFbeta1BP1 protein human) #od1( hCINAP protein human) #od1( P144 peptide) #od1( beta-glycan 730-743 human) #od1( betaglycan 730743 human) #od1( latent transforming growth factor beta binding protein 1 human) #od1( CINAP protein human) #od1( TGF beta1 Latency Associated Protein) #od1( DENTT protein human) #od1( TSPX protein human) #od1( Transforming Growth Factor beta 1 Latency Associated Peptide) #od1( transforming growth factor beta-1 binding protein 1 human) #od1( transforming growth factor beta1 binding protein 1 human) #od1( DNA segment Chr X Brigham and Womens Genetics 1396 expressed protein mouse) #od1( DENTT protein mouse) #od1( cutaneous T-cell lymphoma-associated tumor antigen se20-4 protein human) #od1( cutaneous Tcell lymphomaassociated tumor antigen se204 protein human) #od1( TGF-beta1LAP) #od1( TGFbeta1LAP) #od1( Transforming Growth Factor beta I) #od1( DXBwg1396e protein mouse) #od1(  transforming growth factor beta 1) #od1(TGF beta1) #od1(TGFbetaI) #od1(TGFbeta-1) #od1(TGF beta-I) #od1(TGF-beta-I) #od1(TGFbeta-I) #od1(TGF beta-1) #od1(TGF-beta I) #od1(TGFbeta I) #od1(TGF beta 1) #od1(TGFbeta1) #od1(TGFbeta 1) #od1(TGF-beta1) #od1(TGFp) #od1(TGF-beta-1) #od1(TGF) #od1(TGF-beta 1) #od1(TGF beta I) #od1(TGF betaI) #od1(TGF-betaI))";
    
    query = "#filreq( #band (  " +
    		//"#syn( TGF-beta1 #od1( DNA segment Chr X Brigham and Womens Genetics 1396 expressed protein mouse) #od1( DENTT protein mouse) #od1( TGF-beta1 Latency-Associated Protein) #od1( TGF-beta-1) #od1( transforming growth factor beta1 binding protein 1 human) #od1( differentially expressed nucleolar TGFbeta1 target protein mouse) #od1( DENTT protein human) #od1( P144 peptide) #od1(TGF-beta I) #od1( Latency-Associated Protein TGF-beta1) #od1(TGF beta 1) #od1( differentially expressed nucleolar TGF-beta1 target protein mouse) #od1( TGF-beta1LAP) #od1( TGFbeta1BP1 protein human) #od1( latent transforming growth factor beta binding protein 1 human) #od1( TGFbeta1) #od1(TGF-beta-1) #od1( TGF beta1 Latency Associated Protein) #od1(TGF-beta 1) #od1( CINAP protein human) #od1(TGF beta I) #od1( cutaneous T-cell lymphoma-associated tumor antigen se20-4 protein human) #od1( differentially expressed nucleolar TGFbeta1 target protein human) #od1(TGF beta1) #od1( differentially expressed nucleolar TGF-beta1 target protein human) #od1(TGFbetaI) #od1(TGFbeta-1) #od1(TGF beta-I) #od1(TGF-beta-I) #od1(TGFbeta-I) #od1( hCINAP protein human) #od1(TGF beta-1) #od1(TGFbeta I) #od1(TGFbeta1) #od1( Transforming Growth Factor beta 1 Latency Associated Peptide) #od1( TGFbeta1LAP) #od1(TGFbeta 1) #od1(TGF-beta1) #od1(TGFp) #od1( beta-glycan 730-743 human) #od1( LatencyAssociated Protein TGFbeta1) #od1( cutaneous Tcell lymphomaassociated tumor antigen se204 protein human) #od1( TGFbeta1 LatencyAssociated Protein) #od1( transforming growth factor beta-1 binding protein 1 human) #od1(TGF) #od1( TSPX protein human) #od1( TGF beta1LAP) #od1( DXBwg1396e protein mouse) #od1(TGF betaI) #od1(TGF-betaI) #od1( TGF-beta1-BP-1 protein human) #od1( Transforming Growth Factor beta I) #od1( betaglycan 730743 human) #od1( TGF-beta1))" 
          x  +    		
    		"#syn( #od2(Transforming growth factor-beta1) #od1( TGFbeta12) #od1( peptantagonist TGFbeta1 41-65) #od1( TGF-beta1 Latency-Associated Protein) #od1( TGF-beta-1) #od1( TGFbeta1 41-65) #od1( TGFbeta1 4165) #od1( Transforming) #od1( TGF-beta12) #od1( peptantagonist TGFbeta1 4165) #od1( Latency-Associated Protein TGF-beta1) #od1( TGFbeta1LAP) #od1( Transforming Growth Factor beta 1 Latency Associated Peptide) #od1( LatencyAssociated Protein TGFbeta1) #od1( TGF-beta1LAP) #od1( TGF beta 1) #od1( TGFbeta1) #od1( TGF beta1 Latency Associated Protein) #od1( TGFbeta1 LatencyAssociated Protein) #od1( TGF beta1LAP) #od1( Transforming Growth Factor beta I) #od1( TGF-beta1)))" +
    		"" +
    		"#weight(  0.2 role 0.6 " +
    		//"#syn( TGF-beta1 #od1( DNA segment Chr X Brigham and Womens Genetics 1396 expressed protein mouse) #od1( DENTT protein mouse) #od1( TGF-beta1 Latency-Associated Protein) #od1( TGF-beta-1) #od1( transforming growth factor beta1 binding protein 1 human) #od1( differentially expressed nucleolar TGFbeta1 target protein mouse) #od1( DENTT protein human) #od1( P144 peptide) #od1(TGF-beta I) #od1( Latency-Associated Protein TGF-beta1) #od1(TGF beta 1) #od1( differentially expressed nucleolar TGF-beta1 target protein mouse) #od1( TGF-beta1LAP) #od1( TGFbeta1BP1 protein human) #od1( latent transforming growth factor beta binding protein 1 human) #od1( TGFbeta1) #od1(TGF-beta-1) #od1( TGF beta1 Latency Associated Protein) #od1(TGF-beta 1) #od1( CINAP protein human) #od1(TGF beta I) #od1( cutaneous T-cell lymphoma-associated tumor antigen se20-4 protein human) #od1( differentially expressed nucleolar TGFbeta1 target protein human) #od1(TGF beta1) #od1( differentially expressed nucleolar TGF-beta1 target protein human) #od1(TGFbetaI) #od1(TGFbeta-1) #od1(TGF beta-I) #od1(TGF-beta-I) #od1(TGFbeta-I) #od1( hCINAP protein human) #od1(TGF beta-1) #od1(TGFbeta I) #od1(TGFbeta1) #od1( Transforming Growth Factor beta 1 Latency Associated Peptide) #od1( TGFbeta1LAP) #od1(TGFbeta 1) #od1(TGF-beta1) #od1(TGFp) #od1( beta-glycan 730-743 human) #od1( LatencyAssociated Protein TGFbeta1) #od1( cutaneous Tcell lymphomaassociated tumor antigen se204 protein human) #od1( TGFbeta1 LatencyAssociated Protein) #od1( transforming growth factor beta-1 binding protein 1 human) #od1(TGF) #od1( TSPX protein human) #od1( TGF beta1LAP) #od1( DXBwg1396e protein mouse) #od1(TGF betaI) #od1(TGF-betaI) #od1( TGF-beta1-BP-1 protein human) #od1( Transforming Growth Factor beta I) #od1( betaglycan 730743 human) #od1( TGF-beta1)) " +
    		x +
    		"0.6 #syn( cerebral #od1( MCA Infarction) #od1( Brains) #od1( Traumatic Intracerebral Hemorrhages) #od1( Hemorrhage Traumatic Intracerebral) #od1( Littles Disease) #od1( Intracerebral Hemorrhages Traumatic) #od1( Spastic Diplegia) #od1( Encephalon) #od1( Diplegia Spastic)) " +
    		"" +
    		"0.6 #syn( CAA #od1(cerebral amyloid angiopathy)) " +
    		"" +
    		"0.6 #syn( #od2(Transforming growth factor-beta1) #od1( TGFbeta12) #od1( peptantagonist TGFbeta1 41-65) #od1( TGF-beta1 Latency-Associated Protein) #od1( TGF-beta-1) #od1( TGFbeta1 41-65) #od1( TGFbeta1 4165) #od1( Transforming) #od1( TGF-beta12) #od1( peptantagonist TGFbeta1 4165) #od1( Latency-Associated Protein TGF-beta1) #od1( TGFbeta1LAP) #od1( Transforming Growth Factor beta 1 Latency Associated Peptide) #od1( LatencyAssociated Protein TGFbeta1) #od1( TGF-beta1LAP) #od1( TGF beta 1) #od1( TGFbeta1) #od1( TGF beta1 Latency Associated Protein) #od1( TGFbeta1 LatencyAssociated Protein) #od1( TGF beta1LAP) #od1( Transforming Growth Factor beta I) #od1( TGF-beta1)) " +
    		"" +
    		"0.6 #syn( #od2(amyloid angiopathy) #od1( gammatrace protein human) #od1( Angiopathy Congophilic) #od1( Amyloidosis Icelandic Type) #od1( Presenile dementia with spastic ataxia) #od1( Cerebral Amyloid Angiopathies) #od1( gammatrace alkaline microprotein human) #od1( amyloid) #od1( Cystatin C protein human) #od1( Angiopathy Cerebral Amyloid) #od1( Icelandic Type Amyloidosis) #od1( cystatin C) #od1( post-gamma-globulin protein human) #od1( gamma-trace protein human) #od1( postgammaglobulin protein human) #od1( Congophilic Angiopathies) #od1( gamma-trace alkaline microprotein human) #od1( Neuroendocrine basic polypeptide human) #od1( Amyloidosis  cerebral)) ) )";
    */
    
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