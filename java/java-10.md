# Java - synchronized 동기화

## Synchronized method
Synchronized method는 __클래스 인스턴스__ 에 lock을 겁니다.  
두 개의 스레드에서 하나의 인스턴스에 접근하는 상황을 만들어 봅시다.
```java
public class Main {

    public static void main(String[] args) {
        A a = new A();
        Thread thread1 = new Thread(() -> {
            a.run("thread1");
        });

        Thread thread2 = new Thread(() -> {
            a.run("thread2");
        });

        thread1.start();
        thread2.start();
    }
}
```
```java
public class A {

    public synchronized void run(String name) {
        System.out.println(name + " lock");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(name + " unlock");
    }
}
```
```java
// 출력 결과
thread1 lock
thread1 unlock
thread2 lock
thread2 unlock
```
접근한 순서대로 lock을 획득하고 반납했기 때문에 thread1, thread2가 차례대로 출력됩니다.
<Br>

이번에는 스레드 두 개가 각각의 다른 인스턴스에 접근하는 경우를 만들어 봅시다.
```java
public class Main {

    public static void main(String[] args) {
        A a1 = new A();
        A a2 = new A();
        Thread thread1 = new Thread(() -> {
            a1.run("thread1");
        });

        Thread thread2 = new Thread(() -> {
            a2.run("thread2");
        });

        thread1.start();
        thread2.start();
    }
}
```
```java
public class A {

    public synchronized void run(String name) {
        System.out.println(name + " lock");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(name + " unlock");
    }
}
```
```java
// 출력 결과
thread2 lock
thread1 lock
thread1 unlock
thread2 unlock
```
이 상황에서는 각 스레드가 서로 다른 인스턴스에 접근하기 때문에 lock을 공유하지 않는다는 것을 확인할 수 있습니다.  
앞서 Synchronized method는 __클래스 인스턴스__ 에 lock을 건다고 표현했습니다.  
이 의미가 '인스턴스 접근 자체에 lock이 걸리는 것인가?' 라고 이해할 수 있지만 그렇지 않습니다.  
한 번 만들어서 확인해 봅시다.
```java
public class Main {

    public static void main(String[] args) throws InterruptedException {
        A a = new A();
        Thread thread1 = new Thread(() -> {
            a.run("thread1");
        });

        Thread thread2 = new Thread(() -> {
            a.print("thread2");
        });

        thread1.start();
        Thread.sleep(500);
        thread2.start();
    }
}
```
```java
public class A {

    public void print(String name){
        System.out.println(name + " hello");
    }

    public synchronized void run(String name) {
        System.out.println(name + " lock");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(name + " unlock");
    }
}
```
```java
// 출력
thread1 lock
thread2 hello
thread1 unlock
```
lock을 획득하는 thread1을 먼저 수행하고 thread1이 진행되는 도중에 thread2에서 synchronized 키워드가 붙지 않은 print 메서드를 호출하도록 했습니다.  
출력 결과를 보면 중간에 hello가 찍힌 것을 확인할 수 있습니다.  
__즉, 인스턴스 접근 자체에 lock이 걸린 것은 아닌 것을 확인할 수 있습니다.__  
<br>

그렇다면 print 메서드에도 synchronized 키워드를 붙이면 어떻게 될까요?
```java
public class Main {

    public static void main(String[] args) throws InterruptedException {
        A a = new A();
        Thread thread1 = new Thread(() -> {
            a.run("thread1");
        });

        Thread thread2 = new Thread(() -> {
            a.print("thread2");
        });

        thread1.start();
        Thread.sleep(500);
        thread2.start();
    }
}
```
```java
public class A {

    public synchronized void print(String name){
        System.out.println(name + " hello");
    }

    public synchronized void run(String name) {
        System.out.println(name + " lock");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(name + " unlock");
    }
}
```
```java
// 출력 결과
thread1 lock
thread1 unlock
thread2 hello
```
synchronized를 붙였더니 앞선 결과와 다르게 thread2는 thread1이 lock을 해제하고 나서야 찍힌 것을 확인할 수 있습니다.  
즉, 동기화가 발생했습니다.  
__정리하자면, 인스턴스에 lock을 거는 synchronized 키워드는 synchronized가 적용된 메서드끼리 일괄적으로 lock을 공유합니다.__

