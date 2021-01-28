package org.folio.rest.utility;

import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static java.util.regex.Pattern.MULTILINE;
import static java.util.regex.Pattern.compile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import org.marc4j.MarcException;
import org.marc4j.MarcJsonReader;
import org.marc4j.MarcJsonWriter;
import org.marc4j.MarcStreamReader;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.VariableField;
import org.marc4j.marc.impl.SubfieldImpl;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * A collection of utilities methods intended to be directly used by scripts
 * called by the scripting engine.
 */
public class ScriptEngineUtility {

  private static final String EMAIL_REGEX = "^\\w+([\\.-]?\\w+)*@\\w+([\\.-]?\\w+)*(\\.\\w{2,3})+$";
  private static final String PHONE_REGEX = "^[\\+]?[(]?[0-9]{3}[)]?[-\\s\\.]?[0-9]{3}[-\\s\\.]?[0-9]{4,6}$";
  private static final String URL_REGEX = "(http(s)?:\\\\\\/\\\\\\/.)?(www\\.)?[-a-zA-Z0-9@:%._\\\\\\+~#=]{2,256}\\\\\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_\\\\\\+.~#?&//=]*)";

  private static final Pattern EMAIL_PATTERN = compile(EMAIL_REGEX, CASE_INSENSITIVE | MULTILINE);
  private static final Pattern PHONE_PATTERN = compile(PHONE_REGEX, CASE_INSENSITIVE | MULTILINE);
  private static final Pattern URL_PATTERN = compile(URL_REGEX, CASE_INSENSITIVE | MULTILINE);

  private static final ObjectMapper mapper = new ObjectMapper();

  private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

  public ScriptEngineUtility() {
    mapper.setSerializationInclusion(Include.NON_NULL);
    SimpleModule module = new SimpleModule();
    module.addDeserializer(Subfield.class, new JsonDeserializer<Subfield>() {
      @Override
      public Subfield deserialize(JsonParser jp, DeserializationContext ctxt)
          throws IOException, JsonProcessingException {
        ObjectCodec oc = jp.getCodec();
        JsonNode node = oc.readTree(jp);
        return mapper.treeToValue(node, SubfieldImpl.class);
      }
    });
    mapper.registerModule(module);
  }

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
   * Convert a String representing marc binary into a JSON string.
   *
   * @param rawMarc The marcBinary String to convert into json.
   *
   * @return A String containing the encoded JSON marc data.
   * @throws IOException
   */
  public String rawMarcToJson(String rawMarc) {
    Optional<Record> record = rawMarcToRecord(rawMarc);
    if (record.isPresent()) {
      return recordToJson(record.get());
    }
    return "{}";
  }

  public String getFieldsFromRawMarc(String rawMarc, String[] tags) {
    Optional<Record> record = rawMarcToRecord(rawMarc);
    if (record.isPresent()) {
      return getRecordFields(record.get(), tags);
    }
    return "[]";
  }

  public String getFieldsFromMarcJson(String marcJson, String[] tags) {
    Optional<Record> record = marcJsonToRecord(marcJson);
    if (record.isPresent()) {
      return getRecordFields(record.get(), tags);
    }
    return "[]";
  }

  private Optional<Record> marcJsonToRecord(String marcJson) {
    try (InputStream in = new ByteArrayInputStream(marcJson.getBytes())) {
      final MarcJsonReader reader = new MarcJsonReader(in);
      if (reader.hasNext()) {
        return Optional.of(reader.next());
      }
    } catch (IOException e) {
      // TODO: do something in case of exception
      System.out.println(e.getMessage());
    } catch (final MarcException e) {
      // TODO: do something in case of exception
      System.out.println(e.getMessage());
    }
    return Optional.empty();
  }

  private Optional<Record> rawMarcToRecord(String rawMarc) {
    try (InputStream in = new ByteArrayInputStream(rawMarc.getBytes(DEFAULT_CHARSET))) {
      final MarcStreamReader reader = new MarcStreamReader(in, DEFAULT_CHARSET.name());
      if (reader.hasNext()) {
        return Optional.of(reader.next());
      }
    } catch (IOException e) {
      // TODO: do something in case of exception
      System.out.println(e.getMessage());
    } catch (final MarcException e) {
      // TODO: do something in case of exception
      System.out.println(e.getMessage());
    }
    return Optional.empty();
  }

  private String recordToJson(Record record) {
    try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      final MarcJsonWriter writer = new MarcJsonWriter(out);
      writer.write(record);
      writer.close();
      return out.toString();
    } catch (IOException e) {
      // TODO: do something in case of exception
      System.out.println(e.getMessage());
    }
    return "{}";
  }

  private String getRecordFields(Record record, String[] tags) {
    List<VariableField> fields = record.getVariableFields(tags);
    try {
      return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(fields);
    } catch (JsonProcessingException e) {
      // TODO: do something in case of exception
      System.out.println(e.getMessage());
    }
    return "[]";
  }

}
