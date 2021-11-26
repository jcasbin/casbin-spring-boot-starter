# Casbin Spring Boot Starter

[![Codecov branch](https://img.shields.io/codecov/c/github/jcasbin/casbin-spring-boot-starter/master.svg?logo=codecov&style=flat-square)](https://codecov.io/gh/jcasbin/casbin-spring-boot-starter)
[![GitHub Actions](https://github.com/jcasbin/casbin-spring-boot-starter/workflows/build/badge.svg)](https://github.com/jcasbin/casbin-spring-boot-starter/actions)
[![Maven Central](https://img.shields.io/maven-central/v/org.casbin/casbin-spring-boot-starter.svg?style=flat-square&color=brightgreen)](https://maven-badges.herokuapp.com/maven-central/org.casbin/casbin-spring-boot-starter/)
[![License](https://img.shields.io/github/license/jcasbin/casbin-spring-boot-starter.svg?style=flat-square&color=blue)](http://www.apache.org/licenses/LICENSE-2.0.txt)
[![SpringBootVersion](https://img.shields.io/badge/SpringBoot-2.3.5-heightgreen.svg?style=flat-square)](https://spring.io/projects/spring-boot)
[![JCasbinVersion](https://img.shields.io/badge/JCasbinVersion-1.9.2-heightgreen.svg?style=flat-square)](https://casbin.org)

[![](https://raw.githubusercontent.com/casbin/jcasbin/master/casbin-logo.png)](https://casbin.org)

# [English](https://github.com/jcasbin/casbin-spring-boot-starter)|中文

Casbin Spring Boot Starter 用于帮助你在Spring Boot项目中轻松集成[jCasbin](https://github.com/casbin/jcasbin)。

## 如何使用

1. 在 Spring Boot 项目中加入```casbin-spring-boot-starter```依赖

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

2. 在需要使用的地方注入Enforcer

```java

@Component
public class Test {
    @Autowired
    private Enforcer enforcer;
}
```

3. 添加配置

```yaml
casbin:
  #是否开启Casbin,默认开启
  enableCasbin: true
  #是否使用线程同步的Enforcer,默认false
  useSyncedEnforcer: false
  #是否开启策略自动保存，如适配器支持该功能，默认开启
  autoSave: true
  #存储类型[file,jdbc]，目前支持的jdbc数据库[mysql(mariadb),h2,oracle,postgresql,db2]
  #欢迎编写并提交您所使用的jdbc适配器，参见：org.casbin.adapter.OracleAdapter
  #jdbc适配器将主动寻找您在spring.datasource配置的数据源信息
  #默认使用jdbc,并使用内置h2数据库进行内存存储
  storeType: jdbc
  #当使用jdbc时,定制化数据库表名,默认表名是casbin_rule
  tableName: casbin_rule
  #数据源初始化策略[create(自动创建数据表,如已创建则不再进行初始化),never(始终不进行初始化)]
  initializeSchema: create
  #本地模型配置文件地址,约定默认读取位置:classpath:casbin/model.conf
  model: classpath:casbin/model.conf
  #如默认位置未找到模型配置文件,且casbin.model未正确设置,则使用内置默认rbac模型,默认生效
  useDefaultModelIfModelNotSetting: true
  #本地策略配置文件地址,约定默认读取位置:classpath:casbin/policy.csv
  #如默认位置未找到配置文件，将会抛出异常
  #该配置项仅在casbin.storeType设定为file时生效
  policy: classpath:casbin/policy.csv
  #是否开启CasbinWatcher机制,默认不开启
  #如开启该机制,则casbin.storeType必须为jdbc,否则该配置无效
  enableWatcher: false
  #CasbinWatcher通知方式,默认使用Redis进行通知同步,暂时仅支持Redis
  #开启Watcher后需手动添加spring-boot-starter-data-redis依赖
  watcherType: redis
  #异常抛出时机控制
  exception:
    ... 详见附表-异常设定
```

4. 最简配置

- 不使用其他附加组件配置

```yaml
casbin:
  #如果您使用的模型配置文件位于此地址,则无需任何配置
  model: classpath:casbin/model.conf
```

- 开启Watcher

```yaml
casbin:
  #如果您使用的模型配置文件位于此地址,则无需该配置
  model: classpath:casbin/model.conf
  #开启Watcher后,默认使用RedisWatcher需手动添加spring-boot-starter-data-redis依赖
  enableWatcher: true
```

5. 使用自定义独立数据源

- 只需要在注入自定义数据源时增加```@CasbinDataSource```注解

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

#### 附表：

- 异常设定(casbin.exception)

| name               | description                | default |
| ------------------ | -------------------------- | ------- |
| removePolicyFailed | 删除策略失败时是否抛出异常 | false   |

##### 注意: 如果您没有设置其他数据源,或为H2设定存储文件位置,则默认使用H2将数据存储于内存中

#### 通知:

自版本0.0.11开始, casbin-spring-boot-starter 默认为数据库表结构添加ID字段

0.0.11之前的版本升级时, 需要用户手动添加ID字段

有关详细信息, 请参阅: https://github.com/jcasbin/casbin-spring-boot-starter/issues/21
