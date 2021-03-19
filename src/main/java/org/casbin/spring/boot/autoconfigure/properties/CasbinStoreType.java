package org.casbin.spring.boot.autoconfigure.properties;

/**
 * @author fangzhengjin
 * @version V1.0
 * @title: CasbinStoreType
 * @package org.casbin.spring.boot.autoconfigure.properties
 * @description:
 * @date 2019/4/2 15:31
 */
public enum CasbinStoreType {
    /**
     * 使用文件进行存储
     * 
     * Use files for storage
     */
    FILE,
    /**
     * 使用JDBC进行存储
     * 
     * Use JDBC for storage
     */
    JDBC
}
