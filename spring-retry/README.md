## Spring Retry 소개
Spring의 재시도 기능은 스프링 배치에 포함되어 있다가 2.2.0 버전부터 제외되어 현재는 Spring Retry 라이브러리에 포함되어 있습니다.  
Spring Retry는 동작이 실패하더라도 몇 번 더 시도하면 성공할 수 있는 작업은, 자동으로 다시 시도할 수 있는 기능을 제공합니다.

## build.gradle
```groovy
implementation 'org.springframework:spring-aspects'
implementation 'org.springframework.retry:spring-retry'
```
의존성을 추가해줍니다.

## @EnableRetry
```java
@Configuration
@EnableRetry
public class RetryConfig {
}
```
애플리케이션에서 Spring Retry를 활성화하려면 @Configuration 클래스에 @EnableRetry 애너테이션을 추가해줘야 합니다.

## @Retryable
```java
@Slf4j
@Service
public class RetryService {

    private int count;

    @Retryable(
            value = RuntimeException.class, // retry 예외 대상
            maxAttempts = 3, // 3회 시도
            backoff = @Backoff(delay = 2000) // 재시도 시 2초 후 시도
    )
    public int retrySuccess(){
        count++;
        if (count % 2 == 0){
            return count;
        }
        throw new RuntimeException("runtime exception");
    }
}
```
@Retryable 애너테이션을 사용하면 실패 시 재시도할 방식을 세팅해줄 수 있습니다.
+ value : 예외 대상
+ maxAttempts : 재시도 횟수
+ backoff : 재시도 사이의 시간 간격

위 코드 상으로는 재시도 시 coun가 짝수가 되면서 count를 정상적으로 반환하게 됩니다.

## @Recover
```java
@Slf4j
@Service
public class RetryService {

    @Retryable(
            value = RuntimeException.class, // retry 예외 대상
            maxAttempts = 3, // 3회 시도
            backoff = @Backoff(delay = 2000) // 재시도 시 2초 후 시도
    )
    public int retryFail(String name){
        throw new RuntimeException("runtime exception");
    }

    // target 메서드와 반환값 일치
    // 메서드의 첫 인자는 예외, 이후 인자는 타겟 메서드와 타입을 일치 시켜야함
    @Recover
    public int recover(RuntimeException e, String name) {
        log.info("{}", name);
        return -1;
    }
}
```
@Recover는 @Retryable 세팅에 의해 재시도를 했음에도 불구하고 실패했을 경우 후처리를 담당합니다.  
@Recover 메서드는 @Retryable 메서드의 반환타입이 일치해야 하며, 첫 인자로는 예외, 다음 인자로는 @Retryable 메서드의 인자 타입과 일치해야 합니다.  
<br>

만약 반환 타입도 같고 매개변수의 타입도 같은 경우 옵션으로 recover 메서드를 지정할 수 있습니다.
```java
@Slf4j
@Service
public class RetryService {

    @Retryable(
            value = RuntimeException.class, 
            maxAttempts = 3, 
            backoff = @Backoff(delay = 2000) 
    )
    public int retryFail(String name){
        throw new RuntimeException("runtime exception");
    }

    @Recover
    public int recover(RuntimeException e, String name) {
        log.info("{}", name);
        return -1;
    }

    @Retryable(
            value = RuntimeException.class, 
            maxAttempts = 3, 
            backoff = @Backoff(delay = 2000), 
            recover = "recover2", // 특정 recover 지정법 -> 메서드 이름 명시
    )
    public int retryFail2(String name){
        throw new RuntimeException("runtime exception");
    }

    @Recover
    public int recover2(RuntimeException e, String name) {
        log.info("{}", name);
        return -2;
    }
}
```

## RetryTemplate
애너테이션을 통해 간단하게 해결하는 방법도 있지만 RetryTemplate을 사용하는 방법도 있습니다.
```java
public interface RetryOperations {

    <T, E extends Throwable> T execute(RetryCallback<T, E> retryCallback) throws E;

    <T, E extends Throwable> T execute(RetryCallback<T, E> retryCallback, RecoveryCallback<T> recoveryCallback)
        throws E;

    <T, E extends Throwable> T execute(RetryCallback<T, E> retryCallback, RetryState retryState)
        throws E, ExhaustedRetryException;

    <T, E extends Throwable> T execute(RetryCallback<T, E> retryCallback, RecoveryCallback<T> recoveryCallback,
        RetryState retryState) throws E;

}
```
Spring 에서는 Retry 기능을 제공하는 RetryOperations 인터페이스를 제공하고 구현체로 RetryTemplate을 제공합니다.  
execute의 매개변수 retryCallback은 실패 시 재시도해야 하는 비즈니스 로직 삽입을 허용하는 인터페이스입니다.  
<br>

