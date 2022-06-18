# Spring WebFlux - 스프링 부트 운영, 컨트롤러, Hateoas

## 스프링 부트 운영
애플리케이션을 운영하게 되면 운영팀에서 서버에 ping을 날릴 수 있는지, 모니터링 지표, 통계, 서버 세부상태 등을 요구하게 됩니다. 이는 스프링 부트 액추에이터를 사용하면 됩니다.  
```groovy
implementation 'org.springframework.boot:spring-boot-starter-actuator'
```
의존성을 추가해 줍니다.  
```sh
Exposing 1 endpoint(s) beneath base path '/actuator'
```
실행시켜 보면 액추에이터가 추가되었다고 알려줍니다. 구체적으로 어떤 엔드포인트가 활성화된 것인지는 보안상 알려주지 않지만 로그 수준을 변경하면 확인할 수 있습니다.  

### Ping 정보 - health

운영팀에서 ping을 처리하는 방법은 

> localhost:8080/actuator/health

에 접속해보라고 답해주면 됩니다. 결과값은 다음과 같습니다.
```json
{
    "status": "UP"
}
```
서버 상태의 세부정보를 표기하는 설정은 application.yml에 추가 세팅이 필요합니다.
```yml
management:
  endpoint:
    health:
      show-details: always
```
이제 다시 접속해보면 다음과 같은 결과를 가져옵니다.
```json
{
    "status": "UP",
    "components": {
        "diskSpace": {
            "status": "UP",
            "details": {
                "total": 499963174912,
                "free": 345387679744,
                "threshold": 10485760,
                "exists": true
            }
        },
        "mongo": {
            "status": "UP",
            "details": {
                "version": "3.4.5"
            }
        },
        "ping": {
            "status": "UP"
        }
    }
}
```
자동설정 정보를 사용해 다음 정보를 반환합니다.
+ 몽고디비 상태 및 버전 정보
+ 디스크 상태 및 용량 정보

### 애플리케이션 상제정보 - info
애플리케이션 운영에는 정상상태 점검(health check) 외에도 다른 정보가 필요합니다. 즉, 배포된 애플리케이션에 사용된 컴포넌트의 버전 정보도 필요합니다.  
<br>

새벽 3시에 고객으로부터 오류 발생 전화를 받고 몇 시간 동안 이슈 트래킹 한 결과 이미 2주전에 해결된 버그였고 결국 고객이 최신 버전을 사용하지 않아 생긴 문제라는 사실을 알게 되었다고 가정하면 이는 버전만 알고 있다면 쉽게 처리할 수 있는 문제였을 것입니다.  
해당 정보를 알기 위해서는 actuator/info 로 확인하면 되는데 아무 세팅도 하지 않으면 아무런 응답값도 뱉지 않습니다. 이를 위해서 추가적인 세팅이 필요합니다.  
__build.gradle__  
```groovy
springBoot {
    buildInfo()
}
```
빌드 시 build/resources/main/META-INF/build-info.properties 파일이 생성되고 빌드 정보가 담기도록 하는 설정입니다.

__application.yml__  
```yml
management:
  endpoint:
    health:
      show-details: always
  info:
    java:
      enabled: true
    env:
      enabled: true
    build:
      enabled: true

  endpoints:
    web:
      exposure:
        include: info, health # actuator/{endpoint} 열어둘 정보 지정

info:
  hello: world
```
management.info 설정에서 노출할 정보를 세팅합니다.  
+ build.enabled : 디폴트값이 true로 info에 build 정보를 노출시킵니다.
    - build 정보는 IDE 에서 실행해서는 보이지 않고 java -jar로 실행해야 노출됩니다.
+ java.enabled : 디폴트값이 false로 true 시 info에 java 정보를 노출시킵니다.
+ env.enabled : 디폴트값이 false로 true 시 yml에 info로 시작하는 값을 노출시킵니다.
    - 맨 아래 info:hello 값이 노출되도록 하는 세팅입니다.
+ web.exposure : 웹으로 노출할 End-point 지정
    - 기본적으로 JMX 액추에이터 엔드포인트는 활성화돼 있지만 웹으로는 보안상 활성화 돼있지 않습니다. 따라서 원한다면 따로 위처럼 세팅이 필요합니다.
    - JMX를 사용하려면 애플리케이션이 실행되는 장비에 JConsole을 실행해야 합니다.

