# Spring 로그 설정하기 - Logback



## 1. 로깅 vs System.out.println()
---
로깅을 사용하면 다음과 같은 이점이 있습니다.
+ 출력 형식을 지정할 수 있다.
+ 로그 레벨에 따라 남기고 싶은 로그를 별도로 지정할 수 있따.
+ 콘솔뿐만 아니라 파일이나, 네트워크 등 로그를 별도의 위치에 남길 수 있다.
+ 성능이 System.out 보다 좋다.

<br>

## 2. 로그 레벨
---

레벨|설명
---|---
Fatal|매우 심각한 에러로 프로그램이 종료되는 경우가 많다
Error|의도하지 않은 에러가 발생한 경우로 프로그램이 종료되진 않는다.
Warn|에러가 될 수도 있는 잠재적 사능성이 있는 경우
Info|요구사항에 따라 시스템 동작을 확인할 때, 명확한 의도가 있는 에러의 경우
Debug|Info 레벨보다 더 자세한 정보가 필요한 경우로 Dev환경에서 주로 사용
Trace|Debug 레벨보다 더 자세한 예외 로그를 사용할 때 사용

Error를 기준으로 위와 아래로 분류하면 위쪽은 시스템 상에서 개발자가 의도하지 않은 예외를 나타낼 때 사용하고, 아래는 의도한 예외를 나타낼 때 사용합니다.  
예를 들어, 회원가입 시, DB에 동일한 email을 가진 회원이 있을 때 예외를 던진다면 이 이벤트의 로그는 Info 입니다. 개발자가 의도한 예외이기 때문입니다.  

<br>

## 3. 로깅 vs 디버깅
---
예외 사항을 가장 잘 파악할 수 있는 것은 디버깅입니다.  
변수의 값이나, 메모리 주소 등을 breakPoint가 걸린 시점에서 확인할 수 있기 때문입니다.  
하지만 실제 서버가 구동중이라면 디버깅을 하기엔 무리가 있기 때문에 실제 디버깅을 하기 어려운 환경에서는 로깅이 최선의 선택입니다.  
<br>

## 4. 로깅 프레임워크 SLF4J
---
+ SLF4J
    - 다양한 로깅 프레임워크에 대한 추상화(인터페이스) 역할을 합니다.
    - 추상 로깅 프레임워크이기 때문에 단독으로 사용이 불가능합니다.
    - 최종 사용자가 배포시 원하는 구현체를 선택해서 사용합니다.

