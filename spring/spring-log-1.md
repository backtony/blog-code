# Spring p6spy 적용하기


## 1. p6spy란?
---
p6spy란 쿼리 파라미터를 로그에 남겨주고 추가적인 기능을 제공하는 외부 라이브러리입니다. 사실 이 외부 라이브러리 없이도 application.yml에 다음과 같은 설정을 통해 쿼리 파라미터의 값들을 찍을 수 있습니다.
```yml
# application.yml
logging:
  level:
    org.hibernate.SQL: debug
    org.hibernate.type: trace

    # for native query
    org.springframework.jdbc.core.JdbcTemplate: DEBUG
    org.springframework.jdbc.core.StatementCreatorUtils: TRACE    
```
이렇게 값을 찍게 될 경우 아래 그림과 같이 ?가 찍히고 그 아래 실제 들어간 파라미터 값을 알려줍니다.  

![그림1](https://github.com/backtony/blog-code/blob/master/spring/img/log/1/1-1.PNG?raw=true)

지금은 쿼리가 간단하고 하나의 파라미터만 들어가기 때문에 그나마 보기 편하지만, 쿼리가 복잡해지고 들어가는 파라미터가 많아지면 한줄씩 쿼리 파리머타가 쭉 나열되기 때문에 확인하기 매우 불편합니다. 따라서 p6spy와 같은 외부 라이브러리를 사용하여 보기 쉽게 만드는게 개발하기 편합니다.
<br><br>


## 2. 로직 알아보기
---

### p6spy의 쿼리 캡처 과정
![그림2](https://github.com/backtony/blog-code/blob/master/spring/img/log/1/1-2.PNG?raw=true)

1. DataSource를 래핑하여 프록시를 만듭니다.
2. 쿼리가 발생하여 JDBC가 ResultSet 을 반환하면 이를 만들어둔 프록시가 가로챕니다.
3. 내부적으로 ResultSet의 정보를 분석하고 p6spy의 옵션을 적용합니다.
4. Slf4j 를 사용해 로깅합니다.

<br>

### logMessageFormat
p6spy가 사용하는 포맷은 다음 3가지 종류가 있습니다.
+ SingleLineFormat : 기본 설정이 되어있는 Format
+ CustomLineFormat : 커스터마이징 할 포메터가 아니고 SingleLineFormat을 손본 Format
+ MultiLineFormat : 한 줄로 쭉 늘어진 log를 쿼리문 두 줄만 밑으로 내려주는 Format

<br>

### p6spy의 Format 선택 과정
Format 선택 과정 전에 p6spy의 Default 설정 파일 __P6SpyProperties.java__ 을 보겠습니다. 아래 코드가 기본 설정이구나 하고만 넘어가시면 됩니다.
```java
package com.github.gavlyukovskiy.boot.jdbc.decorator.p6spy;

import com.p6spy.engine.logging.P6LogFactory;
import com.p6spy.engine.spy.appender.FormattedLogger;
import lombok.Getter;
import lombok.Setter;

import java.util.regex.Pattern;

/**
 * Properties for configuring p6spy.
 *
 * @author Arthur Gavlyukovskiy
 */
@Getter
@Setter
public class P6SpyProperties {

    /**
     * Enables logging JDBC events.
     *
     * @see P6LogFactory
     */
    private boolean enableLogging = true;
    /**
     * Enables multiline output.
     */
    private boolean multiline = true;
    /**
     * Logging to use for logging queries.
     */
    private P6SpyLogging logging = P6SpyLogging.SLF4J;
    /**
     * Name of log file to use (only with logging=file).
     */
    private String logFile = "spy.log";
    /**
     * Custom log format.
     */
    private String logFormat;

    /**
     * Tracing related properties
     */
    private P6SpyTracing tracing = new P6SpyTracing();

    /**
     * Class file to use (only with logging=custom).
     * The class must implement {@link com.p6spy.engine.spy.appender.FormattedLogger}
     */
    private Class<? extends FormattedLogger> customAppenderClass;

    /**
     * Log filtering related properties.
     */
    private P6SpyLogFilter logFilter = new P6SpyLogFilter();

    public enum P6SpyLogging {
        SYSOUT,
        SLF4J,
        FILE,
        CUSTOM
    }

    @Getter
    @Setter
    public static class P6SpyTracing {
        /**
         * Report the effective sql string (with '?' replaced with real values) to tracing systems.
         * <p>
         * NOTE this setting does not affect the logging message.
         */
        private boolean includeParameterValues = true;
    }

    @Getter
    @Setter
    public static class P6SpyLogFilter {
        /**
         * Use regex pattern to filter log messages. Only matched messages will be logged.
         */
        private Pattern pattern;
    }
}
```
<br>

앞서 기본 설정을 확인했고 이제는  __PsSpyConfiguration.java__ 에서 실제로 어떤 방식으로 Format을 선택하는지 알아보겠습니다. 코드 하단에 보시면 이와 같은 코드가 있습니다.
```java
if (!initialP6SpyOptions.containsKey("logMessageFormat")) {
    if (p6spy.getLogFormat() != null) {
        System.setProperty("p6spy.config.logMessageFormat", "com.p6spy.engine.spy.appender.CustomLineFormat");
        System.setProperty("p6spy.config.customLogMessageFormat", p6spy.getLogFormat());
    }
    else if (p6spy.isMultiline()) {
        System.setProperty("p6spy.config.logMessageFormat", "com.p6spy.engine.spy.appender.MultiLineFormat");
    }
}
```
Default 설정에 따르면 getLogFormat 은 null이므로 else if 문을 타면서 MultiLineFormat 을 선택하게 됩니다.  
<br>

아래의 MultiLineFormat을 보시면 formatMessage를 재정의 하면서 어떤 포맷으로 출력을 찍어내는지 확인할 수 있습니다. 결론적으로 이 코드를 수정하면 저희가 원하는 대로 콘솔에 찍어낼 수 있습니다.
```java
public class MultiLineFormat implements MessageFormattingStrategy {

  @Override
  public String formatMessage(final int connectionId, final String now, final long elapsed, final String category, final String prepared, final String sql, final String url) {
    return "#" + now + " | took " + elapsed + "ms | " + category + " | connection " + connectionId + "| url " + url + "\n" + prepared + "\n" + sql +";";
  }
  
}
```


<br><br>

## 3. 세팅하기
---
build.gradle 의존성을 추가와 yml 파일에 설정 세팅을 해줍니다. 자세한 내용은 [[여기](https://github.com/gavlyukovskiy/spring-boot-data-source-decorator)]를 참고하시면 됩니다.
```
// build.gradle
implementation 'com.github.gavlyukovskiy:p6spy-spring-boot-starter:1.7.1'

//application.yaml
decorator:
  datasource:
    p6spy:
      enable-logging: true
```
__생각보다 많은 자원을 소모하기 때문에 운영 환경에서는 반드시 enable-logging 를 false로 두고 사용하지 않도록 설정해두어야 합니다.__

<br>

MessageFormattingStrategy 을 구현해서 Format을 나에게 맞게 만들어 주시고 만들 Format을 적용시켜주기만 하면 끝입니다.
```java
public class P6spyPrettySqlFormatter implements MessageFormattingStrategy {

    @Override
    public String formatMessage(int connectionId, String now, long elapsed, String category, String prepared, String sql, String url) {
     sql = formatSql(category, sql);
        Date currentDate = new Date();

        SimpleDateFormat format1 = new SimpleDateFormat("yy.MM.dd HH:mm:ss");

        //return now + "|" + elapsed + "ms|" + category + "|connection " + connectionId + "|" + P6Util.singleLine(prepared) + sql;
        return format1.format(currentDate) + " | "+ "OperationTime : "+ elapsed + "ms" + sql;
    }

    private String formatSql(String category,String sql) {
        if(sql ==null || sql.trim().equals("")) return sql;

        // Only format Statement, distinguish DDL And DML
        if (Category.STATEMENT.getName().equals(category)) {
            String tmpsql = sql.trim().toLowerCase(Locale.ROOT);
            if(tmpsql.startsWith("create") || tmpsql.startsWith("alter") || tmpsql.startsWith("comment")) {
                sql = FormatStyle.DDL.getFormatter().format(sql);
            }else {
                sql = FormatStyle.BASIC.getFormatter().format(sql);
            }
            sql = "|\nHeFormatSql(P6Spy sql,Hibernate format):"+ sql;
        }

        return sql;
    }
}
```
저는 pretty하게 콘솔에 찍히고 현재 날짜와 수행시간만 찍히도록 만들었습니다.

<br>

이제 만든 Format을 적용해 줍니다.
```java
@Configuration
public class P6spyConfig {    
    @PostConstruct
    public void setLogMessageFormat() {
        P6SpyOptions.getActiveInstance().setLogMessageFormat(P6spyPrettySqlFormatter.class.getName());
    }    
}
```
<Br>

그럼 이제 아래 그림처럼 우측 상단에는 날짜와 시간이 아래쪽에는 쿼리들이 pretty 하게 찍히는 것을 확인할 수 있습니다.
![그림3](https://github.com/backtony/blog-code/blob/master/spring/img/log/1/1-3.PNG?raw=true)


<br><br>

## 4. @DataJpaTest에서 사용하기
---
위와 같은 작업을 끝냈더라도 @DataJpaTest에서는 동작하지 않습니다. 이유는 간단합니다. @DataJpaTest는 말 그대로 JPA 관련 테스트를 하기 위한 환경만 올라가기 때문입니다. 해결 방법 또한 간단합니다. @DataJpaTest 애노테이션을 사용하면서 테스트하는 시점에 SQL log를 출력하기 위한 환경을 같이 올리면 됩니다. 그럼 애노테이션을 커스텀해봅시다. 결과는 아래와 같습니다.

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@DataJpaTest(showSql = false)
@ImportAutoConfiguration(DataSourceDecoratorAutoConfiguration.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(P6spyConfig.class)
public @interface CustomDataJpaTest {
}
```
+ DataJpaTest를 사용하도록 붙여주시고 showSql은 false로 줌으로써 Spring Data JPA가 기본으로 제공해주는 SQL 문 출력 기능을 사용하지 않도록 해줍니다.
+ @ImportAutoConfiguration 는 자동환경설정 클래스를 Import하는 기능을 합니다.
+ DataSourceDecoratorAutoConfiguration.class 는 application.yml 파일에서 사용하고 있는 DataSource를 프록시한 객체로 만들어주는 역할을 하는 클래스입니다.
+ DataJpaTest 애노테이션을 까보면 @AutoConfigureTestDatabase이 붙어있습니다. 이 애노테이션은 아래 코드로 작성해두었습니다. 해당 코드를 보시면 기존에 설정되어 있는 데이터베이스 설정 대신 테스트용 인메모리를 강제로 사용하는 Replace.ANY로 설정이 되어 있습니다. 따라서 이 값은 Replace.NONE 으로 변경해야 저희가 의도했던 대로 동작합니다.
+ @Import(P6spyConfig.class) 는 앞서 p6spy Config 세팅을 import 시켜줍니다.

<br>

```java
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@ImportAutoConfiguration
@PropertyMapping("spring.test.database")
public @interface AutoConfigureTestDatabase {

    /**
     * Determines what type of existing DataSource beans can be replaced.
     * @return the type of existing DataSource to replace
     */
    @PropertyMapping(skip = SkipPropertyMapping.ON_DEFAULT_VALUE)
    Replace replace() default Replace.ANY;  // <-- 여기

...
```

이제부터 @DataJpaTest 대신 @CustomDataJpaTest 를 사용하면 원했던 대로 p6spy를 사용할 수 있습니다.



<Br><Br>

__참고__  
<a href="https://github.com/shirohoo/p6spy-custom-formatter" target="_blank"> p6spy custom formatter</a>   
<a href="https://zgundam.tistory.com/199" target="_blank"> @DataJpaTest와 p6spy 함께 사용하기</a>   



