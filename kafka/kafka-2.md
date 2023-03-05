# 카프카 기본 개념 - 1

## 카프카 브로커, 클러스터, 주키퍼
카프카 브로커는 카프카 클라이언트와 데이터를 주고받기 위해 사용하는 주체이자, 데이터를 분산 저장하여 장애가 발생하더라도 안전하게 사용할 수 있도록 도와주는 애플리케이션입니다.  
하나의 서버에는 한 개의 카프카 브로커 프로세스가 실행됩니다.  
보통은 데이터를 안전하게 보관하고 처리하기 위해 3대 이상의 브로커 서버를 1개의 클러스터로 묶어서 운영합니다.  
카프카 클러스터로 묶인 브로커들은 프로듀서가 보낸 데이터를 안전하게 분산 저장하고 복제하는 역할을 수행합니다.  

### 데이터 저장, 전송
프로듀서로부터 데이터를 전달받으면 카프카 브로커는 프로듀서가 요청한 토픽의 파티션에 데이터를 저장하고 컨슈머가 데이터를 요청하면 파티션에 저장된 데이터를 전달합니다.  
프로듀서로부터 전달된 데이터는 파일 시스템에 저장됩니다.  
```sh
ls /tmp/kafka-logs
```
config/server.properties의 log.dir 옵션에 정의한 디렉토리에 데이터가 저장됩니다.  
카프카는 메모리나 데이터베이스에 저장하지 않으며 따로 캐시 메모리를 구현하여 사용하지도 않습니다.  
파일 시스템에 저장하기 때문에 파일 입출력으로 인해 속도 이슈가 있다고 생각할 수 있지만 카프카는 __페이지 캐시__ 를 사용하여 디스크 입출력 속도를 높여 문제를 해결했습니다.  
페이지 캐시란 OS에서 파일 입출력의 성능 향상을 위해 만들어 놓은 메모리 영역을 말합니다.  
이러한 특징 때문에 카프카 브로커를 실행하는데 힙 메모리 사이즈를 크게 설정할 필요가 없습니다.  

### 데이터 복제, 싱크
데이터 복제는 카프카를 장애 허용 시스템으로 동작하도록 하는 원동력입니다.  
복제의 이유는 클러스터로 묶인 브로커 중 일부에 장애가 발생하더라도 데이터를 유실하지 않고 안전하게 사용하기 위함입니다.  
카프카의 데이터 복제는 파티션 단위로 이루어집니다. 토픽을 생성할 때 파티션의 복제 개수도 같이 설정하는데 직접 옵션을 선택하지 않으면 브로커에 설정된 옵션 값을 따라갑니다.  
복제 개수의 최솟값은 1(복제없음)이고 최댓값은 브로커 개수만큼 사용할 수 있습니다.  
만약 복제 개수가 3(자신+복제2개)으로 총 3개의 파티션이 생기게 되었다면 리더 파티션과 팔로워 파티션으로 구성됩니다.  
프로듀서 또는 컨슈머와 직접 통신하는 파티션을 리더, 나머지 복제 데이터를 갖는 파티션을 팔로워라고 합니다.  
팔로워 파티션들은 리더 파티션의 오프셋을 확인하여 현재 자신이 가지고 있는 오프셋과 차이가 나는 경우 리더 파티션으로부터 데이터를 가져와서 자신의 파티션에 저장하는데 이 과정을 복제라고 합니다.  
만약 리더 파티션을 갖고 있는 브로커에 장애가 발생해 다운되면 팔로워 파티션 중 하나가 리더 파티션 지위를 넘겨 받습니다.  
이를 통해 데이터가 유실되지 않고 컨슈머나 프로듀서와 데이터를 주고받도록 동작할 수 있게 합니다.

### 컨트롤러
클러스터의 다수 브로커 중 한 대가 컨트롤러의 역할을 합니다.  
컨트롤러는 다른 브로커들의 상태를 체크하고 브로커가 클러스터에서 빠지는 경우 해당 브로커에 존재하는 리더 파티션을 재분배합니다.  
만약 컨트롤러 역할을 하는 브로커에 문제가 생기면 다른 브로커가 컨트롤러 역할을 수행합니다.

### 데이터 삭제
카프카는 다른 메시징 플랫폼과 다르게 컨슈머가 데이터를 가져가더라도 토픽의 데이터는 삭제되지 않습니다.  
또한 컨슈머나 프로듀서가 데이터 삭제 요청을 할 수 없습니다.  
오직 브로커만이 데이터를 삭제할 수 있습니다.  
데이터 삭제는 파일 단위로 이뤄지는데 이 단위를 '로그 세그먼트'라고 합니다.  
이 세그먼트에는 다수의 데이터가 들어 있기 때문에 일반적인 데이터베이스처럼 특정 데이터를 선별해서 삭제할 수 없습니다.  
세그먼트는 데이터가 쌓이는 동안 파일 시스템으로 열려있으며 카프카 브로커에 log.segment.bytes 또는 log.segment.ms 옵션에 값이 설정되면 설정값에 따라 세그먼트 파일이 닫힙니다.  
세그먼트 파일이 닫히게 되는 기본값은 1GB입니다.  
닫힌 세그먼트 파일은 log.retention.bytes 또는 log.retention.ms 옵션에 설정값이 넘으면 삭제 됩니다.  
닫힌 세그먼트 파일을 체크하는 간격은 카프카 브로커의 옵션에 설정된 log.retention.check.interval.ms에 따릅니다.  

### 컨슈머 오프셋 저장
컨슈머 그룹은 토픽을 특정 파티션으로부터 데이터를 가져가서 처리하고 이 파티션의 어느 레코드까지 가져갔는지 확인하기 위해 오프셋을 커밋합니다.  
커밋한 오프셋은 __consumer_offsets 토픽에 저장되고 저장된 오프셋을 토대로 컨슈머 그룹을 다음 레코드를 가져가서 처리합니다.

### 코디네이터
클러스터의 다수 브로커 중 한 대는 코디네이터 역할을 수행합니다.  
코디네이터는 컨슈머 그룹의 상태를 체크하고 파티션을 컨슈머와 매칭되도록 분배하는 역할을 합니다.  
컨슈머가 컨슈머 그룹에서 빠지면 매칭되지 않은 파티션을 정상 동작하는 컨슈머로 할당하여 끊임없이 데이터가 처리되도록 도와줍니다.  
이렇게 파티션을 컨슈머로 재할당하는 과정을 '리밸런스' 라고 합니다.  

