# Spring - Pinpoint 적용하기



# 1. Pinpoint란?
---
자바 진영에서 유명한 분산 환경 APM (Application Performance Monitoring) 도구를 고르라고 하면 네이버에서 만든 Pinpoint를 쉽게 떠올리곤 합니다.  
Pinpoint는 대규모 분산 시스템의 성능을 분석하고 문제를 진단, 처리하는 플랫폼입니다.  
특히나 요즘 같이 분산환경에서 애플리케이션 모니터링에 최적화된 기능들이 많아 백엔드 구성하는데 있어 큰 도움을 받을 수 있습니다.
<Br>

# 2. 구성요소
---
![그림1](https://github.com/backtony/blog-code/blob/master/spring/img/test/4/4-1.PNG?raw=true)  

Pinpoint에는 여러 모듈이 있지만, 이 중에서 가장 중요한 모듈은 크게 3가지 입니다.  
+ Pinpoint Agent
  - 애플리케이션의 모니터링 정보를 Collector로 전달
+ Pinpoint Collector
  - Agent에서 받은 정보를 HBase에 적재
+ Pinpoint Web
  - 적재된 데이터를 Web으로 노출하여 모니터링 제공

Pinpoint는 코드 수준의 정보를 추적합니다.  
그러다보니 트래픽이 많으면 많을수록 데이터의 양이 폭발적으로 증가한다는 단점이 있습니다.  
그래서 __Pinpont는 이 정보들을 Hbase에 담아서 활용합니다.__  
각 모듈들은 별도의 서버에 구축해야하지만, 예시로 간단하게 아래와 같이 구성해보겠습니다.  

![그림2](https://github.com/backtony/blog-code/blob/master/spring/img/test/4/4-2.PNG?raw=true)   

하나의 EC2에 Hbase, Collector, Web을 모두 설치하고, 다른 EC2에 Pinpoint Agent와 도커로 spring을 띄우는 구성입니다.  
spring을 도커로 띄운것에는 큰 이유는 없습니다. 현재 진행하는 프로젝트에서 도커로 스프링을 띄우고 있어서 이렇게 구성했습니다.  
<br>


# 3. Pinpoint EC2 구성하기
---
EC2는 Amazon Linux 2로 2개 생성해서 진행하겠습니다. 생성과정은 생략하겠습니다.  
EC2 보안 그룹 인바운드에는 9991 ~ 9993까지는 필수로 열려있어야 합니다.  
Agent가 Collector로 로그 전송을 할 때 사용하는 TCP 포트가 9991 ~ 9993까지 기본값으로 되어있기 때문입니다.  
<br>

## 자바 8 설치
기본적으로 자바가 1.8 이상 설치되어있어야 합니다.
```sh
sudo yum install java-1.8.0-openjdk -y
sudo yum install java-1.8.0-openjdk-devel -y

환경변수 설정
readlink -f /usr/bin/java

sudo vi /etc/profile

# 맨 아래에 추가
export JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.302.b08-0.amzn2.0.1.x86_64
export PATH=$PATH:$JAVA_HOME/bin
export CLASSPATH=$JAVA_HOME/jre/lib:$JAVA_HOME/lib/tools.jar

적용
source /etc/profile

확인
echo $JAVA_HOME 
```

<br>

## Hbase 설치
Hbase는 2.X까지 나와있지만, 아직까지 Pinpoint의 모든 버전은 Hbase의 1.2.X 버전에 최적화되어 있습니다.  

```sh
wget https://archive.apache.org/dist/hbase/1.2.7/hbase-1.2.7-bin.tar.gz

tar xzvf hbase-1.2.7-bin.tar.gz

vi /home/ec2-user/hbase-1.2.7/conf/hbase-env.sh

# 이 옵션을 주석처리 하지 않으면 hbase 실행시 warning 이 뜹니다.
# 두 옵션 다 주석처리하여 지워줍니다.
# export HBASE_MASTER_OPTS="$HBASE_MASTER_OPTS -XX:PermSize=128m -XX:MaxPermSize=128m"
# export HBASE_REGIONSERVER_OPTS="$HBASE_REGIONSERVER_OPTS -XX:PermSize=128m -XX:MaxPermSize=128m"

# 매번 hbase-1.2.7 를 입력하기 번거로우니 해당 디렉토리에 link를 걸어서 hbase로 사용하겠습니다.
ln -s hbase-1.2.7 hbase

# hbase 시작
hbase/bin/start-hbase.sh
```
<br>

### Pinpoint 정보를 담을 테이블 생성하기
위에서 Hbase의 설치와 실행은 끝냈습니다.  
이제 Pinpoint 정보를 담을 테이블을 만들어야하는데 이와 관련해서는 이미 스크립트를 지원하기 때문에 다운받아서 적용시켜주면 됩니다.
```sh
# 스크립트 다운
wget https://raw.githubusercontent.com/pinpoint-apm/pinpoint/master/hbase/scripts/hbase-create.hbase

# 스크립트 실행
hbase/bin/hbase shell ../hbase-create.hbase
```
<br>

## Collector 설치
```sh
# jar 파일 다운
wget https://github.com/pinpoint-apm/pinpoint/releases/download/v2.2.2/pinpoint-collector-boot-2.2.2.jar

# 실행권한 부여
chmod +x pinpoint-collector-boot-2.2.2.jar

# 실행
nohup java -jar -Dpinpoint.zookeeper.address=localhost pinpoint-collector-boot-2.2.2.jar >/dev/null 2>&1 &
```
+ nohup ~ >/dev/null 2>&1 &
  - nohup.out 로그 파일 생성 없이, 세션 종료 없이 백그라운드 실행을 위해 사용합니다.
+ -Dpinpoint.zookeeper.address=localhost
  - 원래는 분산된 Hbase의 zookeeper 주소를 써야하지만, 여기서는 다 같은 EC2 안에 있으니 localhost를 사용합니다.

## Web 설치
```sh
# jar 파일 다운
wget https://github.com/pinpoint-apm/pinpoint/releases/download/v2.2.2/pinpoint-web-boot-2.2.2.jar

# 실행 권한 부여
chmod +x pinpoint-web-boot-2.2.2.jar

# 실행
nohup java -jar -Dpinpoint.zookeeper.address=localhost pinpoint-web-boot-2.2.2.jar >/dev/null 2>&1 &
```
<Br>

# 4. Pinpoint Agent EC2 구성하기
---
이제 spring boot가 실행될 EC2로 넘어와서 진행합니다.  
<br>

## Pinpoint Agent 설치
```sh
# agent 설치
wget https://github.com/pinpoint-apm/pinpoint/releases/download/v2.2.2/pinpoint-agent-2.2.2.tar.gz

# 압축 해제
tar xvzf pinpoint-agent-2.2.2.tar.gz

# 이동
cd pinpoint-agent-2.2.2

# config 파일 수정
sudo vi pinpoint-root.config

# 127.0.0.1 로 되어있을 텐데, 앞서 구성한 Pinpoint EC2의 ip로 수정해줍니다.
profiler.transport.grpc.collector.ip=pinpoint ip로 변경
```
<br>

## Docker 설치
```sh
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

# 5. Spring boot의 Dockerfile 작성
---
이제는 로컬 환경에서 개발중인 스프링 부트의 도커파일을 작성하겠습니다.  

```dockerfile
FROM openjdk:11-jdk

VOLUME /tmp

ARG JAR_FILE=./build/libs/test-1.0.jar

COPY ${JAR_FILE} app.jar

# 운영
ENTRYPOINT ["nohup","java","-jar",\
"-javaagent:./pinpoint/pinpoint-bootstrap-2.2.2.jar",\
"-Dpinpoint.agentId=gjgs01","-Dpinpoint.applicationName=gjgs",\
"-Dpinpoint.config=./pinpoint/pinpoint-root.config"\
,"-Dspring.profiles.active=prod","app.jar","2>&1","&"]
```
스프링 부트를 띄울 때 Agent도 같이 띄워주도록 하는 세팅입니다.
+ applicationName
  - Pinpoint에 연결된 많은 프로젝트들 중, 이 프로젝트를 구분짓기 위한 ID 입니다.
  - 스케일아웃하여 여러 EC2로 구성되어있더라도 같은 프로젝트라면 이 값을 모두 같은 값으로 합니다.
+ agentId
  - 같은 PINPOINT_APPLICATION_NAME 내에서 각 서버들을 구분하기 위한 ID 입니다.
+ config
  - 앞서 수정한 config를 명시해서 적용되도록 하는 것입니다.

<br>

이렇게 만들어준 도커파일을 빌드해서 도커허브에 푸시한 뒤에, Agent EC2에서 풀 받아서 실행하겠습니다.

## 로컬에서 진행
```sh
# 스프링 빌드
./gradlew clean build

# 도커 파일 빌드
# 이 부분 태그명은 각자에 맞게 작성하면 됩니다.
docker build -t soma1218/gjgs:5.0 .

# 자신의 도커 허브로 푸시
# 이 부분도 각자에 맞게 작성하시면 됩니다.
docker push soma1218/gjgs:5.0 
```
<br>

## Agent EC2에서 진행
```sh
# 이미지 풀
docker pull soma1218/gjgs:5.0

# 실행
docker run -d -p 80:8080 -e TZ=Asia/Seoul -v /home/ec2-user/pinpoint-agent-2.2.2:/pinpoint --name gjgs soma1218/gjgs:5.0
```
Dockerfile에 작성한 대로 볼륨마운팅을 해주었고, 80번 포트로 띄워주었습니다.  
띄워진 스프링 부트에 여러가지 요청을 보냅니다.  
<br><Br>

![그림3](https://github.com/backtony/blog-code/blob/master/spring/img/test/4/4-3.PNG?raw=true)  
Pinpoint EC2:8080 으로 접속해보시면 이렇게 잘 세팅된 것을 확인할 수 있습니다.



<Br><Br>

__참고__  
<a href="https://jojoldu.tistory.com/573" target="_blank"> Pinpoint APM Node 버전 설치하기</a>   
</a>   

