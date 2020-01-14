package org.folio.rest.model;

import org.folio.rest.workflow.model.ScriptType;

public class Script {

  private String name;

  private String code;

  private ScriptType type;

  public Script() {
    super();
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public ScriptType getType() {
    return type;
  }

  public void setType(ScriptType type) {
    this.type = type;
  }

  public static Script of(String name, String code, ScriptType type) {
    Script script = new Script();
    script.setName(name);
    script.setCode(code);
    script.setType(type);
    return script;
  }

}
