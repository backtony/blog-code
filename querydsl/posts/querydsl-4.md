# Querydsl - 성능 개선


## 1. exist 메서드 개선
---
기본적으로 JPA에서 제공하는 exists는 조건에 해당하는 row 1개만 찾으면 바로 쿼리를 종료하기 때문에 전체를 찾아보지 않아 성능상 문제가 없습니다. 
하지만 조금이라도 복잡하게 되면 메소드명으로만 쿼리를 표현하기 어렵기 때문에 보통 @Query를 사용합니다.  
여기서 문제가 발생합니다. JPQL의 경우 select의 exists를 지원하지 않습니다.(where문의 exists는 지원) 따라서 count쿼리를 사용해야 하는데 이는 총 몇건인지 확인을 위해 전체를 봐야하기 때문에 성능이 나쁠 수 밖에 없습니다.  
이를 개선하기 위해서 Querydsl의 selectOne과 fetchFirst(= limit 1)을 사용해서 직접 exists 쿼리를 구현해서 개선해야 합니다.  
```java
public Boolean exist(Long bookId){
    Integer fetchOne = queryFactory
        .selectOne()
        .from(book)
        .where(book.id.eq(bookId))
        .fetchFirst(); // 한건만 찾으면 바로 쿼리 종료(limit 1)
    
    return fetchOne != null;
}
```
조회결과가 없으면 null이 반환되기 때문에 null로 체크해야 합니다.  

<br>

## 2. Cross Join 회피
---
### 문제 상황
```java
public List<Customer> crossJoin(){
    return queryFactory
        .selectFrom(customer)
        .where(customer.customerNo.gt(customer.shop.shopNo))
        .fetch();
}

// 쿼리 결과
select
    ....
from
    customer customer_cross // cross 가 cross 조인을 의미함
join
    shop shop1_
where
    ....
```
where 문에서 customer.shop 코드로 인해 묵시적 join으로 Cross Join이 발생합니다.  
일부의 DB는 이에 대해 어느정도 최적화가 지원되나 최적화 할수 있음에도 굳이 DB가 해주길 기다릴 필요는 없습니다.  
```java
@Query("SELECT c FROM Customer c WHERE c.customerNo > c.shop.shopNo")
List<Customer> crossJoin();

// 쿼리 결과
select
    ....
from
    customer customer_cross // cross 가 cross 조인을 의미함
join
    shop shop1_
where
    ....
```
이는 Hibernate 이슈라서 Spring Data JPA도 동일하게 발생합니다.  

### 해결방안
```java
public List<Customer> notCrossJoin(){
    return queryFactory
        .selectFrom(customer)
        .innerJoin(customer.shop, shop)
        .where(customer.customerNo.gt(shop.shopNo))
        .fetch();
}
```
실제 join문을 작성하고 alias로 해당 엔티티를 선언하면 명시적 조인이 되기 때문에 outer join이나 inner join처럼 내가 원하는 해당 조인으로 쿼리가 발생하게 됩니다.

<br>

## 3. Entity 보다는 Dto를 우선
---
Entity 조회시 Hibernate 캐시, 불필요한 컬럼 조회, OneToOne N+1 쿼리 등 단순 조회 기능에서는 성능 이슈 요소가 많습니다.  
+ Entity 조회
    - 실시간으로 Entity 변경이 필요한 경우
+ Dto 조회
    - 고강도 성능 개선 or 대량의 데이터 조회가 필요한 경우

### 조회컬럼 최소화하기
```java
// BAD
public List<BookPageDto> getBooks (int bookNo, int pageNo){
    return queryFactory
            .select(Projections.fields(BookPageDto.class,
                    book.name,
                    book.bookNo, // 이미 알고 있는 값으로 조회할 필요가 없음
                    book.id
            ))
            ....
}

// GOOD
public List<BookPageDto> getBooks (int bookNo, int pageNo){
    return queryFactory
            .select(Projections.fields(BookPageDto.class,
                    book.name,
                    Expressions.asNumber(bookNo).as("bookNo"),
                    book.id
            ))
            ....
}
```
이미 알고 있는 값은 asXX 표현식으로 대체할 수 있어 asXX 컬럼은 select에서 제외시킬 수 있습니다.

