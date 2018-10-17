package org.folio.rest.delegate;

import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Service;

@Service
public class System1Delegate extends AbstractLoggingJavaDelegate {

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    log.info("Executing System1 Service Delegate");

    String instanceId = execution.getProcessInstanceId();
    String definitionId = execution.getProcessDefinitionId();

    // @formatter:off
    RepositoryService repositoryService = execution
        .getProcessEngineServices()
        .getRepositoryService();
    
    String processName = repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionId(definitionId)
        .singleResult()
        .getName();
    // @formatter:on

    log.info("Process: {}, Instance: {}", processName, instanceId);

    execution.setVariable("delegateVariable", "SampleStringVariable");
  }

}
