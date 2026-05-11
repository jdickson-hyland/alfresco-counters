package com.hyland.alfresco.counters.countertool.action;

import com.hyland.alfresco.counters.countertool.service.CounterService;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.repo.action.ParameterDefinitionImpl;

import java.util.List;

public class SetCounterValueActionExecuter extends ActionExecuterAbstractBase {

    public static final String NAME               = "set-counter-value";
    public static final String PARAM_COUNTER_NAME = "counter-name";
    public static final String PARAM_TARGET_PROP  = "target-property";

    private CounterService counterService;
    private NodeService    nodeService;
    private NamespaceService namespaceService;

    public void setCounterService(CounterService counterService)       { this.counterService = counterService; }
    public void setNodeService(NodeService nodeService)                 { this.nodeService = nodeService; }
    public void setNamespaceService(NamespaceService namespaceService) { this.namespaceService = namespaceService; }

    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef) {
        String counterName   = (String) action.getParameterValue(PARAM_COUNTER_NAME);
        String targetPropStr = (String) action.getParameterValue(PARAM_TARGET_PROP);
        QName  targetProp    = QName.resolveToQName(namespaceService, targetPropStr);
        String formatted     = counterService.getNextFormattedValue(counterName);
        nodeService.setProperty(actionedUponNodeRef, targetProp, formatted);
    }

    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
        paramList.add(new ParameterDefinitionImpl(
            PARAM_COUNTER_NAME, DataTypeDefinition.TEXT, true,
            getParamDisplayLabel(PARAM_COUNTER_NAME)));
        paramList.add(new ParameterDefinitionImpl(
            PARAM_TARGET_PROP, DataTypeDefinition.TEXT, true,
            getParamDisplayLabel(PARAM_TARGET_PROP)));
    }
}
