# JPA - Lock


## 애그리거트와 트랜잭션
한 주문 애그리거트에 대해 운영자는 배송 상태로 변경할 때 사용자는 배송지 주소를 변경하면 어떻게 될까요?  

![그림1](https://github.com/backtony/blog-code/blob/master/jpa/img/11/1/1.PNG?raw=true)  

위 그림은 운영자와 고객이 동시에 한 주문 애그리거트를 수정하는 과정을 보여줍니다. 트랜잭션마다 리포지토리는 새로운 애그리거트 객체를 생성하므로 운영자 스레드가 고객 스레드는 같은 주문 애그리거트를 나타내는 다른 객체를 구하게 됩니다.  
운영자 스레드가 고객 스레드는 개념적으로 동일한 애그리거트지만 물리적으로 서로 다른 애그리거트 객체를 사용합니다. 때문에 운영자 스레드가 주문 애그리거트 객체를 배송 상태로 변경하더라도 고객 스레드가 사용하는 주문 애그리거트 객체에는 영향을 주지 않습니다. 고객 스레드 입장에서 주문 애그리거트 객체는 아직 배송 상태 전이므로 배송지 정보를 변경할 수 있습니다.  
이 상황에서 두 스레드는 각각 트랜잭션을 커밋할 때 수정한 내용을 DB에 반영합니다. 이 시점에 배송 상태로 바뀌고 배송지 정보도 바뀝니다. 이 순서의 문제점은 운영자는 기존 배송지 정보를 이용해서 배송 상태로 변경했는데 그 사이 고객은 배송지 정보를 변경했다는 점입니다. 즉, 애그리거트의 일관성이 깨집니다.  
이를 방지하기 위해서는 다음 두 가지 중 하나를 해야 합니다.  
+ 운영자가 배송지 정보를 조회하고 상태를 변경하는 동안, 고객이 애그리거트를 수정하지 못하게 막는다.
+ 운영자가 배송지 정보를 조회한 이후 고객이 정보를 변경하면, 운영자가 애그리거트를 다시 조회한 뒤 수정하도록 한다.  

이 두 가지는 애그리거트 자체의 트랜잭션과 관련이 있습니다. DBMS가 지원하는 트랜잭션과 함께 애그리거트를 위한 추가적인 트랜잭션 처리 기법이 필요합니다. 애그리거트에 대해 사용할 수 있는 대표적인 트랜잭션 처리 방식에는 선점(Pessimistic) 잠금과 비선점(Optimistic) 잠금의 두 가지 방식이 있습니다.  

## JPA 락 옵션
JPA가 제공하는 락 옵션과 용도를 알아봅시다.  

락 모드|타입|설명
---|---|---
낙관적 락|OPTIMISTIC|낙관적 락을 사용한다.
낙관적 락|OPTIMISTIC_FORCE_INCREMENT|낙관적 락 + 버전정보를 강제로 증가한다.
비관적 락|PESSIMISTIC_READ|비관적 락, 읽기 락을 사용한다.
비관적 락|PESSIMISTIC_WRITE|비관적 락, 쓰기 락을 사용한다.
비관적 락|PESSIMISTIC_FORCE_INCREMENT|비관적 락 + 버전 정보를 강제로 증가한다.
기타|NONE|락을 걸지 않는다.
기타|READ|JPA 1.0 호환 기능으로 OPTIMISTIC과 동일
기타|WRITE|JPA 1.0 호환 기능으로 OPTIMISTIC_FORCE_INCREMENT와 동일

### 낙관적 락
JPA가 제공하는 낙관적 락은 @Version을 사용합니다. 따라서 낙관적 락을 사용하려면 버전이 있어야합니다. 낙관적 락은 트랜잭션을 커밋하는 시점에 충돌을 알 수 있다는 특징이 있습니다.  
낙관적 락에서 발생하는 예외는 다음과 같습니다.
+ OptimisticLockException(JPA 예외)
+ StaleObjectStateException(하이버네이트 예외)
+ ObjectOptimisticLockingFailureException(스프링 예외 추상화)

#### NONE
락 옵션을 적용하지 않아도 엔티티에 @Version이 적용된 필드만 있으면 낙관적 락이 적용됩니다.  
+ 용도: 조회한 엔티티를 수정할 때 다른 트랜잭션에 의해 변경(삭제)되지 않아야 할때, 조회 시점부터 수정 시점까지 보장한다.
+ 동작: 엔티티를 수정할 때 버전을 체크하면서 버전을 증가한다.(UPDATE 쿼리 사용) 이때 데이터베이스의 버전 값이 현재 버전이 아니면 예외가 발생한다.
+ 이점: 두 번의 갱신 분실 문제를 예방한다.

#### OPTIMISTIC
@Version만 적용했을 때는 엔티티를 수정해야 버전을 체크하지만 이 옵션을 추가하면 엔티티를 조회만 해도 버전을 체크합니다. 즉, 한 번 조회한 엔티티는 트랜잭션을 종료할 때까지 다른 트랜잭션에서 변경하지 않음을 보장합니다.
+ 용도: 조회한 엔티티는 트랜잭션이 끝날 때까지 다른 트랜잭션에 의해 변경되지 않아야 한다. 조회 시점부터 트랜잭션이 끝날 때까지 조회한 엔티티가 변경되지 않음을 보장한다.
+ 동작: 트랜잭션을 커밋할 때 버전 정보를 조회해서(SELECT 쿼리) 현재 엔티티의 버전과 같은지 검증한다. 만약 같지 않으면 예외가 발생한다.
+ 이점: OPTIMISTIC 옵션은 DIRTY READ와 NONE-REPEATABLE READ를 방지한다.

#### OPTIMISTIC_FORCE_INCREMENT
낙관적 락을 사용하면서 버전 정보를 강제로 증가시킵니다. 
+ 용도: 논리적인 단위의 엔티티 묶음을 관리할 수 있다. 예를 들어 게시물과 첨부파일이 일대다, 다대일의 양방향 연관관계이고 첨부파일이 연관관계의 주인이다. 게시물을 수정하는 데 단순히 첨부파일만 추가하면 게시물의 버전은 증가하지 않는다. 해당 게시물은 물리적으로는 변경되지 않았지만, 논리적으로는 변경되었다. 이때 게시물의 버전도 강제로 증가하려면 이 옵션을 사용한다.
+ 동작: 엔티티를 수정하지 않아도 트랜잭션을 커밋할 때 UPDATE 쿼리를 사용해서 버전 정보를 강제로 증가시킨다. 이때 데이터베이스의 버전이 엔티티의 버전과 다르면 예외가 발생한다. 추가로 엔티티를 수정하면 수정 시 버전 UPDATE가 발생한다. 따라서 총 2번의 버전 증가가 나타날 수 있다.
+ 이점: 강제로 버전을 증가해서 논리적인 단위의 엔티티 묶음을 버전 관리할 수 있다. 

길게 풀어썼지만, 애그리거트 루트를 수정하지 않았지만 애그리거트 루트가 관리하는 엔티티를 수정했을 때 애그리거트 루트도 버전을 강제로 증가시킨다는 의미입니다.  

### 비관적 락
JPA가 제공하는 비관적 락은 데이터베이스 트랜잭션 락 메커니즘에 의존하는 방식입니다. 주로 SQL 쿼리에 select for update 구문을 사용하면서 시작하고 버전 정보는 사용하지 않습니다. 비관적 락은 주로 PESSIMISTIC_WRITE 모드를 사용합니다.  
비관적 락은 다음과 같은 특징이 있습니다.

+ 엔티티가 아닌 스칼라 타입을 조회할 때도 사용할 수 있다.
+ 데이터를 수정하는 즉시 트랜잭션 충돌을 감지할 수 있다.

비관적 락에서 발생하는 예외는 다음과 같습니다.
+ PessimisticLockException(JPA예외) 
+ PessimisticLockingFailureException(스프링 예외 추상화)

#### PESSIMICTIC_WRITE
비관적 락이라면 일반적으로 이 옵션을 말합니다. 데이터베이스 쓰기 락을 걸 때 사용합니다.

+ 용도: 데이터베이스에 쓰기 락을 건다.
+ 동작: 데이터베이스 select for update를 사용해서 락을 건다.
+ 이점: NON-REPEATABLE READ를 방지한다. 락이 걸린 로우는 다른 트랜잭션이 수정할 수 없다.

#### PESSIMICTIC_READ
데이터를 반복 읽기만 하고 수정하지 않는 용도로 락을 걸때 사용합니다. 일반적으로 잘 사용하지 않습니다. 데이터베이스 대부분은 방언에 의해 PESSIMISTIC_WRITE로 동작합니다.
+ MySQL: lock in share mode
+ PostgreSQL: for share

#### PESSIMISTIC_FORCE_INCREMENT
비관적 락중 유일하게 버전 정보를 사용합니다. 비관적 락이지만 버전 정보를 강제로 증가시킵니다. 하이버네이트는 nowait를 지원하는 데이터베이스에 대해서 for update nowait옵션을 적용합니다.
+ 오라클: for update nowait
+ postgreSQL: for update nowait
+ nowait을 지원하지 않으면 for update가 사용됩니다.

#### 타임 아웃
비관적 락을 사용하면 락을 획득할 때까지 트랜잭션이 대기하게 되는데 무한정 기다릴 수 없으므로 타임아웃 시간을 줄 수 있습니다. Spring DATA JPA에서는 다음과 같이 사용합니다.
```java
public interface UserRepository extends JpaRepository<User,Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "javax.persistence.lock.timeout", value ="10000")})
    Optional<User> findForUpdateById(Long id);
}
```




## 선점 잠금
선점 잠금은 먼저 애그리거트를 구한 스레드가 애그리거트 사용이 끝날 때까지 다른 스레드가 해당 애그리거트를 수정하지 못하게 막는 방식입니다.  

![그림2](https://github.com/backtony/blog-code/blob/master/jpa/img/11/1/2.PNG?raw=true)  

운영자 스레드가 선점 잠금 방식으로 애그리거트를 구한 뒤 이어서 고객 스레드가 같은 애그리거트를 구하고 있습니다. 이때 고객 스레드는 운영자 스레드가 애그리거트에 대한 잠금을 해제할 때까지 블로킹됩니다. 운영자 스레드가 애그리거트를 수정하고 트랜잭션을 커밋하면 잠금이 해제되고 이 순간 대기하고 있던 고객 스레드가 애그리거트에 접근할 수 있게 됩니다. 운영자 스레드가 트랜잭션을 커밋한 뒤에 고객 스레드가 애그리거트를 구하게 되므로 고객 스레드는 운영자 스레드가 수정한 애그리거트의 내용을 보게 됩니다. 이를 통해 한 스레드가 애그리거트를 구하고 수정하는 동안 다른 스레드가 수정할 수 없으므로 동시에 애그리거트를 수정할 때 발생하는 데이터 충돌 문제를 해소할 수 있습니다.  
<br>

선점 잠금은 보통 DBMS가 제공하는 행단위 잠금을 사용해서 구현합니다. 오라클을 비롯한 다수의 DBMS가 for update와 같은 쿼리를 사용해서 특정 레코드에 한 커넥션만 접근할 수 있는 잠금장치를 제공합니다.  
JPA 프로바이더와 DBMS에 따라 잠금 모드 구현이 다릅니다. 하이버네이트의 경우 PESSIMISTIC_WRITE를 잠금 모드로 사용하면 for update 쿼리를 이용해서 선점 잠금을 구현합니다.  
Spring Data JPA는 @LOCK 애너테이션을 사용해서 잠금 모드를 지정합니다.  
```java
public interface UserRepository extends JpaRepository<User,Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<User> findForUpdateById(Long id);
}
```

### 교착상태
선점 잠금 기능을 사용할 때는 잠금 순서에 따른 교착 상태가 발생하지 않도록 주의해야 합니다. 예를 들어, 다음과 같은 순서로 두 스레드가 잠금을 시도한다고 해봅시다.

1. 스레드1: A 애그리거트에 대한 선점 잠금 구함
2. 스레드2: B 애그리거트에 대한 선점 잠금 구함
3. 스레드1: B 애그리거트에 대한 선점 잠금 시도
4. 스레드2: A 애그리거트에 대한 선점 잠금 시도

이 순서에 따르면 스레드1은 영원히 B 애그리거트에 대한 선점 잠금을 구할 수 없게 되면서 교착 상태에 빠지게 됩니다.  
<br>

선점 잠금에 따른 교착 상태는 상대적으로 사용자 수가 많을 때 발생할 가능성이 높고, 사용자 수가 많아지면 교착 상태에 빠지는 스레드가 많아지고 시스템이 죽게 됩니다. 이 문제가 발생하지 않도록 하려면 잠금을 구할 때 최대 대기 시간을 지정해야 합니다. Spring Data JPA에서 선점 잠금을 시도할 때 최대 대기 기간을 지정하려면 힌트를 사용해야 합니다. 
```java
public interface UserRepository extends JpaRepository<User,Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "javax.persistence.lock.timeout", value ="10000")})
    Optional<User> findForUpdateById(Long id);
}
```

## 비선점 잠금
선점 잠금이 강력해 보이긴 하지만 선점 잠금으로 모든 트랜잭션 충돌 문제가 해결되는 것은 아닙니다.  

![그림3](https://github.com/backtony/blog-code/blob/master/jpa/img/11/1/3.PNG?raw=true)  


1. 운영자는 배송을 위해 주문 정보를 조회한다. 시스템은 정보를 제공한다.
2. 고객이 배송지 변경을 위해 변경 폼을 요청한다. 시스템은 변경 폼을 제공한다.
3. 고객이 새로운 배송지를 입력하고 폼을 전송해서 배송지를 변경한다.
4. 운영자가 1번에서 조회한 주문 정보를 기준으로 배송지를 정하고 배송 상태 변경을 요청한다.

여기서 문제는 운영자가 배송지 정보를 조회하고 배송 상태로 변경하는 사이에 고객이 배송지를 변경한다는 것입니다. 운영자는 고객이 변경하기 전 배송지 정보를 이용하여 배송 준비를 한 뒤에 배송 상태를 변경하게 됩니다. 즉, 배송 상태 변경 전에 배송지를 한 번 더 확인하지 않으면 엉뚱한 곳으로 물건을 보내게 됩니다.  
이 문제는 선점 잠금 방식으로는 해결할 수 없습니다. 이때 필요한 것이 비선점 잠금입니다. 비선점 잠금은 동시에 접근하는 것을 막는 대신 __변경한 데이터를 실제 DBMS에 반영하는 시점에 변경 가능 여부를 확인하는 방식__ 입니다.  
비선점 잠금을 구현하려면 애그리거트 버전으로 사용할 숫자 타입 프로퍼티를 추가해야 합니다. 애그리거트를 수정할 때마다 버전으로 사용할 프로퍼티 값이 1씩 증가하는데 이때 다음과 같은 쿼리가 사용됩니다.
```sql
UPDATE aggtable SET version = version + 1, colx = ?, coly = ?
WHERE aggid = ? and version = 현재 버젼
```
이 쿼리는 수정할 애그리거트와 매핑되는 테이블의 버전 값이 현재 애그리거트의 버전과 동일한 경우에만 데이터를 수정합니다. 그리고 수정에 성공하면 버전 값을 1 증가시킵니다. 다른 트랜잭션이 먼저 데이터를 수정해서 버전 값이 바뀌면 데이터 수정에 실패하게 됩니다. 이를 그림으로 표현하면 아래와 같습니다.  


![그림4](https://github.com/backtony/blog-code/blob/master/jpa/img/11/1/4.PNG?raw=true)  

### 간단한 비선점 잠금 사용법
앞선 예시 상황을 코드로 보기 전에 우선 가장 간단하게 비선점 잠금을 사용하는 법을 살펴봅시다.  
JPA는 버전을 이용한 비선점 잠금 기능을 지원합니다. 버전으로 사용할 필드에 @Version 애너테이션을 붙이고 매핑되는 테이블에 버전을 저장할 컬럼을 추가하면 됩니다.
```java
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private long version;

    private String name;

    public Team(String name) {
        this.name = name;
    }
}
```
@Version으로 추가한 버전 관리 필드는 JPA가 직접 관리하므로 개발자가 임의로 수정하면 안 됩니다.(벌크 연산 제외)  

<Br>

비선점 잠금을 위한 쿼리를 실행할 때 쿼리 실행 경과로 수정된 행의 개수가 0이면 이미 누군가 앞서 데이터를 수정한 것입니다. 이는 트랜잭션이 충돌한 것이므로 트랜잭션 종료 시점에 예외가 발생합니다. 
```java
@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;

    @Transactional
    public void sleepAndChangeStatusPLAY(Long teamId){
        Team team = teamRepository.findById(teamId).orElseThrow(() -> new RuntimeException("team not found"));
        try {
            Thread.sleep(10_000);
            team.play();
        } catch (Exception e){
        }
    }

    @Transactional
    public void changeStatusCANCEL(Long teamId){
        Team team = teamRepository.findById(teamId).orElseThrow(() -> new RuntimeException("team not found"));
        team.cancel();
    }
}
```
다음과 같은 시나리오로 동작하면 트랜잭션이 충돌하면서 OptimisticLockingFailureException이 발생합니다.

1. sleepAndChangeStatusPLAY 가 호출되면 version값이 1인 team 엔티티를 가져와서 10초 동안 쉽니다.
2. 10초 동안 쉬는 사이에 changeStatusCANCEL 메서드에서 같은 team 엔티티(version 값이 1인)를 가져와서 상태를 업데이트하면서 version을 2로 업데이트합니다.
3. sleepAndChangeStatusPLAY 메서드에서 10초가 지나고 team 엔티의 상태를 업데이트하고 version을 2로 올리면서 커밋합니다.
4. version이 이미 2이기 때문에 충돌이 발생하면서 OptimisticLockingFailureException 예외가 발생합니다.  

쿼리에 아무런 옵션도 주지 않았지만 @Version 컬럼이 엔티티에 있기 때문에 낙관적인 락으로 동작합니다. 아무런 옵션도 주지 않아 NONE 옵션으로 동작하여 조회시에는 version이 업데이트 되지 않고 아무런 수정이 없어도 업데이트 되지 않습니다.  
트랜잭션이 종료되면서 충돌 여부를 확인할 수 있기 때문에 presentation 계층에서 예외를 처리해야 합니다.
```java
@Slf4j
@RestController
@RequiredArgsConstructor
public class TeamRestController {

    private final TeamService teamService;

    @GetMapping("sleep/{teamId}")
    public void sleep(@PathVariable Long teamId){
        try {
            teamService.sleepAndChangeStatusPLAY(teamId);
        } catch (OptimisticLockingFailureException ex){
            log.error("{} : {}","낙관적락 예외 발생", "동작 하는 과정에서 누가 중간에 수정했다.");
        }
    }
}
```

### 예시 상황 비선점 잠금 사용법
이제 간단한 비선점 사용 방법을 알아봤으니 앞서 설명한 예시 상황을 비선점 잠금으로 해결해 봅시다.  

![그림3](https://github.com/backtony/blog-code/blob/master/jpa/img/11/1/3.PNG?raw=true)  


위 상황을 해결하기 위해서는 운영자가 주문 정보를 조회하는 시점에 응답값으로 version 값을 같이 반환해야 합니다. 그리고 4번의 배송 상태 변경 요청 시에 조회 시점에 응답값으로 받았던 version 컬럼도 함께 요청 값으로 보내줍니다.

```java
@Service
@Transactional
@RequiredArgsConstructor
public class OrderService {

    private OrderRepository orderRepository;

    @Transactional
    public void startShipping(StartShippingRequestDto req) {
        Order order = orderRepository.findById(req.getOrderId())
                .orElseThrow(() -> new RuntimeException("order not found"));
        if (order.matchVersion(req.getVersion())) { // 요청으로 받은 version값과 DB의 version 값 비교
            throw new VersionConflictException(); // 불일치 하는 경우 예외 발생
        }
        order.startShipping();
    }
}
```
```java
@Slf4j
@RestController
@RequiredArgsConstructor
public class OrderRestController {

    private final OrderService orderService;

    @PostMapping
    public void start(@RequestBody StartShippingRequest startShippingRequest){
        try {
            orderService.startShipping(startShippingRequest.toStartShippingRequestDto());
        } catch (OptimisticLockingFailureException | VersionConflictException e){
            log.error("{}","낙관적락 예외 발생");
        }
    }
}
```
VersionConflictException는 이미 누군가 애그리거트를 수정했다는 것을 의미하고, OptimisticLockingFailureException은 누군가 거의 동시에 애그리거트를 수정했다는 것을 의미합니다.  

### 강제 버전 증가
애그리거트에 애그리거트 루트 외에 다른 엔티티가 존재하는데 기능 실행 도중 루트가 아닌 다른 엔티티의 값만 변경된다고 합시다. 이 경우 JPA는 루트 엔티티의 버전 값을 증가시키지 않습니다. 연관된 엔티티의 값이 변경된다고 해도 루트 엔티티 자체의 값을 바뀌는 것이 없기 때문입니다.  
이런 JPA 특징은 애그리거트 관점에서 보면 문제가 됩니다. 비록 루트 엔티티의 값이 바뀌지 않았더라도 애그리거트의 구성 요소 중 일부 값이 바뀌면 논리적으로 그 애그리거트는 바뀐 것입니다. 따라서 애그리거트 내에 어떤 구성요소의 상태가 바뀌면 루트 애그리거트의 버전값이 증가해야 비선점 잠금이 올바르게 동작한다고 할 수 있습니다.  
JPA는 이런 문제를 처리할 수 있도록 엔티티를 구할 때 강제로 버전 값을 증가시키는 잠금 모드를 지원합니다.
```java
public interface OrderRepository extends JpaRepository<Order,Long> {
    
    @Lock(LockModeType.OPTIMISTIC_FORCE_INCREMENT)
    Optional<Order> findOptimisticLockModeById(Long orderId);
}
```
위 쿼리를 사용할 경우 엔티티의 상태가 변경되었는지에 관계없이 트랜잭션 종료 시점에 버전 값이 증가됩니다. 또한, 루트 엔티티가 아닌 다른 엔티티나 밸류가 변경되더라도 버전 값이 증가되므로 비선점 잠금 기능을 안전하게 적용할 수 있습니다.  





<Br><Br>

__참고__  
<a href="http://www.kyobobook.co.kr/product/detailViewKor.laf?ejkGb=KOR&mallGb=KOR&barcode=9788960777330&orderClick=LAG&Kc=" target="_blank"> 자바 ORM 표준 JPA 프로그래밍</a>   
<a href="http://www.kyobobook.co.kr/product/detailViewKor.laf?ejkGb=KOR&mallGb=KOR&barcode=9791162245385&orderClick=LEa&Kc=" target="_blank"> 도메인 주도 개발 시작하기: DDD 핵심 개념 정리부터 구현까지</a>   

