
## 1. 트랜잭션이란?
---
트랜잭션이란 __데이터베이스의 상태를 변환시키는 하나의 논리적 기능을 수행하기 위한 작업의 단위 또는 한꺼번에 모두 수행되어야 할 일련의 연산들__ 을 의미합니다.  
하나의 트랜잭션이 모두 실행되었을 때를 __Commit__ 이라고 합니다.  
일종의 확인 도장으로 트랜잭션에 묶인 모든 쿼리가 성공되어 결과가 실제 DB에 반영하는 것입니다.  
하나의 트랜잭션이 모두 실행되지 않도록 하는 __RollBack__ 이라고 합니다.  
쿼리 실행결과를 취소하고 DB를 트랜잭션 이전 상태로 되돌리는 것입니다.  
<br>

## 2. 트랜잭션의 특징
---
트랜잭션이 안전하게 수행된다는 것을 보장하기 위한 성질을 __ACID__ 라고 합니다.  
+ A : Atomicity(원자성)
    - 트랜잭션은 DB에 모두 반영되거나, 전혀 반영되지 않아야 한다.
    - 완료되지 않은 트랜잭션의 중간 상태를 DB에 반영해서는 안 된다.
+ C : Consistency(일관성)
    - 트랜잭션이 성공적으로 완료하면 일관성 있는 데이터베이스 상태로 유지한다.
+ I : Isolation(독립성)
    - 둘 이상의 트랜잭션이 동시 실행되고 있을 때, 어떤 트랜잭션도 다른 트랜잭션 연산에 끼어들 수 없다.
    - 각각의 트랜잭션은 서로 간섭 없이 독립적으로 이루어져야 한다.
+ D : Durability(지속성)
    - 트랜잭션이 완료되었으면 결과는 영구히 반영되어야 한다.

ACID 성질은 트랜잭션이 이론적으로 보장해야하는 성질이고, 실제로는 성능 문제를 위해 격리 수준을 조정하여 성질 보장이 완화되곤 합니다.  

