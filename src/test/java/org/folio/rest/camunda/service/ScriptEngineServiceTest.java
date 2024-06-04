package org.folio.rest.camunda.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.folio.rest.camunda.exception.ScriptEngineLoadFailed;
import org.folio.rest.camunda.exception.ScriptEngineUnsupported;
import org.folio.rest.workflow.enums.ScriptType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.lang.reflect.Method;

@ExtendWith(MockitoExtension.class)
public class ScriptEngineServiceTest {

    @InjectMocks
    private ScriptEngineService scriptEngineService;
    
      @Mock
      private ScriptEngineManager scriptEngineManager;
    
      @Mock
      private ScriptEngine scriptEngine;
    
      @Mock
      private ResourceLoader resourceLoader;
    
      @Mock
      private Resource resource;
    
      private Map<String, ScriptEngine> scriptEngines;
    
      @BeforeEach
      void setUp() {
        scriptEngines = new HashMap<>();
        scriptEngineService = new ScriptEngineService();
        
        // Use reflection to access and set the private fields
        try {
          java.lang.reflect.Field scriptEngineManagerField = ScriptEngineService.class.getDeclaredField("scriptEngineManager");
          scriptEngineManagerField.setAccessible(true);
          scriptEngineManagerField.set(scriptEngineService, scriptEngineManager);
    
          java.lang.reflect.Field scriptEnginesField = ScriptEngineService.class.getDeclaredField("scriptEngines");
          scriptEnginesField.setAccessible(true);
          scriptEnginesField.set(scriptEngineService, scriptEngines);
        } catch (Exception e) {
          fail("Failed to set up the test environment");
        }
      }
    
      @Test
      void testRegisterScriptUnsupportedExtension() {
        assertThrows(ScriptEngineUnsupported.class, () -> {
          scriptEngineService.registerScript("unsupported", "testScript", "print('Hello, world!')");
        });
      }
    
      @Test
      void testRegisterScriptLoadFailed() {
        when(scriptEngineManager.getEngineByExtension("js")).thenReturn(null);
    
        assertThrows(ScriptEngineLoadFailed.class, () -> {
          scriptEngineService.registerScript("js", "testScript", "print('Hello, world!')");
        });
      }
    
      @Test
      void testRegisterScriptSuccessful() throws Exception {
        when(scriptEngineManager.getEngineByExtension("js")).thenReturn(scriptEngine);
        when(scriptEngine.eval(anyString())).thenReturn(null);
    
        scriptEngineService.registerScript("js", "testScript", "print('Hello, world!')");
    
        verify(scriptEngineManager).getEngineByExtension("js");
        verify(scriptEngine, times(2)).eval(anyString()); // Once for utils, once for the actual script
      }
    
      @Test
      void testRunScript() throws Exception {
        scriptEngines.put("js", scriptEngine);
        when(scriptEngine instanceof Invocable).thenReturn(true);
        when(((Invocable) scriptEngine).invokeFunction(anyString(), any())).thenReturn("result");
    
        Object result = scriptEngineService.runScript("js", "testScript");
    
        assertEquals("result", result);
      }
    
      @Test
      void testLoadScript() throws Exception {
        String filename = "scripts/test.js";
        String expectedContent = "console.log('Hello, world!');";
    
        // Mocking the ResourceLoader and Resource
        when(resourceLoader.getResource("classpath:" + filename)).thenReturn(resource);
        when(resource.getInputStream()).thenReturn(new ByteArrayInputStream(expectedContent.getBytes(StandardCharsets.UTF_8)));
    
        // Use reflection to access the private loadScript method
        Method loadScriptMethod = ScriptEngineService.class.getDeclaredMethod("loadScript", String.class);
        loadScriptMethod.setAccessible(true);
    
        // Invoke the private method
        String scriptContent = (String) loadScriptMethod.invoke(scriptEngineService, filename);
    
        assertEquals(expectedContent, scriptContent);
      }
    
      @Test
      void testPreprocessScript() throws Exception {
        String pythonScript = "print('Hello, world!')";
        String expectedScript = "  print('Hello, world!')";
    
        // Use reflection to access the private preprocessScript method
        Method preprocessScriptMethod = ScriptEngineService.class.getDeclaredMethod("preprocessScript", String.class, String.class);
        preprocessScriptMethod.setAccessible(true);
    
        // Invoke the private method
        String processedScript = (String) preprocessScriptMethod.invoke(scriptEngineService, pythonScript, ScriptType.PYTHON.getExtension());
    
        assertEquals(expectedScript, processedScript);
      }
    }
    
