package org.folio.rest.exception;

public class WorkflowAlreadyDeactivatedException extends Exception {

  private static final long serialVersionUID = -3039895403839535313L;
  private final static String WORKFLOW_ALREADY_DEACTIVATED_MESSAGE = "The workflow: %s, is already deactivated.";

  public WorkflowAlreadyDeactivatedException(String id) {
    super(String.format(WORKFLOW_ALREADY_DEACTIVATED_MESSAGE, id));
  }

}
