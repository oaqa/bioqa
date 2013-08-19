package edu.cmu.lti.oaqa.bio.utils.retrieval.tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import edu.cmu.lti.oaqa.bio.utils.retrieval.tools.CleanTerms;

/**
 * This class is inspired by Jorg Hakenberg's work, a.k.a. Keywords indicating relations between
 * biological objects, (see http://www2.informatik.hu-berlin.de/~hakenber/corpora/interactors.html)
 * It contains the keywords for protein-protein relation and their synonyms/hyponyms.
 * 
 * @author yanfang <yanfang@cmu.edu>
 * @version 0.1 (only work for Indri)
 */

public class ProteinProteinRelationKeywords {

  CleanTerms cleaner = new CleanTerms();

  HashMap<ArrayList<String>, String> storage = new HashMap<ArrayList<String>, String>();

  ArrayList<String> group1List = new ArrayList<String>();

  ArrayList<String> group2List = new ArrayList<String>();

  ArrayList<String> group3List = new ArrayList<String>();

  ArrayList<String> group4List = new ArrayList<String>();

  ArrayList<String> group5List = new ArrayList<String>();

  ArrayList<String> group6List = new ArrayList<String>();

  ArrayList<String> group7List = new ArrayList<String>();

  ArrayList<String> group8List = new ArrayList<String>();

  ArrayList<String> group9List = new ArrayList<String>();

  ArrayList<String> group10List = new ArrayList<String>();

  ArrayList<String> group11List = new ArrayList<String>();

  ArrayList<String> group12List = new ArrayList<String>();

  ArrayList<String> group13List = new ArrayList<String>();

  ArrayList<String> group14List = new ArrayList<String>();

  ArrayList<String> group15List = new ArrayList<String>();

  ArrayList<String> group16List = new ArrayList<String>();

