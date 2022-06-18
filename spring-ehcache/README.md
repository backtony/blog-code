
Ehcache 3.x 버전을 사용하는 포스팅이므로 2.x 버전을 사용하신다면 [이동욱님 블로그](https://jojoldu.tistory.com/57) 를 참고하세요.

## Ehcache란?
Ehcache는 Spring에서 간단하게 사용할 수 있는 Java 기반 오픈소스 캐시 라이브러리입니다.  
<br>

redis나 memcached 같은 캐시 엔진들도 있지만 이 2개와 달리 Ehcache는 데몬을 가지지 않고 Spring 내부적으로 동작하여 캐싱 처리를 합니다. 따라서 redis같이 별도의 서버를 사용하여 생길 수 있는 네트워크 지연 혹은 단절같은 이슈에서 자유롭고 같은 로컬 환경 일지라도 별도로 구동하는 memcached와 다르게 Ehcache는 서버 어플리케이션과 라이프사이클을 같이 하므로 사용하기 더욱 간편합니다.  
<br>

ehcache는 2.x 버전과 3 버전의 차이가 큽니다.  
3 버전 부터는 javax.cache API (JSR-107)와의 호환성을 제공합니다. 따라서 표준을 기반으로 만들어졌다고 볼 수 있습니다. 또한 기존 2.x 버전과는 달리 3 버전에서는 offheap 이라는 저장 공간을 제공합니다. offheap이란 말 그대로 힙 메모리를 벗어난 메모리로 Java GC에 의해 데이터가 정리되지 않는 공간입니다.


## 세팅
## build.gradle
```groovy
implementation 'org.springframework.boot:spring-boot-starter-cache'
implementation 'org.ehcache:ehcache:3.10.0'

// JSR-107 API를 사용하기 위함
implementation 'javax.cache:cache-api:1.1.1'
```

## Config
```java
@Configuration
@EnableCaching
public class CacheConfig {
}
```
@EnableCaching 를 통해 캐시를 사용하겠다고 세팅해줍니다.


## Ehcache.xml
Ehcache.xml 파일에 캐시 관련 설정을 해야 합니다.  
파일의 위치는 resources 디렉터리 하위에 위치하도록 합니다.
```xml
<!-- config : XML 구성의 루트 요소이다. -->
<config xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xmlns="http://www.ehcache.org/v3"
        xmlns:jsr107="http://www.ehcache.org/v3/jsr107"
        xsi:schemaLocation="
            http://www.ehcache.org/v3 http://www.ehcache.org/schema/ehcache-core-3.0.xsd
            http://www.ehcache.org/v3/jsr107 http://www.ehcache.org/schema/ehcache-107-ext-3.0.xsd">

    <!-- 캐시 설정 시작 -->
    <cache alias="memberInfoResponseDto"> <!-- @Cacheable의 value, 캐시 이름 -->
        <key-type>java.lang.Long</key-type> <!-- @Cacheable의 key, 캐시 키 타입, 기본 값은 java.lang.Object -->
        <!-- @Cacheable의 return 타입 class 위치, 기본 값은 java.lang.Object -->
        <value-type>com.example.springehcache.member.application.dto.MemberInfoResponseDto</value-type>

        <!-- ttl 설정 -->
        <expiry>
            <ttl unit="seconds">30</ttl>
        </expiry>
        <!-- unit은 days, hours, minutes, seconds, millis, micros, nanos 를 세팅할 수 있다. -->

        <!-- Cache의 리스너 클래스를 등록하는 설정, 리스너가 필요 없다면 등록하지 않아도 된다. -->
        <listeners> 
            <listener>
                <!-- 리스너 클래스 위치 -->
                <class>com.example.springehcache.common.CacheEventLogger</class>

                <!-- 비동기 방식 , 반대 동기는 SYNCHRONOUS -->
                <event-firing-mode>ASYNCHRONOUS</event-firing-mode>
                <!-- 순서 보장 X, 반대는 ORDERED -->
                <event-ordering-mode>UNORDERED</event-ordering-mode>

                <!-- EVICTED, EXPIRED, REMOVED, CREATED, UPDATED-->
                <!-- 생성과 만료 시 이벤트 발생 -->
                <events-to-fire-on>CREATED</events-to-fire-on>
                <events-to-fire-on>EXPIRED</events-to-fire-on>
            </listener>
        </listeners>

        <!-- resources는 캐시 데이터의 저장 공간과 용량을 지정한다. 만약 힙 메모리만 사용한다면 <heap> 요소만으로 대체할 수 있다.  -->
        <resources> 
            <!-- heap은 JVM 힙 메모리에 캐시를 저장하도록 세팅하는 요소
            entries는 항목이다. 100개의 항목을 힙에 저장할 수 있다는 뜻 
            100개가 넘어가면 가장 오랫동안 참조하지 않은 것을 삭제하고 새로운 것을 저장(LRU) -->
            <!-- 메모리가 충분하다는 가정 하에 max는 5000 이하로 설정하는 것을 권장 -->
            <heap unit="entries">100</heap>

            <!-- offheap은 JVM 힙 메모리 외부의 메모리에 캐시를 저장하도록 세팅하는 요소이다. -->
            <offheap unit="MB">10</offheap>
            <!-- B, KB, MB, GB, TB -->

            <!-- Disk 메모리, LFU strategy-->
            <!-- persistent="false" shutdown 되면 disk를 비워버립니다.-->
            <!-- persistent="true" shutdown 되도 disk에 보관되고 JVM이 다시 뜨면 load it back 합니다.-->
            <!-- <disk unit="MB" persistent="false">5</disk> -->
        </resources>
    </cache>

</config>
```
resources에 보면 캐시 데이터를 어디에 저장할 지 공간과 용량을 지정합니다. 힙, 외부 메모리, 디스크 방식이 있습니다.  
위에 주석으로 다 설명을 적었고 단 하나만 사용하지 않고 여러 곳 섞어서 저장해도 됩니다.  
다만 주의할 점으로 off-heap에 저장한다면 저장되는 객체는 반드시 직렬화 해야 합니다.


__cf) on-heap store, off-heap store__  
Java 프로그램에서 new 연산 등을 이용해 객체를 생성하면 JVM은 힙(heap) 메모리 영역에 객체를 생성하여 저장, 관리합니다. Java에서는 명시적으로 할당받은 메모리 영역을 해제하는 등의 작업을 GC가 대신 해주기 때문에 따로 처리하지 않아도 됩니다. 이렇게 Java에서 생성된 객체가 저장되는 힙 영역을 On-heap store라고 합니다.  
일반적인 경우라면 on-heap 영역만 인지하고 사용해도 어플리케이션을 사용하는데 문제가 없으나 동작을 더 빠르게 할 수 있는 방법이 있습니다.  
on-heap store에 객체를 저장하다보면 GC 오버헤드(특히 full GC)때문에 어플리케이션의 성능이 저하되는 경험을 하게 됩니다. 특히 굉장히 큰 사이즈의 데이터를 생성한다던가, 굉장히 많은 숫자의 객체를 생성한다던가 하면 GC 대상 객체들의 증가로 어플리케이션이 버벅일 가능성이 높아집니다.  
특히 실시간에 가까운 데이터를 처리해야 하는 빅데이터 플랫폼의 경우 Off-heap 사용을 고려해봐야 할 수 있습니다.(Full GC가 pause로 실시간성을 저해할 수 있기 때문)  
<br>

