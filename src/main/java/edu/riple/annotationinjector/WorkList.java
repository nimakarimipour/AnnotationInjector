package edu.riple.annotationinjector;

import java.util.ArrayList;
import java.util.List;

public class WorkList {
  private final String uri;
  private final ArrayList<Fix> fixes;

  public WorkList(String uri) {
    this.uri = uri;
    this.fixes = new ArrayList<>();
  }

  public void addFix(Fix newFix) {
    for (Fix fix : fixes) if (fix.equals(newFix)) return;
    fixes.add(newFix);
  }

  public ArrayList<Fix> getFixes() {
    return fixes;
  }

  public String getUri() {
    return uri;
  }

  public void addContainingAnnotationsToList(List<String> annotsList) {
    for (Fix fix : fixes) if (!annotsList.contains(fix.annotation)) annotsList.add(fix.annotation);
  }

  public String className(){
      return fixes.get(0).className;
  }
}
