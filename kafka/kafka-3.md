# 카프카 기본 개념 - 2


## 카프카 스트림즈
카프카 스트림즈는 토픽에 적재된 데이터를 실시간으로 변환하여 다른 토픽에 적재하는 라이브러리입니다.  
보통 빅데이터 처리에 필수적이라고 판단되었던 분산 시스템이나 스케줄링 프로그램들은 스트림즈를 운영하는 데에는 불필요합니다.  
자바 라이브러리로 구현하는 스트림즈 애플리케이션은 JVM 위에서 하나의 프로세스로 실행되기 때문입니다.  

> 프로듀서 -> 카프카 클러스터 -> 컨슈머 <br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; ↕ <br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;스트림즈 애플리케이션

<Br>

__cf) 프로듀서와 컨슈머를 조합해서 사용하지 않고 스트림즈를 사용해야 하는 이유__  
스트림 데이터 처리에 있어 필요한 다양한 기능을 스트림즈DSL로 제공하며 필요하다면 프로세서 API를 사용하여 기능을 확장할 수 있기 때문입니다.  
컨슈머와 프로듀서를 조합하여 스트림즈가 제공하는 기능과 유사하게 만들 수는 있으나 스트림즈 라이브러리를 통해 제공되는 단 한 번의 데이터 처리, 장애 허용 시스템 등의 특징들은 컨슈머와 프로듀서의 조합만으로는 완벽하게 구현하기는 어렵습니다.  

<br>

스트림즈 애플리케이션은 내부적으로 스레드를 1개 이상 생성할 수 있으며, 스레드는 1개 이상의 테스크를 가집니다.  
스트림즈의 테스크는 스트림즈 애플리케이션을 실행하면 생기는 데이터 처리 최소 단위입니다.  
만약 3개의 파티션으로 이뤄진 토픽을 처리하는 스트림즈 애플리케이션을 실행하면 내부에 3개의 테스크가 생깁니다.  
스트림즈는 병렬처리를 위해 파티션과 스트림즈 스레드 개수를 늘림으로써 처리량을 늘릴 수 있습니다.  
실제 운영에서는 안정적으로 운영할 수 있도록 2개 이상의 서버로 구성하여 스트림즈 애플리케이션을 운영합니다.  

<br>

카프카 스트림즈의 구조와 사용방법을 알기 위해서는 토폴로지 개념을 알아야 합니다.  
토폴로지란 2개 이상의 노드들과 선으로 이뤄진 집합을 말합니다.  
토폴로지의 종류로는 링형, 트리형, 성형 등이 있는데 스트림즈에서 사용하는 토폴로지는 트리 형태와 유사합니다.  
카프카 스트림즈에서는 토폴로지를 이루는 노드를 하나의 __'프로세서'__ 라고 부르고 노드와 노드를 이은 선을 __'스트림'__ 이라고 부릅니다.  
스트림은 토픽의 데이터를 뜻하는데 레코드라고 보면 됩니다.  
프로세서에는 소스 프로세서, 스트림 프로세서, 싱크 프로세서가 있습니다.  

> 소스 프로세서 -> 스트림 프로세서 -> 싱크 프로세서

+ 소스 프로세서 : 데이터를 처리하기 위해 최초로 선언해야 하는 노드로 하나 이상의 토픽에서 데이터를 가져오는 역할
+ 스트림 프로세서 : 다른 프로세서가 반환하는 데이터를 처리하는 역할
+ 싱크 프로세서 : 데이터를 특정 카프카 토픽으로 저장하는 역할을 하며 스트림즈로 처리된 데이터의 최종 종착지

<br>

스트림즈DSL과 프로세서 API 2가지 방법으로 개발이 가능합니다.  
스트림즈 DSL은 스트림 프로세싱에 쓰일 만한 다양한 기능들을 자체 API로 만들어 놓았기 때문에 쉽게 개발할 수 있고 여기서 제공하지 않는 일부 기능들은 프로세서 API를 사용하여 구현합니다.  
+ 스트림즈DSL로 구현하는 데이터 처리 예시
    - 메시지 값을 기반으로 토픽 분기 처리
    - 지난 10분간 들어온 데이터의 개수 집계
    - 토픽과 다른 토픽의 결합으로 새로운 데이터 생성
+ 프로세서 API로 구현하는 데이터 처리 예시
    - 메시지 값의 종류에 따라 토픽을 가변적으로 전송
    - 일정한 시간 간격으로 데이터 처리

### 스트림즈 DSL
스프림즈DSL에는 레코드의 흐름을 추상화한 3가지 개념인 KStream, KTable, GlobalKTable이 있습니다.  

#### KStream
KStream은 레코드의 흐름을 표현한 것으로 메시지 키와 메시지 값으로 구성되어 있습니다.  
KStream으로 데이터를 조회하면 토픽에 존재하는 모든 레코드가 출력됩니다.  
KStream은 컨슈머로 토픽을 구독하는 것과 동일한 선상에서 사용하는 것이라고 볼 수 있습니다.  

#### KTable
KStream은 토픽의 모든 레코드를 조회할 수 있지만, KTable은 유니크한 메시지 키를 기준으로 가장 최신 레코드를 사용합니다.  
즉, KTable로 데이터를 조회하면 메시지 키를 기준으로 가장 최신에 추가된 레코드의 데이터가 출력됩니다.  

