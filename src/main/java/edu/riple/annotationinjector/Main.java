package edu.riple.annotationinjector;

import org.openrewrite.java.Java8Parser;
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.tree.J;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Main {
  public static void main(String[] args) {
    parse();
  }

  private static void parse() {
    JavaParser parser =
            Java8Parser.builder()
                    .relaxedClassTypeMatching(true)
                    .logCompilationWarningsAndErrors(false)
                    .build();

    String uri = "/Users/nima/Developer/WALA/com.ibm.wala.core/src/main/java/com/ibm/wala/analysis/typeInference/TypeInference.java";
    ArrayList<Path> p = new ArrayList<>();
    p.add(Paths.get(uri));
    ArrayList<J.CompilationUnit> trees = (ArrayList<J.CompilationUnit>) parser.parse(p);
    J.CompilationUnit tree = trees.get(0);
    System.out.println(tree.print());
  }
}
