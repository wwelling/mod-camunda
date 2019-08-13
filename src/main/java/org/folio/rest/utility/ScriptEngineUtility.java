package org.folio.rest.utility;

import java.util.regex.Pattern;

import org.camunda.bpm.engine.impl.util.json.JSONObject;

/**
 * A collection of utilities methods intended to be directly used by scripts called by the scripting engine.
 */
public class ScriptEngineUtility {
  private static final String EMAIL_REGEX = "^\\w+([\\.-]?\\w+)*@\\w+([\\.-]?\\w+)*(\\.\\w{2,3})+$";
  private static final String PHONE_REGEX = "^[\\+]?[(]?[0-9]{3}[)]?[-\\s\\.]?[0-9]{3}[-\\s\\.]?[0-9]{4,6}$";
  private static final String URL_REGEX = "(http(s)?:\\\\\\/\\\\\\/.)?(www\\.)?[-a-zA-Z0-9@:%._\\\\\\+~#=]{2,256}\\\\\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_\\\\\\+.~#?&//=]*)";

  /**
   * Check if a given string might be an e-mail.
   *
   * @param string
   *   The string to check.
   *
   * @return
   *   TRUE if matched, FALSE otherwise.
   */
  public boolean isEmailLike(String string) {
    return string.matches(EMAIL_REGEX);
  }

  /**
   * Check if a given string is a phone number.
   *
   * @param string
   *   The string to check.
   *
   * @return
   *   TRUE if matched, FALSE otherwise.
   */
  public boolean isPhone(String string) {
    Pattern pattern = Pattern.compile(PHONE_REGEX, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
    return pattern.matcher(string).find();
  }

  /**
   * Check if a given string might be a URL.
   *
   * @param string
   *   The string to check.
   *
   * @return
   *   TRUE if matched, FALSE otherwise.
   */
  public boolean isURLLike(String string) {
    boolean isLikeUrl = string.toLowerCase().indexOf("www.") != -1;

    isLikeUrl = isLikeUrl && string.toLowerCase().indexOf(".org") != -1;
    isLikeUrl = isLikeUrl && string.toLowerCase().indexOf(".edu") != -1;
    isLikeUrl = isLikeUrl && string.toLowerCase().indexOf(".net") != -1;
    isLikeUrl = isLikeUrl && string.toLowerCase().indexOf(".us") != -1;
    isLikeUrl = isLikeUrl && string.toLowerCase().indexOf(".io") != -1;
    isLikeUrl = isLikeUrl && string.toLowerCase().indexOf(".co") != -1;

    return isLikeUrl;
  }

  /**
   * Check if a given string is a valid URL.
   *
   * @param string
   *   The string to check.
   *
   * @return
   *   TRUE if matched, FALSE otherwise.
   */
  public boolean isValidUrl(String string) {
    return string.matches(URL_REGEX);
  }

  /**
   * Creating JSONObject.
   *
   * @return
   *   A generated JSON object.
   */
  public JSONObject createJson() {
    return new JSONObject();
  }

  /**
   * Decode a JSON string into a JSONObject.
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