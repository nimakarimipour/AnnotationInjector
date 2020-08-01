package edu.riple.annotationinjector;

import edu.riple.annotationinjector.visitors.ASTHelpers;
import edu.riple.annotationinjector.visitors.AddClassFieldAnnotation;
import edu.riple.annotationinjector.visitors.AddMethodParamAnnotation;
import edu.riple.annotationinjector.visitors.AddMethodReturnAnnotation;
import edu.riple.annotationinjector.visitors.Refactor;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openrewrite.Change;
import org.openrewrite.java.Java8Parser;
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.JavaRefactorVisitor;
import org.openrewrite.java.tree.J;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings(
    "UnusedVariable") // todo: Remove this later, this class is still under construction
public class Injector {

  private final JavaParser parser;
  private final MODE mode;
  private final boolean cleanImports;
  private final List<String> addedImports;
  private List<J.Import> imports;
  private Path fixesFilePath;
  private ArrayList<WorkList> workLists;

  private int counter = 0;
  private int numOfFixed = 0;

  public enum MODE {
    OVERWRITE,
    TEST
  }

  public Injector(MODE mode, boolean cleanImports) {
    this.mode = mode;
    this.cleanImports = cleanImports;
    addedImports = new ArrayList<>();
    parser =
        Java8Parser.builder()
            .relaxedClassTypeMatching(true)
            .logCompilationWarningsAndErrors(false)
            .build();
  }

  public Injector() {
    this(MODE.OVERWRITE, false);
  }

  public static InjectorBuilder builder(MODE mode) {
    return new InjectorBuilder(mode);
  }

  public void start() {
    workLists = readFixes();
    int totalNumberOfFixes = 0;
    for (WorkList workList : workLists) totalNumberOfFixes += workList.getFixes().size();
    System.out.println("NullAway found " + totalNumberOfFixes + " number of fixes");
    applyFixes();
    System.out.println(
        "Processed " + counter + " fixes and applied " + numOfFixed + " number of fixes");
  }

  private void applyFixes() {
    J.CompilationUnit tree;
    Refactor refactor = null;
    for (WorkList workList : workLists) {
      tree = getTree(workList.getUri());
      workList.addContainingAnnotationsToList(addedImports);
      System.out.println(counter + ":Processing " + ASTHelpers.lastName(workList.className()));
      ArrayList<JavaRefactorVisitor> refactors = new ArrayList<>();
      if (!cleanImports) saveImport(tree);
      for (Fix fix : workList.getFixes()) {
        switch (fix.location) {
          case "CLASS_FIELD":
            refactor = new AddClassFieldAnnotation(fix, tree);
            break;
          case "METHOD_LOCAL_VAR":
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
        if (refactor == null) continue;
        JavaRefactorVisitor refactorVisitor = refactor.build();
        if (refactorVisitor == null) {
          System.out.println("Skipped!");
        } else {
          numOfFixed++;
          refactors.add(refactorVisitor);
        }
        counter++;
      }
      Change<J.CompilationUnit> changed = null;
      for (JavaRefactorVisitor r : refactors) {
        if (changed == null) changed = tree.refactor().visit(r).fix();
        else changed = changed.getFixed().refactor().visit(r).fix();
      }
      if(changed != null)
        overWriteToFile(changed, workList.getUri());
    }
  }

  private void saveImport(J.CompilationUnit tree) {
    List<J.Import> tmp = tree.getImports();
    imports = new ArrayList<>(tmp);
    tmp.clear();
  }

  private void overWriteToFile(Change<J.CompilationUnit> change, String uri) {
    if (mode.equals(MODE.TEST)) {
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
    private final Injector injector;

    public InjectorBuilder(MODE mode) {
      injector = new Injector(mode, false);
    }

    public InjectorBuilder(MODE mode, boolean cleanImports) {
      injector = new Injector(mode, cleanImports);
    }

    public InjectorBuilder setFixesJsonFilePath(String path) {
      injector.fixesFilePath = Paths.get(path);
      return this;
    }

    public Injector build() {
      return injector;
    }
  }
}
