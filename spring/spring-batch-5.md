# Spring Batch - Flow와 Scope

## FlowJob
---
### 기본 개념
+ Step을 특정한 상태에 따라 흐름을 전환하도록 구성할 수 있으며 FlowJobBuilder에 의해 생성됩니다.
    - Step이 실패하더라도 Job은 실패로 끝나지 않도록 해야 하는 경우
    - Step이 성공했을 때 다음에 실행해야 할 Step을 구분해야할 경우
    - 특정 Step은 전혀 실행되지 않게 구성해야하는 경우
+ Flow와 Job의 흐름을 구성하는데만 관여하고 실제 로직은 Step에서 이뤄집니다.
+ 내부적으로 SimpleFlow 객체를 포함하고 있으며 job 실행 시 호출됩니다.

### 배치 상태 유형
FlowJob은 조건에 따라 분기되어 실행되는데 그에 대한 조건으로 상태를 이용합니다.

#### Batch Status
Batch Status는 JobExecution과 StepExecution의 속성으로 Job과 Step의 실행 상태를 나타냅니다.  
COMPLETED, STARTING, STARTED, STOPPING, STOPPED, FAILED, ABANDONED, UNKNOWN 총 8 종류의 __ENUM값__ 이 있습니다.  
ABANDONED와 FAILED와의 차이점은 FAILED는 실패 후 재실행 시 재시작이 되지만, ABANDONED는 재실행 시 건너뜁니다.
+ SimpleJob
    - 마지막 Step의 __BatchStatus__ 값을 Job의 최종 BatchStatus에 반영합니다.
    - Step이 실패한 경우 해당 Step이 마지막 Step이 되어 Job의 BatchStatus값으로 반영합니다.
+ FlowJob
    - Flow내 Step의 __ExitStatus__ 값을 FlowExecutionStatus에 반영합니다.
    - 마지막 Flow의 FlowExecutionStatus 값을 Job의 최종 BatchStatus 값으로 반영합니다.

#### ExitStatus
JobExecution과 StepExecution의 속성으로 Job과 Step의 실행 후 종료되는 상태를 나타냅니다.  
__기본적으로 ExitStatus는 BatchStatus와 동일한 값으로 설정됩니다.__  
임의의 값으로 다르게 설정할 수도 있습니다.
+ SimpleJob
    - 마지막 Step의 __ExitStatus__ 값을 Job의 최종 ExitStatus값으로 반영합니다.
+ FlowJob
    - Flow내 Step의 __ExitStatus__ 값을 FlowExecutionStatus 값으로 저장합니다.
    - 마지막 Flow의 FlowExecutionStatus 값을 Job의 최종 ExitStatus값으로 반영합니다.




#### FlowExecutionStatus
+ FlowExecution의 속성으로 Flow의 실행 후 최종 결과 상태를 나타냅니다.  
+ Flow 내 Step이 실행되고 ExitStatus값을 FlowExecutionStatus값으로 저장하게 됩니다.
+ FlowJob의 BatchStatus에 관여합니다.
+ COMPLETED, STOPPED, FAILED, UNKNOWN 의 상태가 있습니다.



### API 소개
![그림1](https://github.com/backtony/blog-code/blob/master/spring/img/batch/5/5-1.PNG?raw=true)  
FlowJob은 API설명만으로 이해하기는 어렵습니다.  
아래서 하나씩 살펴보겠습니다.

#### start(), next()
![그림2](https://github.com/backtony/blog-code/blob/master/spring/img/batch/5/5-2.PNG?raw=true)  
start에는 Flow와 Step이 모두 올 수 있습니다.  
Flow가 오게 되면 jobFlowBuilder가 반환되고, Step이 오면 SimpleJobBuilder가 반환됩니다.  
하지만 simpleJobBuilder도 on을 지원하기 때문에 start에 step을 인자로 넣고 뒤에서 on을 사용하면 jobFlowBuilder가 반환됩니다.  
next는 Step, Flow, JobExecutionDecider 타입을 받을 수 있습니다.  

#### Transition
+ Flow 내 Step의 조건부 전환을 정의합니다.
+ Job의 API설정에서 on(String Pattern) 메서드를 호출하면 TransitionBuilder가 반환되어 Transition Flow를 구성할 수 있습니다.
+ __Step의 종료상태(ExitStatus)__ 가 어떤 pattern과도 매칭되지 않으면 예외가 발생됩니다.
+ API
    - on(String pattern)
        - Step의 실행 결과로 받는 __종료상태(ExitStatus)__ 와 매칭하는 패턴스키마
        - pattern과 ExitStatus와 매칭되면 다음 실행할 Step을 지정할 수 있습니다.
        - 특수문자는 2가지만 허용합니다.
            - \* : 0개 이상의 문자와 매칭
            - ex) c*t은 cat, count와 매칭
            - ? : 정확히 1개의 문자와 매칭
            - ex) c?t는 cat과 매칭, count는 매칭 불가
    - to()
        - 다음 실행할 단계 지정
    - from()
         - 이전 단계에서 정의한 Transition을 새롭게 추가해서 정의
         - 앞선 분기 이외의 새로운 분기를 만든다고 생각하면 됩니다.
