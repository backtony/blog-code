# 스프링 데이터 JPA

## 1. 공통 인터페이스
---
### 설정
```java
@Configuration
@EnableJpaRepositories(basePackages = "jpabook.jpashop.repository")
public class AppConfig {}
```
스프링 부트 사용시 @SpringBootApplication 위치를 지정하여 해당 패키지와 하위 패키지 인식하기 때문에 설정할 필요가 없다.  
<br>

### 사용 예시
```java
// 제너릭
// <T,ID> -> T : 엔티티 타입, ID : 식별자 타입(PK)
public interface TeamRepository extends JpaRepository<Team,Long> {}
```
+ @Repository 애노테이션 생략 가능 
    - 컴포넌트 스캔을 스프링 데이터 JPA가 자동으로 처리
    - JPA 예외를 스프링 예외로 변환하는 과정도 자동 처리


### 공용 인터에피스 사용시 주의사항
+ __추가적 쿼리를 만들 경우 @Transactional(readOnly = true)를 걸어주고 데이터 변경이 필요한 경우 @Transactional을 붙여준다.__
    - 대부분 repository의 쿼리의 경우 조회쿼리가 대부분이므로 전체에 readOnly를 걸어주고 변경하는 부분에만 따로 Transactional을 붙여준다.
+  __기본적으로 JpaRepository는 조회용에는 @Transactional(readOnly = true), save같은 데이터변경에는 @Transactional이 붙어있다.__
    -  __따라서 readOnly가 붙은 조회용 쿼리를 사용하면 스냅샷이 생기지 않는다. 즉, 영속성 컨텍스트에서 관리는 하고 있지만 dirty checking을 안하기 때문에 수정해도 DB에 결과가 반영되지 않는다. 이때는 반드시 @Transactional 애노테이션이 붙은 곳에서 데이터를 수정해야 한다. 예를 들면 Controller에서 find쿼리로 조회해온 상태에서 수정이 필요하다면 @Transactional 애노테이션이 붙은 service 계층으로 넘겨서 수정해야 dirty checking이 수행되어 정상적인 수정 처리가 DB에 반영된다.__

<br>

