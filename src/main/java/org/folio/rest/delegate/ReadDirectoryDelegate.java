package org.folio.rest.delegate;

import java.io.File;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.model.bpmn.instance.FlowElement;
import org.folio.rest.workflow.model.ReadDirectoryTask;
import org.python.jline.internal.Log;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;

@Service
@Scope("prototype")
public class ReadDirectoryDelegate extends AbstractWorkflowOutputDelegate {

  private Expression path;

  private Expression workflow;

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    long startTime = System.nanoTime();

    FlowElement bpmnModelElement = execution.getBpmnModelElementInstance();

    String delegateName = bpmnModelElement.getName();

    String tenant = execution.getTenantId();

    logger.info("{} started", delegateName);

    String path = this.path.getValue(execution).toString();

    String workflow = this.workflow.getValue(execution).toString();

    File workflowDirectory = new File(String.join(File.separator, path, tenant, workflow));

    if (!workflowDirectory.exists()) {
      // TODO: create custom exception and controller advice to handle better
      throw new RuntimeException(String.format("%s does not exist!", workflowDirectory.getAbsolutePath()));
    }

    File[] files = workflowDirectory.listFiles();

    if (files.length > 0) {

      File file = files[0];

      logger.info("Reading file {}", file.getAbsolutePath());

      JsonNode output = objectMapper.readTree(file);

      boolean useCacheOutput = Boolean.parseBoolean(getUseCacheOutput().getValue(execution).toString());

      String outputKey = getOutputKey().getValue(execution).toString();

      if (useCacheOutput) {
        contextCacheService.put(outputKey, output);
      } else {
        execution.setVariable(outputKey, output);
      }

    } else {
      Log.warn("No files in {}", workflowDirectory.getAbsolutePath());
    }

    long endTime = System.nanoTime();
    logger.info("{} finished in {} milliseconds", delegateName, (endTime - startTime) / (double) 1000000);
  }

  public void setPath(Expression path) {
    this.path = path;
  }

  public void setWorkflow(Expression workflow) {
    this.workflow = workflow;
  }

  @Override
  public Class<?> fromTask() {
    return ReadDirectoryTask.class;
  }

}
