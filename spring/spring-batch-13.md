# Spring Batch - 테스트 및 운영

## @SpringBatchTest
---
자동으로 ApplicationContext에 테스트에 필요한 여러 유틸 Bean을 등록해주는 애노테이션입니다.  
+ JobLauncherTestUtils
    - launchJob(), launchStep() 과 같은 스프링 배치 테스트에 필요한 유틸성 메서드 지원
+ JobRepositoryTestUtils
    - JobRepository를 사용해서 JobExecution을 생성 및 삭제 기능 메서드 지원
+ StepScopeTestExecutionListener
    - @StepScope 컨텍스트를 생성해주며 해당 컨텍스트를 통해 JobParameter 등을 단위 테스트에서 DI 받을 수 있습니다.
+ JobScopeTestExecutionListener
    - @JobScope 컨텍스트를 생성해 주며 해당 컨텍스트를 통해 JobParameter 등을 단위 테스트에서 DI 받을 수 있습니다.

사용하기 위해서는 의존성을 추가해줘야 합니다.
```
testImplementation 'org.springframework.batch:spring-batch-test'
```

### JobLauncherTestUtils API
![그림1](https://github.com/backtony/blog-code/blob/master/spring/img/batch/13/13-1.PNG?raw=true)  

### 예시
```java
// JobLauncherTestUtils, JobRepositoryTestUtils 등을 제공하는 애노테이션
@SpringBatchTest 
// Job 설정 클래스 지정, 통합 테스트를 위한 여러 의존성 빈들을 주입받기 위한 애노테이션
// 잡은 한개만 지정해야 함
@SpringBootTest(classes = {SimpleJobConfiguration.class,TestBatchConfig.class})
public class SimpleJobTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;


    @Test
    @DisplayName("simple job success")
    void simpleJob_success() throws Exception{
        // given
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("name", "user1")
                .addLong("date", new Date().getTime())
                .toJobParameters();

        // when
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        // then
        assertEquals(BatchStatus.COMPLETED,jobExecution.getStatus());
        assertEquals(ExitStatus.COMPLETED,jobExecution.getExitStatus());
    }

    @Test
    @DisplayName("step success")
    void step_success() throws Exception{
        //given
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("name", "user1")
                .addLong("date", new Date().getTime())
                .toJobParameters();

        //when
        JobExecution jobExecution = jobLauncherTestUtils.launchStep("step");

        //then
        assertEquals(BatchStatus.COMPLETED,jobExecution.getStatus());
    }

}
------------------------
@Configuration
@EnableAutoConfiguration // 자동 설정
@EnableBatchProcessing // 배치 환경 및 설정 초기화 애노테이션
public class TestBatchConfig {
}
```
간단하게 Job과 Step을 테스트하는 코드 입니다.  
<br>

## JobExplorer
---
+ JobRepository의 readOnly 버전으로 수정, 삭제는 안되고 읽기만 가능합니다.
+ 대부분의 배치 프레임워크 컴포넌트가 JobRepository를 사용해 관련 정보에 접근하지만, JobExplore는 바로 DB에 접근합니다.

쉽게 생각하면, 읽기 전용으로 배치 메타데이터를 찾을 때 사용한다고 보면 됩니다.


### API

메서드|설명
---|---
findRunningJobExecutions(java.lang.String jobName)|종료 시간이 존재하지 않는 모든 JobExecution을 반환
findJobInstancesByJobName(java.lang.String jobName, int start, int count)|전달받은 이름을 가진 JobInstance 목록을 반환
getJobExecution(java.lang.Long executionId)|전달받은 ID를 가진 JobExecution을 반환하며 존재하지 않으면 null 반환
getJobExecutions(JobInstance jobInstance)|전달받은 JobInstance와 관련된 모든 JobExecution 목록을 반환
getJobInstance(java.lang.Long instanceId)|전달받은 ID를 가진 JobInstance를 반환하며, 존재하지 않는다면 null반환
getJobInstances(java.lang.String jobName, int start, int count)|전달받은 인덱스부터 지정한 개수만큼의 범위 내에 있는 JobInstnace를 반환한다. 마지막 파라미터는 반환할 최대 인스턴스 개수를 의미
getJobInstanceCount(java.lang.String jobName|전달받은 잡 이름으로 생성된 JobInstance 개수를 반환
getJobNames()|JobRepository에 저장돼 있는 고유한 모든 잡 이름을 알파벳 순서대로 반환
getStepExecution(java.lang.Long jobExecutionId, java.lang.Long stepExecutionId)|전달받은 StepExecution의 ID와 부모 JobInstance의 ID를 갖는 StepExecution반환

### 예시
```java
public class ExploringTasklet implements Tasklet {

    private JobExplorer explorer;

    public ExploringTasklet(JobExplorer explorer) {
        this.explorer = explorer;
    }

    public RepeatStatus execute(StepContribution stepContribution,
                                ChunkContext chunkContext) {

        String jobName = chunkContext.getStepContext().getJobName();


        List<JobInstance> instances =
                explorer.getJobInstances(jobName,
                        0,
                        Integer.MAX_VALUE);

        System.out.println(
                String.format("There are %d job instances for the job %s",
                        instances.size(),
                        jobName));

        System.out.println("They have had the following results");
        System.out.println("************************************");

        for (JobInstance instance : instances) {
            List<JobExecution> jobExecutions =
                    this.explorer.getJobExecutions(instance);

            System.out.println(
                    String.format("Instance %d had %d executions",
                            instance.getInstanceId(),
                            jobExecutions.size()));

            for (JobExecution jobExecution : jobExecutions) {
                System.out.println(
                        String.format("\tExecution %d resulted in Exit Status %s",
                                jobExecution.getId(),
                                jobExecution.getExitStatus()));
            }
        }

        return RepeatStatus.FINISHED;
    }
}
```


<br>

## JobRegistry
---
+ 생성된 Job을 자동으로 등록, 추적 및 관리하며 여러 곳에서 job을 생성한 경우 ApplicationContext에서 Job을 수집해서 사용할 수 있습니다.
+ 기본 구현체로 map 기반의 MapJobRegistry 클래스를 제공합니다.
    - jobName을 key로 하고 Job을 값으로 매핑합니다.
+ Job 등록
    - jobRegistryBeanPostProcessor을 빈으로 정의하면 빈후처리기에서 bean 초기화 시 자동으로 JobRegistry에 Job을 등록시켜 줍니다.

예시는 바로 아래 REST 방식 예시에서 사용하겠습니다.  

<Br>

## REST 방식으로 잡 실행하기
---
배치 잡을 실행할 때 REST API 방식을 사용할 수 있습니다.  
배치 잡을 즉시 실행할 수 있는 REST API가 존재하는 것은 아니고 직접 개발해야 합니다.  
JobLauncher 인터페이스는 잡을 실행시키는 인터페이스인데 run 메서드 하나만 존재합니다.  
스프링 배치는 기본적으로 유일한 JobLauncher 구현체인 SimpleJobLauncher을 제공하는데 SimpleJobLauncher는 잡 실행과 관련된 대부분의 요구 사항을 만족합니다.(동기 방식으로 동작합니다.)  
```java
@SpringBootApplication
@EnableBatchProcessing
public class BatchApplication {
    public static void main(String[] args) {
        SpringApplication.run(BatchApplication.class, args);
    }
}

// application.yml
spring:  
  batch:
    job:
      enabled: false

// build.gradle
implementation 'org.springframework.boot:spring-boot-starter-batch'
implementation 'org.springframework.boot:spring-boot-starter-jdbc'
implementation 'org.springframework.boot:spring-boot-starter-web'
```
@EnableBatchProcessing 를 추가해주면 스프링 배치가 제공하는 SimpleJobLauncher를 바로 사용할 수 있습니다.  
REST 로 잡을 동작시키기 위해서 enable을 false로 세팅해줍니다.  
<br>

```java
@Configuration
@RequiredArgsConstructor
public class SimpleJobConfiguration {
    private int chunkSize = 10;
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory entityManagerFactory;
    private final JobRegistry jobRegistry;

    @Bean
    public Job helloJob() {
        return jobBuilderFactory.get("job")
                .incrementer(new RunIdIncrementer()) // 동작 안함 -> 아래서 설명
                .start(step())
                .build();
    }

    @Bean
    public Step step() {
        return stepBuilderFactory.get("step")
                .<Customer, Customer>chunk(chunkSize)
                .reader(customItemReader())
                .writer(items -> System.out.println("items = " + items))
                .build();
    }


    @Bean
    public JpaPagingItemReader<Customer> customItemReader() {
        return new JpaPagingItemReaderBuilder<Customer>()
                .name("customItemReader")
                .pageSize(chunkSize)
                .entityManagerFactory(entityManagerFactory)
                .queryString("select c from Customer c order by c.id")
                .build();

    }

    @Bean
    public BeanPostProcessor jobRegistryBeanPostProcessor(){
        JobRegistryBeanPostProcessor jobRegistryBeanPostProcessor = new JobRegistryBeanPostProcessor();
        jobRegistryBeanPostProcessor.setJobRegistry(jobRegistry);
        return jobRegistryBeanPostProcessor;
    }
}
```
데이터베이스에서 Customer을 읽어와서 그대로 출력하는 배치입니다.  
jobRegistryBeanPostProcessor메서드는 jobRegistry를 사용하기 위해 빈으로 등록하는 작업입니다.  
```java
@RestController
@Configuration
@RequiredArgsConstructor
public class RestBatchController {


    private final JobLauncher jobLauncher;
    private final JobRegistry jobRegistry;
    private final JobExplorer jobExplorer;

    @PostMapping("/batch")
    public ExitStatus runJob(@RequestBody JobLaunchRequest request) throws Exception {
        // 잡 이름의 빈 검색
        Job job = jobRegistry.getJob(request.getName());

        // job 파라미터 추출
        // builder의 인자 -> (현재 만드는 잡 파라미터에 추가하고 싶은 잡파라미터, jobExplorer)
        // jobExplorer가 필요한 이유는 다음 파라미터를 위해 기존 run.id를 가져오기 위해서 필요함
        JobParameters jobParameters = new JobParametersBuilder(request.getJobParameters(),jobExplorer)
                        .getNextJobParameters(job)
                        .toJobParameters();

        // 잡 실행하고 ExitStatus 반환
        return this.jobLauncher.run(job, jobParameters).getExitStatus();
    }

    @Setter
    @Getter
    public static class JobLaunchRequest {
        private String name;
        private Properties jobParameters;

        public JobParameters getJobParameters() {
            return new JobParametersBuilder(jobParameters).toJobParameters();
        }
    }
}

// post 요청
{
    "name":"job",
    "jobParameters":{
        "message":"message",
        "foo":"bar"
    }
}
```
JobLauncher는 SimpleJobLauncher이 제공되는데 SimpleJobLauncher는 전달받은 JobParameters의 조작을 지원하지 않습니다.  
따라서 잡에 incrementer를 달아도 동작하지 않습니다.  
이 경우에 잡에 JobParametersIncrementer를 사용해야 한다면, 해당 파라미터가 SimpleJobLauncher로 전달되기 전에 적용해야 합니다.  
즉, jobLuancher로 잡을 실행할 때 애초에 수정해서 넘겨줘야된다는 의미입니다.  
getNextJobParameters 메서드는 RunIdIncrementer메서드와 똑같은 역할을 하기 때문에 이를 통해 JobParameter를 만들 때 해결할 수 있습니다.  

<br>

## 쿼츠를 사용한 스케줄링
---
쿼츠는 오픈소스 스케줄러입니다.  
자바 환경의 규모와 상관없이 사용할 수 있고 안정적이고 활성화된 커뮤니티의 지원을 받을 수 있으며, 잡 실행에 유용한 스프링 부트 지원과 같이 오래전부터 스프링과 연동을 지원하고 있습니다.


### 구성
![그림2](https://github.com/backtony/blog-code/blob/master/spring/img/batch/13/13-2.PNG?raw=true)  
쿼츠는 스케줄러, 잡, 트리거라는 세 가지 주요 컴포넌트를 가집니다.  
+ Scheduler
    - 스케줄러는 SchedulerFactory를 통해서 가져올 수 있으며 JobDetails 및 트리거의 저장소 기능을 합니다.
    - 스케줄러는 연관된 트리거가 작동할 때 잡을 실행하는 역할을 합니다. (잡은 실행할 작업의 단위)
+ Trigger
    - 작업 실행 시점을 정의합니다.
    - 트리거가 작동돼 쿼츠에게 잡을 실행하도록 지시하면 잡의 개별 실행을 정의하는 Job Details 객체가 생성됩니다.
+ Job Details
    - 쿼츠 잡에 대한 참조

쿼츠에서 잡과 JobDetails 객체를 정의하는 모델은 스프링 배치에서 잡과 JobInstance를 정의하는 방식과 매우 유사합니다.

Spring과 Quartz를 연동해서 사용하는 로직은 다음과 같습니다.
1. 스프링 배치 잡을 작성한다.
2. 스프링 QuartzJobBean을 사용해 스프링 배치 잡을 작동시키는 쿼츠 잡을 작성한다.
3. 쿼츠 JobDeatil을 생성하도록 스프링이 제공하는 JobDetailBean을 구성한다.
4. 잡 실행 시점을 정의하도록 트리거를 구성한다.


### 예시
#### 의존성 추가
```
implementation 'org.springframework.boot:spring-boot-starter-quartz'
```

#### 스프링 배치 잡
```java
@Configuration
@RequiredArgsConstructor
public class QuartzConfig {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Step step1() {
        return this.stepBuilderFactory.get("step1")
                .tasklet((stepContribution, chunkContext) -> {
                    System.out.println("step1 ran!");
                    return RepeatStatus.FINISHED;
                }).build();
    }

    @Bean
    public Job job() {
        return this.jobBuilderFactory.get("job")
                .incrementer(new RunIdIncrementer())
                .start(step1())
                .build();
    }

}
```
쿼츠에서 실행시킬 일반적인 스프링 배치 잡을 만듭니다.

#### 쿼츠 잡
```java
@RequiredArgsConstructor
public class BatchScheduledJob extends QuartzJobBean {

    private final Job job;
    private final JobExplorer jobExplorer;
    private final JobLauncher jobLauncher;

    @Override
    protected void executeInternal(JobExecutionContext context) {
        // 파라미터 추출
        JobParameters jobParameters = new JobParametersBuilder(this.jobExplorer)
                .getNextJobParameters(this.job)
                .toJobParameters();

        // 잡 실행
        try {
            this.jobLauncher.run(this.job, jobParameters);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```
앞서 일반적인 스프링 배치 잡을 만들어줬습니다.  
위 코드는 해당 스프링 배치 잡을 실행시키는 로직은 쿼츠 잡 안에 넣은 것입니다.  
QuartzJobBean 클래스를 상속받고 있는데 이는 쿼츠 잡 실행에 필요한 대부분의 배치 기능을 제공합니다.  
executeInternal메서드만 재정의하여 목적에 맞게 확장하면 됩니다.  
executeInternal메서드는 스케줄링 된 이벤트가 발생할 때마다 한 번씩 호출됩니다.  

#### 스케줄러 구성
```java
@Configuration
public class QuartzConfiguration {

    // 쿼츠 잡 빈 등록
    @Bean
    public JobDetail quartzJobDetail() {
        return JobBuilder.newJob(BatchScheduledJob.class) // 쿼츠 잡
                .storeDurably() // 트리거가 존재하지 않더라도 해당 쿼츠 잡이 삭제되지 않도록 하는 옵션
                .build();
    }

    // 언제 쿼츠 잡이 동작할 것인지 등록하는 트리거
    @Bean
    public Trigger jobTrigger() {
        // 언제 동작할지 스케줄 결정
        SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder.simpleSchedule()
                .withIntervalInSeconds(5).withRepeatCount(4);

        return TriggerBuilder.newTrigger()
                .forJob(quartzJobDetail()) // 쿼츠 잡
                .withSchedule(scheduleBuilder) // 스케줄
                .build(); // 트리거 생성
    }
}
```
앞서 스프링 배치 잡, 쿼츠 잡도 만들었습니다.  
마지막으로 쿼츠 잡을 빈으로 등록하고 해당 쿼츠 잡이 언제 동작할지 트리거를 정해주는 코드입니다.  
설명은 주석만으로도 충분히 이해할 수 있을 것입니다.  



<Br><Br>

__참고__  
<a href="https://www.inflearn.com/course/%EC%8A%A4%ED%94%84%EB%A7%81-%EB%B0%B0%EC%B9%98#" target="_blank"> 스프링 배치 - Spring Boot 기반으로 개발하는 Spring Batch</a>   



