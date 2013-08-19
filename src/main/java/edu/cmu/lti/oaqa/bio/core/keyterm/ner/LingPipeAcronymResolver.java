package edu.cmu.lti.oaqa.bio.core.keyterm.ner;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunker;
import com.aliasi.chunk.Chunking;
import com.aliasi.util.Streams;

import edu.cmu.lti.oaqa.bio.framework.data.BioKeyterm;
import edu.cmu.lti.oaqa.bio.utils.retrieval.tools.ExtractAbbrev;
import edu.cmu.lti.oaqa.cse.basephase.keyterm.AbstractKeytermUpdater;
import edu.cmu.lti.oaqa.framework.data.Keyterm;

/**
 * get the abbreviation or long form for the keyterms extracted from the LingPipe NER.
 * 
 * @author yanfang (yanfang@cmu.edu)
 */
public class LingPipeAcronymResolver extends AbstractKeytermUpdater {

  private static Chunker chunker;

  protected void initialize(URL url) throws IOException, ClassNotFoundException {
    ObjectInputStream ois = new ObjectInputStream(url.openStream());
    chunker = (Chunker) ois.readObject();
    Streams.closeQuietly(ois);
  }

  public void initialize(String filePath) throws IOException, ClassNotFoundException {
    File file = new File(filePath);
    if (file.exists()) {
      System.out.println("Reading model from file: " + filePath);
      initialize(file.toURI().toURL());
    } else {
      URL url = getClass().getResource(filePath);
      System.out.println("Reading model from system resource: " + url.getFile());
      initialize(getClass().getResource(filePath));
    }
  }

  public List<String> getNameEntities(String question) {
    Chunking chunking = chunker.chunk(question);
    List<String> nameEntities = new ArrayList<String>();
    for (Chunk chunk : chunking.chunkSet()) {
      nameEntities.add(question.substring(chunk.start(), chunk.end()));
    }

    // @author yanfang@cmu.edu
    HashMap<String, String> abbreviationAndLongForm;
    ExtractAbbrev extractAbb = new ExtractAbbrev();
    if (extractAbb.abbreviateExtractor(question) != null) {
      abbreviationAndLongForm = (extractAbb.abbreviateExtractor(question));
      List<String> nameEntities2 = new ArrayList<String>();
      // go through all the entities
      for (String r : nameEntities) {
        boolean flag = true;
        // go through all the abbreviation and long forms
        for (Map.Entry<String, String> entry : abbreviationAndLongForm.entrySet()) {
          if (r.indexOf(entry.getKey()) > -1 || entry.getKey().indexOf(r) > -1) {
            // if the short or long form is not in the name entity list, add both
            // the short form and the long form
            if (nameEntities2.indexOf(entry.getKey()) == -1) {
              nameEntities2.add(entry.getKey());
              nameEntities2.add(entry.getValue());
            }
            flag = false;
          }
        }
        // if the entity is irrelevant with abbrevaition, add it
        if (flag)
          nameEntities2.add(r);
      }
      return nameEntities2;
    }

    return nameEntities;
  }

  public static List<String> tokenize(String sentence) {
    List<String> result = new ArrayList<String>();
    Chunking chunking = chunker.chunk(sentence);
    for (Chunk chunk : chunking.chunkSet()) {
      result.add(sentence.substring(chunk.start(), chunk.end()));
    }

    return result;
  }

  @Override
  public void initialize(UimaContext aContext) throws ResourceInitializationException {
    super.initialize(aContext);
    String filePath = (String) aContext.getConfigParameterValue("ModelFilePath");
    try {
      initialize(filePath);
    } catch (Exception e) {
      e.printStackTrace();
      throw new ResourceInitializationException(e);
    }
  }

  @Override
  protected List<Keyterm> updateKeyterms(String question, List<Keyterm> keyterms) {
    for (String nameEntity : getNameEntities(question)) {
      keyterms.add(new BioKeyterm(nameEntity, -1, BioKeyterm.PHRASE));
    }
    return keyterms;
  }

  public static void main(String[] args) throws IOException, ClassNotFoundException {
    String sentence = "What is the role of Transforming growth factor-beta1 (TGF-beta1) in cerebral amyloid angiopathy (CAA)?";
    LingPipeAcronymResolver ner = new LingPipeAcronymResolver();
    // ner.initialize("/ne-en-bio-genia.TokenShapeChunker");
    ner.initialize("src/main/resources/ne-en-bio-genia.TokenShapeChunker");
    System.out.println(ner.getNameEntities(sentence));
  }

}
