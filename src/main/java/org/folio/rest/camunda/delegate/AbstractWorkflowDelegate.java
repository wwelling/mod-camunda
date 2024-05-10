package org.folio.rest.camunda.delegate;

public abstract class AbstractWorkflowDelegate extends AbstractRuntimeDelegate {

  public abstract Class<?> fromTask();

}
