package edu.riple.annotationinjector;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;


@RunWith(JUnit4.class)
public class BasicTest {

    Injector injector;
    String fixPath = "/Users/nima/Developer/AnnotationInjector/testfiles/fixes.json";

    @Before
    public void setup(){
        injector = Injector.builder().setFixesJsonFilePath(fixPath).build();
    }

    @Test
    public void basicTest(){
        System.out.println("Injector final output: " + injector.start());
    }
}