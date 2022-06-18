# Redis - 설치 및 아키텍처

# 1. 리눅스 Redis 설치
---
```sh
# 업데이트
sudo yum update
sudo yum install gcc make

# 설치
sudo wget http://download.redis.io/redis-stable.tar.gz
sudo tar xvzf redis-stable.tar.gz

# 이동
mv redis-stable /home/ec2-user/redis
cd /home/ec2-user/redis

make # // 소스 컴파일(=소스 파일을 실행 가능한 형태로 만들어 준다)
sudo make install #/usr/local/bin에 redis-server, redis-cli 등 실행 파일이 복사
```

<br>

# 2. Redis 실행 및 접속하기
---
```sh
### 위치는 src 디렉토리 안에서 진행 ###

# default 설정으로 Redis server 띄우기
./redis-server


# custom Conf 파일(환경 설정) 만들기
vi redis_5000.conf

# 설정
port 5000 # 기본 6379 
daemonize yes # 백그라운드에서 인스턴스 구동 여부 결정
logfile "/home/ec2-user/redis/redis_5000.log" # 로그파일

# custom Conf 로 redis server 띄우기
./redis-server redis_5000.conf

# Client 실행하기
./redis-cli -p 포트번호

# 서버 종료시키기
cli로 접속 후 shutdown 입력

# Sentinel 실행 코드
./redis-sentinel
```
<Br>

# 3. 액세스 컨트롤 권한
---
```sh
### src 디렉토리에서 진행 ###

# 설정파일 생성 및 수정
vi redis_5000.conf

# 접속 암호 입력
requirepass redis123
port 5000

# 실행
./redis-server redis_5000.conf 

# 접속
./redis-cli -p 5000

# 로그인
auth redis123

# 접속할 때 한 번에 로그인
./redis-cli -p 5000 -a redis123
```
<br>

