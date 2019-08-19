use JSON;
use org.folio.rest.utility.ScriptEngineUtility;

sub %s($) {
  my ($inArgs) = @_;
  my $scriptEngineUtility = ScriptEngineUtility();
  my $args = scriptEngineUtility.decodeJson($inArgs);
  my $returnObj = scriptEngineUtility.createJson();
  %s
  return scriptEngineUtility.encodeJson($returnObj);
}