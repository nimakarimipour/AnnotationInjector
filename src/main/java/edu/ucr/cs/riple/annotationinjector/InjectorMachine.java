package edu.ucr.cs.riple.annotationinjector;

import edu.ucr.cs.riple.annotationinjector.visitors.Helper;
import edu.ucr.cs.riple.annotationinjector.visitors.RefactorVisitor;
import org.openrewrite.Change;
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.tree.J;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class InjectorMachine{

  List<WorkList> workLists;
  JavaParser parser;
  boolean cleanImports;
  Injector.MODE mode;
  int processed = 0;

  public InjectorMachine(List<WorkList> workLists, boolean cleanImports, Injector.MODE mode) {
    this.workLists = workLists;
    this.mode = mode;
    this.cleanImports = cleanImports;
    buildParser();
  }

  private void buildParser() {
    parser = JavaParser.fromJavaVersion()
            .relaxedClassTypeMatching(true)
            .logCompilationWarningsAndErrors(false)
            .build();
  }

  private void overWriteToFile(Change change, String uri) {
    if (mode.equals(Injector.MODE.TEST)) {
      uri = uri.replace("src", "out");
    }
    String pathToFileDirectory = uri.substring(0, uri.lastIndexOf("/"));
    try {
      Files.createDirectories(Paths.get(pathToFileDirectory + "/"));
      try (Writer writer = Files.newBufferedWriter(Paths.get(uri), Charset.defaultCharset())) {
        writer.write(change.getFixed().print());
        writer.flush();
        writer.close();
      }
    } catch (IOException e) {
      throw new RuntimeException("Something terrible happened.");
    }
  }

  private J.CompilationUnit getTree(String uri) {
    parser.reset();
    List<J.CompilationUnit> trees = Collections.singletonList(parser.parse(Paths.get(uri), Paths.get("/.")));
    return trees.get(0);
  }

  public Integer call() {
    J.CompilationUnit tree;
    for (WorkList workList : workLists) {
      parser.reset();
      tree = getTree(workList.getUri());
      List<J.CompilationUnit> cus = Collections.singletonList(tree);
      org.openrewrite.Refactor refactor = new org.openrewrite.Refactor().visit(new RefactorVisitor(workList));
      Collection<Change> changeSet = refactor.fix(cus);
      for(Change change: changeSet){
        log(workList.className(), change == null);
        overWriteToFile(change, workList.getUri());
      }
    }
    return processed;
  }

  private void log(String className, boolean fail){
    if(fail) System.out.print("\u001B[31m");
    else System.out.print("\u001B[32m");
    System.out.printf("Processing %-50s", Helper.lastName(className));
    if (fail) System.out.println("✘ (Skipped)");
    else System.out.println("\u2713");
    System.out.print("\u001B[0m");
  }
}
