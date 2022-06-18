# 카프카 빠르게 시작해보기


## EC2 세팅
### 포트 설정
실습을 위한 카프카를 활용하기 위해 실행되는 프로세스는 주키퍼와 카프카 브로커입니다.  
주키퍼는 아파치 소프트웨어 재단 프로젝트 중 하나로서 분산 코디네이션 서비스를 제공하는 오픈소스입니다.  
분산 코디네이션 서비스란 분산 시스템 내부에 상태 정보를 저장하고 데이터를 key-value 저장소로 저장 및 제공하는 서비스를 말합니다.  
카프카에서는 주키퍼를 운영에 필요한 각종 설정과 상태들을 저장하는 데에 사용하고 있습니다.  
주키퍼와 카프카 브로커는 JVM위에서 돌아가는 애플리케이션으로서 힙 메모리를 지정해야 합니다.  
두 프로세스에 각각 400MB의 힙 메모리를 설정하려면 1GB이상의 램이 필요하므로 1GB 이상의 메모리를 가진 EC2를 만들어야 합니다.  
EC2를 만들고 접속하는 과정은 간단하니 생략하고 바로 보안설정으로 넘어가겠습니다.  
카프카 브로커의 기본 포트는 9092이고 주키퍼의 기본 포트는 2181이므로 EC2에 설치된 브로커에 접속하기 위해서는 EC2 보안그룹에서 해당 포트들을 열어줘야 합니다.  

### 자바 설치
카프카 브로커를 실행하기 위해서는 JDK가 필요합니다.  
```sh
# 설치
sudo yum install -y java-1.8.0-openjdk-devel.x86_64

# 확인
java -version
```

### 주키퍼, 카프카 브로커 실행
카프카 브로커를 실행하기 위해서 카프카 바이너리 패키지를 다운로드합니다.  
카프카 바이너리 패키지에는 자바 소스코드를 컴파일하여 실행하기 위해 준비해 놓은 바이너리 파일들이 들어 있습니다.  
```sh
# 다운
wget https://archive.apache.org/dist/kafka/2.5.0/kafka_2.12-2.5.0.tgz

# 압축 해제
tar -xvzf kafka_2.12-2.5.0.tgz

# 이동
cd kafka_2.12-2.5.0/
```

### 카프카 브로커 힙 메모리 설정
카프카 브로커를 실행하기 위해서는 힙 메모리 설정이 필요합니다.  
카프카 브로커는 레코드의 내용은 페이지 캐시로 시스템 메모리를 사용하고 나머지 객체들을 힙 메모리에 저장하여 사용한다는 특징이 있습니다.  
이러한 특징으로 카프카 브로커를 운영할 때 힙 메모리를 5GB 이상으로 설정하지 않는 것이 일반적입니다.  
카프카 패키지의 힙 메모리는 카프카 브로커는 1G, 주키퍼는 512MB로 기본 설정되어 있습니다.  
실습용으로 띄운 프리티어 EC2는 1G의 메모리를 갖기 때문에 동시에 실행하면 메모리가 부족하게 되므로 사전에 환경변수로 힙 메모리 사이즈를 변경해주고 실행해야 합니다.  
```sh
# Xms는 힙 최소 크기, Xmx는 힙 최대 크기
export KAFKA_HEAP_OPTS="-Xmx400m -Xms400m"

# 확인
echo $KAFKA_HEAP_OPTS
```
터미널에서 설정한 환경변수는 터미널이 종료되면 다시 초기화되기 때문에 ~/.bashrc파일에 설정해주도록 합니다.  
해당 파일은 bash쉘이 실행될 때마다 반복적으로 구동되어 적용되는 파일입니다.
```sh
# 편집기 열기
vi ~/.bashrc

# 맨 아래 입력하고 저장
export KAFKA_HEAP_OPTS="-Xmx400m -Xms400m"

# source 명령어는 스크립트 파일을 수정한 후에 수정된 값을 바로 적용하기 위해 사용합니다.
source ~/.bashrc

# 확인
echo $KAFKA_HEAP_OPTS
```


### 카프카 브로커 실행 옵션 설정
config 폴더에 있는 server.properties 파일에는 카프카 브로커가 클러스터 운영에 필요한 옵션들을 지정할 수 있습니다.  
실습용이므로 advertised.listener만 설정하면 됩니다.  
advertised.listener는 카프카 클라이언트 또는 커맨드 라인 툴을 브로커와 연결할 때 사용합니다.  