#### GlobalKTable
KTable과 동일하게 메시지 키를 기준으로 묶어서 사용됩니다.  
그러나 KTable로 선언된 토픽은 1개 파티션이 1개 태스크에 할당되어 사용되고, GlobalKTable로 선언된 토픽은 모든 파티션 데이터가 각 태스크에 할당되어 사용됩니다.  
GlobalKTable을 설명하는 가장 좋은 예는 KStream과 KTable 데이터 조인을 수행할 때입니다.  
KStream과 KTable을 조인하려면 반드시 코파티셔닝되어 있어야 합니다. 코파티셔닝이란 조인을 하는 2개 데이터의 파티션 개수가 동일하고 파티셔닝 전략을 동일하게 맞추는 작업입니다.  
파티션 개수가 동일하고 파티셔닝 전략이 같은 경우에는 동일만 메시지 키를 가진 데이터가 동일한 태스크에 들어가는 것을 보장합니다.  
이를 통해 각 태스크는 KStream의 레코드와 KTable의 메시지 키가 동일할 경우 조인을 수행할 수 있습니다.  
문제는 조인을 수행하려는 토픽들이 코파티셔닝되어 있음을 보장할 수 없다는 것으로 이때는 TopologyException이 발생하게 됩니다.  
이 경우에는 새로운 토픽에 새로운 메시지 키를 가지도록 재배열 하는 리파티셔닝 과정이 필요합니다.  
리피타니셔닝은 토픽에 기존 데이터를 중복해서 생성할 뿐만 아니라 파티션을 재배열하기 위해 프로세싱 과정도 거쳐야 합니다.  
이렇게 코파티셔닝 되지 않은 KStream과 KTable을 조인해서 사용하고 싶다면 KTable을 GlobalKTable로 선언하여 사용하면 됩니다.  
GlobalKTable로 정의된 데이터는 스트림즈 애플리케이션의 모든 태스크에 동일하게 공유되어 사용되기 때문에 코파티셔닝 되지 않은 KStream과 데이터 조인을 할 수 있습니다.  
다만, GlobalKTable을 사용하면 각 태스크마다 GlobalKTable로 정의된 모든 데이터를 저장하고 사용하기에 로컬 스토리지 사용량이 증가하고 네트워크, 브로커에 부하가 생기므로 되도록 작은 용량의 데이터일 경우에만 사용을 권장합니다.  
따라서, 많은 양의 데이터를 가진 토픽으로 조인할 경우에는 리파티셔닝을 통해 KTable의 사용이 권장됩니다.  

#### 스트림즈DSL 주요 옵션
+ 필수 옵션
    - bootstrap.servers
        - 프로듀서가 데이터를 전송할 대상 카프카 클러스터에 속한 브로커의 '호스트 이름:포트' 를 1개 이상 작성합니다.
        - 2개 이상 브로커 정보를 입력하여 일부 브로커에 이슈가 발생하더라도 접속하는 데에 이슈가 없도록 설정할 수 있습니다.
    - application.id
        - 스트림즈 애플리케이션을 구분하기 위한 고유 아이디를 설정합니다.
+ 선택 옵션
    - default.key.serde
        - 레코드의 메시지 키를 직렬화, 역직렬화하는 클래스를 지정합니다.
        - 기본값은 Serdes.ByteArray().getClass().getName() 입니다.
    - default.value.serde
        - 레코드의 메시지 값을 직렬화, 역직렬화하는 클래스를 지정합니다.
        - 기본값은 Serdes.ByteArray().getClass().getName() 입니다.
    - num.stream.thread
        - 스트림 프로세싱 실행 시 실행될 스레드 개수를 지정합니다.
        - 기본값은 1
    - state.dir
        - 상태기반 데이터 처리를 할 때 데이터를 저장할 디렉토리를 지정합니다.
        - 기본값은 /tmp/kafka-streams
    
#### 스트림즈DSL - stream(), to()
가장 간단한 스트림 프로세싱은 특정 토픽의 데이터를 다른 토픽으로 전달하는 것입니다.  
stream_log 토픽에서 stream_log_copy 토픽으로 옮기는 요구사항을 구현해 봅시다.  
특정 토픽을 KStream 형태로 가져오려면 stream 메서드를 사용하면 되고 KStream 데이터를 특정 토픽으로 저장하려면 to 메서드를 사용하면 됩니다.  

> stream_log 토픽 -> (소스 프로세서) stream() -> (싱크 프로세서) to() -> stream_log_copy 토픽


```groovy
implementation 'org.apache.kafka:kafka-streams'
```
의존성을 추가해 줍니다.
```java
public class SimpleStreamApplication {

    private static String APPLICATION_NAME = "streams-application"; // (1)
    private static String BOOTSTRAP_SERVERS = "my-kafka:9092";
    private static String STREAM_LOG = "stream_log";
    private static String STREAM_LOG_COPY = "stream_log_copy";

    public static void main(String[] args) {

        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, APPLICATION_NAME); // (1)
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass()); // (2)
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass()); // (2)

        StreamsBuilder builder = new StreamsBuilder(); // (3)
        KStream<String, String> stream = builder.stream(STREAM_LOG); // (4)

        stream.to(STREAM_LOG_COPY); // (5)

        KafkaStreams streams = new KafkaStreams(builder.build(), props); // (6)
        streams.start(); // (6)

    }
}
```
1. 스트림즈 애플리케이션은 application.id 값을 기준으로 병렬처리 하기 때문에 아이디를 지정해야 합니다. 만약 다른 스트림즈 애플리케이션을 운영하고 싶다면 다른 아이디를 작성합니다.
2. 직렬화, 역직렬화 방식을 지정합니다. 데이터를 처리할 때는 역직렬화하고 데이터를 토픽에 넣을 때는 직렬화해서 저장합니다.
3. 스트림 토폴로지를 정의하기 위한 용도로 사용합니다.
4. stream_log 토픽으로부터 KStream 객체를 만들기 위해 streamBuilder의 stream 메서드를 사용합니다. StreamBuilder는 KTable을 만드는 table(), GlobalKTable을 만드는 globalTable()메서드를 지원합니다. 이 세 메서드들은 최초의 토픽 데이터를 가져오느 소스 프로세서입니다.
5. stream_log의 토픽을 담은 KStream 객체를 다른 토픽으로 전송하기 위해 to 메서드를 사용합니다. to 메서드는 KStream 인스턴스의 데이터들을 특정 토픽으로 저장하기 위한 용도로 사용되므로 to 메서드는 싱크 프로세서입니다.
6. StreamBuilder로 정의한 토폴로지에 대한 정보와 스트림즈 실행을 위한 옵션을 파라미터로 주고 KafkaStreams 인스턴스를 생성합니다. 실행하기 위해서 start 메서드를 사용하면 됩니다. 결과적으로 stream_log 토픽의 데이터를 stream_log_copy 토픽으로 전달하게 됩니다. 

