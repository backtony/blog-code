# Spring - @Async



## 간단한 비동기 처리
```java
@EnableAsync
@SpringBootApplication
public class AsyncApplication {

    public static void main(String[] args) {
        SpringApplication.run(AsyncApplication.class, args);
    }
}
```
```java
@Service
public class UserService {

    @Async
    public void asyncMethod(){

    }
}
```
@EnableAsync 어노테이션을 Application 클래스 위에 붙여 주고, 비동기 방식으로 처리하고 싶은 동기 로직의 메소드 위에 @Async 어노테이션을 붙이면 비동기로 동작합니다. 하지만 위 방식은 스레드를 관리하지 않는다는 문제가 있습니다. @Async의 기본 설정은 SimpleAsyncTaskExecutor를 사용하도록 되어 있는데, 이것은 스레드 풀이 아니고 단순히 스레드를 만들어내는 역할을 하기 때문입니다.  
기본값 세팅은 다음과 같이 구성되어있습니다.
+ corePoolSize : 1
+ maxPoolSize : Integer.MAX_VALUE
+ keepAliveSeconds : 60(second)
+ QueueCapacity : Integer.MAX_VALUE
+ AllowCoreThreadTimeOut : false
+ WaitForTasksToCompleteOnShutdown : false
+ RejectedExecutionHandler : AbortPolicy

스레드를 재사용하지 않고 호출마다 새로운 스레드를 시작하며, AbortPolicy 이므로 처리할 수 없는 수준이 되면 Exception을 발생시키며  종료됩니다.  
상세한 세팅 설정은 개별 적용법에서 진행하겠습니다.  

## 비동기 커스텀
간단하게 연습하는 경우가 아니라면 비동기 설정을 따로 커스텀해서 사용해야 합니다.  
애플리케이션 전체에 적용하는 방법이 있고 개별적으로 적용하는 방법이 있습니다.  

### 전체 적용법
```java
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(30);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("DDAJA-ASYNC-");
        executor.initialize();
        return executor;
    }
}
```
AsyncConfigurer 인터페이스의 getAsyncExecutor 메서드에서 ThreadPoolTaskExecutor을 재정의해줍니다.  
이제 앞으로 @Async 애너테이션을 붙이는 쪽에서는 재정의한 ThreadPoolTaskExecutor의 설정을 따르게 됩니다.  


### 개별 적용법
```java
@Configuration
@EnableAsync
public class AsyncConfig {

    private static final int CORE_POOL_SIZE = 5;
    private static final int MAX_POOL_SIZE = 30;
    private static final int QUEUE_CAPACITY = 50;

    @Bean(name = "EVENT_HANDLER_TASK_EXECUTOR")
    public Executor eventHandlerTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(CORE_POOL_SIZE);
        executor.setMaxPoolSize(MAX_POOL_SIZE);
        executor.setQueueCapacity(QUEUE_CAPACITY);
        executor.setThreadNamePrefix("event-handler-");
        executor.initialize();
        return executor;
    }

    @Bean(name = "HELLO_WORLD_EXECUTOR")
    public Executor helloWorldExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(CORE_POOL_SIZE);
        executor.setMaxPoolSize(MAX_POOL_SIZE);
        executor.setQueueCapacity(QUEUE_CAPACITY);
        executor.setThreadNamePrefix("hello-world");
        executor.initialize();
        return executor;
    }
}
```
```java
@Service
public class UserService {

    @Async("EVENT_HANDLER_TASK_EXECUTOR")
    public void asyncMethod(){

    }

    @Async("HELLO_WORLD_EXECUTOR")
    public void asyncMethod2(){

    }
}
```
각 Executor 세팅을 따로 설정해두고 빈을 등록한 뒤에 사용처에서는 @Async 옵션에 빈 이름을 명시해주면 각 세팅의 Executor로 동작하게 됩니다.  
executor의 세팅값은 다음과 같습니다.
+ CorePoolSize : 기본적으로 생성해두고 실행 대기하는 Thread의 수
+ MaxPoolSize : 동시 동작하는 최대 Thread의 수
+ QueueCapacity 
    - MaxPoolSize 초과 요청에서 Thread 생성 요청시, 해당 요청을 Queue에 저장하는데 이때 최대 수용 가능한 Queue의 크기
    - Queue에 저장되어있다가 Thread에 자리가 생기면 하나씩 빠져나가 스레드 할당
+ ThreadNamePrefix : 생성되는 Thread 접두사 지정
+ initialize() : 생성하고 사용할 수 있도록하는 설정

위에서는 이정도의 세팅을 해주었지만 추가적은 세팅도 가능합니다.
+ keepAliveSeconds 
    - maxPoolSize까지 스레드가 생성되어 모두 사용되다가 idle로 돌아갔을 때 종료하기까지 대기하는 걸리는 시간
    - 예를들어 core size가 5이며, max size가 15라고 하겠습니다. 요청이 많아져서 thread pool이 바빠져서 추가적으로 10개의 thread를 생성해서 15개의 thread를 사용하게됩니다. 그리고 얼마 후 바빠진게 해소가 되어서 이제 10개의 추가된 thread는 한가한(idle) 상태가 되게됩니다. 그리고 자원의 절약을 위해 한가한 상태의 thread가 죽게(die) 됩니다. thread가 idle 상태에서 die 상태가 되기까지 대기하는 시간을 keepAlivesSeconds 옵션으로 설정할 수 있습니다. thread를 다시 생성하는 비용과 idle 상태로 유지하는 비용의 trade-off를 잘 생각해서 설정하면 좋습니다
