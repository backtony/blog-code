# 카프카 상세 개념

## 토픽과 파티션

### 적정 파티션 개수
토픽 최초 생성 시 파티션의 개수를 정하는 데에 고려해야 할 점은 3가지입니다.
+ 데이터 처리량
+ 메시지 키 사용 여부
+ 브로커, 컨슈머 영향도

파티션의 개수가 많아지면 많아질수록 1:1 매핑되는 컨슈머 개수가 늘어나기 때문에 파티션은 카프카의 병렬처리의 핵심입니다.  
그렇기 때문에 파티션 개수를 정할 때는 해당 토픽에 필요한 데이터 처리량을 측정하여 정하는 것이 중요합니다.  

<br>

데이터 처리 속도를 올리는 방법은 2가지입니다.  
+ 컨슈머의 처리량을 늘리는 방법
+ 컨슈머를 추가해서 병렬처리량을 늘리는 방법

보통 컨슈머 서버의 사양을 스케일업해서 처리량을 높이는 데에는 한계가 있기 때문에 파티션의 개수를 늘리고 파티션 개수만큼 컨슈머를 늘리는 방법이 가장 확실합니다. 
그러므로 프로듀서가 보내는 데이터양과 컨슈머의 데이터 처리량을 계산해서 파티션 개수를 정하면 됩니다.  
만약 프로듀서가 보내는 데이터가 초당 1,000 레코드이고 컨슈머가 처리할 수 있는 데이터가 초당 100레코드라면 최소한 필요한 파티션 개수는 10개라고 보면 됩니다.  

> 프로듀서 전송 데이터량 < 컨슈머 데이터 처리량 * 파티션 개수

파티션 개수만큼 컨슈머 스레드를 운영한다면 토픽의 병렬 처리를 극대화 할 수 있습니다.  
반면에 전체 컨슈머 데이터 처리량이 프로듀서가 보내는 데이터보다 적다면 컨슈머 랙이 생기고, 데이터 처리 지연이 발생하게 됩니다.  
따라서, 컨슈머 전체 데이터 처리량이 프로듀서 데이터 처리량보다 많아야 합니다.  
컨슈머 데이터 처리량을 구하는 방법은 상용에서 운영 중인 카프카에서 더미 데이터로 테스트 해보는 것입니다.  
컨슈머는 카프카 클러스터와 다른 시스템과 연동되는 특성이 있어 로컬, 테스트 환경과 운영 환경에서 처리량이 차이가 날 확률이 높기 때문에 상용환경에서 테스트를 권장합니다.  
<br>

다음으로는 메시지 키 사용 여부를 정해야 합니다. 더 정확히 말하자면 메시지 키를 사용함과 동시에 데이터 처리 순서를 지켜야 하는 경우에 대해 고려해야 합니다.  
메시지 키를 사용하면 프로듀서가 토픽으로 데이터를 보낼 때 메시지 키를 해시 변환하여 메시지를 파티션에 매칭시킵니다.  
만약 파티션 개수가 달라지면 매칭이 깨지고 다른 파티션에 데이터가 할당되기 때문에 이때부터 컨슈머는 특정 메시지 키의 순서를 보장받지 못합니다.  
따라서 메시지 키를 사용하고 처리 순서가 보장되어야 한다면 최대한 파티션의 변화가 발생하지 않는 방식으로 운영해야 합니다.  
만약 파티션 개수가 변해야 하는 경우에는 기존에 사용하던 메시지 키의 매칭을 그대로 가져가기 위해 커스텀 파티셔너를 개발하고 적용해야 합니다.  
이러한 어려움 때문에 보통 메시지 키별로 처리 순서를 보장하기 위해서는 파티션 개수를 프로듀서가 전송하는 데이터양보다 더 넉넉하게 잡고 생성하는 것이 권장됩니다.  
반면에 처리 순서가 관계 없다면 처음부터 넉넉하게 잡지 않아도 됩니다.  
<br>

마지막으로 고려해야 할 점은 브로커와 컨슈머의 영향도 입니다.  
카프카에서 파티션은 각 브로커의 파일 시스템을 사용하기 때문에 파티션이 늘어나는 만큼 브로커에서 접근하는 파일 개수가 많아집니다.  
그런데 운영체제에서는 프로세스당 열 수 있는 파일 최대 개수를 제한하고 있습니다.  
그러므로 안정적으로 유지하기 위해서는 각 브로커당 파티션 개수를 모니터링 해야하고 데이터양이 많아져서 파티션 개수를 늘려야 하는 상황이라면 브로커당 파티션 개수를 확인하고 진행해야 합니다.  
만약 브로커가 관리하는 파티션 개수가 너무 많다면 파티션 개수를 분산하기 위해 카프카 브로커 개수를 늘리는 방안도 같이 고려해야 합니다.  

### 토픽 정리 정책
토픽의 데이터는 시간 용량에 따라 삭제 규칙을 적용할 수 있습니다.  
데이터를 사용하지 않을 경우 cleanup.policy 옵션을 사용하여 데이터를 삭제할 수 있는데 2가지 정책을 제공합니다.
+ delete : 데이터 완전 삭제
    - 대부분의 토픽이 delete 옵션을 사용하게 됩니다.
    - 세그먼트 단위로 삭제를 진행합니다.
    - 세그먼트는 토픽의 데이터를 저장하는 명시적인 파일 시스템 단위로 세그먼트는 파티션마다 별개로 생성되며 세그먼트의 파일 이름은 오프셋 중 가장 작은 값이 됩니다. 즉, 하나의 파티션에는 여러 개의 세그먼트가 존재하고 세그먼트 안에 레코드들이 저장되며 레코드들 중 가장 작은 오프셋이 세그먼트 파일 이름이 됩니다.
    - segment.bytes 옵션으로 1개의 세그먼트의 크기를 설정할 수 있습니다. 해당 크기보다 커질 경우에는 기존에 적재하던 세그먼트 파일을 닫고 새로운 새그먼트를 열어서 데이터를 저장합니다. 현재 사용 중인 세그먼트를 액티브 세그먼트라고 합니다.
    - 삭제 정책이 실행되는 시점은 시간 또는 용량을 기준으로 진행됩니다.
    - retention.ms로 토픽의 데이터를 유지하는 기간을 밀리초로 설정하고 카프카는 일정 주기마다 세그먼트 파일의 마지막 수정 시간과 retention.ms를 비교하여 세그먼트를 제거합니다.
    - retention.bytes는 토픽의 최대 데이터 크기를 제어하고 넘어간 세그먼트 파일을 삭제합니다.        