/actuator/info 로 접속하면 다음과 같은 결과값이 노출됩니다.
```json
{
    "hello": "world",
    "build": {
        "artifact": "webflux",
        "name": "webflux",
        "time": "2022-06-15T05:33:44.384Z",
        "version": "0.0.1-SNAPSHOT",
        "group": "com.example"
    },
    "java": {
        "version": "11.0.11",
        "vendor": {
            "name": "AdoptOpenJDK",
            "version": "AdoptOpenJDK-11.0.11+9"
        },
        "runtime": {
            "name": "OpenJDK Runtime Environment",
            "version": "11.0.11+9"
        },
        "jvm": {
            "name": "OpenJDK 64-Bit Server VM",
            "vendor": "AdoptOpenJDK",
            "version": "11.0.11+9"
        }
    }   
}
```



### 로깅 정보 - loggers
loggers 엔드포인트를 공개하면 사용 중인 모든 로거와 로그 레벨 정보를 확인할 수 있습니다.
```json
{
    "levels": [
        "OFF",
        "ERROR",
        "WARN",
        "INFO",
        "DEBUG",
        "TRACE"
    ],
    "loggers": {
        "ROOT": {
            "configuredLevel": "INFO", // 1
            "effectiveLevel": "INFO" // 2
        },
        "com": {
            "configuredLevel": null, // 3
            "effectiveLevel": "INFO" // 4
        },
        "_org.springframework": {
            "configuredLevel": null,
            "effectiveLevel": "INFO"
        },
        "_org.springframework.web": {
            "configuredLevel": null,
            "effectiveLevel": "INFO"
        },
        "_org.springframework.web.reactive": {
            "configuredLevel": null,
            "effectiveLevel": "INFO"
        },
        ...
}
```
1. ROOT 로거는 스프링 부트에 의해 INFO 레벨로 기본으로 추가된다.
2. 다른 정책으로 로그 레벨을 변경하지 않았으므로 실제 적용 레벨 값도 INFO
3. 애플리케이션 최상위 패키지인 com은 로그 레벨이 명시적으로 지정되지 않았다.
4. com 패키지에 대한 로그 레벨이 INFO로 지정되었다.

### 운영 데이터 확인
`/actuator/threaddump`에 접속하면 현재 애플리케이션에서 사용되고 있는 모든 스레드 정보를 확인할 수 있습니다.  
유의해야할 것은 기본적으로 리액터 기반으로 처리되는 로직은 리액터 스레드에서 실행된다는 것입니다. 그리고 리액터에서 사용되는 스케줄러는 기본적으로 CPU 코어 하나당 한 개의 스레드만 생성합니다. 그래서 4코어 장비에서는 4개의 리액터 스레드만 생성합니다.  
<br>

하지만 리액터 스레드만 있는 것은 아니고 그 외에도 많은 스레드가 사용됩니다. 예를 들면 스프링 부트 개발자 도구가 제공하는 라이브 리로드 서버는 리액터 스레드가 아닌 별도의 스레드에서 실행됩니다. 이런 스레드는 애플리케이션 코드에 의해 직접적으로 사용되지 않는다는 점을 감안하면, 애플리케이션 코드에서 리액터 플로우를 제대로 동작하게 만들기 위해 블로킹 코드를 사용하지 못하게 한다고 해도 이런 스레드는 영향을 받지 않습니다.  
<Br>

스레드 정보를 확안하면 애플리케이션의 여러 단계에서 어떻게 동작하고 있는지 상세하게 조사할 수 있는 스냅샷을 얻을 수 있습니다. 부하가 많이 걸릴 때나 부하가 별로 없을 때 각각 스레드 정보를 확인하면 애플리케이션의 상태를 스레드 수준에서 세부적으로 확인할 수 있습니다.  

