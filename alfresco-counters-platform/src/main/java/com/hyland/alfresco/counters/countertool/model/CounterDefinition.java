package com.hyland.alfresco.counters.countertool.model;

import org.json.JSONObject;

public class CounterDefinition {

    private String name;
    private long   startValue    = 0;
    private int    increment     = 1;
    private int    padding       = 0;
    private String formatTemplate = "{value}";

    public String getName()            { return name; }
    public long   getStartValue()      { return startValue; }
    public int    getIncrement()       { return increment; }
    public int    getPadding()         { return padding; }
    public String getFormatTemplate()  { return formatTemplate; }

    public void setName(String name)                    { this.name = name; }
    public void setStartValue(long startValue)          { this.startValue = startValue; }
    public void setIncrement(int increment)             { this.increment = increment; }
    public void setPadding(int padding)                 { this.padding = padding; }
    public void setFormatTemplate(String formatTemplate){ this.formatTemplate = formatTemplate; }

    public JSONObject toJson() {
        JSONObject obj = new JSONObject();
        obj.put("name", name);
        obj.put("startValue", startValue);
        obj.put("increment", increment);
        obj.put("padding", padding);
        obj.put("formatTemplate", formatTemplate != null ? formatTemplate : "");
        return obj;
    }

    public static CounterDefinition fromJson(JSONObject json) {
        CounterDefinition def = new CounterDefinition();
        def.setName(json.optString("name", null));
        def.setStartValue(json.optLong("startValue", 0));
        def.setIncrement(json.optInt("increment", 1));
        def.setPadding(json.optInt("padding", 0));
        def.setFormatTemplate(json.optString("formatTemplate", "{value}"));
        return def;
    }
}
