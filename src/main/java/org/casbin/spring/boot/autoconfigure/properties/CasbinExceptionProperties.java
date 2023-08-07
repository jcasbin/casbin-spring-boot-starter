package org.casbin.spring.boot.autoconfigure.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author fangzhengjin
 * @version V1.0
 * @title CasbinExceptionProperties
 * @package org.casbin.spring.boot.autoconfigure.properties
 * @description
 * @date 2019/9/24 15:25
 */
@ConfigurationProperties("casbin.exception")
public class CasbinExceptionProperties {

    /**
     * Whether to throw an exception when the delete strategy fails
     */
    private boolean removePolicyFailed = false;

    public boolean isRemovePolicyFailed() {
        return removePolicyFailed;
    }

    public void setRemovePolicyFailed(boolean removePolicyFailed) {
        this.removePolicyFailed = removePolicyFailed;
    }
}

