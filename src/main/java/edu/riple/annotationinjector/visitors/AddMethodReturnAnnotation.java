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
        J.MethodDecl finalMethodDecl = null;
        for(J.ClassDecl classDecl : tree.getClasses()){
            for(J.MethodDecl methodDecl : classDecl.getMethods()){
                if(matches(methodDecl)){
                    finalMethodDecl = methodDecl;
                    break;
                }
            }
        }
        return new AddAnnotation.Scoped(finalMethodDecl, fix.annotation);
    }

    private boolean matches(J.MethodDecl methodDecl){
        //todo: Fix this to match method signature
        return true;
    }
}