### 힙 정보 확인 - heapdump
`actuator/heapdump`에 접속해보면 브라우저가 JSON 데이터를 화면에 보여주는 대신 gzip으로 압축된 hprof 파일 다운로드 받게 됩니다. 
```sh
jhat ~/Downloads/heapdump
```
위 명령어로 실행시키고 `localhost:7000`에 접속하면 스프링 부트 액추에이터의 ThreadDumpEndpoint에 의해 만들어진 heapdump파일을 확인할 수 있고 다음과 같은 리포트 데이터를 확인할 수 있습니다.
+ 힙 히스토그램
+ 플랫폼 포함 모든 클래스의 인스턴스 개수
+ 플랫폼 제외 모든 클래스의 인스턴스 개수

기본적이지만 jhat 명령이 JDK에 포함돼 있어 JDK가 설치된 곳 어디에서나 사용가능하다는 장점이 있습니다. 더 자세한 분석이 필요하다면 비주얼VM을 따로 설치해서 사용하는 것을 추천합니다.  


### HTTP 호출 트레이싱 - httptrace
`actuator/httptrace`는 누가 호출하는지 쉽게 볼 수 있는 기능을 제공합니다.  
+ 가장 많이 사용되는 클라이언트 유형은? 모바일, 특정 브라우저?
+ 어떤 언어로 된 요청이 가장 많은가? 독일어? 영어?
+ 가장 많이 요청되는 엔드포인트는?
+ 요청이 가장 많이 발생하는 지리적 위치는?

스프링 부트는 HttpTraceRepository 인터페이스를 제공하고, 이 인터페이스를 구현한 빈을 자동으로 찾아서 `actuator/httptrace`요청을 처리합니다. 간단하게 메모리 기반으로 동작하는 방식은 다음과 같습니다.
```java
@Bean
HttpTraceRepository httpTraceRepository(){
    return new InMemoryHttpTraceRepository();
}
```
HttpTraceRepository 빈이 컨테이너에 등록되면 액추에이터가 이를 감지하고 자동으로 `actuator/httptrace` 엔드포인트를 활성화합니다.  
서버에 다양한 요청을 보내고 httptrace에 접속해보면 다음과 같은 정보들이 포함돼 있습니다.
+ 타임스탬프
+ 보안 상세정보
+ 세션ID
+ 요청 상세정보(HTTP 메서드, URI, 헤더)
+ 응답 상세정보(HTTP 상태코드, 헤더)
+ 처리 시간(밀리초)

메모리 기반 리포지토리이므로 다음과 같은 특징이 있습니다.
+ 트레이스 정보는 현재 인스턴스에만 존재한다. 로드밸런서 뒤에 여러 대의 인스턴스가 존재한다면 인스턴스마다 자기 자신에게 들어온 요청에 대한 트레이스 정보가 생성된다.
+ 현재 인스턴스를 재시작하면 트레이스 정보는 소멸된다.  

<Br>

이제부터 설명하는 내용은 스프링 몽고디비와 너무 강하게 결합됩니다. 결합도 높은 코드를 직접 작성해서 트레이싱 시스템을 만드는 것보다 트레이스 데이터를 수집하고 분석할 수 있는 서드파티 모니터링 도구를 조사해서 사용하는 편이 더 좋습니다.  

<br>

간단한 경우에는 인메모리를 사용해도 되지만 수십만 사용자의 요청을 추적할 때는 인메모리로는 적절치 않고 직접 만들어야 합니다.
+ 애플리케이션 재시작 시 트레이스 정보는 유지돼야 한다.
+ 모든 인스턴스에서 발생하는 트레이스 정보가 중앙화된 하나의 데이터 스토어에 저장돼야 한다.

