# Spring 비동기 FCM 알림서버 구현하기 (Feat.ApplicationEvent)

# 1. FCM이란?
---
+ FCM은 FireBase Cloud Messaging의 약자로, 무료로 메시지를 안정적으로 전송할 수 있는 교차 플랫폼 메시징 솔루션입니다.
+ 모든 사용자에게 알림 메시지를 전송할 수도 있고, 그룹을 지어 메시지를 전송할 수도 있습니다.
+ Firebase 서비스는 요금 정책에 따라 이용 범위가 다르지만, FCM 서비스는 무료로 사용이 가능합니다.

<br>

# 2. FCM을 사용하는 이유
기존에는 iOS, Andriod, Web 등의 플랫폼에서 Push 메시지를 보내기 위해서는 각 플랫폼 환경별로 개발해야하는 불편함이 있었습니다. 하지만 FCM은 교차 플랫폼 메시지 솔루션이기 때문에 플랫폼에 종속되지 않고 Push 메시지를 전송할 수 있습니다. 서버 단에서 직접 알림 로직을 만들어도 되지 않을까라고 생각할 수 있는데 예시를 통해 문제점을 설명하겠습니다.  
A가 애플리케이션을 이용해서 B에게 메시지를 보내면 A의 메시지는 해당 애플리케이션 서버를 거쳐서 B에게 도달하게 됩니다. A -> 애플리케이션 서버 -> B 형태입니다. 이 상황에서는 B가 실시간으로 메시지를 받기 위해서는 B는 서버에 계속 접속해 있어야 합니다. 이것을 실제로 구현한다면, 많은 배터리와 네트워크 사용으로 인해 문제가 생길 수 있습니다. 클라우드 메시징 서비스를 이용한다면, A -> 애플리케이션 서버 -> 클라우드 메시징 서버 -> B 형태로 클라우드 메시징 서버를 중간에 둠으로써, 사용자는 낮은 배터리와 네트워크 사용만으로도 메시지를 실시간으로 송수신 처리할 수 있게 됩니다.

<br>

# 3. FCM의 특징
+ 메시지 타입
    - 알림 메시지
    - 데이터 메시지
    - 휴대폰 푸시 알림 메시지는 알림 메시지를, 알림 메시지를 클릭 했을 때 앱 내 특정 페이지로 이동하거나 액션 발생은 데이터 메시지로 이루어지도록 보통 혼용해서 사용합니다.
+ 타겟팅
    - 단일 기기
    - 기기 그룹
    - 주제를 구독한 기기
+ 클라이언트 앱에서 메시지 전송
    - 앱 서버 -> 클라이언트 앱으로 다운 스트림 메시지 전송 가능
    - 클라이언트 앱 -> 앱 서버로 업 스트림 메시지 전송 가능

<br>

# 4. FCM 서버와 통신을 위한 방식
---
+ Firebase AdminSDK
    - Node.js, Java, Python, C#, Go 프로그래밍 언어 지원
    - 기기에서 주제 구독 및 구독 취소가 가능하고, 다양한 타켓 플랫폼에 맞는 메세지 페이로드 구성
    - HTTP v1 API를 기반으로 동작
    - FCM에서 가장 권장하는 옵션
+ HTTP v1 API
    - 가장 최신 프로토콜로 안전한 승인과 유연한 교차 플랫폼 메시징 기능 제공
+ HTTP 
    - legacy로 HTTp v1으로 이전을 권장
+ XMPP 서버 프로토콜
    - 업 스트림 메시징을 사용하기 위한 옵션

