package org.folio.rest.controller.advice;

import org.folio.rest.exception.WorkflowAlreadyActiveException;
import org.folio.rest.exception.WorkflowAlreadyDeactivatedException;
import org.folio.spring.model.response.Errors;
import org.folio.spring.utility.ErrorUtility;
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
  public Errors handleWorkflowAlreadyActiveException(WorkflowAlreadyActiveException exception) {
    logger.debug(exception.getMessage(), exception);
    return ErrorUtility.buildError(exception, HttpStatus.NOT_ACCEPTABLE);
  }

  @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
  @ExceptionHandler(WorkflowAlreadyDeactivatedException.class)
  public Errors handleWorkflowAlreadyDeactivatedException(WorkflowAlreadyDeactivatedException exception) {
    logger.debug(exception.getMessage(), exception);
    return ErrorUtility.buildError(exception, HttpStatus.NOT_ACCEPTABLE);
  }
}