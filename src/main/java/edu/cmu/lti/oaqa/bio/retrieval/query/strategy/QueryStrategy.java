package edu.cmu.lti.oaqa.bio.retrieval.query.strategy;

import java.util.ArrayList;
import java.util.List;

import edu.cmu.lti.oaqa.bio.framework.data.BioKeyterm;
import edu.cmu.lti.oaqa.bio.retrieval.query.structure.KeytermInQuery;
import edu.cmu.lti.oaqa.bio.retrieval.query.structure.QueryComponent;
import edu.cmu.lti.oaqa.bio.retrieval.query.structure.QueryComponentContainer;
import edu.cmu.lti.oaqa.bio.retrieval.tools.CheckTerms;
import edu.cmu.lti.oaqa.bio.retrieval.tools.CleanTerms;
import edu.cmu.lti.oaqa.bio.retrieval.tools.ProteinProteinRelationKeywords;

/**
 * The QueryStrategy class is to form a {@link QueryComponenetContainer} which contains all
 * information of forming query. This class must accept a list of {@link BioKeyterm}, which has
 * original text, synonyms, etc. Also, different weights and resources for keyterms are supported.
 * 
 * @author yanfang (yanfang@cmu.edu)
 */
public class QueryStrategy {

  private List<BioKeyterm> keyTerms;

  private CheckTerms checker = new CheckTerms();

  private CleanTerms cleaner = new CleanTerms();

  private ProteinProteinRelationKeywords relationKeywordsSysnonyms = new ProteinProteinRelationKeywords();

  private List<String> conceptTerms = new ArrayList<String>();

  private boolean hasMESH = true;

  private boolean hasENTREZ = true;

  private boolean hasUMLS = false;

  private boolean hasMESHAcronym = false;

  private boolean hasENTREZAcronym = false;

  private boolean hasUMLSAcronym = true;

  private boolean hasLexicalVariants = true;

  private boolean hasPOSTagger = true;

  private String conceptTermWeight = "0.6";

  private String regularTermWeight = "0.4";

  private String verbTermWeight = "0.2";

  private String geneTermWeight = "0.3";

  private String mainPart = "";

  private int count = 0;

  private int countMain = 0;

  private QueryComponentContainer queryContainer = new QueryComponentContainer();

  private static final int ORIGINAL_KEY_TERMS = 0;

  private static final int PHRASE_KEY_TERMS = 1;

  private static final String MESH = "MeSH";

  private static final String ENTREZ = "EntrezGene";

  private static final String UMLS = "UMLS";

  private static final String ABBREVIATION = "AbbreviationAndLongForm";

  private static final String LEXICAL_VARIANTS = "LexicalVariants";

  public QueryStrategy() {
  }

  public QueryStrategy(List<BioKeyterm> keyterm) {
    this.keyTerms = keyterm;
  }
  
  // set all the variables.
  public void setKeyterms(List<BioKeyterm> keyterm) {
    this.keyTerms = keyterm;
  }

  public void setConceptTermWeight(String w) {
    this.conceptTermWeight = w;
  }

  public void setRegularTermWeight(String w) {
    this.regularTermWeight = w;
  }

  public void setVerbTermWeight(String w) {
    this.verbTermWeight = w;
  }

  public void setGeneTermWeight(String w) {
    this.geneTermWeight = w;
  }

  // set which resources that will be used. (default is true)
  public void hasMESH(boolean x) {
    this.hasMESH = x;
  }

  public void hasEntrez(boolean x) {
    this.hasENTREZ = x;
  }

  public void hasUMLSAcronym(boolean x) {
    this.hasUMLSAcronym = x;
  }

  public void hasMESHAcronym(boolean x) {
    this.hasMESHAcronym = x;
  }

  public void hasEntrezAcronym(boolean x) {
    this.hasENTREZAcronym = x;
  }

  public void hasUMLS(boolean x) {
    this.hasUMLS = x;
  }

  public void hasLexicalVariants(boolean x) {
    this.hasLexicalVariants = x;
  }

  public void hasPOSTagger(boolean x) {
    this.hasPOSTagger = x;
  }

  // public void

  // get the value of variables
  public List<String> getConceptTerms() {
    return this.conceptTerms;
  }

