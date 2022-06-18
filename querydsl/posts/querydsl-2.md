# Querydsl - 중급 문법

## 1. 프로젝션 결과 반환
---
### 튜플
튜플은 Querydsl에서 제공하는 타입이다. 튜플을 리포지토리 계층을 넘어서서 서비스, 컨트롤러 계층까지 사용한다면 좋을 설계가 아니다. 하부 기술에 의존하지 않도록 설계해야 나중에 하부 기술을 바꾸더라도 문제가 없기 때문이다. 따라서 튜플은 리포지토리 계층안에서 사용하는건 괜찮지만 밖으로 던지는 것은 DTO로 변환하여 넘기는게 좋다.  
<br>

### DTO
기존에 JPQL로 쿼리를 작성했을 때 DTO로 프로젝션 받기 위해서는 DTO 생성자를 이용해 패키지명까지 전부 적어줬어야 했다. 하지만 Querydsl을 이용해면 패키지명 없이 간단히 작성할 수 있도록 3가지 방법을 제공한다.  
+ Projections.bean(클래스,변수...) : Setter 접근법
  - DTO의 일치하는 Setter를 호출해서 세팅
+ Projections.fields(클래스,변수...) : 필드 접근법
  - 일치하는 필드에 바로 세팅
+ Projections.constructor(클래스,변수...) : 생성자 접근법
  - 생성자 호출해서 세팅

__DTO를 프로젝션에서 사용하려면 반드시 DTO의 기본 생성자를 만들어야 하고 public으로 접근제한자를 두어야 한다.__

```java
// bean 방식을 예시로 들었는데 
//그냥 bean부분만 fields, constructor로 바꾸면 다른 방식으로 동작한다
List<MemberDto> result = queryFactory
                .select(Projections.bean(MemberDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();
```
Bean은 일치하는 프로퍼티로, fields는 일치하는 필드로, constructor은 일치하는 타입으로 매칭시켜주게 된다. 그런데 만약 엔티티의 필드와 DTO의 필드가 다르다면 어떻게 해결해야 할까? 예를 들면, Member은 username을 사용하는데 DTO는 name으로 사용한다면 말이다. 해결책은 별칭이다.

```java
List<UserDto> fetch = queryFactory
                .select(Projections.fields(UserDto.class,
                        // 엔티티 필드 username을 dto name에 매칭
                        member.username.as("name"), 
                        // 서브 쿼리의 결과값이 dto name에 매칭
                        ExpressionUtils.as(
                                JPAExpressions
                                        // 서브쿼리이므로 다른 QMember사용
                                        .select(memberSub.age.max())
                                        .from(memberSub), "age")
                        )
                ).from(member)
                .fetch();
```
+ 간단하게 필드명만 다를 경우 as를 이용해서 매칭시킨다.
+ 서브쿼리 같이 복잡한 것을 매칭시키기 위해서는 ExpressionUtils.as 를 사용한다.
  - JPAExpressions로 서브쿼리를 작성하고 ExpressionUtils.as로 감싸면서 마지막 파라미터로 dto 필드명을 적어주면 된다.

__cf) distinct__  
select().distinct() 를 사용한다.

<br>

### @QueryProjection 활용
이 방법이 사실상 가장 안전한 방법이다. DTO 생성자에 @QueryProjection만 붙여주고 JPQL에서 사용하던 방식에 패키지명 없이 사용하는 방식이다. 위에서 제시한 방법들은 애플리케이션 로딩 시점(런타임 시점)에 오류를 확인할 수 있어서 가장 안좋은 오류가 발생할 뿐만 아니라 매칭되지 않는 필드의 경우 오류가 발생하지 않고 그냥 무시된다. 하지만 이 방법은 컴파일 시점에 바로 오류를 확인할 수도 있고 ctrl + p 를 이용하면 타입도 바로바로 확인할 수 있다. 다만 __DTO 생성자에 Querydsl 애노테이션인 @QueryProjection을 사용__ 해야한다는게 단점이다. DTO는 여러 계층을 오고가는 클래스이기 때문에 하부 기술인 Querydsl을 다른 것으로 바꾸게 되면 코드를 수정해야 하는 문제가 생긴다. DTO를 깔끔하게 가져가기로 하는 프로젝트에서는 위에서 제시한 방법을 사용하고, 애플리케이션 전반적으로 Querydsl에 의존하고 있다면 이 방법을 사용하는게 가장 좋다.

```java
@Data
public class MemberDto {
    private String username;
    private int age;
    public MemberDto() {
    }
    // DTO 생성자에 붙여주기만 하면 끝
    @QueryProjection
    public MemberDto(String username, int age) {
        this.username = username;
        this.age = age;
    }
}

List<MemberDto> result = queryFactory
            // 패키지명만 뺀 JPQL방식 그대로 사용
            .select(new QMemberDto(member.username, member.age))
            .from(member)
            .fetch();
```
<br>

## 2. 동적 쿼리
---
동적 쿼리를 해결하는 방식으로 BooleanBuilder 방식, Where 다중 파라미터 방식 2가지가 있다. Where 다중 파라미터가 훨씬 간편하고 가독성이 좋다.  

