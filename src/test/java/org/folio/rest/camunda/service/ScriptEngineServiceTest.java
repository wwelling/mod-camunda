package org.folio.rest.camunda.service;

import org.folio.rest.camunda.exception.ScriptEngineLoadFailed;
import org.folio.rest.camunda.exception.ScriptEngineUnsupported;
import org.folio.rest.workflow.enums.ScriptType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class ScriptEngineServiceTest {

    private ScriptEngineService scriptEngineService;
    private ScriptEngineManager scriptEngineManagerMock;
    private ScriptEngine scriptEngineMock;

    @BeforeEach
    void setUp() throws Exception {
        scriptEngineService = new ScriptEngineService();
        scriptEngineManagerMock = mock(ScriptEngineManager.class);
        scriptEngineMock = mock(ScriptEngine.class);

        setPrivateField(scriptEngineService, "scriptEngineManager", scriptEngineManagerMock);
        setPrivateField(scriptEngineService, "scriptEngines", new HashMap<String, ScriptEngine>());
    }

    private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @SuppressWarnings("unchecked")
    private <T> T getPrivateField(Object target, String fieldName) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return (T) field.get(target);
    }

    @Test
    void testRegisterScript_withUnsupportedScriptEngine_throwsScriptEngineUnsupported() {
        assertThrows(ScriptEngineUnsupported.class, () -> {
            scriptEngineService.registerScript("unsupported", "testFunction", "test script");
        });
    }

    @Test
    void testRegisterScript_withValidScriptEngine_registersSuccessfully() throws Exception {
        when(scriptEngineManagerMock.getEngineByExtension(anyString())).thenReturn(scriptEngineMock);
        when(scriptEngineMock.eval(anyString())).thenReturn(null);

        scriptEngineService.registerScript("js", "testFunction", "function testFunction() {}");

        Map<String, ScriptEngine> scriptEngines = getPrivateField(scriptEngineService, "scriptEngines");
        assertTrue(scriptEngines.containsKey("js"));
        //This item runs twice for some reason
        verify(scriptEngineMock, times(2)).eval(anyString());
    }

    @Test
    void testRegisterScript_withEngineLoadFailure_throwsScriptEngineLoadFailed() {
        when(scriptEngineManagerMock.getEngineByExtension(anyString())).thenReturn(null);

        assertThrows(ScriptEngineLoadFailed.class, () -> {
            scriptEngineService.registerScript("js", "testFunction", "function testFunction() {}");
        });
    }

    @Test
    void testRunScript_withRegisteredScript_executesSuccessfully() throws Exception {
        when(scriptEngineManagerMock.getEngineByExtension(anyString())).thenReturn(scriptEngineMock);
        when(scriptEngineMock.eval(anyString())).thenReturn(null);

        scriptEngineService.registerScript("js", "testFunction", "function testFunction() {}");

        Invocable invocableMock = mock(Invocable.class);
        when(invocableMock.invokeFunction(anyString(), any())).thenReturn("result");

        Map<String, ScriptEngine> scriptEngines = getPrivateField(scriptEngineService, "scriptEngines");
        scriptEngines.put("js", (ScriptEngine) invocableMock); 

        Object result = scriptEngineService.runScript("js", "testFunction");

        assertEquals("result", result);
        verify(invocableMock, times(1)).invokeFunction(eq("testFunction"));
    }

    // @Test
    // void testLoadScript_readsFileContentSuccessfully() throws IOException {
    //     String expectedContent = "function testFunction() {}";
    //     InputStream inputStreamMock = mock(InputStream.class);
    //     ClassPathResource classPathResourceMock = mock(ClassPathResource.class);
    //     when(classPathResourceMock.getInputStream()).thenReturn(inputStreamMock);
    //     when(inputStreamMock.read(any(byte[].class), anyInt(), anyInt())).thenAnswer(invocation -> {
    //         byte[] buffer = invocation.getArgument(0);
    //         System.arraycopy(expectedContent.getBytes(), 0, buffer, 0, expectedContent.length());
    //         return expectedContent.length();
    //     });
    //     when(scriptEngineService.loadScript(anyString())).thenCallRealMethod();

    //     try (InputStream inputStream = new ClassPathResource("scripts/utils.js").getInputStream()) {
    //         String script = StreamUtils.copyToString(inputStream, Charset.defaultCharset());
    //         assertEquals(expectedContent, script);
    //     }
    // }
}
