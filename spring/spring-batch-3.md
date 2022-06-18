# Spring Batch - Job


## 배치 초기화 설정
---
### JobLauncherApplicationRunner
+ Spring Batch 작업을 시작하는 ApplicationRunner로서 BatchAutoConfiguration에서 생성됩니다.
+ 스프링 부트에서 제공하는 ApplicationRunner의 구현체로 애플리케이션이 정상적으로 구동되자 마자 실행됩니다.
+ 기본적으로 빈으로 등록된 모든 Job을 실행시킵니다.


### BatchProperties
application.yml에서 Spring Batch의 환경 설정을 할 수 있습니다.
```yml
spring:
  batch:
    job:
      enabled: true 
      names: ${job.name:NONE} 
    jdbc:
      initialize-schema: always
      table-prefix: SYSTEM_ 
```
+ enable
    - 기본값은 true
    - false는 스프링이 자동으로 Job을 실행하는 것을 막습니다.
+ names
    - enable이 true일 때, 자동으로 전체 Job이 실행되는데 특정 Job만 실행하도록 하는 옵션입니다.
    - 여러 개 작성 시 쉼표(,)로 구분합니다.
    - 하드코딩으로 넣어도 되지만 위처럼 사용하면 실행 시 argument로 주입받아서 사용할 수 있습니다.
        - ex) \--job.name=job1,job2
        - job1, job2만 실행하고 존재하지 않으면 실행하지 않습니다.
        - 만약 아무런 argument가 주입되지 않는다면 아무런 배치도 실행되지 않습니다.
+ initialize-schema
    - EMBEDDED : __내장 DB일 때만__ 실행되며 스키마가 자동으로 생성(Default)
    - ALWAYS : 스크립트 항상 실행
    - NEVER 
        - 스크립트 항상 실행 안하기 때문에 내장 DB일 경우 스크립트가 생성이 안되기 때문에 오류 발생
        - __운영에서는 수동으로 스크립트 생성 후 설정하는 것을 권장__
+ table-prefix
    - 기본값은 BATCH_
    - 테이블의 앞의 문자를 변경하는 옵션입니다.

<Br>

## JobBuilderFactory, JobBuilder
---
스프링 배치는 Job을 쉽게 생성 및 설정할 수 있도록 Util 성격의 빌더 클래스들을 제공합니다.  
+ JobBuilderFactory
    - JobBuilder를 생성하는 팩토리 클래스로 get 메서드를 제공합니다.
    - JobBuilderFactory.get("jobName")
        - 내부적으로 JobBuilder가 jobName을 잡의 이름으로 하여 잡을 생성하도록 로직이 구성되어 있습니다.
+ JobBuilder
    - Job을 구성하는 설정 조건에 따라 두 개의 하위 빌더 클래스를 생성하여 직접 Job을 생성하지 않고 Job 생성을 위임합니다.
    - SimpleJobBuilder
        - SimpleJob을 생성하는 Builder 클래스
        - Job 실행과 관련된 여러 설정 API 제공
    - FlowJobBuilder
        - FlowJob을 생성하는 Builder 클래스
        - 내부적으로 FlowBuilder를 반환함으로써 Flow 실행과 관련된 여러 설정 API를 제공

