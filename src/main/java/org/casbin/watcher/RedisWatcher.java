package org.casbin.watcher;

import lombok.extern.slf4j.Slf4j;
import org.casbin.jcasbin.persist.Watcher;
import org.casbin.spring.boot.autoconfigure.CasbinRedisWatcherAutoConfiguration;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.UUID;

/**
 * @author fangzhengjin
 * @version V1.0
 * @title: RedisWatcher
 * @package org.casbin.watcher
 * @description:
 * @date 2019-4-06 1:58
 */
@Slf4j
public class RedisWatcher implements Watcher {
    private Runnable updateCallback;
    private StringRedisTemplate stringRedisTemplate;
    private final static String REDIS_WATCHER_UUID = UUID.randomUUID().toString();

    public RedisWatcher(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
        logger.info("Current casbin redis watcher uuid: {}", REDIS_WATCHER_UUID);
    }

    @Override
    public void setUpdateCallback(Runnable runnable) {
        updateCallback = runnable;
    }

    @Override
    public void update() {
        stringRedisTemplate.convertAndSend(
                CasbinRedisWatcherAutoConfiguration.CASBIN_POLICY_TOPIC,
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
        logger.info("Casbin policy updated.");
    }
}
