package org.casbin.exception;

/**
 * @author shingmoyeung
 * @version V1.0
 * @title CasbinWatcherLettuceTypeUnsupportedException
 * @package org.casbin.exception
 * @description
 * @date 2023-8-8 14:04
 */
public class CasbinWatcherLettuceTypeUnsupportedException extends RuntimeException {
    /**
     * Constructor
     */
    public CasbinWatcherLettuceTypeUnsupportedException() {
    }

    /**
     * Constructor
     *
     * @param message message
     */
    public CasbinWatcherLettuceTypeUnsupportedException(String message) {
        super(message);
    }

    /**
     * Constructor
     *
     * @param message message
     * @param cause   cause
     */
    public CasbinWatcherLettuceTypeUnsupportedException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor
     *
     * @param cause cause
     */
    public CasbinWatcherLettuceTypeUnsupportedException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructor
     *
     * @param message            message
     * @param cause              cause
     * @param enableSuppression  enableSuppression
     * @param writableStackTrace writableStackTrace
     */
    public CasbinWatcherLettuceTypeUnsupportedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}