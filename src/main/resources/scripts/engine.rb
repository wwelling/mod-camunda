require 'json'
require 'use org.folio.rest.camunda.utility.ScriptEngineUtility'

def %s(inArgs)
  scriptEngineUtility = ScriptEngineUtility();
  args = scriptEngineUtility.decodeJson(inArgs);
  returnObj = scriptEngineUtility.createJson();
  %s
  return scriptEngineUtility.encodeJson(returnObj)
end