+ WaitForTasksToCompleteOnShutdown : 시스템을 종료(shutdown)할 때 queue에 남아있는 작업을 모두 완료한 후 종료 하도록 처리합니다.
+ AwaitTerminationSeconds : 시스템을 종료(shutdown)할 때 queue에 남아있는 작업을 모두 완료한 후 종료 하도록 처리하거나 타임아웃을 지정해서 해당 시간이 넘으면 강제 종료합니다.
+ RejectedExecutionHandler : ThreadPoolExecutor에서 task를 더 이상 받을 수 없을 때 호출됩니다. 이런 경우는 queue 허용치를 초과하거나 Executor가 종료되어 Thread 또는 큐를 사용할 수 없는 경우에 발생하게 됩니다.
    - ThreadPoolExecutor.AbortPolicy() : default로 Reject된 작업이 RejectedExecutionException을 던집니다.
    - ThreadPoolExecutor.CallerRunsPolicy() : 호출한 Thread에서 reject된 작업을 대신 실행합니다.
    - ThreadPoolExecutor.DiscardPolicy() : Exception 없이 Reject된 작업을 버립니다.
    - ThreadPoolExecutor.DiscardOldestPolicy() : queue에서 가장 오래되고 처리되지 않은 요청을 삭제하고 다시 시도합니다.

### 예외 처리
@Async가 붙으면 비동기로 동작하므로 메서드 반환 타입은 다음과 같습니다.
+ Future
+ CompletableFuture
+ ListenableFuture
+ void

반환 타입이 Future 타입의 경우, 예외를 던지지만 void인 경우 예외가 호출 스레드로 전파되지 않기에 따로 예외처리가 필요합니다.  
```java
@Slf4j
public class CustomAsyncExceptionHandler implements AsyncUncaughtExceptionHandler {
    @Override
    public void handleUncaughtException(Throwable ex, Method method, Object... params) {
        log.error("async return 타입이 void 인 경우 예외 처리");
   }
}
```
AsyncUncaughtExceptionHandler 인터페이스를 구현하여 사용자 지정 비동기 예외 처리기를 만듭니다.  
잡히지 않은 비동기 예외가 있는 경우 handleUncaughtException 메서드가 호출됩니다.  
```java
@Configuration
@EnableAsync
public class GlobalAsyncConfig implements AsyncConfigurer {
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new CustomAsyncExceptionHandler();
    }
}
```
앞서 전체 적용법에서 사용했던 구현체에 등록해주면 됩니다.  

