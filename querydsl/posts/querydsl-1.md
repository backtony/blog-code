# Querydsl - 기본 문법

## 1. build.gradle 설정
``` java
plugins {
    id 'org.springframework.boot' version '2.6.2'
    id 'io.spring.dependency-management' version '1.0.11.RELEASE'
    id 'java'
}


group = 'com.example'
version = '0.0.1-SNAPSHOT'
sourceCompatibility = '11'

configurations {
    compileOnly {
        extendsFrom annotationProcessor
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-web'
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
    testImplementation 'org.springframework.boot:spring-boot-starter-test'

    // querydsl
    implementation 'com.querydsl:querydsl-jpa'
    annotationProcessor "com.querydsl:querydsl-apt:${dependencyManagement.importedProperties['querydsl.version']}:jpa"
    annotationProcessor "jakarta.persistence:jakarta.persistence-api"
    annotationProcessor "jakarta.annotation:jakarta.annotation-api"
}

```

<br>

```java
@Configuration
public class QuerydslConfig {

    @PersistenceContext
    private EntityManager em;

    @Bean
    public JPAQueryFactory jpaQueryFactory() {
        return new JPAQueryFactory(em);
    }

}
```

## 2. 테스트
---
잘 동작하는지 테스트 코드를 작성
```java
@SpringBootTest
@Transactional
class QuerydslApplicationTests {
	@Autowired
	EntityManager em;
	@Test
	void contextLoads() {
		Hello hello = new Hello();
		em.persist(hello);
		JPAQueryFactory query = new JPAQueryFactory(em);
		QHello qHello = QHello.hello; //Querydsl Q타입 동작 확인
		Hello result = query
				.selectFrom(qHello)
				.fetchOne();
		Assertions.assertThat(result).isEqualTo(hello);
//lombok 동작 확인 (hello.getId())
		Assertions.assertThat(result.getId()).isEqualTo(hello.getId());
	}
}
```



## 3. 기본 Q-Type 활용
---
```java
// 애노테이션 생략
@RequiredArgsConstructor
public class QuerydslBasicTest {
    Private final JPAQueryFactory queryFactory;
    
    // Querydsl을 사용하기 위해서는 JPAQueryFactory가 필요하다.
    // jpaQueryFactory를 만들때 생성자 파라미터로 EntityManager을 넣어줘야한다.
    public QuerydslBasicTest(EntityManager em){
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Test
    void startquerydsl() throws Exception{
        // compileQuerydsl로 만들어진 QXXX을 사용하여 query를 작성한다.
        // querydsl에서 사용하는 Member을 꺼내온다.
        // 결국 쿼리에서는 m을 기준으로 사용하게 된다.
        // QMember m = QMember.member;

        // 하지만 이것 또한 static import로 줄일 수 있다.       
        // 그냥 쿼리에서 전부 QMember.member하되 
        // Qmember을 static import하게되면 결론적으로 member만으로 사용 가능

        Member findMember = queryFactory
                .select(member)
                .from(member)
                .where(member.username.eq("member1")) // 파라미터 바인딩 처리
                .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
        
        // 같은 테이블을 조인해야하는경우 별칭이 같으면 안되므로
        // 다른 별칭을 사용해야함
        // member는 그대로 사용하고
        // QMember memberSub = new QMember("memberSub");
        // 따로 하나 만들어서 사용

        // queryFactory.selectFrom(member). ~~~
    }
}
```
+ compileQuerydsl로 만들어진 'Q+엔티티명' 을 사용하여 쿼리 작성
+ Q엔티티명.맨앞소문자엔티티명 은 Q엔티티의 생성자이다. Q엔티티를 static import해서 첫글자소문자엔티티명으로 간편하게 사용할 수 있다.
+ 파라미터 바인딩을 여러가지로 처리 가능한데 eq는 =를 의미
+ 같은 테이블을 조인해야 하는 경우 다른 별칭을 주어 사용한다.
+ JPQL에서 오타는 사용자 발생 시점에 run time error로 발생하지만 querydsl의 경우 컴파일 타임에 바로 오류가 잡힌다.
+ select와 from이 같은 파라미터를 가지면 selectFrom으로 합칠 수 있다.

<br>

