package edu.cmu.lti.oaqa.bio.test.yanfang.keyterm;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;

import edu.cmu.lti.oaqa.bio.framework.data.BioKeyterm;
import edu.cmu.lti.oaqa.bio.retrieval.query.strategy.QueryStrategy;
import edu.cmu.lti.oaqa.bio.retrieval.query.structure.QueryComponent;
import edu.cmu.lti.oaqa.bio.retrieval.query.structure.QueryComponentContainer;
import edu.cmu.lti.oaqa.cse.basephase.keyterm.AbstractKeytermUpdater;
import edu.cmu.lti.oaqa.framework.UimaContextHelper;
import edu.cmu.lti.oaqa.framework.data.Keyterm;

/**
 * This class is to refine the keyterms by
 * <ul>
 * <li>removing duplicated synonyms and adding a new synonyms resource called RefinedSynonyms</li>
 * <li>adding probability (a.k.a. score) for each keyterm</li>
 * </ul>
 * 
 * From the probability, you can decide whether you should use the keyterm or not. Which resources to
 * use is specified in the yaml file.
 * 
 * @author yanfang (yanfang@cmu.edu)
 */
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
    this.useENTREZAcronym = UimaContextHelper.getConfigParameterBooleanValue(aContext,
            "ENTREZ-Acronym", false);
    this.useMESHAcronym = UimaContextHelper.getConfigParameterBooleanValue(aContext,
            "MESH-Acronym", false);
    this.useUMLSAcronym = UimaContextHelper.getConfigParameterBooleanValue(aContext,
            "UMLS-Acronym", false);
    this.useLexicalVariants = UimaContextHelper.getConfigParameterBooleanValue(aContext,
            "LexicalVariants", false);
    this.usePosTagger = UimaContextHelper.getConfigParameterBooleanValue(aContext, "PosTagger",
            false);
  }

  @Override
  protected List<Keyterm> updateKeyterms(String question, List<Keyterm> keyterms) {

    List<BioKeyterm> bioKeyterms = new ArrayList<BioKeyterm>();
    List<Keyterm> result = new ArrayList<Keyterm>();

    for (Keyterm keyterm : keyterms) {
      bioKeyterms.add((BioKeyterm) keyterm);
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

    //QueryComponentContainer qc = refiner.getAllQueryComponents();

    
    /*
    for (BioKeyterm keyterm : bioKeyterms) {
      
     // System.out.println(keyterm.getText());
      
      for (QueryComponent q : qc.getQueryComponent()) {
        
        
        // TODO take care of the phrases
        
        // only change the terms that will be used for query.
        if (q.getKeyterm().getText().toLowerCase().equals(keyterm.getText().toLowerCase())) {
          // TODO test if this is correct or not. Because this may be replaced by simple weight
          // Though the probablity is 1, the weight actually should be 0.6, not 1.
          System.out.println(q.getKeyterm().getText());
          
          if (q.isConcept())
            keyterm.setProbablity(1);
          else
            keyterm.setProbablity(Float.valueOf(q.getWeight()));
          // add as a kind of the external resource. The synonyms here should be the correct one.
          keyterm.addExternalResource("", "", q.getSynonyms(), "RefinedSynonyms");
          break;
        }
      }
      result.add(keyterm);
    }
    */
    
    //keyterms = (List<Keyterm>) (List<?>) refiner.getRefinedKeyterms();
    
    for(BioKeyterm bioK : refiner.getRefinedKeyterms()) {
      result.add(bioK);
      //System.out.println(bioK.getText());
      //System.out.println(bioK.getProbability());
      //System.out.println(bioK.getSynonymsBySource("RefinedSynonyms"));
    }
    return result;
  }

}