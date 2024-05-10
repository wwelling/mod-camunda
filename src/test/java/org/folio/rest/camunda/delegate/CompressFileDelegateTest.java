package org.folio.rest.camunda.delegate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.util.ReflectionTestUtils.getField;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.folio.rest.workflow.model.CompressFileTask;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CompressFileDelegateTest {

  @Mock
  private DelegateExecution delegateExecution;

  @Mock
  private Expression expression;

  @Spy
  private CompressFileDelegate compressFileDelegate;

  @Test
  void testSetSourceWorks() {
    setField(compressFileDelegate, "source", null);

    compressFileDelegate.setSource(expression);
    assertEquals(expression, getField(compressFileDelegate, "source"));
  }

  @Test
  void testSetDestinationWorks() {
    setField(compressFileDelegate, "destination", null);

    compressFileDelegate.setDestination(expression);
    assertEquals(expression, getField(compressFileDelegate, "destination"));
  }

  @Test
  void testSetFormatWorks() {
    setField(compressFileDelegate, "format", null);

    compressFileDelegate.setFormat(expression);
    assertEquals(expression, getField(compressFileDelegate, "format"));
  }

  @Test
  void testSetContainerWorks() {
    setField(compressFileDelegate, "container", null);

    compressFileDelegate.setContainer(expression);
    assertEquals(expression, getField(compressFileDelegate, "container"));
  }

  @Test
  void testFromTaskWorks() {
    assertEquals(CompressFileTask.class, compressFileDelegate.fromTask());
  }

}
