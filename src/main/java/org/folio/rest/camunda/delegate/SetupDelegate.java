package org.folio.rest.camunda.delegate;

import static org.camunda.spin.Spin.JSON;

import com.fasterxml.jackson.core.type.TypeReference;
import java.util.List;
import java.util.Map;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.spin.json.SpinJsonNode;
import org.folio.rest.camunda.service.ScriptEngineService;
import org.folio.rest.workflow.model.EmbeddedProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope("prototype")
public class SetupDelegate extends AbstractRuntimeDelegate {

  private static final String TIMESTAMP_VARIABLE_NAME = "timestamp";
  private static final String TENANT_VARIABLE_NAME = "tenantId";

  @Autowired
  private ScriptEngineService scriptEngineService;

  private Expression initialContext;

  private Expression processors;

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    final long startTime = determineStartTime(execution);

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

    List<EmbeddedProcessor> processorsValue = objectMapper.readValue(this.processors.getValue(execution).toString(),
      new TypeReference<List<EmbeddedProcessor>>() {
    });

    for (EmbeddedProcessor processor : processorsValue) {
      String extension = processor.getScriptType().getExtension();
      String functionName = processor.getFunctionName();
      String code = processor.getCode();
      scriptEngineService.registerScript(extension, functionName, code);
      getLogger().info("{}: {}", processor.getFunctionName(), processor.getCode());
    }

    determineEndTime(execution, startTime);
  }

  public void setInitialContext(Expression initialContext) {
    this.initialContext = initialContext;
  }

  public void setProcessors(Expression processors) {
    this.processors = processors;
  }

}
