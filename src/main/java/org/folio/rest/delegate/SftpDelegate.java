package org.folio.rest.delegate;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.model.bpmn.instance.FlowElement;
import org.folio.rest.workflow.model.SftpOp;
import org.folio.rest.workflow.model.SftpTask;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope("prototype")
public class SftpDelegate extends AbstractWorkflowIODelegate {

  private Expression originPath;

  private Expression destinationPath;

  private Expression op;

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

    String host = this.host.getValue(execution).toString();

    SftpOp op = SftpOp.valueOf(this.op.getValue(execution).toString());

    int port = Integer.parseInt(this.port.getValue(execution).toString());

    String username = this.username.getValue(execution).toString();

    String password = this.password.getValue(execution).toString();

    Session jschSession = null;
    try {
      JSch jsch = new JSch();

      jschSession = jsch.getSession(username, host, port);

      // dont have known_host location
      jschSession.setConfig("StrictHostKeyChecking", "no");

      jschSession.setPassword(password);

      jschSession.connect(15000);

      Channel sftp = jschSession.openChannel("sftp");

      sftp.connect(5000);

      ChannelSftp channelSftp = (ChannelSftp) sftp;

      switch (op) {
        case GET:
          channelSftp.get(destinationPath, originPath);
          break;
        case PUT:
          channelSftp.put(originPath, destinationPath);
          break;
        default:
          break;
      }

      channelSftp.exit();

    } finally {
      if (jschSession != null) {
        jschSession.disconnect();
      }
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
    return SftpTask.class;
  }

}