### 주키퍼
주키퍼는 카프카의 메타데이터를 관리하는 데에 사용됩니다.  
주키퍼 쉘 명령어를 통해 사용할 수 있는 내부 명령어들에 대한 설명은 [공식 홈페이지](https://zookeeper.apache.org/doc/current/zookeeperStarted.html)를 참고바랍니다.  
주키퍼 쉘 명령어는 zookeeper-shell.sh로 실행할 수 있으며 bin 폴더 안에 있습니다.  
카프카 서버에서 직접 주키퍼에 붙으려면 카프카 서버에서 실행되고 있는 주키퍼에 연결해야 합니다.
```sh
# 여기서 나오는 my-kafka는 앞선 포스팅에서 세팅한 호스트 주소입니다. 앞선 포스팅을 참고하세요.
# 동일 환경에서 실행되는 주키퍼에 접속
bin/zookeeper-shell.sh my-kafka:2181 

# root znode의 하위 znode들을 확인
# 카프카 실행 시 주키퍼의 하위 경로로 지정하지 않았으므로 카프카는 root znode를 기준으로 데이터를 저장
ls /

# 카프카 브로커에 대한 정보 확인
get /brokers/ids/0

# 어느 브로커가 컨트롤러인지에 대한 정보
get /controller

# 카프카에 저장된 토픽 확인
ls /brokers/topics
```
카프카 클러스터로 묶인 브로커들은 동일한 경로의 주키퍼 경로로 선언해야 같은 카프카 브로커 묶음이 됩니다.  
만약 클러스터를 여러 개로 운영한다면 한 개의 주키퍼에 다수의 카프카 클러스터를 연결할 수도 있습니다.  

<br>

__주키퍼에 다수의 카프카 클러스터를 사용하는 방법__  
주키퍼의 서로 다른 znode에 카프카 클러스터들을 설정하면 됩니다.  
znode란 주키퍼에서 사용하는 데이터 저장 단위입니다. 마치 파일 시스템처럼 znode 간에 계층 구조를 갖습니다. 1개의 znode에는 n개의 하위 znode가 존재하고 계속 tree 구조로 znode가 존재합니다.  
2개 이상의 카프카 클러스터를 구축할 때는 root znode(최상위 znode)가 아닌 한 단계 아래의 znode를 카프카 브로커 옵션으로 지정합니다. 각기 다른 하위 znode로 설정된 서로 다른 카프카 클러스터는 각 클러스터의 데이터에 영향을 미치지 않고 정상 동작합니다.


## 토픽과 파티션
토픽은 카프카에서 데이터를 구분하기 위해 사용하는 단위로 토픽은 1개 이상의 파티션을 소유하고 있습니다.  
파티션에는 프로듀서가 보낸 데이터들이 들어가 저장되는데 이 데이터를 '레코드'라고 합니다.  

> 토픽 안에 여러 개의 파티션이 있고 파티션 안에 저장된 데이터들 각각을 레코드라고 한다.

파티션은 카프카의 병렬처리의 핵심으로써 그룹으로 묶인 컨슈머들이 레코드를 병렬로 처리할 수 있도록 매칭됩니다.  
컨슈머의 처리량이 한정된 상황에서 많은 레코드를 병렬로 처리하는 가장 좋은 방법은 컨슈머의 개수를 늘려 스케일 아웃하는 것입니다.  
컨슈머의 개수를 늘림과 동시에 파티션 개수도 늘리면 처리량이 증가하는 효과를 볼 수 있습니다.  
파티션은 큐와 비슷한 구조로 생각하면 쉽습니다. FIFO구조와 같이 먼저 들어간 레코드는 컨슈머가 먼저 가져가게 됩니다.  
다만 큐에서는 데이터를 가져가면서 레코드가 삭제되지만 카프카에서는 삭제하지 않습니다.  
파티션의 레코드는 컨슈머가 가져가는 것과 별개로 관리됩니다.  
이러한 특징 때문에 토픽의 레코드는 다양한 목적을 가진 여러 컨슈머 그룹들이 토픽의 데이터를 여러 번 가져갈 수 있습니다.  

### 토픽 이름 제약 조건
+ 빈 문자열 불가능
+ 마침표 하나(.) 또는 마침표 둘(..)로 생성될 수 없다.
+ 길이는 249자 미만
+ 영어 대소문자, 숫자 0부터 9, 마침표(.), 언더바(_), 하이픈(-) 조합으로만 생성 가능
+ 카프카 내부 로직 관리 목적으로 사용되는 토픽 __customer_offsets, __transaction_state와 동일한 이름으로 생성 불가
+ 카프카 내부 사용 로직 때문에 마침표와 언더바가 동시에 들어가면 안 된다. 생성은 가능하지만 이슈가 발생한다.
+ 이미 생성된 토픽 이름의 마침표를 언더바로 바꾸거나 언더바를 마침표로 바꾼 경우 신규 토픽 이름과 동일하다면 생성할 수 없다.

### 의미 있는 토픽 이름 작명 방법
토픽 이름을 통해 어떤 개발환경에서 사용되는 것인지 판단 가능해야 하고 어떤 애플리케이션에서 어떤 데이터 타입으로 사용되는지 유추할 수 있어야 합니다.  
대소문자가 구분되기 때문에 실수를 방지하기 위해 케밥케이스 또는 스네이크 표기법과 같이 소문자를 쓰되 구분자로 특수문자를 조합하는 것을 권장합니다.  

+ <환경>.<팀명>.<애플리케이션명>.<메시지타입>
    - ex) prd.marketing-team.sms-plateform.json
+ <프로젝트명>.<서비스명>.<환경>.<이벤트명>
    - ex) commerce.payment.prd.notification
+ <환경>.<서비스명>.<지라번호>.<메시지타입>
    - ex) dev.email-sender.jira-1234.email-vo-custom
+ <카프카-클러스터명>.<환경>.<서비스명>.<메시지타입>
    - ex) aws-kafka.live.marketing-platform.json

카프카는 토픽 이름 변경을 지원하지 않으므로 이름을 변경하기 위해서는 삭제 후 다시 생성하는 것 외에는 방법이 없습니다.  
따라서 생성 당시 규칙을 잘 정하고 따르는 것이 중요합니다.  

### 레코드
레코드는 타임스탬프, 메시지 키, 메시지 값, 오프셋으로 구성됩니다.  
프로듀서가 생성한 레코드가 브로커로 전송되면 오프셋과 타임스탬프가 지정되어 저장됩니다.  
브로커에 한번 적재된 레코드는 수정할 수 없고 로그 리텐션 기간 또는 용량에 따라서만 삭제됩니다.  

#### 메시지 키
메시지 키는 메시지 값을 순서대로 처리하거나 메시지 값의 종류를 나타내기 위해 사용합니다.  
메시지 키를 사용하면 프로듀서가 토픽에 레코드를 전송할 때 메시지 키의 해시값을 토대로 파티션을 지정하게 됩니다.  
즉, 동일한 메시지 키라면 동일한 파티션으로 들어가게 됩니다.  
다만 어느 파티션에 지정될지는 알 수 없고 파티션 개수가 변경되면 매칭이 달라집니다.  
만약 메시지 키를 사용하지 않는다면 프로듀서에서 레코드를 전송할 때 메시지 키를 선언하지 않으면 됩니다.  
이때는 null로 설정되고 해당 레코드는 기본 설정 파티셔너에 따라서 파티션에 분배되어 적재됩니다.  

#### 메시지 값
메시지 값에는 실질적으로 처리할 데이터가 들어 있습니다.  
메시지 키와 메시지 값은 직렬화되어 브로커로 전송되기 때문에 컨슈머가 이용할 때는 직렬화한 형태와 동일한 형태로 역직렬화를 수행해야 합니다.  
예를 들어 프로듀서가 StringSerializer로 직렬화한 메시지 값을 컨슈머가 IntegerDeserializer로 역직렬화하면 정상적인 데이터를 얻을 수 없습니다.  

#### 오프셋
오프셋은 0이상의 숫자로 이뤄집니다.  
레코드의 오프셋은 직접 지정할 수 없고 브로커에 저장될 때 이전에 전송된 레코드의 오프셋 + 1 의 값으로 생성됩니다.  
오프셋은 카프카 컨슈머가 데이터를 가져갈 때 사용됩니다. 
오프셋을 사용하면 컨슈머 그룹으로 이뤄진 카프카 컨슈머들이 파티션의 데이터를 어디까지 가져갔는지 명확히 지정할 수 있게 됩니다.  

