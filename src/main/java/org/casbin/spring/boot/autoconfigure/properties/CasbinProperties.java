package org.casbin.spring.boot.autoconfigure.properties;

import org.casbin.exception.CasbinModelConfigNotFoundException;
import org.casbin.exception.CasbinPolicyConfigNotFoundException;
import org.casbin.utils.FileUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.InputStream;

/**
 * @author fangzhengjin
 * @version V1.0
 * @title: CasbinProperties
 * @package org.casbin.spring.boot.autoconfigure.properties
 * @description:
 * @date 2019/4/2 15:25
 */
@ConfigurationProperties("casbin")
public class CasbinProperties {
    /**
     * Default table name when storage strategy is JDBC
     */
    private String tableName = "casbin_rule";
    /**
     * Enable Casbin
     */
    private boolean enableCasbin = true;
    /**
     * Whether to use a synchronized Enforcer
     */
    private boolean useSyncedEnforcer = false;
    /**
     * Local model file
     */
    private String model = "classpath:casbin/model.conf";
    /**
     * Local policy file
     */
    private String policy = "classpath:casbin/policy.csv";
    /**
     * Storage strategy
     */
    private CasbinStoreType storeType = CasbinStoreType.JDBC;
    /**
     * Watcher synchronization strategy
     */
    private CasbinWatcherType watcherType = CasbinWatcherType.REDIS;
    /**
     * Redis topic for Watcher
     */
    private String policyTopic = "CASBIN_POLICY_TOPIC";
    /**
     * Data table initialization strategy
     */
    private CasbinDataSourceInitializationMode initializeSchema = CasbinDataSourceInitializationMode.CREATE;
    /**
     * Whether to use Watcher for strategy synchronization
     */
    private boolean enableWatcher = false;
    /**
     * The configuration will only take effect if the adapter supports this function
     * Can be manually switched through enforcer.enableAutoSave(true)
     */
    private boolean autoSave = true;
    /**
     * If the local model file address is not set or the file is not found in the default path
     * Use default rbac configuration
     */
    private boolean useDefaultModelIfModelNotSetting = true;

    public String getModelContext() {
        String text = FileUtils.getFileAsText(model);
        if (text == null) {
            throw new CasbinModelConfigNotFoundException();
        } else {
            return text;
        }
    }

    public InputStream getPolicyInputStream() {
        InputStream stream = FileUtils.getFileAsInputStream(policy);
        if (stream == null) {
            throw new CasbinPolicyConfigNotFoundException();
        } else {
            return stream;
        }
    }

    public boolean isEnableCasbin() {
        return enableCasbin;
    }

    public void setEnableCasbin(boolean enableCasbin) {
        this.enableCasbin = enableCasbin;
    }

    public boolean isUseSyncedEnforcer() {
        return useSyncedEnforcer;
    }

    public void setUseSyncedEnforcer(boolean useSyncedEnforcer) {
        this.useSyncedEnforcer = useSyncedEnforcer;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getPolicy() {
        return policy;
    }

    public void setPolicy(String policy) {
        this.policy = policy;
    }

    public CasbinStoreType getStoreType() {
        return storeType;
    }

    public void setStoreType(CasbinStoreType storeType) {
        this.storeType = storeType;
    }

    public CasbinWatcherType getWatcherType() {
        return watcherType;
    }

    public void setWatcherType(CasbinWatcherType watcherType) {
        this.watcherType = watcherType;
    }

    public CasbinDataSourceInitializationMode getInitializeSchema() {
        return initializeSchema;
    }

    public void setInitializeSchema(CasbinDataSourceInitializationMode initializeSchema) {
        this.initializeSchema = initializeSchema;
    }

    public boolean isEnableWatcher() {
        return enableWatcher;
    }

    public void setEnableWatcher(boolean enableWatcher) {
        this.enableWatcher = enableWatcher;
    }

    public boolean isAutoSave() {
        return autoSave;
    }

    public void setAutoSave(boolean autoSave) {
        this.autoSave = autoSave;
    }

    public boolean isUseDefaultModelIfModelNotSetting() {
        return useDefaultModelIfModelNotSetting;
    }

    public void setUseDefaultModelIfModelNotSetting(boolean useDefaultModelIfModelNotSetting) {
        this.useDefaultModelIfModelNotSetting = useDefaultModelIfModelNotSetting;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getPolicyTopic() {
        return policyTopic;
    }

    public void setPolicyTopic(String policyTopic) {
        this.policyTopic = policyTopic;
    }
}

