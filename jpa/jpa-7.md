# JPA 기본 - 객체지향 쿼리 언어 JPQL - 1


## 1. JPQL
---
+ 객체지향 쿼리 언어
+ 테이블이 아닌 엔티티 객체를 대상으로 쿼리
+ SQL을 추상화해서 특정 데이터베이스 SQL에 의존 X
+ JPQL은 결국 SQL로 변환됨

## 2. 기본 문법
---
+ 엔티티와 속성은 대소문자 구분
  - ex) Member, name
+ 키워드는 대소문자 구분 X
  - ex) select, from
+ 별칭 필수(as 생략 가능하지만 사용 권장)
+ 테이블 이름이 아닌 엔티티 이름 사용

<br>

### TypeQuery, Query
+ TypeQuery : 반환 타입이 명확할 때 사용
+ Query : 반환 타입이 명확하지 않을 때 사용

```java
// select 하는 m이 Member.class로 명확
TypedQuery<Member> query = em.createQuery("select m from Member m", Member.class);

// select하는 username은 String, age는 int로 반환타입이 불명확
Query query2 = em.createQuery("select m.username,m.age from Member m");
```
<br>

## 3. 결과 조회 API
---
+ getResultList() : 결과를 리스트로 반환하고 없다면 빈 리스트를 반환 -> null 걱정이 없음
+ getSingleResult() : 결과가 정확히 하나일 때만 사용  
  - 둘 이상이면 NonUniqueResultException
  - 결과가 없으면 NoResultException
    - 결과가 없는데 exception이 터지면 try catch 써야하는게 너무 불편해서 spring data jpa에서는 NoResultException이 터지만 그냥 null이나 optional로 반환함
  
```java
// 단순 쿼리
List<Member> result = em.createQuery("select m from Member m", Member.class)
                    .getResultList();
Member result2 = em.createQuery("select m from Member m", Member.class)
        .getSingleResult();

// 위치 기준 파라미터 바인딩도 있는데 enum과 같이 밀리는 비슷한 문제가 발생해서 사용 안함
// 이름 기준 파라미터 바인딩
Member result2 = em.createQuery("select m from Member m where m.username=:username", Member.class)
                    // 변수명, 넣을 값
                    .setParameter("username","member1")
                    .getSingleResult();
```
<br>

## 4. 프로젝션
---
+ select 절에 조회할 대상을 지정하는 것
+ 프로젝션 대상 : 엔티티, 임베디드 타입, 스칼라 타입(숫자, 문자 등 기본 데이터 타입)이 가능, 참고로 관계형 db는 스칼라 타입만 가능함
+ em.createQuery로 조회하면 영속성 컨텍스트에서 관리
+ 여러 값을 조회할 경우
  - Query 타입으로 조회
  - Object[] 타입으로 조회
  - new 명령어로 조회 : 이 방법이 best

```java
List<MemberDTO> resultList = em.createQuery("select new jpql.MemberDTO(m.username,m.age) from Member m", MemberDTO.class)
                    .getResultList();
```
username과 age는 String과 int로 타입이 다르다. 즉 여러 타입의 값을 조회해야한다. 이때는 조회할 것 DTO로 따로 만들어 준다. 여기서는 MemberDTO 클래스를 만들었고 이것은 Entity가 아니다. DTO에 생성자를 만들어두고 쿼리문에 new로 패키지명을 포함한 전체 클래스명을 입력해서 생성자로 호출하면 된다.
<br>

object는 다음과 같이 뽑아낼 수 있다.
```java
List<Object[]> resultList = em.createQuery("select m.username, m.age from Member m")
                    .getResultList();

            Object[] result = resultList.get(0);
```
타입이 여러개이므로 object 타입 배열로 뽑아내서 사용한다.

<br>



## 5. 페이징
---
+ setFirstResult(int startPosition) : 조회 시작 위치
+ setMaxResults(int maxResult) : 조회할 데이터 수


```java
List<Member> resultList = em.createQuery("select m from Member m order by m.age", Member.class)
                    .setFirstResult(1)
                    .setMaxResults(10)
                    .getResultList();
```
1번부터 10번까지 가져온다.

<br>

## 6. 조인
---
join을 아래처럼 쿼리문에 적어주는 것을 명시적 조인이라고 하고, 안 적어줘도 알아서 join이 나가는 것을 묵시적 조인이라고 하는데 한 번에 파악하기 위해서는 웬만하면 명시적 조인을 사용하는게 좋다.
```java
// 내부 조인
em.createQuery("select m from Member m join m.team t",Member.class)
// 외부 조인
em.createQuery("select m from Member m left join m.team t",Member.class)
// 크로스 조인(상호 조인)
em.createQuery("select m from Member m, Team t where m.username=t.name",Member.class)
// on절 사용
em.createQuery("select m from Member m join m.team t on t.name='A'",Member.class)
// 연관관계 없는 엔티티 외부 조인, 
em.createQuery("select m from Member m left join Team t on m.username = t.name",Member.class)
```
위와 같이 join을 사용하면 연관관계가 지정되어 있기에 알아서 on으로 fk=pk로 나간다. 하지만 여기에 on을 사용하면 쿼리문 on절에서 fk=pk and로 추가로 조건이 걸린다.  
만약 연관관계 매핑이 안되어있어 fk,pk를 조인하지 못할 수가 있다. 그런 부분은 on절을 사용해서 직접 지정해주면 된다.
<br>