## static synchronized method
static이 붙은 synchronized method는 일반적으로 생각하는 static 성질을 갖으므로 인스턴스가 아닌 __클래스 단위__ 로 lock을 겁니다.  
```java
public class Main {

    public static void main(String[] args) throws InterruptedException {
        A a1 = new A();
        A a2 = new A();
        Thread thread1 = new Thread(() -> {
            a1.run("thread1");
        });

        Thread thread2 = new Thread(() -> {
            a2.run("thread2");
        });

        thread1.start();
        thread2.start();
    }
}
``` 
```java
public class A {

    public static synchronized void run(String name) {
        System.out.println(name + " lock");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(name + " unlock");
    }
}
```
```java
// 출력 결과
thread1 lock
thread1 unlock
thread2 lock
thread2 unlock
```
다른 인스턴스에 접근했지만 lock이 발생한 것을 확인할 수 있습니다.  
__즉, 다른 인스턴스더라도 static 메서드에 synchronized가 붙은 경우 lock을 공유하는 것을 확인할 수 있습니다.__  

<br>

그렇다면 static synchronized method와 synchronized method이 섞여 있다면 어떻게 동작할까요?
```java
public class Main {

    public static void main(String[] args) throws InterruptedException {
        A a1 = new A();
        A a2 = new A();
        Thread thread1 = new Thread(() -> {
            a1.run("thread1");
        });

        Thread thread2 = new Thread(() -> {
            a2.print("thread2");
        });

        thread1.start();
        thread2.start();
    }
}
```
```java
public class A {

    public static synchronized void run(String name) {
        System.out.println(name + " lock");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(name + " unlock");
    }

    public synchronized void print(String name) {
        System.out.println(name + " hello");
    }
}
```
```java
// 출력 결과
thread1 lock
thread2 hello
thread1 unlock
```
thread2가 thread1 사이에 출력된 것으로 보아 인스턴스 단위 lock과 클래스 단위 lock은 공유되지 않는 것을 확인할 수 있습니다.  
__정리하자면, static synchronized method와 synchronized method의 lock은 공유되지 않습니다.__  

## synchronized block
synchronized block은 __인스턴스의 block 단위__ 로 lock을 걸고 lock 객체를 지정해야 합니다.  
```java
public class Main {

    public static void main(String[] args) throws InterruptedException {
        A a = new A();
        Thread thread1 = new Thread(() -> {
            a.run("thread1");
        });

        Thread thread2 = new Thread(() -> {
            a.run("thread2");
        });

        thread1.start();
        thread2.start();
    }
}
```
```java
public class A {

    public void run(String name) {
        synchronized (this){
            System.out.println(name + " lock");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(name + " unlock");
        }
    }
}
```
```java
// 출력 결과
thread1 lock
thread1 unlock
thread2 lock
thread2 unlock
```
block의 인자로 this를 주었습니다.  
this는 해당 인스턴스를 의미하고 위 코드에서는 method 전체가 block 영역으로 감싸져 있으므로 메서드 선언부에 synchronized 키워드를 붙인 것이랑 똑같이 동작합니다.  
실질적인 block 사용 방식은 아래와 같습니다.  
```java
public class A {

    public void run(String name) {

        // 전처리 로직 ....

        // 동기화 시작
        synchronized (this){
            System.out.println(name + " lock");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(name + " unlock");
        }
        // 동기화 해제

        // 후처리 로직 ...
    }
}
```
동기화 전후를 기점으로 로직을 섞을 수 있고 block에 진입할 때만 lock을 획득하고 빠져나오므로 효율적으로 사용이 가능합니다.  
앞서 말했듯이 block에 this를 명시할 경우 method에 synchornized를 붙인 것처럼 인스턴스 단위로 lock이 걸립니다.  
따라서 아래와 같이 각각의 스레드가 서로 다른 인스턴스에 접근할 경우 lock은 공유되지 않습니다.
```java
public class Main {

    public static void main(String[] args) throws InterruptedException {
        A a1 = new A();
        A a2 = new A();
        Thread thread1 = new Thread(() -> {
            a1.run("thread1");
        });

        Thread thread2 = new Thread(() -> {
            a2.run("thread2");
        });

        thread1.start();
        thread2.start();
    }
}
```
```java
public class A {

    public void run(String name) {
        synchronized (this){
            System.out.println(name + " lock");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(name + " unlock");
        }
    }
}
```
```java
// 출력 결과
thread1 lock
thread2 lock
thread2 unlock
thread1 unlock
```
<br>