### 공통 인터페이스 구성
![그림1](https://github.com/backtony/blog-code/blob/master/jpa/img/9/1-1.PNG?raw=true)

spring-data-commons라는 공통의 프로젝트가 있고 그 밑에 jpa, mongoDB, redis 등의 각각에 특화된 기능들이 있는 라이브러리가 있다.

<br>

## 2. 쿼리 메서드 기능
---
### 메서드 이름으로 쿼리 생성
[공식 문서 링크](https://docs.spring.io/spring-data/jpa/docs/2.4.6/reference/html/#jpa.query-methods.query-creation)

```java
// By이후의 필드가 where조건으로 들어간다고 보면 되고
// GreaterThan이라는 형식이 이미 정해져있어 그냥 사용하면 된다.
public interface MemberRepository extends JpaRepository<Member,Long> {
    List<Member> findByUsernameAndAgeGreaterThan(String username, int age);
}
```
스프링 데이터 JPA가 메서드 이름을 분석해서 JPQL을 생성하고 실행한다.
+ 조회 : find...By, read...By, query...By, get...By
+ COUNT : count...By 반환타입 long
+ EXISTS : exists...By 반환타입 boolean
+ 삭제 : delete...By, remove...By 반환타입 long
+ DISTINCT : find...DistinctBy 
+ Containing : find...By..Containing : 무엇이 포함되어 있는지 확인
    - ex) findByUsernameContaining
+ LIMIT : findFirst3, findFirst, findTop, findTop3
    - [limit 공식 문서](https://docs.spring.io/spring-data/jpa/docs/2.4.6/reference/html/#repositories.limit-query-result)

...은  findMemberBy처럼 식별하기 위한 내용설명이 들어가는 것인데 안적어도 된다. 리포지토리 이름 자체가 Member이라서 굳이 적어줘야하나 싶기도 하다.  
조건이 많아질수록 By뒤에 이름이 길어지는 단점이 있다. 따라서 3개부터는 다른 방식을 선택하는게 좋다.  
<br>

__가장 좋은 오류__  
+ 가장 좋은 오류 : 컴파일러가 잡아줄 수 있는 오류
+ 그다음 좋은 오류 : 애플리케이션 실행 시점에 잡을 수 있는 오류
+ 가장 나쁜 오류 : 고객이 클릭했을 때 오류

메서드 이름으로 쿼리를 생성한 경우 애플리케이션 실행 시점에 오타를 잡을 수 있다.
<br>

### @Query, 리포지토리 메서드에 쿼리 정의
앞서 메서드 이름으로 쿼리를 생성할 경우 조건이 2개가 넘어가면 이름이 길어지므로 다른 방법을 선택하라고 했다. 그 방법 이 이 방법이다. 그냥 JPQL을 짜는 것이다.
```java
public interface MemberRepository extends JpaRepository<Member,Long> {
    // @Param으로 파라미터 넣어준다.
    @Query("select m from Member m where m.username =:username and m.age =:age")
    List<Member> findMember(@Param("username") String username, @Param("age") int age);

    // 값타입
    @Query("select m.username from Member m")
    List<String> findUsernameList();

    // DTO 
    @Query("select new study.datajpa.dto.MemberDto(m.id,m.username,t.name) from Member m join m.team t")
    List<MemberDto> findMemberDto();  

    // 컬렉션 파라미터 바인딩
    // 다른 것도 받을 수 있게 상위인 Collection사용
    @Query("select m from Member m where m.username in :names")
    List<Member> findByNames(@Param("names") Collection<String> names);
}
```
실행할 메서드에 정적 쿼리를 직접 작성하므로 이름없는 Named 쿼리라고 할 수 있고, 애플리케이션 실행 시점에 문법 오류를 발견할 수 있다.  
<br>

### 반환 타입
[공식 문서 링크](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#repository-query-return-types)  
스프링 데이터 JPA는 유연한 반환 타입을 지원한다. 
+ 컬렉션 : 결과가 없다면 빈 컨렉션 반환
+ 단건 조회
    - 결과 없음 -> exception 안 터지고 null 반환 -> optional 나오기 이전의 문제였음
    - 현재는 없는지 있는지 모름 -> Optional 사용 
    - 결과가 2건 이상 -> 원래는 NonUniqueResultException 발생 -> 스프링 데이터 JPA가 스프링프레임워크 exception으로 바꿔서 반환 -> IncorrectResultSizeDataAccessException 발생
        - 변환하는 이유는 리포지토리 기술은 JPA가 아닌 다른 기술이 될 수 있다. 서비스 계층의 클라이언트 코드들은 jpa에 의존하는게 아니라 스프링이 추상화한 예외에 의존하면 다른 기술로 바뀌어도 스프링은 동일한 Exception을 내려주게 되어 이걸 사용하는 클라이언트 코드들을 바꿀 필요가 없어지게 되기 때문이다.

<br>

### 페이징
__JPA에서 제공하는 페이징은 페이지가 1이 아니라 0부터 시작함으로 반드시 주의하자.__
```java
public interface MemberRepository extends Repository<Member, Long> {
 // 메서드 이름으로 쿼리 생성에 파라미터 Pageable 추가 + 반환 타입을 Page로 감싸기
 // pageable로 size, sort, page 파라미터를 하나로 받음
 // Slice형식이면 반환타입만 Slice로 변경
 Page<Member> findByAge(int age, Pageable pageable);
}
```
findByAge 로 메서드 이름으로 쿼리 생성한 경우이므로 Age를 기준으로 찾는 기능이다. 그런데 Age로 찾을 때 페이징할 예정이므로 반환타입을 Page로 감싸주고 파라미터로 Pageable을 추가해줬다. 여기서 Pageable는 awt와 spring이 있는데 스프링을 사용해야 한다. 페이징에는 Page와 Slice가 있다. 

+ Page : 일반적으로 총 페이지 개수가 있고 현재 몇 번째 페이지인지 알려주는 방식, Page 메서드는 아래 코드에서 확인
+ Slice : 페이징한 개수 + 1 형식으로 화면에서 전체 페이지 갯수를 알려주지 않고 [ 더보기 ]와 같은 형식으로 제시하는 방식 -> 토탈 데이터 개수와 토탈 페이지 개수를 모름
    - int getNumber() : 현재 페이지
    - int getSize() : 페이지 크기
    - int getNumberOfElements() : 현재 페이지에 나올 데이터 수
    - List< T > getContent() : 조회된 데이터
    - boolean hasContent() : 조회된 데이터 존재 여부
    - Sort getSort() : 정렬 정보
    - boolean isFirst() : 현재 페이지가 첫 페이지 인지 여부
    - boolean isLast() : 현재 페이지가 마지막 페이지 인지 여부
    - boolean hasNext() : 다음 페이지 여부
    - boolean hasPrevious() : 이전 페이지 여부
    - Pageable getPageable() : 페이지 요청 정보
    - Pageable nextPageable() :  다음 페이지 객체
    - Pageable previousPageable() : 이전 페이지 객체
    - < U > Slice< U > map(Function<? super T, ? extends U> converter); //변환기

```java
// Page
@Test
void paging() throws Exception{
    memberRepository.save(new Member("가", 10));
    memberRepository.save(new Member("나", 10));
    memberRepository.save(new Member("라", 10));
    memberRepository.save(new Member("마", 10));
    memberRepository.save(new Member("다", 10));

    // jpa는 페이지를 0부터 시작
    // 페이지 , 데이터 개수(페이지 사이즈), 정렬
    // username을 기준으로 내림차순 sorting한 데이터를 0페이지부터 3개 가져오기
    PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC,"username"));

    Page<Member> page = memberRepository.findByAge(10, pageRequest);

    List<Member> content = page.getContent(); // 페이징해서 가져온 데이터 꺼내기
    long totalElements = page.getTotalElements(); // 전체 모든 데이터 개수(가져온 데이터 개수가 아님)
    int totalPages = page.getTotalPages(); // 총 페이지 개수 -> 3개씩 끊었으므로 2페이지 나옴
    boolean first = page.isFirst(); // 이 페이지가 첫 페이지인지
    boolean hasNext = page.hasNext(); // 다음 페이지가 있는지
}
```
<br>

#### Page 방식에서 totalPages 최적화
Page방식에서는 전체 데이터의 개수를 구해주는데 이 데이터의 개수를 구하는 카운트 쿼리는 프로젝션만 count(id값)으로 변경되고 from 이후에는 기존 쿼리랑 동일하게 나간다. 만약 기존 쿼리가 조인으로 인해 복잡해지면 카운트 쿼리도 조인되어 복잡한 쿼리가 나간다. 하지만 카운트 쿼리는 그냥 개수만 세면 되므로 join할 필요가 없는 경우도 있다. 이런 경우 카운트 쿼리를 분리해서 해결할 수 있다.
```java
public interface MemberRepository extends JpaRepository<Member,Long> {
    @Query(value = "select m from Member m left join m.team t",
        countQuery = "select count(m) from Member m")
    Page<Member> findByAge(int age, Pageable pageable);
}
```

__cf) 지금까지 잘못 알고 있던 점__  
team과 member가 연관관계에 있으므로 select m from Member m 하면 묵시적 조인으로 Lazy라도 조인되어 쿼리가 나가는 줄 알았다. 나가는 쿼리를 살펴보니 join이 안나간다. Lazy로 설정되어 있으면 그냥 조인 자체가 안되고 프록시를 끼워 넣는 것이었다. Lazy로 되어있으면 join을 명시적으로 써줘야만 join쿼리가 나간다. 이때는 join 쿼리가 나가지만 그래도 들어오는 건 프록시이다. 결론적으로 위에서 사용한 카운트 쿼리문은 조인이 안나간다.  
<br>

#### DTO로 변환
page는 현재 타입이 Member 엔티티이므로 API 의 경우 그대로 반환하면 절대 안된다. DTO로 변환해줘야 하는데 map을 통해서 쉽게 DTO로 변환할 수 있다.
```java
PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC,"username"));
Page<Member> page = memberRepository.findByAge(10, pageRequest);
Page<MemberDto> toMap = page.map(m -> new MemberDto(m.getId(), m.getUsername(), null));
```
<br>

### 벌크성 수정 쿼리
```java
public interface MemberRepository extends JpaRepository<Member,Long> {
    @Modifying(clearAutomatically = true)
    @Query("update Member m set m.age = m.age + 1 where m.age >= :age")
    int bulkAgePlus(@Param("age") int age);
}
```
+ @Modifying : 조회성 쿼리가 아니라는 것을 알려주는 것으로 JPA가 만들어 줄때 .getResultList가 아니라 .executeUpdate를 붙여주게 된다.
+ 벌크성 수정 쿼리는 영향 받은 데이터의 개수가 반환된다.
+ clearAutomatically = true : 쿼리 날린 후 영속선 컨텍스트 비워준다.
    - 벌크성 수정 쿼리는 영속성 컨텍스트를 무시하고 바로 DB에 날리기 때문에 영속성 컨텍스트와 DB 간의 불일치가 생긴다. 따라서 벌크성 쿼리 이후에는 영속성 컨텍스트를 비워줘야하는데 이 옵션이 비워주는 역할을 해준다.

<br>

### @EntityGraph
지연로딩 관계를 한 번에 땡겨올 때 페치조인을 사용했다. 쿼리가 복잡할 경우에는 @Query로 직접 짜는게 좋다. 하지만 간단한 경우에는 @EntityGraph을 이용해서 페치조인을 할 수 있다.
```java
public interface MemberRepository extends JpaRepository<Member,Long> {
    // JPA 리포지토리에서 제공하는 findAll override로 수정하기
    // 기존에는 Lazy라면 프록시로 땡겨오지만
    // fetch 조인으로 수정하기
    @Override
    // 페치 조인할 필드명 추가
    @EntityGraph(attributePaths = {"team"})    
    List<Member> findAll();

    //JPQL + 엔티티 그래프
    @EntityGraph(attributePaths = {"team"})
    @Query("select m from Member m")
    List<Member> findMemberEntityGraph();

    //메서드 이름으로 쿼리에서 특히 편리하다.
    @EntityGraph(attributePaths = {"team"})
    List<Member> findByUsername(String username)
}
```
findAll 같이 기본적으로 제공하는 부분은 지연로딩으로 해놨으면 프록시로 땡겨질 것이다. 이것을 @Override 해서 @EntityGraph(attributePaths = {"필드명"}) 으로 페치 조인할 필드를 넣어주면 페치 조인해서 가져올 수 있다. @EntityGraph(attributePaths = {"필드명"}) 는 메서드 이름으로 쿼리, jPQL에서도 적용할 수 있다.  
__참고로 Entitygraph는 left outer join 이고, 기본적인 fetch join은 inner join이다.__


<br>

__cf) 페치 조인은 기본적으로 left outer join으로 동작한다.__  

<br>

### Hint
find로 찾아올 때 기본적으로 영속성 컨텍스트에는 스냅샷을 가지고 있다. 따라서 변경 감지를 위해 결국 두 가지 상태를 가지게 되므로 메모리를 더 쓰게 된다. 하지만 만약 내가 수정은 안하고 딱 조회용으로만 사용할 것이라면 굳이 스냅샷을 가지고 있을 필요가 없다. 따라서 아래와 같이 사용한다.
```java
public interface MemberRepository extends JpaRepository<Member,Long> {
    @QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value = "true"))
    Member findReadOnlyByUsername(String username);
```
readOnly를 True로 준다면 스냅샷이 생기지도 않고 실제로 find해서 수정한다해도 수정 쿼리가 무시된다. 그런데 사실 이걸 전부 다 넣는다고 해서 성능 최적화가 미비하다. 성능에 문제가 되는 건 복잡한 쿼리가 잘못 나가는 문제다. 진짜 중요하고 트래픽이 많은 것에만 넣어주는 것이 좋은 선택이다. 그런데 진짜 성능이 딸리면 이미 캐시를 깔아야하는 단계가 오기 때문에 이걸로 얻을 수 있는 이점이 크진 않다. 결론은 실시간 트래픽이 많은 상황에서 아직까진 버틸 수 있는 정도일 때 성능을 약간이나마 향상시키고자 한다면 사용하면 되고 이후로 버틸 수 없다면 다른 선택을 해야한다.  
<br>


<br>

## 3. 확장 기능
---
### 사용자 정의 리포지토리
스프링 데이터 JPA 리포지토리는 인터페이스만 정의하고 구현체는 스프링이 자동으로 생성해준다. 그렇다면 이제 JPA 직접 사용(EntityManager), Querydsl, JDBC Template을 사용하고 싶다면 인터페이스를 구현하는 구현체를 직접 만들고 전부 override해서 구현하고 원하는 부분을 추가해서 만들어야 한다. 이렇게 전부 구현하는 것은 상당히 번거롭기 때문에 이 문제를 해결하기 위해서 사용자 정의 인터페이스를 사용한다. 방법은 간단하다.  
1. 인터페이스 만들기
2. 이름 규칙을 맞춰서 인터페이스 구현체 만들기
    - 규칙 : JPA 리포지토리명 + Impl
3. JPA 리포지토리에 만든 인터페이스 상속하기

```java
// 사용자 정의 인터페이스
public interface MemberRepositoryCustom {
 List<Member> findMemberCustom();
}

// 구현체
@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepositoryCustom {
 private final EntityManager em;
 // 생략
}

// 사용자 정의 인터페이스 상속
public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom {}
```
여기서 사용자 정의 인터페이스명은 아무렇게나 지어도 된다. 하지만 구현체의 경우 JPA 인터페이스명+Impl 로 지어줘야한다. 구현체명을 저렇게 맞춰줘야 스프링 데이터 JPA가 인식해서 스프링 빈으로 등록해준다.  
<br>

__반드시 기억할 것__  
JPA 리포지토리에는 대부분의 핵심 로직을 제공하기 때문에 분명 JPA리포지토리가 핵심 로직을 담게 된다. 핵심 로직에 추가적인 내용이 필요하다면 사용자 정의 인터페이스를 추가해서 사용한다. 하지만 모든 내용을 JPA 리포지토리에 담으면 유지보수성이 떨어진다. 앞서 공부했듯이, __핵심 비즈니스 로직이 있는 리포지토리와 화면에 맞춘 DTO, 복잡한 통계성 쿼리 뽑는 리포지토리를 분리해야한다.__ 사용자 정의 리포지토리를 사용하면 이건 분리하는게 아니라 결국 JPA 리포지토리가 상속받아 해당 기능을 가지고 있기 때문에 JPA리포지토리가 커지는 것이다. 따라서 핵심 비즈니스에 사용되는 것이 아니라면 사용자 정의 인터페이스를 사용하는 것이 아니라 MemberQueryRepository처럼 따로 리포지토리를 만들어서 분리하는게 좋은 선택이다.  
<br>

### Auditing
엔티티를 생성, 변경할 때 변경한 사람과 시간을 추적하고 싶을 때 사용한다. 전에 공부했던 @MappedSuperclass를 사용한다. 실제로 거의 모든 테이블에서는 만든 시간과 업데이트 한 시간을 필요로 한다. 하지만 등록한 사람과 수정한 사람은 굳이 필요하지 않을 때가 있다. 따라서 이를 분리하는게 좋다. 따로 클래스를 만들어 준 뒤 시간쪽을 부모로 두고 작성자,수정자 쪽이 상속받아서 사용하는 형식으로 분리한다.  
먼저 Autiting 기능을 사용하기 위해서는 스프링 부트 설정 클래스에 @EnableJpaAuditing 애노테이션을 붙여줘야 한다. 스프링 부트 설정 클래스는 XXXApplication을 의미한다. 
```java
@EnableJpaAuditing // Auditing을 사용하기 위한 애노테이션
@SpringBootApplication
public class DataJpaApplication {
	public static void main(String[] args) {SpringApplication.run(DataJpaApplication.class, args);
	}	
}

// 시간과 사람 엔티티를 분리하기
// BaseTimeEntity 
@EntityListeners(AuditingEntityListener.class)
@Getter
@MappedSuperclass
public class BaseTimeEntity {
    @CreatedDate
    @Column(updatable = false) // 변경 불가능
    private LocalDateTime createdDate; // 만든 시간

    @LastModifiedDate
    private LocalDateTime lastModifiedDate; // 마지막 수정 시간
}

// BaseEntity -> TimeEntity를 상속받기
@EntityListeners(AuditingEntityListener.class)
@MappedSuperclass
@Getter
public class BaseEntity extends BaseTimeEntity{

    @CreatedBy
    @Column(updatable = false)
    private String createdBy; // 만든사람

    @LastModifiedBy 
    private String lastModifiedBy; // 수정한 사람

}
```
+ @EntityListeners(AuditingEntityListener.class) : JPA 이벤트가 발생하기 직전에 실행되도록 하는 애노테이션
    - 스프링 데이터 JPA가 제공하는 이벤트를 엔티티 전체에 적용하려면 orm.xml에 적용하면 되는데 강의 노트를 참고하자. 설정을 하게 되면 해당 애노테이션을 붙이지 않아도 된다.
    - 해당 애노테이션을 붙이면 JPA 이벤트 시점 직전에 아래 애노테이션들이 동작한다. 
+ @CreatedDate : 만든 시간
+ @LastModifiedDate : 마지막 수정 시간
+ @CreatedBy : 만든 사람
+ @LastModifiedBy : 마지막 수정한 사람

시간의 경우 애노테이션에 의해 자동으로 값이 세팅되지만 사람에 관련된 애노테이션의 경우 스프링 부트 설정 클래스에서 설정해줘야 한다. 실무에서는 스프링 부트 설정 클래스에서 세션 정보나 스프링 시큐리티 로그인 정보에서 Id를 받는 것을 빈으로 등록해서 사용한다. 이에 관해서는 나중에 공부가 필요할 듯하다.  
<br>

참고로 첫 저장시점에 등록일과 수정일, 등록자와 수정자는 같은 데이터가 저장된다. 데이터가 중복되는 것 같지만, 이렇게 해두면 변경 컬럼만 확인해도 마지막에 업데이트한 유저를 확인할 수 있어 유지보수 관점에서 편리하다. 이렇게 하지 않으면 변경 컬럼이 null일때 등록 컬럼을 또 찾아야 하기 때문이다.  
<br>

### Web 확장 - 페이징과 정렬
__Pageable 사용은 항상 Page가 0부터 시작한다는 점을 기억하자.__  
앞서 JPA 리포지토리에서 페이징을 쉽게 하는 방식을 공부했다. 이번에는 WEB에서 넘어오는 페이징 정보를 처리하는 방법을 알아보자. 쿼리 파라미터로 넘어온 페이징 정보는 pageable 변수로 들어온다. JPA 리포지토리에서 제공하는 구현체는 pageable 파라미터를 받을 수 있다.
```java
@GetMapping("/members")
public Page<MemberDto> list(Pageable pageable){
    return memberRepository.findAll(pageable) // 엔티티로 나오니 DTO로 변환
            .map(m -> new MemberDto(m.getId(), m.getUsername(), null));
}
// 참고로 위처럼 꺼내서 생성자에 넣는게 아니라 DTO 생성자 파라미터로 엔티티를 넣어도 된다.
// 생성자로 엔티티를 받으면 ::로 더 간단하게 아래와 같이 바꿀 수 있음
// Page<Member> page = memberRepository.findAll(pageable);
// Page<MemberDto> pageDto = page.map(MemberDto::new);
```
파라미터로 Pageable을 받을 수 있다. Pageable은 인터페이스로 PageRequest 객체를 생성해서 넣어준다. 
+ page : 현재 페이지, __0부터 시작__
+ size : 한 페이지에 노출할 데이터 건수
+ sort : 정렬 조건

```
/members?page=0&size=3&sort=id,desc&sort=username,desc
```
위와 같은 요청이 오면 0번째 페이지에 데이터 건수는 3개, id와 username을 기준으로 정렬에 관한 정보가 pageable에 들어간다. 이걸 JPA 리포지토리의 메서드에 넘기면 결과를 Page< > 로 감싸서 반환해준다. 엔티티를 Page로 감싸서 반환해줄 것이기에 이걸 반환하고자 한다면 반드시 DTO로 변환하고 반환한다.  
기본값은 페이지 사이즈 20, 최대 페이지 사이즈 2000 인데 수정하고 싶다면 application.yml에서 수정하면 된다.
```yml
# application.yml
spring:  
  data:
    web:
      pageable:
        default-page-size: 
        max-page-size: 
```
항상 글로벌 설정보다 개별 설정이 더 우선이 되는데 개별 설정은 Pageable 앞에 @PageableDefault(옵션) 을 붙여주면 된다.
```java
public Page<MemberDto> list(@PageableDefault(size=12,sort="username",direction=Sort.Direction.DESC) Pageable pageable){}
```
<br>

참고로 PathVariable로 넘어오는 pk값에 엔티티를 변수로 놓으면 도메인 클래스 컨버터가 중간에 동작해서 엔티티 객체를 반환한다. 이 방식은 엔티티를 파라미터로 받는데 이것 자체가 좋은 코드가 아니므로 잘 사용하지 않는다.(사용법은 강의노트 참고)
<br>

<br>

## 4. 스프링 데이터 JPA 분석
---
### 변경
JPA의 모든 변경은 트랜잭션 안에서 동작해야 한다. JPA 리포지토리 인터페이스 안에 있는 변경 메서드의 경우 다 트랜잭션이 붙어있다. 그래서 따로 처리하지 붙여주지 않아도 된다. 서비스 계층에서 트랜잭션을 시작하면 리파지토리는 해당 트랜잭션을 전파 받아서 사용하고 서비스 계층에서 시작하지 않았다면 리파지토리에서 트랜잭션을 시작한다.  
<br>

### Save의 동작 과정
JPA 리포지토리 안에 구현되어 있는 Save 메서드를 확인해 보면 다음과 같다.
```java
public <S extends T> S save(S entity) {

		Assert.notNull(entity, "Entity must not be null.");

		if (entityInformation.isNew(entity)) {
			em.persist(entity);
			return entity;
		} else {
			return em.merge(entity);
		}
	}
```
새로운 엔티티면 저장하고 새로운 엔티티가 아니면 병합한다. 새로운 엔티티를 판단하는 기본 전략은 식별자가 객체( ex) Long )일 때는 null로 판단, 식별자가 기본 타입( ex) long )일 때 0 으로 판단한다. 기본적으로 pk값에는 @GeneratedValue를 사용하기 때문에 처음에는 값이 들어있지 않으므로 null로 판단되어 persist하게 되는 것이다. 그런데 만약 식별자 생성 전략이 @Id 애노테이션만 사용하고 값을 직접 할당하게 된다면 이야기가 달라진다. 직접 할당의 경우 생성자로 Id값을 세팅해준다고 했을 때 if문에서 이미 식별자가 있으므로 merge쪽으로 진행된다. merge는 일단 DB에 있을 것으로 가정하고 동작하므로 DB에서 select으로 데이터를 가져와 덮어씌우고 insert하는 방식으로 동작하므로 매우 비효율적이다. 불필요한 쿼리가 나간다는 것도 문제고 사실 실제로 merge를 사용해야할 일은 거의 없다.(변경감지를 사용하기 때문) __따라서 직접할당의 경우의 해당 클래스에 persistable 인터페이스를 구현함으로 이 문제를 해결한다.__