+ compact : 동일 메시지 키의 가장 오래된 데이터를 삭제
    - 메시지 키를 기준으로 데이터를 삭제하기 때문에 오프셋의 증가가 일정하지 않을 수도 있습니다.
        - 1부터 10까지 오프셋이 있고 4,5,6이 동일키라면 4,5가 삭제되어 오프셋이 중간에 연속적이지 않을 수 있습니다.
    - KTable 같이 메시지 키를 기반으로 데이터를 처리할 경우 유용합니다. 가장 마지막으로 업데이트 된 메시지 키의 데이터가 중요할 경우 가장 최신 데이터를 제외한 나머지 데이터들을 삭제할 수 있기 때문입니다.
    - 액티브 세그먼트를 제외한 나머지 세그먼트들에 한해서만 데이터를 처리합니다.
    - 압축 시점은 min.cleanable.dirty.ratio 옵션 값을 따르는데 해당 값은 액티브 세그먼트를 제외한 세그먼트에 남아 있는 데이터의 테일 영역의 레코드 개수와 헤드 영역의 레코드 개수의 비율을 뜻합니다. 
        - 테일 영역은 브로커의 압축 정책에 의해 압축이 완료된 레코드를 의미하고 테일 영역의 레코드들은 클린 로그라고 부릅니다. 압축이 완료됐기 때문에 테일 영역에는 중복된 메시지 키가 없습니다.
        - 헤드 영역의 레코드들을 더티로그라고 부르고 압축이 되기 전 레코드들이 있으므로 중복된 메시지 키를 가진 레코드들이 있습니다. 더티 비율은 (더티 레코드 개수) / (클린 레코드 개수 + 더티 레코드 개수)입니다.
    - 예를 들어, 압축 완료된 클린 영역 레코드가 3개 있고 아직 안된 더티 영역에 레코드가 3개 있다면 더티 비율은 0.5가 됩니다. min.cleanbale.dirty.ratio 옵션 값을 0.5로 설정하면 앞서 계산한 비율이 0.5가 넘어가면 압축이 진행됩니다.

### ISR(In-Sync-Replicas)
ISR은 리더 파티션과 팔로워 파티션이 모두 싱크된 상태를 뜻합니다.  
예를 들어 복제 개수가 2인 토픽에서 리더 파티션에 오프셋이 [0,3]까지 있고 팔로워 파티션에 [0,2]까지 있으면 동기화가 완벽하게 된 상태가 아니고 팔로워 파티션도 [0,3]까지 있어야 ISR입니다.  
ISR 용어가 나온 이유는 팔로워 파티션이 리더 파티션으로부터 데이터를 복제하는 데에 시간이 걸리기 때문입니다.  
리더 파티션에 데이터가 적재된 이후 팔로워 파티션이 복제하는 시간차 때문에 리더 파티션과 팔로워 파티션 간에 오프셋 차이가 발생하게 되고 이런 차이를 모니터링하기 위해 리더 파티션은 replica.lag.time.max.ms값만큼의 주기를 가지고 팔로워 파티션이 데이터를 복제하는지 확인합니다.  
해당 시간보다 더 긴 시간 동안 데이터를 가져가지 않는다면 해당 팔로워 파티션에 문제가 생긴 것으로 판단하고 ISR 그룹에서 제외합니다.  
ISR로 묶인 파티션은 모두 동일한 데이터가 존재하기 때문에 팔로워 파티션은 리더로 선출될 자격을 가지게 됩니다.  
ISR 이외의 파티션도 리더로 선출 자격을 부여하고 싶다면 unclean.leader.election.enable 값을 true로 주면 됩니다.  
다만 false로 설정하면 장애 발생 시 ISR에 남아있는 파티션이 없다면 브로커가 다시 실행될 때까지 서비스가 중단됩니다.
```sh
bin/kafka-topics.sh --bootstrap-server my-kafka:9092 --create --topic my-topic --config unclean.leader.election.enable=false
```

## 카프카 프로듀서
프로듀서는 데이터를 저장하는 첫 단계입니다.  
카프카 클러스터는 3대 이상의 브로커로 이뤄져 있어야 일부 브로커에 이슈가 생기더라도 데이터의 유실을 막을 수 있습니다.  

### acks 옵션
카프카 프로듀서의 acks 옵션은 0, 1, all(또는 -1) 값을 가질 수 있습니다.  
이 옵션을 통해 프로듀서가 전송한 데이터가 카프카 클러스터에 얼마나 신뢰성 높게 저장할지 지정할 수 있습니다.  

#### acks=0
프로듀서가 리더 파티션으로 데이터를 전송했을 때 리더 파티션으로 데이터가 저장되었는지 확인하지 않는다는 뜻입니다.  
리더 파티션은 데이터가 저장된 이후에 데이터가 몇 번째 오프셋에 저장됐는지 리턴하는데, 이에 대해 응답을 받지 않는다는 의미입니다.  
이때는 프로듀서가 전송 하자마자 데이터가 저장되었음을 가정하고 다음으로 넘어가기 때문에 데이터 전송 실패여부를 알 수 없기에 retries 옵션값도 무의미합니다.  
덕분에 전송 속도는 가장 빠르고 데이터가 일부 유실되더라도 전송속도가 중요한 경우 사용합니다.  

#### acks=1
프로듀서가 보낸 데이터가 리더 파티션에만 정상적으로 적재되었는지 확인합니다. 적재되지 않았다면 재시도할 수 있습니다.  
리더 파티션에 적재됐음을 보장하더라도 데이터는 유실될 수 있습니다.  
복제 개수가 2이상이라면 팔로워 파티션에 데이터가 복제되기 직전에 리더 파티션에 문제가 발생하면 동기화되지 못한 일부 데이터가 유실되기 때문입니다.  

#### acks=all 또는 acks=-1
프로듀서가 보낸 데이터가 리더 파티션과 팔로워 파티션에 모두 정상적으로 적재되었는지 확인합니다.  
따라서 일부 브로커에 장애가 발생하더라도 안전하게 전송, 저장을 보장합지만 앞선 옵션에 비해 느립니다.  
all로 설정한 경우에는 토픽 단위로 설정 가능한 min.insync.replicas 옵션값에 따라 데이터의 안전성이 달라집니다.  
all은 모든 리더 파티션과 팔로워 파티션의 적재를 뜻하는 것은 아니고 ISR에 포함된 파티션들을 뜻하는 것이기 때문입니다.  
min.insync.replicas 옵션은 프로듀서가 리더 파티션과 팔로워 파티션에 적재되었는지 확인하기 위한 최소 ISR그룹의 파티션 개수 입니다.  
예를 들어, min.insync.replicas가 1이라면 ISR 중 최소 1개 이상의 파티션에 데이터가 적재되었음을 확인하는 것입니다.  
이 경우 acks를 1로 했을 때와 동일한 동작을 하는데 ISR 중 가장 처음 적재가 완료되는 파티션은 리더 파티션이기 때문입니다.  
따라서 min.insync.replicas 를 2이상으로 설정했을 때부터 acks=all 설정이 의미가 있어집니다.  
2로 둔다면 적어도 리더 파티션과 1개의 팔로워 파티션에 데이터가 적재되었음을 보장합니다.  
min.insync.replicas를 설정할 때는 복제 개수도 함께 고려해야 합니다.  
예를 들어 복제 개수를 3으로 설정하고 min.insync.replicas를 3으로 설정하게 된다면, 브로커 1대가 이슈가 발생하게 되면 브로커가 2개만 남는데 min.insync.replicas는 최소한 3개 복제를 보장해야하기 때문에 동작하지 못하게 됩니다.  
__즉, min.insync.replicas옵션은 반드시 브로커 개수 미만으로 설정해서 운영해야 합니다.__  
상용 환경에서는 브로커를 3대 이상으로 묶어서 클러스터를 구성하게 되는데 가장 안정적인 방식은 토픽 복제 개수는 3, min.insync.replicas는 2로 설정하고 프로듀서는 acks=all로 설정하는 것을 권장합니다.  


