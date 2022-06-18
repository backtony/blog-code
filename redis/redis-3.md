# Redis - Master & Slave & Sentinel


# 1. Master & Slave & Sentinel
---
Redis 서버는 마스터-슬레이브, 마스터-슬레이브-센티넬, 파티션 클러스터 기능을 통해 데이터를 복제하고 분산 처리할 수 있습니다.

## 마스터-슬레이브
사용자 데이터를 실시간으로 처리(입력, 수정, 삭제, 조회)할 수 있는 마스터 서버 1대에 대해 슬레이브 서버는 마스터 서버의 데이터가 실시간으로 복제됩니다. 슬레이브는 마스터 서버에 의해서만 쓰기작업을 수행할 수 있고 사용자는 오직 읽기 작업만 수행할 수 있습니다. 마스터 서버에 장애가 발생하는 경우 슬레이브 서버는 마스터 서버로 자동 전환(FailOver)되지 않으며 사용자는 슬레이브 서버에 복제된 데이터를 이용하여 마스터 서버를 복구할 수는 있습니다. 마스터 서버에 장애가 발생하는 경우에도 슬레이브 서버에 대해서는 지속적인 읽기 작업을 수행할 수 있습니다. 하지만 쓰기 작업을 수행할 수는 없기 때문에 일시적인 서비스 중단이 발생할 수 밖에 없습니다.

## 마스터-슬레이브-센티널 
마스터-슬레이브의 문제점을 보완한 방식입니다. 센티널 서버는 일상적인 업무 환경에서는 마스터 서버와 슬레이브 서버를 지속적으로 모니터링 하다가 마스터 서버에 장애가 감지된 경우 슬레이브 서버를 즉시 마스터 서버로 자동 전환시켜 데이터 유실이 발생하지 않도록 Fail-Over하는 역할을 합니다.  
센티널 서버는 오직 마스터 서버와 슬레이브 서버의 장애 상태만을 모니터링하는 역할을 수행하기 때문에 좋은 하드웨어가 요구되지 않습니다. 또한 원본 센티널 서버에 장애가 발생하는 경우에 대비하여 복제 센티널 서버를 추가로 구성할 수도 있습니다. 안정적인 시스템 구성을 위해서 센티널 서버는 3대를 권장하며 최소 구성은 센티널 서버 1대 입니다. 센티널 서버의 기능을 정리하면 다음과 같습니다.
+ 마스터 서버와 슬레이브 서버가 어떤 상태인지 거의 실시간으로 HeartBeat를 통해 감시하고 관련 정보 제공
+ 장애가 발생해도 언제든지 서비스를 수행하기 위해 슬레이브 서버를 자동으로 마스터 서버로 올리는 Failover 기능 
+ 데이터 서버에 장애가 발생하는 경우 문자 또는 이메일 등을 통해 관련 상태 정보를 사용자에게 전달

