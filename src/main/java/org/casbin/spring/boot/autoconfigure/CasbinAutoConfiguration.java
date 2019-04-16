package org.casbin.spring.boot.autoconfigure;

import lombok.extern.slf4j.Slf4j;
import org.casbin.adapter.JdbcAdapter;
import org.casbin.adapter.OracleAdapter;
import org.casbin.exception.CasbinAdapterException;
import org.casbin.exception.CasbinModelConfigNotFoundException;
import org.casbin.jcasbin.main.Enforcer;
import org.casbin.jcasbin.model.Model;
import org.casbin.jcasbin.persist.Adapter;
import org.casbin.jcasbin.persist.Watcher;
import org.casbin.jcasbin.persist.file_adapter.FileAdapter;
import org.casbin.spring.boot.autoconfigure.properties.CasbinDataSourceInitializationMode;
import org.casbin.spring.boot.autoconfigure.properties.CasbinProperties;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.DatabaseDriver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.MetaDataAccessException;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.util.List;

/**
 * @author fangzhengjin
 * @version V1.0
 * @title: CasbinAutoConfiguration
 * @package org.casbin.spring.boot.autoconfigure
 * @description:
 * @date 2019-4-05 13:53
 */

@Slf4j
@Configuration
@EnableConfigurationProperties(CasbinProperties.class)
@AutoConfigureAfter({JdbcTemplateAutoConfiguration.class, CasbinRedisWatcherAutoConfiguration.class})
@ConditionalOnExpression("${casbin.enableCasbin:true}")
public class CasbinAutoConfiguration {

    /**
     * 自动配置文件存储适配器
     */
    @Bean
    @ConditionalOnProperty(name = "casbin.storeType", havingValue = "file")
    @ConditionalOnMissingBean
    public Adapter autoConfigFileAdapter(CasbinProperties properties) {
        // 选择使用文件存储并正确设置了policy文件位置，则创建文件适配器
        if (!StringUtils.isEmpty(properties.getPolicy())) {
            String policyPath = properties.getPolicyRealPath();
            return new FileAdapter(policyPath);
        }
        throw new CasbinAdapterException("Cannot create file adapter, because policy file is not set");
    }

    /**
     * 自动配置JDBC适配器
     */
    @Bean
    @ConditionalOnProperty(name = "casbin.storeType", havingValue = "jdbc", matchIfMissing = true)
    @ConditionalOnBean(JdbcTemplate.class)
    @ConditionalOnMissingBean
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public Adapter autoConfigJdbcAdapter(JdbcTemplate jdbcTemplate, CasbinProperties properties) {
        String databaseName = getDatabaseName(jdbcTemplate.getDataSource());
        CasbinDataSourceInitializationMode initializeSchema = properties.getInitializeSchema();
        boolean autoCreateTable = initializeSchema == CasbinDataSourceInitializationMode.CREATE;
        switch (databaseName) {
            case "mysql":
            case "h2":
            case "postgresql":
                return new JdbcAdapter(jdbcTemplate, autoCreateTable);
            case "oracle":
                return new OracleAdapter(jdbcTemplate, autoCreateTable);
            default:
                throw new CasbinAdapterException("Can't find " + databaseName + " jdbc adapter");
        }
    }

    /**
     * 自动配置enforcer
     */
    @Bean
    @ConditionalOnMissingBean
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public Enforcer enforcer(CasbinProperties properties, Adapter adapter, List<Watcher> watchers) {
        Model model = new Model();
        try {
            String modelRealPath = properties.getModelRealPath();
            model.loadModel(modelRealPath);
        } catch (CasbinModelConfigNotFoundException e) {
            // 如果未设置本地model文件地址或默认路径未找到文件,使用默认rbac配置
            if (!properties.isUseDefaultModelIfModelNotSetting()) {
                throw e;
            }
            logger.info("Con't found model config file, use default model config");
            // request definition
            model.addDef("r", "r", "sub, obj, act");
            // policy definition
            model.addDef("p", "p", "sub, obj, act");
            // role definition
            model.addDef("g", "g", "_, _");
            // policy effect
            model.addDef("e", "e", "some(where (p.eft == allow))");
            // matchers
            model.addDef("m", "m", "g(r.sub, p.sub) && r.obj == p.obj && r.act == p.act");
        }
        Enforcer enforcer = new Enforcer(model, adapter);
        enforcer.enableAutoSave(properties.isAutoSave());

        if (watchers.size() > 0) {
            Watcher watcher = watchers.get(0);
            logger.info("Casbin set watcher: {}", watcher.getClass().getName());
            enforcer.setWatcher(watcher);
        }

        return enforcer;
    }

    /**
     * 获取当前使用数据库类型
     */
    private static String getDatabaseName(DataSource dataSource) {
        try {
            String productName = JdbcUtils.
                    commonDatabaseName(JdbcUtils.extractDatabaseMetaData(
                            dataSource, "getDatabaseProductName"
                    ).toString());
            DatabaseDriver databaseDriver = DatabaseDriver.fromProductName(productName);
            if (databaseDriver == DatabaseDriver.UNKNOWN) {
                throw new IllegalStateException("Unable to detect database type");
            }
            return databaseDriver.getId();
        } catch (MetaDataAccessException ex) {
            throw new IllegalStateException("Unable to detect database type", ex);
        }
    }
}
