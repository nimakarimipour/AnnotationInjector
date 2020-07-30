package edu.riple.annotationinjector.visitors;


import edu.riple.annotationinjector.Fix;
import org.openrewrite.java.AddAnnotation;
import org.openrewrite.java.JavaRefactorVisitor;
import org.openrewrite.java.tree.J;

public class AddMethodReturnAnnotation extends Refactor {

    public AddMethodReturnAnnotation(Fix fix, J.CompilationUnit tree) {
        super(fix, tree);
    }

    @Override
    public JavaRefactorVisitor build() {
        J.ClassDecl classDecl = ASTHelpers.findClassDecl(tree, fix.className);
        if (classDecl == null)
            return null;
        J.MethodDecl methodDecl = ASTHelpers.findMethodDecl(classDecl, fix.method);
        if(methodDecl == null) throw new RuntimeException("No method found with signature: " + fix);
        return new AddAnnotation.Scoped(methodDecl, fix.annotation);
    }
}
