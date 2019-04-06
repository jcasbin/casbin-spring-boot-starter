package org.casbin.spring.boot.autoconfigure.properties;

import lombok.Data;
import org.casbin.exception.CasbinModelConfigNotFoundException;
import org.casbin.exception.CasbinPolicyConfigNotFoundException;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.ResourceUtils;

import java.io.FileNotFoundException;

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

    public String getModelRealPath() {
        try {
            return ResourceUtils.getURL(model).getPath();
        } catch (FileNotFoundException e) {
            throw new CasbinModelConfigNotFoundException(e.getMessage(), e.getCause());
        }
    }

    public String getPolicyRealPath() {
        try {
            return ResourceUtils.getURL(policy).getPath();
        } catch (FileNotFoundException e) {
            throw new CasbinPolicyConfigNotFoundException(e.getMessage(), e.getCause());
        }
    }
}

