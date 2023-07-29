# Casbin Spring Boot Starter

[![Codecov branch](https://img.shields.io/codecov/c/github/jcasbin/casbin-spring-boot-starter/master.svg?logo=codecov&style=flat-square)](https://codecov.io/gh/jcasbin/casbin-spring-boot-starter)
[![GitHub Actions](https://github.com/jcasbin/casbin-spring-boot-starter/workflows/build/badge.svg)](https://github.com/jcasbin/casbin-spring-boot-starter/actions)
[![Maven Central](https://img.shields.io/maven-central/v/org.casbin/casbin-spring-boot-starter.svg?style=flat-square&color=brightgreen)](https://mvnrepository.com/artifact/org.casbin/casbin-spring-boot-starter/latest)
[![License](https://img.shields.io/github/license/jcasbin/casbin-spring-boot-starter.svg?style=flat-square&color=blue)](http://www.apache.org/licenses/LICENSE-2.0.txt)
[![SpringBootVersion](https://img.shields.io/badge/SpringBoot-2.3.5-heightgreen.svg?style=flat-square)](https://spring.io/projects/spring-boot)
[![JCasbinVersion](https://img.shields.io/badge/JCasbinVersion-1.9.2-heightgreen.svg?style=flat-square)](https://casbin.org)

[![](https://raw.githubusercontent.com/casbin/jcasbin/master/casbin-logo.png)](https://casbin.org)

Casbin Spring Boot Starter is designed to help you easily integrate [jCasbin](https://github.com/casbin/jcasbin) into
your Spring Boot project.

## how to use

1. Add ```casbin-spring-boot-starter``` to the Spring Boot project.

```Maven```

```xml

<dependency>
    <groupId>org.casbin</groupId>
    <artifactId>casbin-spring-boot-starter</artifactId>
    <version>version</version>
</dependency>
```

```Gradle```

```groovy
implementation 'org.casbin:casbin-spring-boot-starter:version'
```

2. Inject the Enforcer where you need to use it

```java

@Component
public class Test {
    @Autowired
    private Enforcer enforcer;
}
```

3. Add configuration

```yaml
casbin:
  #Whether to enable Casbin, it is enabled by default.
  enableCasbin: true
  #Whether to use thread-synchronized Enforcer, default false
  useSyncedEnforcer: false
  #Whether to enable automatic policy saving, if the adapter supports this function, it is enabled by default.
  autoSave: true
  #Storage type [file, jdbc], currently supported jdbc database [mysql (mariadb), h2, oracle, postgresql, db2]
  #Welcome to write and submit the jdbc adapter you are using, see: org.casbin.adapter.OracleAdapter
  #The jdbc adapter will actively look for the data source information you configured in spring.datasource
  #Default use jdbc, and use the built-in h2 database for memory storage
  storeType: jdbc
  #Customized policy table name when use jdbc, casbin_rule as default.
  tableName: casbin_rule
  #Data source initialization policy [create (automatically create data table, no longer initialized if created), never (always do not initialize)]
  initializeSchema: create
  #Local model configuration file address, the default reading location: classpath: casbin/model.conf
  model: classpath:casbin/model.conf
  #If the model configuration file is not found in the default location and casbin.model is not set correctly, the built-in default rbac model is used, which takes effect by default.
  useDefaultModelIfModelNotSetting: true
  #Local policy configuration file address, the default reading location: classpath: casbin/policy.csv
  #If the configuration file is not found in the default location, an exception will be thrown.
  #This configuration item takes effect only when casbin.storeType is set to file.
  policy: classpath:casbin/policy.csv
  #Whether to enable the CasbinWatcher mechanism, the default is not enabled.
  #If the mechanism is enabled, casbin.storeType must be jdbc, otherwise the configuration is invalid.
  enableWatcher: false
  #CasbinWatcher notification mode, defaults to use Redis for notification synchronization, temporarily only supports Redis
  #After opening Watcher, you need to manually add spring-boot-starter-data-redis dependency.
  watcherType: redis
  exception:
    ... See Schedule A for exception settings.
```

4. The simplest configuration

- Do not use other add-on configurations

```yaml
casbin:
  #If you are using a model profile at this address, no configuration is required
  model: classpath:casbin/model.conf
```

- Turn on Watcher

```yaml
casbin:
  #If the model profile you are using is located at this address, you do not need this configuration
  model: classpath:casbin/model.conf
  #When you open Watcher, the default use of RedisWatcher requires manual addition of spring-boot-starter-data-redis dependency.
  enableWatcher: true
```

5. Use custom independent data sources

- Only increase ```@CasbinDataSource``` annotation when injecting custom data source

```java

@Configuration
public class CasbinDataSourceConfiguration {
    @Bean
    @CasbinDataSource
    public DataSource casbinDataSource() {
        return DataSourceBuilder.create().url("jdbc:h2:mem:casbin").build();
    }
}
```

##### Schedule A

- ExceptionSettings(casbin.exception)

| name               | description                                      | default |
|--------------------|--------------------------------------------------|---------|
| removePolicyFailed | Throws an exception when the delete policy fails | false   |

##### Note: If you do not set another data source, or set the storage file location for H2, the data is stored in memory by default using H2.

#### Notice:

Since version 0.0.11, casbin-spring-boot-starter adds an id field to the database table structure by default.

The version before 0.0.11 is upgraded to version 0.0.11 and later requires the user to manually add the id field.

See https://github.com/jcasbin/casbin-spring-boot-starter/issues/21 for details
