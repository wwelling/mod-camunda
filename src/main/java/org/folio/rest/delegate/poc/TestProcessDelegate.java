package org.folio.rest.delegate.poc;

import java.nio.charset.Charset;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.model.bpmn.instance.FlowElement;
import org.folio.rest.service.StreamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

@Service
@Scope("prototype")
public class TestProcessDelegate extends TestAbstractRuntimeDelegate {

  @Autowired
  private StreamService streamService;

  private Expression script;

  private Expression scriptType;

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    FlowElement bpmnModelElemen = execution.getBpmnModelElementInstance();
    String delegateName = bpmnModelElemen.getName();

    if(scriptType != null && script != null) {
      ScriptEngine engine =  new ScriptEngineManager().getEngineByName("JavaScript");
      String scriptTemplate = "%s var %s = function(inArgs) {var args = JSON.parse(inArgs); var returnObj = {}; %s return JSON.stringify(returnObj);}";
      String javascriptUtilsContent = StreamUtils.copyToString( new ClassPathResource("scripts/javascriptUtils.js").getInputStream(), Charset.defaultCharset()  );
      String templatedScript = String.format(scriptTemplate, javascriptUtilsContent, delegateName, script.getValue(execution).toString());

      CompiledScript cscript = ((Compilable) engine).compile(templatedScript);
      Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
      cscript.eval(bindings);
      Invocable invocable = (Invocable) cscript.getEngine();

      String primaryStreamId = (String) execution.getVariable("primaryStreamId");
<<<<<<< HEAD

      streamService.getFlux(primaryStreamId).map(d -> {
=======
      
      log.info(String.format("%s STARTED", delegateName));
      streamService.getFlux(primaryStreamId).map(d -> {

>>>>>>> sprint1-staging
        try {
          d = (String) invocable.invokeFunction(delegateName, d);
        } catch (NoSuchMethodException | ScriptException e) {
         e.printStackTrace();
        }

        return d;
      });
    }
  }

  public void setScript(Expression script) {
    this.script = script;
  }

  public void setScriptType(Expression scriptType) {
    this.scriptType = scriptType;
  }

}
