package edu.riple.annotationinjector;

import org.openrewrite.java.Java8Parser;
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.tree.J;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Main {
  public static void main(String[] args) {
//    parse();
    testMulti();
  }

  private static void testMulti() {
    final List<Callable<Integer>> workers = new ArrayList<>();
    int[] times = {4000, 3000, 1000, 8000};
    for (int i = 0; i < 4; i++) {
      workers.add(new Worker(i, times[i]));
    }
    final ExecutorService pool = Executors.newFixedThreadPool(4);
    int ids = 0;
    try {
      for (final Future<Integer> future : pool.invokeAll(workers)) {
        ids += future.get();
      }
    } catch (ExecutionException | InterruptedException ex) {
      System.err.println("Found exception executor faced an exception.");
    }
    pool.shutdown();
    System.out.println("IDS " + ids);
  }

  static class Worker implements Callable<Integer>{

    int id;
    int sleepTime;

    public Worker(int id, int sleepTime) {
      this.id = id;
      this.sleepTime = sleepTime;
    }

    @Override
    public Integer call(){
      try {
        for (int i = 0; i < 100000; i++) {
          System.out.println(id);
        }
        Thread.sleep(0);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      System.out.println("Completed: " + id);
      return id;
    }
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