소스를 실행시키기 전에 사전 작업을 해봅시다.
```sh
# 토픽 만들기
bin/kafka-topics.sh --create --bootstrap-server my-kafka:9092 --partitions 3 --topic stream_log

# 프로듀서 만들기
bin/kafka-console-producer.sh --bootstrap-server my-kafka:9092 --topic stream_log

# 데이터 넣기
>hello
>my
>name
>is
>backtony

# java 애플리에키션 실행시키고 나서 stream_log_copy 토픽 확인
bin/kafka-console-consumer.sh --bootstrap-server my-kafka:9092 --topic stream_log_copy --from-beginning
```

#### 스트림즈DSL - filter()
토픽으로 들어온 문자열 데이터 중 문자열의 길이가 5보다 큰 경우만 필터링하는 스트림즈 애플리케이션을 스트림 프로세서를 사용해 만들 수 있습니다.  
메시지 키 또는 메시지 값을 필터링하여 특정 조건에 맞는 데이터를 골라낼 때는 filter() 메서드를 사용합니다.  
filter 메서드는 스트림즈DSL에서 사용 가능한 필터링 스트림 프로세서입니다.  

> stream_log 토픽 -> (소스 프로세서) stream() -> (스트림 프로세서) filter() -> (싱크 프로세서) to() -> stream_log_filter 토픽

```java
public class StreamsFilter {

    private static String APPLICATION_NAME = "streams-filter-application";
    private static String BOOTSTRAP_SERVERS = "my-kafka:9092";
    private static String STREAM_LOG = "stream_log";
    private static String STREAM_LOG_FILTER = "stream_log_filter";

    public static void main(String[] args) {

        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, APPLICATION_NAME);
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, String> streamLog = builder.stream(STREAM_LOG);
        streamLog.filter((key, value) -> value.length() > 5).to(STREAM_LOG_FILTER); // (1)

        KafkaStreams streams;
        streams = new KafkaStreams(builder.build(), props);
        streams.start();
    }
}
```
1. 데이터를 필터링하는 filter 메서드는 자바의 함수형 인터페이스인 predicate를 파라미터로 받습니다. 메시지 값의 길이가 5보다 큰 경우로 필터링해주도록 했습니다.

```sh
# 앞서 데이터는 넣어놨으니 java 애플리케이션을 실행시키고 확인해 봅시다.
bin/kafka-console-consumer.sh --bootstrap-server my-kafka:9092 --topic stream_log_filter --from-beginning
```

#### 스트림즈DSL - KTable과 KStream을 join()
KTable과 KStream은 메시지 키를 기준으로 조인할 수 있습니다.  
대부분의 DB는 정적으로 저장된 데이터를 조인하지만 카프카에서는 실시간으로 들어오는 데이터를 조인할 수 있습니다.  
예를 들어, 이름을 메시지 키, 주소를 메시지 값으로 갖고 있는 KTable과 이름을 메시지 키, 주문한 물품을 메시지 값으로 가지고 있는 KStream이 존재한다고 하면 사용자가 물품을 주문하면 이미 토픽에 저장된 이름:주소 형식의 KTable과 조인하여 물품과 주소가 결합된 데이터를 새로 생성할 수 있습니다.  
즉, 사용자의 이벤트 데이터를 데이터베이스에 저장하지 않고도 조인하여 스트리밍 처리할 수 있다는 것입니다.  
이를 통해 이벤트 기반 스트리밍 데이터 파이프라인을 구성할 수 있습니다.  
<br>

> (소스 프로세서) stream(), table() -> (스트림 프로세서) join() -> (싱크 프로세서) to()

KStream과 KTable을 조인할 때 가장 중요한 것은 코파티셔닝이 되어있는지 확인하는 것입니다.  
따라서, 각각 사용할 토픽을 생성할 때 동일한 파티션 개수, 동일한 파티셔닝을 사용해야 합니다.  
<Br>

만들어 봅시다.

```sh
# address 토픽 생성
bin/kafka-topics.sh --create --bootstrap-server my-kafka:9092 --partitions 3 --topic address

# order 토픽 생성
bin/kafka-topics.sh --create --bootstrap-server my-kafka:9092 --partitions 3 --topic order

# ordeR_join 토픽 생성
bin/kafka-topics.sh --create --bootstrap-server my-kafka:9092 --partitions 3 --topic order_join
```