## 4. 검색 조건 쿼리
---
Querydsl은 JPQL이 제공하는 모든 검색 조건을 제공한다. 아래 내용을 포함하고 뭐가 있는지 궁금하면 .를 눌러보면 쭉 나온다. 스프링 데이터 JPA에서는 바인딩을 @Param로 했는데 Querydsl은 () 안에 넣어주면 바인딩 된다.
```java
member.username.eq("member1") // username = 'member1'
member.username.ne("member1") //username != 'member1'
member.username.eq("member1").not() // username != 'member1'
member.username.isNotNull() //이름이 is not null
member.age.in(10, 20) // age in (10,20)
member.age.notIn(10, 20) // age not in (10, 20)
member.age.between(10,30) //between 10, 30
member.age.goe(30) // age >= 30
member.age.gt(30) // age > 30
member.age.loe(30) // age <= 30
member.age.lt(30) // age < 30
member.username.like("member%") //like 검색
member.username.contains("member") // like ‘%member%’ 검색
member.username.startsWith("member") //like ‘member%’ 검색
```
<br>

```java
// and, or도 가능
Member findMember = queryFactory
 .selectFrom(member)
 .where(member.username.eq("member1")
 .and(member.age.eq(10)))
 .fetchOne();

// and -> 쉼표 처리
queryFactory
 .selectFrom(member)
 .where(member.username.eq("member1"),
        member.age.eq(10))
 .fetch();
```
where 조건을 엮어줄 때 and()와 or을 사용할 수 있다. 그런데 and의 경우 쉼표(,)도 and로 인식하기 때문에 쉼표로 하는게 더 깔끔하다.  
<br>

## 5. 결과 조회
---
+ fetch() : 리스트 조회, 데이터 없으면 빈 리스트 반환
+ fetchOne() : 단 건 조회
    - 결과가 없으면 : null
    - 결과가 둘 이상이면 : com.querydsl.core.NonUniqueResultException
+ fetchFirst() : 첫 건만 조회, limit(1).fetchOne() 와 결과 동일
+ fetchResults() : 페이징 정보 포함, total count 쿼리 추가 실행
    - 스프링 데이터 JPA에서 사용했던 pageable과 동일
    - 쿼리가 복잡해지면 카운트 쿼리는 별도로 작성해야 함
    - 아래 예시 참고
+ fetchCount() : count 쿼리로 변경해서 count 수 조회


```java
@Test
void fetch() throws Exception{
    QueryResults<Member> result = queryFactory
            .selectFrom(member)
            .fetchResults();

    long total = result.getTotal(); // 모든 전체 개수 
    List<Member> content = result.getResults(); // 꺼내야 이것에 대한 데이터가 나옴        
    // result.getlimit getoffset등 페이징에 관한 정보를 가져올 수 있음
}
```
<br>

## 6. 정렬
---
+ desc() : 내림차순
+ asc() : 오름차순
+ nullsLast() : null을 제일 마지막으로
+ nullsFirst() : null을 제일 처음으로

```java
List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.eq(100))
                .orderBy(member.age.desc(), member.username.asc().nullsLast())
                .fetch();
```
nullsLast, nullsFirst를 사용하기 위해서는 바로 앞에 desc, asc를 사용해야만 한다.  
<br>

## 7. 페이징
---
페이징 쿼리를 가지고 앞서 설명했던 fetchResults로 정보를 빼낼 수 있다. 스프링 데이터 JPA와 마찬가지로 offset의 시작은 0이다.
```java
@Test
public void paging2() {
    QueryResults<Member> queryResults = queryFactory
            .selectFrom(member)
            .orderBy(member.username.desc())
            .offset(1)
            .limit(2)
            .fetchResults();
    
    // 총 데이터가 4개 들어있다고 가정
    assertThat(queryResults.getTotal()).isEqualTo(4); // 총 데이터는 4개 // 결과 개수가 아님!!
    assertThat(queryResults.getLimit()).isEqualTo(2); // limit 개수
    assertThat(queryResults.getOffset()).isEqualTo(1); // 첫 시작점
    assertThat(queryResults.getResults().size()).isEqualTo(2); // 페이징 결과로 뽑아낸 데이터의 개수
}
```
<br>

