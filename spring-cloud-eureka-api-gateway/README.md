## Spring cloud - Eureka, API GateWay Example

## Service Discovery
---
![그림1](https://backtony.github.io/assets/img/post/spring/cloud/1-1.PNG)

+ Service Discovery는 단어 직역 그대로, 외부의 서비스들이 마이크로서비스를 검색하기 위해 사용하는 일종의 전화번호부와 같은 역할로 __각각의 마이크로서비스가 어느 위치에 있는지를 등록해 놓은 곳__ 입니다.
+ __Spring Cloud Netflix Eureka__ 가 Service Discovery의 역할을 제공합니다.
+ 요청 정보가 Load Balancer(API Gateway)에 전달되면 다음으로 service Discovery에 전달됩니다. 그럼 service Discovery는 필요한 서비스가 어느 곳에 있는지에 대한 정보를 API Gateway로 반환하고 API Gateway는 이에 따라 해당 서비스를 호출하고 결과를 받게 됩니다.

### 유레카 서버 프로젝트 설정
+ 의존성 Eureka Server 필요합니다.
+ __XXApplication에 @EnableEurekaServer 애노테이션 추가하여 EurekaServer로서 자격을 명시해줍니다.__

```groovy
// build.gradle
implementation 'org.springframework.cloud:spring-cloud-starter-netflix-eureka-server'
```
```java
@SpringBootApplication
@EnableEurekaServer
public class DiscoveryserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(DiscoveryserviceApplication.class, args);
	}

}
```
```yml
# application.yml 세팅

# 유레카 서버가 웹서비스의 성격으로 구동된다.
# 포트 번호 지정
server:
  port: 8761

# 마이크로서비스를 담당하는 스프링부트 프레임 워크에
# 각각의 마이크로서비스에 고유한 아이디를 부여하는 설명
spring:
  application:
    name: discoveryservice


## client 설정 ##
# 현재 유레카 서버를 구동하는데 왜 client 설정이 필요한가?
# eureka 라이브러리가 포함된 채 스프링부트가 구동이 되면
# 기본적으로 eureka 클라이언트의 역할로서 어딘가에 등록하는 작업을 시도한다.
# 아래 설정들은 기본값이 true로 현재 프로젝트를 client의 역할로 전화번호부에 등록하는 것과 같다.
# 현재 프로젝트는 서버(전화번호부)가 될 것인데 자신의 정보를 자신에게 등록하는 현상이 된다. 
# 의미가 없는 작업이므로 false로 세팅해준다.
# 정리하자면 현재 프로젝트는 유레카 서버(전화번호부)이므로 클라이언트 역할로 자동으로 등록되는 세팅을 막아주는 설정이다.
eureka:
  client:
    register-with-eureka: false
    fetch-registry: false
```
서버를 띄워서 localhost:8761 에 접속해보면 유레카 서버가 잘 띄워져 있는 것을 확인할 수 있습니다.  
client 옵션은 기본값이 true이기 때문에 앞으로 진행하는 프로젝트에서는 true로 설정할 경우 생략하도록 하겠습니다.



## API Gateway Service
---
![그림3](https://backtony.github.io/assets/img/post/spring/cloud/1-3.PNG)

+ API Gateway는 모든 클라이언트의 요청을 받아 설정해 놓은 라우팅 설정에 따라서 각각의 endPoint로 클라이언트 대신에 요청을 보내고 응답을 받아 클라이언트에게 전달하는 프록시 역할을 합니다.
+ 시스템 내부 구조는 숨기고 외부의 요청에 대해서 적절한 형태로 가공해서 응답할 수 있다는 장점이 있습니다.
+ 클라이언트 요청 -> API Gateway -> Service Discovery(Eureka Server)에서 마이크로서비스 위치 정보 확인 -> API Gateway -> 해당 마이크로서비스로 요청 -> API Gateway로 응답 반환 -> 클라이언트에게 반환


### 서비스 프로젝트 만들기
> 클라이언트 요청 -> API Gateway -> Service Discovery(Eureka Server)에서 마이크로서비스 위치 정보 확인 -> API Gateway -> 해당 마이크로서비스로 요청 -> API Gateway로 응답 반환 -> 클라이언트에게 반환

#### first-service
위와 같은 흐름을 만들기 위해서 요청이 들어오면 최종적으로 요청을 받게 되는 서비스 프로젝트(유레카에 등록될 프로젝트) 2개를 우선적으로 만들어 봅시다.
```groovy
// build.gradle
implementation 'org.springframework.boot:spring-boot-starter-web'
implementation 'org.springframework.cloud:spring-cloud-starter-netflix-eureka-client'
```
의존성을 추가해줍니다.  
<Br>

```yml
# application.yml
server:
  port: 0 # 매번 랜덤하게 사용 가능한 포트 번호 부여

spring:
  application:
    name: my-first-service

eureka:
  instance:
    # 표기되는 규칙 변경
    instance-id: ${spring.cloud.client.hostname}:${spring.application.instance_id:${random.value}}
  client:
    service-url:
      defaultZone: http://127.0.0.1:8761/eureka
```
+ port : 값을 0으로 주게 되면 매번 랜덤하게 사용가능한 포트를 부여해서 띄워줍니다.
+ name : 프로젝트의 애플리케이션 이름을 정해서 입력해줍니다.
+ instance-id
    - 프로젝트를 유레카 서버에 등록하고 유레카 서버로 접속해보면 아래와 같이 등록된 서비스를 확인할 수 있습니다.
    - 같은 프로젝트를 여러 개 띄우게 되면 포트번호가 같기 때문에 여러 개 띄워지더라도 1개만 표시되는 현상이 발생합니다.
    - 따라서 실행되는 프로젝트마다 구분해주기 위해 표기되는 instance-id값을 변경해서 적용해주는 설정입니다.
    - 만약 hostname보다 ip를 표기하고 싶다면 앞쪽부분만 spring.cloud.client.ip-address 로 수정해주면 되고, 애플리케이션 이름으로 표기하고 싶다면 앞부분만 spring.application.name으로 변경해주면 됩니다.

![그림2](https://backtony.github.io/assets/img/post/spring/cloud/1-2.PNG)  
<br>

```java
@RestController
public class FirstServiceController {

    @GetMapping("/welcome")
    public String welcome(){
        return "welcome to the first service";
    }
}
```
간단한 컨트롤러를 하나 만들어 줍니다.
```java
@SpringBootApplication
@EnableDiscoveryClient
public class XXXApplication {
  ...
}
```
application 클래스에 @EnableDiscoveryClient 애노테이션을 추가해서 유레카 서버에 등록됨을 명시해줍니다.

#### second-service
이제 약간만 다른 2번 째 프로젝트를 만들어 줍니다.  
의존성은 똑같이 추가해주면 됩니다.
```yml
# application.yml
server:
  port: 0 

spring:
  application:
    name: my-second-service

eureka:
  instance:
    instance-id: ${spring.cloud.client.hostname}:${spring.application.instance_id:${random.value}}
  client:
    service-url:
      defaultZone: http://127.0.0.1:8761/eureka
```
첫 번째 프로젝트랑 똑같고 name만 다릅니다.
```java
@RestController
public class SecondServiceController {

    @GetMapping("/welcome")
    public String welcome(){
        return "welcome to the second service";
    }
}
```
간단한 컨트롤러를 만들어 줍니다.
```java
@SpringBootApplication
@EnableDiscoveryClient
public class XXXApplication {
  ...
}
```
application 클래스에 @EnableDiscoveryClient 애노테이션을 추가해서 유레카 서버에 등록됨을 명시해줍니다.

<br><br>

```yml
eureka:
  client:
    register-with-eureka: true
    fetch-registry: true
```
두 프로젝트 모두 유레카 서버에 등록해야 하는 위와 같은 세팅이 application.yml에 필요하나 true값이 디폴트이기 때문에 작성하지 않아도 됩니다.  
따라서 두 프로젝트는 이제 유레카 서버에 등록된 것입니다.

### Gateway 프로젝트 세팅
```groovy
// build.gradle
implementation 'org.springframework.cloud:spring-cloud-starter-gateway'
implementation 'org.springframework.cloud:spring-cloud-starter-netflix-eureka-client'
```
gateway와 유레카 client 의존성을 추가해줍니다.

```yml
# application.yml
server:
  port: 8000

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka

spring:
  application:
    name: apigateway-service 
  cloud:
    gateway:
      routes: # 라우팅 설정
        - id: first-service # 구분하기 위한 id값으로 임의로 작성해도 무관
          predicates: # 조건
            - Path=/first-service/** # first-service/ 으로 요청이 들어오면 
          uri: lb://MY-FIRST-SERVICE # 유레카 서버에서 MY-FIRST-SERVICE를 찾아서 그곳으로 요청을 보낸다.
          filters:
            # url 재정의
            # ?<변수명>은 뒤에 나오는 정규식을 변수처럼 사용할 수 있도록 한다. ()는 하나의 묶음 처리 -> segment는 (.*)를 의미
            # 콤마(,)를 기준으로 왼쪽 url을 오른쪽 url로 재정의한다.
            # 콤마 기준 오른쪽 부분은 ${변수명}으로 url 가져오고 앞에 / 붙여준거라고 보면 된다.
            - RewritePath=/first-service/(?<segment>.*), /$\{segment}
        - id: second-service
          predicates:
            - Path=/second-service/** 
          uri: lb://MY-SECOND-SERVICE          
          filters:
            - RewritePath=/second-service/(?<segment>.*), /$\{segment}
```
+ predicates : api-gateway 서버로 요청이 들어왔을 때 요청 url의 조건을 명시합니다.
+ uri
    - predicates 조건에 매칭되는 url을 어디로 라우팅 시킬지 명시합니다.
    - lb:// 뒤에는 유레카 서버에 등록된 프로젝트의 application.name을 적어주면 됩니다.
    - lb는 load balancer의 약자로 my-first-service가 여러 개 띄워져 있다면 라운드 로빈 방식으로 트래픽을 분산해줍니다.
+ filters : 필터를 적용합니다.
    - RewritePath : 요청으로 들어온 url을 변경해줍니다.
    - predicate의 조건에 따르면 first-service/ 또는 second-service/ 와 같은 url로 요청하면 해당 마이크로서비스로 요청을 라우팅해줍니다.
    - 그러나 필터로 rewirtePath를 해주지 않으면 라우팅될 때, 요청 url이 localhost:8080/first-service의 형태로 요청됩니다.
    - 즉, first-service프로젝트와 second-service의 컨트롤러에 @RequestMapping("/first-service")와 같은 매핑이 필요하게 됩니다.
    - 따라서 앞쪽에 first-service, second-service를 제거하고 뒤쪽의 url만 사용하여 요청을 보낼 수 있도록 rewrite해줘야 합니다.

```java
@SpringBootApplication
@EnableDiscoveryClient
public class XXXApplication {
  ...
}
```
application 클래스에 @EnableDiscoveryClient 애노테이션을 추가해서 유레카 서버에 등록됨을 명시해줍니다.  
<Br>

이제 실행시켜서 잘 작동되는지 확인해 볼 차례입니다.

> 유레카 서버 실행 -> first-service, second-service, api-gateway 실행 -> localhost:8000/welcome 으로 요청 보내기

위와 같이 요청을 보내면 잘 응답되는 것을 확인할 수 있습니다.


### 필터 추가하기
이번에는 api-gateway로 들어오는 요청들에 대해 필터를 적용해 봅시다.  
api-gateway 프로젝트에서 적용할 필터 클래스들을 만들어 주고 yml에 등록해주면 됩니다.  
모든 요청에 대해 적용할 필터와 first-service에만 적용할 필터 총 2개를 만들어 보겠습니다.


```java
@Slf4j
@Component
public class GlobalFilter extends AbstractGatewayFilterFactory<GlobalFilter.Config> {

    public GlobalFilter(){
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {

        return (exchange, chain) -> {
            // custom pre filter
            ServerHttpRequest request = exchange.getRequest();
            ServerHttpResponse response = exchange.getResponse();

            log.info("Global Filter baseMessage : {}",config.baseMessage);

            if (config.isPreLogger()){
                log.info("Global Filter start: request id -> {}",request.getId());
            }

            // custom post filter
            return chain.filter(exchange).then(Mono.fromRunnable(() -> {

                if (config.isPostLogger()){
                    log.info("Global Filter End: response code -> {}",response.getStatusCode());
                }
            }));
        };
    }

    // yml 파일에서 값을 주입받는다.
    @Data
    public static class Config{
        private String baseMessage;
        private boolean preLogger;
        private boolean postLogger;
    }
}
```
+ Config 클래스
    - 해당 클래스의 필드 값은 application.yml 에서 args를 입력해주면 주입받을 수 있습니다.
    - 생성자에서 해당 클래스를 super의 인자로 넣어주면 apply 메서드의 인자로 들어오는 config값으로 동일하게 사용할 수 있습니다.
+ apply 메서드
    - exchange에서 request와 response를 가져올 수 있습니다.
    - 유레카 의존성을 추가할 경우 내장서버로 톰켓이 아니라 비동기 방식인 netty가 내장서버로 띄워집니다.
    - 따라서 webFlux를 사용하기 때문에 기존 방식인 httpServletRequest, httpServletResponse가 아닌 ServerHttpXXX를 사용하게 됩니다.
    - 람다식 안의 return 전에 코드는 실질적인 메인 코드를 실행하기 전인 filter의 전처리 로직입니다.
    - 람다식 안의 return 문 코드는 실질적인 메인 코드를 실행한 후 filter의 후처리 로직입니다.


<br>

```java
@Slf4j
@Component
public class LoggingFilter extends AbstractGatewayFilterFactory<LoggingFilter.Config> {

    public LoggingFilter(){
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {

        GatewayFilter filter = new OrderedGatewayFilter((exchange,chain) -> {
            // custom pre filter
            ServerHttpRequest request = exchange.getRequest();
            ServerHttpResponse response = exchange.getResponse();

            log.info("Logging Filter baseMessage : {}",config.baseMessage);

            if (config.isPreLogger()){
                log.info("Logging Pre Filter : request id -> {}",request.getId());
            }

            // custom post filter
            return chain.filter(exchange).then(Mono.fromRunnable(() -> {

                if (config.isPostLogger()){
                    log.info("Logging Post Filter : response code -> {}",response.getStatusCode());
                }
            }));
        }, Ordered.HIGHEST_PRECEDENCE); // 모든 필터보다 가장 우선으로 적용

        return filter;
    }

    // yml 파일에서 값을 주입받는다.
    @Data
    public static class Config{
        private String baseMessage;
        private boolean preLogger;
        private boolean postLogger;
    }
}
```
코드는 앞서 작성했던 코드와 거의 일치합니다.  
람다식으로 필터를 만들어서 리턴하는 것이 아니라 이번에는 인스턴스를 생성해서 세팅하고 반환해주도록 코드를 작성했습니다.  
OrderedGatewayFilter 2번째 생성 인자로 우선순위를 주었습니다.  
<br>

이제 application.yml에 필터를 등록해줄 차례입니다.
```yml
server:
  port: 8000

eureka:
  client:
    register-with-eureka: true
    fetch-registry: true
    service-url:
      defaultZone: http://localhost:8761/eureka

spring:
  application:
    name: apigateway-service # 애플리케이션 이름, 임의로 작성해도 무관
  cloud:
    gateway:
      default-filters: # 모든 라우팅에 적용하는 필터
        - name: GlobalFilter # 적용할 필터의 클래스명
          args: # 필터 클래스의 Config 클래스 필드값 정보 세팅
            baseMessage: Spring Cloud Gateway Logger
            preLogger: true
            postLogger: true
      routes:
        - id: first-service
          predicates:
            - Path=/first-service/** 
          uri: lb://MY-FIRST-SERVICE 
          filters:
            - RewritePath=/first-service/(?<segment>.*), /$\{segment}
        - id: second-service
          predicates:
            - Path=/second-service/**
          uri: lb://MY-SECOND-SERVICE 
          # 특정 필터 적용
          filters:
            - RewritePath=/second-service/(?<segment>.*), /$\{segment}
            - name: LoggingFilter
              args:
                baseMessage: hi there
                preLogger: true
                postLogger: true
```
+ default-filters : 모든 라우팅에 적용할 필터를 등록해줍니다.
+ args : 필터 클래스에서 Config 클래스의 필드 값들의 데이터를 채워줍니다.
+ filters : 각 라우팅마다 filter를 별도로 등록할 때 사용합니다. 만약 config에 넣을 데이터가 없다면 args는 작성하지 않아도 됩니다.