## 마스터-슬레이브-센티널 구축 예제
![그림1](https://github.com/backtony/blog-code/blob/master/redis/img/3/2-1.PNG?raw=true)  
마스터 1대, 슬레이브 1대, 센티널 1대를 구축해보겠습니다.
+ Master : 3.36.90.220
+ Slave : 13.125.226.112
+ Sentinel : 52.79.78.8

__모든 서버 공통 작업 - 사용자 계정 생성 및 권한 부여, redis 설치__  
계정을 따로 생성하지 않고 ec2-user 계정에서 진행해도 상관 없습니다. 이 예시에서는 계정을 만들고 진행하겠습니다.
```sh
## 계정 생성 ##
sudo useradd redis -m # redis 이름의 계정 생성
sudo passwd redis # redis 계정 패스워드 변경
redis123 # redis123으로 패스워드 변경

## 해당 계정에 sudo 사용 권한 부여 ##
# sudoers 파일은 쓰기(w) 권한이 없기 때문에 vi 편집기로 수정할 수 없다. visudo 사용
# 해당 파일 수정은 아래 그림 참고
sudo visudo -f /etc/sudoers

su redis # redis 계정으로 로그인
redis 123 # 패스워드 입력

cd /home/redis # 이동

# 업데이트
sudo yum update -y
sudo yum install gcc make -y

# 설치
wget http://download.redis.io/redis-stable.tar.gz
tar xvzf redis-stable.tar.gz

# redis-stable 디렉토리 안의 파일 전부 옮기기
mv /home/redis/redis-stable/* /home/redis

# 압축파일, 압축해제 폴더 삭제
rm redis-stable.tar.gz
rm -rf redis-stable

make # // 소스 컴파일(=소스 파일을 실행 가능한 형태로 만들어 준다)
sudo make install #/usr/local/bin에 redis-server, redis-cli 등 실행 파일이 복사
![그림2](https://github.com/backtony/blog-code/blob/master/redis/img/3/2-2.PNG?raw=true)  

일반 사용자 계정명 ALL=(ALL) ALL을 추가하여 사용자가 sudo 명령어를 사용할 수 있도록 설정합니다. 이 설정만 하면 redis 계정에서 sudo를 사용할 때마다 password를 입력해야합니다. 따라서 패스워드 없이 sudo를 사용할 수 있도록 아래 Same thing without a password 부분에도 설정을 추가해주어 sudo를 패스워드 없이 사용할 수 있도록 해줍니다.
<br>

__Master 서버__
```sh
cd /home/redis

cp redis.conf redis_6379.conf
vi redis_6379.conf

## 아래의 설정 찾아서 수정 후 wq로 저장 ##
port 6379 # 포트
daemonize yes # 백그라운드 구동
masterauth redis123 # 보안설정 암호
requirepass redis123 # 서버의 암호
dir /home/redis/6379 # 데이터 경로
logfile /home/redis/redis_6379.log # 로그정보 파일
bind 0.0.0.0 # 레디스 서버 인스턴스에 모든 IP접근을 허용
protected-mode no # 외부 bind할 경우 no
## 수정 끝 ## 

mkdir 6379 # dir 폴더 생성

# 서버 실행
src/redis-server /home/redis/redis_6379.conf


# 접속하기
src/redis-cli -p 6379 -a redis123 # 또는 포트까지만 입력하고 이후 auth redis123 으로 로그인해도 됨
```
참고로 설정 파일에 example로 여러가지 설정이 들어가있어서 찾기 어려울 텐데 / 키를 누르고 찾고자 하는 단어를 입력하고 엔터를 누르면 찾을 수 있습니다. n키를 누르면 다음 N키를 누르면 이전으로 이동할 수 있습니다..

<br>

__Slave 서버__
```sh
cd /home/redis

cp redis.conf redis_6379.conf
vi redis_6379.conf

## 아래의 설정 찾아서 수정 후 wq로 저장 ##
port 6379 # 포트
daemonize yes # 백그라운드 구동
masterauth redis123 # 보안설정 암호
requirepass redis123 # 서버의 암호
dir /home/redis/6379 # 데이터 경로
logfile /home/redis/redis_6379.log # 로그정보 파일
replicaof 3.36.90.220 6379 # Slave가 바라볼 Master 서버 정보
bind 0.0.0.0 # 레디스 서버 인스턴스에 모든 IP접근을 허용
protected-mode no # 외부 bind할 경우 no
## 수정 끝 ## 

mkdir 6379 # dir 폴더 생성

# 서버 실행
src/redis-server /home/redis/redis_6379.conf

# 접속하기
src/redis-cli -p 6379 -a redis123 # 또는 포트까지만 입력하고 이후 auth redis123 으로 로그인해도 됨
```
<br>

__sentinel 서버__  
```sh
cd /home/redis

cp redis.conf redis_6379.conf
vi redis_6379.conf

## 아래의 설정 찾아서 수정 후 wq로 저장 ##
port 6379 # 포트
daemonize yes # 백그라운드 구동
dir /home/redis/6379 # 데이터 경로
logfile /home/redis/redis_6379.log # 로그정보 파일
# 센티넬 설정도 sample에는 없으므로 아무대나 적을 것 -> 포트 아래다 적음
sentinel monitor mymaster 3.36.90.220 6379 1 # 센티넬 서버가 바라볼 Master 정보
sentinel auth-pass mymaster redis123
sentinel down-after-milliseconds mymaster 3000 # 장애발생 시 FailOver Timeout (3초)
bind 0.0.0.0 # 레디스 서버 인스턴스에 모든 IP접근을 허용
protected-mode no # 외부 bind할 경우 no
## 수정 끝 ## 

mkdir 6379 # dir 폴더 생성

# 서버 실행
src/redis-sentinel /home/redis/redis_6379.conf

# 접속하기
src/redis-cli -p 6379 -a redis123 # 또는 포트까지만 입력하고 이후 auth redis123 으로 로그인해도 됨
```
+ sentinel monitor mymaster 3.36.90.220 6379 1
    - mymaster : Master 서버를 지징하는 별칭
    - 3.36.90.220 : Master 서버의 물리적 IP-Address
    - 6379 : Master의 포트 번호
    - 1 : quorum(정족수) default:2
        - 예를 들어 센티널이 3대 일때 quorum이 2이면 2대 이상의 센티널에서 해당 레디스 마스터가 다운되었다고 인식하면 장애조치(failover) 작업을 진행합니다. 테스트 환경에서는 센티널을 1대로 했기 때문에 quorum을 1로 설정합니다. 이 명령은 나중에 센티널에 접속해서 실행해도 됩니다. 반드시 sentinel.conf에 있을 필요는 없습니다.
    - sentinel down-after-milliseconds mymaster 3000 : 단위는 millisecond로 마스터가 다운되었다고 인지하는 시간입니다. 즉, 마스터 서버에 정기적으로 PING을 보내는데, 이 시간 동안 응답이 없으면 다운된 것으로 판단하고 장애조치(failover) 작업을 시작합니다. 3초에서 5초 사이로 설정합니다.

<br>

__Master 서버 확인__
```sh
## 입력 ## -> client 접속한 상태로 가정
info replication # 정보 확인

## 출력 화면 ##
role:master # 현재 서버가 master임 확인
connected_slaves:1 # master서버가 1개의 slave 서버가 존재

## 입력 ##
set foo bar # 테스트 데이터 입력
get foo
```
<Br>

__Slave 서버 확인__
```sh
## 입력 ## -> client 접속한 상태로 가정
info replication

## 출력 화면 ##
role:slave # slave 서버임을 확인
master_host: 3.36.90.220 # master 서버의 ip 확인
master_port: 6379        # master 서버의 port

## 입력 ##
get foo # master 서버에서 입력한 데이터를 slave서버에서 확인
```
<br>

__Sentinel 서버 확인__
```sh
## 입력 ## -> client 접속한 상태로 가정
info replication

## 출력 화면 ##
role:info sentinel

sentinel_masters:1 # 현재 서버가 sentinel인 것을 확인
```
<br>

__Fail-Over 테스트__  
Master 서버를 cli에서 shutdown 시키면 sentinel 서버에 의해 Slave 서버는 Master 서버가 됩니다.
```sh
## Master 서버에 client에 접속한 상태로 가정 ##
shutdown

## Slave 서버에 client에 접속한 상태로 가정 ##
info replication

## 출력 화면 ##
role:Master  # Master로 변경된 것을 확인

## Master 서버로 돌아와서 Master 서버 다시 키고 cli 접속
src/redis-server /home/redis/redis_6379.conf
src/redis-cli -p 6379 -a redis123 

ROLE # 역할 확인

## 출력 화면 ##
1) "slave" # 기존 Master 서버가 Slave 서버로 뜨는 것을 확인
2) "13.125.226.112" # 현재 Master 서버의 ip
3) (integer) 6379 
4) "connected"
5) (integer) 47800
```
만약 다시 Master 서버를 키게 된다면 Master 서버로 켜지지 않고 Slave가 됩니다.
<br>

__shutdown 없이 Step Down 하는 방법__  
master을 shutdown하지 않고 slave를 master로 바꾸는 방법 입니다.
```sh
## Slave 서버 cli 접속 상태로 가정 ##
CONFIG SET slave-read-only no # read only 모드 해제
REPLICAOF NO ONE # Step down (Slave를 Master로 전환)

