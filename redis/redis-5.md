# Spring - Redis Cluster 구축 및 연동하기 

# 1. 구성도
![그림1](https://github.com/backtony/blog-code/blob/master/redis/img/5/2-1.PNG?raw=true)  

3개의 서버에 각각 한개의 Master node를 놓고 각 서버마다 2개의 Slave를 배치한 뒤 Master의 Slave는 Master와 같은 서버가 아닌 다른 서버에 배치하는 구성입니다. 연습용으로 구축한 것이기 때문에 3개의 서버로 진행했지만 실제 업무에서 사용할 때는 각각 하나의 서버를 할당하여 총 9개의 서버를 만들고 진행해야 한다고 합니다.

<br>

# 2. Redis 설치 및 환경설정
---
## Redis 설치
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

mkdir redis-cluster
```

## EC2 환경 튜닝
Redis에 적합하도록 EC2 설정을 하겠습니다. 이에 관한 자세한 설명은 [여기](https://velog.io/@backtony/Redis-Cluster-%EA%B5%AC%EC%B6%95-%EB%B0%8F-%EC%84%B1%EB%8A%A5-%ED%8A%9C%EB%8B%9D#%EC%8B%9C%EC%8A%A4%ED%85%9C-%ED%8A%9C%EB%8B%9D)를 참조하시면 됩니다.
```sh
# 루트 계정 패스워드 설정 -> 원하는 패스워드 입력하여 설정
sudo passwd root

# root 계정으로 전환 -> 앞서 설정한 패스워드 입력
su root

# THP 기능 끄기
echo never > /sys/kernel/mm/transparent_hugepage/enabled
cat /sys/kernel/mm/transparent_hugepage/enabled
always madvise [never]

# ec2-user 계정으로 돌아오기
exit

sudo vi /etc/sysctl.conf

net.ipv4.tcp_keepalive_time = 7200
vm.overcommit_memory=1
net.core.somaxconn = 65535

sudo vi /etc/security/limits.conf

#<domain>      <type>  <item>         <value>
ec2-user        soft    nofile          65536
ec2-user        hard    nofile          65536
ec2-user        soft    nproc           131072
ec2-user        hard    nproc           131072

# 재부팅
sudo reboot
```

## 포트 열기
모든 Redis 클러스터 노드에는 두 개의 TCP 연결이 열려 있어야 합니다. 클라이언트에 서비스를 제공하는 데 사용되는 일반 Redis TCP 포트(예: 6379)와 데이터 포트에 10000을 추가하여 얻은 포트(예: 16379) 이렇게 두개의 포트가 열려있어야 합니다. 따라서 AWS EC2 보안 그룹에서 해당 포트들을 열어줘야합니다.

<Br>

# 3. Redis Cluster 구성
---

## 3개의 EC2의 Redis Cluster config 
```sh
cd /home/ec2-user/redis/redis-cluster
mkdir 5001 5002 5003

cp ../redis.conf ./5001/.

cd 5001
vi redis.conf

#### config 설정 시작 ####
# port 번호
port 5001

# cluster 모드 사용
cluster-enable yes 
# cluster의 node 구성이 기록되는 파일 -> 자동으로 관리
cluster-config-file /home/ec2-user/redis/redis-cluster/5001/nodes-5001.conf
# cluster 구성원인 node가 다운된 상태인지 여부를 판단하기 위한 시간 -> 지정된 시간 내 응답하지 않으면 다운된 것으로 판단
cluster-node-timeout 4000 
# cluster의 일부 node가 다운되어도 운영할 방법 설정 -> yes : slave가 없는 master 다운시 cluster 전체 중지
cluster-require-full-coverage yes
# 마스터에 연결되는 slave 최소 수
cluster-migration-barrier 1
# master의 slave 중 승격대상을 제외시킬 때 사용하는 계수
# 계산식 : (node-timeout * cluster-replica-validity-factor) + repl-ping-replica-period
# 계산식에 따른 시간만큼 응답이 없으면 해당 slave는 승격대상에서 제외
cluster-replica-validity-factor 10

# 기본 디렉토리 설정
dir /home/ec2-user/redis/redis-cluster/5001
# AOF 설정으로 캐시용도로만 사용한다면 no 다른 용도도 있다면 yes
appendonly no
# 백그라운드로 실행
daemonize yes
# AOF 파일에서 발생하는 디스크 IO 대응 옵션
auto-aof-rewrite-percentage 100
auto-aof-rewrite-min-size 64mb
appendfsync everysec
no-appendfsync-on-rewrite no

# 키 삭제작업 성능지연 문제 해결
lazyfree-lazy-eviction      yes
lazyfree-lazy-expire        yes
lazyfree-lazy-server-del    yes
replica-lazy-flush          yes
lazyfree-lazy-user-del      yes # 6.0추가 옵션

# 보안설정
protected-mode yes
# bind에 작성하는 IP는 서버의 network interface IP입니다.
# 즉, 서버에서 리눅스 ifconfig 명령으로 나오는 IP 중 실제로 통신에 사용되는 IP를 말합니다. 클라이언트 IP를 의미하는 것이 아닙니다.
# ifconfig 입력하여 inet의 ip를 입력하면 됩니다.
# redis 5버전에서는 버그로 인해 127 앞쪽에 ip를 작성해야 합니다.
bind 10.0.5.234 127.0.0.1 
# 보안설정 암호
masterauth Team1218! 
# 서버의 암호
requirepass Team1218! 

# 메모리 설정
maxmemory 600000000 #권장하는 값은 가용 메모리의 60 ~ 70% -> 0.6gb
maxmemory-policy volatile-lru # 가장 최근에 사용하지 않은 값을 삭제하는 정책

# 부분 동기화 작업
repl-backlog-size 10mb

#### config 설정 완료 ####

# 5002와 5003의 config 파일은 방금 만든 5001의 config 파일을 복사하여 5001을 5002, 5003으로 바꿔주면 됩니다.
cp /home/ec2-user/redis/redis-cluster/5001/redis.conf /home/ec2-user/redis/redis-cluster/5002/redis.conf
cp /home/ec2-user/redis/redis-cluster/5001/redis.conf /home/ec2-user/redis/redis-cluster/5003/redis.conf

# 실행
redis-server /home/ec2-user/redis/redis-cluster/5001/redis.conf
redis-server /home/ec2-user/redis/redis-cluster/5002/redis.conf
redis-server /home/ec2-user/redis/redis-cluster/5003/redis.conf
```
나머지 2개의 서버도 똑같이 해주면 됩니다. 다른 점은 bind가 서버마다 다를 것입니다.
<br>

## Cluster 구성하기
```sh
# Master 구성
# EC2 보안 그룹에서 5001,5002,5003,15001,15002,15003 포트를 열어야 합니다.
# -a 뒤에는 패스워드 입니다.
redis-cli --cluster create 10.0.5.234:5001 10.0.6.23:5001 10.0.5.7:5001 -a Team1218! 

# Slave 구성
redis-cli --cluster add-node 10.0.6.23:5002 10.0.5.234:5001 --cluster-slave -a Team1218! 
redis-cli --cluster add-node 10.0.5.7:5003 10.0.5.234:5001 --cluster-slave -a Team1218! 

redis-cli --cluster add-node 10.0.5.234:5003 10.0.6.23:5001 --cluster-slave -a Team1218! 
redis-cli --cluster add-node 10.0.5.7:5002 10.0.6.23:5001 --cluster-slave -a Team1218! 

redis-cli --cluster add-node 10.0.5.234:5002 10.0.5.7:5001 --cluster-slave -a Team1218! 
redis-cli --cluster add-node 10.0.6.23:5003 10.0.5.7:5001 --cluster-slave -a Team1218! 

# cli 접속
redis-cli -c -h localhost -p 5001
# 로그인
auth Team1218! 

# 확인
cluster info
cluster nodes

## 참고 사항 ##
# 클러스터 초기화 ########################### 패스워드 필요?
redis-cli --cluster call 127.0.0.1:5001 flushall -a Team1218! # 데이터 초기화
redis-cli --cluster call 127.0.0.1:5001 cluster reset -a Team1218! # 클러스터 리셋
```

<br>

# 4. Spring 연동하기
---
spring boot 2.0 버전 이상부터는 auto-configuration으로 위의 빈(redisConnectionFactory, RedisTemplate, StringTemplate)들이 자동으로 생성되기 때문에 Configuration을 만들지 않아도 즉시 사용가능합니다. 따라서 yml만 다음과 같이 설정해주면 됩니다.
```yml
spring:
  redis:
    pool:
      min-idle: 0
      max-idle: 8
      max-active: 8
    cluster:
      nodes:
        - 10.0.5.234:5001
        - 10.0.6.23:5001
        - 10.0.5.7:5001
    password: Team1218!
```
Redis를 이용한 캐싱에 관해서는 [여기](https://velog.io/@backtony/Spring-Redis-%EC%97%B0%EB%8F%99%ED%95%98%EA%B8%B0)를 참고하면 될 것 같습니다.

<br>

# 5. Redis-stat
---
__Redis-Stat 는 대표적인 오픈소스 Redis 모니터링 도구입니다.__  
구글링을 통해 여러 가지 방법을 시도했지만, 버전 관련 문제가 계속 발생하여 도커로 설치하여 해결했습니다.  
본 포스팅에서도 도커로 띄우는 방법을 설명하고, Redis-Stat는 __새로운 EC2__ 에 설치하겠습니다.  
Redis-stat는 웹으로 확인할 수 있는데 이를 위해서 보안그룹에서 기본 포트인 63790을 열어줍니다.  


## 도커 설치
```
패키지 업데이트
sudo yum -y upgrade

도커 설치
sudo yum -y install docker

도커 설치 작업이 잘 되었는지 버전 확인
docker -v

도커 시작
sudo service docker start

도커 그룹에 사용자 추가 -> docker가 그룹명, ec2-user가 사용자명
sudo usermod -aG docker ec2-user 

// 참고 // 
docker ps 시 Permission Denied가 발생할 경우 아래 명령어 입력할 것
sudo chmod 666 /var/run/docker.sock
```
<br>

## Redis-Stat 설치
```
docker run --name redis-stat -p 63790:63790 -d insready/redis-stat --server 10.0.5.234:5001 10.0.6.23:5001 10.0.5.7:5001 -a 비밀번호
```
자세한 옵션은 아래서 설명하겠습니다. server 뒤에는 클러스터한 Redis Master들의 IP:포트 입니다.  

### 옵션
![그림2](https://github.com/backtony/blog-code/blob/master/redis/img/5/2-2.PNG?raw=true)  

+ -a : redis 연결시 requirepass password가 필요할때 쓰는 옵션
+ -v : 세부적인 정보를 출력
+ --csv : csv 파일로 저장
+ --es : 엘라스틱서치로 보내는 옵션
+ --server : 콘솔이 아닌 웹으로 모니터링하는 옵션으로 포트를 지정하지 않으면 기본 63790 포트를 사용
+ --daemon : 백그라운드로 프로세스 실행

### 모니터링
![그림3](https://github.com/backtony/blog-code/blob/master/redis/img/5/2-3.PNG?raw=true)  
redis-stat EC2의 IP:63790 포트로 접속해보면 위와 같이 모니터링을 할 수 있습니다.
<br>

변수명|풀 네임|설명
---|---|---
us|used_cpu_user|사용자 공간 CPU 퍼센트
sy|used_cpu_sys|커널 공간 CPU 퍼센트
cl|connected_clients|연결 클라이언트 수
bcl|blocked_clients|클라이언트 차단 수
mem|used_memory|총 메모리 사용량
res|used_memory_ress|물리적 메모리 사용량
keys|dbc.keys|key의 총 수량
cmd/s|command/s|초당 명령 실행 수
exp/s|expired_keys/s|초당 만료 key 개수
evt/s|evicted_keys/s|초당 제거 key 개수
hit%/s|keyspace_hitratio/s|초당 히트 백분율  
hit/s|keyspace_hits/s|초당 히트 수
mis/s|keyspace_miss/s|초당 미스 수
aofcs|aof_current_size|현재 AOF 로그 크기




<Br><Br>

__참고__  
<a href="https://ozofweird.tistory.com/entry/Redis-Redis-%EC%84%A4%EC%A0%95-%ED%8C%8C%EC%9D%BC" target="_blank"> redis.conf 설정 파일</a>   
<a href="https://medium.com/garimoo/%EA%B0%9C%EB%B0%9C%EC%9E%90%EB%A5%BC-%EC%9C%84%ED%95%9C-%EB%A0%88%EB%94%94%EC%8A%A4-%ED%8A%9C%ED%86%A0%EB%A6%AC%EC%96%BC-04-17256c55493d" target="_blank"> 개발자를 위한 레디스 튜토리얼 04</a>   
<a href="http://redisgate.kr/redis/configuration/param_lazyfree.php" target="_blank"> Redis LAZYFREE parameter</a>   
<a href="http://redisgate.kr/redis/server/bind.php" target="_blank"> Redis BIND</a>   
<a href="https://redis.io/topics/cluster-tutorial/" target="_blank"> Scaling with Redis Cluster</a>   
<a href="http://redisgate.kr/redis/server/protected-mode.php" target="_blank"> Redis PROTECTED-MODE</a>   
<a href="https://github.com/INsReady/docker-redis-stat" target="_blank"> docker redis stat github</a>   
<a href="https://github.com/junegunn/redis-stat" target="_blank"> docker redis stat github</a>   



