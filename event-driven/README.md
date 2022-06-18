# Spring - Event Driven

## 시스템 간 강결합 문제
쇼핑몰에서 구매를 취소하면 환불 처리를 해야 합니다.  
```java
@Service
public class CancelOrderService {
    private RefundService refundService;

    @Transactional
    public void cancel() {
        
        // order 취소 로직

        // 환불 로직
        try {
          refundService.refund();
        } catch(Exception ex){
          ???
        }
    }
}
```
보통 결제 시스템은 외부에 존재하므로 RefundService는 외부에 있는 결제 시스템이 제공하는 환불 서비스를 호출합니다.  
이때 세 가지 문제가 발생할 수 있습니다.  
+ 외부 서비스가 정상이 아닐 경우 트랜잭션 처리를 어떻게 해야 하는가? 롤백 해야 하나? 일단 커밋 해야 하나?
  - 환불 서비스 실행하는 과정에서 예외가 발생했으므로 주문 취소 트랜잭션을 롤백 하는 것이 맞아 보입니다.
  - 하지만 반드시 트랜잭션을 롤백 해야 하는 것이 아니라 주문은 취소 상태로 변경하고 환불만 나중에 다시 시도하는 방법으로 처리할 수도 있습니다.
+ 성능 문제 발생
  - 외부 시스템의 응답 시간이 길어지면 그만큼 대기 시간도 길어집니다.
  - 환불 처리 기능이 30초가 걸리면 주문 취소 기능은 30초만큼 대기 시간이 증가합니다.
  - 즉, 외부 서비스 성능에 직접적인 영향을 받게 됩니다.
+ 연관 관계 증가
  - 현재는 refundService와 연관 관계를 맺고 있지만, 추후 메일 서비스가 추가된다면 트랜잭션 처리가 더욱 복잡해집니다.
  - 즉, Service 들이 강결합되는 문제가 발생합니다.


이런 강한 결합을 없앨 수 있는 방법이 이벤트입니다.  
특히 비동기 이벤트를 사용하면 두 시스템 간의 결합을 크게 낮출 수 있습니다.  

## 이벤트 개요
이벤트라는 용어는 '과거에 벌어진 어떤 것'을 의미합니다.  
앞선 예시에서는 '주문을 취소했음'이 이벤트가 됩니다.  
스프링 이벤트 발행(event publishing)은 ApplicationContext가 제공하는 기능 중 하나입니다.  

### 이벤트 구성 요소
도메인 모델에서 이벤트 생성 주체는 엔티티, 밸류, 도메인 서비스와 같은 __도메인 객체__ 에 해당합니다.  
도메인 모델에 이벤트를 도입하려면 이벤트, 이벤트 생성 주체, 이벤트 디스패처(퍼블리셔), 이벤트 핸들러(구독자)를 구현해야 합니다.  


> 이벤트 생성 주체가 이벤트 생성 -> 이벤트 퍼블리셔가 이벤트 발행 -> 이벤트 핸들러가 받아서 처리

위와 같은 과정으로 이벤트가 동작합니다.  
풀어서 설명하자면, 이벤트 생성 주체는 이벤트를 생성하여 퍼블리셔에 이벤트를 전달합니다.  
이벤트를 전달받은 퍼블리셔는 해당 이벤트를 처리할 수 있는 핸들러에 이벤트를 전파합니다.  
핸들러는 이벤트를 받아서 최종적으로 처리합니다.  
이벤트 디스패처의 구현 방식에 따라 이벤트 생성과 처리를 동기나 비동기로 실행하게 됩니다.  


### 이벤트 구성
이벤트는 보통 다음과 같은 정보를 담습니다.
+ 이벤트 종류 : 클래스 이름으로 이벤트 종류를 표현
+ 이벤트 발생 시간
+ 추가 데이터 : 주문번호, 신규 배송지 정보 등 이벤트와 관련된 정보

예를 들어, 배송지를 변경할 때 발생하는 이벤트는 다음과 같은 정보를 가질 수 있습니다.
```java
public class ShippingInfoChangedEvent {
    private final OrderNo number;
    private final ShippingInfo newShippingInfo;
    private long timestamp;    
}
```
클래스 이름을 보면 Changed로 과거 시제를 사용했는데 이벤트는 현재 기준으로 과거에 벌어진 것을 표현하기 때문에 과거 시제를 사용합니다.  

### 이벤트 용도
이벤트는 크게 두 가지 용도로 쓰입니다.
+ 트리거
  - 도메인의 상태가 바뀔 때 다른 후처리가 필요하면 후처리를 실행하기 위한 용도
  - 주문을 취소하면 환불을 처리해야 하는데 이때 환불 처리를 위한 트리거로 주문 취소 이벤트를 사용할 수 있습니다.
+ 서로 다른 시스템 간의 동기화
  - 배송지를 변경하여 외부 배송 서비스에 바뀐 배송지 정보를 전송해야 할 때, 주문 도메인은 배송지 변경 이벤트를 발생시키고 이벤트 핸들러에서 외부 배송 서비스와 배송지 정보를 동기화하도록 처리할 수 있습니다.

### 이벤트 장점
+ 연관 관계 제거
+ 기능 확장 시 용이

## 이벤트, 핸들러, 디스패처 구현

### 이벤트 클래스
이벤트는 과거에 벌어진 상태 변화나 사건을 의미하므로 과거 시제를 사용하고 접미사로 Event를 사용해 이벤트로 사용하는 클래스라는 것을 명시적으로 표현합니다.  
이벤트 클래스는 이벤트를 처리하는 데 필요한 최소한의 데이터를 포함해야 합니다.  
간단한 경우 이벤트 자체의 상위 타입이 존재하지 않지만 모든 이벤트가 공통으로 갖는 프로퍼티가 존재한다면 상위 클래스를 만들고 상속받아서 구현할 수도 있습니다.  
```java
public abstract class Event {
    private long timestamp;

    public Event() {
        this.timestamp = System.currentTimeMillis();
    }

    public long getTimestamp() {
        return timestamp;
    }

}
```
```java
@Getter
@AllArgsConstructor
public class OrderCanceledEvent extends Event {
    private Long orderId;
}
```

