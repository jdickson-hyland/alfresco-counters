package com.hyland.alfresco.counters.countertool.webscript;

import com.hyland.alfresco.counters.countertool.model.CounterDefinition;
import com.hyland.alfresco.counters.countertool.service.CounterNotFoundException;
import com.hyland.alfresco.counters.countertool.service.CounterService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;

import java.util.Collections;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CounterUpdateWebScriptTest extends BaseCounterWebScriptTest {

    private static final String DESCRIPTOR =
        "alfresco/extension/templates/webscripts/hyland/counters/counter.put.desc.xml";

    private CounterUpdateWebScript webScript;
    private CounterService counterService;

    @Before
    public void setUp() {
        counterService = mock(CounterService.class);
        webScript = new CounterUpdateWebScript();
        webScript.setCounterService(counterService);
    }

    private void mockNameVar(String name) {
        Match match = new Match("/hyland/counters/{name}", Collections.singletonMap("name", name), "/hyland/counters/" + name);
        when(req.getServiceMatch()).thenReturn(match);
    }

    private void mockBody(String json) throws Exception {
        Content content = mock(Content.class);
        when(content.getContent()).thenReturn(json);
        when(req.getContent()).thenReturn(content);
    }

    @Test
    public void testExecute_updatesCounter_returnsUpdatedJson() throws Exception {
        mockNameVar("invoice");
        mockBody("{\"formatTemplate\":\"INV-{YYYY}-{value}\",\"increment\":2,\"padding\":6}");
        CounterDefinition updated = makeDefinition("invoice", "INV-{YYYY}-{value}", 2, 6);
        when(counterService.getCounter("invoice")).thenReturn(updated);

        webScript.execute(req, res);

        verify(counterService).updateCounter(any(CounterDefinition.class));
        String json = response();
        assertTrue(json.contains("\"invoice\""));
        assertTrue(json.contains("\"INV-{YYYY}-{value}\""));
    }

    @Test
    public void testExecute_nameInBodyIsOverriddenByPathVar() throws Exception {
        mockNameVar("invoice");
        // Body contains a different name — the path variable must win
        mockBody("{\"name\":\"WRONG\",\"formatTemplate\":\"X-{value}\",\"increment\":1,\"padding\":0}");
        CounterDefinition updated = makeDefinition("invoice", "X-{value}", 1, 0);
        when(counterService.getCounter("invoice")).thenReturn(updated);

        webScript.execute(req, res);

        verify(counterService).updateCounter(any(CounterDefinition.class));
        assertTrue(response().contains("\"invoice\""));
    }

    @Test
    public void testExecute_unknownCounter_returns404() throws Exception {
        mockNameVar("nonexistent");
        mockBody("{\"formatTemplate\":\"X-{value}\",\"increment\":1,\"padding\":0}");
        doThrow(new CounterNotFoundException("Counter not found: nonexistent"))
            .when(counterService).updateCounter(any(CounterDefinition.class));

        webScript.execute(req, res);

        verify(res).setStatus(Status.STATUS_NOT_FOUND);
        assertTrue(response().contains("\"error\""));
    }

    @Test
    public void testDescriptor_requiresAdminAuthentication() throws Exception {
        assertDescriptorRequiresAdminAuth(DESCRIPTOR);
    }
}
