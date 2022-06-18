# Spring & Jenkins & Docker & DockerHub & GitHub 활용한 CI/CD


사용하는 도구 및 환경은 다음과 같습니다.  
+ Mac
+ Spring boot
+ gradle 7.0.2
+ 프리티어 EC2 2대 (젠킨스용,운영용)
+ Jenkins
+ Java 11
+ Docker
+ IntellJ
+ DockerHub private Repository
+ Github private Repository

<br>

# 1. 동작 과정
---
![그림1](https://github.com/backtony/blog-code/blob/master/aws/img/1/1-1.PNG?raw=true)

1. local에서 Github로 푸시합니다.
2. Github의 webhook을 이용해 jenkins에 전달합니다.
3. jenkins 동작 과정
  - Github의 코드를 받아 빌드, 테스트를 진행합니다.
  - Dockerfile을 이용해 이미지를 빌드하고 DockerHub에 푸시합니다.
  - deploy.sh 파일을 운영 EC2로 전송하고 운영 EC2에 deploy.sh 실행 명령어를 보냅니다.
  - deploy.sh 안에는 DockeHub에서 이미지를 받아오고 실행시키는 코드가 담겨있습니다.
4. 운영용 EC2에서 deploy.sh 파일 실행하여 docker로 spring boot 프로젝트를 띄웁니다.




<br><br>

# 2. Spring code
---
간단하게 HealthCheck가 가능한 프로젝트를 만들겠습니다.  
간단하게 다음 코드만 작성하겠습니다.
+ HealthController :  healthCheck가 가능한 간단한 컨트롤러
+ Dockerfile : 도커파일
+ deploy.sh : 배포용 스크립트
+ application.yml 

__패키지 구조__  
![그림68](https://github.com/backtony/blog-code/blob/master/aws/img/1/1-68.PNG?raw=true)

<Br><Br>

__HealthController__  
```java
@RestController
public class HealthController {    
    @GetMapping
    public String healthCheck(){
        return "health ok";
    }
}
```
<br>

__Dockerfile__
```dockerfile
# JDK11 이미지 사용
FROM openjdk:11-jdk

VOLUME /tmp

# JAR_FILE 변수에 값을 저장
ARG JAR_FILE=./build/libs/awsstudy-1.0.jar

# 변수에 저장된 것을 컨테이너 실행시 이름을 app.jar파일로 변경하여 컨테이너에 저장
COPY ${JAR_FILE} app.jar

# 빌드된 이미지가 run될 때 실행할 명령어
ENTRYPOINT ["java","-jar","app.jar"]
```
+ 마운트에 /tmp를 사용하는 이유
  - 문서는 아래와 같이 작성되어 있습니다.
  - We added a VOLUME pointing to "/tmp" because that is where a Spring Boot application creates working directories for Tomcat by default. The effect is to create a temporary file on your host under "/var/lib/docker" and link it to the container under "/tmp".
  - 번역해보면 spring boot의 Tomcat의 default 저장소가 /tmp인데 위와 같이 볼륨 마운트를 해주면 호스트의 /var/lib/docker에 임시파일을 만들고 컨테이너 안의 /tmp 와 연결할 수 있다는 뜻입니다.


<br>

__deploy.sh__
```sh
# 가동중인 awsstudy 도커 중단 및 삭제
sudo docker ps -a -q --filter "name=awsstudy" | grep -q . && docker stop awsstudy && docker rm awsstudy | true

# 기존 이미지 삭제
sudo docker rmi backtony/awsstudy:1.0

# 도커허브 이미지 pull
sudo docker pull backtony/awsstudy:1.0

# 도커 run
docker run -d -p 8080:8080 --name awsstudy backtony/awsstudy:1.0

# 사용하지 않는 불필요한 이미지 삭제 -> 현재 컨테이너가 물고 있는 이미지는 삭제되지 않습니다.
docker rmi -f $(docker images -f "dangling=true" -q) || true
```
deploy.sh 파일은 운영 EC2에서 수행할 작업을 명시해놓은 파일입니다.


<br><br>

# 3. EC2 만들기
---
## 운영용 EC2 만들기

AWS홈페이지에 접속하여 로그인한 뒤 EC2를 검색하고 오른쪽 상단에 인스턴스 시작을 클릭합니다.  
__1단계 : 프리티어로 사용 가능한 Amazon Linux 2 AMI를 선택합니다.__
![그림3](https://github.com/backtony/blog-code/blob/master/aws/img/1/1-3.PNG?raw=true)


<Br><Br>

__2단계 : 프리티어로 t2.micro를 선택합니다.__
![그림4](https://github.com/backtony/blog-code/blob/master/aws/img/1/1-4.PNG?raw=true)


<Br><Br>

__3단계 : 기본 설정을 건드리지 않고 바로 다음을 누릅니다.__
![그림72](https://github.com/backtony/blog-code/blob/master/aws/img/1/1-72.PNG?raw=true)
<br><br>

__4단계 : 크기를 30으로 변경합니다. 기본적으로 프리티어는 30GB까지 무료로 사용 가능합니다.__
![그림5](https://github.com/backtony/blog-code/blob/master/aws/img/1/1-5.PNG?raw=true)


<br><Br>

__5단계 : 추후 알기쉽도록 태그값을 달아줍니다.__  
![그림6](https://github.com/backtony/blog-code/blob/master/aws/img/1/1-6.PNG?raw=true)


<br><Br>

__6단계 : 보안 그룹을 구성합니다. ssh로는 자신의 IP를 선택해주시고 8080 포트는 전체로 열어줍니다. 그림에는 없지만 규칙추가로 HTTPS 443 포트도 전체로 열어줍니다.__
![그림7](https://github.com/backtony/blog-code/blob/master/aws/img/1/1-7.PNG?raw=true)


<br><Br>

__7단계 : 앞으로 해당 EC2에 접근할 때 사용할 키를 생성하고 다운로드합니다. 추후 EC2 접근에 사용되니 잘 보관합니다.__
![그림8](https://github.com/backtony/blog-code/blob/master/aws/img/1/1-8.PNG?raw=true)


<br><Br>

## Jenkins용 EC2 만들기

__4단계까지의 과정은 위의 운영용 EC2 생성과정과 동일합니다.__
<br><br>

__5단계 : 추후 알기쉽도록 태그값을 달아줍니다.__  
![그림9](https://github.com/backtony/blog-code/blob/master/aws/img/1/1-9.PNG?raw=true)


<br><br>

__6단계 : 보안그룹을 구성합니다. ssh는 자신의 IP를 선택해주시고 이번에는 8080포트 또한 내 IP를 선택합니다.__
![그림10](https://github.com/backtony/blog-code/blob/master/aws/img/1/1-10.PNG?raw=true)


<br><br>

__7단계 : 기존 키페어를 선택하시고 운영용 EC2를 만들 때 사용한 키를 선택합니다.__
![그림11](https://github.com/backtony/blog-code/blob/master/aws/img/1/1-11.PNG?raw=true)


<Br>

## EC2에 탄력적 IP 할당하기
EC2 인스턴스도 결국 하나의 서버이기 때문에 IP가 존재합니다. 인스턴스는 생성 시에 항상 새 IP를 할당하는데, 같은 인스턴스를 중지하고 __다시 시작할 때도 새 IP가 할당__ 됩니다. 즉, 요금을 아끼기 위해 잠깐 인스턴스를 중지하고 다시 시작하게 되면 매번 접속해야 하는 IP가 변경돼서 PC에서 접근할 때마다 IP주소를 확인해야 하는 번거로움이 발생합니다. 따라서 매번 IP가 변경되지 않고 고정 IP를 가지게 하는 방법이 EC2에 탄력적 IP를 할당하는 것입니다.  
<br><br>

__왼쪽 메뉴에서 탄력적 IP를 선택합니다.__
![그림74](https://github.com/backtony/blog-code/blob/master/aws/img/1/1-74.PNG?raw=true)

<BR><Br>

__오른쪽 상단에 탄련적 IP주소 할당을 클릭합니다.__  
![그림13](https://github.com/backtony/blog-code/blob/master/aws/img/1/1-13.PNG?raw=true)

<br><br>

__추가적인 설정 없이 바로 할당을 클릭합니다.__
![그림14](https://github.com/backtony/blog-code/blob/master/aws/img/1/1-14.PNG?raw=true)

<br><br>

__만들어진 탄력적 IP를 선택하고 작업 -> 탄력적 IP주소 연결을 클릭합니다.__  
![그림15](https://github.com/backtony/blog-code/blob/master/aws/img/1/1-15.PNG?raw=true)

<br><Br>

__인스턴스를 클릭하여 앞서 만들었던 하나의 EC2를 선택하고 연결합니다.__
![그림16](https://github.com/backtony/blog-code/blob/master/aws/img/1/1-16.PNG?raw=true)

<br><br>

__왼쪽 메뉴에서 인스턴스를 클릭합니다.__
![그림73](https://github.com/backtony/blog-code/blob/master/aws/img/1/1-73.PNG?raw=true)

<br><Br>

__연결한 인스턴스를 보시면 오른쪽 끝에 탄련적 IP가 부여된 것을 확인할 수 있습니다.__
![그림18](https://github.com/backtony/blog-code/blob/master/aws/img/1/1-18.PNG?raw=true)

<br><Br>

__하나의 인스턴스에만 탄력적 IP를 할당했으므로 나머지 다른 인스턴스에도 똑같이 탄력적 IP를 할당해주시면 됩니다.__

<br><br>

# 4. EC2 초기 세팅
---
## EC2 접속하기
아래 과정은 Mac 환경에서의 과정입니다. 윈도우는 다른 방식을 사용해야합니다.  
AWS 같은 외부 서버로 SSH 접속을 하려면 매번 다음과 같이 긴 명령어를 입력해야 합니다.
```
ssh -i pem키위치 탄력적IP주소
```
매번 위와 같은 명령어를 사용하는 것은 매우 불편하기 때문에 나중에도 접속하기 쉽도록 설정을 진행하겠습니다.  
<br>

EC2를 만들면서 다운받았던 pem파일을 ~/.ssh/로 복사합니다.
```
cp pem키의위치 ~/.ssh/
```
<br>

pem키의 권한을 변경합니다
```
chmod 600 ~/.ssh/pem키파일명.pem
```
<br>

~/.ssh 디렉토리에 config 파일을 생성합니다.
```
vim ~/.ssh/config
```
<br>

아래 사진과 같이 작성합니다. 여기서 Host는 이후에 접속할때 사용할 단축명으로 계속 사용하게 되기 때문에 사용하기 쉬운 이름이므로 작성하시면되고, HostName은 EC2에 부여한 탄력적 IP 주소를 적으시면 됩니다. User은 사용자를 의미하고 IdentityFile은 pem키의 위치를 나타냅니다. 자신의 pem키파일명을 작성해주시면 됩니다.  
![그림19](https://github.com/backtony/blog-code/blob/master/aws/img/1/1-19.PNG) ?raw=true 

위와 같이 작성을 마치면 :wq 명령어로 저장하고 종료합니다.  
<br>

생성된 config 파일은 실행 권한이 필요하므로 권한 설정을 진행합니다.
```
chmod 700 ~/.ssh/config
```
<br>

모든 설정을 끝냈습니다. 이제부터는 아래와 같은 방식으로 config 파일에서 작성했던 Host 명으로 쉽게 EC2에 접근이 가능합니다.
```
ssh Host명

예시
ssh deploy-test
ssh jenkins-test
```
![그림20](https://github.com/backtony/blog-code/blob/master/aws/img/1/1-20.PNG?raw=true)

최초 접속시 yes라고 한 번 더 입력해주시면 접속이 됩니다.
<br><br>

## EC2 hostname 변경
현재 EC2에 접속하시면 아래 사진과 IP만으로 표시되고 있습니다. 여러 서버를 관리 중일 경우 IP만으로 어떤 서비스의 서버인지 확인하기 어렵습니다.
![그림21](https://github.com/backtony/blog-code/blob/master/aws/img/1/1-21.PNG?raw=true)
따라서 Hostname을 변경해주겠습니다.
```
sudo hostnamectl set-hostname 원하는이름

예시
sudo hostnamectl set-hostname deploy
```
변경해주었다면 sudo reboot 를 이용해 EC2를 재부팅 시키고 조금 뒤에 다시 접속해보면 아래 사진과 같이 deploy로 변경된 것을 확인할 수 있습니다.
![그림22](https://github.com/backtony/blog-code/blob/master/aws/img/1/1-22.PNG?raw=true)

<br><br>

## EC2 시간 변경
EC2 서버의 기본 타임존은 UTC입니다. 한국시간으로 변경시켜주겠습니다.
```
sudo rm /etc/localtime
sudo ln -s /usr/share/zoneinfo/Asia/Seoul /etc/localtime
```
위 명령어를 입력한 뒤에 date를 입력하면 시간이 변경된 것을 확인할 수 있습니다.

<br><br>

# 5. 운영 EC2 세팅하기
---
운영 EC2에서는 Jenkins가 도커허브에 푸시한 이미지를 가져와 도커로 띄울 것이기 때문에 우선 도커 설치작업을 진행하겠습니다.
```
운영 EC2 접속
ssh 호스트명

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
```
참고로 echo $USER 을 입력하시면 현재 사용자를 알 수 있습니다.  
<br><br>

# 6. Jenkins EC2 세팅하기
---
## 프리티어 EC2 메모리 부족 해결하기
현재 젠킨스 서버로 사용하는 프리티어 EC2는 젠킨스를 버틸 수 없습니다. 젠킨스를 도커로 띄우고 설정까지는 문제가 없으나 깃허브 웹훅으로 젠킨스를 이용해 spring을 빌드하는 과정에서 램을 1기가 이상(프리티어 EC2 램은 1기가) 사용하게 되면서 EC2가 먹통이 되어버립니다. 이 문제의 해결 방안으로는 리눅스의 하드디스크를 가상 메모리로 전환시켜 사용할 수 있다는 점을 이용하면 됩니다.  
AWS에서는 메모리의 양에 따라 스왑 메모리의 크기를 아래와 같이 권장하고 있습니다.  
<br>

![그림23](https://github.com/backtony/blog-code/blob/master/aws/img/1/1-23.PNG?raw=true)
<Br>

__1. 스왑 파일 생성하기__  
아래 명령은 시간이 약 10~20초 정도 걸리므로 여유있게 기다리면 됩니다.
```
sudo dd if=/dev/zero of=/swapfile bs=128M count=16
```
+ dd 멸령을 사용하여 루트 파일 시스템에 스왑 파일을 생성합니다.
+ 명령에서 dd는 블록 크기이고 count는 블록 수 입니다.
+ 지정한 블록 크기는 인스턴스에서 사용 가능한 메모리보다 작아야합니다. 그렇지 않으면 memory exhauted 오류가 발생합니다.
+ 프리티어의 메모리는 1GB이므로 권장사항대로 2GB를 증설시켜야 합니다.
+ 현재 위 코드대로라면 2GB(128MB * 16 = 2,048MB) 입니다.

<br>

__2. 스왑 파일에 대한 일기 쓰기 권한 업데이트하기__
```
sudo chmod 600 /swapfile
```
<br>

__3. Linux 스왑 영역 설정하기__
```
sudo mkswap /swapfile
```
<Br>

__4. 스왑 공간에 스왑 파일을 추가하여 스왑 파일을 즉시 사용할 수 있도록 하기__
```
sudo swapon /swapfile
```
<br>

__5. 절차가 성공했는지 확인하기__
```
sudo swapon -s
```
<Br>

__6. /etc/fstab 파일을 편집하여 부팅 시 스왑 파일을 활성화하기__
```
파일 열기
sudo vi /etc/fstab

파일 가장 마지막에 다음을 추가하고 :wq로 저장하고 종료
/swapfile swap swap defaults 0 0
```
<br>

__7. free 명령어로 메모리 확인하기__
```
free
```
아래 사진을 보시면 Swap이 잘 된 것을 확인할 수 있습니다.
![그림24](https://github.com/backtony/blog-code/blob/master/aws/img/1/1-24.PNG?raw=true)
<Br>

## 도커 세팅
운영용 EC2와 마찬가지로 도커를 설치합니다.
```
운영 EC2 접속
ssh 호스트명

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
```
<Br>

현재 시나리오는 Docker을 이용해서 Jenkins를 띄우고 Jenkins안에서 Dockerfile을 빌드하여 DockerHub로 Push하는 시나리오입니다. 따라서 도커로 띄운 Jenkins 컨테이너를 안에서 도커 명령어가 사용이 가능해야 합니다. 즉, 도커위에서 도커를 사용해야 합니다. 이에 대해서는 2가지 방식이 있습니다. DinD(Docker in Docker)방식과 DooD(Docker Out of Docker)방식이 있습니다. Docker 측에서는 도커 안에 도커를 띄우는 것을 권장하진 않지만 그나마 DooD방식을 권장하고 있으며, 이에 대해서는 [[링크](https://aidanbae.github.io/code/docker/dinddood/){:target="_blank"}]를 참조하시면 도움이 될 것 같습니다. 결론적으로 DooD 방식으로 도커위에 도커를 띄우기 위해서는 -v 옵션으로 호스트의 docker socket을 빌려서 사용하면 됩니다. 그럼 이제 Dockerfile과 docker_install.sh 파일을 작성해봅시다.
```
Dockerfile 생성
sudo vim Dockerfile

Dockerfile 작성 시작
FROM jenkins/jenkins:jdk11

#도커를 실행하기 위한 root 계정으로 전환
USER root

#도커 설치
COPY docker_install.sh /docker_install.sh
RUN chmod +x /docker_install.sh
RUN /docker_install.sh

# 도커 그룹에 사용자 추가
RUN usermod -aG docker jenkins
USER jenkins
```
<Br>

__cf) docker.sock__  
docker.sock은 Docker 서버 측 데몬인 dockerd가 REST API를 통해 명령을 줄 인터페이스와 통신할 수 있도록 하는 Unix 소켓입니다. 컨테이너 내부에서 데몬과 상호작용 할 수 있게 도와주는 것이라고 생각하면 됩니다. 소켓은 소프트웨어 사이에 데이터를 전달하는 네트워크 엔드 포인트입니다. 
<br><br>

위에 작성한 Dockerfile을 읽어보시면 도커를 설치하는 코드가 있습니다. docker_install.sh 파일은 도커 파일 내에 도커를 설치하기 위한 파일이라고 보시면 됩니다. 어렵게 생각하지 말고 그냥 도커내에 도커를 설치하는 코드를 작성한 파일이구나 라고 생각하시면 편합니다. docker_install.sh를 작성해봅시다. 
```
docker_install.sh 파일 생성
sudo vim docker_install.sh

docker_install.sh 파일 작성 시작
#!/bin/sh
apt-get update && \
apt-get -y install apt-transport-https \
  ca-certificates \
  curl \
  gnupg2 \
  zip \
  unzip \
  software-properties-common && \
curl -fsSL https://download.docker.com/linux/$(. /etc/os-release; echo "$ID")/gpg > /tmp/dkey; apt-key add /tmp/dkey && \
add-apt-repository \
"deb [arch=amd64] https://download.docker.com/linux/$(. /etc/os-release; echo "$ID") \
$(lsb_release -cs) \
stable" && \
apt-get update && \
apt-get -y install docker-ce
```
<br>

두 파일을 같은 위치에 두고 빌드하기 전에 해야할 작업이 있습니다. 앞서 도커안에 있는 도커는 host의 docker.sock을 빌려서 사용한다고 했습니다. 따라서 docker.sock 파일의 권한을 변경하여 그룹 내 다른 사용자도 접근 가능하도록 변경해줘야 합니다. 다음 명령어로 접근 가능하도록 세팅해줍니다.
```
sudo chmod 666 /var/run/docker.sock
```
<br>

이제 Dockerfile을 빌드하여 이미지를 생성합니다.
```
docker build -t jenkins .
```
-t 는 태그를 주는 옵션이고 . 은 현재 위치에 있는 도커파일을 가리킵니다.
<br><br>

만들어진 이미지를 사용해서 젠킨스를 띄우기 전에 -v 옵션으로 볼륨 마운트할 폴더를 만들어야 합니다. 폴더를 만들어서 볼륨 마운트해주는 이유는 간단합니다. 볼륨 마운트를 안해줄 경우 젠킨스 컨테이너가 죽어버리면 모든 데이터가 사라집니다. 하지만 볼륨 마운트를 사용하여 젠킨스 컨테이너의 데이터와 EC2 폴더를 연동시켜놓는다면 젠킨스 컨테이너가 죽어도 데이터는 EC2에 마운트 시켜놓은 폴더에 저장되어 있기 때문에 데이터의 유실을 막을 수 있습니다.
```
jenkins 폴더 만들기
mkdir jenkins

해당 폴더에 대해 권한 부여하기
sudo chown -R 1000 ./jenkins
```
<br>

이제 젠킨스 컨테이너를 띄워봅시다.
```
sudo docker run -d --name jenkins \
-v /home/ec2-user/jenkins:/var/jenkins_home \
-v /var/run/docker.sock:/var/run/docker.sock \
-p 8080:8080 \
-e TZ=Asia/Seoul \
jenkins
```
+ -d : 벡그라운드로 띄우기
+ -v : 볼륨 마운트
+ -p : 포트 설정
+ -e : 환경변수 설정 옵션으로 한국시간으로 설정하는 옵션을 주었습니다.

<br>

# 7. 젠킨스 세팅
## 접속 및 계정 세팅
__앞서 젠킨스를 도커로 띄웠으니 웹에서 Jenkins에 접속해봅시다.__
```
웹에서 아래 url 입력
탄력적IP:8080

예시
http://3.38.22.133:8080/
```
<br>

__비밀번호를 입력하라는 아래와 같은 화면이 나옵니다.__
![그림25](https://github.com/backtony/blog-code/blob/master/aws/img/1/1-25.PNG?raw=true)

```
패스워드를 확인하기 위해 해당 컨테이너로 진입
sudo docker exec -it jenkins bash

패스워드 출력
cat /var/jenkins_home/secrets/initialAdminPassword
```
출력된 패스워드를 입력합니다. 
<br><br><br>

__install suggested plugins를 클릭하고 계정 생성을 진행합니다.__
![그림26](https://github.com/backtony/blog-code/blob/master/aws/img/1/1-26.PNG?raw=true)

<Br><Br>

## 플러그인 세팅

__메인 페이지의 왼쪽 메뉴바에서 Jenkins 관리를 클릭합니다.__
![그림75](https://github.com/backtony/blog-code/blob/master/aws/img/1/1-75.PNG?raw=true)

<br><br>

__System Configuration에서 플러그인 관리를 클릭합니다.__
![그림28](https://github.com/backtony/blog-code/blob/master/aws/img/1/1-28.PNG?raw=true)

<br><br>

__설치가능탭을 클릭하시고 아래의 플러그인들을 설치합니다.__
![그림29](https://github.com/backtony/blog-code/blob/master/aws/img/1/1-29.PNG?raw=true)
```
설치할 플러그인 목록 
gradle
github integration
post build task
publish over ssh
```
왼쪽 하단의 install without restart 클릭하여 설치하시면 되고, 만약 설치가능 탭에 해당 플러그인이 없다면 이미 플러그인이 설치되어 있을 것입니다. 상단 탭에서 설치된 플러그인 목록에서 검색해보시면 확인할 수 있습니다.  
+ gradle : spring boot의 gradle을 사용하기 위한 플러그인
+ github integration : github의 webhook을 사용하기 위한 플러그인
+ post build task : 빌드 로그를 판단하여' script 혹은 shell을 실행할 수 있게 하는 플러그인
+ publish over ssh : 다른 EC2에 접속하여 작업을 가능하게 해주는 플러그인


<br><br>

## Credentials 세팅
__다시 메인페이지로 돌아와서 Jenkins 관리를 클릭합니다__
![그림75](https://github.com/backtony/blog-code/blob/master/aws/img/1/1-75.PNG?raw=true)

<br><Br>

__Security에 Manage Credentials를 클릭합니다.__
![그림30](https://github.com/backtony/blog-code/blob/master/aws/img/1/1-30.PNG?raw=true)
여기는 Jenkins에서 사용할 계정정보들을 등록하는 곳입니다. 저희는 깃허브와 연동이 필요하므로 깃허브 계정정보가 필요하고, DockerHub의 private 저장소에 이미지를 push할 것이므로 DockerHub의 계정 정보가 필요합니다.

<br><Br>

__global 옆의 드롭박스를 클릭하고 Add credentials를 클릭합니다.__
![그림31](https://github.com/backtony/blog-code/blob/master/aws/img/1/1-31.PNG?raw=true)

<br><Br>

__깃허브 계정정보를 입력합니다. Id는 젠킨스에서 식별하는 고유 Id값으로 적지 않아도 됩니다. 이 과정과 동일하게 DockerHub 계정도 등록해줍니다.__  
![그림32](https://github.com/backtony/blog-code/blob/master/aws/img/1/1-32.PNG?raw=true)

<br><Br>

## Github webhook 연동
Jenkins와 Github을 연동하는 작업을 하겠습니다.  
<br>

__자신의 깃허브 계정에 Settings를 클릭합니다.__  
![그림33](https://github.com/backtony/blog-code/blob/master/aws/img/1/1-33.PNG?raw=true)

<br><Br>

__왼쪽 하단에 Developer settings를 클릭합니다.__  
![그림76](https://github.com/backtony/blog-code/blob/master/aws/img/1/1-76.PNG?raw=true)

<br><Br>

__왼쪽 탭에서 Personal access tokens를 클릭합니다.__
![그림77](https://github.com/backtony/blog-code/blob/master/aws/img/1/1-77.PNG?raw=true)

<br><Br>

__오른쪽 상단에 Generate new token을 클릭합니다.__
![그림36](https://github.com/backtony/blog-code/blob/master/aws/img/1/1-36.PNG?raw=true)

<br><Br>

__토큰 이름을 정해주고 repo, admin, admin:repo_hook을 선택하고 하단에 Generate token을 클릭합니다.__
![그림37](https://github.com/backtony/blog-code/blob/master/aws/img/1/1-37.PNG?raw=true)

<br><Br>

__토큰이 발급된 것을 확인할 수 있습니다. 토큰은 만든 당시에만 확인이 가능하니 잘 보관해둡니다.__
![그림38](https://github.com/backtony/blog-code/blob/master/aws/img/1/1-38.PNG?raw=true)

<br><Br>

__Jenkins와 web hook으로 연결하고 싶은 프로젝트로 들어가서 오른쪽에 Settings를 클릭합니다.__
![그림78](https://github.com/backtony/blog-code/blob/master/aws/img/1/1-78.PNG?raw=true)

<br><Br>

__왼쪽 탭에서 Webhooks를 클릭합니다.__  
![그림79](https://github.com/backtony/blog-code/blob/master/aws/img/1/1-79.PNG?raw=true)

<br><Br>

__Payload URL을 작성하고 하단의 Add webhook을 클릭합니다. Payload URL은 http://JenkinsEC2의탄력적IP/github-webhook/ 형식으로 작성하시면 됩니다.__
![그림67](https://github.com/backtony/blog-code/blob/master/aws/img/1/1-67.PNG?raw=true)


<br><Br>

__다시 젠킨스 화면으로 돌아와서 젠킨스 관리 -> 시스템 설정을 클릭합니다.__  
![그림39](https://github.com/backtony/blog-code/blob/master/aws/img/1/1-39.PNG?raw=true)

<br><Br>

__아래로 내리다보면 GitHub를 찾으실 수 있고, Add GitHub Server을 클릭합니다. Name에는 식별하기 편한 이름을 적어주시고 API URL은 그대로 두시면 됩니다. 하단에 Credentials에 Add를 클릭합니다.__  
![그림40](https://github.com/backtony/blog-code/blob/master/aws/img/1/1-40.PNG?raw=true)

<br><Br>

__kind에서 Secrete text를 선택하고 Secret에는 Github에서 발급받은 토큰을 입력하고 왼쪽 하단에 Add를 클릭합니다.__  
![그림41](https://github.com/backtony/blog-code/blob/master/aws/img/1/1-41.PNG?raw=true)

<br><Br>

__만든 Credentials를 선택하고 오른쪽에 Test connection을 클릭해서 연결을 확인한 뒤에 화면 왼쪽 하단에 Apply를 클릭합니다.__
![그림80](https://github.com/backtony/blog-code/blob/master/aws/img/1/1-80.PNG?raw=true)

Jenkins에서 Github을 연동하는 작업은 끝났으나 아직 정상적으로 작동하지 않습니다. 이유는 Jenkins EC2에서는 Github에서 webhook으로 보내는 요청의 IP를 허용해주지 않았기 때문입니다. 따라서 web hook으로 Jenkins EC2로 들어오는 IP를 AWS Jenkins EC2의 보안 그룹에서 포트를 열어줘야합니다. 이 작업은 SSH Servers 세팅을 하는 과정에서 같이 진행하도록 하겠습니다.
<br><Br>


## SSH Servers 세팅
__방금 화면에서 그대로 맨 아래로 내려오면 SSH Server탭을 확인할 수 있고 추가를 눌러서 작성합니다.__
![그림43](https://github.com/backtony/blog-code/blob/master/aws/img/1/1-43.PNG?raw=true)

+ Name : Job에 표시될 이름
+ Hostname : IP Address
  - 운영 EC2의 탄력적 IP를 작성하시면 됩니다.
+ Username : ssh 접근 계정
+ Remote Directory : 업로드될 디렉토리

<br><Br>

__오른쪽 하단에 고급을 클릭하고 Use password authentication, or user a different key를 클릭하고 Key를 작성합니다. 여기서 Key는 운영용 EC2로 접속할 때 사용하는 Pem키를 말합니다.__
![그림44](https://github.com/backtony/blog-code/blob/master/aws/img/1/1-44.PNG?raw=true)
```
Key 확인 방법
cat 키위치

예시
cat ~/.ssh/awsstudy.pem
```
출력된 키 전체를 복사해서 Key에 붙여넣어주면 됩니다.  
이 접근 과정은 Jenkins EC2가 ssh키(pem키)를 가지고 운영용 EC2에 접근하는 과정입니다. 따라서 운영용 EC2의 보안 그룹에서는 Jenkins의 ssh 접근을 허용해줘야 합니다. 현재 페이지는 그대로 나두고 포트를 열어주는 작업은 다른 웹페이지를 키고 진행하겠습니다.

<br><br>

__AWS홈페이지에서 로그인한 뒤 EC2를 검색하여 인스턴스 페이지로 들어옵니다. 운영용 ec2 인스턴스 ID를 클릭합니다. 저는 deploy를 선택하겠습니다.__  
![그림45](https://github.com/backtony/blog-code/blob/master/aws/img/1/1-45.PNG?raw=true)
<br><br>

__하단의 보안 탭을 클릭합니다.__  
![그림46](https://github.com/backtony/blog-code/blob/master/aws/img/1/1-46.PNG?raw=true)
<br><br>

__보안 그룹을 선택합니다.__  
![그림47](https://github.com/backtony/blog-code/blob/master/aws/img/1/1-47.PNG?raw=true)
<br><br>

__하단의 Inbound rules 탭에서 오른쪽에 Edit Inbound reulses을 선택합니다.__  
![그림48](https://github.com/backtony/blog-code/blob/master/aws/img/1/1-48.PNG?raw=true)
<br><br>

__규칙 추가를 클릭하시고 SSH를 선택합니다. 오른쪽에 Jenkin EC2의 탄력적 IP를 작성해주시고 저장합니다.__  
![그림49](https://github.com/backtony/blog-code/blob/master/aws/img/1/1-49.PNG?raw=true)
<br><br>


__이번엔 Github의 web hook IP 허용 작업을 하겠습니다. 다시 인스턴스 페이지로 돌아와서 이번엔 jenkins 인스턴스 ID를 클릭합니다.__  
![그림45](https://github.com/backtony/blog-code/blob/master/aws/img/1/1-45.PNG?raw=true)

<br><Br>

__앞에서한 과정과 마찬가지로 보안그룹에 진입하여 아래의 IP를 8080포트로 열어줍니다.__
![그림50](https://github.com/backtony/blog-code/blob/master/aws/img/1/1-50.PNG?raw=true)
```
192.30.252.0/22
185.199.108.0/22
140.82.112.0/20
```
<br><br>

__모든 포트 설정을 끝냈으니 좀전에 작성하던 Jenkins ssh 설정 페이지로 돌아옵니다. Test Configuration을 클릭하여 연결을 확인하고 하단에 저장버튼클 클릭합니다.__
![그림51](https://github.com/backtony/blog-code/blob/master/aws/img/1/1-51.PNG?raw=true)

<br><Br>

## Gradle 세팅
빌드에서 사용할 Gradle 버전을 세팅해줘야합니다.
<br><br>

__메인페이지 -> Jenkins관리 -> Global Tool Configuration 를 클릭합니다.__
![그림55](https://github.com/backtony/blog-code/blob/master/aws/img/1/1-55.PNG?raw=true)
<br><br>

__아래쪽에 스크롤을 내리면 Gradle 설정하는 탭이 있습니다. 사용하는 프로젝트의 Gradle 버전을 선택하고 추후에 알기 쉬운 이름을 정해서 저장해줍니다.__
![그림56](https://github.com/backtony/blog-code/blob/master/aws/img/1/1-56.PNG?raw=true)
<br><br>

## 프로젝트 세팅
이제 환경세팅은 끝냈고 본격적으로 Jenkins에서 빌드, 배포하는 작업을 세팅해야 합니다.  
<br>

__메인 페이지 왼쪽에 새로운 Item을 클릭합니다.__  
![그림27](https://github.com/backtony/blog-code/blob/master/aws/img/1/1-27.PNG?raw=true)

<br><Br>

__프로젝트명을 지정해주시고 Freestyle project를 선택합니다.__
![그림52](https://github.com/backtony/blog-code/blob/master/aws/img/1/1-52.PNG?raw=true)

<br><Br>

__GitHub project를 선택하고 project url에 본인의 프로젝트 주소를 입력합니다.__
![그림53](https://github.com/backtony/blog-code/blob/master/aws/img/1/1-53.PNG?raw=true)

<br><Br>

__소스 코드 관리에서 Git을 선택하고 Repository URL에 깃허브프로젝트url.git 형태로 입력합니다. Credentials에는 전에 만들었던 GitHub 계정의 Credentials을 선택하시고 아래쪽에는 빌드할 브랜치를 입력하시면 됩니다.__
![그림54](https://github.com/backtony/blog-code/blob/master/aws/img/1/1-54.PNG?raw=true)

<br><Br>

__빌드 유발에 GitHub hook trigger for GITscm polling을 클릭합니다.__
![그림57](https://github.com/backtony/blog-code/blob/master/aws/img/1/1-57.PNG?raw=true)

<br><Br>

__빌드 환경에서 Use secret text or file을 선택하고 Bindings의 Add 드롭박스를 클릭하고 Username and password (separated)를 선택합니다.__
![그림58](https://github.com/backtony/blog-code/blob/master/aws/img/1/1-58.PNG?raw=true)

<Br><br>

__각각 Variable에 Username으로 사용할 변수명, password로 사용할 변수명을 지정해주고 Credentials에 specific credentials을 선택하고 전에 만들어놓았던 dockerhub 계정을 선택합니다.__
![그림59](https://github.com/backtony/blog-code/blob/master/aws/img/1/1-59.PNG?raw=true)

<Br><Br>

__Build에서 Invoke Gradle script 를 클릭합니다.__  
![그림63](https://github.com/backtony/blog-code/blob/master/aws/img/1/1-63.PNG?raw=true)

<Br><Br>

__Gradle Version에서 전에 세팅해줬던 gradle 선택해주고 Tasks에는 빌드 명령어를 적어줍니다. clean build는 빌드와 테스트를 같이 수행해줍니다. 분명 CI 환경에서는 다른 profile로 빌드해야할 때가 있는데 그때는 build 뒤에 -Pprofile=프로파일명 을 사용합니다.__
![그림61](https://github.com/backtony/blog-code/blob/master/aws/img/1/1-61.PNG?raw=true)

<Br><Br>

__Add build step에서 Execute shell을 선택합니다.__  
![그림62](https://github.com/backtony/blog-code/blob/master/aws/img/1/1-62.PNG?raw=true)

<Br><Br>

__빌드를 진행한 후 동작할 명령어들을 입력합니다.__
![그림64](https://github.com/backtony/blog-code/blob/master/aws/img/1/1-64.PNG?raw=true)
```
도커파일에 -t로 태그명을 주고 이미지를 빌드합니다.
docker build -t backtony/awsstudy:1.0 .

도커 로그인을 시도합니다. $변수명 을 이용하면 위에서 작성한 변수명으로 값을 사용할 수 있습니다.
echo $PASSWORD | docker login -u $USERNAME --password-stdin

도커허브에 빌드된 이미지를 푸시합니다.
docker push backtony/awsstudy:1.0

푸시한 이미지를 삭제합니다.
docker rmi backtony/awsstudy:1.0

참고사항 
docker build -t <dockerUserName>/<repository>:<tag> .
docker push <dockerUserName>/<repository>
```

[[링크](https://hub.docker.com/){:target="_blank"}] 에 접속하여 도커허브에 private Repository를 생성합니다. 여기까지가 코드를 빌드, 테스트하고 이미지를 빌드하여 도커허브에 푸시하는 작업입니다. 다음 작업은 Jenkins EC2에서 운영용 EC2로 접속하여 수행하는 작업입니다.

<Br><Br>

__빌드 후 조치를 선택하고 send build artifacts over SSH를 선택합니다.__  
![그림65](https://github.com/backtony/blog-code/blob/master/aws/img/1/1-65.PNG?raw=true)
<br><br>

__빌드 후 수행할 동작을 작성해줍니다.__
![그림81](https://github.com/backtony/blog-code/blob/master/aws/img/1/1-81.PNG?raw=true)

+ Name : 전에 세팅했던 ssh 서버를 선택합니다.
+ Source files : 운영용 EC2로 보낼 파일의 위치를 작성합니다.
+ Remove prefix : 파일 앞부분의 경로를 적어줍니다.
  - scripts/deploy.sh 파일의 scripts/를 제거하고 deploy.sh만 복사시켜 줍니다.
+ Remote directory : 운영용 EC2에 업로드될 경로를 작성합니다.
+ Exec command : 실행할 명령어를 작성합니다.

```
운영용 EC2에서 도커에 로그인을 합니다.
echo $PASSWORD | docker login -u $USERNAME --password-stdin

deploy.sh 파일을 실행합니다.
sh deploy.sh
```
참고로 Name 바로 아래 고급을 클릭하고 Verbos output in console을 체크해두면 빌드 후 조치 과정에서 발생하는 output 도 콘솔에 찍힙니다. 

<br><br>

__최종적으로 작업이 끝난 후 workspace를 비우도록 합니다.__  
![그림82](https://github.com/backtony/blog-code/blob/master/aws/img/1/1-82.PNG?raw=true)

하단에 저장을 클릭합니다. 모든 세팅을 끝냈습니다. 이제 github에 코드를 푸시하시면 자동으로 빌드되고 배포까지 성공하게 됩니다.

<br><br>

# 8. 수동으로 동작시켜보기
---
__메인 페이지로 이동해보시면 방금 만든 프로젝트가 보일 것입니다. 클릭해줍니다.__
![그림69](https://github.com/backtony/blog-code/blob/master/aws/img/1/1-69.PNG?raw=true)

<br><Br>

__왼쪽 탭에서 Build Now를 클릭하시면 왼쪽 하단 Build History에 동작하고 있는 것을 확인할 수 있습니다.__
![그림70](https://github.com/backtony/blog-code/blob/master/aws/img/1/1-70.PNG?raw=true)
<br><Br>

__동작하고 있는 빌드를 클릭하신 후 왼쪽 탭에서 Console Output을 클릭하시면 콘솔 출력을 확인할 수 있습니다.__
![그림71](https://github.com/backtony/blog-code/blob/master/aws/img/1/1-71.PNG?raw=true)
<br><Br>

# 9. 빌드실패 시 이메일 받기
---
구글 계정으로 진행하겠습니다. 빌드 실패시 이메일로 결과를 받기 위해서는 우선적으로 구글 계정 세팅이 필요합니다. 구글 로그인 한 다음 설정 -> 보안에서 수준이 낮은 앱의 액세스 허용하고 2차 비밀번호를 설정하시면 됩니다. 2차비밀번호는 Jenkins 설정에서 사용합니다. 참고로 구글 SMTP 서비스는 하루 100개의 메일을 무료로 제공합니다.
<br><br>

__메인페이지에서 젠킨스 관리 -> 시스템 설정을 클릭합니다.__  
![그림39](https://github.com/backtony/blog-code/blob/master/aws/img/1/1-39.PNG?raw=true)

<br><Br>

__아래로 내리다보면 E-mail로 알려줌이 있습니다. 각 빈칸들을 다음과 같이 채워줍니다.__  
![그림83](https://github.com/backtony/blog-code/blob/master/aws/img/1/1-83.PNG?raw=true)

+ SMTP 서버 : smtp.gmail.com 
+ Use SMTP Authentication 체크
  - 사용자명 : 구글 계정
  - 비밀번호 : 2차 비밀번호
+ SSL 사용 체크
+ SMPT Port : 465
+ Test configuration by sending test e-mail 체크하고 자신의 이메일을 작성한 후 오른쪽 Test Configuration을 클릭하여 이메일이 오는지 테스트

<br><br>

__해당 프로젝트의 구성으로 들어와 빌드 후 조치 추가에서 E-mail Notification을 클릭합니다.__
![그림84](https://github.com/backtony/blog-code/blob/master/aws/img/1/1-84.PNG?raw=true)

<br><Br>

__메일을 받을 사람들의 이메일을 작성합니다. 쉼표로 구분할 수 있습니다.__
![그림85](https://github.com/backtony/blog-code/blob/master/aws/img/1/1-85.PNG?raw=true)

<br>

아래 설명을 읽어보시면 아시겠지만 빌드가 실패할 경우, unstable 상태일 경우에 이메일을 보내줍니다. 또한 해당 상태에서 정상적으로 돌아온 경우에도 메일을 보내줍니다.

<br><br>

# cf) Docker에서 Spring 외부 설정 파일 주입하기
---
이 부분은 저의 시행 착오 과정에서 공부한 내용입니다. 지금 생각해보면 private repository를 사용한다면 굳이 할 필요 없었던 작업이었습니다.  
application.yml에 중요한 내용이 들어있다고 가정하여 application.yml을 github에 올리지 않고, EC2에 옮겨 놓은 뒤 볼륨 마운트를 이용해서 외부 설정 파일을 사용하는 방식입니다.  
<Br>

__Dockerfile__
```dockerfile
# JDK11 이미지 사용
FROM openjdk:11-jdk

VOLUME /tmp

# JAR_FILE 변수에 값을 저장
ARG JAR_FILE=./build/libs/awsstudy-1.0.jar

# 변수에 저장된 것을 컨테이너 실행시 이름을 app.jar파일로 변경하여 컨테이너에 저장
COPY ${JAR_FILE} app.jar

# 빌드된 이미지가 run될 때 실행할 명령어
ENTRYPOINT ["java","-jar","app.jar", \
"--spring.config.location=/config/application.yml"]
```

+ ENTRYPOINT에 spring.config.location을 사용한 이유
  - 현재 시나리오는 application.yml에 key정보가 들어가있는 상태라 GitHub에 올릴 수 없습니다. 하지만 Jenkins에서 빌드와 테스트를 하는 과정에서 spring 프로젝트가 돌아가기 위해서는 application.yml이 필요합니다. 즉, Github에 올라갈 application.yml은 빌드할 때 필요한 정보만 들어가있는(Key정보들이 없는) 껍데기 application.yml입니다. 그렇다면 당연히 운영 EC2에서 spring을 띄울 때는 다른 application.yml을 사용해야합니다. 이를 위해 운영 EC2에 접속하여 실제로 사용해야할 운영용 application.yml 파일을 작성한뒤 spring 프로젝트를 띄울 때 운영용 application.yml을 사용하도록 설정해주어야 합니다. 
  - spring.config.location는 환경 프로퍼티로 특정 위치를 참조할 수 있도록 하는 옵션이라 위의 상황에서 적절하게 사용할 수 있습니다. 여기서 주의해야할 점은 경로입니다. 원하는 경로를 만들어서 넣으시면 안되고 규칙에 맞게 경로를 지정해야 합니다. 자세한 내용은 [[링크](https://www.latera.kr/reference/java/2019-09-29-spring-boot-config-externalize/){:target="_blank"}]를 확인하시면 도움이 될 것 같습니다. 결론은 /config 디렉토리로 지정해야 합니다.


<br>

__deploy.sh__
```sh
# 가동중인 awsstudy 도커 중단 및 삭제
sudo docker ps -a -q --filter "name=awsstudy" | grep -q . && docker stop awsstudy && docker rm awsstudy | true

# 기존 이미지 삭제
sudo docker rmi backtony/awsstudy:1.0

# 도커허브 이미지 pull
sudo docker pull backtony/awsstudy:1.0

# 도커 run
docker run -d -p 8080:8080 -v /home/ec2-user:/config --name awsstudy backtony/awsstudy:1.0

# 사용하지 않는 불필요한 이미지 삭제 -> 현재 컨테이너가 물고 있는 이미지는 삭제 안됨
docker rmi -f $(docker images -f "dangling=true" -q) || true
```
도커 run 부분에는 위에서 언급했던 application.yml을 연결시키기 위해 EC2의 /home/ec2-user 디렉토리와 도커컨테이너의 /config 디렉토리를 볼륨 마운트 시켜줬습니다. __그리고 운영용 EC2의 /home/ec2-user/ 위치에 운영용 application.yml을 작성해두었습니다.__ 


<br><br>


__여기까지 글을 마치겠습니다. 감사합니다 :)__
