package org.casbin.spring.boot.autoconfigure.properties;

/**
 * @author shingmoyeung
 * @version V1.0
 * @title CasbinWatcherLettuceRedisType
 * @package org.casbin.spring.boot.autoconfigure.properties
 * @description
 * @date 2023/8/7 08:42
 */
public enum CasbinWatcherLettuceRedisType {
    /**
     * Redis Type. [NONE] use Jedis ,not Lettuce.
     */
    NONE,
    STANDALONE,
    CLUSTER
}