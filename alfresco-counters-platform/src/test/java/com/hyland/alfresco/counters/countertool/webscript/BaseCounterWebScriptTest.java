package com.hyland.alfresco.counters.countertool.webscript;

import com.hyland.alfresco.counters.countertool.model.CounterDefinition;
import org.junit.Before;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class BaseCounterWebScriptTest {

    protected WebScriptRequest req;
    protected WebScriptResponse res;
    protected StringWriter responseOutput;

    @Before
    public void setUpBase() throws IOException {
        req = mock(WebScriptRequest.class);
        res = mock(WebScriptResponse.class);
        responseOutput = new StringWriter();
        when(res.getWriter()).thenReturn(new PrintWriter(responseOutput));
    }

    protected String response() {
        return responseOutput.toString();
    }

    protected void assertDescriptorRequiresAdminAuth(String classpathPath) throws Exception {
        URL resource = getClass().getClassLoader().getResource(classpathPath);
        assertNotNull("Descriptor not found on classpath: " + classpathPath, resource);
        String xml = new String(Files.readAllBytes(Paths.get(resource.toURI())), StandardCharsets.UTF_8);
        assertTrue(
            "Descriptor " + classpathPath + " must declare <authentication>admin</authentication>",
            xml.contains("<authentication>admin</authentication>"));
    }

    protected static CounterDefinition makeDefinition(String name, String template, int increment, int padding) {
        CounterDefinition def = new CounterDefinition();
        def.setName(name);
        def.setFormatTemplate(template);
        def.setIncrement(increment);
        def.setPadding(padding);
        def.setStartValue(0);
        return def;
    }
}
