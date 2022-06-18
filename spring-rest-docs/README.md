# Spring REST Docs 적용 및 최적화 하기


## 1. REST Docs 란?
---
+ 테스트 코드 기반으로 Restful API 문서를 돕는 도구입니다.
+ Asciidoctor를 이용해서 HTML 등등 다양한 포맷으로 문서를 자동으로 출력할 수 있습니다.
+ RestDocs의 가장 큰 장점은 테스트 코드 기반으로 문서를 작성한다는 점입니다.
+ API Spec과 문서화를 위한 테스트 코드가 일치하지 않으면 테스트 빌드를 실패하게 되어 테스트 코드로 검증된 문서를 보장할 수 있습니다.

<br>

## 2. Swagger VS REST Docs
---
보통 Spring 에서 문서화를 할 때, Swagger과 Restdocs를 사용하게 됩니다.  
Swagger는 마치 Postman처럼(직접 요청하듯이) API를 테스트해 볼 수 있는 화면을 제공하여 동작 테스트하는 용도에 조금 더 특화되어있습니다.  
그렇다면 Swagger는 문서화도 되고 테스트도 가능하니 더 좋은 것이 아닌가라고 생각할 수 있습니다.  
하지만 Swagger를 사용할 경우 명확한 단점이 존재합니다.  
1. 로직에 애노테이션을 통해 명세를 작성하게 되는데 지속적으로 사용하게 된다면 명세를 위한 코드들이 많이 붙게되어 전체적으로 가독성이 떨어진다.
2. 테스트 기반이 아니기에 문서가 100% 정확하다고 확신할 수 없다.
3. 모든 오류에 대한 여러 가지 응답을 문서화할 수 없다.

<br>

아래 코드는 swagger를 적용한 코드인데 해당 애노테이션들이 계속 붙게되면서 코드가 가독성이 매우 떨어집니다. 

```java
// swagger 예시
public class SignupForm {

    @ApiModelProperty(value = "카카오 id", required = true, example = "1")
    private Long id;

    @ApiModelProperty(value = "카카오 image url", required = true, example = "\"http://k.kakaocdn.net\"")
    private String imageFileUrl;
}
```
<br>

반면에 REST Docs를 사용하면 다음과 같은 이점이 있습니다.  
1. 테스트 기반으로 문서가 작성되어 테스트 코드가 일치하지 않으면 테스트 빌드가 실패하게 되기 때문에 문서를 신뢰할 수 있다.
2. 테스트 코드에서 명세를 작성하기 때문에 비즈니스 로직의 가독성에 영향을 미치지 않는다.  



<br>

