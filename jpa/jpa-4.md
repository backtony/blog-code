# JPA 기본 - 고급 매핑

## 1. 상속관계 매핑
---
관계형 데이터베이스는 상속 관계라는게 없고 그나마 슈퍼타입 서브타입 관계라는 모델링 기법이 객체의 상속과 유사하다. 그래서 상속 관계 매핑을 객체의 상속 구조와 DB의 슈퍼타입 서브타입 관계를 매핑하는 형식으로 한다. 이에 대해 슈퍼타입 서브타입 논리 모델을 실제 물리 모델로 구현하는 전략은 3가지가 있다.
![그림1](https://github.com/backtony/blog-code/blob/master/jpa/img/4/6-1.PNG?raw=true)

전략을 설명하기 앞서 Item은 혼자 사용되는 경우가 없고 album,movie,book이 상속받아서 사용하는 것이므로 Item은 추상 클래스로 설정해야 한다.
<br>

### 조인 전략
![그림2](https://github.com/backtony/blog-code/blob/master/jpa/img/4/6-2.PNG?raw=true)

+ 조인 전략이 제일 좋은 전략이다.
+ 조회시에 join으로 인한 성능 저하가 있다.
+ 추상 클래스와 구현 클래스 모두 테이블이 만들어진다.
+ 운영을 위해서 @DiscriminatorColumn으로 DTYPE열을 만들어주는게 좋다.


```java
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn // DTYPE 열 만들어주는 애노테이션 name 속성으로 DTYPE말고 다른 이름으로 변경 가능
// DTYPE은 만들어주는게 좋음운영하기 편하기 위해
public abstract class Item {// Item을 단일로 사용하는 경우는 절때없음 -> 추상 클래스

    @Id @GeneratedValue
    private Long id;

    private String name;
    private int price;
}
```
자바코드에서는 그냥 하던대로 Item을 추상클래스로 나머지 클래스는 extends로 상속받아서 코딩하면 끝이고, 추상클래스에 @Inheritance(strategy = InheritanceType.JOINED) 애노테이션을 붙여주면 db에는 알아서 적용된다. 쿼리가 나가는 것을 보면 Item, Album, Movie, Book 테이블이 만들어진다. 만약 Book에 author,isbn, name, price를 세팅하고 persist한다면 insert 쿼리는 book 테이블과 item 테이블로 총 두번이 나가게 되고 find로 찾아오게 되는 경우는 Item의 PK와 book의 FK가 조인되어 select 쿼리가 나간다. 따라서 조회시 조인과 저장시 insert 두번 호출로 성능이 저하될 수 있지만 테이블의 정규화, 외래 키 참조 무결성 제약조건, 저장공간 효율화로 제일 좋은 전략이라고 볼 수 있다.  
@DiscriminatorColumn 애노테이션은 DTPYE열을 만들어주는 애노테이션이다. name 속성으로 열이름을 DTYPE에서 다른 이름으로 변경할 수 있다. DTYPE은 만들어 주는게 좋다. 운영에서 db에 item만 select 쿼리를 날리면 어디서 들어온 건지 모르므로 dtype으로 나타내어주는게 좋다. 상속받은 클래스에서는 DTYPE 열에 클래스명 그대로 들어간다. 예를 들어, Book 클래스면 DTYPE열에 Book이라고 찍히는데 이를 바꾸고 싶다면 해당 클래스에 @DiscriminatorValue("B") 애노테이션으로 이와 같이 B로 바꿀 수도 있다.

<br>

### 싱글 테이블 전략

![그림3](https://github.com/backtony/blog-code/blob/master/jpa/img/4/6-3.PNG?raw=true)


+ 추상 클래스 테이블이 만들어지고 이 테이블로 전부 관리
+ @DiscriminatorColumn 애노테이션 없이 기본으로 DTYPE열이 생성
+ 자식 엔티티가 매핑한 컬럼은 모두 null허용 -> 치명적 단점


```java
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public abstract class Item {// Item을 단일로 사용하는 경우는 절때없음 -> 추상 클래스

    @Id @GeneratedValue
    private Long id;

    private String name;
    private int price;
```
싱글 테이블 전략은 말 그대로 하나의 테이블에서 모든 것을 관리한다는 뜻이다. Album, Book, Movie 클래스가 상속받아서 엔티티로 등록되어도 테이블은 ITEM 테이블은 만드는 쿼리만 나가고 이것들의 구분은 DTYPE으로 한다. 싱글 테이블 전략의 경우 조인 전략과 달리 전략 자체에 DTYPE이 기본값으로 들어가 있다. 하나의 테이블에 모든 정보가 들어있으므로 조인 전략과 다르게 조회시 조인이 필요 없고, 삽입도 한 번만 삽입하면 되기때문에 성능상 빠르다. 하지만 자식 엔티티가 매핑한 컬럼은 모두 null을 허용해야하기 때문에 치명적인 단점이 있다.
<br>


### 구현 클래스마다 테이블 전략
```java
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
```
item 테이블 없이 각각의 구현 클래스만 테이블이 만들어지는데 조회할 때 모든 것을 다 찾아봐야하고 통합해서 쿼리하기 어려우므로 사용하지 않는게 좋다.
<br>

### 결론
결론적으로 조인 전략을 사용하되 매우 간단한 경우라면 싱글 테이블 전략을 사용하자.


<br>

## 2. @MappedSuperclass
---

![그림4](https://github.com/backtony/blog-code/blob/master/jpa/img/4/6-4.PNG?raw=true)

+ 자바에서는 그냥 상속받아 사용하되 추상 클래스에 @MappedSuperclass 애노테이션을 붙여주면 끝

객체끼리 공통되는 속성(프로퍼티)가 겹치는 경우 객체마다 일일이 작성해주지 않고 특정 클래스를 만들어서 상속받아서 사용하는게 훨씬 편할 것이다. 이것을 자바안에서는 그냥 상속받아서 하면 되지만, db에서도 이 행위를 인식시키기 위해서 @MappedSuperclass 애노테이션을 사용한다. 공통 속성들을 모아놓은 클래스는 당연히 혼자 사용할 일이 없으므로 abstract 추상 클래스로 만들어 주는 것이 좋고 여기다가 @MappedSuperclass 애노테이션을 붙이면 된다. 이 추상 클래스는 당연히 db상에는 올라가지 않으므로 find로 추상클래스를 넣으면 찾아올 수 없다.  

<br>

## 3. 정리
---
+ 상속관계 매핑
  - 객체간의 상속관계를 DB에 적용시키기 위한 작업
  - 부모 클래스에 @Inheritance 애노테이션 붙여서 상속관계 명시
  - JOINED 전략이 제일 합리적, 매우 간단하면 SINGLE_TABLE 전략 사용
  - 부모, 자식 클래스 모두 @Entity
+ @MappedSuperclass
  - 객체끼리 공통되는 속성(프로퍼티)를 뽑아서 만든 클래스
  - Item을 부모로 book, album 같이 포함되는 상속관계가 아니라 id, name같이 객체끼리 공통되는 필드가 겹칠 때 사용
  - 자바에서는 상속으로 사용하나 상속관계 매핑과 다르게 __엔티티가 아니라 DB상에 올라가지 않음__
  




<Br><Br>

__참고__  
<a href="https://www.inflearn.com/course/ORM-JPA-Basic/dashboard" target="_blank"> 자바 ORM 표준 JPA 프로그래밍 - 기본편</a>   