## 카프카 클라이언트
카프카 클러스터에 명령을 내리거나 데이터를 송수신하기 위해 카프카 클라이언트 라이브러리는 카프카 프로듀서, 컨슈머, 어드민 클라이언트를 제공하는 카프카 클라이언트를 사용하여 애플리케이션을 개발합니다.  
카프카 클라이언트는 라이브러리이기 때문에 자체 라이프사이클을 갖는 프레임워크나 애플리에키션 위에서 구현하고 실행해야 합니다.  

### 프로듀서 API
프로듀서는 데이터를 전송할 때 리더 파티션을 가지고 있는 카프카 브로커와 직접 통신합니다.  
프로듀서를 구현하는 가장 기본적인 방식은 카프카 클라이언트를 라이브러리로 추가하여 자바 애플리케이션을 만드는 것입니다.  

```sh
bin/kafka-topics.sh --bootstrap-server my-kafka:9092 --create --topic test --partitions 3
```
자바 애플리케이션을 만들기 전에 테스트 용으로 토픽부터 만들어 줍니다.  
```groovy
// 의존성 추가
implementation 'org.springframework.kafka:spring-kafka'
```
```java
@Slf4j
public class SimpleProducer {
    // 전송하고자 하는 토픽 이름
    private final static String TOPIC_NAME = "test";
    // 전송하고자 하는 카프카 클러스터 서버의 host와 IP
    private final static String BOOTSTRAP_SERVERS = "my-kafka:9092";

    public static void main(String[] args) {

        // 설정하기
        Properties configs = new Properties();
        // 서버 지정
        configs.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        // serializer 지정
        configs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        configs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        // properties를 파라미터로 추가하여 kafka 인스턴스 생성
        // 인스턴스는 producerRecord를 전송할 때사용
        KafkaProducer<String, String> producer = new KafkaProducer<>(configs);

        // 레코드 생성, 키는 명시하지 않았으므로 null 처리
        String messageValue = "testMessage";
        // 제네릭은 키, 값의 타입 -> 앞서 configs에 설정한 serializer와 동일한 타입이어야 한다.
        ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC_NAME, messageValue);

        // record 전송
        // 즉각 전송이 아니라 record를 프로듀서 내부에 가지고 있다가 배치 형태로 묶어서 브로커에 전송한다.
        producer.send(record);

        log.info("{}", record);

        // 프로듀서 내부 버퍼에 가지고 있던 레코드 배치를 브로커로 전송
        producer.flush();
        // producer 인스턴스와 리소스들을 안전하게 종료
        producer.close();
    }
}
```
```java
// 출력 화면

// 카프카 프로듀서 구동 시 설정한 옵션들이 출력된다.
// 선택 옵션들도 모두 출력되므로 설정한 옵션이 잘 들어갔는지 확인할 수 있다.
22:15:50.962 [main] INFO org.apache.kafka.clients.producer.ProducerConfig - ProducerConfig values: 
	acks = -1
	batch.size = 16384
	bootstrap.servers = [my-kafka:9092]
	buffer.memory = 33554432
	client.dns.lookup = use_all_dns_ips
	client.id = producer-1
	compression.type = none
	connections.max.idle.ms = 540000
	delivery.timeout.ms = 120000
	enable.idempotence = true
	interceptor.classes = []
	key.serializer = class org.apache.kafka.common.serialization.StringSerializer
	... 생략

// 버전 정보
22:15:51.368 [main] INFO org.apache.kafka.common.utils.AppInfoParser - Kafka version: 3.0.0

// 전송한 producerRecord 출력
22:15:51.968 [main] INFO com.example.kafkaapp.SimpleProducer - ProducerRecord(topic=test, partition=null, headers=RecordHeaders(headers = [], isReadOnly = true), key=null, value=testMessage, timestamp=null)
```
실제로 잘 적재 되었는지도 확인해 봅시다.
```sh
bin/kafka-console-consumer.sh --bootstrap-server my-kafka:9092 --topic test --from-beginning
```

#### 프로듀서 중요 개념
> ProducerRecord -> send 호출 -> Partitioner -> Accumulator 내부에 토픽별로 배치를 만들어 저장 -> sender -> 카프카 클러스터

프로듀서는 카프카 브로커로 데이터를 전송할 때 내부적으로 파티셔너, 배치 생성 단계를 거칩니다.  
앞서 자바 코드에서 사용한 ProducerRecord 내부에는 토픽, 파티션, 타임스탬프, 메시지 키, 메시지 값이 필드로 있습니다.  
인스턴스 생성 시 필수 파라미터인 토픽, 메시지 값만 설정해도 되지만 생성 시 추가 파라미터를 사용해 변경할 수 있습니다.  
즉, 파티션 번호를 직접 지정하거나 타임 스탬프를 설정할 수도 있습니다.  
레코드의 타임스탬프는 카프카 브로커에 저장될 때 브로커 시간을 기준으로 설정되지만 필요에 따라 레코드 생성 시간 또는 다른 시간으로도 설정할 수 있습니다.  
KafkaProducer 인스턴스가 send 메서드를 호출하면 파티셔너에서 토픽이 어느 파티션으로 전송될 것인지 정해집니다.  
KafkaProducer 인스턴스를 생성할 때 파티셔너를 따로 설정하지 않았다면 기본값인 DefaultPartitioner로 설정되어 파티션이 정해집니다.  
파티셔너에 의해 구분된 레코드는 데이터를 전송하기 전에 Accumulator에 데이터를 버퍼로 쌓아놓고 버퍼로 쌓인 데이터는 배치로 묶어서 전송하게 됩니다.  
프로듀서 API를 사용하면 UniformStickyPartitioner와 RoundRobinPartitioner 2개 파티션을 제공합니다.  
둘 다 메시지 키가 있을 때는 메시지 키의 해시값과 파티션을 매칭하여 데이터를 전송합니다.  
메시지 키가 없을 때는 파티션에 최대한 동일하게 분배하는 로직이 들어있는데 전자가 후자의 단점을 개선했다고 보면 됩니다.  
후자는 레코드가 들어오는 대로 파티션을 순회하면서 전송하기 때문에 배치로 묶이는 빈도가 적었지만, 전자는 Accumulator에서 데이터가 배치로 모두 묶일 때까지 기다렸다가 배치로 묶인 데이터는 모두 동일한 파티션에 전송함으로써 성능적으로 이점을 가져갑니다.  
카프카 클라이언트 라이브러리에서는 사용자 지정 파티셔너를 생성하기 위한 Partitioner 인터페이스를 제공하고 이를 통해 메시지 키, 메시지 값에 따른 파티션 지정 로직도 적용할 수 있습니다.  
카프카 프로듀서는 압축 옵션을 통해 브로커로 전송 시 압축 방식을 정할 수 있습니다.  
압축 옵션을 정하지 않으면 압축이 되지 않은 상태로 전송됩니다.  
gzip, snappy, lz4, zstd를 지원하는데 압축하면 네트워크 처리량에 이득을 볼 수 있지만 압축을 하는데 리소스를 사용하게 되고 컨슈머쪽에서는 압축을 풀게 되면서 리소스를 사용하기 때문에 적절한 압축 옵션을 사용하는 것이 중요합니다.  

