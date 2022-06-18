# JPA 기본 - 다양한 연관관계 매핑

## 1. 다대일
---
### 단방향
![그림1](https://github.com/backtony/blog-code/blob/master/jpa/img/3/5-1.PNG?raw=true)

+ 가장 많이 사용하는 연관관계
+ 다대일 중 다에 @ManyToOne 붙여서 다대일을 명시
+ 다대일 중 다에 @JoinColumn 으로 FK 명시

<br>

### 양방향
![그림2](https://github.com/backtony/blog-code/blob/master/jpa/img/3/5-2.PNG?raw=true)

+ 다대일 단방향에서 테이블은 변화없이 일에서 다쪽으로 단방향 추가한 상태 -> 양쪽을 서로 참조하도록 개발
+ 다대일 중 다에는 단방향과 마찬가지로 @ManyToOne, @JoinColumn
+ 단방향과 다르게 다대일 중 일에는 @OneToMany(mappedBy="")를 붙여줌으로써 양방향임을 명시해주고 mappedBy로 연관관계 주인 명시
    - 다대일 중 일에 mappedBy를 명시해주면서 다를 연관관계 주인으로 지정

<br>

## 2. 일대다
---
대부분 다대일 관계를 사용하고 일대다는 거의 사용하지 않는다. 이유는 앞선 포스트에서 설명했듯이, 일대다의 경우 FK를 일에서 매핑해주게 되는데 나중에 일에서 값을 바꾸면 다쪽의 테이블에 쿼리가 나가면서 확인하기도 어렵고 여러개의 테이블을 다룰 때 복잡해질뿐만 아니라 성능이슈도 있기 때문이다. 따라서 __결론적으로는 일대다보다는 다대일을 사용하는게 좋다.__
<br>

### 단방향

![그림3](https://github.com/backtony/blog-code/blob/master/jpa/img/3/5-3.PNG?raw=true)
+ 일대다 중 일이 연관관계의 주인
+ 테이블에는 항상 다쪽에 외래 키가 있기에 객체기준에서 보면 반대편 테이블의 외래 키를 관리하는 특이한 구조
+ 일에 @OneToMany, @JoinColumn -> 일쪽에서 FK를 매핑시켰기 때문에 일쪽에서 작업하면 DB에서 다쪽 테이블로 쿼리가 나감 -> 실무에서 확인하기 힘들 수 있다.
+ @JoinColumn 반드시 사용, 사용 하지 않으면 조인 테이블 방식으로 중간에 테이블이 추가됨

<br>

### 양방향
![그림4](https://github.com/backtony/blog-code/blob/master/jpa/img/3/5-4.PNG?raw=true)

+ 공식적으로 존재하진 않으나 약간의 트릭을 써서 만든 것
+ 일에는 단방향과 마찬가지로 @OneToMany, @JoinColumn
+ 다쪽에는 @ManyToOne, @JoinColumn(insertable=false,updatable=false)
    - 공식적으로 존재하는게 아니기에 mappedBy를 사용할 수 없고, 속성으로 읽기 전용으로 만들어준 것

<br>

## 3. 일대일
---
+ 주 테이블이나 대상 테이블 중에 외래 키를 선택해서 넣으면 된다.
+ 일대일로 만들어야 하니 외래 키에 데이터베이스 __유니크 제약조건을 추가__ 해줘야 한다. -> 외래 키를 설정하는 joinColumn 부분에서 옵션으로 unique = true로 주면 된다.

<br>

### 주 테이블 외래 키 단방향
![그림5](https://github.com/backtony/blog-code/blob/master/jpa/img/3/5-5.PNG?raw=true)

+ 주 테이블에 외래 키를 설정한 단방향 모델
    - 주 테이블이란 주로 사용하는 테이블
+ 외래 키를 설정한 주 테이블의 객체에 @OneToOne, @JoinColumn

<br>

### 주 테이블 외래 키 양방향
![그림6](https://github.com/backtony/blog-code/blob/master/jpa/img/3/5-6.PNG?raw=true)

+ 외래 키를 설정한 주 테이블의 객체에는 @OneToOne, @JoinColumn
+ 반대편 객체에는 @OneToOne(mappedby="")

<br>

### 대상 테이블 외래 키 단방향
![그림7](https://github.com/backtony/blog-code/blob/master/jpa/img/3/5-7.PNG?raw=true)

일대다에서 사용했던 관계랑 똑같다고 보면 된다. 하지만 일대일에서는 이런 관계를 지원하지 않는다. 일대일 관계는 자신의 객체만이 자신의 테이블을 관리할 수 있다고 생각하면 된다. 

<br>

### 대상 테이블 외래 키 양방향
![그림8](https://github.com/backtony/blog-code/blob/master/jpa/img/3/5-8.PNG?raw=true)

주 테이블이 대상 테이블을 참조하는 건 당연한거고, 대상 테이블에 외래 키를 주고 싶다면 대상 객체는 당연히 주 객체를 참조하고 있어야 외래키를 매핑할 수 있다. 따라서 양방향이 될 수밖에 없다.  
Locker에 외래 키를 주고 싶다면 그냥 일대일 주 테이블 외래 키 양방향에서 애노테이션 위치만 바뀐 것이다.
<br>

### 정리
+ 주 테이블 외래 키
    - 대부분의 비즈니스는 웬만하면 주 테이블은 조회해와야 하기 때문에 주 테이블에 외래 키를 두게 되면 주 테이블만 조회해도 대상 테이블에 데이터가 있는 지 확인할 수 있고 JPA 매핑이 편리하다. 
    - 값이 없으면 외래 키에 null허용하는 단점
    - 객체지향 개발자 선호
+ 대상 테이블 외래 키
    - 양방향 구조만 지원
    - 일대일에서 일대다 관계로 변경할 때 테이블 구조 유지 장점
    - 프록시 기능의 한계로 지연 로딩으로 설정해도 항상 즉시 로딩 되는 단점
    - 데이터베이스 개발자 선호

<br>

## 4. 다대다
---
![그림9](https://github.com/backtony/blog-code/blob/master/jpa/img/3/5-9.PNG?raw=true)

설명하기에 앞서, 다대다 관계는 실무에서 사용하면 안된다. @ManyToMany 애노테이션을 사용하면 엔티티 없이 중간에 테이블을 하나 만들어서 매핑해주는데 사실 실무에서는 중간에 여러가지 변경, 추가 작업이 있기 때문에 이렇게 보이지 않는 테이블을 묶어서 만들어 놓으면 수정작업을 할 수가 없다.  
해결 방안으로는 @ManyToMany를 @OneToMany @ManyToOne 관계로 풀어서 중간에 연결 테이블용 엔티티를 추가로 만들어서 사용하는 것이다. 그럼 중간 테이블이 엔티티로 추가되었기 때문에 변경, 추가 작업같은 수정작업을 할 수 있게 된다.  
결론은 @ManyToMany 애노테이션은 사용하지 않고 다대다 관계는 일대다 다대일 관계로 풀어서 사용해야 한다.



<Br><Br>

__참고__  
<a href="https://www.inflearn.com/course/ORM-JPA-Basic/dashboard" target="_blank"> 자바 ORM 표준 JPA 프로그래밍 - 기본편</a>   

