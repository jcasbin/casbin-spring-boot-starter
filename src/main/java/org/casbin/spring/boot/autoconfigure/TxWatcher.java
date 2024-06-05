package org.casbin.spring.boot.autoconfigure;

import org.casbin.jcasbin.persist.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionSynchronization;

import java.util.function.Consumer;

import static org.springframework.transaction.support.TransactionSynchronizationManager.isActualTransactionActive;
import static org.springframework.transaction.support.TransactionSynchronizationManager.registerSynchronization;

public class TxWatcher implements Watcher {
    private final static Logger logger = LoggerFactory.getLogger(TxWatcher.class);
    private final Watcher watcher;

    public TxWatcher(Watcher watcher) {
        logger.info("TxWatcher proxy watcher: {}", watcher.getClass().getName());
        this.watcher = watcher;
    }

    @Override
    public void setUpdateCallback(Runnable runnable) {
        this.watcher.setUpdateCallback(runnable);
    }

    @Override
    public void setUpdateCallback(Consumer<String> consumer) {
        this.watcher.setUpdateCallback(consumer);
    }

    @Override
    public void update() {
        if (isActualTransactionActive()) {
            registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    watcher.update();
                }
            });
        } else {
            watcher.update();
        }
    }
}