# 5. Spring과 연동
---
![그림2](https://github.com/backtony/blog-code/blob/master/spring/img/fcm/1/1-2.PNG?raw=true)

스프링에서 Firebase로 알림을 보내면 Firebase에서는 App으로 알림을 보내주는 과정으로 진행됩니다. 아래 작성한 코드는 로그인 과정에서 프론트에서 FcmToken을 주어 서버쪽 DB에 FCM 토큰을 저장하고 있는 상황을 가정하고 진행됩니다.
<br>


## Step 1 - Firebase 프로젝트 만들기
[[firebase 링크](https://console.firebase.google.com/u/0/){:target="_blank"}] 에서 프로젝트를 만들어 줍니다. 

## Step 2 - 비공개 키 생성하기
![그림3](https://github.com/backtony/blog-code/blob/master/spring/img/fcm/1/1-3.PNG?raw=true)  

메인 페이지 왼쪽에 있는 프로젝트 개요의 톱니바퀴를 클릭하고 프로젝트 설정을 클릭합니다.  

<br><br>

![그림4](https://github.com/backtony/blog-code/blob/master/spring/img/fcm/1/1-4.PNG?raw=true)  

상단의 서비스 계정을 클릭하고 하단의 새 비공개 키 생성을 클릭하여 키를 다운 받은 뒤에 main/resources 에 넣어줍니다.

## Step 3 - 의존성 추가와 설정

```yml
# build.gradle
implementation group: 'com.google.firebase', name: 'firebase-admin', version: '6.8.1'

# application.yml
fcm:
  key:
    path: gjgs-fcm.json
    scope: https://www.googleapis.com/auth/cloud-platform
```
build.gradle 과 application.yml에 세팅을 해줍니다. 여기서 path는 비공개키의 파일명을 적어주시면 됩니다. scope는 뒤에서 설명하겠습니다.  

## Step 4 - 로직 작성
```java
@Slf4j
@Service
public class NotificationServiceImpl implements NotificationService {

    @Value("${fcm.key.path}")
    private String FCM_PRIVATE_KEY_PATH;

    // 
    // 메시징만 권한 설정
    @Value("${fcm.key.scope}")
    private String fireBaseScope;

    // fcm 기본 설정 진행
    @PostConstruct
    public void init() {
        try {
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(
                            GoogleCredentials
                                    .fromStream(new ClassPathResource(FCM_PRIVATE_KEY_PATH).getInputStream())
                                    .createScoped(List.of(fireBaseScope)))
                    .build();
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                log.info("Firebase application has been initialized");
            }
        } catch (IOException e) {
            log.error(e.getMessage());
            // spring 뜰때 알림 서버가 잘 동작하지 않는 것이므로 바로 죽임
            throw new RuntimeException(e.getMessage());
        }
    }


    // 알림 보내기
    public void sendByTokenList(List<String> tokenList) {
        
        // 메시지 만들기
        List<Message> messages = tokenList.stream().map(token -> Message.builder()
                .putData("time", LocalDateTime.now().toString())
                .setNotification(new Notification("제목", "알림 내용"))
                .setToken(token)
                .build()).collect(Collectors.toList()); 

        // 요청에 대한 응답을 받을 response
        BatchResponse response;
        try {

            // 알림 발송
            response = FirebaseMessaging.getInstance().sendAll(messages);

            // 요청에 대한 응답 처리
            if (response.getFailureCount() > 0) {
                List<SendResponse> responses = response.getResponses();
                List<String> failedTokens = new ArrayList<>();

                for (int i = 0; i < responses.size(); i++) {
                    if (!responses.get(i).isSuccessful()) {
                        failedTokens.add(tokenList.get(i));
                    }
                }
                log.error("List of tokens are not valid FCM token : " + failedTokens);
            }
        } catch (FirebaseMessagingException e) {
            log.error("cannot send to memberList push message. error info : {}", e.getMessage());
        }
    }
}
```
코드를 하나씩 살펴보겠습니다.  
<br>

__init : Firebase에 Admin 계정을 인증하는 작업입니다.__  
+ 앞서 비공개 키를 생성하는 페이지를 보시면 Node.js, Java, Python, Go를 선택하면 코드 스니펫을 보여주는 곳이 있습니다. 해당 코드가 Firebase 에 Admin 계정으로 인증하는 기본적인 코드이고 이를 약간만 수정해서 사용하면 됩니다.
+ 기본 코드와 다른 점은 scope 를 지정한 것입니다. Admin 계정을 사용하지만 어떠한 권한만 사용 가능하도록 지정하는 것입니다. 자세한 내용은 [권한 설정 관련 문서 링크](https://developers.google.com/identity/protocols/oauth2/scopes#fcm) 를 확인하시면 됩니다.


<Br>

__Message 만들기__  
![그림5](https://github.com/backtony/blog-code/blob/master/spring/img/fcm/1/1-5.PNG?raw=true)  

+ Message 클래스는 앞서 의존성을 세팅해준 라이브러리에 들어있는 클래스이며 위와 같은 구성을 가지고 있습니다. 자세한 내용은 [링크](https://firebase.google.com/docs/reference/fcm/rest/v1/projects.messages)를 참고하시면 됩니다.
+ 해당 클래스에서 필요한 부분만 사용하여 메시지를 보내면 됩니다. 저는 현재 시간과 Firebase에서 제공하는 Notification 클래스에 제목과 알림 내용을 담아서 메시지를 만들었습니다. 토큰에는 알림을 받은 기기의 fcmToken을 넣어주시면 됩니다.

<br>

__FirebaseMessaging.getInstance().sendAll(messages)__  
+ 만든 메시지를 Firebase 서버로 보내는 작업입니다. FirebaseMessaging은 라이브러리에 의해 들어와 있습니다.
+ sned, snedAll, sendAsync, sendAllAsync 등의 메서드로 단일 기기, 여러 기기, 비동기로 메시지를 보낼 수 있습니다.
+ sendAll의 경우 BatchResponse로 받으면 모든 요청에 대한 응답값을 받아올 수 있습니다. token이 invalid한 경우 exception이 터지지 않고 reponse 내부에 exception이 담겨오기 때문에 catch에 잡히지 않아 try문 안에서 처리해야 합니다. 이외의 문제는 catch에서 잡히도록 했습니다.

<br>

# 5. 비동기 처리하기
---
분명 Firebase에서 SendAsync와 같이 비동기 기능을 제공해주지만, 제 경험상 프로젝트에서는 알림을 보냈으면 알림을 DB에 저장하는 로직이 필요했습니다. 즉, 알림을 보내고 저장하는 로직까지 비동기로 한번에 처리해야 했습니다. 소개팅 매칭 앱을 예로 들어보겠습니다. 처음에는 간단하게 매칭을 완료로직 이후 바로 @Aync 를 붙인 메서드를(알림을 보내고 저장하는 로직) 호출하면 되지 않을까 생각했지만 이렇게 된다면 결국 매칭을 담당하는 로직 안에 알림 로직까지 섞여있게 됩니다. 따라서 이에 대한 의존성을 줄이기 위해 __ApplicationEvent__ 를 사용하려고 합니다. ApplicationEvent를 사용하면 더이상 매칭 Service에서는 알림을 진행하는 NotificationService를 알 필요가 없으므로 느슨하게 결합(느슨한 결합의 원칙 Loose Coupling)되어 더 좋은 코드를 만들 수 있습니다. 이에 관해 자세한 내용은 [[링크](https://medium.com/@SlackBeck/spring-framework%EC%9D%98-applicationevent-%ED%99%9C%EC%9A%A9%EA%B8%B0-845fd2d29f32){:target="_blank"}]를 참고하시면 됩니다.  
이제 바로 코드를 작성해보겠습니다. 시나리오는 매칭이 완료되어 매칭 완료 대상에게 알림을 보내고 DB에 알림을 저장하는 시나리오이며, Member 엔티티가 FcmToken을 가지고 있습니다.
```java
// 매칭 서비스
@Service
@Transactional
@RequiredArgsConstructor
public class MatchingServiceImpl implements MatchingService {

    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void matching(String accessToken, MatchingForm matchingForm) {

            // 매칭 관련 로직 생략 //

            // 매칭 완료 //

            // ------ 알림 보내기 시작 ------- //            
            eventPublisher.publishEvent(new MatchingCompleteEvent(memberList));    

    }
}

// publishEvent에서 인자로 사용할 클래스 -> 리스너에게 전달되는 클래스
@Getter
@RequiredArgsConstructor
public class MatchingCompleteEvent {
    private final List<Member> MemberList;
}
```
Spring Framework에서 제공하는 ApplicationEvent를 주입받아 publishEvent의 인자로 추후 로직에 사용할 정보들을 담아줍니다.  
<br>

```java
// EventListener
@Component
@Async("matching")
@Transactional
@RequiredArgsConstructor
public class MatchingEventListener {

    private final NotificationService notificationService;
    private final NotificationJdbcRepository notificationJdbcRepository;

    @EventListener
    public void handleMatchingCompleteEvent(MatchingCompleteEvent matchingCompleteEvent){

        // 알림 보낼 멤버 목록
        List<Member> memberList = matchingCompleteEvent.getMemberList();

        // 로그아웃 안한 회원의 fcmToken 뽑기
        // 로그아웃한 회원들의 fcmToken 필드는 "" 공백입니다.
        List<String> fcmTokenList = memberList
                .stream()
                .filter(m -> !m.getFcmToken().isBlank())
                .map(m -> m.getFcmToken()).collect(toList());

        // 로그아웃 안한 대상에게 알림 보내기
        if (fcmTokenList.size()!=0){
            notificationService.sendByTokenList(fcmTokenList);
        }

        // 알림 엔티티 만드는 로직 생략 //
        
        // 알림 벌크 저장
        notificationJdbcRepository.insertNotificationWithTeamId(notificationList);
    }
}
```
+ @Async("matching")는 해당 클래스를 비동기작업으로 동작하게 만들어 줍니다.
+ MatchingEventListener 클래스는 매칭 서비스에서 publishEvent 로 인한 이벤트를 받아 로직을 수행할 클래스입니다. 메서드를 하나 만들고 @EventListner 애노테이션을 붙인 뒤 메서드의 인자로 publishEvent 호출할 때 넣어줬던 인자와 일치시켜주면 publishEvent(특정 인자) 호출 시 @EventListner 애노테이션이 붙어있고 같은 인자를 같는 Listner 메서드가 호출됩니다.
+ handleMatchingCompleteEvent 의 메서드는 matchingCompleteEvent에서 알림을 보낼 멤버목록을 가져와 fcmToken을 추출하여 알림 요청을 보내고 알림 엔티티를 만들어 DB에 저장하는 로직입니다.


<br>

마지막으로 Async 관련 설정을 해줘야합니다.
```java
@Slf4j
@EnableAsync
@Configuration
public class AsyncConfig {

    @Bean(name = "matching") 
    public Executor threadPoolExecutor(){
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        int processors = Runtime.getRuntime().availableProcessors();
        log.info("processors count {}",processors);
        executor.setThreadNamePrefix("MatchingAsync-"); // thread 이름 설정
        executor.setCorePoolSize(processors); // 기본 스레드 수
        executor.setMaxPoolSize(processors*2); // 최대 스레드 개수
        executor.setQueueCapacity(50); // 최대 큐 수
        executor.setKeepAliveSeconds(60); // maxpoolsize로 인해 덤으로 더 돌아다니는 튜브는 60초 후에 수거해서 정리
        executor.initialize(); // 초기화후 반환
        return executor;
    }
}
```
+ @EnableAsync : @Async 를 사용할 수 있도록 합니다.
+ @Bean(name = "matching") : 빈으로 등록하고 사용할 때 @Async("matching)으로 사용하면 됩니다.
+ Pool 생성 과정
    - 1. 기본 thread(TASK_CORE_POOL_SIZE) 수까지 순차적으로 쌓입니다.
    - 2. 기본 thread(TASK_CORE_POOL_SIZE) 크기가 넘어 설 경우 queue에 쌓입니다.
    - 3. 큐에 최대치까지 쌓이면 TASK_MAX_POOL_SIZE까지 순차적으로 한개씩 증가시킵니다.


<br><Br>

이렇게 비동기 작업까지 끝냈습니다. 여기까지 글을 마치겠습니다. 감사합니다 :)