package org.casbin.spring.boot.autoconfigure.properties;

/**
 * @author fangzhengjin
 * @version V1.0
 * @title: CasbinDataSourceInitializationMode
 * @package org.casbin.spring.boot.autoconfigure.properties
 * @description:
 * @date 2019/4/2 15:31
 */
public enum CasbinDataSourceInitializationMode {
    /**
     * 自动创建数据表
     * 
     * Automatically create data tables
     */
    CREATE,
    /**
     * 启动时不自动建表
     * 
     * Do not automatically build tables
     */
    NEVER
}