  public ProteinProteinRelationKeywords() {

    String group1 = "activation accumulation elevation hasten incitation "
            + "increase induce initiation promotion stimulation " + "transactivation upregulation";
    String group2 = "association";
    String group3 = "attachment addendum apparatus bond catalyzation cluster complex fusion ligand";
    String group4 = "#od1(break bond) cleavage demethylation dephosphorylation severance"; // the
                                                                                           // #od
                                                                                           // is
                                                                                           // only
                                                                                           // for
                                                                                           // Indri
    String group5 = "cause influence";
    String group6 = "containment";
    String group7 = "#od1(create bond) methylation phosphorylation";
    String group8 = "generation expression hyperexpression hyper-expression overexpression production";
    String group9 = "inactivation block decrease depletion downregulation impairment inactivation inhibition"
            + " reduction repression suppression";
    String group10 = "modification acetylation";
    String group11 = "process apoptosis myogenesis zygosis";
    String group12 = "reaction interaction";
    String group13 = "release disassembly discharge";
    String group14 = "signaling mediation modulation participation regulation";
    String group15 = "substitution replacement";
    String group16 = "abrogation antagonist conversion destabilization obstruction stabilization affinity dimerization formation "
            + "synthesis encoding activity augmentation comprehension conjugation contact control dependency derivation "
            + "effect enhancement exhibition impact interference link recognition recruitment response restriction yield affect "
            + "potentiation glycosylation hydroxylation";

    String[] groupTerms;
    groupTerms = group1.split("[\\s+]");
    for (String s : groupTerms) {
      if (s.trim().isEmpty())
        continue;
      group1List.add(CleanTerms.getStemmedTerm(s));
    }

    groupTerms = group2.split("[\\s+]");
    for (String s : groupTerms) {
      if (s.trim().isEmpty())
        continue;
      group2List.add(CleanTerms.getStemmedTerm(s));
    }

    groupTerms = group3.split("[\\s+]");
    for (String s : groupTerms) {
      if (s.trim().isEmpty())
        continue;
      group3List.add(CleanTerms.getStemmedTerm(s));
    }

    groupTerms = group4.split("[\\s+]");
    for (String s : groupTerms) {
      if (s.trim().isEmpty())
        continue;
      group4List.add(CleanTerms.getStemmedTerm(s));
    }

    groupTerms = group5.split("[\\s+]");
    for (String s : groupTerms) {
      if (s.trim().isEmpty())
        continue;
      group5List.add(CleanTerms.getStemmedTerm(s));
    }

    groupTerms = group6.split("[\\s+]");
    for (String s : groupTerms) {
      if (s.trim().isEmpty())
        continue;
      group6List.add(CleanTerms.getStemmedTerm(s));
    }

    groupTerms = group7.split("[\\s+]");
    for (String s : groupTerms) {
      if (s.trim().isEmpty())
        continue;
      group7List.add(CleanTerms.getStemmedTerm(s));
    }

    groupTerms = group8.split("[\\s+]");
    for (String s : groupTerms) {
      if (s.trim().isEmpty())
        continue;
      group8List.add(CleanTerms.getStemmedTerm(s));
    }

    groupTerms = group9.split("[\\s+]");
    for (String s : groupTerms) {
      if (s.trim().isEmpty())
        continue;
      group9List.add(CleanTerms.getStemmedTerm(s));
    }

    groupTerms = group10.split("[\\s+]");
    for (String s : groupTerms) {
      if (s.trim().isEmpty())
        continue;
      group10List.add(CleanTerms.getStemmedTerm(s));
    }

    groupTerms = group11.split("[\\s+]");
    for (String s : groupTerms) {
      if (s.trim().isEmpty())
        continue;
      group11List.add(CleanTerms.getStemmedTerm(s));
    }

    groupTerms = group12.split("[\\s+]");
    for (String s : groupTerms) {
      if (s.trim().isEmpty())
        continue;
      group12List.add(CleanTerms.getStemmedTerm(s));
    }

    groupTerms = group13.split("[\\s+]");
    for (String s : groupTerms) {
      if (s.trim().isEmpty())
        continue;
      group13List.add(CleanTerms.getStemmedTerm(s));
    }

    groupTerms = group14.split("[\\s+]");
    for (String s : groupTerms) {
      if (s.trim().isEmpty())
        continue;
      group14List.add(CleanTerms.getStemmedTerm(s));
    }

    groupTerms = group15.split("[\\s+]");
    for (String s : groupTerms) {
      if (s.trim().isEmpty())
        continue;
      group15List.add(CleanTerms.getStemmedTerm(s));
    }

    groupTerms = group16.split("[\\s+]");
    for (String s : groupTerms) {
      if (s.trim().isEmpty())
        continue;
      group16List.add(CleanTerms.getStemmedTerm(s));
    }

    storage.put(group1List, group1);
    storage.put(group2List, group2);
    storage.put(group3List, group3);
    storage.put(group4List, group4);
    storage.put(group5List, group5);
    storage.put(group6List, group6);
    storage.put(group7List, group7);
    storage.put(group8List, group8);
    storage.put(group9List, group9);
    storage.put(group10List, group10);
    storage.put(group11List, group11);
    storage.put(group12List, group12);
    storage.put(group13List, group13);
    storage.put(group14List, group14);
    storage.put(group15List, group15);
    // storage.put(group16List, group16);

  }

  /**
   * get the synonyms for the relation keyword
   * 
   * @param term
   *          the key word
   * @return empty if not found; a string of the synonyms otherwise
   */

  public String getSynonyms(String originalTerm) {
    String term = CleanTerms.getStemmedTerm(originalTerm);
    Iterator<ArrayList<String>> getKeys = storage.keySet().iterator();
    while (getKeys.hasNext()) {
      ArrayList<String> temp = getKeys.next();
      if (temp.indexOf(term) > -1) {
        return storage.get(temp);
      }
    }

    if (group16List.indexOf(term) > -1)
      return originalTerm;
    return "";
  }

}