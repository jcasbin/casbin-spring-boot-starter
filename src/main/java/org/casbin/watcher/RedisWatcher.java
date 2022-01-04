package org.casbin.watcher;

import org.casbin.jcasbin.persist.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.UUID;
import java.util.function.Consumer;

/**
 * @author fangzhengjin
 * @version V1.0
 * @title: RedisWatcher
 * @package org.casbin.watcher
 * @description:
 * @date 2019-4-06 1:58
 */
public class RedisWatcher implements Watcher {

    private Runnable updateCallback;
    private Consumer<String> consumer;
    private final String policyTopic;
    private final StringRedisTemplate stringRedisTemplate;
    private final static String REDIS_WATCHER_UUID = UUID.randomUUID().toString();
    private final static Logger logger = LoggerFactory.getLogger(RedisWatcher.class);

    public RedisWatcher(StringRedisTemplate stringRedisTemplate, String policyTopic) {
        this.policyTopic = policyTopic;
        this.stringRedisTemplate = stringRedisTemplate;
        logger.info("Current casbin redis watcher uuid: {}, subscribe topic: {}", REDIS_WATCHER_UUID, this.policyTopic);
    }

    @Override
    public void setUpdateCallback(Runnable runnable) {
        updateCallback = runnable;
    }

    @Override
    public void setUpdateCallback(Consumer<String> func) {
        this.consumer = func;
    }

    @Override
    public void update() {
        stringRedisTemplate.convertAndSend(
                this.policyTopic,
                "Casbin policy has a new version from redis watcher: " + REDIS_WATCHER_UUID
        );
    }

    public void updatePolicy(String message) {
        if (message.contains(REDIS_WATCHER_UUID)) {
            logger.info("This casbin policy update notification comes from the current redis watcher instance: {}", REDIS_WATCHER_UUID);
            return;
        } else {
            logger.info(message);
        }

        updateCallback.run();
        if (consumer != null) {
            consumer.accept(message);
        }
        logger.info("Casbin policy updated.");
    }
}
