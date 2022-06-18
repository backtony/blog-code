# Java - ExecutorService와 ForkJoinPool


## ExecutorService
java.util.concurrent.Executors와 java.util.concurrent.ExecutorService를 이용하면 간단히 쓰레드풀을 생성하여 병렬처리를 할 수 있습니다.  
ExecutorService에 Task만 지정해주면 알아서 ThreadPool을 이용해 Task를 실행하고 관리합니다.  

### 생성
Executors는 ExecutorService 객체를 생성하며, 다음 메소드를 제공하여 쓰레드 풀을 개수 및 종류를 정할 수 있습니다.
```java
// 인자 개수만큼 고정된 스레드를 생성하는 스레드 풀
ExecutorService executor = Executors.newFixedThreadPool(int n);

// 필요할 때 필요한 만큼 스레드를 무한정 생성하고 60초간 작업이 없다면 pool에서 제거하는 스레드풀
// 무한정으로 스레드를 생성하기 때문에 조심해서 사용해야 한다.
ExecutorService executor = Executors.newCachedThreadPool();

// 스레드 1개인 ExecutorService 리턴
ExecutorService executor = Executors.newSingleThreadExecutor();

// 일정 시간 뒤에 실행되는 작업이나, 주기적으로 수행되는 작업이 있다면 사용하는 것
ExecutorService executor = Executors.newScheduledThreadPool(int n);
```

ExecutorService에서 제공하는 API는 다음과 같습니다.
+ 작업 할당을 위한 메서드
    - execute(runnableTask) : 리턴타입이 void로 Task의 실행 결과나 상태를 알 수 없다.
    - submit(callableTask) : task를 할당하고 Future 타입의 결과값을 받는다. 결과 리턴이 되어야 하므로 Callable 구현체를 인자로 넣는다.
    - invokeAny(callableTasks) : Task를 Collection에 넣어서 인자로 넘긴다. 실행에 성공한 Task 중 하나의 리턴값을 반환한다.
    - invokeAll(callableTasks) : Task를 Collection에 넣어서 인자로 넘긴다. 모든 Task의 리턴값을 List\<Future\<>> 형태로 반환한다.
+ 종료 메서드
    - shutdown() : 더 이상 Task를 받지 않고 처리 중인 Task를 모두 완료한 뒤 스레드풀을 종료한다.
    - shutdownNow() : 즉시 종료시도하지만 모든 Thread가 동시에 종료되는 것을 보장하지는 않고 실행되지 않은 Task를 반환
    - awaitTermination() : 이미 수행 중인 Task가 지정된 시간동안 끝나기를 기다리고 지정된 시간 내에 끝나지 않으면 false를 리턴


### 예시
#### newFixedThreadPool
ExecutorService의 submit 메서드르 작업을 추가하면 됩니다.
```java
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ExecutorServiceTest {

    public static void main(String args[]) throws InterruptedException {
        // 4개의 스레드를 가진 스레드 풀 생성
        ExecutorService executor = Executors.newFixedThreadPool(4);

        // submit 메서드로 멀티스레드로 처리할 작업을 예약
        // 예약과 동시에 먼저 생성된 4개의 스레드는 작업을 처리
        executor.submit(() -> {
            String threadName = Thread.currentThread().getName();
            System.out.println("Job1 " + threadName);
        });
        executor.submit(() -> {
            String threadName = Thread.currentThread().getName();
            System.out.println("Job2 " + threadName);
        });
        executor.submit(() -> {
            String threadName = Thread.currentThread().getName();
            System.out.println("Job3 " + threadName);
        });
        executor.submit(() -> {
            String threadName = Thread.currentThread().getName();
            System.out.println("Job4 " + threadName);
        });

        // 더이상 ExecutorService에 Task를 추가할 수 없습니다.        
        // 실행중인 모든 task가 수행되었다면 스레드풀을 종료합니다.
        executor.shutdown();

        // shutdown() 호출 전에 등록된 Task 중에 아직 완료되지 않은 Task가 있을 수 있습니다.
        // Timeout을 20초 설정하고 완료되기를 기다립니다.
        // 20초 전에 완료되면 true를 리턴하며, 20초가 지나도 완료되지 않으면 false를 리턴합니다.
        if (executor.awaitTermination(20, TimeUnit.SECONDS)) {
            System.out.println(LocalTime.now() + " All jobs are terminated");
        } else {
            System.out.println(LocalTime.now() + " some jobs are not terminated");

            // 모든 Task를 강제 종료합니다.
            executor.shutdownNow();
        }

        System.out.println("end");
    }
}

// 출력 결과
Job2 pool-1-thread-2
Job1 pool-1-thread-1
Job4 pool-1-thread-4
Job3 pool-1-thread-3
14:25:54.144895 All jobs are terminated
end
```
shutdown()은 더 이상 쓰레드풀에 작업을 추가하지 못하도록 합니다.  
그리고 처리 중인 Task가 모두 완료되면 쓰레드풀을 종료시킵니다.  
awaitTermination()은 이미 수행 중인 Task가 지정된 시간동안 끝나기를 기다립니다.  
지정된 시간 내에 끝나지 않으면 false를 리턴하며, 이 때 shutdownNow()를 호출하면 실행 중인 Task를 모두 강제로 종료시킬 수 있습니다.  