+ 중단 API
    - Flow가 실행되면 FlowExecutionStatus에 상태값이 저장되고 최종적으로 Job의 BatchStatus와 ExitStatus에 반영되지만, __Step의 BatchStatus와 ExitStatus에는 아무런 영향을 주지 않습니다.__
    - stop()
        - FlowExecutionStatus가 STOPPED 상태로 종료
        - Job의 BatchStatus와 ExitStatus가 모두 STOPPED로 종료됩니다.
    - fail()
        - FlowExecution가 FAILED 상태로 종료
        - Job의 BatchStatus와 ExitStatus가 모두 FAILED로 종료됩니다.
    - end()
        - FlowExecution가 COMPLETED 상태로 종료
        - Job의 BatchStatus와 ExitStatus가 모두 COMPLETED로 종료됩니다.
        - Step의 ExitStatus가 FAILED이더라도 Job의 BatchStatus가 COMPLETED로 종료되도록 하여 Job의 재시작이 불가능
        - 하나의 SimpleFlow 객체를 생성할 때도 사용됩니다.(flow를 다 정의하고 마지막에 붙인다고 생각) -> SimpleFlow는 뒤에서 설명합니다.
    - stopAndReStart(Step or Flow or JobExecutionDecider)
        - stop()과 기본 흐름은 동일
        - 특정 step에서 작업을 중단하도록 설정하면 중단 이전의 Step만 COMPLETED로 저장되고 이후의 Step은 실행되지 않고 STOPPED 상태로 종료
        - job이 다시 실행됐을 때 실행해야 할 Step을 인자로 넘기면 이전에 COMPLETED로 저장된 Step은 건너뛰고 중단 이후 Step부터 시작


#### 예시
```java
@Configuration
@RequiredArgsConstructor
public class HelloJobConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job helloJob() {
        return jobBuilderFactory.get("job")
                .start(step1())
                    .on("COMPLETED")
                    .to(step2())
                    .on("FAILED")
                    .to(step3())
                .from(step1())
                    .on("FAILED")
                    .end()
                .from(step2())
                    .on("COMPLETED")
                    .to(step4())
                    .next(step5())                    
                .end() // SimpleFlow 객체 생성
                .incrementer(new RunIdIncrementer())
                .build();
    }
    ...
}
```
경우에 따라 하나씩 보겠습니다.
+ step1 성공 -> step2 실패 -> step3 진행 -> step3의 ExitStatus에 따른 Job의 BatchStatus, ExitStatus 업데이트
+ step1 성공 -> step2 성공 -> step4 진행 -> step5 진행 -> step5에 ExitStatus에 따른 Job의 BatchStatus, ExitStatus 업데이트
+ step1 실패 -> step1의 StepExecution의 BatchStatusExitStatus, ExitStatus는 FAILED이지만 end로 인해 Job의 BatchStatus, ExitStatus는 COMPLETED
+ 맨 마지막의 end()는 정의한 flow를 하나의 SimpleFlow 객체로 생성하는 메서드

<Br>

```java
@Configuration
@RequiredArgsConstructor
public class HelloJobConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job helloJob() {
        return jobBuilderFactory.get("job")
                .start(step1())
                    .on("COMPLETED")
                    .stop()
                .from(step1())
                    .on("*")
                    .to(step2())
                    .on("FAILED")
                    .stopAndRestart(step3())
                .end() // SimpleFlow 객체 생성
                .build();
    }
    ...
}    
```
+ step1 성공 -> step1의 StepExecution의 BatchStatusExitStatus, ExitStatus는 COMPLETED이지만 stop으로 인해 Job의 BatchStatus, ExitStatus는 STOPPED
+ step1 실패 -> step2 성공 -> step2의 상태에 맞춰 Job의 BatchStatus, ExitStatus는 COMPLETED
+ step1 실패 -> step2 실패 -> StepExecution의 BatchStatusExitStatus, ExitStatus는 FAILED이지만 stopAndRestart로 인해 Job의 BatchStatus, ExitStatus는 STOPPED -> 재실행시 step3부터 시작

