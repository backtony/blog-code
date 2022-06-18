# Java 디자인 패턴 - 템플릿 메서드, 전략, 템플릿 콜백 패턴

## 템플릿 메서드 패턴
---
GOF에서 템플릿 메서드 패턴의 정의는 다음과 같습니다.  

>템플릿 메서드 패턴은 __작업에서 알고리즘의 골격을 정의하고 일부 단계를 하위 클래스로 연기__ 합니다.  
>하위 클래스가 알고리즘 구조를 변경하지 않고도 알고리즘의 특정 단계를 재정의 할 수 있습니다.  

<br>

![그림1](https://github.com/backtony/blog-code/blob/master/java/img/4/42-1.PNG?raw=true)  

풀어서 설명하면, __부모 클래스에 알고리즘의 골격인 템플릿을 정의하고 일부 변경되는 로직은 자식 클래스에서 오버라이드로__ 정의하는 것입니다.  
이렇게 되면 자식 클래스가 알고리즘의 전체 구조를 변경하지 않고 특정 부분만 재정의할 수 있습니다.  
결국 __다형성과 상속을 이용해 해결하는 패턴__ 입니다.  

### 예시
```java
@Slf4j
public class TemplateMethodTest {

    @Test
    void simpleTest(){
        logic1();
        logic2();
    }

    private void logic1() {
        long startTime = System.currentTimeMillis();

        // 실제 메인 로직 실행
        System.out.println("hello world");
        // 실제 메인 로직 종료

        long endTime = System.currentTimeMillis();
        log.info("time ={}",endTime-startTime);
    }

    private String logic2() {
        long startTime = System.currentTimeMillis();

        // 실제 메인 로직 실행
        log.info("{}","hello backtony!");
        // 실제 메인 로직 종료

        long endTime = System.currentTimeMillis();
        log.info("time ={}",endTime-startTime);

        // 리턴
        return "ok";
    }
}
```
simpleTest 코드를 보면 logic1과 logic2를 실행하고 있습니다.  
각각의 logic 메서드를 보면 실질적으로 메인 로직이 되는 부분을 제외하고는 구조적 틀이 일치합니다.  
지금은 2개의 로직밖에 없지만 이렇게 시간을 찍어야하는 경우가 매우 많아질 경우, 중복코드가 엄청나게 증가합니다.  
이를 템플릿 메서드 패턴으로 해결해보겠습니다.  
<br>

```java
@Slf4j
public abstract class Template<T> {
    public T execute(){
        long startTime = System.currentTimeMillis();

        // 실제 메인 로직 실행
        T result = call();
        // 실제 메인 로직 종료

        long endTime = System.currentTimeMillis();
        log.info("time ={}",endTime-startTime);

        return result;
    }

    protected abstract T call();
}
```
먼저 공통되는 구조를 추상클래스로 만들어 줍니다.  
logic1과 logic2가 반환하는 값이 타입이 다르기 때문제 제네릭을 사용합니다.  
공통되는 시간 체크 로직을 만들어주고 공통되지 않는 로직은 call 추상메서드로 만들어 줍니다.  
전체 구조가 완성되었으니 이제 해당 클래스를 상속받아 자신만의 메인 로직을 call안에 담아주면 됩니다.  
<br>

```java
@Slf4j
public class TemplateMethodTest {

    @Test
    void templateMethodTest(){
        Template<Void> template1 = new Template<Void>() {
            @Override
            protected Void call() {
                System.out.println("hello world");
                return null;
            }
        };

        Template<String> template2 = new Template<String>() {
            @Override
            protected String call() {
                log.info("{}","hello backtony!");
                return "OK";
            }
        };

        template1.execute();
        template2.execute();
    }   
}
```
익명 내부 클래스를 이용해 Template 클래스의 자식 클래스를 정의하고 각각의 메인 로직을 call 안에 작성했습니다.  
logic1의 경우 반환값이 void였기 때문에 call의 리턴값으로 null을 보내주면 됩니다.  
익명 클래스 때문에 지저분한 부분이 있긴 하지만 전보다 중복코드가 많이 줄었습니다.  

### 한계
템플릿 메서드 패턴은 상속을 사용합니다. 따라서 __상속에서 오는 단점__ 들을 그대로 가지고 갑니다.  
특히 부모 클래스와 컴파일 시점에 __강하게 결합__ 되는 문제가 있는데 의존관계에 대한 문제입니다.  
현재 위에서 작성한 예시를 보면 실질적으로는 부모 클래스의 기능을 전혀 사용하지 않았습니다.  
부모 클래스를 수정하게 되면 자식 클래스는 영향을 받게 됩니다.  
즉, 부모에서 어떤 추상 메서드가 추가된다면 모든 자식 클래스는 해당 추상 메서드를 정의해야 합니다.  
또한, 상속 구조이기 때문에 별도의 자식 클래스나 익명 내부 클래스를 만들어야 하는 부분도 코드상 복잡하게 작용합니다.  
이런 문제들을 제거한 디자인 패턴이 __전략 패턴__ 입니다.  
<br>

## 전략 패턴
---
GOF에서 전략 패턴의 정의는 다음과 같습니다.  
> 알고리즘 제품군을 정의하고 각각을 캡슐화하여 상호 교환 가능하게 만든다.  
> 전략을 사용하면 알고리즘을 사용하는 클라이언트와 독립적으로 알고리즘을 변경할 수 있다.

![그림2](https://github.com/backtony/blog-code/blob/master/java/img/4/42-2.PNG?raw=true)  
앞선 템플릿 메서드 패턴은 부모 클래스에 변하지 않는 템플릿을 두고 변하는 부분은 자식 클래스에 두어서 상속을 통해 해결했습니다.  
__전략 패턴에서는 변하지 않는 부분을 Context라는 곳에 두고 변하는 부분을 Strategy라는 인터페이스를 만들고 해당 인터페이스를 구현하도록 하여 해결하는 방식입니다.__  
상속이 아니라 __위임__ 으로 해결하는 것입니다.  

### 예시
```java
@Slf4j
public class TemplateMethodTest {

    @Test
    void simpleTest(){
        logic1();
        logic2();
    }

    private void logic1() {
        long startTime = System.currentTimeMillis();

        // 실제 메인 로직 실행
        System.out.println("hello world");
        // 실제 메인 로직 종료

        long endTime = System.currentTimeMillis();
        log.info("time ={}",endTime-startTime);
    }

    private String logic2() {
        long startTime = System.currentTimeMillis();

        // 실제 메인 로직 실행
        log.info("{}","hello backtony!");
        // 실제 메인 로직 종료

        long endTime = System.currentTimeMillis();
        log.info("time ={}",endTime-startTime);

        // 리턴
        return "ok";
    }
}
```
앞선 템플릿 예시와 똑같은 예시를 전략 패턴으로 해결해보겠습니다.  

__Context__  
```java
@Slf4j
public class Context {

    private final Strategy strategy;

    public Context(Strategy strategy) {
        this.strategy = strategy;
    }

    public <T> T execute(){
        long startTime = System.currentTimeMillis();

        // 실제 메인 로직
        T result = (T) strategy.call();

        long endTime = System.currentTimeMillis();
        log.info("time ={}",endTime-startTime);
        return result;
    }
}
```

<br>

__Strategy 인터페이스와 구현체__  
```java
public interface Strategy<T> {
    T call();
}

public class StrategyLogic1 implements Strategy{
    @Override
    public Void call() {
        System.out.println("hello world");
        return null;
    }
}

@Slf4j
public class StrategyLogic2 implements Strategy{
    @Override
    public String call() {
        log.info("{}","hello backtony!");
        return "OK";
    }
}
```
<Br>

__실제 사용__  
```java
class StrategyLogicTest {

    @Test
    void simpleTest(){
        Context context1 = new Context(new StrategyLogic1());
        Context context2 = new Context(new StrategyLogic2());
        context1.execute();
        context2.execute();

        // 인터페이스 함수가 1개이므로 람다도 가능
        Context context4 = new Context(()->{
            System.out.println("happy birthday");
            return null;
        });
        context4.execute();
    }
}
```
앞선 템플릿 패턴에서는 상속으로 인해 강한 결합, 익명 클래스 코드 가독성 저하의 문제가 있었습니다.  
__전략 패턴은 상속이 아닌 위임으로 인터페이스를 사용함으로 인해 람다식을 통해 가독성 좋게 구현이 가능하며, Context가 인터페이스에 의존하고 있기 때문에 인터페이스의 구현체를 변경하거나 새롭게 만들어도 Context 코드에는 영향을 주지 않습니다.__  
<br>

지금까지 진행한 전략 패턴의 경우, Context의 내부 필드로 Strategy를 두고 사용하기 때문에 Context와 Strategy를 실행 전에 원하는 모양으로 조립해두고 Context를 실행하는 선 조립 후 실행 방식입니다.  
Context와 Strategy를 한번 조립하고 나면 이후로 Context를 실행하기만 된다는 의미입니다. 이는 템플릿 패턴 메서드도 마찬가지입니다.  
스프링 애플리케이션 로딩 시점에 의존 관계 주입을 통해 필요한 의존관계를 모두 맺어두고 난 다음에 실제 요청을 처리하는 것과 같은 원리입니다.  
이 방식의 단점은 Context와 Strategy를 조립한 이후에는 전략을 변경하기 번거롭다는 점입니다.  
Setter 메서드를 통해 변경이 가능하긴 하지만 싱글톤일 경우 동시성 이슈로 인해 고려해야할 점이 많습니다.  
Context에 대해서 Strategy를 변경하면서 사용하고 싶은 경우 전략 패턴에서는 간단하게 필드로 Strategy를 보관하지 않고, 함수 인자로 사용하여 해결할 수 있습니다.  
```java
@Slf4j
public class Context2 {

    public void execute(Strategy strategy){
        long startTime = System.currentTimeMillis();

        // 실제 메인 로직
        strategy.call();

        long endTime = System.currentTimeMillis();
        log.info("time ={}",endTime-startTime);
    }
}

class StrategyLogicTest {

    @Test
    void simpleTest(){
        Context2 context = new Context2();
        context.execute(new StrategyLogic1());
        context.execute(() -> System.out.println("람다식"));
    }
}
```
이런 식으로 사용하면 매번 인자로 전략을 넣어줘야하는 단점이 있지만, 하나의 Context를 가지고 여러 Strategy를 유연하게 사용할 수 있습니다.  
<br>

정리하자면, 전략 패턴은 __인터페이스의 사용__ 으로 템플릿 패턴에서 상속으로 인한 문제를 해결했으며, __생성자로 전략을 받아 지정__ 해두고 사용하는 방식과 __함수의 인자로 전략을 받아 유연하게 사용__ 하는 방식이 있습니다.


<br>

## 템플릿 콜백 패턴
---
템플릿 콜백 패턴은 전략 패턴의 실행할 때마다 전략을 바꾸는 방식과 동일합니다.  
전략 패턴의 Context가 템플릿 역할을하고, Strategy 부분이 콜백으로 넘어온다고 보면 됩니다.  
다른 함수의 인자로 실행 가능한 코드를 넘겨주는 것을 콜백(callback)이라고 합니다.  
쉽게 말해서 callback은 실행 가능한 코드가 함수의 인자로 호출(call)되는데 코드를 넘겨준 곳의 뒤(인자를 받은 함수)에서 실행된다는 뜻입니다.  
스프링에서는 JdbcTemplate, RestTemplate 등의 XXXTemplate가 템플릿 콜백 패턴으로 만들어져 있다고 보면 됩니다.  
![그림3](https://github.com/backtony/blog-code/blob/master/java/img/4/42-3.PNG?raw=true)  
Template의 함수에 실행 가능한 코드 callback을 전달하고, Template안에서는 전달받은 실행 가능한 코드를 즉시 혹은 후에 실행하는 형태 입니다.  
<br><Br>

전략 패턴과 매우 유사하므로 간단하게 만들어보겠습니다.
```java
@Slf4j
public class Template {

    public void execute(Callback callback){
        long startTime = System.currentTimeMillis();

        // 실제 메인 로직
        callback.call();

        long endTime = System.currentTimeMillis();
        log.info("time ={}",endTime-startTime);

    }
}

public interface Callback {
    void call();
}

public class CallbackTest {

    @Test
    void simple(){
        Template template = new Template();
        template.execute(() -> System.out.println("call back"));
        template.execute(() -> System.out.println("hello world"));
    }
}
```
하나의 Template 객체를 가지고 유연하게 사용할 수 있습니다.  
해당 람다식은 인터페이스의 익명 구현체로 실행 가능한 코드인데 template에 넘겨져서 template에서 실행되므로 템플릿 콜백 패턴이라고 합니다.  


<Br>

## 정리
---
+ 템플릿 메서드, 전략, 템플릿 콜백 패턴의 공통적인 목적
    - __변하는 코드와 변하지 않는 코드를 분리하자.__
+ 템플릿 메서드 패턴
    - __골격이 정의된 부모 클래스를 상속받아 일부분의 로직은 자식 클래스에서 구현하는 방식__
    - 상속과 다형성을 이용
    - __상속으로 인한 강한 결합이 단점__ -> 부모 변경시 자식 전부 수정
    - 상속으로 인한 자식 클래스 생성
+ 전략 패턴 방식
    - 위임을 이용
    - 골격이 정의된 Context 클래스를 두고, 일부분의 로직을 __인터페이스로 구현하는 방식__
    - __Context가 인터페이스(추상화)에 의존하기 때문에 구현체가 변해도 Context에 영향을 주지 않음__
    - 단일 추상 메서드가 있는 인터페이스의 경우 __람다식__ 으로 간편하게 구현 가능
    - 구현 종류
        - 전략을 필드고 갖고 생성자에서 주입해서 고정으로 사용하는 방식
        - 함수의 인자로 전략을 받아서 유연하게 전략을 바꿔가면서 사용하는 방식 = 템플릿 콜백 패턴과 동일
+ 템플릿 콜백 패턴    
    - 전략 패턴의 실행할 때마다 전략을 바꾸는 방식과 동일
    - 골격의 정의된 Template 클래스를 두고, 일부분의 로직을 함수의 인자로 들어온 코드로 구현하는 방식
    - 다른 코드의 인수로서 실행 가능한 코드를 넘겨주는 것을 콜백(callback)이라고 함
    - 쉽게 말하면, 함수의 인자로 실행 가능한 코드를 넘기면(Call) 코드를 넘겨준 곳의 뒤(Back)에서 실행된다는 의미
    - 스프링에서는 JdbcTemplate, RestTemplate 등의 XXXTemplate가 템플릿 콜백 패턴으로 만들어져 있다.


## 한계
---
앞선 3가지 패턴 모두 변하지 않는 코드와 변하는 코드를 분리하기 위한 목적으로 잘 분리할 수는 있었지만, 이를 적용하기 위해서는 __실제 로직 코드를 수정__ 해야 합니다.  
실제 로직에서 template 같이 추가적인 코드가 작성이 필요하기 때문입니다.  
중복 코드 또한 많이 해결했지만, 전체에 대한 실제 로직 수정이 필요하기 때문에 수백개의 코드를 조금 덜 힘들게 수정하게 되었을 뿐입니다.  



<Br><Br>

__참고__  
<a href="https://www.inflearn.com/course/%EC%8A%A4%ED%94%84%EB%A7%81-%ED%95%B5%EC%8B%AC-%EC%9B%90%EB%A6%AC-%EA%B3%A0%EA%B8%89%ED%8E%B8#" target="_blank"> 스프링 핵심 원리 - 고급편</a>   





