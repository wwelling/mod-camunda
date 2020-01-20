package org.folio.rest.delegate;

import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.model.bpmn.instance.FlowElement;
import org.folio.rest.workflow.model.DirectoryAction;
import org.folio.rest.workflow.model.DirectoryTask;
import org.python.jline.internal.Log;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope("prototype")
public class ReadDirectoryDelegate extends AbstractWorkflowIODelegate {

  private Expression path;

  private Expression workflow;

  private Expression action;

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    long startTime = System.nanoTime();
    FlowElement bpmnModelElement = execution.getBpmnModelElementInstance();
    String delegateName = bpmnModelElement.getName();

    logger.info("{} started", delegateName);

    String tenant = execution.getTenantId();

    String path = this.path.getValue(execution).toString();

    String workflow = this.workflow.getValue(execution).toString();

    DirectoryAction action = DirectoryAction.valueOf(this.action.getValue(execution).toString());

    File workflowDirectory = new File(String.join(File.separator, path, tenant, workflow));

    if (!workflowDirectory.exists()) {
      // TODO: create custom exception and controller advice to handle better
      throw new RuntimeException(String.format("%s does not exist!", workflowDirectory.getAbsolutePath()));
    }

    File[] files = workflowDirectory.listFiles();

    if (files.length > 0) {

      File file = files[0];

      Object output;

      switch (action) {
      case DELETE_NEXT:
        logger.info("Removing file {}", file.getAbsolutePath());
        if (!Files.deleteIfExists(file.toPath())) {
          // TODO: create custom exception and controller advice to handle better
          throw new RuntimeException(String.format("%s would not delete!", file.getAbsolutePath()));
        }
        output = files.length - 1;
        break;
      case LIST:
        logger.info("Listing files {}", workflowDirectory.getAbsoluteFile());
        output = Arrays.asList(files).stream().map(f -> f.getName()).collect(Collectors.toList());
        break;
      case READ_NEXT:
        logger.info("Reading file {}", file.getAbsolutePath());
        output = objectMapper.readTree(file);
        break;
      case WRITE:
        // TODO: create custom exception and controller advice to handle better
        throw new RuntimeException(String.format("%s not yet supported!", action));
      default:
        // TODO: create custom exception and controller advice to handle better
        throw new RuntimeException(String.format("%s not a valid action!", action));
      }
      setOutput(execution, output);
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
    return DirectoryTask.class;
  }

}
