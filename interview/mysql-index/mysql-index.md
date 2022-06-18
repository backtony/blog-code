# MySQL 인덱스

## 1. Index란?
---
인덱스란 (검색을 위해) __지정한 컬럼들을 기준으로 메모리 영역에 일종의 목차를 생성하는 것__ 입니다.  
인덱스를 사용하게 되면 데이터 추가, 수정, 삭제의 성능을 희생하고 대신 __조회의 성능을 향상__ 시킵니다.  
여기서 주의할 것은 update, delete 행위가 느린것이지 __update, delete를 하기 위해 해당 데이터를 조회하는 것은 인덱스가 있으면 빠르게 조회__ 됩니다.  

<br>

## 2. Clustered Index vs Non-Clustered Index
---
인덱스 종류에는 clustered와 Non-Clustered 두 가지가 있습니다.  
MySQL의 InnoDB엔진에서는 인덱스가 B-Tree(균형 트리) 자료구조로 구성되어 있습니다.  

### Cluster Index(클러스터형 인덱스)
![그림3](https://github.com/backtony/blog-code/blob/master/interview/mysql-index/img/index-3.PNG?raw=true)  
+ __한 테이블당 1개 (Primary Key)__
+ 인덱스로 지정한 컬럼을 기준으로 __물리적으로 정렬__ 합니다.
+ 데이터 삽입 시 정렬이 이루어지기 때문에 입력, 수정, 삭제는 다소 느리지만 검색속도는 빠릅니다.

<br>

### Non-Clustered Index(보조 인덱스)
![그림4](https://github.com/backtony/blog-code/blob/master/interview/mysql-index/img/index-4.PNG?raw=true)  
+ __한 테이블당 여러 개 가능__
    - 테이블 당 3~4개를 권장합니다.
    - __카디널리티가 높은 순__ 으로 잡습니다.(중복도가 낮으면 카디널리티가 높습니다.)
+ __데이터 자체는 정렬되지 않고, 인덱스 값을 기준으로 정렬하여 내부적으로 인덱스 페이지가 생성됩니다.__
+ 검색 속도는 Cluster에 비해 느리지만 입력, 수정, 삭제가 빠릅니다.
+ Cluster 인덱스처럼 리프 페이지가 바로 데이터를 나타내지 않고, 데이터 페이지의 주소값을 갖고 있어 한단계 더 나아가야 하므로 검색의 경우 Cluster보다 느리지만 데이터 자체의 정렬작업이 없으므로 입력, 수정, 삭제는 Cluster에 비해 빠릅니다.

<br>

## 3. Cluster + Non-Clustered Index
---
![그림5](https://github.com/backtony/blog-code/blob/master/interview/mysql-index/img/index-5.PNG?raw=true)  

보통 테이블에는 클러스터형 인덱스와 보조 인덱스가 같이 있는 경우가 많습니다.  
이때는 B트리의 내부적으로 가리키는 값들이 변화합니다.  
회원 테이블에서 Name에 보조 인덱스, UserId를 Primary Key로 Cluster 인덱스를 세팅했다고 가정하고 위 그림을 보겠습니다.  
```sql
select * from Member WHERE name = '홍길동';
```
name에 보조 인덱스가 걸려있으므로 보조 인덱스에 대한 루트 -> 리프 페이지에서 값을 찾습니다.  
이때 리프 페이지의 값을 보면 기존의 보조 인덱스 페이지에서는 데이터 페이지의 주소값이 들어있었는데 이제는 __Cluster 인덱스 값__ 이 들어있습니다.  
만약 여기서 select 절에 원하는 필드가 name 값이거나 Cluster 인덱스 값이라면 끝이 나겠지만, member의 다른 컬럼까지 원하게 되면 해당 Cluster 인덱스를 갖고 Cluster의 인덱스 루트 페이지에서부터 검색을 시작합니다.  
이후는 기존 Cluster 인덱스의 동작 과정과 동일합니다.  

<br>


## 4. 멀티 인덱스
---
멀티 인덱스란 __두개 이상의 필드를 조합하여 생성한 인덱스__ 를 의미합니다.  
그냥 각각에 인덱스를 주고 사용해서 조회하면 되지 않을까 생각할 수 있습니다.  
하지만 MySQL은 단일 쿼리를 실행할 때 __하나의 테이블 당 하나의 인덱스__ 만 사용할 수 있습니다.  
```sql
create table member(
	id bigint not null auto_increment,
    name varchar(255) NOT null,
    age int not null,
    address varchar(255) not null,
    primary key (id),
    key idx_name(name),
    key idx_address(address)
);

select * from member where name='홍길동' and address='경기도';
```
위와 같이 각각의 인덱스를 where 문에 넣어주고 쿼리를 날리면 MySQL은 __인덱싱된 데이터 중 행이 적은 것의 인덱스를 사용해서 동작__ 합니다. 즉, 한개의 인덱스만 사용하는 것입니다.  
<br>

```sql
create table member1(
	id bigint not null auto_increment,
    name varchar(255) NOT null,
    age int not null,
    address varchar(255) not null,
    primary key (id),
    key idx_multi(name,address)    
);

-- 문제 없음
select * from member1 where name ='홍길동' and address='강남';
select * from member1 where address='강남' and name ='홍길동';
select * from member1 where name ='홍길동';

-- 문제 발생
select * from member1 where address='강남';
```
위와 같이 멀티 인덱스를 생성하고 조회 시 주의해야할 점이 있습니다.  
기본적으로 멀티 인덱스를 모두 조건문에 넣거나, 멀티 인덱스의 가장 첫 인덱스를 조건문에 넣으면 조회 시 인덱스를 사용해서 조회하게 됩니다.  
하지만 address 만 사용해서 조회 시 인덱스를 사용하지 않고 Full Table Scan 을 해버립니다.  
이유는 멀티 인덱스는 첫 인덱스인 name으로 우선 정렬되고 그 다음으로 address가 name에 의존하여 정렬되기 때문입니다.  
__즉, 멀티 인덱스는 인덱스 순서대로 정렬되어 바로 앞의 인덱스에 의존해서 정렬됩니다.__  
__따라서 멀티 인덱스의 경우, 인덱스를 사용하고 싶다면 조건문에 모든 인덱스를 포함하거나, 인덱스의 순서를 지켜야 합니다.__  
단일 인덱스를 여러개 사용하는 것보다 같이 조건에 사용하는 인덱스가 있다면 묶어서 멀티 인덱스로 사용하는 것이 조회측면에서는 좋으나, 멀티 인덱스는 단일 컬럼 인덱스보다 비효율적으로 Index, Update, Delete를 수행하기 때문에 사용에 신중해야 합니다.  
가급적이면 업데이트가 안되는 값을 선정하는 것이 좋습니다.

<br>

## 5. 어떤 컬럼에 인덱스를 설정할까?
---
+ __카디널리티가 높은 순으로__
    - 중복도가 낮은것 우선
    - 멀티 인덱스를 만들 때도 카디널리티가 높은 순으로
+ __활용도가 높은 것__
    - where, join에서 절에 자주 활용되는지
+ __업데이트가 빈번하지 않은 컬럼__
+ __인덱스의 개수는 3~4개__
    - 너무 많은 인덱스는 새로운 Row를 등록할 때마다 인덱스를 추가해야하고, 수정/삭제시마다 인덱스 수정이 필요하므로 이슈가 있습니다.
    - 인덱스 역시 공간을 차지하기에 많아질수록 이슈가 있습니다.
    - 인덱스가 많아질 수록 옵티마이저가 잘못된 인덱스를 선택할 확률이 높습니다. 

<br>

## 6. 인덱스 주의사항
---
+ in은 =이 여러번 수행되는 연산이므로 인덱스가 적용된다.
    - in에는 서브쿼리보다는 join으로 해결한다.
+ or은 비교할 row가 늘어나기 때문에 Full scan이 발생할 확률이 높아 주의한다.
+ 인덱스로 사용된 컬럼값은 연산하지 않고 그대로 사용해야 적용된다.
    - where salary * 1000 = 10000;  이렇게 왼쪽에 연산을 하면 안된다.
+ 멀티 인덱스의 경우
    - 범위 조건 이후의 인덱스는 인덱스를 타지 않는다.
    - 인덱스의 조건문 사용 순서와 select절 사용 순서를 일치시킬 필요는 없다.
    - group by
        - 인덱스의 순서를 지켜야한다.
        - 인덱스 순서가 지켜진다면 뒤에 인덱스 컬럼을 명시하지 않아도 적용된다.
        - 인덱스 이외의 것이 포함되면 안된다.
        - where의 동등 조건과 사용시 적용된다. 
            - (a,b,c) 일때 b가 where문에 나와되지만 group by는 a,c 로 순서를 일치시켜야 한다.
    - order by
        - 모든 인덱스 컬럼을 포함하고, 순서도 지키고, 다른 컬럼이 작성되지 않아야만 동작한다.
        - where의 동등 조건과 사용시 적용된다.
            - (a,b,c) 인덱스 -> where a=1 order by b,c
    - where + group by + order by
        - group by와 order by 모두 각각의 인덱스 적용 조건읆 만족해야만 적용된다.
+ 8.0 버전 이전에서는 인덱스 생성시 desc를 실질적으로 지원하지 않는다.
    - asc로 만들어진 인덱스를 앞에서부터 읽느냐 뒤에서부터 읽느냐의 차이일 뿐이다.


__아래 내용은 위와 동일한 내용이지만 예시를 들어가면서 조금 길게 작성한 내용입니다.__  
<br><Br>


+ __멀티 인덱스에서 between, like, <, > 등 범위조건은 해당 컬럼은 인덱스를 타지만, 그 뒤 인덱스 컬럼들은 인덱스가 사용되지 않습니다.__
    - (a,b,c)으로 멀티 인덱스가 잡혀있는데 조회 쿼리를 where a=XX and c=YY and b > ZZ 으로 잡으면 c는 인덱스가 사용되지 않습니다.
    - 즉, b에서 범위 조건이 걸렸으므로 a, b까지는 인덱스를 사용하고 c는 인덱스를 타지 않는다는 의미 입니다.
+ __=, in은 다음 컬럼도 인덱스를 사용합니다.__
    - in은 결국 = 를 여러번 실행시킨 것이기 때문입니다.
    - 단, __in은 인자값으로 서브쿼리를 넣으면 성능상 이슈가 발생합니다. -> join을 하는 것이 성능상 좋습니다.__
        - in의 인자로 서브쿼리가 들어가면 서브쿼리의 외부가 먼저 실행되고 in 서브쿼리는 체크 조건으로 반복해서 실행되서 성능상 이슈가 있습니다.
        - MySQL 5.6 부터는 서브쿼리를 사용하면 내부적으로 join으로 풀어서 실행하지만, 동작과정을 보면 서브 쿼리가 먼저 실행되어 그 결과로 임시 테이블을 생성한 뒤 그것을 메인 테이블과 Join해서 결과를 반환하게 됩니다. 
        - 임시 테이블을 만들기 때문에 일반 Join에 비해 성능이 조금 떨어지기 때문에 가능하면 Join을 사용하는게 좋습니다.
+ AND 연산자는 각 조건들이 읽어와야할 ROW수를 줄이는 역할을 하지만, or 연산자는 비교해야할 ROW가 더 늘어나기 때문에 풀 테이블 스캔이 발생할 확률이 높습니다.
    - __where에서 or를 사용할 때는 주의가 필요합니다.__
+ 인덱스로 사용된 __컬럼값 그대로 사용해야만 인덱스가 사용__ 됩니다.
    - where salary * 10 > 10000; 는 인덱스를 사용하지 못하지만, where salary > 15000/10;은 인덱스를 사용합니다.
    - 즉, where 조건의 왼쪽에 나오는 인덱스 변수는 추가적인 작업을 하면 안됩니다.
+ 멀티 인덱스 where 문에서 꼭 인덱스 순서와 select 절의 조회 순서를 지킬 필요는 없습니다.
    - __인덱스 컬럼들이 조회조건에 포함되어 있는지__ 가 중요합니다.
    - 단, 옵티마이저가 조회 조건의 컬럼을 인덱스 컬럼 순서에 맞춰 재배열하는 과정이 추가되긴 하지만 거의 차이가 없습니다.
+ 멀티 인덱스의 경우, Group By 절에 명시된 컬럼은 인덱스 컬럼 순서와 같아야 합니다.
    - index가 (a,b,c) 라면 아래 모두 적용이 안됩니다.
    - group by b
    - group by b, a
    - group by a, c, b
+ 멀티 인덱스의 경우, 앞에 있는 컬럼이 group by에 명시되지 않으면 인덱스가 적용되지 않습니다.
    - index가 (a,b,c) 라면 아래 모두 적용이 안됩니다.
    - group by b, c
+ 멀티 인덱스의 경우, 인덱스에 없는 컬럼이 group by에 포함되어 있으면 인덱스가 적용되지 않습니다.
    - index(a,b,c) -> group by a,b,c,d -> 적용 X
+ 멀티 인덱스의 경우, 뒤에 있는 컬럼이 Group By에 명시되지 않아도 인덱스가 적용됩니다.
    - index가 (a,b,c) 라면 아래 모두 적용 됩니다.
    - group by a
    - group by a, b
    - group by a, b, c
+ Where 조건과 Group by가 함께 사용되면 __Where 조건이 동등 비교일 경우__ group by 절에 해당 컬럼이 없어도 인덱스가 적용됩니다.    
    - 인덱스 (a,b,c) 일때, 다음은 모두 인덱스가 적용됩니다.
    - where a=1 group by b,c
    - where a=1 and b=2 group by c
    - where b=1 group by a,c
    - 만약 아래와 같이 group by에서 순서를 바꾸게 된다면 임시 테이블을 만들게 되므로 성능이 떨어집니다.
    - where a=1 group by c,b
+ where 조건문에 동등 조건이 아닌 like와 같은 연산을 하게 되면 using temporary(임시테이블)이 별도로 생성되고 그 안에서 using filesort(정렬)이 발생합니다.
    - 인덱스 (a,b,c) 일때, 아래는 의도한 대로 인덱스를 타지 못합니다.
    - where a like 'TAKE%' group by b,c
+ 8.0 버전 이전에서는 인덱스 생성시 desc를 실질적으로 지원하지 않는다.
    - MySQL에서는 인덱스 생성시 컬럼 마다 asc/desc를 정할 수 있는 것처럼 보이나 8.0 이전 버전까지는 문법만 지원되고 실질적으로 Desc 인덱스가 지원되는 것은 아닙니다. 단지 Asc로 만들어진 인덱스를 앞에서부터 읽을 것인지, 뒤에서부터 읽을 것인지에 차이만 있을 뿐입니다. 즉, 인덱스 컬럼 중 특정 컬럼만 Desc가 되지 않으며 인덱스 컬럼 전체를 asc 혹은 desc 스캔하는 방법 뿐입니다.
+ 멀티 인덱스 (a,b,c)의 order by에서 인덱스가 적용이 안되는 경우 
    - order by의 경우, 순서도 지키고, 모두 포함하고, 다른 컬럼이 추가되면 안됩니다.
    - order by b,c
    - order by a,c
    - order by a,b,c
    - order by a, b desc, c
    - order by a,b,c,d
+ 멀티 인덱스 (a,b,c)에서 where문과 order by를 함께 사용시 where문이 동등비교라면 인덱스가 가능합니다.
    - where a=1 order by b,c
    - where a = 1, b = 1 order by c
    - 동등비교가 아니라면 group by와 마찬가지로 인덱스가 되지 않습니다.
+ where + group by + order by 멀티 인덱스(a,b,c)인 경우
    - group by와 order by가 함께 사용된 경우라면, 둘다 모두 인덱스를 타야지만 인덱스가 적용됩니다.
    - group by a,b,c order by a,b,c -> 적용
    - group by a,b,c order by b,c -> 미적용

        
    

<br>

## 7. 커버링 인덱스
---
커버링 인덱스란 __모든 항목이 인덱스 컬럼으로 이루어진 상태__ 를 의미합니다.  
앞서 클러스터 인덱스와 논클러스터 인덱스를 함께 사용하게 될 경우, 논클러스터 인덱스에서 1차적으로 조회하고 cluster 인덱스를 사용해서 2차적으로 조회하게 된다고 설명했습니다.  
커버링 인덱스를 사용할 경우, 추가적인 정보가 필요 없이 인덱스로만 결과값을 도출해낼 수 있기 때문에 2차적인 접근이 필요 없습니다.  
```sql
create table member3(
	id bigint not null auto_increment,
    name varchar(255) not null,
    age int not null,
    primary key(id),
    key idx_name(name)
);

explain select * from member3 where id=1;
explain select m.id from member2 m where name='홍길동';
```
![그림1](https://github.com/backtony/blog-code/blob/master/interview/mysql-index/img/index-1.PNG?raw=true)  
explain을 이용해서 위 쿼리를 살펴보면 첫 번째 쿼리의 경우 key 항목에는 사용된 인덱스가, Extra가 빈값으로 나옵니다.  
즉, where 절에는 인덱스가 사용되었지만 select 절의 필드를 완성하는데는 데이터 블록 접근(2차 접근)이 있었다는 의미입니다.  
<br><Br>

![그림2](https://github.com/backtony/blog-code/blob/master/interview/mysql-index/img/index-2.PNG?raw=true)  
반면에 두 번째 쿼리를 보면 key 항목에는 인덱스가, Extra 항목에는 Using Index라고 표기됩니다.  
Using Index는 쿼리 전체가 인덱스 컬럼값으로 다 채워진 즉, 커버링 인덱스가 사용된 경우입니다.



<br><Br>

__참고__  
<a href="https://jojoldu.tistory.com/243" target="_blank"> [mysql] 인덱스 정리 및 팁</a>  
<a href="https://jojoldu.tistory.com/476" target="_blank"> 1. 커버링 인덱스 (기본 지식 / WHERE / GROUP BY)</a>  
<a href="https://jojoldu.tistory.com/481?category=761883" target="_blank"> 2. 커버링 인덱스 (WHERE + ORDER BY / GROUP BY + ORDER BY )</a>