### 멱등성 프로듀서
멱등성이란 여러 번 연산을 수행하더라도 동일한 결과를 나타내는 것을 의미합니다.  
멱등성 프로듀서는 동일한 데이터를 여러 번 전송하더라도 카프카 클러스터에 단 한 번만 저장되는 것을 의미합니다.  
기본 프로듀서의 동작 방식은 적어도 한번 전달을 지원하기에 여러 번 전달되면 데이터의 중복이 발생합니다.  
enable.idempotence 옵션을 true로 사용하면 정확히 한번 전달을 지원하게 되고 기본값은 false입니다.  
멱등성 프로듀서는 기본 프로듀서와 달리 데이터를 브로커로 전달할 때 프로듀서 PID와 시퀀스 넘버를 함께 전달하고 브로커는 프로듀서의 PID와 시퀀스 넘버를 통해 동일한 데이터는 단 한번만 적재하게 됩니다.  
단, 프로듀서에서 이슈가 발생하고 종료되어 다시 시작하게 되면 PID가 달라지기 때문에 이때는 정확히 한번 적재를 보장하지 못합니다.  
해당 옵션을 true로 주면 retries 기본값으로 Integer.MAX_VALUE로 설정되고 acks 옵션은 all로 설정됩니다.  
이는 적어도 한 번 이상 브로커에 데이터를 보냄으로써 브로커에 단 한 번만 데이터가 적재되는 것을 보장하기 위함인데 멱등성 프로듀서는 정확히 한번 브로커에 데이터를 적재하기 위해 정말 한번 전송하는 것이 아니고 여러 번 전송하되 브로커가 이를 확인하고 중복된 데이터는 적재하지 않는 방식이기 때문이라고 보면 됩니다.  

### 트랜잭션 프로듀서
트랜잭션 프로듀서는 다수의 파티션에 데이터를 저장할 경우 모든 데이터에 대해 동일한 원자성(atomic)을 만족시키기 위해 사용됩니다.  
원자성을 만족시킨다는 의미는 다수의 데이터를 동일 트랜잭션으로 묶음으로써 전체 데이터를 처리하거나 전체 데이터를 처리하지 않도록 하는 것을 의미합니다.
컨슈머는 기본적으로 파티션에 쌓이는 대로 모두 가져가서 처리합니다.  
하지만 트랜잭션으로 묶인 데이터를 브로커에서 가져갈 때는 다르게 동작하도록 설정할 수 있습니다.  
트랜잭션 프로듀서를 사용하려면 enable.idempotence를 true로 설정하고 transactional.id를 임의의 String 값으로 정의합니다.  
그리고 컨슈머의 isolation.level을 read_committed로 설정하면 프로듀서와 컨슈머는 트랜잭션으로 처리 완료된 데이터만 쓰고 읽게 됩니다.  

트랜잭션 프로듀서는 사용자가 보낸 데이터를 레코드로 파티션에 저장할 뿐만 아니라 트랜잭션의 사작과 끝을 표현하기 위해 트랜잭션 레코드를 한 개 더 보냅니다.  
트랜잭션 컨슈머는 파티션에 저장된 트랜잭션 레코드를 보고 트랜잭션이 완료(commit)되었음을 확인하고 데이터를 가져갑니다.  
만약 데이터만 존재하고 트랜잭션 레코드가 존재하지 않으면 아직 트랜잭션이 완료되지 않았다고 판단하고 컨슈머는 데이터를 가져가지 않습니다.  
트랜잭션 레코드는 실질적인 데이터는 가지고 있지 않고 트랜잭션이 끝난 상태를 표기하는 정보만 가지고 있습니다.  
대신 레코드의 특성은 그대로 가지고 있어 오프셋 하나를 차지합니다.  

## 카프카 컨슈머
컨슈머는 카프카에 적재된 데이터를 처리합니다.  

### 멀티 스레드 컨슈머
토픽의 파티션은 1개 이상으로 이뤄져 있으며 1개의 파티션은 1개의 컨슈머가 할당되어 데이터를 처리하게 됩니다.  
파티션을 여러 개로 운영하는 경우 데이터 병렬처리를 위해 파티션 개수와 컨슈머 개수를 동일하게 맞추는 것이 가장 좋습니다.  
그러므로 n개의 스레드를 가진 1개의 프로세스를 운영하거나 1개의 스레드를 가진 프로세스를 n개 운영하는 방법이 있습니다.  
멀티코어 CPI를 가진 가상/물리 서버 환경에서 멀티 컨슈머 스레드를 운영하여 제한된 리소스 내에서 최상의 성능을 발휘할 수 있기 때문에 컨슈머를 멀티 스레드로 활용하는 방식은 크게 두 가지로 나뉩니다.  
1. 컨슈머 스레드는 1개만 실행하고 데이터 처리를 담당하는 워크 스레드를 여러 개 실행하는 방법인 멀티 워커 스레드 전략
2. 컨슈머 인스턴스에서 poll 메서드를 호출하는 스레드를 여러 개 띄워서 사용하는 컨슈머 멀티 스레드 전략

#### 멀티 워커 스레드 전략
멀티 스레드를 생성하는 ExecutorService 자바 라이브러리를 사용하면 레코드를 병렬처리하는 스레드를 효율적으로 생성하고 관리할 수 있습니다.  
Executors를 사용하여 스레드 개수를 제어하는 스레드 풀을 생성할 수 있는데, 데이터 처리 환경에 맞는 스레드 풀을 사용하면 됩니다.  
작업 이후 스레드를 종료시켜야만 한다면 CachedThreadPool을 사용하려 스레드를 실행하면 됩니다.  