### BooleanBuilder
```java
@Test
public void 동적쿼리_BooleanBuilder() throws Exception {
    String usernameParam = "member1";
    Integer ageParam = 10;
    // searchMember1 이라는 함수에 파라미터에 따라 다른 쿼리문 만들기
    List<Member> result = searchMember1(usernameParam, ageParam);
}
// 들어온 조건에 따라 쿼리문을 조합하여 쿼리문 날린 결과를 반환하는 메서드
private List<Member> searchMember1(String usernameCond, Integer ageCond) {
    // 동적 쿼리를 만들기
    BooleanBuilder builder = new BooleanBuilder(); 
    if (usernameCond != null) {
      // 이름 조건
        builder.and(member.username.eq(usernameCond));
    }
    if (ageCond != null) {
      // 나이 조건
        builder.and(member.age.eq(ageCond));
    }
    return queryFactory
            .selectFrom(member)
            .where(builder) // where문에 파라미터로 넘기기
            .fetch();
}

// 파라미터로 null이 들어오지 못하도록 방어코드를 작성했다면
// builder에 초기값을 줄 수 있다.
BooleanBuilder builder = new BooleanBuilder(member.username.eq(usernameCond).and(member.age.eq(ageCond))); 
```
BooleanBuilder을 만들어서 조건에 따라 and, or로 추가하여 만든 것을 where문의 파라미터로 넘겨서 동적 쿼리를 만들었다. 생성자의 파라미터로 초기값을 세팅할 수도 있다.
<br>

### Where 다중 파라미터 사용
앞서 설명했듯이 where문에서는 and 대신 쉼표(,)를 사용할 수 있다. 여기서 핵심은 __쉼표를 사용할 경우 null을 무시한다.__ 예를 들면 where(null, member.XX ) 이면 null은 무시되고 member.XX 만 조건으로 들어가는 것이다. 이것을 활용하면 코드를 매우 간단하게 만들 수 있다.

```java
@Test
void 동적쿼리() throws Exception{
    String usernameParam = "member1";
    Integer ageParam = 10;
    // 동적 쿼리 만들기
    List<Member> result = searchMember2(usernameParam,ageParam);
}

private List<Member> searchMember2(String usernameCond, Integer ageCond) {
    return queryFactory
            .selectFrom(member)
            // where 파라미터를 함수로 구성
            .where(usernameEq(usernameCond),ageEq(ageCond))
}

// 반환 타입 predicate -> BooleanExpression로 수정
private BooleanExpression usernameEq(String usernameCond) {
    // 삼항연산자가 간편한 경우 if문 대신 삼항연산자 사용
    return usernameCond != null ? member.username.eq(usernameCond) : null;
}

// 반환 타입 predicate -> BooleanExpression로 수정
private BooleanExpression ageEq(Integer ageCond) {
    return ageCond != null ? member.age.eq(ageCond) : null;
}

// 두 함수를 조합할 경우
// where문 파라미터에 이 함수를 넣으면 된다.
// 하지만 주의해야할 점이 조합한 경우 쉼표(,)가 아니라
// and로 들어가기 때문에 null의 경우 오류가 난다.
// 그러므로 사실 아래처럼 날리면 안되고
// null처리를 따로 해줘야 한다.
private BooleanExpression allEq(String usernameCond, int ageCond){
      return usernameEq(usernameCond).and(ageEq(ageCond));
  }
```
사실상 개발자가 코드를 볼때는 searchMember2를 보게 되고 where문에 들어가있는 함수명으로 어떤 동작을 하는지 유추할 수도 있어 가독성도 좋아진다. 또한 이렇게 만들어진 함수는 재활용성도 높고 조합도 가능하다. __이 함수들을 조합하고자 한다면 함수의 반환형을 BooleanExpression로 수정해야 한다.__ ctrl + alt + m 으로 함수로 뽑아내면 Predicate로 반환형이 잡히는데 만든 함수를 조합하지 않을 것이면 상관없지만 조합하고자 한다면 타입을 BooleanExpression로 수정해야 한다. 따라서 웬만하면 BooleanExpression로 바꿔두고 사용하도록 하자.  
<br>

## 3. 벌크 연산
---
```java
long count = queryFactory
            .update(member) // delete 도 가능
            // 수정할 필드, 수정
            .set(member.age, member.age.add(1))
            .execute(); // 다른 쿼리와 다르게 execute 사용
```
__벌크연산은 영속성 컨텍스트를 무시하고 DB에 날리므로 항상 실행 이후에는 영속성 컨텍스트를 비워줘야 한다.__ 스프링 데이터 JPA에서는 옵션으로 clearAutomatically을 사용하면 자동으로 비워줬는데 Querydsl에는 옵션이 없으므로 __em.clear()__ 로 꼭 비워주도록 하자. (작성한 쿼리가 나가기 전에 em.flush가 되기 때문에 flush는 할 필요 없음) 
set에는 파라미터로 수정할 필드와 수정값을 주면 되는데 수정하는 값이 숫자일 때 간단한 연산이 필요한 경우 add(), multiply() 를 사용한다. 마이너스는 없으므로 필요한 경우 add에 음수를 넣으면 된다.  
<br>

## 4. SQL function 호출
---
```java
List<String> = queryFactory
                .select(Expressions.stringTemplate(
                        "function('replace', {0}, {1}, {2})",
                        member.username, "member", "M"))
                .from(member)
                .fetchFirst();

// lower, upper과 같은 ansi 표준 함수들은
// expressions 없이 간단히 사용 가능
.where(member.username.eq(member.username.lower()))
```
+ JPA와 마찬가지로 Dialect에 등록된 함수를 사용할 수 있다. 
+ __Expressions.타입Template__ : Expressions를 이용해 함수를 사용할 수 있고, 타입에 맞는 Template을 선택해주면 된다.
+ function의 첫 파라미터는 함수명, 이후는 파라미터 바인딩 위치이다.


<Br><Br>

__참고__  
<a href="https://www.inflearn.com/course/Querydsl-%EC%8B%A4%EC%A0%84#" target="_blank"> 실전! Querydsl</a>   


