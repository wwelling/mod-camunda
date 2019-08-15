var %s = function(inArgs) {
  var ScriptEngineUtilityClass = Java.type("org.folio.rest.utility.ScriptEngineUtility");
  var scriptEngineUtility = new ScriptEngineUtilityClass();
  var args = scriptEngineUtility.decodeJson(inArgs);
  var returnObj = scriptEngineUtility.createJson();
  %s
  return scriptEngineUtility.encodeJson(returnObj);
}