```java
// runnable 인터페이스를 구현한 구현체를 Thread에 전달하면 스레드객체가 생성되고 start를 호출하면 정의한 동작이 수행된다.
public class ConsumerWorker implements Runnable {

    private String recordValue;

    ConsumerWorker(String recordValue) {
        this.recordValue = recordValue;
    }

    @Override
    public void run() {
        // 데이터 처리 로직
    }
}
```
```java
@Slf4j
public class ConsumerWithMultiWorkerThread {
    private final static String TOPIC_NAME = "test";
    private final static String BOOTSTRAP_SERVERS = "my-kafka:9092";
    private final static String GROUP_ID = "test-group";

    public static void main(String[] args) {
        Properties configs = new Properties();
        configs.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        configs.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
        configs.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        configs.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        configs.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        configs.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, 10000);


        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(configs);
        consumer.subscribe(Arrays.asList(TOPIC_NAME));

        // 스레드 실행을 위해 ExecutorService 사용 
        // ExecutorService는 다양한 스레드 풀을 제공하는데 newCachedThreadPool는 일을 다하면 종료되는 스레드 
        ExecutorService executorService = Executors.newCachedThreadPool();
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(10));
            for (ConsumerRecord<String, String> record : records) {
                // 작업이 명시된 클래스 생성
                ConsumerWorker worker = new ConsumerWorker(record.value());
                // excutorService에 전달하여 동작하도록 한다.
                executorService.execute(worker);
            }
        }
    }
}
```
원래는 이전 레코드의 처리가 끝날 때까지 다음 레코드는 기다리는데 워커 스레드로 병렬처리하기 때문에 각기 다른 레코드들의 데이터 처리를 동시에 실행하게 되어 처리 시간을 줄일 수 있습니다.  
하지만 주의사항이 몇 가지 있습니다.  
1. 스레드를 사용함으로써 데이터 처리가 끝나지 않았음에도 불구하고 커밋을 하기 때문에 리밸런싱, 컨슈머 장애 시 데이터 유실 발생
2. 스레드 처리 시간이 각각 다르기 때문에 레코드 순서가 뒤바뀌는 현상이 발생

따라서 레코드 처리에 있어 중복이 발생하거나 데이터 역전현상이 발생해도 되며 매우 빠른 처리 속도가 필요한 데이터 처리에 적합합니다.  
서버 리소스 모니터링 파이프라인, IoT 서비스 센터 데이터 수집 파이프라인이 그 예입니다.  


#### 컨슈머 멀티 스레드 전략
하나의 파티션은 동일 컨슈머 중 최대 1개까지 할당 가능하고 하나의 컨슈머는 여러 파티션에 할당될 수 있습니다.  
이런 특징을 가장 잘 살리는 방법이 1개의 애플리케이션에 구독하고자 하는 토픽의 파티션 개수만큼 컨슈머 스레드 개수를 늘려서 운영하는 것입니다.  
컨슈머 스레드를 늘려서 운영하면 각 스레드에 각 파티션이 할당되며, 파티션의 레코드들을 병렬처리 할 수 있습니다.  
앞선 방식은 여러 개의 파티션이 애플리케이션에 하나의 컨슈머에 매핑되어 데이터를 받아서 데이터 처리를 여러 개의 워커 스레드가 처리했다면 이제는 여러 개의 파티션이 하나의 애플리케이션에 여러 개의 컨슈머와 매핑되는 것입니다.  
여기서 주의해야할 점은 구독하고자 하는 토픽의 파티션 개수만큼만 컨슈머 스레드를 운영해야 하는 것입니다. 컨슈머 스레드가 파티션 개수보다 많아지면 할당할 파티션 개수가 더는 없으므로 파티션에 할당되지 못한 컨슈머 스레드는 데이터를 처리하지 않게 됩니다.
<br>

하나의 애플리케이션에 n개의 컨슈머 스레드를 띄워 봅시다. 

```java
@Slf4j
public class ConsumerWorker implements Runnable {
    private Properties prop;
    private String topic;
    private String threadName;
    private KafkaConsumer<String, String> consumer; // 1

    ConsumerWorker(Properties prop, String topic, int number) { // 2
        this.prop = prop;
        this.topic = topic;
        this.threadName = "consumer-thread-" + number;
    }

    @Override
    public void run() {
        consumer = new KafkaConsumer<>(prop); // 1
        consumer.subscribe(Arrays.asList(topic)); // 3
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(1));
            for (ConsumerRecord<String, String> record : records) {
                log.info("{}", record); // 4
            }
            consumer.commitSync();
        }
    }
}
```
1. KafkaConsumer는 스레드 세이프하지 않기 때문에 스레드별로 별개로 만들어서 운용해야만 하기 때문에 각 스레드에서 생성해줍니다.
2. KafkaConsumer 인스턴스를 생성하기 위해 필요한 변수를 생성자 인자로 받도록 합니다.
3. 토픽을 구독합니다.
4. poll로 받은 레코드를 처리합니다.

<Br>

```java
public class MultiConsumerThread {

    private final static String TOPIC_NAME = "test";
    private final static String BOOTSTRAP_SERVERS = "my-kafka:9092";
    private final static String GROUP_ID = "test-group";
    private final static int CONSUMER_COUNT = 3; // 1

    public static void main(String[] args) {
        Properties configs = new Properties();
        configs.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        configs.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
        configs.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        configs.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        ExecutorService executorService = Executors.newCachedThreadPool(); // 2
        for (int i = 0; i < CONSUMER_COUNT; i++) {
            ConsumerWorker worker = new ConsumerWorker(configs, TOPIC_NAME, i); // 3
            executorService.execute(worker); // 3
        }
    }
}
```
1. 생성할 컨슈머 스레드 개수를 정해줍니다.
2. 내부 작업이 완료되는 스레드를 종료하는 ExecutoerService를 만들어 줍니다.
3. 지정해둔 스레드 개수만큼 스레드를 생성하고 실행시킵니다.

## 컨슈머 랙
컨슈머 랙은 토픽의 최신 오프셋(LOG-END-OFFSET)과 컨슈머 오프셋(CURRENT-OFFSET) 간의 차이입니다.  
컨슈머 랙은 컨슈머가 정상 동작하는지 여부를 확인할 수 있기 때문에 필수적으로 모니터링해야하는 지표 입니다.  
컨슈머 랙은 컨슈머 그룹과 토픽, 파티션별로 생성됩니다.  
1개의 토픽에 3개의 파티션이 있고 1개의 컨슈머 그룹이 토픽을 구독하여 데이터를 가져가면 컨슈머 랙은 총 3개가 됩니다.  
프로듀서가 보내는 데이터량이 컨슈머 데이터 처리량보다 크면 컨슈머 랙은 늘어나고 그 반대는 줄어드는데 최소값은 0으로 지연이 없음을 의미합니다.  
즉, 컨슈머 랙이 늘어나면 이슈가 발생했음을 유추할 수 있습니다.  
컨슈머 랙을 확인하는 방법은 총 3가지가 있습니다.  
1. 카프카 명령어 사용
2. 컨슈머 애플리에키션에서 metrics 메서드 사용
3. 외부 모니터링 툴

가장 최선의 방법은 외부 모니터링 툴입니다.  
데이터독(Datadog), 컨플루언트 컨트롤 센터와 같은 카프타 클러스터 종합 모니터링 툴을 사용하면 카프카 운영에 필요한 다양한 지표를 모니터링 할 수 있습니다.  

### 카프카 버로우
컨슈머 랙 모니터링만을 위한 툴로 오픈소스로 공개되어 있는 버로우(burrow)가 있고 이는 REST API를 통해 컨슈머 그룹별로 컨슈머 랙을 확인할 수 있습니다.  

