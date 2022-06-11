package org.casbin.spring.boot.autoconfigure;

import org.casbin.jcasbin.main.Enforcer;
import org.casbin.jcasbin.persist.Watcher;
import org.casbin.spring.boot.autoconfigure.properties.CasbinProperties;
import org.casbin.watcher.RedisWatcher;
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
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

/**
 * @author fangzhengjin
 * @version V1.0
 * @title: CasbinRedisWatcherAutoConfiguration
 * @package org.casbin.spring.boot.autoconfigure
 * @description:
 * @date 2019-4-05 13:53
 */

@Configuration
@EnableConfigurationProperties({CasbinProperties.class, RedisProperties.class})
@AutoConfigureAfter({RedisAutoConfiguration.class, CasbinAutoConfiguration.class})
@ConditionalOnExpression("'jdbc'.equalsIgnoreCase('${casbin.storeType:jdbc}') && ${casbin.enableWatcher:false} && 'redis'.equalsIgnoreCase('${casbin.watcherType:redis}') ")
public class CasbinRedisWatcherAutoConfiguration {

    private final static Logger logger = LoggerFactory.getLogger(CasbinRedisWatcherAutoConfiguration.class);

    @Bean
    @ConditionalOnBean(RedisTemplate.class)
    @ConditionalOnMissingBean
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public Watcher redisWatcher(RedisProperties redisProperties, Enforcer enforcer) {
        int timeout = 2000;
        if (redisProperties.getTimeout() != null) {
            timeout = (int) redisProperties.getTimeout().toMillis();
        }
        RedisWatcher watcher = new RedisWatcher(redisProperties.getHost(), redisProperties.getPort(), redisProperties.getClientName(),
                timeout, redisProperties.getPassword());
        enforcer.setWatcher(watcher);
        logger.info("Casbin set watcher: {}", watcher.getClass().getName());
        return watcher;

    }
}
