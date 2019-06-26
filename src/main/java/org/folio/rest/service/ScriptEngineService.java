package org.folio.rest.service;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

@Service
public class ScriptEngineService {

  private ScriptEngineManager scriptEngineManager;

  private Map<String, ScriptEngine> scriptEngines;

  private String scriptTemplate = "var %s = function(inArgs) {var args = JSON.parse(inArgs); var returnObj = {}; %s return JSON.stringify(returnObj);}";

  public ScriptEngineService() {
    configureScriptEngines();
  }

  private void configureScriptEngines() {
    scriptEngineManager = new ScriptEngineManager();
    scriptEngines = new HashMap<String, ScriptEngine>();
  }

  public void registerScript(String type, String name, String script) throws ScriptException, IOException {
    Optional<ScriptEngine> maypeScriptEngine = Optional.ofNullable(scriptEngines.get(type));
    if(!maypeScriptEngine.isPresent()) {
      ScriptEngine newEngine = scriptEngineManager.getEngineByExtension(type);
      scriptEngines.put(type, newEngine);
      if(type.equals("js")) {
        String javascriptUtilsContent = StreamUtils.copyToString( new ClassPathResource("scripts/javascriptUtils.js").getInputStream(), Charset.defaultCharset()  );
        newEngine.eval(javascriptUtilsContent);
      }
      maypeScriptEngine = Optional.of(newEngine);
    } 
    ScriptEngine scriptEngine = maypeScriptEngine.get();
    scriptEngine.eval(String.format(scriptTemplate, name, script));
  }

  public Object runScript(String type, String name, Object ...args)
      throws NoSuchMethodException, ScriptException {
    Invocable invocable = (Invocable) scriptEngines.get(type);
    return invocable.invokeFunction(name, args);
  }

}
