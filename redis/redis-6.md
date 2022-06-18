# Spring - Redis Expire Event 연동하기 

# 1. redis.conf 설정
---
## 이벤트 종류
+ K   Keyspace events, publish prefix "__keyspace@<db>__:".
+ E   Keyevent events, publish prefix "__keyevent@<db>__:".
+ g   공통 명령: del, expire, rename, ...
+ $   스트링(String) 명령
+ l   리스트(List) 명령
+ s   셋(Set) 명령
+ h   해시(Hash) 명령
+ z   소트 셋(Sorted set) 명령
+ x   만료(Expired) 이벤트 (키가 만료될 때마다 생성되는 이벤트)
+ e   퇴출(Evicted) 이벤트 (최대메모리 정책으로 키가 삭제될 때 생성되는 이벤트)
+ A   모든 이벤트(g$lshzxe), "AKE"로 지정하면 모든 이벤트를 받는다.

## expire 설정
기본적인 redis.conf 파일에는 key event 설정이 꺼져있기 때문에 켜줘야 합니다.  
redis.conf 파일에 아래 설정을 추가하면 됩니다. key가 ttl로 expire되었을 때 이벤트를 받기 위해서는 Ex로 설정하면 됩니다.  
```
notify-keyspace-events Ex
```
설정을 해두면 pattern Topic이 \__keyevent@*__:expired 으로 전달되게 됩니다.

<br>

# 2. Spring 연동
---
## 메시지 리스너 만들기
```java
@Slf4j
@Component
public class ExpirationListener implements MessageListener {

    @Override
    public void onMessage(Message message, byte[] pattern) {
        orderService.changeStatus(message.toString());
        System.out.println("########## onMessage pattern " + new String(pattern) + " | " + message.toString());
    }
}
```
MessageListener를 구현해서 onMessage를 override 해줍니다. onMessage에는 메시지를 받아서 수행할 동작을 정의해주면 됩니다.

## 리스터 등록
```java
@Slf4j
@Configuration
public class RedisConfig {

    private final String PATTERN = "__keyevent@*__:expired";

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory redisConnectionFactory, ExpirationListener expirationListener) {
        RedisMessageListenerContainer redisMessageListenerContainer = new RedisMessageListenerContainer();
        redisMessageListenerContainer.setConnectionFactory(redisConnectionFactory);
        redisMessageListenerContainer.addMessageListener(expirationListener, new PatternTopic(PATTERN));
        redisMessageListenerContainer.setErrorHandler(e -> log.error("There was an error in redis key expiration listener container", e));
        return redisMessageListenerContainer;
    }
}
```
앞서 만든 메시지 리스너와 PatternTopic까지 같이 등록해주면 ttl이 만료될 때 나오는 메시지를 받을 수 있습니다.  



<Br><Br>

__참고__  
<a href="http://redisgate.kr/redis/server/event_notification.php" target="_blank"> Redis Event Notification</a>   