#### SingleThreadExecutor
SingleThreadExecutor는 Thread가 1개인 Executor입니다.  
1개이기 때문에 작업을 예약한 순서대로 처리를 합니다. 동시성(Concurrency)을 고려할 필요가 없습니다.
```java
public class ExecutorServiceTest {

    public static void main(String args[]) throws InterruptedException {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            String threadName = Thread.currentThread().getName();
            System.out.println("Job1 " + threadName);
        });
        executor.submit(() -> {
            String threadName = Thread.currentThread().getName();
            System.out.println("Job2 " + threadName);
        });


        executor.shutdown();
        executor.awaitTermination(20, TimeUnit.SECONDS);
        System.out.println("end");
    }
}

// 출력 결과
Job1 pool-1-thread-1
Job2 pool-1-thread-1
end
```
로그를 보면 순서대로 처리된 것을 확인할 수 있습니다.

#### ScheduledThreadPoolExecutor
만약 어떤 작업을 일정 시간 지연 후에 수행하거나, 일정 시간 간격으로 주기적으로 실행해야 한다면, ScheduledThreadPoolExecutor를 고려해보는 것이 좋습니다.  
ScheduledThreadPoolExecutor 다음 4개의 메소드들을 제공합니다.  
+ schedule(Runnable command, long delay, TimeUnit unit) : 작업을 일정 시간 뒤에 한번 실행합니다.
+ schedule(Callable command, long delay, TimeUnit unit) : 작업을 일정 시간 뒤에 한번 실행하고, 그 결과를 리턴합니다.
+ scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) : 작업을 일정 시간 간격으로 반복적으로 실행시킵니다.
+ scheduleWithFixedDelay(Runnable command, long initialDelay, long period, TimeUnit unit) : 작업이 완료되면 일정 시간 뒤에 다시 실행시킵니다. scheduleAtFixedRate()와 다른 점은 작업 종료시점이 기준이라는 것입니다.

```java
public class ScheduledThreadPoolExecutorExample {

    public static void main(String[] args) {

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);
        Runnable runnable = () -> System.out.println("Runnable task : " + LocalTime.now());
        int delay = 3;

        // Job을 스케쥴링합니다.
        System.out.println("Scheduled task : " + LocalTime.now() );
        // 일정 시간 delay 후 job 실행
        executor.schedule(runnable, delay, TimeUnit.SECONDS);
    }
}
```
```java
public class ScheduledThreadPoolExecutorExample {

    public static void main(String[] args) throws ExecutionException, InterruptedException {

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);
        Callable<String> callable =  () -> "Callable task : " + LocalTime.now();
        int delay = 3;

        // 일정 시간 뒤에 job 실행
        // Callable을 인자로 받기 때문에 Future로 리턴하고 get으로 값을 꺼낼 수 있습니다.
        ScheduledFuture<String> future =
            executor.schedule(callable, delay, TimeUnit.SECONDS);

        // 결과가 리턴될 때 까지 기다립니다.
        String result = future.get();
        System.out.println(result);
    }
}
```
```java
public class ScheduledThreadPoolExecutorExample {

    public static void main(String[] args) throws ExecutionException, InterruptedException {

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);
        Runnable runnable = () -> {
            System.out.println("++ Repeat task : " + LocalTime.now());
            sleepSec(3);
            System.out.println("-- Repeat task : " + LocalTime.now());
        };
        int initialDelay = 2;
        int delay = 3;

        // 일정 시간 간격으로 실행
        // initialDelay는 처음 실행될 때까지 기다리는 시간
        // 완료되는 시간과 무관하게 일정 delay 후 다시 job이 실행된다.
        executor.scheduleAtFixedRate(
            runnable, initialDelay, delay, TimeUnit.SECONDS);
    }

    private static void sleepSec(int sec) {
        try {
            TimeUnit.SECONDS.sleep(sec);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
```
```java
public class ScheduledThreadPoolExecutorExample4 {

    public static void main(String[] args) throws ExecutionException, InterruptedException {

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);
        Runnable runnable = () -> {
            System.out.println("++ Repeat task : " + LocalTime.now());
            sleepSec(3);
            System.out.println("-- Repeat task : " + LocalTime.now());
        };
        int initialDelay = 2;
        int delay = 3;

        // job이 완료된 후, 완료된 시점을 기준으로 일정 시간 뒤에 다시 job을 실행
        executor.scheduleWithFixedDelay(
            runnable, initialDelay , delay, TimeUnit.SECONDS);
    }

    private static void sleepSec(int sec) {
        try {
            TimeUnit.SECONDS.sleep(sec);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
```