```sh
# 편집기 열기
vi config/server.properties

# 해당 옵션 자신의 EC2 ip로 변경해주고 주석 해제해주기
advertised.listeners=PLAINTEXT://3.36.131.80:9092
```

__cf) properties 옵션 살펴보기__  
server.properties에는 많은 옵션들이 있기에 몇 가지 살펴봅시다.  

+ broker.id 
    - 실행하는 카프카 브로커의 번호를 명시합니다.
    - 클러스터를 구축할 때 브로커들을 구분하기 위해 단 하나뿐인 번호로 설정해야 합니다.
    - 다른 카프카 브로커와 동일한 id를 가질 경우 비정상적인 동작이 발생합니다.
+ listeners=PLAINTEXT://:9092
    - 카프카 브로커가 통신을 위해 열어둘 인터페이스 IP, port, 프로토콜을 설정할 수 있습니다.
    - 따로 설정하지 않으면 모든 ip, port에서 접속할 수 있습니다.
+ advertised.listeners=yourhostip:9092
    - 카프카 클라이언트 또는 카프카 커맨드 라인 툴에서 접속할 때 사용하는 IP와 port 정보를 명시합니다.
    - 인스턴스를 생성할 때 발급받은 퍼블릭 IPv4값을 IP에 넣고 port에는 카프카 기본 포트인 9092를 넣습니다.    
+ listener.security.protocol.map=PLAINTEXT:PLAINTEXT,SSL:SSL,SASL_PLAINTEXT:SASL_PLAINTEXT,SASL_SSL:SASL_SSL
    - 보안 설정 시 프로토콜 매핑을 위한 설정입니다.
+ num.network.threads=3
    - 네트워크를 통한 처리를 할 때 사용할 네트워크 스레드 개수 설정입니다.
+ num.io.threads=8
    - 카프카 브로커 내부에서 사용할 스레드 개수입니다.
+ log.dirs=/tmp/kafka-logs
    - 통신을 통해 가져온 데이터를 파일로 저장할 디렉토리 위치입니다.
    - 디렉토리가 생성되어 있지 않으면 오류가 발생할 수 있으므로 브로커 실행 전에 디렉토리 생성 여부를 확인해야 합니다.
+ num.partitions=1
    - 파티션 개수를 명시하지 않고 토픽을 생성할 때 기본 설정되는 파티션 개수입니다.
    - 파티션 개수가 많아지면 병렬처리 데이터양이 늘어납니다.
+ log.retention.hours=168
    - 카프카 브로커가 저장한 파일이 삭제되기까지 걸리는 시간 설정입니다.
    - 가장 작은 단위를 기준으로 하므로 log.retention.hours보다는 log.retention.ms값을 설정하여 운영하는 것을 권장합니다.
    - log.retention.ms값을 -1로 설정하면 파일은 영원히 삭제되지 않습니다.
+ log.segment.bytes=1073741824
    - 카프카 브로커가 저장할 파일의 최대 크기를 지정합니다.
    - 데이터양이 많아 크기를 채우게 되면 새로운 파일이 생성됩니다.
+ log.retention.check.interval.ms=300000
    - 카프카 브로커가 지정한 파일을 삭제하기 위해 체크하는 간격을 지정합니다.
+ zookeeper.connect=localhost:2181
    - 카프카 브로커와 연동할 주키퍼의 ip와 port를 설정합니다.
+ zookeeper.connection.timeout.ms=18000
    - 주키퍼의 세션 타임아웃 시간을 설정합니다.

### 주키퍼 실행
카프카 바이너리가 포함된 폴더에는 브로커와 같이 실행할 주키퍼가 이미 준비되어 있습니다.  
앞서 설명했듯이 주키퍼는 카프카의 클러스터 설정 리더 정보, 컨트롤러 정보를 담고 있어 카프카를 실행하는 데에 필요한 필수 애플리케이션입니다.  
주키퍼 설정 경로와 함께 주키퍼 시작 스크립트를 실행하면 주키퍼를 실행할 수 있습니다.  
```sh
# 데몬으로 실행
bin/zookeeper-server-start.sh -daemon config/zookeeper.properties

# 실행 확인
jps -vm
```
데몬 옵션을 사용할 경우 실행 중인 터미널의 세션이 끊기더라도 계속 동작하고 백그라운드에서 실행하기 때문에 현재 창에서 다른 작업을 수행할 수 있습니다.  
jps는 JVM 프로세스 상태를 보는 도구로서 JVM 위에서 동작하는 주키퍼의 프로세스를 확인할 수 있습니다.  
m 옵션은 main 메서드에 전달된 인자를 확인할 수 있고, v옵션은 jvm에 전달된 인자(힙 메모리 설정, log4j설정 등)을 확인할 수 있습니다.  