```groovy
implementation 'org.mongodb:mongodb-driver-sync:4.6.1'
```
우선 전통적인 몽고디비 드라이버를 추가합니다.(Repository 마킹 인터페이스 때문에 필요합니다.)  
HttpTraceRepository는 트레이스 정보를 HttpTrace 인스턴스에 담아서 저장합니다. 하지만 HttpTrace는 키로 사용할 속성이 없어서 DB에 바로 저장할 수 없습니다. 게다가 HttpTrace는 final로 선언돼 있어 상속받아 새로운 클래스를 만들어 사용할 수도 없습니다. 따라서 HttpTrace를 감싸는 새로운 래퍼 클래스를 만들어 몽고 DB에 저장해 봅시다.
```java
public class HttpTraceWrapper {

    private @Id String id; 
    
    private HttpTrace httpTrace; 

    public HttpTraceWrapper(HttpTrace httpTrace) { 
        this.httpTrace = httpTrace;
    }

    public HttpTrace getHttpTrace() { 
        return httpTrace;
    }
}
```
```java
public interface HttpTraceWrapperRepository extends
		Repository<HttpTraceWrapper, String> {

	Stream<HttpTraceWrapper> findAll();  // 검색

	void save(HttpTraceWrapper trace);  // 저장
}
```
MongoRepository나 CrudRepository를 보면 Repository 마커 인터페이스를 상속해서 상당히 다양한 메서드를 정의하고 있기 때문에 해당 인터페이스를 삭속받으면 미리 만들어져 있는 여러 메서드를 따로 구현할 필요 없이 그대로 사용할 수 있는 것입니다. 하지만 여기서는 꼭 필요한 API만 만들기 위해서 마커 인터페이스를 상속하여 직접 구현합니다. 네이밍 규칙을 지키면 쿼리는 자동으로 생성되고 빈으로 등록됩니다.  

```java
public class SpringDataHttpTraceRepository implements HttpTraceRepository {

	private final HttpTraceWrapperRepository repository;

	public SpringDataHttpTraceRepository(HttpTraceWrapperRepository repository) {
		this.repository = repository; 
	}

	@Override
	public List<HttpTrace> findAll() {
		return repository.findAll() //
				.map(HttpTraceWrapper::getHttpTrace) 
				.collect(Collectors.toList());
	}

	@Override
	public void add(HttpTrace trace) {
		repository.save(new HttpTraceWrapper(trace)); 
	}
}
```
```java
@Bean
HttpTraceRepository httpTraceRepository(HttpTraceWrapperRepository repository){
    return new SpringDataHttpTraceRepository(repository);
}
```
이제 HttpTrace 객체를 저장할 수 있게 됐습니다. 하지만 스프링 데이터 몽고디비는 저장된 객체 데이터를 읽어와서 HttpTrace 객체로 만들어내는 방법을 모릅니다.  
<br>

스프링 데이터는 몽고디비뿐만 아니라 다른 데이터 저장소에 대해서도 대체로 가변 객체 패러다임을 지원합니다. 가변 객체 패러다임은 setter 메서드로 객체의 속성값을 지정하는 방식을 말합니다. 하지만 액추에이터의 HttpTrace는 불변 타입입니다. 생성자를 사용해서 인스턴스를 만든 후 setter 메서드로 속성값을 지정할 수 없으므로 역직렬화하는 방법이 생성자에 마련돼야 합니다. 이때 스프링 데이터 컨버터가 사용됩니다.
```java
static org.springframework.core.convert.converter.Converter<Document, HttpTraceWrapper> CONVERTER = //
        new Converter<Document, HttpTraceWrapper>() { 
            @Override
            public HttpTraceWrapper convert(Document document) {
                Document httpTrace = document.get("httpTrace", Document.class);
                Document request = httpTrace.get("request", Document.class);
                Document response = httpTrace.get("response", Document.class);

                return new HttpTraceWrapper(new HttpTrace( 
                        new HttpTrace.Request( 
                                request.getString("method"), 
                                URI.create(request.getString("uri")), 
                                request.get("headers", Map.class), 
                                null),
                        new HttpTrace.Response( 
                                response.getInteger("status"), 
                                response.get("headers", Map.class)),
                        httpTrace.getDate("timestamp").toInstant(), 
                        null, 
                        null, 
                        httpTrace.getLong("timeTaken")));
            }
        };
```
이 정적 컨버터는 몽고디비 Document에서 HttpTrace 레코드를 추출하고 정보를 읽어서 HttpTraceWrapper 객체를 생성하고 반환합니다. IDE가 Converter 클래스를 람다로 교체할 수 있는 힌트를 줄 수 있는데 실제로 교체하면 안됩니다. 스프링 데이터는 제네릭 파라미터를 기준으로 적절한 컨버터를 판별하고 찾아서 사용하는데 람다로 교체하면 자바의 타입 소거 규칙에 의해 제네릭 파라미터가 소거되므로 컨버터를 올바로 사용할 수 없습니다.  
<br>

이제 컨버터를 스프링 데이터 몽고디비에 등록해야 합니다.

