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

    this.backupQuery = QueryGenerator.generateIndriQuery(keyterms,"",false);

    String s2 = QueryGenerator.generateIndriQuery(keyterms,"",true);
 
    /*
    s2 = "#filreq( #band (  #syn( COP2 #od1(COP-II) #od1(COP-2) #od1(COPII) #od1(COP2) #od1(COP 2) " +
    		"#od1(COP II))  #syn(CFTR #od1(cystic fibrosis transmembrane conductance regulator)) )" +
    		"#weight(  0.6 #syn( COP2 #od1(COP-II) #od1(COP-2) #od1(COPII) #od1(COP2) #od1(COP 2) #od1(COP II)) " +
    		"0.2 contribute 0.6 #syn(CFTR #od1(cystic fibrosis transmembrane conductance regulator)) 0.6 #syn( export #od1( Nuclear Import) #od1( Peptides NES) " +
    		"#od1( Import Nuclear) #od1( Chromosome Segregation 1 Like Protein) #od1( Transport Nuclear) " +
    		"#od1( CAPTS gene product) #od1( Nuclear Transport) #od1( Transport Nucleo-cytoplasmic) " +
    		"#od1( PHAX protein human) #od1( Nucleo cytoplasmic Transport) #od1( NES Peptides) " +
    		"#od1( CSE1L Protein) #od1( RNUXA protein human) #od1( Transport Nucleocytoplasmic)) " +
    		"0.6 #syn( #od2(endoplasmic reticulum) #od1( Reticulum Endoplasmic) #od1( Ergastoplasm) " +
    		"#od1( endoplasmic) #od1( ERAD Pathways) #od1( Pathway ERAD) " +
    		"#od1( ReticulumAssociated Degradation Endoplasmic) #od1( Reticulum Stress Endoplasmic) " +
    		"#od1( Reticulum-Associated Degradation Endoplasmic)) ) )";
    */
    
    System.out.println("Query~~~:" + s2);
    
    return s2;
  }

  @Override
  protected List<RetrievalResult> retrieveDocuments(String query) {
    
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