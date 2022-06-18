# JPA Batch Insert와 JDBC Batch Insert

## 1. Bulk Insert
---
MySQL을 사용하면서 Batch Insert를 수행하기 위해서는 2가지 방법이 있습니다.
+ JPA Batch Insert + Table 전략
+ JDBC Batch Insert

<br> 

## 2. IDENTITY 전략으로 Batch INSERT가 불가능한 이유
---
많은 블로그에서 다루고 있는 JPA + Batch Insert를 MySQL에서 사용하기 위해서는 ID 전략을 Table 전략으로 수정해야 합니다.  
하지만 MySQL은 Sequence 전략이 없습니다.  
일반적으로 MySQL에서 사용하는 IDENTITY 전략은 auto_increment으로 PK 값을 자동으로 증분해서 생성하는 합니다.  
즉, Insert를 실행하기 전까지는 ID에 할당된 값을 알 수 없기 때문에 Transactional Write Behind를 할 수 없고 결과적으로 Batch Insert를 진행할 수 없습니다.  
간단하게 말하자면, Entity를 persist 하려면 @Id로 지정한 필드에 값이 필요한데 IDENTITY(auto_increment) 타입은 실제 DB에 insert를 해야만 값을 얻을 수 있기 때문에 batch 처리가 불가능한 것입니다.  

<br>

## 3. Batch Insert 세팅
---
MySQL에서 Bulk Insert를 사용하기 위해서 JPA Batch를 사용하나 JDBC Native Query를 사용하나 다음 설정이 application.yml에 반드시 필요합니다.
```yml
spring:
    datasource:
        url: jdbc:mysql://localhost:3306/db명?rewriteBatchedStatements=true
    jpa:
        properties:
            hibernate:
                ## bulk insert 옵션 ##
                # 정렬 옵션
                order_inserts: true
                order_updates: true
                # 한번에 나가는 배치 개수 -> 100개의 insert를 1개로 보낸다.
                jdbc:
                    batch_size: 100
```
rewriteBatchedStatements를 true로 세팅해두지 않으면 Insert쿼리가 여전히 각각 나가게 됩니다.  
order_inserts, order_updates 옵션은 insert하는 것과 update 하는 것의 순서를 말합니다.  
예를들어 트랜잭션에 부모 엔티티를 save한 후 자식 엔티티를 save하는 순서가 있다고 가정해보면 원래는 다음과 같이 쿼리가 나갑니다.
```sql
insert into 부모 value a1
insert into 자식 value b1
insert into 부모 value a2
insert into 자식 value b2
```
이렇게 정렬이 되지 않고 쿼리가 나가게 되면 쿼리를 묶어서 한번에 사용할 수 없습니다.  
옵션들을 true로 주게되면 아래와 같이 쿼리가 바뀌어 나갑니다.
```sql
insert into 부모 value (a1, a2)
insert into 자식 value (b1, b2)
```
<br>


Batch Insert가 정확하게 나가는지 확인하고 싶으시면 다음과 같이 옵션을 추가하면 확인할 수 있습니다.  
```yml
spring:
    datasource:
        url: jdbc:mysql://localhost:3306/db명?rewriteBatchedStatements=true&profileSQL=true&logger=Slf4JLogger&maxQuerySizeToLog=999999
```
+ postfileSQL = true : Driver에 전송하는 쿼리를 출력합니다.
+ logger=Slf4JLogger : Driver에서 쿼리 출력시 사용할 로거를 설정합니다.
    - MySQL 드라이버 : 기본값은 System.err 로 출력하도록 설정되어 있기 때문에 필수로 지정해 줘야 합니다.
    - MariaDB 드라이버 : Slf4j 를 이용하여 로그를 출력하기 때문에 설정할 필요가 없습니다.
+ maxQuerySizeToLog=999999 : 출력할 쿼리 길이
    - MySQL 드라이버 : 기본값이 0 으로 지정되어 있어 값을 설정하지 않을경우 아래처럼 쿼리가 출력되지 않습니다.
    - MariaDB 드라이버 : 기본값이 1024 로 지정되어 있습니다. MySQL 드라이버와는 달리 0으로 지정시 쿼리의 글자 제한이 무제한으로 설정됩니다.


