package com.hyland.alfresco.counters.countertool.model;

import org.alfresco.service.namespace.QName;

public interface CounterModel {

    String NAMESPACE   = "http://www.hyland.com/model/counter/1.0";
    String LOCK_NAMESPACE = "http://www.hyland.com/lock/counter/1.0";
    long   LOCK_TTL_MS = 5000L;

    String COUNTER_STORE_PATH = "/app:company_home/app:dictionary/cm:Counter_x0020_Store";

    QName TYPE_COUNTER         = QName.createQName(NAMESPACE, "counter");
    QName PROP_COUNTER_NAME    = QName.createQName(NAMESPACE, "counterName");
    QName PROP_CURRENT_VALUE   = QName.createQName(NAMESPACE, "currentValue");
    QName PROP_START_VALUE     = QName.createQName(NAMESPACE, "startValue");
    QName PROP_INCREMENT       = QName.createQName(NAMESPACE, "increment");
    QName PROP_PADDING         = QName.createQName(NAMESPACE, "padding");
    QName PROP_FORMAT_TEMPLATE = QName.createQName(NAMESPACE, "formatTemplate");
}
