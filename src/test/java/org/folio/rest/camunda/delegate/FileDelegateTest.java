package org.folio.rest.camunda.delegate;

import static org.folio.rest.camunda.utility.TestUtility.i;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.model.bpmn.instance.FlowElement;
import org.folio.rest.camunda.service.ScriptEngineService;
import org.folio.rest.workflow.enums.FileOp;
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

  private final Map<String, Object> mockData = new HashMap<>() {{
    put("data", new ArrayList<>() {{
      add("Hello, World!");
    }});
    put("path", "/test/path");
    put("tenandId", "diku");
    put("timestamp", new Date().getTime());
  }};

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

    Set<EmbeddedVariable> inputs = objectMapper.readValue(inputVariablesValue, new TypeReference<Set<EmbeddedVariable>>() {});

    for (EmbeddedVariable variable : inputs) {
      Object value = mockData.get(variable.getKey());
      switch (variable.getType()) {
        case LOCAL:
          when(execution.getVariableLocal(variable.getKey())).thenReturn(value);
          break;
        case PROCESS:
          when(execution.getVariable(variable.getKey())).thenReturn(value);
          break;
        default:
          break;
      }
    }

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

      switch (fileOp) {
        case LIST, READ, READ_LINE, LINE_COUNT:
          EmbeddedVariable output = objectMapper.readValue(outputVariableValue, EmbeddedVariable.class);
          switch (output.getType()) {
            case LOCAL:
              verify(execution, times(1)).setVariableLocal(eq(output.getKey()), any());
              break;
            case PROCESS:
              verify(execution, times(1)).setVariable(eq(output.getKey()), any());
              break;
            default:
              break;
          }
          break;
        case WRITE:
          assertTrue(new File(pathValue).exists());
          break;
        case COPY:
          // for when file doesn't exist and no exception thrown
          if (StringUtils.isNotEmpty(pathValue)) {
            assertTrue(new File(pathValue).exists());
            assertTrue(new File(targetValue).exists());
          }
          break;
        case MOVE:
          // for when file doesn't exist and no exception thrown
          if (StringUtils.isNotEmpty(pathValue)) {
            assertTrue(!new File(pathValue).exists());
            assertTrue(new File(targetValue).exists());
          }
          break;
        case DELETE:
          assertTrue(!new File(pathValue).exists());
          break;
        // case POP:
        // case PUSH:
        default:
          break;
      }


    }
  }

  /**
   * Helper function for parameterized test providing tests with
   *
   * @return
   *   The arguments array stream with the stream columns as:
   *         - inputVariables (set of EmbeddedVariable as JSON)
   *         - outputVariable (EmbeddedVariable as JSON)
   *         - path (path of source)
   *         - line (line in file)
   *         - op (GET, PUT)
   *         - target (input variable identifier)
   * @throws IOException
   * @throws JsonProcessingException
   */
  private static Stream<Arguments> executionStream() throws IOException {
    // arguments required for delegate expression

    // read input variables and output variable from files
    String inputVariables = "[]";

    String outputVariable = "{}";

    String files = "src/test/resources/files";

    String plain_txt = files + "/plain.txt";

    String zero = "0";
    String one = "1";

    String no_path = "";

    // must match an input variable key or target file path
    String no_target = "";

    String data_target = "data";

    String temp_plain_txt = files + "/temp/plain.txt";

    String temp_output = files + "/temp/output";

    // arguments for whether to expect exception thrown
    String noException = null;

    // arguments to assert about the test

    return Stream.of(
        Arguments.of(inputVariables, i("/output/file_task/data.json"), files, zero, FileOp.LIST.toString(), no_target, noException),
        Arguments.of(inputVariables, i("/output/file_task/data.json"), plain_txt, zero, FileOp.READ.toString(), no_target, noException),
        Arguments.of(inputVariables, i("/output/file_task/data.json"), plain_txt, zero, FileOp.LINE_COUNT.toString(), no_target, noException),
        Arguments.of(inputVariables, i("/output/file_task/data.json"), plain_txt, one, FileOp.READ_LINE.toString(), no_target, noException),
        Arguments.of(i("/input/file_task/write.json"), outputVariable, temp_output, zero, FileOp.WRITE.toString(), data_target, noException),

        // Arguments.of(inputVariables, outputVariable, plain_txt, zero, FileOp.PUSH.toString(), no_target, noException),
        // Arguments.of(inputVariables, outputVariable, plain_txt, zero, FileOp.POP.toString(), no_target, noException),

        // fails silently
        Arguments.of(inputVariables, outputVariable, no_path, zero, FileOp.COPY.toString(), temp_plain_txt, noException),
        // fails silently
        Arguments.of(inputVariables, outputVariable, no_path, zero, FileOp.MOVE.toString(), temp_plain_txt, noException),

        // must be done last

        // copy file
        Arguments.of(inputVariables, outputVariable, plain_txt, zero, FileOp.COPY.toString(), temp_plain_txt, noException),

        // delete a file
        Arguments.of(inputVariables, outputVariable, plain_txt, zero, FileOp.DELETE.toString(), no_target, noException),

        // move file
        Arguments.of(inputVariables, outputVariable, temp_plain_txt, zero, FileOp.MOVE.toString(), plain_txt, noException),

        // delete temp_output
        Arguments.of(inputVariables, outputVariable, temp_output, zero, FileOp.DELETE.toString(), no_target, noException)
    );
  }

}