off-heap store은 '힙 밖에 저장한다'라는 의미를 가집니다. 힙 밖에 저장한다는 의미는 GC의 대상으로 삼지 않겠다는 의미입니다.  
Java에서는 nio를 통해 off-heap의 사용을 제공하고 있습니다. Direct ByteBuffer를 이용해 할당받은 버퍼 공간은 GC의 대상이 되지 않는 off-heap store에 저장됩니다. 다만 GC가 하던 작업을 직접해야 하기 때문에 번거롭고 위험할 수 있기에 Ehcache, Terrcotta BigMemory 같은 off-heap store을 이용한 캐시 라이브러리를 사용하는 것이 좋습니다.(이것들이 대신 관리해줍니다.)  
off-heap store에 저장되는 객체는 직렬화(Serialize)한 다음에 저장해야 합니다. 이런저런 이유로 Allocation/Deallocation 비용이 일반 버퍼에 비해 높기 때문에 off-heap store에 저장되는 객체는 사이즈가 크고 오랫동안 메모리에 살아있는 객체이면서 시스템 네이티브 I/O 연산의 대상이 되는 객체를 사용하는 것이 좋습니다.  
이처럼 off-heap을 사용해서 성능 향상이 보장되는 경우에만 off-heap을 사용하는 것이 좋습니다. off-heap으로 객체들을 옮겨 on-heap 사이즈를 줄이게 되면 Full GC로부터 발생할 수 있는 성능 저하 요인이 많이 저감됩니다.

