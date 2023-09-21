package org.casbin.exception;

/**
 * @author fangzhengjin
 * @version V1.0
 * @title CasbinPolicyConfigNotFoundException
 * @package org.casbin.exception
 * @description
 * @date 2019-4-05 14:46
 */
public class CasbinPolicyConfigNotFoundException extends RuntimeException {
    public CasbinPolicyConfigNotFoundException() {
    }

    public CasbinPolicyConfigNotFoundException(String message) {
        super(message);
    }

    public CasbinPolicyConfigNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public CasbinPolicyConfigNotFoundException(Throwable cause) {
        super(cause);
    }

    public CasbinPolicyConfigNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}