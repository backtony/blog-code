# Spring Test - TestContainer + MySQL 모든 테스트에서 공유하기


## 1. 운영과 로컬에서의 운용 DB가 다른 경우의 문제점
---
+ 운영 DB와 In-Memory DB의 문법이 100% 호환되지 않는다.
+ In-Memory DB는 정상 동작하나 운영 DB에서는 동작하지 않을 수 있다.

유닛테스트에서는 DB를 사용하지 않거나 간단한 쿼리 정도를 사용하게 되므로 In-Memory DB를 사용해도 괜찮지만 복잡한 쿼리를 포함하여 진행하는 통합 테스트의 경우에는 In-Memory DB를 사용할 경우 위와 같은 문제들이 생길 수 있습니다. 저 또한 최근에 진행하는 프로젝트에서 벌크성 쿼리 관련하여 H2에서 잘 찍히지 않은 경험이 있습니다. 이에 대한 방안으로 TestContainer를 사용하여 운영환경과 DB를 일치시킬 수 있는 방법이 있습니다.  
<br>

### 그럼 항상 Test DB환경을 운영환경과 일치시켜야할까?
이에 대해 많은 고민과 구글링을 시도해 본 결과 다음과 같은 자료들을 찾을 수 있었습니다.  
[Remove the in-memory provider ](https://github.com/dotnet/efcore/issues/18457)  
[Avoid In-Memory Databases for Tests](https://jimmybogard.com/avoid-in-memory-databases-for-tests/)  
[Don't use In-Memory Databases (H2, Fongo) for Tests](https://phauer.com/2017/dont-use-in-memory-databases-tests-h2/)  

이에 관해 많은 논쟁이 있는 것 같았고 정답은 없는 것 같습니다. 깃허브 링크 논쟁에서는 유닛 테스트에서는 In-Memory DB로 충분하고 통합테스트에서는 Real DB를 사용해야 한다고 말하고 있고, 깃허브를 제외한 두 링크에서는 H2를 사용하는 것은 유닛테스트에서도 그닥 이점이 없다고 말하고 있습니다.  
여담으로 저는 현재 진행하는 프로젝트에서 실제 운용 DB인 MySQL를 사용해야 확인할 수 있는 Test가 발생하였기 때문에 TestContainer을 사용해야 하는 상황에 있었습니다. TestContainer을 띄워서 사용하면 해당 컨테이너를 모든 테스트에서 공유할 수 있어, 하나의 Test에서 TestContainer을 사용하는 순간 굳이 다른 테스트에서 H2를 사용해야할 필요성이 없어졌습니다. 따라서 모든 RepositoryTest에 환경을 일치시키기로 했습니다.
<br><br>

## 2. 세팅하기
---
```yml
# build.gradle
testImplementation group: 'org.testcontainers', name: 'testcontainers', version: '1.15.3'
testImplementation group: 'org.testcontainers', name: 'junit-jupiter', version: '1.15.3'
testImplementation group: 'org.testcontainers', name: 'mysql', version: '1.15.3'

# /test/resources/application-test.yml
spring:
  datasource:
    url: jdbc:tc:mysql:8.0.23:///?rewriteBatchedStatements=true&profileSQL=true&logger=Slf4JLogger&maxQuerySizeToLog=999999
    driver-class-name: org.testcontainers.jdbc.ContainerDatabaseDriver
    username:
    password:
    hikari:
      maximum-pool-size: 5
  sql:
    init:
      data-locations: classpath:data.sql
      username:
      password:
      mode: never

  jpa:
    hibernate:
      ddl-auto: update # create-drop 하면 @datajpatest 있는곳마다 새로 entity을 올림
```
해당 설정은 Batch Insert 혹은 JDBC native bulk Insert 쿼리를 사용하고, data.sql을 사용할 때 설정한 yml 파일입니다. data.sql파일을 사용하지 않으시고 벌크 Insert도 사용하지 않으신다면 url의 /// 이후의 세팅과 sql: 세팅을 지우시면 됩니다. 참고로 해당 yml은 반드시 test의 resource에 작성해야합니다. main쪽의 resource에 작성하게 되면 driver-class-name을 찾아오지 못합니다.   

+ [MVNrepository](https://mvnrepository.com/search?q=org.testcontainers%3Atestcontainers) 에서 org.testcontainers:testcontainers 를 검색해서 나오는 testcontainers core, junit jupiter extension, 선택할 DB 이렇게 3개의 의존성을 받아 build.gradle 에 추가해 줍니다.
+ url : jdbc:tc:mysql:이미지버전:/// 여기까지가 기본적인 설정입니다. 기존에 사용하는 url, 포트, db명은 작성할 필요가 없이 ///로 작성합니다. 테스트를 위해서만 필요하기 때문에 다음과 같이 세팅해두면 DB명은 test로 설정되고 알아서 빈 포트를 찾아 뜨게 되고 테스트가 끝나면 꺼집니다. 뒤쪽의 ? 이후의 것들은 bulk Insert를 사용할 경우 찍히는 쿼리를 보여주는 설정으로 bulk insert를 사용하지 않으신다면 /// 뒤쪽은 지우셔도 무방합니다.
+ sql : 이후 설정은 data.sql 관련 설정으로 mode는 never로 해주었습니다. 저는 모든 @DataJpaTest에서 해당 컨테이너를 사용할 것이기 때문에 always를 사용하게 되면 @DataJpaTest가 붙은 클래스마다 data.sql이 실행하게 됩니다. 따라서 저는 테스트 코드도 data.sql에 의존하지 않도록 코딩하고 none으로 data.sql이 실행되지 않도록 했습니다.
+ ddl-auto : update 이 설정도 sql설정돠 비슷한 이유입니다. 저는 해당 컨테이너를 모든 DB Test에서 공유할 것이기 때문에 create-drop을 할 경우 @DataJpaTest가 붙은 클래스마다 삭제되고 올라가는 과정을 반복하게 되어 낭비가 심합니다. 따라서 update로 한 번 올린 것을 계속 사용하도록 하였습니다.

<br><br>

## 3. test에 적용하기
```java
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class UserRoleRepositoryTest{   

    @Test
    public void test(){
        // test
    }   
}
```
+ @ActiveProfiles("test") : application-test.yml 을 사용하도록 세팅합니다.
+ @AutoConfigureTestDatabase(replace = 옵션값 )
    - Replace.Any : 기본적으로 내장된 Embedded 데이터베이스를 사용합니다.
    - Replace.NONO : 설정한 프로퍼티에 따라 데이터소스가 적용됩니다.(none을 사용하여 docker MySQL을 사용)

이렇게 세팅해두면 하나의 클래스에 여러 테스트가 있을 경우뿐만 아니라, 다른 클래스에서도 같은 컨테이너를 공유합니다. 

