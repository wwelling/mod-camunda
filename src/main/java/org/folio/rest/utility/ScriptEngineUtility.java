package org.folio.rest.utility;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.camunda.bpm.engine.impl.util.json.JSONObject;
import org.json.JSONException;
import org.json.XML;
import org.marc4j.MarcReader;
import org.marc4j.MarcStreamReader;
import org.marc4j.MarcWriter;
import org.marc4j.MarcXmlWriter;
import org.marc4j.marc.Record;

/**
 * A collection of utilities methods intended to be directly used by scripts
 * called by the scripting engine.
 */
public class ScriptEngineUtility {

  private static final String EMAIL_REGEX = "^\\w+([\\.-]?\\w+)*@\\w+([\\.-]?\\w+)*(\\.\\w{2,3})+$";
  private static final String PHONE_REGEX = "^[\\+]?[(]?[0-9]{3}[)]?[-\\s\\.]?[0-9]{3}[-\\s\\.]?[0-9]{4,6}$";
  private static final String URL_REGEX = "(http(s)?:\\\\\\/\\\\\\/.)?(www\\.)?[-a-zA-Z0-9@:%._\\\\\\+~#=]{2,256}\\\\\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_\\\\\\+.~#?&//=]*)";

  private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX,
      Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
  private static final Pattern PHONE_PATTERN = Pattern.compile(PHONE_REGEX,
      Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
  private static final Pattern URL_PATTERN = Pattern.compile(URL_REGEX, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
  
  /**
   * Check if a given string is a proper e-mail address.
   *
   * @param string The string to check.
   *
   * @return TRUE if matched, FALSE otherwise.
   */
  public boolean isEmail(String string) {
    return EMAIL_PATTERN.matcher(string).find();
  }

  /**
   * Check if a given string might be an e-mail.
   *
   * @param string The string to check.
   *
   * @return TRUE if matched, FALSE otherwise.
   */
  public boolean isEmailLike(String string) {
    return isEmail(string) || (string == null ? false : string.toLowerCase().indexOf("@") != -1);
  }

  /**
   * Check if a given string is a phone number.
   *
   * @param string The string to check.
   *
   * @return TRUE if matched, FALSE otherwise.
   */
  public boolean isPhone(String string) {
    return PHONE_PATTERN.matcher(string).find();
  }

  /**
   * Check if a given string might be a URL.
   *
   * @param string The string to check.
   *
   * @return TRUE if matched, FALSE otherwise.
   */
  public boolean isURLLike(String string) {
    if (string == null) {
      return false;
    }

    boolean isLikeUrl = string.toLowerCase().indexOf("www.") != -1;

    isLikeUrl = isLikeUrl || string.toLowerCase().indexOf(".org") != -1;
    isLikeUrl = isLikeUrl || string.toLowerCase().indexOf(".edu") != -1;
    isLikeUrl = isLikeUrl || string.toLowerCase().indexOf(".net") != -1;
    isLikeUrl = isLikeUrl || string.toLowerCase().indexOf(".us") != -1;
    isLikeUrl = isLikeUrl || string.toLowerCase().indexOf(".io") != -1;
    isLikeUrl = isLikeUrl || string.toLowerCase().indexOf(".co") != -1;

    return (isValidUrl(string) || isLikeUrl) && !isEmailLike(string);
  }

  /**
   * Check if a given string is a valid URL.
   *
   * @param string The string to check.
   *
   * @return TRUE if matched, FALSE otherwise.
   */
  public boolean isValidUrl(String string) {
    if (string == null) {
      return false;
    }

    return URL_PATTERN.matcher(string).find();
  }

  /**
   * Creating JSONObject.
   *
   * @return A generated JSON object.
   */
  public JSONObject createJson() {
    return new JSONObject();
  }

  /**
   * Decode a JSON string into a JSONObject.
   *
   * @param json The JSON string to decode.
   *
   * @return A generated JSON object, containing the decoded JSON string.
   */
  public JSONObject decodeJson(String json) {
    return new JSONObject(json);
  }

  /**
   * Encode a JSONObject into a JSON string.
   *
   * @param json The JSONObject to encode.
   *
   * @return A String containing the encoded JSON data.
   */
  public String encodeJson(JSONObject json) {
    if (json == null) {
      return "{}";
    }

    return json.toString(2);
  }

  /**
   * Convert a String representing marc binary into a JSON string.
   *
   * @param rawMarc The marcBinary String to convert into json.
   *
   * @return A String containing the encoded JSON marc data.
   */
  public String rawMarcToJsonString(String rawMarc) {
    InputStream rawMarcIS = new ByteArrayInputStream(rawMarc.getBytes(StandardCharsets.UTF_8));
    ByteArrayOutputStream rawMarcOS = new ByteArrayOutputStream();
    MarcReader reader = new MarcStreamReader(rawMarcIS);
    MarcWriter writer = new MarcXmlWriter(rawMarcOS, true);
    while (reader.hasNext()) {
      Record record = reader.next();
      writer.write(record);
    }
    writer.close();
    org.json.JSONObject marcJson = XML.toJSONObject(rawMarcOS.toString());
    org.json.JSONObject marcJsonCollection = null;
    try {
      marcJsonCollection = marcJson.getJSONObject("marc:collection");
    } catch(JSONException e) {
      e.printStackTrace();
    }
    org.json.JSONObject marcJsonRecord = null;
    if(marcJsonCollection != null) {
      try {
        marcJsonRecord = marcJsonCollection.getJSONObject("marc:record");
      } catch(JSONException e) {
        e.printStackTrace();
      }
    }
    return marcJsonRecord != null ? marcJsonRecord.toString() : "{}";
  }

}