지금까지는 block에 this를 사용해서 Synchornized를 메서드 선언부에 붙인 것과 별반 다를게 없이 사용했습니다.  
이제는 block에 자원을 명시하고 사용해 봅시다.
```java
public class Main {

    public static void main(String[] args) {
        A a = new A();
        Thread thread1 = new Thread(() -> {
            a.run("thread1");
        });

        Thread thread2 = new Thread(() -> {
            a.run("thread2");
        });

        Thread thread3 = new Thread(() -> {
            a.print("자원 B와 관련 없는 thread3");
        });

        thread1.start();
        thread2.start();
        thread3.start();
    }
}
```
```java
public class A {

    B b = new B();

    public void run(String name) {
        synchronized (b){
            System.out.println(name + " lock");
            b.run();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(name + " unlock");
        }
    }

    public synchronized void print(String name) {
        System.out.println(name + " hello");
    }
}
```
```java
public class B extends Thread {

    @Override
    public synchronized void run() {
        System.out.println("B lock");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("B unlock");
    }
}
```
```java
// 출력 결과
thread1 lock
자원 B와 관련 없는 thread3 hello
B lock
B unlock
thread1 unlock
thread2 lock
B lock
B unlock
thread2 unlock
```
thread1, 2는 자원 b를 lock하는 run 메서드를 호출했고 thread3는 synchornized 키워드가 붙은 메서드를 호출했습니다.  
호출 결과를 보면 thread1, 2는 순서차적으로 수행되었고 thread3는 중간에 껴서 수행된 것을 확인할 수 있습니다.  
__즉, 인스턴스 단위 lock과 B를 block한 lock은 공유되지 않고 별도로 관리되는 것을 확인할 수 있습니다.__  

<Br>

이번에는 block에 인스턴스가 아니라 class를 명시해봅시다. 
```java
public class Main {

    public static void main(String[] args) {
        A a = new A();
        Thread thread1 = new Thread(() -> {
            a.run("thread1");
        });

        Thread thread2 = new Thread(() -> {
            a.run("thread2");
        });

        thread1.start();
        thread2.start();
    }
}
```
```java
public class A {

    B b = new B();

    public void run(String name) {
        synchronized (B.class){
            System.out.println(name + " lock");
            b.run();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(name + " unlock");
        }
    }

    public synchronized void print(String name) {
        System.out.println(name + " hello");
    }
}
```
```java
public class B extends Thread {

    @Override
    public synchronized void run() {
        System.out.println("B lock");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("B unlock");
    }
}
```
```java
// 출력 결과
thread1 lock
B lock
B unlock
thread1 unlock
thread2 lock
B lock
B unlock
thread2 unlock
```
block의 인자로 인스턴스가 아닌 .class를 주었습니다.  
출력 결과를 보면 lock을 공유하고 있는 것을 확인할 수 있습니다.  
__정리하자면, block에는 객체를 지정할 수도 있고 class 형식으로 넘기면 해당 class에 lock을 걸 수 있습니다.__  

