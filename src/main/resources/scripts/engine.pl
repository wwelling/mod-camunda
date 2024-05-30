use JSON;
use org.folio.rest.camunda.utility.ScriptEngineUtility;

sub %s($) {
  my ($inArgs) = @_;
  my $scriptEngineUtility = ScriptEngineUtility();
  my $args = scriptEngineUtility.decodeJson($inArgs);
  my $returnObj = scriptEngineUtility.createJson();
  %s
  return scriptEngineUtility.encodeJson($returnObj);
}