```java
public class KStreamJoinKTable {

    private static String APPLICATION_NAME = "order-join-application";
    private static String BOOTSTRAP_SERVERS = "my-kafka:9092";
    private static String ADDRESS_TABLE = "address";
    private static String ORDER_STREAM = "order";
    private static String ORDER_JOIN_STREAM = "order_join";

    public static void main(String[] args) {

        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, APPLICATION_NAME);
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        // KTable과 KStream 인스턴스 생성
        StreamsBuilder builder = new StreamsBuilder();
        KTable<String, String> addressTable = builder.table(ADDRESS_TABLE);
        KStream<String, String> orderStream = builder.stream(ORDER_STREAM);

        orderStream.join(addressTable, // (1)
                (order, address) -> order + " send to " + address) // (2)
                .to(ORDER_JOIN_STREAM); // (3)

        KafkaStreams streams = new KafkaStreams(builder.build(), props);
        streams.start();

    }
}
```
1. 조인을 위해 KStream 인스턴스의 join 메서드를 사용합니다. 첫 번째 파라미터로 조인을 수행할 KTable 인스턴스를 넣습니다.
2. 동일한 메시지 키를 가진 데이터를 찾았을 경우 각각의 메시지 값을 조합해서 어떤 데이터를 만들지 정의합니다. 
3. 조인을 통해 생성된 데이터를 order_join 토픽으로 저장하기 위해 to 싱크 프로세서를 사용합니다.

```sh
# java 애플리케이션을 키고 진행합니다.

# address 토픽에 데이터 넣기
bin/kafka-console-producer.sh --bootstrap-server my-kafka:9092 --topic address --property "parse.key=true" --prop
erty "key.separator=:"
# 입력
>backtony:seoul

# order 토픽에 데이터 넣기
bin/kafka-console-producer.sh --bootstrap-server my-kafka:9092 --topic order --property "parse.key=true" --property "key.separator=:"
# 입력
>backtony:iphone

# order_join 토픽에서 데이터 확인
bin/kafka-console-consumer.sh --bootstrap-server my-kafka:9092 --topic order_join --property print.key=true --pro
perty key.separator=":" --from-beginning
# 출력
backtony:iphone send to seoul
```
order_join 토픽에서 데이터가 합쳐진 것을 확인할 수 있고 사용했던 메시지 키는 조인이 된 데이터의 메시지 키로 들어갑니다.  
```sh
# 데이터 추가
bin/kafka-console-producer.sh --bootstrap-server my-kafka:9092 --topic address --property "parse.key=true" --property "key.separator=:"
>backtony:jeju

# 데이터 추가
bin/kafka-console-producer.sh --bootstrap-server my-kafka:9092 --topic order --property "parse.key=true" --property "key.separator=:"
>backtony:tesla

# 다시 확인
bin/kafka-console-producer.sh --bootstrap-server my-kafka:9092 --topic address --property "parse.key=true" --property "key.separator=:"
backtony:iphone send to seoul
backtony:tesla send to jeju
```
현재 address 토픽에 backtony:seoul로 되어 있는데 값은 jeju로 변경해서 데이터를 다시 넣게 되면 현재 Java 애플리케이션에서 address는 KTable이므로 가장 최근에 데이터를 유효한 데이터로 인지합니다.  
order는 KStream이므로 여러 데이터를 그대로 KTable과 조인시키기 때문에 order_join 토픽을 다시 확인해보면 2개의 데이터가 조인되어 레코드로 저장되있는 것을 확인할 수 있습니다.  

#### 스트림즈DSL - GlobalKTable과 KStream을 join()
코파티셔닝되지 않은 데이터를 조인하는 방법은 두 가지가 있습니다.  
1. 리파티셔닝을 수행한 이후에 코파티셔닝이 된 상태로 조인 처리
2. KTable로 사용하는 토픽을 GlobalKTable로 선언

GlobalKTable을 사용해서 만들어 봅시다.  
```sh
# 파티션이 2개인 토픽을 만들어 줍니다.
bin/kafka-topics.sh --create --bootstrap-server my-kafka:9092 --partitions 2 --topic address_v2
```

```java
public class KStreamJoinGlobalKTable {

    private static String APPLICATION_NAME = "global-table-join-application";
    private static String BOOTSTRAP_SERVERS = "my-kafka:9092";
    private static String ADDRESS_GLOBAL_TABLE = "address_v2";
    private static String ORDER_STREAM = "order";
    private static String ORDER_JOIN_STREAM = "order_join";

    public static void main(String[] args) {

        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, APPLICATION_NAME);
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        StreamsBuilder builder = new StreamsBuilder();
        // GlobalKTable 인스턴스 생성
        GlobalKTable<String, String> addressGlobalTable = builder.globalTable(ADDRESS_GLOBAL_TABLE);
        KStream<String, String> orderStream = builder.stream(ORDER_STREAM);

        orderStream.join(addressGlobalTable, // (1)
                (orderKey, orderValue) -> orderKey, // (2)
                (order, address) -> order + " send to " + address) // (3)
                .to(ORDER_JOIN_STREAM); // (4)

        KafkaStreams streams = new KafkaStreams(builder.build(), props);
        streams.start();
    }
}
```
1. GlobalKTable을 조인하기 위해 join 메서드를 사용하고 첫 번재 파라미터로 GlobalKTable 인스턴스를 넣어줍니다.
2. GlobalKTable은 KTable의 조인과 다르게 레코드를 매칭할 때 KStream의 메시지 키와 메시지 값을 둘다 사용할 수 있습니다. 여기서는 KStream의 메시지 키와 GlobalKTable의 메시지 키를 매칭시켰습니다.
3. 주문한 물품과 주소를 조합하여 데이터를 만들었습니다.
4. order_join 토픽으로 데이터를 저장합니다.