<Br>

__전체 application.yml__  
```yml
spring:
  h2:
    console:
      enabled: true

  datasource:
    url: jdbc:mariadb://localhost:3307/test?rewriteBatchedStatements=true&profileSQL=true&logger=Slf4JLogger&maxQuerySizeToLog=999999
    driver-class-name: org.mariadb.jdbc.Driver
    username: root
    password: root


  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        format_sql: true
        default_batch_fetch_size: 100
        ## bulk insert 옵션 ##
        # 정렬 옵션
        order_inserts: true
        order_updates: true
        # 배치 개수 옵션
        jdbc.batch_size: 100

logging:
  level:
    org.hibernate.SQL: debug
    org.hibernate.type: trace


```
table 전략과 sequence 전략을 다 확인해보기 위해 mariadb를 사용했습니다.  

<Br>


## 4. 키 맵핑 전략
---
JPA Batch Insert를 사용하기 위해서는 앞서 언급했듯이 Identity 전략을 사용할 수 없습니다.  
따라서 Sequence 또는 Table 전략을 사용해야 합니다.  
Sequence 전략은 오라클, PostgreSQL, H2 데이터베이스에서 사용할 수 있지만 MySQL에서는 사용할 수 없어서 MySQL에서는 Table 전략을 사용해야 합니다.  

### Sequence 전략
시퀀스는 유일한 값을 순서대로 생성하는 특별한 데이터베이스 오브젝트입니다.  
시퀀스 사용 코드는 IDENTITY 전략과 같지만 내부 동작 방식이 다릅니다.  
시퀀스 전략은 em.persist()를 호출할 때 먼저 데이터베이스 시퀀스를 사용해서 식별자를 조회해서 가져오고 조회한 식별자를 엔티티에 할당한 후에 엔티티를 영속성 컨텍스트에 저장합니다.  
이후 트랜잭션을 커밋해서 플러시가 일어나면 엔티티를 데이터베이스에 저장합니다.  

```java
@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Board {

    @Id
    @SequenceGenerator(
            name = "BOARD_SEQ_GENERATOR",
            sequenceName = "BOARD_SEQ",
            initialValue = 1, allocationSize = 50
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
                    generator = "BOARD_SEQ_GENERATOR")
    private Long id;

    private String title;
}
```
@SequenceGenerator를 사용해서 시퀀스 생성기를 등록합니다.  
name은 식별자 생성기 이름을 정해주고 이 시퀀스 생성자를 사용할 곳에 세팅해주면 됩니다. (GenerateValue에 generator로 세팅해주면 됩니다.)  
데이터베이스에는 sequenceName으로 설정한 "BOARD_SEQ"가 데이터베이스의 BOARD_SEQ 시퀀스와 매핑됩니다.  
yml 옵션에서 ddl을 생성하게 해두었다면 board 테이블이 생성되기 전에 다음과 같은 쿼리가 나갑니다.
```sql
-- 위 Board에서 설정한 initialValue와 allocationSize는 디폴트 값을 명시적으로 세팅해준거라 지워도 됩니다.
create sequence board_seq start with 1 increment by 50

create sequence [sequenceName]
start with [initialValue] increment by [allocationSize]
```


__cf) @SequenceGenerator 속성 정리__  

속성|기능|기본값
---|---|---
name|식별자 생성기 이름|필수
sequenceName|데이터베이스에 등록되어 있는 시퀀스 이름|hibernate_sequence
initialValue|DDL 생성 시에만 사용되며 시퀀스 DDL을 생성할 때 처음 시작하는 수를 지정|1
allocationSize|시퀀스 한 번 호출에 증가하는 수(성능 최적화)|50
catalog, schema|데이터베이스 catalog, schema 이름|


allocationSize는 기본값이 50이므로 시퀀스를 호출할 때마다 값이 50씩 증가합니다.  
기본값이 50인 이유는 최적화 때문입니다.  
시퀀스 전략은 데이터베이스와 2번 통신합니다.  
1. 식별자를 구하기 위해 데이터 베이스 시퀀스 조회
    + SELECT BOARD_SEQ.NEXTVAL FROM DUAL
