## 1. Flyway란?
---
![그림1](https://github.com/backtony/blog-code/blob/master/spring/img/db/1/1-1.PNG?raw=true)  
__Flyway는 데이터베이스의 형상관리를 목적으로 하는 툴__ 입니다.  
우리는 보통 소스 코드를 git을 이용해서 관리합니다. 이와 같은 기능으로 flyway는 데이터베이스 소스코드를 관리합니다.  
Flyway는 버전 관리 목적인 SCHEMA_VERSION 테이블을 통해 SQL 스크립트의 변화를 추적하면서 자동적으로 관리합니다.

<Br>

## 2. 적용하기
---
### build.gradle
```
implementation 'org.flywaydb:flyway-core'
```
의존성을 추가해줍니다.  

<br>

### application.yml
```yml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/flyway?serverTimezone=UTC&characterEncoding=UTF-8
    username: 아이디
    password: 패스워드
  jpa:
    database: mysql
    database-platform: org.hibernate.dialect.MySQL8Dialect
    show-sql: true
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        format_sql: true

  flyway:
    enabled: true 
    baseline-on-migrate: true 
    url: jdbc:mysql://localhost:3306/flyway?serverTimezone=UTC&characterEncoding=UTF-8
    user: 아이디
    password: 패스워드

logging:
  level:
    org.hibernate.SQL: debug
    org.hibernate.type: trace
```
다른 설정들은 단순한 database, jpa 설정이므로 flyway 설정만 살펴보겠습니다.  
+ enabled 
    - flyway를 사용하겠다는 설정으로 기본값은 true입니다.
+ baseline-on-migrate 
    - flyway는 버전 정보를 flyway_shcema_history 라는 테이블에서 관리하는데 해당 테이블을 자동으로 생성해주는 옵션입니다.
    - 기본값은 false로 히스토리 테이블이 이미 존재할 때 사용합니다.

<br>

### Member.java  
간단하게 Member 엔티티를 구성해보겠습니다.  
```java
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MEMBER_ID")
    private Long id;

    private String name;
}
```
<br>

### Flyway 작성하기
#### 패키지 구조
![그림2](https://github.com/backtony/blog-code/blob/master/spring/img/db/1/1-2.PNG?raw=true)  
패키지 구조는 위와 같습니다. 반드시 resources/db/migration 위치에서 작성해야 합니다.  
만약 경로를 변경하고 싶다면 application.properties 파일에 spring.flyway.location=파일위치 로 수정할 수 있습니다.
<Br><br>

#### sql 네이밍 규칙
![그림3](https://github.com/backtony/blog-code/blob/master/spring/img/db/1/1-3.PNG?raw=true)  
+ Prefix 
    - V, U, R 중 하나를 선택합니다.
    - V(Version) : 버전 마이그레이션
    - U(Undo) : 실행 취소
    - R(Repeatable) : 반복 가능한 마이그레이션
        - 버전에 상관없이 매번 실행되는 스크립트로 버전 명시가 필요 없습니다.
        - 예시로 테스트를 위해 매번 더미데이터를 넣을 때 사용합니다.
+ Separator : 구분자로 _ 가 2개인 것을 주의해야 합니다.
+ Description : 실질적 파일 명으로 밑줄이나 공백으로 단어를 구분합니다.
+ Suffix : 접미사로 보통 .sql을 사용합니다.

<br>

__V1\_\_init.sql__
```sql
CREATE TABLE IF NOT EXISTS `flyway`.`member`(
    `member_id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(255),
    PRIMARY KEY (`member_id`))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;
```
<br>

#### flyway_schema_history
![그림4](https://github.com/backtony/blog-code/blob/master/spring/img/db/1/1-4.PNG?raw=true)  
이제 스프링부트를 실행시켜주고 WorkBench를 이용해서 yml에 세팅해준 설정대로 생성된 flyway_schema_history를 확인해봅니다.  
version 컬럼에 1로 잘 저장된 것을 확인할 수 있습니다.  
__flyway로 등록된 순간을 기준으로 flyway가 DB버전 관리를 하게 되므로 해당 스크립트를 수정, 삭제를 하면 안됩니다.__
<br>

### 버전 업데이트
```java
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MEMBER_ID")
    private Long id;

    private String name;

    @Column(nullable = false)
    private int age;

}
```
Member에 age라는 새로운 컬럼을 추가해보겠습니다.  
이 상태에서 바로 스프링을 돌려보면 아래와 같은 에러가 발생합니다.
![그림5](https://github.com/backtony/blog-code/blob/master/spring/img/db/1/1-5.PNG?raw=true)  
이유는 flyway에게 업데이트된 정보를 알려주지 않았기 때문입니다.
<Br><Br>

![그림6](https://github.com/backtony/blog-code/blob/master/spring/img/db/1/1-6.PNG?raw=true)  
V2로 새롭게 버전 정보를 만들어주고 아래 코드를 입력합니다.
```sql
ALTER TABLE member ADD COLUMN age integer default 0
```
![그림7](https://github.com/backtony/blog-code/blob/master/spring/img/db/1/1-7.PNG?raw=true)  
스프링을 다시 띄워주고 테이블을 확인해보면 version 2가 입력되어 있는 것을 확인할 수 있습니다.

<br>

## 3. 기존 데이터나 migration 이력이 존재하는 경우
---
앞선 적용하기에서는 초기에 DB에 아무런 데이터가 없을 때 사용한 flyway 였습니다.  
따라서 기존 데이터나 migration 이력이 존재한다면 조금 다르게 접근해야 합니다.
<br>

### 기존 테이블 정보, 데이터만 있는 경우
__1. application.yml에 설정을 추가합니다.__  
```yml
spring:  
  # ... 생략

  flyway:
    enabled: true 
    baseline-on-migrate: true 
    url: jdbc:mysql://localhost:3306/flyway?serverTimezone=UTC&characterEncoding=UTF-8
    user: 아이디
    password: 패스워드
```
<br>

__2. resources/db/migration 위치에 V1 migration script를 작성합니다.__  
이때 반드시 스크립트 파일이 있어야 합니다.
특별한 버전1 없이, 그냥 기존 테이블과 데이터를 유지하고자 해도 빈 script를 작성해야 합니다.  
즉, 파일 자체는 있어야 한다는 의미입니다.  
이렇게 세팅해주면 끝입니다.
<br>

### flyway_schema_history 테이블이 있는 경우
기본 테이블 정보, 데이터, flyway_schema_history 테이블 까지 있는 경우 입니다.  
__1. application.yml 설정을 수정합니다.__  
```yml
spring:  
  # ... 생략

  flyway:
    enabled: true 
    baseline-on-migrate: false
    url: jdbc:mysql://localhost:3306/flyway?serverTimezone=UTC&characterEncoding=UTF-8
    user: 아이디
    password: 패스워드
```
baseline-on-migrate 옵션을 false로 해줍니다. 기본값이 false 입니다.  
<br>

__2-1. migration script가 남아있지 않은 경우, 히스토리 내역 최신보다 더 높은 버전으로 빈 스크립트를 추가하고 실행합니다.__  
예를 들어, flyway_schema_history를 조회했을 때, 가장 최신 버전이 4라면, V5로 시작하는 스크립트를 작성하면 됩니다. 이때도 마찬가지로 비어있더라도 반드시 스크립트 파일이 있어야 합니다.  
<br>

__2-2. migration script가 남아있는 경우 resources/db/migration 위치에 기존 히스토리 내역과 일치하는 스크립트를 그대로 추가하고 실행합니다.__  
<br>

__3. flyway_schema_history 테이블에서 <<Flyway Base>> 이 표시됨을 확인할 수 있을 것입니다.__





<Br><Br>

__참고__  
<a href="https://www.youtube.com/watch?v=pxDlj5jA9z4&t" target="_blank"> 우아한 테크코스 코기의 Flyway</a>   