## 주의사항
@Async 애너테이션은 Spring AOP가 적용됩니다.  
즉, @Async가 적용된 Method는 Spring이 method를 가로채 다른 Thread에서 실행 시켜주는 동작 방식입니다.  
Spring AOP에 의해 동작하기 때문에 AOP의 주의점을 그대로 가져갑니다.
+ private method에는 사용 불가능, public method에만 사용 가능
+ [self-invocation(자가 호출) 불가](https://velog.io/@backtony/Spring-AOP-%EC%B4%9D%EC%A0%95%EB%A6%AC#%ED%94%84%EB%A1%9D%EC%8B%9C-%EB%82%B4%EB%B6%80-%ED%98%B8%EC%B6%9C)  
+ RejectedExecutionHandler세팅이 default일 경우, 호출하는 쪽에느 RejectedExecutionException 방어 로직을 작성해야 합니다.(ex. try-catch)


## 반환값에 따른 동작
앞서 예외 처리에서 언급했듯이 @Async가 붙으면 4가지 반환 타입을 사용할 수 있습니다.
+ Future
+ CompletableFuture
+ ListenableFuture
+ void

@Async 애너테이션으로 선언된 메서드는 리턴 타입에 따라 내부적으로 상이하게 동작합니다. 

### Void
```java
@Slf4j
@Service
@RequiredArgsConstructor
public class CallService {

    private final UserService userService;

    public void call(){
        userService.hello();
        log.info("call");
    }
}
```
```java
@Slf4j
@Service
public class UserService {

    @Async("HELLO_WORLD_EXECUTOR")
    public void hello(){
        try {
            Thread.sleep(1000);
            log.info("{}","hello");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
```
CallService에서 call 메서드가 UserService의 비동기 메서드 hello를 호출합니다. 비동기 메서드이므로 call을 호출한 스레드는 call의 수행을 기다리지 않고 __(논블로킹)__ 바로 log.info를 호출합니다.  

### Future
메서드의 결과값은 전달받아야 한다면 Future을 사용해야 합니다. 앞서 설명한 나머지 타입 모두 Future 계열에 해당하고 Future이 기본이 됩니다. Spring에서 제공하는 AsyncResult는 Future의 구현체이며 이를 사용해 Future 타입으로 리턴할 수 있습니다.  
```java
@Slf4j
@Service
@RequiredArgsConstructor
public class CallService {

    private final UserService userService;

    public void futureCall() {
        Future<String> future = userService.returnFuture();
        try {
            log.info("{}",future.get());
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.info("origin void call");
    }
}
```
```java
@Slf4j
@Service
public class UserService {

    @Async
    public Future<String> returnFuture(){
        try {
            Thread.sleep(1000);
            log.info("{}","async return future");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return new AsyncResult<>("async return future");
    }
}
```
futureCall 메서드에서 비동기 메서드 returnFuture을 호출하여 결과값을 get으로 꺼냅니다. 비동기 메서드는 비동기로 동작하지만 future의 get 메서드는 메서드의 결과를 조회할 때까지 계속 기다리게 됩니다. 즉, __블로킹__ 현상이 발생합니다.  
Future의 경우 비동기 블로킹 방식이 되기 때문에 성능이 좋지 않아 잘 사용하지 않습니다.  

### ListenableFuture
```java
@Slf4j
@Service
@RequiredArgsConstructor
public class CallService {

    private final UserService userService;

    public void listenableFutureCall() {
        ListenableFuture<String> future = userService.returnListenableFuture();
        try {
            future.addCallback(s -> log.info(s),ex -> log.error(ex.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.info("origin listenableFuture call");
    }
}
```
```java
@Slf4j
@Service
public class UserService {
    @Async
    public ListenableFuture<String> returnListenableFuture(){
        try {
            Thread.sleep(1000);
            log.info("{}","async return ListenableFuture");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return new AsyncResult<>("async return ListenableFuture");
    }
}
```
AsyncResult는 Future의 구현체이면서 동시에 ListenableFuture의 구현체이기에 AsyncResult로 리턴할 수 있습니다.  
listenableFutureCall 메서드에서 비동기 메서드 returnListenableFuture를 호출하여 addCallback 메서드로 결과값을 받습니다. Future.get을 사용했을 때는 메서드가 처리될 때까지 블로킹 현상이 발생했지만, 콜백 메서드를 사용한다면 결과를 얻을 때까지 기다릴 필요가 없습니다.  

1. 비동기 메서드 호출
2. 비동기 콜백 메서드 실행
3. 다른 작업 수행(논블로킹)

즉, addCallback 메서드를 통해 나중에 결과값이 나오면 처리하도록 하고 바로 다른 작업을 처리하러 갈 수 있는 __논블로킹__ 으로 동작합니다.  
참고로 ListenableFuture는 Future을 상속받기 때문에 Future의 기본적인 기능을 사용할 수 있습니다.  

### CompletableFuture 
```java
@Slf4j
@Service
@RequiredArgsConstructor
public class CallService {

    private final UserService userService;

    public void completableFutureCall() {
        CompletableFuture<String> future = userService.returnCompletableFuture();
        try {
            future.thenAccept(s -> log.info("{}",s));
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.info("origin completableFutureC call");
    }
}
```
```java
@Slf4j
@Service
public class UserService {

    @Async
    public CompletableFuture<String> returnCompletableFuture(){
        try {
            Thread.sleep(1000);
            log.info("{}","async return CompletableFuture");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return new AsyncResult<>("async return CompletableFuture").completable();
    }
}
```
AsyncResult에서 제공하는 completable 메서드를 사용하면 CompletableFuture로 리턴할 수 있습니다.  
completableFutureCall메서드에서 비동기 returnCompletableFuture메서드를 호출하고 결과값을 받습니다. thenAccept 메서드로 결과값이 나오면 수행할 일을 정의해주었습니다. 즉, thenAccept 메서드를 통해 나중에 결과값이 나오면 처리하도록 하고 바로 다른 작업을 처리하러 갈 수 있는 __논블로킹__ 으로 동작합니다.  
참고로 CompletableFuture도 Future를 상속하여 Future의 기본적인 기능을 사용할 수 있습니다.  
CompletableFuture의 사용법은 [여기](https://velog.io/@backtony/Java-ExecutorService%EC%99%80-ForkJoinPool#completablefuture)를 확인하세요.  



<Br><Br>

__참고__  
<a href="https://velog.io/@gillog/Spring-Async-Annotation%EB%B9%84%EB%8F%99%EA%B8%B0-%EB%A9%94%EC%86%8C%EB%93%9C-%EC%82%AC%EC%9A%A9%ED%95%98%EA%B8%B0" target="_blank"> [Spring] @Async Annotation(비동기 메소드 사용하기)</a>   
<a href="https://brunch.co.kr/@springboot/401" target="_blank"> Spring Boot @Async 어떻게 동작하는가?</a>   
<a href="https://sabarada.tistory.com/215" target="_blank"> [Spring] @Async에서 사용하는 ThreadPoolTaskExecutor 최적화하기</a>   
<a href="https://www.baeldung.com/spring-async" target="_blank"> How To Do @Async in Spring</a>   