```java
@Bean
public MappingMongoConverter mappingMongoConverter(MongoMappingContext context) {

    MappingMongoConverter mappingConverter = //
            new MappingMongoConverter(NoOpDbRefResolver.INSTANCE, context); // <1>

    mappingConverter.setCustomConversions( // <2>
            new MongoCustomConversions(Collections.singletonList(CONVERTER))); // <3>

    return mappingConverter;
}
```
1. 몽도디비의 DBRef 값에 해석이 필요할 때 UnsupportedOperationException을 던지는 NoOpBdRefResolver를 사용해서 MappingMongoConverter 객체를 생성합니다. HttpTrace에는 DBRef 객체가 없으므로 DBRef 값 해석이 발생하지 않으며, NoOpBdRefResolver에 의해 예외가 발생할 일은 없습니다. 단지 MappingMongoConverter의 생성자가 DbRefResolver 타입 인자를 받기 때문에 전달합니다.
2. MongoCustomConversions 객체를 mappingConverter에 설정합니다.
3. 커스텀 컨버터 한 개로 구성된 리스트를 추가해서 MongoCustomConversions를 생성합니다.

사실 위 코드는 스프링 몽고디비와 너무 강하게 결합된 것으로 보입니다. 이렇게 결합도 높은 코드를 직접 작성해서 트레이싱 시스템을 만드는 것보다 트레이스 데이터를 수집하고 분석할 수 있는 서드파티 모니터링 도구를 조사해서 사용하는 편이 더 좋습니다.  


### 그 밖에 엔드포인트

엔드포인트|설명
---|---
/actuator/auditevents|감사(audit) 이벤트 표시
/actuator/beans|직접 작성한 빈과 자동설정에 의해 등록된 모든 빈 표시
/actuator/caches|모든 캐시 정보 표시
/actuator/conditions|스프링 부트 자동설정 기준 조건 표시
/actuator/configprops|모든 환경설정 정보 표시
/actuator/env|현재 시스템 환경 정보 표시
/actuator/flyway|등록된 플라이웨이 데이터베이스 마이그레이션 도구 표시
/actuator/mappings|모든 스프링 웹플럭스 경로 표시
/actuator/matrics|마이크로미터(micrometer)를 사용해서 수집하는 지표 표시

## 스프링 부트 API 서버 구축
```java
@PostMapping("/api/items") 
Mono<ResponseEntity<?>> addNewItem(@RequestBody Mono<Item> item) { 

    return item.flatMap(s -> this.repository.save(s)) // <1>
            .map(savedItem -> ResponseEntity //
                    .created(URI.create("/api/items/" + 
                            savedItem.getId())) // <2>
                    .body(savedItem)); // <3>
}
```
1. save에서 Mono가 나오므로 이중으로 감싸지지 않도록 flatMap을 사용합니다.
2. ResponseEntity에는 ok, created, accepted, noContent, badRequest, notFound 등 응답메시지를 편리하게 구성할 수 있는 메서드가 포함돼 있습니다. create를 사용해서 새로 생성된 Item 객체의 id값을 포함하는 URI를 응답 헤더에 추가합니다.
3. body 부에는 새로 생성한 savedItem 객체를 담아 반환합니다. 해당 객체를 직렬화해서 응답 본문에 적는 일은 웹플럭스가 담당합니다.  

map을 써야하나 flatMap을 써야하나 고민이 될 때가 많습니다. 가장 쉽게 판단하는 방법은 안에서 Mono 값을 리턴하고 있다면 이중으로 감싸지 않기 위해 flatMap을 사용하고 안에서 리액터 타입 없이 반환한다면 map을 사용하면 됩니다.  
또한 Map은 동기식으로 동작하고 flatMap은 비동기식으로 동작하기 때문에 webFlux에서 map은 주의해서 사용해야 합니다.  
<br>

하나 더 봅시다.  
```java
@PutMapping("/api/items/{id}") 
public Mono<ResponseEntity<?>> updateItem( 
        @RequestBody Mono<Item> item, 
        @PathVariable String id) {

    return item //
            .map(content -> new Item(id, content.getName(), content.getDescription(), 
                    content.getPrice())) // <1>
            .flatMap(this.repository::save) // <2>
            .map(ResponseEntity::ok); // <3>
}
```
1. item 인스턴스를 생성하고 기존 시작점 객체가 Mono 였기 때문에 Mono로 감싸서 반환됩니다.
2. save 메서드는 item을 Mono에 담아서 반환하고, flatMap 메서드는 Mono에서 item을 꺼내서 다시 Mono에 담아서 반환합니다.
3. ResponseEntity의 ok메서드를 사용하여 데이터를 반환합니다.