### 카프카 브로커 실행 및 로그 확인
주키퍼를 실행시켰으니 이제 카프카 브로커를 실행할 차례입니다.  
```sh
# 데몬으로 실행
bin/kafka-server-start.sh -daemon config/server.properties

# 확인
jps -m

# 로그 확인
tail -f logs/server.log
```
카프카 클라이언트를 개발할 때뿐만 아니라 카프카 클러스터를 운영할 때 이슈가 발생할 경우 모두 카프카 브로커에 로그가 남기 때문에 로그를 확인하는 것은 매우 중요합니다.  
정상적으로 연결되지 않거나 데이터가 전송되지 않는다면 카프카 브로커가 실행되고 있는 서버로 접속하여 로그를 확인하면 빠르게 문제를 해결할 수 있습니다.  

### 로컬 컴퓨터에서 카프카와 통신
EC2 인스턴스에 테스트용 카프카 브로커를 실행했으니 로컬 컴퓨터에서 원격으로 카프카 브로커로 명령을 내려 정상적으로 통신하는지 확인해 봅시다.  
가장 쉬운 방법은 카프카 브로커 정보를 요청하는 것입니다.  
카프카 바이너리 패키지는 카프카 브로커에 대한 정보를 가져올 수 있는 kafka-broker-api-version.sh 명령어를 제공합니다.  
이 명령어를 통해 카프카 브로커와 정상적으로 연동되는지 확인할 수 있습니다.  
해당 명령어를 로컬에서 사용하기 위해서는 로컬에 카프카 바이너리 패키지를 다운로드해야 합니다.  
```sh
# 내 로컬 컴퓨터에서 진행
# 다운
curl https://archive.apache.org/dist/kafka/2.5.0/kafka_2.12-2.5.0.tgz --output kafka.tgz

# 압축 해제
tar -xvzf kafka.tgz

# 이동
cd kafka_2.12-2.5.0/

# 통신 확인
bin/kafka-broker-api-versions.sh --bootstrap-server 3.36.131.80:9092
```

### 테스트 편의를 위한 hosts 설정
앞으로 로컬에서 EC2에 요청을 보내 실습할 것인데 매번 ip주소를 입력하기는 번거로우니 특정한 이름으로 매핑합시다.
```sh
# 로컬에서 진행
vi /etc/hosts
ec2의ip 매핑할이름

# 예시
3.36.131.80 my-kafka
```


## 카프카 커맨드 라인 툴
커맨드 라인 툴을 통해 카프카 브로커 운영에 필요한 다양한 명령을 내릴 수 있습니다.  
카프카 브로커가 설치된 EC2 에 접속하여 명령을 실행해도 되고, 브로커에 9092로 접근 가능한 컴퓨터에서 명령을 수행할 수도 있습니다.  
커맨드 라인 툴을 통해 토픽 관련 명령을 실행할 때 필수 옵션과 선택 옵션이 있습니다.  
선택 옵션을 지정하지 않을 시 브로커에 설정된 기본 설정값 또는 커맨드 라인 툴의 기본값으로 대체되어 설정됩니다.  
그러므로 커맨드 라인 툴을 사용하기 전에 현재 브로커에 옵셥이 어떻게 설정되어 있는지 확인한 후에 사용하면 실수할 확률이 줄어듭니다.  

### kafka-topics.sh
이 커맨드 라인 툴을 통해 토픽과 관련된 명령을 실행할 수 있습니다.  
토픽이란 카프카에서 데이터를 구분하는 가장 기본적인 개념입니다. 마치 RDBMS에서 사용하는 테이블과 유사하다고 볼 수 있습니다.  
카프카 클러스터에 토픽은 여러 개 존재할 수 있고 토픽에는 파티션이 존재하는데 파티션의 개수는 최소 1개부터 시작합니다.  
파티션을 통해 한 번에 처리할 수 있는 데이터 양을 늘릴 수 있고 토픽 내부에서도 파티션을 통해 데이터의 종류를 나눠 처리할 수 있습니다.  

