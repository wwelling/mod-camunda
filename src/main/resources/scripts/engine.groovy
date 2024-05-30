import org.folio.rest.camunda.utility.ScriptEngineUtility

def %s(String inArgs) {
  def scriptEngineUtility = new ScriptEngineUtility();
  def args = scriptEngineUtility.decodeJson(inArgs);
  def returnObj = scriptEngineUtility.createJson();
  %s
  return scriptEngineUtility.encodeJson(returnObj);
}