## 3. 트랜잭션 격리 수준
---
![그림1](https://github.com/backtony/blog-code/blob/master/interview/transaction/img/transaction-1.PNG?raw=true)  
격리 수준이란 동시에 DB에 접근할 때 그 접근을 어떻게 제어할지에 대한 설정을 의미합니다.  
<br>

### READ-UNCOMMITTED
__커밋 전의 트랜잭션의 데이터 변경 내용을 다른 트랜잭션이 읽은 것을 허용합니다.__  
![그림2](https://github.com/backtony/blog-code/blob/master/interview/transaction/img/transaction-2.PNG?raw=true)  
트랜잭션 A에서 데이터를 조회하여 변경하고 아직 COMMIT하지 않은 상태에서 트랜잭션 B가 같은 데이터를 읽게 되면 변경되었지만 아직 커밋되지 않은 데이터를 읽어오게 됩니다.  
이렇게 __변경 사항이 반영되지 않은 값을 다른 트랜잭션에서 읽는 현상을 Dirty Read__ 라고 합니다.  
즉, READ-UNCOMMITTED 수준에서는 Dirty Read의 문제가 발생합니다.(뒤에서 언급되는 Nonrepeatable read, Phantom read도 발생합니다.)  

### READ-COMMITTED
__커밋이 완료된 트랜잭션의 변경사항만 다른 트랜잭션에서 조회하는 것을 허용합니다.__  
![그림3](https://github.com/backtony/blog-code/blob/master/interview/transaction/img/transaction-3.PNG?raw=true)  
트랜잭션 A가 데이터를 조회하고 변경한 뒤 커밋하기 전 상태에서 트랜잭션 B가 같은 데이터를 조회하면, 변경하기 전 값을 읽어오게 됩니다.  
트랜잭션 A가 커밋한 이후에 트랜잭션 B에서 데이터를 조회하면 커밋된 데이터를 읽어오게 됩니다.  
트랜잭션 B에서는 한 트랜잭션 내에서 같은 쿼리를 두 번 수행했는데 그 사이에 트랜잭션 A가 값을 수정 또는 삭제함으로써 두 쿼리의 결과가 다르게 나타나는 비일관성 현상이 발생했습니다.  
이렇게 __한 트랜잭션 내에서 같은 쿼리를 두 번 수행할 때 그 사이에 다른 트랜잭션이 값을 수정 또는 삭제함으로써 두 쿼리의 결과가 상이하게 나타나는 비 일관성 발생하는 현상을 Nonrepeatable read__ 라고 합니다.  
즉, READ-COMMITTED는 Dirty Read 현상을 해결했지만, Nonrepeatable read 현상이 발생합니다.(뒤에서 언급되는 Phantom read도 발생합니다.)  

### REPEATABLE READ
__커밋이 완료된 데이터만 읽을 수 있으며, 트랜잭션 범위 내에서 조회한 내용이 항상 동일함을 보장합니다.__  
한 트랜잭션이 조회한 데이터는 트랜잭션이 종료될 때까지 다른 트랜잭션이 변경 및 삭제하는 작업을 막아 한 번 조회한 데이터는 반복적으로 조회해도 같은 값을 반환합니다.  
![그림4](https://github.com/backtony/blog-code/blob/master/interview/transaction/img/transaction-4.PNG?raw=true)  
트랜잭션 A에서 데이터를 읽고 수정한 뒤 커밋하기 전 상태에서 트랜잭션 B가 같은 데이터를 읽으면 수정하기 전 상태의 데이터를 읽게 됩니다.  
트랜잭션 A가 커밋을 했다고 하더라도, 트랜잭션 B에서는 변경된 값이 아니라 이전에 조회했던 결과값을 그대로 사용합니다.  
<br><Br>

![그림5](https://github.com/backtony/blog-code/blob/master/interview/transaction/img/transaction-5.PNG?raw=true)  
REPEATABLE READ에서는 Phantom read현상이 발생할 수 있습니다.  
한 트랜잭션 안에서 __일정범위의 레코드를 두번 이상 읽을 때, 첫 번재 쿼리에서 없던 유령 레코드가 두번째 쿼리에서 나타나는 현상을 __Phantom read__ 라고 합니다.  
Nonrepeatable read와 약간 혼동될 수 있는데 Phantom read는 Nonrepeatable read의 한 종류로, 해당 쿼리로 읽히는 데이터의 들어가는 행이 새로 생기거나 없어진것 이라고 생각하면 됩니다.  

### SERIALIZABLE
__한 트랜잭션에서 사용하는 데이터를 다른 트랜잭션에서 접근 불가능하도록 하는 것입니다.__  
가장 엄격한 수준으로 앞선 문제들을 방지하지만 동시성 처리 성능이 급격히 하락합니다.  


### 정리
![그림6](https://github.com/backtony/blog-code/blob/master/interview/transaction/img/transaction-6.PNG?raw=true)  

<br>

## 4. 트랜잭션이 복구하는 방식
---
계좌 이체 과정을 통해 예시를 들어보겠습니다.  
1. 구매자의 계좌에서 돈이 출금된다.
2. 판매자의 계좌에서 돈이 입금된다.

```sql
UPDATE accounts
SET balance = balance - 10000
WHERE user = '구매자'

UPDATE accounts
SET balance = balance + 10000
WHERE user = '판매자'
```
만약 위의 수행 과정 중에 오류가 발생하면 다음과 같은 오류가 예상됩니다.  
+ 구매자의 계좌에 돈이 출금된 뒤 DB가 다운
+ 구매자의 계좌에서 돈이 출금되지 않았는데, 판매자에게 돈이 입금
+ 출금도 입금도 X

<br>

위와 같은 어중간한 상태로 두면 안되기 때문에 __초기상태로 되돌리는게__ 중요합니다.  
이를 해주는 역할을 하는 것이 __트랜잭션__ 입니다.  
![그림8](https://github.com/backtony/blog-code/blob/master/interview/transaction/img/transaction-8.PNG?raw=true)  
```sql
-- 실행되는 쿼리
BEGIN TRAN
UPDATE accounts
SET balance = balance - 10000
WHERE user = '구매자';
UPDATE accounts
SET balance = balance + 10000
WHERE user = '판매자';
COMMIT TRAN
```
1. 구매자의 계좌에서 인출하는 쿼리를 요청합니다.
2. 쿼리 처리기를 통과합니다.
3. 데이터 캐시에 데이터가 있는지 확인합니다.
4. 데이터 파일에서 데이터를 가져와 데이터 캐시로 로드합니다.
5. 로그에 기록합니다.
    + ReDo 로그 : 변경 후의 값을 기록합니다.
    + UndDo 로그 : 변경 전의 값을 기록합니다.
6. 데이터 캐시에 있는 데이터를 업데이트(변경)힙니다.
7. 판매자에게 입금하는 요청도 똑같이 발생합니다.

판매자 입금 요청까지 다 로그 캐시에 쌓인 상태에서 문제가 생겨 ROLLBACK을 해야 한다면 UnDo로그를 역순으로 기록하면 이전 상태로 원상 복구가 가능합니다.  
만약 ROLLBACK 명령어 없이 전원이 나가는 등 예상치 못한 오류가 발생할 경우, ReDo로그를 순차적으로 사용해서 일관성있게 데이터를 만들어주고 UnDo 로그를 역순으로 기록하여 원상 복구합니다.  
만약 동시 접근이 가능하다면 락을 사용하여 데이터 접근을 트랜잭션이 끝날 때 까지 점유해줍니다.  

<br>

## 5. Spring의 트랜잭션
---
데이터베이스에서는 각각의 명령을 하나의 트랜잭션으로 보고 보장해주기 때문에 여러 명령을 하나의 트랜잭션으로 묶고 싶은 경우, 개발자가 직접 트랜잭션의 경계설정을 통해 트랜잭션을 명시하는 일이 필요합니다.  
![그림9](https://github.com/backtony/blog-code/blob/master/interview/transaction/img/transaction-9.PNG?raw=true)  
스프링은 트랜잭션 추상화 인터페이스인 PlatformTransactionManager을 통해 다양한 DataSource에 맞게 트랜잭션을 관리할 수 있게 하고 있습니다.  
getTransaction, rollback, commit으로 구성되어 있습니다.  
getTransaction 메서드를 통해 파라미터로 전달되는 TransactionDefinition에 따라 트랜잭션을 시작합니다.  
트랜잭션을 문제없이 마치면 commit을, 문제가 발생하면 rollback을 호출합니다.  
getTransaction부터 commit이나 rollback을 하는 부분까지가 트랜잭션 경계 설정입니다.  
<br>

### PlatformTranctionManager 구현체
![그림10](https://github.com/backtony/blog-code/blob/master/interview/transaction/img/transaction-10.PNG?raw=true)  
대표적으로 DataSourceTransactionManager, JpaTransactionManager, JtaTransactionManager가 있습니다.  
DataSourceTransactionManager는 JDBC에, JpaTransactionManager는 JPA에 사용하는 매니저 입니다.  
이 두 매니저는 하나의 데이터베이스를 사용하거나 각각의 데이터를 독립적으로 사용하는 로컬 트랜잭션의 경우에 사용할 수 있습니다.  
하나 이상의 데이터베이스가 참여하는 경우에는 글로벌 트랜잭션에 사용되는 JtaTransactionManager를 사용할 수 있습니다.  
여러 개의 데이터베이스에 대한 작업을 하나의 트랜잭션으로 묶을 수 있고, 다른 서버에 분산된 것도 묶을 수 있습니다.  
위와 같이 직접적으로 코드에 구현하는 방식 외에도 스프링은 AOP를 이용한 선언적 트랜잭션을 제공하고 있습니다.  
선언적 트랜잭션은 tx namespace를 이용하는 방안과 애노테이션을 기반으로 설정하는 방안이 있습니다.  
tx namespace를 사용하는 방식은 Bean 설정 파일에서 트랜잭션 매니저를 빈으로 등록하고 속성과 대상을 정의해 트랜잭션을 적용하겠다고 명시하는 것으로 코드에 영향을 주지 않고 일괄적으로 트랜잭션을 적용하고 변경할 수 있다는 장점이 있습니다.  
애노테이션 기반은 일반적으로 가장 많이 사용하는 방식입니다.  
```java
@Transactional
public class A{
    
    ...

    @Transactional
    public sendMoney(){
        ...
    }
}
```
트랜잭션 애노테이션은 메서드, 클래스, 인터페이스 등에 적용할 수 있습니다.  
클래스 상단에 적용된 애노테이션은 해당 클래스에 존재하는 모든 메서드에 애노테이션이 적용됩니다.  
중첩되어 존재하는 경우에는 클래스 메서드, 클래스, 인터페이스 메서드, 인터페이스 순으로 우선순위를 갖습니다.  
애노테이션이 적용된 메서드는 메서드 시작부터 트랜잭션이 시작되고, 메서드가 성공적으로 끝나면 트랜잭션 커밋, 문제가 생기면 롤백하는 과정이 진행됩니다.  
<br>

Spring boot에서 @Transactional 애노테이션을 붙이게 되면 프록시 객체가 우리가 만든 메서드를 한번 감싸서 메서드 위, 아래로 코드를 삽입해줍니다.
```java
public class BooksProxy {
  private final Books books;
  private final TransactonManager manager = TransactionManager.getInstance();
  
  public BooksProxy(Books books) {
    this.books = books;
  }
  
  public void addBook(String bookName) {
    try {
      manager.begin(); // 트랜잭션 시작
      books.addBook(bookName); // 실제 로직 호출
      manager.commit(); // 트랜잭션 커밋
    } catch (Exception e) {
      manager.rollback(); // 트랜잭션 롤백
    }
  }
}
```


### Transaction 전파 타입
트랜잭션 전파란 __트랜잭션의 경계에서 이미 트랜잭션이 진행 중인 트랜잭션이 있을 때 어떻게 동작할지를 결정하는 것__ 입니다.  
![그림7](https://github.com/backtony/blog-code/blob/master/interview/transaction/img/transaction-7.PNG?raw=true)  

#### REQUIRED
![그림11](https://github.com/backtony/blog-code/blob/master/interview/transaction/img/transaction-11.PNG?raw=true)  
```java
@Transactional(propagation=Propagation.REQUIRED)
```
디폴트 설정은 REQUIRED입니다.  
진행중인 트랜잭션이 없다면 새로 트랜잭션이 생성하여 사용합니다.  
이미 시작된 트랜잭션이 있다면 해당 트랜잭션의 중간에 참여하여 실행되게 됩니다.  
따라서 두 메서드가 하나의 트랜잭션으로 실행되기 때문에 어느 곳에서라도 문제가 발생하면 전부 롤백됩니다.  

#### REQUIRES_NEW
```java
@Transactional(propagation=Propagation.REQUIRES_NEW)
```
진행중인 트랜잭션이 없다면 새로운 트랜잭션을 만들어 동작하고, 진행중인 트랜잭션이 존재한다면 해당 트랜잭션을 잠시 보류시키고 자신이 만든 트랜잭션을 실행합니다.  

#### SUPPORTS
```java
@Transactional(propagation=Propagation.SUPPORTS)
```
진행중인 트랜잭션이 없다면 트랜잭션 없이 메서드를 실행하고, 진행중인 트랜잭션이 있다면 REQUIRED처럼 해당 트랜잭션에 참여합니다.  

#### NOT_SUPPORTED
```java
@Transactional(propagation=Propagation.NOT_SUPPORTED)
```
진행중인 트랜잭션이 없다면 트랜잭션이 없이 진행하고, 진행중인 트랜잭션이 있다면 해당 트랜잭션을 보류하고 트랜잭션 없이 진행합니다.  

#### MANDATORY
```java
@Transactional(propagation=Propagation.MANDATORY)
```
진행중인 트랜잭션이 없다면 예외가 발생하고, 진행중인 트랜잭션이 있다면 참여합니다.  

#### NEVER
```java
@Transactional(propagation=Propagation.NEVER)
```
진행중인 트랜잭션이 없다면 트랜잭션 없이 진행하고, 진행중인 트랜잭션이 있다면 예외를 발생시킵니다.  

#### NESTED
![그림12](https://github.com/backtony/blog-code/blob/master/interview/transaction/img/transaction-12.PNG?raw=true)  
```java
@Transactional(propagation=Propagation.NESTED)
```
진행중인 트랜잭션이 없다면 새로운 트랜잭션을 만들어 수행하고, 진행중인 트랜잭션이 있다면 새로운 트랜잭션을 만들고 진행중인 트랜잭션 내에 삽입합니다.  
새로 만든 트랜잭션은 부모 트랜잭션의 커밋, 롤백에는 영향을 받지만 부모 트랜잭션은 새로 만든 트랜잭션의 커밋과 롤백에는 영향을 받지 않습니다.  

<br>

### 격리 수준
기본적으로 데이터베이스에 설정되어 있지만 속성을 통해 재설정이 가능합니다.  
앞서 이에 대해서는 설명했기에 적용하는 방식만 작성하겠습니다.  
```java
@Transactional(isolation=Isolation.DEFAULT)
@Transactional(isolation=Isolation.READ_UNCOMMITTED)
...
```
DAFAULT 설정은 데이터베이스의 기본 설정을 따른다는 것입니다.  

<br>

### timeout
```java
@Transactional(timeout=10)
```
정해준 시간을 기준으로 시간이 지나면 예외가 발생하고 롤백되도록 하는 설정입니다.  
따로 설정하지 않으면 지정되지 않습니다.
<br>

### readOnly
```java
@Transactional(readOnly=true)
```
트랜잭션 안에서 update, insert, delete 작업이 일어나는 것을 방지합니다.  
flush모드가 manual로 설정되어 jpa 더티체킹기능이 무시됩니다.  
<br>

### rollabackFor
```java
@Transactional(rollbackFor=NoSuchElementException.class)
```
__기본적으로 트랜잭션은 런타임 예외와 Error가 발생했을 때만 롤백합니다.__  
특정 Exception을 클래스로 전달해서 사용하면 롤백 대상으로 사용할 수 있습니다.

<Br>

### noRollabackFor
```java
@Transactional(noRollbackFor={IOException.class,SqlException.class})
```
rollabackFor과 반대로 예외를 지정해서 롤백 대상에서 제외시킬 수 있습니다.


<Br><Br>

__참고__  
<a href="https://www.youtube.com/watch?v=ImvYNlF_saE&list=PLgXGHBqgT2TvpJ_p9L_yZKPifgdBOzdVH" target="_blank"> [10분 테코톡] 🙊 에이든의 트랜잭션 메커니즘</a>  
<a href="https://www.youtube.com/watch?v=e9PC0sroCzc&list=PLo0ta52hn1uHQ5iQ3hAeRoMUeLJFIeRew" target="_blank"> [10분 테코톡] 🌼 예지니어스의 트랜잭션</a>  
<a href="https://www.youtube.com/watch?v=aX9c7z9l_u8&list=PLo0ta52hn1uHQ5iQ3hAeRoMUeLJFIeRew" target="_blank"> [10분 테코톡] 🐤 샐리의 트랜잭션</a>


