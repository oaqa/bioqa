package edu.cmu.lti.oaqa.bio.test.yanfang.keyterm;

import java.util.HashMap;
import java.util.List;

import edu.cmu.lti.oaqa.bio.framework.data.BioKeyterm;
import edu.cmu.lti.oaqa.bio.retrieval.tools.ExtractAbbrev;
import edu.cmu.lti.oaqa.cse.basephase.keyterm.AbstractKeytermUpdater;
import edu.cmu.lti.oaqa.framework.data.Keyterm;


/**
 * get the abbreviation or long form for the keyterms
 * 
 * @author yanfang <yanfang@cmu.edu>
 *
 */
public class AddAbbreviationAndLongForm extends AbstractKeytermUpdater {

    @Override
    protected List<Keyterm> updateKeyterms(String question, List<Keyterm> keyterms) {
        ExtractAbbrev extracAbb = new ExtractAbbrev();
        HashMap<String, String> abbreviationAndLongForm;
        if (extracAbb.abbreviateExtractor(question) != null) {
            abbreviationAndLongForm = extracAbb.abbreviateExtractor(question);
            for (Keyterm keyterm : keyterms) {
                BioKeyterm bk = (BioKeyterm) keyterm;
                if (abbreviationAndLongForm.containsKey(keyterm.getText())) {
                    bk.addSynonym(abbreviationAndLongForm.get(keyterm.getText()),
                                    "AbbreviationAndLongForm");
                }
                keyterm = bk;
            }
        }
        return keyterms;
    }

}