# 4. Redis 아키텍처
---
![그림1](https://github.com/backtony/blog-code/blob/master/redis/img/2/1-1.PNG?raw=true)  

위 그림은 Redis 서버의 기본 아키텍처 구조이며 3가지 영역으로 구성되어 있습니다.

## 메모리 영역
+ Resident Area : 사용자가 Redis 서버에 접속해서 처리하는 모든 데이터가 가장 먼저 저장되는 영역이며 실제 작업이 수행되는 공간이고 WorkingSet 영역이라고 표현합니다.
+ Data Structure : Redis 서버를 운영하다 발생하는 다양한 정보와 서버 상태를 모니터링하기 위해 수집한 상태 정보를 저장하고 관리하기 위한 메모리 공간입니다.

## 파일 영역
+ AOF 파일 : Redis는 모든 데이터를 메모리 상에 저장하고 관리하는 InMemory 기반의 데이터 처리기술을 제공합니다. 하지만 중요한 데이터의 경우 사용자의 필요에 따라 지속적으로 저장해야 할 필요가 있는데 이를 위해 제공되는 디스크 영역입니다.(스냅샷 데이터)
+ DUMP 파일 : AOF 파일과 같이 사용자 데이터를 디스크 상에 저장할 수 있지만 소량의 데이터를 일시적으로 저장할 때 사용되는 파일입니다.

## 프로세스 영역
+ Server Process : redis-server.exe 또는 redis-sentinel.exe 실행 코드에 의해 활성화되는 프로세스를 서버 프로세스라고 하며 Redis 인스턴스를 관리해 주며 사용자가 요구한 작업을 수행하는 프로세스입니다. Redis Server 프로세스는 4개의 멀티 쓰레드로 구성됩니다.
    - main thread : Redis 서버에 수행되는 대부분의 명령어와 이벤트를 처리하는 역할
    - sub thread 1(BIO-Close-File) : 쓰레드 1은 AOF에 데이터를 Rewrite할 때 기존 파일은 Close하고 새로운 AOF파일에 Write할 때 사용됩니다.
    - sub thread 2(BIO-AOF-Resync) : 쓰레드 2는 AOF에 쓰기 작업을 수행할 때 사용됩니다.
    - sub thread 3(BIO-Lazy-Free) : 쓰레드 3은 UNLINK, FLUSHALL, FLUSHDB 명령어를 실행할 때 빠른 성능을 보장하기 위해 백그라운드에서 사용됩니다.

## 메모리 운영기법
Redis는 InMemory DB에서 제공하는 최적의 성능 보장을 위해 메모리 영역을 효율적으로 운영 관리할 수 있는 LRU알고리즘과 LFU 알고리즘(4.0버전)을 제공합니다.

### LRU(Least Recently Used) 알고리즘
가장 최근에 처리된 데이터들을 메모리 영역에 최대한 재 배치시키는 알고리즘입니다. 즉, 사용할 수 있는 메모리 크기는 정해져 있는데 그 공간에 데이터를 저장해야 한다면 가장 최근에 입력, 수정, 삭제, 조회된 데이터를 저장하고 오래전에 처리된 데이터는 메모리로부터 제거하여 최근 사용된 데이터들이 최대한 메모리 상에 존재할 수 있도록 운영하는 것입니다. 사용자가 처리한 데이터들 중에 오래전에 처리한 데이터에 비해 상대적으로 최근에 처리한 데이터들이 재사용 또는 재검색될 가능성이 높을 수 밖에 없기 때문에 메모리 영역으로부터 즉시 데이터를 참조할 수 있도록 해줌으로써 성능 개선에 도움을 줄 수 있습니다.  
LRU 알고리즘으로 Redis 서버 인스턴스를 운영하기 위해서는 redis.conf 파일에 다음과 같이 관련 파라미터를 설정해야 합니다.
```sh
vi redis_5000.conf

Maxmemory 3000000000 # Redis 인스턴스를 위한 총 메모리 크기
maxmemory-sample 5 # LRU 알고리즘에 의한 메모리 운영
```
+ Maxmemory : Redis 서버를 위해 활성화할 수 있는 최대 메모리 크기를 제한할 때 사용합니다.
+ maxmemory-sample : LRU 알고리즘에 대한 메모리 운영시 사용합니다.

<br>

### LFU(Least Frequently Used) 알고리즘
가장 자주 참조되는 데이터만 배치하고 그렇지 않은 데이터들을 메모리로부터 제거하여 자주 참조되는 데이터들이 배치될 수 있도록 운영하는 방법입니다.  
LFU 알고리즘으로 Redis 서버를 운영하기 위해서는 redis.conf파일에 다음과 같이 관련 파라미터를 설정해야 합니다.
```sh
vi redis_5000.conf

lfu-log-factor 10 # LFU 알고리즘에 의한 메모리 운영 default 10
lfu-decay-time 1
```
<br>

## LazyFree 파라미터
빅데이터에 대한 쓰기 작업이 발생하면 Max 메모리에 도달하게 되는데 이때 연속 범위의 키에 대한 삭제 작업을 수행하게 되면 성능 지연문제가 발생하게 됩니다. LazyFree 쓰레드는 이 경우 백그라운드 작업을 수행해주기 때문에 빠른 처리가 가능해집니다.
```sh
vi redis_5000.conf

lazyfree-lazy-eviction      yes
lazyfree-lazy-expire        yes
lazyfree-lazy-server-del    yes
replica-lazy-flush          yes
lazyfree-lazy-user-del      yes
```
+ lazyfree-lazy-eviction : 메모리 영역이 Full 되었을 때 연속적인 범위의 Key 값을 삭제하면 기존 메모리 영역에 저장되어 있던 데이터는 DEL 명령어에 의해 삭제하는데 이 작업은 서버 프로세스의 main thread에 의해 실행되면서 블록킹(Blocking: 작업지연상태) 현상이 발생합니다. 이 파라메터 값을 yes로 설정하면 UNLINK 명령어가 실행되고 서버 프로세스의 Sub Thread3에 의해 백그라운드에서 수행되기 떄문에 블록킹 현상을 피할수 있습니다.
+ lazyfree-lazy-expire : EXPIRE 명령어를 실행하면 메모리 상에 무효화된 키 값을 삭제하는데 이 때 역시 DEL 명형어가 실행되면서 블록킹 현상이 발생합니다. 이 파라메터값을 yes로 설정하면 UNLINK 명형어가 실행되면서 블록킹 현상을 피할 수 있습니다.
+ lazyfree-lazy-server-del : 메모리 상에 이미 저장되어 있는 키 값에 대해 SET 또는 RENAME 명령어를 실행하면 내부적으로 DEL명령어가 실행되면서 블록킹 현상이 발생합니다. 이를 피하기 위해서는 파라메터 값을 yes로 설정해주어야합니다.
+ replica-lazy-flush : Master-Slave 또는 Partition-Replication 서버 환경에서 복제 서버는 마스터 서버의 데이터를 복제할 때 변경된 부분에 대해서만 부분 복제하는 경우도 있지만 때에 따라서는 기존 복제 데이터를 모두 삭제한 후 다시 복제하는 경우도 있습니다. 이 경우 기존 복제 데이터를 빠른 시간내에 삭제하고 동시에 다시 복제작업을 빠르게 수행해야 합니다. 이 파라메터 값을 yes로 설정하면 빠른 동기화 작업을 수행할 수 있습니다.
+ lazyfree-lazy-user-del : 6.0에서 추가된 옵션으로 Yes로 설정하면 DEL 명령이 내부적으로 UNLINK 명령으로 동작합니다. UNLINK 명령은 비동기로(async)로 키를 삭제해서 응답이 빠릅니다.

<Br>

## 데이터 Persistence
Redis도 RDB 처럼 File 기반으로 데이터를 저장할 수 있는 기능을 제공합니다.
```sh
vi redis_5000.conf

save 60 1000 # 60초마다 1000개의 Key 저장

appendonly yes  # aof 환경 설정
```
appendonly는 데이터를 Append Only File에 쓸지 여부를 정하는 파라미터입니다. yes/no로 설정할 수 있고 디폴트는 no입니다.  
레디스는 주 기억 장소가 메인 메모리(RAM)입니다. 그러므로 서버 이상 시(레디스 서버 비정상 종료, OS 이상 상황 발생, 서버 전원 끊김 등) 저장한 모든 데이터가 날아가게 됩니다. 이를 방지하기 위한 기능 중 하나가 Appendonly입니다. Appendonly를 yes로 설정하면 레디스 서버에 데이터가 입력/수정/삭제될 때 마다 디스크에 쓰기를 합니다. 그래서 서버 이상 시에도 데이터를 보존할 수 있습니다. 디스크에 써진 데이터는 레디스 서버 시작 시 읽켜서 메모리에 저장됩니다. 저장 파일명은 appendfilename 파라미터에서 정할 수 있습니다. AppendOnly, AppendOnlyFile을 줄여서 AOF라고 합니다. 디스크에 쓰는 시점은 appendfsync 파라미터로 정해집니다.


### RDB 파일을 이용하여 저장하는 방법
Save 명령어를 이용하여 일정한 주기마다 일정한 개수의 Key 데이터-셋 값을 디스크 상 dump.rdb 파일로 저장하는 방법입니다. 사용자가 저장 주기와 저장 단위를 결정할 수 있고 시스템 자원이 최소한으로 요구된다는 최대 장점이 있지만 데이터를 최종 저장한 이후 새로운 저장이 수행되는 시점 전에 시스템에 장애가 발생한 경우 데이터 유실이 발생할 수 있기 때문에 지속성이 떨어지는 단점이 있습니다. RDB 파일에는 Save 명령어를 실행한 시점에 메모리 상에 저장된 모든 데이터를 SnapShot 형태로 저장해 줍니다.
```sh
# 다음 상황을 가정하고 진행
# redis server를 redis_5000.conf로 start 
# redis에 이미 여러 키들이 저장되어 있음
# redis-cli로 접속한 상태

save # redis_5000.conf 에 세팅한대로 동작

exit

vi dump.rdb
```
dump.rdb 파일은 dafult로 src 위치에 저장됩니다.

### AOF(Append Only File) 명령어를 이용하여 저장하는 방법
AOF 명령어를 이용하여 디스크 상에 appendonly.aof 파일로 저장하는 방법입니다. 이 방법은 redis-shell 상에서 bgrewriteaof 명령어를 실행한 이후에 입력, 수정, 삭제되는 모든 데이터를 저장해 줍니다.
```sh
# 다음 상황을 가정하고 진행
# redis server를 redis_5000.conf로 start 
# redis에 이미 여러 키들이 저장되어 있음
# redis-cli로 접속한 상태

bgrewriteaof
keys *

exit

vi appendonly.aof
```


### RDS vs AOF
save 명령어와 AOF 명령어는 디스크 저장장치에 사용자가 데이터를 저장하는 용도로 사용된다는 공통점이 있지만 작동 방법이 다르기 때문에 선택적으로 사용해야 합니다.  

AOF(Append Only File)|RDS(SnapShot)
---|---
시스템 자원이 집중적으로 요구(지속적인 쓰기 작업 발생)|시스템 자원이 최소한으로 요구(특정 시점에 쓰기 작업이 발생)
마지막 시점까지 데이터 복구가 가능|지속성이 떨어짐(특정 시점마다 다름)
대용량 데이터 파일로 복구 작업 시 복구 성능이 떨어짐|복구 시간이 빠름
저장 공간이 압축되지 않기 때문에 데이터 양에 따라 크기 결정|저장 공간이 압축되기 때문에 최소 필요(별도의 파일 압축이 필요 없음)

<br>

## Data Export & Import
Redis 서버의 기본 아키텍처는 1차적으로 모든 데이터의 저장과 관리는 메모리에서 이루어집니다. 디스크에 즉시 저장하지 않는 이유는 디스크 IO로 인해 발생하는 성능 지연 문제를 피하고 이를 통해 성능을 개선시키는 것이 목적입니다. 하지만 예기치 못한 원인으로 인해 시스템이 종료되는 경우 데이터가 유실될 수 있기 때문에 모든 데이터를 메모리만 저장 관리할 수는 없습니다. 

### Save 명령어에 의해 Export 된 rdb 파일 import하는 방법
```sh
redis-cli -p 포트 --rdb /home/ec2-user/rdb위치/dump.rdb
```

### appendonly 명령어에 의해 Export 된 aof 파일 import하는 방법
```sh
redis-cli -p 포트 -n 1000 --pipe < appendonly.aof
```

### Redis Server 데이터를 text 파일로 export하는 방법
```sh
redis-cli -p 포트 --csv --scan > 20210830_data.csv
```