#### 명확한 로직이 정해지지 않은 경우
앞선 예시들을 보면 분명 조건을 명시해주지 않았던 작업에 대해서도 동작하는 것을 확인할 수 있습니다.  
이는 스프링 배치에서 Flow 조건에 따른 전환이 명확하게 명시되어 있지 않을 경우 후속 처리를 하기 때문입니다.  
예를 들면, 스텝이 성공했을 경우에만 조건으로 처리했을 경우, 스프링 배치에서 나머지 상태의 경우에 대한 조건을 기본적으로 추가해버립니다.  
```java
private void addDanglingEndStates() {
   Set<String> froms = new HashSet<>();
   for (StateTransition transition : transitions) {
      froms.add(transition.getState().getName());
   }
   if (tos.isEmpty() && currentState != null) {
      tos.put(currentState.getName(), currentState);
   }
   Map<String, State> copy = new HashMap<>(tos);
   // Find all the states that are really end states but not explicitly declared as such
   for (String to : copy.keySet()) {
      if (!froms.contains(to)) {
         currentState = copy.get(to);
         if (!currentState.isEndState()) {
            addTransition("COMPLETED", completedState);
            addTransition("*", failedState);
         }
      }
   }
   copy = new HashMap<>(tos);
   // Then find the states that do not have a default transition
   for (String from : copy.keySet()) {
      currentState = copy.get(from);
      if (!currentState.isEndState()) {
         if (!hasFail(from)) {
            addTransition("*", failedState);
         }
         if (!hasCompleted(from)) {
            addTransition("*", completedState);
         }
      }
   }
}
```
위 코드는 FlowBuilder 소스 중 일부입니다.  
만약 현재 스텝의 조건 전환이 끝나지 않았는데 아무런 상태 flow가 정의되어 있지 않은 경우에는 COMPLETED 를 추가하고 나머지 작업에 대해서는 FAILED 처리 flow가 추가됩니다.  
만약 현재 스텝의 조건 전환이 끝나지 않았는데 완료조건만 있다면 실패조건이 추가되고, 실패조건만 있다면 완료조건이 추가됩니다.  
여기서 주의할 점은 만약 Custom한 Exitstatus만으로 on절을 명시하고 default transition인 FAILED, COMPLETED 정의가 모두 되어있지 않을 경우 FAILED 정의가 *으로 우선적으로 추가되기 때문에 step이 COMPLETED로 끝나고 잡은 FAILED 처리됩니다.
<br>

이제 예시를 보겠습니다.
```java
@Configuration
@RequiredArgsConstructor
public class HelloJobConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job helloJob() {
        return jobBuilderFactory.get("job")
                .start(step1())
                    .on("FAILED")
                    .to(step2())
                    .on("PASS")
                    .stop()
                .end() // SimpleFlow 객체 생성
                .build();
    }
    ...
}     
```
step2에는 성공, 실패에 대한 모든 정의가 되어있지 않기 때문에 실패에 대한 flow가 우선적으로 추가되어 step2가 성공해도 실패로 처리됩니다.  
만약에 step1이 COMPLETED 되었다면 이때는 예외가 발생합니다.  
스프링 배치가 기본적으로 조건을 추가해주는 작업은 to의 인수로 있는 작업에 한에서만 입니다.  
따라서 step1은 to문에 들어있지 않기 때문에 기본적인 조건 추가가 안들어가게 되어 예외가 발생하는 것입니다.  



#### ExitStatus 커스텀하기
간단하게 Step이 종료된 이후에 StepExecution의 ExitStatus를 PASS로 조작하는 리스너를 하나 만들어서 step2에 붙였습니다.  
사실 리스너를 붙여서 조작하는 방식보다는 바로 다음에 설명하는 JobExecutionDecider를 사용하는게 더 좋습니다.  
```java
public class PassCheckListener implements StepExecutionListener {
    @Override
    public void beforeStep(StepExecution stepExecution) { }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        String exitCode = stepExecution.getExitStatus().getExitCode();
        if(!exitCode.equals(ExitStatus.FAILED.getExitCode())){
            return new ExitStatus("PASS"); // 스텝이 끝난 이후 ExitStatus 코드를 PASS로 조작하는 리스너
        }
        return null;
    }
}
--------------------------------------------------------------------------
@Configuration
@RequiredArgsConstructor
public class HelloJobConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job helloJob() {
        return jobBuilderFactory.get("job")
                .start(step1())
                    .on("FAILED")
                    .to(step2())
                    .on("PASS")
                    .stop()
                .end() // SimpleFlow 객체 생성
                .incrementer(new RunIdIncrementer())
                .build();
    }

    @Bean
    public Step step1() {
        return stepBuilderFactory.get("step1")
                .tasklet((stepContribution, chunkContext) -> {
                    System.out.println("step1 completed");
                    contribution.setExitStatus(ExitStatus.FAILED);
                    return RepeatStatus.FINISHED;
                })
                .build();
    }

    @Bean
    public Step step2() {
        return stepBuilderFactory.get("step2")
                .tasklet((stepContribution, chunkContext) -> {
                    System.out.println("step2 completed");
                    return RepeatStatus.FINISHED;
                })
                .listener(new PassCheckListener()) // 리스너 추가
                .build();
    }
}
```

