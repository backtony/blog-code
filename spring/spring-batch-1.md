# Spring Batch - 시작하기


## 배치 애플리케이션이란?
---
__배치(Batch)는 일괄처리란 뜻을 갖고 있습니다.__  
쇼핑몰에서 매일 전날의 매출 데이터를 집계해야 한다고 가정해보겠습니다.  
매출 데이터가 대용량이라면 하루 매출 데이터를 읽고, 가공하고, 저장한다면 해당 서버는 순식간에 CPU, I/O 등의 자원을 다 써버려서 다른 작업을 할 수 없게 됩니다.  
집계 기능은 하루에 1번만 수행된다면 이를 위해 API를 구성하는 것은 낭비가 될 수 있고, 데이터 처리 중에 실패했다면 처음부터가 아니라 실패시점부터 다시 처리하고 싶을 수 있습니다.  
이런 단발성으로 __대용량의 데이터를 처리하는 애플리케이션을 배치 애플리케이션__ 이라고 합니다.  
배치 애플리케이션은 다음 조건을 만족해야 합니다.
+ 대용량 데이터 - 배치 애플리케이션은 대량의 데이터를 가져오거나, 전달하거나, 계산하는 등의 처리를 할 수 있어야 합니다.
+ 자동화 - 배치 애플리케이션은 심각한 문제 해결을 제외하고는 사용자 개입이 없이 실행되어야 합니다.
+ 견고성 - 배치 애플리케이션은 잘못된 데이터를 충돌/중단 없이 처리할 수 있어야 합니다.
+ 신뢰성 - 배치 애플리케이션은 무엇이 잘못되었는지를 추적할 수 있어야 합니다.(로깅, 알림)
+ 성능 - 배치 애플리케이션은 지정한 시간 안에 처리를 완료하거나 동시에 실행되는 다른 애플리케이션을 방해하지 않도록 수행되어야 합니다.

__Spring 진영에서는 배치 애플리케이션을 지원하는 모듈로 Spring Batch가 있습니다.__  

<br>