## static synchronized block
static 메서드 안에도 synchronized block을 사용할 수 있는데 이때는 this와 같이 현재 객체를 가르키는 표현은 사용할 수 없습니다.  
static synchronized method 방식과의 차이는 lock 객체를 지정하고 block 범위를 한정 지을 수 있다는 점입니다.  
클래스 단위로 lock을 공유한다는 점은 같습니다.  
```java
public class Main {

    public static void main(String[] args) {
        A a1 = new A();
        A a2 = new A();
        Thread thread1 = new Thread(() -> {
            a1.run("thread1");
        });

        Thread thread2 = new Thread(() -> {
            a2.run("thread2");
        });

        thread1.start();
        thread2.start();
    }
}
```
```java
public class A {

    public static void run(String name) {
        synchronized (A.class){
            System.out.println(name + " lock");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(name + " unlock");
        }
    }
}
```
```java
// 출력 결과
thread1 lock
thread1 unlock
thread2 lock
thread2 unlock
```

## 동기화 순서
synchronized를 통해 lock을 물고 있을 때 여러 개의 스레드가 접근 요청을 한다고 가정해봅시다.  
이후 lock이 풀리고 나면 과연 접근한 순서대로 스레드가 접근하게 되는 것일까요?  
```java
public class Main {

    public static void main(String[] args) throws InterruptedException {
        A a = new A();

        Thread[] threads = new Thread[5];

        for (int i = 0; i < threads.length; i++) {
            final int idx = i;
            threads[i] = new Thread(() -> {
                a.run("thread" + idx);
            });
        }

        for (Thread thread : threads) {
            thread.start();
            Thread.sleep(100);
        }
    }
}
```
```java
public class A {

    public synchronized void run(String name) {

        System.out.println(name + " lock");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(name + " unlock");

    }
}
```
```java
// 출력 결과
thread0 lock
thread0 unlock
thread4 lock
thread4 unlock
thread3 lock
thread3 unlock
thread2 lock
thread2 unlock
thread1 lock
thread1 unlock
```
__첫 번째 0이 진입한 이후에는 동기화 순서가 보장되지 않는 것을 확인할 수 있습니다.__  

## 정리
+ synchronized method
    - 인스턴스 단위 lock
    - 동일한 인스턴스 내 synchronized 키워드가 적용된 메서드끼리 lock을 공유
+ synchronized block
    - this를 명시하면 synchronized method와 동일하게 동작하면서 synchronized method와 lock을 공유
    - 특정 객체를 명시하면 해당 객체에만 특정 lock을 걸면서 __해당 객체에 lock을 거는 block끼리만__ lock을 공유        
    - .class 형식 명시하면 해당 클래스에만 특정 lock을 걸면서 __해당 클래스에 lock을 거는 block끼리만__ lock을 공유
+ static synchronized method
    - 클래스 단위 lock
    - static synchronized와 synchronized가 혼용되어있을 때 각자의 lock으로 관리
+ static synchronized block
    - 클래스 단위 lock
    - block의 인자로 정적 인스턴스나 클래스만 사용
+ __synchornized는 Thread의 동기화 순서를 보장하지 않는다.__


