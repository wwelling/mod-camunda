package org.folio.rest.camunda.delegate;

import static org.folio.spring.test.mock.MockMvcConstant.JSON_OBJECT;
import static org.folio.spring.test.mock.MockMvcConstant.KEY;
import static org.folio.spring.test.mock.MockMvcConstant.PATH;
import static org.folio.spring.test.mock.MockMvcConstant.VALUE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.getField;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.model.bpmn.instance.FlowElement;
import org.folio.rest.camunda.service.DatabaseConnectionService;
import org.folio.rest.workflow.model.DatabaseQueryTask;
import org.folio.rest.workflow.model.EmbeddedVariable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DatabaseQueryDelegateTest {

  private static final String QUERY = "query";

  @Mock
  private DatabaseConnectionService connectionService;

  @Mock
  private Connection connection;

  @Mock
  private Statement statement;

  @Mock
  private FlowElement flowElementBpmn;

  @Mock
  private DelegateExecution delegateExecution;

  private Expression designationExpression;

  private Expression includeHeaderExpression;

  private Expression inputVariablesExpression;

  private Expression outputPathExpression;

  private Expression outputVariableExpression;

  private Expression queryExpression;

  private Expression resultTypeExpression;

  private ResultSet resultSet;

  private ResultSetMetaData resultSetMetaData;

  private ObjectMapper objectMapper;

  @InjectMocks
  private DatabaseQueryDelegate databaseQueryDelegate;

  @BeforeEach
  void beforeEach() {
    designationExpression = mock(Expression.class);
    includeHeaderExpression = mock(Expression.class);
    inputVariablesExpression = mock(Expression.class);
    outputPathExpression = mock(Expression.class);
    outputVariableExpression = mock(Expression.class);
    queryExpression = mock(Expression.class);
    resultTypeExpression = mock(Expression.class);
    resultSet = mock(ResultSet.class);
    resultSetMetaData = mock(ResultSetMetaData.class);

    objectMapper = new ObjectMapper();
  }

  @Test
  void testExecuteWorksWithPositiveUpdateCount() throws Exception {
    setupExecuteMocking();

    when(connectionService.getConnection(anyString())).thenReturn(connection);
    when(connection.createStatement()).thenReturn(statement);
    when(statement.execute(anyString())).thenReturn(true);
    when(statement.getUpdateCount()).thenReturn(1);

    databaseQueryDelegate.execute(delegateExecution);

    verify(statement).getUpdateCount();
  }

  @Test
  void testExecuteWorksWithNegativeUpdateCountNullOutputPath() throws Exception {
    setupExecuteMocking();

    when(connectionService.getConnection(anyString())).thenReturn(connection);
    when(connection.createStatement()).thenReturn(statement);
    when(statement.execute(anyString())).thenReturn(true);
    when(statement.getUpdateCount()).thenReturn(-1);
    when(statement.getResultSet()).thenReturn(resultSet);
    when(resultSet.getMetaData()).thenReturn(resultSetMetaData);
    when(outputVariableExpression.getValue(any())).thenReturn(null);

    setField(databaseQueryDelegate, "outputPath", null);
    setField(databaseQueryDelegate, "outputVariable", outputVariableExpression);

    databaseQueryDelegate.execute(delegateExecution);

    verify(statement).getUpdateCount();

    when(includeHeaderExpression.getValue(any())).thenReturn(null);

    databaseQueryDelegate.execute(delegateExecution);

    verify(statement, times(2)).getUpdateCount();

    setField(databaseQueryDelegate, "includeHeader", null);

    databaseQueryDelegate.execute(delegateExecution);

    verify(statement, times(3)).getUpdateCount();
  }

  @Test
  void testExecuteWorksWithNegativeUpdateCountWithOutputPath() throws Exception {
    setupExecuteMocking();

    when(connectionService.getConnection(anyString())).thenReturn(connection);
    when(connection.createStatement()).thenReturn(statement);
    when(statement.execute(anyString())).thenReturn(true);
    when(statement.getUpdateCount()).thenReturn(-1);
    when(statement.getResultSet()).thenReturn(resultSet);
    when(outputVariableExpression.getValue(any())).thenReturn(null);
    when(outputPathExpression.getValue(any())).thenReturn(PATH);

    // Return a mocked enum that is private to the class using reflection.
    Class<?>[] classes = databaseQueryDelegate.getClass().getDeclaredClasses();
    for (int i = 0; i < classes.length; i++) {
      if ("DatabaseResultTypeOp".equals(classes[i].getSimpleName())) {
        Object[] enums = classes[i].getEnumConstants();
        if (enums.length > 0) {
          when(resultTypeExpression.getValue(any())).thenReturn(enums[0]);
          break;
        }
      }
    }

    setField(databaseQueryDelegate, "outputPath", outputPathExpression);
    setField(databaseQueryDelegate, "outputVariable", outputVariableExpression);

    databaseQueryDelegate.execute(delegateExecution);

    verify(statement).getUpdateCount();
  }

  @Test
  void testExecuteThrowsException() throws JsonProcessingException, SQLException {
    setupExecuteMocking();

    doThrow(SQLException.class).when(connectionService).getConnection(anyString());

    assertThrows(Exception.class, () -> {
      databaseQueryDelegate.execute(delegateExecution);
    });
  }

  @Test
  void testSetIncludeHeaderWorks() {
    setField(databaseQueryDelegate, "includeHeader", null);

    databaseQueryDelegate.setIncludeHeader(includeHeaderExpression);
    assertEquals(includeHeaderExpression, getField(databaseQueryDelegate, "includeHeader"));
  }

  @Test
  void testSetOutputPathWorks() {
    setField(databaseQueryDelegate, "outputPath", null);

    databaseQueryDelegate.setOutputPath(outputPathExpression);
    assertEquals(outputPathExpression, getField(databaseQueryDelegate, "outputPath"));
  }

  @Test
  void testSetQueryWorks() {
    setField(databaseQueryDelegate, "query", null);

    databaseQueryDelegate.setQuery(queryExpression);
    assertEquals(queryExpression, getField(databaseQueryDelegate, "query"));
  }

  @Test
  void testSetResultTypeWorks() {
    setField(databaseQueryDelegate, "resultType", null);

    databaseQueryDelegate.setResultType(resultTypeExpression);
    assertEquals(resultTypeExpression, getField(databaseQueryDelegate, "resultType"));
  }

  @Test
  void testFromTaskWorks() {
    assertEquals(DatabaseQueryTask.class, databaseQueryDelegate.fromTask());
  }

  /**
   * Provide common mocking behavior for the execute() method.
   *
   * @throws JsonProcessingException On JSON processing error.
   */
  private void setupExecuteMocking() throws JsonProcessingException {
    final Set<EmbeddedVariable> inputs = new HashSet<>(List.of(new EmbeddedVariable()));

    when(delegateExecution.getBpmnModelElementInstance()).thenReturn(flowElementBpmn);
    when(flowElementBpmn.getName()).thenReturn(KEY);
    when(designationExpression.getValue(any())).thenReturn(VALUE);
    when(queryExpression.getValue(any())).thenReturn(QUERY);
    when(includeHeaderExpression.getValue(any())).thenReturn(JSON_OBJECT);
    when(inputVariablesExpression.getValue(any())).thenReturn(objectMapper.writeValueAsString(inputs));

    setField(databaseQueryDelegate, "designation", designationExpression);
    setField(databaseQueryDelegate, "connectionService", connectionService);
    setField(databaseQueryDelegate, "includeHeader", includeHeaderExpression);
    setField(databaseQueryDelegate, "outputPath", outputPathExpression);
    setField(databaseQueryDelegate, "query", queryExpression);
    setField(databaseQueryDelegate, "resultType", resultTypeExpression);
    setField(databaseQueryDelegate, "inputVariables", inputVariablesExpression);
    setField(databaseQueryDelegate, "objectMapper", objectMapper);
  }
}
