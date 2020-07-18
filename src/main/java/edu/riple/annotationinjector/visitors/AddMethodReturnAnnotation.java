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
        J.MethodDecl methodDecl = ASTHelpers.findMethodDecl(tree, fix);
        if(methodDecl == null) throw new RuntimeException("No method found with signature: " + fix);
        return new AddAnnotation.Scoped(methodDecl, fix.annotation);
    }
}
