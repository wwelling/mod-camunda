package org.folio.rest.delegate;

import java.util.Map;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.model.bpmn.instance.FlowElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;

@Service
@Scope("prototype")
public class EmailDelegate extends AbstractWorkflowInputDelegate {

  @Autowired
  private JavaMailSender emailSender;

  private Expression to;

  private Expression cc;

  private Expression bcc;

  private Expression from;

  private Expression subject;

  private Expression text;

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    long startTime = System.nanoTime();
    FlowElement bpmnModelElement = execution.getBpmnModelElementInstance();
    String delegateName = bpmnModelElement.getName();

    logger.info("{} started", delegateName);

    String to = this.to.getValue(execution).toString();
    String cc = this.cc.getValue(execution).toString();
    String bcc = this.bcc.getValue(execution).toString();
    String from = this.from.getValue(execution).toString();
    String subjectTemplate = this.subject.getValue(execution).toString();
    String textTemplate = this.text.getValue(execution).toString();

    Map<String, Object> inputs = getInputs(execution);

    Configuration cfg = new Configuration(Configuration.VERSION_2_3_23);

    StringTemplateLoader stringLoader = new StringTemplateLoader();
    stringLoader.putTemplate("subject", subjectTemplate);
    stringLoader.putTemplate("text", textTemplate);
    cfg.setTemplateLoader(stringLoader);

    String subject = FreeMarkerTemplateUtils.processTemplateIntoString(cfg.getTemplate("subject"), inputs);
    String text = FreeMarkerTemplateUtils.processTemplateIntoString(cfg.getTemplate("text"), inputs);

    SimpleMailMessage message = new SimpleMailMessage();
    message.setTo(to);
    message.setCc(cc);
    message.setBcc(bcc);
    message.setFrom(from);
    message.setSubject(subject);
    message.setText(text);

    emailSender.send(message);

    long endTime = System.nanoTime();
    logger.info("{} finished in {} milliseconds", delegateName, (endTime - startTime) / (double) 1000000);
  }

  public void setTo(Expression to) {
    this.to = to;
  }

  public void setCc(Expression cc) {
    this.cc = cc;
  }

  public void setBcc(Expression bcc) {
    this.bcc = bcc;
  }

  public void setFrom(Expression from) {
    this.from = from;
  }

  public void setSubject(Expression subject) {
    this.subject = subject;
  }

  public void setText(Expression text) {
    this.text = text;
  }

  @Override
  public Class<?> fromTask() {
    return EmailDelegate.class;
  }

}
