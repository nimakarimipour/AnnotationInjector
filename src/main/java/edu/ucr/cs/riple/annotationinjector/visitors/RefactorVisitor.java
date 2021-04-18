package edu.ucr.cs.riple.annotationinjector.visitors;

import edu.ucr.cs.riple.annotationinjector.Fix;
import edu.ucr.cs.riple.annotationinjector.WorkList;
import org.openrewrite.java.AddAnnotation;
import org.openrewrite.java.JavaIsoRefactorVisitor;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.Statement;

import java.util.HashMap;
import java.util.Map;

public class RefactorVisitor extends JavaIsoRefactorVisitor {
  private final WorkList workList;

  public RefactorVisitor(WorkList workList) {
    this.workList = workList;
    setCursoringOn();
  }

  @Override
  public J.ClassDecl visitClassDecl(J.ClassDecl classDecl) {
    Map<String, String > toAdd = new HashMap<>();
    for(Fix fix: workList.getFixes()){
      if(fix.location.equals("CLASS_FIELD") && Helper.lastName(classDecl.getSimpleName()).equals(Helper.lastName(fix.className))){
        toAdd.put(fix.param, fix.annotation);
      }
    }
    for(J.VariableDecls var: classDecl.getFields()){
      if(var.getVars() == null || var.getVars().size() < 1){
        continue;
      }
      String fieldName = var.getVars().get(0).getSimpleName();
      if(toAdd.containsKey(fieldName)){
        andThen(new AddAnnotation.Scoped(var, toAdd.get(fieldName)));
      }
    }
    return super.visitClassDecl(classDecl);
  }

  @Override
  public J.MethodDecl visitMethod(J.MethodDecl method) {
    for (Fix fix : workList.getFixes()) {
      if (Helper.matchesMethodSignature(method, fix.method)) {
        switch (fix.location) {
          case "METHOD_PARAM":
            for (Statement param : method.getParams().getParams()) {
              if (param instanceof J.VariableDecls) {
                J.VariableDecls par = (J.VariableDecls) param;
                if(par.getVars() == null || par.getVars().size() < 1){
                    continue;
                }
                if(par.getVars().get(0).getSimpleName().equals(fix.param)){
                    andThen(new AddAnnotation.Scoped(par, fix.annotation));
                }
              }
            }
            break;
          case "METHOD_RETURN":
            andThen(new AddAnnotation.Scoped(method, fix.annotation));
            break;
          default:
            return super.visitMethod(method);
        }
      }
    }
    return super.visitMethod(method);
  }
}
