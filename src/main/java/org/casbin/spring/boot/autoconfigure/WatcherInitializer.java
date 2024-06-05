package org.casbin.spring.boot.autoconfigure;

import org.casbin.jcasbin.main.Enforcer;
import org.casbin.jcasbin.persist.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class WatcherInitializer implements InitializingBean {
    private final static Logger logger = LoggerFactory.getLogger(WatcherInitializer.class);
    private final Enforcer enforcer;
    private final Watcher watcher;

    public WatcherInitializer(Enforcer enforcer, Watcher watcher) {
        this.enforcer = enforcer;
        this.watcher = watcher;
    }

    @Override
    public void afterPropertiesSet() {
        logger.info("Casbin set watcher: {}", watcher.getClass().getName());

        enforcer.setWatcher(watcher);
    }
}
