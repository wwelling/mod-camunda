package org.folio.rest.delegate;

import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.model.bpmn.instance.FlowElement;
import org.folio.rest.workflow.model.EmailTask;
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

  private Expression mailTo;

  private Expression mailCc;

  private Expression mailBcc;

  private Expression mailFrom;

  private Expression mailSubject;

  private Expression mailText;

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    long startTime = System.nanoTime();
    FlowElement bpmnModelElement = execution.getBpmnModelElementInstance();
    String delegateName = bpmnModelElement.getName();

    logger.info("{} started", delegateName);

    String to = this.mailTo.getValue(execution).toString();
    String cc = Objects.nonNull(this.mailCc) ? this.mailCc.getValue(execution).toString() : StringUtils.EMPTY;
    String bcc = Objects.nonNull(this.mailBcc) ? this.mailBcc.getValue(execution).toString() : StringUtils.EMPTY;
    String from = this.mailFrom.getValue(execution).toString();
    String subjectTemplate = this.mailSubject.getValue(execution).toString();
    String textTemplate = this.mailText.getValue(execution).toString();

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

  public void setMailTo(Expression mailTo) {
    this.mailTo = mailTo;
  }

  public void setMailCc(Expression mailCc) {
    this.mailCc = mailCc;
  }

  public void setMailBcc(Expression mailBcc) {
    this.mailBcc = mailBcc;
  }

  public void setMailFrom(Expression mailFrom) {
    this.mailFrom = mailFrom;
  }

  public void setMailSubject(Expression mailSubject) {
    this.mailSubject = mailSubject;
  }

  public void setMailText(Expression mailText) {
    this.mailText = mailText;
  }

  @Override
  public Class<?> fromTask() {
    return EmailTask.class;
  }

}
