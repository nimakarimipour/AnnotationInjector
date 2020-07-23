package edu.riple.annotationinjector;

import edu.riple.annotationinjector.visitors.AddClassFieldAnnotation;
import edu.riple.annotationinjector.visitors.AddMethodParamAnnotation;
import edu.riple.annotationinjector.visitors.AddMethodReturnAnnotation;
import edu.riple.annotationinjector.visitors.Refactor;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openrewrite.Change;
import org.openrewrite.java.Java11Parser;
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


@SuppressWarnings("UnusedVariable") //todo: Remove this later, this class is still under construction
public class Injector{

    private final JavaParser parser;
    private Path fixesFilePath;
    private ArrayList<Fix> fixes;
    private final MODE mode;

    public enum MODE{
        OVERWRITE,
        TEST
    }

    public Injector(MODE mode) {
        this.mode = mode;
        parser = Java11Parser.builder().relaxedClassTypeMatching(true).build();

    }

    public Injector(){
        this(MODE.OVERWRITE);
    }

    public static InjectorBuilder builder(MODE mode){
        return new InjectorBuilder(mode);
    }

    public void start() {
        fixes = readFixes();
        applyFixes();
    }

    private void applyFixes() {
        J.CompilationUnit tree;
        Refactor refactor = null;
        for (Fix fix : fixes) {
            tree = getTree(fix);
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
            if(refactor == null) throw new RuntimeException("Could not figure out the fix for: " + fix);
            Change<J.CompilationUnit> changed = tree.refactor().visit(refactor.build()).fix();
            overWriteToFile(changed, fix);
        }
    }

    private void overWriteToFile(Change<J.CompilationUnit> change, Fix fix){
        String path = fix.uri;
        if(mode.equals(MODE.TEST)){
            path = path.replace("src", "out");
        }
        String input = change.getFixed().print();
        String pathToFileDirectory = path.substring(0, path.lastIndexOf("/"));
        try {
            Files.createDirectories(Paths.get(pathToFileDirectory + "/"));
            try (Writer writer = Files.newBufferedWriter(
                    Paths.get(path), Charset.defaultCharset())) {
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
        if(trees == null || trees.size() != 1) throw new RuntimeException("Error in crating AST tree for file at path: " + fix.uri);
        return trees.get(0);
    }

    private ArrayList<Fix> readFixes(){
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
            fixes.add(Fix.createFromJson((JSONObject) o));
        }
        return fixes;
    }

    public static class InjectorBuilder{
        private final Injector injector;

        public InjectorBuilder(MODE mode){
            injector = new Injector(mode);
        }

        public InjectorBuilder setFixesJsonFilePath(String path){
            injector.fixesFilePath = Paths.get(path);
            return this;
        }


        public Injector build(){
            return injector;
        }
    }
}
