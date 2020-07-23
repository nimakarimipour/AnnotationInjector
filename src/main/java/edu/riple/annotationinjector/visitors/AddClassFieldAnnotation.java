package edu.riple.annotationinjector.visitors;

import edu.riple.annotationinjector.Fix;
import org.openrewrite.java.AddAnnotation;
import org.openrewrite.java.JavaRefactorVisitor;
import org.openrewrite.java.tree.J;

public class AddClassFieldAnnotation extends Refactor {

  public AddClassFieldAnnotation(Fix fix, J.CompilationUnit tree) {
    super(fix, tree);
  }

  @Override
  public JavaRefactorVisitor build() {
    J.ClassDecl classDecl = ASTHelpers.findClassDecl(tree, fix.className);
    if (classDecl == null) throw new RuntimeException("No class found associated to fix: " + fix);
    J.VariableDecls variableDecls = null;
    for (J.VariableDecls v : classDecl.getFields()) {
      for(J.VariableDecls.NamedVar namedVar : v.getVars()){
        if (namedVar.getSimpleName().equals(fix.param)) {
          variableDecls = v;
          break;
        }
      }
    }
    if (variableDecls == null)
      throw new RuntimeException("No variable found associated to fix: " + fix);
    return new AddAnnotation.Scoped(variableDecls, fix.annotation);
  }
}