## 7. 서브 쿼리
---
+ 일반적인 sql 그대로 사용이 가능
+ JPA는 where, Having 절에서만 서브 쿼리가 가능하지만 하이버네이트에서는 select 절도 지원 -> 대부분 하이버네이트 사용하니 select,having, where에서 서브 쿼리 사용 가능
+ from 절에서 서브쿼리 사용 불가능! -> 조인으로 풀 수 있으면 조인으로 풀어서 해결

```java
em.createQuery("select m from Member m where m.age > (select avg(m2.age) from Member m2)",Member.class)
```
<br>

## 8. JPQL 타입 표현과 기타식
---
### 타입 표현
+ 문자 : 'Hi'
+ 숫자 : 10L, 10D, 10F
+ Boolean : TRUE, FALSE
+ ENUM : 패키지명 포함
+ 엔티티 타입 : TYPE(M) = Member (상속관계에서 사용)

```java
// type이 enum 타입인 경우 패키지와 함께 적어줘야 한다
em.createQuery("select m.username from Member m where m.type=jpql.MemberType.ADMIN",Member.class);

// 바인딩 할 경우 클래스명부터 시작
em.createQuery("select m.username from Member m where m.type=:userType",Member.class)
                  .setParameter("userType",MemberType.ADMIN);

// 엔티티 타입
// book은 item을 상속받았다. 원하는 상속 타입만 뽑아내기
em.createQuery("select i from Item i where type(i) = Book",Item.class);
```
<br>

### 기타
+ SQL문법과 일치
+ and, or, not, in between, like, is null, exists ...

<Br>

## 9. 조건식
---
```java
String query =
                    "select "+
                            "case t.name" +
                            "     when 'A' then '잘함' " +
                            "     when 'A' then '보통' " +
                            "     else '판단불가' " +
                            " end "+
                    "from Team t";
em.createQuery(query,String.class);
```
기본 case식, 단순 case식 다 지원하고 위는 단순 case식이다.
<br>

+ coalesce : 첫 번째 값이 null이 아니면 첫번째를, null이면 두 번째 값을 반환
+ nullif : 두 값이 같으면 null, 다르면 첫 번째 반환

```java
// coalesce
em.createQuery("select coalesce(m.username,'모르는 사람') from Member m",String.class);

// nullif
em.createQuery("select nullif(m.username,'모르는 사람') from Member m",String.class);
```

<br>

## 10. 함수
---
### 기본 함수
+ CONCAT
+ SUBSTRING
+ TRIM
+ LOWER, UPPER
+ LENGTH
+ LOCATE
+ ABS, SQRT, MOD
+ SIZE, INDEX(JPA 용도)

<br>

### 사용자 정의 함수
![그림9](https://github.com/backtony/blog-code/blob/master/jpa/img/7/9-1.PNG?raw=true)

dialect 패키지를 만들고 사용하는 DB방언을 상속받아서 클래스를 만든다.
```java
package dialect;

import org.hibernate.dialect.H2Dialect;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.type.StandardBasicTypes;

public class MyH2Dialect extends H2Dialect {
    public MyH2Dialect() {
        registerFunction("group_concat", new StandardSQLFunction("group_concat", StandardBasicTypes.STRING));
    }
}
```
위와 같이 만들었으면 persistence.xml에서 방언 연결을 내가 만든 클래스로 연결해주면 된다. 함수 만드는 방법은 위 코드에서 H2Dialect를 타고 들어가면 만들어 진 것들이 있는데 그것을 참고해서 만들면 된다.
<br>

```java
// function으로 함수 호출 명시, 함수명, 파라미터
em.createQuery("select function('group_concat',m.username) from Member m",String.class);

// 하이버네이트 사용시 아래처럼 사용 가능
// 실행은 되나 문법오류로 밑줄이 그어지는데 
// alt + enter -> inject language -> ID를 HQL로 변경
em.createQuery("select group_concat(m.username) from Member m",String.class);
```


<Br><Br>

__참고__  
<a href="https://www.inflearn.com/course/ORM-JPA-Basic/dashboard" target="_blank"> 자바 ORM 표준 JPA 프로그래밍 - 기본편</a>   

