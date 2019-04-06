package org.casbin.spring.boot.autoconfigure;

import lombok.extern.slf4j.Slf4j;
import org.casbin.jcasbin.persist.Watcher;
import org.casbin.spring.boot.autoconfigure.properties.CasbinProperties;
import org.casbin.watcher.RedisWatcher;
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

@Slf4j
@Configuration
@EnableConfigurationProperties(CasbinProperties.class)
@AutoConfigureAfter({RedisAutoConfiguration.class})
@ConditionalOnExpression("${casbin.enableWatcher:true} && '${casbin.watcherType}'.equalsIgnoreCase('redis') ")
public class CasbinRedisWatcherAutoConfiguration {

    public final static String CASBIN_POLICY_TOPIC = "CASBIN_POLICY_TOPIC";

    @Bean
    @ConditionalOnBean(RedisTemplate.class)
    @ConditionalOnMissingBean
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public Watcher redisWatcher(StringRedisTemplate stringRedisTemplate) {
        return new RedisWatcher(stringRedisTemplate);
    }

    /**
     * 消息监听器适配器，绑定消息处理器，利用反射技术调用消息处理器的业务方法
     */
    @Bean
    @ConditionalOnMissingBean
    public MessageListenerAdapter messageListenerAdapter(Watcher receiver) {
        return new MessageListenerAdapter(receiver, "updatePolicy");
    }

    /**
     * redis消息监听器容器
     * 可以添加多个监听不同话题的redis监听器，只需要把消息监听器和相应的消息订阅处理器绑定，该消息监听器
     * 通过反射技术调用消息订阅处理器的相关方法进行一些业务处理
     */
    @Bean
    @ConditionalOnMissingBean
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            MessageListenerAdapter listenerAdapter
    ) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        //订阅CASBIN_POLICY_TOPIC通道
        container.addMessageListener(listenerAdapter, new ChannelTopic(CASBIN_POLICY_TOPIC));
        return container;
    }
}
