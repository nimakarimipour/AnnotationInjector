package edu.riple.annotationinjector.visitors;

import org.openrewrite.java.JavaRefactorVisitor;

public interface Refactor {
    JavaRefactorVisitor build();
}
