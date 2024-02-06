package org.folio.rest.camunda.controller.advice;

import org.folio.rest.camunda.exception.WorkflowAlreadyActiveException;
import org.folio.rest.camunda.exception.WorkflowAlreadyDeactivatedException;
import org.folio.spring.web.model.response.ResponseErrors;
import org.folio.spring.web.utility.ErrorUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class WorkflowControllerAdvice {

  private static final Logger logger = LoggerFactory.getLogger(WorkflowControllerAdvice.class);

  @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
  @ExceptionHandler(WorkflowAlreadyActiveException.class)
  public ResponseErrors handleWorkflowAlreadyActiveException(WorkflowAlreadyActiveException exception) {
    logger.debug(exception.getMessage(), exception);
    return ErrorUtility.buildError(exception, HttpStatus.NOT_ACCEPTABLE);
  }

  @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
  @ExceptionHandler(WorkflowAlreadyDeactivatedException.class)
  public ResponseErrors handleWorkflowAlreadyDeactivatedException(WorkflowAlreadyDeactivatedException exception) {
    logger.debug(exception.getMessage(), exception);
    return ErrorUtility.buildError(exception, HttpStatus.NOT_ACCEPTABLE);
  }
}
