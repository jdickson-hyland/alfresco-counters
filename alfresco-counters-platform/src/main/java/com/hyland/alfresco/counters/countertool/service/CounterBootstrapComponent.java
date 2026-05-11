package com.hyland.alfresco.counters.countertool.service;

import org.alfresco.repo.module.AbstractModuleComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;

public class CounterBootstrapComponent extends AbstractModuleComponent {

    private CounterServiceImpl counterService;

    public void setCounterService(CounterServiceImpl counterService) {
        this.counterService = counterService;
    }

    @Override
    protected void executeInternal() {
        AuthenticationUtil.runAsSystem(() -> {
            counterService.getOrCreateCounterStoreRef(counterService.findDataDictionary());
            return null;
        });
    }
}
