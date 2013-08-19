package edu.cmu.lti.oaqa.bio.core.keyterm.pos;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;

/**
 * A more specific {@link AbstractPosTagger} that requires a model to be read from a resource path.
 * 
 * @author Zi Yang <ziy@cs.cmu.edu>
 * 
 */
public abstract class AbstractModelBasedPosTagger extends AbstractPosTagger {

  protected abstract void initialize(URL url) throws IOException, ClassNotFoundException;

  public final void initialize(String filePath) throws IOException, ClassNotFoundException {
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

}
