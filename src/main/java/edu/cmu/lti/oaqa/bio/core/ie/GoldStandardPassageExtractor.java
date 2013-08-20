package edu.cmu.lti.oaqa.bio.core.ie;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.common.io.Files;

public class GoldStandardPassageExtractor {

  private Map<String, File> name2file;

  private BufferedWriter bw;

  @SuppressWarnings("unchecked")
  public GoldStandardPassageExtractor(File htmlDir, File outputFile) throws FileNotFoundException {
    name2file = Maps.newHashMap();
    for (File file : (Collection<File>) FileUtils.listFiles(htmlDir, null, true)) {
      name2file.put(Files.getNameWithoutExtension(file.getName()), file);
    }
    System.out.println(name2file.size() + " files found.");
    bw = Files.newWriter(outputFile, Charsets.UTF_8);
  }

  public void extractGoldStandardPassages(File gsFile) throws IOException {
    BufferedReader br = Files.newReader(gsFile, Charsets.UTF_8);
    String line;
    while ((line = br.readLine()) != null) {
      String[] segs = line.split("\t");
      String gsPassage = Files.toString(name2file.get(segs[1]), Charsets.UTF_8)
              .substring(Integer.parseInt(segs[2]), Integer.parseInt(segs[3]))
              .replaceAll("\\s", " ");
      segs[4] = gsPassage;
      bw.write(line + "\t" + gsPassage + "\n");
    }
  }

  public void close() throws IOException {
    bw.close();
  }

  public static void main(String[] args) throws IOException {
    GoldStandardPassageExtractor gspe = new GoldStandardPassageExtractor(new File(args[0]),
            new File(args[1]));
    for (int i = 2; i < args.length; i++) {
      gspe.extractGoldStandardPassages(new File(args[i]));
    }
    gspe.close();
  }
}
