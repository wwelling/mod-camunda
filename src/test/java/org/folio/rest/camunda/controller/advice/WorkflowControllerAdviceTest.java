package org.folio.rest.camunda.controller.advice;

import static org.folio.spring.test.mock.MockMvcConstant.APP_JSON;
import static org.folio.spring.test.mock.MockMvcConstant.JSON_OBJECT;
import static org.folio.spring.test.mock.MockMvcConstant.OKAPI_HEAD;
import static org.folio.spring.test.mock.MockMvcRequest.appendBody;
import static org.folio.spring.test.mock.MockMvcRequest.appendHeaders;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.log;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.folio.rest.camunda.controller.WorkflowController;
import org.folio.rest.camunda.exception.WorkflowAlreadyActiveException;
import org.folio.rest.camunda.exception.WorkflowAlreadyDeactivatedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
class WorkflowControllerAdviceTest {

  private static final String PATH = "/workflow-engine/workflows";

  private static final String PATH_ACTIVATE = PATH + "/activate";

  private static final String PATH_DEACTIVATE = PATH + "/deactivate";

  @Autowired
  private WorkflowControllerAdvice workflowControllerAdvice;

  @Autowired
  @Mock
  private WorkflowController workflowController;

  private MockMvc mvc;

  @BeforeEach
  void beforeEach() {
    mvc = MockMvcBuilders.standaloneSetup(workflowController)
      .setControllerAdvice(workflowControllerAdvice)
      .build();
  }

  @ParameterizedTest
  @MethodSource("provideExceptionsToMatchForActivateWorkflow")
  void exceptionsThrownForActivateWorkflowTest(Exception exception, String simpleName, int status) throws Exception {
    when(workflowController.activateWorkflow(any(), any())).thenThrow(exception);

    MockHttpServletRequestBuilder request = appendHeaders(post(PATH_ACTIVATE), OKAPI_HEAD, APP_JSON, APP_JSON);

    MvcResult result = mvc.perform(appendBody(request, JSON_OBJECT))
      .andDo(log()).andExpect(status().is(status)).andReturn();

    Pattern pattern = Pattern.compile("\"type\":\"" + simpleName + "\"");
    Matcher matcher = pattern.matcher(result.getResponse().getContentAsString());
    assertTrue(matcher.find());
  }

  @ParameterizedTest
  @MethodSource("provideExceptionsToMatchForDeactivateWorkflow")
  void exceptionsThrownForDectivateWorkflowTest(Exception exception, String simpleName, int status) throws Exception {
    when(workflowController.deactivateWorkflow(any())).thenThrow(exception);

    MockHttpServletRequestBuilder request = appendHeaders(post(PATH_DEACTIVATE), OKAPI_HEAD, APP_JSON, APP_JSON);

    MvcResult result = mvc.perform(appendBody(request, JSON_OBJECT))
      .andDo(log()).andExpect(status().is(status)).andReturn();

    Pattern pattern = Pattern.compile("\"type\":\"" + simpleName + "\"");
    Matcher matcher = pattern.matcher(result.getResponse().getContentAsString());
    assertTrue(matcher.find());
  }

  /**
   * Helper function for parameterized test providing the exceptions to be matched for activate workflow.
   *
   * @return
   *   The arguments array stream with the stream columns as:
   *     - Exception exception.
   *     - String simpleName (exception name to match).
   *     - int status (response HTTP status code for the exception).
   */
  private static Stream<Arguments> provideExceptionsToMatchForActivateWorkflow() {
    return Stream.of(
      Arguments.of(new WorkflowAlreadyActiveException(null), WorkflowAlreadyActiveException.class.getSimpleName(), 400)
    );
  }

  /**
   * Helper function for parameterized test providing the exceptions to be matched for deactivate workflow.
   *
   * @return
   *   The arguments array stream with the stream columns as:
   *     - Exception exception.
   *     - String simpleName (exception name to match).
   *     - int status (response HTTP status code for the exception).
   */
  private static Stream<Arguments> provideExceptionsToMatchForDeactivateWorkflow() {
    return Stream.of(
      Arguments.of(new WorkflowAlreadyDeactivatedException(null), WorkflowAlreadyDeactivatedException.class.getSimpleName(), 400)
    );
  }
}