## ForkJoinPool
ForkJoinPool은 ExecutorService와 비슷합니다.  
Thread Pool을 생성하여 여러 작업을 병렬처리로 수행할 수 있습니다.  
ForkJoinPool가 조금 다른 부분은 Task의 크기에 따라 분할(Fork)하고, 분할된 Task가 처리되면 그것을 합쳐(Join) 리턴해 줍니다. 마치 분할정복법(Divide And Conquer) 알고리즘처럼 동작합니다.  

### Fork, Join

![그림1](https://backtony.github.io/assets/img/post/interview/java-4.PNG)  
+ Fork
    - Task를 분할하여 다른 스레드에서 처리시킨다는 의미입니다.
    - 위 그림처럼 하나의 Task를 작은 여러 Task로 나누고, 여러 스레드에 Task를 할당해 줍니다.
+ Join
    - 다른 스레드에서 처리된 결과를 기다렸다가 합친다는 의미입니다.
    - 위 그림처럼 Parent는 Child에서 처리되는 Task가 완료될 때까지 기다린 후 결과를 합쳐 더 상위의 Parent로 전달합니다.


### RecursiveAction과 RecursiveTask
RecursiveAction과 RecursiveTask는 Task를 대표하는 클래스입니다.  
ForkJoinPool에서 어떤 Task를 처리하려면 이 두개의 클래스를 이용해야 합니다.  
이 두 클래스의 차이점은 다음과 같습니다.  
+ RecursiveAction: 리턴 값이 없는 Task입니다. 리턴 값이 필요하지 않는 Task라면 이 클래스로 Task를 정의하면 됩니다.
+ RecursiveTask: 리턴 값이 있는 Task입니다. Parent는 Child Task의 리턴 값을 기다려 합친 후 상위 Parent로 전달합니다.

```java
// forkJoinPool 생성 방식 -> 인자로 생성할 스레드 개수 할당
ForkJoinPool forkJoinPool = new ForkJoinPool(4);
```

#### RecursiveAction
Task가 실행되면 compute()가 호출됩니다.  
Task를 분할하고 싶다면 RecursiveTask를 생성하고 fork()를 호출하여 다른 쓰레드에서 작업이 처리되도록 합니다.

```java
public class MyRecursiveAction extends RecursiveAction {

    private long workLoad = 0;

    public MyRecursiveAction(long workLoad) {
        this.workLoad = workLoad;
    }

    // 테스크 실행 함수
    @Override
    protected void compute() {
        String threadName = Thread.currentThread().getName();

        // 처리 작업이 16개보다 많으면 분할 작업
        if(this.workLoad > 16) {
            System.out.println("[" + LocalTime.now() + "][" + threadName + "]"
                    + " Splitting workLoad : " + this.workLoad);
            sleep(1000);
            List<MyRecursiveAction> subtasks =
                    new ArrayList<MyRecursiveAction>();

            subtasks.addAll(createSubtasks());

            // 분할된 테스크를 다른 스레드에서 처리하도록 fork 호출
            for(RecursiveAction subtask : subtasks){
                subtask.fork();
            }

        } else { // 나눠도 되지 않는 경우, 현재 스레드에서 해당 작업 실행, 실질적으로 수행할 작업 명시
            System.out.println("[" + LocalTime.now() + "][" + threadName + "]"
                    + " Doing workLoad myself: " + this.workLoad);
        }
    }

    // 테스크 절반으로 분할
    private List<MyRecursiveAction> createSubtasks() {
        List<MyRecursiveAction> subtasks =
                new ArrayList<MyRecursiveAction>();

        MyRecursiveAction subtask1 = new MyRecursiveAction(this.workLoad / 2);
        MyRecursiveAction subtask2 = new MyRecursiveAction(this.workLoad / 2);

        subtasks.add(subtask1);
        subtasks.add(subtask2);

        return subtasks;
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
위 코드를 보시면 작업에 부하를 주기 위해 sleep() 코드를 추가하였습니다.  
바로 종료되면 동일 쓰레드에서 Task가 연달아 실행될 수 있기 때문에 병렬처리되는 것처럼 보이지 않을 수 있습니다.  
Task이름과 시간을 출력하여 어떤 쓰레드에서 어떤 작업이 언제 처리되는지 쉽게 볼 수 있습니다.  
compute()에서는 workload가 16보다 클 때 Task를 나누고 16이하면 더 이상 나누지 않고 그 쓰레드에서 처리하도록 정의하였습니다.  
```java
ForkJoinPool forkJoinPool = new ForkJoinPool(4);

MyRecursiveAction myRecursiveAction = new MyRecursiveAction(128); // 128개의 처리 작업
forkJoinPool.invoke(myRecursiveAction);

// Just wait until all tasks done
forkJoinPool.awaitTermination(5, TimeUnit.SECONDS);
```
위와 같이 forkJoinPool.invoke()으로 RecursiveAction을 인자로 전달하고 처리되도록 할 수 있습니다.

#### RecursiveTask
RecursiveTask는 리턴 값이 있는 Task입니다.  
RecursiveAction과 실행 방법은 동일하지만 처리된 결과를 리턴 받기 위해, Parent는 join()으로 Child의 Task가 완료될 때까지 기다립니다.  

```java
public class MyRecursiveTask extends RecursiveTask<Long> {

    private long workLoad = 0;

    public MyRecursiveTask(long workLoad) {
        this.workLoad = workLoad;
    }

    protected Long compute() {
        String threadName = Thread.currentThread().getName();

        // 처리 작업이 16개보다 많으면 분할 작업
        if(this.workLoad > 16) {
            System.out.println("[" + LocalTime.now() + "][" + threadName + "]"
                    + " Splitting workLoad : " + this.workLoad);
            sleep(1000);
            List<MyRecursiveTask> subtasks =
                    new ArrayList<MyRecursiveTask>();
            subtasks.addAll(createSubtasks());

            // 분할된 테스크를 다른 스레드에서 처리하도록 fork 호출
            for(MyRecursiveTask subtask : subtasks){
                subtask.fork();
            }

            long result = 0;
            for(MyRecursiveTask subtask : subtasks) {
                result += subtask.join(); // 각 테스크의 결과값 기다림
                System.out.println("[" + LocalTime.now() + "][" + threadName + "]"
                        + "Received result from subtask");
            }
            return result;

        } else { // 나눠도 되지 않는 경우, 현재 스레드에서 해당 작업 실행, 실질적으로 수행할 작업 명시
            sleep(1000);
            System.out.println("[" + LocalTime.now() + "]["
                    + " Doing workLoad myself: " + this.workLoad);
            return workLoad * 3;
        }
    }

    // 테스크 분할
    private List<MyRecursiveTask> createSubtasks() {
        List<MyRecursiveTask> subtasks =
                new ArrayList<MyRecursiveTask>();

        MyRecursiveTask subtask1 = new MyRecursiveTask(this.workLoad / 2);
        MyRecursiveTask subtask2 = new MyRecursiveTask(this.workLoad / 2);

        subtasks.add(subtask1);
        subtasks.add(subtask2);

        return subtasks;
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
```java
ForkJoinPool forkJoinPool = new ForkJoinPool(4);

MyRecursiveTask myRecursiveTask = new MyRecursiveTask(128);
long mergedResult = forkJoinPool.invoke(myRecursiveTask);
System.out.println("mergedResult = " + mergedResult);

// Just wait until all tasks done
forkJoinPool.awaitTermination(5, TimeUnit.SECONDS);
```
지금까지 forkJoinPool의 invoke 메서드로 인자를 넘겨서 실행시켰지만 submit 메서드에 인자로 넘겨줘도 됩니다.  
submit 메서드에 넘기게 되면 Future을 리턴받습니다.  
```java
ForkJoinPool forkJoinPool = new ForkJoinPool(4);
MyRecursiveTask myRecursiveTask = new MyRecursiveTask(128);
Future<Long> future = forkJoinPool.submit(myRecursiveTask);

System.out.println("Do something....");

System.out.println("mergedResult = " + future.get());

forkJoinPool.awaitTermination(5, TimeUnit.SECONDS);
```


## Future
Future는 비동기적인 연산의 결과를 표현하는 클래스입니다.  
Future를 이용하면 멀티쓰레드 환경에서 처리된 어떤 데이터를 다른 쓰레드에 전달할 수 있습니다.  
Future 내부적으로 Thread-Safe 하도록 구현되었기 때문에 synchronized block을 사용하지 않아도 됩니다.  

```java
ExecutorService executor = Executors.newSingleThreadExecutor();

// ExecutorService의 submit 메서드는 리턴값을 future로 감싸서 반환
Future<Integer> future = executor.submit(() -> {
    System.out.println(LocalTime.now() + " Starting runnable");
    Integer sum = 1 + 1;
    Thread.sleep(3000);
    return sum;
});

System.out.println(LocalTime.now() + " Waiting the task done");
Integer result = future.get(); // 꺼내기
System.out.println(LocalTime.now() + " Result : " + result);
```
submit()으로 Callable을 전달하면, 인자로 전달된 Callable을 수행하고 리턴값을 Future로 감싸서 아직 값이 설정되지 않은 상태로 리턴해줍니다.  
future.get()는 Future 객체에 어떤 값이 설정될 때까지 기다립니다.  
submit()에 전달된 Callable이 어떤 값을 리턴하면 그 값을 Future에 설정합니다.  
Future에 데이터가 set되지 않으면 Future.get()을 호출한 Thread는 무한정 대기하게 됩니다.  
따라서 Future.get()에 timeout을 설정하여 일정 시간 내에 응답이 없으면 리턴하여 다음 작업을 처리하도록 세팅해줘야 합니다.  
```java
ExecutorService executor
        = Executors.newSingleThreadExecutor();

Future<Integer> future = executor.submit(() -> {
    System.out.println(LocalTime.now() + " Starting runnable");
    Integer sum = 1 + 1;
    Thread.sleep(4000);
    System.out.println(LocalTime.now() + " Exiting runnable");
    return sum;
});

System.out.println(LocalTime.now() + " Waiting the task done");
Integer result = null;
try {
    result = future.get(2000, TimeUnit.MILLISECONDS); // 2초 이내 응답이 없으면 TimeoutException 발생
} catch (TimeoutException e) {
    System.out.println(LocalTime.now() + " Timed out");
    result = 0;
}
System.out.println(LocalTime.now() + " Result : " + result);
```
<br>

한 가지 예시만 더 살펴보겠습니다.  
```java
public class ExecutorServiceTest {

    public static void main(String args[]) {
        // 사용가능한 코어 개수만큼 스레드 풀 만드는 방법
        final int maxCore = Runtime.getRuntime().availableProcessors();
        final ExecutorService executor = Executors.newFixedThreadPool(maxCore);

        final List<Future<String>> futures = new ArrayList<>();

        for (int i = 1; i < 5; i++) {
            final int index = i;

            // future로 반환되는 return문을 리스트에 저장
            futures.add(executor.submit(() -> {
                System.out.println("finished job" + index);
                return "job" + index + " " + Thread.currentThread().getName(); 
            }));
        }

        
        for (Future<String> future : futures) {
            String result = null;
            try {
                result = future.get(); // 해당 스레드의 작업이 끝날 때까지 대기하고 끝나면 결과값을 받아온다.
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            System.out.println(result);
        }

        executor.shutdownNow(); // 종료
    }
}
```
future.get()은 스레드의 작업이 종료될 때 까지 기다립니다.   
List에 future를 Job1에서 Job4까지의 작업을 순서대로 추가했기 때문에, For문에서 1~4 작업을 순서대로 기다립니다.  
그래서 로그를 출력해보면 순서대로 출력이 됩니다.  
Future에 대한 for문이 끝나면 ExecutorService는 필요가 없기 때문에 바로 종료할 수 있습니다.  
Runtime.getRuntime().availableProcessors()는 현재 사용가능한 core 개수를 리턴해 줍니다.  
현재 PC(장치)의 사용가능한 Core 개수를 알 수 있기 때문에 효율적으로 쓰레드를 생성할 수 있습니다.  


## BlockingQueue
사실 위의 Future에 대한 코드를 보면 비효율적인 부분이 있습니다.  
첫번째 작업이 늦게 처리된다면 다른 작업에 대한 로그도 늦게 출력이 됩니다.  
BlockingQueue는 이것을 편하게 도와줍니다.  
```java
public class ExecutorServiceTest {

    public static void main(String args[]) {
        ParallelExcutorService service = new ParallelExcutorService();
        service.submit("job1");
        service.submit("job2");
        service.submit("job3");
        service.submit("job4");

        for (int i = 0 ; i < 4; i++) {
            String result = service.take();
            System.out.println(result);
        }

        System.out.println("end");
        service.close();
    }

    private static class ParallelExcutorService {
        private final int maxCore = Runtime.getRuntime().availableProcessors();
        private final ExecutorService executor = Executors.newFixedThreadPool(maxCore);
        private final BlockingQueue<String> queue = new ArrayBlockingQueue<>(10);

        public ParallelExcutorService() {
        }

        public void submit(String job) {
            executor.submit(() -> {
                String threadName = Thread.currentThread().getName();
                System.out.println("finished " + job);
                String result = job + ", " + threadName;
                try {
                    queue.put(result); // 작업이 완료되면 큐에 삽입
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        public String take() {
            try {
                return queue.take(); // 큐에서 꺼내기 -> 없다면 대기하고 들어오면 꺼내준다.
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(e);
            }
        }

        public void close() {
            List<Runnable> unfinishedTasks = executor.shutdownNow();
            if (!unfinishedTasks.isEmpty()) {
                System.out.println("Not all tasks finished before calling close: " + unfinishedTasks.size());
            }
        }
    }
}

// 출력 결과
finished job3
finished job1
finished job2
finished job4
job4, pool-1-thread-4
job1, pool-1-thread-1
job3, pool-1-thread-3
job2, pool-1-thread-2
end
```
작업이 끝날 때 BlockingQueue에 결과를 추가하고 메인쓰레드에서 Queue를 기다리면 됩니다.   
전체적으로 보면, 멀티쓰레드에서 이 Queue에 add를 하는 구조입니다. 동시성 문제가 발생할 것 같지만, BlockingQueue 객체는 동시성을 보장하도록 구현되어있습니다.  
출력 결과를 보면 먼저 끝난 것에 대해 출력되는 것을 확인할 수 있습니다.

## CompletableFuture
CompletableFuture는 Future와 CompletionStage를 구현한 클래스입니다.  
Future이지만 직접 쓰레드를 생성하지 않고 async로 작업을 처리할 수 있고, 여러 CompletableFuture를 병렬로 처리하거나, 병합하여 처리할 수 있게 합니다.  
또한 Cancel, Error를 처리할 수 있는 방법을 제공합니다.

### 사용법
```java
// 생성
CompletableFuture<String> future = new CompletableFuture<>();

Executors.newCachedThreadPool().submit(() -> {
    Thread.sleep(2000);    
    future.complete("Finished");  // 결과 저장
    return null;
});

future.get(); // 결과 꺼내기

// 이미 결과를 알고 있다면 굳이 스레드를 만들지 않아도 된다.
Future<String> completableFuture = CompletableFuture.completedFuture("Skip!");
completableFuture.get();
```

### cancel
```java
CompletableFuture<String> future = new CompletableFuture<>();
Executors.newCachedThreadPool().submit(() -> {
    Thread.sleep(2000);
    future.cancel(false); // cancel 호출
    return null;
});

String result = null;
try {
    result = future.get();
} catch (CancellationException e) {
    e.printStackTrace();
    result = "Canceled!";
}
```
스레드에서 cancel이 호출되면 get에서 CancellationException이 발생합니다.  

### supplyAsync(), runAsync()
CompletableFuture는 supplyAsync()와 runAsync()를 제공하여 직접 쓰레드를 생성하지 않고 작업을 async로 처리하도록 할 수 있습니다.  
```java
CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> "future example");
future.get();
```
이런식으로 supplyAsync()에 Lambda로 인자를 전달할 수 있습니다.  
인자로 전달된 Lambda는 다른 쓰레드에서 처리가 됩니다.  
<br>

```java
CompletableFuture<Void> future = CompletableFuture.runAsync(() -> log("future example"));
future.get();
```
runAsync()도 사용법은 동일하지만 runAsync()는 리턴값이 없습니다.  
따라서 제네릭을 Void로 해야 합니다.  

### Exception Handling
CompletableFuture에서 작업을 처리하는 중에 Exception이 발생할 수 있습니다.  
이런 경우, handle()로 예외를 처리할 수 있습니다.
```java
CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
    String name = null;
    if (name == null) {
        throw new RuntimeException("Computation error!");
    }
    return "Hello, " + name;
}).handle((s, t) -> s != null ? s : "Hello, Stranger!");

future.get();
```

### thenApply()
supplyAsync()으로 어떤 작업이 처리되면, 그 결과를 가지고 다른 작업도 수행하도록 구현할 수 있습니다.  
thenApply() 메소드는 인자와 리턴 값이 있는 Lambda를 수행합니다.  
여기서 인자는 supplyAsync()에서 리턴되는 값이 됩니다.  
```java
CompletableFuture<String> future = CompletableFuture
        .supplyAsync(() -> "Hello")
        .thenApply(s -> s + " World")
        .thenApply(s -> s + " Future");

future.get();
```
thenApply() 또한 리턴값이 있기 때문에, 연달아 thenApply()를 적용할 수 있습니다.  

### thenAccept()
thenAccept()도 thenApply()와 비슷합니다.  
하지만, 인자를 받긴 하지만 로직을 명시한 람다에서는 리턴값이 존재하지 않습니다.  
```java
CompletableFuture<String> future1 = CompletableFuture
        .supplyAsync(() -> "Hello");

CompletableFuture<Void> future2 = future1.thenAccept(
        s -> log(s + " World"));
```
리턴값이 없기 때문에 제네릭을 Void로 세팅해줘야 합니다.  

### thenCompose() 
thenCompose()는 chain처럼 두개의 CompletableFuture를 하나의 CompletableFuture으로 만들어주는 역할을 합니다.  
첫번째 CompletableFuture의 결과가 리턴되면 그 결과를 두번째 CompletableFuture으로 전달하며, 순차적으로 작업이 처리됩니다.
```java
CompletableFuture<String> future = CompletableFuture
        .supplyAsync(() -> "Hello")
        .thenCompose(s -> CompletableFuture.supplyAsync(() -> s + " World"));
```
여기서 s는 Hello에 해당합니다.  

### thenCombine()
thenCompose()가 여러개의 CompletableFuture를 순차적으로 처리되도록 만들었다면, thenCombine()는 여러 CompletableFuture를 병렬로 처리되도록 만듭니다.  
모든 처리가 완료되고 그 결과를 하나로 합칠 수 있습니다.
```java
CompletableFuture<String> future1 = CompletableFuture
        .supplyAsync(() -> "Future1")
        .thenApply((s) -> {
            log("Starting future1");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return s + "!";
        });

CompletableFuture<String> future2 = CompletableFuture
        .supplyAsync(() -> "Future2")
        .thenApply((s) -> {
            log("Starting future2");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return s + "!";
        });
//                  병렬처리 대상, 결과값 하나로 합차는 처리
future1.thenCombine(future2, (s1, s2) -> s1 + " + " + s2)
        .thenAccept((s) -> log(s)); // 합친 결과값 받아서 출력
```
thenCombine()으로 이 두개의 future가 병렬로 진행되도록 하고 그 결과를 하나로 합치도록 합니다.  
하지만 실제로 결과를 찍어보면 순차적으로 처리된 것처럼 보이는데 이는 thenApply()가 동일한 스레드를 사용하기 때문입니다.  

### thenApplyAsync
thenApply()를 사용하는 곳에서는 서로 스레드를 공유합니다.  
대신에 thenApplyAsync()를 사용하면 다른 쓰레드에서 동작하도록 만들 수 있습니다.  
바로 위에 예시를 thenApplyAsync로 변경하면 두 개의 작업이 다른 스레드로 처리되도록 할 수 있습니다.
```java
CompletableFuture<String> future1 = CompletableFuture
        .supplyAsync(() -> "Future1")
        .thenApplyAsync((s) -> {
            log("Starting future1");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return s + "!";
        });

CompletableFuture<String> future2 = CompletableFuture
        .supplyAsync(() -> "Future2")
        .thenApplyAsync((s) -> {
            log("Starting future2");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return s + "!";
        });

future1.thenCombine(future2, (s1, s2) -> s1 + " + " + s2)
        .thenAccept((s) -> log(s));
```

### anyOf()
nyOf()는 여러개의 CompletableFuture 중에서 빨리 처리되는 1개의 결과만을 가져오는 메소드입니다.
```java
CompletableFuture<String> future1 = CompletableFuture
        .supplyAsync(() -> {
            log("starting future1");
            return "Future1";
        });

CompletableFuture<String> future2 = CompletableFuture
        .supplyAsync(() -> {
            log("starting future2");
            return "Future2";
        });

CompletableFuture<String> future3 = CompletableFuture
        .supplyAsync(() -> {
            log("starting future3");
            return "Future3";
        });

CompletableFuture.anyOf(future1, future2, future3)
        .thenAccept(s -> log("Result: " + s));
```
3개의 future 중에 가장 먼저 처리되는 1개의 결과만 thenAccept()으로 전달됩니다.  
물론 3개의 future는 모두 실행이 됩니다. thenAccept()에 전달되는 것이 1개일 뿐입니다.

### allOf()
allOf()는 모든 future의 결과를 받아서 처리를 할 수 있습니다.  
get()은 null을 리턴합니다.
```java
CompletableFuture<String> future1 = CompletableFuture
        .supplyAsync(() -> "Future1");

CompletableFuture<String> future2 = CompletableFuture
        .supplyAsync(() -> "Future2");

CompletableFuture<String> future3 = CompletableFuture
        .supplyAsync(() -> "Future3");

CompletableFuture<Void> combinedFuture
        = CompletableFuture.allOf(future1, future2, future3);

log("get() : " + combinedFuture.get()); // null
log("future1.isDone() : " + future1.isDone()); // true
log("future2.isDone() : " + future2.isDone()); // true
log("future3.isDone() : " + future3.isDone()); // true
```

### asynch method
thenApply()와 thenApplyAsync()처럼 뒤에 async가 붙은 메소드들이 항상 존재합니다.  
위의 예제에서 소개한 것처럼 동일한 쓰레드를 사용하지 않고 다른 쓰레드를 사용하여 처리하고 싶을 때 async가 붙은 메소드를 사용하면 됩니다.  
예를 들어, thenAccept()는 thenAcceptAsync()라는 메소드를 갖고 있습니다. 이런식으로 대부분 async가 붙은 메소드들이 pair로 존재합니다.  

## CountDownLatch
CountDownLatch는 어떤 쓰레드가 다른 쓰레드에서 작업이 완료될 때 까지 기다릴 수 있도록 해주는 클래스입니다.  
예를 들어, Main thread에서 5개의 쓰레드를 생성하여 어떤 작업을 병렬로 처리되도록 할 수 있습니다.  
이 때 Main thread는 다른 쓰레드가 종료되는 것을 기다리지 않고 다음 코드(statements)를 수행합니다.  
여기서 CountDownLatch를 사용하면 다음 코드(statements)를 실행하지 않고 기다리도록 만들 수 있습니다.  

다른 예로, 어떤 프로세스가 실행되기를 기다리거나 Network 등의 외부에서 어떤 이벤트가 발생하길 기다린다면, 그런 이벤트가 발생하지 않았을 때 무한히 기다리게 될 수도 있습니다.  
이럴 때, 다른 Thread에서 이 작업을 수행하도록 하고 Main thread는 일정 시간을 초과하면 작업을 기다리지 않도록, Timeout을 설정할 수 있습니다.

### 작동 원리
```java
// 인자로 Latch 숫자 전달하여 생성
CountDownLatch countDownLatch = new CountDownLatch(5);

// Latch 1씩 감소
countDownLatch.countDown();

// Latch의 숫자가 0이 될 때까지 대기
// 다른 쓰레드에서 countDown()을 5번 호출하게 된다면 Latch는 0이 되며, 
// await()은 더 이상 기다리지 않고 다음 코드를 실행하게 됩니다.
countDownLatch.await();
```

### 다른 스레드 작업이 완료될 때까지 대기
```java
public class CountDownLatchExample {

    public static void main(String args[]) throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(5); // 5개 Latch 생성

        // 5개 스레드 생성하면서 countDownLatch 인자로 전달
        List<Thread> workers = Stream
                .generate(() -> new Thread(new Worker(countDownLatch)))
                .limit(5)
                .collect(toList());

        System.out.println("Start multi threads (tid: "
                + Thread.currentThread().getId() + ")");

        // 스레드 실행
        workers.forEach(Thread::start);

        // 메인스레드는 Latch가 0이 될때 까지 대기
        countDownLatch.await();

        System.out.println("Finished (tid: "
            + Thread.currentThread().getId() + ")");
    }

    public static class Worker implements Runnable {
        private CountDownLatch countDownLatch;

        public Worker(CountDownLatch countDownLatch) {
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void run() {
            System.out.println("Do something (tid: " + Thread.currentThread().getId() + ")");
            countDownLatch.countDown(); // 수행 후 Latch 1씩 감소
        }
    }
}
```

### 정해진 시간만 기다리기
위의 코드들의 문제점은 어떤 쓰레드가 작업을 완료하지 못하면 countDown() 호출이 안되어 Main thread가 무한히 기다리게 된다는 것입니다.  
await()에 Timeout을 설정하면, 정해진 시간만 기다리도록 만들 수 있습니다.  
```java
public class CountDownLatchExample3 {

    public static void main(String args[]) throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(5);
        List<Thread> workers = Stream
                .generate(() -> new Thread(new Worker(countDownLatch)))
                .limit(5)
                .collect(toList());

        System.out.println("Start multi threads (tid: "
                + Thread.currentThread().getId() + ")");

        workers.forEach(Thread::start);

        // 5초가 지나도 Latch가 0이 아니라면 그냥 다음 코드 수행
        countDownLatch.await(5, TimeUnit.SECONDS);

        System.out.println("Finished (tid: "
                + Thread.currentThread().getId() + ")");
    }

    public static class Worker implements Runnable {
        private CountDownLatch countDownLatch;

        public Worker(CountDownLatch countDownLatch) {
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void run() {
            System.out.println("Doing something (tid: " + Thread.currentThread().getId() + ")");
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            countDownLatch.countDown();
            System.out.println("Done (tid: " + Thread.currentThread().getId() + ")");
        }
    }
}
```
Timeout을 5초로 설정하였고, 5초가 지날 때 까지 Latch가 0이 되지 않으면 더 기다리지 않고 다음 코드를 수행하게 됩니다.  
시간의 단위는 MINUTES 등으로 변경할 수도 있습니다.



<Br><Br>

__참고__  
<a href="https://codechacha.com/ko/category/java/concurrency/" target="_blank">Cuncurrency 시리즈</a>   