  public String getMainPart() {
    return this.mainPart;
  }

  public int getItemCount() {
    return this.count;
  }

  public int getMainItemCount() {
    return this.countMain;
  }

  public QueryComponentContainer getAllQueryComponents() {
    this.formQueryContainer();
    return this.queryContainer;
  }

  /**
   * Forms a query container which has all the necessary information for a query, and deletes
   * duplicated content.
   */
  private void formQueryContainer() {

    this.queryContainer.clear();
    ArrayList<String> termsUsedInPhrase = new ArrayList<String>(); // mark the terms that have been
    ArrayList<String> apostropheTerms = new ArrayList<String>(); // store the terms having
                                                                 // apostrophe.

    // PART I: phrase part -- this part considers all the phrases in this list of BioKeyterm
    for (BioKeyterm keyterm : this.keyTerms) {

      if (keyterm.getTokenType() == PHRASE_KEY_TERMS) {

        // removes characters that will blow Indri out
        String keytermText = this.cleaner.removeIndriSpeCha(keyterm.getText());

        // not consider any single word in this part
        if (!keytermText.trim().contains(" ")) {
          continue;
        }

        // finds out the word having apostrophe and puts it in the apostropheTerms list
        String apostropheRemoved = "";
        if (keyterm.getText().contains("'")) {
          String[] split = keyterm.getText().split("\\s+");
          for (String s : split) {
            if (s.endsWith("'s") && s.length() >= 3) {
              apostropheRemoved = cleaner.removeIndriSpeCha(s.substring(0, s.length() - 2));
              apostropheTerms.add(apostropheRemoved);
            }
          }
        }

        // checks the special terms, the terms that do not have very good resources attached
        if (!SpecialTermProcess(keytermText).isEmpty()) {
          QueryComponent temp = SpecialTermProcess(keytermText);
          temp.setConcept(false);
          this.queryContainer.add(temp);
          continue;
        }

        //this.printKeytermContent(keyterm);
        
        // add synonyms
        List<String> resources = new ArrayList<String>();
        if (hasUMLS) {
          for (String resource : keyterm.getAllResourceSources()) {
            if (resource.startsWith(UMLS))
              resources.addAll(keyterm.getSynonymsBySource(resource));
          }
        }
        if (hasMESH)
          resources.addAll(keyterm.getSynonymsBySource(MESH));
        if (hasENTREZ)
          resources.addAll(keyterm.getSynonymsBySource(ENTREZ));

        // deals with the brackets in the synonyms
        resources = this.cleaner.processBrackets(resources);

        // when the phrase does not have any synonyms, considers this phrase as a misclassfied
        // phrase
        if (resources.isEmpty())
          continue;

        String[] splits;
        splits = keytermText.split("\\s+");
        for (String s : splits) {

          // when one of the words in phrase is concept term, considers this word as a synonym of
          // the phrase
          if (checker.isConceptTerm(s))
            resources.add(s);
          // adds the first word in a phrase to the synonym empircally.
          else {
            if (splits.length == 2) {
              resources.add(splits[0]);
            }
          }

          // records used terms
          termsUsedInPhrase.add(cleaner.getStemmedTerm(s));
        }

        String newKeytermText = keytermText;

        // for apostrophe (e.g. "'s") situation
        if (!apostropheRemoved.isEmpty())
          newKeytermText = keytermText.replace(apostropheRemoved + "s", apostropheRemoved);
        KeytermInQuery phraseKeyterm = new KeytermInQuery(newKeytermText, this.conceptTermWeight);
        QueryComponent temp = new QueryComponent(phraseKeyterm,
                this.cleaner.removeDuplicatedSynonyms(keytermText, resources));

        // for apostrophe (e.g. "'s") situation
        if (!apostropheRemoved.isEmpty()) {
          temp.replaceStringKeepingOriginalInSynonyms(apostropheRemoved + "s", apostropheRemoved);
        }

        if (checker.isConceptTerm(keytermText)) {
          temp.setConcept(true);
        } else {
          temp.setConcept(false);
        }

        this.queryContainer.add(temp);
      }
    }

    // PART II: single term -- a term which only has one word
    for (BioKeyterm keyterm : this.keyTerms) {

      if (keyterm.getTokenType() == ORIGINAL_KEY_TERMS) {

        String keytermText = this.cleaner.removeIndriSpeCha(keyterm.getText());

        // not consider stopwords, "gene", "s", "or" and words that occure in phrases
        // "or" for Q171 "s" is generated by " 's "
        if (this.checker.isStopwords(keytermText.toLowerCase()) || keytermText.equals("or")
                || keytermText.equals("gene") || keytermText.equals("s")
                || termsUsedInPhrase.indexOf(this.cleaner.getStemmedTerm(keytermText)) > -1) {
          continue;
        }

        // "'s" situation. adds to apostropheTerms list
        if (keyterm.getText().endsWith("'s") && keyterm.getText().length() >= 3) {
          apostropheTerms.add(cleaner.removeIndriSpeCha(keyterm.getText().substring(0,
                  keyterm.getText().length() - 2)));
        }

        // Considers special terms, which do not have good resources attached
        if (!SpecialTermProcess(keytermText).isEmpty()) {
          QueryComponent temp = SpecialTermProcess(keytermText);
          temp.setConcept(false);
          this.queryContainer.add(temp);
          continue;
        }

        // Lower the weights of verbs
        if (keyterm.getTagBySource("LingPipeHmmPosTagger") == null)
          hasPOSTagger = false; // Guarantee the code works even there is no POStagger attached

        if (hasPOSTagger) {
          if (keyterm.getTagBySource("LingPipeHmmPosTagger").equals("VVB")
                  || keyterm.getTagBySource("LingPipeHmmPosTagger").equals("VVI")) {
            KeytermInQuery temp = new KeytermInQuery(keytermText, this.verbTermWeight);
            QueryComponent temp2 = new QueryComponent(temp, false);
            this.queryContainer.add(temp2);
            continue;
          }
        }

       // this.printKeytermContent(keyterm);
        
        // add synonyms
        List<String> resources = new ArrayList<String>();

        // use UMLS for acronyms
        if (checker.isAcronym(keytermText)) {
          if (hasUMLSAcronym) {
            for (String resource : keyterm.getAllResourceSources()) {
              if (resource.startsWith(UMLS))
                resources.addAll(keyterm.getSynonymsBySource(resource));
            }
          }
          if (hasENTREZAcronym)
            resources.addAll(keyterm.getSynonymsBySource(ENTREZ));
          if (hasMESHAcronym)
            resources.addAll(keyterm.getSynonymsBySource(MESH));
        } else {
          if (hasUMLS) {
            for (String resource : keyterm.getAllResourceSources()) {
              if (resource.startsWith(UMLS))
                resources.addAll(keyterm.getSynonymsBySource(resource));
            }
          }
          // use ENTREZ and MESH for other terms
          if (hasENTREZ)
            resources.addAll(keyterm.getSynonymsBySource(ENTREZ));
          if (hasMESH)
            resources.addAll(keyterm.getSynonymsBySource(MESH));

          // add the categories as the synonyms
          for (String s : keyterm.getCategories()) {
            String[] split = s.toString().replaceAll("[\\(\\)\\[\\]].*[\\(\\)\\[\\]]", "")
                    .split(";");
            for (String s1 : split) {
              resources.add(s1);
            }
          }
        }

        // it works in the situation where the resources are not good enough
        if (!additionalSynonyms(keytermText).isEmpty()) {
          resources.clear();
          resources = additionalSynonyms(keytermText);
        }

        // adds to the container based on the different conditions.
        KeytermInQuery singleKeyterm = new KeytermInQuery(keytermText, this.conceptTermWeight);
        QueryComponent temp = new QueryComponent(singleKeyterm, true,
                this.cleaner.removeDuplicatedSynonyms(keytermText, resources));

        if (hasLexicalVariants) {
          if (keyterm.getSynonymsBySource(LEXICAL_VARIANTS) != null) {
            temp.addAllSynonyms(keyterm.getSynonymsBySource(LEXICAL_VARIANTS));
          }
        }

        if (!checker.isConceptTerm(keytermText)) {
          if (resources.isEmpty())
            temp.setWeight(this.regularTermWeight);
          temp.setConcept(false);
        }

        this.queryContainer.add(temp);
      }
    }
  }

