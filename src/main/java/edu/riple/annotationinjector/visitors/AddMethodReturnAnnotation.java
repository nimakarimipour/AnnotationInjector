package edu.riple.annotationinjector.visitors;


import edu.riple.annotationinjector.Fix;
import org.openrewrite.java.AddAnnotation;
import org.openrewrite.java.JavaRefactorVisitor;
import org.openrewrite.java.tree.J;

public class AddMethodReturnAnnotation implements Refactor {

    private final Fix fix;
    private final J.CompilationUnit tree;

    public AddMethodReturnAnnotation(J.CompilationUnit tree, Fix fix) {
        this.fix = fix;
        this.tree = tree;
    }

    @Override
    public JavaRefactorVisitor build() {
        return new AddAnnotation.Scoped(ASTHelpers.findMethodDecl(tree, fix), fix.annotation);
    }
}
