package org.casbin.spring.boot.autoconfigure.properties;

/**
 * @author fangzhengjin
 * @version V1.0
 * @title CasbinWatcherType
 * @package org.casbin.spring.boot.autoconfigure.properties
 * @description
 * @date 2019/4/2 15:31
 */
public enum CasbinWatcherType {
    /**
     * Use Redis notification synchronization strategy
     */
    REDIS,
    JMS
}