### Select 컬럼에 Entity 자제
```java
// BAD
queryFactory
    .select(Projections.fields(AdBond.class,
    adItem.amount.sum().as("amount"),
    adItem.txDate,
    adItem.orderType,
    adItem.customer) // AdBond와 연결된 Customer 엔티티를 조회하게 된다.
    )
    .from(adItem)
    .where(adItem.orderType.in(orderTypes))
        .and(adItem.txDate.between(startDate, endDate()))
    .groupBy(adItem.orderType, adItem.txDate, adItem.customer)
    .fetch();

// GOOD
queryFactory
    .select(Projections.fields(AdBond.class,
    adItem.amount.sum().as("amount"),
    adItem.txDate,
    adItem.orderType,
    adItem.customer.id.as("customerId")) // id만 조회
    )
    .from(adItem)
    .where(adItem.orderType.in(orderTypes))
        .and(adItem.txDate.between(startDate, endDate()))
    .groupBy(adItem.orderType, adItem.txDate, adItem.customer)
    .fetch();

// 실제 사용 사례
public AdBond toEntity(){
    return AdBond.builder()
            ...
            // AdBond가 저장되는 시점에는 Customer의 Id 외에는 다른 값이 필요 없다.
            .customer(new Customer(customerId)) 
            .build();
}       
```
QueryDSL로 조회된 결과를 신규 Entity로 생성하기 위해서 위와 같은 쿼리를 작성했습니다.  
목적은 새로 만들게 될 AdBound와 관계를 맺을 Customer의 Id값만 필요한데 이 경우 Customer의 모든 컬럼이 조회됩니다.  
여기서 더 문제가 되는 것은 Customer이 shop 엔티티와 OneToOne 관계를 갖고 있을 경우, shop이 매 건마다 조회가 되어 N+1 상황이 발생합니다.(OneToOne 관계는 null인 경우 때문에 Lazy Loading을 허용하지 않음)  
위의 코드에는 없지만 만약 distinct가 있을 경우 customer의 모든 컬럼이 대상에 포함되기 때문에 distinct를 위한 임시 테이블을 만드는 공간, 시간이 낭비되게 됩니다.
<br>

## 4. Group By 최적화
---
MySQL에서 Group By를 실행하면 별도의 Order by이 쿼리에 포함되어 있지 않음에도 Filesort(정렬 작업이 쿼리 실행시 처리되는)가 필수적으로 발생합니다.  
인덱스에 있는 컬럼들로 Group by를 한다면 이미 인덱스로 인해 컬럼들이 정렬된 상태이기 때문에 큰 문제가 되지 않으나 굳이 정렬이 필요 없는 Group by에서 정렬을 다시 할 필요는 없기 때문에 이 문제를 해결해야 하는 것이 좋습니다.
```sql
select 1
from ad_offset
group by customer_no
order by null asc;
```
MySQL에서는 order by null을 사용하면 Filesort가 제거되는 기능을 제공하지만 이는 QueryDSL에서는 지원되지 않습니다.  
따라서 이를 직접 구현해야 합니다.
```java
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.NullExpression;

public class OrderByNull extends OrderSpecifier {
    public static final OrderByNull DEFAULT = new OrderByNull();

    private OrderByNull() {
        super(Order.ASC, NullExpression.DEFAULT, NullHandling.Default);
    }
}

// 실제 사용
...
.groupBy(...)
.orderBy(OrderByNull.DEFAULT)
.fetch();
```
null을 그냥 넣게 되면 Querydsl의 정렬을 담당하는 OrderSpecifier 에서 제대로 처리하지 못합니다. Querydsl에서는 공식적으로 null에 대해 NullExpression.DEFAULT 클래스로 사용하길 권장하니 이를 활용합니다.  
__단, 페이징일 경우, order by null을 사용하지 못하므로 페이징이 아닌 경우에만 사용해야 합니다.__  
<br>

정렬이 필요하더라도, 조회 결과가 100건 이하라면, 애플리케이션에서 정렬해야합니다.  
일반적인 자원의 입장에서 DB보다는 WAS의 자원이 더 저렴합니다. DB는 3~4대를 사용하더라도 WAS는 수십대를 유지하는 경우가 빈번합니다. 따라서 정렬이란 자원이 필요할 경우 WAS가 DB보다는 여유롭기 때문에 WAS에서 처리하는 것이 좋습니다.  

<br>

