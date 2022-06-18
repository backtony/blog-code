# Querydsl - 활용


## 1. JPAQueryFactory 빈 등록
---
Querydsl을 사용하기 위해서는 JPAQueryFactory이 필요하고 JPAQueryFactory는 EntityManager을 주입받아야 한다. 두 가지 방법이 있다. 
+ 생성자를 직접 만들어 생성자의 파라미터로 em을 주입받고 거기서 JPAQueryFactory에 em을 new로 주입받는 방법
+ JPAQueryFactory를 Bean으로 등록해두고 lombok의 @RequiredArgsConstructor 을 사용하는 방법

사용하기에는 bean으로 등록해두고 하는 것이 편리하나 테스트 코드 작성시 두 개를 주입받으니 조금 귀찮아진다. Bean은 XXXApplication에 작성하거나 따로 AppConfig을 만들어서 등록하면 된다.

```java
// 생성자 방식
public XXX(EntityManager em){
    this.em = em;
    this.queryFactory = new JPAQueryFactory(em);
}


// 빈 등록 방식
@Bean
JPAQueryFactory jpaQueryFactory(EntityManager em){
    return new JPAQueryFactory(em);
}
```
<br>

## 2. 동적 쿼리 성능 최적화 조회 - Where절 파라미터 사용
---
```java
// 사용하려는 DTO
// 검색 조건으로 들어올 것 받을 DTO
@Data
public class MemberSearchCondition {

    private String username;
    private String teamName;
    private Integer ageGoe;
    private Integer ageLoe;
}


// 반환할 DTO
@Data
public class MemberTeamDto {

    private Long memberId;
    private String username;
    private int age;
    private Long teamId;
    private String teamName;

    @QueryProjection
    public MemberTeamDto(Long memberId, String username, int age, Long teamId, String teamName) {
        this.memberId = memberId;
        this.username = username;
        this.age = age;
        this.teamId = teamId;
        this.teamName = teamName;
    }
}
```
위의 DTO를 사용해서 검색 조건이 들어오면 세팅해서 MemberTeamDto로 반환하는 쿼리 리포지토리를 작성해보자. 

```java
public List<MemberTeamDto> search(MemberSearchCondition condition){
        // private final JPAQueryFactory queryFactory; 위쪽에서 주입받아서 사용
        
        // querydsl 사용하니까 DTO도 QXX 사용해야함
        // compileQuerydsl 로 먼저 QXX 만들어야함
        return queryFactory
                .select(new QMemberTeamDto(
                        // as로 DTO 필드명과 매칭
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")))
                .from(member)
                .leftJoin(member.team, team)
                .where(
                        // 검색 조건 쿼리 함수 만들고
                        // 파라미터로 들어온 검색 조건 넣기
                        usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe())
                )
                .fetch();
}

// predicate -> BooleanExpression
private BooleanExpression usernameEq(String username) {
    return hasText(username) ? member.username.eq(username):null;
}

private BooleanExpression teamNameEq(String teamName) {
    return hasText(teamName) ? team.name.eq(teamName):null;
}

private BooleanExpression ageGoe(Integer ageGoe) {
    return ageGoe != null ? member.age.goe(ageGoe):null;
}

private BooleanExpression ageLoe(Integer ageLoe) {
    return ageLoe != null ? member.age.loe(ageLoe):null;
}
```
+ Querydsl을 사용하므로 DTO 생성자도 QXX로 만들어진 것을 사용
+ 일단 쿼리를 작성하고 그 후에 Where 조건 생각해서 메서드 만들고 extract method로 빼서 만드는 순서로 진행
+ __문자열을 조건으로 받는 경우, null 또는 "" 공백이 들어올 수 있는데 이걸 다 처리해주는게 스프링 프레임워크의 StringUtils.hasText -> 위에서는 static import로 줄여서 사용__
+ Integer의 경우 null 비교

위 코드에서 만약 조건으로 모두 null이 들어오게 되면 모든 데이터를 땡겨서 반환하게 된다. 그렇게 되면 데이터가 많은 경우 많은 데이터를 꺼내게 되므로 문제가 발생한다. 따라서 반드시 페이징이 들어가 있거나 조건이 기본값으로 들어가 있는게 좋다. 이에 대해서는 아래 페이징에서 설명한다.

<br>