2. 조회한 시퀀스를 기본 키 값으로 사용해 데이터베이스에 저장
    + INSERT INTO BOARD ...

JPA는 시퀀스에 접근하는 횟수를 줄이기 위해 allocationSize를 사용합니다.  
간단히 설명하자면, 여기에 설정한 값만큼 한 번에 시퀀스 값을 증가시키고 나서 그만큼 메모리에 시퀀스 값을 할당합니다.  
예를 들어 allocationSize 값이 50이면 DB에서 시퀀스를 한 번에 50 증가시킨 다음에 1~50까지는 메모리에서 식별자를 할당하는데 사용합니다.  
그리고 51이 되면 다시 시퀀스 값을 DB에서 100으로 증가시키고 51~100까지 메모리에서 식별자를 할당합니다.  
이 최적화 방법은 시퀀스 값을 선점하므로 여러 JVM이 동시에 동작해도 기본 키 값이 충돌하지 않는 장점이 있습니다.  

<br>

bach insert가 정상적으로 나가는지 확인해보기 위해 100개를 저장시켜 보았습니다.  
```java
@PostMapping("/board")
public void test(){
    List<Board> ls = new ArrayList<>();

    for (int i=0; i<100;i++){
        Board title = Board.builder().title("title").build();
        ls.add(title);
    }
    boardRepository.saveAll(ls);
}
------------------------------------------------------------------------
Query: insert into board (title, id) values (?, ?), parameters ['title',1],['title',2],['title',3],['title',4],['title',5],['title',6],['title',7],['title',8],['title',9],['title',10],['title',11],['title',12],['title',13],['title',14],['title',15],['title',16],['title',17],['title',18],['title',19],['title',20],['title',21],['title',22],['title',23],['title',24],['title',25],['title',26],['title',27],['title',28],['title',29],['title',30],['title',31],['title',32],['title',33],['title',34],['title',35],['title',36],['title',37],['title',38],['title',39],['title',40],['title',41],['title',42],['title',43],['title',44],['title',45],['title',46],['title',47],['title',48],['title',49],['title',50],['title',51],['title',52],['title',53],['title',54],['title',55],['title',56],['title',57],['title',58],['title',59],['title',60],['title',61],['title',62],['title',63],['title',64],['title',65],['title',66],['title',67],['title',68],['title',69],['title',70],['title',71],['title',72],['title',73],['title',74],['title',75],['title',76],['title',77],['title',78],['title',79],['title',80],['title',81],['title',82],['title',83],['title',84],['title',85],['title',86],['title',87],['title',88],['title',89],['title',90],['title',91],['title',92],['title',93],['title',94],['title',95],['title',96],['title',97],['title',98],['title',99],['title',100]
```
한번에 처리되는 것을 확인할 수 있습니다.




### Table 전략
테이블 전략은 키 생성 전용 테이블을 하나 만들고 여기에 이름과 값으로 사용할 컬럼을 만들어 데이터베이스 시퀀스 전략을 흉내내는 전략입니다.  
먼저 키 생성 테이블을 만들어야 합니다.
```sql
create table 테이블명 (
    sequence_name varchar(255) not null,
    next_val bigint,
    primary key (sequence_name)
)

-- 예시
create table table_sequence (
    TABLE_SEQ varchar(255) not null,
    next_val bigint,
    primary key (TABLE_SEQ)
)
```
sequence_name 컬럼은 시퀀스 이름으로 사용하고 next_val 컬럼을 시퀀스 값으로 사용합니다.  

```java
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Tb {

    @Id
    @TableGenerator(
            name = "TABLE_SEQ_GENERATOR",
            table = "TABLE_SEQUENCE", // 위에서 생성한 테이블명
            pkColumnName = "TABLE_SEQ" // 위에서 생성한 테이블에 sequence_name
    )
    @GeneratedValue(strategy = GenerationType.TABLE,
                    generator = "TABLE_SEQ_GENERATOR")
    private long id;

    private String title;
}
```
시퀀스 방식과 매우 유사합니다.  
<br>

__@TableGenerator 속성 정리__  