<br>

## JobExecutionDecider
---
앞서 ExitStatus의 상태값을 조작하기 위해서 step 안에 Listener를 추가했습니다.  
Step은 비즈니스 로직에 집중해야하는데 Step안에 flow를 위한 exitStatus 조작 코드가 들어가게 되니 그다지 좋은 방법은 아니라는 느낌이 듭니다.  
JobExecutionDecider는 __Transition 처리를 위한 전용 클래스__ 로 ExitStatus를 조작하거나 StepExecutionListener를 등록할 필요 없이 내부적인 로직으로 흐름을 제어할 수 있습니다.  
__JobExecutionDecider는 ExitStatus가 아닌 FlowExecutionStatus 상태값을 새롭게 생성하여 반환합니다.__  
<br>

__예시__  
간단하게 짝홀 decider를 만들어보고 그에 따른 흐름에 맞게 동작하는 Job을 만들겠습니다.  

```java
@Configuration
@RequiredArgsConstructor
public class HelloJobConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;


    @Bean
    public Job helloJob() {
        return jobBuilderFactory.get("job")
                .start(step()) 
                .next(decider()) // step이 완료되면 decider를 실행
                    // decider의 반환값에 따른 분기
                    .from(decider()).on("ODD").to(oddStep()) 
                    .from(decider()).on("EVEN").to(evenStep())
                .end() // SimpleFlow 객체 생성
                .build();
    }


    @Bean
    public JobExecutionDecider decider() {
        return new CustomDecider();
    }

    @Bean
    public Step step() {
        return stepBuilderFactory.get("step")
                .tasklet((stepContribution, chunkContext) -> {
                    System.out.println("step completed");
                    return RepeatStatus.FINISHED;
                })
                .build();
    }

    @Bean
    public Step evenStep() {
        return stepBuilderFactory.get("evenStep")
                .tasklet((stepContribution, chunkContext) -> {
                    System.out.println("even completed");
                    return RepeatStatus.FINISHED;
                })
                .build();
    }

    @Bean
    public Step oddStep() {
        return stepBuilderFactory.get("oddStep")
                .tasklet((stepContribution, chunkContext) -> {
                    System.out.println("odd completed");
                    return RepeatStatus.FINISHED;
                })
                .build();
    }
}

--------------------------------------------------------------------
public class CustomDecider implements JobExecutionDecider {
    private int count = 0;

    @Override
    public FlowExecutionStatus decide(JobExecution jobExecution, StepExecution stepExecution) {
        count++;

        if (count % 2 == 0) {
            return new FlowExecutionStatus("EVEN");
        } else {
            return new FlowExecutionStatus("ODD");
        }
    }
}
```
<br>

