package org.folio.rest.model;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
public class ProcessorTask extends Task {

  @Column(length=10000)
  private String script;

  private TaskScriptType scriptType;

  public ProcessorTask() {
    super();
    this.setDelegate("testProcessDelegate");
  }

  public String getScript() {
    return script;
  }

  public void setScript(String script) {
    this.script = script;
  }

  public TaskScriptType getScriptType() {
    return scriptType;
  }

  public void setScriptType(TaskScriptType scriptType) {
    this.scriptType = scriptType;
  }

}