## 스프링 배치 계층 구조
---
![그림1](https://github.com/backtony/blog-code/blob/master/spring/img/batch/1/1-1.PNG?raw=true)  

스프링 배치는 레이어 구조로 세 개로 구분되어 있습니다.  
+ 인프라 레이어
    - 애플리케이션과 코어 모두 공통 인프라 위에서 빌드됩니다.
    - Job 실행의 흐름과 처리를 위한 __틀__ 을 제공합니다.
    - 개발자와 애플리케이션에서 사용하는 일반적인 Reader와 Writer 그리고 RetryTemplate과 같은 서비스를 포함합니다.
+ 코어 레이어
    - 배치 작업을 시작하고 제어하는데 필요한 핵심 런타임 클래스를 포함합니다.
    - JobLauncher, Job, Step, Flow
+ 애플리케이션 레이어
    - 개발자가 작성한 모든 배치 작업과 사용자 정의 코드를 포함합니다.

스프링 배치는 계층 구조로 설계되어 있기 때문에 개발자는 Application 계층의 비즈니스 로직에 집중할 수 있습니다.  
배치의 동작과 관련된 것은 Batch Core에 있는 클래스들을 이용하여 제어할 수 있습니다.  

<br>

## @EnableBatchProcessing
---
```java
@SpringBootApplication
@EnableBatchProcessing
public class SpringBatchApplication {
    public static void main(String[] args) {
        SpringApplication.run(SpringBatchApplication.class, args);
    }
}
```
@EnableBatchProcessing 을 추가하면 다음과 같은 작업이 자동으로 수행됩니다.
+ 총 4개의 설정 클래스를 실행시키며, 스프링 배치의 모든 초기화 및 실행 구성이 이뤄집니다.      
    - SimpleBatchConfiguration
        - JobBuilderFactory와 StepBuilderFactory 생성
        - 스프링 배치의 주요 구성 요소 생성(프록시 객체로 생성됨)
    - BatchConfigurerConfiguration
        - BasicBatchConfigurer
            - SimpleBatchConfiguration에서 생성한 프록시 객체의 실제 대상 객체를 생성하는 설정 클래스
            - 빈으로 의존성 주입 받아서 주요 객체들을 참조해서 사용할 수 있습니다.
        - JpaBatchConfigurer
            - BasicBatchConfigurer를 상속받아 JPA 관련 객체를 생성하는 설정 클래스
        - 위 두개는 BatchConfigurer 인터페이스를 구현했습니다.
    - BatchAutoConfiguration
        - 스프링 배치가 초기화 될 때 자동으로 실행되는 설정 클래스
        - Job을 수행하는 JobLuancherApplicationRunner 빈을 생성
        - 스프링 부트가 자동으로 job을 실행시키는 것과 관련된 일들을 한다고 보면 됩니다.

스프링 부트 배치의 자동 설정 클래스가 실행됨으로 빈으로 등록된 모든 Job을 검색해서 초기화와 동시에 Job을 수행하도록 구성됩니다.

### 동작 순서
![그림2](https://github.com/backtony/blog-code/blob/master/spring/img/batch/1/1-2.PNG?raw=true)  

<br>

## 기본 용어 이해
---
![그림3](https://github.com/backtony/blog-code/blob/master/spring/img/batch/1/1-3.PNG?raw=true)  
+ Job
    - 하나의 일을 말합니다.
+ Step
    - 하나의 일(Job) 안에서 단계를 의미합니다.
+ Tasklet
    - 하나의 단계(Step) 안에서 실질적으로 수행하는 작업 내용을 의미합니다.

<Br>

## 간단한 배치 애플리케이션 만들어보기
---
위에서 설명한 개념만으로 배치 애플리케이션을 하나 만들어보겠습니다.  
코드가 길이가 조금 있기 때문에 주석으로 설명을 달겠습니다.  
```java
@Configuration // 하나의 배치 Job을 정의하고 빈으로 등록
@RequiredArgsConstructor
public class HelloJobConfiguration {

    // Job을 생성하는 빌더 팩토리로, Job을 생성할 때 new Job이 아니라 jobBuilderFactory을 사용해 쉽게 Job을 생성합니다.
    private final JobBuilderFactory jobBuilderFactory; 
    // JobBuilderFactory와 같은 맥락입니다.
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job helloJob(){
        return jobBuilderFactory.get("helloJob") // helloJob을 생성합니다.
                .start(helloStep1()) // Job의 첫번째 Step으로 helloStep1을 등록합니다.
                .next(helloStep2()) // helloStep1을 수행한 이후 다음 Step을 등록합니다.
                .build(); 
    }

    @Bean
    public Step helloStep1() {
        return stepBuilderFactory.get("helloStep1") // helloStep1을 생성합니다.
                .tasklet(new Tasklet() { // Step의 작업 내용 Tasklet을 정의합니다. -> 단일 메서드 인터페이스로 람다식이 가능합니다.
                    @Override
                    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                        System.out.println("hellStep1");
                        // Step은 기본적으로 Tasklet을 무한 반복시킵니다.  
                        // 따라서 null이나 RepeatStatus.FINISHED를 반환해줘야 1번만 Tasklet을 실행합니다.
                        return RepeatStatus.FINISHED;
                    }
                })
                .build();
    }

    // helloStep1과 같은 맥락입니다.
    @Bean
    public Step helloStep2() {
        return stepBuilderFactory.get("helloStep2")
                .tasklet(new Tasklet() {
                    @Override
                    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                        System.out.println("hellStep2");
                        return RepeatStatus.FINISHED;
                    }
                })
                .build();
    }
}
```
<br>

## DB 스키마
---
> Job -> Step -> Tasklet

지금까지 설명한 바에 의하면 Job은 위와 같이 구성됩니다.  
스프링 배치는 내부적으로 Job이 구성이 되면 Job이 실행이되는 정보들, Job의 상태 정보 등의 메타 데이터를 저장하는 JobExecution 클래스가 생성됩니다.  
Step도 마찬가지로 StepExecution 클래스가 생성됩니다.  
이러한 데이터를 담고 있는 클래스들을 데이터베이스에 저장해서 현재 Job, Step들의 정보를 보관하게 됩니다.  
이렇게 해야 Job이 끝나고 향후 정산, 통계, 오류 등의 내용을 파악할 수 있기 때문입니다.  
스프링 배치에서는 위와 같은 클래스의 스키마 테이블을 기본적으로 스크립트로 제공합니다.  

### 스키마 생성
![그림4](https://github.com/backtony/blog-code/blob/master/spring/img/batch/1/1-4.PNG?raw=true)  

> 라이브러리 : org.springframework.batch:spring-batch-core  
> 패키지 : org.springframework.batch.core.schema-*.sql

위의 위치에서 DB 유형별로 스크립트가 제공된 것을 확인할 수 있습니다.  
+ 수동 생성
    - 위의 위치에서 제공하는 스크립트로 쿼리를 복사 후 직접 실행
+ 자동 생성
    - application.yml에서 spring.batch.jdbc.initialize-schema 속성 설정
        - EMBEDDED : __내장 DB일 때만__ 실행되며 스키마가 자동으로 생성(Default)
        - ALWAYS : 스크립트 항상 실행
        - NEVER 
            - 스크립트 항상 실행 안 하기 때문에 내장 DB일 경우 스크립트가 생성이 안되기 때문에 오류 발생
            - __운영에서는 수동으로 스크립트 생성 후 설정하는 것을 권장__


### 스키마 구조
![그림5](https://github.com/backtony/blog-code/blob/master/spring/img/batch/1/1-5.PNG?raw=true)  
스키마는 왼쪽에 Step 관련 테이블 2개, 오른쪽에 Job 관련 테이블 4개로 구성되어 있습니다.  

#### Job 관련 테이블
##### BATCH_JOB_INSTANCE
![그림6](https://github.com/backtony/blog-code/blob/master/spring/img/batch/1/1-6.PNG?raw=true)  
+ job이 실행될 때 JobInstace 정보가 저장됩니다.
+ __동일한 job_name과 job_key로 중복 저장될 수 없습니다.__
    - 즉, 동일한 job name의 job instance를 만들려면 매번 다른 jobParameter를 사용해야 합니다.

필드명|설명
---|---
JOB_INSTANCE_ID|고유하게 식별할 수 있는 기본 키
VERSION|업데이트 될 때마다 1씩 증가
JOB_NAME|job을 구성할 때 부여하는 job 이름
JOB_KEY|job_name과 jobParameter를 합쳐서 해싱한 값

<br>

##### BATCH_JOB_EXECUTION
![그림7](https://github.com/backtony/blog-code/blob/master/spring/img/batch/1/1-7.PNG?raw=true)  
__job의 실행정보가 저장되며 job 생성 시간, 시작 시간, 종료 시간, 실행 상태, 메시지 등을 관리합니다.__

필드명|설명
---|---
JOB_EXECUTION_ID|JobExecution을 고유하게 식별하는 기본키, JOB_INSTANCE와 다대일 관계(자신기준)
VERSION|업데이트 될 때마다 1씩 증가
JOB_INSTANCE_ID|JOB_INSTANCE의 기본 키
CREATE_TIME|실행(Execution)이 생성된 시점을 TimeStamp 형식으로 기록
START_TIME|실행(Execution)이 시작된 시점을 TimeStamp 형식으로 기록
END_TIME|실행(Execution)이 종료된 시점을 TimeStamp 형식으로 기록하며 job 실행 도중 오류가 발생해서 job이 중단된 경우 값이 저장되지 않을 수 있음
STATUS|실행 상태(BatchStatus)를 저장(COMPLETED,FAILED,STOPPED..)
EXIT_CODE|실행 종료된(ExitStatus)를 저장(COMPLETED,FAILED..)
EXIT_MESSAGE|Status가 실패일 경우 실패 원인 등의 내용을 저장
LAST_UPDATED|마지막 실행(Execution) 시점을 TimeStamp 형식으로 기록

<br>

##### BATCH_JOB_EXECUTION_PARAMS
![그림8](https://github.com/backtony/blog-code/blob/master/spring/img/batch/1/1-8.PNG?raw=true)  
+ job과 함께 실행되는 JobParameter 정보를 저장합니다.  
+ job 파라미터는 key-value 값으로 값을 저장합니다.

필드명|설명
---|---
JOB_EXECUTION_ID|JobExecution 식별 키, JOB_EXECUTION과 다대일 관계(자신기준)
TYPE_CD|String, Long, Date, Double 타입 정보
KEY_NAME|파라미터 키 값
STRING_VAL|파라미터 문자 값
DATE_VAL|파라미터 날짜 값
LONG_VAL|파라미터 Long 값
DOUBLE_VAL|파라미터 Double 값
IDENTIFYING|식별 여부(True, False)

<br>

##### BATCH_JOB_EXECUTION_CONTEXT
![그림9](https://github.com/backtony/blog-code/blob/master/spring/img/batch/1/1-9.PNG?raw=true)  
+ job의 실행동안 여러가지 상태정보, 공유 데이터를 직렬화(Json 형식)하여 저장합니다.
+ __Step에서 해당 데이터를 서로 공유하여 사용합니다.__

필드명|설명
---|---
JOB_EXECUTION_ID|JobExecution 식별 키
SHORT_CONTEXT|Job의 실행 상태 정보, 공유 데이터 등의 정보를 문자열로 저장
SERIALIZED_CONTEXT|직렬화(Serialized)된 전체 컨텍스트

<br>


#### STEP 관련 테이블
##### BATCH_STEP_EXECUTION
![그림10](https://github.com/backtony/blog-code/blob/master/spring/img/batch/1/1-10.PNG?raw=true)  
__Step의 실행정보가 저장되며 생성 시간, 종료 시간, 실행상태, 메시지 등을 관리합니다.__  

필드명|설명
---|---
STEP_EXECUTION_ID|Step 실행정보를 고유하게 식별하는 기본 키
VERSION|업데이트 될 때마다 1씩 증가
STEP_NAME|Step을 구성할 때 부여하는 이름
JOB_EXECUTION_ID|JobExecution의 기본키, JobExecution과 다대일 관계(자신기준)
START_TIME|실행(Execution)이 시작된 시점을 TimeStamp 형식으로 기록
END_TIME|실행이 종료된 시점을 TimeStamp 형식으로 기록하며, job 실행 도중 오류가 발생해서 job이 중단된 경우 값이 저장되지 않을 수 있음
STATUS|실행 상태(BatchStatus)를 저장(Completed, Failed, Stopped..)
COMMIT_COUNT|트랜잭션 당 커밋되는 수를 기록
READ_COUNT|실행시점에 Read한 Item 수를 기록
FILTER_COUNT|실행도중 필터링한 Item 수를 기록
WRITE_COUNT|실행도중 저장되고 커밋된 Item 수를 기록
READ_SKIP_COUNT|실행도중 Read가 Skip된 Item 수를 기록
WRITE_SKIP_COUNT|실행도중 Write가 Skip된 Item 수를 기록
PROCESS_SKIP_COUNT|실행도중 Process가 Skip된 Item 수를 기록
ROLLBACK_COUNT|실행도중 rollback이 일어난 수를 기록
EXIT_CODE|실행종료코드(ExitStatus)를  저장(Completed, Failed)
EXIT_MESSAGE|Status가 실패일 경우 실패 원인 등의 내용을 저장
LAST_UPDATED|마지막 실행(Execution) 시점을 TimeStamp형식으로 기록

<br>


##### BATCH_STEP_EXECUTION_CONTEXT
![그림11](https://github.com/backtony/blog-code/blob/master/spring/img/batch/1/1-11.PNG?raw=true)  
+ Step의 실행동안 여러가지 상태정보, 공유 데이터를 직렬화(Json 형식)하여 저장합니다.
+ __Step 별로 저장되기 때문에 Step간 서로 공유할 수 없습니다.__

필드명|설명
---|---
STEP_EXECUTION_ID|StepExecution의 기본 키
SHORT_CONTEXT|Step의 실행 상태 정보, 공유 데이터 등의 정보를 문자열로 저장
SERIALIZED_CONTEXT|직렬화(Serialized)된 전체 컨텍스트







<Br><Br>

__참고__  
<a href="https://www.inflearn.com/course/%EC%8A%A4%ED%94%84%EB%A7%81-%EB%B0%B0%EC%B9%98#" target="_blank"> 스프링 배치 - Spring Boot 기반으로 개발하는 Spring Batch</a>   



