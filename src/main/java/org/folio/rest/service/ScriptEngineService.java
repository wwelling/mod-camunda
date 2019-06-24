package org.folio.rest.service;

import java.util.HashMap;
import java.util.Map;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.springframework.stereotype.Service;

@Service
public class ScriptEngineService {

  private ScriptEngineManager scriptEngineManager;

  private Map<String, ScriptEngine> scriptEngines;

  private String scriptTemplate = "var %s = function(args) {var returnObj = {}; %s return returnObj;}";

  public ScriptEngineService() {
    configureScriptEngines();
  }

  private void configureScriptEngines() {
    scriptEngineManager = new ScriptEngineManager();
    scriptEngines = new HashMap<String, ScriptEngine>();
    scriptEngines.put("JS", scriptEngineManager.getEngineByExtension("js"));
  }

  public Object runScript(String type, String name, String script, Object ...args)
      throws NoSuchMethodException, ScriptException {
    ScriptEngine scriptEngine = scriptEngines.get(type);
    if(!isFunction(scriptEngine, name)) {      
      scriptEngine.eval(String.format(scriptTemplate, name, script));
    }
    Invocable invocable = (Invocable) scriptEngine;
    return invocable.invokeFunction(name, args);
  }

  private static boolean isFunction(ScriptEngine engine, String name)
      throws ScriptException {
    String test = "typeof " + name
        + " === 'function' ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE";
    return (Boolean) engine.eval(test);
  }

}