## Lock, Condition
동기화 처리하는 블록을 Synchronized로 묶게 되면 해당 블록에는 하나의 스레드만 접근할 수 있습니다.    
만약 하나의 스레드가 들어가서 특정 조건이 만족될 때까지 반복 루프를 돌게 될 경우 빠져나오지 않는 상황이 발생할 수 있습니다.  
이런 상황에서 동기화의 효율을 높이기 위해 wait, notify를 사용합니다.  
이 경우에는 안에서 wait을 호출하여 객체의 lock을 풀고 루프를 돌고 있는 스레드를 해당 객체의 waiting pool에 넣습니다.  
그리고 조건을 만족시키도록 하는 로직에서 작업 후 notify를 호출하여 대기 중인 스레드 중 하나를 깨워줍니다.  
notify를 통해 스레드를 깨울 때는 랜덤하게 pool에 있는 것 중 하나를 깨우기 때문에 일반적으로는 notifyAll를 사용해서 전체를 깨웁니다.  
하지만 이렇게 로직을 짜다보면 wait과 notify가 어떤 것을 위한 것인지 구분하기 어렵고 같은 메서드 내에서만 lock을 걸 수 있다는 제약이 불편합니다.  
이에 대한 대안으로 나온 것이 Lock, Condition입니다.  
정리하자면, synchronized는 블록구조를 사용하여 메서드안에 임계영역의 시작과 끝이 있고 알아서 lock을 회수해주는 반면에 Lock은 lock, unlock 메서드로 시작과 끝을 명시하기 때문에 명확하고 임계 영역을 여러 메서드에서 나눠서 작성할 수 있습니다.  


<br>

concurrent.locks 패키지는 내부적으로 synchronized를 사용하여 구현되어 있지만 더욱 유연하고 세밀하게 처리하기 위해 사용됩니다.  
+ __Lock__ : 공유 자원에 한 번에 한 쓰레드만 read, write가 수행 가능하도록 제공하는 인터페이스
    - __ReentrantLock__ : Lock의 구현체로 임계 영역의 시작과 종료 지점을 직접 명시할 수 있습니다.
+ __ReadWriteLock__ : 공유 자원에 여러 개의 스레드가 read 가능하고, write는 한 스레드만 가능한 인터페이스로 읽기를 위한 lock과 쓰기를 위한 lock을 별도로 제공합니다.
    - __ReentrantReadWriteLock__ : ReadWriteLock의 구현체
+ StampedLock
    - __ReentrantReadWriteLock에 낙관적인 lock기능을 추가한 것입니다.__
    - lock을 걸거나 해지할 때 스탬프(long 타입의 정수)를 사용하여 일긱와 쓰기를 위한 lock외에 낙관적인 lock이 추가된 것입니다.
    - 기존에는 읽기 lock이 걸려있으면, 쓰기 lock을 얻기 위해서는 읽기 lock이 풀릴 때까지 기다려야 하지만, 낙관적인 읽기 lock은 쓰기 lock에 의해 바로 풀립니다. 따라서 낙관적인 읽기에 실패하면 읽기 lock을 얻어와서 다시 읽어야 합니다.
    - __무조건 읽기 lock을 걸지 않고, 쓰기와 읽기가 충돌할 때만 쓰기가 끝난 후에 읽기 lock을 겁니다.__

### ReentrantLock 예시
```java
class SomeClass { 

    private ReentrantLock lock = new ReentrantLock(); // 인자로 true 주면 공정성 부여
    private Condition forCook = lock.newCondition();
    private Condition forCust = lock.newCondition();

    // 단편적인 예시
    public void add(String dish){
        lock.lock();
        try{
            while(dishes.size() >= MAX_FOOD){
                String name = Thread.currentThread().getName();
                System.out.println(name+" is waiting");
                try{
                    forCook.await(); // wait(); // 음식이 가득찼으므로 COOK쓰레드를 기다리게 한다.
                    Thread.sleep(500);
                }catch (InterruptedException e){}
            }
            dishes.add(dish);
            forCust.signal(); // notify(); // 음식이 추가되면 기다리고 있는 CUST를 깨우게 함
            System.out.println("Dishes : " + dishes.toString());
        }finally {
            lock.unlock();  // 쓰레드의 락을 풉니다.(동기화 끝지점) 
        }
    }

    // lockerCondition.await(); //기존의 동기화에서 wait(); 
    // lockerCondition.signal(); //기존의 동기화에서 notify(); 
    // lockerCondition.signalAll(); //기존의 동기화에서 notifyAll(); 
}
```
synchronized와 달리 확실히 어떤 것에 대한 lock인지 명확성에 차이가 보이지만, 공정성에도 차이가 있습니다.  
synchronized는 lock을 풀고 다음 스레드가 차지할 때 순서대로 실행되지 않습니다.  
다른 스레드들에게 우선순위가 밀려 자원을 계속해서 할당받지 못하는 기아상태가 발생하게 되는데 이를 불공정하다고 합니다.  
reentrantLock의 경우에는 생성자를 통해 fair/nonFair를 설정할 수 있어 경쟁이 발생했을 때 가장 오랫동안 기다린 스레드에게 lock을 제공합니다.  
하지만 락을 요청하는 시간 간격이 긴 경우가 아니라면, 스레드를 공정하게 관리하는 것보다는 불공정하게 관리하는 것이 성능이 우수하기 때문에 불공정 방식이 더 많이 사용됩니다.  
<br>

