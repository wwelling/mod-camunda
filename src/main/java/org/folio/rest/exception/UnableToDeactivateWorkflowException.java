package org.folio.rest.exception;

public class UnableToDeactivateWorkflowException extends Exception {

  private static final long serialVersionUID = 5136418536493410632L;
  private final static String UNABLE_TO_DEACTIVATE_MESSAGE = "Unable to deactivate workflow: %s.";

  public UnableToDeactivateWorkflowException(String id) {
    super(String.format(UNABLE_TO_DEACTIVATE_MESSAGE, id));
  }

}
