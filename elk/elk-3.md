# Elastic Stack - SSL, TSL, HTTPS 보안 구성하기

# 1. 구조
---
![그림1](https://github.com/backtony/blog-code/blob/master/elk/img/3/3-1.PNG?raw=true)  

__사용 환경__
+ Ubuntu server 20.04 EC2 4대
+ elasticsearch : 10.0.7.189
+ kibana : 10.0.7.163, 3.36.72.204
+ logstash : 10.0.7.136
+ filebeat : 10.0.7.127
+ deb 패키지로 설치

kibana는 public IP를 할당받아 외부에서 접속이 가능하도록 했습니다.

<br>

# 2. Elasticsearch를 위한 SSL 인증서 생성과 TLS 활성화
---
```sh
pwd
/home/ubuntu

# tmp 폴더 생성
mkdir -p tmp/cert_blog
cd tmp/cert_blog

# 인스턴스 yml 파일 생성
sudo vi instance.yml

# 구성할 elastic stack 요소들 작성
instances:
        - name: 'master'
          ip:
                  - 10.0.7.189
        - name: 'kibana'
          ip:
                  - 10.0.7.163
        - name: 'logstash'
          ip:
                  - 10.0.7.136

# CA 및 yml 파일에 작성한 각 요소들의 서버 인증서 생성
cd /usr/share/elasticsearch
sudo bin/elasticsearch-certutil cert --keep-ca-key --pem --in ~/tmp/cert_blog/instance.yml --out ~/tmp/cert_blog/certs.zip

# 인증서 압축 해제
cd ~/tmp/cert_blog
sudo apt install unzip
sudo unzip certs.zip -d ./certs

# elasticsearch 설정 폴더 접근 권한 부여
sudo chmod 777 /etc/elasticsearch

# cert 파일을 config 폴더로 복사
cd /etc/elasticsearch
mkdir certs
cp ~/tmp/cert_blog/certs/ca/ca* ~/tmp/cert_blog/certs/master/* certs

# yml 파일 구성
sudo vi elasticsearch.yml

node.name: master
network.host: 10.0.7.189
xpack.security.enabled: true
xpack.security.http.ssl.enabled: true
xpack.security.transport.ssl.enabled: true
xpack.security.http.ssl.key: certs/master.key
xpack.security.http.ssl.certificate: certs/master.crt
xpack.security.http.ssl.certificate_authorities: certs/ca.crt
xpack.security.transport.ssl.key: certs/master.key
xpack.security.transport.ssl.certificate: certs/master.crt
xpack.security.transport.ssl.certificate_authorities: certs/ca.crt
discovery.seed_hosts: [ "10.0.7.189" ]
cluster.initial_master_nodes: [ "master" ]

# elasticsearch 실행 및 상태 확인
sudo service elasticsearch start
sudo service elasticsearch status

# 기본 사용자 비밀전호 설정
cd /usr/share/elasticsearch
sudo bin/elasticsearch-setup-passwords interactive -u "https://10.0.7.189:9200"

Please confirm that you would like to continue [y/N]y
# 편의상 123456 으로 전부 입력
Enter password for [elastic]:
Reenter password for [elastic]:
Enter password for [apm_system]:
Reenter password for [apm_system]:
Enter password for [kibana_system]:
Reenter password for [kibana_system]:
Enter password for [logstash_system]:
Reenter password for [logstash_system]:
Enter password for [beats_system]:
Reenter password for [beats_system]:
Enter password for [remote_monitoring_user]:
Reenter password for [remote_monitoring_user]:

# 접속 확인
curl --cacert ~/tmp/cert_blog/certs/ca/ca.crt -u elastic 'https://10.0.7.189:9200/_cat/nodes?v'
# 123456 입력
Enter host password for user 'elastic':
# 정상 작동
ip         heap.percent ram.percent cpu load_1m load_5m load_15m node.role   master name
10.0.7.189           38          81  11    0.14    0.18     0.11 cdfhilmrstw *      master
```
<br>

# 3. Kibana TSL 활성화
---
## Kibana EC2로 인증서 옮기기
앞서 Elasticsearch의 EC2에서 진행합니다.
```sh
cd /home/ubuntu/tmp/cert_blog/certs

# 키바나 EC2 에 접속하는 pem키를 작성합니다. 
sudo vi gjgs.pem
-----BEGIN RSA PRIVATE KEY-----
MIIEowIBAAKCAQEAi6o4PU2hcZNdtOyv7P00I0NUzjPMb39VtXiwQJo6n2rRZH37
....
uhDxN1FKEE6eluajvt1KJJ5DJTEPKCnG4he/vEBUFm2xwJtZktH5
-----END RSA PRIVATE KEY-----

# pem 키 권한 축소
sudo chmod 600 gjgs.pem

# scp -i [pem파일경로] [업로드할 파일 이름] [ec2-user계정명]@[ec2 instance의 public DNS]:~/[경로]
sudo scp -i gjgs.pem ca/ca.crt ubuntu@10.0.7.163:~/
sudo scp -i gjgs.pem kibana/* ubuntu@10.0.7.163:~/
```

## Kibana 설정
Kibana EC2에서 진행합니다.
```sh
pwd
/home/ubuntu

# Kibana 설정 폴더 권한 부여
sudo chmod 777 /etc/kibana

# 인증서 폴더 생성
cd /etc/kibana
sudo mkdir -p config/certs

# 인증서 이동
cd /home/ubuntu
sudo mv ca.crt /etc/kibana/config/certs
sudo mv kibana/* /etc/kibana/config/certs

# kibana.yml 파일 구성
cd /etc/kibana
sudo vi kibana.yml

server.name: "kibana"
server.host: "0.0.0.0"
server.ssl.enabled: true
server.ssl.certificate: /etc/kibana/config/certs/kibana.crt
server.ssl.key: /etc/kibana/config/certs/kibana.key
elasticsearch.hosts: ["https://10.0.7.189:9200"]
elasticsearch.username: "kibana"
elasticsearch.password: "123456"
elasticsearch.ssl.certificateAuthorities: [ "/etc/kibana/config/certs/ca.crt" ]

# 퍼블릭 ip로 접속해보기
# 오류창이 뜰텐데 윈도우의 경우 고급에서 이동을 클릭하면 되고 
# 맥의 경우 빈 공간을 클릭하고 thisisunsafe 를 입력하면 접속할 수 있습니다.
https://3.36.72.204:5601

# 아이디 패스워드
elastic
123456 
```

## Logstash 사용자 준비
aws iam 만드는 것과 비슷합니다. 권한을 만들고 해당 권한을 사용자에게 부여하는 과정입니다.

![그림2](https://github.com/backtony/blog-code/blob/master/elk/img/3/3-2.PNG?raw=true)  
키바나에 접속한 상태로 진행합니다. 왼쪽 탭 최하단에 Stack Management -> Roles를 클릭하고 우측 상단에 Create role을 클릭합니다.
<Br><br>

![그림3](https://github.com/backtony/blog-code/blob/master/elk/img/3/3-3.PNG?raw=true)  
이름을 정해주고 cluster privileges에는 monitor과 manage_index_templates 를 넣어줍니다. indices에는 logstash의 default인 logstash* 를 추가해줍니다. 저는 spring* 인덱스로 올리는 설정을 하고 있어서 spring*도 설정해줬습니다. 하단의 privileges에는 write와 create_index를 추가해주고 만들어줍니다.
<br><br>

![그림4](https://github.com/backtony/blog-code/blob/master/elk/img/3/3-4.PNG?raw=true)  
이번에는 왼쪽 탭에서 Users를 클릭하고 상단에 Create User을 클릭해서 User을 만들어 줍니다. 이름을 주고 패스워드는 간단하게 123456으로 주었습니다. Roles에는 방금 만들었던 role을 선택해주고 만들어줍니다.

<Br>

# 4. Logstash를 위한 TLS 활성화
---
## Logstash EC2로 인증서 옮기기
Kibana에서 인증서를 옮겨왔던 것과 똑같이 Logstash도 인증서를 옮겨와야합니다. Elasticsearch EC2에서 진행합니다.
```sh
cd /home/ubuntu/tmp/cert_blog/certs

# scp -i [pem파일경로] [업로드할 파일 이름] [ec2-user계정명]@[ec2 instance의 public DNS]:~/[경로]
sudo scp -i gjgs.pem ca/ca.crt ubuntu@10.0.7.136:~/
sudo scp -i gjgs.pem logstash/* ubuntu@10.0.7.136:~/
```

## Logstash 세팅
Logstash EC2에서 진행합니다.
```sh
# logstash 설정 폴더 권한 부여
sudo chmod 777 /etc/logstash

# 인증서 폴더 생성
cd /etc/logstash
sudo mkdir -p config/certs

# 인증서 이동
cd /home/ubuntu
sudo mv ca.crt /etc/logstash/config/certs
sudo mv logstash/* /etc/logstash/config/certs

# Beats 입력 플러그인을 위해 logstash.key를 PKC#8 형식으로 변환
cd /etc/logstash
sudo openssl pkcs8 -in config/certs/logstash.key -topk8 -nocrypt -out config/certs/logstash.pkcs8.key

# logstash.yml 구성
sudo vi logstash.yml
node.name: logstash
xpack.monitoring.enabled: true
xpack.monitoring.elasticsearch.username: logstash_system
xpack.monitoring.elasticsearch.password: '123456'
xpack.monitoring.elasticsearch.hosts: [ 'https://10.0.7.189:9200' ]
xpack.monitoring.elasticsearch.ssl.certificate_authority: /etc/logstash/config/certs/ca.crt

# conf 구성
cd /etc/logstash/conf.d
sudo vi web.conf
input {
        beats {
                port => 5044
                type => "spring"
                ssl => true
                ssl_key => '/etc/logstash/config/certs/logstash.pkcs8.key'
                ssl_certificate => '/etc/logstash/config/certs/logstash.crt'
        }
}

filter {
        if [type] == "spring" {
                dissect {
                        mapping => {"message" => "[%{timestamp}][%{level}][%{method}] - %{message}"}
                }
                date {
                        match => ["timestamp", "yyyy-MM-dd HH:mm:ss" ]
                        target => "@timestamp"
                        timezone => "Asia/Seoul"
                        locale => "ko"
                }
        }
}

output {
        if [type] == "spring" {
                elasticsearch {
                        hosts => [ "https://10.0.7.189:9200" ]
                        index => "spring-%{+YYYY.MM.dd}"
                        cacert => '/etc/logstash/config/certs/ca.crt'
                        user => 'logstash_writer'
                        password => '123456'
                }
        }
}
```

<Br>

# 5. Filebeat TLS 설정
---
## Filebeat EC2로 인증서 옮기기
Elasticsearch EC2에서 진행합니다.
```sh
cd /home/ubuntu/tmp/cert_blog/certs

# scp -i [pem파일경로] [업로드할 파일 이름] [ec2-user계정명]@[ec2 instance의 public DNS]:~/[경로]
sudo scp -i gjgs.pem ca/ca.crt ubuntu@10.0.7.127:~/
```

## Filebeat 세팅
Filebeat EC2에서 진행합니다.
```sh
# logstash 설정 폴더 권한 부여
sudo chmod 777 /etc/filebeat

# 인증서 폴더 생성
cd /etc/filebeat
sudo mkdir -p config/certs

# 인증서 이동
cd /home/ubuntu
sudo mv ca.crt /etc/filebeat/config/certs

# filebeat.yml 파일 구성
cd /etc/filebeat
sudo vi filebeat.yml

# ============================== Filebeat inputs ===============================

filebeat.inputs:

# Each - is an input. Most options can be set at the input level, so
# you can use different inputs for various configurations.
# Below are the input specific configurations.

- type: log

  # Change to true to enable this input configuration.
  enabled: true

....

#### elastic search 관련 설정은 모두 주석 처리 ####

# ---------------------------- Elasticsearch Output ----------------------------
#output.elasticsearch:
  # Array of hosts to connect to.
  # hosts: ["localhost:9200"]

  # Protocol - either `http` (default) or `https`.
  #protocol: "https"

  # Authentication credentials - either API key or username/password.
  #api_key: "id:api_key"
  #username: "elastic"
  #password: "changeme"


#### logstash output 부분 주석 해제####

# ------------------------------ Logstash Output -------------------------------
output.logstash:
  # The Logstash hosts
        hosts: ["10.0.7.136:5044"]

  # Optional SSL. By default is off.
  # List of root certificates for HTTPS server verifications
        ssl.certificate_authorities: ["/etc/filebeat/config/certs/ca.crt"]
```
<Br>

# 6. 실행
---
```sh
# 각각의 EC2에서 자신에 맞는 명령어를 실행해줍니다.
sudo service elasticsearch start
sudo service kibana start
sudo service logstash start
sudo service filebeat start

# filebeat 가 로그를 수집하도록 몇가지 로그를 남겨주도록 합니다.
# 저는 spring log 파일이 생기도록 해두었습니다. 이부분은 각자 다를테니 생략하겠습니다.
```
<br>

![그림5](https://github.com/backtony/blog-code/blob/master/elk/img/3/3-5.PNG?raw=true)  
키바나에 접속해서 왼쪽 탭에서 Dev Tools를 클릭해서 쿼리를 날려보면 잘 들어와있는 것을 확인할 수 있습니다.



<Br><Br>

__참고__  
<a href="https://www.elastic.co/kr/blog/configuring-ssl-tls-and-https-to-secure-elasticsearch-kibana-beats-and-logstash" target="_blank"> Elasticsearch, Kibana, Beats, Logstash의 보안을 유지하기 위한 SSL, TLS, HTTPS 구성</a>  

