# Java - 동시성 이슈와 Atomic 사용법

## 멀티스레드 이슈
![그림1](https://github.com/backtony/blog-code/blob/master/java/img/11/java-51-1.PNG?raw=true)  

자바의 메모리 구조는 위와 같이 CPU - RAM 아키텍처 기반으로 다음과 같이 동작합니다.
1. CPU가 작업을 처리하기 위해 필요한 데이터를 RAM에서 읽어 CPU Cache Memory에 복제합니다.
2. 작업을 처리한 뒤, 변경된 CPU Cache Memory 데이터를 RAM에 덮어씌웁니다.(RAM 쓰기 작업)

CPU가 여러 개일 경우, 각 CPU 별 Cache Memory에 저장된 데이터가 달라 문제가 발생할 수 있습니다.  
이런 문제는 __가시성 문제와 동시 접근 문제__ 나뉩니다.  

### 가시성 문제
하나의 스레드에서 공유 자원(변수, 객체 등)을 수정한 결과가 다른 스레드에게 보이지 않을 경우 발생하는 문제입니다.  
```java
public class Main {
    private static boolean stopRequested; 

    public static void main(String[] args) throws InterruptedException {
        Thread background = new Thread(() -> {
            for (int i = 0; !stopRequested ; i++); 
            System.out.println("background 쓰레드가 종료되었습니다!");
        });
        
        background.start(); // (A)

        TimeUnit.SECONDS.sleep(1);
        stopRequested = true; // (B)
        System.out.println("main 쓰레드가 종료되었습니다!");
    }
}
```
(A) 부분에서 스레드를 시작하고 1초 대기 후 stopRequested의 값을 true로 바꾸므로 스레드가 종료되고 메인도 종료될 것이라고 예상할 수 있으나 그렇게 동작하지 않습니다.  
실제 출력은 다음과 같습니다.
```java
main 쓰레드가 종료되었습니다!
// 백그라운드 스레드는 종료되지 않고 계속 살아 있음
```
이 문제가 바로 가시성 문제입니다. 실제로는 다음과 같이 동작합니다.
1. background thread는 Main Thread와는 다른 CPU의 캐시메모리에 메인 메모리에 존재하는 stopRequested 공유자원을 복제합니다.
2. 이후 복제한 stopRequested를 사용해서 조건식을 반복해서 실행합니다.
3. 1초 이후 Main Thread에서 stopRequested를 true로 바꿉니다.
4. 하지만 background Thread 에서는 Main Thread와 다른 CPU Cache Memory에 있는 stopRequested를 참조하기 때문에 Main Thread에서 일어난 변경을 알아채지 못합니다. 

### 동시 접근 문제
__여러 스레드에서 공유자원(변수, 객체 등)을 동시에 접근하였을 때, 연산이 가장 늦게 끝난 결과값으로 덮어씌워지는 문제입니다.__  
```java
public class Main {
    private static int t;

    public static void main(String[] args) throws InterruptedException {
        for (int i = 0; i < 100; i++) {
            new Thread(() -> {
                for (int j = 0; j < 1000; j++)
                    System.out.println(t++);
            }).start();
        }
    }
}
```
코드를 보면 1부터 100000까지 출력할 것 같지만 실제로는 그렇지 않습니다.  
여러 개의 스레드가 공유자원에 접근하면서 값을 바꿔버리기에 순서가 보장되지 않기 때문입니다.


## 자바의 동시성 해결책
자바에서 동시성 문제를 해결하는데 3가지 방법이 있습니다.  
+ synchronized : 안전하게 동시성을 보장할 수 있습니다. 하지만 비용이 가장 큽니다.
+ volatile : 키워드가 붙은 자원은 하나의 thread만이 write하고 나머지는 스레드는 read만 한다는 전제하에만 동시성을 보장합니다.
    - __volatile 키워드를 붙인 자원은 read, write 작업이 CPU Cache Memory가 아닌 Main Memory에서 이뤄집니다.__
    - 즉, 자원을 저장하는 메모리는 하나가 되기 때문에 같은 공유자원에 대해 각각 메모리별로 다른 값을 가지는 경우가 없습니다. 
    - 하지만 여러 스레드에서 Main Memory에 있는 공유자원에 동시에 접근할 수 있으므로 여러 스레드에서 수정하게 되면, 계산값이 덮어씌워지게 되므로 동시 접근 문제를 해결할 수 없습니다.
    - __정리하면, 가시성 문제는 해결할 수 있지만, 동시 접근 문제는 해결할 수 없습니다.__
+ Atomic 클래스는 CAS(compare-and-swap)를 이용하여 동시성을 하므로 여러 쓰레드에서 데이터를 write해도 문제가 없습니다. synchronized 보다 적은 비용으로 동시성을 보장할 수 있습니다.
    - __CAS 알고리즘이란 현재 스레드가 존재하는 CPU의 CacheMemory와 MainMemory에 저장된 값을 비교하여, 일치하는 경우 새로운 값으로 교체하고, 일치하지 않을 경우 기존 교체가 실패되고, 이에 대해 계속 재시도하는 방식입니다.__
    - __CPU가 MainMemory의 자원을 CPU Cache Memory로 가져와 연산을 수행하는 동안 다른 스레드에서 연산이 수행되어 MainMemory의 자원 값이 바뀌었을 경우 기존 연산을 실패처리하고, 새로 바뀐 MainMemory 값으로 재수행하는 방식입니다.__

## AtomicLong
AtomicLong은 Long 자료형을 갖고 있는 Wrapping 클래스입니다.  
Thread-safe로 구현되어 멀티쓰레드에서 synchronized 없이 사용할 수 있습니다.  
또한 synchronized 보다 적은 비용으로 동시성을 보장할 수 있습니다.  

### 객체 생성
+ AtomicLong() : 초기값을 0으로 생성
+ AtomicLong(longVal) : 인자로 초기값을 주고 생성

```java
AtomicLong a1 = new AtomicLong();
System.out.println(a1.get()); // 0

AtomicLong a2 = new AtomicLong(10);
System.out.println(a2.get()); // 10
```


### get(), set(), getAndSet()
+ set(newVal) : 값을 변경
+ get() : 값 읽기
+ getAndSet(newVal) : __현재 값__ 을 리턴하고 새로운 값으로 업데이트

```java
AtomicLong a = new AtomicLong();
a.set(100);
a.get(); // 100
System.out.println(a.getAndSet(5)); // 100
System.out.println(a.get()); // 5
```


### getAndUpdate(), updateAndGet()
+ getAndUpdate(LongUnaryOperator) : 람다식을 받고 현재 값을 반환하고 람다식에 의힌 값으로 업데이트
+ updateAndGet(LongUnaryOperator) : 람다식을 받고 람다식에 의한 값으로 업데이트 한 후 해당 업데이트 된 값을 반환

```java
AtomicLong a = new AtomicLong(10);
LongUnaryOperator square = (n) -> n*n;
System.out.println(a.getAndUpdate(square)); // 10
System.out.println(a.get()); // 100

AtomicLong a2 = new AtomicLong(5);
System.out.println(a2.updateAndGet(square)); // 25
System.out.println(a2.get()); // 25
```


### getAndIncrement(), getAndAdd()
+ getAndIncrement() : 현재 값 리턴하고 +1 증가
+ incrementAndGet() : +1 증가시키고 변경된 값 리턴
+ getAndDecrement() : 현재 값 리턴하고 -1 감소
+ decrementAndGet() : -1 감소시키고 변경된 값 리턴
+ getAndAdd(newValue) : 현재 값 리턴하고, 현재 값에 newValue를 더하기
+ addAndGet(newValue) : 현재 값에 newValue를 더하고, 그 결과를 리턴

```java
AtomicLong a = new AtomicLong(10);
System.out.println(a.getAndIncrement()); // 10
System.out.println(a.get()); // 11

System.out.println(a.incrementAndGet()); // 12
System.out.println(a.get()); // 12

System.out.println(a.getAndDecrement()); // 12
System.out.println(a.get()); // 11

System.out.println(a.decrementAndGet()); // 10
System.out.println(a.get()); // 10

System.out.println(a.getAndAdd(5)); // 10
System.out.println(a.get()); // 15

System.out.println(a.addAndGet(5)); // 20
System.out.println(a.get()); // 20
```

### compareAndSet()
+ compareAndSet(expect, update) : 현재 값이 예상하는 값(expect)와 동일하다면 update 값으로 변경해주고 true를 리턴합니다. 그렇지 않다면 데이터 변경은 없고 false를 리턴합니다.

```java
int expect = 10;
int update = 1000;

AtomicLong a = new AtomicLong(10);
System.out.println(a.compareAndSet(expect,update)); // true
System.out.println(a); // 1000
```

## AtomicInteger
AtomicInteger는 int 자료형을 갖고 있는 wrapping 클래스입니다.  
AtomicInteger 클래스는 멀티쓰레드 환경에서 동시성을 보장합니다.

### 객체 생성
+ AtomicInteger() : 초기값을 0으로 생성
+ AtomicInteger(intVal) : 인자로 초기값을 주고 생성

```java
AtomicInteger a = new AtomicInteger();
System.out.println(a.get()); // 0

AtomicInteger a2 = new AtomicInteger(10);
System.out.println(a2.get()); // 10
```

### get(), set(), getAndSet()
+ set(newVal) : 값을 변경
+ get() : 값 읽기
+ getAndSet(newVal) : __현재 값__ 을 리턴하고 새로운 값으로 업데이트

```java
AtomicInteger a = new AtomicInteger();
System.out.println(a.get()); // 0

a.set(10);
System.out.println(a.get()); // 10

System.out.println(a.getAndSet(20)); // 10
System.out.println(a.get()); // 20
```

### compareAndSet()
+ compareAndSet(expect, update) : 현재 값이 예상하는 값(expect)와 동일하다면 update 값으로 변경해주고 true를 리턴합니다. 그렇지 않다면 데이터 변경은 없고 false를 리턴합니다.

```java
int expected = 20;
AtomicInteger atomic = new AtomicInteger(10);
System.out.println(atomic.compareAndSet(expected, 100)); // false
System.out.println(atomic.get()); // 10
```

## AtomicIntegerArray
AtomicIntegerArray는 int[] 자료형을 갖고 있는 wrapping 클래스입니다.  
멀티쓰레드 환경에서 synchronized 보다 적은 비용으로 동시성을 보장할 수 있습니다.  


### 객체 생성
+ 생성자 인자에 int[] 전달 -> 내부에 동일한 크기와 값을 가진 int[] 배열이 생성됩니다.
+ 생성자의 인자에 배열의 길이 전달 -> 길이만큼 내부에 배열이 생성되며 초기값은 0으로 설정됩니다.

```java
int arr[] = { 1, 2, 3, 4, 5 };
AtomicIntegerArray atomic = new AtomicIntegerArray(arr);
System.out.println(atomic); // [1, 2, 3, 4, 5]

AtomicIntegerArray atomic2 = new AtomicIntegerArray(5);
System.out.println(atomic2); // [0, 0, 0, 0, 0]
```

### get(), set(), getAndSet()
+ set(index, newValue) : 인덱스 위치의 값을 업데이트
+ get(index) : 인덱스 값 읽기
+ getAndSet(index, newValue) : 현재 값을 리턴하고 새로운 값으로 업데이트

```java
int arr[] = { 1, 2, 3, 4, 5 };
AtomicIntegerArray atomic = new AtomicIntegerArray(arr);
System.out.println(atomic); // [1, 2, 3, 4, 5]

int index = 1;
System.out.println(atomic.get(index)); // 2

index = 3;
int newValue = 10;
atomic.set(3, 10);  
System.out.println(atomic.get(index)); // 10

index = 4;
System.out.println(atomic.getAndSet(index, newValue)); // 5
System.out.println(atomic.get(index)); // 10

System.out.println(atomic); // [1, 2, 3, 10, 10]
```

### getAndUpdate
+ getAndUpdate(index, IntUnaryOperator) : 인덱스의 값을 반환하고 2번 째 인자로 람다식을 받아서 값을 업데이트

```java
int arr[] = { 1, 2, 3, 4, 5 };
AtomicIntegerArray atomic = new AtomicIntegerArray(arr);
System.out.println(atomic); // [1, 2, 3, 4, 5]

int index = 3;
IntUnaryOperator square = (i) -> i * i;

System.out.println(atomic.getAndUpdate(index, square)); // 4
System.out.println(atomic.get(index)); // 16
System.out.println(atomic); // [1, 2, 3, 16, 5]
```


### 증감 함수
+ getAndIncrement(index) : 현재 값 리턴하고, 변수에 +1
+ getAndDecrement(index) : 현재 값 리턴하고, 변수에 -1
+ getAndAdd(index, newValue) : 현재 값 리턴하고, newValue 더하기
+ addAndGet(index, newValue) : newValue 더하고 결과를 리턴

```java
int arr[] = { 1, 2, 3, 4, 5 };
AtomicIntegerArray atomic = new AtomicIntegerArray(arr);
System.out.println(atomic); // [1, 2, 3, 4, 5]

int index = 3;
System.out.println(atomic.getAndIncrement(index)); // 4
System.out.println(atomic.getAndIncrement(index)); // 5
System.out.println(atomic.get(index)); // 6

System.out.println(atomic.getAndDecrement(index)); // 6
System.out.println(atomic.getAndDecrement(index)); // 5
System.out.println(atomic.get(index)); // 4

System.out.println(atomic.getAndAdd(index, 10)); // 4
System.out.println(atomic.get(index)); // 14

System.out.println(atomic.addAndGet(index, 10)); // 24
System.out.println(atomic.get(index)); // 24

System.out.println(atomic); // [1, 2, 3, 24, 5]
```

### compareAndSet()
+ compareAndSet(index, expect, update)는 현재 값이 예상하는 값(expect)과 동일하다면 update 값으로 변경해주고 true를 리턴해 줍니다. 그렇지 않다면 데이터 변경은 없고 false를 리턴합니다.

```java
int arr[] = { 1, 2, 3, 4, 5 };
AtomicIntegerArray atomic = new AtomicIntegerArray(arr);
System.out.println(atomic); // [1, 2, 3, 4, 5]

int index = 3;
int expected = 3;
int update = 40;

System.out.println(atomic.compareAndSet(index, expected, update)); // false

expected = 4;
System.out.println(atomic.compareAndSet(index, expected, update)); // true

System.out.println(atomic); // [1, 2, 3, 40, 5]
```

## AtomicBoolean
AtomicBoolean는 boolean 자료형을 갖고 있는 wrapping 클래스입니다.  
AtomicBoolean 클래스는 멀티쓰레드 환경에서 동시성을 보장합니다.  

### 객체 생성
+ 초기값은 false이며 인자로 원하는 boolean을 전달할 수 있습니다.

```java
AtomicBoolean atomicBoolean = new AtomicBoolean();
System.out.println(atomicBoolean.get()); // false

AtomicBoolean atomicBoolean2 = new AtomicBoolean(true);
System.out.println(atomicBoolean2.get()); // true
```

### get(), set(), getAndSet()
+ set(boolean) : boolean 값 변경
+ get() : 값 읽기
+ getAndSet(boolean) : 현재 값을 반환하고 업데이트

```java
AtomicBoolean atomicBoolean = new AtomicBoolean();
System.out.println(atomicBoolean.get()); // false

atomicBoolean.set(true);
System.out.println(atomicBoolean.get()); // true


System.out.println(atomicBoolean.getAndSet(false)); // true
System.out.println(atomicBoolean.get()); // false
```

### compareAndSet()
+ compareAndSet(expect, update) : 현재 값이 예상하는 값(expect)과 동일하다면 update 값으로 변경해주고 true를 리턴해 줍니다. 그렇지 않다면 데이터 변경은 없고 false를 리턴합니다.

```java
boolean expected = true;
AtomicBoolean atomicBoolean = new AtomicBoolean(false);
System.out.println(atomicBoolean.compareAndSet(expected, true)); // false
System.out.println(atomicBoolean.get()); // false

atomicBoolean.set(true);
System.out.println(atomicBoolean.compareAndSet(expected, true)); // true
System.out.println(atomicBoolean.get()); // true
```

## AtomicReference
AtomicReference는 V 클래스(Generic)의 객체를 wrapping 클래스입니다.  
AtomicReference 클래스는 멀티쓰레드 환경에서 동시성을 보장합니다.

### 객체 생성
제네릭 클래스로 구현되기 때문에 다양한 클래스를 넣어서 사용할 수 있습니다.  
생성자에 초기값을 전달할 수 있으며, 입력하지 않으면 객체는 null로 초기화됩니다.  
아래는 Integer 객체를 사용했습니다.  

```java
AtomicReference<Integer> atomic = new AtomicReference<>();
System.out.println(atomic.get()); // null

AtomicReference<Integer> atomic2 = new AtomicReference<>(10);
System.out.println(atomic2.get()); // 10
```

### get(), set(), getAndSet()
+ set(V newVal) : 값 세팅
+ get() : 값 읽기
+ getAndSet(V newValue) : 현재의 value를 리턴하고, 인자로 전달된 값으로 업데이트

```java
AtomicReference<Integer> atomic = new AtomicReference<>();
System.out.println(atomic.get()); // null

atomic.set(100);
System.out.println(atomic.get()); // 100

AtomicReference<Integer> atomic2 = new AtomicReference<>(10);
System.out.println(atomic2.getAndSet(20)); // 10
System.out.println(atomic2.get()); // 20
```

### compareAndSet()
+ compareAndSet(expect, update) : 현재 값이 예상하는 값(expect)과 동일하다면 update 값으로 변경해주고 true를 리턴해 줍니다. 그렇지 않다면 데이터 변경은 없고 false를 리턴합니다.

```java
int expected = 20;
AtomicReference<Integer> atomic = new AtomicReference<>(10);
System.out.println(atomic.compareAndSet(expected, 100)); // false
System.out.println(atomic.get()); // 10

atomic.set(20);
System.out.println(atomic.compareAndSet(expected, 100)); // true
System.out.println(atomic.get()); // 100
```

## ArrayBlockingQueue
ArrayBlockingQueue는 BlockingQueue 인터페이스를 구현한 Array로 구현된 BlockingQueue입니다. 
+ Queue를 생성할 때 크기를 설정하여 내부적으로 배열을 사용하기 때문에 크기가 정해져 있기 때문에 무한히 아이템을 추가할 수 없습니다.  
+ 동시성에 안전하여 멀티스레드 환경에서 synchronized 없이 사용 가능합니다.
+ 추가되는 아이템은 순서가 있으며, FIFO(First In First Out) 순서를 따릅니다.
+ BlockingQueue는 Queue에서 아이템을 가져올 때 비어있으면 null을 리턴하지 않고 아이템이 추가될 때까지 기다립니다. 
+ 아이템을 추가할 때 Queue가 가득차 있으면 공간이 생길 때까지 일정 시간 기다릴 수 있고 Exception을 발생시킬수도 있습니다.

### 생성
+ 인자로 Queue의 크기를 전달해서 간단하게 생성할 수 있습니다.
+ 두 번째 인자로 fair을 전달할 수 있습니다.
    - fiar는 기본적으로 false입니다.
    - true라면 멀티스레드에서 이 queue에 접근할 때 Lock을 공정하게 얻습니다.
    - false라면 정해진 규칙 없습니다. 따라서 어떤 스레드는 오랫동안 Lock을 획득하지 못하여 Queue를 사용하지 못하는 현상이 발생할 수 있습니다.
+ 세 번째 인자로 초기값을 전달할 수 있습니다.

```java
int capacity = 10;
ArrayBlockingQueue<Integer> queue = new ArrayBlockingQueue<Integer>(capacity);

int capacity = 10;
ArrayBlockingQueue<Integer> queue = new ArrayBlockingQueue<Integer>(capacity, true);

List<Integer> list = Arrays.asList(10, 20, 30);
int capacity = 10;
ArrayBlockingQueue<Integer> queue = new ArrayBlockingQueue<Integer>(capacity, true, list);
```

### add()
+ add() : 아이템을 추가합니다.

```java
int capacity = 5;
ArrayBlockingQueue<Integer> queue = new ArrayBlockingQueue<Integer>(capacity);

queue.add(1);
System.out.println(queue); // [1]

queue.add(2);
queue.add(3);
System.out.println(queue); // [1, 2, 3]

queue.add(4);
queue.add(5);
System.out.println(queue); // [1, 2, 3, 4, 5]
```
<br>

일반적으로는 크기보다 더 많은 아이템을 추가하려고 하면 Exception이 발생합니다.  
```java
int capacity = 5;
ArrayBlockingQueue<Integer> queue = new ArrayBlockingQueue<Integer>(capacity);

queue.add(1);
queue.add(2);
queue.add(3);
queue.add(4);
queue.add(5);
System.out.println(queue);

if (queue.remainingCapacity() == 0) {
    System.out.println("Queue is full");
} else {
    System.out.println("Queue is not full");
}

try {
    queue.add(6);
} catch (Exception e) {
    e.printStackTrace();
}

// 출력 결과
[1, 2, 3, 4, 5]
Queue is full
java.lang.IllegalStateException: Queue full
	at java.base/java.util.AbstractQueue.add(AbstractQueue.java:98)
	at java.base/java.util.concurrent.ArrayBlockingQueue.add(ArrayBlockingQueue.java:326)
	at atomic.Main.main(Main.java:27)
```
+ remainingCapacity() : Queue의 여유 공간을 리턴해 줍니다. 이것으로 Queue가 가득 찻는지 알 수 있습니다. Full인 상태에서 add()를 추가하면 IllegalStateException이 발생합니다.

### put()
+ put() : add()와 거의 동일하지만 Queue가 full일 때 Exception을 발생시키지 않고 여유 공간이 생길 때까지 무한히 기다립니다.

```java
int capacity = 2;
ArrayBlockingQueue<Integer> queue = new ArrayBlockingQueue<Integer>(capacity);
try {
    queue.put(1);
    System.out.println(queue); // [1]
    queue.put(2);
    System.out.println(queue); // [1,2]
    queue.put(6); // 무한히 대기
    System.out.println(queue);
} catch (InterruptedException e) {
    e.printStackTrace();
}
```

### offer()
+ offer() : add()와 거의 동일하지만 Queue가 full일 때 Exception을 발생시키지 않고 false를 리턴합니다.

```java
int capacity = 2;
ArrayBlockingQueue<Integer> queue = new ArrayBlockingQueue<Integer>(capacity);

System.out.println(queue.offer(1)); // true
System.out.println(queue.offer(2)); // true

boolean success = queue.offer(6); 
System.out.println(success); // false
```
<Br>

+ offer(timeout) : 인자로 전달한 시간 만큼 기다리고, 정재힌 시간 내에 여유 공간이 생기면 추가합니다. 추가하면 true를 리턴하고 못하면 false를 리턴합니다.

```java
int capacity = 2;
ArrayBlockingQueue<Integer> queue = new ArrayBlockingQueue<Integer>(capacity);

try {
    long timeout = 5000;
    System.out.println(queue.offer(1, timeout, TimeUnit.MILLISECONDS)); // true
    System.out.println(queue.offer(2, timeout, TimeUnit.MILLISECONDS)); // true

    boolean success = queue.offer(6, timeout, TimeUnit.MILLISECONDS);
    System.out.println(success); // false
} catch (Exception e) {
    e.printStackTrace();
}
```

### take()
+ take() : Queue에서 아이템을 삭제하고 그 값을 리턴합니다. Queue가 비어있는 경우 InterruptedException가 발생할 수 있기 때문에 예외처리가 필요합니다.

```java
int capacity = 5;
ArrayBlockingQueue<Integer> queue = new ArrayBlockingQueue<Integer>(capacity);

queue.add(1);
queue.add(2);
queue.add(3);
System.out.println(queue); // [1,2,3]

try {
    int head = queue.take();
    System.out.println(head); // 1
    System.out.println(queue); // [2,3]
} catch (InterruptedException e) {
    e.printStackTrace();
}
```
<Br>

Queue가 empty일 때 take()를 호출하면 block되고, 아이템이 추가될 때까지 기다립니다.  
대기 중에 다른 쓰레드에서 아이템이 추가되면 그 값을 리턴합니다.  
대기 중에 Interrupt가 발생할 수 있기 때문에 InterruptedException에 대한 예외처리가 필요합니다.  
```java
int capacity = 5;
ArrayBlockingQueue<Integer> queue = new ArrayBlockingQueue<Integer>(capacity);
try {
    int head = queue.take();
    System.out.println(head); // 무한히 대기
} catch (InterruptedException e) {
    e.printStackTrace();
}
```

### poll()
+ poll(timeout) : 아이템을 가져올 때 Timeout을 설정하여 무한히 기다리는 일이 발생하지 않도록 할 수 있습니다. Timeout이 발생하면 null을 리턴합니다.

```java
int capacity = 5;
ArrayBlockingQueue<Integer> queue = new ArrayBlockingQueue<Integer>(capacity);
try {
    long timeout = 1000;
    Integer head = queue.poll(timeout, TimeUnit.MILLISECONDS);
    System.out.println(head); // null
} catch (InterruptedException e) {
    e.printStackTrace();
}
```


