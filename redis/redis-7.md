# Redis - 데이터 입출력 및 자료구조 실습하기

## 입출력 실습
---
```sql
-- 데이터 저장 key value
set 1111 "backtony"  
-- 출력
OK

-- 데이터 검색 key
get 1111 
-- 출력
"backtony"

-- 현재 저장된 모든 키 출력
keys *

-- 현재 저장된 키 중에서 1로 끝나는 key만 검색
keys *1

-- 삭제 key
del 1111
-- 출력
(integer) 1

-- key 변경하기
set 1111 "backtony"
rename 1111 1116
get 1116
-- 출력
"backtony"

-- 현재 key 중 랜덤으로 key 검색
randomkey

-- key의 존재 여부 확인
set 1111 "backtony"
exists 1111
-- 출력 1은 존재 0은 존재하지 않음을 의미
(integer) 1 

-- value 길이 확인
set 1111 "backtony"
strlen 1111
-- 출력
(integer) 8

-- 현재 저장되어 있는 모든 Key 삭제
flushall

-- ttl 지정
-- 1111키 "backtony"value를 30초동안 저장
setex 1111 30 "backtony"

-- 남은 ttl 확인
-- ttl key
ttl 1111

-- 여러 개의 키밸류를 한번에 저장
mset 1111 "hello" 1112 "world"

-- 여러 개의 key의 value를 한번에 검색
-- 존재하지 않는 키의 value는 (nil)로 검색됨
mget 1111 1112 1113
-- 출력
"hello"
"world"
(nil)

-- 연속번호 발행해보기
set seq 1
-- value 1 증가
incr seq
-- value 1 감소
decr seq
-- value 2 증가
incrby seq 2
--value 10 감소
decrby seq 10

-- 현재 value에 추가하기
set 1111 "backtony"
append 1111 " world"
get 1111
-- 출력
"backtony world"

-- 화면 지우기
clear

-- redis 서버 설정 상태 조회
info

-- cli exit
exit
```

<br>


## Hash 타입
---
하나의 Key와 하나 이상의 Field:Element값(Value)로 저장하는 방식입니다.  
key에는 아스키 값을 저장할 수 있고 Value에는 기본적으로 String 데이터를 저장할 수 있으며 추가로 컨테이너 타입의 데이터들을 저장할 수 있습니다.  
컨테이너 타입에는 Hash, List, Set/Sorted Set 4가지 유형이 있습니다.  
Hash 타입의 특징은 다음과 같습니다.
+ 기본 관계형 DB에서 Primary-Key와 하나 이상의 컬럼으로 굿어된 테이블 구조와 매우 흡사한 데이터 유형입니다.
+ 하나의 Key는 오브젝트명과 하나 이상의 필드 값을 콜론(:) 기호로 결합하여 표현할 수 있습니다.
   - ex) order:124124, order_detail:1231245
+ 문자값을 저장할 때는 인용부호("")를 사용하고, 숫자 값을 저장할 때는 사용하지 않습니다.
+ 기본적으로 필드 개수는 제한이 없습니다.
+ 데이터를 처리할 때는 hmset, hget, hgetall, hkey, hlen 명령어를 사용합니다.

### 실습
```sql
-- hash 타입
-- key는 order:20220127, 나머지는 해당 Key 안의 value로 Field/Element 형태로 저장
hmset order:20220127 customer_name "backtony" emp_name "github" total 10000 payment_type "credit" order_filled "Y" ship_date 20220128

-- 필드 가져오기
hget order:20220127 customer_name
-- 출력
"backtony"

-- order:20220127 key에 대한 모든 필드와 value 값 검색
hgetall order:20220127
-- 출력
 1) "customer_name"
 2) "backtony"
 3) "emp_name"
 4) "github"
 5) "total"
 6) "10000"
 7) "payment_type"
 8) "credit"
 9) "order_filled"
10) "Y"
11) "ship_date"
12) "20220128"

-- product_name 필드가 존재하는지 확인
hexists order:20220127 product_name
-- 출력, 1:존재 0:존재하지 않음
(integer) 0

-- ship_date 필드 제거
hdel order:20220127 ship_date

-- ship_date 필드 다시 추가
hmset order:20220127 ship_date 20220218

-- order:20220127 key에 대한 모든 Field명만 출력
hkeys order:20220127
-- 출력
1) "customer_name"
2) "emp_name"
3) "total"
4) "payment_type"
5) "order_filled"
6) "ship_date"

-- order:20220127 key에 대한 모든 Field 개수
hlen order:20220127
-- 출력
(integer) 6

-- 해당 key에 정의된 특정 필드의 value만 출력
hmget order:20220127 emp_name total
-- 출력
1) "github"
2) "10000"
```
<br>