### 동작 과정
![그림1](https://github.com/backtony/blog-code/blob/master/spring/img/log/2/log-1.PNG?raw=true)  

동작과정을 간략하게 설명하면, 개발할 때는 SLF4J API를 사용하여 로깅 코드를 작성하고 배포할 때는 바인딩도니 Logging Framework가 실제 로깅 코드를 수행하는 과정을 거칩니다.  
이러한 과정은 SLF4J에서 제공하는 3가지 모듈인 Bridge, API, Binding 모듈을 통해 수행됩니다.  
+ Bridge 모듈
    - SLF4J이외의 다른 로깅 API로의 Logger 호출을 SLF4J 인터페이스로 연결하여 SJF4J API가 대신 처리할 수 있도록 하는 일종의 어댑터 역할의 라이브러리 입니다.
    - 이전의 레거시 로깅 프레임워크를 위한 라이브러리입니다.
    - Binding모듈에서 사용될 프레임워크와 달라야 합니다.
+ SLF4J API 모듈
    - 로깅에 대한 인터페이스를 제공합니다.
    - 결론적으로 로깅 동작에 대한 역할을 수행할 추상메서드를 제공합니다.
    - 하나의 API 모듈에 하나의 Binding 모듈이 필요합니다.
+ Binding 모듈
    - SLF4J API를 로깅 구현체와 연결하는 어댑터 역할을 하는 모듈입니다.
    - SLF4J를 구현한 클래스에서 Binding으로 연결된 Logger의 API를 호출합니다.
+ Logback
    - 로깅 구현체의 종류 중 하나로 스프링부트가 디폴트로 사용하는 라이브러리입니다.


### 간단 실습

```java
//@Slf4j
@RestController
public class LogController {
    
    // @Slf4j 애노테이션으로 생략이 가능한 코드
    private final Logger log = LoggerFactory.getLogger(getClass());

    @GetMapping("/log")
    public String logTest(){
        String name = "spring";

        //  {}는 쉼표 뒤에 파라미터가 치환되는 것
        log.error("error log={}",name);
        log.warn("warn log={}",name);
        log.info("info log={}",name);
        log.debug("debug log={}",name);
        log.trace("trace log={}",name);
        log.trace("trace log " + name);

        return "ok";
    }
}
```
스프링부트의 기본적인 세팅은 info level로 되어있습니다.  
따라서 실행시켜보면 debug와 trace는 찍히지 않습니다.  
__하지만 맨 마지막 줄의 trace는 + 연산자를 사용했는데 이는 trace가 실질적으로 호출되지 않지만 + 연산자가 실행되어 메모리에 올라가기 때문에 성능상 문제가 되어 위쪽에 사용한 방식을 사용하는 것이 좋습니다.__
<br>

## 5. Logback
---
Logback은 SLF4J의 구현체로 Log4J를 토대로 만든 프레임워크입니다.   
스프링 프레임워크에서도 SLF4J와 Logback을 디폴트로 사용하고 있습니다.  

![그림2](https://github.com/backtony/blog-code/blob/master/spring/img/log/2/log-2.PNG?raw=true)  

Logback은 3가지 모듈로 나뉩니다.  
+ logback-core
    - 다른 두 모듈을 위한 기반 역할을 하는 모듈입니다.
    - Appender와 Layout 인터페이스가 여기에 속합니다.
+ logback-classic
    - logback-core에서 확장된 모듈로 logback-core를 가지고 SLF4J API를 구현합니다.
    - Logger 클래스가 여기에 속합니다.
+ logback-access
    - Servlet 컨테이너와 통합되어 HTTP 액세스에 대한 로깅 기능을 제공합니다.
    - logback-core는 logback-access의 기반기술이기에 필요하지만, logback-classic과 SLF4J와는 무관합니다. 
    - 웹 애플리케이션 레벨이 아닌 컨테이너 레벨에서 설치되어야 합니다.

<br>

![그림3](https://github.com/backtony/blog-code/blob/master/spring/img/log/2/log-3.PNG?raw=true)  
+ Logger
    - 실제 로깅을 수행하는 구성요소 입니다.
    - 출력 레벨 : TRACE > DEBUG > INFO > WARN > ERROR
+ Appender
    - Logback은 로그 이벤트를 쓰는 작업을 Appender에게 위임합니다.
    - 로그 메시지가 출력될 대상을 결정하는 요소 입니다.
    - consoleAppender, FileAppender, RollingFileAppender 등이 있습니다.
+ Layout(Encoder)
    - 로그 이벤트를 바이트 배열로 변환하고, 해당 바이트 배열을 OutputStream에 쓰는 작업을 담당합니다.
    - Appender에 포함되어 사용자가 지정한 형식으로 표현될 로그 메시지를 변환하는 역할을 담당하는 요소입니다.
    - FileAppender와 하위 클래스는 encoder를 필요로 하고, 더이상 layout은 사용하지 않기에 이제는 layout보다는 Encoder을 사용합니다.

<br>

## 6. 로그 설정 파일
---
로그 설정을 yml로도 할 수 있지만 규모가 커지게 되면 logback.xml 파일로 세부적으로 설정해서 관리를 해줘야 합니다.  

### Log Pattern
로그 설정에 사용되는 패턴입니다.

+ %logger: 패키지 포함 클래스 정보
+ %logger{0}: 패키지를 제외한 클래스 이름만 출력
+ %logger{length}: Logger name을 축약할 수 있음. {length}는 최대 자리 수, ex)logger{35}
+ %-5level: 로그 레벨, -5는 출력의 고정폭 값(5글자), 로깅레벨이 Info일 경우 빈칸 하나 추가
+ ${PID:-}: 프로세스 아이디
+ %d: 로그 기록시간 출력
+ %p: 로깅 레벨 출력
+ %F: 로깅이 발생한 프로그램 파일명 출력
+ %M: 로깅일 발생한 메소드의 명 출력
+ %line: 로깅이 발생한 호출지의 라인
+ %L: 로깅이 발생한 호출지의 라인
+ %thread: 현재 Thread 명
+ %t: 로깅이 발생한 Thread 명
+ %c: 로깅이 발생한 카테고리
+ %C: 로깅이 발생한 클래스 명 (%C{2}는 somePackage.SomeClass 가 출력됨)
+ %m: 로그 메시지
+ %msg: - 로그 메시지 (=%message)
+ %n: 줄바꿈(new line)
+ %%: %를 출력
+ %r : 애플리케이션 시작 이후부터 로깅이 발생한 시점까지의 시간(ms)
+ %d{yyyy-MM-dd-HH:mm:ss:sss}: %d는 date를 의미하며 중괄호에 들어간 문자열은 dateformat을 의미. 따라서 [2021-07-12 12:42:78]과 같은 날짜가 로그에 출력됨.
+ %-4relative: %relative는 초 아래 단위 시간(밀리초)을 나타냄. -4를하면 4칸의 출력폼을 고정으로 가지고 출력. 따라서 숫자에 따라 [2021-07-12 12:42:78:232] 혹은 [2021-07-12 12:42:78:2332]와 같이 표현됨

### logback-spring.xml
#### 간단한 설정
logback의 설정은 main/resource/ 위치에서 logback.spring.xml 파일을 만들면 적용됩니다.  
```xml
<?xml version="1.0" encoding="UTF-8"?>

<configuration>
    <timestamp key="BY_DATE" datePattern="yyyy-MM-dd"/>
    <property name="LOG_PATTERN"
              value="[%d{yyyy-MM-dd HH:mm:ss}:%-4relative] %green([%thread]) %highlight(%-5level) %boldWhite([%C.%M:%yellow(%L)]) - %msg%n"/>

    <springProfile name="!prod">
        <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
            <encoder>
                <pattern>${LOG_PATTERN}</pattern>
            </encoder>
        </appender>

        <root level="INFO">
            <appender-ref ref="CONSOLE"/>
        </root>
    </springProfile>

</configuration>
```
+ property
    - 변수를 저장해는 곳이라고 생각하면 됩니다.
+ timestamp
    - property와 마찬가지로 변수로 사용됩니다.
    - key가 변수의 이름이 되고, datePattern을 이용해서 년-월-일 을 나타냈습니다.
+ springProfile
    - logback에서는 여러 개의 프로파일 설정이 가능합니다.
    - 위에서는 prod 환경이 아닐 때 사용하는 설정입니다.
+ appender
    - 어디다가 쓸지 정하는 부분으로 위에서는 consoleAppender을 사용했습니다.
+ encoder
    - Encoder는 로그 이벤트를 바이트 배열로 변환하고, 해당 바이트 배열을 OutputStream에 쓰는 작업을 담당합니다.
    - Appender에 포함되어 사용자가 지정한 형식으로 표현 될 로그메시지를 변환하는 역할을 담당하는 요소입니다.
    - 위에서는 pattern을 지정 형식으로 주었습니다.
    - 이 부분이 위에서 언급했던 Log Pattern을 사용하는 곳입니다.
+ root
    - 등록되어 있는 로그들의 최상위 클래스로 레벨을 정하는 곳입니다.
    - INFO로 설정해주었으므로 전체 로그가 이제 error, warn, info 만 찍히게 됩니다.
    - ppender-ref를 통해 위에서 작성한 appender을 넣어줍니다.


#### 복잡한 설정
이번에는 여러 profile 설정을 주고 각각에 따른 설정을 만들어보겠습니다.
```xml
<?xml version="1.0" encoding="UTF-8"?>

<configuration>
    <timestamp key="BY_DATE" datePattern="yyyy-MM-dd"/>
    <property name="LOG_PATTERN"
              value="[%d{yyyy-MM-dd HH:mm:ss}:%-4relative] %green([%thread]) %highlight(%-5level) %boldWhite([%C.%M:%yellow(%L)]) - %msg%n"/>

    <springProfile name="!prod">
        <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
            <encoder>
                <pattern>${LOG_PATTERN}</pattern>
            </encoder>
        </appender>

        <root level="INFO">
            <appender-ref ref="CONSOLE"/>
        </root>
    </springProfile>

    <springProfile name="prod">
        <appender name="FILE-INFO" class="ch.qos.logback.core.rolling.RollingFileAppender">
            <file>./log/info/info-${BY_DATE}.log</file>
            <filter class = "ch.qos.logback.classic.filter.LevelFilter">
                <level>INFO</level>
                <onMatch>ACCEPT</onMatch>
                <onMismatch>DENY</onMismatch>
            </filter>
            <encoder>
                <pattern>${LOG_PATTERN}</pattern>
            </encoder>
            <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
                <fileNamePattern> ./backup/info/info-%d{yyyy-MM-dd}.%i.log</fileNamePattern>
                <maxFileSize>100MB</maxFileSize>
                <maxHistory>30</maxHistory>
                <totalSizeCap>3GB</totalSizeCap>
            </rollingPolicy>
        </appender>

        <appender name="FILE-WARN" class="ch.qos.logback.core.rolling.RollingFileAppender">
            <file>./log/warn/warn-${BY_DATE}.log</file>
            <filter class = "ch.qos.logback.classic.filter.LevelFilter">
                <level>WARN</level>
                <onMatch>ACCEPT</onMatch>
                <onMismatch>DENY</onMismatch>
            </filter>
            <encoder>
                <pattern>${LOG_PATTERN}</pattern>
            </encoder>
            <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
                <fileNamePattern> ./backup/warn/warn-%d{yyyy-MM-dd}.%i.log</fileNamePattern>
                <maxFileSize>100MB</maxFileSize>
                <maxHistory>30</maxHistory>
                <totalSizeCap>3GB</totalSizeCap>
            </rollingPolicy>
        </appender>

        <appender name="FILE-ERROR" class="ch.qos.logback.core.rolling.RollingFileAppender">
            <file>./log/error/error-${BY_DATE}.log</file>
            <filter class = "ch.qos.logback.classic.filter.LevelFilter">
                <level>ERROR</level>
                <onMatch>ACCEPT</onMatch>
                <onMismatch>DENY</onMismatch>
            </filter>
            <encoder>
                <pattern>${LOG_PATTERN}</pattern>
            </encoder>
            <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
                <fileNamePattern> ./backup/error/error-%d{yyyy-MM-dd}.%i.log</fileNamePattern>
                <maxFileSize>100MB</maxFileSize>
                <maxHistory>30</maxHistory>
                <totalSizeCap>3GB</totalSizeCap>
            </rollingPolicy>
        </appender>

        <root level="INFO">
            <appender-ref ref="FILE-INFO"/>
            <appender-ref ref="FILE-WARN"/>
            <appender-ref ref="FILE-ERROR"/>
        </root>
    </springProfile>


</configuration>
```
springProfile이 !prod 환경인 부분은 위에서 언급했으니 prod 환경으로 설정된 부분부터 설명을 진행하겠습니다.  
+ appender
    - 이번에는 RollingFileAppender를 등록했습니다.
    - RollingFileAppender는 FileAppender를 상속하여 로그 파일을 rollover합니다. rollover는 타깃 파일을 바꾸는 것으로 이해할 수 있습니다. 
    - 예를 들어, 타깃 파일로 log.txt에 로그 메시지를 append 하다가 어느 지정한 조건에 다다르면 타킷 파일을 다른 파일로 바꿀 수 있습니다.
+ file
    - 파일의 저장 위치입니다.
    - 앞서 위쪽에 timestampe태그로 정의했던 BY_DATE를 사용하게 되면 파일명이 날짜별로 바뀌어 저장됩니다.
+ filter
    - 로그 레벨에 따른 필터 LevelFilter를 사용했습니다.
    - <level\>는 필터의 조건이 되는 로그 레벨을 입력합니다.
    - <onMatch\>의 ACCEPT는 위에 조건이 되는 레벨이 되면 실행, <onMismatch\>의 DENY는 위 조건이 아닐 경우 실행되지 않게 하는 것입니다.
    - if문이라고 생각하면 됩니다.
+ rollingPolicy
    - RollingFileAppender를 어떻게 사용할지에 대한 정책을 정하는 곳입니다.
    - 보통 TimeBasedRollingPolicy와 SizeAndTimeBasedRollingPolicy를 사용한다고 합니다.
    - SizeAndTimeBasedRollingPolicy은 시간과 사이즈를 기준하는 정책입니다.
+ fileNamePattern
    - 처음에 로그들은 file에 설정한 위치에 저장되다가 maxFileSize, maxHistory, totalSizeCap 옵션 중 어느 하나에 일치하게 되면 fileNamePattern대로 파일이 옮겨지게 되고, 다시 생성되는 로그는 file로 설정한 위치에 저장됩니다.
+ maxFileSize 
    - 파일 분할 용량으로 KB, MB, GB가 있습니다.
+ maxHistory
    - 파일이 저장될 수 있는 기간을 설정합니다.
+ totalSizeCap
    - 전체 파일 크기를 제어하여, 전체 크기 제한을 초과하면 가장 오래된 파일을 삭제합니다.
+ root
    - 전체 프로젝트의 로그 레벨을 설정합니다.
+ appender-ref
    - 사용하고자 하는 appender을 등록합니다.
    - 위에서 작성한 appender의 name을 적어주면 됩니다.

### 설정 분리하기
위에서 작성한 로그 설정은 한 파일에 다 존재하기 때문에 가독성이 떨어지는 문제가 있습니다.  
가장 많은 부분을 차지하는 태그는 appender 입니다.  
따라서 appender 부분을 따로 관리한다면 더욱 가독성이 좋게 만들 수 있습니다.  
이는 include 태그를 사용하여 해결할 수 있습니다.  
main/resource 부분에서 xml 파일을 여러개 만들고 분리해보겠습니다.  
<br>

__console-appender.xml 파일__  
```xml
<included>
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>${LOG_PATTERN}</pattern>
        </encoder>
    </appender>
</included>
```
<br>

__file-error-appender.xml 파일__  
```xml
<included>
    <appender name="FILE-ERROR" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>./log/error/error-${BY_DATE}.log</file>
        <filter class="ch.qos.logback.classic.filter.LevelFilter">
            <level>ERROR</level>
            <onMatch>ACCEPT</onMatch>
            <onMismatch>DENY</onMismatch>
        </filter>
        <encoder>
            <pattern>${LOG_PATTERN}</pattern>
        </encoder>
        <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
            <fileNamePattern>./backup/error/error-%d{yyyy-MM-dd}.%i.log</fileNamePattern>
            <maxFileSize>100MB</maxFileSize>
            <maxHistory>30</maxHistory>
            <totalSizeCap>3GB</totalSizeCap>
        </rollingPolicy>
    </appender>
</included>
```
기존 logback-spring.xml 파일에서 appender 부분만 드러내서 파일을 따로 만들고 included 태그로 감싸주면 됩니다.  
이렇게 다른 appender 파일도 마찬가지로 included 태그를 이용해 분리해줍니다.
<Br>

__logback-spring.xml 파일__  
```xml
<?xml version="1.0" encoding="UTF-8"?>

<configuration>
    <timestamp key="BY_DATE" datePattern="yyyy-MM-dd"/>
    <property name="LOG_PATTERN"
              value="[%d{yyyy-MM-dd HH:mm:ss}:%-4relative] %green([%thread]) %highlight(%-5level) %boldWhite([%C.%M:%yellow(%L)]) - %msg%n"/>

    <springProfile name="!prod">
        <include resource="console-appender.xml"/>

        <root level="INFO">
            <appender-ref ref="CONSOLE"/>
        </root>
    </springProfile>

    <springProfile name="prod">
        <include resource="file-info-appender.xml"/>
        <include resource="file-warn-appender.xml"/>
        <include resource="file-error-appender.xml"/>

        <root level="INFO">
            <appender-ref ref="FILE-INFO"/>
            <appender-ref ref="FILE-WARN"/>
            <appender-ref ref="FILE-ERROR"/>
        </root>
    </springProfile>
</configuration>
```
메인이 되는 logback-spring.xml 파일에서는 included로 분리한 다른 appender.xml 파일들을 include 시켜줍니다.  
<br><br><Br>


이렇게 spring의 로그 설정을 알아보았습니다.  
이전에 진행하던 프로젝트에서는 xml를 사용하기 싫어서 yml 파일에 간단하게 로그 설정을 집어넣고 사용하곤 했는데 아직까지는 상세한 설정을 하기 위해선 xml 설정방식을 사용해야하는 것 같습니다.  
여기까지 글을 마치겠습니다. (__)




<Br><Br>

__참고__  
<a href="https://www.youtube.com/watch?v=1MD5xbwznlI" target="_blank"> [10분 테코톡] ☂️ 검프의 Logging(로깅) #1</a>   
<a href="https://www.youtube.com/watch?v=JqZzy7RyudI" target="_blank"> [10분 테코톡] ☂️ 검프의 Logging(로깅) #2</a>   
<a href="https://livenow14.tistory.com/64" target="_blank"> Logback이란?</a>   


