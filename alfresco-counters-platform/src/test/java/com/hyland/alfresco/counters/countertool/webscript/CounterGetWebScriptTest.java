package com.hyland.alfresco.counters.countertool.webscript;

import com.hyland.alfresco.counters.countertool.service.CounterNotFoundException;
import com.hyland.alfresco.counters.countertool.service.CounterService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;

import java.util.Collections;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CounterGetWebScriptTest extends BaseCounterWebScriptTest {

    private static final String DESCRIPTOR =
        "alfresco/extension/templates/webscripts/hyland/counters/counter.get.desc.xml";

    private CounterGetWebScript webScript;
    private CounterService counterService;

    @Before
    public void setUp() {
        counterService = mock(CounterService.class);
        webScript = new CounterGetWebScript();
        webScript.setCounterService(counterService);
    }

    private void mockNameVar(String name) {
        Match match = new Match("/hyland/counters/{name}", Collections.singletonMap("name", name), "/hyland/counters/" + name);
        when(req.getServiceMatch()).thenReturn(match);
    }

    @Test
    public void testExecute_existingCounter_returnsJson() throws Exception {
        mockNameVar("invoice");
        when(counterService.getCounter("invoice"))
            .thenReturn(makeDefinition("invoice", "INV-{YYYY}{MM}-{value}", 1, 5));

        webScript.execute(req, res);

        String json = response();
        assertTrue(json.contains("\"invoice\""));
        assertTrue(json.contains("\"INV-{YYYY}{MM}-{value}\""));
        assertTrue(json.contains("\"increment\""));
        assertTrue(json.contains("\"padding\""));
    }

    @Test
    public void testExecute_unknownCounter_returns404() throws Exception {
        mockNameVar("nonexistent");
        when(counterService.getCounter("nonexistent"))
            .thenThrow(new CounterNotFoundException("Counter not found: nonexistent"));

        webScript.execute(req, res);

        verify(res).setStatus(Status.STATUS_NOT_FOUND);
        assertTrue(response().contains("\"error\""));
        assertTrue(response().contains("nonexistent"));
    }

    @Test
    public void testDescriptor_requiresAdminAuthentication() throws Exception {
        assertDescriptorRequiresAdminAuth(DESCRIPTOR);
    }
}