리포지토리의 save나 delete 메서드를 사용할 때는 항상 map이 아니라 flatMap을 사용해야 합니다. 그렇지 않으면 저장도 삭제도 되지 않습니다. 리액터 타입은 항상 구독이 되어야(사용 되어야) 동작하는데 flatMap이 한 번 Mono에서 데이터를 꺼내고 다시 Mono에 넣기 때문에 flatMap을 사용해야 하는 것입니다.  

## 하이퍼미디어 기반 웹 서비스 구축
하이퍼미디어를 API에 추가하면 더 유연하게 API를 진화시킬 수 있습니다.  
```groovy
implementation ('org.springframework.boot:spring-boot-starter-hateoas'){
    exclude group: "org.springframework.boot", module: "spring-boot-starter-web"
}
```
스프링 헤이티오스는 원래 스프링 MVC를 지원하는 용도로 만들어져서, 스프링 MVC와 아파치 톰캣을 사용할 수 있게 해주는 spring-boot-starter-web이 포함돼 있습니다. 현재는 네티를 사용하는 spring-boot-starter-webflux를 사용하고 있기 때문에 web을 제거해주도록 합니다.  
<br>


```java
@RestController
public class HypermediaItemController {

	private final ItemRepository repository;

	public HypermediaItemController(ItemRepository repository) {
		this.repository = repository;
	}

	@GetMapping("/hypermedia/items/{id}")
	Mono<EntityModel<Item>> findOne(@PathVariable String id) {
		HypermediaItemController controller = methodOn(HypermediaItemController.class); // <1>

		Mono<Link> selfLink = linkTo(controller.findOne(id)).withSelfRel().toMono(); // <2>

		Mono<Link> aggregateLink = linkTo(controller.findAll()) 
				.withRel(IanaLinkRelations.ITEM).toMono(); // <3>

		return Mono.zip(repository.findById(id), selfLink, aggregateLink) // <4>
				.map(o -> EntityModel.of(o.getT1(), Links.of(o.getT2(), o.getT3()))); // <5>
	}
}
```
1. 스프링 헤이티오스의 정적 메서드인 webFluxLinkBuillder.methodOn() 연산자를 사용해 컨트롤러에 대한 프록시를 생성합니다.
2. webFluxLinkBuilder.linkTo() 연산자를 사용해서 컨트롤러의 findeOne() 메서드에 대한 링크를 생성합니다. 현재 메서드가 findOne() 메서드이므로 self라는 이름의ㅡ 링크를 추가하고 리액터 Mono에 담아서 반환합니다.
3. 모든 상품을 반환하는 findAll 메서드를 찾아서 애그리거트 루트에 대한 링크를 생성합니다. IANA(인터넷 할당 번호 관리기관) 표준에 따라 링크 이름을 item으로 명명합니다.
4. 여러 개의 비동기 요청을 실행하고 각 결과를 하나로 합치기 위해 Mono.zip 메서드를 사용합니다. 예제에서는 findByID 메서드 호출과 두 개의 링크 생성 요청 결과를 타입 안전성이 보장되는 리액터 Tuple 타입에 넣고 Mono로 감싸서 반환합니다.
5. 마지막으로 map을 통해 Tuple에 담겨 있던 여러 비동기 요청 결과를 꺼내서 EntityModel로 만들고 Mono로 감싸서 반환합니다.

하이퍼미디어 링크를 만들 때는 가장 먼저 도메인 객체와 링크를 조합해야 합니다. 이 작업을 쉽게 수행할 수 있도록 스프링 헤이티오스는 다음과 같이 벤더 중립적인 모델을 제공합니다.

