package edu.riple.annotationinjector.visitors;

import org.openrewrite.java.JavaRefactorVisitor;

public abstract class Refactor extends JavaRefactorVisitor {
    public abstract JavaRefactorVisitor build();
}
