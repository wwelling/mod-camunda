package org.folio.rest.camunda.delegate;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.model.bpmn.instance.FlowElement;
import org.folio.rest.camunda.service.ScriptEngineService;
import org.folio.rest.workflow.enums.VariableType;
import org.folio.rest.workflow.model.EmbeddedProcessor;
import org.folio.rest.workflow.model.EmbeddedVariable;
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
class ProcessorDelegateTest {

  @Spy
  protected ObjectMapper objectMapper;

  @Spy
  protected RuntimeService runtimeService;

  @Mock
  private ScriptEngineService scriptEngineService;

  @Mock
  Expression inputVariables;

  @Mock
  Expression outputVariable;

  @Mock
  Expression processor;

  @Mock
  DelegateExecution execution;

  @Mock
  FlowElement element;

  @InjectMocks
  ProcessorDelegate delegate;

  @BeforeEach
  void beforeEach() {
    // input delegate
    delegate.setProcessor(inputVariables);
    // output delegate
    delegate.setProcessor(outputVariable);

    // unique per delegate
    delegate.setProcessor(processor);
  }

  @ParameterizedTest
  @MethodSource("executionStream")
  void testExecute(String processorValue, String inputVariablesValue, String outputVariableValue, Class<Exception> exception) throws Exception {
    if (Objects.nonNull(exception)) {
      assertThrows(exception, () -> delegate.execute(execution));
    } else {

      when(execution.getBpmnModelElementInstance()).thenReturn(element);
      when(element.getName()).thenReturn(delegate.getClass().getSimpleName());
      when(processor.getValue(any(DelegateExecution.class))).thenReturn(processorValue);
      when(inputVariables.getValue(any(DelegateExecution.class))).thenReturn(inputVariablesValue);
      when(outputVariable.getValue(any(DelegateExecution.class))).thenReturn(outputVariableValue);

      when(scriptEngineService.runScript(anyString(), anyString(), any(JsonNode.class))).thenReturn("");

      delegate.execute(execution);

      verify(execution, times(1)).getBpmnModelElementInstance();
      verify(element, times(1)).getName();
      verify(processor, times(1)).getValue(any(DelegateExecution.class));
      verify(objectMapper, times(1)).readValue(processorValue, EmbeddedProcessor.class);
      verify(objectMapper, times(1)).readValue(eq(inputVariablesValue), any(TypeReference.class));
      verify(objectMapper, times(1)).valueToTree(any());
      verify(scriptEngineService, times(1)).runScript(anyString(), anyString(), any(JsonNode.class));

      EmbeddedVariable output = objectMapper.readValue(outputVariableValue, EmbeddedVariable.class);

      if (output.getType().equals(VariableType.LOCAL)) {
        verify(execution, times(1)).setVariableLocal(eq(output.getKey()), any());
      } else {
        verify(execution, times(1)).setVariable(eq(output.getKey()), any());
      }
    }
  }

  /**
   * Helper function for parameterized test providing tests with
   *
   * @return
   *   The arguments array stream with the stream columns as:
   *     - processors to register (JSON of type processor)
   *     - input variables (JSON map of <String, EmbeddedVariable>)
   *     - output variable (JSON of EmbeddedVariable)
   *     - exception that is expected to be thrown for inputs
   */
  private static Stream<Arguments> executionStream() {
    return Stream.of(
      Arguments.of(null, null, null, NullPointerException.class),
      Arguments.of("", "", "",  NullPointerException.class),
      Arguments.of("{}", "[]", "{}", NullPointerException.class),
      Arguments.of("{\"scriptType\": \"JS\", \"functionName\": \"test\", \"code\": \"console.log('test')\", \"buffer\": 0, \"delay\": 0}", "[]", "{\"key\": \"key\", \"type\": \"LOCAL\", \"spin\": false, \"asJson\": false, \"asTransient\": false }", null)
    );
  }

}
