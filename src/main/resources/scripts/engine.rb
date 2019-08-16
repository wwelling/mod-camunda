require 'json'
require 'use org.folio.rest.utility.ScriptEngineUtility'

def %s(inArgs)
  scriptEngineUtility = ScriptEngineUtility();
  args = scriptEngineUtility.decodeJson(inArgs);
  returnObj = scriptEngineUtility.createJson();
  %s
  return scriptEngineUtility.encodeJson(returnObj)
end