속성|기능|기본값
---|---|---
name|식별자 생성기 이름|필수
table|키생성 테이블명|hibernate_sequences
pkColumnName|시퀀스 컬럼명|sequence_name
valueColumnName|시퀀스 값 컬럼명|next_val
pkColumnValue|키로 사용할 값 이름|엔티티 이름
initialValue|초기값, 마지막으로 생성된 값이 기준(즉, 세팅한 다음 값부터 할당|0
allocationSize|시퀀스 한 번 호출에 증가하는 수|50
catalog, schema|데이터베이스 catalog, schema 이름|
uniqueConstraints(DDL)|유니크 제약 조건 지정|

테이블 안에 값이 없으면 JPA가 값을 INSERT하면서 초기화하므로 값을 미리 넣어둘 필요는 없습니다.  
<br>

bach insert가 정상적으로 나가는지 확인해보기 위해 100개를 저장시켜 보았습니다.  
```java
@RestController
@RequiredArgsConstructor
public class Controller {

    private final TbRepository tbRepository;

    @PostMapping("/tb")
    public void testTb(){
        List<Tb> ls = new ArrayList<>();

        for (int i=0; i<100;i++){
            Tb title = Tb.builder().title("title").build();
            ls.add(title);
        }
        tbRepository.saveAll(ls);
    }
}
----------------------------------------------------------------------------------------

[QUERY] insert into tb (title, id) values ('title', 1),('title', 2),('title', 3),('title', 4),('title', 5),('title', 6),('title', 7),('title', 8),('title', 9),('title', 10),('title', 11),('title', 12),('title', 13),('title', 14),('title', 15),('title', 16),('title', 17),('title', 18),('title', 19),('title', 20),('title', 21),('title', 22),('title', 23),('title', 24),('title', 25),('title', 26),('title', 27),('title', 28),('title', 29),('title', 30),('title', 31),('title', 32),('title', 33),('title', 34),('title', 35),('title', 36),('title', 37),('title', 38),('title', 39),('title', 40),('title', 41),('title', 42),('title', 43),('title', 44),('title', 45),('title', 46),('title', 47),('title', 48),('title', 49),('title', 50),('title', 51),('title', 52),('title', 53),('title', 54),('title', 55),('title', 56),('title', 57),('title', 58),('title', 59),('title', 60),('title', 61),('title', 62),('title', 63),('title', 64),('title', 65),('title', 66),('title', 67),('title', 68),('title', 69),('title', 70),('title', 71),('title', 72),('title', 73),('title', 74),('title', 75),('title', 76),('title', 77),('title', 78),('title', 79),('title', 80),('title', 81),('title', 82),('title', 83),('title', 84),('title', 85),('title', 86),('title', 87),('title', 88),('title', 89),('title', 90),('title', 91),('title', 92),('title', 93),('title', 94),('title', 95),('title', 96),('title', 97),('title', 98),('title', 99),('title', 100) [Created on: Fri Feb 11 11:11:20 KST 2022, duration: 2, connection-id: 70, statement-id: 0, resultset-id: 0,	at com.zaxxer.hikari.pool.ProxyStatement.executeBatch(ProxyStatement.java:127)]
```



## 5. JDBC Batch Insert
---
Jdbc Template을 사용하면 MySQL의 IDENTITY 전략을 사용하더라도 아래와 같은 코드로 배치 INSERT 쿼리를 해결할 수 있습니다.  
```java
@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
}
```
```java
@Repository
@RequiredArgsConstructor
public class MemberJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    public void insertMemberList(List<Member> memberList){
        jdbcTemplate.batchUpdate("insert into member (name) values (?)",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setString(1, memberList.get(i).getName());
                    }

                    @Override
                    public int getBatchSize() {
                        return memberList.size();
                    }
                });
    }
}
```
```java
@PostMapping("/member")
public void testMember(){
    List<Member> ls = new ArrayList<>();

    for (int i=0; i<100;i++){
        Member member = Member.builder().name("name").build();
        ls.add(member);
    }
    memberJdbcRepository.insertMemberList(ls);
}

--------------------------------------------------------------------------------------------
Query: insert into member (name) values (?), parameters ['name'],['name'],['name'],['name'],['name'],['name'],['name'],['name'],['name'],['name'],['name'],['name'],['name'],['name'],['name'],['name'],['name'],['name'],['name'],['name'],['name'],['name'],['name'],['name'],['name'],['name'],['name'],['name'],['name'],['name'],['name'],['name'],['name'],['name'],['name'],['name'],['name'],['name'],['name'],['name'],['name'],['name'],['name'],['name'],['name'],['name'],['name'],['name'],['name'],['name'],['name'],['name'],['name'],['name'],['name'],['name'],['name'],['name'],['name'],['name'],['name'],['name'],['name'],['name'],['name'],['name'],['name'],['name'],['name'],['name'],['name'],['name'],['name'],['name'],['name'],['name'],['name'],['name'],['name'],['name'],['name'],['name'],['name'],['name'],['name'],['name'],['name'],['name'],['name'],['name'],['name'],['name'],['name'],['name'],['name'],['name'],['name'],['name'],['name'],['name']
```

## 6. 주의사항
---
JPA 사용 시 bulk insert를 하게 되면 그만큼의 데이터가 영속화 된 이후 flush하게 됩니다.  
메모리가 충분하다면 문제가 없겠지만 너무 많은 데이터를 영속화 시키게 된다면 out of memory가 발생하게 됩니다.  
따라서 적절한 개수만큼 나눠서 영속화 시키고 flush, clear을 반복해줘야 합니다.  

<br>

jdbcTemplate을 사용한다면 영속성 컨텍스트를 사용하지 않으니 상관없겠지라고 생각할 수 있지만 MySQL을 예시로 들면 MySQL Client가 Server로 전달하는 Packet의 크기는 제한되어 있습니다.  
따라서 너무 많은 양의 데이터를 한 번에 넣게 되면 Packet의 크기가 허용치를 넘어 예외가 발생하게 됩니다.  
```sql
SHOW VARIABLES LIKE 'max_allowed_packet’;
```
이는 max_allowed_packet 필드로 확인할 수 있습니다.  
즉, jdbcTemplate을 사용하더라도 JPA와 마찬가지로 적절한 크기로 나눠서 insert를 해야 합니다.  

<br>

__대용량 bulk insert에 @Transactional을 붙이는게 맞는가?__  
예를 들어 10만 개의 데이터를 만개씩 쪼개서 insert를 한다고 해봅시다.
```java
@Service
@Transactional
@RequiredArgsConstructor
public class CsvService {

    private final CsvRepository csvRepository;
    private final EntityManager em;

    public void saveCsvFile() {
        //// 10번 반복 ////
        // 1만개 데이터 save
        // em.flush, em.clear
    }
}
```
간단하게 생각하면 위와 같이 작성할 수 있습니다.  
그런데 9만개의 데이터를 save하고 마지막 10만개째 파일을 save하는 과정에서 unique 키가 중복되서 예외가 발생했다고 해봅시다.  
그럼 saveCsvFile 메서드는 하나의 트랜잭션으로 묶여있기 때문에 앞선 9만개의 저장 데이터가 전부 롤백 됩니다.  
이를 해결하는 방법은 간단합니다. @Transactional로 묶지 않으면 됩니다.  
@Transactional는 autoCommit을 false로 세팅해서 트랜잭션이 끝날 때 롤백 할지 커밋 할지를 결정하게 됩니다.  
@Transactional을 사용하지 않는다면 autoCommit이 true이기 때문에 각각의 save가 다른 트랜잭션으로 돌아 서로에게 영향을 주지 않게 됩니다.  
```java
@Service
@RequiredArgsConstructor
public class UserService {

    private final CsvRepository csvRepository;

    public void saveCsv() {

        // 총 10만개 데이터를 만개씩 쪼갠 리스트
        List<List<CsvDto>> arr = new ArrayList<>();
      
        for (int i=0;i<10;i++){
            try {
                csvRepository.saveAll(arr.get(i)); // 만개씩 save
            } catch (DataIntegrityViolationException e){ // 키 중복 예외 발생
                // 로그 찍기
            }
        }
    }
}
```
위 코드에서 만약 4만개 데이터에서 예외가 발생하면 로그로만 찍히고 3만개 까지 save되고 5만개부터 10만개까지 save됩니다.  
나중에 로그로 찍힌 4만개 데이터만 확인해서 따로 처리해주면 됩니다.  