```java
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
// Persistable<키타입> 구현
public class Item implements Persistable<String> {
    @Id
    private String id;

    @CreatedDate
    private LocalDateTime createdDate;

    public Item(String id) {
        this.id = id;
    }

    /* persistable 인터페이스 구현 */
    // isNew만 손보면 된다.

    @Override
    public String getId() {
        return null;
    }

    // Auditing으로 사용한 createDate을 활용
    // createDate가 세팅되어 있는 경우 실제 값이 DB에 있는 경우
    // 값이 세팅되어 있지 않은 경우 새로운 엔티티
    @Override
    public boolean isNew() {
        return createdDate==null;
    }
}
```
id값을 직접 생성자로 할당해주고 persist한다고 했을 때 해당 엔티티에 Persistable< 키타입 >을 구현해주면 해결이 가능하다. isNew만 조금 손봐주면 된다. createDate는 JPA 이벤트 직전에 실행되므로 if문을 지날 때까지는 아직 비어있을 것이다. 따라서 이것을 이용하면 새로운 엔티티인지 아닌지를 쉽게 판단할 수 있다. 따라서 이렇게 id를 직접 할당해줄 때에 Persistable 인터페이스를 구현함으로서 Save에서 merge를 타지 않도록 하는 해결책을 알아보았다.  
여기서는 한 번에 다 보여주기 위해서 createDate를 직접 써줬는데 사실 BaseTimeEntity로 만들어 상속받아서 사용하고 @EntityListeners 에노테이션도 BaseTimeEntity 클래스에 붙이게 되고 글로벌 세팅으로 가져가면 생략할 수 있다.  
<br>

