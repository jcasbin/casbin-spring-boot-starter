package org.casbin.watcher;

import lombok.extern.slf4j.Slf4j;
import org.casbin.jcasbin.persist.Watcher;
import org.casbin.spring.boot.autoconfigure.CasbinRedisWatcherAutoConfiguration;
import org.springframework.data.redis.core.StringRedisTemplate;

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

    public RedisWatcher(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public void setUpdateCallback(Runnable runnable) {
        updateCallback = runnable;
    }

    @Override
    public void update() {
        stringRedisTemplate.convertAndSend(
                CasbinRedisWatcherAutoConfiguration.CASBIN_POLICY_TOPIC,
                "Casbin policy has a new version!"
        );
    }

    public void updatePolicy(String message) {
        logger.info(message);
        updateCallback.run();
        logger.info("Casbin policy updated.");
    }
}