#### 토픽 생성
```
bin/kafka-topics.sh --create --bootstrap-server my-kafka:9092 --topic hello.kafka
```
+ create
    - 토픽 생성 명렁어
+ bootstrap-server
    - 토픽을 생성할 카프카 클러스터를 구성하는 브로커들의 ip와 port를 명시합니다.
    - 여기서는 1개의 카프카 브로커와 통신하므로 하나만 적습니다.
    - my-kafka는 앞서 테스트 편의를 위해 ip와 매핑한 이름입니다.
+ topic
    - 토픽 이름을 작성합니다.
    - 토픽 이름은 내부 데이터가 무엇이 있는지 유추가 가능할 정도로 자세히 적는 것을 권장합니다.

```sh
# \는 너무 길어서 한칸 내리기 위한 문법
bin/kafka-topics.sh --create --bootstrap-server my-kafka:9092 --partitions 3 \
--replication-factor 1 --config retention.ms=172800000 --topic hello.kafka.2
```
앞선 명령어는 설정된 기본값으로 동작했다면 이번에는 많은 옵션을 줬습니다.
+ partitions
    - 파티션의 개수를 지정합니다.
    - 최소 개수는 1개이고 옵션을 사용하지 않으면 config/server.properties에 있는 num.partitions 옵션을 따릅니다.
+ replication-factor
    - 토픽의 파티션을 복제할 복제 개수를 명시합니다.
    - 1은 복제를 하지 않고 사용한다는 의미이고 2는 1개의 복제본을 사용하겠다는 의미입니다.
    - 파티션의 데이터는 한 개의 브로커에 장애가 발생하더라도 나머지 한 개 브로커에 저장된 데이터를 사용하여 안전하게 데이터 처리를 지속적으로 할 수 있도록 각 브로커마다 저장됩니다.
    - 복제 개수의 최소 설정은 1이고 최대 설정은 통신하는 카프카 클러스터의 브로커 개수 입니다.
    - 옵션을 주지 않으면 마찬가지로 설정 파일에 있는 default.replication.factor 옵션값에 따라 동작합니다.
    - config를 통해 kafka-topics.sh 명령에 포함되지 않은 추가적인 설정을 할 수 있습니다.
    - retention.ms는 토픽의 데이터 유지 기간을 뜻하고 172800000ms는 2일을 의미합니다.

#### 토픽 리스트 조회
```sh
# 토픽명만 조회
bin/kafka-topics.sh --bootstrap-server my-kafka:9092 --list

# 상세 조회
bin/kafka-topics.sh --bootstrap-server my-kafka:9092 --describe --topic hello.kafka.2
```
+ list
    - 토픽들의 이름만 보여줍니다.
+ describe
    - 파티션 개수, 복제된 파티션이 위치한 브로커의 번호, 기타 토픽 구성 설정, 파티션의 리더인 브로커 위치 등을 확인할 수 있습니다.

#### 토픽 옵션 수정
토픽에 설정된 옵션을 변경하기 위해서는 kafka-topics.sh 또는 kafka-configs.sh 두 개를 사용합니다.  
+ kafka-topics.sh
    - 파티션 개수를 변경하기 위해 사용 
+ kafka-configs.sh
    - 토픽 삭제 정책인 리텐션 기간을 변경하기 위해 사용

```sh
# 파티션 4개로 늘리기
bin/kafka-topics.sh --bootstrap-server my-kafka:9092 --topic hello.kafka --alter --partitions 4

# 확인
bin/kafka-topics.sh --bootstrap-server my-kafka:9092 --describe --topic hello.kafka

# 리텐션 기간 수정
bin/kafka-configs.sh --bootstrap-server my-kafka:9092 --entity-type topics --entity-name hello.kafka --alter --add-config retention.ms=86400000

# 확인
bin/kafka-configs.sh --bootstrap-server my-kafka:9092 --entity-type topics --entity-name hello.kafka --describe
```
+ alter옵션과 partition 옵션을 같이 사용하면 파티션 개수를 변경할 수 있습니다. 토픽의 파티션을 늘릴 수 있지만 줄일 수는 없습니다. 따라서 개수를 늘릴 때 반드시 늘려야하는 상황인지 판단하는 것이 중요합니다.
+ add-config 옵션은 이미 존재하는 설정값을 변경하고 존재하지 않는 설정값은 신규로 추가합니다.

