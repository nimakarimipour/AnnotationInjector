package edu.riple.annotationinjector;

import org.openrewrite.java.Java8Parser;
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.tree.J;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;


public class Injector{

    JavaParser parser;
    ArrayList<Path> paths;
    Path fixesFilePath;

    Injector() {
        parser = Java8Parser.builder().build();
        paths = new ArrayList<>();
    }

    public static InjectorBuilder builder(){
        return new InjectorBuilder();
    }

    public boolean start() {
        ArrayList<J.CompilationUnit> trees = (ArrayList<J.CompilationUnit>) parser.parse(paths);
        for (J.CompilationUnit unit : trees){
            System.out.println(unit.getImports().get(0).print());
        }
        return true;
    }

    public static class InjectorBuilder{
        private final Injector injector;

        public InjectorBuilder(){
            injector = new Injector();
        }

        public InjectorBuilder addPath(String path){
            injector.paths.add(Paths.get(path));
            return this;
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
