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
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.data.redis.autoconfigure.DataRedisAutoConfiguration;
import org.springframework.boot.data.redis.autoconfigure.DataRedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
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
@EnableConfigurationProperties({CasbinProperties.class, DataRedisProperties.class})
@AutoConfigureAfter({DataRedisAutoConfiguration.class, CasbinAutoConfiguration.class})
@ConditionalOnExpression("'jdbc'.equalsIgnoreCase('${casbin.store-type:jdbc}') && ${casbin.enable-watcher:false} && 'redis'.equalsIgnoreCase('${casbin.watcher-type:redis}') ")
public class CasbinRedisWatcherAutoConfiguration {

    private final static Logger logger = LoggerFactory.getLogger(CasbinRedisWatcherAutoConfiguration.class);

    @Bean
    @ConditionalOnBean(RedisTemplate.class)
    @ConditionalOnMissingBean
    @ConditionalOnExpression("'redis'.equalsIgnoreCase('${casbin.watcher-type:redis}') && '${casbin.watcher-lettuce-redis-type:none}'.equalsIgnoreCase('none')")
    public Watcher redisWatcher(DataRedisProperties redisProperties, CasbinProperties casbinProperties, Enforcer enforcer) {
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
    public Watcher lettuceRedisWatcher(DataRedisProperties redisProperties, CasbinProperties casbinProperties, Enforcer enforcer) {
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

    @Bean
    @Primary
    @ConditionalOnBean(Watcher.class)
    @ConditionalOnProperty(value = "casbin.watcher-tx-support", havingValue = "true")
    public Watcher txWatcher(Watcher watcher, Enforcer enforcer) {
        TxWatcher txWatcher = new TxWatcher(watcher);
        enforcer.setWatcher(txWatcher);
        logger.info("TxWatcher proxy watcher: {}", watcher.getClass().getName());
        return txWatcher;
    }
}
