package edu.riple.annotationinjector;

import edu.riple.annotationinjector.visitors.ASTHelpers;
import edu.riple.annotationinjector.visitors.AddClassFieldAnnotation;
import edu.riple.annotationinjector.visitors.AddMethodParamAnnotation;
import edu.riple.annotationinjector.visitors.AddMethodReturnAnnotation;
import edu.riple.annotationinjector.visitors.Refactor;
import org.openrewrite.Change;
import org.openrewrite.java.Java8Parser;
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.JavaRefactorVisitor;
import org.openrewrite.java.tree.J;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InjectorMachine implements Callable<Integer> {

  List<WorkList> workLists;
  JavaParser parser;
  ArrayList<String> addedImports;
  boolean cleanImports;
  private ArrayList<J.Import> imports;
  Injector.MODE mode;
  int processed = 0;
  private final int id;

  public InjectorMachine(
      int id, List<WorkList> workLists, boolean cleanImports, Injector.MODE mode) {
    this.workLists = workLists;
    this.id = id;
    this.mode = mode;
    this.cleanImports = cleanImports;
    buildParser();
  }

  private void buildParser() {
    parser = Java8Parser.builder()
            .relaxedClassTypeMatching(true)
            .logCompilationWarningsAndErrors(false)
            .build();
  }

  private void saveImport(J.CompilationUnit tree) {
    List<J.Import> tmp = tree.getImports();
    imports = new ArrayList<>(tmp);
    tmp.clear();
  }

  private void overWriteToFile(Change<J.CompilationUnit> change, String uri) {
    if (mode.equals(Injector.MODE.TEST)) {
      uri = uri.replace("src", "out");
    }
    if (!cleanImports) {
      ArrayList<J.Import> tmp = new ArrayList<>();
      for (J.Import imp : imports) if (!addedImports.contains(imp.getTypeName())) tmp.add(imp);
      change.getFixed().getImports().addAll(tmp);
    }
    String input = postProcess(change.getFixed().print());
    String pathToFileDirectory = uri.substring(0, uri.lastIndexOf("/"));
    try {
      Files.createDirectories(Paths.get(pathToFileDirectory + "/"));
      try (Writer writer = Files.newBufferedWriter(Paths.get(uri), Charset.defaultCharset())) {
        writer.write(input);
        writer.flush();
      }
    } catch (IOException e) {
      throw new RuntimeException("Something terrible happened.");
    }
  }

  private String postProcess(String text) {
    ArrayList<Integer> indexes = new ArrayList<>();
    final String innerClassInstantiationByReferenceRegex =
        "[a-zA-Z][a-zA-Z0-9_]*(\\(\\))?\\s*\\.\\s*new\\s+([a-zA-Z_$][a-zA-Z\\d_$]*\\.)*[a-zA-Z_$][a-zA-Z\\d_$]*+\\(";
    Matcher matcher = Pattern.compile(innerClassInstantiationByReferenceRegex).matcher(text);
    while (matcher.find()) indexes.add(matcher.start());
    indexes.sort(Comparator.naturalOrder());
    StringBuilder sb = new StringBuilder(text.length());
    sb.append(text);
    int count = 0;
    for (Integer index : indexes) {
      sb.replace(index - (count * 3), index + 3 - (count * 3), "");
      count++;
    }
    return sb.toString();
  }

  private J.CompilationUnit getTree(String uri) {
    ArrayList<Path> p = new ArrayList<>();
    p.add(Paths.get(uri));
    parser.reset();
    ArrayList<J.CompilationUnit> trees = (ArrayList<J.CompilationUnit>) parser.parse(p);
    if (trees == null || trees.size() != 1)
      throw new RuntimeException("Error in crating AST tree for file at path: " + uri);
    return trees.get(0);
  }

  @Override
  public Integer call() {
    J.CompilationUnit tree;
    Refactor refactor = null;
    for (WorkList workList : workLists) {
      addedImports = new ArrayList<>();
      parser.reset();
      tree = getTree(workList.getUri());
      workList.addContainingAnnotationsToList(addedImports);
      ArrayList<JavaRefactorVisitor> refactors = new ArrayList<>();
      boolean skipped = false;
      if (!cleanImports) saveImport(tree);
      for (Fix fix : workList.getFixes()) {
          switch (fix.location) {
            case "CLASS_FIELD":
              refactor = new AddClassFieldAnnotation(fix, tree);
              break;
            case "METHOD_PARAM":
              refactor = new AddMethodParamAnnotation(fix, tree);
              break;
            case "METHOD_RETURN":
              refactor = new AddMethodReturnAnnotation(fix, tree);
              break;
            default:
              throw new RuntimeException("Undefined location: " + fix.location);
        }
        JavaRefactorVisitor refactorVisitor = null;
        try{ refactorVisitor = refactor.build(); }
        catch (Exception e){ skipped = true; }
        if(!skipped){
          processed++;
          refactors.add(refactorVisitor);
        }
      }
      log(workList.className(), skipped);
      Change<J.CompilationUnit> changed = null;
      for (JavaRefactorVisitor r : refactors) {
        if (changed == null) changed = tree.refactor().visit(r).fix();
        else changed = changed.getFixed().refactor().visit(r).fix();
      }
      if (changed != null) overWriteToFile(changed, workList.getUri());
    }
    return processed;
  }

  private void log(String className, boolean fail){
    if(fail) System.out.print("\u001B[31m");
    else System.out.print("\u001B[32m");
    System.out.printf("Processing %-50s", ASTHelpers.lastName(className));
    if (fail) System.out.println("✘ (Skipped)");
    else System.out.println("\u2713");
    System.out.print("\u001B[0m");
  }
}
