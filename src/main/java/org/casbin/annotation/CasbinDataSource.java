package org.casbin.annotation;

import org.springframework.beans.factory.annotation.Qualifier;

import java.lang.annotation.*;

/**
 * Qualifier annotation for a DataSource to be injected into Casbin auto-configuration.
 * Can be used on a secondary data source, if there is another one marked as
 * {@code @Primary}.
 *
 * @author fangzhengjin
 * @date 2021-03-28
 */

@Target({ ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.TYPE, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Qualifier
public @interface CasbinDataSource {
}