요청 메서드|호출 경로|설명
---|---|---
GET|/burrow/admin|버로우 헬스 체크
GET|/v3/kafka|버로우와 연동 중인 카프카 클러스터 리스트
GET|/v3/kafka/{클러스터이름}|클러스터 정보 조회
GET|/v3/kafka/{클러스터이름}/consumer|클러스터에 존재하는 컨슈머 그룹 리스트
GET|/v3/kafka/{클러스터이름}/topic|클러스터에 존재하는 토픽 리스트
GET|/v3/kafka/{클러스터이름}/consumer/{컨슈머 그룹 이름}|컨슈머 그룹의 컨슈머 랙, 오프셋 정보 조회
GET|/v3/kafka/{클러스터이름}/consumer/{컨슈머 그룹 이름}/status|컨슈머 그룹의 파티션 정보, 상태 조회
GET|/v3/kafka/{클러스터이름}/consumer/{컨슈머 그룹 이름}/lag|컨슈머 그룹의 파티션 정보, 상태, 컨슈머 랙 조회
DELETE|/v3/kafka/{클러스터이름}/consumer/{컨슈머 그룹 이름}|버로우에서 모니터링 중인 컨슈머 그룹 삭제
GET|/v3/kafka/{클러스터이름}/topic|{토픽이름}|토픽 상세 조회

한 번의 설정으로 다수의 카프카 클러스터 컨슈머 랙을 확인할 수 있지만 버로우는 데이터 적재 기능이 없기 때문에 모니터링을 위해 컨슈머 랙 지표를 수집, 적재, 알람 설정을 하고 싶다면 별도의 저장소와 대시보드를 구축해야 합니다.  
버로우는 랙의 상태를 표현하는 것을 컨슈머 랙 평가라고 합니다.  
컨슈머 랙과 파티션의 오프셋을 슬라이딩 윈도우로 계산하여 상태가 정해지는데 파티션은 OK, STALLED, STOPPED로 포현되고 컨슈머는 OK, WARNING, ERROR로 표현합니다.  
+ 최신 오프셋과 컨슈머 오프셋이 계속 증가하지만 컨슈머 데이터 처리가 못따라 가면 (컨슈머 랙이 계속 늘어나면) 파티션은 OK, 컨슈머는 WARNING
+ 최신 오프셋은 계속 증가하고 컨슈머 오프셋이 멈춘 경우 파티션은 STALLED, 컨슈머는 ERROR

### 컨슈머 랙 모니터링 아키텍처
앞서 언급했듯이 버로우를 통해 컨슈머 랙을 모니터링 할 때는 이미 지나간 컨슈머 랙을 개별적으로 모니터링하기 위해 별개의 저장소와 대시보드를 사용하는 것이 효과적입니다.  
모니터링을 위해 사용할 수 있는 저장소와 대시보드는 다양하지만 빠르고 무료로 설치할 수 있는 방식은 다음과 같습니다.

> 카프카 클러스터 0, 1 -> 카프카 버로우 -> 텔레그래프 -> 엘라스틱 서치 -> 그라파나 <- 사용자 조회

+ 버로우 : REST API로 컨슈머 랙 조회
+ 텔레그래프 : 데이터 수집 및 전달에 특화된 툴
+ 엘라스틱서치 : 컨슈머 랙 정보를 담는 저장소
+ 그라파나 : 엘라스틱서치의 정보를 시각화하고 특정 조건에 따라 슬랙 알람을 보낼 수 있는 웹 대시보드 툴


## 컨슈머 배포 프로세스
애플리케이션 내부 로직의 변경과 배포는 필수적이고 운영 시 로직 변경으로 인한 배포를 하는 방법은 크게 두 가지가 있습니다.
1. 중단 배포
2. 무중단 배포
                                                          
배포는 책 그림이 잘 되어있다.                                                       

### 중단 배포
중단 배포는 컨슈머 애플리케이션을 완전히 종료한 이후에 개선된 코드를 가진 애플리케이션을 배포하는 방식입니다.  
기존 컨슈머 애플리케이션이 종료되면 더는 토픽의 데이터를 가져갈 수 없기 때문에 컨슈머 랙이 늘어나고 이는 지연이 발생함을 의미합니다.  

### 무중단 배포
컨슈머의 중단이 불가능한 애플리케이션의 신규 로직 배포가 필요한 경우에는 무중단 배포가 필요합니다.  
무중단 배포는 3가지 방법으로 블루/그린, 롤링, 카나리 배포가 있습니다.  
+ 무중단 배포
    - 이전 버전 애플리케이션과 신규 버전 애플리케이션을 동시에 띄워놓고 트래픽을 전환하는 방식
    - 파티션 개수와 컨슈머 개수를 동일하게 실행하는 애플리케이션에서 유용합니다.
    - 신규 버전 애플리케이션을 배포하고 동일 컨슈머 그룹으로 파티션을 구독하도록 실행하면 신규 버전 애플리케이션의 컨슈머들은 파티션을 할당 받지 못하고 유휴 상태로 기다릴 수 있기 때문입니다. 파티션 개수와 컨슈머 개수를 동일하게 운영하고 있지 않다면 일부 파티션은 기존 애플리케이션에 할당되고 일부 파티션은 신규 애플리케이션에 할당되어 섞이게 됩니다.
+ 롤링 배포
    - 일반적인 배포로 실행 중인 인스턴스 중 일부를 새로운 애플리케이션으로 띄우는 방식
    - 파티션 개수가 인스턴스 개수와 같거나 그보다 많아야 합니다.
+ 카나리 배포
    - 1개만 우선적으로 신규 애플리케이션에 처리해보고 완료되면 나머지는 롤링, 블루/그린 방식 중 선택해서 배포하는 방식

## 스프링 카프카
스프링 카프카는 카프카를 스프링 프레임워크에서 효과적으로 사용할 수 있도록 만들어진 라이브러리러 기존 카프카 클라이언트 라이브러리를 래핑해서 만들어졌습니다.  
```groovy
implementation 'org.springframework.boot:spring-boot-starter'
implementation 'org.springframework.kafka:spring-kafka'
```
의존성을 추가해서 사용합니다.  

### 스프링 카프카 프로듀서
스프링 카프카 프로듀서는 카프카 템플릿(Kafka Template) 클래스를 사용하고 이는 프로듀서 팩토리(ProducerFactory) 클래스를 통해 생성합니다.  
사용하는 방법은 두 가지가 있습니다.  
1. 스프링 카프카에서 제공하는 기본 카프카 템플릿 사용
2. 직접 사용자가 카프카 템플릿을 프로듀서 팩토리로 생성해서 사용

#### 기본 카프카 템플릿
기본 카프카 템플릿은 기본 프로듀서 팩토리를 통해 생성된 카프카 템플릿을 사용합니다.  
기본 카프카 템플릿을 사용할 때는 application.yml에 옵션을 넣고 사용할 수 있습니다.  
```yml
spring:
  kafka:
    producer:
      bootstrap-servers: my-kafka:9092
      acks: all
```
<br>

