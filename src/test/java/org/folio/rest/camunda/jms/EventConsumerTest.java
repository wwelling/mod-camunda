package org.folio.rest.camunda.jms;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;

import java.util.HashMap;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.MessageCorrelationBuilder;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.folio.spring.messaging.model.Event;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ExtendWith(MockitoExtension.class)
class EventConsumerTest {

  @SpyBean
  private ObjectMapper objectMapper;

  @MockBean
  private RuntimeService runtimeService;

  @Mock
  private MessageCorrelationBuilder messageCorrelationBuilder;

  @Mock
  private ProcessInstance processInstance;

  @InjectMocks
  private EventConsumer eventConsumer;

  static Stream<Event> eventStream() {
    return Stream.of(new Event[] {
        new Event(
          "triggerId",
          "pathPattern",
          "method",
          "tenant",
          "path"
        ),
        new Event(
          "triggerId",
          "pathPattern",
          "method",
          "tenant",
          "path",
          new HashMap<String, String>()
        )
    });
  }

  @ParameterizedTest
  @MethodSource("eventStream")
  @SuppressWarnings("unchecked")
  void testReceive(Event event) throws JsonProcessingException {
    doReturn(processInstance).when(messageCorrelationBuilder).correlateStartMessage();
    doReturn(messageCorrelationBuilder).when(messageCorrelationBuilder).setVariables(anyMap());
    doReturn(messageCorrelationBuilder).when(messageCorrelationBuilder).tenantId(anyString());
    doReturn(messageCorrelationBuilder).when(runtimeService).createMessageCorrelation(anyString());

    doReturn(new HashMap<String, Object>()).when(objectMapper).convertValue(any(JsonNode.class), any(TypeReference.class));

    eventConsumer.receive(event);

    Mockito.verify(messageCorrelationBuilder).correlateStartMessage();
  }

}