```sh
# java 애플리케이션을 실행
# order 토픽에 데이터 추가
bin/kafka-console-producer.sh --bootstrap-server my-kafka:9092 --topic order --property "parse.key=true" --property "key.separator=:"
>hongil:tesla
>haemin:porsche

# address_v2 토픽에 데이터 추가
bin/kafka-console-producer.sh --bootstrap-server my-kafka:9092 --topic address_v2 --property "parse.key=true" --property "key.separator=:"
>hongil:seoul
>haemin:busan

# 확인
bin/kafka-console-consumer.sh --bootstrap-server my-kafka:9092 --topic order_join --property print.key=true --property key.separator=":" --from-beginning
hongil:tesla send to seoul
haemin:porsche send to busan
```
언뜻 보면 KTable와 크게 다르지 않아 보이지만 GlobalKTable로 선언한 토픽은 토픽에 존재하는 모든 데이터를 태스크마다 저장하고 조인 처리를 수행합니다.  
그리고 조인 시 메시지 값을 기준으로 매칭할 수도 있다는 점이 다릅니다.  

### 프로세서 API
스트림즈DSL은 데이터 처리, 분기, 조인을 위한 다양한 메서드를 제공하지만 추가적인 상세 로직의 구현이 필요하다면 프로세서 API를 사용해야 합니다.  
프로세서 API에는 KStream, KTable, GlobalKTable 개념이 없습니다.  
<br>

토픽의 문자열 길이가 5 이상인 데이터를 필터링해서 다른 토픽으로 저장하는 애플리케이션을 만들어 봅시다.  
```java
public class FilterProcessor implements Processor<String,String,String,String> { // (1)

    private ProcessorContext context; // (2)

    @Override
    public void init(ProcessorContext<String, String> context) { // (3)
        this.context = context;
    }

    @Override
    public void process(Record<String, String> record) { // (4)
        if (record.value().length() >= 5) {
            context.forward(record);
        }
        context.commit(); 
    }

    @Override
    public void close() { // (5)
    }
}
```
1. 스트림 프로세서 클래스를 생성하기 위해서는 kafka-streams 라이브러리에서 제공하는 Processor 인터페이스를 구현해야 합니다.
2. ProcessorContext 클래스는 프로세서에 대한 정보를 담고 있습니다. ProcessorContext 클래스로 생성된 인스턴스로 현재 스트림 처리 중인 토폴로지의 토픽 정보, 애플리케이션 아이디를 조회할 수 있습니다. 또는 schedule, forward, commit 등의 프로세싱 처리에 필요한 메서드도 제공합니다.
3. init 메서드는 스트림 프로세서의 생성자입니다. 프로세싱 처리에 필요한 리소스를 선언하는 구문이 들어갈 수 있습니다. 여기서는 ProcessorContext를 받아서 초기화시켜 줍니다.
4. 실질적인 프로세싱 로직이 들어가는 부분입니다. 1개의 레코드를 받는 것을 가정하여 데이터를 처리합니다. 여기서는 메시지 값의 길이가 5이상인 경우를 필터링 했습니다. 필터링 된 데이터는 forward 메서드를 통해 다음 토폴로지(다음 프로세서)로 넘어갑니다. 처리가 완료된 이후에는 commit 메서드를 호출하여 명시적으로 처리가 완료됬음을 알립니다.
5. FilterProcessor가 종료되기 전에 호출되는 메서드입니다. 프로세싱을 하기 위해 사용했던 리소스를 해제하는 구문을 넣으면 됩니다. 여기서는 해제할 리소스가 없으므로 비어둡니다.

<br>


```java
public class SimpleKafkaProcessor {

    private static String APPLICATION_NAME = "processor-application";
    private static String BOOTSTRAP_SERVERS = "my-kafka:9092";
    private static String STREAM_LOG = "stream_log";
    private static String STREAM_LOG_FILTER = "stream_log_filter";

    public static void main(String[] args) {

        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, APPLICATION_NAME);
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        Topology topology = new Topology(); // (1)
        topology.addSource("Source", STREAM_LOG) // (2)
                .addProcessor("Process", // (3)
                        () -> new FilterProcessor(), // (3)
                        "Source") // (3)
                .addSink("Sink", // (4)
                        STREAM_LOG_FILTER, // (4)
                        "Process"); // (4)

        KafkaStreams streaming = new KafkaStreams(topology, props); // (5)
        streaming.start();
    }
}
```
1. Topology 클래스는 프로세서 API를 사용한 토폴로지 구성을 위해 필요합니다.
2. stream_log 토픽을 소스 프로세서로 가져오기 위해 addSource 메서드를 사용합니다. 첫 파라미터는 소스 프로세서의 이름, 두 번째 파라미터는 대상 토픽의 이름을 입력합니다.
3. 스트림 프로세서를 사용하기 위해 addProcessor 메서드를 사용합니다. 첫 파라미터는 스트림 프로세서의 이름을 입력합니다. 두 번째 파라미터는 앞서 정의한 프로세서 인스턴스를 입력합니다. 세 번째 파라미터는 부모 노드를 입력해야 하는데 여기서 부모 노드는 "Source"입니다. 즉, addSource를 통해 선언한 소스 프로세서의 다음 프로세서는 "Process" 스트림 프로세서 입니다. 
4. stream_log_filter를 싱크 프로세서로 사용하여 데이터를 저장하기 위해 addSink 메서드를 사용합니다. 첫 번째 파라미터는 싱크 프로세서의 이름, 두 번째 파라미터는 저장할 토픽 이름, 세 번째 파라미터는 부모 노드를 입력합니다. 부모 노드는 앞선 "Process"입니다.
5. 세팅한 topology 인스턴스를 이용해 KafkaStreams 인스턴스를 만들고 start로 실행해 줍니다.

```sh
# java 애플리케이션 실행하고 진행
# stream_log 토픽에 데이터 추가
bin/kafka-console-producer.sh --bootstrap-server my-kafka:9092 --topic stream_log

# 확인
bin/kafka-console-consumer.sh --bootstrap-server my-kafka:9092 --topic stream_log_filter --from-beginning
```