## List 타입
---
+ List 타입은 기존의 관계형 테이블에는 존재하지 않는 데이터 유형이며 일반적으로 프로그래밍 언어에서 데이터를 처리할 때 사용되는 배열 변수와 유사한 데이터 구조입니다.
+ 기본적인 String 타입의 경우 배열에 저장할 수 있는 데이터 크기는 512MB입니다.
+ List 타입의 데이터를 처리할 때는 lpush, lrange, rpush, rpop, llen, lindex 명령어를 사용합니다.

### 실습
```sql
-- list 타입은 하나의 key에 여러 개의 value를 저장할 수 있다.
-- value는 "" "" 로 구분한다.
-- 리스트에는 itemId가 2번이 0번 인덱스에 1번이 1번 인덱스에 들어간다. 
lpush order_detail:20220127 "<item_id>1</item_id><product_name>back tony</product_name><item_price>135</item_price><qty>500</qty><price>10000</price>" "<item_id>2</item_id><product_name>back tony2</product_name><item_price>13522</item_price><qty>50012</qty><price>20000</price>"

-- 인덱스 0부터 10까지 출력
lrange order_detail:20220127 0 10
-- 출력, 
1) "<item_id>2</item_id><product_name>back tony2</product_name><item_price>13522</item_price><qty>50012</qty><price>20000</price>"
2) "<item_id>1</item_id><product_name>back tony</product_name><item_price>135</item_price><qty>500</qty><price>10000</price>"

-- 기존 저장된 데이터의 오른쪽(마지막) 부분에 새로운 value 저장
rpush order_detail:20220127 "<item_id>3</item_id><product_name>back tony3</product_name><item_price>1353</item_price><qty>5030</qty><price>100003</price>"

-- 인덱스 0부터 10까지 출력
lrange order_detail:20220127 0 10
-- 출력
1) "<item_id>2</item_id><product_name>back tony2</product_name><item_price>13522</item_price><qty>50012</qty><price>20000</price>"
2) "<item_id>1</item_id><product_name>back tony</product_name><item_price>135</item_price><qty>500</qty><price>10000</price>"
3) "<item_id>3</item_id><product_name>back tony3</product_name><item_price>1353</item_price><qty>5030</qty><price>100003</price>"

-- 가장 오른쪽에 저장된 value 값을 제거
rpop order_detail:20220127

-- 저장된 value 개수 count
llen order_detail:20220127

-- 해당 key에 0번째 저장된 데이터 검색
lindex order_detail:20220127 0
-- 출력
"<item_id>2</item_id><product_name>back tony2</product_name><item_price>13522</item_price><qty>50012</qty><price>20000</price>"

-- 해당 key에 1번째 저장된 데이터 검색
lindex order_detail:20220127 1
-- 출력
"<item_id>1</item_id><product_name>back tony</product_name><item_price>135</item_price><qty>500</qty><price>10000</price>"

-- 해당 key에 2번째 저장된 데이터 검색
lindex order_detail:20220127 2
-- 출력
(nil)

-- 0번 value값 변경
lset order_detail:20220127 0 "<item_id>5</item_id><product_name>back tony</product_name><item_price>135</item_price><qty>500</qty><price>10000</price>"

-- 가장 왼쪽에 새로운 value값 저장
lpushx order_detail:20220127 "<item_id>6</item_id><product_name>back tony</product_name><item_price>135</item_price><qty>500</qty><price>10000</price>"

-- item_id:1 value값 앞에 item_id 10 저장
linsert order_detail:20220127 before "<item_id>1</item_id><product_name>back tony</product_name><item_price>135</item_price><qty>500</qty><price>10000</price>" "<item_id>10</item_id><product_name>back tony2</product_name><item_price>13522</item_price><qty>50012</qty><price>20000</price>"

-- 확인
lrange order_detail:20220127 0 10
1) "<item_id>6</item_id><product_name>back tony</product_name><item_price>135</item_price><qty>500</qty><price>10000</price>"
2) "<item_id>5</item_id><product_name>back tony</product_name><item_price>135</item_price><qty>500</qty><price>10000</price>"
3) "<item_id>10</item_id><product_name>back tony2</product_name><item_price>13522</item_price><qty>50012</qty><price>20000</price>"
4) "<item_id>1</item_id><product_name>back tony</product_name><item_price>135</item_price><qty>500</qty><price>10000</price>"

-- 데이터 포맷을 json 타입으로 저장
lpush order_detail:20220128 "{item_id:1, product_name:backtony, item_price:10000, qty:2000, price:10000}" "{item_id:2, product_name:backtony2, item_price:20000, qty:2000, price:20000}"

-- 출력
lrange order_detail:20220128 0 1
1) "{item_id:2, product_name:backtony2, item_price:20000, qty:2000, price:20000}"
2) "{item_id:1, product_name:backtony, item_price:10000, qty:2000, price:10000}"
```
<br>