info # 출력화면에서 role이 Master임을 확인
```
이렇게 구축을 끝냈습니다. 이 예제에서는 redis 계정을 만들고 진행했지만 해당 서버를 redis로만 사용한다면 굳이 계정 만들 필요 없이 ec2-user 계정에서 해도 될 것 같습니다.

<br>

# 2. 장애처리 방법
---
센티널 서버가 장애를 인지하고 Failover하는 절차에 대한 설명입니다.
1. 센티널 서버는 매 1초마다 HeartBeat를 통해 Master 서버와 Slave 서버가 작동 중인지 여부를 확인합니다. 만약, 일정 타임아웃 동안 더 이상 응답이 없으면 장애가 발생한 것으로 간주하는데 이를 주관적 다운(Subjectively Down)이라고 하며 로그 파일에 "+sdown"으로 표기됩니다. 이때 타임아웃 시간은 sentinel.conf 파일에 정의되어 있는 down-after-milliseconds 파라미터에 의해 결정되며 기본 값은 3000millisecond 입니다.
2. 주관적 다운은 하나의 센티널 서버가 장애 상태를 인지한 경우이며 만약 센티널 서버가 여러 대인 경우 모든 센티널 서버가 장애 상태를 인지한다면 이를 객관적 다운(Objectively Down)이라고 하며 로그 파일에 "+odown" 상태로 표기됩니다. 센티널 서버는 Master 서버가 다운된 경우 다른 센티널 서버와 함께 전체 정족수를 확인한 다음 이에 미치지 못한 경우 최종적으로 실제 다운되었다고 판단합니다.
3. 주관적 다운과 객관적 다운이 최종 확인되면 장애조치 작업을 단계별로 수행합니다.
    - 여러 대의 센티널 서버로 구축되어 있는 경우 센티널 리더를 선출해야 하는데 이를 위해 내부에서는 선출과정이 반복적으로 수행되고 최종 센티널 리더가 설출됩니다.
    - 리더로 결정된 센티널 서버는 장애가 발생한 Master 서버를 대신할 Slave서버를 선정합니다.
    - 선정된 Slave 서버는 최종 Master 서버로 승격됩니다.
    - 남은 Slave 서버가 새로운 Master 서버를 모니터링 하도록 명령을 수행합니다.
    - 모든 작업이 완료되면 센티널 서버 정보를 갱신하고 장애 복구 작업을 종료합니다.

<br>

## 특정 센티널 서버를 리더로 결정하는 방법
여러 대의 센티널 서버로 구축되어 있는 시스템 환경에서 장애가 발생하는 경우 센티널 서버 리더를 결정해야 할 때 불필요한 시간 낭비를 최소화해야 할 필요가 있거나, 아니면 서버 관리자의 의도대로 장애 조치를 수행해야하는 경우에는 다음과 같이 우선 순위가 결정될 수 있는 값을 부여할 수 있습니다. 만약 3대의 센티널 서버가 구축되어 있는 경우 down-after-milliseconds는 주관적 다운 상태를 인지할 수 있도록 설정된 값인데 이 값을 각각 다르게 설정해주면 첫 번째 센티널 서버가 다운된 경우 15000 값을 설정한 두 번째 서버가 센티널 서버 리더가 될 수 있습니다.
```sh
vi sentinel1.conf
down-after-milliseconds 1000