## 카프카 커넥트
카프카 커넥트는 카프카 오픈 소스에 포함된 툴 중 하나로 데이터 파이프 라인 생성 시 반복 작업을 줄이고 효율적인 전송을 이루기 위한 애플리케이션입니다.  
파이프라인을 생성할 때 프로듀서, 컨슈머 애플리케이션을 만드는 것도 좋지만 반복적인 파이프라인 생성 작업이 있을 때는 매번 프로듀서와 컨슈머를 만드는 것은 비효율적입니다.  
커넥트는 특정한 작업 형태를 템플릿으로 만들어놓은 커넥터를 실행함으로써 반복 작업을 줄여줍니다.  
파티프라인 생성 시 자주 반복되는 값들(토픽 이름, 파일 이름, 테이블 이름 등)을 파라미터로 받는 커넥터를 코드로 작성하면 이후 파이프라인을 실행할 때는 코드를 작성할 필요가 없어집니다.  
<br>

커넥터는 프로듀서 역할을 하는 __'소스 커넥터'__ 와 컨슈머 역할을 하는 __'싱크 커넥터'__ 로 나뉩니다.  
카프카 2.6에 포함된 커넥트를 실행할 경우 클러스터 간 토픽 미러링을 지원하는 미러메이커2 커넥터와 파일 싱크 커넥터, 파일 소스 커넥터를 기본 플러그인으로 제공합니다.  
이외에 추가적인 커넥터를 사용하고 싶다면 플러그인 형태로 커넥터 jar파일을 추가해 사용할 수 있습니다.  
오픈 소스 커넥터는 직접 커넥터를 만들 필요 없이 커넥터 jar 파일을 다운받아 사용하면 됩니다. 이미 S3 커넥터, 엘라스틱서치 커넥터 등이 공개되어 있습니다.  
<br>

사용자가 커넥트에 커넥터 생성 명령을 내리면 커넥트는 내부에 커넥터와 태스크를 생성합니다. 커넥터는 태스크들을 관리합니다.  
태스크는 커넥터에 종속되는 개념으로 실질적인 데이터 처리를 합니다. 즉, 커넥터를 실행하면 태스크가 실행된다고 보면 됩니다.  
사용자가 커넥터를 사용하여 파이프라인을 생성할 때 컨버터와 트랜스폼 기능을 옵션으로 추가할 수 있습니다.  
컨버터는 데이터 처리를 하기 전에 스키마를 변경할 수 있도록 도와주고 트랜스폼은 데이터 처리 시 각 메시지 단위로 데이터를 간단하게 변환하기 위한 용도로 사용됩니다.  

### 커넥트 실행하는 방법
커넥트는 단일 모드 커넥트와 분산 모드 커넥트가 있습니다.  
단일 모드 커넥트는 1개 프로세스만 실행되는 단일 애플리케이션으로 개발환경에서 사용합니다.  
분산 모드 커넥트는 2대 이상의 서버에서 클러스터 형태로 운영함으로써 데이터 처리량 변화, 중단 이슈에도 대처할 수 있습니다.  

> 소스 애플리케이션 -> (분산모드 카프카 커넥트) 카프카 커넥터 -> 카프카 토픽 -> (분산모드 카프카 커넥트) 싱크 커넥터 -> 싱크 애플리케이션

커넥트는 8083 포트로 REST API를 제공합니다.

요청 메서드|호출 경로|설명
---|---|---
GET|/|실행 중인 커넥트 정보 확인
GET|/connectors|실행 중인 커넥터 이름 확인
POST|/connectors|새로운 커넥터 생성 요청
GET|/connectors/{커넥터 이름}|실행 중인 커넥터 정보 확인
GET|/connectors/{커넥터 이름}/config|실행 중인 커넥터의 설정값 확인
PUT|/connectors/{커넥터 이름}/config|실행 중인 커넥터 설정값 변경 요청
GET|/connectors/{커넥터 이름}/status|실행 중인 커넥터 상태 확인
POST|/connectors/{커넥터 이름}/restart|실행 중인 커넥터 재시작 요청
PUT|/connectors/{커넥터 이름}/pause|커넥터 일시 중지 요청
PUT|/connectors/{커넥터 이름}/resume|일시 중지된 커넥터 실행 요청
DELETE|/connectors/{커넥터 이름}/|실행 중인 커넥터 종료
GET|/connectors/{커넥터 이름}/tasks|실행 중인 커넥터의 태스크 정보 확인
GET|/connectors/{커넥터 이름}/tasks/{태스크아이디}/status|실행 중인 커넥터의 태스크 상태 확인
POST|/connectors/{커넥터 이름}/tasks/{태스크아이디}/restart|실행 중인 커넥터의 태스크 재시작 요청
GET|/connectors/{커넥터 이름}/topics|커넥터별 연동된 토픽 정보 확인
GET|/connector-plugins/|커넥트에 존재하는 커넥터 플러그인 확인
PUT|/connector-plugins/{커넥터 플러그인 이름}/config/validate|커넥터 생성 시 설정값 유효 여부 확인