## 8. 집계와 그룹
---
### 집계
JPQL이 제공하는 모든 집함 함수를 제공한다. 
```java
@Test
public void aggregation() throws Exception {
    List<Tuple> result = queryFactory
            .select(member.count(),
                    member.age.sum(),
                    member.age.avg(),
                    member.age.max(),
                    member.age.min())
            .from(member)
            .fetch();
    
    Tuple tuple = result.get(0); 
    
    // get의 파라미터로 조회한 그대로를 넣으면 그에 대한 값이 나온다.
    assertThat(tuple.get(member.count())).isEqualTo(4);
    assertThat(tuple.get(member.age.sum())).isEqualTo(100);
    assertThat(tuple.get(member.age.avg())).isEqualTo(25);
    assertThat(tuple.get(member.age.max())).isEqualTo(40);
    assertThat(tuple.get(member.age.min())).isEqualTo(10);
}
```
Tuple은 Querydsl에서 제공하는 Tuple이다. 조회하는 것이 여러 개의 타입이 있을 때 사용한다.  

<br>

### 그룹
```java
List<Tuple> result = queryFactory
                .select(team.name, member.age.avg())
                .from(member)
                .join(member.team, team) // Qteam.team을 static import로 줄임
                // on으로 member.team_id = team.team_id 로 들어간다
                .groupBy(team.name)
                .fetch();
```
그루핑은 기존에 알던 방식과 조금 다른데 join의 파라미터로 엔티티를 넣어주면 on절로 id값들을 묶어준다. .having도 가능하다.  
<br>

## 9. 조인
---
### 기본 조인
```java
 List<Member> result = queryFactory
                .selectFrom(member)
                .join(member.team, team)
                .where(team.name.eq("teamA"))
                .fetch();

// 나가는 쿼리문
select
        member0_.member_id as member_i1_1_,
        member0_.age as age2_1_,
        member0_.team_id as team_id4_1_,
        member0_.username as username3_1_ 
    from
        member member0_ 
    inner join
        team team1_ 
            on member0_.team_id=team1_.team_id 
    where
        team1_.name=?

// 세타 조인
List<Member> result = queryFactory
                .select(member)
                .from(member, team)
                .where(member.username.eq(team.name))
                .fetch();

// 테스트 코드 간단하게
// member의 username을 teamA, teamB로 넣었다고 가정
assertThat(result)
                .extracting("username") // 모든 username 필드를 getter로 뽑아냄
                .containsExactly("teamA", "teamB"); // 그 중에 다음 것들이 있는지 확인
```
+ leftjoin, rightjoin 외부조인도 지원한다.
+ 세타 조인은 join으로 적는게 아니라 from에 나열하면 된다.
+ 테스트코드에서 extracting으로 result가 가진 모든 Member에 대해 해당 필드의 getter메서드를 통해 뽑아냈을 때 containsExactly로 해당 값들이 있는지 확인

<br>

### on 절
#### 기본적인 경우
```java
List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(member.team, team).on(team.name.eq("teamA"))
                .fetch();
```
+ on절은 조인 대상을 필터링하여 조인 대상을 줄여준다.
+ leftjoin의 파라미터가 id값끼리 on절로 묶인다.
+ 묶인 on 절 뒤에 and로 추가로 조건을 걸어주는 것이 on절이다.
+ 외부 조인이 아니라 내부조인의 경우는 on절을 사용하나 where절을 사용하나 똑같다.

<br>

#### 연관관계 없는 엔티티 외부 조인
연관관계가 없는 경우 조인을 하고 싶다면 그냥 on절로 묶어주면 된다. 하지만 문법이 약간 다르다.
```java
List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
                // join에 들어가는 파라미터 개수가 다름
                .leftJoin(team).on(member.username.eq(team.name))
                .fetch();
```
연관관계가 있는 경우에는 leftjoin의 파라미터로 2개를 주고 id값을 엮어줬는데 연관관계가 없는 경우에는 자동으로 엮어주지 못하므로 엮고 싶은 엔티티만 넣어주고 on절로 처리한다.  
<br>

