package edu.riple.annotationinjector.visitors;


import edu.riple.annotationinjector.Fix;
import org.openrewrite.java.AddAnnotation;
import org.openrewrite.java.JavaRefactorVisitor;
import org.openrewrite.java.tree.J;

public class AddMethodReturnAnnotation extends Refactor {

    private final Fix fix;
    private final J.CompilationUnit tree;

    public AddMethodReturnAnnotation(J.CompilationUnit tree, Fix fix) {
        this.fix = fix;
        this.tree = tree;
    }

    @Override
    public JavaRefactorVisitor build() {
        J.ClassDecl classDecl = ASTHelpers.findClassDecl(tree, fix.className);
        J.MethodDecl methodDecl = ASTHelpers.findMethodDecl(classDecl, fix.method);
        if(methodDecl == null) throw new RuntimeException("Could not find the method associated to fix: " + fix);
        return new AddAnnotation.Scoped(methodDecl, fix.annotation);
    }
}
