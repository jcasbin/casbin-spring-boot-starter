package org.casbin.spring.boot.autoconfigure.properties;

import lombok.Data;
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
@Data
@ConfigurationProperties("casbin")
public class CasbinProperties {
    /**
     * 启用Casbin
     */
    private boolean enableCasbin = true;
    /**
     * 是否使用同步的Enforcer
     */
    private boolean useSyncedEnforcer = false;
    /**
     * 本地model文件
     */
    private String model = "classpath:casbin/model.conf";
    /**
     * 本地policy文件
     */
    private String policy = "classpath:casbin/policy.csv";
    /**
     * 存储策略
     */
    private CasbinStoreType storeType = CasbinStoreType.JDBC;
    /**
     * Watcher同步策略
     */
    private CasbinWatcherType watcherType = CasbinWatcherType.REDIS;
    /**
     * 数据表初始化策略
     */
    private CasbinDataSourceInitializationMode initializeSchema = CasbinDataSourceInitializationMode.CREATE;
    /**
     * 是否使用Watcher进行策略同步
     */
    private boolean enableWatcher = false;
    /**
     * 仅在适配器支持该功能时配置才会生效
     * 可通过enforcer.enableAutoSave(true)手动切换
     */
    private boolean autoSave = true;
    /**
     * 如果未设置本地model文件地址或默认路径未找到文件
     * 使用默认rbac配置
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
}

