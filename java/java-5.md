# Java 디자인 패턴 - 프록시, 데코레이터 패턴

## 프록시란?
---
![그림1](https://github.com/backtony/blog-code/blob/master/java/img/5/43-1.PNG?raw=true)  
기존에 클라리언트와 서버간의 통신은 중간에 아무것도 없이 직접 호출로 이루어집니다.  
반면에 클라이언트가 요청한 결과를 서버에 직접 요청하는 것이 아니라 어떤 __대리자를 통해서 대신 간접적으로__ 서버에 요청할 수 있는데 이 대리자를 영어로 __프록시(proxy)__ 라고 합니다.  
<br><Br>

![그림2](https://github.com/backtony/blog-code/blob/master/java/img/5/43-2.PNG?raw=true)  
클라이언트가 서버 자체가 아니라 서버의 인터페이스(추상화)의존하면서 프록시를 사용할 수 있게 됩니다.  
즉, 클라이언트는 인터페이스의 어떤 구현체가 오든지 신경쓰지 않고 그냥 호출만 한다는 의미입니다.  
프록시는 서버와 같은 인터페이스를 구현하게 되면서 클라이언트가 의존하는 인터페이스에 서버 구현체 대신 들어가게 됩니다.  
<br><Br>

![그림3](https://github.com/backtony/blog-code/blob/master/java/img/5/43-3.PNG?raw=true)  
따라서, 클라이언트가 호출하게 되면 프록시가 호출되게 되고, 프록시는 어떠한 작업을 진행하고 결과에 따라 서버를 호출하게 됩니다.  
이때 프록시가 중간에서 할 수 있는 주요 기능은 크게 2가지로 분류할 수 있습니다.  
+ __접근 제어__
    - 권한에 따른 접근 차단
    - 캐싱
    - 지연로딩
+ __부가 기능 추가__
    - 원래 서버가 제공하는 기능에 더해서 부가 기능 제공
    - ex) 요청 값 또는 응답값을 중간에 변경하기
    - eX) 실행 시간을 측정해서 로그를 남기기

프록시 패턴과 데코레이터 패턴은 둘다 프록시를 사용하기 때문에 동작 방식 또한 같습니다.  
GOF 디자인 패턴에서는 둘의 __의도__ 에 따라서 패턴을 분류하고 있습니다.  
+ 프록시 패턴 : __접근 제어__ 가 목적
+ 데코레이터 패턴 : __새로운 기능 추가__ 가 목적

<br>

## 프록시 패턴
---
프록시 패턴은 __프록시를 사용하면서 접근 제어에 목적을 두는 패턴__ 입니다.  

### 예시
```java
public interface Subject {
    String operation();
}

@Slf4j
public class RealSubject implements Subject{
    @Override
    public String operation() {
        log.info("실제 객체 호출");
         sleep(1000);
        return "data";
    }

    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

public class ProxyPatternClient {

    private Subject subject;

    public ProxyPatternClient(Subject subject) {
        this.subject = subject;
    }

    public void execute(){
        subject.operation();
    }
}

public class ProxyPatternTest {
    @Test
    void noProxyTest(){
        RealSubject realSubject = new RealSubject();
        ProxyPatternClient proxyPatternClient = new ProxyPatternClient(realSubject);
        proxyPatternClient.execute(); // 직접 호출     
    }
}
```
RealSubject는 Subject를 구현한 구현체이고, ProxyPatternClient는 Subject 인터페이스에 의존하면서 execute함수에서 subject의 기능을 사용합니다.  
그럼 여기에 프록시 패턴을 도입해 보겠습니다.  
```java
@Slf4j
public class CacheProxy implements Subject{

    private Subject target; // 실제 호출할 객체
    private String cacheValue; // 캐시 데이터


    public CacheProxy(Subject target) {
        this.target = target;
    }

    @Override
    public String operation() {
        log.info("프록시 호출");
        if(cacheValue == null){
            cacheValue = target.operation();
        }
        return cacheValue;
    }
}
```
프록시 클래스는 생성자로 타겟이 되는 클래스를 주입받아서 갖고 있습니다.  
실제 클라이언트 단에서 호출이 들어오면 결과에 따라 실제 타겟 클래스를 호출해야하기 때문입니다.  
RealSubject 클래스의 operation함수를 보면 반환값이 data로 고정되어 있습니다.  
프록시 패턴을 사용하고 있다면 굳이 RealSubject의 operation 함수를 타지 않고 프록시에서 바로 값을 반환해줘도 됩니다.(캐싱)  
따라서, 첫 호출시에는 cacheValue가 비어있으니 실제 타겟 클래스의 operation 함수를 호출하게 되고 이 값을 저장해둔뒤, 다음 호출시에는 바로 값을 내려주게 됩니다.  
```java
public class ProxyPatternTest {
    @Test
    void cacheProxyTest() {
        RealSubject realSubject = new RealSubject();
        CacheProxy cacheProxy = new CacheProxy(realSubject);
        ProxyPatternClient proxyPatternClient = new ProxyPatternClient(cacheProxy);
        proxyPatternClient.execute(); // 프록시 첫 호출 -> 반환값이 프록시 캐시에 저장
        proxyPatternClient.execute(); // 캐시 데이터 반환
        proxyPatternClient.execute(); // 캐시 데이터 반환
    }
}
```
<br>

## 데코레이터 패턴
---
데코레이터 패턴은 __프록시를 사용해서 부가기능을 수행하는 패턴__ 입니다.  
동작 구조는 프록시 패턴과 똑같고 단지 __의도(목적)__ 만 다릅니다.  

### 예시
```java
public interface Component {
    String operation();
}

@Slf4j
public class RealComponent implements Component{
    @Override
    public String operation() {
        log.info("realComponent 실행");
        return "data";
    }
}

@Slf4j
public class DecoratorPatternClient {

    private Component component;

    public DecoratorPatternClient(Component component) {
        this.component = component;
    }

    public void execute(){
        String result = component.operation();
        log.info("result = {}",result);
    }
}

@Slf4j
public class DecoratorPatternTest {
    @Test
    void noDecorator() {
        RealComponent realComponent = new RealComponent();
        DecoratorPatternClient client = new DecoratorPatternClient(realComponent);
        client.execute(); // 직접 호출
    }
}
```
프록시 패턴의 예시와 변수망만 다르고 완전 일치합니다.  
RealComponent는 Component인터페이스의 구현체이고 DecoratorPatternClient는 Component인터페이스에 의존해서 execute에서 component의 함수를 호출합니다.  
<br>

이번에는 리턴값을 변형시키고, 동작시간까지 찍는 데코레이터 패턴을 구현해보겠습니다.  

> 클라이언트 요청 -> 시간 찍는 프록시 -> 값을 변형 시키는 프록시 -> 실제 타켓

```java
@Slf4j
public class TimeDecorator implements Component{

    private Component component;

    public TimeDecorator(Component component) {
        this.component = component;
    }

    @Override
    public String operation() {
        log.info("timeDecorator 실행");
        long startTime = System.currentTimeMillis();
        String result = component.operation();
        long endTime = System.currentTimeMillis();
        log.info("runningTime = {}",endTime-startTime);
        return result;
    }
}

@Slf4j
public class MessageDecorator implements Component{

    private  Component component;

    public MessageDecorator(Component component) {
        this.component = component;
    }

    @Override
    public String operation() {
        log.info("messageDecorator 실행");
        String result = component.operation();
        String decoResult = "*****" + result + "*****";
        return decoResult;
    }
}

@Slf4j
public class DecoratorPatternTest {
    @Test
    void decorator2() {
        RealComponent realComponent = new RealComponent();
        MessageDecorator messageDecorator = new MessageDecorator(realComponent);
        TimeDecorator timeDecorator = new TimeDecorator(messageDecorator);
        DecoratorPatternClient client = new DecoratorPatternClient(timeDecorator);
        client.execute(); // timeDecorator 호출
    }
}
```
Component의 구현체들을 순서에 맞게 잘 조립만 하면 됩니다.  
데코레이터 패턴의 경우, 위처럼 프록시와 프록시를 연결해서 사용할 수 있는데 코드상으로 보면 Component를 갖고있는 중복 코드가 존재합니다.  
그렇다면 생성자와 Component 필드를 추상 클래스로 아래와 같이 리팩토링 할 수 있습니다.  
```java
public abstract class AbstractDecorator {
    protected Component component;

    public AbstractDecorator(Component component) {
        this.component = component;
    }
}

@Slf4j
public class MessageDecorator extends AbstractDecorator implements Component{

    public MessageDecorator(Component component) {
        super(component);
    }

    @Override
    public String operation() {
        log.info("messageDecorator 실행");
        String result = component.operation();
        String decoResult = "*****" + result + "*****";
        return decoResult;
    }
}
```
<br>

## 클래스 기반 프록시
---
앞서 프록시, 데코레이터 패턴 모두 인터페이스를 사용했습니다.  
자바 언어에서 __다형성은 인터페이스나 클래스를 구분하지 않고 적용__ 됩니다.  
따라서 해당 타입과 그 타입의 하위 타입은 모두 다형성의 대상이 되기 때문에 인터페이스로 구현되어 있지 않은 경우, 클래스로 프록시를 구현할 수 있습니다.  
클래스로 프록시를 구현할 경우, __상속__ 을 사용하면 됩니다.
```java
@Slf4j
public class ConcreteLogic {
    public String operation(){
        log.info("concreteLogic 실행");
        return "Data";
    }
}

public class ConcreteClient {

    private ConcreteLogic concreteLogic;

    public ConcreteClient(ConcreteLogic concreteLogic) {
        this.concreteLogic = concreteLogic;
    }

    public void execute(){
        concreteLogic.operation();
    }
}

public class ConcreteProxyTest {
    @Test
    void noProxy() {
        ConcreteLogic concreteLogic = new ConcreteLogic();
        ConcreteClient concreteClient = new ConcreteClient(concreteLogic);
        concreteClient.execute();
    }
}
```
앞선 패턴 예시와 다르게 ConcreteClient는 추상화에 의존하지 않고 ConcreteLogic클래스에 의존하고 있습니다.  
클래스 기반 프록시로 실행시간을 찍는 기능을 추가해보겠습니다.  
```java
@Slf4j
public class TimeProxy extends ConcreteLogic{

    private ConcreteLogic target;

    public TimeProxy(ConcreteLogic target) {
        this.target = target;
    }

    @Override
    public String operation() {
        log.info("timeDecorator 실행");
        long startTime = System.currentTimeMillis();

        // 실제 타겟 메서드 호출
        String result = target.operation();

        long endTime = System.currentTimeMillis();
        log.info("runningTime = {}",endTime-startTime);
        return result;
    }
}

public class ConcreteProxyTest {
    @Test
    void proxy() {
        ConcreteLogic concreteLogic = new ConcreteLogic();
        TimeProxy timeProxy = new TimeProxy(concreteLogic);
        ConcreteClient concreteClient = new ConcreteClient(timeProxy);
        concreteClient.execute();
    }
}
```
TimeProxy는 ConcreteLogic를 상속받아서 만든 하위 클래스입니다.  
클래스 상속으로 프록시를 구현하 것입니다.  
실제 사용하는 곳에서는 ConcreteClient의 ConcreteLogic필드에는 ConcreteLogic의 자식인 TimeProxy가 들어오게 되는 것입니다.  
<br>

클래스 기반 프록시는 인터페이스가 없어도 프록시를 만들 수 있다는 장점이 있지만, 단점도 존재합니다.  
+ 상속을 통해 구현하므로 해당 클레스에만 적용할 수 있다.
    - 인터페이스 기반의 경우 인터페이스만 같으면 모든 곳에 적용 가능
+ 상속으로 인한 제약
    - 부모 클래스의 생성자를 호출해야 한다.
        - 부모 클래스의 생성자가 기본 생성자라면 자동으로 super() 이 호출되지만, 기본 생성자가 없다면 super(인자)로 채워줘야 한다.
        - 클래스에 final 키워드가 붙어있으면 구현이 불가능하다.
        - 매서드에 final 키워드가 붙으면 해당 메서드를 오버라이딩 할 수 없다.



<br>

## 정리
---
+ 프록시
    - 클라이언트와 서버 사이에서 중간 대리자 역할을 하는 존재
    - 접근 제어 제공
        - 권한에 따른 접근 차단
        - 캐싱
        - 지연로딩
    - 부가 기능 추가 제공    
        - 요청, 응답값 중간에 변경
        - 실행 시간 로그 남기기
+ 프록시 패턴
    - 프록시를 사용하여 __접근 제어__ 를 목적으로 하는 패턴    
+ 데코레이터 패턴
    - 프록시를 사용하여 __부가 기능 추가__ 를 목적으로 하는 패턴
+ 클래스 기반 프록시
    - 인터페이스가 없을 때 클래스 상속을 이용해서 만드는 프록시
    - 상속으로 인한 단점 존재
        - 부모 생성자 호출
        - final 키워드 붙어있다면 구현 불가능

<br>

## 한계
---
프록시, 데코레이터 패턴을 통해 기존 코드를 수정하지 않고 부가 기능을 적용할 수 있었습니다.  
하지만 __프록시를 사용하기 위해서 너무 많은 프록시 클래스를 만들어야 한다는 단점__ 이 있습니다.  
예를 들어, 하나의 인터페이스를 구현한 100개의 구현 클래스가 존재한다면, 프록시를 적용하기 위해서는 100개의 프록시 클래스를 만들어야 합니다.  
시간 찍는 기능을 추가하고자 했다면 시간 찍는 기능은 동일하고 메인 로직만 바뀌기 때문에 프록시 클래스간의 중복 코드가 존재합니다.  

<Br><Br>

__참고__  
<a href="https://www.inflearn.com/course/%EC%8A%A4%ED%94%84%EB%A7%81-%ED%95%B5%EC%8B%AC-%EC%9B%90%EB%A6%AC-%EA%B3%A0%EA%B8%89%ED%8E%B8#" target="_blank"> 스프링 핵심 원리 - 고급편</a>   




