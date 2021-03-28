package org.casbin.spring.boot.autoconfigure;

import org.casbin.adapter.DB2Adapter;
import org.casbin.adapter.JdbcAdapter;
import org.casbin.adapter.OracleAdapter;
import org.casbin.annotation.CasbinDataSource;
import org.casbin.exception.CasbinAdapterException;
import org.casbin.exception.CasbinModelConfigNotFoundException;
import org.casbin.jcasbin.main.Enforcer;
import org.casbin.jcasbin.main.SyncedEnforcer;
import org.casbin.jcasbin.model.Model;
import org.casbin.jcasbin.persist.Adapter;
import org.casbin.jcasbin.persist.file_adapter.FileAdapter;
import org.casbin.spring.boot.autoconfigure.properties.CasbinDataSourceInitializationMode;
import org.casbin.spring.boot.autoconfigure.properties.CasbinExceptionProperties;
import org.casbin.spring.boot.autoconfigure.properties.CasbinProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
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
import java.io.InputStream;
import java.sql.DatabaseMetaData;

/**
 * @author fangzhengjin
 * @version V1.0
 * @title: CasbinAutoConfiguration
 * @package org.casbin.spring.boot.autoconfigure
 * @description:
 * @date 2019-4-05 13:53
 */

@Configuration
@EnableConfigurationProperties({CasbinProperties.class, CasbinExceptionProperties.class})
@AutoConfigureAfter({JdbcTemplateAutoConfiguration.class})
@ConditionalOnExpression("${casbin.enableCasbin:true}")
public class CasbinAutoConfiguration {

    private final static Logger logger = LoggerFactory.getLogger(CasbinAutoConfiguration.class);

    /**
     * 自动配置文件存储适配器
     * <p>
     * Automatic configuration file storage adapter
     */
    @Bean
    @ConditionalOnProperty(name = "casbin.storeType", havingValue = "file")
    @ConditionalOnMissingBean
    public Adapter autoConfigFileAdapter(CasbinProperties properties) {
        // 选择使用文件存储并正确设置了policy文件位置，则创建文件适配器
        if (!StringUtils.isEmpty(properties.getPolicy())) {
            try (InputStream policyInputStream = properties.getPolicyInputStream()) {
                return new FileAdapter(policyInputStream);
            } catch (Exception ignored) {
            }
        }
        throw new CasbinAdapterException("Cannot create file adapter, because policy file is not set");
    }

    /**
     * 自动配置JDBC适配器
     * <p>
     * Automatic configuration of JDBC adapter
     */
    @Bean
    @ConditionalOnProperty(name = "casbin.storeType", havingValue = "jdbc", matchIfMissing = true)
    @ConditionalOnBean(JdbcTemplate.class)
    @ConditionalOnMissingBean
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public Adapter autoConfigJdbcAdapter(
            @CasbinDataSource ObjectProvider<DataSource> casbinDataSource,
            JdbcTemplate jdbcTemplate,
            CasbinProperties properties,
            CasbinExceptionProperties exceptionProperties
    ) {
        JdbcTemplate jdbcTemplateToUse = getJdbcTemplate(jdbcTemplate, casbinDataSource);
        String databaseName = getDatabaseName(jdbcTemplateToUse.getDataSource());
        CasbinDataSourceInitializationMode initializeSchema = properties.getInitializeSchema();
        boolean autoCreateTable = initializeSchema == CasbinDataSourceInitializationMode.CREATE;
        logger.info("Casbin current use database product: {}", databaseName);
        switch (databaseName) {
            case "mysql":
            case "h2":
            case "postgresql":
                return new JdbcAdapter(jdbcTemplateToUse, exceptionProperties, autoCreateTable);
            case "oracle":
                return new OracleAdapter(jdbcTemplateToUse, exceptionProperties, autoCreateTable);
            case "db2":
                return new DB2Adapter(jdbcTemplateToUse, exceptionProperties, autoCreateTable);
            default:
                throw new CasbinAdapterException("Can't find " + databaseName + " jdbc adapter");
        }
    }

    /**
     * 自动配置enforcer
     * <p>
     * Automatic configuration of the enforcer
     */
    @Bean
    @ConditionalOnMissingBean
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public Enforcer enforcer(CasbinProperties properties, Adapter adapter) {
        Model model = new Model();
        try {
            String modelContext = properties.getModelContext();
            model.loadModelFromText(modelContext);
        } catch (CasbinModelConfigNotFoundException e) {
            /*
             *  如果未设置本地model文件地址或默认路径未找到文件,使用默认rbac配置
             *
             *  If the local model file address is not set or the file is not found in the default path,
             *  the default rbac configuration is used
             */
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
        Enforcer enforcer;
        if (properties.isUseSyncedEnforcer()) {
            enforcer = new SyncedEnforcer(model, adapter);
            logger.info("Casbin use SyncedEnforcer");
        } else {
            enforcer = new Enforcer(model, adapter);
        }
        enforcer.enableAutoSave(properties.isAutoSave());
        return enforcer;
    }

    /**
     * 获取当前使用数据库类型
     * <p>
     * Get the current database type
     */
    private static String getDatabaseName(DataSource dataSource) {
        try {
            String productName = JdbcUtils.
                    commonDatabaseName(JdbcUtils.extractDatabaseMetaData(
                            dataSource, DatabaseMetaData::getDatabaseProductName
                    ));
            DatabaseDriver databaseDriver = DatabaseDriver.fromProductName(productName);
            if (databaseDriver == DatabaseDriver.UNKNOWN) {
                throw new IllegalStateException("Unable to detect database type");
            }
            return databaseDriver.getId();
        } catch (MetaDataAccessException ex) {
            throw new IllegalStateException("Unable to detect database type", ex);
        }
    }

    private static JdbcTemplate getJdbcTemplate(JdbcTemplate jdbcTemplate, ObjectProvider<DataSource> dataSource) {
        DataSource dataSourceIfAvailable = dataSource.getIfAvailable();
        if (dataSourceIfAvailable != null) {
            logger.info("Discover the custom Casbin data source.");
            return new JdbcTemplate(dataSourceIfAvailable);
        } else {
            logger.info("Casbin is using the data source managed by Spring.");
            return jdbcTemplate;
        }
    }
}
