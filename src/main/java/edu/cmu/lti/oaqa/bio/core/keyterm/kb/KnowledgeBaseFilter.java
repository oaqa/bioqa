package edu.cmu.lti.oaqa.bio.core.keyterm.kb;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;

import edu.cmu.lti.oaqa.bio.framework.data.BioKeyterm;
import edu.cmu.lti.oaqa.cse.basephase.keyterm.AbstractKeytermUpdater;
import edu.cmu.lti.oaqa.framework.UimaContextHelper;
import edu.cmu.lti.oaqa.framework.data.Keyterm;

public class KnowledgeBaseFilter extends AbstractKeytermUpdater {

  private boolean useTag;

  private Set<String> sources = new HashSet<String>();

  @Override
  public void initialize(UimaContext c) throws ResourceInitializationException {
    super.initialize(c);
    useTag = UimaContextHelper.getConfigParameterBooleanValue(c, "UseTag", true);
    if (UimaContextHelper.getConfigParameterBooleanValue(c, "UseMesh", true)) {
      sources.add("MeSH");
    }
    if (UimaContextHelper.getConfigParameterBooleanValue(c, "UseEntrezGene", true)) {
      sources.add("EntrezGene");
    }
    if (UimaContextHelper.getConfigParameterBooleanValue(c, "UseUmls", true)) {
      sources.add("UMLS");
    }
  }

  @Override
  protected List<Keyterm> updateKeyterms(String question, List<Keyterm> keyterms) {
    List<Keyterm> newKeyterms = new ArrayList<Keyterm>();
    for (Keyterm keyterm : keyterms) {
      BioKeyterm bioKeyterm = (BioKeyterm) keyterm;
      BioKeyterm newKeyterm = new BioKeyterm(keyterm.getText(), bioKeyterm.getSequenceId(),
              bioKeyterm.getTokenType());
      if (useTag) {
        for (String source : bioKeyterm.getAllTagSources()) {
          newKeyterm.addTag(bioKeyterm.getTagBySource(source), source);
        }
      }
      for (String source : bioKeyterm.getAllResourceSources()) {
        for (String acceptSource : sources) {
          if (!source.startsWith(acceptSource)) {
            continue;
          }
          newKeyterm.addExternalResource(bioKeyterm.getConceptBySource(source),
                  bioKeyterm.getCategoryBySource(source), bioKeyterm.getSynonymsBySource(source),
                  source);
        }
      }
      newKeyterms.add(newKeyterm);
    }
    return newKeyterms;
  }
}
