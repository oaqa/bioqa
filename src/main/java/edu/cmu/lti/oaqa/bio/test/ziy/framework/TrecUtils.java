package edu.cmu.lti.oaqa.bio.test.ziy.framework;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TrecUtils {

  static class TrecFilenameFilter implements FilenameFilter {

    private String prefix;

    public TrecFilenameFilter(String prefix) {
      this.prefix = prefix;
    }

    @Override
    public boolean accept(File dir, String name) {
      return name.startsWith(prefix);
    }
  }

  private static boolean DISABLE_07 = true;

  /*
   * Split TREC file
   */

  public static void splitTrecFileByPipeline(File trecFile, boolean sorted) throws IOException {
    String prefix = getFilenamePrefix(trecFile.getAbsolutePath());
    BufferedReader br = new BufferedReader(new FileReader(trecFile));
    String line;

    if (!sorted) {
      HashMap<String, Writer> runtag2writer06 = new HashMap<String, Writer>();
      HashMap<String, Writer> runtag2writer07 = new HashMap<String, Writer>();
      while ((line = br.readLine()) != null) {
        String[] segs = line.split("\t");
        String runtag = segs[segs.length - 1];
        if (!runtag2writer06.containsKey(runtag)) {
          String filename = prefix + "-" + runtag + "-2006.trec";
          runtag2writer06.put(runtag, new BufferedWriter(new FileWriter(filename)));
          filename = prefix + "-" + runtag + "-2007.trec";
          runtag2writer07.put(runtag, new BufferedWriter(new FileWriter(filename)));
        }
        int qid = Integer.parseInt(segs[0]);
        if (qid >= 160 && qid <= 187) {
          runtag2writer06.get(runtag).write(line + "\n");
        } else if (qid >= 201 && qid <= 235) {
          runtag2writer07.get(runtag).write(line + "\n");
        }
      }

      for (Writer writer : runtag2writer06.values()) {
        writer.close();
      }
      for (Writer writer : runtag2writer07.values()) {
        writer.close();
      }
    } else {
      BufferedWriter writer06 = null, writer07 = null;
      String prevRuntag = "";
      int i = 0;
      while ((line = br.readLine()) != null) {
        String[] segs = line.split("\t");
        String runtag = segs[segs.length - 1];
        if (!runtag.equals(prevRuntag)) {
          System.out.println(i++);
          if (writer06 != null)
            writer06.close();
          if (!DISABLE_07 && writer07 != null)
            writer07.close();
          String filename = prefix + "-" + runtag + "-2006.trec";
          writer06 = new BufferedWriter(new FileWriter(filename));
          if (!DISABLE_07) {
            filename = prefix + "-" + runtag + "-2007.trec";
            writer07 = new BufferedWriter(new FileWriter(filename));
          }
          prevRuntag = runtag;
        }
        int qid = Integer.parseInt(segs[0]);
        if (qid >= 160 && qid <= 187) {
          writer06.write(line + "\n");
        } else if (!DISABLE_07 && qid >= 201 && qid <= 235) {
          writer07.write(line + "\n");
        }
      }
      writer06.close();
      if (!DISABLE_07) {
        writer07.close();
      }
    }

  }

  private static String getFilenamePrefix(String filename) {
    return filename.substring(0, filename.lastIndexOf("."));
  }

  /*
   * Run evaluation script
   */

  private static Runtime runtime = Runtime.getRuntime();

  public static void runEvaluationScript(File trecFile, File evalFile) throws IOException,
          InterruptedException {
    String name = getFilenamePrefix(trecFile.getName());
    String[] segs = name.split("-");
    File dir = new File("trec/scripts");

    Process process = null;
    if (segs[2].equals("2006")) {
      process = runtime.exec("python trecgen2007_score.py " + "trecgen2006.gold.standard.tsv.txt "
              + trecFile.getAbsolutePath(), null, dir);
    } else if (segs[2].equals("2007")) {
      process = runtime.exec("python trecgen2007_score.py " + "trecgen2007.gold.standard.tsv.txt "
              + trecFile.getAbsolutePath(), null, dir);
    }
    process.waitFor();

    BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
    BufferedWriter bw = new BufferedWriter(new FileWriter(evalFile, true));
    String line = br.readLine();
    while ((line = br.readLine()) != null) {
      bw.write(line + "\n");
    }
    bw.close();
  }

  /*
   * Interpret result
   */

  private static Pattern pattern = Pattern
          .compile("(\\d+)-([0-9a-f]+)-(200[67])\t([A-Z]+)\t([0-9MAP]+)\t([0-9.]+)");

  private static HashMap<String, String> traceCodeMap = new HashMap<String, String>();

  private static TreeMap<String, HashMap<String, double[]>> table = new TreeMap<String, HashMap<String, double[]>>();

  public static void interpretResult(File evalFile, File tsvFile) throws IOException,
          ClassNotFoundException, SQLException {
    BufferedReader br = new BufferedReader(new FileReader(evalFile));
    String line;
    while ((line = br.readLine()) != null) {
      Matcher m = pattern.matcher(line);
      if (m.matches()) {
        System.out.println(traceCodeMap.size());
        // parse table
        String traceCode = m.group(2);
        String year = m.group(3);
        String type = m.group(4);
        String qid = m.group(5);
        double score = Double.parseDouble(m.group(6));
        // convert
        String trace = traceCodeMap.containsKey(traceCode) ? traceCodeMap.get(traceCode)
                : queryTrace(traceCode);
        // store into the table
        if (!table.containsKey(qid)) {
          table.put(qid, new HashMap<String, double[]>());
        }
        if (!table.get(qid).containsKey(trace)) {
          table.get(qid).put(trace, new double[6]);
        }
        if (type.equals("DOCUMENT")) {
          if (year.equals("2006")) {
            table.get(qid).get(trace)[0] = score;
          } else if (year.equals("2007")) {
            table.get(qid).get(trace)[1] = score;
          }
        } else if (type.equals("PASSAGE")) {
          if (year.equals("2006")) {
            table.get(qid).get(trace)[2] = score;
          } else if (year.equals("2007")) {
            table.get(qid).get(trace)[3] = score;
          }
        } else if (type.equals("ASPECT")) {
          if (year.equals("2006")) {
            table.get(qid).get(trace)[4] = score;
          } else if (year.equals("2007")) {
            table.get(qid).get(trace)[5] = score;
          }
        }
      }
    }

    BufferedWriter bw = new BufferedWriter(new FileWriter(tsvFile));
    for (String qid : table.keySet()) {
      bw.write(qid + "\n");
      bw.write("TRACE" + "\t" + "DOCUMENT 2006" + "\t" + "2007" + "\t" + "PASSAGE 2006" + "\t"
              + "2007" + "\t" + "ASPECT 2006" + "\t" + "2007" + "\n");
      for (String trace : table.get(qid).keySet()) {
        bw.write(trace);
        for (double score : table.get(qid).get(trace)) {
          bw.write("\t" + score);
        }
        bw.write("\n");
      }
      bw.write("\n");
    }
    bw.close();
  }

  private static Statement statement;

  static {
    try {
      Class.forName("com.mysql.jdbc.Driver");
      Connection conn = DriverManager.getConnection(
              "jdbc:mysql://seit1.lti.cs.cmu.edu:3308/oaevaluation", "oaqacse", "corm767.cups");
      statement = conn.createStatement();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  private static String queryTrace(String traceCode) throws ClassNotFoundException, SQLException {
    String sql = "select distinct cas.trace, traces.trace "
            + "from traces join cas on (traces.casId = cas.traceHash)" + "WHERE traces.trace = '"
            + traceCode + "'";

    ResultSet rs = statement.executeQuery(sql);
    String ret = null;
    if (rs.next()) {
      ret = rs.getString("cas.trace");
      traceCodeMap.put(traceCode, ret);
    }
    rs.close();
    return ret;
  }

  public static void main(String[] args) throws IOException, InterruptedException,
          ClassNotFoundException, SQLException {
    // String date = "20120413";
    // String date = "201204161449";
    // String date = "201204192232";
    // String date = "201204262107";
    // String date = "201204262133";
    // String date = "201204281135";
    // String date = "201204301436";
    // String date = "201205030037";
    // String date = "201205050141";
    // String date = "2012051503";
    // String date = "201209061743";
    // String date = "201209071451";
    // String date = "201209071617";
    String date = "201209081834";

    File trecFile = new File("trec", date + ".trec");
    File evalFile = new File("trec", date + ".eval");
    // File tsvFile = new File("trec", date + ".tsv");
    evalFile.delete();

    TrecUtils.splitTrecFileByPipeline(trecFile, false);
    File[] splitFiles = trecFile.getParentFile().listFiles(new TrecFilenameFilter(date + "-"));
    int i = 0;
    for (File splitFile : splitFiles) {
      System.out.println(i++ + "/" + splitFiles.length);
      TrecUtils.runEvaluationScript(splitFile, evalFile);
    }

    // TrecUtils.interpretResult(evalFile, tsvFile);
  }
}