#### 프로듀서 주요 옵션
+ 필수 옵션
    - bootstrap.servers
        - 프로듀서가 데이터를 전송할 대상 카프카 클러스터에 속한 브로커의 '호스트 이름:포트' 를 1개 이상 작성합니다.
        - 2개 이상 브로커 정보를 입력하여 일부 브로커에 이슈가 발생하더라도 접속하는 데에 이슈가 없도록 설정할 수 있습니다.
    - key.serializer
        - 레코드 메시지 키 직렬화 클래스를 지정합니다.
    - value.serializer 
        - 레코드 메시지 값을 직렬화 클래스를 지정합니다.
+ 선택 옵션
    - acks
        - 프로듀서가 전송한 데이터가 브로커들에 정상적으로 저장되었는지 전송 성공 여부 확인에 사용하는 옵션으로 0,1,-1(all)로 구성됩니다.
        - 1(기본값) : 리더 파티션에 데이터가 저장되면 전송 성공으로 판단
        - 0 : 프로듀서가 전송한 즉시 브로커에 데이터 저장 여부와 상관없이 성공으로 판단
        - -1 또는 all : min, insync, replicas 개수에 해당하는 리더 파티션과 팔로워 파티션에 데이터가 저장되면 성공으로 판단
    - buffer.memory
        - 브로커로 전송할 데이터를 배치로 모으기 위해 설정할 버퍼 메모리 양을 지정합니다.
        - 기본값은 32MB
    - retries
        - 프로듀서가 브로커로부터 에러를 받고 난 뒤 재전송을 시도하는 횟수를 지정합니다.
        - 기본값은 2147483647
    - batch.size
        - 배치로 전송할 레코드 최대 용량을 지정합니다.
        - 기본값은 16384
        - 클라이언트에 장애가 발생하면 배치 내 있던 메시지는 전달되지 않기 때문에 고가용성이 필요한 경우라면 배치 사이즈를 주지 않는 것도 방법입니다.
    - linger.ms
        - 배치 전송하기 전까지 기다리는 최소 시간입니다.
        - 기본값 0
        - 배치형태의 메시지를 보내기 전에 추가적인 메시지들을 위해 기다리는 시간으로 배치 사이즈에 도달하면 이 시간과 관계없이 메시지를 즉시 전송하고 배치 사이즈에 도달하지 못한 상황에서 이 시간만큼 도달했을 경우, 메시지를 전송합니다.
    - partitioner.class
        - 레코드를 파티션에 전송할 때 적용하는 파티셔너 클래스를 지정합니다.
        - 기본값은 DefaultPartitioner
    - enable.idempotence
        - 멱등성 프로듀서로 동작할지 여부를 설정합니다.
        - 기본값은 false
    - transaction.id
        - 프로듀서가 레코드를 전송할 때 레코드를 트랜잭션 단위로 묶을지 여부를 설정합니다.
        - 프로듀서의 고유한 트랜잭션 아이디를 설정할 수 있습니다.
        - 이 값을 설정하면 트랜잭션 프로듀서가 동작하고 기본값은 null입니다.
    - max.reqeust.size
    	- 프로듀서가 보낼 수 있는 최대 메시지 바이트 사이즈입니다.
    	- 기본값은 1MB

#### 메시지 키를 가진 데이터를 전송하는 프로듀서
```java
ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC_NAME, key, messageValue);
```
앞서 작성했던 코드에서 2번 째 인자로 key값을 추가해주면 키를 포함해서 보낼 수 있습니다.  
파티션을 직접 지정하고 싶다면 토픽 이름, 파티션 번호, 메시지 키, 메시지 값 순으로 넣어주면 됩니다.  
잘 들어갔는지는 아래 명령어로 확인할 수 있습니다.  
```sh
bin/kafka-console-consumer.sh --bootstrap-server my-kafka:9092 --topic test --property print.key=true --property key.separator="-" --from-beginning
```

#### 커스텀 파티셔너를 가지는 프로듀서
프로듀서 사용환경에 따라 특정 데이터를 가지는 레코드를 특정 파티션으로 보내야 할 때가 있습니다.  
예를 들어, backtony라는 값을 가진 메시지 키가 0번 파티션으로 들어가야 한다면 Partitioner 인터페이스를 사용하여 사용자 정의 파티셔너를 생성해서 지정해줘야 합니다.  
```java
public class CustomPartitioner implements Partitioner {
    
    // partition 메서드에는 레코드를 기반으로 파티션을 정하는 로직이 포함됩니다.
    // 리턴 값은 주어진 레코드가 들어갈 파티션 번호입니다.
    @Override
    public int partition(String topic, Object key, byte[] keyBytes, Object value, byte[] valueBytes, Cluster cluster) {
        
        // 메시지 키를 지정하지 않은 경우 비정상적인 데이터로 간주하고 예외를 발생시킵니다.
        if (keyBytes == null){
            throw new InvalidRecordException("need message key");            
        }
        
        // 메시지 키가 backtony인 경우 파티션 0으로 지정합니다.
        if(((String)key).equals("backtony")){
            return 0;
        }

        // backtony가 아닌 경우 레코드는 해시값을 지정하여 특정 파티션에 매칭되도록 설정합니다.
        List<PartitionInfo> partitions = cluster.partitionsForTopic(topic); // 해당 토픽의 파티션들 가져오기
        int numPartitions = partitions.size();
        return Utils.toPositive(Utils.murmur2(keyBytes)) % numPartitions;
    }

    @Override
    public void close() {}

    @Override
    public void configure(Map<String, ?> configs) {}
}
```
```java
// 앞선 SimpleProducer클래스에서 Properties를 다루는 부분에 partitioner를 지정하는 코드를 추가해주면 됩니다.
configs.put(ProducerConfig.PARTITIONER_CLASS_CONFIG,CustomPartitioner.class);
```

#### 브로커 정상 전송 여부를 확인하는 프로듀서
KafkaProducer의 send 메서드는 Future 객체를 반환합니다.  
이 객체는 RecordMetadata의 비동기 결과를 표현하는 것으로 ProducerRecord가 카프카 브로커에 정상적으로 적재되었는지에 대한 데이터가 포함되어 있습니다.  
```java
Future<RecordMetadata> metadataFuture = producer.send(record);
RecordMetadata recordMetadata = metadataFuture.get();
log.info("{}",recordMetadata);

// 결과
23:39:17.516 [main] INFO com.example.kafkaapp.ch3.SimpleProducer - test-2@3
```
send의 결과값은 카프카 브로커로부터 응답을 기다렸다가 브로커로부터 응답이 오면 RecordMetadata 인스턴스를 반환합니다.  
레코드가 정상적으로 브로커에 적재되었다면 토픽 이름과 파티션 번호, 오프셋 번호가 출력됩니다.  
위 로그는 test 토픽의 2번째 파티션에 적재되었고 레코드에 부여된 오프셋 번호는 3이라는 의미입니다.  
그러나 동기로 프로듀서의 전송 결과를 확인하는 것은 프로듀서가 전송하고 난 뒤 브로커로부터 전송에 대한 응답 값을 받기 전까지 대기하기 때문에 빠른 전송에 허들이 될 수 있습니다.  
대안으로 프로듀서는 비동기로 결과를 확인할 수 있도록 Callback 인터페이스를 제공합니다.  
```java
@Slf4j
public class ProducerCallback implements Callback {
    @Override
    public void onCompletion(RecordMetadata metadata, Exception exception) {
        if (exception!=null){
            log.error(exception.getMessage(),exception);
        }
        else {
            log.info(metadata.toString());
        }        
    }
}
```
onCompletion 메서드는 레코드의 비동기 결과를 받기 위해 사용합니다.  
```java
producer.send(record, new ProducerCallback());
```
send 메서드를 호출할 때 인자로 넣어주면 됩니다.  
비동기로 결과를 받을 경우 동기로 결과를 받는 경우보다 더 빠른 속도로 데이터를 추가 처리할 수 있게 됩니다.  
하지만 데이터의 순서가 중요한 경우라면 사용하면 안 됩니다.  
비동기로 결과를 기다리는 동안 다음으로 보낼 데이터의 전송이 성공하고 앞서 보낸 데이터의 결과가 실패한 경우 재전송으로 인해 데이터 순서가 역전될 수 있기 때문입니다.  
그러므로 데이터의 순서가 중요하다면 동기로 전송 결과를 받아야 합니다.  

