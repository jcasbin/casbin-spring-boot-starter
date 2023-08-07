package org.casbin.exception;

/**
 * @author fangzhengjin
 * @version V1.0
 * @title CasbinAdapterException
 * @package org.casbin.exception
 * @description
 * @date 2019-4-05 14:46
 */
public class CasbinAdapterException extends RuntimeException {
    public CasbinAdapterException() {
    }

    public CasbinAdapterException(String message) {
        super(message);
    }

    public CasbinAdapterException(String message, Throwable cause) {
        super(message, cause);
    }

    public CasbinAdapterException(Throwable cause) {
        super(cause);
    }

    public CasbinAdapterException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}