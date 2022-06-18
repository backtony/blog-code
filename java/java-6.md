# Java - JDK 동적 프록시

## JDK 동적 프록시
---
동적 프록시 기술을 사용하면 개발자가 직접 프록시 클래스를 만들지 않아도 됩니다.  
동적 프록시는 __프록시를 동적으로 런타임에 개발자 대신 만들어 주고, 개발자는 동적 프록시에 원하는 실행 로직을 지정__ 할 수 있습니다.  
JDK 동적 프록시를 __인터페이스를 기반__ 으로 프록시를 동적으로 만들어주기 때문에 인터페이스가 필수입니다.  

## 예시
```java
public interface AInterface {
    String call();
}

@Slf4j
public class AImpl implements AInterface{
    @Override
    public String call() {
        log.info("A 호출");
        return "A";
    }
}

public interface BInterface {
    String call();
}

@Slf4j
public class BImpl implements BInterface{
    @Override
    public String call() {
        log.info("B 호출");
        return "B";
    }
}
```
간단한 call 추상메서드를 갖는 AInterface와 BInterface가 있고 그에 따른 구현체 클래스가 있습니다.  
만약 실행시간 로그 찍기를 목적(공통 처리)으로 프록시를 만들어야 한다면 프록시, 데코레이터 패턴을 사용한다고 했을 때, 각 인터페이스마다 프록시 클래스를 만들어야 합니다.  
즉, 2개의 프록시 클래스가 생성되어야 합니다.  
<br>

이 문제를 JDK 동적 프록시로 해결한다면 프록시가 수행할 동작을 정의하는 클래스 한 개만으로 해결할 수 있습니다.  
JDK 동적 프록시는 InvocationHandler 인터페이스를 제공합니다.  
```java
package java.lang.reflect;
public interface InvocationHandler {
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable;
}
```
+ proxy : 프록시 자신
+ method : 호출한 메서드
+ args : 메서드를 호출할 때 전달할 인자

<br>

InvocationHandler 인터페이스를 구현하여 프록시가 수행할 구현 클래스를 만들어줍니다.  
```java
@Slf4j
public class TimeInvocationHandler implements InvocationHandler {

    private final Object target; // 프록시가 호출할 대상

    public TimeInvocationHandler(Object target) {
        this.target = target;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        log.info("TimeProxy 실행");
        long startTime = System.currentTimeMillis();

        // 실제 타켓의 메서드 호출
        Object result = method.invoke(target, args);

        long endTime = System.currentTimeMillis();
        log.info("result Time = {}",endTime-startTime);

        return result;
    }
}
```
+ method.invoke(target, args)
    - 여기서 인자로 들어오는 method는 리플렉션의 method입니다.
    - 따라서 method가 있는 클래스(target)과 해당 method를 실행하는데 필요한 인자(args)를 넘겨주면 해당 클래스(target))의 method를 찾아서 args(인자)를 넣어서 실행합니다.

<Br>

```java
@Slf4j
public class JdkDynamicProxyTest {
    @Test
    void dynamicA() {
        AInterface target = new AImpl();
        TimeInvocationHandler handler = new TimeInvocationHandler(target); // 실제 대상 target 주입
        AInterface proxy = (AInterface)Proxy.newProxyInstance(AInterface.class.getClassLoader(),
                new Class[]{AInterface.class}, handler);
        proxy.call();
    }

    @Test
    void dynamicB() {
        BInterface target = new BImpl();
        TimeInvocationHandler handler = new TimeInvocationHandler(target); // 실제 대상 target 주입
        BInterface proxy = (BInterface)Proxy.newProxyInstance(BInterface.class.getClassLoader(),
                new Class[]{BInterface.class}, handler);
        proxy.call();
    }
}
```
+ Proxy.newProxyInstance
    - 동적 프록시를 생성하는 함수입니다.
    - 프록시를 만들고자 하는 인터페이스 클래스의 클래스 로더, 프록시를 만들고자 하는 인터페이스 클래스 정보를 배열로 입력, 프록시의 수행 동작을 정의한 InvocationHandler의 구현체를 순서로 인자를 넣습니다.

동적 프록시를 사용하면 InvocationHandler의 구현체 하나의 클래스만으로 여러 인터페이스에 프록시를 적용할 수 있습니다.  

<br>