## Set 타입
---
+ List 타입은 하나의 필드에 여러 개의 배열(Array) 값을 저장할 수 있는 데이터 구조라면 Set 타입은 배열 구조가 아닌 여러 개의 엘리먼트(Element)로 데이터 값을 표현하는 구조입니다.
+ Set 타입의 데이터를 처리할 때는 sadd, smembers, scard, sdiff, sunion 명령어를 사용합니다.

### 실습
```sql
-- product set 안에 데이터 넣기
sadd product "id:1, product_name:backtony, item_price:100, qty:100, price:10000" "id:2, product_name:backtony2, item_price:200, qty:200, price:20000"

-- product 안에 있는 Element 확인
smembers product
1) "id:2, product_name:backtony2, item_price:200, qty:200, price:20000"
2) "id:1, product_name:backtony, item_price:100, qty:100, price:10000"

-- product_old set안에 데이터 넣기 
sadd product_old "id:91, product_name:backtony_old"

-- product와 product_old 중에서 product에만 있는 value 출력
sdiff product product_old
1) "id:1, product_name:backtony, item_price:100, qty:100, price:10000"
2) "id:2, product_name:backtony2, item_price:200, qty:200, price:20000"

-- product와 product_old 중에서 product에만 있는 value sets을 product_diff에 저장
sdiffstore product_diff product product_old

smembers product_diff
1) "id:1, product_name:backtony, item_price:100, qty:100, price:10000"
2) "id:2, product_name:backtony2, item_price:200, qty:200, price:20000"

-- product와 product_old를 union
sunion product product_old
1) "id:91, product_name:backtony_old"
2) "id:1, product_name:backtony, item_price:100, qty:100, price:10000"
3) "id:2, product_name:backtony2, item_price:200, qty:200, price:20000"

-- union 한 결과를 sunionstore에 저장
sunionstore product_new product product_old

smembers product_new
1) "id:91, product_name:backtony_old"
2) "id:1, product_name:backtony, item_price:100, qty:100, price:10000"
3) "id:2, product_name:backtony2, item_price:200, qty:200, price:20000"

-- 특정 value 제거
srem product_new "id:1, product_name:backtony, item_price:100, qty:100, price:10000"

smembers product_new
1) "id:91, product_name:backtony_old"
2) "id:2, product_name:backtony2, item_price:200, qty:200, price:20000"


-- 랜덤하게 1개 value를 제거
spop product_new 1 
```
<br>

## Sorted Set 타입
---
+ Sorted Set 타입은 Set 타입과 동일한 데이터 구조이며 차이점은 저장된 데이터 값이 분류된 상태이면 Sorted Set 입니다.
+ 데이터를 처리할 때는 zadd, zrange, zcard, zcount, zrank, zrevrank 명령어를 사용합니다.


