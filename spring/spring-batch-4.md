# Spring Batch - Step


## StepBuilderFactory와 StepBuilder
---
+ StepBuilderFactory
    - StepBuilder를 생성하는 팩토리 클래스로서 get 메서드를 제공합니다.
    - StepBuilderFactory.get("stepName")
        - stepName으로 Step을 생성하도록 StepBuilder에게 넘깁니다.
+ StepBuilder
    - Step을 구성하는 설정 조건에 따라 다섯 개의 하위 빌더 클래스를 생성하고 Step 생성을 위임합니다.
    - TaskletStepBuilder
        - Tasklet을 생성하는 기본 빌더 클래스
    - SimpleStepBuilder
        - Tasklet을 생성하며 내부적으로 청크기반 작업을 처리하는 ChunkOrientedTasklet 클래스를 생성합니다.
    - PartitionStepBuilder
        - PartitionStep을 생성하며 멀티 스레드 방식으로 Job을 실행합니다.
    - JobStepBuilder
        - JobStep을 생성하여 Step 안에서 Job을 실행합니다.
    - FlowStepBuilder
        - FlowStep을 생성하여 Step 안에서 Flow를 실행합니다.


### 아키텍처
![그림1](https://github.com/backtony/blog-code/blob/master/spring/img/batch/4/4-1.PNG?raw=true)  

JobBuilderFactory의 구조와 유사합니다.  
SteoBuilderFactory에서 get을 호출하면 내부적으로 StepBuilder를 호출합니다.  
StepBuilder는 API의 파라미터 타입과 구분에 따라 적절한 하위 빌더를 생성합니다.

### 상속 구조
![그림2](https://github.com/backtony/blog-code/blob/master/spring/img/batch/4/4-2.PNG?raw=true)  

상속 구조도 JobBuilderFactory와 유사합니다.  
StepBuilder는 StepBuilderHelper를 상속받고 있고 StepBuilderHelper는 공통적으로 Step 생성하는데 필요한 내용들을 담고 있습니다.  
각각의 하위 빌더들은 StepBuilderHelper를 상속받고 있고 각각에 맞는 Step들을 생성합니다.  
StepBuilder는 API 설정에 따라서 하위 빌더들을 생성하는 역할을 하고 있습니다.  


<br>

## TaskletStep
---
### 기본 개념
+ __스프링 배치에서 제공하는 Step의 구현체로서 Tasklet을 실행시키는 도메인 객체__
+ TaskletStep은 Tasklet을 실행시키는데 있어 중간에 RepeatTemplate을 사용하여 Tasklet의 구문을 트랜잭션 경계 내에서 반복해서 실행시킵니다.
+ Task 기반과 Chunk 기반으로 나눠서 Tasklet을 실행합니다.


### Task와 Chunk 기반 비교
![그림3](https://github.com/backtony/blog-code/blob/master/spring/img/batch/4/4-3.PNG?raw=true)  
스프링 배치에서 Step의 실행 단위는 크게 Step과 Chunk로 나눠집니다.
+ Chunk 기반
    - 하나의 큰 덩어리를 n개씩 나눠서 실행한다는 의미로 대량 처리를 하는 경우에 적합합니다.
    - ItemReader, ItemProcessor, ItemWriter를 사용하며 청크 기반 전용 Tasklet인 ChunkOrientedTasklet 구현체가 제공됩니다.
+ Task 기반
    - 단일 작업 기반으로 처리되는 경우에 적합합니다.
    - 주로 Tasklet 구현체를 만들어서 사용합니다.

### 기본 API 소개
![그림4](https://github.com/backtony/blog-code/blob/master/spring/img/batch/4/4-4.PNG?raw=true)  


### tasklet()
+ Tasklet 타입의 클래스를 세팅하는 API입니다.  
+ TaskletStepBuilder가 반환되어 관련 API를 설정할 수 있습니다.  

#### Tasklet
![그림5](https://github.com/backtony/blog-code/blob/master/spring/img/batch/4/4-5.PNG?raw=true)  
+ 인터페이스로 execute 단일 메서드를 제공합니다.
+ __Step 내에서 구성되고 실행되는 도메인 객체__ 로 주로 단일 테스크를 수행하기 위한 것입니다.
+ TaskletStep에 의해 반복적으로 수행되며 반환값에 따라 계속 수행 혹은 종료 여부가 결정됩니다.
    - RepeatStatus : Tasklet의 반복 여부 상태값
        - FINISHED : 반복 종료(null로 반환시도 이 값으로 적용)
        - CONTINUABLE : 무한 반복
+ 익명 클래스 혹은 구현 클래스로 만들어서 사용합니다.
+ Step에 __오직 하나의 Tasklet 설정이 가능__ 하며 두 개 이상 설정할 경우 마지막 설정한 Tasklet이 적용됩니다.
        
<br>

#### 예시
```java
public class CustomTasklet implements Tasklet {
    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        System.out.println("tasklet 1 complete");
        return RepeatStatus.FINISHED;
    }
}
----------------------------------------------
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
                .incrementer(new RunIdIncrementer())
                .build();
    }

    @Bean
    public Step step1() {
        return stepBuilderFactory.get("step1")
                .tasklet(new CustomTasklet())
                .build();
    }
}
```

### startLimit()
+ Step의 실행 횟수를 조정할 때 사용됩니다.
+ Step 마다 설정할 수 있습니다.
+ 설정 값을 초과한다면 StartLimitExceededException이 발생합니다.
+ 기본값은 Integer.Max_VALUE 입니다.

<br>

__예시__  
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
                .incrementer(new RunIdIncrementer())
                .build();
    }

    @Bean
    public Step step1() {
        return stepBuilderFactory.get("step1")
                .tasklet(new CustomTasklet())
                .startLimit(3) // step1은 3번만 실행이 가능하다.
                .build();
    }
}
```

### allowStartIfComplete()
+ 실패로 인한 재시작이 가능한 Job이 다시 실행될 때, 이전 실행에서 Step의 성공 여부와 관계 없이 항상 Step을 실행하기 위한 설정입니다.  
+ 즉, 잡이 실행될때마다 무조건 해당 Step이 실행되게 하는 옵션입니다.  
+ 실행마다 유효성을 검증하는 Step이나 꼭 필요한 사전 작업인 Step의 경우 사용합니다.  
+ 기본값은 false 입니다.  

<br>

__예시__  
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
                .incrementer(new RunIdIncrementer())
                .build();
    }

    @Bean
    public Step step1() {
        return stepBuilderFactory.get("step1")
                .tasklet(new CustomTasklet())
                .allowStartIfComplete(true) // 잡이 실행될때마다 무조건 실행됩니다.
                .build();
    }
}
```

### TaskletStep 흐름도
![그림6](https://github.com/backtony/blog-code/blob/master/spring/img/batch/4/4-6.PNG?raw=true)  

Job이 TaskletStep을 호출하는 사이에서 StepExecution과 ExecutionContext가 생성되어 TaskletStep에 전달합니다.  
Tasklet이 실행되기 전에 StepListener의 beforeStep이 호출됩니다.
Tasklet 작업이 끝나면 StepListener의 afterStep이 호출됩니다.  
StepExecution에 Step의 완료 상태를 업데이트합니다.  
참고로, StepExecutionListener 호출 후 추가적으로 exitStatus 상태를 업데이트할 수 있습니다.  

<Br>

## JobStep
---
### 기본 개념
+ 외부의 Job을 포함하고 있는 하나의 Step
+ 외부의 Job이 실패하면 해당 Step이 실패하므로 해당 Step을 품고있는 기본 Job도 실패합니다.
+ 모든 메타데이터는 기본 Job과 외부 Job 별로 각각 저장됩니다.
+ 커다란 시스템을 작은 모듈로 쪼개고 job의 흐름을 관리하고자 할 때 사용합니다.
+ Step의 동작이었던 Tasklet 대신 Job을 넣었다고 생각하면 이해가 쉽습니다.

### API 소개
![그림7](https://github.com/backtony/blog-code/blob/master/spring/img/batch/4/4-7.PNG?raw=true)  
parameterExtractor이 이해가 잘 안될텐데 예시에서 살펴보겠습니다.  

### 예시
```java
@Slf4j
@Configuration
@RequiredArgsConstructor
public class HelloJobConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job helloJob() {
        return jobBuilderFactory.get("parentJob")
                .start(jobStep(null)) // null로 넣으면 DI로 알아서 주입
                .incrementer(new RunIdIncrementer())
                .build();
    }


    @Bean
    public Step jobStep(JobLauncher jobLauncher) {
        return stepBuilderFactory.get("jobStep")
                .job(childJob())
                .launcher(jobLauncher)
                .parametersExtractor(jobParametersExtractor())
                // 리스터를 통해서 Step이 시작하기 전에 Step의 ExecutionContext에 name과 backtony 키밸류값 등록
                .listener(new StepExecutionListener() {
                    @Override
                    public void beforeStep(StepExecution stepExecution) {
                        stepExecution.getExecutionContext().putString("name", "backtony");
                    }

                    @Override
                    public ExitStatus afterStep(StepExecution stepExecution) {
                        return null;
                    }
                })
                .build();
    }

    // Step의 ExecutionContext에 name의 key를 갖는 키밸류 값을 꺼내서 
    // JobStep의 JobParameters로 만들어서 넘긴다.
    private JobParametersExtractor jobParametersExtractor() {
        DefaultJobParametersExtractor extractor = new DefaultJobParametersExtractor();
        extractor.setKeys(new String[]{"name"});
        return extractor;
    }

    @Bean
    public Job childJob() {
        return jobBuilderFactory.get("childJob")
                .start(step1())
                .build();

    }

    @Bean
    public Step step1() {
        return stepBuilderFactory.get("step1")
                .tasklet((stepContribution, chunkContext) -> {
                    JobParameters jobParameters = contribution.getStepExecution().getJobExecution().getJobParameters();
                    System.out.println(jobParameters.getString("name"));
                    return RepeatStatus.FINISHED;
                })
                .build();
    }
}
```
JobStep의 경우 Step 안에서 Job이 실행되기 때문에 Job이 실행되기 위해 JobLauncher와 JobParameters가 필요합니다.  
인자로 null을 넘겨주면 jobLauncher의 경우 DI를 통해 자동 주입되서 바로 사용하면 됩니다.  
__parametersExtractor는 StepExecution의 ExecutionContext에 있는 키-벨류값을 추출해서 jobParameters로 만들어 job으로 넘겨줍니다.__  
위 코드에서는 리스너를 통해서 StepExecution의 ExecutionContext에 name키값으로 값을 저장해주었고 jobParametersExtractor를 통해 추출해서 jobParameters로 만들었습니다.  
만약 추출했는데 값이 없다면 무시되고 진행됩니다.  




<Br><Br>

__참고__  
<a href="https://www.inflearn.com/course/%EC%8A%A4%ED%94%84%EB%A7%81-%EB%B0%B0%EC%B9%98#" target="_blank"> 스프링 배치 - Spring Boot 기반으로 개발하는 Spring Batch</a>   