### 단일 모드 커넥트
단일 모드 커넥트를 실행하기 위해서는 단일 모드 커넥트를 참조하는 설정 파일인 connect-standalone.properties 파일을 수정해야 합니다.  
해당 파일은 카프카 바이너리 디렉토리의 config 디렉토리에 위치해 있습니다.
```sh
bootstrap.servers=my-kafka:9092 #1

# 2
key.converter=org.apache.kafka.connect.json.JsonConverter
value.converter=org.apache.kafka.connect.json.JsonConverter
key.converter.schemas.enable=false
value.converter.schemas.enable=false

offset.storage.file.filename=/tmp/connect.offsets # 3
offset.flush.interval.ms=10000 # 4

# 5
#plugin.path=/
```
1. 커넥트와 연동할 카프카 클러스터의 호스트와 포트를 작성합니다. 두 개 이상 작성할 경우 콤마를 구분으로 적으면 됩니다.
2. 데이터를 카프카에 저장할 때, 가져올 때 변환하는데 사용하는 컨버터를 등록합니다. 기본적으로 JsonConverter, StringConverter, ByteArrayConverter를 제공합니다. 만약 스키마 형태를 사용하고 싶지 않다면 enable 옵션을 false로 설정하면 됩니다.
3. 단일 모드 커넥트는 로컬 파일에 오프셋 정보를 저장합니다. 오프셋 정보는 소스 커넥터, 싱크 커넥터가 데이터 처리 시점을 저장하기 위해 사용됩니다. 
4. 태스크가 처리 완료한 오프셋을 커밋하는 주기를 설정합니다.
5. 플러그인 형태로 추가할 커넥터의 디렉토리 주소를 입력합니다. 오픈소스로 다운받거나 직접 개발한 커넥터의 jar 파일의 위치를 명시하면 됩니다, 두 개 이상의 경우 콤마로 구분합니다. 

단일 모드 커넥트는 커넥트 설정파일과 함께 커넥터 설정파일도 정의하여 실행해야 합니다.  
예시로 카프카에서 기본으로 제공하는 파일 소스 커넥터를 살펴봅시다.  
파일 소스 커넥터는 특정 위치에 있는 파일을 읽어서 토픽으로 데이터를 저장하는 데에 사용됩니다.  
config/connect-file-source.properties로 저장되어 있습니다.
```sh
name=local-file-source # 1
connector.class=FileStreamSource # 2
tasks.max=1 # 3
file=test.txt # 4
topic=connect-test% # 5
```
1. 커넥터 이름을 지정합니다. 유일해야 합니다.
2. 커넥터의 클래스 이름을 지정합니다. 여기서는 기본 클래스 중 하나인 FileStreamSource 클래스를 지정했습니다.
3. 커넥터로 실행할 태스크 개수를 지정합니다. 태스크 개수를 늘려서 병렬처리할 수 있습니다. 예를 들어, 다수의 파일을 읽어서 토픽에 저장하고 싶다면 태스크 개수를 늘려서 병렬처리할 수 있습니다.
4. 읽을 파일의 위치를 지정합니다.
5. 읽은 파일의 데이터를 저장할 토픽의 이름을 지정합니다.

```sh
# 실행
bin/connect-standalone.sh config/connect-standalone.properties config/connect-file-cource.properties
```
단일 모드 커넥트를 실행 시 파라미터로 커넥트 설정 파일과 커넥터 설정파일을 차례로 넣어주면 됩니다.

### 분산 모드 커넥트
분산 모드 커넥트는 2개 이상의 프로세스가 1개의 그룹으로 묶여서 운영됩니다.  
설정파일은 config/connect-distributed.properties 에 존재합니다.
```sh
bootstrap.servers=localhost:9092 

# 1
group.id=connect-cluster 

key.converter=org.apache.kafka.connect.json.JsonConverter
value.converter=org.apache.kafka.connect.json.JsonConverter
key.converter.schemas.enable=true
value.converter.schemas.enable=true

# 2
offset.storage.topic=connect-offsets
offset.storage.replication.factor=1
config.storage.topic=connect-configs
config.storage.replication.factor=1
status.storage.topic=connect-status
status.storage.replication.factor=1

# 3
offset.flush.interval.ms=10000

#plugin.path=
```
1. 다수의 커넥트 프로세스들을 묶을 그룹 이름을 지정합니다. 동일한 group.id로 지정된 커넥트들은 같은 그룹으로 인식됩니다. 같은 그룹으로 지정된 커넥트들에서 커넥터가 실행되면 커넥트들에 분산되어 실행됩니다. 
2. 분산 모드 커넥트는 카프카 내부 토픽에 오프셋 정보를 저장합니다.
3. 태스크가 처리 완료한 오프셋을 커밋하는 주기를 설정합니다.

분산 모드 커넥트를 실행할 때는 커넥트 설정파일만 있으면 됩니다.  
커넥터는 커넥트가 실행된 이후에 REST API를 통해 실행/중단/변경할 수 있기 때문입니다.

```sh
# 실행
bin/connect-distributed.sh config/connect-distributed.properties
```

### 소스 커넥터
소스 커넥터는 소스 애플리케이션 또는 소스 파일로부터 데이터를 가져와 토픽으로 넣는 역할을 합니다.  
카프카 커넥트 라이브러리에서 제공하는 SourceConnector와 SourceTask 클래스를 사용하여 직접 소스 커넥터를 구현할 수 있습니다.  
직접 구현한 소스 커넥터를 빌드하여 jar파일로 만들고 커넥트를 실행 시 플러그인으로 추가하여 사용할 수 있습니다.  

```groovy
implementation 'org.apache.kafka:connect-api'
```
의존성을 추가해줍니다.  
소스 커넥터를 만들 때 앞서 2개의 클래스를 사용한다고 햇습니다.
+ SourceConnector : 태스크를 실행하기 전에 커넥터 설정파일을 초기화하고 어떤 태스크 클래스를 사용할 것인지 정의
+ SourceTask : 실제로 데이터를 다루고 토픽으로 데이터를 보내는 역할

