package org.casbin.exception;

/**
 * @author fangzhengjin
 * @version V1.0
 * @title CasbinModelConfigNotFoundException
 * @package org.casbin.exception
 * @description
 * @date 2019-4-05 14:46
 */
public class CasbinModelConfigNotFoundException extends RuntimeException {
    public CasbinModelConfigNotFoundException() {
    }

    public CasbinModelConfigNotFoundException(String message) {
        super(message);
    }

    public CasbinModelConfigNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public CasbinModelConfigNotFoundException(Throwable cause) {
        super(cause);
    }

    public CasbinModelConfigNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}