  /**
   * Provides the solutions for certain terms, which are misclassified.
   * 
   * @param keytermText
   * @param returnQuery
   * @return new {@link QueryComponent} filled with keyterm and its synonyms
   */
  protected QueryComponent SpecialTermProcess(String keytermText) {
    QueryComponent qi = new QueryComponent();
    ArrayList<String> specialTerms = new ArrayList<String>();
    specialTerms.add("receptor");
    specialTerms.add("biology");
    specialTerms.add("delete");
    specialTerms.add("mutations");
    specialTerms.add("role");

    if (keytermText.equals("cell growth")) {
      KeytermInQuery temp = new KeytermInQuery("cell growth", "0.6");
      qi.setKeytermInQuery(temp);
    }

    if (keytermText.equals("tumor progression")) {
      KeytermInQuery temp = new KeytermInQuery("tumor progression", "0.6");
      qi.setKeytermInQuery(temp);
      qi.addSynonyms("tumor");
    }

    if (keytermText.equals("CAA")) {
      KeytermInQuery temp = new KeytermInQuery("CAA", "0.6");
      qi.setKeytermInQuery(temp);
      qi.addSynonyms("cerebral amyloid angiopathy");
    }

    if (specialTerms.indexOf(keytermText) > -1) {
      KeytermInQuery temp = new KeytermInQuery(keytermText, this.verbTermWeight);
      qi.setKeytermInQuery(temp);
    }

    if (keytermText.length() < 2) {
      KeytermInQuery temp = new KeytermInQuery(keytermText, this.geneTermWeight);
      qi.setKeytermInQuery(temp);
    }

    String relaKeySyn = relationKeywordsSysnonyms.getSynonyms(keytermText);

    if (!relaKeySyn.isEmpty()) {
      // special case in Q 172 (further investigation)
      if (keytermText.equals("apoptosis")) {
        KeytermInQuery temp = new KeytermInQuery(keytermText, "0.6");
        qi.setKeytermInQuery(temp);
      } else {
        KeytermInQuery temp = new KeytermInQuery(keytermText, this.verbTermWeight);
        qi.setKeytermInQuery(temp);
      }
    }

    return qi;
  }