vi sentinel2.conf
down-after-milliseconds 1500

vi sentinel3.conf
down-after-milliseconds 2000
```

## 여러 대의 Slave 서버 중에서 Master 서버가 될 수 있는 우선순위 결정 방법
```sh
vi redis_5000.conf
slave-priority 100

vi redis_5001.conf
slave-priority 101

vi redis_5002.conf
slave-priority 102
```
5000번 포트를 Master라고 하고 5001과 5002를 Slave 서버라고 할 경우 마스터가 장애가 발생하면 5001서버가 Master로 Failover 됩니다.
<br>

# 3. 부분 동기화
![그림4](https://github.com/backtony/blog-code/blob/master/redis/img/3/2-3.PNG?raw=true)  

1. Master 1대, Slave 2대로 구성된 복제 서버 환경에서 Master에 장애가 발생하는 경우 새로운 Master 데이터를 Slave 로 복제합니다.
2. 변경된 데이터에 대한 Partial Resync 작업을 수행합니다.
3. 동기화해야 할 부분 데이터의 크기가 repl-backlog-size 파라미터의 크기보다 큰 경우에는 Full sync작업이 수행됩니다.

Master 서버는 항상 Read/Write가 가능하지만 Slave 서버는 기본적으로 Read Only만 가능하도록 설계됩니다. 이때 Master 서버에서 장애가 발생하는 경우 데이터 유실이 발생할 수밖에 없는데 이를 방지하기 위해 동기화 작업을 수행합니다. 실제 동기화해야 할 데이터가 적은 경우 전체 동기화 작업을 수행하게 될 경우 불필요한 작업이 반복될 수 있기 때문에 부분 동기화 작업이 가능하도록 repl-backlog-size 파라미터를 제공하고 있습니다.
```sh
vi redis_5000.conf # Master conf

repl-backlog-size 10MB # 10mb 이상 권장
```