package edu.cmu.lti.oaqa.bio.keyterm.yanfang;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;

import edu.cmu.lti.oaqa.bio.core.keyterm.AbstractKeytermUpdater;
import edu.cmu.lti.oaqa.bio.framework.data.BioKeyterm;
import edu.cmu.lti.oaqa.bio.retrieval.query.strategy.QueryStrategy;
import edu.cmu.lti.oaqa.bio.retrieval.query.structure.QueryComponent;
import edu.cmu.lti.oaqa.bio.retrieval.query.structure.QueryComponentContainer;
import edu.cmu.lti.oaqa.framework.UimaContextHelper;
import edu.cmu.lti.oaqa.framework.data.Keyterm;

public class KeytermRefiner extends AbstractKeytermUpdater {

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
  
  // get information from the yaml file  
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
  protected List<Keyterm> updateKeyterms(String question, List<Keyterm> keyterms) {
    // TODO Auto-generated method stub
      
      List<BioKeyterm> bioKeyterms = new ArrayList<BioKeyterm>();
      List<Keyterm> result = new ArrayList<Keyterm>();
    
      for (Keyterm keyterm : keyterms) {
        bioKeyterms.add((BioKeyterm)keyterm); 
      }
      
      QueryStrategy refiner = new QueryStrategy(bioKeyterms);
      
      // set up the usage of resources
      refiner.hasUMLS(useUMLS);
      refiner.hasEntrez(useENTREZ);
      refiner.hasMESH(useMESH);
      refiner.hasLexicalVariants(useLexicalVariants);
      refiner.hasPOSTagger(usePosTagger);
      refiner.hasUMLSAcronym(useUMLSAcronym);
      refiner.hasEntrezAcronym(useENTREZAcronym);
      refiner.hasMESHAcronym(useMESHAcronym);
      
      QueryComponentContainer qc = refiner.getAllQueryComponents();

      for(BioKeyterm keyterm : bioKeyterms) {
        for(QueryComponent q: qc.getConceptQueryComponent()) {
          
          // only change the terms that will be used for query
          if(q.getKeyterm().getText().endsWith(keyterm.getText())) {
            // TODO test if this is correct or not. Because this may be replaced by simple weight
            // Though the probablity is 1, the weight actually should be 0.6, not 1.
            if(q.isConcept()) keyterm.setProbablity(1);
            else
              keyterm.setProbablity(Float.valueOf(q.getWeight()));

              // add as a kind of the external resource. The synonyms here should be the correct one.
              keyterm.addExternalResource("", "" ,q.getSynonyms(), "RefinedSynonyms");
              break;
          }
        }
        result.add(keyterm);
      }
      return keyterms;
    }
    
}