### 이벤트 디스패처
이벤트 발행을 위해 스프링에서 제공하는 ApplicationEventPublisher을 사용합니다.  
```java
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final ApplicationEventPublisher eventPublisher;

    public void cancel(Long orderId){
        log.info("{} 주문 취소 ",orderId);

        eventPublisher.publishEvent(new OrderCanceledEvent(orderId));
    }
}
```
ApplicationEventPublisher의 publishEvent 메서드를 사용하면 이벤트를 발행할 수 있습니다.  

### 이벤트 핸들러
이벤트를 처리할 핸들러는 Spring에서 제공하는 @EventListener 애너테이션을 사용해서 구현합니다.  

```java
@Component
@RequiredArgsConstructor
public class OrderCanceledEventHandler {

    private final RefundService refundService;

    @EventListener(OrderCanceledEvent.class) // 수신할 이벤트 클래스 지정
    public void handle(OrderCanceledEvent event){
        refundService.refund(event.getOrderId());
    }
}
```
애너테이션으로 지정한 이벤트 클래스가 발행되면 바로 수신하여 이벤트를 처리합니다.  

## 동기 이벤트 처리 문제
이벤트를 사용해 Service 간의 강결합 문제를 해결했지만 아직 외부 서비스에 영향을 받는 문제와 트랜잭션 문제가 남아있습니다.  
```java
@Component
@RequiredArgsConstructor
public class OrderCanceledEventHandler {

    private final RefundService refundService;

    @EventListener(OrderCanceledEvent.class) 
    public void handle(OrderCanceledEvent event){
        refundService.refund(event.getOrderId()); // 오래 걸리거나 예외가 발생한다면??
    }
}
```
외부 서비스와 연동되어 있는 refund 메서드가 오래 걸리게 된다면 이벤트를 발행한 orderService의 cancel 메서드도 함께 느려지게 됩니다.  
또한, refundService에서 예외가 발생하면 orderService의 cancel 메서드의 트랜잭션을 롤백시켜야 할지도 고민해야 합니다.  
일단 구매 취소 자체는 처리하고 환불만 재처리하거나 수동으로 처리할 수도 있습니다.  
외부 시스템과의 연동을 동기로 처리할 때 발생하는 성능과 트랜잭션 범위 문제를 해소하는 방법은 이벤트를 비동기로 처리하거나 이벤트와 트랜잭션을 연계하는 방법이 있습니다.

## 비동기 이벤트 처리
사용자가 회원 가입 신청 시 검증 이메일은 곧 바로 오면 좋겠지만 몇 초 뒤에 와도 크게 문제되지 않습니다.
비슷하게 주문을 취소하자마자 바로 결제를 취소하지 않고 며칠 뒤에 결제가 확실하게 취소되면 문제가 없을 때도 있습니다.  
이와 같이 실제로 구현해야 할 것 중에 'A하면 이어서 B하라'는 내용을 담고 있는 요구사항은 실제로 'A하면 최대 언제까지 B'하라는 경우가 많습니다.  
이러한 요구사항의 경우 이벤트를 비동기로 처리하는 방식으로 구현할 수 있습니다.  
비동기 이벤트 처리 방식에는 네 가지 방식이 있습니다.
+ 로컬 핸들러를 비동기로 실행
+ 메세지 큐 사용
+ 이벤트 저장소와 이벤트 제공 API 사용
+ 이벤트 저장소와 이벤트 포워더 사용

### 로컬 핸들러 비동기 실행
이벤트 핸들러를 비동기로 실행하는 방법은 이벤트 핸들러를 별도 스레드로 실행하는 것입니다.  
이는 스프링에서 제공하는 @Async 애너테이션을 사용하면 쉽게 해결할 수 있습니다.  
+ @EnableAsync : 비동기 기능 활성화
+ @Async : 비동기 처리

```java
@EnableAsync // 비동기 기능 활성화
@SpringBootApplication
public class EventDrivenApplication {

    public static void main(String[] args) {
        SpringApplication.run(EventDrivenApplication.class, args);
    }
}
```
```java
@Component
@RequiredArgsConstructor
public class OrderCanceledEventHandler {

    private final RefundService refundService;

    @Async // 비동기 처리
    @EventListener(OrderCanceledEvent.class)
    public void handle(OrderCanceledEvent event){
        refundService.refund(event.getOrderId());
    }
}
```
@Async을 추가하면 해당 메서드는 기존 스레드와 분리되게 됩니다.  
따라서 OrderService의 cancel 메서드의 응답 대기는 사라지며 트랜잭션도 분리됩니다.  


### 메시징 시스템을 이용한 비동기
카프카나 래빗MQ와 같은 메시징 시스템을 사용해서 구현할 수도 있습니다.  
이벤트가 발생하면 이벤트 디스패처는 이벤트를 메시지 큐로 보내고 구독자는 메시지 리스너로 이를 전달받아 이벤트를 처리합니다.  
이때 이벤트를 메시지 큐에 저장하는 과정과 메시지 큐에서 이벤트를 읽어와서 처리하는 과정은 별도의 스레드에서 처리됩니다.  

> OrderCanceledEvent 생성(1) -> eventPublisher가 메시지 큐로 발행(2) -> 메시징 시스템 보관(3) -> 메시지 리스너가 메시지 consume 및 처리(4)

여기서 트랜잭션 범위는 1,2가 하나로 묶이고 3,4가 하나로 묶입니다.  

