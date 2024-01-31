package org.folio.rest.delegate;

import static org.camunda.spin.Spin.JSON;

import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.model.bpmn.instance.FlowElement;
import org.camunda.spin.json.SpinJsonNode;
import org.folio.rest.service.ScriptEngineService;
import org.folio.rest.workflow.model.EmbeddedProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;

@Service
@Scope("prototype")
public class SetupDelegate extends AbstractRuntimeDelegate {

  private final static String TIMESTAMP_VARIABLE_NAME = "timestamp";
  private final static String TENANT_VARIABLE_NAME = "tenantId";

  @Autowired
  private ScriptEngineService scriptEngineService;

  private Expression initialContext;

  private Expression processors;

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    long startTime = System.nanoTime();
    FlowElement bpmnModelElement = execution.getBpmnModelElementInstance();
    String delegateName = bpmnModelElement.getName();

    getLogger().info("{} started", delegateName);

    getLogger().info("loading initial context");

    Map<String, Object> context = objectMapper.readValue(initialContext.getValue(execution).toString(),
        new TypeReference<Map<String, Object>>() {
        });

    for (Map.Entry<String, Object> entry : context.entrySet()) {
      SpinJsonNode node = JSON(objectMapper.writeValueAsString(entry.getValue()));
      execution.setVariable(entry.getKey(), node);
      getLogger().info("{}: {}", entry.getKey(), node);
    }

    String timestamp = String.valueOf(System.currentTimeMillis());

    execution.setVariable(TIMESTAMP_VARIABLE_NAME, timestamp);
    execution.setVariable(TENANT_VARIABLE_NAME, execution.getTenantId());    

    getLogger().info("loading scripts");

    List<EmbeddedProcessor> processors = objectMapper.readValue(this.processors.getValue(execution).toString(),
        new TypeReference<List<EmbeddedProcessor>>() {
        });

    for (EmbeddedProcessor processor : processors) {
      String extension = processor.getScriptType().getExtension();
      String functionName = processor.getFunctionName();
      String code = processor.getCode();
      scriptEngineService.registerScript(extension, functionName, code);
      getLogger().info("{}: {}", processor.getFunctionName(), processor.getCode());
    }

    long endTime = System.nanoTime();
    getLogger().info("{} finished in {} milliseconds", delegateName, (endTime - startTime) / (double) 1000000);
  }

  public void setInitialContext(Expression initialContext) {
    this.initialContext = initialContext;
  }

  public void setProcessors(Expression processors) {
    this.processors = processors;
  }

}
