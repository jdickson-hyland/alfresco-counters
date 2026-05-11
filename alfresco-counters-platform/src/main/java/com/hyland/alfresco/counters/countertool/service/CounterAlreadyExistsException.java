package com.hyland.alfresco.counters.countertool.service;

public class CounterAlreadyExistsException extends RuntimeException {
    public CounterAlreadyExistsException(String message) {
        super(message);
    }
}
