package org.folio.rest.delegate;

import java.io.File;
import java.net.URI;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.Selectors;
import org.apache.commons.vfs2.VFS;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.model.bpmn.instance.FlowElement;
import org.folio.rest.workflow.model.SftpOp;
import org.folio.rest.workflow.model.FtpTask;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope("prototype")
public class FtpDelegate extends AbstractWorkflowIODelegate {

  private Expression originPath;

  private Expression destinationPath;

  private Expression op;

  private Expression scheme;

  private Expression host;

  private Expression port;

  private Expression username;

  private Expression password;

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    long startTime = System.nanoTime();
    FlowElement bpmnModelElement = execution.getBpmnModelElementInstance();
    String delegateName = bpmnModelElement.getName();

    logger.info("{} started", delegateName);

    String originPath = this.originPath.getValue(execution).toString();

    String destinationPath = this.destinationPath.getValue(execution).toString();

    SftpOp op = SftpOp.valueOf(this.op.getValue(execution).toString());

    String scheme = this.scheme.getValue(execution).toString();

    String host = this.host.getValue(execution).toString();

    int port = Integer.parseInt(this.port.getValue(execution).toString());

    Optional<String> username = Objects.nonNull(this.username)
      ? Optional.of(this.username.getValue(execution).toString())
      : Optional.empty();

    Optional<String> password = Objects.nonNull(this.password)
      ? Optional.of(this.password.getValue(execution).toString())
      : Optional.empty();

    FileSystemManager manager = VFS.getManager();

    String userInfo = null;

    if (username.isPresent()) {
      userInfo = username.get();
    }

    if (password.isPresent()) {
      userInfo += ":" + password.get();
    }

    switch (op) {
      case GET: {

        File file = new File(destinationPath);

        URI uri = new URI(
          scheme,
          userInfo,
          host,
          port,
          originPath,
          null,
          null
        );

        try (
          FileObject local = manager.resolveFile(file.getAbsolutePath());
          FileObject remote = manager.resolveFile(uri);
        ) {
          local.copyFrom(remote, Selectors.SELECT_SELF);
        }

      } break;
      case PUT: {

        File file = new File(originPath);

        URI uri = new URI(
          scheme,
          userInfo,
          host,
          port,
          destinationPath,
          null,
          null
        );

        try (
          FileObject local = manager.resolveFile(file.getAbsolutePath());
          FileObject remote = manager.resolveFile(uri);
        ) {
          remote.copyFrom(local, Selectors.SELECT_SELF);
        }

      } break;
      default:
        break;
    }

    

    long endTime = System.nanoTime();
    logger.info("{} finished in {} milliseconds", delegateName, (endTime - startTime) / (double) 1000000);
  }

  public void setOriginPath(Expression originPath) {
    this.originPath = originPath;
  }

  public void setDestinationPath(Expression destinationPath) {
    this.destinationPath = destinationPath;
  }

  public void setOp(Expression op) {
    this.op = op;
  }

  public void setScheme(Expression scheme) {
    this.scheme = scheme;
  }

  public void setHost(Expression host) {
    this.host = host;
  }

  public void setPort(Expression port) {
    this.port = port;
  }

  public void setUsername(Expression username) {
    this.username = username;
  }

  public void setPassword(Expression password) {
    this.password = password;
  }

  @Override
  public Class<?> fromTask() {
    return FtpTask.class;
  }

}
