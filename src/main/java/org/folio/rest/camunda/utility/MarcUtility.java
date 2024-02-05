package org.folio.rest.camunda.utility;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.marc4j.MarcException;
import org.marc4j.MarcJsonReader;
import org.marc4j.MarcJsonWriter;
import org.marc4j.MarcStreamReader;
import org.marc4j.MarcStreamWriter;
import org.marc4j.MarcWriter;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.VariableField;
import org.marc4j.marc.impl.SubfieldImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class MarcUtility {

  private static final Logger logger = LoggerFactory.getLogger(MarcUtility.class);

  private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

  private static final ObjectMapper mapper = new ObjectMapper();

  private MarcUtility() {

  }

  static {
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

  public static List<String> splitRawMarcToMarcJsonRecords(String rawMarc)
      throws MarcException, IOException {
    List<String> records = new ArrayList<>();
    try (InputStream in = new ByteArrayInputStream(rawMarc.getBytes(DEFAULT_CHARSET))) {
      final MarcStreamReader reader = new MarcStreamReader(in, DEFAULT_CHARSET.name());
      while (reader.hasNext()) {
        records.add(recordToMarcJson(reader.next()));
      }
    }

    return records;
  }

  public static String addFieldToMarcJson(String marcJson, String fieldJson)
      throws MarcException, IOException {
    JsonNode fieldNode = mapper.readTree(fieldJson);
    MarcFactory factory = MarcFactory.newInstance();
    Record record = marcJsonToRecord(marcJson);

    String tag = fieldNode.get("tag").asText();

    DataField field = factory.newDataField();
    field.setTag(tag);

    if (fieldNode.has("indicator1")) {
      char indicator1 = fieldNode.get("indicator1").asText().charAt(0);
      field.setIndicator1(indicator1);
    }

    if (fieldNode.has("indicator2")) {
      char indicator2 = fieldNode.get("indicator2").asText().charAt(0);
      field.setIndicator2(indicator2);
    }

    ArrayNode subfields = (ArrayNode) fieldNode.get("subfields");

    subfields.forEach(subfieldNode -> {
      char code = subfieldNode.get("code").asText().charAt(0);
      String data = subfieldNode.get("data").asText();
      Subfield subfield = factory.newSubfield();
      subfield.setCode(code);
      subfield.setData(data);
      field.addSubfield(subfield);
    });

    record.addVariableField(field);

    recalculateLeader(record);

    return recordToMarcJson(record);
  }

  public static String updateControlNumberField(String marcJson, String data)
      throws MarcException, IOException {
    Record record = marcJsonToRecord(marcJson);
    if (Objects.nonNull(record.getControlNumberField())) {
      record.getControlNumberField().setData(data);
    }
    else {
      ControlField controlField = MarcFactory.newInstance().newControlField("001");
      controlField.setData(data);
      record.addVariableField(controlField);
    }
    recalculateLeader(record);

    return recordToMarcJson(record);
  }

  public static String marcJsonToRawMarc(String marcJson)
      throws MarcException, IOException {
    return recordToRawMarc(marcJsonToRecord(marcJson));
  }

  public static String rawMarcToMarcJson(String rawMarc)
      throws MarcException, IOException {
    return recordToMarcJson(rawMarcToRecord(rawMarc));
  }

  public static String getFieldsFromRawMarc(String rawMarc, String[] tags)
      throws JsonProcessingException, MarcException, IOException {
    return getRecordFields(rawMarcToRecord(rawMarc), tags);
  }

  public static String getFieldsFromMarcJson(String marcJson, String[] tags)
      throws JsonProcessingException, MarcException, IOException {
    return getRecordFields(marcJsonToRecord(marcJson), tags);
  }

  private static Record marcJsonToRecord(String marcJson)
      throws MarcException, IOException {
    logger.info("Attempting to read MARC JSON to Record: {}", marcJson);
    try (InputStream in = new ByteArrayInputStream(marcJson.getBytes())) {
      final MarcJsonReader reader = new MarcJsonReader(in);
      if (reader.hasNext()) {
        return reader.next();
      }
    }

    throw new MarcException("No record found");
  }

  private static Record rawMarcToRecord(String rawMarc)
      throws MarcException, IOException {
    logger.info("Attempting to read raw MARC to Record: {}", rawMarc);
    try (InputStream in = new ByteArrayInputStream(rawMarc.getBytes(DEFAULT_CHARSET))) {
      final MarcStreamReader reader = new MarcStreamReader(in, DEFAULT_CHARSET.name());
      if (reader.hasNext()) {
        return reader.next();
      }
    }

    throw new MarcException("No record found");
  }

  private static String recordToMarcJson(Record record) throws IOException {
    try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      final MarcJsonWriter writer = new MarcJsonWriter(out);
      writer.write(record);
      writer.close();
      return out.toString();
    }
  }

  private static String recordToRawMarc(Record record) throws IOException {
    try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      final MarcStreamWriter writer = new MarcStreamWriter(out);
      writer.write(record);
      writer.close();
      return out.toString();
    }
  }

  private static String getRecordFields(Record record, String[] tags) throws JsonProcessingException {
    List<VariableField> fields = record.getVariableFields(tags);
    return mapper.writerWithDefaultPrettyPrinter()
      .writeValueAsString(fields);
  }

  private static void recalculateLeader(Record record) throws IOException {
    try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      MarcWriter streamWriter = new MarcStreamWriter(out, DEFAULT_CHARSET.name());
      // use stream writer to recalculate leader
      streamWriter.write(record);
      streamWriter.close();
    }
  }

}
