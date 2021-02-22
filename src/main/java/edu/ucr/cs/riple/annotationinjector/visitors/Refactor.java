package edu.ucr.cs.riple.annotationinjector.visitors;

import edu.ucr.cs.riple.annotationinjector.Fix;
import org.openrewrite.java.JavaRefactorVisitor;
import org.openrewrite.java.tree.J;

public abstract class Refactor {

    protected final Fix fix;
    protected final J.CompilationUnit tree;

    public Refactor(Fix fix, J.CompilationUnit tree) {
        this.fix = fix;
        this.tree = tree;
    }

    public abstract JavaRefactorVisitor build();
}
