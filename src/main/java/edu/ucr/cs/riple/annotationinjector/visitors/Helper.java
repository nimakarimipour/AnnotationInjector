package edu.ucr.cs.riple.annotationinjector.visitors;

import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.Statement;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({
  "UnusedVariable",
  "StringSplitter"
}) // todo: Remove this later, this class is still under construction
public class Helper {

  public static String extractMethodName(String signature) {
    StringBuilder ans = new StringBuilder();
    int level = 0;
    for (int i = 0; i < signature.length(); i++) {
      char current = signature.charAt(i);
      if(current == '(') break;
      switch (current){
        case '>': ++level;
        break;
        case '<': --level;
        break;
        default:
          if(level == 0) ans.append(current);
      }
    }
    return ans.toString();
  }

  public static boolean matchesMethodSignature(J.MethodDecl methodDecl, String signature) {
    if (!methodDecl.getSimpleName().equals(extractMethodName(signature)))
      return false;
    List<String> paramsTypesInSignature = extractParamTypesOfMethodInString(signature);
    List<String> paramTypes = extractParamTypesOfMethodInString(methodDecl);
    if (paramTypes.size() != paramsTypesInSignature.size()) return false;
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
    int index = 0;
    StringBuilder ans = new StringBuilder();
    StringBuilder tmp = new StringBuilder();
    while (index < name.length()) {
      char c = name.charAt(index);
      switch (c) {
        case ' ':
        case '<':
        case '>':
        case ',':
          ans.append(tmp);
          ans.append(c);
          tmp = new StringBuilder();
          break;
        case '.':
          tmp = new StringBuilder();
          break;
        default:
          tmp.append(c);
      }
      index++;
    }
    if(name.length() > 0) ans.append(tmp);
    return ans.toString().replaceAll(" ", "");
  }

  public static List<String> extractParamTypesOfMethodInString(String signature) {
    signature =
        signature
            .substring(signature.indexOf("("))
            .replace("(", "")
            .replace(")", "");
    int index = 0;
    int generic_level = 0;
    List<String> ans = new ArrayList<>();
    StringBuilder tmp = new StringBuilder();
    while (index < signature.length()) {
      char c = signature.charAt(index);
      switch (c) {
        case '@':
          while (signature.charAt(index+1) == ' ' && index + 1 < signature.length()) index++;
          int annot_level = 0;
          boolean finished = false;
          while (!finished && index < signature.length()){
            if(signature.charAt(index) == '(') ++annot_level;
            if(signature.charAt(index) == ')') --annot_level;
            if(signature.charAt(index) == ' ' && annot_level == 0) finished = true;
            index++;
          }
          index --;
          break;
        case '<':
          generic_level++;
          tmp.append(c);
          break;
        case '>':
          generic_level--;
          tmp.append(c);
          break;
        case ',':
          if (generic_level == 0) {
            ans.add(tmp.toString());
            tmp = new StringBuilder();
          } else tmp.append(c);
          break;
        default:
          tmp.append(c);
      }
      index++;
    }
    if (signature.length() > 0 && generic_level == 0) ans.add(tmp.toString());
    return ans;
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
    int numOfDims = variableDecls.getVars().get(0).getDimensionsAfterName().size();
    if(numOfDims == 0) numOfDims = variableDecls.getDimensionsBeforeName().size();
    String begin = removeComments(variableDecls.getTypeExpr().print());
    int index = 0;
    while (index < begin.length() && (begin.charAt(index) == ' ' || begin.charAt(index) == '\n')) index++;
    begin = begin.substring(index);
    String fullName = removeComments(variableDecls.print());
    fullName = fullName.substring(fullName.indexOf(begin));
    StringBuilder ans = new StringBuilder(fullName.substring(0, fullName.lastIndexOf(" ")).replaceAll(" ", "").replace("\n", ""));
    int lastIndex = 0;
    int count = 0;
    while(lastIndex != -1){
      lastIndex = ans.toString().indexOf("[]",lastIndex);
      if(lastIndex != -1){
        count ++;
        lastIndex += "[]".length();
      }
    }
    numOfDims -= count;
    for (int i = 0; i < numOfDims; i++) ans.append("[]");
    return ans.toString();
  }

  private static String removeComments(String text){
    return text.replaceAll("(?:/\\*(?:[^*]|(?:\\*+[^*/]))*\\*+/)|(?://.*)","");
  }
}
