package com.hyland.alfresco.counters.countertool.webscript;

import com.hyland.alfresco.counters.countertool.model.CounterDefinition;
import com.hyland.alfresco.counters.countertool.service.CounterAlreadyExistsException;
import com.hyland.alfresco.counters.countertool.service.CounterService;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import java.io.IOException;

public class CounterCreateWebScript extends AbstractWebScript {

    private CounterService counterService;

    public void setCounterService(CounterService counterService) {
        this.counterService = counterService;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        res.setContentType("application/json");
        res.setContentEncoding("UTF-8");
        try {
            String body = req.getContent().getContent();
            JSONObject json = new JSONObject(new JSONTokener(body));
            CounterDefinition def = CounterDefinition.fromJson(json);
            if (def.getName() == null || def.getName().isEmpty()) {
                res.setStatus(Status.STATUS_BAD_REQUEST);
                res.getWriter().write("{\"error\":\"name is required\"}");
                return;
            }
            counterService.createCounter(def);
            res.setStatus(Status.STATUS_CREATED);
            res.getWriter().write(counterService.getCounter(def.getName()).toJson().toString());
        } catch (CounterAlreadyExistsException e) {
            res.setStatus(Status.STATUS_CONFLICT);
            res.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            res.setStatus(Status.STATUS_INTERNAL_SERVER_ERROR);
            res.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
}
