import json
import org.folio.rest.camunda.utility.ScriptEngineUtility;

def %s(inArgs):
  scriptEngineUtility = org.folio.rest.utility.ScriptEngineUtility();
  args = scriptEngineUtility.decodeJson(inArgs);
  returnObj = scriptEngineUtility.createJson();
  %s
  return scriptEngineUtility.encodeJson(returnObj);
