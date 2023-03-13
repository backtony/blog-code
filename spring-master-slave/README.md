
# datasource 이중화
---
## 구성
+ @Transactional(readOnly = true) 인 경우는 Slave DB 접근
+ @Transactional(readOnly = false) 인 경우에는 Master DB 접근

## application.yml
```yml
spring:
  jpa:
    show-sql: true
    generate-ddl: true
    hibernate:
      ddl-auto: validate
    database: mysql
    database-platform: org.hibernate.dialect.MySQL8Dialect
    open-in-view: false
  sql:
    init:
      mode: never

database:
  master:
    driver-class-name: com.mysql.cj.jdbc.Driver
    jdbc-url: jdbc:mysql://localhost:3306/master?serverTimezone=UTC&characterEncoding=UTF-8
    username: root
    password: root
    maximum-pool-size: 5
    minimum-idle: 5
    connection-timeout: 5000

  slave:
    driver-class-name: com.mysql.cj.jdbc.Driver
    jdbc-url: jdbc:mysql://localhost:3306/slave?serverTimezone=UTC&characterEncoding=UTF-8
    username: root
    password: root
    maximum-pool-size: 5
    minimum-idle: 5
    connection-timeout: 5000
    read-only: true

logging:
  level:
    org.hibernate.SQL: debug
    org.hibernate.type: trace
```

## ReplicationRoutingDataSource.java
```java
@Slf4j
public class RoutingDataSource extends AbstractRoutingDataSource {
    public static final String MASTER = "MASTER";
    public static final String SLAVE = "SLAVE";

    @Override
    protected Object determineCurrentLookupKey() {
        final boolean readOnly = TransactionSynchronizationManager.isCurrentTransactionReadOnly();
        final boolean actuallyActive = TransactionSynchronizationManager.isActualTransactionActive();
        final String dbKey = actuallyActive && !readOnly ? MASTER : SLAVE;

        log.info("transaction: db={} (readonly={}, actuallyActive={})", dbKey, readOnly, actuallyActive);

        return dbKey;
    }
}
```
트랜잭션이 활성화되어있는지와 readOnly 여부를 통해 어떤 db로 요청을 보낼지 결정하는 RoutingDataSource를 커스텀해준다.  


## DbConfig.java
```java
@Configuration
public class JpaConfig {

    @Primary
    @Bean("dataSource")
    public DataSource dataSource(DataSource routingDataSource) {
        return new LazyConnectionDataSourceProxy(routingDataSource);
    }

    @Bean("routingDataSource")
    public DataSource routingDataSource(
        @Qualifier("masterDataSource") DataSource masterDataSource,
        @Qualifier("slaveDataSource") DataSource slaveDataSource) {
        final Map<Object, Object> dataSourceMap = new HashMap<>();
        dataSourceMap.put(RoutingDataSource.MASTER, masterDataSource);
        dataSourceMap.put(RoutingDataSource.SLAVE, slaveDataSource);

        final RoutingDataSource routingDataSource = new RoutingDataSource();
        routingDataSource.setTargetDataSources(dataSourceMap);

        // default는 slave쪽으로 가도록
        routingDataSource.setDefaultTargetDataSource(slaveDataSource);

        return routingDataSource;
    }

    @Bean("masterDataSource")
    @ConfigurationProperties(prefix = "database.master")
    public DataSource masterDataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    @Bean("slaveDataSource")
    @ConfigurationProperties(prefix = "database.slave")
    public DataSource slaveDataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }
}
```
yml에 적은 설정들을 HiKariDataSource 클래스에 매핑해서 가져와서 빈으로 등록해주고 routingDataSource의 dataSource를 하나 만들어서 엮어준다.  

그리고 최종적으로 실질적으로 사용되는 datasource빈을 만들어서 등록해준다.  

## LazyConnectionDataSourceProxy

+ Spring에서는 트랜잭션에 진입하는 순간 설정된 Datasource의 커넥션을 가져오는데 이로 인한 단점은 아래와 같다.
  + Ehcache같은 Cache를 사용하는 경우 실제 Database에 접근하지 않지만 불필요한 커넥션을 점유
  + Hibernate의 영속성 컨텍스트 1차캐시(실제 Database에 접근하지 않음) 에도 불필요한 커넥션을 점유
  + 외부 서비스(http, etc …)에 접근해서 작업을 수행한 이후에 그 결과값을 Database에 Read/Write하는 경우 외부 서비스에 의존하는 시간만큼 불필요한 커넥션 점유
  + Multi Datasource 환경에서 트랜잭션에 진입한 이후 Datasource를 결정해야할때 이미 트랜잭션 진입시점에 Datasource가 결정되므로 분기가 불가능

LazyConnectionDataSourceProxy을 사용하면 실제로 커넥션이 필요한경우가 아니라면 데이터베이스 풀에서 커넥션을 점유하지 않고 실제로 필요한 시점에만 커넥션을 점유하게 할 수 있다. 이는
트랜잭션 시작시에 Connection Proxy 객체를 리턴하고 실제로 쿼리가 발생할 때 데이터소스에서 getConnection()을 호출하는 역할을 제공한다.  

## 주의사항
처음 spring boot 뜰때는 ddl이 생성되는 경우 `TransactionSynchronizationManager.isActualTransactionActive()` 가 false이기 때문에 slave쪽으로 ddl이 나가게 된다. slave는 read-only 설정을 config에 해뒀기 때문에 ddl생성에 문제가 생긴다. 이 부분은 사전에 ddl을 생성해놓으면 해결된다.

