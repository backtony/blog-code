# Spring - 프록시 팩토리와 빈 후처리기

## 1. 프록시 팩토리
---
+ JDK 동적 프록시 
    - 인터페이스 기반
+ CGLIB
    - 구체 클래스 기반

JDK 동적 프록시와 CGLIB에서 기반으로 하는 대상이 다르기 때문에 같은 기능을 제공하기 위해서는 JDK 동적 프록시가 제공하는 InvocationHandler와 CGLIB가 제공하는 MethodInterceptor로 따로 만들고 중복으로 관리해야하는 문제가 있습니다.  
스프링에서 제공하는 프록시 팩토리는 이 문제를 해결해줍니다.

<br><Br>

![그림1](https://github.com/backtony/blog-code/blob/master/spring/img/aop/1/1-1.PNG?raw=true)  
프록시 팩토리는 인터페이스가 있으면 JDK 동적 프록시를 사용하고, 클래스만 있다면 CGLIB을 사용합니다.(설정 변경 가능)  
프록시 팩토리가 조건에 맞게 선택을 해준다고 하더라도 동작을 정의하는 클래스가 InvocationHandler와 MethodInterceptor로 서로 다르기 때문에 각각 구현해야하는 문제가 있습니다.  
<br><br>


![그림2](https://github.com/backtony/blog-code/blob/master/spring/img/aop/1/1-2.PNG?raw=true)  
스프링은 이 문제를 해결하기 위해 부가 기능을 적용할 때, __Advice__ 라는 개념을 도입하였습니다.  
프록시 팩토리는 Advice를 호출하는 전용 InvocationHandler와 MethodInterceptor를 내부에서 사용하여 Advice를 호출하도록 구성되어 있습니다.  
따라서, 개발자는 각각을 구현할 필요없이 Advice만 만들어주면 됩니다.  
<br>

Advice를 구현하기 위해서는 MethodInterceptor 인터페이스를 구현하면 됩니다.  
```java
package org.aopalliance.intercept;
public interface MethodInterceptor extends Interceptor {
    Object invoke(MethodInvocation invocation) throws Throwable;
}
```
+ MethodInterceptor
    - CGLIB의 프록시 기능 정의할 때 사용하는 와 이름이 동일하지만 패키지 명이 다릅니다.  
    - MethodInterceptor는 Interceptor를 상속하고 Interceptor는 Advice 인터페이스를 상속합니다.
+ MethodInvocation invocation
    - 내부에는 다음 메서드를 호출하는 방법, 현재 프록시 객체 인스턴스, args, 메서드 정보등이 포함되어 있습니다.
    - 기존에 InvocationHandler와 MethodInterceptor를 구성할 때 제공되었던 파라미터들이 invocation안으로 다 들어갔다고 보면 됩니다.


#### 예시
간단하게 실행 시간을 찍는 프록시를 만들겠습니다.  
```java
@Slf4j
public class TimeAdvice implements MethodInterceptor {
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        log.info("TimeProxy 실행");
        long startTime = System.currentTimeMillis();

        // 실제 로직 실행
        Object result = invocation.proceed();

        long endTime = System.currentTimeMillis();
        log.info("result Time = {}",endTime-startTime);

        return result;
    }
}
```
+ invocation.proceed()
    - 실제 target 클래스의 대상 메서드를 호출하고 그 결과를 받습니다.
    - JDK 동적 프록시와 CGLIB를 사용할 때는, 인자로 target과 args를 넣어줘야 했는데 이는 프록시 팩토리에서 프록시를 생성하는 단계에서 전달받기 때문에 invocation이 이미 갖고 있습니다.

<br>


__인터페이스 기반__
```java
@Slf4j
public class ProxyFactoryTest {

    @Test
    @DisplayName("인터페이스가 있으면 JDK 동적 프록시 사용")
    void interfaceProxy() {
        ServiceInterface target = new ServiceImpl();
        
        // 프록시 팩토리 생성시, 프록시의 호출 대상을 인자로 넘긴다.
        ProxyFactory proxyFactory = new ProxyFactory(target);

        // 실행 동작 정의한 Advice 주입
        proxyFactory.addAdvice(new TimeAdvice());
        ServiceInterface proxy = (ServiceInterface) proxyFactory.getProxy();

        // 실행
        proxy.save();

        assertThat(AopUtils.isAopProxy(proxy)).isTrue();
        assertThat(AopUtils.isJdkDynamicProxy(proxy)).isTrue();
        assertThat(AopUtils.isCglibProxy(proxy)).isFalse();
    }
}
```
+ new ProxyFactory(target);
    - 프록시 팩토리 생성시, 프록시의 호출 대상을 인자로 넘깁니다.
    - 이때 인자로 넘기는 인스턴스에 인터페이스가 있다면 프록시를 만들 때 JDK 동적프록시를, 없다면 CGLIB을 통해 프록시를 생성합니다.
    - 위에서는 인터페이스를 넘겼지만, GCLIB을 사용하고 싶다면 인터페이스가 없는 단순 클래스를 넘기면 됩니다.
+ addAdvice
    - 프록시 팩토리를 통해서 만든 프록시가 사용할 부가 기능 로직을 세팅합니다.
+ proxyFactory.getProxy()
    - 프록시 팩토리에서 프록시 객체를 생성하고 그 결과를 받습니다.

<br>

__인터페이스가 있어도 GCLIB 사용하기__  
```java
@Slf4j
public class ProxyFactoryTest {
    @Test
    @DisplayName("ProxyTargetClass 옵션을 사용하면 인터페이스가 있어도 CGLIB를 사용하고, 클래스 기반 프록시 사용")
    void proxyTargetClass() {
        ServiceInterface target = new ServiceImpl();
        ProxyFactory proxyFactory = new ProxyFactory(target);
        
        // TargetClass -> 구체 클래스사용하기 -> CGLIB 사용
        proxyFactory.setProxyTargetClass(true);

        proxyFactory.addAdvice(new TimeAdvice());
        ServiceInterface proxy = (ServiceInterface) proxyFactory.getProxy();

        proxy.save();

        assertThat(AopUtils.isAopProxy(proxy)).isTrue();
        assertThat(AopUtils.isJdkDynamicProxy(proxy)).isFalse();
        assertThat(AopUtils.isCglibProxy(proxy)).isTrue();
    }
}
```
+ setProxyTargetClass
    - 해당 옵션을 true로 넘기게 되면 인터페이스가 있어도 구체 클래스를 기반으로 CGLIB을 통해 동적 프록시를 생성합니다.


<br>

### 포인트컷, 어드바이스, 어드바이저
---
![그림3](https://github.com/backtony/blog-code/blob/master/spring/img/aop/1/1-3.PNG?raw=true)  
+ 포인트컷(Pointcut)    
    - 어디에는 적용하고 어디에는 적용하지 않을지 판단하는 필터링 기능을 합니다.
    - 주로 클래스와 메서드 이름으로 필터링 합니다.
+ 어드바이스(Advice)
    - 프록시가 호출하는 부가 기능으로 단순하게 프록시가 수행하는 로직이라고 생각하면 됩니다.
+ 어드바이저(Advisor)
    - 포인트컷 1개 + 어드바이스 1개의 쌍을 의미합니다.
    - 어디에 어떤 로직을 적용할지 알고 있는 것을 의미합니다.


#### 예시
![그림4](https://github.com/backtony/blog-code/blob/master/spring/img/aop/1/1-4.PNG?raw=true)  
위와 같은 형태로 간단하게 포인트컷, 어드바이스, 어드바이저를 만들어보겠습니다.
```java
public class MultiAdvisorTest {

    @Test
    @DisplayName("하나의 프록시, 여러 어드바이저")
    void multiAdvisorTest2() {
        //client -> proxy -> advisor2 -> advisor1 -> target

        DefaultPointcutAdvisor advisor1 = new DefaultPointcutAdvisor(Pointcut.TRUE, new Advice1());
        DefaultPointcutAdvisor advisor2 = new DefaultPointcutAdvisor(Pointcut.TRUE, new Advice2());

        //프록시 팩토리 생성
        ServiceInterface target = new ServiceImpl();
        ProxyFactory proxyFactory = new ProxyFactory(target);

        // 어드바이저 등록
        proxyFactory.addAdvisor(advisor2);
        proxyFactory.addAdvisor(advisor1);

        // 프록시 생성
        ServiceInterface proxy = (ServiceInterface) proxyFactory.getProxy();

        //실행
        proxy.save();
    }

    @Slf4j
    static class Advice1 implements MethodInterceptor {
        @Override
        public Object invoke(MethodInvocation invocation) throws Throwable {
            log.info("advice1 호출");
            return invocation.proceed();
        }
    }

    @Slf4j
    static class Advice2 implements MethodInterceptor {
        @Override
        public Object invoke(MethodInvocation invocation) throws Throwable {
            log.info("advice2 호출");
            return invocation.proceed();
        }
    }
}
```
간단한 Advice 클래스 2개를 만들고 DefaultPointcutAdvisor를 이용해 Advice와 Pointcut을 인자로 넘겨 Advisor 2개를 만들었습니다.  
프록시 팩토리를 만들고 Advisor 2개를 등록하고 프록시를 만들었습니다.  

+ DefaultPointcutAdvisor
    - Advisor 인터페이스의 가장 일반적인 구현체로 생성자를 통해 한개의 포인트컷과 한개의 어드바이스를 넣어주면 됩니다.
+ Pointcut.TRUE
    - 항상 True를 반환하는 포인트 컷입니다.
+ proxyFactory.addAdvisor
    - 프록시 팩토리에 적용할 어드바이저를 저장합니다.
    - 등록한 순서대로 어드바이저가 적용됩니다.

스프링은 AOP를 적용할 때, 최적화를 진행해서 위의 예시처럼 프록시는 하나만 만들고, 하나의 프록시에 여러 Advisor를 적용하게 됩니다.  
즉, __스프링 AOP는 target마다 단 한개의 프록시만 생성합니다.__  

<br>

참고로 스프링은 무수히 많은 포인트컷을 제공합니다.  
+ NameMatchMethodPointcut
    - 메서드 이름 기반 매칭
    - PatternMatchUtils 사용
+ JdkRegexpMethodPointcut : JDK 정규 표현식 기반 매칭
+ TruePointcut : 항상 참
+ AnnotationMatchingPointcut : 애노테이션 매칭
+ AspectJExpressionPointcut : aspectJ 표현식 매칭

무수히 많은 포인트컷을 제공하지만 가장 중요한 것은 aspectJ이고 거의 aspectJ만 사용하게 됩니다.  


<br>

### 한계
---
__프록시 팩토리 덕분에 인터페이스 기반, 구체 클래스 기반을 구분하지 않고 프록시를 간편하게 생성할 수 있었습니다.__  
추가로 어드바이저, 어드바이스, 포인트컷 개념으로 __어떤 부가기능__ 을 __어디에 적용할지__ 명확하게 분리해서 사용할 수 있었습니다.  
__하지만 프록시 팩토리를 만들기 위해 너무 많은 설정을 해야 합니다.__  
만약 스프링 빈이 100개가 있고 여기에 프록시를 등록해 부가 기능을 부여한다고 한다면 100개의 동적 프록시 생성 코드를 만들어 프록시를 반환하도록 해야 합니다.  
이렇게 빈이 100개가 등록되어 있다면 결국 하고자 한다면 할 수는 있지만, 만약 __해당 빈들이 컴포넌트 스캔으로 올라간 경우 위 방법으로는 중간에 끼어들 수가 없기 때문에 프록시 적용이 불가능합니다.__  
이에 대한 해결책은 __빈 후처리기__ 입니다.  

<br>

## 2. 빈 후처리기
---
![그림5](https://github.com/backtony/blog-code/blob/master/spring/img/aop/1/1-5.PNG?raw=true)  
빈 후처리기는 __스프링이 빈 저장소에 등록할 목적으로 생성한 객체를 빈 저장소에 등록하기 직전에 조작할 때__ 사용됩니다.  
동작 과정은 다음과 같습니다.  
1. 스프링 빈 대상이 되는 객체를 생성한다.(@Bean, 콤포넌트 스캔 대상)
2. 생성된 객체를 빈 저장소에 등록하기 직전에 빈 후처리기에 전달한다.
3. 빈 후처리기는 전달된 스프링 빈 객체를 조작하거나 다른 객체로 바꿔치기할 수 있다.
4. 빈 후처리기는 객체를 빈 저장소에 반환하고 해당 빈은 빈 저장소에 등록된다.

빈 후처리기에서 바꿔치기 하는 작업에서 프록시를 생성해서 프록시를 반환하게 되면 빈 저장소에는 프록시가 빈으로 등록되게 됩니다.

<br>

빈 후처리기를 구현하기 위해서는 BeanPostProcessor 인터페이스를 구현하고 스프링 빈으로 등록하면 됩니다.
```java
public interface BeanPostProcessor {
    Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException
    Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException
}
```
+ postProcessBeforeInitialization
    - 객체 생성 이후에 @PostConstruct같은 초기화 작업 전에 호출되는 포스트 프로세서
+ postProcessAfterInitialization
    -  객체 생성 이후에 @PostConstruct같은 초기화 작업 후에 호출되는 포스트 프로세서

BeanPostProcessor 인터페이스를 직접 구현해서 빈으로 등록해도 되지만, 스프링에서는 더 편리한 방식을 제공합니다.  

### 스프링이 제공하는 빈 후처리기
스프링에서 제공하는 빈 후처리기를 사용하기 위해서는 의존성을 추가해야 합니다.
```
implementation 'org.springframework.boot:spring-boot-starter-aop'
```
라이브러리를 추가하면 aspectjweaver 라는 aspectJ 관련 라이브러리를 등록하고, 스프링 부트가 AOP 관련 클래스를 자동으로 스프링 빈에 등록합니다.  
이때 AnnotationAwareAspectJAutoProxyCreator라는 빈 후처리기가 스프링 빈에 자동으로 등록되는데, __프록시를 생성해주는 빈 후처리기__ 입니다.  
이 빈 후처리기는 __스프링 빈으로 등록된 Advisor들을 자동으로 찾아서 프록시가 필요한 곳에 자동으로 프록시를 적용하여 프록시를 반환합니다.__  
Advisor만으로 프록시가 필요한 곳을 찾고 적용할수 있는 이유는 Advisor 안에는 Pointcut과 Advice가 이미 포함되어 있어 Pointcut으로 프록시를 적용할지 여부를 판단하고, Advice로 부가 기능을 적용할 수 있습니다.  
참고로 AnnotationAwareAspectJAutoProxyCreator는 @AspectJ와 관련된 AOP 기능도 찾아서 자동으로 처리하고, @Aspect도 자동으로 인식해서 프록시를 만들고 AOP를 적용합니다.


### 동작 과정
![그림6](https://github.com/backtony/blog-code/blob/master/spring/img/aop/1/1-6.PNG?raw=true)  

1. 스프링 빈 대상이 되는 객체를 생성한다.(@Bean, 콤포넌트 스캔 대상)
2. 생성된 객체를 빈 저장소에 등록하기 직전에 빈 후처리기에 전달한다.
3. 모든 Advisor 빈을 조회하고 Pointcut을 통해 클래스와 메서드 정보를 매칭해보면서 프록시를 적용할 대상인지 판단합니다.
4. 모든 Advisor 중 하나의 조건에만 만족한다면 프록시를 생성하고 프록시를 빈 저장소로 반환합니다.
5. 만약 프록시 생성 대상이 아니라면 들어온 빈 그대로 빈 저장소로 반환합니다.
6. 빈 저장소는 객체를 받아서 빈으로 등록합니다.

<br><Br>

![그림7](https://github.com/backtony/blog-code/blob/master/spring/img/aop/1/1-7.PNG?raw=true)  
여기서 주의할 점은 여러 Advisor의 대상이 된다고 하더라도 __프록시는 1개만__ 만들고 그 안에 Advisor을 여러개 담게 된다는 것입니다.  

### 예시
```java
@Configuration
public class AutoProxyConfig {
    @Bean
    public Advisor advisor(LogTrace logTrace) {
        //pointcut
        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
        pointcut.setExpression("execution(* hello.proxy.app..*(..)) && !execution(* hello.proxy.app..noLog(..))");
        
        //advice 
        LogTraceAdvice advice = new LogTraceAdvice(logTrace);

        // Advisor 반환하여 빈 등록
        return new DefaultPointcutAdvisor(pointcut, advice);
    }
}
```
+ AspectJExpressionPointcut
    - AspectJ 포인트컷 표현식
    - \* : 모든 반환 타입
    - hello.proxy.app.. : 해당 패키지와 그 하위 패키지
    - *(..)
        - \* : 모든 메서드 이름
        - (..) : 파라미터는 무관

AspectJExpressionPointcut을 통해 pointcut을 정의하고 advice 하나를 만들고 DefaultPointcutAdvisor에 인자로 넘겨서 advisor를 만들어 빈으로 등록한 코드입니다.  


## 정리
---
+ 프록시 팩토리
    - 프록시를 적용하기 위해서는 JDK 동적 프록시, CGLIB 기능이 존재하지만 JDK는 인터페이스 기반, CGLIB는 구체 클래스 기반이기 때문에 공통 로직이라도 두개를 만들어 중복코드를 관리해야 하는 문제점이 있다.
    - __프록시 팩토리는 포인트컷, 어드바이스, 어드바이저 개념을 도입해 JDK 동적 프록시와 CGLIB을 구분하지 않고 사용하는 기능을 제공함으로써 기반이 다른 경우 2개로 관리해야했던 중복 코드를 없애주었다.__
    - __하지만 프록시 팩토리를 사용하기 위해서 빈 등록시 실제 프록시를 반환하는 코드를 직접 작성하는 등 너무 많은 설정이 필요하고, 컴포넌트 스캔 대상이 되는 빈 객체들은 사용할 수 없다.__
+ 빈 후처리기
    - __스프링이 빈 저장소에 등록할 목적으로 생성한 객체를 빈 저장소에 등록하기 직전에 조작하는 기능을 제공하여 프록시 팩토리의 문제점을 해결한다.__
    - 스프링이 제공하는 빈 후처리기를 사용하면 __Advisor만 빈으로 등록하면 알아서 대상에 대해 프록시를 생성한다.__
    - 스프링 빈이 되는 대상이 생성될 때, 빈으로 등록되어 있는 모든 Advisor를 조회하여 pointcut을 통해 대상 여부를 판단하고 맞다면 프록시를 반환하여 빈 저장소에서 프록시를 빈으로 등록하게 된다.
    - 여러 Advisor의 대상이 되는 경우, __하나의 프록시__ 안에 여러 개의 Advisor가 들어간다.
    - __Pointcut은 2가지에 사용된다.__
        - __빈이 생성되는 단계에서 프록시 적용 여부를 판단할 때__
            - pointcut을 통해 매칭되는 클래스와 해당 클래스 안에 여러 메서드 중 매칭되는 것이 하나라도 있다면 해당 클래스 혹은 인터페이스를 프록시로 생성된다.
        - __해당 빈이 실제 사용되는 시점에 부가 기능의 적용 대상인지 판단할 때__
            - 클래스 혹은 인터페이스가 프록시로 들어와있는데 해당 클래스 혹은 인터페이스 안에 모든 메서드가 프록시 적용 대상은 아니기 때문에 Pointcut으로 한번 더 판단을 해야 한다.

<br>

__프록시 팩토리와 빈 후처리기는 스프링 AOP를 이해하기 위한 지식입니다.__  
지금까지는 pointcut과 advice를 구현해서 advisor를 만들고 이를 빈으로 등록했습니다.  
실질적으로 Spring AOP가 동작할 때도 똑같이 동작하게 되는데 스프링은 이런 과정을 애노테이션을 통해서 더욱 간단하게 사용할 수 있도록 기능을 제공하고 있습니다.  
따라서 위에서 설명한 예시처럼 코딩할 일은 거의 없고 실무에서는 스프링에서 제공하는 애노테이션을 기반으로 구현하게 됩니다.  
__즉, 스프링 AOP가 동작하기 위해 이전에는 어떤 문제들이 있었고, 이 문제를 어떤 방식으로 해결하여 현재 사용하고 있는 Spring AOP가 나오게 됬는지 정도로 알고 지나가면 될 것 같습니다.__  



<Br><Br>

__참고__  
<a href="https://www.inflearn.com/course/%EC%8A%A4%ED%94%84%EB%A7%81-%ED%95%B5%EC%8B%AC-%EC%9B%90%EB%A6%AC-%EA%B3%A0%EA%B8%89%ED%8E%B8#" target="_blank"> 스프링 핵심 원리 - 고급편</a>   