### 컨슈머 API
프로듀서가 전송한 데이터는 카프카 브로커에 적재되고 컨슈머는 적재된 데이터를 사용하기 위해 브로커로부터 데이터를 가져와서 필요한 처리를 합니다.  

```java
@Slf4j
public class SimpleConsumer {

    private final static String TOPIC_NAME = "test";
    private final static String BOOTSTRAP_SERVERS = "my-kafka:9092";    
    private final static String GROUP_ID = "test-group";

    public static void main(String[] args) {
        // consumer 인스턴스 설정 세팅
        Properties configs = new Properties();
        configs.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        configs.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);        
        // 반드시 프로듀서에서 직렬화한 타입으로 역직렬화 해야 한다.
        configs.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        configs.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        
        // 세팅한 것을 인자로 주고 컨슈머 인스턴스 생성
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(configs);
        
        // 컨슈머에게 토픽을 할당하는 메서드로 인자로 1개 이상의 토픽 이름을 컬렉션으로 받는다.
        consumer.subscribe(Arrays.asList(TOPIC_NAME));
        
        while (true) {
            // 컨슈머는 poll 메서드로 데이터를 가져와 처리한다.
            // 지속적으로 데이터를 처리하기 위해서는 반복문을 사용해야 한다.
            // poll 메서드는 Duration 타입을 인자로 받고 이는 브로커로부터 데이터를 가져올 때 컨슈머 버퍼에 데이터를 기다리기 위한 타임아웃 간격이다.
            // poll 메서드는 ConsumerRecord 리스트를 반환한다.
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(1));
            
            // ConsumerRecord 리스트를 순사적으로 꺼내서 처리
            for (ConsumerRecord<String, String> record : records) {
                log.info("record:{}", record);
            }
        }
    }
}
```
대부분은 주석으로 적어두었고 프로듀서와 특별히 다른 점이 있다면 컨슈머 그룹을 선언해야 한다는 것입니다.  
컨슈머 그룹을 통해 컨슈머의 목적을 구분하고 컨슈머 오프셋을 관리하기 때문에 subscribe 메서드를 사용해 토픽을 구독하는 경우에는 컨슈머 그룹을 선언해야 합니다.  
그래야만 컨슈머가 중단되거나 재시작되더라도 컨슈머 그룹의 컨슈머 오프셋을 기준으로 이후 데이터를 처리할 수 있습니다.  
컨슈머 그룹을 선언하지 않으면 어떤 그룹에도 속하지 않는 컨슈머로 동작하게 됩니다.  

#### 컨슈머 중요 개념
토픽의 파티션으로부터 데이터를 가져가기 위해 컨슈머를 운영하는 방법은 크게 2가지가 있습니다.  
첫 번째는 1개 이상의 컨슈머로 이뤄진 컨슈머 그룹을 운영하는 것이고 두 번째는 토픽의 특정 파티션만 구독하는 컨슈머를 운영하는 것입니다.  
<br>

컨슈머 그룹으로 운영하는 방법은 컨슈머를 각 컨슈머 그룹으로부터 격리된 환경에서 안전하게 운영할 수 있도록 도와주는 카프카의 독특한 방식입니다.  
컨슈머 그룹으로 묶인 컨슈머들은 토픽에 있는 1개 이상 파티션들에 할당되어 데이터를 가져갈 수 있습니다.  
반면에 파티션은 최대 1개의 컨슈머에 할당 가능합니다.  
이러한 특징으로 컨슈머 그룹의 컨슈머 개수는 가져가고자 하는 토픽의 파티션 개수와 같거나 작아야 합니다.  
만약 커지게 되면 특정 컨슈머는 파티션이 할당되지 않아 스레드만 차지하게 되기 때문입니다.  
컨슈머 그룹은 다른 컨슈머 그룹과 격리되는 특징을 가지고 있어 컨슈머 그룹끼리 영향을 받지 않게 처리할 수 있기 때문에 컨슈머 그룹으로 따로 나눌 수 있는 경우는 최대한 나누는 것이 좋습니다.  
<br>

컨슈머 그룹으로 이뤄진 컨슈머들 중 일부 컨슈머에 장애가 발생하면, 장애가 발생한 컨슈머에 할당된 파티션은 장애가 발생하지 않은 컨슈머로 소유권이 넘어가는데 이를 '리밸런싱' 이라고 합니다.  
리밸런싱은 크게 컨슈머가 추가되는 상황, 컨슈머가 제외되는 상황 두 가지에서 발생합니다.  
리밸런싱은 컨슈머가 데이터를 처리하는 도중에 언제든지 발생할 수 있으므로 데이터 처리 중 발생한 리밸런싱에 대응하는 코드를 작성해야 합니다.  
리밸런싱이 발생할 때 파티션의 소유권을 컨슈머로 재할당하는 과정에서 해당 컨슈머 그룹의 컨슈머들이 토픽의 데이터를 읽을 수 없기 때문에 자주 일어나서는 안 됩니다.  
그룹 조정자(group coordinator)는 리밸런싱을 발동시키는 역할을 하는데 컨슈머 그룹의 컨슈머가 추가되고 삭제될 때를 감지합니다.  
카프카 브로커 중 한 대가 그룹 조정자의 역할을 수행합니다.  
<Br>