## FlowJob 아키텍처
---
![그림3](https://github.com/backtony/blog-code/blob/master/spring/img/batch/5/5-3.PNG?raw=true)  
JobLauncher는 FlowJob과 Jobparameters를 갖고 Job을 실행시킵니다.  
그 사이에 Job이 실행될 때 필요한 메타 데이터들을 생성합니다.(JobInstance, JobExecution, ExecutionContext)  
Job이 실행되기 전에 JobListener에서 beforeJob이 호출됩니다.  
FlowExecutor가 SimpleFlow를 실행시킵니다.  
그 사이에서 FlowExecution 객체가 생성되는데 이는 JobExecution, StepExecution 처럼 Flow를 실행시키면서 발생하는 정보를 담는 객체입니다.  
실행 결과 Flow가 가진 상태값 FlowExecutionStatus를 반환하게 되고 이를 FlowExecution에 업데이트합니다.  
이후 JobListener에서 afterJob이 호출되고, 마지막으로 FlowExecutionStatus 값을 Job의 BatchStatus와 ExitStatus에 업데이트 시키고 끝납니다.  

<br>

## SimpleFlow
---
### 기본 개념
+ 스프링 배치에서 제공하는 __Flow 인터페이스의 구현체__ 로 각 요소(Step, Flow, JobExecutionDecider)들을 담고 있는 State를 실행시키는 도메인 객체
+ FlowBuilder를 사용해서 생성하며 Transition과 조합하여 여러 개의 Flow 및 중첩 Flow를 만들어 Job을 구성할 수 있습니다.

### 구조 및 구성
![그림4](https://github.com/backtony/blog-code/blob/master/spring/img/batch/5/5-4.PNG?raw=true)  
SimpleFlow는 Flow 인터페이스를 구현한 구현체입니다.  
여기서 갑자기 State라는 개념이 등장합니다.  
FlowJob을 실행시키면 Job을 구성한 여러 요소들을 포함하고 있는 SimpleFlow로 동작이 수행됩니다.  
SimpleFlow는 State 인터페이스를 startState 변수로 갖고 있습니다.  
State의 구현체로는 FlowState, StepState 등 여러 개의 구현체가 있습니다.  
예를 들어, StepState는 내부적으로 Step을 갖고 있습니다.  
즉, 각 State 구현체들은 내부적으로 타입에 맞는 인스턴스(Step, Flow 등)을 갖고 있습니다.  
결과적으로 SimpleFlow는 갖고 있는 State의 handle 메서드를 통해 실제 인스턴스(Step, Flow)의 동작을 수행시키게 되고 handle 메서드는 FlowExecutionStatus를 반환합니다.  
__정리하자면, State는 각 실행 요소들을 한번 감싸서 가지고 있고 해당 요소를 실행하는 역할을 하는 객체라고 보면 됩니다.__  
각각의 State는 Job이 구성되는 시점에 FlowBuilder에 의해서 생성됩니다.  

<br>

![그림5](https://github.com/backtony/blog-code/blob/master/spring/img/batch/5/5-5.PNG?raw=true)  
각각의 api의 인자로 들어가있는 flow1과 flow2도 SimpleFlow입니다.  
end()는 전체를 감싸는 SimpleFlow를 생성합니다.  
즉, 하나의 큰 SimpleFlow 안에 여러 개의 SimpleFlow가 담겨있는 것입니다.  
여기서 flow1은 Flow이므로 FlowState가 생성되며 이는 flow1을 필드로 갖게 되고, flow2 마찬가지로 FlowState가 생성되어 flow2를 담게 됩니다.  

### 아키텍처
![그림6](https://github.com/backtony/blog-code/blob/master/spring/img/batch/5/5-6.PNG?raw=true)  
앞서 각각의 State는 Job이 구성되는 시점에 인수타입에 맞게 FlowBuilder에 의해 생성된다고 했습니다.  
그림의 왼쪽 부분이 해당 내용입니다. 각 타입에 맞는 State가 생성되고 그 안에 해당 인수들이 담깁니다.  
이런 State들이 on에서 어떤 패턴을 만나면 어떤 다음 State로 이동하는 지에 대한 정보가 StateTransition 객체에 담기게 됩니다.  
SimpleFlow는 각각의 StateTransition를 List로 갖고 이를 통해서 나머지 필드값들을 초기화 합니다.  
<br>

![그림7](https://github.com/backtony/blog-code/blob/master/spring/img/batch/5/5-7.PNG?raw=true)  
이제 전체적인 그림으로 보겠습니다.  
FlowJob은 FlowExecutor를 통해서 SimpleFlow를 실행합니다.  
SimpleFlow는 각각의 State들의 Flow를 담은 StateTransition를 리스트 형태로 갖고 있고 이를 통해서 StateMap을 초기화합니다.  
처음 실행될 StartState를 지정하고 실행합니다.  
StartState가 완료되면 다음 State를 실행하기 위해 resume() 메서드를 호출합니다.  
resume()메서드에서는 다음 State를 선택하기 위해서 nextState()를 호출하고 nextState에서는 StateMap을 통해 다음 State를 선택합니다.  
결과적으로 resume() 메서드로 돌아와 다음 State를 실행하게 됩니다.  
만약 다음 State가 FlowState일 경우, 그 안에 Flow가 존재하므로 다시 반복하게 됩니다.  
이런 구조로 SimpleFlow가 동작하게 됩니다.  

### 예시1
```java
@Slf4j
@Configuration
@RequiredArgsConstructor
public class HelloJobConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;


    @Bean
    public Job helloJob() {
        return jobBuilderFactory.get("job")
                .start(flow1()) // SimpleFlow
                    .on("COMPLETED")
                    .to(flow2()) // SimpleFlow
                .end() // 전부를 포괄하는 SimpleFlow 생성
                .build();
    }

    @Bean
    public Flow flow1() {
        FlowBuilder<Flow> builder = new FlowBuilder<>("flow1");
        builder.start(step1())
                .end();
        return builder.build();
    }

    @Bean
    public Flow flow2() {
        FlowBuilder<Flow> builder = new FlowBuilder<>("flow2");
        builder.start(step2())
                .end();
        return builder.build();
    }


    @Bean
    public Step step1() {
        return stepBuilderFactory.get("step1")
                .tasklet((stepContribution, chunkContext) -> {
                    System.out.println("step1 completed");
                    return RepeatStatus.FINISHED;
                })
                .build();
    }

    @Bean
    public Step step2() {
        return stepBuilderFactory.get("step2")
                .tasklet((stepContribution, chunkContext) -> {
                    System.out.println("step2 completed");
                    return RepeatStatus.FINISHED;
                })
                .build();
    }
}
```

### 예시2
```java
@Configuration
@RequiredArgsConstructor
public class HelloJobConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;


    @Bean
    public Job helloJob() {
        return jobBuilderFactory.get("job")
                .start(flow1())
                    .on("FAILED")
                    .to(flow2())
                .end()
                .build();
    }

    @Bean
    public Flow flow1() {
        FlowBuilder<Flow> builder = new FlowBuilder<>("flow1");
        builder.start(step1())
                .end();
        return builder.build();
    }

    @Bean
    public Flow flow2() {
        FlowBuilder<Flow> builder = new FlowBuilder<>("flow2");
        builder.start(step2())
                .end();
        return builder.build();
    }


    @Bean
    public Step step1() {
        return stepBuilderFactory.get("step1")
                .tasklet((stepContribution, chunkContext) -> {
                    System.out.println("step1 completed");
                    contribution.setExitStatus(ExitStatus.FAILED);
                    return RepeatStatus.FINISHED;
                })
                .build();
    }

    @Bean
    public Step step2() {
        return stepBuilderFactory.get("step2")
                .tasklet((stepContribution, chunkContext) -> {
                    System.out.println("step2 completed");
                    return RepeatStatus.FINISHED;
                })
                .build();
    }

}
```
flow1에서 수행되는 step1의 ExitStatus를 FAILED처리 시켰고 이후 flow2가 수행됩니다.  
이때 JOB의 ExitStatus와 BatchStatus에 주목해야 합니다.  
Job의 BatchStatus는 COMPLETED이지만, ExitStatus는 FAILED입니다.  
이는 포스팅의 맨 앞쪽에서 step들끼리 on, to로 연결한 것과 다른 방식으로 업데이트 됩니다.  
즉, flow로 Job이 구성된 경우에는 다르게 업데이트 된다는 의미입니다.  
Job의 BatchStatus는 최종 흐름의 성공 여부에 따라 값이 반영됩니다.  
위의 경우 흐름에 따라 flow1이 실패하고 flow2를 성공적으로 끝내서 결과적으로 성공했습니다.  
따라서 BatchStatus는 COMPLETED로 처리됩니다.  
하지만 흐름중에서 어떠한 STEP에서 FAILED가 발생한다면 나머지 모두가 성공하더라도 Job의 ExitStatus는 FAILED 처리됩니다.  
위에서는 flow1에서 step1이 FAILED 처리되었기에 최종적으로 Job이 성공했더라도 ExitStatus는 FAILED처리되는 것입니다.  
따라서 일반적으로 배치가 최종 성공했는지 실패했는지를 판단하는 기준은 BatchStatus라고 보면 됩니다.  




<br>

## FlowStep
---
### 기본 개념
+ Step 내에 Flow를 할당하여 실행시키는 도메인 객체
+ JobStep이 Step 안에서 Job을 할당하는 것 처럼 Step 내에서 Flow를 할당하는 객체입니다.
+ flowStep도 Step이기 때문에 BatchStatus와 ExitStatus가 존재하는데, 품고 있는 Flow의 FlowExecutionStatus값을 통해 업데이트 됩니다.


### 예시
```java
@Configuration
@RequiredArgsConstructor
public class HelloJobConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;


    @Bean
    public Job helloJob() {
        return jobBuilderFactory.get("job")
                .start(flowStep())
                .next(step2())
                .incrementer(new RunIdIncrementer())
                .build();
    }

    // 스텝이지만 flow를 요소로 갖고 있는 flowStep
    @Bean
    public Step flowStep() {
        return stepBuilderFactory.get("flowStep")
                .flow(flow())
                .build();
    }

    @Bean
    public Flow flow() {
        FlowBuilder<Flow> builder = new FlowBuilder<>("flow");
        builder.start(step1())
                .end();
        return builder.build();
    }


    @Bean
    public Step step1() {
        return stepBuilderFactory.get("step1")
                .tasklet((stepContribution, chunkContext) -> {
                    System.out.println("step1 completed");
                    return RepeatStatus.FINISHED;
                })
                .build();
    }

    @Bean
    public Step step2() {
        return stepBuilderFactory.get("step2")
                .tasklet((stepContribution, chunkContext) -> {
                    System.out.println("step2 completed");
                    return RepeatStatus.FINISHED;
                })
                .build();
    }
}
```
간단하게 job에 flowStep 한개와 Step 한개를 넣었습니다.  
처음에 flowStep을 실행하게 되면서 flowStep내부에 있는 flow를 실행하게 되고 flow가 step1을 실행하게 됩니다.  
이후 next에 의해 step2가 실행됩니다.  
만약 step1이 실패하게 되면 해당 flowStep도 실패처리됩니다.  

<Br>

## @JobScope와 @StepScope
---
Scope는 스프링 컨테이너에서 빈이 관리되는 범위를 의미합니다.  
+ Job과 Step의 빈 생성과 실행에 관여하는 스코프입니다.
+ __프록시 모드__ 를 기본값으로 합니다.
+ 프록시 모드이기 때문에 애플리케이션 구동 시점에는 빈의 프록시 빈이 생성되고 실행 시점에 빈 생성이 이뤄집니다.
    - 이를 통해 빈의 실행 시점에 값을 참조할 수 있는 일종의 Lazy Binding이 가능해집니다.
        - @Value("#{jobParameters[파라미터명]}")
        - @Value("#{jobExecutionContext[파라미터명]}")
        - @Value("#{stepExecutionContext[파라미터명]}")
        - 을 사용해서 값을 주입받습니다.
    - @Value를 사용할 경우 빈 선언문에 @JobScope, @StepScope를 반드시 정의해야 합니다.
+ 병렬 처리 시 각 스레드 마다 생성된 스코프 빈이 할당되기 때문에 스레드에 안전하게 실행이 가능합니다
    - 각 스레드마다 생성된 스코프 빈이 할당되어 각 스레드마다 프록시를 갖고 있어 빈을 호출 시 스레드마다 각각의 빈을 따로 생성하여 갖게 됩니다.
+ Bean과 연관되어 사용하는 것이기 때문에 Tasklet도 당연히 빈등록 해줘야 합니다.

### JobScope
+ Step 선언문에 붙입니다.
+ @Value로 JobParameter과 JobExectionContext만 사용 가능합니다.

### StepScope
+ Tasklet이나 ItemReader, ItemWriter, ItemProcessor 선언문에 붙입니다.
+ @Value로 JobParameter, JobExecutionContext, StepExecutionContet 사용 가능합니다.


### 예시
```java
@Configuration
@RequiredArgsConstructor
public class Test2Config {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job helloJob() {
        return jobBuilderFactory.get("job")
                .start(step1(null)) // 런타임시 주입받을 것이므로 현재는 null로 주입
                .listener(new CustomJobListener())
                .build();
    }

    @Bean
    @JobScope
    public Step step1(@Value("#{jobParameters['message']}") String message) {
        System.out.println("message = " + message);
        return stepBuilderFactory.get("step1")
                .tasklet(tasklet(null,null)) // 런타임 시 주입되므로 null 
                .listener(new CustomStepListener())
                .build();
    }

    @Bean
    @StepScope
    public Tasklet tasklet(@Value("#{jobExecutionContext['name']}") String name,
                           @Value("#{stepExecutionContext['name2']}") String name2){
        return (stepContribution, chunkContext) -> {
            System.out.println("name = " + name);
            System.out.println("name2 = " + name2);
            return RepeatStatus.FINISHED;
        };
    }
}
-------------------------------------------------------
public class CustomJobListener implements JobExecutionListener {
    @Override
    public void beforeJob(JobExecution jobExecution) {
        jobExecution.getExecutionContext().putString("name","user1");
    }

    @Override
    public void afterJob(JobExecution jobExecution) {

    }
}
-------------------------------------------------------
public class CustomStepListener implements StepExecutionListener {
    @Override
    public void beforeStep(StepExecution stepExecution) {
        stepExecution.getExecutionContext().putString("name2","user2");
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        return null;
    }
}
```
@Value를 통해서 런타임시 주입되는 값들에 대해서는 코드로 아무것도 주지 않으면 컴파일 에러가 나기 때문에 null값으로 채워줍니다.  
리스너를 통해서 name, name2 값을 넣어줬고, 실행 시점에 IDE의 Configuration을 통해서 arguments로 message=message로 주고 실행시키면 주입한 값이 정상적으로 찍히게 됩니다.  

### 사용하는 이유
처음 공부하는 입장에서는 사실 위에 설명과 예시를 보고 도대체 왜 사용하는지에 대한 의문이 들 수 있어서 다시 정리하겠습니다.
+ 표현식 언어를 통해 유연하고 편리하게 주입받아 파라미터로 사용할 수 있게 됩니다.
    - StepContribution에서 일일이 원하는 값을 꺼내서 사용하지 않아도 된다는 뜻입니다.
+ Step 빈 생성이 구동시점이 아닌 런타임 시점에 생성되어 객체의 지연 로딩이 가능해집니다.
    - 이 덕분에 위에 표현식을 사용할 수 있는 것입니다. 
    - 표현식으로 적은 값들은 컴파일 시점에 존재하지 않고 런타임 시점에 채워지면서 존재하는 값입니다.  
    - 만약 빈이 애플리케이션 로딩 시점에 만들어진다면 DI를 해야하는데 해당 값들이 현재 존재하지 않기 때문에 찾을 수가 없습니다.
    - 하지만 런타임 시점에 빈을 만들게 되면 값을 다 받아놓고(표현식에 명시한 값들) 빈을 만들기 때문에 주입이 가능하게 됩니다.
+ 병렬 처리시에 각 스레드마다 Step 객체가 생성되어 할당되기 때문에 Tasklet에 멤버 변수가 존재해도 동시성에 문제가 없습니다.

### 아키텍처
+ 프록시 객체 생성
    - @JobScope, @StepScope 애노테이션이 붙은 빈 선언은 내부적으로 프록시 빈 객체가 생성되어 등록됩니다.
    - Job 실행 시 Proxy 객체가 실제 빈을 호출해서 해당 메서드를 실행시키는 구조 입니다.
+ JobScope, StepScope
    - 애노테이션이 붙은 것과는 다른 것으로 Proxy 객체의 실제 대상이 되는 Bean을 등록, 해제하는 역할을 하는 클래스입니다.
    - 실제 대상이 되는 빈을 저장하고 있는 JobContext, StepContext를 갖고 있습니다.
    - Job의 실행 시점에 프록시 객체는 실제 빈을 찾기 위해서 JobScope, StepScope의 JobContext, StepContext를 찾게 됩니다.


![그림8](https://github.com/backtony/blog-code/blob/master/spring/img/batch/5/5-8.PNG?raw=true)  
1. JobScope가 붙어서 프록시로 생성된 Step에 요청이 들어옵니다.
2. 프록시는 JobScope의 JobContext에서 실제 타겟 빈이 존재하는지 확인합니다.
3. 있으면 찾아서 반환합니다.
4. 없으면 빈 팩토리에서 실제 Step빈을 생성하고 JobContext에 담고 이를 반환합니다.

### Chunk 기반에서 사용 시 주의사항
```java
@Bean
@StepScope
public ItemReader<? extends Customer> customItemReader(
        @Value("#{stepExecutionContext['minValue']}") Long minValue,
        @Value("#{stepExecutionContext['maxValue']}") Long maxValue) {
        ....

        return new JpaPagingItemReaderBuilder<Customer>()
                ....
                .build();
}
```
처음에 위와 같이 사용했다가 null 포인트 예외가 터져서 한참 찾았습니다.  
Scope가 아닐 경우에는 Jpa 구현체가 빈으로 등록되기 때문에 전혀 문제가 되지 않습니다.  
하지만 위 코드와 같이 Scope를 사용하면 구현체가 아니라 ItemReader 인터페이스의 프록시 객체가 빈을 등록되서 문제가 발생합니다.  
구현체의 경우 ItemReader와 ItemStream을 모두 구현하고 있기 때문에 문제가 없지만 ItemReader는 read 메서드만 있습니다.  
실제로 stream을 open/close하는 메서드는 ItemStream에 있습니다.  
즉, 위와 같이 사용하면 EntityManagerFactory에서 entityManager을 생성하는게 원래 Stream에서 진행되는 거라 itemReader인 프록시는 그런게 없기 때문에 null 포인트 예외가 발생하게 됩니다.  
이에 대한 해결책은 그냥 구현체를 반환하면 됩니다.  
```java
@Bean
@StepScope
public JpaPagingItemReader<? extends Customer> customItemReader(
        @Value("#{stepExecutionContext['minValue']}") Long minValue,
        @Value("#{stepExecutionContext['maxValue']}") Long maxValue) {
        ....

        return new JpaPagingItemReaderBuilder<Customer>()
                ....
                .build();
}
```
더욱 자세한 내용은 [여기](https://jojoldu.tistory.com/132)를 참고하시면 좋을 것 같습니다.





<Br><Br>

__참고__  
<a href="https://www.inflearn.com/course/%EC%8A%A4%ED%94%84%EB%A7%81-%EB%B0%B0%EC%B9%98#" target="_blank"> 스프링 배치 - Spring Boot 기반으로 개발하는 Spring Batch</a>   