#### kafka-console-producer.sh
생성된 hello.kafka 토픽에 데이터를 넣을 수 있는 kafka-console-producer.sh 명령어를 실행해 봅시다.  
토픽에 넣는 데이터는 __레코드__ 라고 부르며 메시지 키(key)와 메시지 값(value)로 구성되어 있습니다.  
메시지 키 없이 보내면 자바의 null로 설정되어 브로커로 전송됩니다.  
```sh
bin/kafka-console-producer.sh --bootstrap-server my-kafka:9092 --topic hello.kafka
>hello
>kafka
>0
>1
>2
>3
>4
>5
```
입력하면 별다른 응답 없이 메시지 값이 전송됩니다.  
여기서 주의할 점은 kafka-console-producer.sh로 전송되는 레코드 값은 UTF-8을 기반으로 Byte로 변환되고 ByteArraySerializer로만 직렬화된다는 점입니다.  
즉, String이 아닌 타입으로는 직렬화하여 전송할 수 없습니다. 그러므로 텍스트 목적으로 문자열만 전송할 수 있고 다른 타입으로 직렬화하여 데이터를 브로커로 전송하고 싶다면 카프카 프로듀서 애플리케이션을 직접 개발해야 합니다.  
이제 키를 갖는 레코드를 전송해봅시다. 
```sh
bin/kafka-console-producer.sh --bootstrap-server my-kafka:9092 --topic hello.kafka --property "parse.key=true" --property "key.separator=:"
>key1:no1
>key2:no2
>key3:no3
```
+ parse.key 를 true로 두면 레코드를 전송할 때 메시지 키를 추가할 수 있습니다.
+ key.separator는 메시지 키와 값을 구분하는 구분자를 선언합니다. 설정하지 않는다면 탭키(\t)를 디폴트 값으로 갖기에 키값을 입력하고 탭키를 입력하고 메시지 값을 입력하면 됩니다.

메시지 키가 null인 경우 프로듀서가 파티션으로 전송할 때 레코드 배치 단위로 라운드로빈으로 전송합니다.  
메시지 키가 존재하는 경우 키의 해시값을 작성하여 존재하는 파티션 중 한 개에 할당됩니다. 따라서 키가 같으면 같은 파티션으로 전송됩니다.  
이런 메시지 키와 파티션 할당은 프로듀서에서 설정한 파티셔너에 의해 결정되므로, 키에 따른 파티션 할당을 다르게 커스텀할 수도 있습니다.  
<br>

__cf) 파티션의 개수가 늘어나면 프로듀싱되는 레코드들은 어느 파티션으로 갈까?__  
메시지 키를 가진 레코드의 경우 파티션이 추가되면 파티션과 메시지 키의 일관성이 보장되지 않습니다.  
따라서 이전에 키값을 넣으면 다른 파티션으로 이동할 가능성이 존재합니다.  
이를 해결하려면 커스텀 파티셔너를 만들어 운영해야 합니다.  

#### kafka-console-consumer.sh
이제 hello.kafka 토픽으로 전송한 데이터를 kafka-console-consumer.sh로 확인해봅시다.  
이때 필수 옵션이 몇 가지 필요하다.
+ bootstrap-server : 카프카 클러스터 정보
+ topic : 토픽 이름
+ from-beginning : 이 옵션을 추가로 주면 토픽에 저장된 가장 처음 데이터부터 출력

```sh
bin/kafka-console-consumer.sh --bootstrap-server my-kafka:9092 --topic hello.kafka --from-beginning

no2
hello
0
1
생략
```
이전에 producer로 보낸 값들이 출력된 것을 확인할 수 있습니다.  
만약 데이터의 키와 값을 같이 확인하고 싶다면 추가적인 옵션이 필요합니다.

