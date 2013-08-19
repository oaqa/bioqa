package edu.cmu.lti.oaqa.bio.core.keyterm.ner;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunker;
import com.aliasi.chunk.Chunking;
import com.aliasi.util.Streams;

import edu.cmu.lti.oaqa.bio.framework.data.BioKeyterm;
import edu.cmu.lti.oaqa.cse.basephase.keyterm.AbstractKeytermUpdater;
import edu.cmu.lti.oaqa.framework.data.Keyterm;

/**
 * A named entity recognizer by wrapping the LingPipe tool.
 * 
 * @author Zi Yang <ziy@cs.cmu.edu>
 *
 */
public class LingPipeNamedEntityRecognizer extends AbstractKeytermUpdater {

  private Chunker chunker;

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
    return nameEntities;
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
    String sentence = "What are the known drug side effects associated with the different alleles of CYP2C19's?";
    LingPipeNamedEntityRecognizer ner = new LingPipeNamedEntityRecognizer();
    ner.initialize("ne-en-bio-genia.TokenShapeChunker");
    ner.initialize("src/main/resources/ne-en-bio-genia.TokenShapeChunker");
    System.out.println(ner.getNameEntities(sentence));
  }

}
