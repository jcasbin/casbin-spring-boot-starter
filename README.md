# Casbin Spring Boot Starter

[![Codecov branch](https://img.shields.io/codecov/c/github/jcasbin/casbin-spring-boot-starter/master.svg?logo=codecov&style=flat-square)](https://codecov.io/gh/jcasbin/casbin-spring-boot-starter)
[![Build Status](https://img.shields.io/travis/com/jcasbin/casbin-spring-boot-starter/master.svg?style=flat-square)](https://travis-ci.com/jcasbin/casbin-spring-boot-starter)
[![Maven Central](https://img.shields.io/maven-central/v/org.casbin/casbin-spring-boot-starter.svg?style=flat-square&color=brightgreen)](https://maven-badges.herokuapp.com/maven-central/org.casbin/casbin-spring-boot-starter/)
[![Bintray](https://img.shields.io/bintray/v/casbin/maven/casbin-spring-boot-starter.svg?style=flat-square&color=blue)](https://bintray.com/casbin/maven/casbin-spring-boot-starter/_latestVersion)
[![License](https://img.shields.io/github/license/jcasbin/casbin-spring-boot-starter.svg?style=flat-square&color=blue)](http://www.apache.org/licenses/LICENSE-2.0.txt)
[![SpringBootVersion](https://img.shields.io/badge/SpringBoot-2.1.4-heightgreen.svg?style=flat-square)](https://spring.io/projects/spring-boot)
[![JCasbinVersion](https://img.shields.io/badge/JCasbinVersion-1.4.0-heightgreen.svg?style=flat-square)](https://casbin.org)

[![](https://raw.githubusercontent.com/casbin/jcasbin/master/casbin-logo.png)](https://casbin.org)

#### English|[中文](https://github.com/jcasbin/casbin-spring-boot-starter/blob/master/README_CN.md)

Casbin Spring Boot Starter is designed to help you easily integrate [jCasbin](https://github.com/casbin/jcasbin) into your Spring Boot project.

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
public class Test{
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
-  Turn on Watcher
```yaml
casbin:
  #If the model profile you are using is located at this address, you do not need this configuration
  model: classpath:casbin/model.conf
  #When you open Watcher, the default use of RedisWatcher requires manual addition of spring-boot-starter-data-redis dependency.
  enableWatcher: true
```
##### Schedule A

- ExceptionSettings(casbin.exception)

| name               | description                                      | default |
| ------------------ | ------------------------------------------------ | ------- |
| removePolicyFailed | Throws an exception when the delete policy fails | false   |



##### Note: If you do not set another data source, or set the storage file location for H2, the data is stored in memory by default using H2.