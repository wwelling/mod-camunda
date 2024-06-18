package org.folio.rest.camunda.delegate;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Objects;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.model.bpmn.instance.FlowElement;
import org.folio.rest.camunda.service.ScriptEngineService;
import org.folio.rest.workflow.enums.ScriptType;
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
    delegate.setInputVariables(inputVariables);
    // output delegate
    delegate.setOutputVariable(outputVariable);

    // unique per delegate
    delegate.setProcessor(processor);
  }

  @ParameterizedTest
  @MethodSource("executionStream")
  @SuppressWarnings("unchecked")
  void testExecute(String processorValue, String inputVariablesValue, String outputVariableValue,
      Class<Exception> exception) throws Exception {

    lenient().when(execution.getBpmnModelElementInstance()).thenReturn(element);
    lenient().when(element.getName()).thenReturn(delegate.getClass().getSimpleName());
    lenient().when(processor.getValue(any(DelegateExecution.class))).thenReturn(processorValue);
    lenient().when(inputVariables.getValue(any(DelegateExecution.class))).thenReturn(inputVariablesValue);
    lenient().when(outputVariable.getValue(any(DelegateExecution.class))).thenReturn(outputVariableValue);

    lenient().when(scriptEngineService.runScript(anyString(), anyString(), any(JsonNode.class))).thenReturn("");

    if (Objects.nonNull(exception)) {
      assertThrows(exception, () -> delegate.execute(execution));
    } else {

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
   *         The arguments array stream with the stream columns as:
   *         - processors to register (JSON of type processor)
   *         - input variables (JSON map of <String, EmbeddedVariable>)
   *         - output variable (JSON of EmbeddedVariable)
   *         - exception that is expected to be thrown for inputs
   * @throws JsonProcessingException
   */
  private static Stream<Arguments> executionStream() throws JsonProcessingException {
    ObjectMapper om = new ObjectMapper();

    EmbeddedProcessor jsTest = new EmbeddedProcessor();
    jsTest.setScriptType(ScriptType.JS);
    jsTest.setFunctionName("test");

    String js_test = om.writeValueAsString(jsTest);

    EmbeddedProcessor groovyTest = new EmbeddedProcessor();
    groovyTest.setScriptType(ScriptType.GROOVY);
    groovyTest.setFunctionName("test");

    String groovy_test = om.writeValueAsString(groovyTest);

    EmbeddedVariable l = new EmbeddedVariable();
    l.setKey("key");
    l.setType(VariableType.LOCAL);

    String local = om.writeValueAsString(l);

    EmbeddedVariable p = new EmbeddedVariable();
    p.setKey("key");
    p.setType(VariableType.PROCESS);

    String process = om.writeValueAsString(p);

    return Stream.of(
        Arguments.of(null, null, null, NullPointerException.class),
        Arguments.of("", "", "", MismatchedInputException.class),
        Arguments.of("{}", "[]", "{}", NullPointerException.class),
        Arguments.of(js_test, "[]", local, null),
        Arguments.of(groovy_test, "[]", process, null));
  }

}