## 3. 프로파일 설정
---
테스트를 실행할 때와 로컬에서 tomcat을 띄울 때를 서로 다른 케이스(상황)를 가지고 할 때 프로파일 설정이 필요하다. tomcat으로 돌릴 때 샘플 데이터 추가 로직이 동작하게 만들고 테스트 케이스를 돌릴 때는 샘플 데이터를 넣는 로직이 동작하지 않도록 하는 것이다. 현재 테스트는 지금 DB에 데이터를 테스트에 맞게 딱 세트를 맞춰놨는데 샘플 데이터가 테스트에서도 존재하게 되면 테스트가 다 깨지게 되기 때문이다.  
test 폴더에 resources 폴더를 만들고 application.yml을 복붙하고 profiles 설정을 추가한다.
```yml
# main의 resources/application.yml
spring:
  profiles:
    active: local

# test의 resources/application.yml
spring:
  profiles:
    active: test
```
<br>

설정 파일은 위와 같이 추가만 해주면 되고 이제 tomcat을 띄울 때 샘플 데이터를 추가해주면 된다.
```java
@Profile("local") // 프로파일 설정
@Component
@RequiredArgsConstructor
public class InitMember {

    private final InitMemberService initMemberService;

    // 빈 등록과정에서 생성자 호출되고
    // 이후에 초기값 세팅으로 PostConstruct 붙은 메서드 실행
    @PostConstruct
    public void init(){
        initMemberService.init();
    }

    @Component // 내부 클래스도 따로 빈으로 등록해줘야함
    static class InitMemberService{
        @PersistenceContext
        private EntityManager em;

        @Transactional
        public void init(){
            Team teamA = new Team("teamA");
            Team teamB = new Team("teamB");

            em.persist(teamA);
            em.persist(teamB);

            for(int i=0;i<100;i++){
                Team selectedTeam = i%2 ==0 ? teamA:teamB;
                em.persist(new Member("member" + i, i, selectedTeam));

            }
        }
    }
}
```
+ __외부 클래스와 내부 클래스를 component 애노테이션으로 따로 등록해줘야 한다.__
+ @Transactional이 붙은 init을 바로 @PostConstruct에 작성하지 않는 이유는 스프링 라이프싸이클 때문에 __@PostConstruct와 @Transactional을 같이 붙여줄 수 없기 때문에 분리해줘야 한다.__

<br>