컨슈머는 카프카 브로커로부터 데이터를 어디까지 가져갔는지 커밋을 통해 기록합니다.  
특정 토픽의 파티션을 어떤 컨슈머 그룹이 몇 번째까지 가져갔는지 카프카 브로커 내부에서 사용되는 내부 토픽(__consumer_offsets)에 기록됩니다.  
컨슈머 동작에 이슈가 발생하여 consumer_offsets 토픽에 어느 레코드까지 읽어갔는지 오프셋 커밋이 기록되지 못했다면 데이터 처리의 중복이 발생할 수 있습니다.  
데이터 처리의 중복이 발생하지 않게 하기 위해서는 컨슈머 애플리케이션이 오프셋 커밋을 정상적으로 처리했는지 검증해야만 합니다.  
오프셋 커밋은 컨슈머 애플리케이션에서 명시적, 비명시적으로 수행할 수 있습니다.  
기본 옵션은 poll 메서드가 수행될 때 일정 간격마다 오프셋을 커밋하도록(enable.auto.commit=true)로 설정되어 있습니다.  
이렇게 일정 간격마다 자동으로 커밋되는 것을 '비명시 오프셋 커밋' 이라고 부릅니다.  
이 옵션은 auto.commit-interval.ms에 설정된 값과 함께 사용되는데, poll 메서드가 auto.commit-interval.ms에 설정된 값 이상이 지났을 때 그 시점까지 레코드 오프셋을 커밋합니다.  
poll 메서드를 호출할 때 커밋을 수행하므로 코드상에서 따로 커밋 관련 코드를 작성할 필요가 없습니다.  
비명시 오프셋 커밋은 편리하지만 poll 메서드 호출 이후에 리밸런싱 또는 컨슈머 강제종료 발생 시 컨슈머가 처리하는 데이터가 중복 또는 유실될 수 있는 가능성이 있는 취약한 구조를 갖고 있습니다.  
그러므로 데이터 중복이나 유실을 허용하지 않는 서비스라면 자동 커밋을 사용하면 안 됩니다.  
명시적으로 오프셋을 커밋하려면 poll 메서드 호출 이후에 반환받은 데이터의 처리가 완료되고 commitSync 메서드를 호출하면 됩니다.  
commitSync 메서드는 poll 메서드를 통해 반환된 레코드의 가장 마지막 오프셋을 기준으로 커밋을 수행합니다.  
commitSync 메서드는 브로커에 커밋 요청을 하고 커밋이 정상적으로 처리되었는지 응답하기까지 기다리는데 이는 컨슈머의 처리량에 영향을 끼칩니다.  
데이터 처리 시간에 비해 커밋 요청 및 응답에 시간이 오래 걸린다면 동일 시간당 데이터 처리량이 줄어들기 때문입니다.  
이를 해결하기 위해 commitAsync 메서드를 사용해 커밋 요청을 전송하고 응답이 오기 전까지 데이터 처리를 수행할 수 있습니다.  
하지만 비동기 커밋은 커밋 요청이 실패했을 경우 현재 처리 중인 데이터의 순서를 보장하지 않으며 데이터의 중복 처리가 발생할 수 있습니다.  
<br>

컨슈머는 poll 메서드를 통해 레코드들을 반환받지만 poll 메서드를 호출하는 시점에 클러스터에서 데이터를 가져오는 것은 아닙니다.  
컨슈머 애플리케이션을 실행하게 되면 내부에서 Fetcher 인스턴스가 생성되어 poll 메서드를 호출하기 전에 미리 레코드들을 내부 큐로 가져옵니다.  
이후에 사용자가 명시적으로 poll을 호출하면 컨슈머는 내부 큐에 있는 레코드들을 반환받아 처리를 수행하게 됩니다.  

#### 컨슈머 주요 옵션
+ 필수 옵션
    - bootstrap.servers
        - 프로듀서가 데이터를 전송할 대상 카프카 클러스터에 속한 브로커 '호스트 이름:포트' 를 1개 이상 작성
        - 2개 이상 입력하여 일부 브로커에 이슈가 발생하더라도 접속하는 데에 이슈가 없도록 설정 권장
    - key.deserializer, value.deserializer : 역직렬화 클래스 지정
+ 선택 옵션
    - group.id
        - 컨슈머 그룹 아이디
        - subscribe 메서드로 토픽을 구독하여 사용할 때는 필수로 넣어야 합니다.
        - 기본값은 null
    - auto.offset.reset
        - 컨슈머 그룹이 특정 파티션을 읽을 때 저장된 컨슈머 오프셋이 없는 경우 어느 오프셋부터 읽을지 선택하는 옵션
        - 이미 컨슈머 오프셋이 있다면 이 옵션값은 무시됩니다.
        - lastest, earliest, none이 있는데 lastest는 가장 마지막(최근)의, earliset는 가장 오래전에 넣은(가장 초기), none은 컨슈머 그룹이 커밋한 기록이 없으면 오류를 반환하고 있다면 기존 커밋 기록 이후부터 오프셋을 읽기 시작합니다.
        - 기본값은 lastest
    - fetch.min.bytes
	- 한번에 가져올 수 있는 최소 데이터 사이즈
	- 만약 지정한 사이즈보다 작은 경우, 요청에 대해 응답하지 않고 데이터가 누적될 때까지 기다립니다.
	- 무작정 기다리는 것은 아니고 fetch.wait.max.ms 설정의 영향을 받습니다.
    - fetch.wait.max.ms
	- 브로커에 fetch.min.bytes 이상의 메시지가 쌓일때까지 최대 대기 시간
	- 기본은 500ms
    - fetch.max.bytes
	- 한번에 가져올 수 있는 최대 데이터 사이즈
    - request.timeout.ms
	- 요청에 대해 응답을 기다리는 최대 시간	
    - enable.auto.commit
        - 자동 커밋 여부 결정
        - 기본값 true
	- auto.commit.interval.ms를 5ms로 설정했다면(기본값) 컨슈머는 poll을 호출할 때 가장 마지막 오프셋을 커밋합니다.
    - auto.commit.interval.ms
        - 자동 커밋일 경우 오프셋 커밋 간격을 지정
        - 기본값 5000(5초)
    - max.poll.records
        - poll 메서드를 통해 반환되는 레코드 개수 지정
        - 기본값 500
    - session.timeout.ms
        - 컨슈머가 브로커와 연결이 끊기는 최대 시간
        - 이 시간 내에 하트비트를 전송하지 않으면 브로커는 컨슈머에 이슈가 발생했다고 가정하고 리밸런싱을 시작합니다.
        - 보통 하트비트 시간 간격의 3배로 설정합니다.
        - 기본값은 10000(10초)
    - heartbeat.interval.ms
        - 하트비트 전송하는 시간 간격
        - 기본값은 3000(3초)
    - max.poll.records
	- 단일 호출 poll에 대한 최대 레코드 수를 조정합니다.
    - max.poll.interval.ms
        - poll 메서드를 호출하는 간격의 최대 시간
	- hearbeat는 주기적으로 보내고 있으나 실제로 메시지를 가져가지 않는 경우가 있을 수 있어, 컨슈머가 무한정 해당 파티션을 점유할 수 없도록 주기적으로 poll을 호출하지 않으면 장애라고 판단합니다.
        - poll 메서드를 호출한 이후에 데이터를 처리하는 데에 시간이 너무 많이 걸리는 경우 비정상으로 판단하고 리밸런싱을 시작합니다.
        - 기본값은 300000(5분)
    - isolation.level
        - 트랜잭션 프로듀서가 레코드를 트랜잭션 단위로 보낼 경우 사용
        - read_committed, read_uncommitted로 설정 가능
        - 전자는 설정하면 커밋이 완료된 레코드만 읽고 후자는 모든 레코드를 읽습니다.
        - 기본값은 read_uncommitted

