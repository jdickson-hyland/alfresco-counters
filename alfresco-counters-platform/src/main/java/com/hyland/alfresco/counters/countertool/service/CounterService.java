package com.hyland.alfresco.counters.countertool.service;

import com.hyland.alfresco.counters.countertool.model.CounterDefinition;

import java.util.List;

public interface CounterService {

    String getNextFormattedValue(String counterName);

    long getNextRawValue(String counterName);

    void createCounter(CounterDefinition def);

    void updateCounter(CounterDefinition def);

    void deleteCounter(String counterName);

    CounterDefinition getCounter(String counterName);

    List<String> listCounters();
}
