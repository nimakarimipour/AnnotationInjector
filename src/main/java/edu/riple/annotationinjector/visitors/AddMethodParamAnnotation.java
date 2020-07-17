package edu.riple.annotationinjector.visitors;

import edu.riple.annotationinjector.Fix;
import org.openrewrite.java.AddAnnotation;
import org.openrewrite.java.JavaRefactorVisitor;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.Statement;

public class AddMethodParamAnnotation implements Refactor {

    private final Fix fix;
    private final J.CompilationUnit tree;

    public AddMethodParamAnnotation(J.CompilationUnit tree, Fix fix) {
        this.fix = fix;
        this.tree = tree;
    }

    @Override
    public JavaRefactorVisitor build() {
        J.MethodDecl methodDecl = ASTHelpers.findMethodDecl(tree, fix);
        for (Statement param : methodDecl.getParams().getParams()) {
            if (param instanceof J.VariableDecls) {
                if (fix.param.equals(ASTHelpers.getFullNameOfType((J.VariableDecls) param)))
                    return new AddAnnotation.Scoped(param, fix.annotation);
            } else throw new RuntimeException("Unknown tree type for method parameter declaration: " + param);
        }
        throw new RuntimeException("Could not find the param to for method to inject annotation: " + fix);
    }
}