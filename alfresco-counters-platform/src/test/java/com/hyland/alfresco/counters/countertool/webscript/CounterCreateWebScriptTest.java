package com.hyland.alfresco.counters.countertool.webscript;

import com.hyland.alfresco.counters.countertool.model.CounterDefinition;
import com.hyland.alfresco.counters.countertool.service.CounterAlreadyExistsException;
import com.hyland.alfresco.counters.countertool.service.CounterService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.Status;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CounterCreateWebScriptTest extends BaseCounterWebScriptTest {

    private static final String DESCRIPTOR =
        "alfresco/extension/templates/webscripts/hyland/counters/counters.post.desc.xml";

    private CounterCreateWebScript webScript;
    private CounterService counterService;

    @Before
    public void setUp() {
        counterService = mock(CounterService.class);
        webScript = new CounterCreateWebScript();
        webScript.setCounterService(counterService);
    }

    private void mockBody(String json) throws Exception {
        Content content = mock(Content.class);
        when(content.getContent()).thenReturn(json);
        when(req.getContent()).thenReturn(content);
    }

    @Test
    public void testExecute_validPayload_returns201WithCounterJson() throws Exception {
        CounterDefinition created = makeDefinition("invoice", "INV-{YYYY}{MM}-{value}", 1, 5);
        when(counterService.getCounter("invoice")).thenReturn(created);
        mockBody("{\"name\":\"invoice\",\"formatTemplate\":\"INV-{YYYY}{MM}-{value}\",\"increment\":1,\"padding\":5,\"startValue\":0}");

        webScript.execute(req, res);

        verify(res).setStatus(Status.STATUS_CREATED);
        verify(counterService).createCounter(any(CounterDefinition.class));
        assertTrue(response().contains("\"invoice\""));
        assertTrue(response().contains("\"INV-{YYYY}{MM}-{value}\""));
    }

    @Test
    public void testExecute_missingName_returns400() throws Exception {
        mockBody("{\"formatTemplate\":\"INV-{value}\",\"increment\":1}");

        webScript.execute(req, res);

        verify(res).setStatus(Status.STATUS_BAD_REQUEST);
        assertTrue(response().contains("\"error\""));
        assertTrue(response().contains("name is required"));
    }

    @Test
    public void testExecute_emptyName_returns400() throws Exception {
        mockBody("{\"name\":\"\",\"formatTemplate\":\"INV-{value}\"}");

        webScript.execute(req, res);

        verify(res).setStatus(Status.STATUS_BAD_REQUEST);
    }

    @Test
    public void testExecute_duplicateCounter_returns409() throws Exception {
        doThrow(new CounterAlreadyExistsException("Counter already exists: invoice"))
            .when(counterService).createCounter(any(CounterDefinition.class));
        mockBody("{\"name\":\"invoice\",\"increment\":1,\"startValue\":0,\"padding\":0,\"formatTemplate\":\"{value}\"}");

        webScript.execute(req, res);

        verify(res).setStatus(Status.STATUS_CONFLICT);
        assertTrue(response().contains("\"error\""));
        assertTrue(response().contains("invoice"));
    }

    @Test
    public void testDescriptor_requiresAdminAuthentication() throws Exception {
        assertDescriptorRequiresAdminAuth(DESCRIPTOR);
    }
}
