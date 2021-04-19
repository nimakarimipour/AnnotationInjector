package edu.ucr.cs.riple.annotationinjector;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

@SuppressWarnings(
    "UnusedVariable") // todo: Remove this later, this class is still under construction
public class Injector {


  public final MODE mode;
  private final boolean cleanImports;
  private final Path fixesFilePath;

  public enum MODE {
    OVERWRITE,
    TEST
  }

  public Injector(MODE mode, boolean cleanImports, String fixesFilePath) {
    this.mode = mode;
    this.cleanImports = cleanImports;
    this.fixesFilePath = Paths.get(fixesFilePath);
  }

  public Injector() {
    this(MODE.OVERWRITE, false, "/tmp/NullAwayFix/");
  }

  public static InjectorBuilder builder() {
    return new InjectorBuilder();
  }

  public Report start() {
    ArrayList<WorkList> workLists = readFixes();
    Report report = new Report();
    for (WorkList workList : workLists)
      report.totalNumberOfDistinctFixes += workList.getFixes().size();
    System.out.println("NullAway found " + report.totalNumberOfDistinctFixes + " number of fixes");
    report.processed = new InjectorMachine(workLists, cleanImports, mode).call();
    System.out.println(
        "Received "
            + report.totalNumberOfDistinctFixes
            + " fixes and applied "
            + report.processed
            + " number of fixes");
    return report;
    }


  private ArrayList<WorkList> readFixes() {
    try {
      BufferedReader bufferedReader =
          Files.newBufferedReader(this.fixesFilePath, Charset.defaultCharset());
      JSONObject obj = (JSONObject) new JSONParser().parse(bufferedReader);
      JSONArray fixesJson = (JSONArray) obj.get("fixes");
      bufferedReader.close();
      return extractFixesFromJson(fixesJson);
    } catch (FileNotFoundException ex) {
      throw new RuntimeException("Unable to open file: " + fixesFilePath.toUri());
    } catch (IOException ex) {
      throw new RuntimeException("Error reading file: " + fixesFilePath.toUri());
    } catch (ParseException e) {
      throw new RuntimeException("Error in parsing object: " + e);
    }
  }

  private ArrayList<WorkList> extractFixesFromJson(JSONArray fixesJson) {
    ArrayList<String> uris = new ArrayList<>();
    ArrayList<WorkList> workLists = new ArrayList<>();
    for (Object o : fixesJson) {
      Fix fix = Fix.createFromJson((JSONObject) o);
      if (!uris.contains(fix.uri)) {
        uris.add(fix.uri);
        WorkList workList = new WorkList(fix.uri);
        workLists.add(workList);
        workList.addFix(fix);
      } else {
        for (WorkList workList : workLists) {
          if (workList.getUri().equals(fix.uri)) {
            workList.addFix(fix);
            break;
          }
        }
      }
    }
    return workLists;
  }

  public static class InjectorBuilder {
    private String fixesFilePath = "/tmp/NullAwayFix/";
    private MODE mode = MODE.OVERWRITE;
    private boolean cleanImports = false;

    public InjectorBuilder setFixesJsonFilePath(String fixesFilePath) {
      this.fixesFilePath = fixesFilePath;
      return this;
    }

    public InjectorBuilder setMode(MODE mode){
      this.mode = mode;
      return this;
    }

    public InjectorBuilder setCleanImports(boolean cleanImports){
      this.cleanImports = cleanImports;
      return this;
    }

    public Injector build() {
      return new Injector(mode, cleanImports, fixesFilePath);
    }
  }
}
