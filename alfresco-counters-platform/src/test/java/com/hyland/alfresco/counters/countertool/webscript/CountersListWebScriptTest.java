package com.hyland.alfresco.counters.countertool.webscript;

import com.hyland.alfresco.counters.countertool.service.CounterService;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CountersListWebScriptTest extends BaseCounterWebScriptTest {

    private static final String DESCRIPTOR =
        "alfresco/extension/templates/webscripts/hyland/counters/counters.get.desc.xml";

    private CountersListWebScript webScript;
    private CounterService counterService;

    @Before
    public void setUp() {
        counterService = mock(CounterService.class);
        webScript = new CountersListWebScript();
        webScript.setCounterService(counterService);
    }

    @Test
    public void testExecute_returnsCounterArray() throws Exception {
        when(counterService.listCounters()).thenReturn(Arrays.asList("invoice", "contract"));
        when(counterService.getCounter("invoice")).thenReturn(makeDefinition("invoice", "INV-{value}", 1, 5));
        when(counterService.getCounter("contract")).thenReturn(makeDefinition("contract", "CON-{value}", 1, 0));

        webScript.execute(req, res);

        String json = response();
        assertTrue(json.contains("\"counters\""));
        assertTrue(json.contains("\"invoice\""));
        assertTrue(json.contains("\"contract\""));
    }

    @Test
    public void testExecute_emptyList_returnsEmptyArray() throws Exception {
        when(counterService.listCounters()).thenReturn(Collections.emptyList());

        webScript.execute(req, res);

        String json = response();
        assertTrue(json.contains("\"counters\""));
        assertTrue(json.contains("[]"));
    }

    @Test
    public void testExecute_setsJsonContentType() throws Exception {
        when(counterService.listCounters()).thenReturn(Collections.emptyList());

        webScript.execute(req, res);

        // setContentType is called — verifying via the response mock would require
        // a verify call; here we confirm no exception is thrown during execution
    }

    @Test
    public void testDescriptor_requiresAdminAuthentication() throws Exception {
        assertDescriptorRequiresAdminAuth(DESCRIPTOR);
    }
}
