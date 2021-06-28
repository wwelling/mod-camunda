package org.folio.rest.delegate;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.Selectors;
import org.apache.commons.vfs2.VFS;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.model.bpmn.instance.FlowElement;
import org.folio.rest.workflow.model.FtpTask;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope("prototype")
public class FtpDelegate extends AbstractWorkflowIODelegate {

  private Expression originPath;

  private Expression destinationUri;

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    long startTime = System.nanoTime();
    FlowElement bpmnModelElement = execution.getBpmnModelElementInstance();
    String delegateName = bpmnModelElement.getName();

    logger.info("{} started", delegateName);

    String originPath = this.originPath.getValue(execution).toString();

    String destinationUri = this.destinationUri.getValue(execution).toString();

    File file = new File(originPath);

    FileSystemManager manager = VFS.getManager();

    try {

      URI uri = new URI(destinationUri);

      try (
        FileObject local = manager.resolveFile(file.getAbsolutePath());
        FileObject remote = manager.resolveFile(uri);
      ) {
        remote.copyFrom(local, Selectors.SELECT_SELF);
      } catch (FileSystemException e) {
        if (logger.isDebugEnabled()) {
          e.printStackTrace();
        }
        logger.error("Failed to transfer {} to {}: {}", originPath, destinationUri, e.getMessage());
      }

    } catch (URISyntaxException e) {
      if (logger.isDebugEnabled()) {
        e.printStackTrace();
      }
      logger.error("Failed to parse destination {}: {}", destinationUri, e.getMessage());
    }

    long endTime = System.nanoTime();
    logger.info("{} finished in {} milliseconds", delegateName, (endTime - startTime) / (double) 1000000);
  }

  public void setOriginPath(Expression originPath) {
    this.originPath = originPath;
  }

  public void setDestinationUri(Expression destinationUri) {
    this.destinationUri = destinationUri;
  }

  @Override
  public Class<?> fromTask() {
    return FtpTask.class;
  }

}