## application.yml
```java
spring:
  cache:
    jcache:
      config: classpath:ehcache.xml
```
xml 파일을 config로 등록해줍니다.


## 캐시 적용
```java
@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    @Cacheable(
            value = "memberInfoResponseDto",
            key = "#memberId"            
    )
    @Transactional(readOnly = true)
    public MemberInfoResponseDto getMember(Long memberId){
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("member not found"));
        return MemberInfoResponseDto.from(member);
    }


    @CacheEvict(
            value = "memberInfoResponseDto",
            key = "#memberId"
    )
    @Transactional
    public void updateMemberName(Long memberId, String name){
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("member not found"));

        member.changeName(name);
    }
}
```
+ @Cacheable : 캐시가 있으면 캐시의 정보를 가져오고 없으면 등록합니다..
+ @CachePut : 무조건 캐시에 저장합니다. 캐시를 갱신하기 위해 실행을 강제합니다.
+ @CacheEvict : 캐시를 삭제합니다.

앞서 xml에 캐시 이름을 memberInfoResponseDto로 했으므로 value에 적어주어 xml 세팅을 적용시킵니다. key 값은 #를 사용해 파라미터 이름을 적어주어 구분해줍니다. 다른 옵션도 있는데 해당 설명은 [Spring Caching](https://backtony.github.io/spring/redis/2021-08-29-spring-redis-1/#6-spring-caching) 포스팅의 마지막 예시 부분을 참고하세요.(spring redis 포스팅이므로 redis config는 넘겨서 보시면 됩니다.)  

```java
@Getter
@AllArgsConstructor
public class MemberInfoResponseDto implements Serializable {

    private String name;
    private int age;

    public static MemberInfoResponseDto from(Member member){
        return new MemberInfoResponseDto(member.getName(),member.getAge());
    }
}
```
앞서 xml 설정에서 offheap에도 저장한다고 세팅해두었기 때문에 해당 객체는 Serializable을 구현해줘야 합니다.


## Listener
```java
@Slf4j
public class CacheEventLogger implements CacheEventListener<Object, Object> {

    public void onEvent(CacheEvent<? extends Object, ? extends Object> cacheEvent) {
        log.info("cache event logger message. getKey: {} / getOldValue: {} / getNewValue:{}", cacheEvent.getKey(), cacheEvent.getOldValue(), cacheEvent.getNewValue());
    }
}
```
앞서 xml에서 Listener를 등록했기에 CacheEventListener의 구현체로 구현해줍니다.  
xml에서 CREATED, EXPIRED에 발생하도록 했기 때문에 해당 작업이 있을 때마다 리스너가 호출됩니다.





<br><Br><br>

---

__참고__  

[Spring Boot Ehcache Example](https://www.baeldung.com/spring-boot-ehcache)    
[Spring 로컬 캐시 라이브러리 ehcache](https://medium.com/finda-tech/spring-%EB%A1%9C%EC%BB%AC-%EC%BA%90%EC%8B%9C-%EB%9D%BC%EC%9D%B4%EB%B8%8C%EB%9F%AC%EB%A6%AC-ehcache-4b5cba8697e0)    
[[Spring] ehCache2와 달라진 ehCache3 사용](https://chati.tistory.com/147)    
[공식 문서](https://www.ehcache.org/documentation/3.10/xml.html)    
[[Java] On-heap과 Off-heap](https://soft.plusblog.co.kr/163)    








