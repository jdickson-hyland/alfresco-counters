package com.hyland.alfresco.counters.countertool.service;

import com.hyland.alfresco.counters.countertool.model.CounterDefinition;
import com.hyland.alfresco.counters.countertool.model.CounterModel;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.repo.lock.JobLockService;
import org.alfresco.repo.transaction.RetryingTransactionHelper;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CounterServiceImpl implements CounterService {

    private NodeService nodeService;
    private RetryingTransactionHelper retryingTransactionHelper;
    private JobLockService jobLockService;

    private volatile NodeRef counterStoreRef;

    // --- Spring setters ---

    public void setNodeService(NodeService nodeService)                         { this.nodeService = nodeService; }
    public void setRetryingTransactionHelper(RetryingTransactionHelper h)       { this.retryingTransactionHelper = h; }
    public void setJobLockService(JobLockService jobLockService)                { this.jobLockService = jobLockService; }

    // --- Public API ---

    @Override
    public String getNextFormattedValue(String counterName) {
        QName lockQName = QName.createQName(CounterModel.LOCK_NAMESPACE, safeLocalName(counterName));
        String lockToken = jobLockService.getLock(lockQName, CounterModel.LOCK_TTL_MS);
        try {
            return retryingTransactionHelper.doInTransaction(() -> {
                NodeRef node = findCounterNode(counterName);
                long current   = toLong(nodeService.getProperty(node, CounterModel.PROP_CURRENT_VALUE));
                int  increment = toInt(nodeService.getProperty(node, CounterModel.PROP_INCREMENT));
                long next      = current + increment;
                nodeService.setProperty(node, CounterModel.PROP_CURRENT_VALUE, next);
                String template = (String) nodeService.getProperty(node, CounterModel.PROP_FORMAT_TEMPLATE);
                int    padding  = toInt(nodeService.getProperty(node, CounterModel.PROP_PADDING));
                return applyTemplate(template, next, padding);
            }, false, true);
        } finally {
            jobLockService.releaseLock(lockToken, lockQName);
        }
    }

    @Override
    public long getNextRawValue(String counterName) {
        QName lockQName = QName.createQName(CounterModel.LOCK_NAMESPACE, safeLocalName(counterName));
        String lockToken = jobLockService.getLock(lockQName, CounterModel.LOCK_TTL_MS);
        try {
            return retryingTransactionHelper.doInTransaction(() -> {
                NodeRef node = findCounterNode(counterName);
                long current   = toLong(nodeService.getProperty(node, CounterModel.PROP_CURRENT_VALUE));
                int  increment = toInt(nodeService.getProperty(node, CounterModel.PROP_INCREMENT));
                long next      = current + increment;
                nodeService.setProperty(node, CounterModel.PROP_CURRENT_VALUE, next);
                return next;
            }, false, true);
        } finally {
            jobLockService.releaseLock(lockToken, lockQName);
        }
    }

    @Override
    public void createCounter(CounterDefinition def) {
        retryingTransactionHelper.doInTransaction(() -> {
            boolean exists = counterExists(def.getName());
            if (exists) {
                throw new CounterAlreadyExistsException("Counter already exists: " + def.getName());
            }
            NodeRef storeRef = getCounterStoreRef();
            Map<QName, Serializable> props = new HashMap<>();
            props.put(CounterModel.PROP_COUNTER_NAME,    def.getName());
            props.put(CounterModel.PROP_CURRENT_VALUE,   def.getStartValue());
            props.put(CounterModel.PROP_START_VALUE,     def.getStartValue());
            props.put(CounterModel.PROP_INCREMENT,       def.getIncrement() > 0 ? def.getIncrement() : 1);
            props.put(CounterModel.PROP_PADDING,         def.getPadding());
            props.put(CounterModel.PROP_FORMAT_TEMPLATE, def.getFormatTemplate() != null ? def.getFormatTemplate() : "{value}");
            QName assocName = QName.createQName(CounterModel.NAMESPACE, safeLocalName(def.getName()));
            nodeService.createNode(storeRef, ContentModel.ASSOC_CONTAINS, assocName,
                CounterModel.TYPE_COUNTER, props);
            return null;
        }, false, false);
    }

    @Override
    public void updateCounter(CounterDefinition def) {
        retryingTransactionHelper.doInTransaction(() -> {
            NodeRef node = findCounterNode(def.getName());
            if (def.getFormatTemplate() != null) {
                nodeService.setProperty(node, CounterModel.PROP_FORMAT_TEMPLATE, def.getFormatTemplate());
            }
            if (def.getIncrement() > 0) {
                nodeService.setProperty(node, CounterModel.PROP_INCREMENT, def.getIncrement());
            }
            if (def.getPadding() >= 0) {
                nodeService.setProperty(node, CounterModel.PROP_PADDING, def.getPadding());
            }
            return null;
        }, false, false);
    }

    @Override
    public void deleteCounter(String counterName) {
        retryingTransactionHelper.doInTransaction(() -> {
            NodeRef node = findCounterNode(counterName);
            nodeService.deleteNode(node);
            return null;
        }, false, false);
    }

    @Override
    public CounterDefinition getCounter(String counterName) {
        return retryingTransactionHelper.doInTransaction(() -> {
            NodeRef node = findCounterNode(counterName);
            return nodeToDefinition(node);
        }, true, false);
    }

    @Override
    public List<String> listCounters() {
        return retryingTransactionHelper.doInTransaction(() -> {
            NodeRef storeRef = getCounterStoreRef();
            List<ChildAssociationRef> children = nodeService.getChildAssocs(storeRef);
            List<String> names = new ArrayList<>(children.size());
            for (ChildAssociationRef child : children) {
                Object name = nodeService.getProperty(child.getChildRef(), CounterModel.PROP_COUNTER_NAME);
                if (name != null) {
                    names.add(name.toString());
                }
            }
            return names;
        }, true, false);
    }

    // --- Package-level helpers used by CounterBootstrapComponent ---

    NodeRef getOrCreateCounterStoreRef(NodeRef dataDictRef) {
        NodeRef existing = nodeService.getChildByName(dataDictRef, ContentModel.ASSOC_CONTAINS, "Counter Store");
        if (existing != null) {
            counterStoreRef = existing;
            return existing;
        }
        Map<QName, Serializable> props = new HashMap<>();
        props.put(ContentModel.PROP_NAME, "Counter Store");
        ChildAssociationRef assoc = nodeService.createNode(dataDictRef, ContentModel.ASSOC_CONTAINS,
            QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "Counter_x0020_Store"),
            ContentModel.TYPE_FOLDER, props);
        counterStoreRef = assoc.getChildRef();
        return counterStoreRef;
    }

    // --- Private helpers ---

    private NodeRef getCounterStoreRef() {
        if (counterStoreRef == null) {
            synchronized (this) {
                if (counterStoreRef == null) {
                    counterStoreRef = AuthenticationUtil.runAsSystem(() ->
                        retryingTransactionHelper.doInTransaction(() ->
                            getOrCreateCounterStoreRef(findDataDictionary()),
                            false, true)
                    );
                }
            }
        }
        return counterStoreRef;
    }

    NodeRef findDataDictionary() {
        NodeRef rootNode = nodeService.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        List<ChildAssociationRef> homes = nodeService.getChildAssocs(rootNode,
            ContentModel.ASSOC_CHILDREN,
            QName.createQName(NamespaceService.APP_MODEL_1_0_URI, "company_home"));
        if (homes.isEmpty()) {
            throw new AlfrescoRuntimeException("Company Home not found");
        }
        List<ChildAssociationRef> dicts = nodeService.getChildAssocs(homes.get(0).getChildRef(),
            ContentModel.ASSOC_CONTAINS,
            QName.createQName(NamespaceService.APP_MODEL_1_0_URI, "dictionary"));
        if (dicts.isEmpty()) {
            throw new AlfrescoRuntimeException("Data Dictionary folder not found");
        }
        return dicts.get(0).getChildRef();
    }

    private NodeRef findCounterNode(String counterName) {
        NodeRef storeRef = getCounterStoreRef();
        List<ChildAssociationRef> children = nodeService.getChildAssocs(storeRef);
        for (ChildAssociationRef child : children) {
            Object name = nodeService.getProperty(child.getChildRef(), CounterModel.PROP_COUNTER_NAME);
            if (counterName.equals(name)) {
                return child.getChildRef();
            }
        }
        throw new CounterNotFoundException("Counter not found: " + counterName);
    }

    private boolean counterExists(String counterName) {
        NodeRef storeRef = getCounterStoreRef();
        List<ChildAssociationRef> children = nodeService.getChildAssocs(storeRef);
        for (ChildAssociationRef child : children) {
            Object name = nodeService.getProperty(child.getChildRef(), CounterModel.PROP_COUNTER_NAME);
            if (counterName.equals(name)) {
                return true;
            }
        }
        return false;
    }

    private CounterDefinition nodeToDefinition(NodeRef node) {
        CounterDefinition def = new CounterDefinition();
        def.setName((String) nodeService.getProperty(node, CounterModel.PROP_COUNTER_NAME));
        def.setStartValue(toLong(nodeService.getProperty(node, CounterModel.PROP_START_VALUE)));
        def.setIncrement(toInt(nodeService.getProperty(node, CounterModel.PROP_INCREMENT)));
        def.setPadding(toInt(nodeService.getProperty(node, CounterModel.PROP_PADDING)));
        def.setFormatTemplate((String) nodeService.getProperty(node, CounterModel.PROP_FORMAT_TEMPLATE));
        return def;
    }

    private String applyTemplate(String template, long value, int padding) {
        String valueStr = padding > 0
            ? String.format("%0" + padding + "d", value)
            : String.valueOf(value);
        if (template == null || template.isEmpty()) {
            return valueStr;
        }
        LocalDate now = LocalDate.now();
        return template
            .replace("{value}", valueStr)
            .replace("{YYYY}", String.format("%04d", now.getYear()))
            .replace("{YY}",   String.format("%02d", now.getYear() % 100))
            .replace("{MM}",   String.format("%02d", now.getMonthValue()))
            .replace("{DD}",   String.format("%02d", now.getDayOfMonth()));
    }

    private static String safeLocalName(String name) {
        return QName.createValidLocalName(name);
    }

    private static long toLong(Object o) {
        if (o instanceof Long)    return (Long) o;
        if (o instanceof Integer) return ((Integer) o).longValue();
        if (o instanceof Number)  return ((Number) o).longValue();
        return 0L;
    }

    private static int toInt(Object o) {
        if (o instanceof Integer) return (Integer) o;
        if (o instanceof Long)    return ((Long) o).intValue();
        if (o instanceof Number)  return ((Number) o).intValue();
        return 0;
    }
}