이외에도 tryLock() 메서드가 존재합니다.  
일반 lock 메서드와 달리 다른 스레드에 의해 lock이 걸려 있으면 lock을 얻으려고 기다리지 않거나 지정된 시간만큼만 기다리도록 할 수 있습니다.  
lock을 얻으면 true를 반환하고, 얻지 못하면 false를 반환합니다.  
일반 lock 메서드는 lock을 얻을 때까지 스레드를 블락시키므로 스레드의 응답성이 나빠질 수 있습니다.  
따라서 응답이 중요한 경우 tryLock을 이용해서 지정된 시간동안 lock을 얻지 못하면 다시 작업을 시도할 것인지 포기할 것인지 결정해주는 것이 좋습니다.  
그리고 tryLock 메서드는 interruptException을 발생시킬 수 있는데, 이것은 지정된 시간동안 lock을 얻으려고 기다리는 중에 interrupt에 의해 작업을 취소할 수 있도록 코드를 작성할 수 있다는 의미입니다.  
제공하는 메서드를 정리하면 다음과 같습니다.
+ lock() : 락 획득하기
+ unlock() : 락 해제
+ tryLock() : 일정 시간 기다리거나 즉시 상태를 확인하고 true, false 리턴, true 인 경우 락을 획득
+ isHeldByCurrentThread() : 현재 Thread가 Lock을 얻었는지 확인
+ hasQueuedThreads() : 해당 객체의 Lock을 얻기위해 기다리는 Threads가 있는지 확인
+ getOwner() : 지금 Lock을 갖고 있는 Thread를 Return 



### ReadWriteLock 예시
```java
public class ReadWriteList<E> {
    private List<E> list = new ArrayList<>();
    private ReadWriteLock rwLock = new ReentrantReadWriteLock();
 
    public ReadWriteList(E... initialElements) {
        list.addAll(Arrays.asList(initialElements));
    }
 
    public void add(E element) {
        Lock writeLock = rwLock.writeLock();
        writeLock.lock(); // 쓰기 잠금
 
        try {
            list.add(element);
        } finally {
            writeLock.unlock(); // 쓰기 잠금 해제
        }
    }
 
    public E get(int index) {
        Lock readLock = rwLock.readLock();
        readLock.lock(); // 읽기 잠금
 
        try {
            return list.get(index);
        } finally {
            readLock.unlock(); // 읽기 잠금 해제
        }
    }

}
```

### StampedLock 예시
```java
public class Member {

    private int balance;
    private StampedLock lock = new StampedLock();

    public int getBalance() {
        long stamp = lock.tryOptimisticRead(); // 낙관적 읽기 lock 시작 -> 정수 가져오기
        
        int curBalance = this.balance; // 공유 데이터 balance 읽기
        
        if(!lock.validate(stamp)){ // 쓰기 lock에 의해 낙관적 읽기 lock이 풀렸는지 확인
            stamp = lock.readLock(); // lock이 풀렸기 때문에 읽기 lock을 얻기 시도
            
            try {
                curBalance = this.balance; // 공유 데이터 다시 읽기
            } finally {
                lock.unlockRead(stamp); // 읽기 락 해제
            }
        }        
        
        return curBalance;
    }
}
```