각각 어떤 부분을 구현해야 하는지 알아봅시다.
```java
public class TestSourceConnector extends SourceConnector { // 1
    @Override
    public String version() {} // 2

    @Override
    public void start(Map<String, String> props) {} // 3

    @Override
    public Class<? extends Task> taskClass() {} // 4

    @Override
    public List<Map<String, String>> taskConfigs(int maxTasks) {} // 5

    @Override
    public ConfigDef config() {} // 6

    @Override
    public void stop() {} // 7
}
```
1. 상속받은 클래스를 정의합니다. 사용자가 지정한 이 클래스 이름은 최종적으로 커넥트에서 호출할 때 사용되므로 명확하게 어떻게 사용되는 커넥터인지 이름을 잘 명시해야 합니다.
2. 커넥터의 버전을 리턴합니다. 커넥트에 포함된 커넥터 플러그인을 조회할 때 이 버전이 노출됩니다. 커넥터를 지속적으로 유지보수하고 신규 배포할 때 이 메서드가 리턴하는 버전 값을 변경해야 합니다.
3. 사용자가 JSON 또는 config 파일 형태로 입력한 설정값을 초기화하는 메서드입니다. 만약 올바르지 않다면 ConnectException이 호출됩니다.
4. 커넥터가 사용할 태스크 클래스를 지정합니다.
5. 태스크 개수가 2개 이상인 경우 태스크마다 각기 다른 옵션을 부여할 때 사용합니다.
6. 커넥터가 사용할 설정값에 대한 정보를 받습니다. 
7. 커넥터가 종료될 때 필요한 로직을 작성합니다.

<br>

```java
public class TestSourceTask extends SourceTask {
    @Override
    public String version() { } // 1

    @Override
    public void start(Map<String, String> props) {} // 2

    @Override
    public List<SourceRecord> poll() throws InterruptedException {} // 3

    @Override
    public void stop() {} // 4
}
```
1. 태스크의 버전을 지정합니다. 보통 커넥터의 version 메서드에서 지정한 버전과 동일하게 작성합니다.
2. 태스크가 시작할 때 필요한 로직을 작성합니다. 태스크는 실질적으로 데이터를 처리하는 역할을 하므로 데이터 처리에 필요한 모든 리소스를 여기서 초기화하면 됩니다.
3. 소스 애플리케이션 또는 소스 파일로부터 데이터를 읽어오는 로직을 작성합니다. 데이터를 읽어오면 토픽으로 보낼 데이터를 SourceRecord로 정의합니다. SourceRecord 클래스는 토픽으로 데이터를 정의하기 위해 사용됩니다. List\<SourceRecord> 인스턴스에 데이터를 담아 리턴하면 데이터가 토픽으로 전송됩니다.
4. 태스크가 종료될 때 필요한 로직을 작성합니다.


## 카프카 미러메이커2
카프카 미러메이커2는 서로 다른 두 개의 카프카 클러스터 간에 토픽을 복제하는 애플리케이션입니다.  

> 카프카 클러스터1의 토픽들 -> 미러메이커2 -> 카프카 클러스터2로 복제


### 미러메이커2를 활용한 단방향 토픽 복제
미러메이커2를 사용하기 위해서는 config/connect-mirror-maker.properties 파일을 수정해야 합니다.  
해당 파일에는 클러스터들에 대한 정보와 토픽 정보, 토픽을 복제하면서 사용할 내부 토픽에 대한 정보가 포함됩니다.  
카프카 클러스터 A에서 B로 옮기는 형식으로 작성해보겠습니다.
```sh
# 복제할 클러스터의 닉네임을 작성합니다.
# 여기서 클러스터의 이름을 A, B라고 지칭했는데 이 클러스터 닉네임은 이후 옵션에서 다시 사용됩니다.
# 또한 토픽이 복제될 때 복제된 토픽의 접두사로 붙게 됩니다.
# 예를 들어, 클러스터 A에 있는 click_log 토픽을 클러스터 B로 복제하게 되면 클러스터 B에는 A.click_log로 토픽이 생성됩니다.
clusters = A, B

# 미러메이커2에서 사용할 클러스터 접속 정보를 작성합니다.
A.bootstrap.servers = a-kafka:9092
B.bootstrap.servers = b-kafka:9092

# 클러스터A에서 클러스터 B로 복제를 진행할 것인지 어떤 토픽을 복제할 것인지 명시합니다.
A->B.enabled = true
A->B.topics = test

# 미러메이커2는 양방향 토픽 복제가 가능합니다.
# 여기서는 false로 지정해 줍시다.
B->A.enabled = false
B->A.topics = .*

# 복제되어 신규 생성된 토픽의 복제 개수를 지정합니다.
replication.factor=1

# 토픽 복제에 필요한 데이터를 저장하는 내부 토픽의 복제 개수를 설정합니다.
checkpoints.topic.replication.factor=1
heartbeats.topic.replication.factor=1
offset-syncs.topic.replication.factor=1
offset.storage.replication.factor=1
status.storage.replication.factor=1
config.storage.replication.factor=1
```
```sh
# 실행
bin/connect-mirror-maker.sh config/connect-mirror-maker.properties

# test 토픽에 데이터 추가
bin/kafka-console-producer.sh --bootstrap-server a-kafka:9092 -- topic test

# 확인하기
bin/kafka-console-consumer.sh --bootstrap-server b-kafka:9092 --topic A.test --from-beginning
```
복제된 토픽에는 클러스터 이름을 접두사로 붙이므로 클러스터 A의 test 토픽은 클러스터 B로 A.test 토픽으로 저장됩니다.  
```sh
# 동기화 테스트
# 파티션 개수 변경
bin/kafka-topics.sh --bootstrap-server a-kafka:9092 --topic test --alter --partitions 5

# 확인
bin/kafka-topics.sh --bootstrap-server b-kafka:9092 --topic A.test --describe 
```
미러메이커2는 실행 시 기본값으로 5초마다 토픽의 설정값을 확인하고 동기화합니다.  
변경 후 5초 뒤에 클러스터 B의 A.test 토픽의 파티션 개수가 변경된 것을 확인할 수 있습니다.  

