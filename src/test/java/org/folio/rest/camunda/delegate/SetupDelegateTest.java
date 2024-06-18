package org.folio.rest.camunda.delegate;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Objects;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.model.bpmn.instance.FlowElement;
import org.folio.rest.camunda.service.ScriptEngineService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ExtendWith(MockitoExtension.class)
class SetupDelegateTest {

  @Spy
  protected ObjectMapper objectMapper;

  @Spy
  protected RuntimeService runtimeService;

  @Spy
  private ScriptEngineService scriptEngineService;

  @Mock
  Expression initialContext;

  @Mock
  Expression processors;

  @Mock
  DelegateExecution execution;

  @Mock
  FlowElement element;

  @InjectMocks
  SetupDelegate delegate;

  @BeforeEach
  void beforeEach() {
    // this is unique per delegate
    delegate.setInitialContext(initialContext);
    delegate.setProcessors(processors);
  }

  @ParameterizedTest
  @MethodSource("executionStream")
  @SuppressWarnings("unchecked")
  void testExecute(String initialContextValue, String processorsValue, Class<Exception> exception) throws Exception {
    lenient().when(execution.getTenantId()).thenReturn("diku");
    lenient().when(execution.getBpmnModelElementInstance()).thenReturn(element);
    lenient().when(element.getName()).thenReturn(delegate.getClass().getSimpleName());
    lenient().when(initialContext.getValue(any(DelegateExecution.class))).thenReturn(initialContextValue);
    lenient().when(processors.getValue(any(DelegateExecution.class))).thenReturn(processorsValue);

    if (Objects.nonNull(exception)) {
      assertThrows(exception, () -> delegate.execute(execution));
    } else {

      delegate.execute(execution);

      verify(element, times(1)).getName();
      verify(initialContext, times(1)).getValue(any(DelegateExecution.class));
      verify(processors, times(1)).getValue(any(DelegateExecution.class));
      verify(objectMapper, times(1)).readValue(eq(initialContextValue), any(TypeReference.class));

      // initialContext are not yet used and are subject to removal
      // for each initial context variable
      // verify objectMapper writeValueAsString and execution setVariable for each initial context

      verify(execution, times(1)).setVariable(eq("timestamp"), anyString());
      verify(execution, times(1)).setVariable("tenantId", "diku");

      // processors are not yet used and are subject to removal
      // for each processor
      // mock processor getScriptType getExtension chain
      // mock processor getFunctionName and getCode
      // mock scriptEngineService registerScript
      // verify objectMapper writeValueAsString and execution setVariable for each initial context
    }
  }

  /**
   * Helper function for parameterized test providing tests with
   *
   * @return
   *   The arguments array stream with the stream columns as:
   *     - initial context variables (JSON map of type Map<String, Object>)
   *     - processors to register (JSON list of type EmbeddedProcessor)
   *     - exception that is expected to be thrown for inputs
   */
  private static Stream<Arguments> executionStream() {
    return Stream.of(
      Arguments.of(null, null, NullPointerException.class),
      Arguments.of(null, "",   NullPointerException.class),
      Arguments.of(null, "[]", NullPointerException.class),
      Arguments.of("",   null, MismatchedInputException.class),
      Arguments.of("",   "",   MismatchedInputException.class),
      Arguments.of("",   "[]", MismatchedInputException.class),
      Arguments.of("{}", null, NullPointerException.class),
      Arguments.of("{}", "",   MismatchedInputException.class),
      Arguments.of("{}", "[]", null)
    );
  }


}