## 5. Projections
---
JPA에서는 결과를 받을 때 결국 엔티티이거나 DTO의 생성자로 받을 수 있는 방법뿐이 없다. Projections 기능을 사용하면 딱 원하는 해당 필드만 선택해서 조회가 가능하다. 근데 사실 그냥 엔티티 받아서 원하는 것 조회하는 것이랑 크게 성능차가 안난다. 그냥 이런게 있다 정도 알아두자.  
방식은 인터페이스 사용과 클래스 사용이 있다.
### 인터페이스 사용
인터페이스를 하나 만들어서 조회할 엔티티의 필드를 getter 형식 메서드로 만들어 두고 JPA 리포지토리에서 해당 메서드의 반환 타입으로 만들어둔 인터페이스를 꼽으면 구현체는 스프링 데이터 JPA가 제공한다.
```java
// 만든 인터페이스
public interface UsernameOnly {
    // 원하는 필드 getter 형식 메서드 
    String getUsername();
}

public interface MemberRepository ... {
    // 반환타입에 꼽기
    List<UsernameOnly> findProjectionsByUsername(String username);
}

// 실제 나가는 쿼리
select m.username from member m
where m.username= 파라미터로 받은 이름;
```
<br>

### 클래스 사용
```java
public class UsernameOnlyDto {
 private final String username;
 public UsernameOnlyDto(String username) {
 this.username = username;
 }
 public String getUsername() {
 return username;
 }
}

public interface MemberRepository ... {
 List<UsernameOnlyDto> findProjectionsByUsername(String username);
}
```
클래스로 사용하려면 getter을 직접 만들어줘야한다. 또한 클래스 생성자의 파라미터 명으로 매칭해서 가져오므로 생정자의 파라미터 명을 가져오고자 하는 것과 맞춰줘야 한다.  
<br>

나가는 쿼리가 똑같은데 받은 타입만 달라질 경우 제네릭을 사용하면 된다. 사용할 때 타입만 추가해서 넣어주면 된다.
```java
public interface MemberRepository ... {
    <T> List<T> findProjectionsByUsername(String username, Class<T> type);
}
```
<br>

### 정리
+ 프로젝션 대상이 root 엔티티일 경우, JPQL SELECT 절 최적화 가능
+ 프로젝션 대상이 root가 아니면(root 안에 있는 다른 엔티티) 결국 LEFT OUTER JOIN 처리와 함께 중첩구조 엔티티를 다 끌고와서 select한 다음에 조합 
+ 결론은 프로젝션 대상이 root 엔티티일 때만 사용



<Br><Br>

__참고__  
<a href="https://www.inflearn.com/course/%EC%8A%A4%ED%94%84%EB%A7%81%EB%B6%80%ED%8A%B8-JPA-%ED%99%9C%EC%9A%A9-1/" target="_blank"> 실전! 스프링 부트와 JPA 활용1</a>   