```sh
bin/kafka-console-consumer.sh --bootstrap-server my-kafka:9092 \
--topic hello.kafka --property print.key=true --property key.separator="-" \
--group hello-group --from-beginning

key2-no2
null-hello
null-0
null-1
null-kafka
생략
```
+ print.key=true : 키값을 보여줍니다.
+ key.separator : 키 밸류 구분자를 설정합니다.
+ group 옵션을 통해 신규 컨슈머 그룹을 생성합니다. 컨슈머 그룹은 1개 이상의 컨슈머로 이뤄져 있습니다. 해당 그룹을 통해 가져간 토픽의 메시지는 가져간 메시지에 대해 커밋을 합니다. 커밋이란 컨슈머가 특정 레코드까지 처리를 완료했다고 레코드의 오프셋 번호를 카프카 브로커에 저장하는 것입니다. 커밋 정보는 __consumer_offsets 이름의 내부 토픽에 저장됩니다.

결과값을 보면 producer로 전송했던 데이터의 순서가 현재 출력되는 순서와 다릅니다.  
이는 파티션 개념 때문에 생기는 현상인데 consumer.sh 명령어를 통해 토픽의 데이터를 가져가게 되면 토픽의 모든 파티션으로부터 동일한 중요도로 데이터를 가져갑니다.  
즉, 프로듀서가 넣은 데이터의 순서와 컨슈머가 토픽에서 가져간 데이터의 순서가 달라지게 됩니다.  
데이터의 순서를 보장하고 싶다면 가장 좋은 방법은 파티션 1개로 구성된 토픽을 만들면 됩니다.  

#### kafka-consumer-groups.sh
앞서 hello-group이름의 컨슈머 그룹으로 생성된 컨슈머로 hello.kafka 토픽의 데이터를 가져왔습니다.  
컨슈머 그룹을 따로 생성하는 명령을 날리지 않고 컨슈머를 동작할 때 컨슈머 그룹 이름을 지정하면 새로 생성됩니다.  
생성된 컨슈머 그룹의 리스트는 kafka-consumer-groups.sh 명령어로 확인할 수 있습니다.
```sh
# list 옵션이 컨슈머 그룹명을 출력해줍니다.
bin/kafka-consumer-groups.sh --bootstrap-server my-kafka:9092 --list
```
```sh
bin/kafka-consumer-groups.sh --bootstrap-server my-kafka:9092 --group hello-group --describe

# 출력
Consumer group 'hello-group' has no active members.

GROUP           TOPIC           PARTITION  CURRENT-OFFSET  LOG-END-OFFSET  LAG             CONSUMER-ID     HOST            CLIENT-ID
hello-group     hello.kafka     3          1               1               0               -               -               -
hello-group     hello.kafka     2          3               3               0               -               -               -
hello-group     hello.kafka     1          4               4               0               -               -               -
hello-group     hello.kafka     0          3               3               0               -               -               -
```
+ 옵션
    - group : 어떤 컨슈머 그룹에 대한 상세 내용을 볼 것인지 지정
    - describe : 컨슈머 그룹의 상세 내용 확인
+ 출력
    - GROUP, TOPIC, PARTITION : 조회한 컨슈머 그룹이 마지막으로 커밋한 토픽과 파티션을 나타냅니다.
    - CURRENT-OFFSET : 컨슈머 그룹이 가져간 토픽의 파티션에 가장 최신 offset이 몇 번인지 나타냅니다.
        - 오프셋이란 파티션의 각 레코드에 할당된 번호입니다. 이 번호는 데이터가 파티션에 들어올 때마다 1씩 증가합니다.
    - LOG-END-OFFSET : 해당 컨슈머 그룹의 컨슈머가 어느 오프셋까지 커밋했는지 알 수 있습니다.
    - LAG : 컨슈머 그룹이 토픽의 파티션에 있는 데이터를 가져가는 데 얼마나 지연이 발생하는지 나타내는 지표입니다. 랙은 컨슈머 그룹이 커밋한 오프셋과 해당 파티션의 가장 최신 오프셋 간의 차이 입니다.
    - CONSUMER-ID : 컨슈머의 토픽(파티션) 할당을 카프카 내부적으로 구분하기 위해 사용하는 id입니다. 이 값은 client id에 uuid값을 붙여서 자동 할당되어 유니크한 값으로 설정됩니다.
    - HOST : 컨슈머가 동작하는 host 명을 출력합니다.
    - CLIENT-ID : 컨슈머에 할당된 id입니다.


