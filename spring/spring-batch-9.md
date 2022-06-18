# Spring Batch - ItemProcessor


## ItemProcessor
---
![그림7](https://github.com/backtony/blog-code/blob/master/spring/img/batch/6/6-7.PNG?raw=true)  
+ __데이터를 출력하기 전에 데이터를 가공 및 필터링 역할을 하는 인터페이스__ 입니다.
+ ItemReader 및 ItemWriter와 분리되어 비즈니스 로직을 구현할 수 있습니다.
+ ItemReader로부터 받은 아이템을 특정 타입으로 변환해서 ItemWriter에 넘겨 줄 수 있습니다.
+ Itemreader로부터 받은 아이템들 중 필터과정을 거쳐서 원하는 아이템들만 ItemWriter로 넘겨줄 수 있습니다.
+ ChunkOrientedTasklet 실행 시 선택적 요소기 때문에 필수 요소는 아닙니다.
+ O process()
    - I 제네릭은 ItemReader에서 받을 데이터 타입
    - O 제네릭은 ItemWriter에게 보낼 데이터 타입
    - 아이템을 하나씩 가공 처리하며 null을 리턴할 경우 해당 아이템은 Chunk\<O>에 저장되지 않습니다.
+ ItemStream을 구현하지 않고 거의 대부분 Customizing해서 사용하기 때문에 기본적으로 제공되는 구현체가 적습니다.
+ __Null을 반환하면 해당 item은 ItemWriter로 전달되지 않습니다.__


<Br>

![그림1](https://github.com/backtony/blog-code/blob/master/spring/img/batch/9/9-1.PNG?raw=true)  
Processor는 대부분 직접 구현해서 사용하기 때문에 Writer와 Reader에 비해 상대적으로 적은 구현체들을 제공하고 있습니다.  
<br>

## CompositeItemProcessor
---
![그림3](https://github.com/backtony/blog-code/blob/master/spring/img/batch/9/9-3.PNG?raw=true)  
ItemProcessor들을 연결(Chaining)해서 위임하면 각 ItemProcessor를 실행시킵니다.  
이전의 ItemProcessor 반환 값은 다음 ItemProcessor 값으로 연결됩니다.  

### API
![그림2](https://github.com/backtony/blog-code/blob/master/spring/img/batch/9/9-2.PNG?raw=true)  

### 예시
```java
@Configuration
@RequiredArgsConstructor
public class HelloJobConfiguration {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private int chunkSize = 10;

    @Bean
    public Job helloJob() {
        return jobBuilderFactory.get("job")
                .start(step1())
                .incrementer(new RunIdIncrementer())
                .build();
    }

    @Bean
    public Step step1() {
        return stepBuilderFactory.get("step")
                .<String, String>chunk(chunkSize)
                .reader(customItemReader())
                .processor(customItemProcessor())
                .writer(items -> {
                    for (String item : items) {
                        System.out.println("item = " + item);
                    }
                })
                .build();
    }

    // 구성하는 프로세서 제네릭이 일치하므로 반환값 제네릭 사용 가능
    // 일치하지 않으면 제네릭 사용 불가
    @Bean
    public ItemProcessor<String, String> customItemProcessor() {
        List<ItemProcessor> processorList = new ArrayList<>();
        processorList.add(new CustomItemProcessor1());
        processorList.add(new CustomItemProcessor2());

        CompositeItemProcessor processor = new CompositeItemProcessor<>();
        processor.setDelegates(processorList);

        return processor;
    }


    @Bean
    public ItemReader<String> customItemReader() {
        return new ItemReader<String>() {
            int i = 0;

            @Override
            public String read() {
                i++;
                return i > 10 ? null : "item";
            }
        };
    }

}
-------------------------------------------------------
public class CustomItemProcessor1 implements ItemProcessor<String,String> {

    @Override
    public String process(String item) throws Exception {
        return item + " processor1";
    }
}
-------------------------------------------------------
public class CustomItemProcessor2 implements ItemProcessor<String,String> {
    @Override
    public String process(String item) throws Exception {
        return item + " processor2";
    }
}
```
간단하게 넘어온 아이템에 processor 번호를 붙이는 processor 2개를 만들고 CompositeItemProcessor를 사용해서 묶어서 하나의 Processor로 전달했습니다.  
CompositeItemProcessor를 구성하는 프로세서가 같은 제네릭 타입을 갖기 때문에 customItemProcessor 메서드의 반환값을 제네릭으로 세팅할 수 있었지만 구성하는 프로세서가 다른 제네릭 타입을 갖는다면 반환값에 제네릭을 세팅할 수 없습니다.  
반환값이 다르다면 아래와 같이 세팅하면 됩니다.
```java
@Bean
public CompositeItemProcessor compositeProcessor() {
    List<ItemProcessor> delegates = new ArrayList<>(2);
    delegates.add(processor1());
    delegates.add(processor2());

    CompositeItemProcessor processor = new CompositeItemProcessor<>();

    processor.setDelegates(delegates);

    return processor;
}
```

<br>

## ClassifierCompositeItemProcessor
---
Classifier로 라우팅 패턴을 구현해서 ItemProcessor 구현체 중 하나를 선택해서 호출하는 기능을 제공합니다.  

### API
![그림3](https://github.com/backtony/blog-code/blob/master/spring/img/batch/9/9-3.PNG?raw=true)  

### 예시
```java
@Slf4j
@Configuration
@RequiredArgsConstructor
public class HelloJobConfiguration {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private int chunkSize = 10;

    @Bean
    public Job helloJob() {
        return jobBuilderFactory.get("job")
                .start(step1())
                .incrementer(new RunIdIncrementer())
                .build();
    }

    @Bean
    public Step step1() {
        return stepBuilderFactory.get("step")
                .<Integer, String>chunk(chunkSize)
                .reader(customItemReader())
                .processor(customItemProcessor())
                .writer(items -> {
                    for (String item : items) {
                        System.out.println("item = " + item);
                    }
                })
                .build();
    }

    @Bean
    public ItemProcessor<Integer, String> customItemProcessor() {
        // 프로세서 생성해서 맵에 담기
        Map<Integer, ItemProcessor<Integer, String>> processorMap = new HashMap<>();
        processorMap.put(0, new CustomItemProcessor1());
        processorMap.put(1, new CustomItemProcessor2());
        
        // 인자로 맵 전달
        ProcessorClassifier<Integer, ItemProcessor<?, ? extends String>> classifier = new ProcessorClassifier<>(processorMap);

        // ClassifierCompositeItemProcessor에 세팅
        ClassifierCompositeItemProcessor<Integer, String> processor = new ClassifierCompositeItemProcessor<>();
        processor.setClassifier(classifier);

        return processor;
    }


    @Bean
    public ItemReader<Integer> customItemReader() {
        return new ItemReader<Integer>() {
            int i = 0;

            @Override
            public Integer read() {
                i++;
                return i > 10 ? null : i;
            }
        };
    }

}
-----------------------------------------------------------------
public class ProcessorClassifier<C,T> implements Classifier<C,T> {

    private Map<Integer, ItemProcessor<Integer,String>> processorMap = new HashMap<>();

    @Override
    public T classify(C classifiable) {        
        return (T) processorMap.get((Integer)(classifiable)%2); // 로직에 따른 적절한 프로세서를 Map에서 꺼내서 반환
    }

    public ProcessorClassifier() {
    }

    public ProcessorClassifier(Map<Integer, ItemProcessor<Integer, String>> processorMap) {
        this.processorMap = processorMap;
    }
}
-----------------------------------------------------------------
public class CustomItemProcessor1 implements ItemProcessor<Integer,String> {

    @Override
    public String process(Integer item) throws Exception {
        return item + " processor1";
    }
}
-----------------------------------------------------------------
public class CustomItemProcessor2 implements ItemProcessor<Integer,String> {
    @Override
    public String process(Integer item) throws Exception {
        return item + " processor2";
    }
}
```
코드가 길지만 실제 로직 자체는 간단합니다.  
Reader에서는 1부터 10까지의 수를 읽어서 프로세서에 전달합니다.  
프로세서에서는 classifier에 의해서 적절한 프로세서를 꺼내서 주고 그에 따른 처리 로직일 진행하는 예시입니다.  
<br>

위와 같이 제공하는 프로세서들 중 CompositeItemProcessor은 가끔 사용하고 다른 것들은 잘 사용하지 않는다고 합니다.  










<Br><Br>

__참고__  
<a href="https://www.inflearn.com/course/%EC%8A%A4%ED%94%84%EB%A7%81-%EB%B0%B0%EC%B9%98#" target="_blank"> 스프링 배치 - Spring Boot 기반으로 개발하는 Spring Batch</a>   



