package org.folio.rest.camunda.utility;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.stream.Stream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.graalvm.shadowed.org.json.JSONException;
import org.graalvm.shadowed.org.json.JSONObject;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ScriptEngineUtilityTest {

  static Stream<TestInput> validJsonStream() {
    return Stream.of(new TestInput[] {
        new TestInput("{}"),
        new TestInput("{\"key\":\"value\"}"),
        new TestInput("{\"outerKey\":{\"innerKey\":\"innerValue\"}}"),
        new TestInput("{\"string\":\"text\",\"number\":123,\"boolean\":true,\"nullValue\":null,\"object\":{\"nestedKey\":\"nestedValue\"},\"array\":[1,2,3]}"),
        new TestInput("{\"users\":[{\"id\":1,\"name\":\"Alice\",\"roles\":[\"admin\",\"user\"]},{\"id\":2,\"name\":\"Bob\",\"roles\":[\"user\"],\"profile\":{\"age\":30,\"interests\":[\"reading\",\"gaming\"]}}],\"settings\":{\"theme\":\"dark\",\"notifications\":{\"email\":true,\"sms\":false}}}"),
    });
  }

  @ParameterizedTest
  @MethodSource("validJsonStream")
  void testDecodeAndEncodeValidJson(TestInput input) throws JsonProcessingException {
    ScriptEngineUtility seu = new ScriptEngineUtility();

    JSONObject decoded = seu.decodeJson(input.json);

    assertNotNull(decoded);

    String encoded = seu.encodeJson(decoded);

    ObjectMapper om = new ObjectMapper();
    JsonNode expected = om.readValue(input.json, JsonNode.class);
    JsonNode actual = om.readValue(encoded, JsonNode.class);

    assertEquals(expected, actual);
  }

  static Stream<TestInput> invalidJsonStream() {
    return Stream.of(new TestInput[] {
        new TestInput("", "A JSONObject text must begin with '{' at 0 [character 1 line 1]"),
        new TestInput("{", "A JSONObject text must end with '}' at 1 [character 2 line 1]"),
        new TestInput("}", "A JSONObject text must begin with '{' at 1 [character 2 line 1]"),
        new TestInput("{\"\"}", "Expected a ':' after a key at 4 [character 5 line 1]"),
        new TestInput("{\"test\"}", "Expected a ':' after a key at 8 [character 9 line 1]"),
        new TestInput("[]", "A JSONObject text must begin with '{' at 1 [character 2 line 1]"),
        new TestInput("null", "A JSONObject text must begin with '{' at 1 [character 2 line 1]"),
        new TestInput("true", "A JSONObject text must begin with '{' at 1 [character 2 line 1]"),
        new TestInput("false", "A JSONObject text must begin with '{' at 1 [character 2 line 1]"),
        new TestInput("10", "A JSONObject text must begin with '{' at 1 [character 2 line 1]"),
    });
  }

  @ParameterizedTest
  @MethodSource("invalidJsonStream")
  void testDecodeInvalidJson(TestInput input) {
    ScriptEngineUtility seu = new ScriptEngineUtility();

    Exception thrown = assertThrows(
        JSONException.class,
        () -> seu.decodeJson(input.json),
        "decodeJson(json) did not throw exception as expected");

    assertEquals(input.message, thrown.getMessage());
  }

  static class TestInput {
    String json;
    String message;

    TestInput(String json) {
      this.json = json;
    }

    TestInput(String json, String message) {
      this.json = json;
      this.message = message;
    }
  }

}