위에서 적용한 프록시의 경우, 해당 인터페이스의 모든 메서드에 적용됩니다.  
적용이 필요 없는 메서드는 제외시키고 조금 더 실용적으로 Spring의 Repository 인터페이스에 적용되는 동적 프록시를 만들어보겠습니다.  
OrderRepository 인터페이스의 구현체 OrderRepositoryImpl는 @Repository 애노테이션을 사용하지 않아서 빈으로 등록되지 않는 상황으로 가정합니다.  
프록시의 동작을 정의할 InvocationHandler를 하나 만들고, Repository를 프록시로 빈등록 하겠습니다.
```java
@Slf4j
public class TimeInvocationHandler implements InvocationHandler {

    private final Object target;
    private final String[] patterns;

    public TimeInvocationHandler(Object target, String[] patterns) {
        this.target = target;
        this.patterns = patterns;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        String methodName = method.getName();
        
        // 해당 패턴에 매칭되지 않는 메서드는 시간을 찍지 않고 기존 타겟의 메서드 동작만 수행한다.
        if (!PatternMatchUtils.simpleMatch(patterns,methodName)){
            return method.invoke(target,args); 
        }        

        log.info("TimeProxy 실행");
        long startTime = System.currentTimeMillis();

        Object result = method.invoke(target, args);

        long endTime = System.currentTimeMillis();
        log.info("result Time = {}",endTime-startTime);

        return result;
    }
}

@Configuration
public class DynamicProxyFilterConfig {

    private static final String[] PATTERNS = {"request*","order*","save*"};

    @Bean
    public OrderRepository orderRepository(LogTrace logTrace){
        OrderRepository orderRepository = new OrderRepositoryImpl();

        OrderRepository proxy = (OrderRepository)Proxy.newProxyInstance(OrderRepository.class.getClassLoader()
                , new Class[]{OrderRepository.class}, new TimeInvocationHandler(orderRepository, PATTERNS));

        return proxy;
    }
}
```
+ TimeInvocationHandler 
    - 기존과 다르게 patterns이 추가되었고 PatternMatchUtils.simpleMatch를 사용해서 프록시의 추가적인 동작을 수행할 메서드만 필터링합니다.
    - PatternMatchUtils의 패턴 방식
        - xxx : 정확하게 일치
        - *xxx : xxx로 끝나면 일치
        - xxx* : xxx로 시작하면 일치
        - \*xxx* : xxx가 있으면 일치
+ DynamicProxyFilterConfig
    - orderRepositoryImpl에 @Repository 애노테이션이 안붙었다고 가정했으므로 빈으로 등록이 안되있습니다.
    - @Bean을 통해 수동으로 등록해주면서 프록시로 등록해주는 과정입니다.

실질적으로 client에서 호출하면 다음과 같이 동작하게 됩니다.  

> 요청 -> proxy -> TimeInvocationHandler -> OrderRepositoryImpl

이렇게 빈으로 등록하게 되면 실질적으로 OrderRepositoryImpl의 코드를 건드리지 않았기 때문에 OCP(개방폐쇄원칙)를 지킬 수 있고, 수많은 프록시 클래스를 생성하지 않아도 됩니다.  
<br>

__정리하면, JDK 동적 프록시는 기존에 프록시, 데코레이터 패턴이 수많은 프록시 클래스를 생성해야됬던 문제를 프록시가 할 일은 정의하는 InvocationHandler의 구현체를 만들어줌으로써 수많은 프록시 클래스를 생성하지 않도록 할 수 있습니다.__  
또한, 수많은 중복 코드를 하나의 클래스에서 관리하게 되므로 SRP(단일책임원칙)도 지키게 됩니다.(필터링 기능이 있다면 SRP가 아닙니다.)  
하지만 JDK 동적 프록시는 인터페이스 기반이기 때문에, 클래스 기반의 경우 CGLIB이 프록시 생성을 하게 됩니다.  
__따라서 기반이 다르기 때문에 중복되는 로직이더라도 둘로 나눠서 중복해서 관리해야하는 문제가 있습니다.__  



<Br><Br>

__참고__  
<a href="https://www.inflearn.com/course/%EC%8A%A4%ED%94%84%EB%A7%81-%ED%95%B5%EC%8B%AC-%EC%9B%90%EB%A6%AC-%EA%B3%A0%EA%B8%89%ED%8E%B8#" target="_blank"> 스프링 핵심 원리 - 고급편</a>   


