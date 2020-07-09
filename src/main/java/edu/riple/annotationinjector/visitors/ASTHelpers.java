package edu.riple.annotationinjector.visitors;

import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.Statement;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"UnusedVariable", "StringSplitter"}) //todo: Remove this later, this class is still under construction
public class ASTHelpers {

    public static J.ClassDecl findClassDecl(J.CompilationUnit tree, String fullName){
        String[] path =  fullName.split("\\.");
        for (J.ClassDecl classDecl : tree.getClasses()){
            if(classDecl.getSimpleName().equals(path[path.length - 1]))
                return classDecl;
        }
        return null;
    }

    public static J.MethodDecl findMethodDecl(J.ClassDecl classDecl, String signature){
        for(J.MethodDecl methodDecl : classDecl.getMethods()){
            if(matchesMethodSignature(methodDecl, signature)){
                return methodDecl;
            }
        }
        return null;
    }

    public static boolean matchesMethodSignature(J.MethodDecl methodDecl, String signature) {
        if(!methodDecl.getSimpleName().equals(signature.substring(0, signature.indexOf("("))))
            return false;
        String[] paramsTypesInSignature = signature.substring(signature.indexOf("("), signature.indexOf(")"))
                .replace("(", "")
                .replace(")", "")
                .split(",");
        List<Statement> params = methodDecl.getParams().getParams();
        ArrayList<String> paramTypes = new ArrayList<>();
        for (Statement param : params){
            if(param instanceof J.VariableDecls){
                paramTypes.add( ((J.VariableDecls) param).getTypeExpr().print());
            }else throw new RuntimeException("Unknown tree type for method parameter declaration: " + param);
        }
        if(paramTypes.size() != paramsTypesInSignature.length) return false;
        for(String p : paramsTypesInSignature)
            if (!paramTypes.contains(p)) return false;
        return true;
    }
}