  /**
   * this method is to add synonyms for the terms which do not get any synonyms from existing
   * database
   * 
   * @param keytermText
   * @return synonyms list
   */
  protected List<String> additionalSynonyms(String keytermText) {
    List<String> resources = new ArrayList<String>();
    // this is very very special case. Orphan...
    if (keytermText.equals("Nurr-77")) {
      resources.add("nur77");
      resources.add("NR4A1");
      resources.add("NGFI-B");
    }
    // this synonym can be gotten from
    if (keytermText.equals("development")) {
      resources.add("growth");
    }
    if (keytermText.equals("GFs")) {
      resources.add("Hemophane");
      resources.add("Growth");
      resources.add("Factor");
    }
    if (keytermText.equals("HPV11")) {
      resources.add("human papillomavirus type 11");
      resources.add("human papillomavirus");
    }

    return resources;

  }

  /**
   * Only for test
   */
  private void printKeytermContent(BioKeyterm keyterm) {
    // for the sake of information
    // System.out.println(keyterm.getTagBySource("LingPipeHmmPosTagger"));
    System.out.println("-------------------------" + keyterm.getText() + "   " + keyterm.getTokenType()
            + " " + keyterm.getTags() + " " + keyterm.getAllTagSources() + " "
            + keyterm.getAllResourceSources() + " " + keyterm.getCategories());
    System.out.println("MESH:" + keyterm.getSynonymsBySource(MESH));
    System.out.println("EG:" + keyterm.getSynonymsBySource(ENTREZ));
    System.out.println("UMLS:");
    
    for (String resource : keyterm.getAllResourceSources()) {
        if (resource.startsWith(UMLS))
          System.out.println(keyterm.getSynonymsBySource(resource));
      }
    
    System.out.println("Abbre:" + keyterm.getSynonymsBySource(ABBREVIATION));
    System.out.println("LexicalVariants:" + keyterm.getSynonymsBySource(LEXICAL_VARIANTS));
    System.out.println("POStag:" + keyterm.getAllTagSources());
    for (String s : keyterm.getCategories()) {
      System.out.println("Category: " + s);
    }
  }

}
