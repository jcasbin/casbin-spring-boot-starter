package org.casbin.spring.boot.autoconfigure;

import org.casbin.exception.CasbinWatcherLettuceTypeUnsupportedException;
import org.casbin.jcasbin.main.Enforcer;
import org.casbin.jcasbin.persist.Watcher;
import org.casbin.spring.boot.autoconfigure.properties.CasbinProperties;
import org.casbin.watcher.RedisWatcher;
import org.casbin.watcher.lettuce.LettuceRedisWatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * @author fangzhengjin
 * @version V1.0
 * @title CasbinRedisWatcherAutoConfiguration
 * @package org.casbin.spring.boot.autoconfigure
 * @description
 * @date 2019-4-05 13:53
 */
@Configuration
@EnableConfigurationProperties({CasbinProperties.class, RedisProperties.class})
@AutoConfigureAfter({RedisAutoConfiguration.class, CasbinAutoConfiguration.class})
@ConditionalOnExpression("'jdbc'.equalsIgnoreCase('${casbin.store-type:jdbc}') && ${casbin.enable-watcher:false} && 'redis'.equalsIgnoreCase('${casbin.watcher-type:redis}') ")
public class CasbinRedisWatcherAutoConfiguration {

    private final static Logger logger = LoggerFactory.getLogger(CasbinRedisWatcherAutoConfiguration.class);

    @Bean
    @ConditionalOnBean(RedisTemplate.class)
    @ConditionalOnMissingBean
    @ConditionalOnExpression("'redis'.equalsIgnoreCase('${casbin.watcher-type:redis}') && '${casbin.watcher-lettuce-redis-type:none}'.equalsIgnoreCase('none')")
    public Watcher redisWatcher(RedisProperties redisProperties, CasbinProperties casbinProperties, Enforcer enforcer) {
        int timeout = redisProperties.getTimeout() != null ? (int) redisProperties.getTimeout().toMillis() : 2000;
        RedisWatcher watcher = new RedisWatcher(redisProperties.getHost(), redisProperties.getPort(),
                casbinProperties.getPolicyTopic(), timeout, redisProperties.getPassword());
        enforcer.setWatcher(watcher);
        logger.info("Casbin set watcher: {}", watcher.getClass().getName());
        return watcher;
    }

    @Bean
    @ConditionalOnBean(RedisTemplate.class)
    @ConditionalOnMissingBean
    @ConditionalOnExpression("'redis'.equalsIgnoreCase('${casbin.watcher-type:redis}') && ('${casbin.watcher-lettuce-redis-type:standalone}'.equalsIgnoreCase('standalone') || '${casbin.watcher-lettuce-redis-type:cluster}'.equalsIgnoreCase('cluster'))")
    public Watcher lettuceRedisWatcher(RedisProperties redisProperties, CasbinProperties casbinProperties, Enforcer enforcer) {
        int timeout = redisProperties.getTimeout() != null ? (int) redisProperties.getTimeout().toMillis() : 2000;
        if (casbinProperties.getWatcherLettuceRedisType().name().equalsIgnoreCase("standalone")) {
            LettuceRedisWatcher lettuceRedisWatcher = new LettuceRedisWatcher(redisProperties.getHost(), redisProperties.getPort(),
                    casbinProperties.getPolicyTopic(), timeout, redisProperties.getPassword());
            enforcer.setWatcher(lettuceRedisWatcher);
            logger.info("Casbin set watcher: {}", lettuceRedisWatcher.getClass().getName());
            return lettuceRedisWatcher;
        } else if (casbinProperties.getWatcherLettuceRedisType().name().equalsIgnoreCase("cluster")) {
            LettuceRedisWatcher lettuceRedisWatcher = new LettuceRedisWatcher(String.join(",", redisProperties.getCluster().getNodes()),
                    casbinProperties.getPolicyTopic(), timeout, redisProperties.getPassword());
            enforcer.setWatcher(lettuceRedisWatcher);
            logger.info("Casbin set watcher: {}", lettuceRedisWatcher.getClass().getName());
            return lettuceRedisWatcher;
        } else {
            // Unsupported watcher type. eg: sentinel etc.
            throw new CasbinWatcherLettuceTypeUnsupportedException("Unsupported watcher type!");
        }
    }
}