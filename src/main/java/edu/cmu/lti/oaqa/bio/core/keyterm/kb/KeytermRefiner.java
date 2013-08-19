package edu.cmu.lti.oaqa.bio.core.keyterm.kb;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;

import edu.cmu.lti.oaqa.bio.framework.data.BioKeyterm;
import edu.cmu.lti.oaqa.bio.utils.retrieval.query.strategy.QueryStrategy;
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
 * From the probability, you can decide whether you should use the keyterm or not. Which resources
 * to use is specified in the yaml file.
 * 
 * New keyterm synonsym called "RefinedSynonyms"
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

  private String conceptWeight;

  private String regularWeight;

  private String verbWeight;

  private String geneWeight;

  private String specialWeight;

  private String mustHaveTermWeight;

  private boolean customizedDictionary;

  @Override
  public void initialize(UimaContext aContext) throws ResourceInitializationException {
    super.initialize(aContext);

    // get information from the yaml file
    // TODO
    // this.useENTREZ = UimaContextHelper.getConfigParameterBooleanValue(aContext, "ENTREZ", false);

    this.useMESH = UimaContextHelper.getConfigParameterBooleanValue(aContext, "MESH", false);

    this.useENTREZ = this.useMESH;

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
    this.conceptWeight = String.valueOf(UimaContextHelper.getConfigParameterFloatValue(aContext,
            "concept-term-weight", 0.6F));
    this.regularWeight = String.valueOf(UimaContextHelper.getConfigParameterFloatValue(aContext,
            "regular-term-weight", 0.4F));
    this.verbWeight = String.valueOf(UimaContextHelper.getConfigParameterFloatValue(aContext,
            "verb-term-weight", 0.2F));
    this.geneWeight = String.valueOf(UimaContextHelper.getConfigParameterFloatValue(aContext,
            "gene-term-weight", 0.3F));
    this.specialWeight = String.valueOf(UimaContextHelper.getConfigParameterFloatValue(aContext,
            "special-term-weight", 0.3F));
    this.mustHaveTermWeight = String.valueOf(UimaContextHelper.getConfigParameterFloatValue(
            aContext, "must-have-term-weight", 0.6F));
    this.customizedDictionary = UimaContextHelper.getConfigParameterBooleanValue(aContext,
            "customized-dictionary", true);
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
    refiner.setConceptTermWeight(this.conceptWeight);
    refiner.setRegularTermWeight(this.regularWeight);
    refiner.setVerbTermWeight(this.verbWeight);
    refiner.setGeneTermWeight(this.geneWeight);
    refiner.setSpecialTermWeight(this.specialWeight);
    refiner.setMustHaveTermWeight(this.mustHaveTermWeight);
    refiner.setCustomizedDictionary(this.customizedDictionary);

    for (BioKeyterm bioK : refiner.getRefinedKeyterms()) {
      result.add(bioK);
    }
    return result;
  }

}
