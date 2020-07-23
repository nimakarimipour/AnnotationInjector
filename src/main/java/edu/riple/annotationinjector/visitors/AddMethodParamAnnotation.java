package edu.riple.annotationinjector.visitors;

import edu.riple.annotationinjector.Fix;
import org.openrewrite.java.AddAnnotation;
import org.openrewrite.java.JavaRefactorVisitor;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.Statement;

public class AddMethodParamAnnotation extends Refactor {


    public AddMethodParamAnnotation(Fix fix, J.CompilationUnit tree) {
        super(fix, tree);
    }

    @Override
    public JavaRefactorVisitor build() {
        J.MethodDecl methodDecl = ASTHelpers.findMethodDecl(tree, fix);
        if(methodDecl == null) throw new RuntimeException("No method found with signature: " + fix);
        for (Statement param : methodDecl.getParams().getParams()) {
            if (param instanceof J.VariableDecls) {
                if (fix.param.equals(((J.VariableDecls) param).getVars().get(0).getSimpleName()))
                    return new AddAnnotation.Scoped(param, fix.annotation);
            } else throw new RuntimeException("Unknown tree type for method parameter declaration: " + param);
        }
        throw new RuntimeException("Could not find the param to for method to inject annotation: " + fix);
    }
}
