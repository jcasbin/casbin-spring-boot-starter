package org.casbin.spring.boot.autoconfigure.properties;

/**
 * @author fangzhengjin
 * @version V1.0
 * @title CasbinDataSourceInitializationMode
 * @package org.casbin.spring.boot.autoconfigure.properties
 * @description
 * @date 2019/4/2 15:31
 */
public enum CasbinDataSourceInitializationMode {
    /**
     * Automatically create data tables
     */
    CREATE,
    /**
     * Do not automatically build tables
     */
    NEVER
}
