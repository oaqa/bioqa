package edu.cmu.lti.oaqa.bio.core.keyterm.kb;

import java.util.ArrayList;
import java.util.List;

import edu.cmu.lti.oaqa.bio.framework.data.BioKeyterm;
import edu.cmu.lti.oaqa.framework.data.Keyterm;

public class Vocabulary {

  public static final String[] terms = { "PrnP", "mad", "IDE", "APC", "CTSD", "ApoE", "TGF-beta1",
      "NM23", "BRCA1", "APC", "CFTR", "T", "p53", "alpha7", "cystic", "HNF4", "COUP-TF1",
      "Huntingtin", "Hedgehog", "Pes", "Presenilin-1", "PSD-95", "smooth", "PmrD", "CD44", "LITAF",
      "LPS", "HIV", "NFkappaB", "iNOS" };

  public static final List<Keyterm> keyterms = new ArrayList<Keyterm>();

  static {
    for (String term : terms) {
      keyterms.add(new BioKeyterm(term, 0, 0));
    }
  }
}