### 아키텍처
![그림1](https://github.com/backtony/blog-code/blob/master/spring/img/batch/3/3-1.PNG?raw=true)  

1\. JobBuilderFactory에서 get메서드를 통해 JobBuilder 클래스를 생성합니다.  
2-1. JobBuilder에서 start(step)를 사용하면 JobBuilder 내부적으로 SimpleJobBuilder를 생성하여 SimpleJob을 생성합니다.  
2-2. JobBuilder에서 start(flow) 또는 flow(step)를 사용하면 JobBuilder 내부적으로 FlowJobBuilder를 생성하여 FlowJob을 생성합니다. FlowJobBuilder는 내부적으로 JobFlowBuilder 클래스를 생성하여 Flow를 생성합니다.  
3\. SimpleJobBuilder와 JobFlowBuilder는 위 그림과 같이 다양한 API를 제공합니다.

### 상속 구조
![그림2](https://github.com/backtony/blog-code/blob/master/spring/img/batch/3/3-2.PNG?raw=true)  

JobBuilderFactory는 JobBuilder 클래스를 생성하는 역할을 합니다.  
JobBuilder 클래스는 JobBuilderHelper를 상속받고 있습니다.  
JobBuilderHelper 클래스는 내부 static 클래스로 CommonJobProperties를 갖고 있으며 이를 필드값으로 갖고 있는데 Job을 생성하는 공통적인 내용을 갖고 있다고 보면 됩니다.  
JobFactory에서 JobBuilder를 생성할 때 생성자의 인자로 JobRepository를 넘기는데 이게 JobBuilder에 CommonJobProperties에 담기게 됩니다.  
JobBuilder가 생성하는 SimpleJobBuilder와 FlowJobBuilder는 JobBuilderHelper를 상속받고 있습니다.  
SimpleJobBuilder와 FlowJobBuilder는 각각의 Job을 생성하게 되고 JobRepository는 빌더 클래스를 통해 Job 객체에 전달되어 메타데이터를 기록하는데 사용됩니다.  

<br>

## SimpleJob
---
### 기본 개념
+ SimpleJob은 __Step을 실행시키는 Job 구현체로서 SimpleJobBuilder에 의해 생성__ 됩니다.
+ 여러 단계의 Step으로 구성할 수 있으며 Step을 순차적으로 실행시킵니다.
+ 모든 step의 실행이 성공적으로 완료되어야 Job이 성공적으로 완료됩니다.
+ 맨 마지막에 실행한 Step의 BatchStatus가 Job의 최종 BatchStatus가 됩니다.

### 기본 API
![그림3](https://github.com/backtony/blog-code/blob/master/spring/img/batch/3/3-3.PNG?raw=true)  

### validator()
![그림4](https://github.com/backtony/blog-code/blob/master/spring/img/batch/3/3-4.PNG?raw=true)  

+ Job 실행에 꼭 필요한 파라미터를 검증하는 용도로 사용됩니다.
+ DefaultJobParametersValidator 구현체를 지원하며, 좀 더 복잡한 제약조건이 있다면 인터페이스를 직접 구현할 수도 있습니다.

<br>

#### 동작 과정
![그림5](https://github.com/backtony/blog-code/blob/master/spring/img/batch/3/3-5.PNG?raw=true)  

기본적으로 제공하는 DefaultJobParametersValidator는 생성자의 인자로 반드시 있어야 하는 key값을 담고 있는 requiredKeys 배열과 있으나 없으나 상관 없는 optionalKeys 배열을 받습니다.  
만약 optionalKeys와 requiredKeys 둘 안에 없는 파라미터가 들어오면 예외를 뱉어냅니다.  

#### 실습
```java
// 커스텀해서 만든 Validator
public class CustomJobParametersValidator implements JobParametersValidator {
    @Override
    public void validate(JobParameters parameters) throws JobParametersInvalidException {
        if(parameters.getString("name") == null){
            throw new JobParametersInvalidException("name parameters is not found");
        }
    }
}
------------------------------------------------------------------------------------------------------------
@Slf4j
@Configuration
@RequiredArgsConstructor
public class HelloJobConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job helloJob() {
        return jobBuilderFactory.get("job1")
                .start(step1())
                // 커스텀 방식
                //.validator(new CustomJobParametersValidator())

                // defaultValidator
                .validator(new DefaultJobParametersValidator(new String[]{"name","date"},new String[]{"count"}))
                .build();
    }

    @Bean
    public Step step1() {
        return stepBuilderFactory.get("step1")
                .tasklet((stepContribution, chunkContext) -> {
                    System.out.println("step1 has executed");
                    return RepeatStatus.FINISHED;
                })
                .build();
    }
}
```
validator를 한 개이상 등록하게 되면 마지막에 등록한 것으로 처리된다.  

### preventRestart()
+ 기본적으로 Job은 실패할 경우 재시작이 가능한데(기본값 true) 해당 옵션을 false로 주면 Job의 재시작을 지원하지 않습니다.
+ false인 상태에서 재시작하려고 시도하면 JobRestartException이 발생합니다.

```java
@Slf4j
@Configuration
@RequiredArgsConstructor
public class HelloJobConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job helloJob() {
        return jobBuilderFactory.get("job1")
                .start(step1())
                .preventRestart()
                .build();
    }
    ....
}
```
위 코드처럼 preventRestart()를 호출하면 옵션이 false인 상태로 들어갑니다.  
preventRestart()를 호출하지 않으면 기본값 true로 설정됩니다.  


### incrementer()
JobParameters로 잡이 성공했다면 기본적으로 동일한 JobParameters로 동일한 Job을 실행시킬 수 없습니다.  
하지만 같은 JobParemeters로 계속 Job을 실행해야 할 때가 있습니다.  
이때 JobIncrementer를 사용합니다.  
즉, __기존의 JobParameter 변경없이 Job을 여러 번 시작하고자 할 때 사용__ 합니다.  
RunIdIncrementer 구현체를 지원하며 JobParametersIncrementer 인퍼테이스를 직접 구현할 수도 있습니다.  
__주의할 점은 Incrementer는 특별한 일을 하는 것이 아니라 실행되기 위해 주입된 파라미터에 추가로 파라미터를 더해주는 역할일 뿐이므로 validator을 사용할 경우 추가해주는 값을 명시해줘야 합니다.__  

```java
public class CustomJobParametersIncrementor implements JobParametersIncrementer {

    private static final SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd-hhmmss");

    @Override
    public JobParameters getNext(JobParameters parameters) {
        String id = format.format(new Date());
        return new JobParametersBuilder().addString("custom",id).toJobParameters();
    }
}
----------------------------------------------------------------------------------
@Slf4j
@Configuration
@RequiredArgsConstructor
public class HelloJobConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job helloJob() {
        return jobBuilderFactory.get("job1")
                .start(step1())
                .incrementer(new CustomJobParametersIncrementor())
                //.incrementer(new RunIdIncrementer())
                .build();
    }
    ......
}
```
구현체로 지원하는 RunIdIncrementer()의 경우 run.id 라는 키로 실행될 때마다 1부터 증가하여 파라미터에 값을 채워넣습니다.


### SimpleJob 흐름도
![그림6](https://github.com/backtony/blog-code/blob/master/spring/img/batch/3/3-6.PNG?raw=true)  

JobLauncher는 SimpleJob과 Jobparameters를 갖고 Job을 실행시킵니다.  
그 사이에 Job이 실행될 때 필요한 메타 데이터들을 생성합니다.(JobInstance, JobExecution, ExecutionContext)  
Job이 실행되기 전에 JobListener에서 beforeJob이 호출됩니다.  
각각 Step이 실행되면서 StepExecution과 ExecutionContext가 생성됩니다.  
Step이 모두 종료되면 JobListener에서 afterJob이 호출됩니다.  
마지막 Step 단계의 BatchStatus와 ExitStatus를 Job에 업데이트시키고 끝납니다.  



<Br><Br>

__참고__  
<a href="https://www.inflearn.com/course/%EC%8A%A4%ED%94%84%EB%A7%81-%EB%B0%B0%EC%B9%98#" target="_blank"> 스프링 배치 - Spring Boot 기반으로 개발하는 Spring Batch</a>   



