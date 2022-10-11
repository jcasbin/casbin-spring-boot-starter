package org.casbin.spring.boot.autoconfigure;

import org.casbin.adapter.JDBCAdapter;
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
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
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
@EnableConfigurationProperties({CasbinProperties.class, CasbinExceptionProperties.class, DataSourceProperties.class})
@AutoConfigureAfter({JdbcTemplateAutoConfiguration.class})
@ConditionalOnExpression("${casbin.enableCasbin:true}")
public class CasbinAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(CasbinAutoConfiguration.class);

    /**
     * Automatic configuration file storage adapter
     */
    @Bean
    @ConditionalOnProperty(name = "casbin.storeType", havingValue = "file")
    @ConditionalOnMissingBean
    public Adapter autoConfigFileAdapter(CasbinProperties properties) {
        // if the file storage is chosen and the policy file location is set correctly, then create a file adapter
        if (StringUtils.hasText(properties.getPolicy())) {
            try (InputStream policyInputStream = properties.getPolicyInputStream()) {
                return new FileAdapter(policyInputStream);
            } catch (Exception ignored) {
            }
        }
        throw new CasbinAdapterException("Cannot create file adapter, because policy file is not set");
    }

    /**
     * Automatic configuration of JDBC adapter
     */
    @Bean
    @ConditionalOnProperty(name = "casbin.storeType", havingValue = "jdbc", matchIfMissing = true)
    @ConditionalOnBean(JdbcTemplate.class)
    @ConditionalOnMissingBean
    public Adapter autoConfigJdbcAdapter(
            @CasbinDataSource ObjectProvider<DataSource> casbinDataSource,
            JdbcTemplate jdbcTemplate,
            CasbinProperties properties,
            CasbinExceptionProperties exceptionProperties,
            DataSourceProperties dataSourceProperties
    ) throws Exception {
        JdbcTemplate jdbcTemplateToUse = getJdbcTemplate(jdbcTemplate, casbinDataSource);
        String databaseName = getDatabaseName(jdbcTemplateToUse.getDataSource());
        CasbinDataSourceInitializationMode initializeSchema = properties.getInitializeSchema();
        boolean autoCreateTable = initializeSchema == CasbinDataSourceInitializationMode.CREATE;
        String tableName = properties.getTableName();
        logger.info("Casbin current use database product: {}", databaseName);
        return new JDBCAdapter(dataSourceProperties.determineDriverClassName(), dataSourceProperties.getUrl(),
                dataSourceProperties.getUsername(), dataSourceProperties.getPassword(),
                exceptionProperties.isRemovePolicyFailed(), tableName, autoCreateTable);

    }

    /**
     * Automatic configuration of the enforcer
     */
    @Bean
    @ConditionalOnMissingBean
    public Enforcer enforcer(CasbinProperties properties, Adapter adapter) {
        Model model = new Model();
        try {
            String modelContext = properties.getModelContext();
            model.loadModelFromText(modelContext);
        } catch (CasbinModelConfigNotFoundException e) {
            // if the local model file address is not set or the file is not found in the default path, the default rbac configuration is used
            if (!properties.isUseDefaultModelIfModelNotSetting()) {
                throw e;
            }
            logger.info("Can't found model config file, use default model config");
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
