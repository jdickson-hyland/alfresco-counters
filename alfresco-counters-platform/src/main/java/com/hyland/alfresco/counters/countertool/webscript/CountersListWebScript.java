package com.hyland.alfresco.counters.countertool.webscript;

import com.hyland.alfresco.counters.countertool.service.CounterService;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import java.io.IOException;
import java.util.List;

public class CountersListWebScript extends AbstractWebScript {

    private CounterService counterService;

    public void setCounterService(CounterService counterService) {
        this.counterService = counterService;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        res.setContentType("application/json");
        res.setContentEncoding("UTF-8");
        List<String> names = counterService.listCounters();
        JSONArray arr = new JSONArray();
        for (String name : names) {
            arr.put(counterService.getCounter(name).toJson());
        }
        JSONObject result = new JSONObject();
        result.put("counters", arr);
        res.getWriter().write(result.toString());
    }
}