#### 동기 오프셋 커밋
poll 메서드가 호출된 이후에 commitSync 메서드를 호출하면 오프셋 커밋을 명시적으로 수행할 수 있다고 했습니다. 코드로 봅시다.
```java
@Slf4j
public class ConsumerWithSyncCommit {
    private final static String TOPIC_NAME = "test";
    private final static String BOOTSTRAP_SERVERS = "my-kafka:9092";
    private final static String GROUP_ID = "test-group";

    public static void main(String[] args) {
        Properties configs = new Properties();
        configs.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        configs.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
        configs.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        configs.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());        
        // auto commit 옵션 끄기
        configs.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(configs);
        consumer.subscribe(Arrays.asList(TOPIC_NAME));

        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(1));
            // 데이터 처리
            for (ConsumerRecord<String, String> record : records) {
                log.info("record:{}", record);
            }
            // 커밋
            consumer.commitSync();
        }
    }
}
```
poll로 받은 데이터를 모두 처리하고 commitSync 메서드를 호출하여 커밋합니다.  
동기 커밋의 경우 브로커로 커밋을 요청한 이후에 커밋이 완료될 때까지 기다립니다.  
브로커로부터 컨슈머 오프셋 커밋이 완료되었음을 받기까지 컨슈머는 데이터를 더 처리하지 않고 기다리기 때문에 다른 방식에 비해 동일 시간당 데이터 처리량이 적습니다.  
commiySync에 파라미터가 들어가지 않으면 poll로 반환된 가장 마지막 레코드의 오프셋을 기준으로 커밋됩니다.  
만약 개별 레코드 단위로 매번 오프셋을 커밋하고 싶다면 인자로 Map\<TopicPartition, OffsetAndMetadata> 인스턴스를 넣어주면 됩니다.  
```java
while (true) {
    ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(1));
    Map<TopicPartition, OffsetAndMetadata> currentOffset = new HashMap<>();

    for (ConsumerRecord<String, String> record : records) {
        // 데이터 처리...


        // 하나의 데이터 처리후 Map에 담아서 commitSync 처리
        log.info("record:{}", record);
        currentOffset.put(
                new TopicPartition(record.topic(), record.partition()),
                new OffsetAndMetadata(record.offset() + 1, null));
        consumer.commitSync(currentOffset);
    }
}
```
TopicPartition는 토픽과 파티션 정보를 담고 있고, OffsetAndMetadata는 오프셋 정보를 담고 있습니다.  
여기서 주의할 점은 현재 처리한 오프셋에 1을 더해야 한다는 점입니다.  
컨슈머가 poll을 수행할 때 마지막으로 커밋한 오프셋부터 레코드를 리턴하기 때문입니다.  

#### 비동기 오프셋 커밋
동기 오프셋 커밋을 사용할 경우 커밋 응답을 기다리는 동안 데이터 처리가 일시적으로 중단되기 때문에 더 많은 데이터를 처리하기 위해서 비동기 오프셋 커밋을 사용할 수 있습니다.  

```java
while (true) {
    ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(1));
    for (ConsumerRecord<String, String> record : records) {
        log.info("record:{}", record);
    }
    consumer.commitAsync();
}
```
비동기 오프셋 커밋도 동기 커밋과 마찬가지로 poll 메서드로 리턴된 가장 마지막 레코드를 기준으로 오프셋을 커밋합니다.  
다만 동기 오프셋 커밋과 다른 점은 커밋이 완료될 때까지 응답을 기다리지 않는다는 것입니다.  
이 때문에 동기 오프셋 커밋을 사용할 때보다 동일 시간당 데이터 처리량이 더 많습니다.  
비동기 오프셋 커밋을 사용할 경우 비동기로 커밋 응답을 받기 때문에 callback 함수를 파라미터로 받아서 결과를 얻을 수도 있습니다.
```java
 while (true) {
    ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(1));
    for (ConsumerRecord<String, String> record : records) {
        log.info("record:{}", record);
    }
    consumer.commitAsync(new OffsetCommitCallback() {
        public void onComplete(Map<TopicPartition, OffsetAndMetadata> offsets, Exception e) {
            if (e != null){
                log.error("Commit failed for offsets {}", offsets, e);
                System.err.println("Commit failed");
            }
            else
                System.out.println("Commit succeeded");
        }
    });
}
```
OffsetCommitCallback 함수는 commitAsync의 응답을 받을 수 있도록 도와주는 콜백 인터페이스 입니다.  
정상적으로 커밋되었다면 커밋 완료된 오프셋 정보가 offsets 인자로 들어옵니다.  

#### 리밸런스 리스너를 가진 컨슈머
컨슈머 그룹에서 컨슈머가 추가 또는 제거되면 파티션을 컨슈머에 재할당하는 과정인 리밸런스가 일어납니다.  
poll 메서드를 통해 반환받은 데이터를 모두 처리하기 전에 리밸런스가 발생하면 데이터를 중복 처리할 수 있습니다.  
이유는 poll 메서드를 통해 받은 데이터 중 일부를 처리했으나 커밋하지 않았기 때문입니다.  
리밸런스 발생 시 데이터를 중복 처리하지 않게 하기 위해서는 리밸런스 발생 시 처리한 데이터를 기준으로 커밋을 시도해야 합니다.  
리밸런스 발생을 감지하기 위해 카프카 라이브러리는 ConsumerRebalancListener 인터페이스를 지원합니다.  
onPartitionAssgined 메서드와 onPartitionRevoked 메서드가 존재하는데 전자는 리밸런스가 끝난 뒤에 파티션이 할당 완료되면 호출되는 메서드이고, 후자는 리밸런스가 시작되기 직전에 호출되는 메서드 입니다.  
마지막으로 처리한 레코드를 기준으로 커밋을 하기 위해서는 리밸런스가 시작하기 직전에 커밋을 하면 되므로 후자를 사용하여 처리합니다.  
```java
@Slf4j
public class ConsumerWithRebalanceListener {
    private final static String TOPIC_NAME = "test";
    private final static String BOOTSTRAP_SERVERS = "my-kafka:9092";
    private final static String GROUP_ID = "test-group";


    private static KafkaConsumer<String, String> consumer;
    private static Map<TopicPartition, OffsetAndMetadata> currentOffset;

    public static void main(String[] args) {
        Properties configs = new Properties();
        configs.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        configs.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
        configs.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        configs.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        configs.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,false);

        consumer = new KafkaConsumer<>(configs);
        // 리스터 추가
        consumer.subscribe(Arrays.asList(TOPIC_NAME), new RebalanceListener());

        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(1));
            currentOffset = new HashMap<>();
            
            for (ConsumerRecord<String, String> record : records) {
                log.info("{}", record);
                // 레코드 하나씩 커밋 진행 중
                currentOffset.put(new TopicPartition(record.topic(), record.partition()),
                        new OffsetAndMetadata(record.offset()+1, null));
                consumer.commitSync(currentOffset);
            }
        }
    }

    @Slf4j
    private static class RebalanceListener implements ConsumerRebalanceListener {

        public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
            log.warn("Partitions are assigned : " + partitions.toString());

        }

        public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
            log.warn("Partitions are revoked : " + partitions.toString());
            // 리밸런싱 직전에 커밋
            consumer.commitSync(currentOffset);
        }
    }
}
```

#### 파티션 할당 컨슈머
컨슈머를 운영할 때 subscribe 메서드를 사용하여 구독 형태로 사용하는 것 외에도 직접 파티션을 컨슈머에 명시적으로 할당할 수 있습니다.  
명시적으로 할당할 경우 assign 메서드를 사용하면 되는데 인자로 다수의 TopicPartition 인스턴스를 지닌 컬렉션 타입을 받습니다.  
TopicPartition 클래스는 카프카 라이브러리 내/외부에서 사용되는 토픽, 파티션의 정보를 담는 객체로 사용됩니다.
```java
@Slf4j
public class ConsumerWithExactPartition {
    private final static String TOPIC_NAME = "test";
    private final static int PARTITION_NUMBER  = 0;
    private final static String BOOTSTRAP_SERVERS = "my-kafka:9092";

    public static void main(String[] args) {
        Properties configs = new Properties();
        configs.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        configs.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        configs.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(configs);

        // 파티션 명시적 할당
        consumer.assign(Collections.singleton(new TopicPartition(TOPIC_NAME, PARTITION_NUMBER)));
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(1));
            for (ConsumerRecord<String, String> record : records) {
                log.info("record:{}", record);
            }
        }
    }
}
```
직접 컨슈머가 특정 토픽의 특정 파티션에 할당되므로 리밸런싱하는 과정이 없습니다.  

