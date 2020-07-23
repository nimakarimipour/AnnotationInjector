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
        J.MethodDecl methodDecl = ASTHelpers.findMethodDecl(tree, fix);
        if(methodDecl == null) throw new RuntimeException("No method found with signature: " + fix);
        return new AddAnnotation.Scoped(methodDecl, fix.annotation);
    }
}
