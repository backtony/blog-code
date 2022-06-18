# Spring - AOP 총정리


## 1. AOP란?
---
Spring의 핵심 개념 중 하나인 DI가 애플리케이션 모듈들 간의 결합도를 낮춘다면, __AOP(Aspect-Oriented Programming)는 핵심 로직과 부가 기능을 분리하여 애플리케이션 전체에 걸쳐 사용되는 부가 기능을 모듈화하여 재사용할 수 있도록 지원하는 것__ 입니다.  
Aspect-Oriented Programming이란 단어를 번역하면 __관점(관심) 지향 프로그래밍__ 이 됩니다. 프로젝트 구조를 바라보는 관점을 바꿔보자는 의미입니다.
<Br>

![그림1](https://github.com/backtony/blog-code/blob/master/spring/img/aop/2/8-1.PNG?raw=true)  

각각의 Service의 핵심기능에서 바라보았을 때 User과 Order는 공통된 요소가 없습니다. 하지만 부가기능 관점에서 바라보면 이야기가 달라집니다.
<br><Br>

![그림2](https://github.com/backtony/blog-code/blob/master/spring/img/aop/2/8-2.PNG?raw=true)  

부가기능 관점에서 바라보면 각각의 Service의 getXX 메서드를 호출하는 전후에 before과 after라는 메서드가 공통되는 것을 확인할 수 있습니다.  
기존에 OOP에서 바라보던 관점을 다르게 하여 부가기능적인 측면에서 보았을때 공통된 요소를 추출하자는 것입니다. 이때 가로(횡단) 영역의 공통된 부분을 잘라냈다고 하여, AOP를 __크로스 컷팅(Cross-Cutting)__ 이라고 부르기도 합니다.
+ OOP : 비즈니스 로직의 모듈화
    - 모듈화의 핵심 단위는 비즈니스 로직
+ AOP : 인프라 혹은 부가기능의 모듈화
    - 대표적인 예 : 모니터링 및 로깅, 동기화, 오류 검사 및 처리, 성능 최적화(캐싱) 등
    - 각각의 모듈들의 주 목적 외에 필요한 부가적인 기능들

간단하게 한줄로 AOP를 정리해보자면, __AOP는 공통된 기능을 재사용하는 기법__ 입니다.  
OOP에선 공통된 기능을 재사용하는 방법으로 상속이나 위임을 사용합니다.  
하지만 전체 애플리케이션에서 여기저기 사용되는 부가기능들은 상속이나 위임으로 처리하기에는 깔끔한 모듈화가 어렵습니다.  
그래서 등장한 것이 AOP입니다.  
AOP의 장점은 다음과 같습니다.
+ 애플리케이션 전체에 흩어진 공통 기능이 하나의 장소에서 관리되어 유지보수가 좋다.
+ 핵심 로직과 부가 기능의 명확한 분리로, 핵심 로직은 자신의 목적 외에 사항들에는 신경쓰지 않는다.

<Br>

## 2. AOP 적용 방식
---
AOP의 적용 방식은 크게 3가지 방법이 있습니다.
+ 컴파일 시점
    - .java 파일을 컴파일러를 통해 .class를 만드는 시점에 부가 기능 로직을 추가하는 방식
    - 모든 지점에 적용 가능
    - AspectJ가 제공하는 특별한 컴파일러를 사용해야 하기 때문에 특별할 컴파일러가 필요한 점과 복잡하다는 단점
+ 클래스 로딩 시점
    - .class 파일을 JVM 내부의 클래스 로더에 보관하기 전에 조작하여 부가 기능 로직 추가하는 방식
    - 모든 지점에 적용 가능
    - 특별한 옵션과 클래스 로더 조작기를 지정해야하므로 운영하기 어려움
+ __런타임 시점__
    - __스프링이 사용하는 방식__
    - 컴파일이 끝나고 클래스 로더에 이미 다 올라가 자바가 실행된 다음에 동작하는 런타임 방식
    - 실제 대상 코드는 그대로 유지되고 프록시를 통해 부가 기능이 적용
    - __프록시는 메서드 오버라이딩 개념으로 동작하기 때문에 메서드에만 적용 가능__ -> __스프링 빈에만 AOP를 적용 가능__
    - 특별한 컴파일러나, 복잡한 옵션, 클래스 로더 조작기를 사용하지 않아도 스프링만 있으면 AOP를 적용할 수 있기 때문에 스프링 AOP는 런타임 방식을 사용


<Br>

__cf) 참고__  
스프링 AOP는 AspectJ 문법을 차용하고 프록시 방식의 AOP를 제공합니다.  
스프리에서는 AspectJ가 제공하는 애노테이션이나 관련 인터페이스만 사용하고 실제로 AspectJ가 제공하는 컴파일, 로드타임 위버등은 사용하지 않습니다.  
따라서 스프링 AOP는 AspectJ를 직접 사용하는 것은 아닙니다.

<br>

## 3. AOP 용어
---
![그림1](https://github.com/backtony/blog-code/blob/master/spring/img/aop/2/2-1.PNG?raw=true)  
+ Join point
    - __추상적인 개념__ 으로 advice가 적용될 수 있는 모든 위치를 말합니다.
    - ex) 메서드 실행 시점, 생성자 호출 시점, 필드 값 접근 시점 등등..
    - __스프링 AOP는 프록시 방식을 사용하므로 조인 포인트는 항상 메서드 실행 지점__
+ Pointcut
    - 조인 포인트 중에서 advice가 적용될 위치를 선별하는 기능
    - __스프링 AOP는 프록시 기반이기 때문에 조인 포인트가 메서드 실행 시점 뿐이 없고 포인트컷도 메서드 실행 시점만 가능__
+ Target
    - advice의 대상이 되는 객체
    - Pointcut으로 결정
+ advice
    - 실질적인 부가 기능 로직을 정의하는 곳
    - 특정 조인 포인트에서 Aspect에 의해 취해지는 조치
+ Aspect
    - advice + pointcut을 모듈화 한 것
    - @Aspect와 같은 의미
+ Advisor
    - 스프링 AOP에서만 사용되는 용어로 advice + pointcut 한 쌍
+ Weaving
    - pointcut으로 결장한 타겟의 join point에 advice를 적용하는 것
+ AOP 프록시
    - AOP 기능을 구현하기 위해 만든 프록시 객체
    - 스프링에서 AOP 프록시는 JDK 동적 프록시 또는 CGLIB 프록시
    - __스프링 AOP의 기본값은 CGLIB 프록시__

<br>

## 4. Aspect
---
이 내용은 [Spring - 프록시 팩토리와 빈 후처리기](https://velog.io/@backtony/Spring-%ED%94%84%EB%A1%9D%EC%8B%9C-%ED%8C%A9%ED%86%A0%EB%A6%AC%EC%99%80-%EB%B9%88-%ED%9B%84%EC%B2%98%EB%A6%AC%EA%B8%B0)에서 이어지는 내용입니다.  
스프링은 빈을 등록할 때, 빈 후처리기에서 모든 Advisor 빈을 조회한 뒤 Pointcut으로 매칭해보면서 프록시 적용 대상인지 판단하고 대상이라면 프록시를 빈으로 등록한다고 했습니다.  
따라서 전 포스팅에서는 advice와 pointcut를 구현해서 Advisor를 만들어 빈으로 등록했지만 그 과정이 다소 불편했습니다.  
__@Aspect 애노테이션__ 을 사용한다면 Advisor를 더욱 쉽게 구현할 수 있습니다.  
스프링 AOP를 사용하기 위해서는 다음과 같은 의존성을 추가해야 합니다.
```
implementation 'org.springframework.boot:spring-boot-starter-aop'
```
해당 의존성을 추가하게 되면 자동 프록시 생성기(AnnotationAwareAspectJAutoProxyCreator)를 사용할 수 있게 되고, 이것이 Advisor 기반으로 프록시를 생성하는 역할을 합니다.  
이와 더불어, 자동 프록시 생성기는 __@Aspect를 보고 Advisor로 변환해서 저장하는 작업__ 을 수행합니다.  

![그림2](https://github.com/backtony/blog-code/blob/master/spring/img/aop/2/2-2.PNG?raw=true)  
자동 프록시 생성기에 의해 @Asepct에서 Advisor로 변환된 Advisor는 @Aspect Advisor 빌더 내부에 저장됩니다.  

<br><br>

### 동작 과정
그럼 자동 프록시 생성기에 의해 성성된 Advisor는 기존 로직에서 어느 시점에 끼어드는지 보겠습니다.  
![그림3](https://github.com/backtony/blog-code/blob/master/spring/img/aop/2/2-3.PNG?raw=true)  

1. 스프링 빈 대상이 되는 객체를 생성한다.(@Bean, 콤포넌트 스캔 대상)
2. 생성된 객체를 빈 저장소에 등록하기 직전에 빈 후처리기에 전달한다.
3. 모든 Advisor 빈을 조회합니다.
4. __@Aspect Advisor 빌더 내부에 저장된 모든 Advisor를 조회합니다.__
4. 3,4에서 조회한 Advisor에 포함되어 있는 포인트컷을 통해 클래스와 메서드 정보를 매칭하면서 프록시를 적용할 대상인지 아닌지 판단합니다.
5. 여러 Advisor의 하나라도 포인트컷의 조건을 만족한다면 프록시를 생성하고 프록시를 빈 저장소로 반환합니다.
5. 만약 프록시 생성 대상이 아니라면 들어온 빈 그대로 빈 저장소로 반환합니다.
6. 빈 저장소는 객체를 받아서 빈으로 등록합니다.

Advisor 빈을 조회하고 이후에 @Aspect Advicor 빌더 내부에 저장된 모든 Advisor를 조회하는 로직이 추가된 것을 확인할 수 있습니다.  

### 주의사항
@Aspect는 Advisor를 쉽게 만들 수 있도록 도와주는 역할을 하는 것이지 컴포넌트 스캔이 되는 것은 아닙니다.  
__따라서 반드시 스프링 빈으로 등록해줘야 합니다.__  
다음 세가지 방식 중 아무것이나 선택해서 등록하면 됩니다.
+ @Bean 으로 수동 등록
+ @Component 로 컴포넌트 스캔 사용해서 자동 등록
+ @Import 를 사용해서 파일 추가


<br>

## Advice
---
Advice는 실질적으로 프록시에서 수행하게 되는 로직을 정의하게 되는 곳입니다.  
스프링에서는 Advice에 관련된 5가지 애노테이션을 제공합니다.  
애노테이션은 메서드에 붙이게 되는데 해당 메서드는 advice의 로직을 정의하게 되고, 애노테이션의 종류에 따라 __포인트컷에 지정된 대상 메서드에서 Advice가 실행되는 시점__ 을 정할 수 있습니다.  
또한 __속성값으로 Pointcut을 지정__ 할 수 있습니다.  
+ @Around
    - __뒤에 나올 4가지 애노테이션을 모두 포함하는 애노테이션__
    - 메서드 호출 전후 작업 명시 가능
    - 조인 포인트 실행 여부 선택 가능
    - __반환값 자체를 조작 가능__
    - __예외 자체를 조작 가능__
    - 조인 포인트를 여러번 실행 가능(재시도)
+ @Before
    - 조인 포인트 실행 이전에 실행(실제 target 메서드 수행 전에 실행)
    - 입력값 자체는 조작 불가능
    - 입력값의 내부에 setter같은 수정자가 있다면 내부값은 수정 가능
+ @AfterReturning
    - 조인 포인트가 정상 완료 후 실행(실제 target 메서드 수행 완료 후 실행)
    - 반환값 자체는 조작 불가능
    - 반환값 내부에 setter같은 수정자가 있따면 내부값은 수정 가능
+ @AfterThrowing
    - 메서드가 예외를 던지는 경우 실행(실제 target 메서드가 예외를 던지는 경우 실행)
    - 예외 조작 불가능
+ @After
    - 조인 포인트의 정상, 예외 동작과 무관하계 실행(실제 target 메서드가 정상적 수행을 하든 예외를 던지든 수행 이후에 무조건 실행)

### Advice 종류
#### @Around
```java
@Slf4j
@Aspect
public class AspectV6Advice {

    @Around("execution(* com.example.mvc.order..*(..))")
    public Object doTransaction(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            // @Before 수행
            log.info("[트랜잭션 시작] {}", joinPoint.getSignature());
            // @Before 종료

            // Target 메서드 호출
            Object result = joinPoint.proceed();
            // Target 메서드 종료

            // @AfterReturning 수행
            log.info("[트랜잭션 커밋] {}", joinPoint.getSignature());
            // @AfterReturning 종료

            // 값 반환
            return result;
        } catch (Exception e) {
            // @AfterThrowing 수행
            log.info("[트랜잭션 롤백] {}", joinPoint.getSignature());
            throw e;
            // @AfterThrowing 종료
        } finally {
            //@ After 수행
            log.info("[리소스 릴리즈] {}", joinPoint.getSignature());
            //@ After 종료
        }
    }
}
```
@Around의 속성은 Pointcut을 명시하는 곳으로 다음에 자세히 설명하겠습니다.  
@Around는 나머지 4개의 애노테이션을 모두 포함하는 애노테이션이라고 설명했습니다.  
주석을 보면 각 애노테이션이 어느 시점에 적용되는지 확인할 수 있습니다.  
__@Around 애노테이션을 제외한 나머지 4개의 애노테이션은 @Around 애노테이션의 기능을 Target 메서드 실행 전, 후, 예외, 후에 무조건(finally) 로 분리한 것입니다.__  
모든 Advice 애노테이션은 첫번째 파라미터로 org.aspectj.lang.JoinPoint를 사용할 수 있는데 @Around만 예외적으로 JoinPoint 의 하위타입인 ProceedingJoinPoint를 사용합니다.  
JoinPoint의 인터페이스의 주요 기능은 다음과 같습니다.
+ getArgs() : 메서드 인수 반환
+ getThis() : 프록시 객체 반환
+ getTarget() : 대상 객체 반환
+ getSignature() : 조언되는 메서드에 대한 설명 반환
+ toString() : 조언되는 방법에 대한 유용한 설명 인쇄

ProceedingJoinPoint의 주요기능에는 위의 주요 기능에서 다음 advice나 타겟을 호출하는 proceed() 메서드가 추가됩니다.  
이는 @Around를 나머지 4개의 애노테이션으로 분리한 이유와 연관이 있습니다.  
위에 코드를 보면 joinPoint.proceed() 메서드로 Target 메서드를 호출하는 코드가 있습니다.  
아래서 보겠지만, __@Around를 제외한 나머지 4개의 애노테이션은 보통 인자로 JoinPoint를 받아서 사용하고, proceed를 호출하지 않습니다.__  
즉, @Around의 경우 ProceedingJoinPoint를 인자로 받아 타겟 메서드를 실행하는 proceed 코드를 반드시 적어야 target 메서드를 호출하지만 나머지 4개의 애노테이션의 경우, 타겟 메서드를 호출하는 proceed를 명시하지 않아도 알아서 호출됩니다.  
따라서 개발자가 실수로 proceed 코드를 작성하지 않아 발생할 실수를 방지할 수 있습니다.  
또한, 이렇게 애노테이션을 분리함으로 인해 __의도를 명확하게 판단할 수 있습니다.__  
__하나 더 큰 차이가 있다면 @Around는 입력, 반환값 자체를 다른 객체로 조작이 가능하지만, 나머지 4개의 애노테이션의 경우 입력, 반환값 객체 자체를 다른 객체로 조작할 수 없습니다.__  


#### @Before
@Before은 조인 포인트 실행 전(타겟 메서드 실행 전)에 작업을 수행합니다.
```java
@Before("execution(* com.example.mvc.order..*(..))")
public void doBefore(JoinPoint joinPoint) {
    log.info("[before] {}", joinPoint.getSignature());
}
```
@Around와 달리 proceed 코드가 없이 정의한 로직이 수행된 후 자동으로 target 메서드를 호출합니다.  

#### @AfterReturing
@AfterReturing은 조인 포인트가 정상적으로 실행되고 값을 반환할 때 실행됩니다.(타겟 메서드가 예외가 아닌 정상값을 반환할 때)
```java
@AfterReturning(value = "execution(* com.example.mvc.order..*(..))", returning = "result")
public void doReturn(JoinPoint joinPoint, Object result) {
    log.info("[return] {} return={}", joinPoint.getSignature(), result);
}
```
다른 애노테이션과 다르게 속성값으로 returning이 추가되었습니다.  
이 부분에는 Target 메서드가 반환하는 변수명을 적어주고, advice 메서드의 인자로 변수명을 일치시켜준다면 해당 값을 가져와서 사용할 수 있습니다.  
여기서 주의할점은 returning 값을 받는 인자의 타입이 해당 리턴 값의 부모타입 혹은 같은 타입이어야만 해당 Advice가 동작합니다.  
__타입이 부모 혹은 동일 타입이 아니라면 해당 Advice 자체가 동작하지 않으니 주의해야 합니다.__  

#### @AfterThrowing
@AfterThrowing는 타겟 메서드 실행이 예외를 던져서 종료될 때 실행됩니다.  
```java
 @AfterThrowing(value = "execution(* com.example.mvc.order..*(..))", throwing = "ex")
public void doThrowing(JoinPoint joinPoint, Exception ex) {
    log.info("[ex] {} message={}", joinPoint.getSignature(), ex.getMessage());
}
```
@AfterReturning과 비슷하게 throwing 속성이 추가되고 advice 메서드 인자에 변수명을 일치시키면 받아서 사용할 수 있습니다.  

#### @Around
@Around는 타겟 메서드의 실행이 종료되면 무조건 실행됩니다.(try catch의 finally문과 같습니다.)  

### 애노테이션 동작 순서
![그림4](https://github.com/backtony/blog-code/blob/master/spring/img/aop/2/2-4.PNG?raw=true)  
동일한 @Aspect 안에서는 위와 같은 우선순위로 동작합니다.  
즉, 동일한 @Aspect 안에서 여러 개의 Advice가 존재하는데 타겟 메서드가 여러 Advice의 대상이 될 경우 다음과 같이 동작합니다.  

> Around -> Before -> AfterThrowing -> AfterReturning -> After -> Around

### Advice 순서 지정하기
애노테이션의 동작 순서는 정의되어 있더라도, 같은 애노테이션에 대한 동작 순서는 보장되지 않습니다.  
```java
@Slf4j
@Aspect
public class AspectV1 {
    @Around("execution(* com.example.mvc.order..*(..))")
    public Object doLog(ProceedingJoinPoint joinPoint)throws Throwable{
        // 생략
    }

    @Around("execution(* com.example.mvc.service..*(..))")
    public Object anotherLog(ProceedingJoinPoint joinPoint)throws Throwable{
        // 생략
    }
}
```
위와 같이 같은 수준의 애노테이션이 붙어있는 경우 동작 순위를 보장하지 않습니다.  
순서를 보장하고 싶다면 @Aspect 적용 단위로 __@Order 애노테이션__ 을 지정해야 합니다.  
__즉, Advice 단위가 아니라 @Aspect 클래스 단위로만 지정이 가능합니다.__  
따라서 하나의 Aspect 안에 여러 Advice가 존재한다면 순서를 보장할 수 없어 별도의 클래스로 분리해야 합니다.  
```java
@Slf4j
public class AspectV5Order {

    @Aspect
    @Order(1)
    public static class TxAspect {
        @Around("hello.aop.order.aop.Pointcuts.orderAndService()")
        public Object doTx(ProceedingJoinPoint joinPoint) throws Throwable {
            // 생략
        }
    }

    @Aspect
    @Order(2)
    public static class LogAspect {
        @Around("hello.aop.order.aop.Pointcuts.allOrder()")
        public Object doLog(ProceedingJoinPoint joinPoint) throws Throwable {
            // 생략
        }
    }    
}
```
위에서는 내부 static 클래스로 분리했지만, 따로 클래스를 분리해도 됩니다.  
결과적으로 아래 그림처럼 동작하게 됩니다.  
![그림5](https://github.com/backtony/blog-code/blob/master/spring/img/aop/2/2-5.PNG?raw=true)  



### 포인트컷 분리
```java
@Slf4j
@Aspect
public class AspectV6Advice {

    @Around("execution(* com.example.mvc.order..*(..))")
    public Object doTransaction(ProceedingJoinPoint joinPoint) throws Throwable {
       // 생략
}
```
앞서 Advice애노테이션의 속성으로 Pointcut을 명시해서 사용했습니다.  
이렇게 애노테이션 마다 각각의 Pointcut을 명시할 수 있지만 보통 분리해서 한 곳에서 만들어두고 가져다가 사용합니다.  
<Br>

```java
@Slf4j
@Aspect
public class AspectV2 {

    // Pointcut 분리
    // com.example.mvc.order패키지와 하위 패키지
    @Pointcut("execution(* com.example.mvc.order..*(..))")
    private void allOrder(){} 

    // 분리된 pointcut 적용
    @Around("allOrder()")
    public Object doLog(ProceedingJoinPoint joinPoint)throws Throwable{
        // 생략
    }

    // 분리된 pointcut 적용
    @Around("allOrder()")
    public Object anotherJob(ProceedingJoinPoint joinPoint)throws Throwable{
        // 생략
    }

    // 다른 Pointcut
    // 클래스 이름 패턴이 *Service
    @Pointcut("execution(* *..*Service.*(..))")
    private void allService(){}

    // Pointcut 조합
    @Around("allOrder() && allService()")
    public Object anotherJob(ProceedingJoinPoint joinPoint)throws Throwable{
        // 생략
    }
}
```
하나의 Aspect 안에서 여러개의 Advice를 정의하는데 Pointcut으로 사용하는 것이 같다면 private으로 뽑아두고 사용할 수 있습니다.  
또한, &&와 ||으로 조합하여 사용할 수 있습니다.  

<br>

```java
package com.example.mvc.order.aop;
public class Pointcuts {
    @Pointcut("execution(* com.example.mvc.order..*(..))")
    public void allOrder(){}
}

@Slf4j
@Aspect
public class AspectV3 {
    
    @Around("com.example.mvc.order.aop.Pointcuts.allOrder()")
    public Object doLog(ProceedingJoinPoint joinPoint)throws Throwable{
        // 생략
    }
}
```
만약 Pointcut을 공통적으로 많은 곳에서 사용한다면 클래스로 빼두고 사용할 수 있습니다.  
위처럼 따로 Pointcut을 외부로 뽑아두고 사용하는 경우, 사용하는 Aspect에서는 해당 Pointcut이 위치한 __패키지명.클래스명.메서드명__ 형식으로 명시하면 됩니다.  

<Br>

## 5. Pointcut
---
Pointcut은 Advice가 적용될 위치를 선별하는 기능입니다.  
스프링 AOP는 프록시 기반이기 때문에 메서드만 적용 가능하므로 어느 메서드에 적용할 것인지 명시하는 것이라고 생각해도 됩니다.  
앞서 Advice 애노테이션의 속성으로 Pointcut을 명시해서 사용했었습니다.  
포인트컷 지시자의 종류는 여러가지가 있기 때문에 하나씩 살펴보겠지만 실질적으로는 __execution과 @annotation__ 만 거의 사용하게 됩니다.  
아래서 사용하는 모든 예시는 아래 코드를 기반으로 합니다.  
```java
package com.example.mvc.aop.member;
public interface MemberService {
    String hello(String param);
}

package com.example.mvc.aop.member;
@Component
public class MemberServiceImpl implements MemberService {
    @Override
    public String hello(String param) {
        return "ok";
    }

    public String internal(String param){
        return "ok";
    }
}
```

### Pointcut 종류
#### execution
__많은 종류의 포인트컷이 있지만 실질적으로는 가장 많이 사용하게 되는 종류입니다.__  
```
execution(접근제어자? 반환타입 선언타입?메서드이름(파리미터) 예외?)
```
+ ?가 붙은 것들은 생략이 가능합니다. -> 접근제어자, 선업타입, 예외
+ \* 패턴을 통해 모든 타입 허용을 표현합니다.
+ ..을 통해 모든타입 허용과 파라미터수가 상관없다는 것을 표현합니다.
+ __기본적으로 상위 타입을 명시하면 하위 타입에도 적용이 되지만, 하위 타입에만 메서드를 명시하는 경우 매칭이 불가능합니다.__
    - 즉, 타입은 상위타입으로 명시하고 메서드는 하위타입에만 있다면 적용 불가능
+ 파라미터 타입의 경우 정확해야만 매칭됩니다. -> __부모타입을 허용하지 않습니다.__


예시가 많기 때문에 설명은 주석으로 달겠습니다.
```java
@Slf4j
class ExecutionTest {

    AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
    Method helloMethod;

    @BeforeEach
    public void init() throws NoSuchMethodException {
        helloMethod = MemberServiceImpl.class.getMethod("hello",String.class);
    }

    @Test
    void printMethod(){
        // public java.lang.String com.example.mvc.aop.member.MemberServiceImpl.hello(java.lang.String)
        log.info("helloMethod={}",helloMethod);
    }

    // 생략없이 정확한 매칭
    // 접근제한자 반환타입 타입(패키지를포함한클래스명).메서드명(인자타입)
    @Test
    void exactMatch(){
        pointcut.setExpression("execution(public String com.example.mvc.aop.member.MemberServiceImpl.hello(String))");
        Assertions.assertThat(pointcut.matches(helloMethod, MemberServiceImpl.class)).isTrue();
    }

    // 모든 대상 적용
    // 생략 가능한 접근제한자, 타입 생략
    // 모든반환타입허용 모든메서드허용(모든인자허용)
    @Test
    void allMatch() {
        pointcut.setExpression("execution(* *(..))");
        Assertions.assertThat(pointcut.matches(helloMethod,MemberServiceImpl.class)).isTrue();
    }

    // 특정 메서드 적용
    // 모든반환타입허용 hello메서드명만허용 모든파라미터허용
    @Test
    void nameMatch() {
        pointcut.setExpression("execution(* hello(..))");
        Assertions.assertThat(pointcut.matches(helloMethod,MemberServiceImpl.class)).isTrue();
    }

    // 특정 메서드명으로 시작하는 모든 메서드에 적용
    // 모든반환타입허용 hel로시작하는모든메서드허용 모든파라미터허용
    @Test
    void nameMatchStar1() {
        pointcut.setExpression("execution(* hel*(..))");
        Assertions.assertThat(pointcut.matches(helloMethod,MemberServiceImpl.class)).isTrue();
    }

    // 특정 단어를 포함한 모든 메서드에 적용
    // 모든반환타입허용 l이들어간모든메서드허용 모든파라미터허용
    @Test
    void nameMatchStar2() {
        pointcut.setExpression("execution(* *l*(..))");
        Assertions.assertThat(pointcut.matches(helloMethod,MemberServiceImpl.class)).isTrue();
    }

    // 메서드명 불일치로 매칭 실패
    @Test
    void nameMatchFalse() {
        pointcut.setExpression("execution(* nono*(..))");
        Assertions.assertThat(pointcut.matches(helloMethod,MemberServiceImpl.class)).isFalse();
    }

    // 모든반환타입허용 특정메서드명 모든파라미터허용
    @Test
    void packageMatch() {
        pointcut.setExpression("execution(* com.example.mvc.aop.member.MemberServiceImpl.hello(..))");
        Assertions.assertThat(pointcut.matches(helloMethod,MemberServiceImpl.class)).isTrue();
    }

    // 모든반환타입허용 member패키지에있는모든타입허용(모든클래스허용) member패키지에있는모든타입의모든메서드허용 모든파라미터허용
    @Test
    void packageMatch2() {
        pointcut.setExpression("execution(* com.example.mvc.aop.member.*.*(..))");
        Assertions.assertThat(pointcut.matches(helloMethod,MemberServiceImpl.class)).isTrue();
    }

    // MemberServiceImpl은 aop패키지가 아니라 aop의 하위패키지에 존재하므로 매칭 실패함
    // 모든반환타입허용 aop패키지에있는모든타입허용 aop패키지에있는모든타입의모든메서드허용 모든파라미터허용
    @Test
    void packageMatchFalse() {
        pointcut.setExpression("execution(* com.example.mvc.aop.*.*(..))");
        Assertions.assertThat(pointcut.matches(helloMethod,MemberServiceImpl.class)).isFalse();
    }

    // ..가 타입명에 들어가있음 -> aop와 그의 모든 하위패키지를 허용함
    // 모든반환타입허용 aop패키지와그하위패키지의모든타입허용 모든메서드허용 모든파라미터허용
    @Test
    void packageMatchSubPackage() {
        pointcut.setExpression("execution(* com.example.mvc.aop..*.*(..))");
        Assertions.assertThat(pointcut.matches(helloMethod,MemberServiceImpl.class)).isTrue();
    }

    // 모든반환타입허용 member패키지의MemberServiceImpl타입허용 모든메서드허용 모든파라미터허용
    @Test
    void typeExactMatch() {
        pointcut.setExpression("execution(* com.example.mvc.aop.member.MemberServiceImpl.*(..))");
        Assertions.assertThat(pointcut.matches(helloMethod,MemberServiceImpl.class)).isTrue();
    }

    // MemberServiceImpl는 MemberService의 구현체 즉, 자식이기 때문에 허용된다.
    // 모든반환타입허용 member패키지의MemberService타입허용 모든메서드허용 모든파라미터허용
    @Test
    void typeExactSuperType() {
        pointcut.setExpression("execution(* com.example.mvc.aop.member.MemberService.*(..))");
        Assertions.assertThat(pointcut.matches(helloMethod,MemberServiceImpl.class)).isTrue();
    }

    // 모든반환타입허용 member패키지의MemberService타입허용 모든메서드허용 모든파라미터허용
    // MemberService 인터페이스에는 없고 MemberServiceImpl구현체에만 따로 선언한 internal함수를 매칭하고 있으므로 매칭 실패
    // 부모타입을 명시했기 때문에 하위타입을 매칭할때도 부모타입에 존재하는 것만 매칭이 가능함
    @Test
    void typeExactNoSuperTypeInternal() throws NoSuchMethodException {
        pointcut.setExpression("execution(* com.example.mvc.aop.member.MemberService.*(..))");
        Method internalMethod = MemberServiceImpl.class.getMethod("internal",String.class);
        Assertions.assertThat(pointcut.matches(internalMethod,MemberServiceImpl.class)).isFalse();
    }

    // 모든반환타입허용 모든메서드허용 String파라미터1개만허용
    @Test
    void argMatch() throws NoSuchMethodException {
        pointcut.setExpression("execution(* *(String))");
        Assertions.assertThat(pointcut.matches(helloMethod,MemberServiceImpl.class)).isTrue();
    }

    // 모든반환타입허용 모든타입허용(생략) 모든메서드허용 파라미터없어야만허용
    @Test
    void noArgMatch() throws NoSuchMethodException {
        pointcut.setExpression("execution(* *())");
        Assertions.assertThat(pointcut.matches(helloMethod,MemberServiceImpl.class)).isFalse();
    }

    // 모든반환타입허용 모든타입허용(생략) 모든메서드허용 단1개의모든타입파라미터허용
    @Test
    void argsMatchStar() throws NoSuchMethodException {
        pointcut.setExpression("execution(* *(*))");
        Assertions.assertThat(pointcut.matches(helloMethod,MemberServiceImpl.class)).isTrue();
    }

    // 모든반환타입허용 모든타입허용(생략) 모든메서드허용 여러개의모든타입파라미터허용
    @Test
    void argsMatchAll() throws NoSuchMethodException {
        pointcut.setExpression("execution(* *(..))");
        Assertions.assertThat(pointcut.matches(helloMethod,MemberServiceImpl.class)).isTrue();
    }

    // 모든반환타입허용 모든타입허용(생략) 모든메서드허용 1번째는String파라미터만허용 이후에는여러개의모든타입의파라미터허용
    @Test
    void argsMatchComplex() throws NoSuchMethodException {
        pointcut.setExpression("execution(* *(String, ..))");
        Assertions.assertThat(pointcut.matches(helloMethod,MemberServiceImpl.class)).isTrue();
    }
}
```

#### within
within은 타입이 매칭되면 그 안에 모든 메서드가 매칭됩니다.  
execution에서 타입부분만 사용하는거라고 보면 됩니다.  
__차이점으로는 상위타입으로 하위타입매칭이 불가능합니다.__  
즉, 정확하게 타입이 맞아야만 동작합니다.  
```java
public class WithinTest {

    AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
    Method helloMethod;

    @BeforeEach
    public void init() throws NoSuchMethodException {
        helloMethod = MemberServiceImpl.class.getMethod("hello", String.class);
    }

    // member패키지 MemberServiceImpl타입의 모든 메서드
    @Test
    void withinExact() {
        pointcut.setExpression("within(com.example.mvc.aop.member.MemberServiceImpl)");
        assertThat(pointcut.matches(helloMethod, MemberServiceImpl.class)).isTrue();
    }

    // member패키지 MemberServiceImpl가포함된타입의 모든 메서드
    @Test
    void withinStar() {
        pointcut.setExpression("within(com.example.mvc.aop.member.*MemberService*)");
        assertThat(pointcut.matches(helloMethod, MemberServiceImpl.class)).isTrue();
    }

    // aop 패키지와 그 하위패키지의 모든 타입의 모든 메서드
    @Test
    void withinSubPackage() {
        pointcut.setExpression("within(com.example.mvc.aop..*)");
        assertThat(pointcut.matches(helloMethod, MemberServiceImpl.class)).isTrue();
    }

    // member패키지의 MemberService 타입
    // MemberServiceImpl는 구현체이므로 정확한 타입매칭이 아니라 실패한다. -> execution은 허용함
    @Test
    @DisplayName("타켓의 타입에만 직접 적용, 인터페이스를 선정하면 안된다.")
    void withinSuperTypeFalse() {
        pointcut.setExpression("within(com.example.mvc.aop.member.MemberService)");
        assertThat(pointcut.matches(helloMethod, MemberServiceImpl.class)).isFalse();
    }
}
```

#### args
args는 파라미터 타입으로 매칭합니다.  
execution은 파라미터 타입이 정확하게 일치해야했지만, args는 부모타입의 경우, 하위타입 매칭이 가능합니다.  
__주의해야할 점은 args는 단독으로 사용하면 안됩니다.__  
args의 경우 스프링은 모든 스프링 빈에 AOP를 적용하려고 시도하기 때문에 스프링 내부에서 사용하는 빈 중에서 final로 지정된 빈들도 있어 오류가 발생할 수 있습니다.
```java
@Test
void args() {
    // hello(String)과 매칭
    assertThat(pointcut("args(String)")
            .matches(helloMethod, MemberServiceImpl.class)).isTrue();
    // Object는 String의 상위 타입으로 매칭 가능
    assertThat(pointcut("args(Object)")
            .matches(helloMethod, MemberServiceImpl.class)).isTrue();
    // 인자가 없는 경우
    assertThat(pointcut("args()")
            .matches(helloMethod, MemberServiceImpl.class)).isFalse();
    // 인자의 타입과 개수 무관
    assertThat(pointcut("args(..)")
            .matches(helloMethod, MemberServiceImpl.class)).isTrue();
    // 단 하나의 타입무관 파라미터
    assertThat(pointcut("args(*)")
            .matches(helloMethod, MemberServiceImpl.class)).isTrue();
    // 첫 인자는 String, 이후는 개수,타입 무관
    assertThat(pointcut("args(String,..)")
            .matches(helloMethod, MemberServiceImpl.class)).isTrue();
}
```

#### @target, @within
![그림6](https://github.com/backtony/blog-code/blob/master/spring/img/aop/2/2-6.PNG?raw=true)  
+ @target
    - 자신의 클래스와 자신의 모든 부모클래스의 모든 메서드에 적용합니다.
+ @within
    - 자신의 클래스의 모든 메서드에만 적용합니다.
+ 둘다 __타입에 있는 애노테이션으로 AOP적용 여부를 판단__ 합니다.

__@target과 @within도 args와 같은 이유로 단독으로 사용하면 안됩니다.__  
```java
// 부착할 애노테이션
package com.example.mvc.aop.member.annotation;
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ClassAop {
}

// 애노테이션 미부착
static class Parent {
    public void parentMethod(){} //부모에만 있는 메서드
}

// 애노테이션 부착
@ClassAop
static class Child extends Parent {
    public void childMethod(){}
}

// ClassAop 애노테이션이 붙어있는 타입과 붙어있지 않더라도 그에 대한 상위 타입까지 전부 적용
@Around("execution(* hello.aop..*(..)) && @target(com.example.mvc.aop.member.annotation.ClassAop)")
public Object atTarget(ProceedingJoinPoint joinPoint) throws Throwable {
        // 생략
}

// ClassAop 애노테이션이 붙어있는 타입의 메서드에만 적용
@Around("execution(* hello.aop..*(..)) && @within(com.example.mvc.aop.member.annotation.ClassAop)")
public Object atWithin(ProceedingJoinPoint joinPoint) throws Throwable {
    // 생략
}
```

#### @annotation
@annotation은 __메서드가 주어진 애노테이션을 갖고 있는 경우__ 적용됩니다.  
@Target의 경우는 애노테이션이 붙어있는 클래스였는데 @annotation의 경우는 메서드입니다.  
__@annotation의 경우는 종종 사용하는 경우가 있어 알아두는게 좋습니다.__  
```java
// 부착할 애노테이션
package com.example.mvc.aop.member.annotation;
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MethodAop {
    String value();
}

public class MemberServiceImpl {
    // 대상 메서드에 부착
    @MethodAop("test value")
    public String hello(String param) {
        return "ok";
    }
}

// 적용
@Around("@annotation(com.example.mvc.aop.member.annotation.MethodAop)")
public Object doAtAnnotation(ProceedingJoinPoint joinPoint) throws Throwable {
    // 생략
}
```

#### bean
스프링 빈의 이름으로 AOP 적용 여부를 지정합니다.  
스프링에서만 사용할 수 있는 특별한 지시자입니다.  
```java
// 빈 이름이 orderService이거나 Repository로 끝나는 빈에 적용
@Around("bean(orderService) || bean(*Repository)")
public Object doLog(ProceedingJoinPoint joinPoint) throws Throwable {
    // 생략
}
```

#### this, target
```
this(hello.aop.member.MemberService)
target(hello.aop.member.MemberService)
```
스프링에서 AOP를 적용하면 실제 대상(target) 객체 대신에 프록시가 스프링 빈으로 등록됩니다.  
+ this
    - 스프링 빈 객체(스프링 AOP 프록시)를 대상으로 매칭합니다.
+ target
    - Target 객체(스프링 AOP 프록시가 가르키는 실제 대상)을 대상으로 매칭합니다.  
+ 둘다 * 패턴을 사용할 수 없습니다.
+ 둘다 적용 타입 하나를 정확하게 지정한다.
+ 부모타입을 허용합니다. 

__둘의 가장 큰 차이는 프록시 생성 방식에 따른 AOP적용 여부입니다.__  

__JDK 동적 프록시__  
![그림7](https://github.com/backtony/blog-code/blob/master/spring/img/aop/2/2-7.PNG?raw=true)  

JDK 동적 프록시는 인터페이스 기반이므로 프록시를 생성합니다.  
+ MemberService 인터페이스 지정
    - this(hello.aop.member.MemberService), target(hello.aop.member.MemberService)
        - 빈으로 등록되있는 프록시는 memberService 인터페이스의 구현체 입니다.
        - 실제 동작할때 memberService는 빈으로 등록된 프록시(구현체)의 부모이므로 AOP의 대상이 됩니다.
+ MemberServiceImpl 구체 클래스 지정
    - this(hello.aop.member.MemberServiceImpl)
        - 현재 빈으로 등록된 프록시는 MemberServiceImpl의 인터페이스인 MemberService 인터페이스의 구현체 입니다.
        - 인터페이스의 구현체로 memberServiceImpl과 프록시가 있는 상태입니다.
        - this는 프록시를 대상으로 하기 때문에 memberServiceImpl은 프록시의 부모가 아니므로 __AOP의 대상이 아닙니다.__
    - target(hello.aop.member.MemberServiceImpl)
        - 프록시의 대상 객체로 판단하기 때문에 MemberServiceImpl은 AOP 적용 대상입니다.



__CGLIB 프록시__  
![그림8](https://github.com/backtony/blog-code/blob/master/spring/img/aop/2/2-8.PNG?raw=true)  
CGLIB은 구체 클래스 기반으로 프록시를 생성하여 빈으로 등록합니다.  
+ MemberService 인터페이스를 지정했을 때
    - this(hello.aop.member.MemberService), target(hello.aop.member.MemberService)
        - 빈에 들어있는 프록시는 인터페이스의 구현체를 상속받아서 구현되었기 때문에 부모인 MemberService는 AOP의 대상이 됩니다.
+ MemberServiceImpl 구체 클래스 지정
    - this(hello.aop.member.MemberServiceImpl)
        - 빈에 들어있는 프록시는 인터페이스의 구현체를 상속받아서 구현되었기 때문에 부모인 MemberServiceImpl는 __AOP의 대상이 됩니다.__
    - target(hello.aop.member.MemberServiceImpl)
        - target 객체라 MemberServiceImpl로 일치하므로 AOP 적용 대상이 됩니다.

정리하자면, JDK 동적 프록시를 사용할 때, this의 대상을 구체 클래스로 지정하게 되면 구체 클래스의 인터페이스의 구현체가 프록시로 등록되면서 지정했던 구체 클래스는 AOP의 대상이 아닙니다.  
CGLIB은 모두 AOP의 적용 대상이 됩니다.  
__즉, 프록시를 대상으로 하는 this의 경우 구체 클래스를 지정하면 프록시 생성 전략에 따라 다른 결과가 나올 수 있습니다.__  
사실 this와 target을 많이 사용하진 않기 때문에 구체 클래스(Impl)는 CGLIB의 경우 AOP의 대상이 되지만, JDK 동적 프록시의 대상이 되지 못한다 정도로 알아두면 됩니다.  
이 이유때문에 Spring이 기본 프록시로 CGLIB을 선택하는데도 기여를 합니다.

### 매개변수 전달하기
앞서 execution과 @annotation를 주로 사용한다고 했습니다.  
하지만 this, target, args,@target, @within, @annotation, @args를 이용하면 매개변수를 쉽게 꺼내서 사용할 수 있습니다.  
먼저 사용하지 않고 파라미터를 꺼내보겠습니다.  
```java
@Pointcut("execution(* com.example.mvc.aop.member..*.*(..))")
private  void allMember(){}

@Around("allMember()")
public Object logArgs(ProceedingJoinPoint joinPoint) throws Throwable {
    Object arg = joinPoint.getArgs()[0];
    log.info("args ={}",arg);
    return joinPoint.proceed();
}
```
joinPoint.getArgs()를 꺼내면 Object 배열이 나오게 됩니다.  
함수명 그대로 인자값을 Object배열로 꺼내게 되는 것이고 인덱스로 꺼내서 인자를 사용할 수 있습니다.  
하지만 타입 캐스팅도 해줘야되고 조금 불편한 감이 있습니다.  
이제 위에 언급한 내용을 사용해서 매개변수를 꺼내보겠습니다.  
```java
@Around("allMember() && args(arg,..)")
public Object logArgs2(ProceedingJoinPoint joinPoint, String arg) throws Throwable {
    log.info("[logArgs2]{}, arg={}", joinPoint.getSignature(), arg);
    return joinPoint.proceed();
}
```
allMember()를 통해 Pointcut 대상을 한정시킨 뒤에 args를 통해 파라미터를 매칭시킵니다.  
logArgs2메서드의 2번째 인자로 String arg를 줬는데 이 변수명은 포인트컷의 파라미터명 arg와 일치시켜야만 주입됩니다.  
또한, 타입을 String으로 주었는데 이는 대상 메서드의 첫번째 파라미터 타입이 String 인 것만 해당 Advice가 적용됩니다.  
__즉, 포인트컷의 파라미터명과 advice의 파라미터명을 일치시켜야하며, advice의 파라미터의 타입과 포인트컷 대상 메서드의 파라미터 타입이 일치해야만 해당 Advice가 적용됩니다.__  
<br>

```java
 @Before("allMember() && @target(annotation)")
public void atTarget(JoinPoint joinPoint, ClassAop annotation) {
    log.info("[@target]{}, obj={}", joinPoint.getSignature(), annotation);
}

@Before("allMember() && @within(annotation)")
public void atWithin(JoinPoint joinPoint, ClassAop annotation) {
    log.info("[@within]{}, obj={}", joinPoint.getSignature(), annotation);
}

@Before("allMember() && @annotation(annotation)")
public void atAnnotation(JoinPoint joinPoint, MethodAop annotation) {
    log.info("[@annotation]{}, annotationValue={}", joinPoint.getSignature(), annotation.value());
}
```
@target, @within, @annotation 모두 애노테이션이 붙어있는 것을 대상으로 진행합니다.  
기존에는 속성으로 패키지명.타입명을 명시했지만, 이제는 Advice 함수에서 파라미터로 받을 변수명을 적어주고 Advice 함수의 파라미터에서 해당 타입을 명시하고 변수명을 속성으로 명시한 변수명으로 일치시킵니다.  
결과적으로 &&로 execution으로 포인트컷을 명시했으니, 추가적으로 annotation이 붙은 경우까지 필터링해서 해당 애노테이션을 파라미터로 받는 것입니다.

<br>

```java
@Before("allMember() && this(obj)")
public void thisArgs(JoinPoint joinPoint, MemberService obj) {
    log.info("[this]{}, obj={}", joinPoint.getSignature(), obj.getClass());
}

@Before("allMember() && target(obj)")
public void targetArgs(JoinPoint joinPoint, MemberService obj) {
    log.info("[target]{}, obj={}", joinPoint.getSignature(), obj.getClass());
}
```
+ this
    - 대상 타입의 프록시 전달합니다.
+ target
    - 실제 대상 타입을 전달합니다.
+ 둘다 실제 타입과 다른 경우 Advice가 적용되지 않습니다.

<br>

## 6. AOP 재시도
---
```java
@Aspect
public class RetryAspect {
    @Around("@annotation(retry)")
    public Object doRetry(ProceedingJoinPoint joinPoint, Retry retry) throws Throwable {
        
        int maxRetry = retry.value();
        Exception exceptionHolder = null;

        for (int retryCount = 1; retryCount <= maxRetry; retryCount++) {
            try {        
                return joinPoint.proceed();
            } catch (Exception e) {
                exceptionHolder = e;
            }
        }
        throw exceptionHolder;
    }
}
```
간헐적으로 외부 서버에 통신이 안되거나 할 경우, 몇 번의 재시도 횟수를 지정해두고 AOP를 재시도할 수 있습니다.  
try-catch문에서 타겟 메서드를 호출했을 때, 예외가 터지면 catch에서 먹고 for문으로 지정 횟수만큼 반복을 진행합니다.  
최대 횟수 안에 성공한다면 정상적으로 리턴하게 되고, 최대 횟수 전에 성공하지 못하면 마지막에 예외를 던집니다.  
<br>

## 7. 실무 주의사항
---
### 프록시 내부 호출
```java
@Slf4j
@Component
public class CallServiceV0 {
    public void external() {
        log.info("call external");
        internal(); //내부 메서드 호출(this.internal())
    }
    public void internal() {
        log.info("call internal");
    } 
}
```
위의 두 메서드에 모두 AOP가 Before로 들어갔다고 가정해봅시다.  
그렇다면 빈 컨테이너에는 CallServiceV0의 프록시가 들어가 있을 것이고, external과 internal이 각각 호출된다면 프록시를 통해서 호출됩니다.  
하지만, external 함수의 경우 내부에서 internal을 호출하고 있는데, external이 호출될 때는 AOP가 적용되지만 내부에서 internal을 호출할 때는 AOP가 적용되지 않고 바로 내부 internal을 호출합니다.  
![그림9](https://github.com/backtony/blog-code/blob/master/spring/img/aop/2/2-9.PNG?raw=true)  

#### 대안 - 구조 변경하기
```java
@Slf4j
@Component
public class InternalService {
    public void internal() {
        log.info("call internal");
    }
}

@Slf4j
@Component
@RequiredArgsConstructor
public class CallServiceV3 {

    private final InternalService internalService;

    public void external() {
        log.info("call external");
        internalService.internal(); //외부 메서드 호출
    }
}
```
둘을 분리해서 만들고 internalService를 통해서 internal 함수를 호출하게 만듭니다.  
internalService는 프록시로 빈에 등록되어 있기 때문에 결과적으로 프록시를 통해 호출하게 됩니다.
![그림10](https://github.com/backtony/blog-code/blob/master/spring/img/aop/2/2-10.PNG?raw=true)  

### CGLIB과 JDK 동적 프록시 중 Spring의 선택
JDK 동적 프록시는 인터페이스 기반, CGLIB은 구체 클래스 기반으로 프록시를 생성합니다.  
인터페이스가 없다면 당연히 CGLIB으로 동작하겠지만, 인터페이스가 있는 경우라면 CGLIB과 JDK 동적 프록시 중에서 선택할 수 있습니다.  

#### 타입 캐스팅
__JDK 동적 프록시__  
![그림11](https://github.com/backtony/blog-code/blob/master/spring/img/aop/2/2-11.PNG?raw=true)  
앞서 target과 this를 설명할 때도 언급했었는데, __JDK 동적 프록시는 구체 클래스로 타입 캐스팅이 불가능__ 합니다.  
MemberServiceImpl을 대상으로 프록시를 생성한다면 인터페이스인 MemberService를 기반으로 구현체(프록시)로 만들어 빈으로 등록합니다.  
따라서 프록시는 MemberService로는 타입캐스팅이 가능하나, MemberServiceImpl로는 타입캐스팅이 불가능합니다.  

<br>

__CGLIB__  
![그림12](https://github.com/backtony/blog-code/blob/master/spring/img/aop/2/2-12.PNG?raw=true)  
__CGLIB은 구체 클래스로 타입 캐스팅이 가능__ 합니다.  
CGLIB은 구체 클래스를 상속받아 프록시를 생성합니다.  
따라서 MemberServiceImpl을 프록시 대상으로 선택한다면, MemberServiceImpl을 상속받아서 프록시를 만들어 빈으로 등록하기 때문에 프록시는 당연히 MemberServiceImpl로 타입캐스팅이 가능합니다.  

<br>

이런 차이는 __의존관계 주입__ 에서 문제를 발생시킵니다.  
```java
// 부가적인 코드는 생략
@Slf4j
@SpringBootTest(properties = {"spring.aop.proxy-target-class=false"}) //JDK 동적 프록시
public class ProxyDITest {

    @Autowired
    MemberService memberService;

    @Autowired
    MemberServiceImpl memberServiceImpl;

}
```
JDK 동적 프록시 설정으로 돌리게 되면 빈에 등록된 프록시는 MemberServiceImpl으로 타입캐스팅이 불가능하므로 의존관계 주입에 실패합니다.  
반면에 CGLIB을 사용할 경우 앞서 설명했듯이 타입캐스팅이 가능하기 때문에 의존관계 주입이 가능합니다.  
__정리하면, CGLIB은 구체 클래스가 AOP의 대상이 되고, JDK 동적 프록시는 구체 클래스가 AOP의 대상이 되지 못합니다.__

#### CGLIB 한계
앞서 타입 캐스팅까지는 CGLIB이 좋은 것 같지만 CGLIB에는 다음과 같은 문제점이 있습니다.  
+ 대상 클래스에 기본 생성자 필수
    - 구체 클래스를 상속받아 구현되기 때문에 자바에서는 자식 생성자에서 부모 클래스의 생성자를 반드시 호출해야 합니다. 
+ 부모 생성자 2번 호출 문제
    - 실제 target의 객체를 생성할 때 1번
    - 프록시 객체를 생성할 때 부모 생성자 호출 1번
+ final 키워드 클래스, 메서드 사용 불가
    - final 키워드에서 오는 상속의 문제점이 있지만, 일반적인 웹 애플리케이션 개발에서는 final 키워드를 잘 사용하지 않아서 특별한 문제가 되진 않습니다.

위와 같은 문제점이 있지만 스프링에서는 위의 문제들을 해결하고 __GCLIB 사용을 채택__ 했습니다.  
+ objenesis 라이브러리로 기본 생성자 없이 객체 생성이 가능 -> 대상 클래스 기본 생성자 필수 문제 해결
+ target을 생성할 때는 생성자 호출 1번, objenesis 라이브러리로 프록시 객체 생성할때는 생성자 호출 없이 객체 생성 -> 부모 생성자 호출 2번 문제 해결

<br><Br>

결과적으로 __스프링 부트 2.0부터는 CGLIB를 기본으로 사용__ 하고 있습니다.  




<Br><Br>

__참고__  
<a href="https://www.inflearn.com/course/%EC%8A%A4%ED%94%84%EB%A7%81-%ED%95%B5%EC%8B%AC-%EC%9B%90%EB%A6%AC-%EA%B3%A0%EA%B8%89%ED%8E%B8#" target="_blank"> 스프링 핵심 원리 - 고급편</a>   


