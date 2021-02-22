package edu.ucr.cs.riple.annotationinjector.visitors;


import edu.ucr.cs.riple.annotationinjector.Fix;
import org.openrewrite.java.AddAnnotation;
import org.openrewrite.java.JavaRefactorVisitor;
import org.openrewrite.java.tree.J;

public class AddMethodReturnAnnotation extends Refactor {

  public AddMethodReturnAnnotation(Fix fix, J.CompilationUnit tree) {
    super(fix, tree);
  }

  @Override
  public JavaRefactorVisitor build() {
    J.MethodDecl methodDecl;
    J.ClassDecl classDecl = ASTHelpers.findClassDecl(tree, fix.className);
    if (classDecl == null) {
      methodDecl = ASTHelpers.findMethodDecl(tree, fix.method);
    } else {
      methodDecl = ASTHelpers.findMethodDecl(classDecl, fix.method);
    }
    if (methodDecl == null) throw new RuntimeException("No method found with signature: " + fix);
    return new AddAnnotation.Scoped(methodDecl, fix.annotation);
  }
}
