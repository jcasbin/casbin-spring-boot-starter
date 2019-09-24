package org.casbin.spring.boot.autoconfigure.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author fangzhengjin
 * @version V1.0
 * @title: CasbinExceptionProperties
 * @package org.casbin.spring.boot.autoconfigure.properties
 * @description: 异常设置
 * @date 2019/9/24 15:25
 */
@Data
@ConfigurationProperties("casbin.exception")
public class CasbinExceptionProperties {

    /**
     * 删除策略失败时是否抛出异常
     */
    private boolean removePolicyFailed = false;

}

