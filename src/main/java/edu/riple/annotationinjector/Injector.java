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
import java.util.List;

@SuppressWarnings(
    "UnusedVariable") // todo: Remove this later, this class is still under construction
public class Injector {

  private final JavaParser parser;
  private final MODE mode;
  private final boolean cleanImports;
  private final List<String> addedImports;
  private List<J.Import> imports;
  private Path fixesFilePath;
  private ArrayList<Fix> fixes;
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
    fixes = readFixes();
    System.out.println("NullAway found " + fixes.size() + " number of fixes");
    applyFixes();
    System.out.println("Processed " + counter + " fixes and applied " + numOfFixed + " number of fixes");
  }

  private void applyFixes() {
    J.CompilationUnit tree;
    Refactor refactor = null;
    for (Fix fix : fixes) {
      if(!addedImports.contains(fix.annotation)) addedImports.add(fix.annotation);
      System.out.println(counter + ":Processing " + ASTHelpers.lastName(fix.className) + ", for method: " + fix.method + "|" + fix.location);
      tree = getTree(fix);
      if(!cleanImports) saveImport(tree);
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
      if (refactor == null) {
        System.out.println("Skipped...");
      }
      else {
        numOfFixed++;
        Change<J.CompilationUnit> changed = tree.refactor().visit(refactor.build()).fix();
        overWriteToFile(changed, fix);
      }
      counter++;
    }
  }

  private void saveImport(J.CompilationUnit tree) {
    List<J.Import> tmp = tree.getImports();
    imports = new ArrayList<>(tmp);
    tmp.clear();
  }

  private void overWriteToFile(Change<J.CompilationUnit> change, Fix fix) {
    String path = fix.uri;
    if (mode.equals(MODE.TEST)) {
      path = path.replace("src", "out");
    }
    if(!cleanImports) {
      ArrayList<J.Import> tmp = new ArrayList<>();
      for(J.Import imp: imports) if(!addedImports.contains(imp.getTypeName())) tmp.add(imp);
      change.getFixed().getImports().addAll(tmp);
    }
    String input = change.getFixed().print();
    String pathToFileDirectory = path.substring(0, path.lastIndexOf("/"));
    try {
      Files.createDirectories(Paths.get(pathToFileDirectory + "/"));
      try (Writer writer = Files.newBufferedWriter(Paths.get(path), Charset.defaultCharset())) {
        writer.write(input);
        writer.flush();
      }
    } catch (IOException e) {
      throw new RuntimeException("Something terrible happened.");
    }
  }

  private J.CompilationUnit getTree(Fix fix) {
    ArrayList<Path> p = new ArrayList<>();
    p.add(Paths.get(fix.uri));
    parser.reset();
    ArrayList<J.CompilationUnit> trees = (ArrayList<J.CompilationUnit>) parser.parse(p);
    if (trees == null || trees.size() != 1)
      throw new RuntimeException("Error in crating AST tree for file at path: " + fix.uri);
    return trees.get(0);
  }

  private ArrayList<Fix> readFixes() {
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

  private ArrayList<Fix> extractFixesFromJson(JSONArray fixesJson) {
    ArrayList<Fix> fixes = new ArrayList<>();
    for (Object o : fixesJson) {
      Fix fix = Fix.createFromJson((JSONObject) o);
      if(!fixes.contains(fix))
        fixes.add(fix);
    }
    return fixes;
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