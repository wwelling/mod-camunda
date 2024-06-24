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

  private static final String PLAIN_TEXT_FILE_PATH = "src/test/resources/files/plain.txt";

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

    // if (fileOp == FileOp.LINE_COUNT ||fileOp == FileOp.READ_LINE || fileOp == FileOp.READ || fileOp == FileOp.LIST) {
    lenient().when(outputVariable.getValue(any(DelegateExecution.class))).thenReturn(outputVariableValue);
    // }

    when(path.getValue(any(DelegateExecution.class))).thenReturn(pathValue);
    when(line.getValue(any(DelegateExecution.class))).thenReturn(lineValue);
    when(op.getValue(any(DelegateExecution.class))).thenReturn(opValue);

    if (fileOp == FileOp.WRITE) {
      when(target.getValue(any(DelegateExecution.class))).thenReturn(targetValue);
    }

    if (Objects.nonNull(exception)) {
      assertThrows(exception, () -> delegate.execute(execution));
    } else {

      delegate.execute(execution);

      // verify mock method calls were as expected

      // condition against parameters to not verify methods that are not called



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

    String plain_txt = PLAIN_TEXT_FILE_PATH;

    String zero = "0";

    // FileOp: LIST, READ, WRITE, COPY, MOVE, DELETE, LINE_COUNT, READ_LINE, PUSH, POP
    String listOp = "LIST";
    String readOp = "READ";
    String writeOp = "WRITE";
    String copyOp = "COPY";
    String moveOp = "MOVE";
    String deleteOp = "DELETE";
    String lineCountOp = "LINE_COUNT";
    String readLineOp = "READ_LINE";
    String pushOp = "PUSH";
    String popOp = "POP";

    // must match an input variable key
    String no_target = "";

    // arguments for whether to expect exception thrown
    String noException = null;

    // arguments to assert about the test

    return Stream.of(
        Arguments.of(inputVariables, outputVariable, plain_txt, zero, listOp, no_target, noException),
        Arguments.of(inputVariables, outputVariable, plain_txt, zero, readOp, no_target, noException),
        Arguments.of(inputVariables, outputVariable, plain_txt, zero, writeOp, no_target, NullPointerException.class),
        Arguments.of(inputVariables, outputVariable, plain_txt, zero, copyOp, no_target, noException),
        Arguments.of(inputVariables, outputVariable, plain_txt, zero, moveOp, no_target, noException),
        Arguments.of(inputVariables, outputVariable, plain_txt, zero, deleteOp, no_target, noException),
        Arguments.of(inputVariables, outputVariable, plain_txt, zero, lineCountOp, no_target, noException),
        Arguments.of(inputVariables, outputVariable, plain_txt, zero, readLineOp, no_target, noException),
        Arguments.of(inputVariables, outputVariable, plain_txt, zero, pushOp, no_target, noException),
        Arguments.of(inputVariables, outputVariable, plain_txt, zero, popOp, no_target, noException)
    );
  }

}
