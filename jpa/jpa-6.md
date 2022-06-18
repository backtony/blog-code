# JPA 기본 - 값 타입

## 1. JPA 데이터 타입 분류
---
### 엔티티 타입
+ @Entity로 정의하는 객체
+ 데이터가 변해도 식별자로 추적 가능

<br>

### 값 타입
+ int, Integer, String처럼 단순히 값으로 사용하는 자바 기본 타입이나 객체
+ 식별자가 없고 값만 있으므로 변경시 추적 불가

<br>

### 값 타입 분류
+ 기본값 타입
    - 자바 기본 타입(int, double)
        - 기본타입은 참조값 복사가 아니라 값자체를 복사하는 것이기에 공유가 안된다.
    - 래퍼 클래스(Integer,Long), String
        - 래퍼 클래스와  String은 참조값을 공유하기 때문에 공유 가능한 객체이지만 변경이 불가능        
    - 생명 주기를 엔티티에 의존 -> 회원 삭제하면 이름, 나이 필드도 삭제된다.
+ 임베디드 타입
+ 컬렉션 값 타입


<br>

## 2. 임베디드 타입
---
기본 값 타입을 모아서 하나의 클래스로 만들어 놓은 것을 JPA에서는 임베디드 타입이라고 한다.
![그림1](https://github.com/backtony/blog-code/blob/master/jpa/img/6/8-1.PNG?raw=true)

Entity에 id, name, startDate, endDate, city, street, zipcode 필드가 있었다면 기간과 주소를 따로 class로 뽑아내어 만들어 놓고 Entity에서 사용할 때는 클래스를 가져와서 사용하는 것이다. 따로 뽑아낸 클래스에는 @Embeddable 애노테이션을 붙여 값 타입을 정의하는 곳으로 표시해두고, Entity에서 사용할 때는 @Embedded 애노테이션을 사용해서 값 타입을 사용한다고 표시해준다. 이렇게 따로 클래스로 관리해주면 이에 대한 의미 있는 메서드도 만들 수도 있고 재사용도 편리하다. 생명주기는 당연히 소유한 엔티티에 의존한다. __임베디드 타입도 엔티티처럼 스펙상 기본 생성자는 꼭 있어야한다.__ 참고로 임베디드 타입의 값으로 Entity를 가질 수도 있다.

```java
@Embeddable
public class Period{
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    // 게터, 다른 메서드 등등 응집성 있게 사용 가능
}
```
<br>

### 한 엔티티에서 같은 값 타입을 사용

```JAVA
@Embedded
private Address homeAddress;

@Embedded
@AttributeOverrides({
        @AttributeOverride(name="startDate",column = @Column("WORK_STARTDATE")),
        @AttributeOverride(name="endDate",column = @Column("WORK_ENDDATE")),
})
private Address companyAddress;
```
위와 같이 한 엔티티에 같은 값 타입을 사용한다면 컬럼명이 중복되므로 @AttributeOverrides로 각 필드에 대해서 컬럼명을 새로 지정해줘야 한다.  
참고로 임베디드 타입에 아무것도 넣어주지 않으면 모든 필드에 null이 들어간다.
<br>

### @MappedSuperclass vs 임베디드 타입
사실상 @MappedSuperclass와 임베디드 타입은 거의 똑같다. MappedSuperclass은 부모에 @MappedSuperclass을 붙이고 상속받아 사용하고, 임베디드 타입은 부모에 @Embeddable 애노테이션을 붙이고 사용하는 곳에 상속이 아니라 @Embedded만 붙여서 위임한다. 객체지향의 일반적인 법칙에 따르면 상속보다 위임이 좋기 때문에 위임을 보통 선택하지만 이 경우에는 편의에 따라 상속을 선택하는게 좋은 선택이 될 수도 있다.
```sql
-- 임베디드 타입
select m from Member m where m.traceDate.createdDate > ?

-- 상속
select m from Member m where m.createdDate > ?
```
임베디드 타입의 경우 JPQL에서 한 번 더 접근해서 사용해야 하지만 상속의 경우는 한 번만 접근하면 되므로 JPQL에서 조금 더 편리함이 있다. 어떤 것을 사용하든 상관없지만 코드를 줄이고 싶다면 상속을 사용하는게 더 좋은 선택이다.
<br>

### 주의점
같은 임베디드 타입을 여러 엔티티에서 공유하면 side effect가 발생한다.
```java
Address address = new Address("city","street","1111");

Member member = new Member();
member.setName("member1");
member.setHomeAddress(address);
em.persist(member);

// Address copyAddress = new Address(address.getCity(),address.getZipcode(),address.getZipcode());
// 복사한 값을 member2의 address값 세팅에 대입

Member member2 = new Member();
member.setName("member2");
member.setHomeAddress(address);
em.persist(member2);
```
기본 타입(primitive type)의 경우 = 을 사용하면 값을 참조하는게 아니라 값을 그냥 복사한다. 하지만 객체 타입의 경우 = 을 사용하거나 파라미터로 객체를 넘기면 객체가 복사되는게 아니라 객체의 참조값이 넘어간다. 여기서 문제가 생긴다. 위와 같이 임베디드 타입인 Address를 member1과 memeber2가 공유하고 있게되면 member1.getHomeAddress().setCity로 값을 변경해버리면 member2에 들어가있는 address에도 영향을 주게 된다. 이런 side effect는 실무에서 정말 찾기 어렵다. 따라서 주석처리 해놓은 것처럼 같은 객체를 넣는 것이 아니라 객체를 복사해서 새로운 객체를 복사해 만들고 그것을 넣어주는 것이 방법이다.  
<Br>

### 불변 객체
위의 주의점에서 연장선상에 있는 내용이다. 객체 타입을 수정할 수 없게 만들어야 side effect를 원천 차단할 수 있다. 따라서 값 타입은 생성 시점 이후 절대 값을 변경할 수 없는 객체인 불변 객체로 만들어야 한다. 생성자로만 값을 세팅하고 setter을 만들지 않으면 된다. __만약 값의 수정이 필요하다면 수정을 원하는 값만 수정하는게 아니라 새로운 address를 만들어서 갈아끼워줘야 한다는 단점이 있다.__ 다소 불편할지라도 후에 원인모를 오류를 예방할 수 있다.

<br>

## 3. 값 타입의 비교
---
비교에는 동일성 비교(참조값 비교 ==), 동등성 비교(인스턴스의 값 비교 equals)가 있다. 값 타입의 비교에서는 동등성 비교를 사용해야 한다. 당연히 다른 인스턴스이므로 == 비교하면 무조건 False가 나올 것이므로 equals 비교로 적절하게 재정의해서 사용해야 한다. 윈도우에서 alt+insert 해서 equals 치면 hash랑 같이 override해서 알아서 만들어 주는데 아래그림에서 use getters를 클릭해서 만들어주는게 좋다. 선택해주면 getter을 호출해서 만들어주는데 선택하지 않으면 필드에 직접 접근한다. 필드에 직접 접근하면 프록시일때 계산이 안되므로 getter를 이용해서 만들어주어야 한다.
![그림2](https://github.com/backtony/blog-code/blob/master/jpa/img/6/8-2.PNG?raw=true)


<br>

## 4. 값 타입 컬렉션
---
![그림3](https://github.com/backtony/blog-code/blob/master/jpa/img/6/8-3.PNG?raw=true)

관계형 db에는 컬렉션의 구조를 담을 수 있는 개념이 없다. 따라서 테이블을 따로 만들어서 관리한다. 사실 거의 사용하지 않고 이것 대신 일대다 관계를 사용한다. 그냥 이런게 있다 정도만 알아두자.   
[joinColumn 설명](https://backtony.github.io/jpa/2021/02/23/jpa-start-4/#1-%EB%8B%A8%EB%B0%A9%ED%96%A5-%EC%97%B0%EA%B4%80%EA%B4%80%EA%B3%84)

```java
@Entity
public class Member{
    // 생략

    // joinColumn에 관한 설명은 위 링크 참고
    
    @ElementCollection // 값 컬렉션임을 인지
    @CollectionTable(name ="ADDRESSES", // 테이블만들고 이름 설정
        joinColumns = @JoinColumn(name = "MEMBER_ID")) // 현재 엔티티를 참조하는 FK 설정
    // 컬럼명 바꾸러면 AttributeOverrides 사용하면 됨
    private List<Address> addresses = new ArrayList<>(); // 값 타입을 리스트
           

    @ElementCollection // 값 컬렉션임을 인지
    // 컬렉션 테이블 만들기, 테이블 이름, 외래키 설정
    @CollectionTable(name ="FAVORITE_FOOD",
    joinColumns = @JoinColumn(name = "MEMBER_ID"))
    @Column(name="FOOD_NAME") // 위와 달리 값이 하나고 내가 정의한 것이 아니므로 테이블 만들 때 컬럼명을 이처럼 수정 가능
    private Set<String> favoriteFoods = new HashSet<>();

    
}


// String 타입인 값타입 컬렉션은 그냥 add remove로 넣고 빼면 된다.
// 임베디드 타입인 값타입 컬렉션은 아래와 같이 처리한다.
// 대부분의 컬렉션은 equals 비교가 기본이기 때문에 반드시 overrided해놨어야 정상적으로 작동한다.
// REMOVE 시점, findMember에 찾아온 멤버가 담겨져있다고 가정
findMember.getAddresses().remove(new Address("city","street","1111"));
```
+ 값 타입을 하나 이상 저장할 때 사용
+ 기본값이 지연 로딩으로 설정되어있음 -> find 조회시 조회쿼리가 안나감
+ @ElementCollection : 값 컬렉션임을 명시
+ @CollectionTable : 컬렉션 테이블 설정
+ 값 타입 컬렉션은 영속성 전이 + 고아 객체 제거 기능을 필수로 가진다. 따라서 remove, add를 사용하면 알아서 쿼리가 나가고 생명주기는 엔티티에 달려있다.
+ 값 타입 컬렉션을 매핑하는 테이블은 모든 컬럼을 묶어서 기본 키를 따로 만들어줘야 함

### 값 타입 컬렉션의 주의점
remove에서 문제가 있다. 간단한 String 값 타입의 경우는 문제가 없는데 임베디드 타입같은 경우에 문제가 있다. __remove를 하는 시점에 내가 remove 파라미터로 날린 객체만 삭제가 되는게 아니라 일단은 전부 삭제하고 내가 지우지 않은 데이터 값을 다시 insert 쿼리로 넣어준다. 따라서 사실 실무에서는 이러한 이유와 값 타입 컬렉션을 매핑하는 테이블의 모든 컬럼을 묶어서 기본키를 구성해야한다는 이유(null X, 중복 저장 X)로 값 타입 컬렉션을 사용하지 않는다.__  
대안으로 일대다 관계를 사용한다. 값 타입으로 만들었던 클래스 대신 그냥 새로운 엔티티를 만들어 일대다 관계로 매핑하는 것이다. 거기에 영속성 전이 + 고아 객체 제거를 사용하면 값 타입 컬렉션처럼 사용이 가능해진다.
<br>

### 결론
__값 타입 컬렉션은 사용하기 보다는 엔티티를 새로 만들어 일대다 관계 매핑에 영속성 전이 + 고아 객체 제거를 사용하면 값타입 컬렉션처럼 사용할 수 있다.__  

```java
// 값 타입 컬렉션 대신 그냥 하나의 엔티티를 만들고 일대다 관계로 매핑
@Entity
public class AddressEntity{
    @Id @GeneratedValue
    private Long id;
    @Embedded // @Embeddable 적어줬으면 생략 가능
    private Address address;
}

@Entitty
public class Member{
    // 생략

    // 일대다 매핑 + 영속성 전이 + 고아 객체 제거 사용
    // 값 타입 컬렉션보다 훨씬 활용 가능성 높음, 최적화도 유리, 수정도 편리
    @OneToMany(cascade =CascadeType.ALL,orphanRemoval=true)
    @JoinColumn(name="MEMBER_ID")    
    private List<AddressEntity> addressHistory = new ArrayList<>();
}
```


<br>

## 5. 정리
+ 엔티티 타입의 특징
    - 식별자
    - 생명 주기 관리
    - 공유
+ 값 타입의 특징
    - 식별자 X
    - 생명 주기를 엔티티에 의존
    - 공유하지 않는 것이 안전 => 복사해서 사용
    - 불변 객체로 만드는 것이 안전
    - 값 타입 컬렉션 사용보다는 엔티티 하나 만들어서 일대다 + 영속성 전이 + 고아 객체 제거 사용


<Br><Br>

__참고__  
<a href="https://www.inflearn.com/course/ORM-JPA-Basic/dashboard" target="_blank"> 자바 ORM 표준 JPA 프로그래밍 - 기본편</a>   

