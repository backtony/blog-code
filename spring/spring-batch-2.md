# Spring Batch - 도메인 이해

## Job
---
### 기본 개념
+ 배치 계층 구조에서 가장 상위에 있는 개념으로서 __하나의 배치작업 자체__ 를 의미합니다.
+ Job Configuration을 통해 생성되는 객체 단위로서 __배치 작업을 어떻게 구성하고 실행할 것인지 전체적으로 설정하고 명세해 놓은 객체__ 입니다.
+ 배치 Job을 구성하기 위한 최상위 인터페이스이며 스프링 배치가 기본 구현체(SimpleJob, FlowJob)를 제공합니다.
    - SimpleJob
        - 순차적으로 Step을 실행시키는 Job
        - 가장 보편적이고 모든 Job에서 사용할 수 있는 표준 기능 제공
    - FlowJob
        - 특정 조건과 흐름에 따라 Step을 구성하여 실행시키는 Job
        - Flow 객체를 실행시켜서 작업을 진행        
+ Job은 여러 Step을 포함하고 있는 컨테이너로서 반드시 __한 개 이상의 Step__ 으로 구성되어야 합니다.


### 동작 과정 및 구조
  
![그림1](https://github.com/backtony/blog-code/blob/master/spring/img/batch/2/2-1.PNG?raw=true)  
배치 잡을 실질적으로 실행하는 주체는 __JobLuancher__ 입니다.  
잡을 실행시킬 때 필요한 인자가 Job과 JobParameters 입니다.  
JobLuancher에서 job, paramegers를 인자로 받아 run 함수를 호출하면 Job은 내부적으로 execute 메서드를 호출해서 각각의 Step을 실행합니다.  
<br>

그림의 오른편은 Job의 상속 구조입니다.  
Job은 최상이 인터페이스로 execute 라는 Job 실행 메서드가 있습니다.  
Job의 구현체로 AbstractJob 추상 클래스가 있고, AbstractJob를 상속하여 재정의한 SimpleJob과 FlowJob가 있습니다.  


<br>

## JobInstance
---
### 기본 개념
+ Job이 실행될 때 생성되는 __Job의 논리적 실행 단위 객체__ 로서 __고유하게 식별 가능한 작업 실행__ 을 나타냅니다.
+ Job의 설정과 구성은 동일하지만 Job이 실행되는 시점에 처리하는 내용이 다르기 때문에 Job의 실행을 구분해야 합니다.
    - 예를 들어, 하루에 한번씩 배치 Job이 실행된다면 매일 실행되는 각각의 Job은 JobInstance로 구분됩니다.
+ JobInstance 생성 및 실행
    - 처음 시작하는 Job + JobParameters 일 경우 새로운 JobInstance를 생성합니다.
    - 이전과 동일한 Job + JobParameters으로 실행할 경우 이미 존재하는 JobInstance를 리턴합니다.
        - 내부적으로 JobName + jobKey(JobParameters의 해시값)을 통해서 존재하던 JobInstance를 얻습니다.
        - 이전 수행이 실패했을 경우에는 다시 수행이 가능하지만, 이전 수행이 성공했다면 다시 수행할 수 없습니다.(같은 내용을 반복할 필요가 없기 때문)
+ Job : JobInstance = 1:M 관계

### BATCH_JOB_INSTANCE 테이블과 매핑
방금 위에서도 언급했지만, JOB_NAME(Job)과 JOB_KEY(JobParameters 해시값)가 동일한 데이터는 중복해서 저장할 수 없습니다.  
즉, __JobInstance는 중복되지 않는 오직 유일한 하나의 값만 DB에 저장__ 됩니다.  

### 동작 과정
![그림2](https://github.com/backtony/blog-code/blob/master/spring/img/batch/2/2-2.PNG?raw=true)  

1. JobLauncher가 Job과 JobParameters를 인자로 run 메서드를 호출합니다.
2. JobRepository를 통해 Job과 JobParameters를 얻어 DB로부터 해당 값을 갖는 JobInstance가 존재하는지 여부를 확인합니다.
    - JobRepository : 잡 실행 중 발생하는 메타 데이터를 DB에 업데이트 및 저장하는 역할을 하는 클래스
3. 없다면 새롭게 JobInstance를 생성합니다.
4. 존재한다면 기존 JobInstance를 리턴합니다. 이때는 이미 처리가 완료된 작업일 경우 예외가 발생합니다.
    


<br>

## JobParameter
---
### 기본 개념
+ __Job을 실행할 때 함께 포함되어 사용되는 파라미터를 가진 도메인 객체__ 
+ 하나의 Job에 존재할 수 있는 여러 개의 JobInstance를 구분하기 위한 용도
+ JobParameters와 JobInstance는 1:1관계

### 생성 및 바인딩
+ 애플리케이션 실행 시 주입
    - java -jar XXX.jar requestDate=20211231
+ 코드로 생성
    - JobParameterBuilder, DefaultJobParemetersConverter를 사용
+ SpEL 이용
    - @Value("#{jobParameter[requestDate]}")
    - 애플리케이션 실행 시 주입받은 내용을 그대로 가져와서 사용하는 방식
    - @JobScope와 @StepScope 선언 필수

### BATCH_JOB_EXECUTION_PARAM 테이블과 매핑
JOB_EXECUTION : BATCH_JOB_EXECUTION_PARAM = 1:M 관계

### 구조
![그림3](https://github.com/backtony/blog-code/blob/master/spring/img/batch/2/2-3.PNG?raw=true)  
JobParameters는 JobParameter를 감싸고 있는 Wrapper 클래스 입니다.  
내부적으로는 LinkedHashMap으로 구성되어 있고 value 값으로 JobParameter를 갖습니다.  
JobParameter에는 파라미터 값(val)과 값에 대한 타입이 있습니다.  

### 예시
#### 코드 생성 방식
간단하게 잡 파라미터 코드 생성방식으로 만들어보겠습니다.
```java
@Component
@RequiredArgsConstructor
public class JobParameterTest implements ApplicationRunner {


    private final Job job;
    private final JobLauncher jobLauncher;


    @Override
    public void run(ApplicationArguments args) throws Exception {
        // job 파라미터 생성
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("name", "backtony")
                .addLong("seq", 1L)
                .addDate("date", new Date())
                .addDouble("age", 26.5)
                .toJobParameters();

        // 잡 실행
        jobLauncher.run(job, jobParameters);
    }
}
```
```java
@Slf4j
@Configuration
@RequiredArgsConstructor
public class HelloJobConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    // 잡 구성
    @Bean
    public Job helloJob() {
        return jobBuilderFactory.get("helloJob")
                .start(helloStep1())
                .build();
    }

    // 스텝 구성
    @Bean
    public Step helloStep1() {
        return stepBuilderFactory.get("helloStep1")
                .tasklet(new Tasklet() {
                    // 스텝이 하는 일 구성
                    @Override
                    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                        JobParameters jobParameters = contribution.getStepExecution().getJobExecution().getJobParameters();

                        // 파라미터가 잘 들어왔는지 확인
                        Date date = jobParameters.getDate("date");
                        Double age = jobParameters.getDouble("age");
                        Long seq = jobParameters.getLong("seq");
                        String name = jobParameters.getString("name");
                        log.info("date ={}", date);
                        log.info("age ={}", age);
                        log.info("seq ={}", seq);
                        log.info("name ={}", name);

                        return RepeatStatus.FINISHED;
                    }
                })
                .build();
    }
}
```
+ contribution.getStepExecution().getJobExecution().getJobParameters()
    - 주입받은 잡 파라미터를 꺼내서 사용할 수 있습니다.

#### 애플리케이션 실행 시 주입
```
java -jar xxx.jar name=backtony seq(long)=2L date(date)=2021/12/31 age(double)=16.5
```
기본적으로 문자열로 인식하기 때문에 타입을 명시해서 작성해야 합니다.

#### Configuration으로 주입
![그림4](https://github.com/backtony/blog-code/blob/master/spring/img/batch/2/2-4.PNG?raw=true)  
Configurations에서 Program arguments로 값을 넘겨도 됩니다.

<Br>

## JobExecution
---
### 기본 개념
JobExecution은 __JobInstance에 대한 한 번의 시도를 의미하는 객체로서 Job 실행 중에 발생한 정보들을 저장하고 있는 객체__ 입니다.  
시작시간, 종료시간, 상태(시작됨,완료,실패), 종료상태의 속성을 갖습니다.  
Job이 실행되면 __JobInstnace는 JobParameters가 동일하면 단 한번만 실행__ 됩니다.  
JobExecution은 JobInstance가 실행될 때마다 생성됩니다.  
JobInstance는 분명 단 한번만 실행이 가능하다고 했는데 JobExecution은 JobInstance가 실행 될때마다 생성된다??  
뭔가 말이 안 맞는데 이것은 JobInstance와의 관계에서 이유를 찾을 수 있습니다.  
<Br>

__JobExecution과 JobInstance와의 관계__  
+ JobExecution은 'FAILED' 또는 'COMPLETED' 등의 Job의 실행 상태를 갖고 있습니다.
+ JobExecution의 실행 상태 결과가 'COMPLETED'면 JobInstance 실행이 완료된 것으로 간주해서 재실행이 불가능합니다.(기존에 알고 있던 내용)
+ __JobExecution의 실행 상태 결과가  'FAILED'면 JobInstance 실행이 완료되지 않은 것으로 간주해서 재실행이 가능합니다.__ (새로운 내용)
    - JobParameters가 동일한 값으로 Job을 실행할지라도 이전의 JobExecution이 실패했기 때문에 기존의 JobInstance에서 __새로운 JobExecution을 생성하여 실행이 이뤄집니다.__
    - 즉, JobExecution의 실행 상태 결과가 'COMPLETE' 될 때까지 하나의 JobInstance 내에서 JobExecution이 생성될 수 있습니다.

### BATCH_JOB_EXECUTION 테이블과 매핑
JobInstance와 JobExecution은 1:M 관계로서 JobInstance에 대한 성공/실패 내역을 갖고 있습니다.


### 구조
![그림5](https://github.com/backtony/blog-code/blob/master/spring/img/batch/2/2-5.PNG?raw=true)  

### 동작 과정
![그림6](https://github.com/backtony/blog-code/blob/master/spring/img/batch/2/2-6.PNG?raw=true)  
앞서 JobInstance의 동작 과정에서 몇 가지 부분이 추가되었습니다.  
JobInstance가 실행되면 JobExecution이 새로 생성되는 부분과 이미 기존에 존재하던 JobInstance일 경우 BatchStatus의 상태를 통해 새로운 JobExecution이 생성될지 말지를 결정하는 부분이 추가되었습니다.  

<br>

## Step
---
### 기본 개념
+ __Batch Job을 구성하는 독립적인 하나의 단계로서, 실제 배치 처리를 정의하고 컨트롤하는데 필요한 모든 정보를 가지고 있는 도메인 객체__
+ 단순한 단일 태스크뿐 아니라 입력과 처리 그리고 출력과 관련된 복잡한 비즈니스 로직을 포함하는 모든 설정들을 담고 있습니다.
+ __모든 Job은 하나 이상의 Step으로 구성됩니다.__

### 기본 구현체
+ TaskletStep
    - 가장 기본이 되는 클래스로서 Tasklet 타입의 구현체들을 제어합니다.
+ PartitionStep
    - 멀티 스레드 방식으로 Step을 여러 개로 분리해서 실행합니다.
+ JobStep
    - Step 내에서 Job을 실행합니다. 
+ FlowStep
    - Step 내에서 Flow를 실행합니다.

### 구조
![그림7](https://github.com/backtony/blog-code/blob/master/spring/img/batch/2/2-7.PNG?raw=true)  
Step 인터페이스는 Step을 실행하는 execute 메서드를 갖고 있고, 이를 구현한 구현체로 AbstractStep이 있습니다.  
그리고 AbstractStep을 상속받은 4가지 Step이 있습니다.

<Br>

## StepExecution
---
### 기본 개념
+ __Step에 대한 한 번의 시도를 의미하는 객체로서 Step 실행 중에 발생한 정보들을 저장하고 있는 객체__
    - 시작시간, 종료시간, 상태, commit count, rollback count 등의 속성을 갖습니다.
+ Step이 __매번 시도될 때마다 새로 생성__ 되며 __각 Step 별로 생성__ 됩니다.
+ __Job이 실패되어 재시작될 경우 이미 성공적으로 완료된 Step은 재실행되지 않고 실패한 Step만 실행됩니다.__
    - 모두 재시작 할 수 있는 옵션도 존재합니다.
+ 이전 단계 Step이 실패해서 현재 Step을 실행되지 않았다면 StepExecution은 생성되지 않습니다.
+ JobExecution과의 관계
    - Step의 StepExecution이 모두 정상적으로 완료되야만 JobExecution이 정상적으로 완료됩니다.
    - Step의 StepExecution 중 하나라도 실패하면 JobExecution은 실패합니다.

### BATCH_STEP_EXECUTION 테이블과 매핑
+ JobExecution과 StepExecution은 1:M 관계
+ 하나의 Job에 여러 개의 Step으로 구성됬을 경우 각 StepExecution은 하나의 JobExecution을 부모로 갖습니다.


### 구조
![그림8](https://github.com/backtony/blog-code/blob/master/spring/img/batch/2/2-8.PNG?raw=true)  

<br>

## StepContribution
---
### 기본 개념
+ __청크 프로세스의 변경 사항을 버퍼링 한 후 StepExecution 상태를 업데이트하는 도메인 객체__
    - 쉽게 말하면, Step에서 정의한 일들을 처리한 결과들을 저장해두다가 StepExecution에 업데이트 하는 일을 말합니다.
+ 청크 커밋 직전에 StepExecution의 apply 메서드를 호출하여 상태를 업데이트합니다.
+ ExitStatus의 기본 종료코드 외 사용자 정의 종료코드를 생성해서 적용할 수 있습니다.

### 구조
![그림9](https://github.com/backtony/blog-code/blob/master/spring/img/batch/2/2-9.PNG?raw=true)  

### 동작 과정
![그림10](https://github.com/backtony/blog-code/blob/master/spring/img/batch/2/2-10.PNG?raw=true)  

1. 잡이 실행되면 TaskletStep에서 StepExecution을 생성합니다.
2. StepExecution은 StepContribution을 만듭니다.
3. Chunk 기반 Tasklet이 실행됩니다.
4. 청크 프로세스의 데이터들이 StepContribution에 쌓입니다.
5. StepExecution이 완료되는 시점에 apply 메서드를 호출하여 StepContribution의 필드 값들을 StepExecution에 업데이트 시킵니다.

<br>

## ExecutionContext
---
### 기본 개념
+ __프레임워크에서 유지 및 관리하는 "키-값"으로 된 컬렉션으로 StepExecution 또는 JobExecution 객체의 상태(필드값들)을 저장하는 공유 객체__
+ DB에 직렬화 한 값으로 저장됩니다.("key":"value")
+ 공유 범위
    - Step 범위 : 각 Step Execution에 저장되며 __Step 간 서로 공유 불가능__
    - Job 범위 : 각 Job의 Execution에 저장되며 Job간 서로 공유는 되지 않지만, __해당 Job에 속한 Step은 공유 가능__
+ Job 재시작시 이미 처리한 Row 데이터는 건너뛰고 이후부터 수행하도록 상태 정보를 활용합니다.

### 구조
![그림11](https://github.com/backtony/blog-code/blob/master/spring/img/batch/2/2-11.PNG?raw=true)  
Map을 갖고 키-벨류 형태로 값을 저장하게 됩니다.  
<br><BR>

![그림12](https://github.com/backtony/blog-code/blob/master/spring/img/batch/2/2-12.PNG?raw=true)  
JobExecution과 StepExecution 각각 필드값으로 ExecutionContext를 갖고 있습니다.  

```java
public class ExecutionContextTasklet implements Tasklet {
    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        ExecutionContext jobExecutionContext = contribution.getStepExecution().getJobExecution().getExecutionContext();
        ExecutionContext stepExecutionContext = contribution.getStepExecution().getExecutionContext();

        ExecutionContext jobExecutionContext2 = chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext();
        ExecutionContext stepExecutionContext2 = chunkContext.getStepContext().getStepExecution().getExecutionContext();
        return RepeatStatus.FINISHED;
    }
}
```
인자 두개에서 모두 꺼내서 사용할 수 있습니다.

<br>

## JobRepository
---
### 기본 개념
+ __배치 작업 중의 정보를 저장하는 저장소 역할__
+ Job이 언제 수행되었고, 언제 끝났으며, 몇 번이 실행되었고 실행에 대한 결과 등의 배치 작업의 수행과 관련된 모든 메타 데이터를 저장합니다.
    - JobLauncher, Job, Step 구현체 내부에서 CRUD 기능을 처리합니다.
    - 실행 과정에서 데이터를 DB에 저장하고, 필요한 데이터는 읽어오는 작업을 하는 객체라고 보면 됩니다.
    


### 구조
![그림13](https://github.com/backtony/blog-code/blob/master/spring/img/batch/2/2-13.PNG?raw=true)  

### 주요 메서드
![그림14](https://github.com/backtony/blog-code/blob/master/spring/img/batch/2/2-14.PNG?raw=true)  

<br>

## JobLuancher
---
### 기본 개념
+ __배치 Job을 실행시키는 역할__
+ Job과 Job Parameters를 인자로 받아 배치 작업을 수행한 후 최종 client에게 JobExecution을 반환합니다.
+ 스프링 부트 배치가 구동되면 JobLauncher 빈이 __자동 생성__ 됩니다.
+ Job 실행
    - JobLauncher.run(job,jobParameters)
    - 스프링 부트 배치에서는 jobLauncherApplicationRunner가 자동적으로 JobLuancher을 실행시킵니다.
    - 동기적 실행
        - taskExecutor를 SyncTaskExecutor로 설정한 경우(기본값)
        - JobExecution을 획득하고 배치 처리를 최종 완료한 이후 Client에게 JobExecution을 반환
        - 스케줄러에 의한 배치처리에 적합(배치처리시간이 길어도 무관한 경우)
    - 비동기적 실행
        - taskExecutor가 SimpleAsyncTaskExecutor로 설정할 경우
        - JobExecution을 획득한 후 Client에게 바로 JobExecution을 반환하고 배치처리를 진행
        - HTTP 요청에 의한 배치처리에 적합(배치처리 시간이 길 경우 응답이 늦어지지 않도록 함)

### 동작 과정
![그림15](https://github.com/backtony/blog-code/blob/master/spring/img/batch/2/2-15.PNG?raw=true)  


### 자동 시작 끄기
기본적으로 스프링 배치는 시작 시 자동으로 배치가 동작합니다.  
이는 application.yml에서 옵션으로 설정할 수 있습니다.  
```yml
spring:
    batch:
        job:
            enabled: false
```

### 비동기 설정하기
```java
private final JobLauncher jobLauncher;
private final BasicBatchConfigurer basicBatchConfigurer;

public void run(){
    SimpleJobLauncher simpleJobLauncher = (SimpleJobLauncher) basicBatchConfigurer.getJobLauncher();
    simpleJobLauncher.setTaskExecutor(new SimpleAsyncTaskExecutor());
    simpleJobLauncher.run(job, jobParameters);
}
```
부가적은 코드는 생략했습니다.  
동기는 기본값 그대로 사용하면 되지만, 비동기의 경우 taskExecutor 설정을 바꿔줘야 합니다.  
기존에 사용할 때는 JobLauncher에 의존성 주입을 받아서 사용했습니다.  
taskExecutor를 세팅하기 위해서는 JobLauncher를 SimpleJobLauncher로 다운 캐스팅해야하는데 실제로 주입된 값은 프록시이기 때문에 불가능합니다.  
따라서 basicBatchConfigurer를 이용해 꺼내서 사용해야 합니다.  




<Br><Br>

__참고__  
<a href="https://www.inflearn.com/course/%EC%8A%A4%ED%94%84%EB%A7%81-%EB%B0%B0%EC%B9%98#" target="_blank"> 스프링 배치 - Spring Boot 기반으로 개발하는 Spring Batch</a>   



