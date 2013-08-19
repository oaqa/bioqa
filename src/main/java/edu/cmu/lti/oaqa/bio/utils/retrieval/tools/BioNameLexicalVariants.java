package edu.cmu.lti.oaqa.bio.utils.retrieval.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.cmu.lti.oaqa.bio.utils.retrieval.tools.CheckTerms;

/**
 * BioNameLexicalVariants is the class to generate all the lexical variants for a given biological
 * name. The rules are inspired by <i>Zhou, W., Yu, C. T., Torvik, V. I., & Smalheiser N. R. (2006).
 * A Concept-based Framework for Passage Retrieval in Genomics. TREC 2006 Proceedings</i>.
 * 
 * @author yanfang <yanfang@cmu.edu>
 * @version 0.1.1
 */

public class BioNameLexicalVariants {

  private static final Pattern VALID_PATTERN = Pattern.compile("[0-9]+|[A-Za-z]+");

  /**
   * Gets all the possible lexical variants for a biological term.
   * 
   * @param term
   *          The biological term.
   * @return a String list of all the lexical variants. <code>null</code> if the term does not have
   *         any lexical variants.
   */
  public static List<String> getLexicalVariants(String term) {

    boolean isAlphanumeric = CheckTerms.hasNumeric(term);
    boolean isHyphened = term.contains("-");
    ArrayList<String> variants = new ArrayList<String>();

    // Processes when the term has hyphen.
    if (isHyphened) {

      String originalTerm = term;
      String[] splitedByHyphenTerms = originalTerm.split("-");
      List<List<String>> rawLexicalVariants = new ArrayList<List<String>>();

      for (String segmentedPart : splitedByHyphenTerms) {

        List<String> tempTerm = new ArrayList<String>();
        List<String> alphanumericalVariants;

        // Adds "p" if the segmented part is a protein or gene name. This is a
        // rule of thumb. For example, "Sec6" is the same as "Sec6p" in some
        // situations.
        if (CheckTerms.isProtein(segmentedPart) || CheckTerms.isGene(segmentedPart)) {
          variants.add(segmentedPart.concat("p"));
        }

        // Concept term: protein, gene, disease and other biological concept
        if (CheckTerms.isConceptTerm(segmentedPart)) {
          variants.add(segmentedPart);
        }

        // Consider the case where the segmented part has numeric.
        if ((alphanumericalVariants = getAlphanumbericalVariants(segmentedPart)) != null)
          tempTerm.addAll(alphanumericalVariants);
        else
          tempTerm.add(segmentedPart);

        // There are many combinations between alphanumerical terms. This
        // variable will be needed in the next step.
        rawLexicalVariants.add(tempTerm);
      }

      // Adds all the combinations between the alphanumerical variants.
      variants.addAll(getAllCombinations(rawLexicalVariants));

      return variants;
    }

    // When the term does not have any hyphen.
    if (isAlphanumeric)
      return getAlphanumbericalVariants(term);

    // When the term is not hyphened nor alphanumerical.
    return null;
  }

  /**
   * Gets all the alphanumerical variants (including Roman format) for a given term. For example,
   * "Sec6" will get "Sec6", "Sec 6", "SecVI" and "Sec VI".
   * 
   * @param term
   *          The biological term.
   * @return a String list of all the lexical variants. <code>null</code> if the term does not have
   *         nay numeric.
   */
  public static List<String> getAlphanumbericalVariants(String term) {

    // When the term does not have any numberic
    if (!CheckTerms.hasNumeric(term))
      return null;

    List<List<String>> rawVariants = new ArrayList<List<String>>();

    // Splits the term into alphabetical part and numerical part
    ArrayList<String> chunks = new ArrayList<String>();
    Matcher matcher = VALID_PATTERN.matcher(term);
    while (matcher.find()) {
      chunks.add(matcher.group());
    }

    // Puts the chunks into lists to get difference combinations
    for (String chunk : chunks) {
      List<String> temp = new ArrayList<String>();

      // Gets the Roman numerical if it has
      if (getRomanNumericals(chunk) != null) {
        temp.add(getRomanNumericals(chunk));
      }

      // Adds the original part
      temp.add(chunk);

      // The variants that have not been combined
      rawVariants.add(temp);
    }

    // Get all the combinations and return the value
    return getAllCombinations(rawVariants);
  }

  /**
   * Gets all the combinations by retrieving one entity from each list with the order of the lists.
   * For example, for [List1["11","12"], List2["21","22"], List3["31","32"]], this method returns
   * ["11","21","31"],["12","21","31"]...(totally 2*2*2 combinations).
   * 
   * @param inputList
   *          This list contains the lists that will be combined.
   * @return a list of all the possible combinations
   */
  public static ArrayList<String> getAllCombinations(List<List<String>> inputList) {

    ArrayList<String> combinations = new ArrayList<String>();

    for (List<String> list : inputList) {

      // Temp is to store the results from last run and combination will store
      // all the latest combinations.
      @SuppressWarnings("unchecked")
      ArrayList<String> temp = (ArrayList<String>) combinations.clone();
      combinations.clear();

      for (String current : list) {

        // When it is the first run of the loop, adds the original entities from
        // list to the combinations.
        if (temp.isEmpty()) {
          combinations.add(current);
        }

        // Combines the previous combinations with the current entities.
        for (String previous : temp) {
          combinations.add(previous + " " + current);
          combinations.add(previous + "" + current);
          combinations.add(previous + "-" + current);
        }
      }
    }
    return combinations;
  }

  /**
   * Converts an Arabic numeral into a Roman numerical. But this method only works for
   * 
   * @param n
   *          The Arabic numeral
   * @return Roman numerical. <code>null<code> if the number if not an integer between 1 and 20.
   */
  public static String getRomanNumericals(String n) {

    String[] romanNumerical = { "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X", "XI",
        "XII", "XIII", "XIV", "XV", "XVI", "XVII", "XVIII", "XIV", "XX" };
    int number;

    if (CheckTerms.isAllNumeric(n) && (number = Integer.parseInt(n)) <= romanNumerical.length + 1) {
      return romanNumerical[number - 1];
    } else
      return null;
  }

  // test case
  public static void main(String arg[]) {
    System.out.println(BioNameLexicalVariants.getAlphanumbericalVariants("su7").toString());
    System.out.println(BioNameLexicalVariants.getLexicalVariants("Sec6-mutated").toString());
    System.out.println(BioNameLexicalVariants.getLexicalVariants("Sec6-mutated").size());
  }

}
