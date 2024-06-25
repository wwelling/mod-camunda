package org.folio.rest.camunda.delegate;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.net.URI;
import java.util.Objects;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.Selectors;
import org.apache.commons.vfs2.VFS;
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
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ExtendWith(MockitoExtension.class)
class FtpDelegateTest {

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
  Expression originPath;

  @Mock
  Expression destinationPath;

  @Mock
  Expression op;

  @Mock
  Expression scheme;

  @Mock
  Expression host;

  @Mock
  Expression port;

  @Mock
  Expression username;

  @Mock
  Expression password;

  @Mock
  DelegateExecution execution;

  @Mock
  FlowElement element;

  @InjectMocks
  FtpDelegate delegate;

  @BeforeEach
  void beforeEach() {
    // input delegate
    delegate.setInputVariables(inputVariables);
    // output delegate
    delegate.setOutputVariable(outputVariable);

    // unique per delegate
    delegate.setOriginPath(originPath);
    delegate.setDestinationPath(destinationPath);
    delegate.setOp(op);
    delegate.setScheme(scheme);
    delegate.setHost(host);
    delegate.setPort(port);
    delegate.setUsername(username);
    delegate.setPassword(password);
  }

  @ParameterizedTest
  @MethodSource("executionStream")
  void testExecute(
      String inputVariablesValue,
      String outputVariableValue,
      String originPathValue,
      String destinationPathValue,
      String opValue,
      String schemeValue,
      String hostValue,
      String portValue,
      String usernameValue,
      String passwordValue,
      Class<Exception> exception
  ) throws Exception {
    // lenient mock all expression variables from parameters here
    // to avoid mimicking branching behavior of code before executing
    // this allows for all paths to have all mocks able to be called
    lenient().when(execution.getBpmnModelElementInstance()).thenReturn(element);
    lenient().when(element.getName()).thenReturn(delegate.getClass().getSimpleName());

    lenient().when(inputVariables.getValue(any(DelegateExecution.class))).thenReturn(inputVariablesValue);
    lenient().when(outputVariable.getValue(any(DelegateExecution.class))).thenReturn(outputVariableValue);
    lenient().when(originPath.getValue(any(DelegateExecution.class))).thenReturn(originPathValue);
    lenient().when(destinationPath.getValue(any(DelegateExecution.class))).thenReturn(destinationPathValue);
    lenient().when(op.getValue(any(DelegateExecution.class))).thenReturn(opValue);
    lenient().when(scheme.getValue(any(DelegateExecution.class))).thenReturn(schemeValue);
    lenient().when(host.getValue(any(DelegateExecution.class))).thenReturn(hostValue);
    lenient().when(port.getValue(any(DelegateExecution.class))).thenReturn(portValue);
    lenient().when(username.getValue(any(DelegateExecution.class))).thenReturn(usernameValue);
    lenient().when(password.getValue(any(DelegateExecution.class))).thenReturn(passwordValue);

    try (MockedStatic<VFS> utility = Mockito.mockStatic(VFS.class)) {

      FileSystemManager manager = mock(FileSystemManager.class);

      utility.when(VFS::getManager).thenReturn(manager);

      FileObject local = mock(FileObject.class);
      FileObject remote = mock(FileObject.class);

      if (opValue == "GET") {

        String filePath = new File(destinationPathValue).getAbsolutePath();

        URI uri = new URI(
            schemeValue,
            usernameValue + ":" + passwordValue,
            hostValue,
            Integer.valueOf(portValue),
            originPathValue,
            null,
            null
          );

        when(manager.resolveFile(filePath)).thenReturn(local);
        when(manager.resolveFile(uri)).thenReturn(remote);

        doNothing().when(local).copyFrom(remote, Selectors.SELECT_SELF);

      } else if (opValue == "PUT") {

        String filePath = new File(originPathValue).getAbsolutePath();

        URI uri = new URI(
            schemeValue,
            usernameValue + ":" + passwordValue,
            hostValue,
            Integer.valueOf(portValue),
            destinationPathValue,
            null,
            null
          );

        when(manager.resolveFile(filePath)).thenReturn(local);
        when(manager.resolveFile(uri)).thenReturn(remote);

        doNothing().when(remote).copyFrom(local, Selectors.SELECT_SELF);

      }

      if (Objects.nonNull(exception)) {
        assertThrows(exception, () -> delegate.execute(execution));
      } else {

        delegate.execute(execution);

        // verify mock method calls were as expected

        // condition against parameters to not verify methods that are not called

        if (opValue == "GET") {
          verify(local, times(1)).copyFrom(remote, Selectors.SELECT_SELF);
        } else if (opValue == "PUT") {
          verify(remote, times(1)).copyFrom(local, Selectors.SELECT_SELF);
        }

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
   *         - originPath (path of source)
   *         - destinationPath (path of target)
   *         - op (GET, PUT)
   *         - scheme [ftp, ftps]
   *         - host
   *         - port
   *         - username
   *         - password
   * @throws JsonProcessingException
   */
  private static Stream<Arguments> executionStream() {
    // arguments required for delegate expression
    String inputVariables = "[]";

    String outputVariable = "{}";

    String originPath = "";

    String destinationPath = "";

    String getOp = "GET";
    String putOp = "PUT";

    String scheme = "ftp";
    String host = "localhost";
    String port = "22";
    String username = "test";
    String password = "test";

    // arguments for whether to expect exception thrown
    String noException = null;

    // arguments to assert about the test

    return Stream.of(
        Arguments.of(inputVariables, outputVariable, originPath, destinationPath, getOp, scheme, host, port, username, password, noException),
        Arguments.of(inputVariables, outputVariable, originPath, destinationPath, putOp, scheme, host, port, username, password, noException)
    );
  }

}