### 페치 조인
페치 조인은 Lazy로 설정되어 있는 관계를 즉시로딩으로 땡겨오는 것이다. 사용법은 간단하다. join 옆에 fetchjoin만 붙여주면 된다. __JPA와 마찬가지로 페치 조인의 대상은 on 절과 where절에 사용할 수 없다.__
```java
 Member findMember = queryFactory
                .selectFrom(member)
                .join(member.team, team).fetchJoin()
                .where(member.username.eq("member1"))
                .fetchOne();

// 페치 조인 검증 테스트는 프록시의 초기화 여부를 확인하면 된다.
@PersistenceUnit
EntityManagerFactory emf;

// 프록시가 초기화 되었나 안되었나? boolean반환
boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
assertThat(loaded).as("페치 조인 적용").isTrue(); // 초기화 완료됬으면 True
```
<br>

## 10. 서브쿼리
---
JPQL 서브쿼리의 한계점으로는 from 절의 서브쿼리는 지원하지 않는다는 것이다. 당연히 Querydsl도 지원하지 않는다. 이에 대한 해결책은 앞서 공부했듯이, from 절의 서브쿼리를 join으로 변경하거나 애플리케이션에서 쿼리를 2번 분리해서 실행하거나, 최후의 경우에는 nativeSQL을 사용한다.  
```java
@Test
public void subQueryIn() throws Exception {
    // 서브쿼리를 위한 다른 별칭의 QMember
    QMember memberSub = new QMember("memberSub");

    List<Member> result = queryFactory
            .selectFrom(member)
            .where(member.age.in(
                    JPAExpressions
                            .select(memberSub.age)
                            .from(memberSub)
                            .where(memberSub.age.gt(10))
            ))
            .fetch();
    // 검증
    assertThat(result).extracting("age")
            .containsExactly(20, 30, 40);    
}
```
사용법은 간단하다. 서브쿼리의 시작을 __JPAExpressions.__ 으로 시작해서 이후에는 사용하던 그대로 작성하면 된다. __SQL에서 사용하듯이 서브쿼리의 바깥과 안쪽을 별칭이 달라야 한다.__ 따라서 서브쿼리에서 사용할 QMember은 새로 만들어서 사용해야 한다. JPAExpressions도 static import할 수 있는데 하게 되면 정말 SQL과 비슷하게 사용할 수 있다.  
<br>

## 11. Case문
---
DB에서 굳이 Case문을 사용해야할까를 고민해야 한다. 웬만하면 DB에서는 데이터를 필터링하고 그룹핑하는 정도로 최소한의 작업을 하고 전환하고 바꾸는 것은 DB에서 하기보다는 애플리케이션 작업하는 것이 좋다.

```java
// 단순 케이스문
List<String> result = queryFactory
 .select(member.age
 .when(10).then("열살")
 .when(20).then("스무살")
 .otherwise("기타"))
 .from(member)
 .fetch();

// 복잡한 조건의 case문
List<String> result = queryFactory
 .select(new CaseBuilder()
 .when(member.age.between(0, 20)).then("0~20살")
 .when(member.age.between(21, 30)).then("21~30살")
 .otherwise("기타"))
 .from(member)
 .fetch();
```
단순한 경우는 그냥 case문을 사용하면 되고 복잡한 경우에는 new CaseBuilder를 사용해야 한다. case문이 길어지게 되면 new CaseBuilder을 따로 변수로 빼고 변수를 넣어줘도 된다.


<br>

## 12. 상수, 문자 더하기
---
### 원하는 상수 찍기
```java
Tuple result = queryFactory
 .select(member.username, Expressions.constant("A"))
 .from(member)
 .fetchFirst();
```
Expression.constant("찍고 싶은 것") 을 넣으면 쿼리에서는 안나가고 결과문에서만 찍은것을 받는다.
<br>

### 문자 더하기
```java
String result = queryFactory
 .select(member.username.concat("_").concat(member.age.stringValue()))
 .from(member)
 .where(member.username.eq("member1"))
 .fetchOne();
```
SQL대로 concat을 이용하면 된다. concat은 문자를 연결해주는 것이므로 문자가 아닌 타입들은 __stringValue()__ 로 문자로 바꿔주고 사용하면 된다. __특히, ENUM을 처리할 때 자주 사용한다.__



<Br><Br>

__참고__  
<a href="https://www.inflearn.com/course/Querydsl-%EC%8B%A4%EC%A0%84#" target="_blank"> 실전! Querydsl</a>   


