package org.folio.rest.camunda.utility;

import org.graalvm.shadowed.org.json.JSONObject;

/**
 * Provide utility functions specifically needed for scripting engines.
 */
public class ScriptEngineUtility {

    /**
     * Decode a JSON string into a JSONObject.
     *
     * This is required by several of the scripting engines, such as engine.py.
     *
     * @param json
     *   The JSON string to decode.
     *
     * @return
     *   A generated JSON object, containing the decoded JSON string.
     */
    public JSONObject decodeJson(String json) {
      return new JSONObject(json);
    }

    /**
     * Encode a JSONObject into a JSON string.
     *
     * @param json
     *   The JSONObject to encode.
     *
     * @return
     *   A String containing the encoded JSON data.
     */
    public String encodeJson(JSONObject json) {
      return json.toString(2);
    }
}
