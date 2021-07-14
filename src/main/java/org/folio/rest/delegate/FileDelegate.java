package org.folio.rest.delegate;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.model.bpmn.instance.FlowElement;
import org.folio.rest.workflow.model.FileOp;
import org.folio.rest.workflow.model.FileTask;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;

@Service
@Scope("prototype")
public class FileDelegate extends AbstractWorkflowIODelegate {

  private Expression path;

  private Expression op;

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    long startTime = System.nanoTime();
    FlowElement bpmnModelElement = execution.getBpmnModelElementInstance();
    String delegateName = bpmnModelElement.getName();

    logger.info("{} started", delegateName);

    String pathTemplate = this.path.getValue(execution).toString();

    StringTemplateLoader pathLoader = new StringTemplateLoader();
    pathLoader.putTemplate("path", pathTemplate);

    Configuration cfg = new Configuration(Configuration.VERSION_2_3_23);
    cfg.setTemplateLoader(pathLoader);

    Map<String, Object> inputs = getInputs(execution);
    String path = FreeMarkerTemplateUtils.processTemplateIntoString(cfg.getTemplate("path"), inputs);

    FileOp op = FileOp.valueOf(this.op.getValue(execution).toString());

    File file = new File(path);

    switch (op) {
      case DELETE:
        if (file.exists()) {
          boolean deleted = file.delete();
          if (deleted) {
            logger.info("{} has been deleted", path);
          }
        } else {
          logger.info("{} does not exist", path);
        }
        break;
      case READ:
        if (file.exists()) {
          String content = new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
          setOutput(execution, content);
          logger.info("{} read", path);
        } else {
          logger.info("{} does not exist", path);
        }
        break;
      case WRITE:
        StringBuilder content = new StringBuilder();
        for (Object value : inputs.values()) {
          if (value instanceof String) {
            content.append(value);
          } else {
            content.append(objectMapper.writeValueAsString(value));
          }
          content.append("\n");
        }
        FileUtils.writeStringToFile(file, content.toString(), StandardCharsets.UTF_8);
        logger.info("{} written", path);
        break;
    }

    long endTime = System.nanoTime();
    logger.info("{} finished in {} milliseconds", delegateName, (endTime - startTime) / (double) 1000000);
  }

  public void setPath(Expression path) {
    this.path = path;
  }

  public void setOp(Expression op) {
    this.op = op;
  }

  @Override
  public Class<?> fromTask() {
    return FileTask.class;
  }

}
