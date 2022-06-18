# Spring DB - 커넥션 풀, 데이터 소스, 트랜잭션


## 커넥션 풀 이해
### 커넥션 풀을 사용하지 않을 때
![그림1](https://github.com/backtony/blog-code/blob/master/spring/img/db/2/1-1.PNG?raw=true)  

1. 애플리케이션 로직은 DB 드라이버를 통해 커넥션을 조회합니다.
2. DB 드라이버는 DB와 TCP/IP 커넥션을 연결합니다.
3. DB 드라이버는 연결이 완료되면 ID, PW와 기타 부가정보를 DB에 전달합니다.
4. DB는 ID, PW를 통해 인증을 완료하고 내부에 DB 세션을 생성합니다.
5. DB는 커넥션 생성이 완료되었다는 응답을 보냅니다.
6. DB 드라이버는 커넥션 객체를 생성해서 클라이언트에 반환합니다.  

커넥션 풀을 사용하지 않을 경우 매번 커넥션을 생성하는 위와 같은 과정을 거쳐야 합니다.  

### 커넥션 풀을 사용할 때
![그림2](https://github.com/backtony/blog-code/blob/master/spring/img/db/2/1-2.PNG?raw=true)  

__애플리케이션을 시작하는 시점에 커넥션 풀은 필요한 만큼 커넥션을 미리 확보해서 풀에 보관합니다.__  
보통 얼마나 보관할지는 서비스의 특징과 스펙에 따라 다르지만 기본값은 10개입니다.  
커넥션 풀에 들어 있는 커넥션은 이미 TCP/IP로 DB와 커넥션이 연결되어 있는 상태이기 때문에 언제든지 즉시 SQL을 DB에 전달할 수 있습니다.  
<br>


![그림3](https://github.com/backtony/blog-code/blob/master/spring/img/db/2/1-3.PNG?raw=true)  

애플리케이션 로직에서는 이제 DB 드라이버를 통해서 새로 커넥션을 획득하는 것이 아니라 커넥션 풀을 통해 이미 생성되어 있는 커넥션을 객체 참조로 그냥 가져다 쓰기만 하면 됩니다.  
커넥션 풀에 커넥션을 요청하면 커넥션 풀은 자신이 가지고 있는 커넥션 중 하나를 반환합니다.  
__커넥션을 모두 사용하고 나면 커넥션을 종료하는 것이 아니라 다음에 다시 사용할 수 있도록 해당 커넥션을 그대로 커넥션 풀에 반환합니다.__  
<br>

커넥션 풀을 사용하므로써 시간적, 리소스적으로 절약할 수 있음과 더불어 DB에 무한정 연결이 생성되는 것을 막아주어 DB를 보호하는 효과도 있습니다.  
커넥션 풀 오픈 소스는 여러 가지 있지만 성능과 사용의 편리함 측면에서 hikariCP를 사용합니다.  
__스프링 부트 2.0부터는 기본 커넥션 풀로 hikariCP를 제공합니다.__  

<br>

## DataSource 이해
![그림4](https://github.com/backtony/blog-code/blob/master/spring/img/db/2/1-4.PNG?raw=true)  

커넥션을 얻는 방법은 DriverManager를 직접 사용해서 새로운 커넥션을 새로 생성하거나 커넥션 풀을 사용하는 등 다양한 방법이 있습니다.  
만약 DriverManager를 통해서 커넥션을 획득하다가 HikariCP 커넥션 풀을 사용하는 방법으로 변경하려고 한다면 커넥션을 획득하는 애플리케이션 로직을 전부 변경해야 하는 문제가 생깁니다.  
의존관계가 DriverManager에서 HikariCP로 변경되기 때문입니다.  
이런 문제 때문에 등장한 것이 DataSource 입니다.  
<Br>

![그림5](https://github.com/backtony/blog-code/blob/master/spring/img/db/2/1-5.PNG?raw=true)  

DataSource는 __커넥션을 획득하는 방법을 추상화하는 인터페이스__ 입니다.  
인터페이스의 핵심 기능은 __커넥션 조회__ 입니다.  
대부분의 커넥션 풀은 DataSource 인터페이스를 이미 구현해두었으므로 커넥션 풀이 아니라 DataSource 인터페이스에만 의존하도록 애플리케이션 로직을 구현하면 됩니다.  
이후 커넥션 조회 기술을 변경하고 싶다면 해당 구현체로 갈아끼우기만 하면 됩니다.  
<br>

예외적으로 DriverManager는 DataSource 인터페이스를 사용하지 않습니다.  
이 문제를 해결하기 위해 DriverManager도 DataSource를 통해 사용할 수 있도록 스프링에서는 DriverManagerDataSource라는 DataSource를 구현한 클래스를 제공합니다.  

<br>

정리하자면, 커넥션을 생성하고 가져오는 방식에는 여러 가지 오픈 소스 커넥션 풀과 DriverManager가 있는데 코드레벨에서는 다를지라도 논리적인 기능적 측면에서 보면 커넥션을 생성하고 가져오는 일을 하기 때문에 이 기능을 DataSource로 추상화한 것입니다.  
따라서, 로직에서는 추상화인 DataSource에 의존하도록 작성하고 기술을 교체해야 하는 일이 생기면 구현체만 교체하면 됩니다.  


### DriverManager로 커넥션 얻기
```java
@Slf4j
public class ConnectionTest {

    @Test
    void driverManager() throws Exception {
        // DriverMananger 사용
        // 커넥션을 획득 할 때마다 인자를 넘겨야 한다.
        Connection con1 = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        Connection con2 = DriverManager.getConnection(URL, USERNAME, PASSWORD);

        log.info("connection = {} , classes = {}", con1, con1.getClass());
        log.info("connection = {} , classes = {}", con2, con2.getClass());
    }

    @Test
    void dataSourceDriverManager() throws Exception{
        // dataSource를 구현한 DriverManagerDataSource 사용
        // 초기 세팅에만 설정값을 넘긴다.
        DriverManagerDataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);
        useDataSource(dataSource);
    }

    private void useDataSource(DataSource dataSource) throws SQLException {
        Connection con1 = dataSource.getConnection();
        Connection con2 = dataSource.getConnection();
        log.info("connection = {} , classes = {}", con1, con1.getClass());
        log.info("connection = {} , classes = {}", con2, con2.getClass());
    }
}
```
+ DriverManager
  - 커넥션을 획득 할 때마다 설정 정보를 인자로 넘겨야 합니다.
+ DriverManagerDataSource
  - 내부적으로 DriverManager를 사용하지만 DataSource의 구현체입니다.
  - DriverManager 사용 방식에서 설정과 사용을 분리했습니다.
  - 설정은 초기에 한 번만 입력하고, 이후 사용하는 곳에서는 getConnection만 호출합니다.
  - __설정과 사용을 분리함으로써 향후 변경에 더 유연하게 대처하고 손쉽게 유지보수 할 수 있습니다.__


### HikariDataSource로 커넥션 얻기
```java
@Slf4j
public class ConnectionTest {

    @Test
    void dataSourceConnectionPool() throws Exception{
        // DataSource 인터페이스로 받을 수도 있지만 추가적인 세팅이 필요하므로 HikariDataSource로 받습니다.
        // DataSource 인터페이스는 커넥션 조회 기능만 제공하므로 이외 설정 세팅을 하기 위해서는 구현체로 받아서 사용해야 합니다.
        HikariDataSource dataSource = new HikariDataSource(); 
        dataSource.setJdbcUrl(URL);
        dataSource.setUsername(USERNAME);
        dataSource.setPassword(PASSWORD);
        dataSource.setMaximumPoolSize(10);
        dataSource.setPoolName("myPool");
        useDataSource(dataSource);
        Thread.sleep(1000); // Connection 얻어오는 로그를 보기 위해 잠시 중지
    }

    private void useDataSource(DataSource dataSource) throws SQLException {
        Connection con1 = dataSource.getConnection();
        Connection con2 = dataSource.getConnection();
        log.info("connection = {} , classes = {}", con1, con1.getClass());
        log.info("connection = {} , classes = {}", con2, con2.getClass());
    }
}

// 마지막 로그 출력
// 총 10개, 2개 사용중, 8개 쉬는중, 기다리는거 0개
... myPool - After adding stats (total=10, active=2, idle=8, waiting=0)
```
__커넥션 풀에서 커넥션을 생성하는 작업은 애플리케이션 실행 속도에 영향을 주지 않기 위해 별도의 스레드에서 동작합니다.__  
따라서 thread.sleep을 시키지 않으면 바로 죽어버리기 때문에 로그를 보기 위해서 잠시 sleep을 시켜줬습니다.  

<br>

## 트랜잭션

### 데이터베이스 연결 구조와 DB 세션
![그림6](https://github.com/backtony/blog-code/blob/master/spring/img/db/2/1-6.PNG?raw=true)  

사용자는 WAS나 DB 접근 툴 같은 클라이언트를 사용해 DB 서버에 접근하여 연결을 요청하고 커넥션을 맺습니다.  
이때 DB 서버는 내부에 세션을 만들고 앞으로 해당 커넥션을 통한 모든 요청을 해당 세션을 통해 실행합니다.  
즉, 개발자가 클라이언트를 통해 SQL을 전달하면 현재 커넥션에 연결된 세션이 SQL을 실행합니다.  
세션은 트랜잭션을 시작하고, 커밋 또는 롤백을 통해 트랜잭션을 종료할 수 있으며 이후에 새로운 트랜잭션을 다시 시작할 수도 있습니다.

### 트랜잭션 추상화
![그림7](https://github.com/backtony/blog-code/blob/master/spring/img/db/2/1-7.PNG?raw=true)  
데이터베이스 접근 기술에는 여러 가지가 존재합니다.  
만약 JDBC에 의존하는 코드를 작성했다가 JPA로 전환하고자 한다면 기존 코드를 전부 고쳐야하는 문제가 발생합니다.  
하지만 논리적인 로직은 트랜잭션을 열고, 닫고, 커밋하고, 롤백하는 과정은 똑같다고 볼 수 있습니다.  
이 문제를 해결하기 위해서 스프링은 트랜잭션을 추상화해서 제공합니다.  

<br>

![그림8](https://github.com/backtony/blog-code/blob/master/spring/img/db/2/1-8.PNG?raw=true)  
PlatformTransactionManager 인터페이스는 __트랜잭션 매니저__ 라고 불리는데 트랜잭션 시작, 종료, 커밋, 롤백에 관한 내용이 있고 이에 대한 각 접근 기술에 대한 구현체를 제공합니다.  
따라서 비즈니스 로직은 스프링 트랜잭션 추상화 인터페이스에 의존하게 하여 전환 시 구현체만 갈아껴서 사용하면 됩니다.  


### 트랜잭션 매니저와 트랜잭션 동기화 매니저
보통 코드를 작성하면 서비스 단에서 트랜잭션이 시작되고 서비스 로직이 끝나면 트랜잭션이 종료됩니다.  
즉, 하나의 서비스 로직에서 리포지토리로 접근하는 로직이 여러 개 있다고 여러 개의 트랜잭션을 사용하는 것이 아니라 같은 트랜잭션을 사용합니다.  
이를 위해 스프링은 스레드 로컬을 사용해 커넥션을 동기화해주는 __트랜잭션 동기화 매니저__ 를 제공합니다.  
트랜잭션 매니저는 내부적으로 트랜잭션 동기화 매니저를 사용합니다.  
이 과정의 동작 방식을 하나씩 살펴봅시다.  

<br>

![그림9](https://github.com/backtony/blog-code/blob/master/spring/img/db/2/1-9.PNG?raw=true)  

클라이언트의 요청으로 서비스 로직을 실행합니다.
1. 서비스 계층에서 TransactionManager.getTransaction()을 호출해 트랜잭션을 시작합니다.
2. 트랜잭션을 시작하려면 먼저 데이터베이스 커넥션이 필요하기 때문에 트랜잭션 매니저는 생성자의 인자로 전달받는 데이터소스를 사용해 커넥션을 생성합니다.
3. 만든 커넥션을 수동 커밋 모드로 변경하고나서야 실제 데이터베이스 트랜잭션을 시작합니다.
4. 트랜잭션 동기화 매니저에 해당 커넥션을 보관합니다. 
5. 트랜잭션 동기화 매니저는 커넥션을 스레드 로컬에 보관하기 때문에 멀티 스레드 환경에서 안전합니다.

<br>

![그림10](https://github.com/backtony/blog-code/blob/master/spring/img/db/2/1-10.PNG?raw=true)  

6. 서비스는 비즈니스 로직을 실행하면서 리포지토리의 메서드를 호출합니다.
7. 리포지토리 메서드들은 트랜잭션이 시작된 커넥션이 필요하므로 DataSourceUtils.getConnection()을 사용해 트랜잭션 동기화 매니저에 보관된 커넥션을 꺼내서 사용합니다. 따라서 같은 커넥션을 사용하기에 트랜잭션도 유지되게 됩니다.
8. 획득한 커넥션을 사용해서 SQL을 데이터베이스에 전달해서 실행합니다.

<br>

![그림11](https://github.com/backtony/blog-code/blob/master/spring/img/db/2/1-11.PNG?raw=true)  
9. 비즈니스 로직이 전부 끝나면 트랜잭션 종료를 요청합니다.
10. 트랜잭션을 종료하기 위해 트랜잭션 동기화 매니저를 통해 동기화된 커넥션을 획득합니다.
11. 획득한 커넥션을 통해 데이터베이스에 트랜잭션을 커밋하거나 롤백합니다.
12. 전체 리소스를 정리합니다.
    - 트랜잭션 동기화 매니저 정리(쓰레드 로컬 정리)
    - con.setAutoCommit(true)로 되돌리기(커넥션 풀을 고려)
    - con.close()를 호출해서 커넥션을 종료, 커넥션 풀을 사용하는 경우 커넥션 풀에 반환


### 트랜잭션 AOP
AOP를 살펴보기 전에, 트랜잭션 매니저를 사용하면 작성하게 되는 코드를 살펴봅시다.  
```java
@Slf4j
@RequiredArgsConstructor
public class MemberServiceV3_1 {

    private final MemberRepositoryV3 memberRepository;
    private final PlatformTransactionManager transactionManager;

    public void accountTransfer(String fromId, String toId, int money) throws SQLException {
        // 트랜잭션 시작
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());

        try {            
            bizLogic(fromId, toId, money); // 비즈니스 로직
            transactionManager.commit(status); // 성공시 커밋

        } catch (Exception e){
            transactionManager.rollback(status); // 롤백
            throw new IllegalStateException(e);
        }
        // 커넥션 release는 커밋되거나 롤백되면 알아서 transactionManager가 처리한다.
    }
    ... 생략
}
```
코드르 보면 트랜잭션을 처리하는 객체와 비즈니스 로직을 처리하는 서비스 객체가 섞여있는 것을 확인할 수 있습니다.  
섞여있으니 당연히 가독성도 떨어지고 유지 보수도 어려워집니다.  


<br>

![그림12](https://github.com/backtony/blog-code/blob/master/spring/img/db/2/1-12.PNG?raw=true)  
스프링에서는 프록시를 사용하여 트랜잭션을 처리하는 객체와 비즈니스 로직을 처리하는 서비스 객체를 명확하게 분리합니다.  
생성되는 프록시 객체는 아래와 같습니다.
```java
// 프록시 객체
public class TransactionProxy {
    
    private MemberService target;

    public void logic() { 
        // 트랜잭션 시작
        TransactionStatus status = transactionManager.getTransaction(..);
        try {            
            target.logic(); // 실제 대상 호출
            transactionManager.commit(status); // 성공시 커밋 
        } catch (Exception e) {
            transactionManager.rollback(status); // 실패시 롤백
            throw new IllegalStateException(e);
        }
    }
}
```
__프록시 객체는 트랜잭션 작업을 처리하고 중간에 서비스 로직을 호출하는 역할을 하게 됩니다.__  
이로써 서비스 계층에서는 트랜잭션에 대한 코드를 작성하지 않아도 됩니다.  
스프링에서 제공하는 @Transactional 애노테이션이 해당 기능을 수행합니다.
```java
@Slf4j
@Service
@RequiredArgsConstructor
public class MemberServiceV3_3 {

    private final MemberRepositoryV3 memberRepository;

    @Transactional
    public void accountTransfer(String fromId, String toId, int money) throws SQLException {
        bizLogic(fromId, toId, money);
    }

    ... 생략
}
```
<br>

![그림13](https://github.com/backtony/blog-code/blob/master/spring/img/db/2/1-13.PNG?raw=true)  
결과적으로 Spring AOP인 @Transactional을 사용하면 위와 같은 방식으로 동작하게 됩니다.  

1. 요청이 들어오면 프록시가 호출됩니다.
2. 스프링 컨테이너를 통해 트랜잭션 매니저를 획득합니다.
3. DataSourceUtils.getConnection()을 호출해 트랜잭션을 시작합니다.
4. 데이터소스를 통해 커넥션을 생성합니다.
5. 만든 커넥션을 수동 커밋 모드로 변경하고나서야 실제 데이터베이스 트랜잭션을 시작합니다.
6. 커넥션을 트랜잭션 동기화 매니저에 보관합니다.
7. 보관된 커넥션은 스레드 로컬에서 멀티 스레드에 안전하게 보관됩니다.
8. 실제 서비스 로직을 호출합니다.
9. 리포지토리의 데이터 접근 로직에서는 트랜잭션 동기화 매니저에서 커넥션을 가져와 동작합니다.
10. 트랜잭션 처리 로직(AOP 프록시)으로 돌아와 성공이면 커밋하고 예외가 발생하면 롤백을 수행하고 트랜잭션을 종료합니다.


<Br>

## Spring 예외 추상화
각각의 데이터베이스마다 SQL ErrorCode가 전부 다릅니다.  
따라서 개발자가 처리하기 위해서는 모든 데이터베이스의 예외에 대해 알아야하고 각각 따로 처리해야 합니다.  
이러한 문제를 Spring에서는 추상화를 통해 해결하여 제공합니다.  

![그림14](https://github.com/backtony/blog-code/blob/master/spring/img/db/2/1-14.PNG?raw=true)  

예외의 최고 상위는 DataAccessException입니다.  
그림에서 보는 것처럼 런타임 예외를 상속 받았기 때문에 스프링이 제공하는 데이터 접근 계층의 모든 예외는 런타임 예외입니다.  
DataAccessException은 크게 2가지로 구분되는데 NonTransient 예외와 Transient 예외입니다.  
+ Transient
    - 일시적이라는 뜻으로 하위 예외는 동일한 SQL을 다시 시도했을 때 성공할 가능성이 존재합니다.
    - 예를 들어 쿼리 타임아웃, 락과 관련된 오류가 이에 해당합니다.
    - 이런 오류들은 데이터베이스 상태가 좋아지거나 락이 풀렸을 때 다시 시도하면 성공할 수 있습니다.
+ NonTransient
    - 일시적이지 않다는 뜻으로 같은 SQL을 그대로 반복해서 실행하면 실패합니다.
    - SQL 문법 오류, 데이터베이스 제약조건 위배 등이 이에 해당합니다.

단순 JPA를 사용할 경우 JPA 예외를 스프링 프레임워크가 제공하는 추상화된 예외로 변경하려면 스프링빈을 등록해야 합니다.
```java
@Bean
public PersistenceExceptionTranslationPostProcessor exceptionTranslation(){
    return new PersistenceExceptionTranslationPostProcessor();
}
```
이것은 @Repository 애노테이션을 사용한 곳에서 예외 변환 AOP를 적용해서 JPA 예외를 스프링 프레임워크가 추상화한 예외로 변환해줍니다.  
만약 Spring Data Jpa를 사용한다면 따로 설정할 필요 없이 알아서 추상화된 예외로 던져집니다.  

## 트랜잭션 롤백 주의사항
사실상 처리할 내용은 없지만 어떻게 동작하는지 알아둬야할 내용입니다.  
트랜잭션을 롤백하는 것은 데이터베이스의 반영사항만 롤백하는 것이지 수정한 자바 객체까지 원상태로 복구하는 것은 아닙니다.  
예를 들어 엔티티를 조회해서 수정하는 중에 문제가 있어서 트랜잭션을 롤백하면 데이터베이스의 데이터는 원래대로 복구되지만 객체는 수정된 상태로 영속성 컨텍스트에 남아있습니다.  
따라서 트랜잭션이 롤백된 영속성 컨텍스트를 그대로 사용하는 것은 위험합니다.  
이 경우에는 새로운 영속성 컨텍스트를 생성하여 사용하거나 EntityManager.clear()를 호출해서 영속성 컨텍스트를 초기화한 다음에 사용해야 합니다.  
스프링 프레임워크는 이런 문제를 예방하기 위해 영속성 컨텍스트의 범위에 따라 다른 방법을 사용합니다.  
기본 전략인 트랜잭션당 영속성 컨텍스트 전략은 문제가 발생하면 트랜잭션 AOP 종료 시점에 트랜잭션을 롤백하면서 영속성 컨텍스트도 함께 종료하므로 문제가 발생하지 않습니다.  
문제는 OSIV처럼 영속성 컨텍스트의 범위를 트랜잭션 범위보다 넓게 사용해서 여러 트랜잭션이 하나의 영속성 컨텍스트를 사용할 때 발생합니다.  
이때는 트랜잭션을 롤백해서 영속성 컨텍스트에 이상이 발생해도 다른 트랜잭션에서 해당 영속성 컨텍스트를 그대로 사용하는 문제가 있습니다.  
스프링 프레임워크는 영속성 컨텍스트의 범위를 트랜잭션의 범위보다 넓게 설정하면 트랜잭션 롤백시 영속성 컨텍스트를 초기화해서 잘못된 컨텍스트를 사용하는 문제를 예방해서 처리합니다.  




<Br><Br>

__참고__  
<a href="https://www.inflearn.com/course/%EC%8A%A4%ED%94%84%EB%A7%81-db-1#" target="_blank"> 스프링 DB 1편 - 데이터 접근 핵심 원리</a>   


