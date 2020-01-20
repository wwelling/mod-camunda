package org.folio.rest.delegate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.text.CaseUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.model.bpmn.instance.FlowElement;
import org.folio.rest.service.ScriptEngineService;
import org.folio.rest.workflow.model.ProcessorTask;
import org.folio.rest.workflow.model.ScriptType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;

@Service
@Scope("prototype")
public class ProcessorDelegate extends AbstractWorkflowIODelegate {

  @Autowired
  private ScriptEngineService scriptEngineService;

  private Expression scriptType;

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    long startTime = System.nanoTime();

    FlowElement bpmnModelElement = execution.getBpmnModelElementInstance();

    String delegateName = bpmnModelElement.getName();

    logger.info("{} started", delegateName);

    String scriptTypeExtension = ScriptType.valueOf(scriptType.getValue(execution).toString()).getExtension();

    Map<String, Object> inputs = new HashMap<String, Object>();

    Set<String> contextKeys = objectMapper.readValue(getContextInputKeys().getValue(execution).toString(),
        new TypeReference<Set<String>>() {
        });

    contextKeys.forEach(reqKey -> inputs.put(reqKey, execution.getVariable(reqKey)));

    Set<String> contextCacheKeys = objectMapper.readValue(getContextCacheInputKeys().getValue(execution).toString(),
        new TypeReference<Set<String>>() {
        });

    contextCacheKeys.forEach(key -> {
      Optional<Object> cacheValue = contextCacheService.pull(key);
      if (cacheValue.isPresent()) {
        inputs.put(key, cacheValue.get());
      } else {
        logger.warn("Cannot find {} in context cache", key);
      }
    });

    JsonNode input = objectMapper.valueToTree(inputs);

    String scriptName = CaseUtils.toCamelCase(delegateName, false, ' ');

    String output = (String) scriptEngineService.runScript(scriptTypeExtension, scriptName, input);

    boolean useCacheOutput = Boolean.parseBoolean(getUseCacheOutput().getValue(execution).toString());

    String outputKey = getOutputKey().getValue(execution).toString();

    if (useCacheOutput) {
      contextCacheService.put(outputKey, output);
    } else {
      execution.setVariable(outputKey, output);
    }

    long endTime = System.nanoTime();
    logger.info("{} finished in {} milliseconds", delegateName, (endTime - startTime) / (double) 1000000);
  }

  public void setScriptType(Expression scriptType) {
    this.scriptType = scriptType;
  }

  public void setScriptEngineService(ScriptEngineService scriptEngineService) {
    this.scriptEngineService = scriptEngineService;
  }

  @Override
  public Class<?> fromTask() {
    return ProcessorTask.class;
  }

}