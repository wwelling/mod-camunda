package org.folio.rest.camunda.exception;

public class ScriptTaskDeserializeCodeFailure extends Exception {

  private static final long serialVersionUID = -6270663785866339965L;

  private static final String MESSAGE = "Failed to De-serialize code for ScriptTask %s.";

  public ScriptTaskDeserializeCodeFailure(String scriptTaskUuid) {
    super(String.format(MESSAGE, scriptTaskUuid));
  }

  public ScriptTaskDeserializeCodeFailure(String scriptTaskUuid, Exception e) {
    super(String.format(MESSAGE, scriptTaskUuid), e);
  }

}
