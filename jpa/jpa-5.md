# JPA 기본 - 프록시와 연관관계 관리


## 1. 프록시
---
### 프록시를 왜 사용 하는가?
![그림1](https://github.com/backtony/blog-code/blob/master/jpa/img/5/7-1.PNG?raw=true)

위와 같이 Member에 프로퍼티로 Team을 가지고 있다고 하고 member 객체를 만들어서 값을 다 세팅했다고 가정하자. 그럼 em.find로 member을 조회하면 쿼리로 member와 team을 같이 땡겨올 것이다. 그런데 만약 내가 지금은 team은 사용하지 않을 것이고 member의 username 프로퍼티만 사용할 것이었다고 한다면 team은 굳이 조회하지 않아도 되는데 조회하게 된 것이다. 그럼 한 번에 땡겨오지 말고 내가 필요한 순간에 땡겨오게 하는 방법은 없을까? 그게 바로 프록시를 사용하는 이유이다.
<br>

### 프록시 기초
em.find을 사용하면 DB를 통해 쿼리를 날려 실제 엔티티 객체를 조회한다. 반면에 em.getReference()를 사용하면 DB로 쿼리를 날리지 않고 파라미터로 넣어준 클래스를 상속받은 가짜(프록시) 엔티티를 만들어서 프록시 객체가 영속성 컨텍스트에 들어간다. 프록시의 구성은 다음과 같다.
![그림2](https://github.com/backtony/blog-code/blob/master/jpa/img/5/7-2.PNG?raw=true)

파라미터로 받은 클래스를 상속받았기 때문에 모든 기능이 같고 다른점은 Entity target이라는 필드를 가지고 있다. 이것은 실제 객체의 참조를 보관하는 곳이다. 처음 em.getReference 시점에 받아온 프록시 엔티티는 target 값이 비어있다. 그럼 어떻게 진짜 객체를 참조할까? 과정은 다음과 같다.

![그림3](https://github.com/backtony/blog-code/blob/master/jpa/img/5/7-3.PNG?raw=true)

일단 getReference를 하면 프록시 객체가 영속성 컨텍스트에 있다. 처음에는 null을 참조하고 있다가 사용자가 객체의 프로퍼티를 사용하기 위해 메서드를 호출하면 프록시는 영속성 컨텍스트에 초기화 요청을 보내고 db에서 실제 Entity를 조회한다. 그리고 그것의 참조값을 target에 넣어준다.  
참고로 여기서 getId 같이 pk를 조회하면 초기화 요청을 하지 않는다. 왜냐하면 이미 파라미터로 id값을 줬으므로 id값은 이미 알고있기에 db에 쿼리를 날릴 필요가 없기 때문이다.
<br>

### 특징
__JPA에서 같은 pk로 꺼내는 값은 항상 == 비교가 성립해야 한다.__  
위의 내용을 기본으로 깔고 가면 아래 내용을 쉽게 이해할 수 있다.
+  영속성 컨텍스트에 찾는 엔티티가 이미 있으면 em.getReference()를 호출해도 영속성 컨텍스트에 있는 실제 엔티티가 반환
    - ex) find로 가져온 다음 getReference 하면 실제 엔티티 반환
+ getReference를 먼저하고 find 한다면 find 시점에 프록시가 반환 -> 항상 == 비교가 성립해야하기 때문
    - em.find() 같은 경우에는 실제 데이터가 존재하는 객체를 조회해야 하는데 이미 프록시 객체가 영속성 컨텍스트에 있다. 따라서 영속성 컨텍스트의 동일성을 보장하기 위해서 해당 프록시 객체를 그대로 반환하되, DB에 쿼리를 날려서 내부에서 프록시를 한번 초기화 해준다고 이해하면 된다.
+ 준영속 상태일 때, 프록시를 초기화하면 LazyInitializationException 예외 발생
    - em.getReference를 호출하고 아직 초기화하지 않은 상태에서 detach, close, clear 같이 준영속 상태로 만들어버리면 초기화할 수 없다는 의미이다. -> 애초에 초기화하려면 영속성 컨텍스트를 이용해야하기 때문
+ 프록시를 사용할 경우, 파라미터로 사용한 부모 클래스와 == 비교는 당연히 실패 -> 상속받았으므로 instanceof 사용

<br>

### 프록시 확인 메서드
+ emf.getPersistenceUnitUtil().isLoaded(Object entity) : 파라미터로 프록시를 넣으면 현재 프록시의 초기화 여부 확인
+ getClass : 프록시 클래스 확인
+ Hibernate.initialize(entity) : 파라미터로 프록시를 넣으면 강제로 초기화
    - JPA 표준은 강제 초기화가 없으므로 직접 객체의 메서드 호출해야함

사실 em.getReference를 직접 사용하는 일은 없고 즉시 로딩과 지연 로딩을 위해서 프록시란 것을 먼저 알아두어야 한다.

<br>

## 2. 즉시 로딩과 지연 로딩
---
+ 즉시 로딩 : find 파라미터로 넣은 엔티티를 한 번에 땡겨오기 위해 여러번의 쿼리가 나간다.
+ 지연 로딩 : find 파라미터로 넣은 엔티티를 땡겨오는데 프록시를 통해 나중에 가져오도록 한다.

```java
@Entity
public class Member {
    @Id
    @GeneratedValue
    private Long id;
    
    // @ManyToOne(fetch = FetchType.EAGER) // 즉시 로딩
    @ManyToOne(fetch = FetchType.LAZY) // 지연 로딩
    @JoinColumn(name = "TEAM_ID")
    private Team team; 
}
```
em.find(Member.class,id) 를 입력 시 Member 엔티티를 땡겨오는데 FetchType.LAZY이므로 지연 로딩으로 설정된 부분은 프록시로 땡겨온다. FetchType.EAGER로 설정하면 즉시 로딩으로 엔티티의 내용들을 다 채우기 위해 여러번의 쿼리가 나간다.
<br>

### 주의점
+ 실무에서는 모두 지연 로딩으로 우선 설정해 놓고 필요한 부분에서 JPQL fetch조인이나 엔티티 그래프 기능을 사용해서 원하는 부분에서만 즉시 로딩하도록 설정한다.
    - 즉시 로딩은 JPQL에서 N+1 문제를 일으킨다 -> 하나의 쿼리가 N개의 쿼리를 날리는 문제가 발생
    - 예를 들면, member만 필요해서 find 쿼리를 날렸는데 즉시 로딩으로 설정되어 있으면 만약 10개의 member을 찾았다면 안에 있는 team이 즉시 로딩이므로 10개의 쿼리가 더 나간다.
+ @ManyToOne, @OneToOne -> XXXToOne은 default가 즉시 로딩
+ @OneToMany, @ManyToMany -> XXXToMany은 default가 지연 로딩

### 결론
모든 연관관계는 지연 로딩을 사용하고 즉시 로딩이 필요한 경우는 지연 로딩이 붙여진 상태에서 fetch 조인이나 엔티티 그래프 기능을 사용하여 즉시 로딩을 한다.

<br>

## 3. 영속성 전이(cascade)
---
### 영속성 전이 cascade
특정 엔티티를 영속 상태로 만들 때 연관된 엔티티도 함께 영속 상태로 만들고 싶을 때 사용한다. 영속성 전이는 연관관계 매핑하는 것과 아무런 관련이 없고 영속화(persist)를 한 번에 처리하기 위한 편의를 위해 제공하는 것일 뿐이다.
<br>

### 영속성 전이를 사용하는 이유
```java
@Entity
public class Parent {
    
    @Id @GeneratedValue
    private Long id;
    
    @OneToMany(mappedBy = "parent")
    private List<Child> children = new ArrayList<>();
}

@Entity
public class Child {
    @Id @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "PARENT_ID")
    private Parent parent;
}
```
Parent와 Child가 일대다 양방향 관계로 매핑되어있다고 하고 child 두 개를 만들어 놓고 연관관계 편의 메서드로 값을 세팅해준 뒤 DB에 쿼리를 날리려면 persist로 parent, child1, child2를 차례로 넣어줘야 한다. 그렇다면 persist를 반복해서 사용해야 하는데 그냥 parent를 persist 하는 시점에 child까지 같이 persist하는 방법은 없을까? 이에 대한 방법이 영속성 전이(cascade) 이다.

__cf) parent를 먼저 persist하는 이유__  
child가 일대다 중에서 다에 속하므로 db에 매핑해주려면 FK를 무조건 가지고 있어야한다. FK를 가지려면 우선 parent를 persist해서 db에서 id값을 가져와야만 child에도 persist 시점에 FK를 가지고 들어갈 수 있다. 만약 child를 먼저 persist하면 fk가 없는 상태에서 들어가고 parent를 마지막에 persist하면 이미 insert 쿼리가 저장소에 저장되어있으므로 update쿼리가 추가되어 나간다. parent를 persist하지 않으면 child의 FK가 없기 때문에 insert 쿼리가 나가지도 않는다. 정리하자면, 다쪽에는 FK가 있어야하므로 일쪽 먼저 persist한다. 추가적으로 쉽게 생각해보면 양방향 관계에서 연관관계 주인은 다쪽에 해당하므로 다쪽에 수정작업을 하면 일쪽 db에 영향을 준다. 그럼 먼저 일쪽을 만들어놔야 영향이 가지 않을까 생각해보면 당연한 말이다. 따라서 일쪽은 먼저 persist 해야한다.
<br>

```java
@Entity
public class Parent {
    
    @Id @GeneratedValue
    private Long id;
    
    @OneToMany(mappedBy = "parent",cascade = CascadeType.ALL)
    private List<Child> children = new ArrayList<>();
}
```
위 코드와 같이 매핑 애노테이션의 속성으로 cascade의 값을 주면 된다. 값으로는 ALL, PERSIST, REMOVE, MBERGE, REFRECH, DETACH 가 있는데 ALL하면 모든 속성이 적용되고 PERSIST하면 영속만, REMOVE하면 삭제만 적용된다. 위와 같이 CASCADE를 사용했다면 parent를 persist하면 parent를 우선 persist하고 내부에 cascade 속성값이 persist 혹은 all이라면 해당 프로퍼티에 대해서도 persist를 해준다.
<br>

### 주의점
+ 참조하는 곳이 하나일 때만 사용해야 한다. 위의 코드에서는 Parent 엔티티만이 Child를 소유한다. 만약 Member 엔티티도 Child를 소유한다면 cascade는 사용하면 안된다.
+ __영속성 전이 cascade는 연관관계 매핑하는 것과 전혀 관련이 없다.__ -> 연관관계 매핑과 관련이 있었다면 children 리스트는 읽기전용이므로 리스트를 꺼내서 인덱스를 지워도 db에 반영되지 않아야 한다. 하지만 cascade는 매핑과 관련이 없기때문에 delete 쿼리를 날린다.

## 4. 고아 객체 제거
---
고아 객체는 부모 엔티티와 연관관계가 끊어진 자식 엔티티를 말한다. 

```java
@Entity
public class Parent {
    
    @Id @GeneratedValue
    private Long id;
    
    @OneToMany(mappedBy = "parent",cascade = CascadeType.PERSIST,orphanRemoval = true)
    private List<Child> children = new ArrayList<>();
}
```
위와 같이 매핑 애노테이션의 속성으로 orphanRemoval = true로 주었다. 만약 find로 Parent를 조회해서 getChildren으로 리스트를 가져온다음 remove로 0번째 인덱스를 지웠다고 해보자. 원래는 연관관계의 주인이 아니기 때문에 수정쿼리가 날라가지 않는데 orphanRemoval 속성에 의해 db에 반영되도록 Child테이블에 delete from 쿼리가 나간다.  
<br>

### 주의점
+ cascade와 마찬가지로 참조하는 곳이 하나일 때만 사용해야 한다.
+ @OneToXXX만 사용 가능하다. 즉, 일대X에서 일에서만 사용하는 것이다. 그래야만 자식처럼 하나를 가지거나(OneToOne) 혹은 자식을 여러명 List로(OneToMany) 가질 수 있기 때문이다.
+ find로 parent를 찾아와서 em.remove로 찾은 parent를 지워버리면 안에 있는 자식도 지워진다.(child가 다 지워짐) -> cascadeType.REMOVE 처럼 동작한다.(parent 지우면 cascadeType.REMOVE 속성을 가진 것들도 다 remove 쿼리가 나간다.)
    - 여기서 그럼 cascadeType.REMOVE를 쓰지 왜 orphanRemoval을 쓰냐고 생각할 수 있는데 orphanRemoval은 아래와 같은 기능도 한다.
+ 리스트에서 개별적으로 제거하면 부모와 연결이 끊어진 것으로 db에 해당 자식을 지우는 delete 쿼리가 나간다.

<br>

## 5. 영속성 전이 + 고아 객체 = 생명 주기
---
스스로 생명주기를 관리하는 엔티티는 em.persist로 영속화하고, em.remove로 제거한다. 위 예시 중에서 parent라고 보면 된다. Parent에서 children 리스트에 CascadeType.ALL과 orphanRemovel=true 값을 준다면 child의 생명 주기는 parent가 관리하게 된다. 즉, 부모 엔티티를 통해서 자식의 생명 주기가 관리된다. CascadeType.ALL에 의해 부모가 당하는 작업에 대해 똑같이 작업이 들어가고 orphanRemovel에 의해 부모에서 떨어지는 순간 delete 쿼리가 나가니 생명주기가 부모 엔티티에 달려있다고 보면 되는 것이다.



<Br><Br>

__참고__  
<a href="https://www.inflearn.com/course/ORM-JPA-Basic/dashboard" target="_blank"> 자바 ORM 표준 JPA 프로그래밍 - 기본편</a>   

