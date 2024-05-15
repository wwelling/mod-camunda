import org.graalvm.shadowed.org.json.JSONObject;
import org.folio.rest.camunda.utility.ScriptEngineUtility;

public String %s(String inArgs) {
  ScriptEngineUtility scriptEngineUtility = new ScriptEngineUtility();
  JSONObject args = scriptEngineUtility.decodeJson(inArgs);
  JSONObject returnObj = scriptEngineUtility.createJson();
  %s
  return scriptEngineUtility.encodeJson(returnObj);
}