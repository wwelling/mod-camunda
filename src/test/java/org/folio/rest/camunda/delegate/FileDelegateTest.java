package org.folio.rest.camunda.delegate;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.util.Objects;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.model.bpmn.instance.FlowElement;
import org.folio.rest.camunda.service.ScriptEngineService;
import org.folio.rest.workflow.enums.FileOp;
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
class FileDelegateTest {

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
  Expression path;

  @Mock
  Expression line;

  @Mock
  Expression op;

  @Mock
  Expression target;

  @Mock
  DelegateExecution execution;

  @Mock
  FlowElement element;

  @InjectMocks
  FileDelegate delegate;

  @BeforeEach
  void beforeEach() {
    // input delegate
    delegate.setInputVariables(inputVariables);
    // output delegate
    delegate.setOutputVariable(outputVariable);

    // unique per delegate
    delegate.setPath(path);
    delegate.setLine(line);
    delegate.setOp(op);
    delegate.setTarget(target);
  }

  @ParameterizedTest
  @MethodSource("executionStream")
  void testExecute(
      String inputVariablesValue,
      String outputVariableValue,
      String pathValue,
      String lineValue,
      String opValue,
      String targetValue,
      Class<Exception> exception
  ) throws Exception {

    FileOp fileOp = FileOp.valueOf(opValue);

    // mock all expression variables from parameters here
    when(execution.getBpmnModelElementInstance()).thenReturn(element);
    when(element.getName()).thenReturn(delegate.getClass().getSimpleName());

    when(inputVariables.getValue(any(DelegateExecution.class))).thenReturn(inputVariablesValue);

    lenient().when(outputVariable.getValue(any(DelegateExecution.class))).thenReturn(outputVariableValue);


    when(path.getValue(any(DelegateExecution.class))).thenReturn(pathValue);
    when(line.getValue(any(DelegateExecution.class))).thenReturn(lineValue);
    when(op.getValue(any(DelegateExecution.class))).thenReturn(opValue);

    lenient().when(target.getValue(any(DelegateExecution.class))).thenReturn(targetValue);

    if (Objects.nonNull(exception)) {
      assertThrows(exception, () -> delegate.execute(execution));
    } else {

      delegate.execute(execution);

      // verify lenient mock method calls were as expected


    }
  }

  /**
   * Helper function for parameterized test providing tests with
   *
   * @return
   *   The arguments array stream with the stream columns as:
   *         - inputVariables (set of JSON EmbeddedVariable)
   *         - outputVariable (JSON of EmbeddedVariable)
   *         - path (path of source)
   *         - line (line in file)
   *         - op (GET, PUT)
   *         - target (input variable identifier)
   * @throws JsonProcessingException
   */
  private static Stream<Arguments> executionStream() {
    // arguments required for delegate expression
    String inputVariables = "[]";

    String outputVariable = "{}";

    String files = "src/test/resources/files";

    String plain_txt = files + "/plain.txt";

    String zero = "0";

    // must match an input variable key or target file path
    String no_target = "";

    String temp_plain_txt = files + "/temp/plain.txt";

    // arguments for whether to expect exception thrown
    String noException = null;

    // arguments to assert about the test

    return Stream.of(
        Arguments.of(inputVariables, outputVariable, files, zero, FileOp.LIST.toString(), no_target, noException),
        Arguments.of(inputVariables, outputVariable, plain_txt, zero, FileOp.READ.toString(), no_target, noException),
        Arguments.of(inputVariables, outputVariable, plain_txt, zero, FileOp.WRITE.toString(), no_target, NullPointerException.class),

        Arguments.of(inputVariables, outputVariable, plain_txt, zero, FileOp.LINE_COUNT.toString(), no_target, noException),
        Arguments.of(inputVariables, outputVariable, plain_txt, zero, FileOp.READ_LINE.toString(), no_target, noException),
        // Arguments.of(inputVariables, outputVariable, plain_txt, zero, FileOp.PUSH.toString(), no_target, noException),
        // Arguments.of(inputVariables, outputVariable, plain_txt, zero, FileOp.POP.toString(), no_target, noException),

        // must be done last

        // copy file
        Arguments.of(inputVariables, outputVariable, plain_txt, zero, FileOp.COPY.toString(), temp_plain_txt, noException),

        // delete a file
        Arguments.of(inputVariables, outputVariable, plain_txt, zero, FileOp.DELETE.toString(), no_target, noException),

        // move file
        Arguments.of(inputVariables, outputVariable, temp_plain_txt, zero, FileOp.MOVE.toString(), plain_txt, noException)
    );
  }

}
