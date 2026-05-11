package com.hyland.alfresco.counters.countertool.webscript;

import com.hyland.alfresco.counters.countertool.model.CounterDefinition;
import com.hyland.alfresco.counters.countertool.service.CounterNotFoundException;
import com.hyland.alfresco.counters.countertool.service.CounterService;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import java.io.IOException;

public class CounterGetWebScript extends AbstractWebScript {

    private CounterService counterService;

    public void setCounterService(CounterService counterService) {
        this.counterService = counterService;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        res.setContentType("application/json");
        res.setContentEncoding("UTF-8");
        String name = req.getServiceMatch().getTemplateVars().get("name");
        try {
            CounterDefinition def = counterService.getCounter(name);
            res.getWriter().write(def.toJson().toString());
        } catch (CounterNotFoundException e) {
            res.setStatus(Status.STATUS_NOT_FOUND);
            res.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
}
