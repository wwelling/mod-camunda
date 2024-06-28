package org.folio.rest.camunda.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import java.util.Map;

import org.folio.rest.camunda.exception.ScriptEngineLoadFailed;
import org.folio.rest.camunda.exception.ScriptEngineUnsupported;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ScriptEngineServiceTest {

  @InjectMocks
  private ScriptEngineService scriptEngineService;

  @Mock
  private ScriptEngineManager scriptEngineManager;

  @Mock
  private Map<String, ScriptEngine> scriptEngines;

  @Mock
  private ScriptEngine scriptEngine;

  @Test
  void testRegisterScript_withUnsupportedScriptEngine_throwsScriptEngineUnsupported() {
    assertThrows(ScriptEngineUnsupported.class, () -> {
      scriptEngineService.registerScript("unsupported", "testFunction", "test script");
    });
  }

  @Test
  void testRegisterScript_withValidScriptEngine_registersSuccessfully() throws Exception {
    when(scriptEngines.get(anyString())).thenReturn(null);
    when(scriptEngineManager.getEngineByExtension(anyString())).thenReturn(scriptEngine);
    when(scriptEngine.eval(anyString())).thenReturn(null);

    scriptEngineService.registerScript("js", "testFunction", "function testFunction() {}");

    // this occurs twice; once to load the utils javascript and second for the script itself
    verify(scriptEngine, times(2)).eval(anyString());
  }

  @Test
  void testRegisterScript_withEngineLoadFailure_throwsScriptEngineLoadFailed() {
    when(scriptEngines.get(anyString())).thenReturn(null);
    when(scriptEngineManager.getEngineByExtension(anyString())).thenReturn(null);

    assertThrows(ScriptEngineLoadFailed.class, () -> {
      scriptEngineService.registerScript("js", "testFunction", "function testFunction() {}");
    });
  }

  // NOTE: this test works as expected
  // whereas the ScriptEngineService throws exception trying to execute Java from JavaScript post GraalVM upgrade
  @Test
  void testRunScript_withRegisteredScript_executesSuccessfully() throws Exception {
      when(scriptEngines.get(anyString())).thenReturn(null);
      when(scriptEngineManager.getEngineByExtension(anyString())).thenReturn(scriptEngine);
      when(scriptEngine.eval(anyString())).thenReturn(null);

      String script = "function testFunction() { return 'result'; }";

      scriptEngineService.registerScript("js", "testFunction", script);

      ScriptEngineManager testScriptEngineManager = new ScriptEngineManager();
      ScriptEngine testEngine = testScriptEngineManager.getEngineByExtension("js");

      when(scriptEngines.get(anyString())).thenReturn(testEngine);

      testEngine.eval(script);

      Object result = scriptEngineService.runScript("js", "testFunction");

      assertEquals("result", result);
  }

}
