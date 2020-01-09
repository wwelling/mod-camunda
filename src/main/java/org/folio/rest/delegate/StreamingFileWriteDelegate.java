package org.folio.rest.delegate;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.folio.rest.service.StreamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@Scope("prototype")
public class StreamingFileWriteDelegate extends AbstractReportableDelegate {

  @Value("${tenant.default-tenant}")
  private String DEFAULT_TENANT;

  @Autowired
  private StreamService streamService;

  @Autowired
  private ObjectMapper objectMapper;

  private Expression path;

  private Expression workflow;

  private Expression filenameTemplate;

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    super.execute(execution);
    String delegateName = execution.getBpmnModelElementInstance().getName();

    String tenant = execution.getTenantId() != null ? execution.getTenantId() : DEFAULT_TENANT;

    String path = this.path.getValue(execution).toString();

    String workflow = this.workflow.getValue(execution).toString();

    String filenameTemplate = this.filenameTemplate.getValue(execution).toString();

    File workflowDirectory = getOrCreateDirectory(path, tenant, workflow);

    String workflowDataPath = workflowDirectory.getAbsolutePath();

    String primaryStreamId = (String) execution.getVariable("primaryStreamId");

    updateReport(primaryStreamId, String.format("%s started at %s", delegateName, Instant.now()));

    streamService.map(primaryStreamId, d -> {
      byte[] content = d.getBytes(StandardCharsets.UTF_8);
      try {
        TypeReference<HashMap<String, String>> typeRef = new TypeReference<HashMap<String, String>>() { };
        Map<String, String> valuesMap = objectMapper.readValue(content, typeRef);
        String filename = getFilename(valuesMap, filenameTemplate);
        String filePath = String.join(File.separator, workflowDataPath, filename);
        Files.write(Paths.get(filePath), content);
        updateReport(primaryStreamId, String.format("Created file: %s", filePath));
      } catch (IOException e) {
        log.error(e.getMessage());
        updateReport(primaryStreamId, e.getMessage());
      }
      return d;
    });
  }

  public void setPath(Expression path) {
    this.path = path;
  }

  public void setWorkflow(Expression workflow) {
    this.workflow = workflow;
  }

  public void setFilenameTemplate(Expression filenameTemplate) {
    this.filenameTemplate = filenameTemplate;
  }

  private File getOrCreateDirectory(String... path) {
    List<String> ap = new ArrayList<String>();
    File directory = null;
    for (String p : path) {
      ap.add(p);
      directory = new File(String.join(File.separator, ap.toArray(new String[ap.size()])));
      if (!directory.exists()) {
        directory.mkdir();
      }
    }
    return directory;
  }

  private String getFilename(Map<String, String> valuesMap, String template) {
    StringSubstitutor sub = new StringSubstitutor(valuesMap);
    return StringUtils.leftPad(sub.replace(template), 16, "0");
  }

}