### 이벤트 저장소를 이용한 API 비동기 처리 방식
![그림1](https://github.com/backtony/blog-code/blob/master/spring/img/event/1/1.PNG?raw=true)  
API 방식은 이벤트를 외부에 제공하는 API를 사용하는 것입니다.  
API 방식은 외부 핸들러가 API 서버를 통해 이벤트 목록을 가져가고 이벤트 목록을 요구하는 외부 핸들러가 자신이 어디까지 이벤트를 처리했는지 기억해야 합니다.  
<br>

이벤트 저장소를 위한 기반이 되는 클래스를 우선 구현해 봅시다.  
```java
// 이벤트 저장소에 저장될 클래스
@Getter
public class EventEntry {
    private Long id;
    private String type;
    private String contentType;
    private String payload;
    private long timestamp;

    public EventEntry(String type, String contentType, String payload) {
        this.type = type;
        this.contentType = contentType;
        this.payload = payload;
        this.timestamp = System.currentTimeMillis();
    }
}
```
+ 이벤트 저장소에 보관할 데이터입니다.
+ id : 이벤트를 식별자
+ type : 이벤트 타입
+ contentType : 직렬화한 데이터 형식
+ payload: 이벤트 객체를 직렬화한 데이터
+ timestamp : 이벤트 시간

```java
public interface EventStore {
    void save(Object event);

    List<EventEntry> get(long offset, long limit);
}
```
```java
@Component
@RequiredArgsConstructor
public class JdbcEventStore implements EventStore {

    private final ObjectMapper objectMapper;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void save(Object event) {
        EventEntry entry = new EventEntry(event.getClass().getName(),
                "application/json", toJson(event));
        jdbcTemplate.update(
                "insert into event_entry " +
                        "(type, content_type, payload, timestamp) " +
                        "values (?, ?, ?, ?)",
                ps -> {
                    ps.setString(1, entry.getType());
                    ps.setString(2, entry.getContentType());
                    ps.setString(3, entry.getPayload());
                    ps.setTimestamp(4, new Timestamp(entry.getTimestamp()));
                });
    }

    private String toJson(Object event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            throw new PayloadConvertException(e);
        }
    }

    @Override
    public List<EventEntry> get(long offset, long limit) {
        return jdbcTemplate.query(
                "select * from event_entry order by id asc limit ?, ?",
                ps -> {
                    ps.setLong(1, offset);
                    ps.setLong(2, limit);
                },
                (rs, rowNum) -> {
                    return new EventEntry(
                            rs.getLong("id"),
                            rs.getString("type"),
                            rs.getString("content_type"),
                            rs.getString("payload"),
                            rs.getTimestamp("timestamp").getTime());
                });
    }
}
```
EventStore은 이벤트 객체를 직렬화하여 payload에 저장합니다.  
JSON으로 직렬화 했다면 contentType을 application/json을 갖습니다.  
이벤트는 과거에 벌어진 사건이므로 데이터가 변경되지 않기 때문에 저장과 조회 기능만 제공합니다.  
<br>

이벤트 저장소의 기반이 되는 클래스는 구현했으므로 이제 발행한 이벤트를 이벤트 저장소에 추가하는 이벤트 핸들러를 구현할 차례입니다.  
```java
@Component
@RequiredArgsConstructor
public class EventStoreHandler {
    private final EventStore eventStore;

    @EventListener(Event.class)
    public void handle(Event event) {
        eventStore.save(event);
    }
}
```
추상 클래스 Event.class를 명시하여 Event 타입을 상속받는 이벤트 타입을 처리하도록 해줍니다.  
이제 어떠한 Event가 발행되면 위 리스너를 통해 이벤트 저장소에 이벤트가 저장됩니다.  
<br>

마지막으로 REST API를 구현해줍니다.
```java
@RestController
@RequiredArgsConstructor
public class EventRestController {

    private final EventStore eventStore;

    public List<EventEntry> list(
            @RequestParam("offset") Long offset,
            @RequestParam("limit") Long limit){
        return eventStore.get(offset,limit);
    }
}
```
API를 사용하는 클라이언트는 일정 간격으로 다음 과정을 실행합니다.
1. 가장 마지막에 처리한 데이터의 offset인 lastOffset을 구한다. 없으면 0
2. 마지막에 처리한 lastOffset을 offset으로 사용해서 API 실행한다.
3. API 결과로 받은 데이터를 처리한다.
4. offset + 데이터 개수를 lastOffset으로 저장한다.

클라이언트 APi를 이용해 언제든지 원하는 이벤트를 가져올 수 있기 때문에 이벤트 처리에 실패하면 다시 실패한 이벤트부터 읽어와 처리할 수 있습니다.  
API 서버에 장애가 발생한 경우에도 주기적으로 재시도 해서 API가 살아나면 이벤트를 처리할 수 있습니다.

### 이벤트 저장소를 이용한 포워더 비동기 처리 방식
![그림2](https://github.com/backtony/blog-code/blob/master/spring/img/event/1/2.PNG?raw=true)  
API 방식과 마찬가지로 이벤트를 일단 DB에 저장한 뒤에 별도 프로그램을 이용해서 이벤트 핸들러에 전달하는 것입니다.  
이벤트가 발생하면 핸들러는 스토리지에 이벤트를 저장하고 포워더가 주기적으로 이벤트 저장소에서 이벤트를 가져와 이벤트 핸들러에 전달하면 됩니다.  
API 방식 클라이언트와 마찬가지로 마지막으로 전달한 이벤트의 offset을 기억해 두었다가 다음 조회 시점에 마지막으로 처리한 offset부터 이벤트를 가져오면 됩니다.  
포워더는 별도의 스레드를 이용하기 때문에 이벤트 발행과 처리가 비동기로 처리되며 이벤트를 물리적 저장소에 보관하기 때문에 핸들러가 이벤트 처리에 실패할 경우 포워더는 다시 이벤트 저장소에서 이벤트를 읽어와 핸들러를 실행하면 됩니다.  

```java
@Component
@RequiredArgsConstructor
public class EventForwarder {
    private static final int DEFAULT_LIMIT_SIZE = 100;

    private final EventStore eventStore;
    private final OffsetStore offsetStore;
    private final EventSender eventSender;
    private final int limitSize = DEFAULT_LIMIT_SIZE;


    // @scheduled 애너테이션을 사용하려면 XXXApplication클래스에 @EnableScheduling을 붙여야 합니다.
    @Scheduled(initialDelay = 1000L, fixedDelay = 1000L) // 스케줄링
    public void getAndSend() {
        long nextOffset = getNextOffset(); // 읽어올 이벤트의 offset 가져오기
        List<EventEntry> events = eventStore.get(nextOffset, limitSize); // 이벤트 가져오기
        if (!events.isEmpty()) {
            int processedCount = sendEvent(events); // 이벤트 전달
            if (processedCount > 0) {
                saveNextOffset(nextOffset + processedCount); // offset 업데이트
            }
        }
    }

    private long getNextOffset() {
        return offsetStore.get();
    }

    private int sendEvent(List<EventEntry> events) {
        int processedCount = 0;
        try {
            for (EventEntry entry : events) {
                eventSender.send(entry); // 이벤트 전달
                processedCount++;
            }
        } catch(Exception ex) {           
            // 로깅 처리
        }
        // 예외 발생시 전송을 중단하고 처리한 것 까지만 해서 개수 반환하므로
        // 이후 시도는 예외 시점부터 다시 동작
        return processedCount;
    }

    private void saveNextOffset(long nextOffset) {
        offsetStore.update(nextOffset);
    }
}
```
eventSender.send의 실질적인 로직은 외부 메시징 시스템에 이벤트를 전송하거나 원하는 핸들러에 이벤트를 전달하면 됩니다.  


<br>

__주의사항__  
이벤트 저장소를 DB로 사용할 때 주요키로 자동 증가 컬럼을 사용하면 주의해야할 점이 있습니다.  
자동 증가 컬럼은 insert 쿼리를 실행하는 시점에 값이 증가하지만 실제 데이터는 트랜잭션을 커밋하는 시점에 DB에 반영됩니다.  
즉, insert 쿼리를 실행해서 자동 증가 컬럼이 증가했더라도 트랜잭션을 커밋하기 전에 조회하면 증가한 값을 가진 레코드는 조회되지 않습니다.  
또한 커밋 시점에 따라 DB에 반영되는 시점이 달라질 수도 있습니다.  
예를 들어 마지막 자동 증가 컬럼 값이 10인 상태에서 A 트랜잭션이 insert 쿼리를 실행한 뒤에 B 트랜잭션이 insert쿼리를 실행했다면 A는 11, B는 12를 자동 증가 컬럼 값으로 사용하게 됩니다.  
그런데 B트랜잭션이 먼저 커밋되고 그다음에 A트랜잭션이 커밋되면 12가 DB에 먼저 반영되고 그다음 11이 반영됩니다.  
만약 B 트랜잭션 커밋과 A 트랜잭션 커밋 사이에 데이터를 조회한다면 11은 조회되지 않고 12만 조회되는 상황이 발생합니다.  
이점을 인지하지 못하고 가장 마지막 offset을 12로 업데이트 시키면 11이 누락되는 현상이 발생하게 됩니다.  
이를 해결하기 위해서는 트랜잭션 격리 레벨을 높이거나 데이터 조회 시점과 최대 ID가 증가하는 시점에 차이를 둬야 합니다. CDC(change Data Capture)을 사용하는 방식도 있다고 합니다.  

## 이벤트 적용 시 추가 고려사항
앞에서는 구현하지 않았지만 이벤트를 구현할 때 추가로 고려해야 할 점이 있습니다.  
<br>

첫 번째는 이벤트 소스를 EventEntry에 추가할지 여부입니다.  
앞서 구현한 EventEntry는 이벤트 발생 주체에 대한 정보를 갖지 않습니다. 따라서 'Order가 발생시킨 이벤트만 조회하기'처럼 특정 주체가 발생시킨 이벤트만 조회하는 기능을 구현할 수 없습니다. 이 기능을 구현하려면 이벤트에 발생 주체 정보를 추가해야 합니다.  

<Br>

두 번째는 포워더에서 전송 실패를 얼마나 허용할 것이냐에 대한 것입니다.  
포워더는 이벤트 전송에 실패하면 실패한 이벤트부터 다시 읽어와 전송을 시도합니다. 그런데 특정 이벤트에서 계속 전송이 실패한다면 나머지 이벤트를 처리할 수 없게됩니다. 따라서 포워더를 구현할 때는 실패한 이벤트의 재전송 횟수 제한을 두어야 합니다. 예를 들어 동일 이벤트를 전송하는 데 3회 실패했다면 해당 이벤트는 생략하고 다음 이벤트로 넘어간다는 등의 정책이 필요합니다. 처리에 실패한 이벤트를 생략하지 않고 별도 실패용 DB나 메시징 큐에 저장하기도 하는데 이를 물리적인 저장소에 남겨두면 이후 실패 이유 분석이나 후처리에 도움이 됩니다.  

<br>

세 번째는 이벤트 손실에 대한 것입니다.  
이벤트 저장소를 사용하는 방식은 이벤트 발생과 이벤트 저장을 한 트랜잭션으로 처리하기 때문에 트랜잭션에 성공하면 이벤트 저장소에 보관된다는 것을 보장할 수 있습니다. 반면에 로컬 핸들러를 이용해서 이벤트를 비동기로 처리할 경우 이벤트 처리에 실패하면 이벤트를 유실하게 됩니다.  

<br>

네 번째는 이벤트 순서에 대한 것입니다.  
이벤트 발생 순서대로 외부 시스템에 전달해야 할 경우, 이벤트 저장소를 사용하는 것이 좋습니다. 이벤트 저장소는 저장소에 이벤트를 발생 순서대로 저장하고 그 순서대로 이벤트 목록을 제공하기 때문입니다. 반면에 메시징 시스템은 사용 기술에 따라 이벤트 발생 순서와 메시지 전달 순서가 다를 수 있습니다.  

<br>

다섯 번째 고려할 점은 이벤트 재처리에 대한 것입니다.  
동일한 이벤트를 다시 처리해야 할 때 이벤트를 어떻게 할지 결정해야 합니다. 가장 쉬운 방법은 마지막으로 처리한 이벤트의 순번을 기억해 두었다가 이미 처리한 순번의 이벤트가 도착하면 해당 이벤트를 처리하지 않고 무시하는 것입니다. 예를 들어 회원 가입 신청 이벤트가 처음 도착하면 이메일을 발송하는데, 동일한 순번의 이벤트가 다시 들어오면 이메일을 발송하지 않는 방식으로 구현합니다. 또는 이벤트를 멱등으로 처리하는 방법이 있습니다. 이벤트를 한 번 적용하나 여러 번 적용하나 시스템이 같은 상태가 되도록 핸들러를 구현하면 됩니다. 예를 들어 배송지 정보 변경 이벤트를 받아서 주소를 변경하는 핸들러는 그 이벤트를 한 번 처리하나 여러 번 처리하나 결과적으로 동일 주소를 값으로 갖습니다. 같은 이벤트를 여러 번 적용해도 결과가 같으므로 이 이벤트 핸들러는 멱등성을 갖습니다. 이벤트 핸들러가 멱등성을 가지면 시스템 장애로 인해 같은 이벤트가 중복해서 발생해도 결과적으로 동일 상태가 되므로 중복 발생이나 중복 처리에 대한 부담이 줄어듭니다.  

### 이벤트 처리와 DB 트랜잭션 고려
이벤트를 처리할 때는 DB 트랜잭션을 함께 고려해야 합니다.  
예를 들어 주문 취소와 환불 기능을 다음과 같이 이벤트를 사용해 구현했다고 해봅시다.  
+ 주문 취소 기능은 주문 취소 이벤트를 발생시킨다.
+ 주문 취소 이벤트 핸들러는 환불 서비스에 환불 처리를 요청한다.
+ 환불 서비스는 외부 API를 호출해서 결제를 취소한다.


위 과정을 동기방식으로 간략하게 표현하면 '주문 취소 -> 결제 취소 -> 주문 상태 변경' 으로 볼 수 있습니다.  
결제 취소까지 다 성공하고 마지막에 주문 상태 변경을 DB 업데이트애 실패했다면, 결제 취소는 외부 서비스를 사용하기 때문에 결제 취소가 되었지만, DB에는 주문이 취소되지 않는 상태로 남게됩니다.  

<br>

> 주문 취소 -> 이벤트 발행 -> DB 업데이트(동기), 결제 취소 로직 호출(비동기)

비동기로 처리하게 되면 주문 취소 로직과 결제 취소 로직이 분리되서 실행됩니다.  
만약 DB 업데이트와 트랜잭션을 다 커밋한 뒤에 환불 로직이 실행되었다고 가정했을 때 결제 취소 외부 API 호출이 실패하더라도 여전히 DB에는 결제 취소로 업데이트가 되는 문제가 발생합니다.  
<br>

이벤트 처리를 동기로 하든 비동기로 하든 이벤트 처리 실패와 트랜잭션 실패를 함께 고려해야 합니다.  
트랜잭션 실패와 이벤트 처리 실패를 모두 고려하면 복잡해지므로 경우의 수를 줄이는 것이 좋습니다.  
경우의 수를 줄이는 방법은 트랜잭션이 성공할 때만 이벤트 핸들러를 실행하는 것입니다.  
스프링은 @TransactionalEventListener 애너테이션을 지원합니다.  
이는 스프링 트랜잭션 상태에 따라 이벤트 핸들러를 실행할 수 있게 합니다.  
```java
@Async // 기존 트랜잭션 이후 동작을 비동기로 수행
@TransactionalEventListener(
        classes = OrderCanceledEvent.class,
        phase = TransactionPhase.AFTER_COMMIT // 기존 트랜잭션 커밋 이후 호출
)
public void handle(OrderCanceledEvent event) {
    refundService.refund(event.getOrderNumber());
}
```
기존 @EventListener의 경우 applicationEventPublisher에서 publish 한 이후 바로 리스너가 동작하게 됩니다.  
반면에 @TransactionalEventListener의 phase 속성 값으로 AFTER_COMMIT을 명시하면 현재 트랜잭션 커밋이 성공한 뒤에 핸들러 메서드를 실행합니다.  
이를 통해 이벤트 핸들러를 실행했는데 트랜잭션이 롤백되는 상황을 막을 수 있습니다.  
즉, 트랜잭션 실패에 대한 경우의 수를 없앨 수 있습니다.  
주의할 점은 TransactionalEventListener은 기존 트랜잭션을 커밋 이후에 작업을 수행하기 때문에 핸들러에서 시작하는 로직부터는 엔티티 수정을 가해봐야 이미 커밋했으므로 반영되지 않는다는 점입니다.  
@Async로 새로운 스레드에서 동작하면서 refundService에서 트랜잭션을 열어서 엔티티를 다시 가져와서 작업하는 것은 가능합니다.  
하지만 @Async 없이 refundService에서 새로운 트랜잭션을 열어서 엔티티를 가져와 수정하는 것은 제대로 동작하지 않습니다.  


<br>

이벤트 저장소로 DB를 사용하는 경우에는 이벤트 발생 코드와 이벤트 저장 처리를 한 트랜잭션으로 처리하면 동일한 효과를 볼 수 있습니다.  
이렇게 하면 트랜잭션이 성공할 때만 이벤트가 DB에 저장되므로, 트랜잭션이 실패했는데 이벤트 핸들러가 실행되는 상황은 발생하지 않습니다.  

<br>

트랜잭션이 성공할 때만 이벤트 핸들러를 실행하게 되면 트랜잭션 실패에 대한 경우의 수가 줄어 이제 이벤트 처리 실패만 고려하면 됩니다.  
이는 이벤트 특성에 따라 재처리 방식을 결정해서 해결하면 됩니다.  




## 우아한 형제들의 Event Driven
앞서 Event Driven의 기본적인 개념과 동작 방식에 대해 알아봤습니다.  
하지만 도대체 실무에서는 어떻게 사용하는 게 좋은 방식인지 파악하기는 어려웠고, 해답은 우아한 형제들 기술 블로그의 [포스팅](https://techblog.woowahan.com/7835/)과 [발표](https://www.youtube.com/watch?v=BnS6343GTkY&t=1765s)에서 찾을 수 있었습니다.  
자세한 내용은 링크를 타고 들어가셔서 보시면 되고 저는 간략하게 제가 궁금했던 부분만 정리해 보겠습니다.  

### 동작 과정

![그림3](https://github.com/backtony/blog-code/blob/master/spring/img/event/1/3.PNG?raw=true)  
도메인 로직에서 관심사 로직을 제외한 모든 비관심사 로직은 이벤트 발행을 통해 처리하여 결합도를 낮춥니다.  
이벤트 핸들러(첫 번째 구독자 계층)는 발행된 이벤트를 전달받아 AWS SNS로 전달합니다.  

<br>

![그림4](https://github.com/backtony/blog-code/blob/master/spring/img/event/1/4.PNG?raw=true)  
예를 들어, 기존 로직에서 1개의 관심사 로직과 2개의 비관심사 로직이 있었다면 이벤트 발행을 통해 2개의 비관심사 로직을 해결하면 됩니다.  
2개의 비관심사 로직을 해결하기 위해 SNS는 2개의 SQS로 이벤트를 전파하고 각각의 메시지 구독자가 이를 consume해서 이벤트를 처리합니다.  
__하나의 시스템 내의 하나의 로직__ 에서 비관심사를 이벤트로 처리하는 과정으로 SQS에서 메시지를 받는 메시지 구독자가 두 번째 구독차 계층이 됩니다.  

<br>

![그림5](https://github.com/backtony/blog-code/blob/master/spring/img/event/1/5.PNG?raw=true)  
시스템 내의 비관심사를 분리했지만, MSA를 위한 외부 시스템과의 관심사 분리를 위한 외부 이벤트 발행이 필요할 수 있습니다.  
외부 시스템에 이벤트를 전파하는 행위 또한 도메인 내에 존재하던 비관심사로 볼 수 있습니다.  
외부 시스템에 이벤트를 전파하는 행위는 다른 내부 이벤트 처리와 동일하게 두번째 구독자 계층의 SNS 발행을 책임지는 이벤트 구독자로부터 외부 이벤트가 발행됩니다.  
즉, 두번째 구독자 계층에서는 비관심사 내부 로직을 처리하는 구독자와 외부 이벤트를 발행하는 구독자가 존재하는 것입니다.  

<br>

![그림6](https://github.com/backtony/blog-code/blob/master/spring/img/event/1/6.PNG?raw=true)  
내부 이벤트를 외부에서 구독하도록 처리하면 될텐데 번거롭게 굳이 내, 외부 이벤트를 분리할 필요가 있을까? 라고 생각할 수 있습니다.  
구분하는 이유는 내부 이벤트와 외부 이벤트를 분리함으로써 내부에는 열린, 외부에는 닫힌 이벤트를 제공할 수 있다는 장점이 있기 때문입니다.  
동일한 이벤트를 수신하더라도 각 구독자마다 서로 다른 목적을 가지고 있습니다. 이로인해 각 구독자는 이벤트를 인지하는 것 이상으로 데이터가 더 필요하게 될 수 있습니다.  


### 열린 내부이벤트, 닫힌 외부이벤트
내부이벤트에는 구독자가 필요한 데이터를 페이로드에 제공하여 이벤트 처리의 효율을 챙길 수 있습니다. 이런 페이로드의 확장을 열어둘 수 있는 것은 이 이벤트가 내부 이벤트이기 때문입니다. 내부 이벤트는 시스템 내에 존재하기 때문에 이벤트의 발행이 구독자에게 미치는 영향을 파악하고 관리할 수 있습니다. 또한 외부에 알릴 필요없는 내부의 개념을 이벤트에 녹일 수도 있습니다. 이러한 확장이 가능한 것 또한 내부 이벤트는 시스템 내에 존재하는 이벤트이기 때문입니다.  

반면 외부 시스템 으로 전파되는 외부이벤트는 내부 이벤트와는 다릅니다. 내부 이벤트는 도메인에 존재하는 비관심사를 분리하여 도메인의 응집도를 높이고 비관심사를 효율적으로 처리하는 것을 목적으로 하며, 외부 이벤트는 시스템과 시스템의 결합을 줄이는 것을 목적으로 합니다. 시스템 간의 결합을 느슨하게 만들기 위해 발행되는 외부 이벤트는 이벤트 발행처에서 이벤트 구독자가 어떤 행위를 하는지 관심을 가지면 안되며, 관리할 수 없습니다. 이벤트 발행처가 이벤트 구독자의 행위에 관심을 갖게 된다면 이는 또 다시 논리적인 의존 관계를 형성하게 되는 것 입니다.  

외부시스템에서도 이벤트를 처리하기 위해 더 많은 정보가 필요할 것 입니다. 그러나 외부시스템의 비지니스에서 필요한 데이터를 페이로드에 추가하게 되면, 외부시스템의 비지니스 변화에 직접적인 의존 관계를 형성하게 될 것 입니다. 외부시스템과의 의존을 갖지 않는 이벤트를 만들기 위해 하나의 형태로 이벤트를 전달할 수 있는 이벤트에 대한 일반화가 필요합니다.  


### 이벤트 일반화
외부 시스템이 이벤트로 수행하려는 행위는 광범위하겠지만, 이벤트를 인지하는 과정은 쉽게 일반화할 수 있습니다.  
__“언제, 어떤 회원이(식별자) 무엇을 하여(행위) 어떤 변화(변화 속성)가 발생했는가"__  
식별자와 행위, 속성, 이벤트 시간 이 있다면 어떠한 시스템에서도 필요한 이벤트를 인지할 수 있음을 알 수 있습니다.  
이를 페이로드로 구현하면 이벤트를 수신하는 측에서 필요한 이벤트를 분류하여 각 시스템에서 필요한 행위를 수행할 수 있습니다.  
```java
// 멤버 도메인 이벤트
public class ExternalEvent {
    private final String memberNumber; // 식별자
    private final MemberEventType eventType; // 행위
    private final List<MemberEventAttributeType> attributeTypes; // 변화 속성
    private final LocalDateTime eventDateTime; // 이벤트 시간
}
```
외부 시스템들은 정해진 이벤트 형식 내에서 필요한 행위를 수행하면 되므로, 이벤트를 발행하는 시스템은 외부 시스템의 변화에 영향을 받지 않을 수 있습니다.

### ZERO-PAYLOAD 방식
닫혀있는 외부이벤트의 부가 데이터를 전달하는 방식으로는 ZERO-PAYLOAD 방식을 선택합니다. ZERO-PAYLOAD 방식이란 이벤트 발행에 ID와 몇 가지 정보만 넣어서 보내고 이외의 필요한 정보는 수신한 곳에서 ID를 기반으로 API를 호출하여 데이터를 채워서 처리하는 방식을 말합니다.  
이벤트 발행 방식을 이벤트 메시지 안에 모든 데이터 혹은 변경된 데이터를 보낼 수 있겠지만 이렇게 되면 이벤트의 순서를 고려해야 합니다. 예를 들면, 가게 연락처를 A라고 변경했다가 B로 다시 변경했다 하더라도 B가 먼저 오고 A가 나중에 들어올 수도 있습니다.  
이 문제를 해결하려면 너무 많은 작업이 필요하기에 차라리 이벤트를 수신하면 항상 최신의 데이터를 조회해서 갱신하는 방법을 선택합니다. 이를 통해 이벤트 순서에 대한 보장 문제를 해소하고 필요한 부가 정보는 API를 통해 보장된 최신상태의 데이터를 사용할 수 있습니다.  

### 이벤트 저장소 구축

#### 이벤트 저장소를 구축해야 하는 이유
__이벤트 발행에 대한 보장 유실__  
![그림7](https://github.com/backtony/blog-code/blob/master/spring/img/event/1/7.PNG?raw=true)  

SNS-SQS-애플리케이션 구간에서는 SQS의 정책을 통해 안정적인 실패 처리, 재시도 처리가 가능하지만 애플리케이션-SNS 구간에서는 HTTP 통신을 사용하므로 이벤트 발행하는 과정에 문제가 발생할 수 있습니다.  
내부 이벤트를 발행하는 과정을 트랜잭션 내부로 정의하면 메시징 시스템의 장애가 곧 시스템의 장애로 이러질 수 있기 때문에 이를 해결해야 합니다.
```java
@Async(EVENT_HANDLER_TASK_EXECUTOR)
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
public void handleJoinEvent(MemberJoinApplicationEvent event) {
    MemberJoinEventPayload payload = MemberJoinEventPayload.from(event);
    notificationMessagingTemplate.sendNotification(clientNameProperties.getSns().getJoin(), payload, null);
}
```
TransactionalEventListener의 AFTER_COMMIT 옵션과 @Async를 사용하여 해결합니다.  
비동기이기 때문에 기존 로직과 이벤트 로직이 분리되면서 SNS와의 통신에 장애가 발생해도 기존 로직을 성공적으로 처리할 수 있습니다.  
또한, TransactionalEventListener의 AFTER_COMMIT 옵션을 통해 기존 로직이 트랜잭션 커밋(성공)이 된 이후에야 이벤트 핸들러가 동작하기 때문에 트랜잭션은 실패했는데 이벤트가 비동기라서 이벤트가 수행되는 현상을 막을 수 있습니다.  

<br>

![그림8](https://github.com/backtony/blog-code/blob/master/spring/img/event/1/8.PNG?raw=true)  

그러나 트랜잭션 외부에서 SNS로 메시지를 발행하는 동작이 처리되기 때문에 이벤트 발행에 대한 보장이 사라지게 되었습니다.  
즉, 기존 로직이 SNS 발행 로직과 다른 스레드에서 돌기 때문에 발행 로직이 실패하더라도 기존 로직은 성공하기 때문에 이벤트 발행을 보장할 수 없다는 의미입니다.  
<br>

__이벤트 저장소를 구축해야 하는 이유2: 이벤트 재발행__  
구독자들이 이벤트를 정상적으로 처리하더라도, 이벤트 처리를 잘못할 수 있기 때문에 언제든 이벤트를 재발행 해줄 수 있어야 합니다.  
<br>

#### 이벤트 저장 시점
앞선 문제점들을 해결하기 위해서 이벤트 저장소를 도입합니다.  
이벤트 저장소를 사용해 다시 복구를 하기 위해서는 이벤트 저장소에 이벤트 저장하는 것을 도메인의 중요한 행위로 정의해야 합니다.  
모든 도메인 이벤트는 반드시 이벤트 저장소에 저장되어야 하며, 저장소에 저장이 실패하게 되었을 때 도메인 행위도 실패했다고 간주한다는 리스크가 있지만, 어딘가에서는 반드시 데이터를 보장해야 하기 때문에 이런 정의가 필요합니다.  
```java
@EventListener
@Transactional
public void handleEvent(MemberJoinApplicationEvent event) {
    memberEventRecorder.record(event.toEventCommand());
}
```
이 정의를 통해 이벤트 저장소에 대한 저장을 트랜잭션 범위 내에서 처리하는 구독자를 하나 만들어 줍니다.  
즉, 기존 로직과 더불어 이벤트 저장소에 이벤트를 저장하는 로직이 하나의 트랜잭션에서 처리되기에 이벤트 저장소에 저장이 실패하면 기존 로직도 실패합니다.  
첫 번째 구독 계층에서 SNS로 보내는 구독자와 이벤트 저장소에 저장하는 구독자가 있는 것입니다.  
![그림9](https://github.com/backtony/blog-code/blob/master/spring/img/event/1/9.PNG?raw=true)  

#### 이벤트 저장 데이터 형태
```sql
create table member_event
(
    id            varchar(128) not null primary key,
    published     tinyint      not null,
    published_at  datetime     null,
    created_at    datetime     not null
);

create index ix_member_event_created_at_published
    on member_event (created_at, published);
```
이벤트 발행을 보장하기 위해 이벤트가 발행되었는지 여부를 확인할 수 있어야 합니다.  
이벤트 발행에 대한 여부를 확인할 수 있도록 발행 여부 플래그가 필요하며, 이벤트 자체에 대한 식별자가 필요합니다.  

```sql
alter table member_event add member_number varchar(12) not null;
alter table member_event add event_type varchar(255) not null;
alter table member_event add attributes text not null;

create index ix_member_event_event_type_created_at
    on member_event (event_type, created_at);

create index ix_member_event_member_number
    on member_event (member_number);
```
식별자와 행위, 속성, 이벤트 시간이 있다면 어떤 시스템에서도 필요한 이벤트를 인지할 수 있으므로 이를 추가적으로 정의하여 이벤트 조회를 해결합니다.  


#### 이벤트 발행 보장
이벤트 발행에 대한 보장이 필요한 지점은 __내부 이벤트를 발행하는 과정__ 입니다.  
최초 이벤트를 기록할 때는 발행 여부를 false로 저장하고, 두 번째 구독자 계층에 이벤트 발행 여부를 기록하는 구독자를 추가하여 데이터를 업데이트 처리합니다.  
이때 이벤트 발행 여부를 기록하는 구독자는 이벤트의 ID만 있다면 처리할 수 있기 때문에 모든 이벤트의 super class를 정의하여 모든 이벤트가 이벤트 ID를 갖도록 만들어줍니다.  
```java
public abstract class EventPayload {
    private final String eventId;
}
```
```java
@SqsListener(value = "${sqs.event-publish-record}", deletionPolicy = SqsMessageDeletionPolicy.ON_SUCCESS)
public void recordEventPublish(@Payload EventPayload eventPayload) {
    eventPublishRecordCommand.record(eventPayload.getEventId());
}
```
![그림10](https://github.com/backtony/blog-code/blob/master/spring/img/event/1/10.PNG?raw=true)  


1. 도메인 이벤트가 발생할 때 첫번째 계층의 이벤트 저장 구독자는 트랜잭션을 확장하여 도메인 행위와 함께 이벤트가 저장소에 저장되게 됩니다.
2. 첫번째 계층의 SNS 발행 구독자는 AFTER_COMMIT 옵션으로 인해 도메인의 트랜잭션이 정상 처리되었을 때 SNS로 내부이벤트를 발행하게 됩니다.
3. 두번째 계층의 이벤트 발행 기록 구독자는 내부이벤트를 수신하여 이벤트가 정상 발행되었음을 기록합니다.
4. 정상 발행되지 않은 이벤트는 이벤트 발행 감지 배치 를 통해 자동 재발행 처리됩니다.

이제 내부 이벤트가 메시징 시스템으로 정상 발행되었다면 반드시 이벤트의 발행여부가 업데이트될 것 입니다.  
여기에 추가적으로 이벤트 발행이 누락된 케이스를 사람이 감지하는 것이 아닌 시스템이 감지하여 자동으로 재발행할 수 있도록 배치 프로그램을 구성합니다. 이 배치 프로그램은 이벤트 저장 시간을 기준으로 5분이 지나도 발행처리 되지 않은 이벤트를 SNS 에 재발행 합니다. 5분을 기준으로 한 이유는 AWS SQS의 재시도 처리가 최대 5분까지 진행될 수 있도록 설정을 해두었기 때문입니다. 이 배치 프로그램은 직접 이벤트의 상태를 변경하지 않습니다. 이벤트를 재발행하여 메시징 시스템에 정상적으로 전달이 된다면 이벤트 발행 처리 구독자에 의해 구독 처리가 될 것이기 때문입니다.  
이제 이벤트 저장소에 모든 이벤트가 남아있기 때문에 이벤트 저장소를 통해 모든 이벤트를 재발행할 수 있습니다.  

<Br>

우아한 형제들은 이렇게 이벤트 저장소와 발행 처리 구독자, 배치 프로그램을 통해 메시지 발행이 보장되는 이벤트 시스템을 구축했다고 합니다.  





<Br><Br>

__참고__  
<a href="https://www.baeldung.com/spring-events" target="_blank"> Spring Events</a>   
<a href="http://www.kyobobook.co.kr/product/detailViewKor.laf?ejkGb=KOR&mallGb=KOR&barcode=9791162245385&orderClick=LEa&Kc=" target="_blank"> 도메인 주도 개발 시작하기</a>   
<a href="https://www.youtube.com/watch?v=BnS6343GTkY&t=1765s" target="_blank"> [우아콘2020] 배달의민족 마이크로서비스 여행기</a>   
<a href="https://techblog.woowahan.com/7835/" target="_blank"> 회원시스템 이벤트기반 아키텍처 구축하기</a>   