## 5. 커버링 인덱스
---
쿼리를 충족시키는데 필요한 모든 컬럼을 갖고 있는 인덱스로 select / where / order by /group by 등에서 사용되는 모든 컬럼이 인덱스에 포함된 상태를 의미합니다.  
select 절에서 *를 이용하여 단순히 조회할 경우(where 조건문에 Non Clustered Key(보조 인덱스)를 사용한 경우) Non Clusterd Key에 있는 Clusted Key를 이용해 다시 실제 데이터 접근을 하여 데이터를 가져오게 됩니다.[[참고](https://jojoldu.tistory.com/476){:target="_blank"}]  
결과적으로 1차적으로 보조 인덱스에 대해 검색하고 2차적으로 cluster index에 대해 검색하게 되는 것입니다.  
커버링 인덱스를 사용할 경우 1차적인 검색만으로 끝낼 수 있게 됩니다.  

<br>

Book 테이블을 만들고 예시를 들어보겠습니다.
```sql
create table book(
	id bigint not null auto_increment,
    book_no bigint not null,
    name varchar(255) not null,
    type varchar(255),
    primary key(id),
    key idx_name(name)
);

select id, book_no, book_type, name
from book
where name like '200%'
order by id desc
limit 10 offset 10000;
```
위의 select 문에서는 book_no와 book_type이 인덱스가 아니기 때문에 커버링 인덱스가 될 수 없습니다.  
결국에는 pk값으로 2차적인 접근을 한다는 뜻인데, 그렇다면 pk값을 커버링 인덱스로 빠르게 가져오고 해당 pk값을 조건문으로 넣으면 pk값에 대한 조회로 빠르게 가져올 수 있을 것입니다.  
따라서 Cluster Key(PK)를 커버링 인덱스로 빠르게 조회하고, 조회된 Key로 Select 컬럼들을 후속조회 하는 방식을 사용해야 합니다.  
```java
public List<BookPaginationDto> paginationCoveringIndex(String name, int pageNo, int pageSize) {
        // 1) 커버링 인덱스로 대상 조회
        List<Long> ids = queryFactory
                .select(book.id)
                .from(book)
                .where(book.name.like(name + "%"))
                .orderBy(book.id.desc())
                .limit(pageSize)
                .offset(pageNo * pageSize)
                .fetch();

        // 1-1) 대상이 없을 경우 추가 쿼리 수행 할 필요 없이 바로 반환
        if (CollectionUtils.isEmpty(ids)) {
            return new ArrayList<>();
        }

        // 2)
        return queryFactory
                .select(Projections.fields(BookPaginationDto.class,
                        book.id.as("bookId"),
                        book.name,
                        book.bookNo,
                        book.bookType))
                .from(book)
                .where(book.id.in(ids))
                .orderBy(book.id.desc())
                .fetch(); 
}
```


<Br>

## 6. No Offset 으로 구조 변경하기
---
기존에 사용하는 페이징 쿼리는 일반적으로 아래와 같습니다.
```sql
SELECT *
FROM items
WHERE 조건문
ORDER BY id DESC
OFFSET 페이지번호
LIMIT 페이지사이즈
```
이와 같은 페이징 쿼리가 뒤로 갈수록 느린 이유는 __앞에서 읽었던 행을 다시 읽어야 하기 때문__ 입니다.  
예를 들어 offset이 10000이고 limit이 20이라면 결과적으로 10000개부터 20개를 읽어야하니 10020개를 읽고 10000개를 버리는 행위와 같습니다.  

<br>

No Offset 방식은 __조회 시작 부분을 인덱스로 빠르게 찾아 매번 첫 페이지만 읽도록 하는 방식__ 입니다.  
```java
public List<BookPaginationDto> paginationLegacy(String name, int pageNo, int pageSize) {
    return queryFactory
            .select(Projections.fields(BookPaginationDto.class,
                    book.id.as("bookId"),
                    book.name,
                    book.bookNo))
            .from(book)
            .where(
                    book.name.like(name + "%") // like는 뒤에 %가 있을때만 인덱스가 적용됩니다.
            )
            .orderBy(book.id.desc()) // 최신순으로
            .limit(pageSize) // 지정된 사이즈만큼
            .offset(pageNo * pageSize) // 지정된 페이지 위치에서 
            .fetch(); // 조회
}
```
기존 코드가 위와 같이 offset + limit 까지 읽어와서 offset을 버리고 반환하는 형식 입니다.  
<br>

```java
public List<BookPaginationDto> paginationNoOffset(Long bookId, String name, int pageSize) {

    return queryFactory
            .select(Projections.fields(BookPaginationDto.class,
                    book.id.as("bookId"),
                    book.name,
                    book.bookNo))
            .from(book)
            .where(
                    ltBookId(bookId),
                    book.name.like(name + "%")
            )
            .orderBy(book.id.desc())
            .limit(pageSize)
            .fetch();
}

private BooleanExpression ltBookId(Long bookId) {
    if (bookId == null) {
        return null; // BooleanExpression 자리에 null이 반환되면 조건문에서 자동으로 제거된다
    }

    return book.id.lt(bookId);
}
```
위 코드가 No Offset 방식으로 변경한 코드입니다.  
함수에 들어오는 인자에 Id값이 존재합니다.  
클라이언트 단에서 현재 갖고있는 id값의 마지막 값을 보내주면 id값을 조건에 넣고 limit으로 원하는 만큼 땡겨오는 방식입니다.  
이렇게 작성하면 offset 만큼의 데이터를 읽을 필요가 없게 됩니다.  
또한, 클러스터 인덱스인 Id값을 조건문으로 시작했기 때문에 빠르게 조회할 수 있습니다.


<Br><Br>

__참고__  
<a href="https://www.youtube.com/watch?v=zMAX7g6rO_Y&t" target="_blank"> [우아콘2020] 수십억건에서 QUERYDSL 사용하기</a>   
<a href="https://jojoldu.tistory.com/476" target="_blank"> 커버링인덱스</a>   
<a href="https://jojoldu.tistory.com/481" target="_blank"> 커버링인덱스 2</a>   
<a href="https://jojoldu.tistory.com/528" target="_blank"> 페이징 성능 개선하기 - No Offset 사용하기</a>   
<a href="https://jojoldu.tistory.com/529?category=637935" target="_blank"> 페이징 성능 개선 - 커버링 인덱스 사용하기</a>   


