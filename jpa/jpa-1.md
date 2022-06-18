# JPA 기본 - application.yml 설정

## 1. jpa와 db설정
---
![그림7](https://github.com/backtony/blog-code/blob/master/jpa/img/1/1-7.PNG?raw=true)

properties나 yml이나 둘 중 하나 정해서 설정파일로 사용하면 되는데 무거워질수록 yml이 좋다고 한다. 따라서 properties는 지우고 yml을 따로 만들어서 사용한다.
```yml
spring:
  h2:
    console:
      enabled: true

  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
    username: sa
    password:

# 설정들은 spring.io에서 doc에서 찾아서 공부해야한다.
# jpa설정
  jpa:
    hibernate: # hibernate 사용 설정
    # 애플리케이션 실행 시점에 테이블을 다 지우고, 내가 가진 entity 정보를 보고 다시 테이블 자동 생성
    # if exists drop table 해주고 다시 만들어준다고 보면 된다.
      ddl-auto: create
    properties: # property 사용 설정
      hibernate: # hibernate property 설정
        format_sql: true

# 로그 레벨 설정
logging:
  level:
  # hibernate 가 남기는 모든 로그가 debug모드로 설정
  # jpa hibernate가 생성하는 sql이 로거를 통해서 찍히도록 하는 설정
    org.hibernate.SQL: debug
    org.hibernate.type: trace # 실제 들어가는 파라미터값 찍어주는 설정
```
![그림8](https://github.com/backtony/blog-code/blob/master/jpa/img/1/1-8.PNG?raw=true)

이렇게 로그에 hibernate가 만든 sql을 찍어준다. 그리고 실제 파라미터 정보를 알려준다. 실제 파라미터 정보를 좀 더 편하게 보여주는 라이브러리들이 있는데 [링크](https://github.com/gavlyukovskiy/spring-boot-data-source-decorator) 에서 찾아서 build.gradle에 추가해주면 된다. 버전 정보는 글 읽어보면 최신 버전이 적혀있으니 그걸로 적어주면 된다. 이런 편의를 위한 라이브러리는 배포할 때는 병목현상을 유발할 수 있으므로 개발시에는 편리해도 배포시에는 사용을 고려해봐야 한다.
<br>

__cf) 웹 콘솔로 h2 접속하기__  
![그림2](https://github.com/backtony/blog-code/blob/master/jpa/img/1/1-2.PNG?raw=true) 
localhost:8080/h2-console 에 접속해서 connect를 누르면 바로 이용 가능하다.  


<br>

__cf) mysql 연결__
```yml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/db명?serverTimezone=UTC&characterEncoding=UTF-8
    username: 아이디
    password: 패스워드
  jpa:
    database: mysql
    database-platform: org.hibernate.dialect.MySQL8Dialect
    show-sql: true
    hibernate:
      ddl-auto: create
    properties:
      hibernate:
        format_sql: true


logging:
  level:
    org.hibernate.SQL: debug
    org.hibernate.type: trace
```
<Br>

__cf) data.sql__  
resources의 data.sql을 적용하는 옵션으로 mode에는 always, never, embedded 가 있다.  
추가적으로 Hibernate 초기화를 통해 생성된 스키마에다가 데이터를 채우기를 위해서 data.sql가 실행되기를 원한다면 application.yml(또는 properties)에 spring.jpa.defer-datasource-initialization 옵션 값을 true로 추가해주어야 한다.
```yml
spring:
  sql:
    init:
      data-locations: classpath:data.sql
      username: root
      password: root
      mode: always
  jpa:
    defer-datasource-initialization: true
    database: mysql    
    database-platform: org.hibernate.dialect.MySQL8Dialect
    hibernate:
      ddl-auto: create-drop
    properties:
      hibernate.default_batch_fetch_size: 1000
      hibernate:
        format_sql: true
```

## 2. Properties와 profile
---
### Properties
application.properties 또는 application.yml 로 스프링부트 설정을 할 수 있다. resource 디렉토리 안에 있는데 main과 test 둘 다 사용할 수 있다. 예를 들어 application.properties가 main에도 있고 application.properties가 test에도 있다고 가정해보자. 먼저 main쪽에서 한번 빌드되고 test에서 한번 빌드가 되는 2단계를 거친다. 즉, test에서 빌드할 때는 이미 main에서 빌드한 application.properties를 test에 있는 것으로 아애 교체해버리는 것이다. 최종적으로는 main에서 사용 했던 설정이 사라지게 되는 것이므로 오류가 발생한다.  
해결하기 위한 방법은 main과 test에서의 application.properties의 이름을 다르게 주면 된다. main에서는 application.properties를 주고 test에서는 application-test.properties로 설정을 주면 이름이 다르기 때문에 덮어씌워지지 않는다. 결국 두개의 설정파일이 빌드된 상태인 것이다. 여기서 주의해야할 점은 application.properties가 항상 base 설정으로 들어간다는 것이다. 따라서 test에서 properties 파일을 만들어 놓아도 사용하겠다는 세팅을 해놓지 않으면 항상 application.properties로 적용된다. test에서는 application-test.properties 로 설정을 따르고 싶다면 클래스에 @ActiveProfile("test")를 붙여주면 일단은 application.properties가 적용된 상태에서 test의 properties로 override 적용되어 동작한다. application.properties 설정을 base로 가져가고 같은 설정정보가 있는 경우 test에 있는 설정으로 따르게 되는 것이다.  
main에서도 여러가지 properties를 만들 수 있는데 이것도 application.properties를 base로 하고 override하는 것이다. main에서 application-dev.properties를 만들고 이 properties로 main을 실행하고 싶다면 아래 그림의 빨간색 동그라미를 클릭하고 Edit Configurations 에서 Active profiles에서 dev를 넣고 돌리면 application-dev.properties가 적용된다.
![그림11](https://github.com/backtony/blog-code/blob/master/jpa/img/1/1-11.PNG?raw=true) 
<br>

### profile
profile은 설정에 따라 빈의 등록 유무를 결정하기 위해 사용한다.
```java
@Profile("prod")
@Component
public class ProTest {
    @Bean
    public String hello(){
        return "hello";
    }
}

// application.properties
spring.profiles.active=prod
```
위와 같이 코딩한 상태에서 그냥 실행하면 ProTest라는 빈은 등록되지 않는다. ProTest를 빈으로 등록하기 위해서는 application.properties에서 spring.profiles.active=프로파일이름 을 적어주면 빈으로 등록된다.  
<br>

### properties의 값들로 주입받기
application.properties에 적어둔 값을 사용할 수 있는 방법이 있다. 
```java
// XXApplication 
@SpringBootApplication
@ConfigurationPropertiesScan
public class SpringbootStartApplication {
    public static void main(String[] args) {
        SpringApplication.run(SpringbootStartApplication.class, args);
    }
}

// application.properties
tony.name=backtony
tony.age=26
tony.fullname=idontknow

// 아무 클래스 만들어서 설정값 받기
@ConstructorBinding // 
@ConfigurationProperties(prefix = "tony") // prefix로 application.properties에 적은 prefix 매칭
@Getter
@AllArgsConstructor
public class AppProperties {
    private String name;
    private int age;
    private String fullName;
}
```
+ 먼저 XXApplication 에 @ConfigurationPropertiesScan 애노테이션을 붙인다. 
  - 이로 인해 @ConfigurationProperties가 붙은 클래스가 빈으로 등록된다.
+ application.properties 에 원하는 값들을 적어 둔다.
+ 설정값 받을 클래스 만들고 Getter와 AllArgsConstructor 세팅해주고 @ConfigurationProperties(prefix = "") 와 @ConstructorBinding을 붙여준다.
  - 해당하는 값들이 생성자를 통해서 들어오게 되므로 Immutable하게 만들 수 있다.
+ properties에서 설장한 값이 한글일 때 깨지는 현상이 있는데 인텔리제이 설정에서 encoding -> file encoding에서 transparent native-to-ascii conversion 체크박스를 선택해주면 해결된다.


참고로 사용할 값이 몇개 없다면 @Value로 바로 가져와서 사용할 수 있다. 



<Br><Br>

__참고__  
<a href="https://www.inflearn.com/course/%EC%8A%A4%ED%94%84%EB%A7%81%EB%B6%80%ED%8A%B8-JPA-%ED%99%9C%EC%9A%A9-1/" target="_blank"> 실전! 스프링 부트와 JPA 활용1</a>   



