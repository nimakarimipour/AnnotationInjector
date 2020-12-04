package edu.riple.annotationinjector;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@SuppressWarnings(
    "UnusedVariable") // todo: Remove this later, this class is still under construction
public class Injector {


  public final MODE mode;
  private final boolean cleanImports;
  private final int numberOfMachines;

  public enum MODE {
    BATCH,
    TEST
  }

  public Injector(MODE mode, boolean cleanImports, int numberOfMachines) {
    this.numberOfMachines = numberOfMachines;
    this.mode = mode;
    this.cleanImports = cleanImports;
  }

  public static InjectorBuilder builder() {
    return new InjectorBuilder();
  }

  public Report start(List<WorkList> workLists) {
    Report report = new Report();
    for (WorkList workList : workLists) report.totalNumberOfDistinctFixes += workList.getFixes().size();
    System.out.println("NullAway found " + report.totalNumberOfDistinctFixes + " number of fixes");
    if (mode.equals(MODE.TEST) || numberOfMachines == 1) {
      report.processed = new InjectorMachine(1, workLists, cleanImports, mode).call();
      System.out.println(
              "Received " + report.totalNumberOfDistinctFixes + " fixes and applied " + report.processed + " number of fixes");
      return report;
    }
    final List<Callable<Integer>> workers = new ArrayList<>();
    int realNumberOfMachines = report.totalNumberOfDistinctFixes > (numberOfMachines * 5) ? numberOfMachines : 1;
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
        report.processed += future.get();
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
        "Received " + report.totalNumberOfDistinctFixes + " fixes and applied " + report.processed + " number of fixes");
    return report;
  }

  public static class InjectorBuilder {
    private int numberOfWorkers = 1;
    private MODE mode = MODE.BATCH;
    private boolean cleanImports = false;

    public InjectorBuilder setMode(MODE mode){
      this.mode = mode;
      return this;
    }

    public InjectorBuilder setCleanImports(boolean cleanImports){
      this.cleanImports = cleanImports;
      return this;
    }

    public InjectorBuilder setNumberOfWorkers(int numberOfWorkers) {
      this.numberOfWorkers = numberOfWorkers;
      return this;
    }

    public Injector build() {
      return new Injector(mode, cleanImports, numberOfWorkers);
    }
  }
}