+ RepresentationModel: 링크 정보를 포함하는 도메인 객체를 정의하는 기본 타입
+ EntityModel : 도메인 객체를 감싸고 링크를 추가할 수 있는 모델, RepresentationModel을 상속받는다.
+ CollectionModel : 도메인 객체 컬렉션을 감싸고 링크를 추가할 수 있는 모델, RepresentationModel을 상속받는다.
+ PagedModel : 페이징 관련 메타데이터를 포함하는 모델, CollectionModel을 상속받는다.

스프링 헤이티오스는 위 네가지 모델과 Link, Links 객체를 기반으로 하이퍼미디어 기능을 제공합니다. 웹 메서드가 이 네가지 모델 중 하나를 그대로 반환하거나 리액터 타입에 담아서 반환하면 스프링 헤이티오스가 직렬화 기능이 동작하고 하이퍼미디어를 만들어 냅니다.  

### 하이퍼미디어의 가치
단순히 데이터만을 제공하기 위해 하이퍼미디어를 사용하는 것이 아닙니다. 데이터 사용 방법에 대한 정보도 함께 제공하기 위해 하이퍼미디어를 사용합니다. 그래서 하이퍼미디어 문서에 데이터에 대한 설명을 여러 가지 JSON 형식으로 제공하는 프로파일 링크가 종종 포함되기도 합니다. 프로파일 링크에 포함된 링크는 자바스크립트 라이브러리가 자동으로 생성/수정 입력폼을 만드는 데 사용될 수도 있습니다.  

<br>

만약 내/외부 여러 팀에서 사용하는 API를 만들어 공개했고 주문 상태 정보를 포함하는 주문 서비스 API를 만든다고 가정해 봅시다. 주문 상태가 '준비 중'일 때는 주문을 취소할 수 있습니다. 하지만 주문 상태가 '발송 완료'로 바뀌면 취소할 수 없습니다. 클라이언트가 '주문 취소' 버튼 표시 여부를 주문의 상태에 따라 결정하도록 로직을 작성했다면 백엔드와 강하게 결합돼 있는 것입니다.  
백엔드가 새로운 상태를 추가하고 클라이언트가 백엔드 서버에서 받은 데이터를 기반으로 작성했다면, 새로 추가한 상태 때문에 클라이언트 로직은 깨실 수도 있습니다. 만약 국제화를 적용했다면 대부분의 클라이언트에서 심대한 영향을 받게 됩니다.  
<br>

클라이언트가 직접적으로 도메인 지식에 의존하는 대신에 프로토콜에만 의존하게 만들면, 예를 들어 클라이언트가 주문에 대한 지식을 직접 사용하지 말고 단순히 링크를 읽고 따라가게만 만든다면, 클라이언트는 백엔드의 변경에서 유발되는 잠재적인 문제를 피해갈 수 있습니다.  

### API에 행동 유도성 추가
동일한 URI를 가리키는 GET과 PUT을 함께 담으면 HAL 문서는 한 개의 링크만 생성합니다. 그 결과 사용자는 원래 GET, PUT 두 가지의 서로 다른 선택지가 존재했었다는 사실을 알 수 없게 됩니다.  
GET과 PUT을 다른 링크로 표현하도록 강제하더라도 클라이언트가 PUT 요청을 보내면 어떤 속성 정보를 제공해야 하는지 클라이언트에 알려주지 않습니다. 이를 클라이언트가 알아내게 하는 것은 결코 좋은 방법이 아닙니다. 바로 이 지점에서 스프링 헤이티오스가 하이퍼미디어에 행동 유도성을 추가한 API를 제공합니다.  
```java
@PutMapping("/affordances/items/{id}") 
public Mono<ResponseEntity<?>> updateItem(@RequestBody Mono<EntityModel<Item>> item, // <1>
        @PathVariable String id) {
    return item //
            .map(EntityModel::getContent) // <2>
            .map(content -> new Item(id, content.getName(), // <3>
                    content.getDescription(), content.getPrice())) //
            .flatMap(this.repository::save) // <4>
            .then(findOne(id)) // <5>
            .map(model -> ResponseEntity.noContent() // <6>
                    .location(model.getRequiredLink(IanaLinkRelations.SELF).toUri()).build());
}
```
1. Mono가 EntityModel을 감싸고 있기 때문에 클라이언트는 Item 객체를 보낼 수도 있고, 하이퍼미디어 형식일 수도 있습니다. 
2. item을 꺼내서 Mono에 담습니다.
3. Mono에 담긴 item을 꺼내서 새로운 item을 생성합니다.
4. 저장합니다.
5. 컨트롤러의 findOne() 메서드를 호출해 새로 저장된 객체를 조회합니다. findOne은 위 코드에서는 없지만 몽고디비에서 데이터를 조회해서 하이퍼미디어에 담아서 반환합니다.
6. ResponseEntity의 정적 메서드를 통해서 location 헤더에 self 링크 URI을 담고 204(no content) 코드로 리턴합니다.

