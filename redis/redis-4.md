# Redis Cluster 구축 및 성능 튜닝


# 1. Redis Cluster 구축 및 운영
---
![그림1](https://github.com/backtony/blog-code/blob/master/redis/img/4/3-1.PNG?raw=true)  

하나의 스탠드어론 서버만으로 처리할 수 없을 만큼 빅데이터가 발생하는 비즈니스 환경에서는 성능 지연 문제 뿐만 아니라 다양한 장애 현상이 빈번하게 발생할 수 있습니다. 이 때 해결 방안으로는 서버의 자원을 업그레이드 하는 Scale-up 방식과 별도의 서버를 추가하는 Scale-out 방식이 있습니다. Scale-up의 경우 서버의 조건에 따라 매우 제한적이므로 보통 Scale-out을 을 사용합니다.  
하나의 테이블에 저장되는 데이터를 2개 이상의 서버로 동시에 분산 저장 하는 방법을 샤딩이라고 합니다. 샤딩을 통해 데이터를 분산 저장하다보면 장애가 생겨되어 데이터 유실이 발생할 수 있는데 이를 방지하기 위해 분산 서버마다 복제 서버를 함께 구축해서 운영하게 됩니다. 이와 같이 데이터 분산 처리를 위한 샤딩과 안정성 확보를 위한 복제 시스템은 함께 사용될 수밖에 없는데 이를 Redis 클러스터(Shared-Replication)이라고 표현합니다. 클러스터 환경에서 센티널 서버는 요구되지 않습니다. 위 그림은 3개의 분산 서버(마스터)마다 복제 서버(슬레이브)로 구축된 Redis 클러스터 시스템이며 Redis Cluster Manager을 통해 운용 관리되고 있는 구성도 입니다.  

# 2. Redis Cluster 주요 특징
---
+ 클러스터 모드는 Database 0번만 사용 가능합니다.
+ 클러스터 모드에서는 MSET 명령어를 실행할 수 없으며 Hash-Tag를 통해 데이터를 표현할 수 있고 데이터를 분산 저장합니다.
+ 기본적으로 Master, Slave 서버만으로 구성되며 Sentinel 서버는 요구되지 않습니다.
+ Redis 서버는 기본적으로 16,384개의 슬롯을 가지는데 빅데이터를 여러 대의 서버에 분산 저장할 때 각 슬롯 당 데이터를 일정한 단위로 분류하여 저장할 때 사용됩니다. 예를 들어, 3대의 Redis 서버가 구축되어 있는 환경에서 첫 번째 서버에는 0~5460, 두 번째 서버에는 5461~10922, 세번째는 10923~16384 슬롯 정보가 분산될 것입니다. 서버수가 증가하면 증가된 수를 16384로 나누면 각 서버에 저장될 슬롯 정보를 가늠해 볼 수 있습니다.
+ 해시 파티션을 통해 데이터를 분산 저장할 수 있는데 이 경우 해시 함수는 CRC16 함수를 사용합니다.

# 3. 구성 방식
---
Redis 클러스터 시스템을 구축하는 방법은 2가지가 있습니다.
+ Cluster 명령어를 이용한 수동 설정 방법
    - 사용자가 직접 물리적 설계를 해야 합니다.
    - 최적화된 Cluster 서버 환경을 구축할 수 있습니다.
    - 장애 발생 시 관리자의 의도에 따라 대응할 수 있습니다.
+ redis-cli 유틸리티를 이용한 자동 설정 방법
    - 자동화된 알고리즘을 통해 Cluster 서버를 구축할 수 있습니다.
    - Cluster 구축 및 운용 관리에 대한 기술적 이해가 부족하더라도 구현가능 합니다.
    - 장애 발생 시 자동화 알고리즘을 통해 시스템 재구성이 수행됩니다.

## Cluster 명령어를 이용한 수동 설정
계정을 생성하지 않고 ec2-user로 진행하겠습니다.
```sh
# 뒤쪽에서 사용할 config 파일 설명 미리보기
vi redis_5001.conf
port 5001
cluster-enable yes
cluster-config-file /home/ec2-user/redis/redis-cluster/5001/nodes-5001.conf
cluster-node-timeout 5000
dir /home/ec2-user/redis/redis-cluster/5001/data
appendonly yes
daemonize yes
```
+ cluster-enable : 클러스터 모드 설정을 위한 기본 파라미터
+ cluster-config-file : 클러스터 노드 상태 정보를 기록하는 Binary 파일이며 해당 파일의 경로와 파일명을 지정하는 파라미터
+ cluster-node-timeout : 클러스터 노드가 다운된 상태인지 여부를 판단하기 위한 타임아웃 시간을 설정하는 파라미터이며 지정된 시간 내에 노드가 응답하지 않으면 다운된 것으로 판단

하나의 Master 또는 두 대의 Master 서버 환경으로 구축할 수 있지만 분산과 복제를 기반으로 하는 클러스터 서버 구축이 목적이므로 최소 3대의 Master 서버를 통해 데이터를 분산 저장해야 하며 각 Master 서버에 장애가 발생하는 경우에 대비하기 위해 각 Slave 서버를 통해 복제 시스템 구축을 목표로 해야 합니다. 데이터를 분산 저장하는 목적은 초당 10만 건 이상의 데이터를 빠르게 저장하는 것이 목적인데 만약 하나의 서버에 장애가 발생한다면 분산 처리할 수 없게 되므로 Master 서버는 적정 3대가 권장됩니다. 이와 같은 경우 최소한의 분산과 복제 시스템을 구축할 수 있는데 만약 첫 번째 Master 서버와 복제 서버가 동시에 장애가 발생하는 최악의 경우도 예상해 볼 수 있습니다. 하지만 안전성 확보가 무엇보다도 요구되는 비즈니스 환경이라면 Master1 - slave 1 - slave 2와 같이 slave 서버를 추가로 확보했을 때 가장 이상적인 시스템을 구축할 수 있겠지만 서버를 추가하는 경우 유지보수 비용이 증가하는 문제점이 있으므로 충분한 고려를 해야 합니다.  
Redis 서버의 경우 반드시 Master 서버 수와 Slave 서버 수가 1:1로 매핑되어야할 필요는 없습니다. 예를 들면 Master1 - Slave1, Master2 - Slave1 - Slave2 로 구성할 수도 있습니다. Cluster 서버 명령어를 이용한 수동 설정 방법은 사용자가 직접 설계하고 설치하기 때문에 Master 서버와 Slave 서버를 균형 맞게 구축할 수 있지만 redis-cli 유틸리티를 이용한 설치 방법에서는 유틸리티에 의해 자동 설치되기 때문에 사용자의 의도대로 구축할 수 없는 경우가 발생할 수 있습니다.  
하나의 서버 안에서 구축하도록 하겠습니다.
```sh
# redis 설치
sudo yum update -y
sudo yum install gcc make -y

wget http://download.redis.io/redis-stable.tar.gz
tar xvzf redis-stable.tar.gz

mkdir redis
mv redis-stable/* /home/ec2-user/redis
rm -rf redis-stable
rm redis-stable.tar.gz
cd /home/ec2-user/redis

make 
sudo make install 

# ruby 설치 - Cluster 명령어는 Ruby이기 때문
sudo amazon-linux-extras install ruby3.0
gem install redis

# 이동 후 config 파일 작성
mkdir -p redis-cluster/5001/data
cd /home/ec2-user/redis/redis-cluster/5001
vi redis_5001.conf

## 첫 번째 노드 환경 ##
# config 작성
port 5001
cluster-enabled yes
cluster-config-file /home/ec2-user/redis/redis-cluster/5001/nodes-5001.conf
cluster-node-timeout 5000
dir /home/ec2-user/redis/redis-cluster/5001/data
appendonly yes
daemonize yes

# 실행
/home/ec2-user/redis/src/redis-server redis_5001.conf

## 두 번째 노드부터 여섯 번째 노드까지 동일한 환경 ##
mkdir -p /home/ec2-user/redis/redis-cluster/5002/data
cd /home/ec2-user/redis/redis-cluster/5002
vi redis_5002.conf

port 5002
cluster-enabled yes
cluster-config-file /home/ec2-user/redis/redis-cluster/5002/nodes-5002.conf
cluster-node-timeout 5000
dir /home/ec2-user/redis/redis-cluster/5002/data
appendonly yes
daemonize yes

# 실행
/home/ec2-user/redis/src/redis-server redis_5003.conf

## 6개의 노드를 실행한 상태 ##
# Cluster 멤버 등록
redis-cli -h 127.0.0.1 -p 5001 cluster meet 127.0.0.1 5002
redis-cli -h 127.0.0.1 -p 5001 cluster meet 127.0.0.1 5003
redis-cli -h 127.0.0.1 -p 5001 cluster meet 127.0.0.1 5004
redis-cli -h 127.0.0.1 -p 5001 cluster meet 127.0.0.1 5005
redis-cli -h 127.0.0.1 -p 5001 cluster meet 127.0.0.1 5006

# 등록된 멤버 확인
redis-cli -h 127.0.0.1 -p 5001 cluster nodes

## 출력 화면 ##

1e61bafb8cd7531f732390fb4f8aa76acc0fbb13 127.0.0.1:5001@15001 myself,master - 0 1630480004000 1 connected
73b85fb60f29ead4d401593235210745c77f9b8a 127.0.0.1:5002@15002 master - 0 1630480003000 2 connected
9e2d2346857c70268669f55c9f8c8fb4bc39759f 127.0.0.1:5003@15003 master - 0 1630480003866 0 connected
146ce4b9204afccb2718d682e0d7f114998e39bf 127.0.0.1:5004@15004 master - 0 1630480003565 3 connected
2d28b1a3d2dea05e770e172c251609adab6b82ec 127.0.0.1:5005@15005 master - 0 1630480004869 4 connected
034034945a38bcb745f0295876cd84f98486bfdb 127.0.0.1:5006@15006 master - 0 1630480004568 5 connected

# 4번 노드를 1번 Master 서버의 복제 서버로 설정
redis-cli -h 127.0.0.1 -p 5004 cluster replicate 1e61bafb8cd7531f732390fb4f8aa76acc0fbb13

# 5번 노드를 2번 Master 서버의 복제 서버로 설정
redis-cli -h 127.0.0.1 -p 5005 cluster replicate 73b85fb60f29ead4d401593235210745c77f9b8a

# 6번 노드를 3번 Master 서버의 복제 서버로 설정
redis-cli -h 127.0.0.1 -p 5006 cluster replicate 9e2d2346857c70268669f55c9f8c8fb4bc39759f

# 마스터 노드에 슬롯 설정
redis-cli -h 127.0.0.1 -p 5001 cluster addslots {0..5461}
redis-cli -h 127.0.0.1 -p 5002 cluster addslots {5462..10922}
redis-cli -h 127.0.0.1 -p 5003 cluster addslots {10923..16383}

# 설정된 슬롯 확인
redis-cli -h 127.0.0.1 -p 5005 cluster slots

# 클러스터 설정 상태 확인
redis-cli -c -h 127.0.0.1 -p 5001 cluster info
```
구축이 끝났습니다.  
<Br>

### 새로운 노드 추가 삭제
```sh
# 환셩 설정
mkdir -p /home/ec2-user/redis/redis-cluster/5007/data
cd /home/ec2-user/redis/redis-cluster/5007
vi redis_5007.conf

port 5007
cluster-enabled yes
cluster-config-file /home/ec2-user/redis/redis-cluster/5002/nodes-5007.conf
cluster-node-timeout 5000
dir /home/ec2-user/redis/redis-cluster/5007/data
appendonly yes
daemonize yes

# 실행
/home/ec2-user/redis/src/redis-server redis_5007.conf

# 추가
redis-cli -h 127.0.0.1 -p 5001 cluster meet 127.0.0.1 5007

# 5007이 들어갔는지 확인
redis-cli -h 127.0.0.1 -p 5001 cluster nodes

## 출력 화면 ## 
034034945a38bcb745f0295876cd84f98486bfdb 127.0.0.1:5006@15006 slave 9e2d2346857c70268669f55c9f8c8fb4bc39759f 0 1630482240048 0 connected
1e61bafb8cd7531f732390fb4f8aa76acc0fbb13 127.0.0.1:5001@15001 myself,master - 0 1630482240000 1 connected 0-5461
73b85fb60f29ead4d401593235210745c77f9b8a 127.0.0.1:5002@15002 master - 0 1630482240449 2 connected 5462-10922
9e2d2346857c70268669f55c9f8c8fb4bc39759f 127.0.0.1:5003@15003 master - 0 1630482240950 0 connected 10923-16383
631b92fc5e4e95ec78711b0cf508ace188976df2 127.0.0.1:5007@15007 master - 0 1630482239000 6 connected
2d28b1a3d2dea05e770e172c251609adab6b82ec 127.0.0.1:5005@15005 slave 73b85fb60f29ead4d401593235210745c77f9b8a 0 1630482240000 2 connected
146ce4b9204afccb2718d682e0d7f114998e39bf 127.0.0.1:5004@15004 slave 1e61bafb8cd7531f732390fb4f8aa76acc0fbb13 0 1630482239000 1 connected

# 제거
# FORGET 옵션절은 전체 노드에서 실행해야 하며 (타임아웃 60초 범위 내에서 실행해야 함)
redis-cli -h 127.0.0.1 -p 5001 cluster forget 631b92fc5e4e95ec78711b0cf508ace188976df2
redis-cli -h 127.0.0.1 -p 5002 cluster forget 631b92fc5e4e95ec78711b0cf508ace188976df2
redis-cli -h 127.0.0.1 -p 5003 cluster forget 631b92fc5e4e95ec78711b0cf508ace188976df2
redis-cli -h 127.0.0.1 -p 5004 cluster forget 631b92fc5e4e95ec78711b0cf508ace188976df2
redis-cli -h 127.0.0.1 -p 5005 cluster forget 631b92fc5e4e95ec78711b0cf508ace188976df2
redis-cli -h 127.0.0.1 -p 5006 cluster forget 631b92fc5e4e95ec78711b0cf508ace188976df2

# 5007이 제거되었는지 확인
redis-cli -h 127.0.0.1 -p 5001 cluster nodes
```
<br>

## redis-cli 유틸리티를 이용한 자동 설정방법
```sh
# 뒤쪽에서 사용할 config 파일 설명 미리보기
vi redis_5001.conf
port 5001
cluster-enable yes
cluster-config-file /home/ec2-user/redis/redis-cluster/5001/nodes-5001.conf
cluster-node-timeout 5000

cluster-require-full-coverage yes
cluster-migration-barrier 1
cluster-replica-validity-factor 10

dir /home/ec2-user/redis/redis-cluster/5001
appendonly yes
daemonize yes
```
+ cluster-require-full-coverage : cluster의 일부 node가 다운되어도 운영할 방법 설정
    - yes : slave가 없는 master가 다운되면 cluster 전체 중지
    - no : slave가 없는 master가 다운되더라도 cluster 유지
    - 일부 데이터가 유실되도 괜찮다면 no, 데이터의 정합성이 중요하다면 yes
    - no를 설정하더라도 절반 이상의 node가 다운되면 cluster는 중지
+ cluster-migration-barrier : master에 연결되어 있어야 하는 최소 slave수(기본값 = 1)
+ cluster-slave-validity-factor : cluster는  master node 다운 시 해당 노드의 slave node를 master로 변경하는 장애조치(failover)를 시작하는데 이때 master와 slave node 간의 체크가 일정 시간 동안 단절된 상태라면 해당 slave는 승격 대상에서 제외됩니다. 이때 승격 대상에서 제외하는 판단 기준의 시간을 설정하는 옵션입니다
    - 문서에 따르면 만약 값이 0 이라면, 항상 자신을 유효한 것으로 간주하므로 마스터와 슬레이브 사이의 링크가 연결이 끊긴 시간에 관계없이 항상 마스터를 페일오버하려고 시도합니다.
    - 계산식 : (cluster-node-timeout * cluster-replica-validity-factor) + repl-ping-slave-period
    - 각각을 디폴트 값으로 계산하면 (ms는 초로 변경) -> 디폴트 값으로 계산시 15 * 10 + 10 = 160초, -> 위 설정에서는 5 * 10 + 10 = 60초
    - repl-ping-slave-period : 복제 서버는 마스터에 repl-ping-replica-period 간격(default값은 10초)으로 ping을 보내는데 timeout 시간 동안 응답이 없거나 마스터로 부터 timeout 시간(default 60초) 동안 데이터가 오지 않으면 마스터와의 연결이 다운된 것으로 인식합니다.

<Br>

동일하게 3대의 분산 서버(Master)와 3대의 복제 서버(Slave)로 클러스터 서버를 하나의 EC2 안에서 구축시키겠습니다.
```sh
# redis 설치
sudo yum update -y
sudo yum install gcc make -y

wget http://download.redis.io/redis-stable.tar.gz
tar xvzf redis-stable.tar.gz

mkdir redis
mv redis-stable/* /home/ec2-user/redis
rm -rf redis-stable
rm redis-stable.tar.gz
cd /home/ec2-user/redis

make 
sudo make install 

# ruby 설치 - Cluster 명령어는 Ruby이기 때문
sudo amazon-linux-extras install ruby3.0 -y
gem install redis

# 이동 후 config 파일 작성
mkdir redis-cluster
cd redis-cluster
mkdir 5001 5002 5003 5004 5005 5006
cp ../redis.conf ./5001/.
cp ../redis.conf ./5002/.
cp ../redis.conf ./5003/.
cp ../redis.conf ./5004/.
cp ../redis.conf ./5005/.
cp ../redis.conf ./5006/.

# 5001 config 설정
cd 5001
vi redis.conf

port 5001
cluster-enable yes
cluster-config-file /home/ec2-user/redis/redis-cluster/5001/nodes-5001.conf
cluster-node-timeout 5000

cluster-require-full-coverage yes
cluster-migration-barrier 1
cluster-replica-validity-factor 0

dir /home/ec2-user/redis/redis-cluster/5001
appendonly yes
daemonize yes

# 5002 ~ 5006 config 설정
cd 5002
port 5002
cluster-enable yes
cluster-config-file /home/ec2-user/redis/redis-cluster/5001/nodes-5001.conf
cluster-node-timeout 5000
dir /home/ec2-user/redis/redis-cluster/5002
appendonly yes
daemonize yes

# 실행
# 재실행할 경우 반드시 nodes-500x.conf 파일을 제거해야합니다.
redis-server /home/ec2-user/redis/redis-cluster/5001/redis.conf
redis-server /home/ec2-user/redis/redis-cluster/5002/redis.conf
redis-server /home/ec2-user/redis/redis-cluster/5003/redis.conf
redis-server /home/ec2-user/redis/redis-cluster/5004/redis.conf
redis-server /home/ec2-user/redis/redis-cluster/5005/redis.conf
redis-server /home/ec2-user/redis/redis-cluster/5006/redis.conf

# 클러스터 구성
# 각 port를 cluster의 node로 묶고 master는 최소 1개의 slave를 가지도록 하는 명령어
# 입력된 순서대로 앞선 3개가 master, 뒤에 3개가 slave로 결정
# Redis Cluster 서버는 최소 3대의 마스터 서버가 요구되기 때문에 3대의 서버 대수를 제공하지 못하면 환경 설정 자체를 수행 불가능
redis-cli --cluster create 127.0.0.1:5001 127.0.0.1:5002 127.0.0.1:5003 127.0.0.1:5004 127.0.0.1:5005 127.0.0.1:5006 --cluster-replicas 1

# cli를 클러스터모드(-c)로 접속
# 클러스터 모드로 접속해야 다른 노드 데이터도 조회 가능
redis-cli -c -h 127.0.0.1 -p 5001

# 상태 확인
cluster info

## 출력 화면 ##
cluster_state:ok
cluster_slots_assigned:16384
cluster_slots_ok:16384
cluster_slots_pfail:0
cluster_slots_fail:0
cluster_known_nodes:6       # 총 노드 수
cluster_size:3              # 구축된 Master 서버 수
cluster_current_epoch:6
cluster_my_epoch:1
cluster_stats_messages_ping_sent:127
cluster_stats_messages_pong_sent:140
cluster_stats_messages_sent:267
cluster_stats_messages_ping_received:135
cluster_stats_messages_pong_received:127
cluster_stats_messages_meet_received:5
cluster_stats_messages_received:267
```
구축이 끝났습니다.

<Br>

### 새로운 노드 추가 삭제
```sh
### 추가 작업 ###
# 5007 ~ 5008 설정
cd /home/ec2-user/redis/redis-cluster
mkdir 5007
cp ../redis.conf ./5007/.
cd 5007
vi redis.conf

port 5007
cluster-enable yes
cluster-config-file /home/ec2-user/redis/redis-cluster/5007/nodes-5007.conf
cluster-node-timeout 5000
dir /home/ec2-user/redis/redis-cluster/5007
appendonly yes
daemonize yes

# 실행
redis-server /home/ec2-user/redis/redis-cluster/5007/redis.conf
redis-server /home/ec2-user/redis/redis-cluster/5008/redis.conf

## 클러스터에 노드 추가 ##
# Master 노드 추가
# redis-cli --cluster add-node <new master ip:port> <마스터ip:port>
# 7번 노드를 Master 노드로 추가
redis-cli --cluster add-node 127.0.0.1:5007 127.0.0.1:5001 

# slave 노드 추가
# redis-cli --cluster add-node <replica ip:port> <마스터ip:port> --cluster-slave
# 8번 노드를 7번 노드의 slave로 설정
redis-cli --cluster add-node 127.0.0.1:5008 127.0.0.1:5007 --cluster-slave
# Master/slave 노드 확인
redis-cli -h 127.0.0.1 -p 5001 cluster nodes

redis-cli -c -h 127.0.0.1 -p 5001
cluster info

## 출력 화면 ##
cluster_state:ok
cluster_slots_assigned:16384
cluster_slots_ok:16384
cluster_slots_pfail:0
cluster_slots_fail:0
cluster_known_nodes:8
cluster_size:3              # 마스터 노드수가 노드 7번이 반영이 안됨
cluster_current_epoch:6
cluster_my_epoch:1
cluster_stats_messages_ping_sent:3064
cluster_stats_messages_pong_sent:3120
cluster_stats_messages_sent:6184
cluster_stats_messages_ping_received:3114
cluster_stats_messages_pong_received:3064
cluster_stats_messages_meet_received:6
cluster_stats_messages_received:6184

## 7,8번 노드가 추가되었지만 Reresh가 안된 상태이기 때문에 전체 서버로 재분산 작업 수행 ##
# resharding 작업 실행
redis-cli --cluster reshard 127.0.0.1:5001

## 출력 화면 ##
M: e77fe30c900cfe3bbe6891e3bb80b0b4d69150f9 127.0.0.1:5001
   slots:[0-5460] (5461 slots) master
   1 additional replica(s)
S: 1a89d2eebb71744241150a3e1be02db732d1a392 127.0.0.1:5005
   slots: (0 slots) slave
   replicates 449a92cba41d41480e064be0cc301f395dd23c91
M: 449a92cba41d41480e064be0cc301f395dd23c91 127.0.0.1:5003
   slots:[10923-16383] (5461 slots) master
   1 additional replica(s)
M: 14d76178fa2f880352394a6f4cdb1176889d1f27 127.0.0.1:5002
   slots:[5461-10922] (5462 slots) master
   1 additional replica(s)
M: 47473c468e2e1eed14ad6006241de22a881ccce3 127.0.0.1:5007
   slots: (0 slots) master
   1 additional replica(s)
S: 782a86de935645c49b74f580e5a9a142bcdac27b 127.0.0.1:5006
   slots: (0 slots) slave
   replicates e77fe30c900cfe3bbe6891e3bb80b0b4d69150f9
S: ff57d92eca1a16c2534a883ff957859af5ba60cd 127.0.0.1:5008
   slots: (0 slots) slave
   replicates 47473c468e2e1eed14ad6006241de22a881ccce3
S: b6ed6a09a14d49d663c7cad8b8ed3123ae579164 127.0.0.1:5004
   slots: (0 slots) slave
   replicates 14d76178fa2f880352394a6f4cdb1176889d1f27

# 이동 시킬 슬롯 수 결정
# 추후에 rebalance로 슬롯수 조정 예정이므로 그냥 1로 줍니다.
How many slots do you want to move (from 1 to 16384)? 1

# 슬롯 받을 Node id
What is the receiving node ID ? 47473c468e2e1eed14ad6006241de22a881ccce3

# 슬롯 빼올 Node id
Source node #1: e77fe30c900cfe3bbe6891e3bb80b0b4d69150f9
Source node #2: done

# 선택사항 확인
Do you want to proceed with the proposed reshard plan (yes/no)? yes

# 확인
redis-cli -c -h 127.0.0.1 -p 5001
cluster info

## 출력 화면 ##
cluster_state:ok
cluster_slots_assigned:16384
cluster_slots_ok:16384
cluster_slots_pfail:0
cluster_slots_fail:0
cluster_known_nodes:8
cluster_size:4              # master 서버 반영 완료
cluster_current_epoch:7
cluster_my_epoch:1
cluster_stats_messages_ping_sent:8326
cluster_stats_messages_pong_sent:8601
cluster_stats_messages_sent:16927
cluster_stats_messages_ping_received:8594
cluster_stats_messages_pong_received:8327
cluster_stats_messages_meet_received:7
cluster_stats_messages_received:16928

# 슬롯수 재분배
redis-cli --cluster rebalance 127.0.0.1:5001


### 삭제 작업 ###
## 마스터에 장애를 유발시키고 슬레이브 서버 중 하나가 마스터 서버로 Failover 되는지 확인해보기
redis-cli -h 127.0.0.1 -p 5001 cluster nodes | grep master # Master 노드만 확인
redis-cli -p 5002 debug segfault # 5002 서버 강제 종료

# 다시 5002 키기 -> slave로 켜짐
redis-server /home/ec2-user/redis/redis-cluster/5002/redis.conf

## 출력 화면 ##
# Master fail된 것과 새로 Master로 승격된 5004 포트를 확인할 수 있음
449a92cba41d41480e064be0cc301f395dd23c91 127.0.0.1:5003@15003 master - 0 1630564242453 3 connected 12288-16383
14d76178fa2f880352394a6f4cdb1176889d1f27 127.0.0.1:5002@15002 master,fail - 1630564207296 1630564205000 2 disconnected
47473c468e2e1eed14ad6006241de22a881ccce3 127.0.0.1:5007@15007 master - 0 1630564242554 7 connected 0-1364 5461-6826 10923-12287
e77fe30c900cfe3bbe6891e3bb80b0b4d69150f9 127.0.0.1:5001@15001 myself,master - 0 1630564243000 1 connected 1365-5460
b6ed6a09a14d49d663c7cad8b8ed3123ae579164 127.0.0.1:5004@15004 master - 0 1630564243457 8 connected 6827-10922

# 테스트할 더미 키 밸류 입력
redis-cli -c -h 127.0.0.1 -p 5001
set 1 "test"
set 2 "test"
set 3 "test"
set 4 "test"
set 5 "test"
set 6 "test"
set 7 "test"
set 8 "test"
set 9 "test"
set 10 "test"

# 각각 cli에 들어가서 골고루 저장되어있는지 확인
redis-cli -c -h 127.0.0.1 -p 5001
keys *
redis-cli -c -h 127.0.0.1 -p 5002 
keys *
...

# 노드 Id 확인
redis-cli -h 127.0.0.1 -p 5001 cluster nodes

## 출력 화면 ##
1a89d2eebb71744241150a3e1be02db732d1a392 127.0.0.1:5005@15005 slave 449a92cba41d41480e064be0cc301f395dd23c91 0 1630565072662 3 connected
449a92cba41d41480e064be0cc301f395dd23c91 127.0.0.1:5003@15003 master - 0 1630565072161 3 connected 12288-16383
14d76178fa2f880352394a6f4cdb1176889d1f27 127.0.0.1:5002@15002 slave b6ed6a09a14d49d663c7cad8b8ed3123ae579164 0 1630565073063 8 connected
47473c468e2e1eed14ad6006241de22a881ccce3 127.0.0.1:5007@15007 master - 0 1630565072060 7 connected 0-1364 5461-6826 10923-12287
782a86de935645c49b74f580e5a9a142bcdac27b 127.0.0.1:5006@15006 slave e77fe30c900cfe3bbe6891e3bb80b0b4d69150f9 0 1630565072060 1 connected
e77fe30c900cfe3bbe6891e3bb80b0b4d69150f9 127.0.0.1:5001@15001 myself,master - 0 1630565071000 1 connected 1365-5460
ff57d92eca1a16c2534a883ff957859af5ba60cd 127.0.0.1:5008@15008 slave 47473c468e2e1eed14ad6006241de22a881ccce3 0 1630565071558 7 connected
b6ed6a09a14d49d663c7cad8b8ed3123ae579164 127.0.0.1:5004@15004 master - 0 1630565071156 8 connected 6827-10922

# 5008 노드 클러스터에서 삭제
# redis-cli --cluster del-node <node IP>:<port> <Node Id>
redis-cli --cluster del-node 127.0.0.1:5008 ff57d92eca1a16c2534a883ff957859af5ba60cd

# Master 서버인 5007은 reshard 해야만 제거가 가능
redis-cli --cluster reshard 127.0.0.1:5001

## 출력 화면 ##
M: e77fe30c900cfe3bbe6891e3bb80b0b4d69150f9 127.0.0.1:5001
   slots:[1365-5460] (4096 slots) master
   1 additional replica(s)
S: 1a89d2eebb71744241150a3e1be02db732d1a392 127.0.0.1:5005
   slots: (0 slots) slave
   replicates 449a92cba41d41480e064be0cc301f395dd23c91
M: 449a92cba41d41480e064be0cc301f395dd23c91 127.0.0.1:5003
   slots:[12288-16383] (4096 slots) master
   1 additional replica(s)
S: 14d76178fa2f880352394a6f4cdb1176889d1f27 127.0.0.1:5002
   slots: (0 slots) slave
   replicates b6ed6a09a14d49d663c7cad8b8ed3123ae579164
M: 47473c468e2e1eed14ad6006241de22a881ccce3 127.0.0.1:5007
   slots:[0-1364],[5461-6826],[10923-12287] (4096 slots) master
S: 782a86de935645c49b74f580e5a9a142bcdac27b 127.0.0.1:5006
   slots: (0 slots) slave
   replicates e77fe30c900cfe3bbe6891e3bb80b0b4d69150f9
M: b6ed6a09a14d49d663c7cad8b8ed3123ae579164 127.0.0.1:5004
   slots:[6827-10922] (4096 slots) master
   1 additional replica(s)

# 옮길 슬롯수 -> 위 출력화면에서 5007의 슬롯 수 입력
How many slots do you want to move (from 1 to 16384)? 4096

# 슬롯 받을 node id -> 5001번 노드
What is the receiving node ID? e77fe30c900cfe3bbe6891e3bb80b0b4d69150f9

# 슬롯 빼올 node id -> 5007번 노드
Source node #1: 47473c468e2e1eed14ad6006241de22a881ccce3
Source node #2: done

# 선택사항 확인
Do you want to proceed with the proposed reshard plan (yes/no)? yes


# 5007 노드 클러스터에서 삭제
redis-cli --cluster del-node 127.0.0.1:5007 47473c468e2e1eed14ad6006241de22a881ccce3

# 5007 노드가 빠진지 확인
redis-cli -h 127.0.0.1 -p 5001 cluster nodes | grep master 
```
<br>

### Master와 Slave 서버 직접 선택하는 방법
redis-cli --cluster create ip들 --cluster-slave를 사용하여 6개의 ip를 넣으면 앞선 3개는 master 나머지 3개는 slave가 되지만 각 slave가 어떤 master을 지정할지는 설정할 수 없습니다. 이것을 해결할 수 있는 방법은 Master 등록과 slave등록을 따로 진행하는 방법입니다.
```sh
# 먼저 앞서 사용했던 cluster을 초기화 시킵니다. 
redis-cli --cluster call 127.0.0.1:5001 flushall
redis-cli --cluster call 127.0.0.1:5001 cluster reset

# Master 노드 등록
redis-cli --cluster create 127.0.0.1:5001 127.0.0.1:5002 127.0.0.1:5003

# 등록 확인
redis-cli -h 127.0.0.1 -p 5001 cluster nodes

# Slave 등록
redis-cli --cluster add-node 127.0.0.1:5004 127.0.0.1:5001 --cluster-slave
redis-cli --cluster add-node 127.0.0.1:5005 127.0.0.1:5002 --cluster-slave
redis-cli --cluster add-node 127.0.0.1:5006 127.0.0.1:5003 --cluster-slave

# 등록 확인
redis-cli -h 127.0.0.1 -p 5001 cluster nodes
```

<br>

## Master 서버를 이용한 Slave서버 복구
Master 서버에 장애가 발생하여 Slave 서버가 새로운 Master 서버로 Failover 된 다음 장애가 발생했던 서버를 현재 시점의 Master 서버로 복구하는 방법과 절차입니다. 5001 ~ 5003 이 Master, 5004 ~ 5006 이 순서대로 Slave로 Cluster가 구성된 상태를 가정하고 진행하겠습니다. 
```sh
# cluster 상태 체크
redis-cli --cluster check 127.0.0.1:5001

## 출력 화면 ##
M: e77fe30c900cfe3bbe6891e3bb80b0b4d69150f9 127.0.0.1:5001
   slots:[0-5460] (5461 slots) master
   1 additional replica(s)
S: 1a89d2eebb71744241150a3e1be02db732d1a392 127.0.0.1:5005
   slots: (0 slots) slave
   replicates 14d76178fa2f880352394a6f4cdb1176889d1f27
M: 449a92cba41d41480e064be0cc301f395dd23c91 127.0.0.1:5003
   slots:[10923-16383] (5461 slots) master
   1 additional replica(s)
M: 14d76178fa2f880352394a6f4cdb1176889d1f27 127.0.0.1:5002
   slots:[5461-10922] (5462 slots) master
   1 additional replica(s)
S: 782a86de935645c49b74f580e5a9a142bcdac27b 127.0.0.1:5006
   slots: (0 slots) slave
   replicates 449a92cba41d41480e064be0cc301f395dd23c91
S: b6ed6a09a14d49d663c7cad8b8ed3123ae579164 127.0.0.1:5004
   slots: (0 slots) slave
   replicates e77fe30c900cfe3bbe6891e3bb80b0b4d69150f9

# redis 서버 찾기
ps -ef | grep redis

## 출력 화면 ##
ec2-user  9343     1  0 05:03 ?        00:00:20 redis-server 127.0.0.1:5001 [cluster]
ec2-user 10136     1  0 06:43 ?        00:00:11 redis-server 127.0.0.1:5002 [cluster]
ec2-user  9355     1  0 05:03 ?        00:00:19 redis-server 127.0.0.1:5003 [cluster]
ec2-user  9363     1  0 05:03 ?        00:00:18 redis-server 127.0.0.1:5004 [cluster]
ec2-user  9369     1  0 05:03 ?        00:00:17 redis-server 127.0.0.1:5005 [cluster]
ec2-user  9375     1  0 05:03 ?        00:00:17 redis-server 127.0.0.1:5006 [cluster]

# 5002 마스터 서버 강제 종료
kill -9 10136

# 상태 확인
redis-cli --cluster check 127.0.0.1:5001

## 출력 화면 ## -> Master 5002가 죽고 Slave였던 5005가 Master로 승격 
M: e77fe30c900cfe3bbe6891e3bb80b0b4d69150f9 127.0.0.1:5001
   slots:[0-5460] (5461 slots) master
   1 additional replica(s)
M: 1a89d2eebb71744241150a3e1be02db732d1a392 127.0.0.1:5005
   slots:[5461-10922] (5462 slots) master
M: 449a92cba41d41480e064be0cc301f395dd23c91 127.0.0.1:5003
   slots:[10923-16383] (5461 slots) master
   1 additional replica(s)
S: 782a86de935645c49b74f580e5a9a142bcdac27b 127.0.0.1:5006
   slots: (0 slots) slave
   replicates 449a92cba41d41480e064be0cc301f395dd23c91
S: b6ed6a09a14d49d663c7cad8b8ed3123ae579164 127.0.0.1:5004
   slots: (0 slots) slave
   replicates e77fe30c900cfe3bbe6891e3bb80b0b4d69150f9

# 현재 Master의 정보를 5002로 옮기기
cp /home/ec2-user/redis/redis-cluster/5005/nodes-5005.conf /home/ec2-user/redis/redis-cluster/5002/nodes-5002.conf
cp /home/ec2-user/redis/redis-cluster/5005/appendonly.aof /home/ec2-user/redis/redis-cluster/5002/appendonly.aof
cp /home/ec2-user/redis/redis-cluster/5005/dump.rdb /home/ec2-user/redis/redis-cluster/5002/dump.rdb

cd /home/ec2-user/redis/redis-cluster/5002
vi nodes-5002.conf
# myself,master에서 myself, 를 삭제
# master,fail을 myself,slave로 변경

# 5002 재시작
redis-server /home/ec2-user/redis/redis-cluster/5002/redis.conf

# 상태 확인
redis-cli --cluster check 127.0.0.1:5001
```
<br>

# 4. Logging & Monitoring
---
## Logging
Redis 서버 내에서 발생하는 다양한 상태 정보를 얼마나 구체적이고 자세하게 수집할 것인지를 loglevel 파라미터를 통해 결정하고 수집된 정보는 logfile 파라미터를 통해 사용자가 원하는 경로에 원하는 이름의 파일로 저장할 수 있습니다.
```sh
# 예시
vi redis_5001.conf

logfile /home/ec2-user/redis/log_5001.log
loglevel notice
syslog-enable yes
syslo-ident redis123
```
+ logfile : 로그파일 경로
+ loglevel : 로그 레벨 지정
    - debug, verbose, notice, warning : 우측으로 갈수록 로그 내용이 줄어 듭니다.
    - debug : a lot of information, userful for development/testing
    - verbose : many rarely useful info, but not a mess like the debug level
    - notice : moderately verbos, what you want in production probably
    - warning : only very important, critical messages are logged
+ syslog-enabled : 시스템 로그 정보의 수집 여부 결정
+ syslog-ident : 시스템 로그 작성에 로깅시 ID값 -> 시스템 로그 식별자

<br>

## Monitoring
Redis 서버의 상태를 모니터링하는 방법입니다.
```sh
# 예시
vi redis_5001.conf

latency-monitor-threshold 25
```
+ latency-monitor-threshold 25 : 25 millisecond 이상 소요되는 작업을 수집 분석합니다.
+ cli에서 모니터링 명령어
    - latency latest : 조건을 만족한 작업 리스트
    - latency doctor : advice report 제공
+ redis-cli -p 포트 -latency : latency 상태 모니터링
+ redis-cli -p 포트 --latency-history : latency 히스토리 상태 모니터링

<br>

# 5. 성능 튜닝
---
## 시스템 튜닝
### NUMA & Transparent Huge Page (THP) 설정 해제
Redis 서버는 메모리 영역에 대한 할당과 운영, 관리와 관련된 다양한 매커니즘을 자체적으로 제공합니다. 이와 같이 리눅스 서버의 경우에도 자체적인 매커니즘인 NUMA와 THP를 제공합니다. 하지만 리눅스 서버에서 제공하는 메모리 운영 매커니즘은 Redis 서버에서 제공하는 메모리 운영 매커니즘들이 정상적으로 작동되지 못합니다. 따라서 설정을 해제하는 것이 좋습니다.
```sh
cat /sys/kernel/mm/transparent_hugepage/enabled
always [madvise] never

# root 계정으로 해야합니다.
# THP 기능 끄기
echo never > /sys/kernel/mm/transparent_hugepage/enabled
cat /sys/kernel/mm/transparent_hugepage/enabled
always madvise [never]
```

### Client Keep Alive Time
클라이언트가 Redis 서버에 접속한 후 일정한 타임아웃 시간이 지나고 나면 해당 세션은 종료되고 다시 재접속을 요구하는 경우 즉시 접속될 수 없는 상태일 때 대기시간이 발생하는 것을 피할 수 없습니다. 이걸 해결하기 위해서는 클라이언트가 일정 시간 동안 작업을 수행하지 않더라도 세션을 일정 시간동안 유지시킴으로써 재접속을 피하고 대기시간을 줄일 수 있습니다.
```sh
vi /etc/sysctl.conf

# 7200초가 default
net.ipv4.tcp_keepalive_time = 7200
```

### 최적화 시스템 환결 설정
인메모리 기반 Redis는 시스템 환결 설정이 매우 중요한데 아래는 시스템 설정에 관해 기타 내용들입니다.
__overcommit__  
```sh
vi /etc/sysctl.conf

vm.overcommit_memory=1
```
메모리 오버커밋(memory overcommit)은 물리 메모리 공간 이상을 쓸 수 있는 방법으로, 가상 메모리(virtual memory)를 함께 사용하여 더 많은 메모리를 할당할 수 있는 기법을 의미합니다. 파일에 존재하는 디스크의 공간을 스왑 메모리로 설정하면, 사용할 수 있는 메모리 공간이 커집니다. 
<Br>

__네트워크 설정__  
```sh
sudo sysctl -w net.core.somaxconn=65535

cat /proc/sys/net/core/somaxconn
65535

# reboot 후에도 적용되도록 sysctl.conf 파일도 수정
vi /etc/sysctl.conf

net.core.somaxconn = 65535
```
많은 연결을 처리하기위한 네트워크 설정입니다. client 의 연결 요청은 accept() 를 호출하기 전에 queue 에 쌓이게 됩니다. queue 가 작으면 client 의 연결 요청이 drop 될 수 있으므로 다음 항목을 크게 설정해 주어야 합니다.
<br>


__Max Number Of Open File__  
```sh
vi /etc/security/limits.conf
redis soft nofile 65536
redis hard nofile 65536
```
Max number of open files 설정입니다. 클라이언트 개수가 많아지거나 레디스 서버를 여러 개 띄우다 보면 maximum open files에러메시지가 뜨게 됩니다. 최대 오픈 파일 개수가 부족하다는 에러인데 maxclient는 OS(리눅스)의 제한으로 설정할 수가 없습니다. 리눅스에서 파일의 의미는 실제 파일은 물론이고 네트워크/소켓 접속도 파일로 취급합니다. 이를 해결하기 위해서는 리눅스의 max open files(nofile)을 변경하는 것입니다. __레디스를 실행하는 User가 redis면 위와 같이 앞에 redis를 입력하고 root라면 root를, *를 입력하면 모든 User에게 적용됩니다.__

<br>

__Max Number Of Proccesses__  
```sh
vi /etc/security/limits.conf
redis soft nproc 131072
redis hard nproc 131072
```
Max number of processes 설정입니다. 레디스 서버는 여러 개의 프로세스를 사용하지 않으므로 특별히 신경쓰지 않아도 되나 나중에 발생할 수 있는 문제를 대비하기 위해 설정합니다. 레디스 서버는 평상시에는 1개 프로세스로 운영되고 자식 프로세스가 필요할 때는 부모 프로세스 포함해서 최대 2개의 프로세스가 동시에 뜹니다. 그런데 여기서 process는 쓰레드(thread)까지 포함해서 계산합니다. 참고로 레디스 서버는 메인 쓰레드를 포함해서 4개 쓰레드로 운영됩니다.


## 서버 튜닝
### 스와핑 대응
과도한 스와핑이 발생하는 경우 이를 최소화하기 위한 대응 방안입니다.
```sh
vi redis.conf
maxmemory 1000000000 # redis의 인스턴스 크기 늘리기
maxmemory-policy volatile-lru
```
+ redis 서버를 설치하면 기본값은 시스템 RAM 메모리가 허용하는 최대 범위까지 사용할 수 있도록 설정됩니다. 하지만 기본 값은 해당 시스템에 하나의 Redis 서버가 독립적으로 설치 운영되고 있는 환경에는 문제가 없지만 다른 것들과 함께 운영된다면 메모리 효율성이 떨어지게 되며 메모리가 부족하게 되면 성능지연 문제를 유발합니다.
+ 권장하는 값은 가용 메모리의 60 ~ 70% 선에서 Max memory를 설정하는 것입니다. 
+ maxmemory-policy : 메모리가 꽉 찼을 때 데이터들을 어떻게 처리해야하는지에 대한 정책입니다.
   - 기본값은 noeviction으로 메모리가 가득 차면 더이상 새로운 입력을 받지 않는 것입니다.
   - volatile-lru 는 가장 최근에 사용하지 않은 값을 삭제하는 정책입니다. 캐시용도로 사용할 경우 필요없는 값을 삭제하고 새로운 입력을 받아들이는 것이 좋으므로 이 정책을 사용합니다.

### AOF 파일에서 발생하는 디스크 IO 문제 대응
데이터 저장 구조 환경에서 사용자 데이터의 일부를 저장하기 위해 AOF 기능을 사용하게 되는데 빅데이터 환경에서 과도한 쓰기 작업이 발생할 때 성능 지연 문제가 발생합니다. 이를 피하기 위해 파라미터 설정을 해야합니다.
```sh
vi redis.conf

appendonly yes
appendfilename "appendonly.aof"
auto-aof-rewrite-percentage 100
auto-aof-rewrite-min-size 64mb
appendfsync everysec
no-appendfsync-on-rewrite no
```
appendfsync 파라미터는 Redis 인스턴스 상에 데이터를 AOF 파일로 내려쓰기 하는 시점을 언제 수행할지 결정하는 파라미터입니다. everysec는 1초마다 쓰기 작업을 수행하게 되며 always를 선택하면 실시간으로 지속적인 쓰기작업을 수행합니다. 데이터 유실이 없어야 하며 중요도가 높은 비즈니스 영역에서 사용한다면 everysec로 설정하는 것을 권장합니다.