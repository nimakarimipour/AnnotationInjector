package edu.riple.annotationinjector;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openrewrite.java.Java8Parser;
import org.openrewrite.java.JavaParser;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@SuppressWarnings(
    "UnusedVariable") // todo: Remove this later, this class is still under construction
public class Injector {


  public final MODE mode;
  private final boolean cleanImports;
  private final Path fixesFilePath;
  private final int numberOfMachines;

  private int processed = 0;

  public enum MODE {
    OVERWRITE,
    TEST
  }

  public Injector(MODE mode, boolean cleanImports, int numberOfMachines, String fixesFilePath) {
    this.numberOfMachines = numberOfMachines;
    this.mode = mode;
    this.cleanImports = cleanImports;
    this.fixesFilePath = Paths.get(fixesFilePath);
  }

  public Injector() {
    this(MODE.OVERWRITE, false, 1, "/tmp/NullAwayFix/");
  }

  public static InjectorBuilder builder() {
    return new InjectorBuilder();
  }

  public void start() {
    ArrayList<WorkList> workLists = readFixes();
    int totalNumberOfFixes = 0;
    for (WorkList workList : workLists) totalNumberOfFixes += workList.getFixes().size();
    System.out.println("NullAway found " + totalNumberOfFixes + " number of fixes");
    if (mode.equals(MODE.TEST)) {
      processed = new InjectorMachine(1, workLists, cleanImports, mode).call();
      System.out.println(
              "Received " + totalNumberOfFixes + " fixes and applied " + processed + " number of fixes");
      return;
    }
    final List<Callable<Integer>> workers = new ArrayList<>();
    int realNumberOfMachines = totalNumberOfFixes > (numberOfMachines * 5) ? numberOfMachines : 1;
    System.out.println("Number Of Instantiated Machines: " + realNumberOfMachines);
    int size = workLists.size() / realNumberOfMachines;
    for (int i = 0; i < realNumberOfMachines; i++) {
      List<WorkList> machinesWorkList;
      if (i == realNumberOfMachines - 1) {
        machinesWorkList = workLists.subList(i * size, workLists.size());
      } else {
        machinesWorkList = workLists.subList(i * size, (i + 1) * size);
      }
      workers.add(new InjectorMachine(i+1, machinesWorkList, cleanImports, mode));
    }
    final ExecutorService pool = Executors.newFixedThreadPool(realNumberOfMachines);
    try {
      for (final Future<Integer> future : pool.invokeAll(workers)) {
        processed += future.get();
      }
    } catch (ExecutionException ex) {
      System.err.println("Injector executor faced an exception. (ExecutionException)");
      ex.printStackTrace();
    } catch (InterruptedException ex) {
      System.err.println("Injector executor faced an exception. (InterruptedException)");
      ex.printStackTrace();
    }
    pool.shutdown();
    System.out.println(
        "Received " + totalNumberOfFixes + " fixes and applied " + processed + " number of fixes");
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
    private int numberOfWorkers = 1;
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

    public InjectorBuilder setNumberOfWorkers(int numberOfWorkers){
      this.numberOfWorkers  = numberOfWorkers;
      return this;
    }

    public Injector build() {
      return new Injector(mode, cleanImports, numberOfWorkers, fixesFilePath);
    }
  }
}