단순하게 보면 item 데이터를 받아서 id를 식별자로 하여 저장하고, 저장된 정보를 findOne(id) 메서드로 다시 조회해서 필요한 URI를 추가해서 사용자에게 반환합니다.  
<br>

findOne 메서드는 행동 유도성이 적용된 이 클래스에는 아직 정의되지 않았기에 만들어 봅시다.  

```java
@RestController
public class AffordancesItemController {

    ...

	@GetMapping("/affordances/items/{id}") 
	Mono<EntityModel<Item>> findOne(@PathVariable String id) {
		AffordancesItemController controller = methodOn(AffordancesItemController.class); // <1>

		Mono<Link> selfLink = linkTo(controller.findOne(id)) 
				.withSelfRel() 
				.andAffordance(controller.updateItem(null, id)) // <2>
				.toMono();

		Mono<Link> aggregateLink = linkTo(controller.findAll()) 
				.withRel(IanaLinkRelations.ITEM) 
				.toMono();

		return Mono.zip(repository.findById(id), selfLink, aggregateLink) 
				.map(o -> EntityModel.of(o.getT1(), Links.of(o.getT2(), o.getT3())));
	}
}
```
1. AffordancesItemController 프록시를 생성합니다.
2. andAffordance 메서드를 사용한 것 외에는 앞서 HypermediaController에 있던 findOne과 같습니다. andAffordance 메서드는 Item을 수정할 수 있는 update 메서드를 사용되는 경로를 findOne 메서드의 self 링크에 연결합니다.  

위 컨트롤러를 호출하면 다음과 같은 응답이 옵니다. 
```json
{
    "id": "id",
    "name": "name",
    "description": "description",
    "price": 19.9,
    "_links" : {
        "self" : {
            "href" : "http:localhost:8080/affordances/items/id"
        },
        "item" : {
            "href" : "http://localhost:8080/affordances/items"
        },
        "_templates" : {
            "default" : {
                "method" : "put",
                "properties" : [ {
                    "name" : "description"
                }, {
                    "name": "id"
                }, {
                    "name" : "name"
                }, {
                    "name" : "price"
                } ]
            }
        }
    }
}
```
응답 데이터와 탐색 가능한 링크뿐만 아니라 PUT 요청을 보낼 때 필요한 메타데이터까지 _templates 항목에 표시해 줍니다.  
<br>

마지막으로 item 객체 목록을 반환하는 항목 유동성이 포함된 API를 만들어봅시다.  
```java
@GetMapping("/affordances/items")
Mono<CollectionModel<EntityModel<Item>>> findAll() {
    AffordancesItemController controller = methodOn(AffordancesItemController.class);

    Mono<Link> aggregateRoot = linkTo(controller.findAll()) //
            .withSelfRel() //
            .andAffordance(controller.addNewItem(null)) // <1>
            .toMono();

    return this.repository.findAll() // <2>
            .flatMap(item -> findOne(item.getId())) // <3>
            .collectList() // <4>
            .flatMap(models -> aggregateRoot //
                    .map(selfLink -> CollectionModel.of( //
                            models, selfLink))); // <5>
}
```
1. andAffordance 메서드를 사용하여 self 링크에 addNewItem 에 대한 링크를 추가합니다.
2. 아이템을 전체 조회합니다.
3. 각 아이템마다 findOne 메서드를 호출합니다.
4. 조회 결과를 리스트에 담습니다.
5. item 객체 목록을 조회할 수 있는 애그리거트 루트 링크와 함께 CollectionModel에 저장합니다.







<Br><Br>

__참고__  
<a href="https://github.com/onlybooks/spring-boot-reactive" target="_blank"> 스프링 부트 실전 활용 마스터</a>   






