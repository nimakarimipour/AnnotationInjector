package edu.riple.annotationinjector.visitors;

import edu.riple.annotationinjector.Fix;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.Statement;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({
  "UnusedVariable",
  "StringSplitter"
}) // todo: Remove this later, this class is still under construction
public class ASTHelpers {

  public static J.ClassDecl findClassDecl(J.CompilationUnit tree, String fullName) {
    String[] path = fullName.split("\\.");
    for (J.ClassDecl classDecl : tree.getClasses()) {
      if (classDecl.getSimpleName().equals(path[path.length - 1])) return classDecl;
      J.ClassDecl innerClass = findInnerClassDecl(classDecl, path[path.length - 1]);
      if (innerClass != null) return innerClass;
    }
    return null;
  }

  public static J.ClassDecl findInnerClassDecl(J.ClassDecl classDecl, String name) {
    for (J statement : classDecl.getBody().getStatements()) {
      if (statement instanceof J.ClassDecl) {
        J.ClassDecl innerClass = (J.ClassDecl) statement;
        if (innerClass.getSimpleName().equals(name)) return innerClass;
        else {
          J.ClassDecl res = findInnerClassDecl(innerClass, name);
          if (res != null) return res;
        }
      }
    }
    return null;
  }

  public static J.MethodDecl findMethodDecl(J.CompilationUnit tree, Fix fix) {
    J.ClassDecl classDecl = ASTHelpers.findClassDecl(tree, fix.className);
    if (classDecl == null)
      throw new RuntimeException("Could not find the class associated to fix: " + fix);
    return ASTHelpers.findMethodDecl(classDecl, fix.method);
  }

  public static J.MethodDecl findMethodDecl(J.ClassDecl classDecl, String signature) {
    for (J.MethodDecl methodDecl : classDecl.getMethods()) {
      if (matchesMethodSignature(methodDecl, signature)) return methodDecl;
    }
    return null;
  }

  public static boolean matchesMethodSignature(J.MethodDecl methodDecl, String signature) {
    if (!methodDecl.getSimpleName().equals(signature.substring(0, signature.indexOf("("))))
      return false;
    String[] paramsTypesInSignature =
        signature
            .substring(signature.indexOf("("), signature.indexOf(")"))
            .replace(" ", "")
            .replace("(", "")
            .replace(")", "")
            .split(",");

    if (paramsTypesInSignature.length == 1 && paramsTypesInSignature[0].equals(""))
      paramsTypesInSignature = new String[0];
    ArrayList<String> paramTypes =
        (ArrayList<String>) extractParamTypesOfMethodInString(methodDecl);
    if (paramTypes.size() != paramsTypesInSignature.length) return false;
    for (String i : paramsTypesInSignature) {
      String found = null;
      String last_i = lastName(i);
      for (String j : paramTypes) {
        String last_j = lastName(j);
        if (j.equals(i) || last_i.equals(last_j)) found = j;
      }
      if (found == null) return false;
      paramTypes.remove(found);
    }
    return true;
  }

  public static String lastName(String name) {
    if (!name.contains(".")) return name;
    String[] names = name.split("\\.");
    return names[names.length - 1];
  }

  public static List<String> extractParamTypesOfMethodInString(J.MethodDecl methodDecl) {
    ArrayList<String> paramTypes = new ArrayList<>();
    if (methodDecl.getParams().getParams().size() == 0) return paramTypes;
    if (methodDecl.getParams().getParams().get(0) instanceof J.Empty) return paramTypes;
    for (Statement param : methodDecl.getParams().getParams()) {
      if (param instanceof J.VariableDecls)
        paramTypes.add(getFullNameOfType((J.VariableDecls) param));
      else
        throw new RuntimeException("Unknown tree type for method parameter declaration: " + param);
    }
    return paramTypes;
  }

  public static String getFullNameOfType(J.VariableDecls variableDecls) {
    if (variableDecls.getTypeExpr().getType() != null) {
      String primeType = variableDecls.getTypeExpr().getType().toTypeTree().print();
      if (variableDecls.print().replace(" ", "").contains("[]")) primeType += "[]";
      return primeType;
    } else return variableDecls.getTypeExpr().print();
  }
}