## 4. 사용자 정의 리포지토리
---
스프링 데이터 JPA에서 사용하던 것도 똑같다. JPA리포지토리를 만들고 Querydsl을 사용한 메서드를 만들고 싶다면 MemberRepositoryCustom(사실 아무 이름이나 상관 없음) 인터페이스를 만들고 이 구현체로 MemberRepositoryImpl(JPA리포지토리명+Impl 이 규칙)으로 만들어 구현하고 JPA리포지토리에 extends로 추가해주면 된다. JPA리포지토리가 핵심 리포지토리이므로 만약 특정 API, 화면에 특화된 경우라면 이렇게 추가방식이 아니라 MemberQueryRepository로 리포지토리를 새로 만들어서 사용하는게 좋다. 이에 관한 설명은 [[스프링 데이터 JPA 포스팅](https://backtony.github.io/jpa/2021-04-01-jpa-springdatajpa-1/#3-%ED%99%95%EC%9E%A5-%EA%B8%B0%EB%8A%A5){:target="_blank"}]을 참고하자.  
<br>

## 5. 페이징
---
### 카운트 쿼리 최적화가 필요 없는 경우(분리가 필요하지 않은 경우)
```java
// 리포지토리
// 검색 조건과 페이징 정보가 함께 파라미터로 들어옴
public Page<MemberTeamDto> searchPageSimple(MemberSearchCondition condition, Pageable pageable) {
        // querydsl 사용하니까 DTO도 QXX 사용해야함
        QueryResults<MemberTeamDto> results = queryFactory
                .select(new QMemberTeamDto(
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")))
                .from(member)
                .leftJoin(member.team, team)
                .where(
                        usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetchResults();

        List<MemberTeamDto> content = results.getResults();
        long total = results.getTotal();

        //pageImpl 은 sprint data JPA의 page의 구현체임
        // content, pageable, 데이터전체개수를 파라미터로 받음
        return new PageImpl<>(content,pageable,total);
    }
```
+ 검색조건에 따라 쿼리를 작성하고 뒤쪽에 offset으로 시작점, limit으로 데이터 개수를 정함
+ Querydsl에서 제공하는 페이징을 활용하기 위해 fetchResults 사용 -> count쿼리도 나가므로 총 2개의 쿼리가 나감
+ getResults로 정보 빼주고, getTotal로 전체의 모든 데이터 개수를 빼줌
+ PageImpl<> 는 스프링 데이터 JPA에서 제공하는 Page의 구현체로 파라미터로 content, pageable, total을 받는다.

참고로 orderby가 있다면 카운트 쿼리에서는 자동으로 orderby가 지워지고 쿼리가 나간다.

<br>

### 카운트 쿼리 최적화가 필요한 경우(분리가 필요한 경우)
기존 쿼리에 그대로 count로 쿼리가 나가기 때문에 기존 쿼리가 join같이 복잡해지는 경우 카운트 쿼리만 따로 분리하여 할 수 있다면 카운트 쿼리와 content 쿼리를 분리해야한다.
```java
public Page<MemberTeamDto> searchPageComplex(MemberSearchCondition condition, Pageable pageable) {
        // querydsl 사용하니까 DTO도 QXX 사용해야함
        // 검색 쿼리
        List<MemberTeamDto> content = queryFactory
                .select(new QMemberTeamDto(
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")))
                .from(member)
                .leftJoin(member.team, team)
                .where(
                        usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 카운트 쿼리 조립
        // 쿼리만 만들고 fetch같은것 사용하지 않음 
        JPAQuery<Member> countQuery = queryFactory
                .selectFrom(member)
                .leftJoin(member.team, team)
                .where(
                        usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe())
                );

        // 카운트 쿼리 생략과 같은 최적화를 위해
        // PageableExecutionUtils.getPage를 사용
        return PageableExecutionUtils.getPage(content, pageable,()->countQuery.fetchCount();
    }
```
+ 검색 쿼리는 최적화 전과 같은데 fetchResults가 아니라 fetch로 리스트로 content를 받음
+ 카운트 쿼리는 카운트만 하기 때문에 DTO로 만들 필요가 없고 어느 경우에 따라서는 join할 필요가 없었기 때문에 따로 작성하되 count 쿼리가 생략 가능한 경우 생략하기 위해 바로 fetchCount를 사용하지 않고 일단 쿼리문으로만 만들어 둔다.
    - 페이지 시작이면서 컨텐츠 사이즈가 페이지 사이즈보다 작을 때(컨텐츠 사이즈가 전체 사이즈(데이터 개수))
    - 마지막 페이지 일 때(offset + 컨텐츠 사이즈 -> 전체 사이즈(데이터 개수))
    - 위의 경우에는 괄호안에 같은 방식으로 전체 사이즈(데이터 개수)를 구할 수 있기 때문에 카운트 쿼리가 필요 없다.
    - PageableExecutionUtils.getPage 를 사용하면 생략이 가능한 경우 3번째 파라미터의 카운트 함수를 호출하지 않아 count 쿼리가 나가지 않도록 할 수 있다.
+ 모든 카운트 쿼리를 나눠서 최적화가 필요한게 아니라 정말 많은 join으로 인해 쿼리가 복잡해졌을 때만 카운트 쿼리를 따로 작성하는 식으로 설계하는게 좋다. 불필요한 경우의 최적화는 낭비일 뿐이다.



<br>

RestAPI 컨트롤러에서는 아래와 같이 간단히 호출하면 끝이다. DTO로 하나 감싸서 리스트가 아니라 객체로 반환하는게 좋기는 하나 이게 조건 검색 쿼리이므로 굳이 다른 데이터가 들어갈 필요가 없으므로 그냥 반환해도 될 것 같다.
```java
@GetMapping("/v3/members")
public Page<MemberTeamDto> searchMemberV3(MemberSearchCondition condition, Pageable pageable) {
    return memberRepository.searchPageComplex(condition, pageable);
}
```

<br>

## 6. 정렬
---
정렬은 조건이 조금만 복잡해져도 Pageable의 sort기능을 사용하기 어렵다. 루트 엔티티 범위를 넘어가는 동적 정렬 기능이 필요하면 스프링 데이터 페이징이 제공하는 Sort를 사용하기 보다는 파라미터를 받아서 직접 처리하는게 권장된다.





<Br><Br>

__참고__  
<a href="https://www.inflearn.com/course/Querydsl-%EC%8B%A4%EC%A0%84#" target="_blank"> 실전! Querydsl</a>   


