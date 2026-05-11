package com.hyland.alfresco.counters.countertool.webscript;

import com.hyland.alfresco.counters.countertool.service.CounterNotFoundException;
import com.hyland.alfresco.counters.countertool.service.CounterService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CounterDeleteWebScriptTest extends BaseCounterWebScriptTest {

    private static final String DESCRIPTOR =
        "alfresco/extension/templates/webscripts/hyland/counters/counter.delete.desc.xml";

    private CounterDeleteWebScript webScript;
    private CounterService counterService;

    @Before
    public void setUp() {
        counterService = mock(CounterService.class);
        webScript = new CounterDeleteWebScript();
        webScript.setCounterService(counterService);
    }

    private void mockNameVar(String name) {
        Match match = new Match("/hyland/counters/{name}", Collections.singletonMap("name", name), "/hyland/counters/" + name);
        when(req.getServiceMatch()).thenReturn(match);
    }

    @Test
    public void testExecute_existingCounter_deletesAndReturnsEmpty() throws Exception {
        mockNameVar("invoice");

        webScript.execute(req, res);

        verify(counterService).deleteCounter("invoice");
        assertEquals("{}", response());
    }

    @Test
    public void testExecute_unknownCounter_returns404() throws Exception {
        mockNameVar("nonexistent");
        doThrow(new CounterNotFoundException("Counter not found: nonexistent"))
            .when(counterService).deleteCounter("nonexistent");

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
