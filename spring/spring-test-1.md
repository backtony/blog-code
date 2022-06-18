# Spring Test - Assertion, Mockito, SpringBootTest, WebMvcTest

## 1. @DisplayName
---
```java
@DisplayName("회원 생성")
@Test
void createMember() throws Exception{
}
```
+ 어떤 테스트인지 테스트 이름을 보다 쉽게 표현할 수 있는 방법을 제공하는 애노테이션

<Br>

## 2. Assertion
---
![그림1](https://github.com/backtony/blog-code/blob/master/spring/img/test/1/1-1.PNG?raw=true)

+ org.junit.jupiter.api.Assertions.*

<Br>

```java
assetEquals("test","hello","String으로 메시지를 주면 항상 연산");
assetEquals("test","hello",()-> "람다식을 사용하면 실패할때 즉 필요할때만 연산된다.");
```
+ 마지막 인자로 message를 줄 수 있음
    - 바로 String을 줄 경우 항상 연산되므로 부담되는 경우 람다식을 사용
    
<br>

```java
assetThrows(IllegalArgumentException.class, ()-> new Study(-10))
```
+ 람다식이 실행될 때 해당 예외가 발생하는지 판단

<br>

```java
assetAll(
    () -> assertNotNull(study),
    () -> assertEquals("test","test")
);
```
+ 묶어서 한번에 실행

<br>

```java
assertThat(1).isGreaterThan(0);
```
+ assertThat 뒤에 자동완성 기능을 사용하면 다양한 api가 존재

<br>

## 3. Mockito
---
+ spring boot를 사용하면 자동으로 starter-test에서 의존성으로 들어옴
+ Mock : 진짜 객체와 비슷하게 동작하지만 프로그래머가 직접 그 객체의 행동을 관리하는 객체
+ Mockito : Mock 객체를 쉽게 만들고 관리하고 검증할 수 있는 방법을 제공

```java
MemberService memberService = Mockito.mock(MemberService.class);
StudyRepository studyRepository = mock(StudyRepository.class); // static import로 단축

// 애노테이션으로 더 단축
@ExtendWith(MockitoExtension.class)
class StudyServiceTest {

    @Mock
    MemberService memberService;

    @Mock
    StudyRepository studyRepository;
}
```
<br>

### Mock 객체 Stubbing
+ Mock 객체의 행동을 조작하는 행위
+ 모든 Mock 객체의 기본 행동
    - 리턴값이 있는 경우 NULL 반환, Optional 타입은 Optional.empty 반환
    - Primitive 타입은 기본 Primitive 값
    - 컬렉션은 비어있는 컬렉션
    - Void 메서드는 예외를 던지지 않고 아무런 일도 발생하지 않음

```java
// 이게 정의되지 않은 상태라도 이 메서드가 호출되면 하는 행위를 지정 가능
memberService.findById(1L);

// 위 메서드 호출시 다음과 같은 동작을 수행하길 원한다.
Member member = new Member();
member.setId(1L);
member.setEmail("test@gmail.com");

// Stubbing수행    
Mockito.when(memberService.findById(1L)).thenReturn(member); // Mockito는 static import로 단축 가능

// 파라미터를 아무거나 -> any()
Mockito.when(memberService.findById(any())).thenReturn(member);

// 에러 던지기
Mockito.when(memberService.findById(any())).thenThrow(new RuntimeException());
doThrow(new RuntimeException()).when(memberService).findById(1L);

// 순서에 따른 다른 동작
// 첫번째는 에러, 두번째는 멤버, 세번째는 다시 에러 던짐
Mockito.when(authService.login(any()))
                .thenThrow(new RuntimeException())
                .thenReturn(member)
                .thenThrow(new RuntimeException());
```

<br>

### verify
+ mock 객체가 제대로 동작하는지 확인하는 방법

```java
// mock으로 만든 memberservice의 notify메서드가 인자 study를 가지고 1번 호출되었는지 확인
verify(memberService, times(1)).notify(study); // static import로 Mockito 단축

// validate메서드가 어떤 인자에 대해서도 전혀 호출 X
verify(memberService, never()).validate(any());
```

<br>

### Mockito BDD 스타일 API
+ 애플리케이션이 어떻게 행동해야 하는지에 대한 공통된 이해를 구성하는 방법으로 TDD에서 창안된 방식
+ 앞서 mockito에서 사용한 방식을 tdd 형식으로 사용하는 것
+ BDDMockito 사용 

```java
given(memberService.findById(1L)).willReturn(member); // when과 동일하지만 api만 다름

then(memberService).should(times(1)).notify(study);// verify 대신
then(memberService).shouldHaveNoMoreInteractions(); // 더이상 호출 X
```


<br>

## 4. Testcontainers
---
+ 테스트에서 도커 컨테이너를 실행할 수 있는 라이브러리
+ 테스트 실행시 DB를 설정하거나 별도의 프로그램 또는 스크립트를 실행할 필요 X
+ 보다 Production에 가까운 테스트를 만들 수 있음
+ 단점은 테스트가 느려짐
+ [[공식 링크](https://www.testcontainers.org/)]

```xml
<!-- 의존성 추가-->
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>junit-jupiter</artifactId>
    <version>1.15.1</version>
    <scope>test</scope>
</dependency>
```
+ @Testcontainers
    - JUnit 5 확장팩으로 테스트 클래스에 @Container를 사용한 필드를 찾아서 컨테이너 라이프사이클 관련 메소드를 실행
+ @Container
    - 인스턴스 필드에 사용하면 모든 테스트 마다 컨테이너를 재시작 하고, 스태틱 필드에 사용하면 클래스 내부 모든 테스트에서 동일한 컨테이너를 재사용
+ 여러 DB 모듈을 제공하는데 각 DB에 맞는 의존성을 추가해줘야 함
    - [[링크](https://www.testcontainers.org/modules/databases/)]

```xml
<!-- postgresql 의존성 추가-->
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>postgresql</artifactId>
    <version>1.15.1</version>
    <scope>test</scope>
</dependency>
```

```java
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@Transactional
class StudyTest{
    @Container
    static PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer()
        .withDatabaseName("studytest"); // db 이름 주기
}
```
+ static을 붙이지 않으면 매 test마다 컨테이너가 새로 만들어짐 -> static을 사용하면 공유해서 사용
+ 컨테이너는 임의의 포트로 뜨게 되므로 설정이 필요함

<br>

```
spring.datasource.url=jdbc:tc:postgresql:///studytest
spring.datasource.driver-class-name=org.testcontainers.jdbc.ContainerDatabaseDriver
```
+ application-test.properties 설정파일
+ hostname과 port는 중요하지 않고 studytest는 DB 이름
+ 각 DB 설정은 [[링크](https://www.testcontainers.org/modules/databases/jdbc/)] 참고

<br>

### docker-compose
```java
 @Container
static DockerComposeContainer composeContainer =
        new DockerComposeContainer(new File("src/test/resources/docker-compose.yml"))
        .withExposedService("study-db", 5432);
```
```yml
# test resource에 있는 docker-compose.yml

version: "3"

services:
  study-db:
    image: postgres
    ports:
      - 5432 # 포트매핑을 :로 안해줬음 -> 랜덤으로 뜨기 때문
    environment:
      POSTGRES_PASSWORD: study
      POSTGRES_USER: study
      POSTGRES_DB: study
```
<br>

## 5. @SpringBootTest VS @WebMvcTest
---
### @SpringBootTest
+ @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK) 이 디폴트 값으로 옵션을 주지 않으면 자동으로 적용된다. 이 설정에 의해서 서블릿 컨테이너가 모킹된다. 
    - 내장톰캣을 실행하지 않고 MockMvc를 빈으로 등록하지 않는다.
    - 모킹한 객체를 의존성 주입받기 위해서 @AutoCOnfigureMockMvc 와 같이 사용한다. 
    - @AutoConfigureMockMvc 는 @WebMvcTest와 비슷한 역할은 하는데 가장 큰 차이점은 컨트롤러뿐만 아니라 테스트 대상이 아닌 @Service, @Repository가 붙은 객체들도 모두 메모리에 올린다는 점이다.
+ 스프링이 관리하는 모든 빈을 등록시켜서 테스트하기 때문에 무겁다.
+ Mockmvc 를 주입받을 때 webApplicationContext를 세팅하지 않고 autowired로 주입받은 것을 사용하면 post 요청시 403에러가 터진다. 따라서 세팅이 필요하다. 세팅하지 않고 사용하고 싶다면 요청마다 with(csrf())를 해줘야 한다. 필터를 추가해 준 것은 한글깨짐을 방지하기 위해서이다.


```java
@SpringBootTest
@AutoConfigureMockMvc
public class TestJpaRestControllerTest {

MockMvc mvc;
@Autowired WebApplicationContext webApplicationContext;

@BeforeEach() 
public void setup() {
    this.mvc = MockMvcBuilders
        .webAppContextSetup(webApplicationContext)
        .addFilters(new CharacterEncodingFilter("UTF-8", true)) // 필터
        .build();
    }
}
```


<BR>

### WebMvcTest
+ MVC 관련 설정인 @Controller, @ControllerAdvice, @JsonComponent와 Filter, WebMvcConfigurer,  HandlerMethodArgumentResolver만 로드되기 때문에, 실제 구동되는 애플리케이션과 똑같이 컨텍스트를 로드하는 @SpringBootTest 어노테이션보다 가볍게 테스트 할 수 있다.
+ WebMvcTest는 SpringSecurity도 함께 올라간다. @Autowired로 Mockmvc를 주입받으면 Webapplicationcontext와 시큐리티설정까지 붙은 Mockmvc가 주입된다. 
+ 웹상에서 요청과 응답에 대해 테스트할 수 있을 뿐만 아니라 시큐리티 혹은 필터까지 자동으로 테스트하며 수동으로 추가/삭제까지 가능하다.
+ @Service나 @Repository가 붙은 객체들은 테스트 대상이 아닌 것으로 처리되기 때문에 생성되지 않는다. -> 필요한 경우 Mock을 이용해 가짜로 만들어 사용한다.
+ 서블릿 컨테이너를 모킹한 MockMvc타입의 객체를 목업하여 컨트롤러에 대한 테스트코드를 작성할 수 있다.
+ value 속성을 통해 원하는 컨트롤러, 특정 클래스들만 올릴 수도 있고, excludeFilters를 통해 내가 작성한 security 설정을 제외시키고 기본 시큐리티 설정이 들어간 것을 @withMockUser을 이용해서 security 설정을 무시하고 테스트코드를 작성할 수 있다.
+ WebMvcTest의 Security 관련 주의사항
    - SecurityConfig는 올리지만, SecurityConfig 안에서 주입받고 있던 가령, 특정 Service 같은 것들은 올라가지 않기 때문에 @MockBean으로 등록하여 필요한 동작과정을 정의해줘야 한다.
    - Security를 등록하고 싶지 않다면 excludFilters 속성을 통해 @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class) 을 명시하면 된다. 이 경우에 @Autowired로 Mockmvc를 주입받을 경우 Security설정이 빠진 Mockmvc가 주입된다. 시큐리티에 관한 모든 설정이 빠지기 때문에 Mockmvc에서 get요청을 제외한 다른 요청부분에서 csrf가 없어서 에러가 발생할 수 있다는 점을 주의해야한다. 따라서 Security설정을 빼주고 테스트를 하고 싶다면 Mockmvc를 @Autowired로 주입받지 않고 BeforeEach를 사용해서 webapplicationcontext를 @Autowired로 주입받아서 Mockmvcbuilder로 세팅하여 테스트를 진행해야 한다.

```java
// 예시 1) security 설정을 들어올린 경우 -> SecurityConfig 에서 필요한 클래스들을 @MockBean으로 주입이 필요함
@WebMvcTest(
        value = {LoginController.class, GlobalControllerAdvice.class,
                SignupFormValidator.class
        }
)
class LoginControllerTest {
    @Autowired
    MockMvc mockMvc;

    // SecurityConfig 에서 필요한 클래스들을 @MockBean으로 주입이 필요함
    @MockBean JwtTokenUtil jwtTokenUtil;
    @MockBean JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    @MockBean JwtAccessDeniedHandler jwtAccessDeniedHandler;
    @MockBean RedisUtil redisUtil;
    @MockBean MemberRepository memberRepository;
}

// 예시 2) Security를 제외시킨 경우 -> WithMockUser로 대체하고, Mockmvc에 webapplicationcontext를 세팅하고 사용
@WebMvcTest(
        value = {
                FavoriteController.class, GlobalControllerAdvice.class
        }
        ,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class)
        }
)
@WithMockUser("USER")
class FavoriteControllerTest {
    MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                // mock 테스트에서 한글 깨짐 방지
                .addFilters(new CharacterEncodingFilter("UTF-8", true))
                .build();
    }
}
```


### 결론
+ 통합테스트 -> @SpringBootTest + @AutoConfigureMockMvc 사용
+ MVC 슬라이스테스트 -> @WebMvcTest
+ @WebMvcTest와 @SpringBootTest는 각자 서로의 MockMvc를 모킹하기 때문에 충돌이 발생하여 같이 사용 불가능

<br>

## 6. @Mock, @MockBean, @InjectMocks
---
+ @Mock : 실제 인스턴스 없이 가상의 mock 인스턴스를 만들어 반환
+ @MockBean : ApplicationContext에 mock객체를 추가
+ @InjectMocks : 해당 클래스가 필요한 의존성과 맞는 Mock 객체들을 감지하여 해당 클래스의 객체가 만들어질때 자동으로 주입
    - new 생성자(인자,인자)에 주입할 필요 없이 자동으로 주입 된다는 의미

### @InjectMocks와의 조합
@MockBean는 ApplicationContext에 mock 객체를 등록하는 역할로 즉, mock 객체를 스프링 컨텍스트에 등록하는 것이기 때문에 @InjectMocks가 찾을 수 없다. 따라서 @MockBean을 사용할 경우 @InjectMocks와의 조합이 아니라 스프링 컨텍스트를 통해 주입받는 @Autowired와의 조합을 사용해야 한다.  
__정리하자면, @MockBean은 @SpringBootTest 또는 @WebMVCTest와 함께 사용하고, @InjectMocks + @Mock을 사용한다.__  


<br>

## 7. @ActiveProfiles, @Profile
---
+ ActiveProfiles :Test 할 profile을 지정하는 애노테이션
+ Profile : 각 환경에 맞게 Spring의 Bean들을 올릴 수 있도록 하는 애노테이션

<br>

## 8. @DataJpaTest
---
+ JPA 관련된 설정만 로드한다.
+ 데이터소스의 설정이 정상적인지, JPA를 사
Datasource의 설정이 정상적인지, JPA를 사용하서 데이터를 제대로 생성, 수정, 삭제하는지 등의 테스트가 가능하다.
+ 기본적으로 인메모리 데이터베이스를 사용한다.
+ @Entity 클래스를 스캔하여 스프링 데이터 JPA 저장소를 구성한다.
+ QueryDSL 사용시 Repository를 상속받지 않고 분리시킨 경우에는 @Import 애노테이션을 사용해서 추가한다. 아래 예시 참고

```java
// QueryDSL을 사용하기 위한 설정
@TestConfiguration
public class TestConfig {

    @PersistenceContext
    private EntityManager em;

    @Bean
    public JPAQueryFactory jpaQueryFactory() {
        return new JPAQueryFactory(em);
    }
}

// Import로 QueryDSL Repository 추가
@DataJpaTest
@Import({TestConfig.class,MemberQueryRepository.class})
class MemberQueryRepositoryTest {
}
```

<Br><Br>

__참고__  
<a href="https://www.inflearn.com/course/the-java-application-test#" target="_blank"> 더 자바, 애플리케이션을 테스트하는 다양한 방법</a>   
