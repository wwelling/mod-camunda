package org.folio.rest.camunda.delegate;

import static org.folio.spring.test.mock.MockMvcConstant.KEY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.getField;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.mail.internet.MimeMessage;
import java.util.Arrays;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.model.bpmn.instance.FlowElement;
import org.folio.rest.workflow.model.EmailTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;

@ExtendWith(MockitoExtension.class)
class EmailDelegateTest {

  private static final String ATTACHMENT_PATH = "attachment_path";

  private static final String INCLUDE_ATTACHMENT = "include_attachment";

  private static final String MAIL_BCC = "mail_bcc";

  private static final String MAIL_CC = "mail_cc";

  private static final String MAIL_FROM = "mail_from";

  private static final String MAIL_MARKUP = "mail_markup";

  private static final String MAIL_SUBJECT = "mail_subject";

  private static final String MAIL_TEXT = "mail_text";

  private static final String MAIL_TO = "mail_to";

  @Mock
  private DelegateExecution delegateExecution;

  @Mock
  private JavaMailSender emailSender;

  @Mock
  private FlowElement flowElementBpmn;

  @Mock
  private MimeMessage mimeMessage;

  private Expression attachmentPathExpression;

  private Expression includeAttachmentExpression;

  private Expression mailBccExpression;

  private Expression mailCcExpression;

  private Expression mailFromExpression;

  private Expression mailMarkupExpression;

  private Expression mailSubjectExpression;

  private Expression mailTextExpression;

  private Expression mailToExpression;

  @InjectMocks
  private EmailDelegate emailDelegate;

  @BeforeEach
  void beforeEach() {
    attachmentPathExpression = mock(Expression.class);
    includeAttachmentExpression = mock(Expression.class);
    mailBccExpression = mock(Expression.class);
    mailCcExpression = mock(Expression.class);
    mailFromExpression = mock(Expression.class);
    mailMarkupExpression = mock(Expression.class);
    mailSubjectExpression = mock(Expression.class);
    mailTextExpression = mock(Expression.class);
    mailToExpression = mock(Expression.class);
  }

  @Test
  void testExecuteWorks() throws Exception {
    setupExecuteMocking();

    emailDelegate.execute(delegateExecution);

    verify(emailSender).send(any(MimeMessagePreparator.class));
  }

  @Test
  void testSetAttachmentPathWorks() {
    setField(emailDelegate, "attachmentPath", null);

    emailDelegate.setAttachmentPath(attachmentPathExpression);
    assertEquals(attachmentPathExpression, getField(emailDelegate, "attachmentPath"));
  }

  @Test
  void testSetIncludeAttachmentWorks() {
    setField(emailDelegate, "includeAttachment", null);

    emailDelegate.setIncludeAttachment(includeAttachmentExpression);
    assertEquals(includeAttachmentExpression, getField(emailDelegate, "includeAttachment"));
  }

  @Test
  void testSetMailBccWorks() {
    setField(emailDelegate, "mailBcc", null);

    emailDelegate.setMailBcc(mailBccExpression);
    assertEquals(mailBccExpression, getField(emailDelegate, "mailBcc"));
  }

  @Test
  void testSetMailCcWorks() {
    setField(emailDelegate, "mailCc", null);

    emailDelegate.setMailCc(mailCcExpression);
    assertEquals(mailCcExpression, getField(emailDelegate, "mailCc"));
  }

  @Test
  void testSetMailFromWorks() {
    setField(emailDelegate, "mailFrom", null);

    emailDelegate.setMailFrom(mailFromExpression);
    assertEquals(mailFromExpression, getField(emailDelegate, "mailFrom"));
  }

  @Test
  void testSetMailMarkupWorks() {
    setField(emailDelegate, "mailMarkup", null);

    emailDelegate.setMailMarkup(mailMarkupExpression);
    assertEquals(mailMarkupExpression, getField(emailDelegate, "mailMarkup"));
  }

  @Test
  void testSetMailSubjectWorks() {
    setField(emailDelegate, "mailSubject", null);

    emailDelegate.setMailSubject(mailSubjectExpression);
    assertEquals(mailSubjectExpression, getField(emailDelegate, "mailSubject"));
  }

  @Test
  void testSetMailTextWorks() {
    setField(emailDelegate, "mailText", null);

    emailDelegate.setMailText(mailTextExpression);
    assertEquals(mailTextExpression, getField(emailDelegate, "mailText"));
  }

  @Test
  void testSetMailToWorks() {
    setField(emailDelegate, "mailTo", null);

    emailDelegate.setMailTo(mailToExpression);
    assertEquals(mailToExpression, getField(emailDelegate, "mailTo"));
  }

  @Test
  void testFromTaskWorks() {
    assertEquals(EmailTask.class, emailDelegate.fromTask());
  }

  /**
   * Provide common mocking behavior for the execute() method.
   *
   * @throws JsonProcessingException On JSON processing error.
   */
  private void setupExecuteMocking() throws JsonProcessingException {
    when(delegateExecution.getBpmnModelElementInstance()).thenReturn(flowElementBpmn);
    when(flowElementBpmn.getName()).thenReturn(KEY);
    when(attachmentPathExpression.getValue(any())).thenReturn(ATTACHMENT_PATH);
    when(includeAttachmentExpression.getValue(any())).thenReturn(INCLUDE_ATTACHMENT);
    when(mailBccExpression.getValue(any())).thenReturn(MAIL_BCC);
    when(mailCcExpression.getValue(any())).thenReturn(MAIL_CC);
    when(mailFromExpression.getValue(any())).thenReturn(MAIL_FROM);
    when(mailMarkupExpression.getValue(any())).thenReturn(MAIL_MARKUP);
    when(mailSubjectExpression.getValue(any())).thenReturn(MAIL_SUBJECT);
    when(mailTextExpression.getValue(any())).thenReturn(MAIL_TEXT);
    when(mailToExpression.getValue(any())).thenReturn(MAIL_TO);

    doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        Object[] args = invocation.getArguments();

        if (args.length == 1) {
          MimeMessagePreparator preparator = (MimeMessagePreparator) args[0];
          preparator.prepare(mimeMessage);
        }

        return null;
      }
    }).when(emailSender).send(any(MimeMessagePreparator.class));

    setField(emailDelegate, "emailSender", emailSender);
    setField(emailDelegate, "attachmentPath", attachmentPathExpression);
    setField(emailDelegate, "includeAttachment", includeAttachmentExpression);
    setField(emailDelegate, "mailBcc", mailBccExpression);
    setField(emailDelegate, "mailCc", mailCcExpression);
    setField(emailDelegate, "mailFrom", mailFromExpression);
    setField(emailDelegate, "mailMarkup", mailMarkupExpression);
    setField(emailDelegate, "mailSubject", mailSubjectExpression);
    setField(emailDelegate, "mailText", mailTextExpression);
    setField(emailDelegate, "mailTo", mailToExpression);
  }

}