## 3. 프로젝트 구성
---
### build.gradle 전체 코드
```js
plugins {
    id 'org.springframework.boot' version '2.5.5'
    id 'io.spring.dependency-management' version '1.0.11.RELEASE'
    id 'java'
    // Asciidoctor 플러그인 적용
    // gradle 7.0 이상부터는 jvm 사용
    id "org.asciidoctor.jvm.convert" version "3.3.2" 

}

group = 'com.example'
version = '1.0'
sourceCompatibility = '11'

configurations {
    asciidoctorExtensions // dependencies 에서 적용한 것 추가
    compileOnly {
        extendsFrom annotationProcessor
    }
}

repositories {
    mavenCentral()
}


dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-actuator'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    implementation 'org.springframework.boot:spring-boot-starter-web'
    compileOnly 'org.projectlombok:lombok'
    runtimeOnly 'com.h2database:h2'
    annotationProcessor 'org.projectlombok:lombok'
    testAnnotationProcessor 'org.projectlombok:lombok' // 추가
    testImplementation 'org.springframework.boot:spring-boot-starter-test'

    // build/generated-snippets 에 생긴 .adoc 조각들을 프로젝트 내의 .adoc 파일에서 읽어들일 수 있도록 연동해줍니다.
    // 이 덕분에 .adoc 파일에서 operation 같은 매크로를 사용하여 스니펫 조각들을 연동할 수 있는 것입니다.
    // 그리고 최종적으로 .adoc 파일을 HTML로 만들어 export 해줍니다.
    asciidoctorExtensions 'org.springframework.restdocs:spring-restdocs-asciidoctor' 

    // restdocs-mockmvc의 testCompile 구성 -> mockMvc를 사용해서 snippets 조각들을 뽑아낼 수 있게 된다.
    // MockMvc 대신 WebTestClient을 사용하려면 spring-restdocs-webtestclient 추가
    // MockMvc 대신 REST Assured를 사용하려면 spring-restdocs-restassured 를 추가
    testImplementation 'org.springframework.restdocs:spring-restdocs-mockmvc' 
}

ext {
    // 아래서 사용할 변수 선언
    snippetsDir = file('build/generated-snippets') 
}


test {
    // 위에서 작성한 snippetsDir 디렉토리를 test의 output으로 구성하는 설정 -> 스니펫 조각들이 build/generated-snippets로 출력
    outputs.dir snippetsDir 
    useJUnitPlatform()
}

asciidoctor { // asciidoctor 작업 구성
    dependsOn test // test 작업 이후에 작동하도록 하는 설정
    configurations 'asciidoctorExtensions' // 위에서 작성한 configuration 적용
    inputs.dir snippetsDir // snippetsDir 를 입력으로 구성

    // source가 없으면 .adoc파일을 전부 html로 만들어버림
    // source 지정시 특정 adoc만 HTML로 만든다.
    sources{
        include("**/index.adoc","**/common/*.adoc")
    }    

    // 특정 .adoc에 다른 adoc 파일을 가져와서(include) 사용하고 싶을 경우 경로를 baseDir로 맞춰주는 설정입니다.
    // 개별 adoc으로 운영한다면 필요 없는 옵션입니다.
    baseDirFollowsSourceFile()
}

// static/docs 폴더 비우기
asciidoctor.doFirst {
    delete file('src/main/resources/static/docs')
}

// asccidoctor 작업 이후 생성된 HTML 파일을 static/docs 로 copy
task copyDocument(type: Copy) {
    dependsOn asciidoctor
    from file("build/docs/asciidoc")
    into file("src/main/resources/static/docs")
}

// build 의 의존작업 명시
build {
    dependsOn copyDocument
}


// 참고사항 //
// 공식 문서에서는 위의 ascidoctor.doFirst부터 아래 내용은 없고 이와 같은 내용만 있습니다.
// 이렇게 하면 jar로 만들어 질때 옮겨지는 것으로 IDE로 돌릴 때는 build 폴더에서만 확인이 가능합니다.
// 위 방법을 사용하면 IDE에서도 static으로 옮겨진 것을 확인할 수 있습니다.
// 위에 방법을 사용하든 아래 방법을 사용하든 편한 선택지를 사용하시면 됩니다.
bootJar {
	dependsOn asciidoctor 
	from ("${asciidoctor.outputDir}/html5") { 
		into 'static/docs'
	}
}
```
위와 같이 세팅하게 되면 ./gradlew build 진행시 test -> asciidoctor -> copyDocument -> build 순서로 진행되게 됩니다.  
블로그마다 설정들이 전부 다른데 정확한 설정은 [공식문서](https://docs.spring.io/spring-restdocs/docs/current/reference/html5/#getting-started-build-configuration){:target="_blank"}를 참고하시는 것을 권장합니다.

<br>

### 테스트코드
간단하게 멤버 id를 파라미터로 받아서 멤버 정보를 반환하는 테스트를 작성해보겠습니다.  
```java
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "name", nullable = false)
    private String name;
}
```
```java
// import문을 잘 확인해야 합니다.
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs // rest docs 자동 설정
class MemberControllerTest  {

    @Autowired
    MockMvc mockMvc;

    @Test
    public void member_get() throws Exception {
        // 조회 API -> 대상의 데이터가 있어야 합니다.
        mockMvc.perform(
                get("/api/members/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andDo( // rest docs 문서 작성 시작
                        document("member-get", // 문서 조각 디렉토리 명
                                pathParameters( // path 파라미터 정보 입력
                                        parameterWithName("id").description("Member ID") 
                                ),
                                responseFields( // response 필드 정보 입력
                                        fieldWithPath("id").description("ID"),
                                        fieldWithPath("name").description("name"),
                                        fieldWithPath("email").description("email")
                                )
                        )
                )
        ;
    }
}
```
![그림1](https://github.com/backtony/blog-code/blob/master/spring/img/test/3/3-1.PNG?raw=true)  
빌드하고 나면 build/generated-snippets 폴더에 테스트에서 작성한 member-get 으로 조각들이 생성되어 있습니다.  
기본적으로 다음과 같은 조각들이 default로 생성됩니다.  
+ curl-request.adoc
+ http-request.adoc
+ httpie-request.adoc
+ http-response.adoc
+ request body
+ response body 

<br>

테스트 코드에 따라 추가적인 조각이 생성될 수 있습니다.
+ response-fields.adoc
+ request-parameters.adoc
+ request-parts.adoc
+ path-parameters.adoc
+ request-parts.adoc 

이제 이 조각들로 문서를 작성해야 합니다.
<br>

### index.adoc 만들기
![그림2](https://github.com/backtony/blog-code/blob/master/spring/img/test/3/3-2.PNG?raw=true)  
우선 adoc 파일 작성의 편의를 위해 AsciiDoc 플러그인을 설치해줍니다.  
<br><Br>

![그림3](https://github.com/backtony/blog-code/blob/master/spring/img/test/3/3-3.PNG?raw=true)  
테스트로 만들어준 조각파일들을 이용해서 문서를 만들 차례입니다.  
그 전에 우선 main/resources/static/docs 디렉토리를 만들어줍니다. 앞서 gradle 설정에 의해 이곳으로 html 파일이 복사되어 이곳으로 옮겨집니다.  
그리고 src/docs/asciidoc 디렉토리를 만들고 안에 index.adoc 파일을 만들어줍니다.
```js
= REST Docs 문서 만들기 (글의 제목)
backtony.github.io(부제)
:doctype: book
:icons: font
:source-highlighter: highlightjs // 문서에 표기되는 코드들의 하이라이팅을 highlightjs를 사용
:toc: left // toc (Table Of Contents)를 문서의 좌측에 두기
:toclevels: 2
:sectlinks:

[[Member-API]]
== Member API

[[Member-단일-조회]]
=== Member 단일 조회
operation::member-get[snippets='http-request,path-parameters,http-response,response-fields']
```
+ :source-highlighter: highlightjs 
    - 문서에 표기되는 코드들의 하이라이팅을 highlightjs를 사용합니다.
+ :toc: left 
    - toc (Table Of Contents)를 문서의 좌측에 둡니다.
+ =, ==, ===
    - Markdown의 #, \<h1>, \<h2>, \<h3> 와 같은 역할을 합니다.
+ [[텍스트]]
    - 해당 텍스트에 링크를 겁니다.
+ operation::디렉토리명[snippets='원하는 조각들']
    - 문서로 사용할 조각들을 명시해줍니다.
+ include::{snippets}/member-get/XXX.adoc[]
    - opertaion의 경우 한번에 원하는 조각들을 넣을 수 있었는데 include는 특정 adoc을 지정하여 넣을 수 있습니다.

추가적으로 다른 Asciidoc 사용법은 [[링크](https://narusas.github.io/2018/03/21/Asciidoc-basic.html)]를 참고하시면 도움이 될 것 같습니다.  

<br>

![그림4](https://github.com/backtony/blog-code/blob/master/spring/img/test/3/3-4.PNG?raw=true)  
다시 빌드를 해주시면 main/resources/static/docs 폴더에 index.html 파일이 만들어지게 됩니다.
<Br><Br>

![그림5](https://github.com/backtony/blog-code/blob/master/spring/img/test/3/3-5.PNG?raw=true)  
인텔리제이의 도움을 받아 문서를 확인해보면 잘 만들어진 것을 확인할 수 있습니다.
<br>

### 문서 분리하기
문서화 작업이 많아진다면 index.adoc 파일이 매우 길어질 수 있습니다.
![그림6](https://github.com/backtony/blog-code/blob/master/spring/img/test/3/3-6.PNG?raw=true)  

위 그림과 같이 길어진다면 Member-API에서 option + enter 를 입력하고 Extract Include Directive를 클릭하면 인텔리제이가 해당 API를 빼줍니다.
<br><br>

![그림7](https://github.com/backtony/blog-code/blob/master/spring/img/test/3/3-7.PNG?raw=true)  
src/docs/asciidoc 위치로 해당 API를 빼주게 됩니다.  
<br><Br>

![그림8](https://github.com/backtony/blog-code/blob/master/spring/img/test/3/3-8.PNG?raw=true)  
그럼 파일이 위와 같이 깔끔하게 정리됩니다.
<br>

### 문서 커스텀하기
![그림9](https://github.com/backtony/blog-code/blob/master/spring/img/test/3/3-9.PNG?raw=true)  
지금까지 만든 문서를 보면 Type, Description과 같은 필드만 명시되어 있습니다.  
추가적으로 필수여부, 제약조건 등의 정보를 명시해줘야 하는 경우 snippets를 커스텀해줘야 합니다.  
<br><Br>

![그림10](https://github.com/backtony/blog-code/blob/master/spring/img/test/3/3-10.PNG?raw=true)  
커스텀하기 위해서는 src/test/resources/org/springframework/restdocs/templates 경로에 커스텀한 snippets을 만들어주면 됩니다.  
템플릿 코드는 블로그에 작성했더니 깨지는 현상이 발생하여 [깃허브](https://github.com/backtony/blog-code/tree/master/spring-rest-docs/src/test/resources/org/springframework/restdocs/templates)를 참고해주시면 좋을 것 같습니다.  
snippet은 mustache 문법을 사용합니다.  
snippet 코드를 보시면 name과 path가 보입니다. test에서 문서 작성시 parameterWithName, fieldWithPath 등을 사용하게 되는데 여기서의 name, path 가 위에서 \{\{\}\}안에 값입니다.  
request-field.snippet에는 직접 커스텀한 constraints를 추가해주었습니다. 이는 추후 테스트 코드에서 사용합니다.
<Br><Br>

![그림11](https://github.com/backtony/blog-code/blob/master/spring/img/test/3/3-11.PNG?raw=true)  
이제 다시 빌드해주고 문서를 확인하면 위와 같이 적용된 것을 확인할 수 있습니다. 


### 테스트코드 리팩토링
리팩토링 할 내용은 다음과 같습니다.  
1. 테스트 코드에서 andDo(document()) 부분에서 문서명을 항상 지정해줘야 하는 점
2. 테스트 코드로 인해 build/generated-snippets에 생성된 파일 내용들을 보면 json 포멧이 한줄로 작성되어 보기 매우 불편한 점
3. 관례상 andDo(print()) 를 모두 붙이는데 이 코드가 중복된다는 점

<Br>

![그림12](https://github.com/backtony/blog-code/blob/master/spring/img/test/3/3-12.PNG?raw=true)  

test 디렉토리에서 RestDocsTestSupport, RestDocsConfig 클래스를 생성하여 위의 문제들을 해결하겠습니다.  
__RestDocsConfig.java__  
```java
import static org.springframework.restdocs.snippet.Attributes.Attribute;

@TestConfiguration
public class RestDocsConfig {

    @Bean
    public RestDocumentationResultHandler write(){
        return MockMvcRestDocumentation.document(
                "{class-name}/{method-name}",
                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                Preprocessors.preprocessResponse(Preprocessors.prettyPrint())
        );
    }

    public static final Attribute field(
            final String key,
            final String value){
        return new Attribute(key,value);
    }
}
```
+ "{class-name}/{method-name}"
    - 1번을 해결한 코드입니다.
    - 조각이 생성되는 디렉토리 명을 클래스명/메서드 명으로 정합니다.
+ prettyPrint
    - 2번을 해결한 코드입니다.
    - json이 한 줄로 출력되던 내용을 pretty 하게 찍어줍니다.
+ Attribute field 메서드
    - rest docs에서 기본적으로 문서작성에 필요한 optional(필수값여부), description(설명) 같은 체이닝 메서드는 제공하지만 제약조건 같이 커스텀으로 작성하는 내용에 대한 기능은 없습니다.
    - 따라서 Attribute를 이용해 key, value 값으로 넣어주기 위한 함수입니다.

<Br>

__ControllerTest.java__  
```java
@Disabled
@WebMvcTest({
        MemberController.class,
        CommonDocController.class
})
public abstract class ControllerTest {

    @Autowired protected ObjectMapper objectMapper;

    @Autowired protected MockMvc mockMvc;

    @MockBean protected MemberRepository memberRepository;

    protected String createJson(Object dto) throws JsonProcessingException {
        return objectMapper.writeValueAsString(dto);
    }
}
```
<Br>

__RestDocsTestSupport.java__  
```java
@Disabled
@Import(RestDocsConfig.class)
@ExtendWith(RestDocumentationExtension.class)
public class RestDocsTestSupport extends ControllerTest {

    @Autowired
    protected RestDocumentationResultHandler restDocs;

    @BeforeEach
    void setUp(final WebApplicationContext context,
               final RestDocumentationContextProvider provider) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(MockMvcRestDocumentation.documentationConfiguration(provider))  // rest docs 설정 주입
                .alwaysDo(MockMvcResultHandlers.print()) // andDo(print()) 코드 포함 -> 3번 문제 해결
                .alwaysDo(restDocs) // pretty 패턴과 문서 디렉토리 명 정해준것 적용
                .addFilters(new CharacterEncodingFilter("UTF-8", true)) // 한글 깨짐 방지
                .build();
    }
}
```
+ @ExtendWith(RestDocumentationExtension.class)
    - 앞선 코드에서는 @AutoConfigureRestDocs로 자동으로 주입시켰지만, 이제 중복 작업을 제거하기 위해서는 직접 MockMvc를 커스텀해서 주입해야합니다.
    - 따라서 자동 주입이 아니라 필요한 것들을 가져와서 주입하기 위해 사용하는 코드입니다.
+ @Import(RestDocsConfig.class)
    - 앞서 작성한 Config를 추가해주는 코드입니다.

<br>


이를 바탕으로 optional과 커스텀해서 넣은 constraints를 명시해서 테스트를 작성해보겠습니다.
```java
class MemberControllerTest extends RestDocsTestSupport {

    @Test
    public void member_page_test() throws Exception {
        Member member = new Member("backtony@gmail.com", 27, MemberStatus.NORMAL);
        PageImpl<Member> memberPage = new PageImpl<>(List.of(member), PageRequest.of(0, 10), 1);
        given(memberRepository.findAll(ArgumentMatchers.any(Pageable.class))).willReturn(memberPage);

        mockMvc.perform(
                get("/api/members")
                        .param("size", "10")
                        .param("page", "0")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(
                        restDocs.document(
                                requestParameters(
                                        parameterWithName("size").optional().description("size"), // 필수여부 false
                                        parameterWithName("page").optional().description("page") // 필수여부 false
                                )
                        )
                )
        ;
    }
    

    @Test
    public void member_create() throws Exception {
        MemberSignUpRequest dto = MemberSignUpRequest.builder()
                .name("name")
                .email("hhh@naver.com")
                .status(MemberStatus.BAN)
                .build();
        mockMvc.perform(
                post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
        )
                .andExpect(status().isOk())
                .andDo(
                        restDocs.document(
                                requestFields(
                                        // 앞서 작성한 RestDocsConfig의 field 메서드로 constraints를 명시
                                        fieldWithPath("name").description("name").attributes(field("constraints", "길이 10 이하")),
                                        fieldWithPath("email").description("email").attributes(field("constraints", "길이 30 이하")),
                                        fieldWithPath("status").description("Code Member Status 참조")
                                )
                        )
                )
        ;
    }
}
```
![그림13](https://github.com/backtony/blog-code/blob/master/spring/img/test/3/3-13.PNG?raw=true)  
빌드해서 보면 이렇게 잘 나오는 것을 확인할 수 있습니다.

<br>

### enum 코드 문서화
문서 작성 시 사용되는 타입에 enum이 없기 때문에 enum인 경우 따로 만들어야 합니다.  
Member의 Status와 Sex Enum 값을 문서화 진행해보겠습니다.  
```java
public interface EnumType {
    String getName();
    String getDescription();
}
```
```java
@AllArgsConstructor
public enum MemberStatus implements EnumType {
    LOCK("일시 정지"),
    NORMAL("정상"),
    BAN("영구 정지");

    private final String description;

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public String getName() {
        return this.name();
    }
}
```
```java
@AllArgsConstructor
@Getter
public enum Sex implements EnumType {
    MALE("남자"),
    FEMALE("여자")
    ;

    private String description;

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public String getName() {
        return this.name();
    }
}
```
```java
@Entity
@Table(name = "member")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "age", nullable = false)
    private int age;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private MemberStatus status;

    @Column(name = "sex", nullable = false)
    @Enumerated(EnumType.STRING)
    private Sex sex;

   // ... 생략
}
```
위와 같은 간단한 코드에서 진행합니다.  
사용하는 Enum 타입은 반드시 EnumType를 구현하여야 합니다. 문서화 작업시 사용됩니다.  

<br>

본격적으로 작업을 시작하겠습니다.  
1. Test 패키지에 문서화하고자 하는 Enum 값을 반환하는 컨트롤러를 만듭니다.(테스트 패키지에 작성하면 테스트 실행시에만 동작하므로 실제 운영에서는 동작하지 않습니다)
2. 1번에서 만든 Controller에 대한 테스트 코드 작성하여 snippet 조각들을 추출합니다.
3. 조각을 기반으로 문서를 작성합니다.

<Br>


![그림14](https://github.com/backtony/blog-code/blob/master/spring/img/test/3/3-14.PNG?raw=true)  
패키지 구조는 위와 같습니다.  
test 디렉토리에 document 패키지를 만들었고 이 안에 Enum를 작성하기 위한 클래스들로 구성했습니다.  
resources에는 custom-response-fields.snippet을 추가했습니다.  
템플릿 코드는 [여기](https://github.com/backtony/blog-code/blob/master/spring-rest-docs/src/test/resources/org/springframework/restdocs/templates/custom-response-fields.snippet)에서 확인하시면 됩니다.  
템플릿을 따로 만드는 이유는 간단합니다. 기존에 앞서 만들어준 response-fields.snippet이 default로 사용되는데 enum값은 단지 필드명과 설명만 담아주는 문서를 작성하면 되는데 default로 사용되는 템플릿을 사용하면 불필요한 컬럼들이 생기게 되기 때문입니다.
<br><br>

__CustomResponseFieldsSnippet.java__  
```java
public class CustomResponseFieldsSnippet extends AbstractFieldsSnippet {

    public CustomResponseFieldsSnippet(String type, PayloadSubsectionExtractor<?> subsectionExtractor,
                                       List<FieldDescriptor> descriptors, Map<String, Object> attributes,
                                       boolean ignoreUndocumentedFields) {
        super(type, descriptors, attributes, ignoreUndocumentedFields,
                subsectionExtractor);
    }

    @Override
    protected MediaType getContentType(Operation operation) {
        return operation.getResponse().getHeaders().getContentType();
    }

    @Override
    protected byte[] getContent(Operation operation) throws IOException {
        return operation.getResponse().getContent();
    }
}
```
이 클래스는 default 템플릿이 아닌 custom 템플릿을 사용하기 위한 클래스입니다.  
생성자의 인자 중 type을 보고 template에서 맞는 템플릿을 선택해서 동작합니다.  
즉, 방금 만든 custom-response-fields.snippet을 사용하기 위해서는 type의 값으로 "custom-response"를 주면 됩니다.  
여기까지 custom 템플릿과 custome 템플릿을 사용할 수 있도록 하는 클래스를 만들었습니다. 이제 Enum값을 문서화하기 위한 작업을 진행하겠습니다.
<br><Br>

__ApiResponseDto.java__  
```java
@ToString
@Getter
@NoArgsConstructor
@Builder
public class ApiResponseDto<T> {

    private T data;

    private ApiResponseDto(T data){
        this.data=data;
    }

    public static <T> ApiResponseDto<T> of(T data) {
        return new ApiResponseDto<>(data);
    }
}
```
Test 패키지에서 만들 컨트롤러에서 반환값으로 사용하는 클래스입니다.

<br><br>

__EnumDocs.java__  
```java
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnumDocs {
    // 문서화하고 싶은 모든 enum값을 명시
    Map<String,String> Sex;
    Map<String,String> memberStatus;
}
```
이 클래스 또한 Test 패키지에서 만들 컨트롤러에서 사용할 클래스입니다.  
문서화하고자 하는 모든 enum값을 명시해줍니다.  
<br><br>

__CommonDocController.java__  
```java
@RestController
@RequestMapping("/test")
public class CommonDocController {

    @GetMapping("/enums")
    public ApiResponseDto<EnumDocs> findEnums() {

        // 문서화 하고 싶은 -> EnumDocs 클래스에 담긴 모든 Enum 값 생성
        Map<String, String> memberStatus = getDocs(MemberStatus.values());
        Map<String, String> sex = getDocs(Sex.values());

        // 전부 담아서 반환 -> 테스트에서는 이걸 꺼내 해석하여 조각을 만들면 된다.
        return ApiResponseDto.of(EnumDocs.builder()
                .memberStatus(memberStatus)
                .Sex(sex)
                .build()
        );
    }

    private Map<String, String> getDocs(EnumType[] enumTypes) {
        return Arrays.stream(enumTypes)
                .collect(Collectors.toMap(EnumType::getName, EnumType::getDescription));
    }
}
```
EnumDocs 클래스에 문서화하고자 명시해놓은 enum값들을 모두 생성해주고, ApiResponseDto에 담아서 반환하는 테스트 코드를 작성합니다. 
이를 바탕으로 만들어진 조각으로 문서화를 진행할 것입니다.  

<br><Br>

__CommonDocControllerTest.java__  
```java
// restdocs의 get 이 아님을 주의!!
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;


class CommonDocControllerTest extends RestDocsTestSupport {

    @Test
    public void enums() throws Exception {
        // 요청
        ResultActions result = this.mockMvc.perform(
                get("/test/enums")
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // 결과값
        MvcResult mvcResult = result.andReturn();

        // 데이터 파싱
        EnumDocs enumDocs = getData(mvcResult);

        // 문서화 진행
        result.andExpect(status().isOk())
                .andDo(restDocs.document(
                        customResponseFields("custom-response", beneathPath("data.memberStatus").withSubsectionId("memberStatus"), // (1)
                                attributes(key("title").value("memberStatus")),
                                enumConvertFieldDescriptor((enumDocs.getMemberStatus()))
                        ),
                        customResponseFields("custom-response", beneathPath("data.sex").withSubsectionId("sex"), 
                                attributes(key("title").value("sex")),
                                enumConvertFieldDescriptor((enumDocs.getSex()))
                        )
                ));
    }

    // 커스텀 템플릿 사용을 위한 함수
    public static CustomResponseFieldsSnippet customResponseFields
                                (String type,
                                 PayloadSubsectionExtractor<?> subsectionExtractor,
                                 Map<String, Object> attributes, FieldDescriptor... descriptors) {
        return new CustomResponseFieldsSnippet(type, subsectionExtractor, Arrays.asList(descriptors), attributes
                , true);
    }

    // Map으로 넘어온 enumValue를 fieldWithPath로 변경하여 리턴
    private static FieldDescriptor[] enumConvertFieldDescriptor(Map<String, String> enumValues) {
        return enumValues.entrySet().stream()
                .map(x -> fieldWithPath(x.getKey()).description(x.getValue()))
                .toArray(FieldDescriptor[]::new);
    }

    // mvc result 데이터 파싱
    private EnumDocs getData(MvcResult result) throws IOException {
        ApiResponseDto<EnumDocs> apiResponseDto = objectMapper
                                                .readValue(result.getResponse().getContentAsByteArray(),
                                                new TypeReference<ApiResponseDto<EnumDocs>>() {}
                                                );
        return apiResponseDto.getData();
    }
}
```
코드의 대략적인 설명은 주석으로 적어두었고, (1)으로 표기한 내용에 대해 설명을 진행하겠습니다.  
+ (1)
    - 앞서 작성한 커스텀 템플릿을 사용하도록 하는 코드입니다.
    - 첫번째 인자 type
        - custom-response-fields.snippet 템플릿을 사용할 것이므로 "custom-response" 를 인자로 넘깁니다.
    - 두번째 인자 subsectionExtractor
        - 현재 컨트롤러는 요청에 대한 응답으로 ApiResponseDto 객체를 보냅니다. 앞서 코드에서 반환값으로 ApiResponseDto는 data필드를 가지고 있고 이 데이터 필드 안에 문서화하고자 하는 enum값을 담아서 보냈습니다.
        - sex값을 예로 들면, data.sex에 값이 들어있습니다. 따라서 beneathPath에는 data.sex, withSubsectionId에는 sex를 명시해주면 이에 따라 데이터를 추출합니다.
    - 세번째 인자 attributes
        - 속성값을 넣는 곳인데 이 부분은 아래서 볼 문서화 과정에서 보시는게 훨씬 이해하기 편할 것 같습니다.
    - 네번째 인자 descriptors
        - 요청에 대한 응답값을 파싱해서 enumDocs를 추출해내면 이 안에는 Map 형태로 enum값들이 들어가 있습니다.
        - 이 값들을 문서화애 사용하기 위해 enumConvertFieldDescriptor 함수를 만들어 enum값들을 추출하여 FieldDescriptor로 만들어 인자로 넣어줍니다.

<br><br>


![그림15](https://github.com/backtony/blog-code/blob/master/spring/img/test/3/3-15.PNG?raw=true)  
이제 빌드를 해주면 위와 같이 조각이 만들어진 것을 확인할 수 있습니다.  
custom-response-fields-memberStatus.adoc 파일을 보시면 attributes(key("title").value("memberStatus")) 코드로 인해 문서 상단에 memberStatus라는 문자가 들어가게 됩니다.  
<br>

여기까지 enum 문서화를 끝냈습니다.  
하지만 만들어진 조각을 바탕으로 문서화하면 매우 번거롭습니다. 일단 간단하게 문서화를 해놓고 보겠습니다.
![그림16](https://github.com/backtony/blog-code/blob/master/spring/img/test/3/3-16.PNG?raw=true)  
표에는 MemberStatus 코드를 참고하여 값을 확인하라고 적혀있습니다.  
그럼 왼쪽 목록에서 해당 링크를 타고 가서 확인해야 합니다. 만약 링크를 타고 이동해서 확인하고 원래 보던 곳으로 다시 되돌아가려면 전에 보고 있던 문서의 지점을 찾기가 어렵습니다.  
따라서 표 안에서 클릭으로 팝업창을 띄울 수 있다면 훨씬 편하게 문서를 확인할 수 있을 것입니다.  
<br>

asciidoc에서는 팝업을 제공하지 않아 우회하여 해결해야 합니다.  
asccidoctor의 docinfo 라는 속성이 있는데 adoc 파일에 html 파일을 주입 할 수 있게 해주는 속성 입니다.  
+ docinfo 는 private, shared, head, footer 등의 조합을 할 수 있습니다.
+ private 시 특정 파일 이름을 선언해서 사용 가능합니다.
+ shared 선언 시 docinfo.html 을 기본적으로 가져다 사용합니다.
+ head 는 private-head 또는 shared-head 로 선언이 가능하며 선언 시 head 위치에 붙습니다.
+ footer 는 head 와 반대입니다.
+ docinfo1, docinfo2 등등 도 있는데 이것은 alias 입니다.

그럼 이제 만들어지는 HTML의 a tag 에 class 속성을 넣고 클릭 시 html에 선언한 javascript로 팝업을 띄워보겠습니다.  
![그림17](https://github.com/backtony/blog-code/blob/master/spring/img/test/3/3-17.PNG?raw=true)  
패키지 구조는 위와 같습니다.  
<br>

__docinfo.html__  
```js
<script>
    function ready(callbackFunc) {
        if (document.readyState !== 'loading') {
            // Document is already ready, call the callback directly
            callbackFunc();
        } else if (document.addEventListener) {
            // All modern browsers to register DOMContentLoaded
            document.addEventListener('DOMContentLoaded', callbackFunc);
        } else {
            // Old IE browsers
            document.attachEvent('onreadystatechange', function () {
                if (document.readyState === 'complete') {
                    callbackFunc();
                }
            });
        }
    }

    function openPopup(event) {

        const target = event.target;
        if (target.className !== "popup") {
            return;
        }

        event.preventDefault();
        const screenX = event.screenX;
        const screenY = event.screenY;
        window.open(target.href, target.text, `left=${screenX}, top=${screenY}, width=500, height=600, status=no, menubar=no, toolbar=no, resizable=no`);
    }

    ready(function () {
        const el = document.getElementById("content");
        el.addEventListener("click", event => openPopup(event), false);
    });
</script>
```
해당 파일은 기본옵션으로 만들었기 때문에 docinfo.html 이라는 이름이 지정되었고 해당 이름과 경로는 옵션으로 변경 가능합니다.  
각 페이지마다 스타일과 스크립트가 다르다면 옵션으로 만드시고 그게 아니라면 저처럼 하나만 작성하면 됩니다.  
<br>

__index.adoc__  
```
= REST Docs 문서 만들기 (글의 제목)
backtony.github.io(부제)
:doctype: book
:icons: font
:source-highlighter: highlightjs
:toc: left
:toclevels: 2
:sectlinks:
:docinfo: shared-head

include::Member-API.adoc[]
```
기존 코드에서 docinfo 속성만 추가해줍니다.
<br>

__member-status.adoc__  
```js
:doctype: book
:icons: font

[[member-status]]
include::{snippets}/common-doc-controller-test/enums/custom-response-fields-memberStatus.adoc[]
```
enum값의 테스트로 만든 조각을 문서화시켜줍니다.  
여기서 [[member-status]]는 HTML로 변환시 div id="member-status" 가 붙게 됩니다.  
<br>

__MemberControllerTest.java__  
앞서 작성했던 MemberControllerTest를 수정하겠습니다.
```java
class MemberControllerTest extends RestDocsTestSupport {

    ...

    @Test
    public void member_create() throws Exception {
        MemberSignUpRequest dto = MemberSignUpRequest.builder()
                .age(1)
                .email("hhh@naver.com")
                .status(MemberStatus.BAN)
                .build();
        mockMvc.perform(
                post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
        )
                .andExpect(status().isOk())
                .andDo(
                        restDocs.document(
                                requestFields(
                                        fieldWithPath("age").description("age").attributes(field("constraints", "길이 10 이하")),
                                        fieldWithPath("email").description("email").attributes(field("constraints", "길이 30 이하")),
                                        fieldWithPath("status").description("link:common/member-status.html[상태 코드,role=\"popup\"]")
                                )
                        )
                )
        ;
    }

    ...
}
```
달라진 점은 fieldWithPath의 description 쪽입니다.  
role은 doc파일을 생성하면 class가 됩니다. HTML로 변환시 아래와 같이 변환됩니다.  
```html
<td class="tableblock halign-left valign-top"><p class="tableblock"><a href="common/member-status.html" class="popup">상태 코드</a></p></td>
```
<br><br>

![그림18](https://github.com/backtony/blog-code/blob/master/spring/img/test/3/3-18.PNG?raw=true)  
이제 빌드를 해서 문서를 확인해보면 클릭시 팝업창이 뜨는 것을 확인할 수 있습니다.  
sex 필드에 대해서도 똑같이 진행하면 됩니다.  
<br><br>

끝내 완성은 했지만 popup 코드를 일일이 작성한다는 것은 매우 번거롭습니다.  
따라서 클래스로 따로 만들어 빼서 사용하도록 하겠습니다.  
```java
public interface DocumentLinkGenerator {

    static String generateLinkCode(DocUrl docUrl) {
        return String.format("link:common/%s.html[%s %s,role=\"popup\"]", docUrl.pageId, docUrl.text, "코드");
    }

    static String generateText(DocUrl docUrl) {
        return String.format("%s %s", docUrl.text, "코드명");
    }

    @RequiredArgsConstructor
    enum DocUrl {
        MEMBER_STATUS("member-status", "상태"),
        MEMBER_SEX("sex","성별")
        ;

        private final String pageId;
        @Getter
        private final String text;
    }
}
```
<Br>

__MemberControllerTest.java__  
테스트 코드를 DocumentLinkGenerator를 이용해서 수정해줍니다.
```java
class MemberControllerTest extends RestDocsTestSupport {
    @Test
    public void member_create() throws Exception {
        MemberSignUpRequest dto = MemberSignUpRequest.builder()
                .age(1)
                .email("hhh@naver.com")
                .status(MemberStatus.BAN)
                .build();
        mockMvc.perform(
                post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
        )
                .andExpect(status().isOk())
                .andDo(
                        restDocs.document(
                                requestFields(
                                        fieldWithPath("age").description("age").attributes(field("constraints", "길이 10 이하")),
                                        fieldWithPath("email").description("email").attributes(field("constraints", "길이 30 이하")),
                                        fieldWithPath("status").description(DocumentLinkGenerator.generateLinkCode(DocumentLinkGenerator.DocUrl.MEMBER_STATUS))
                                )
                        )
                )
        ;
    }
}
```
명시적으로 작성하기 위해 길게 나뒀지만, static import를 이용하면 간단하게 줄여서 사용할 수 있습니다.  
<Br>

### 공통 코드 문서화
이제 마지막으로 HTTP Error Response, HTTP status codes, Host 환경 같은 내용을 문서화해보겠습니다.  
에러 관련 문서화 내용이 필요하므로 테스트 패키지에서 에러를 발생시키는 컨트롤러를 만들어주고 테스트를 작성합니다.  

__CommonDocController.java__  
```java

@RestController
@RequestMapping("/test")
public class CommonDocController {

    @PostMapping("/error")
    public void errorSample(@RequestBody @Valid SampleRequest dto) {
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SampleRequest {

        @NotEmpty
        private String name;

        @Email
        private String email;
    }

    ....

}
```
기존에 있던 CommonDocController 위 코드를 추가합니다.  

<br>

__CommonDocControllerTest.java__
```java
class CommonDocControllerTest extends RestDocsTestSupport {

    @Test
    public void errorSample() throws Exception {
        CommonDocController.SampleRequest sampleRequest = new CommonDocController.SampleRequest("name","hhh.naver");
        mockMvc.perform(
                post("/test/error")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest))
        )
                .andExpect(status().isBadRequest())
                .andDo(
                        restDocs.document(
                                responseFields(
                                        fieldWithPath("message").description("에러 메시지"),
                                        fieldWithPath("code").description("Error Code"),
                                        fieldWithPath("errors").description("Error 값 배열 값"),
                                        fieldWithPath("errors[0].field").description("문제 있는 필드"),
                                        fieldWithPath("errors[0].value").description("문제가 있는 값"),
                                        fieldWithPath("errors[0].reason").description("문재가 있는 이유")
                                )
                        )
                )
        ;
    }

    ...
}
```
기존 CommonDocControllerTest에서 위 코드를 추가해줍니다. 잘못된 email 형식을 넣어 에러가 터지게 합니다.  
다시 한번 빌드해줍니다.  
<br>

이제 빌드로 생성된 조각을 이용해서 문서를 작성하겠습니다.  
src/docs/asciidocs 위치에 overview.adoc 를 만들어줍니다.  
__overview.adoc__
```js
[[overview]]
== Overview

[[overview-host]]
=== Host

|===
| 환경 | Host

| Beta
| `beta-backtony.github.io`

| Production
| `backtony.github.io`
|===

[[overview-http-status-codes]]
=== HTTP status codes

|===
| 상태 코드 | 설명

| `200 OK`
| 성공

| `400 Bad Request`
| 잘못된 요청

| `401 Unauthorized`
| 비인증 상태

| `403 Forbidden`
| 권한 거부

| `404 Not Found`
| 존재하지 않는 요청 리소스

| `500 Internal Server Error`
| 서버 에러
|===

[[overview-error-response]]
=== HTTP Error Response
operation::common-doc-controller-test/error-sample[snippets='http-response,response-fields']
```
<Br>

__index.adoc__  
```js
= REST Docs 문서 만들기 (글의 제목)
backtony.github.io(부제)
:doctype: book
:icons: font
:source-highlighter: highlightjs
:toc: left
:toclevels: 2
:sectlinks:
:docinfo: shared-head

include::overview.adoc[]

include::Member-API.adoc[]
```
기본 index.adoc에 overview.adoc을 추가해줍니다.  
그리고 다시 한번 빌드를 합니다.  
<br>

![그림19](https://github.com/backtony/blog-code/blob/master/spring/img/test/3/3-19.PNG?raw=true)  

최종적으로 문서가 완성되었습니다.  

<Br>

## 4. 링크로 문서화
---
현재 한 페이지에서 모든 API를 보여주고 있습니다. 왼쪽 Table of Content로 바로가기를 할 수는 있지만, 가독성이 너무 떨어지는 것 같은 느낌이 들었습니다.  
각 API마다 링크를 걸어주고 새로운 페이지에서 확인할 수 있도록 하여 가독성을 높여보겠습니다.  
<Br>

### build.gradle
```
sources{
        include("**/*.adoc","**/common/*.adoc")
    }
```
앞서 작성했던 build.gradle의 source 부분을 수정해서 모두 html로 문서화 시켜주도록 합니다.
<br>

### API별 문서화
![그림20](https://github.com/backtony/blog-code/blob/master/spring/img/test/3/3-20.PNG?raw=true)  

각각의 API별로 .adoc 파일로 문서화를 진행합니다.  
<br><br>

![그림21](https://github.com/backtony/blog-code/blob/master/spring/img/test/3/3-21.PNG?raw=true)  

build를 진행하면 이제 build/docs 폴더 안에 adoc 파일마다 각각의 HTML 파일이 생성됩니다.
<br>

### index.adoc
![그림22](https://github.com/backtony/blog-code/blob/master/spring/img/test/3/3-22.PNG?raw=true)  
index 파일에서는 이제 * link 를 사용하여 각 html 파일명을 적어줍니다. window blank는 새로운 창을 의미합니다.  
<br>

### 최종 결과
![그림23](https://github.com/backtony/blog-code/blob/master/spring/img/test/3/3-23.PNG?raw=true)  

이제 문서를 확인해보면 각각의 API별로 링크가 들어가 있고 클릭 시 새로운 창이 띄워지게 됩니다.  
이렇게 최종적으로 문서를 완성했습니다.




<Br><Br>

__참고__  
<a href="https://techblog.woowahan.com/2597/" target="_blank"> Spring Rest Docs 적용</a>   
<a href="https://techblog.woowahan.com/2678/" target="_blank"> Spring REST Docs에 날개를… (feat: Popup)
</a>   
<a href="https://docs.spring.io/spring-restdocs/docs/current/reference/html5/" target="_blank"> Spring REST Docs 문서
</a>   

