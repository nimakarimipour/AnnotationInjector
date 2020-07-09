package edu.riple.annotationinjector.visitors;

import org.openrewrite.java.JavaRefactorVisitor;

public abstract class Refactor {
    public abstract JavaRefactorVisitor build();
}
