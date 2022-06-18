# Java - ThreadLocal


## 동시성 문제란?
---
![그림5](https://github.com/backtony/blog-code/blob/master/java/img/3/41-5.PNG?raw=true)  
동시성 문제란 __동일한 자원에 대해 여러 스레드가 동시에 접근__ 하면서 발생하는 문제입니다.  
간단한 예시로 살펴보겠습니다.
```java
@Slf4j
public class FieldService {

    private String nameStore;

    public void logic(String name){
        nameStore.set(name);
        log.info("저장 name={} -> nameStore={}",name,nameStore.get());
        sleep(1000);
        log.info("조회 nameStore ={}",nameStore.get());        
    }

    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
```
FieldService 클래스의 logic 메서드는 간단하게 인자로 들어온 name값을 nameStore 필드에 저장하고 1초 쉬고 저장된 nameStore값을 찍어보는 메서드입니다.  
<Br>

```java
@Slf4j
public class FieldServiceTest {

    private FieldService fieldService = new FieldService();

    @Test
    void field(){
        log.info("main start");

        Thread threadA = new Thread(() -> fieldService.logic("userA"));
        Thread threadB = new Thread(() -> fieldService.logic("userB"));

        threadA.start();
        sleep(100);
        threadB.start();

        sleep(3000);
        log.info("main exit");

    }
    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
```
FieldService의 logic 메서드를 2개의 쓰레드가 0.1초 간격으로 접근하는 테스트입니다.  
동시성 문제를 고려하지 않았다면 로그가 다음과 같이 찍히길 기대하고 있었을 것입니다.  
```
main start
저장 name=userA -> nameStore=userA
조회 nameStore =userA
저장 name=userB -> nameStore=userB
조회 nameStore =userB
main exit
```
하지만 실제로는 동시성 문제가 발생하여 다음과 같이 로그가 찍힙니다.  
```
main start
저장 name=userA -> nameStore=userA
저장 name=userB -> nameStore=userB
조회 nameStore =userB
조회 nameStore =userB
main exit
```
실제로 로직은 다음과 같이 동작합니다.  
1. A쓰레드가 logic에 들어와 nameStore값을 userA로 세팅하고 1초 대기상태로 들어간다.
2. B쓰레드가 logic에 들어와 nameStore값을 userB로 세팅하고 1초 대기상태로 들어간다.
3. A쓰레드가 깨어나서 nameStore값을 읽어서 조회하는데 이때, nameStore에는 자신이 저장한 값이 아닌 B쓰레드가 변경한 값이 들어있다.
4. B쓰레드가 깨어나서 nameStore값을 읽는다.  

<br><br>

이러한 동시성 문제는 지역 변수에서는 발생하지 않습니다.  
지역 변수는 쓰레드마다 각각 다른 메모리 영역이 할당되기 때문입니다.  
__동시성 문제가 발생하는 곳은 같은 인스턴스 필드(주로 싱글톤) 또는 static 같은 공용 필드에 접근할 때 발생합니다.__  
여기서 중요한 점은 값에 무조건 동시에 접근한다고 문제가 발생하는 것이 아니라 __값을 어디선가 변경할 때 발생__ 합니다.  
즉, __읽기만 한다면 동시성 문제는 발생하지 않습니다.__

<br>

## 동시성 문제 해결 - ThreadLocal
---
쓰레드 로컬은 __해당 쓰레드만 접근할 수 있는 개인 저장소__ 를 의미합니다.  
![그림1](https://github.com/backtony/blog-code/blob/master/java/img/3/41-1.PNG?raw=true)  
하나의 자원을 보관 창구 직원, 두 개의 쓰레드를 물건 보관소 고객이라고 생각하면 이해가 쉽습니다.  
두 고객이 동시에 창구 직원에게 물건을 보관하면 직원은 각 물건을 다른 보관함에 넣어두었다가 두 고객이 오면 각 보관함에서 찾아 꺼내주는 형태라고 보면 됩니다.  
<br>

앞선 예시 코드에서 ThreadLocal를 적용해보겠습니다. 
```java
@Slf4j
public class FieldService {

    private ThreadLocal<String> nameStore = new ThreadLocal<>();

    public void logic(String name){
        nameStore.set(name);
        log.info("저장 name={} -> nameStore={}",name,nameStore.get());
        sleep(1000);
        log.info("조회 nameStore ={}",nameStore.get());
        nameStore.remove();      
    }

    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
```
ThreadLocal의 사용법은 다음과 같습니다.
+ 동시에 접근하는 자원의 타입을 ThreadLocal로 변경하고 기존의 타입을 제네릭 안에 넣는다.
+ __반드시 new ThreadLocal 로 생성__ 하여 할당한다.
+ ThreadLocal.set() : 값을 저장
+ ThreadLocal.get() : 값 조회
+ ThreadLocal.remove() : 값 제거
    - __쓰레드가 사용이 끝난 후에는 반드시 remove로 값을 제거해야 한다.__

<br>

코드를 위와 같이 수정하고 앞서 작성했던 FieldServiceTest를 돌려보면 다음과 같은 결과가 나옵니다.
```
main start
저장 name=userA -> nameStore=userA
저장 name=userB -> nameStore=userB
조회 nameStore =userA
조회 nameStore =userB
main exit
```


### 주의 사항
앞서 동시성 문제를 ThreadLocal을 사용하면 간단하게 해결할 수 있었습니다.  
하지만 ThreadLocal을 사용할 때는 반드시 주의해야할 것이 있습니다.  
ThreadLocal을 모두 사용하고 나면 반드시 __ThreadLocal.remove()__ 를 호출해서 쓰레드 로컬에 저장된 값을 제거해야 합니다.  
이유를 하나의 요청 흐름으로 살펴보겠습니다.  
<br>


![그림2](https://github.com/backtony/blog-code/blob/master/java/img/3/41-2.PNG?raw=true)  
1. 사용자 A가 저장 HTTP 요청을 보낸다.
2. WAS는 쓰레드 풀에서 쓰레드를 꺼내서 Thread-A를 할당한다.
3. Thread-A는 사용자 A의 데이터를 Thread-A의 쓰레드 로컬에 저장한다.

<br><Br>

![그림3](https://github.com/backtony/blog-code/blob/master/java/img/3/41-3.PNG?raw=true)  
1. 사용자 A의 HTTP 응답이 종료된다.
2. WAS는 사용이 끝난 Thread-A를 쓰레드 풀에 반환한다.
3. Thread-A는 쓰레드 풀에 아직 살아있기 때문에 Thread-A의 쓰레드 로컬도 여전히 살아있다.

<br><Br>

![그림4](https://github.com/backtony/blog-code/blob/master/java/img/3/41-4.PNG?raw=true)  
1. 사용자 B가 조회 HTTP 요청을 보낸다.
2. WAS는 쓰레드 풀에서 쓰레드를 꺼내서 할당했는데 마침 Thread-A이다.
3. Thread-A가 조회요청을 수행하면서 이전에 사용자 A가 저장했던 쓰레드 로컬에 있는 데이터를 조회한다.

<br><Br>

결과적으로 사용자 B는 사용자 A의 데이터를 확인하게 되는 심각한 문제가 발생합니다.  
__따라서 해당 쓰레드의 요청이 끝날 때 반드시 쓰레드 로컬 값을 비워줘야 합니다.__






<Br><Br>

__참고__  
<a href="https://www.inflearn.com/course/%EC%8A%A4%ED%94%84%EB%A7%81-%ED%95%B5%EC%8B%AC-%EC%9B%90%EB%A6%AC-%EA%B3%A0%EA%B8%89%ED%8E%B8#" target="_blank"> 스프링 핵심 원리 - 고급편</a>   






