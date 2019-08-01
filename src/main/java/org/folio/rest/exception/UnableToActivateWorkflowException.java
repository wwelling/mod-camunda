package org.folio.rest.exception;

public class UnableToActivateWorkflowException extends Exception {

  private static final long serialVersionUID = -494877889944031067L;
  private final static String UNABLE_TO_ACTIVATE_MESSAGE = "Unable to activate workflow: %s.";

  public UnableToActivateWorkflowException(String id) {
    super(String.format(UNABLE_TO_ACTIVATE_MESSAGE, id));
  }

}