#### 컨슈머에 할당된 파티션 확인 방법
```java
KafkaConsumer<String, String> consumer = new KafkaConsumer<>(configs);        
consumer.subscribe(Arrays.asList(TOPIC_NAME));
Set<TopicPartition> assignedTopicPartition = consumer.assignment();
```
assignment 메서드는 Set\<TopicPartition> 인스턴스를 반환합니다.  
여기에는 토픽 이름과 파티션 번호가 포함되어 있습니다.

#### 컨슈머의 안전한 종료
정상적으로 종료되지 않은 컨슈머는 세션 타임아웃이 발생할때까지 컨슈머 그룹에 남게 됩니다.  
이로 인해 종료되었지만 더는 동작하지 않는 컨슈머가 존재하여 파티션의 데이터는 소모되지 못하고 컨슈머 랙이 늘어나게 됩니다.  
컨슈머를 안전하게 종료하기 위해 KafkaConsumer 클래스는 wakeup 메서드를 지원합니다.  
wakeup 메서드가 실행된 이후 poll 메서드가 호출되면 WakeupException 예외가 발생합니다.  
해당 예외를 받으면 사용한 자원들을 해제하고 마지막에는 close메서드를 호출하여 카프카 클러스터에 컨슈머가 안전하게 종료되었음을 명시적으로 알려주면 안전하게 종료됩니다.  
close 메서드는 해당 컨슈머가 더이상 동작하지 않는다는 것을 명시적으로 알려주므로 컨슈머 그룹에서 이탈되고 나머지 컨슈머들이 파티션을 할당받게 됩니다.
```java
try {
    while (true) {
        ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(1));
        for (ConsumerRecord<String, String> record : records) {
            logger.info("{}", record);
        }
        consumer.commitSync();
    }
    } catch (WakeupException e) {
        logger.warn("Wakeup consumer");
    } finally {
        logger.warn("Consumer close");
        consumer.close();
    }
}
```
poll 메서드를 통해 지속적으로 레코드들을 받아서 처리하다가 wakeup 메서드가 호출되면 다음 poll 메서드가 호출될 때 wakeupException 예외가 발생합니다.  
예외가 발생하면 catch 문에서 받아서 컨슈머 종료 전에 사용하던 리소스를 해제할 수 있습니다.  
그렇다면 wakeup 메서드는 어디에서 호출하면 될까요?  
자바 애플리케이션의 경우 코드 내부에 셧다운 훅(shutdown hook)을 구현하여 안전한 종료를 명시적으로 구현할 수 있습니다.  
셧 다운 훅이란 사용자 또는 운영체제로부터 종료 요청을 받으면 실행하는 스레드를 뜻합니다.  
```java
public static void main(String[] args){
    Runtime.getRuntime().addShutdownHook(new ShutdownThread());
    ...
}

static class ShutdownThread extends Thread {
    public void run() {
        log.info("Shutdown hook");
        consumer.wakeup();
    }
}
```
사용자는 안전한 종료를 위해 위 코드로 실행된 애플리케이션에 kill -TERM {프로세스 번호}를 호출하여 셧다운 훅을 발생시킬 수 있습니다.  
셧다운 훅이 발생하면 사용자가 정의한 ShutdownThread 스레드가 실행되면서 wakeup 메서드가 호출되어 컨슈머를 안전하게 종료할 수 있습니다.  

### 어드민 API
카프카 클라이언트에서는 내부 옵션들을 설정하거나 조회하기 위해 AdminClient 클래스를 제공합니다.  
이를 통해 클러스터의 옵션과 관련된 부분을 자동화할 수 있고 예시는 다음과 같습니다.
+ 카프카 컨슈머를 멀티 스레드로 생성할 때, 구독하는 토픽의 파티션 개수만큼 스레드를 생성하고 싶을 때, 스레드 생성 전에 해당 토픽의 파티션 개수를 어드민 API를 통해 가져올 수 있다.
+ AdminClient 클래스로 구현한 웹 대시보드를 통해 ACL이 적용된 클러스터의 리소스 접근 권한 규칙을 추가할 수 있다.
+ 특정 토픽의 데이터양이 늘어남을 감지하고 AdminClient 클래스로 해당 토픽의 파티션을 늘릴 수 있다.

```java
Properties configs = new Properties();
configs.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "my-kafka:9092");
AdminClient admin = AdminClient.create(configs);
```
프로듀서, 컨슈머 API와 다르게 추가 설정 없이 클러스터 정보에 대한 설정만 하고 인스턴스를 생성합니다.  
제공하는 주요 메서드들은 다음과 같습니다.  

+ describeCluster(DescribeClusterOptions options) : 브로커의 정보 조회
+ listTopics(ListTopicsOptions options) : 토픽 리스트 조회
+ listConsumerGroups(ListConsumerGroupsOptions options) : 컨슈머 그룹 조회
+ createTopics(Collection \<NewTopic> newTopics, CreateTopicsOptions options) : 신규 토픽 생성
+ createPartitions(Map\<String,NewPartitions> newPartitions, CreatePartitionsOptions options) : 파티션 개수 변경
+ createAcls(Collection \<AclBinding> acls, CreateAclsOptions options) : 접근 제어 규칙 생성

__사용 예시__  
```java
public class KafkaAdminClient {
    private final static Logger logger = LoggerFactory.getLogger(KafkaAdminClient.class);
    private final static String BOOTSTRAP_SERVERS = "my-kafka:9092";

    public static void main(String[] args) throws Exception {

        Properties configs = new Properties();
        configs.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "my-kafka:9092");
        AdminClient admin = AdminClient.create(configs);

        // 브로커 정보 조회
        logger.info("== Get broker information");
        for (Node node : admin.describeCluster().nodes().get()) {
            logger.info("node : {}", node);
            ConfigResource cr = new ConfigResource(ConfigResource.Type.BROKER, node.idString());
            DescribeConfigsResult describeConfigs = admin.describeConfigs(Collections.singleton(cr));
            describeConfigs.all().get().forEach((broker, config) -> {
                config.entries().forEach(configEntry -> logger.info(configEntry.name() + "= " + configEntry.value()));
            });
        }

        logger.info("== Get default num.partitions");
        for (Node node : admin.describeCluster().nodes().get()) {
            ConfigResource cr = new ConfigResource(ConfigResource.Type.BROKER, node.idString());
            DescribeConfigsResult describeConfigs = admin.describeConfigs(Collections.singleton(cr));
            Config config = describeConfigs.all().get().get(cr);
            Optional<ConfigEntry> optionalConfigEntry = config.entries().stream().filter(v -> v.name().equals("num.partitions")).findFirst();
            ConfigEntry numPartitionConfig = optionalConfigEntry.orElseThrow(Exception::new);
            logger.info("{}", numPartitionConfig.value());
        }
        
        logger.info("== Topic list");
        for (TopicListing topicListing : admin.listTopics().listings().get()) {
            logger.info("{}", topicListing.toString());
        }

        logger.info("== test topic information");
        Map<String, TopicDescription> topicInformation = admin.describeTopics(Collections.singletonList("test")).all().get();
        logger.info("{}", topicInformation);

        logger.info("== Consumer group list");
        ListConsumerGroupsResult listConsumerGroups = admin.listConsumerGroups();
        listConsumerGroups.all().get().forEach(v -> {
            logger.info("{}", v);
        });

        admin.close();
    }
}
```