test0부터 9까지 메시지 값을 클러스터로 보내는 프로듀서 애플리케이션을 만들어봅시다.  
```java
@SpringBootApplication
public class SpringProducerApplication implements CommandLineRunner {

    private static String TOPIC_NAME = "test";

    @Autowired
    private KafkaTemplate<Integer, String> template; // 1

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(SpringProducerApplication.class);
        application.run(args);
    }

    @Override
    public void run(String... args) {
        for (int i = 0; i < 10; i++) {
            template.send(TOPIC_NAME, "test" + i); // 2
        }
        System.exit(0); //3 
    }
}
```
1. application.yml에 설정한 옵션값이 세팅되어 생성된 KafkaTemplate을 주입받아서 사용합니다. 
2. send 메서드에 토픽 이름과 메시지 값을 넣어 전송합니다.
3. 데이터 전송이 완료되면 종료합니다. 

<br>

send 메서드는 다양한 오버로딩을 제공합니다.
+ send(String topic, K key, V data) : 메시지 키와 메시지 값을 포함하여 특정 토픽으로 전달
+ send(String topic, Integer partition, K key, V data) : 메시지 키, 메시지 값이 포함된 레코드를 특정 토픽의 특정 파티션으로 전달
+ send(String topic, Integer partition, Long timestamp, K key, V data) : 메시지 키, 메시지 값, 타임스탬프가 포함된 레코드를 특정 토픽의 특정 파티션에 전달
+ send(ProducerRecord\<K,V> record) : 프로듀서 레코드 객체를 전송

#### 커스텀 카프카 템플릿
커스텀 카프카 템플릿은 프로듀서 팩토리를 통해 만든 카프카 템플릿 객체를 빈으로 등록하여 사용하는 것입니다.  
```java
@Configuration
public class KafkaTemplateConfiguration {

    // kafkaTemplate 객체를 리턴하는 빈 객체 만들기
    // 메서드 이름으로 빈이 생성된다.
    @Bean 
    public KafkaTemplate<String, String> customKafkaTemplate() {
        
        // 옵션 명시
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "my-kafka:9092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.ACKS_CONFIG, "all");

        // 옵션을 인자로 주어 프로듀서 팩토리 초기화
        ProducerFactory<String, String> pf = new DefaultKafkaProducerFactory<>(props);

        // 빈 객체로 사용할 kafkaTemplate 인스턴스를 초기화하고 리턴
        return new KafkaTemplate<>(pf);
    }
}
```
스프링 카프카에서는 kafkaTemplate 외에도 ReplyingKafkaTemplat과 RoutinigKafkaTemplate도 제공합니다.  
+ ReplyKafkaTemplate : 컨슈머가 특정 데이터를 전달받았는지 여부를 확인
+ RoutingKafkaTemplate : 전송하는 토픽별로 옵션을 다르게 설정 가능

<Br>

```java
@SpringBootApplication
public class SpringProducerApplication implements CommandLineRunner {

    private static String TOPIC_NAME = "test";

    @Autowired
    private KafkaTemplate<String, String> customKafkaTemplate;

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(SpringProducerApplication.class);
        application.run(args);
    }

    @Override
    public void run(String... args) {
        ListenableFuture<SendResult<String, String>> future = customKafkaTemplate.send(TOPIC_NAME, "test"); // 1
        future.addCallback(new KafkaSendCallback<String, String>() { // 2
            @Override
            public void onSuccess(SendResult<String, String> result) {

            }

            @Override
            public void onFailure(KafkaProducerException ex) {

            }
        });
        System.exit(0);
    }
}
```
1. 커스텀 카프카 템플릿을 주입받은 이후 kafkaTemplate 인스턴스를 사용하는 것은 기본 카프카 템플릿과 동일합니다. 만약 전송한 이후 정상 적재됐는지 여부를 확인하고 싶다면 ListenableFuture 메서드를 사용하면 됩니다.
2. ListenableFuture 인스턴스에 addCallback 함수를 붙여 프로듀서가 보낸 데이터의 브로커 적재 여부를 비동기로 확인할 수 있습니다. 만약 브로커에 정상 적재되었다면 onSuccess 메서드가 호출됩니다. 적재되지 않고 이슈가 발생했다면 onFailure 메서드가 호출됩니다.

### 스프링 카프카 컨슈머
스프링 카프카의 컨슈머는 기존 컨슈머를 크게 2개의 타입으로 나누고 커밋을 7가지로 나누어 세분화했습니다.  
리스너 타입에 따라 한번 호출하는 메서드에서 처리하는 레코드의 개수가 달라집니다.

+ 레코드 리스너(MessageListener) : 단 1개의 레코드 처리
    - Record 인스턴스 단위 프로세싱, 오토 커밋 또는 컨슈머 컨테이너의 AckMode를 사용하는 경우
    - 디폴트 값
+ 배치 리스너(BatchMessageListener) : 기존 카프카 클라이언트의 poll 메서드로 리턴받은 ConsumerRecords 처럼 한 번에 여러 개의 레코드를 처리
    - Records 인스턴스 단위로 프로세싱, 오토 커밋 또는 컨슈머 컨테이너의 AckMode를 사용하는 경우

스프링 카프카 컨슈머의 기본 리스너 타입은 레코드 리스너이고 아래와 같이 파생된 여러 형태가 있습니다.  
+ Record 타입
    + MessageListener : Record 인스턴스 단위로 프로세셍, 오토커밋 또는 컨슈머 컨테이너의 ackMode를 사용하는 경우
    + AcknowledgingMessageListener : Record 인스턴스 단위로 프로세싱, 메뉴얼 커밋을 사용하는 경우
    + ConsumerAwareMessageListener : Record 인스턴스 단위로 프로세싱, 컨슈머 객체를 활용하고 싶은 경우
    + AcknowledgingConsumerAwareMessageListener : Record 인스턴스 단위로 프로세싱, 매뉴ㅜ얼 커밋을 사용하고 컨슈머 객체를 활용하고 싶은 경우
+ batch 타입
    + BatchMessageListener : Records 인스턴스 단위로 프로세싱, 오토 커밋 또는 컨슈머 컨테이너의 AckMode를 사용하는 경우
    + BatchAcknowledgingMessageListener : Records 인스턴스 단위로 프로세싱, 매뉴얼 커밋을 사용하는 경우
    + BatchConsumerAwareMessageListener : Records 인스턴스 단위로 프로세싱, 컨슈머 객체를 활용하고 싶은 경우
    + BatchAcknowledgingConsumerAwareMessageListener : Records 인스턴스 단위로 프로세싱, 매뉴얼 커밋을 사용하고 컨슈머 객체를 활용하고 싶은 경우


메뉴얼 커밋을 사용할 경우에는 Acknowledging이 붙은 리스너를 사용하고, Kafka Cosumer 인스턴스에 직접 접근하여 컨트롤하고 싶다면 ConsumerAware가 붙은 리스너를 사용하면 됩니다.  
메뉴얼 커밋이란 자동 이 아닌 개발자가 명시적으로 커밋하는 방식을 의미합니다.     
<br>