### 실습
```sql
-- sorted set 저장
zadd order_detail:20220127 1 "{product_name:backtony, item_price:10000, qty:100, price:10000}" 2 "{product_name:backtony2, item_price:20000, qty:100, price:20000}" 3 "{product_name:backtony3, item_price:30000, qty:300, price:30000}"

-- 0인덱스부터 끝까지 확인
zrange order_detail:20220127 0 -1 
1) "{product_name:backtony, item_price:10000, qty:100, price:10000}"
2) "{product_name:backtony2, item_price:20000, qty:100, price:20000}"
3) "{product_name:backtony3, item_price:30000, qty:300, price:30000}"

-- 3번째 앞에 추가, -> 3번 인덱스에 추가
zadd order_detail:20220127 3 "{product_name:back, item_price:30, qty:300, price:300}"

-- 확인
zrange order_detail:20220127 0 -1
1) "{product_name:backtony, item_price:10000, qty:100, price:10000}"
2) "{product_name:backtony2, item_price:20000, qty:100, price:20000}"
3) "{product_name:back, item_price:30, qty:300, price:300}"
4) "{product_name:backtony3, item_price:30000, qty:300, price:30000}"

-- key로 저장되어 있는 value 개수 확인
zcard order_detail:20220127 
-- 출력
(integer) 4

-- 특정 set 제거
zrem order_detail:20220127 "{product_name:backtony2, item_price:20000, qty:100, price:20000}"

-- 확인
zrange order_detail:20220127 0 -1
1) "{product_name:backtony, item_price:10000, qty:100, price:10000}"
2) "{product_name:back, item_price:30, qty:300, price:300}"
3) "{product_name:backtony3, item_price:30000, qty:300, price:30000}"

-- 해당 value의 rank 순서
zrank order_detail:20220127 "{product_name:backtony, item_price:10000, qty:100, price:10000}"
(integer) 0

-- 해당 value의 reverse rank 
zrevrank order_detail:20220127 "{product_name:backtony, item_price:10000, qty:100, price:10000}"
(integer) 2

-- value의 Score(position)
zscore order_detail:20220127 "{product_name:backtony, item_price:10000, qty:100, price:10000}"
-- 출력
"1"
```
<br>

## Bit 타입
---
+ 일반적으로 사용자가 표현하는 데이터는 문자, 숫자, 날짜를 아스키값이라고 표현하는데 컴퓨터는 이를 최종적으로 0과 1로 표현되는 bit값으로 변환하여 저장합니다.
+ Redis에서 제공되는 Bit 타입은 사용자의 데이터를 0과 1로 표현하며 컴퓨터가 가장 빠르게 저장할 수 있고 해석할 수 있도록 표현한 구조입니다.
+ 데이터를 처리할 때는 sebit, getbit, bitcount 명령어를 사용합니다.

### 실습
```sql
-- 사원은 신입사원으로 bit 1
setbit customer 1001 1
setbit customer 1002 1

-- 기존 사원으로 bit 0
setbit customer 1003 0

-- 비트 확인
getbit customer 1001
(integer) 1
```

<Br>

## Geo 타입
---
+ 위치정보(경도, 위도) 데이터를 효율적으로 저장 관리할 수 있으며 이를 활용한 위치 정보 데이터의 분석 및 검색에 사용할 수 있는 타입입니다.
+ 데이터를 처리할 때는 geoadd, geopos, geodist, georadius, geohash 명령어를 사용합니다.

### 실습
```sql
-- 저장
geoadd position 127.111 37.22 "backtony" 127.124 37.124 "house"

-- 확인
geopos position "backtony" "house"
1) 1) "127.11099833250045776"
   2) "29.22000064649827777"
2) 1) "123.12400192022323608"
   2) "37.12399882517448901"

-- 거리
geodist position "backtony" "house"
"954012.3191"

georadius position 127 37 200 km asc
1) "house"
2) "backtony"

georadius position 127 37 200 km desc
1) "backtony"
2) "house"

geohash position "backtony" "house"
1) "wyd7srxqhy0"
2) "wyd7hy0ygy0"

 georadiusbymember position "backtony" 100 km
1) "backtony"
2) "house"
```
<Br>

## HyperLogLogs 타입
---
+ 관계형 DB의 테이블 구조에서 check 제약조건과 유사한 개념의 데이터 구조입니다. 
+ 관계형 DB에서 Check 제약조건을 사용하는 이유는 해당 컬럼에 반드시 저장되어야 할 데이터 값만 저장할 수 있도록 제한을 가하는 것입니다.
+ Redis DB에서도 동일하게 특정 필드 또는 엘리멘트에 저장되어야 할 데이터 값을 미리 생성하여 저장한 후 필요에 따라 연결하여 사용할 수 있는 데이터 타입입니다.
+ 데이터를 처리할 때는 pfadd, pfcount, pfmerge 명령어를 사용합니다.


### 실습
```sql
-- 저장
pfadd credit_type "cash" "credit card" "point"
pfadd domestic_city "seoul" "busan" "daejeon"
pfadd foreign_city "log angeles" "san diego" "new york"

-- 카운트
pfcount credit_type
(integer) 3

-- international_city 에 뒤에 두가지 합치기
pfmerge international_city domestic_city foreign_city

-- 확인
pfcount international_city
(integer) 6
```
