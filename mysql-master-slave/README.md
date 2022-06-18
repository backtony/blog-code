# Spring - AWS RDS로 MySQL Replication 적용하기 (feat. 다중 AZ)


# 1. DB Replication 이란?
---
데이터베이스 이중화 방식 중 하나로 __하나의 Master DB와 여러대의 Slave DB를 구성하는 방식__ 을 의미합니다.

## 사용하는 이유
![그림1](https://github.com/backtony/blog-code/blob/master/mysql-master-slave/img/1-1.PNG?raw=true)

__부하 분산__  
서비스에 사용자가 많아져 트래픽이 늘어날 경우, DB에 쿼리를 날리는 일이 빈번하게 일어납니다. DB에서는 쿼리를 모두 처리하기 힘들어지게 되고 이에 따라 부하를 줄이기 위해 DB를 이중화하여 Master에서는 쓰기/수정/삭제 연산을 처리하고 Slave에서는 읽기 연산만을 처리하여 병목 현상을 줄일 수 있습니다.
<br>

__데이터 백업__  
Master의 데이터가 날아가더라도 Slave에 데이터가 저장되어 있으므로 어느정도 복구할 수 있습니다. MySQL Replication은 비동기 방식이기 때문에 100% 정합성을 보장할 수 없습니다.

## MySQL Replication 동작 원리
![그림2](https://github.com/backtony/blog-code/blob/master/mysql-master-slave/img/1-2.PNG?raw=true)

1. 클라이언트(Application)에서 Commit 을 수행한다.
2. Connection Thead 는 스토리지 엔진에게 해당 트랜잭션에 대한 Prepare(Commit 준비)를 수행한다.
3. Commit 을 수행하기 전에 먼저 Binary Log 에 변경사항을 기록한다.
4. 스토리지 엔진에게 트랜잭션 Commit 을 수행한다.
5. Master Thread 는 시간에 구애받지 않고(비동기적으로) Binary Log 를 읽어서 Slave 로 전송한다.
6. Slave 의 I/O Thread 는 Master 로부터 수신한 변경 데이터를 Relay Log 에 기록한다. (기록하는 방식은 Master 의 Binary Log 와 동일하다)
7. Slave 의 SQL Thread 는 Relay Log 에 기록된 변경 데이터를 읽어서 스토리지 엔진에 적용한다.

<Br>

# 2. 다중 AZ 배포
---
![그림3](https://github.com/backtony/blog-code/blob/master/mysql-master-slave/img/1-3.PNG?raw=true)

다중 AZ 배포 방식은 Amazon RDS가 다른 가용 영역에 __동기식 예비 복제본__ 을 자동으로 프로비저닝하고, DB 인스턴스 장애나 가용 영역 장애가 발생할 경우 Amazon RDS가 자동으로 예비 복제본에 장애 조치를 수행해 예비 복제본이 __마스터로 승격__ 되게 하는 관리하는 방식입니다.  
다중 AZ 배포의 경우, 동기식이기 때문에 데이터의 정합성을 보장할 수 있지만 복제본의 경우 읽기 작업을 할 수 없습니다. 이는 가용성을 위한 것이지 부하 분산을 통한 성능 향상을 위한 것이 아니기 때문입니다.
<br>


# 3. RDS 생성하기
---
![그림4](https://github.com/backtony/blog-code/blob/master/mysql-master-slave/img/1-4.PNG?raw=true)

다중 AZ 배포 방식과 Replication을 함께 사용하면 서로의 장점을 이용할 수 있습니다.  
위 그림처럼 마스터는 AZ배포 방식으로 복제본을 만들어 주고, 마스터의 Replication을 따로 만들어주도록 구성하면 됩니다.
<Br><Br>

![그림5](https://github.com/backtony/blog-code/blob/master/mysql-master-slave/img/1-5.PNG) ?raw=true 
기본적인 RDS를 만들면 됩니다. 다중 AZ 배포 옵션은 프리티어에서 제공하지 않으므로 개발/테스트를 선택해줍니다.
<br><Br>


![그림6](https://github.com/backtony/blog-code/blob/master/mysql-master-slave/img/1-6.PNG) ?raw=true 
AZ 옵션을 활성화 시켜줍니다.
<Br><Br>

![그림7](https://github.com/backtony/blog-code/blob/master/mysql-master-slave/img/1-7.PNG) ?raw=true 
테스트 용이므로 퍼블릭 액세스를 허용해줍니다.
<Br><Br>

![그림8](https://github.com/backtony/blog-code/blob/master/mysql-master-slave/img/1-8.PNG) ?raw=true 
방금 생성한 DB를 선택하시고 읽기 전용 복제본을 생성해줍니다. 기본 옵션으로 진행하면 되고 복제본 생성에서는 AZ 옵션을 꺼주시고, 퍼플릭 엑세스를 허용해줍니다.
<br><Br>

# 4. Spring에 적용하기
---
## 구성
+ @Transactional(readOnly = true) 인 경우는 Slave DB 접근
+ @Transactional(readOnly = false) 인 경우에는 Master DB 접근

## application.yml
```yml
spring:
  datasource:
    url: jdbc:mysql://master.chjqzcooytli.ap-northeast-2.rds.amazonaws.com:3306/test?useSSL=false&useUnicode=true&characterEncoding=utf8
    slave-list:
      - name: slave_1
        url: jdbc:mysql://slave-1.chjqzcooytli.ap-northeast-2.rds.amazonaws.com/test?useSSL=false&useUnicode=true&characterEncoding=utf8
      - name: slave_2
        url: jdbc:mysql://slave-2.chjqzcooytli.ap-northeast-2.rds.amazonaws.com/test?useSSL=false&useUnicode=true&characterEncoding=utf8
    driver-class-name: com.mysql.cj.jdbc.Driver
    username: 비밀
    password: 비밀


  jpa:
    properties:
      hibernate:
        format_sql: true
        hbm2ddl:
          auto: create
        physical_naming_strategy: org.springframework.boot.orm.jpa.hibernate.SpringPhysicalNamingStrategy
        defer-datasource-initialization: true
        database-platform: org.hibernate.dialect.MySQL8Dialect
        open-in-view: false
        show-sql: true
        generate-ddl: true

logging:
  level:
    org.hibernate.SQL: debug
    org.hibernate.type: trace 
```
slave-list를 적어준 부분이고 이는 자동 설정이 아니며, 코드상에 사용할 값들입니다.  
스프링 자동 설정을 제외하고 직접 세팅하는 작업을 진행하기 때문에 추가적인 작업들이 몇가지 필요합니다.  
기존에는 yml에 세팅하면 자동으로 DataSource가 설정되면서 값들을 읽어갔지만 이제는 수동으로 해줘야하기 때문에 yml에 세팅한 값들이 먹히지 않습니다.  
따라서 yml에 Jpa 세팅값을 읽는 JpaProperties 클래스를 사용하여 DataSource를 커스텀할 때 가져와서 사용해야 하므로 위와 같이 properties 안으로 세팅값을 몰아 넣습니다.  
스프링 자동 설정 중 테이블 네이밍 설정이 빠져있기 때문에 테이블 네이밍 설정을 해줘야 합니다. 이 설정이 위의 naming 옵션입니다.  
이외에는 기본적인 기본적인 MySQL DB 설정입니다.

## DbProperty.java
```java
@Getter @Setter @Component
@ConfigurationProperties("spring.datasource")
public class DbProperty {

    private String url;
    private List<Slave> slaveList;

    private String driverClassName;
    private String username;
    private String password;

    @Getter @Setter
    public static class Slave {
        private String name;
        private String url;
    }
}
```
앞서 yml에 명시해줬던 값들을 주입받아서 사용하는 클래스입니다.

## ReplicationRoutingCircularList.java
```java
public class ReplicationRoutingCircularList<T> {
    private List<T> list;
    private static Integer counter = 0;

    public ReplicationRoutingCircularList(List<T> list) {
        this.list = list;
    }

    public T getOne() {
        int circularSize = list.size();
        if (counter + 1 > circularSize) {
            counter = 0;
        }
        return list.get(counter++ % circularSize);
    }
}
```
여러개의 Replication DB의 DataSource를 순서대로 로드밸런싱 하기 위해 사용하는 클래스입니다.

## ReplicationRoutingDataSource.java
```java
@Slf4j
public class ReplicationRoutingDataSource extends AbstractRoutingDataSource {

    private ReplicationRoutingCircularList<String> replicationRoutingDataSourceNameList;

    @Override
    public void setTargetDataSources(Map<Object, Object> targetDataSources) {
        super.setTargetDataSources(targetDataSources);

        replicationRoutingDataSourceNameList = new ReplicationRoutingCircularList<>(
                targetDataSources.keySet()
                        .stream()
                        .filter(key -> key.toString().contains("slave"))
                        .map(Object::toString)
                        .collect(toList()));
    }

    @Override
    protected Object determineCurrentLookupKey() {
        boolean isReadOnly = TransactionSynchronizationManager.isCurrentTransactionReadOnly();
        if (isReadOnly) {
            String slaveName = replicationRoutingDataSourceNameList.getOne();
            log.info("Slave DB name : {}",slaveName); // 테스트에 찍어보기 위한 로그, 운영시 제거
            return slaveName;
        }
        log.info("master DB name : {}","master"); // 테스트에 찍어보기 위한 로그, 운영시 제거
        return "master";
    }
}
```
여러개의 DataSource를 묶고 필요에 따라 분기처리를 하기 위해 AbstractRoutingDataSource클래스를 재정의해서 사용해야 합니다.  
setTargetDataSources 에 의해서 모든 데이터소스는 부모 생성자에 넘기고, determineCurrentLookupKey 메서드에서 사용할 replicationRoutingDataSourceNameList 초기화 해줍니다.  
이때 초기화 되는 값은 DataSource 이름 중에서 Slave가 들어간 것을 toString으로 이름만 빼서 리스트로 담아주는 작업입니다. (yml에서 작성한 slave-list의 name들이 들어가게 됩니다.)  
determineCurrentLookupKey 메서드에서 현재 트랜잭션이 readOnly일 시 slave 데이터 소스 이름을, 아닐 시 master db의 DataSource의 이름을 리턴하도록 작성해줍니다.  
이는 바로 아래 DbConfig를 보면 알겠지만 dataSource를 value를 저장하는 Map의 Key값이 데이터 소스 이름으로 등록하고 있기 때문입니다.

## DbConfig.java
설정에 필요한 부가적인 것들은 모두 만들었으니 이제 최종적으로 이제 최종적으로 DataSource, TransactionManager, EntityManagerFactory를 설정해야합니다.
```java
@Configuration
@RequiredArgsConstructor
// DataSource를 직접 설정해야하기 때문에 자동으로 DataSource를 연결하는 DataSourceAutoConfiguration 클래스를 제외
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class}) 
public class DbConfig {

    private final DbProperty dbProperty;
    private final JpaProperties jpaProperties;

    @Bean
    public DataSource routingDataSource() {
        // 앞서 AbstractRoutingDataSource 를 상속받아 재정의한 ReplicationRoutingDataSource 생성
        ReplicationRoutingDataSource replicationRoutingDataSource = new ReplicationRoutingDataSource();

        // master와 slave 정보를 키(name), 밸류(dataSource) 형식으로 Map에 저장
        Map<Object, Object> dataSourceMap = new LinkedHashMap<>();
        DataSource masterDataSource = createDataSource(dbProperty.getUrl());
        dataSourceMap.put("master", masterDataSource);
        dbProperty.getSlaveList().forEach(slave -> {
            dataSourceMap.put(slave.getName(), createDataSource(slave.getUrl()));
        });

        // TargetDataSources를 세팅하지만 앞서 재정의했듯이 해당 클래스가 Slave이름을 리스트로 갖는 변수를 세팅하는 코드가 있음
        replicationRoutingDataSource.setTargetDataSources(dataSourceMap);

        // 디폴트는 Master 로 설정
        replicationRoutingDataSource.setDefaultTargetDataSource(masterDataSource);
        return replicationRoutingDataSource;
    }

    public DataSource createDataSource(String url) {
        HikariDataSource hikariDataSource = new HikariDataSource();
        hikariDataSource.setJdbcUrl(url);
        hikariDataSource.setDriverClassName(dbProperty.getDriverClassName());
        hikariDataSource.setUsername(dbProperty.getUsername());
        hikariDataSource.setPassword(dbProperty.getPassword());
        return hikariDataSource;
    }

    @Bean
    public DataSource dataSource() {
        // 아래서 설명
        return new LazyConnectionDataSourceProxy(routingDataSource());
    }

    // JPA 에서 사용할 entityManager 설정
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        EntityManagerFactoryBuilder entityManagerFactoryBuilder = createEntityManagerFactoryBuilder(jpaProperties);
        return entityManagerFactoryBuilder.dataSource(dataSource()).packages("com.example.mysqltest").build();
    }

    private EntityManagerFactoryBuilder createEntityManagerFactoryBuilder(JpaProperties jpaProperties) {
        AbstractJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        // jpaProperties는 yml에 있는 jpaProperties를 의미
        return new EntityManagerFactoryBuilder(vendorAdapter, jpaProperties.getProperties(), null);
    }


    // JPA 에서 사용할 TransactionManager 설정
    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        JpaTransactionManager tm = new JpaTransactionManager();
        tm.setEntityManagerFactory(entityManagerFactory);
        return tm;
    }

    // jdbcTemplate 세팅
    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
```
다른 내용은 간단하게 주석으로 적어놨으므로 LazyConnectionDataSourceProxy에 대한 설명만 진행하겠습니다.  
기본적으로 Spring은 @Transactional을 만나면 다음 순서로 처리를 진행합니다.

> transactionManager 선별 -> Datasource에서 connection 획득 -> transaction 동기화


하지만 transaction 동기화가 먼저 되고 ReplicationRoutingDataSource에서 커넥션을 획득해야만 지금까지 한 설정을 사용할 수 있습니다. 이는  ReplicationRoutingDataSource.java를 LazyConnectionDataSoruceProxy로 감싸주어 해결할 수 있습니다.  
LazyConnectionDataSoruceProxy는 실질적인 쿼리 실행 여부와 상관없이 트랜잭션이 걸리면 무조건 Connection 객체를 확보하는 Spring의 단점을 보완하여 트랜잭션 시작시에 Connection Proxy 객체를 리턴하고 실제로 쿼리가 발생할 때 데이터소스에서 getConnection()을 호출하는 역할을 합니다. 따라서 다음과 같이 동작하게 됩니다.

> TransactionManager 선별 -> LazyConnectionDataSourceProxy에서 Connection Proxy 객체 획득 -> Transaction 동기화(Synchronization) -> 실제 쿼리 호출시에 ReplicationRoutingDataSource.getConnection().determineCurrentLookupKey() 호출

<br>

# 5. 테스트해보기
---
__Member.java__
```java
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private int age;
}
```
<Br>

__MemberRepository.java__
```java
public interface MemberRepository extends JpaRepository<Member,Long> {}
```
<Br>

__MemberRepositoryTest.java__
```java
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // 데이터 소스 자동 연결 해제
class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("Save시 데이터 소스는 Master를 선택한다.")
    void save_Member_Success() throws Exception{
        //given
        Member member = Member.of("backtony", 26);
        memberRepository.save(member);
    }

    @Test
    @DisplayName("Slave DB에서 데이터를 조회한다 - 여러번 조회시 slave db 를 번갈아가면서 조회한다.")
    void findMember_Success() throws Exception{
        //given
        int age = 27;
        String name = "backtony";
        Member save = memberRepository.save(Member.of(name, age));

        //when
        Member member = memberRepository.findById(save.getId()).get();
        Member member1 = memberRepository.findById(save.getId()).get();
        Member member2 = memberRepository.findById(save.getId()).get();
        Member member3 = memberRepository.findById(save.getId()).get();

        //then
        Assertions.assertThat(member.getAge()).isEqualTo(age);
        Assertions.assertThat(member.getName()).isEqualTo(name);
    }

}
```
앞세 ReplicationRoutingDataSource클래스의 determineCurrentLookupKey메서드에서 datasource가 들어있는 Map의 키값을 반환할 때 로그로 찍도록 세팅해뒀기 때문에 해당 로그를 통해 적절한 DataSource로 나가는지 확인할 수 있습니다.


<Br><Br>

__참고__  
<a href="http://cloudrain21.com/mysql-replication" target="_blank"> MySQL – Replication 구조</a>   
<a href="https://www.bespinglobal.com/techblog-rds-20180627/" target="_blank"> [Step by Step] Amazon RDS 가용성 및 확장성 (Multi AZ 및 Read Replica)</a>   
<a href="https://velog.io/@kingcjy/Spring-Boot-JPA-DB-Replication-%EC%84%A4%EC%A0%95%ED%95%98%EA%B8%B0" target="_blank"> Spring Boot + JPA DB Replication 설정하기</a>   
<a href="http://kwon37xi.egloos.com/m/5364167" target="_blank"> Java 에서 DataBase Replication Master/Slave (write/read) 분기 처리하기</a>   
<a href="https://tech.pick-git.com/db-replication/" target="_blank"> DB 리플리케이션 적용기</a>   