package edu.riple.annotationinjector;

import java.util.List;

@SuppressWarnings(
    "UnusedVariable") // todo: Remove this later, this class is still under construction
public class Injector {


  public final MODE mode;
  private final boolean cleanImports;

  public enum MODE {
    BATCH,
    TEST
  }

  public Injector(MODE mode, boolean cleanImports) {
    this.mode = mode;
    this.cleanImports = cleanImports;
  }

  public static InjectorBuilder builder() {
    return new InjectorBuilder();
  }

  public Report start(List<WorkList> workLists) {
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

  public static class InjectorBuilder {
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

    public Injector build() {
      return new Injector(mode, cleanImports);
    }
  }
}
