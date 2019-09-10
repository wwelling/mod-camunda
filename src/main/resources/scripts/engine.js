var %s = function(inArgs) {
  var UUID = Java.type("java.util.UUID");
  // var ScriptEngineUtilityClass = Java.type("org.folio.rest.utility.ScriptEngineUtility");
  // var scriptEngineUtility = new ScriptEngineUtilityClass();
  var args = JSON.parse(inArgs);
  var returnObj = {};
  %s
  return JSON.stringify(returnObj);
}
