package edu.ucr.cs.riple.annotationinjector;

import java.util.ArrayList;

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

  public String className(){
    return fixes.get(0).className;
  }
}
