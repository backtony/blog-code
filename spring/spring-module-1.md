# Spring - Gradle 멀티 모듈 프로젝트


## 멀티 모듈이 필요한 이유
![그림1](https://github.com/backtony/blog-code/blob/master/spring/img/module/1/1-1.PNG?raw=true)  

프로젝트가 커지다보면 여러 개의 서버를 만들어야할 때가 있습니다.  
간단한 예시로 위 그림과 같이, WEB 서버와 Batch 서버가 있을 수 있습니다.  
만약 WEB 서버를 만들다가 Batch 서버를 추가해야 하는 시점에 WEB 서버에 있는 Member 엔티티를 Batch 서버에서도 사용해야 한다면 어떻게 사용할 것인가에 대해 고민해야하는 시점이 옵니다.  
가장 쉽고 간단한 방법은 Member 클래스 파일을 그대로 복사해서 만들고 사용하는 방식입니다.  
하지만 이럴 경우 연동되는 프로젝트가 늘어날 경우, Member 클래스 코드에 수정이 필요한 경우 곳곳에 퍼져있는 코드를 수정해야하기 때문에 실수할 여지가 많아집니다.  

<br>

이 문제를 해결할 수 있는 방법이 멀티 모듈입니다.  
멀티 모듈은 하나의 공통 프로젝트를 두고, 이 프로젝트를 여러 프로젝트에서 가져가서 사용할 수 있도록 기능을 제공합니다.  

## 모듈 생성
![그림2](https://github.com/backtony/blog-code/blob/master/spring/img/module/1/1-2.PNG?raw=true)  

Gradle로 새로운 프로젝트를 만들어줍니다.  
방금 만든 새로운 프로젝트가 모든 프로젝트의 Root가 됩니다.  

<br><br>

![그림3](https://github.com/backtony/blog-code/blob/master/spring/img/module/1/1-3.PNG?raw=true)  

이제 여러 개의 모듈을 만들 차례입니다.  
root 모듈을 클릭하고 Module을 클릭합니다.

<br><Br>

![그림4](https://github.com/backtony/blog-code/blob/master/spring/img/module/1/1-4.PNG?raw=true)  

Gradle을 선택하고 만들어줍니다.  
이 과정을 반복해서 여러 개의 모듈을 만들어줍니다.  
저는 core, batch, web 모듈을 만들었습니다.  

<br><Br>

![그림5](https://github.com/backtony/blog-code/blob/master/spring/img/module/1/1-5.PNG?raw=true)  

Root 프로젝트에는 src가 필요 없으니 삭제해줍니다.  

## settings.gradle
![그림6](https://github.com/backtony/blog-code/blob/master/spring/img/module/1/1-6.PNG?raw=true)  

Root 모듈의 settings.gradle에서 현재 Root 프로젝트가 하위 모듈로 어떤 프로젝트를 관리하는지를 명시해줍니다.  
위 코드는 gradle-multi-module 프로젝트가 'module-batch', 'module-core', 'module-web' 프로젝트를 하위 프로젝트로 관리하겠다는 의미입니다.  

## Root build.gradle

### 레거시 버전
```groovy
buildscript {
    ext {
        springBootVersion = '1.5.1.RELEASE'
    }
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("org.springframework.boot:spring-boot-gradle-plugin:${springBootVersion}")
        classpath "io.spring.gradle:dependency-management-plugin:0.6.0.RELEASE"
    }
}

subprojects {
    group 'com.blogcode'
    version '1.0'

    apply plugin: 'java'
    apply plugin: 'spring-boot'
    apply plugin: 'io.spring.dependency-management'

    sourceCompatibility = 1.8

    repositories {
        mavenCentral()
    }

    dependencies {
        testCompile group: 'junit', name: 'junit', version: '4.12'
    }
}

project(':module-api') {
    dependencies {
        compile project(':module-common')
    }
}

project(':module-web') {
    dependencies {
        compile project(':module-common')
    }
}
```
[이동욱님 블로그](https://jojoldu.tistory.com/123){:target="_blank"}의 멀티 모듈 build.gradle로 가져온 내용으로 레거시 버전에 해당한다고 합니다.  
제가 앞으로 작성할 설정과는 다르지만 레거시 버전이 어떤 형식으로 되어있는지 보여드리기 위해 가져왔습니다.  

### 현재 버전
```groovy
plugins {
    id 'org.springframework.boot' version '2.7.0'
    id 'java'
}


repositories {
    mavenCentral()
}

bootJar.enabled = false

subprojects {
    group = 'com.example'
    version = '0.0.1-SNAPSHOT'
    sourceCompatibility = '11'

    apply plugin: 'java'
    // build.gradle에서 api() 를 사용하려면 java-library 사용
    apply plugin: 'java-library' 
    apply plugin: 'org.springframework.boot'
    // spring boot dependency를 사용하여 사용중인 부트 버전에서 자동으로 의존성을 가져온다.
    apply plugin: 'io.spring.dependency-management' 

    configurations {
        compileOnly {
            extendsFrom annotationProcessor
        }
    }

    repositories {
        mavenCentral()
    }

    // 관리하는 모듈에 공통 dependencies
    dependencies {
        compileOnly 'org.projectlombok:lombok'
        annotationProcessor 'org.projectlombok:lombok'
        testImplementation 'org.springframework.boot:spring-boot-starter-test'
    }

    test {
        useJUnitPlatform()
    }
}
```
+ plugins
    - plugins란 미리 구성해놓은 task들의 그룹이며 특정 빌드과정에 필요한 기본정보를 포함하고 있습니다.
    - [spring plugin reference](https://docs.spring.io/spring-boot/docs/current/gradle-plugin/reference/htmlsingle/){:target="_blank"}        
    - id 'org.springframework.boot' version '2.7.0'
        - Spring Boot Gradle 플러그인으로 사용하면 Spring Boot 종속성을 관리하고 Gradle을 빌드 도구로 사용할 때 애플리케이션을 패키징하고 실행할 수 있습니다.
        - 단독으로 사용되는 경우 프로젝트에 거의 영향을 주지 않습니다.
        - 예를 들어 java 플러그인과 함께 적용되면 실행 가능한 jar 빌드 작업이 자동으로 구성됩니다.
        - spring-boot-dependencies를 통해서 의존성 관리 기능을 제공하기도 합니다.
+ repositories
    - 각종 의존성(라이브러리)들을 어떤 원격 저장소에서 받을지를 정해줍니다.
    - mavenCentral()이 기본으로 각종 의존성을 mavenCentral()에서 받아온다는 의미입니다.
    - jcenter() 저장소도 있지만 intelliJ에서 작성하면 경고가 뜨는데 곧 서비스가 종료된다고 합니다.
+ bootJar.enabled = false
    - bootJar 작업은 실행 가능한 jar을 생성하려고 시도하기 때문에 이를 위해서는 main() 메서드가 필요합니다.
    - Root 프로젝트는 main 없이 라이브러리의 역할을 하는 모듈이기 때문에 false로 비활성화해줍니다.
+ subprojects
    - settings.gradle에 include된 프로젝트 전부에 대한 공통 사항을 명시합니다.(루트는 제외)
    - subprojects 블록 안에서는 plugins 블록을 사용할 수 없으므로 apply plugin을 사용해야 합니다.


## 모듈별 설정
### 모듈 패키지 구조
![그림7](https://github.com/backtony/blog-code/blob/master/spring/img/module/1/1-7.PNG?raw=true)  

패키지 컨벤션으로 위와 같은 구조로 잡는다고 가정한다면 Application.java를 기준으로 해당 클래스가 속해 있는 패키지가 componentScan의 base Package가 되면서 그 하위를 스캔하게 됩니다.  

<br>

![그림8](https://github.com/backtony/blog-code/blob/master/spring/img/module/1/1-8.PNG?raw=true)  
패키지 컨벤션에서 알 수 있듯이 멀티 모듈을 구성하게 되면 괴리감이 생기게 됩니다.  
그림처럼 멀티 모듈을 합쳐 놓고 보면 Application.java의 범위(스캔 범위)에 다른 모듈이 포함되지 않는 문제가 발생합니다.  

<br>

![그림9](https://github.com/backtony/blog-code/blob/master/spring/img/module/1/1-9.PNG?raw=true)  

이 문제를 해결하기 위해 @SpringBootApplication에 옵션으로 여러 패키지를 추가하는 등의 작업을 해야하는데 이 작업은 매우 번거롭고 실수하기 쉽습니다.  

<br>

![그림10](https://github.com/backtony/blog-code/blob/master/spring/img/module/1/1-10.PNG?raw=true)  
이를 개선한 방법이 Application.java를 모듈 하위가 아니라 모듈과 동일 레벨에 위치시키면 이를 해결할 수 있습니다.  
이런 구조를 가지고 모듈의 패키지 구조를 만들게 되면 결국에 다른 모듈을 가져와 사용하게 되어도 현재 Application.java의 하위에 위치하게 되므로 추가적인 작업없이 바로 사용할 수 있습니다.  



### module-core
core 쪽에는 공통적으로 사용하는 domain, repository, __domain service(트랜잭션 단위)__ 를 작성해줍니다.  
여기서 domain service에 대해서 잠깐 설명하고 넘어가겠습니다.  
간단한 어플리케이션의 경우에는 domain service없이 어플리케이션 패키지에서 한 개의 service에 @Transactional을 붙이고 사용합니다.  
하지만 멀티 모듈로 구성한다는 자체가 이미 어플리케이션이 복잡하다는 의미고 복잡해지게 된다면 하나의 service가 아니라 여러 개의 domain service를 조합하여 service를 만들게 될 가능성이 큽니다.  
예를 들면, 하나의 요청에서 결제와 알림 로직을 동작시켜야 한다면 결제 service와 알림 service가 있을 것이고 각 서비스에 @Transactional이 붙어서 동작하게 될 것입니다. 이게 domain-service에 해당하고 이것들을 조합해서 하나의 service를 만들게 되는 것입니다.  
<br>

간단하게 Member 관련 코드를 추가하겠습니다.  

#### 패키지 구조  
![그림11](https://github.com/backtony/blog-code/blob/master/spring/img/module/1/1-11.PNG?raw=true)  
core 모듈은 실행이 필요 없기 때문에 Application.java가 존재하지 않습니다.  

<br>

__domain 패키지__  
```java
@Getter
@Entity
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;

    @Column
    private String email;

    @Column
    private String nickname;

}
```
```java
public interface MemberRepository {
    Member save(Member member);

    Optional<Member> findById(Long id);
}
```
<br>

__application 패키지__  
```java
@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    @Transactional
    public void saveAnyMember(){
        memberRepository.save(Member.builder().name("web").build());
    }

    @Transactional(readOnly = true)
    public Member findAnyMember(){
        return memberRepository.findById(1L).get();
    }
}
```
<Br>

__infrastructure 패키지__  
```java
public interface MemberJpaRepository extends JpaRepository<Member,Long> {
}
```
```java
@Repository
@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepository {

    private final MemberJpaRepository memberJpaRepository;

    @Override
    public Member save(Member member) {
        return memberJpaRepository.save(member);
    }

    @Override
    public Optional<Member> findById(Long id) {
        return memberJpaRepository.findById(id);
    }
}
```

#### build.gradle
```groovy
bootJar { enabled = false }
jar { enabled = true }

dependencies {
    api 'org.springframework.boot:spring-boot-starter-data-jpa'
    runtimeOnly 'com.h2database:h2'
}
```
+ bootJar, jar
    - core 모듈의 경우 main 메서드 없이 라이브러리 역할을 하는 모듈이므로 BootJar이 아닌 jar파일로 생성되고 다른 프로젝트에 첨부될 것입니다.
    - 따라서 bootJar은 false, jar은 true로 설정합니다.
+ implementation vs api
    - api는 compile이 deprecated되고 대체된 방식입니다.
    - implementation은 하위 의존에 대한 접근을 제한하고 api는 공개합니다.

```java
// A Module
public class A 

// B Module
api project(':A')

// C Module
implementation project(':B')

public class C {
  public void act() {
    new A() // compile error
  }
}
```
C Module에서 implementation을 사용해서 B를 사용했기 때문에 B에서 제공하는 기능만 사용 가능하고 B 모듈에서 가져온 다른 의존성에 대해서는 접근할 수 없습니다.  
C -> B -> A 에서 B까지만 접근할 수 있다는 의미입니다.  
__그래서 보통 개발할 때 domain 모듈 계층을 최종으로 조합하여 사용하는 어플리케이션 모듈 계층에서는 implementation을 사용하고, 그 외 계층에서는 api 방식을 사용하여 개발합니다.__  

#### 테스트
![그림15](https://github.com/backtony/blog-code/blob/master/spring/img/module/1/1-15.PNG?raw=true)  
module-core는 Application.java 파일이 없기 아무 설정 없이 테스트를 돌리면 위와 같은 문제가 발생합니다.  
@SpringBootApplication과 같은 Spring Context를 불러오는 포인트가 없기 때문입니다.  
<br>

![그림16](https://github.com/backtony/blog-code/blob/master/spring/img/module/1/1-16.PNG?raw=true)  
이를 위해 임시 시작 포인트용 클래스를 만들어서 @SpringBootApplication을 추가해 주면 해결할 수 있습니다.  


### module-batch
제가 정의한 module-batch는 어플리케이션 모듈 계층으로 core 도메인 모듈을 조합해서 사용하는 최종 모듈입니다.  
간단하게 컨트롤러와 서비스 계층 코드를 작성해봅시다.  

#### 패키지 구조
![그림12](https://github.com/backtony/blog-code/blob/master/spring/img/module/1/1-12.PNG?raw=true)  
패키지 구조는 앞서 설명한 것과 동일합니다.  

<br>

__application 패키지__  
```java
/**
 * 지금은 간단해서 하나씩만 있지만 복잡해지게 되면
 * 여러 domain service 모듈을 조합하여 로직 작성
 */

@Service
@RequiredArgsConstructor
public class BatchFacade {

    private final MemberService memberService;

    public void saveAnyMember(){
        memberService.saveAnyMember();
    }

    public Member findAnyMember(){
        return memberService.findAnyMember();
    }
}
```
domain core 모듈에서 domain-service를 __트랜잭션 단위__ 로 정의했습니다.  
따라서, 사용하는 최종 애플리케이션 모듈에서는 domain-service를 조합하여 service를 만들어 줍니다.  

<br>

__presentation 패키지__  
```java
@RestController
@RequiredArgsConstructor
public class BatchController {

    private final BatchFacade batchFacade;

    @PostMapping("/")
    public void saveAnyMember(){
        batchFacade.saveAnyMember();
    }

    @GetMapping("/")
    public Member getNewMember(){
        return batchFacade.findAnyMember();
    }
}
```

#### build.gradle
```groovy
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation project(':module-core')
}
```
batch 모듈은 실행이 필요하므로 앞서 core 모듈에서 작성했던 bootJar 세팅을 해주지 않습니다.  
implementation으로 module-core를 사용하도록 해줍니다.  

#### application.yml
```yml
spring:
  profiles:
    include: core
```
core 모듈에서 application-core.yml을 작성해 놓았다면 이를 include 해줄 수 있습니다.  


### module-web
module-web의 경우 module-batch와 완전히 동일하게 작성했기 때문에 포스팅 최상단에 Github에서 확인부탁드립니다.  



## 확인
![그림13](https://github.com/backtony/blog-code/blob/master/spring/img/module/1/1-13.PNG?raw=true)  
<Br>

![그림14](https://github.com/backtony/blog-code/blob/master/spring/img/module/1/1-14.PNG?raw=true)  

간단하게 postman으로 테스트해 본 결과 잘 동작하는 것을 확인할 수 있습니다.  




<Br><Br>

__참고__  
<a href="https://velog.io/@yangju0411/%EC%8A%A4%ED%94%84%EB%A7%81-%EB%B6%80%ED%8A%B8-build.gradle-%EC%9E%91%EC%84%B1%ED%95%98%EA%B8%B0-%EA%B0%84%EB%8B%A8-%EC%A0%95%EB%A6%AC" target="_blank"> 스프링 부트 build.gradle 작성하기 간단 정리</a>   
<a href="https://www.youtube.com/watch?v=nH382BcycHc&t=3733s" target="_blank"> [우아한테크세미나] 190829 우아한멀티모듈 by 우아한형제들 권용근님</a>   
<a href="https://techblog.woowahan.com/2637/" target="_blank"> 멀티모듈 설계 이야기 with Spring, Gradle</a>   
<a href="https://jojoldu.tistory.com/123" target="_blank"> Gradle 멀티 프로젝트 관리</a>   