스프링 카프카에서는 커밋이라고 부르지 않고 AckMode라고 부릅니다.  
기본값은 BATCH이고 컨슈머의 enable.auto.commit 옵션은 false로 지정됩니다.  

AcksMode|설명
---|---
RECORD|레코드 단위로 프로세싱 이후 커밋
BATCH|poll 메서드로 호출된 레코드가 모두 처리된 이후 커밋<br>스프링 카프카의 기본값
TIME|특정시간 이후 커밋<br>이 옵션을 사용할 경우에는 시간 간격을 선언하는 AckTime 옵션을 설정해야 합니다.
COUNT|특정 개수만큼 레코드가 처리된 이후에 커밋<br>이 옵션을 사용할 경우에는 레코드 개수를 선언하는 AckCount 옵션을 설정해야 합니다.
COUNT_TIME|TIME, COUNT 옵션 중 맞는 조건이 하나라도 나올 경우 커밋
MANUAL|Acknowledgement.acknowledge() 메서드가 호출되면 다음번 poll 때 커밋 합니다.<br>매번 acknowledge 메서드를 호출하면 BATCH 옵션과 동일하게 동작합니다.<br>이 옵션을 사용할 경우에는 AcknowledgingMessageListener 또는 BatchAcknowledgingMessageListener를 리스너로 사용해야 합니다.
MANUAL_IMMEDIATE|Acknowledgement.acknowledge 메서드를 호출한 즉시 커밋합니다.<br>이 옵션을 사용할 경우에는 AcknowledgingMessageListener 또는 BatchAcknowledgingMessageListener를 리스너로 사용해야 합니다.

<br>

리스너를 생성하고 사용하는 방식은 컨슈머와 마찬가지로 2가지가 있습니다.  
1. 기본 리스너 컨테이너 사용
2. 컨테이너 팩토리를 사용하여 직접 리스너 생성하고 사용

#### 기본 리스너 컨테이너
__Record 리스너__  
```yml
spring:
  kafka:
    consumer:
      bootstrap-servers: my-kafka:9092
    listener:
      type: single
```
버전업 되면서 Record 타입이 Single로 변경되었습니다.  
리스너를 사용하기 위해서는 KafkaListener 애노테이션을 포함한 메서드를 선언해야 합니다.  
```java
@SpringBootApplication
public class SpringConsumerApplication {
    public static Logger logger = LoggerFactory.getLogger(SpringConsumerApplication.class);


    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(SpringConsumerApplication.class);
        application.run(args);
    }

    @KafkaListener(topics = "test",
            groupId = "test-group-00")
    public void recordListener(ConsumerRecord<String,String> record) { // 1
        logger.info(record.toString());
    }

    @KafkaListener(topics = "test",
            groupId = "test-group-01")
    public void singleTopicListener(String messageValue) { // 2
        logger.info(messageValue);
    }

    @KafkaListener(topics = "test",
            groupId = "test-group-02", properties = {
            "max.poll.interval.ms:60000",
            "auto.offset.reset:earliest"
    })
    public void singleTopicWithPropertiesListener(String messageValue) { // 3
        logger.info(messageValue);
    }

    @KafkaListener(topics = "test",
            groupId = "test-group-03",
            concurrency = "3")
    public void concurrentTopicListener(String messageValue) { // 4
        logger.info(messageValue);
    }

    @KafkaListener(topicPartitions =
            {
                    @TopicPartition(topic = "test01", partitions = {"0", "1"}),
                    @TopicPartition(topic = "test02", partitionOffsets = @PartitionOffset(partition = "0", initialOffset = "3"))
            },
            groupId = "test-group-04")
    public void listenSpecificPartition(ConsumerRecord<String, String> record) { // 5
        logger.info(record.toString());
    }
}
```
1. 가장 기본적인 리스너 선언입니다. 옵션으로 topics와 groupId를 지정합니다. poll이 호출되어 가져온 레코드들은 차례대로 개별 레코드의 메시지 값을 파라미터로 받게 됩니다. 파라미터로 ConsumerRecord를 받기 때문에 메시지 키, 메시지 값에 대한 처리를 이 메서드 안에서 수행하면 됩니다.
2. 메시지 값을 파라미터로 받는 리스너입니다. 여기서는 스프링 카프카의 역직렬화 클래스 기본값인 StringDeserializer를 사용했으므로 String 클래스로 메시지 값을 전달받았습니다.
3. 개별 리스너에 카프카 컨슈머 옵션값을 부여하고 싶다면 properties 옵션을 사용합니다.
4. 2개 이상의 카프카 컨슈머 스레드를 실행하고 싶다면 concurrency 옵션을 사용하면 됩니다. concurrency 옵션값에 해당하는 만큼 컨슈머 스레드를 만들어 병렬처리합니다. 예를 들어, 파티션이 10개인 토픽을 구독할 때 효율을 높이기 위해 10으로 설정하면 됩니다.
5. 특정 토픽의 특정 파티션만 구독하고 싶다면 topicPartitions 파라미터를 사용합니다. 여기에 추가로 PartitionOffset 애노테이션을 활용하면 특정 파티션의 특정 오프셋까지 지정할 수 있습니다. 이 경우에는 그룹 아이디에 관계없이 항상 설정한 오프셋의 데이터부터 가져옵니다.

<br>

__배치 리스너__  
```yml
spring:
  kafka:
    consumer:
      bootstrap-servers: my-kafka:9092
    listener:
      type: batch
```
배치 리스너는 레코드 리스너와 달리 메서드 파라미터를 List 또는 ConsumerRecords로 받습니다.  
```java
@SpringBootApplication
public class SpringConsumerApplication {
    public static Logger logger = LoggerFactory.getLogger(SpringConsumerApplication.class);

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(SpringConsumerApplication.class);
        application.run(args);
    }

    @KafkaListener(topics = "test",
            groupId = "test-group-01")
    public void batchListener(ConsumerRecords<String, String> records) { // 1
        records.forEach(record -> logger.info(record.toString()));
    }

    @KafkaListener(topics = "test",
            groupId = "test-group-02")
    public void batchListener(List<String> list) { // 2
        list.forEach(recordValue -> logger.info(recordValue));
    }

    @KafkaListener(topics = "test",
            groupId = "test-group-03",
            concurrency = "3")
    public void concurrentBatchListener(ConsumerRecords<String, String> records) { // 3
        records.forEach(record -> logger.info(record.toString()));
    }
}
```
1. 컨슈머 레코드의 묶음을 파라미터로 받습니다. poll 메서드로 리턴받은 ConsumerRecords를 리턴받아 사용하는 것과 동일합니다.
2. 메시지 값들을 List로 받아서 사용합니다.
3. 2개 이상의 컨슈머 스레드로 배치 리스너를 운영할 경우에는 concurrency 옵션을 함께 사용하면 됩니다. 여기서는 3으로 선언했으므로 3개의 컨슈머 스레드가 생성됩니다.

