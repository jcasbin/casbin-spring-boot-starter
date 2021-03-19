package org.casbin.spring.boot.autoconfigure.properties;

/**
 * @author fangzhengjin
 * @version V1.0
 * @title: CasbinWatcherType
 * @package org.casbin.spring.boot.autoconfigure.properties
 * @description:
 * @date 2019/4/2 15:31
 */
public enum CasbinWatcherType {
    /**
     * 使用Redis通知同步策略
     * 
     * Use Redis notification synchronization strategy
     */
    REDIS,
    JMS
}