#### kafka-verifiable-producer, consumer.sh
kafka-verifiable로 시작하는 2개의 스크립트를 사용하면 String 타입 메시지 값을 코드 없이 주고받을 수 있습니다.  
카프카 클러스터 설치가 완료된 이후에 토픽에 데이터를 전송하여 간단한 네트워크 통신 테스트를 할 때 유용합니다.  
```sh
bin/kafka-verifiable-producer.sh --bootstrap-server my-kafka:9092 --max-messages 10 --topic verify-test

# 출력
# 최초 실행 시점
{"timestamp":1646221251926,"name":"startup_complete"} 
# 메시지별로 보낸 시간과 메시지 키, 메시지 값, 토픽, 저장된 파티션, 저장된 오프셋 번호
{"timestamp":1646221252222,"name":"producer_send_success","key":null,"value":"0","offset":0,"topic":"verify-test","partition":0} 

...

# 10개의 데이터가 모두 전송된 이후 통계값 출력 - 평균 처리량 확인
{"timestamp":1646221252236,"name":"tool_data","sent":10,"acked":10,"target_throughput":-1,"avg_throughput":32.15434083601286}
```
+ bootstrap-server : 통신하고자 하는 클러스터 호스트와 포트 입력
+ max-messages : kafka-verifiable-producer.sh로 보내는 데이터 개수를 지정한다. -1을 옵션값으로 입력하면 producer가 종료될 때까지 계속 데이터를 토픽으로 보낸다.
+ topic : 데이터를 받을 대상 토픽 지정

이제 전송한 데이터를 kafka-verifiable-consumer.sh로 확인해 봅시다.
```sh
bin/kafka-verifiable-consumer.sh --bootstrap-server my-kafka:9092 --topic verify-test --group-id test-group

# 출력
# 시작
{"timestamp":1646221635415,"name":"startup_complete"}
# 컨슈머는 토픽에서 데이터를 가져오기 위해 파티션에 할당하는 과정을 거치는데 0 파티션이 할당된 것을 확인
{"timestamp":1646221635851,"name":"partitions_assigned","partitions":[{"topic":"verify-test","partition":0}]}
{"timestamp":1646221635938,"name":"records_consumed","count":10,"partitions":[{"topic":"verify-test","partition":0,"count":10,"minOffset":0,"maxOffset":9}]}
# 컨슈머는 한 번에 다수의 메시지를 가져와 처리하므로 한 번에 10개의 메시지를 정상적으로 받음을 확인
# 메시지 수신 이후 10번 오프셋 커밋 여부도 확인 가능, 정상적으로 커밋 완료
{"timestamp":1646221635949,"name":"offsets_committed","offsets":[{"topic":"verify-test","partition":0,"offset":10}],"success":true}
```
+ bootstrap-server : 통신하고자 하는 클러스터 호스트와 포트 입력
+ topic : 데이터를 가져오고자 하는 토픽 지정
+ group-id : 컨슈머 그룹 지정

#### kafka-delete-records.sh
kafka-delete-records.sh는 이미 적재된 토픽의 데이터를 지울 때 사용합니다.  
이미 적재된 토픽의 데이터 중 가장 오래된 데이터(가장 낮은 숫자의 오프셋)부터 특정 시점의 오프셋까지 삭제할 수 있습니다.  
예를 들어, test 토픽의 0번 파티션에서 0부터 100까지 데이터가 들어있으면 test토픽의 0번 파티션에 저장된 데이터 중 0부터 50번 오프셋 데이터까지 지우고 싶으면 다음과 같습니다.
```sh
vi delete-topic.json
{"partitions": [{"topic":"test", "partition": 0, "offset": 50}], "version":1 }

bin/kafka-delete.records.sh --bootstrap-server my-kafka:9092 --offset-json-file delete-topic.json
```
+ 삭제하고자 하는 데이터에 대한 정보를 파일로 지정해서 사용합니다. 해당 파일에는 삭제하고자 하는 토픽, 파티션, 오프셋 정보가 들어가야 합니다.
+ offset-json-file 옵션으로 삭제 정보를 담은 delete-topic.json을 입력하면 파일을 읽어서 데이터를 삭제합니다.

주의할 점은 토픽의 특정 레코드 하나만 삭제하는 것이 아니라 파티션에 존재하는 __가장 오래된 오프셋부터 지정한 오프셋까지__ 삭제된다는 점입니다.  
카프카에서는 토픽의 파티션에 저장된 특정 데이터만 삭제할 수는 없습니다.  
