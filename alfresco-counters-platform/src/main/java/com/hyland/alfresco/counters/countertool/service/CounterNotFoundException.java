package com.hyland.alfresco.counters.countertool.service;

public class CounterNotFoundException extends RuntimeException {
    public CounterNotFoundException(String message) {
        super(message);
    }
}
