package org.folio.rest.model;

public enum TaskScriptType {
  NONE(null),
  GROOVY("groovy"),
  JAVA("java"),
  JS("js"),
  PYTHON("py"),
  RUBY("rb"),
  XSLT("xslt");

  public final String engineName;
 
  private TaskScriptType(String engineName) {
      this.engineName = engineName;
  }

}