import org.camunda.bpm.engine.impl.util.json.JSONObject;
import org.folio.rest.utility.ScriptEngineUtility;

public String %s(String inArgs) {
  ScriptEngineUtility scriptEngineUtility = new ScriptEngineUtility();
  JSONObject args = scriptEngineUtility.decodeJson(inArgs);
  JSONObject returnObj = scriptEngineUtility.createJson();
  %s
  return scriptEngineUtility.encodeJson(returnObj);
}