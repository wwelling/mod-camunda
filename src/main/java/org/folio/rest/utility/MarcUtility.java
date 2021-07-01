package org.folio.rest.utility;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ArrayNode;

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

public class MarcUtility {

  private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

  private static final ObjectMapper mapper = new ObjectMapper();

  public MarcUtility() {
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

  public List<String> splitRawMarcToMarcJsonRecords(String rawMarc) {
    List<Record> records = new ArrayList<>();
    try (InputStream in = new ByteArrayInputStream(rawMarc.getBytes(DEFAULT_CHARSET))) {
      final MarcStreamReader reader = new MarcStreamReader(in, DEFAULT_CHARSET.name());
      while (reader.hasNext()) {
        records.add(reader.next());
      }
    } catch (IOException e) {
      // TODO: do something in case of exception
      System.out.println(e.getMessage());
    } catch (final MarcException e) {
      // TODO: do something in case of exception
      System.out.println(e.getMessage());
    }

    return records.stream()
      .map(this::recordToMarcJson)
      .collect(Collectors.toList());
  }

  public String addFieldToMarcJson(String marcJson, String fieldJson) throws JsonMappingException, JsonProcessingException {
    JsonNode fieldNode = mapper.readTree(fieldJson);
    MarcFactory factory = MarcFactory.newInstance();
    Optional<Record> record = marcJsonToRecord(marcJson);
    if (record.isPresent()) {
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

      record.get().addVariableField(field);

      recalculateLeader(record.get());

      return recordToMarcJson(record.get());
    }

    return marcJson;
  }

  public String updateControlNumberField(String marcJson, String data) {
    Optional<Record> record = marcJsonToRecord(marcJson);
    if (record.isPresent()) {
      if (Objects.nonNull(record.get().getControlNumberField())) {
        record.get().getControlNumberField().setData(data);
      }
      else {
        ControlField controlField = MarcFactory.newInstance().newControlField("001");
        controlField.setData(data);
        record.get().addVariableField(controlField);
      }
      recalculateLeader(record.get());

      return recordToMarcJson(record.get());
    }

    return marcJson;
  }

  public String marcJsonToRawMarc(String marcJson) {
    Optional<Record> record = marcJsonToRecord(marcJson);
    if (record.isPresent()) {
      return recordToRawMarc(record.get());
    }
    return "";
  }

  public String rawMarcToMarcJson(String rawMarc) {
    Optional<Record> record = rawMarcToRecord(rawMarc);
    if (record.isPresent()) {
      return recordToMarcJson(record.get());
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

  private String recordToMarcJson(Record record) {
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

  private String recordToRawMarc(Record record) {
    try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      final MarcStreamWriter writer = new MarcStreamWriter(out);
      writer.write(record);
      writer.close();
      return out.toString();
    } catch (IOException e) {
      // TODO: do something in case of exception
      System.out.println(e.getMessage());
    }
    return "";
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

  private void recalculateLeader(Record record) {
    try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
      MarcWriter streamWriter = new MarcStreamWriter(os, DEFAULT_CHARSET.name());
      // use stream writer to recalculate leader
      streamWriter.write(record);
      streamWriter.close();
    } catch (IOException e) {
      // TODO: do something in case of exception
      System.out.println(e.getMessage());
    }
  }

}
