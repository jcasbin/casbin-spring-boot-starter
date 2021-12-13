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
@EnableConfigurationProperties(CasbinProperties.class)
@AutoConfigureAfter({RedisAutoConfiguration.class, CasbinAutoConfiguration.class})
@ConditionalOnExpression("'jdbc'.equalsIgnoreCase('${casbin.storeType:jdbc}') && ${casbin.enableWatcher:false} && 'redis'.equalsIgnoreCase('${casbin.watcherType:redis}') ")
public class CasbinRedisWatcherAutoConfiguration {

    private final static Logger logger = LoggerFactory.getLogger(CasbinRedisWatcherAutoConfiguration.class);

    @Bean
    @ConditionalOnBean(RedisTemplate.class)
    @ConditionalOnMissingBean
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public Watcher redisWatcher(StringRedisTemplate stringRedisTemplate, Enforcer enforcer, CasbinProperties casbinProperties) {
        RedisWatcher watcher = new RedisWatcher(stringRedisTemplate, casbinProperties.getPolicyTopic());
        enforcer.setWatcher(watcher);
        logger.info("Casbin set watcher: {}", watcher.getClass().getName());
        return watcher;
    }

    /**
     * Message listener adapter, bind message processor,
     * use reflection technology to call the business method of message processor
     */
    @Bean
    @ConditionalOnMissingBean
    public MessageListenerAdapter messageListenerAdapter(Watcher receiver) {
        return new MessageListenerAdapter(receiver, "updatePolicy");
    }

    /**
     * Redis message listener container
     * You can add multiple redis listeners that monitor different topics.
     * You only need to bind the message listener to the corresponding message subscription processor.
     * The message listener use reflection technology to call the relevant methods of the message subscription processor to perform some business processing
     */
    @Bean
    @ConditionalOnMissingBean
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            MessageListenerAdapter listenerAdapter,
            CasbinProperties casbinProperties
    ) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        // subscribe to the CASBIN_POLICY_TOPIC channel
        container.addMessageListener(listenerAdapter, new ChannelTopic(casbinProperties.getPolicyTopic()));
        return container;
    }
}
