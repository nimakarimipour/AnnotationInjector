package edu.riple.annotationinjector;

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
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;


public class Injector{

    private final JavaParser parser;
    private Path fixesFilePath;
    private ArrayList<Fix> fixes;

    Injector() {
        parser = Java8Parser.builder().build();
    }

    public static InjectorBuilder builder(){
        return new InjectorBuilder();
    }

    public boolean start() {
        fixes = readFixes();
        applyFixes();
        return true;
    }

    private void applyFixes() {
        J.CompilationUnit tree;
        Refactor refactor = null;
        for (Fix fix : fixes) {
            tree = getTree(fix);
            switch (fix.location) {
                case "CLASS_FIELD":
                case "METHOD_LOCAL_VAR":
                case "METHOD_PARAM":
                    break;
                case "METHOD_RETURN":
                    refactor = new AddMethodReturnAnnotation(tree, fix);
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
        //todo: make it to overwrite file.
        System.out.println(change.getFixed().print());
    }

    private J.CompilationUnit getTree(Fix fix) {
        ArrayList<Path> p = new ArrayList<>();
        p.add(Paths.get(fix.uri));
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

        public InjectorBuilder(){
            injector = new Injector();
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
