package org.folio.rest.delegate;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class System1Delegate implements JavaDelegate {

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    log.info("Executing System1 Service Delegate");

    String instanceId = execution.getProcessInstanceId();
    String definitionId = execution.getProcessDefinitionId();

    RepositoryService repositoryService = execution.getProcessEngineServices().getRepositoryService();
    String processName = repositoryService.createProcessDefinitionQuery()
      .processDefinitionId(definitionId)
      .singleResult()
      .getName();

    log.info("Process: {}, Instance: {}", processName, instanceId);

    execution.setVariable("delegateVariable", "SampleStringVariable");
  }
}