### Config 설정
```java
@Configuration
@EnableRetry
public class RetryConfig {

    @Bean
    public RetryTemplate retryTemplate(){
        RetryTemplate retryTemplate = new RetryTemplate();

        FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
        backOffPolicy.setBackOffPeriod(2000); // 재시도 대기 시간 2초
        retryTemplate.setBackOffPolicy(backOffPolicy);

        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(3); // 재시도 횟수
        retryTemplate.setRetryPolicy(retryPolicy);

        return retryTemplate;
    }
}
```
FixedBackOffPolicy는 재시도 대기 시간을 지정하고 SimpleRetryPolicy는 재시도 횟수를 지정합니다.

### 사용법
```java
@RestController
@RequiredArgsConstructor
public class RetryRestController {

    private final RetryService retryService;
    private final RetryTemplate retryTemplate;

    @GetMapping("/template")
    public int template(){
        return retryTemplate.execute(
                context -> retryService.retryTemplate() // 타겟 메서드 호출 -> 예외 발생 시 이 메서드를 재시도함
                , context -> retryService.retryTemplateRecover()); // @Recover에 해당하는 로직

//        람다식 풀어쓴 방식
//        return retryTemplate.execute(new RetryCallback<Integer, RuntimeException>() {
//                                         @Override
//                                         public Integer doWithRetry(RetryContext context) throws RuntimeException {
//                                             return retryService.retryTemplate();
//                                         }
//                                     }
//                , new RecoveryCallback<Integer>() {
//                    @Override
//                    public Integer recover(RetryContext context) throws Exception {
//                        return retryService.retryTemplateRecover();
//                    }
//                });
    }
}
```
execute의 첫 인자로는 수행할 메서드를, 두 번째 인자로는 재시도를 모두 수행 했을 때 실패하게 될 경우 수행할 메서드를 넣어주면 됩니다.  
재시도 후 실패 시 수행할 로직이 필요하지 않다면 첫 인자만 넣어줘도 됩니다.

## Listener
```java
public class RetryListenerSupport implements RetryListener {

	public <T, E extends Throwable> void close(RetryContext context, RetryCallback<T, E> callback,
			Throwable throwable) {
	}

	public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback,
			Throwable throwable) {
	}

	public <T, E extends Throwable> boolean open(RetryContext context, RetryCallback<T, E> callback) {
		return true;
	}

}
```
리스너는 재시도 추가 콜백을 제공합니다.  
RetryListenerSupport가 기본적으로 제공되기 때문에 상속받아서 구현하면 됩니다.

### 구현
```java
@Slf4j
public class DefaultRetryListener extends RetryListenerSupport {

    @Override
    public <T, E extends Throwable> boolean open(RetryContext context, RetryCallback<T, E> callback) {        
        // 로직 작성
        log.info("before call target method");        
        return super.open(context, callback);
    }

    @Override
    public <T, E extends Throwable> void close(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
        // 로직 작성
        log.info("after retry");
        super.close(context, callback, throwable);
    }

    @Override
    public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
        // 로직 작성
        log.info("on error");
        super.onError(context, callback, throwable);
    }
}
```
+ open : 재시도 타겟 메서드(@Retryable이 붙은) 메서드 자체가 호출되기 전
    - 예를 들면, Controller에서 @Retryable이 붙은 Service 메서드를 호출하게 된다면 Service 메서드가 호출되기 직전
+ close : 재시도가 전부 끝난 후에 호출
+ onError : 재시도 타겟 메서드에서 지정한 예외가 발생하면 호출


### 적용
#### RetryTemplate
```java
@Configuration
@EnableRetry
public class RetryConfig {

    @Bean
    public RetryTemplate retryTemplate(){
        RetryTemplate retryTemplate = new RetryTemplate();

        FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
        backOffPolicy.setBackOffPeriod(2000); // 재시도 대기 시간 2초
        retryTemplate.setBackOffPolicy(backOffPolicy);

        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(3); // 재시도 횟수
        retryTemplate.setRetryPolicy(retryPolicy);

        // 리스너 등록
        retryTemplate.registerListener(new DefaultRetryListener());

        return retryTemplate;
    }
}
```
RetryTemplate에 적용하기 위해서는 Config에 추가해주기만 하면 됩니다.

#### @Retryable
```java
@Configuration
@EnableRetry
public class RetryConfig {

    @Bean
    public DefaultRetryListener defaultRetryListener(){
        return new DefaultRetryListener();
    }
}
```
```java
@Slf4j
@Service
public class RetryService {

    @Retryable(
            value = RuntimeException.class, 
            maxAttempts = 3, 
            backoff = @Backoff(delay = 2000), 
            listeners = "defaultRetryListener" 
    )
    public int retryFail2(String name){
        log.info("service method call");
        throw new RuntimeException("runtime exception");
    }
}
```
애너테이션의 옵션으로 등록하고자 한다면 리스너를 우선 빈으로 등록해줍니다.  
그리고 애너테이션 옵션으로 빈 이름을 명시해줍니다.


<br><Br><br>

---

__참고__  

[Guide to Spring Retry](https://www.baeldung.com/spring-retry)  
[공식 문서](https://docs.spring.io/spring-batch/docs/current/reference/html/retry.html)  

