package com.hyland.alfresco.counters.countertool.webscript;

import com.hyland.alfresco.counters.countertool.model.CounterDefinition;
import com.hyland.alfresco.counters.countertool.service.CounterNotFoundException;
import com.hyland.alfresco.counters.countertool.service.CounterService;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import java.io.IOException;

public class CounterUpdateWebScript extends AbstractWebScript {

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
            String body = req.getContent().getContent();
            JSONObject json = new JSONObject(new JSONTokener(body));
            json.put("name", name);
            CounterDefinition def = CounterDefinition.fromJson(json);
            counterService.updateCounter(def);
            res.getWriter().write(counterService.getCounter(name).toJson().toString());
        } catch (CounterNotFoundException e) {
            res.setStatus(Status.STATUS_NOT_FOUND);
            res.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            res.setStatus(Status.STATUS_INTERNAL_SERVER_ERROR);
            res.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
}