<Br>

+ 배치 컨슈머 리스너(BatchConsumerAwareMessageListener)
    - 배치 컨슈머 리스너는 컨슈머를 직접 사용하기 위해 컨슈머 인스턴스를 파라미터로 받습니다.  
    - 컨슈머 인스턴스를 사용해 동기 커밋, 비동기 커밋, 인스턴스에서 제공하는 다양한 메서드를 사용할 수 있습니다.  
+ 배치 커밋 리스너(BatchAcknowledgingMessageListener)
    - 배치 커밋 리스너는 컨테이너에서 관리하는 AckMode를 사용하기 위해 Acknowledgement 인스턴스를 파라미터로 받습니다.  
    - Acknowledgement 인스턴스는 커밋을 수행하기 위한 한정적인 메서드만 제공합니다.  



```java
@SpringBootApplication
public class SpringConsumerApplication {
    public static Logger logger = LoggerFactory.getLogger(SpringConsumerApplication.class);


    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(SpringConsumerApplication.class);
        application.run(args);
    }

    @KafkaListener(topics = "test", groupId = "test-group-01") // 1
    public void commitListener(ConsumerRecords<String, String> records, Acknowledgment ack) {
        records.forEach(record -> logger.info(record.toString()));
        ack.acknowledge();
    }

    @KafkaListener(topics = "test", groupId = "test-group-02") // 2
    public void consumerCommitListener(ConsumerRecords<String, String> records, Consumer<String, String> consumer) {
        records.forEach(record -> logger.info(record.toString()));
        consumer.commitSync();
    }
}
```
1. AckMode를 MANUAL 또는 MANUAL_IMMEDIATE로 사용할 경우에는 수동 커밋을 하기 위해 파라미터로 Acknowledgment 인스턴스를 받아야 합니다. acknowledge 메서드를 호출하여 커밋할 수 있습니다.
2. 동기 커밋, 비동기 커밋을 사용하고 싶다면 컨슈머 인스턴스를 파라미터로 받아서 사용합니다. consumer 인스턴스의 commitSync, commitAsync 메서드를 호출하면 사용자가 원하는 타이밍에 커밋할 수 있도록 로직을 추가할 수 있습니다. 다만, 리스너가 커밋을 하지 않도록 AckMode는 MANUAL 또는 MANUAL_IMMEDIATE로 설정해야 합니다.


#### 커스텀 리스너 컨테이너
서로 다른 설정을 가진 2개 이상의 리스너를 구현하거나 리밸런스 리스너를 구현하기 위해서는 커스텀 리스너 컨테이너를 사용해야 합니다.  
커스텀 리스너 컨테이너를 만들기 위해서 스프링 카프카에서 카프카 리스너 컨테이너 팩토리(KafkaListenerContainerFactory) 인스턴스를 생성해야 합니다.  
카프카 리스너 컨테이너 팩토리를 빈으로 등록하고 KafkaListener 애노테이션에서 커스텀 리스너 컨테이너 팩토리를 등록하면 커스텀 리스너 컨테이너를 사용할 수 있습니다.


```java
@Configuration
public class ListenerContainerConfiguration {

    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, String>> customContainerFactory() { // 1

        // 2
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "my-kafka:9092");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        // 3
        DefaultKafkaConsumerFactory cf = new DefaultKafkaConsumerFactory<>(props);

        // 4
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.getContainerProperties().setConsumerRebalanceListener(new ConsumerAwareRebalanceListener() { // 5
            @Override
            public void onPartitionsRevokedBeforeCommit(Consumer<?, ?> consumer, Collection<TopicPartition> partitions) {

            }

            @Override
            public void onPartitionsRevokedAfterCommit(Consumer<?, ?> consumer, Collection<TopicPartition> partitions) {

            }

            @Override
            public void onPartitionsAssigned(Collection<TopicPartition> partitions) {

            }

            @Override
            public void onPartitionsLost(Collection<TopicPartition> partitions) {

            }
        });
        factory.setBatchListener(false); // 6
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD); // 7
        factory.setConsumerFactory(cf); // 8
        return factory;
    }
}
```
1. KafkaListenerContainerFactory 빈 객체를 리턴하는 메서드를 생성합니다. 이 메서드 이름은 커스텀 리스너 컨테이너 팩토리로 선언할 때 사용됩니다.
2. 카프카 컨슈머를 실행할 때 필요한 옵션값들을 선언합니다. group.id는 리스너 컨테이너에도 선언하므로 지금 바로 선언하지 않아도 됩니다.
3. 컨슈머 옵션값을 파라미터로 받는 DefaultKafkaConsumerFactory 인스턴스를 생성합니다. DefaultKafkaConsumerFactory는 리스너 컨테이너 팩토리를 생성할 때 컨슈머 기본 옵션을 설정하는 용도로 사용됩니다.
4. ConcurrentKafkaListenerContainerFactory는 리스터 컨테이너를 만들기 위해 사용됩니다. 이름에서 알 수 있듯이 2개 이상의 컨슈머 리스너를 만들 때 사용되며 concurrency를 1로 설정할 경우 1개 컨슈머 스레드로 실행됩니다.
5. 리밸런스 리스너를 선언하기 위해 setConsumerRebalanceListener를 호출합니다. setConsumerRebalanceListener는 스프링 카프카에서 제공하는 메서드로 기존에 사용되는 카프카 컨슈머 리밸런스 리스너에 2개의 메서드를 호출합니다. onPartitionsRevokedBeforeCommit는 커밋 되기 전에 리밸런스가 발생한 경우, onPartitionsRevokedAfterCommit는 커밋이 일어난 이후에 리밸런스가 발생했을 때 호출됩니다.
6. 레코드 리스너를 사용함을 명시하기 위해 setBatchListener 메서드에 false 파라미터로 넣습니다. 만약 배치 리스너를 사용하고 싶다면 true로 설정합니다.
7. AckMode를 설정합니다. 여기서는 레코드 단위로 커밋하기 위해 RECORD로 설정했습니다.
8. 컨슈머 설정값을 가지고 있는 DefaultKafkaConsumerFactory 인스턴스를 ConcurrentKafkaListenerContainerFactory의 컨슈머 팩토리에 설정합니다.

<br>

```java
@SpringBootApplication
public class SpringConsumerApplication {
    public static Logger logger = LoggerFactory.getLogger(SpringConsumerApplication.class);

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(SpringConsumerApplication.class);
        application.run(args);
    }

    @KafkaListener(topics = "test",
            groupId = "test-group",
            containerFactory = "customContainerFactory") // 1
    public void customListener(String data) {
        logger.info(data);
    }
}
```
1. KafkaListener 애노테이션의 containerFactory 옵션을 커스텀 컨테이너 팩토리로 설정합니다. 빈 객체로 등록한 이름인 customContainerFactory를 옵션값으로 설정하면 커스텀 컨테이너 팩토리로 생성된 커스텀 리스너 컨테이너를 사용할 수 있습니다.
