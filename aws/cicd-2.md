# Spring & TravisCI & CodeDeploy & Nginx 활용한 CI/CD

# 최종적인 구조

![그림39](https://github.com/backtony/blog-code/blob/master/aws/img/2/2-39.PNG?raw=true) 

# 1. EC2 만들기
---

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

# 2. EC2 초기 세팅하기
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
![그림19](https://github.com/backtony/blog-code/blob/master/aws/img/1/1-19.PNG?raw=true)

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


## EC2 시간 변경
EC2 서버의 기본 타임존은 UTC입니다. 한국시간으로 변경시켜주겠습니다.
```
sudo rm /etc/localtime
sudo ln -s /usr/share/zoneinfo/Asia/Seoul /etc/localtime
```
위 명령어를 입력한 뒤에 date를 입력하면 시간이 변경된 것을 확인할 수 있습니다.


## Java 11 설치하기
```
자바 설치
sudo amazon-linux-extras install java-openjdk11

자바 설치 확인 
java -version
```
자바를 설치하고 설치가 잘 되었는지 버전을 확인해줍니다.


<br><br>

# 3. RDS 인스턴스 세팅하기
---
## MySQL RDS 인스턴스 만들기
![그림1](https://github.com/backtony/blog-code/blob/master/aws/img/2/2-1.PNG?raw=true)  

AWS 홈페이지에서 RDS를 검색하시고 데이터베이스 생성을 클릭합니다.

<br><br>

![그림2](https://github.com/backtony/blog-code/blob/master/aws/img/2/2-2.PNG?raw=true)  

MySQL을 선택하고 하단 템플릿에서 프리 티어를 선택합니다.

<br><Br>

![그림3](https://github.com/backtony/blog-code/blob/master/aws/img/2/2-3.PNG?raw=true)  
인스턴스 식별자에 DB 인스턴스 이름을 정해주시고, DB에 접속 계정 정보를 작성해줍니다.

<br><Br>

![그림4](https://github.com/backtony/blog-code/blob/master/aws/img/2/2-4.PNG?raw=true)  
스토리지는 20 으로 설정해주시고 연습용으로 만드는 것이니 자동 조정 활성화는 해제해줍니다.

<br><Br>

![그림5](https://github.com/backtony/blog-code/blob/master/aws/img/2/2-5.PNG?raw=true)  
네트워크에선 퍼블릭 액세스를 예로 변경합니다. 이후 보안 그룹에서 지정된 IP만 접근 가능하도록 막을 예정입니다.

<br><Br>

![그림6](https://github.com/backtony/blog-code/blob/master/aws/img/2/2-6.PNG?raw=true)  
초기 DB 명을 입력해주고 나머지 옵션은 그대로 둔 채 데이터베이스를 생성합니다.

<br>

## 운영환경에 맞는 파라미터 설정하기
RDS를 처음 생성하면 필수적으로 몇 가지 설정을 진행해야 합니다.

![그림7](https://github.com/backtony/blog-code/blob/master/aws/img/2/2-7.PNG?raw=true)  
왼쪽 메뉴 탭에서 파라미터 그룹을 클릭하고 오른쪽 상단에 파라미터 그룹 생성을 클릭합니다.

<br><br>

![그림8](https://github.com/backtony/blog-code/blob/master/aws/img/2/2-8.PNG?raw=true)  
mysql 8.0을 선택해주시고 그룹 이름은 알기 쉬운 것으로 정해서 생성해줍니다.

<br><br>

![그림9](https://github.com/backtony/blog-code/blob/master/aws/img/2/2-9.PNG?raw=true)  
생성된 파라미터 그룹의 이름을 클릭하면 해당 페이지가 나오게 되고 오른쪽에 수정을 클릭합니다.

<br><br>

![그림10](https://github.com/backtony/blog-code/blob/master/aws/img/2/2-10.PNG?raw=true)  
+ time_zone : Asia/Seoul
+ character_set : utf8mb4
+ collation : utf8mb4_general_ci
+ max_connections : 150

utf8과 utf8mb4의 차이는 이모지 저장의 차이입니다. RDS의 Max Connection은 인스턴스 사양에 따라 자동으로 정해지나 프르티어의 사양으로는 약 60개의 커넥션만 가능해서 넉넉하게 잡아주고 변경사항을 적용해 줍니다.

<br><br>

![그림11](https://github.com/backtony/blog-code/blob/master/aws/img/2/2-11.PNG?raw=true)  
다시 데이터베이스 탭으로 돌아와서 전에 만든 DB를 선택하고 상단에 수정을 클릭합니다.

<br><br>

![그림12](https://github.com/backtony/blog-code/blob/master/aws/img/2/2-12.PNG?raw=true)  
DB 파라미터 그룹을 방금 만든 파라미터 그룹으로 변경해 줍니다.

<br><br>

![그림13](https://github.com/backtony/blog-code/blob/master/aws/img/2/2-13.PNG?raw=true)  
즉시 적용을 선택하고 수정을 완료합니다.

<br><br>

![그림14](https://github.com/backtony/blog-code/blob/master/aws/img/2/2-14.PNG?raw=true)  
해당 DB를 선택한 뒤 작업 -> 재부팅을 진행해줍니다.

<br>


## EC2에 MySQL 설치하기
```
// 설치
sudo yum install mysql

// 접속
mysql -u 계정 -p -h host주소

// 예시
mysql -u backtony -p -h travis.cvowj9xkrrgz.ap-northeast-2.rds.amazonaws.com
```
host 주소는 만드신 데이터베이스를 클릭하면 엔드포인트로 적혀있습니다.  
이후 spring의 application.yml 에서 datasource url을 수정하시면 됩니다.
```yml
spring:
  datasource:
    url: jdbc:mysql://host주소:3306/db명
    driver-class-name: com.mysql.cj.jdbc.Driver
    username: 아이디
    password: 패스워드    
```

<br><Br>

# 4. Travis CI 연동하기
---
Travis CI는 깃허브에서 제공하는 무료 CI 서비스입니다. 젠킨스와 같은 CI툴도 있지만 설치형이기 때문에 이를 위한 인스턴스가 하나 더 필요합니다. 따라서 간단한 경우라면 Travis CI가 좋은 선택입니다.  

[https://www.travis-ci.com/](https://www.travis-ci.com/) 에서 깃허브 계정으로 로그인 한 뒤 오른쪽 상단에 계정명 -> settings를 클릭하고 Travis와 연동할 저장소 이름을 입력해서 찾은 다음 상태바 버튼을 활성화 시킵니다. 웹사이트 설정은 이걸로 끝입니다.


## AWS S3와 Travis CI 연동하기
S3란 AWS에서 제공하는 일종의 파일 서버입니다. 이미지 파일을 비롯한 정적 파일들을 관리하거나 배포 파일들을 관리하는 등의 기능을 지원합니다. 실제 배포는 AWS CodeDeploy라는 서비스를 이용하는데 S3 연동이 필요한 이유는 Jar 파일을 전달하기 위해서 입니다. CodeDeploy는 저장 기능이 없기 때문에 Travis CI가 빌드한 결과물을 받아서 CodeDeploy가 가져갈 수 있도록 보관할 수 있는 공간이 필요합니다. 보통 이때 AWS S3를 사용합니다.  

### AWS Key 발급
일반적으로 AWS 서비스에 외부 서비스가 접근할 수 없습니다. 그러므로 접근 가능한 권한을 가진 Key를 생성해서 사용해야 합니다. AWS에서는 이러한 인증과 관련된 기능을 제공하는 서비스로 IAM이 있습니다. IAM은 AWS에서 제공하는 서비스의 접근 방식과 권한을 관리합니다. IAM을 통해 Travis CI가 AWS의 S3와 CodeDeploy에 접근할 수 있도록 해보겠습니다.  

![그림15](https://github.com/backtony/blog-code/blob/master/aws/img/2/2-15.PNG?raw=true)  

AWS 홈페이지에서 IAM을 검색하고 왼쪽 탭에서 사용자를 클릭한 뒤 오른쪽 상단에 사용자 추가를 클릭합니다.

<br><br>

![그림16](https://github.com/backtony/blog-code/blob/master/aws/img/2/2-16.PNG?raw=true)  

사용자 이름을 정하고 프로그래밍 방식 엑세스를 선택합니다.

<br><br>

![그림17](https://github.com/backtony/blog-code/blob/master/aws/img/2/2-17.PNG?raw=true)  

기존 정책 직접 연결을 클릭하고 AWSCodeDeployFullAccess와 AmazonS3FullAccess 를 추가합니다.

<br><br>

![그림18](https://github.com/backtony/blog-code/blob/master/aws/img/2/2-18.PNG?raw=true)  

태그로 나중에 파악하기 쉬운 이름을 넣어줍니다.

<br><br>

![그림19](https://github.com/backtony/blog-code/blob/master/aws/img/2/2-19.PNG?raw=true)  

여기서 나오는 액세스 키와 비밀 액세스 키는 추후에 알 수 없으니 잘 보관해 둡니다. 연동시에 사용됩니다.

<br>

### Travic CI에 키 등록
![그림20](https://github.com/backtony/blog-code/blob/master/aws/img/2/2-20.PNG?raw=true)  
Travis CI 홈페이지에서 해당 리포지토리를 클릭하고 오른쪽 상단에 More Options에 Setting을 클릭합니다. 그리고 환경 변수를 다음과 같이 추가해줍니다.
+ AWS_ACCESS_KEY : 엑세스 키ID
+ AWS_SECRET_KEY : 비밀 엑세스 키

여기에 등록된 값들은 .travis.yml 에서 $AWS_ACCESS_KEY 로 변수처럼 사용할 수 있습니다.

<br>

### S3 버킷 생성
S3에는 Travis CI에서 생성된 Build 파일을 저장하도록 구성하겠습니다. S3에 저장된 Build 파일은 이후 AWS의 CodeDeploy에서 배포할 파일로 가져가도록 구성할 것입니다.

![그림21](https://github.com/backtony/blog-code/blob/master/aws/img/2/2-21.PNG?raw=true)  
AWS 홈페이지에서 S3를 검색하고 버킷 만들기를 클릭합니다. 버킷 이름을 정해주시고 모든 퍼블릭 액세스를 차단한 뒤 생성해줍니다.

<Br>

## CodeDeploy 연동하기 
AWS의 배포 시스템인 CodeDeploy를 이용하기 전에 배포 대상인 EC2가 CodeDeploy를 연동 받을 수 있게 IAM 역할을 하나 생성해야 합니다. 
+ 역할 
    - AWS 서비스에만 할당할 수 있는 권한
    - EC2, CodeDeploy 등
+ 사용자
    - AWS 서비스 외에 사용할 수 있는 권한
    - 로컬 PC 등

### 역할 만들고 적용하기

![그림22](https://github.com/backtony/blog-code/blob/master/aws/img/2/2-22.PNG?raw=true)  
AWS 홈페이지에서 IAM을 검색하고 왼쪽 탭에서 역할을 클릭한 뒤 오른쪽 상단에 역할 만들기를 클릭합니다.

<Br><Br>

![그림23](https://github.com/backtony/blog-code/blob/master/aws/img/2/2-23.PNG?raw=true)  

일반 사용 사례에서 EC2를 선택합니다.

<Br><Br>

![그림24](https://github.com/backtony/blog-code/blob/master/aws/img/2/2-24.PNG?raw=true)  

AmazonEC2RoleforAWSCodeDeploy를 추가하고 다음으로 넘깁니다. 태그는 알기 쉬운 것으로 추가해주고 다음으로 넘깁니다. 역할 이름은 알기 쉬운것으로 준 뒤 역할을 만듭니다.

<Br><Br>

![그림25](https://github.com/backtony/blog-code/blob/master/aws/img/2/2-25.PNG?raw=true)  

이제 EC2에 만든 역할들 등록할 차례입니다. 해당 EC2의 보안에서 IAM 역할 수정을 클릭합니다.

<Br><Br>

![그림26](https://github.com/backtony/blog-code/blob/master/aws/img/2/2-26.PNG?raw=true)  

역할을 방금 만든 역할로 선택하고 저장해줍니다. 그리고 EC2를 재부팅 시켜줍니다.

<br>

### CodeDeploy 에이전트 설치하기
EC2에 접속하여 CodeDeploy의 요청을 받을 수 있도록 에이전트를 설치해주도록 합니다. EC2에 접속하여 다음 명령어를 입력합니다.

```
aws s3 cp s3://aws-codedeploy-ap-northeast-2/latest/install . --region ap-northeast-2

// 성공시 아래 메시지가 출력됩니다.
download: s3://aws-codedeploy-ap-northeast-2/latest/install to ./install

// 루비 설치
sudo yum install ruby

// install 파일 실행 권한 추가
chmod +x ./install

// install 파일 설치 진행
sudo ./install auto

// Agent 정상적으로 실행되고 있는지 검사
sudo service codedeploy-agent status

// 아래 메시지 출력시 정상
The AWS CodeDeploy agent is running as PID ...
```
<br>

### CodeDeploy를 위한 권한 생성
앞서 EC2가 CodeDeploy를 연동 받을 수 있도록 역할을 부여했습니다. 그럼 이번에는 CodeDeploy가 EC2에 접근하기 위한 권한을 세팅해줄 차례입니다. AWS 서비스이니 역할을 생성하면 됩니다. 

![그림27](https://github.com/backtony/blog-code/blob/master/aws/img/2/2-27.PNG?raw=true)  

앞서 만들었던 역할과 동일하게 진행하되 CodeDeploy를 선택하시고 하단 사용 사례 선택에서 CodeDeploy를 선택합니다. CodeDeploy는 권한 정책이 1개이기 때문에 그냥 쭉 넘기시고 태그와 역할 이름은 알기 쉬운 것으로 정하시고 만들면 됩니다. 

<Br>

### CodeDeploy 생성
앞서 언급했듯이 CodeDeploy는 AWS의 배포 서비스이며 오토 스케일링 그룹 배포, 블루 그린 배포, 롤링 배포, EC2 단독 배포 등 많은 기능을 지원합니다.

![그림28](https://github.com/backtony/blog-code/blob/master/aws/img/2/2-28.PNG?raw=true)  
AWS 홈페이지에서 CodeDeploy를 검색하고 왼쪽 탭에서 애플리케이션을 선택한 뒤 애플리케이션 생성을 클릭합니다.

<Br><Br>

![그림29](https://github.com/backtony/blog-code/blob/master/aws/img/2/2-29.PNG?raw=true)  
애플리케이션 이름을 작성하고, 컴퓨팅 플랫폼은 EC2로 선택합니다.

<Br><Br>

![그림30](https://github.com/backtony/blog-code/blob/master/aws/img/2/2-30.PNG?raw=true)  
배포 그룹 생성을 클릭합니다.

<Br><Br>

![그림31](https://github.com/backtony/blog-code/blob/master/aws/img/2/2-31.PNG?raw=true)  
배포 그룹 이름을 입력하고 서비스 역할 입력은 좀 전에 만들었던 역할을 클릭합니다. 위치도 현재 위치로 선택합니다.

<Br><Br>


![그림32](https://github.com/backtony/blog-code/blob/master/aws/img/2/2-32.PNG?raw=true)  
환경 구성에서 EC2를 선택하고 태그는 알기 쉬운 값으로 하나 줍니다.

<Br><Br>


![그림33](https://github.com/backtony/blog-code/blob/master/aws/img/2/2-33.PNG?raw=true)  
배포 설정은 AllAtOnce로 설정합니다. 배포 구성이란 한번 배포할 때 몇 대의 서버에 배포할지를 결정합니다. 2대 이상이라면 1대씩 배포할지, 30% 또는 50%로 나눠서 배포할지 등등 여러 옵션이 있습니다. 로드 밸런서는 나중에 Nginx로 할 것이므로 체크를 해제해줍니다.

<Br>

## Travis CI, S3, CodeDeploy 연동하기
![그림34](https://github.com/backtony/blog-code/blob/master/aws/img/2/2-34.PNG?raw=true)  

진행하기 전에 배포 과정을 한번 정리하겠습니다.  
1. GitHub에 푸시합니다.
2. Travis CI에서 빌드를 진행하고 S3로 zip 파일을 전달합니다.
3. Travis CI가 CodeDeploy로 배포 요청합니다.
4. CodeDeploy에서 S3에서 zip 파일을 가져와 EC2로 옮겨줍니다.
5. EC2에서 deploy.sh 파일을 통해 실행합니다.

먼저 S3에서 EC2로 넘겨줄 zip 파일을 저장할 디렉토리를 하나 생성하겠습니다. EC2에 접속하여 다음과 같이 디렉토리를 생성합니다.
```
mkdir app
```
Travis CI의 Build가 끝나면 S3로 zip파일을 전송하고, 이 zip 파일은 /home/ec2-user/app 로 복사되어 압축을 풀 것입니다.  
<br>

### appspec.yml

AWS CodeDeploy 설정은 appspec.yml로 가능합니다. build.gradle 와 같은 위치에 생성하면 됩니다. 
```yml
version: 0.0
os: linux
files:
  - source: /
    destination: /home/ec2-user/app
    overwrite: yes

permission:
  - object: /
    pattern: "**"
    owner: ec2-user
    group: ec2-user

hooks:
  ApplicationStart:
    - location: deploy.sh
      timeout: 60
      runas: ec2-user
```
+ version : CodeDeploy의 버전을 의미하고, 프로젝트 버전이 아닐 경우 0.0 외의 다른 버전 사용시 오류가 발생합니다.
+ source : CodeDeploy에서 전달해 준 파일 중 destination으로 이동시킬 대상을 지정합니다. /(루트경로)를 지정하면 전체 파일을 의미합니다.
+ destination : source에서 지정한 파일을 받을 위치입니다.
+ overwirte : 기존에 파일들을 덮어쓸지 결정합니다.
+ permissions : CodeDeploy에서 EC2로 넘겨준 파일들을 모두 ec2-user 권한을 갖도록 합니다.
+ hooks 
    - CodeDeploy 배포 단계에서 실행할 명령어를 지정합니다.
    - ApplicationStart 라는 단계에서 deploy.sh를 ec2-user 권한으로 실행합니다.
    - timeout : 60으로 스크립트 실행 60초 이상 수행되면 실패합니다.


<br>

### deploy.sh
![그림35](https://github.com/backtony/blog-code/blob/master/aws/img/2/2-35.PNG?raw=true)  
위 그림과 같은 위치에 생성해 줍니다. CodeDeploy로 EC2에 파일을 전달한 뒤 해당 파일을 어떻게 실행하는지에 대한 명령어를 담는 파일입니다.
```sh
#!/bin/bash
REPOSITORY=/home/ec2-user/app
PROJECT_NAME=aws-test

echo "> Build 파일 복사"

cp $REPOSITORY/*.jar $REPOSITORY/

echo "> 현재 구동중인 애플리케이션 pid 확인"

CURRENT_PID=$(pgrep -fl aws-test | grep jar | awk '{print $1}')

echo "현재 구동중인 어플리케이션 pid: $CURRENT_PID"

if [ -z "$CURRENT_PID" ]; then
    echo "> 현재 구동중인 애플리케이션이 없으므로 종료하지 않습니다."
else
    echo "> kill -15 $CURRENT_PID"
    kill -15 $CURRENT_PID
    sleep 5
fi

echo "> 새 어플리케이션 배포"

JAR_NAME=$(ls -tr $REPOSITORY/*.jar | tail -n 1)

echo "> JAR Name: $JAR_NAME"

echo "> $JAR_NAME 에 실행권한 추가"

chmod +x $JAR_NAME

echo "> $JAR_NAME 실행"

nohup java -jar $JAR_NAME > $REPOSITORY/nohup.out 2>&1 &
```
+ CURRENT_PID 
    - 현재 실행 중인 스프링 부트 애플리케이션 프로세스 ID를 찾습니다.
    - 스프링 부트 애플리케이션 이름이 aws-test 으로 된 다른 프로그램들이 있을 수 있으니 jar 프로세스를 찾은 뒤 ID를 찾아 있으면 kill 합니다.
+ chmod +x $JAR_NAME
    - Jar 파일은 실행 권한이 없는 상태로 nohup으로 실행할 수 있게 실행 권할을 부여합니다.
+ nohup java -jar $JAR_NAME > $REPOSITORY/nohup.out 2>&1 &
    - nohup : 애플리케이션 실행자가 터미널을 종료해도 애플리케이션이 계속 돌아가도록 하는 명령어입니다. nohup은 자동으로 로그 파일을 생성합니다.
    - $JAR_NAME > $REPOSITORY/nohup.out 2>&1 &: nohup 실행 시 CodeDeploy는 무한 대기하는 문제가 있으므로 이 이슈를 해결하기 위해 nohup.out 파일을 표준 입출력용으로 별도로 사용합니다. 이렇게 하지 않으면 nohup.out 파일이 생기지 않고, CodeDeploy 로그에 표준 입출력이 출력됩니다. 2>&1은 stderr(표준에러)를 stdout(표준출력)으로 redirection하라는 명령어입니다.
    - &은 백그라운드로 실행하라는 의미입니다.




<br>

### .travis.yml
Travis CI의 상세한 설정은 프로젝트에 존재하는 .travis.yml 파일로 할 수 있습니다. build.gradle과 같은 위치에 .travis.yml 파일을 생성하고 다음 코드를 추가합니다.
```yml
language: java
jdk:
  - openjdk11

branches:
  only:
    - master

cache:
  directories:
    - '$HOME/.m2/repository'
    - '$HOME/.gradle'

script: "./gradlew clean build"

before_deploy:
  - mkdir -p before-deploy
  - cp scripts/*.sh before-deploy/
  - cp appspec.yml before-deploy/
  - cp build/libs/*.jar before-deploy/
  - cd before-deploy && zip -r before-deploy *
  - cd ../ && mkdir -p deploy
  - mv before-deploy/before-deploy.zip deploy/aws-test.zip


deploy:
  - provider: s3
    access_key_id: $AWS_ACCESS_KEY # travis repo settings에 설정된 값
    secret_access_key: $AWS_SECRET_KEY # travis repo settings에 설정된 값
    bucket: travis-study # 버킷명
    region: ap-northeast-2
    skip_cleanup: true
    acl: private # zip 파일 접근은 private
    local_dir: deploy # before_deploy에서 생성한 디렉토리로 해당 위치의 파일들만 S3로 전송
    wait-until-deployed: true
  - provider: codedeploy
    access_key_id: $AWS_ACCESS_KEY # travis repo settings에 설정된 값
    secret_access_key: $AWS_SECRET_KEY # travis repo settings에 설정된 값
    bucket: travis-study # 버킷명
    key: aws-test.zip # 빌드 파일을 압축해서 전달
    bundle_type: zip # 압축 확장자
    application: travis # codeDeploy 애플리케이션 명
    deployment_group: travis # 배포 그룹 명
    regoin: ap-northeast-2
    wait-until-deployed: true

notifications:
  email:
    recipients:
      - cjs1863@gmail.com
```
+ language, jdk : 사용 언어와 jdk 버전 명시합니다.
+ branches : Travis CI를 어느 브랜치가 푸시될 때 수행할지 지정합니다.
+ cache : gradle을 통해 의존성을 받게 되면 이를 해당 디렉토리에 캐시하여, 같은 의존성은 다음 배포 때부터 다시 받지 않도록 설정합니다.
+ script : branches에서 정한 브랜치에 푸시되었을 때 해당 코드를 가지고 수행하는 명령어입니다.
+ before_deploy : 배포 전에 수행할 작업을 의미합니다.
    - mkdir -p before-deploy : before-deploy 디렉토리를 생성합니다. -p옵션은 여기서는 별 의미가 없지만 예를 들면 mkdir -p a/b/c 의 경우 디렉토리를 한 번에 만들어 줍니다. zip 으로 압축 시킬 파일들을 담는 디렉토리입니다. S3는 특정 파일만 업로드가 불가능하고 디렉토리 단위로만 업로드가 가능하기 때문에 필요합니다.
    - deploy.sh, appspec.yml *.jar 파일을 before-deploy 폴더로 복사하고 before-deploy로 이동하여 zip -r 옵션으로 압축합니다. -r 옵션은 하위 디렉토리까지 포함하는 옵션입니다.
    - mv 로 압축 파일을 deploy로 옮겨줍니다.
+ deploy : S3, CodeDeploy로 배포 등 외부 서비스와 연동될 행위들을 선언합니다.
+ notifications : Travis CI 실행 완료 시 자동으로 알람이 가도록 설정합니다.


<br>

### CodeDeploy 로그 확인
CodeDeploy와 같이 AWS가 지원하는 서비스는 오류가 발생했을 때 로그 찾는 방법을 모른다면 해결하리 어렵습니다. CodeDeploy에 관한 대부분 내용은 /opt/codedeploy-agent/deployment-root 에 있습니다. 
```
cd /opt/codedeploy-agent/deployment-root
ls
```
+ 최상단의 영문과 대시(-)가 있는 디렉토리명은 CodeDeploy ID입니다.
    - 사용자마다 고유한 ID가 생성되어 각자 다른 ID가 발급되니 본인 서버에는 달느 코드로 되어있습니다.
    - 해당 디렉토리로 들어가면 배포한 단위별로 배포 파일들이 있습니다.
    - 본인의 배포 파일이 정상적으로 왔는지 확인해 볼 수 있습니다.
+ /opt/codedeploy-agent/deployment-root/deployment-logs/codedeploy-agent-deployments.log
    - CodeDeploy 로그 파일입니다.
    - 배포 내용 중 표준 입/출력 내용은 모두 여기 담겨있습니다.
    - 작성한 echo 내용도 모두 표기됩니다.

<br>

# 5. 무중단 배포하기
---
지금까지 Travis CI를 활용하여 배포 자동화 환경을 구축했지만 배포되는 과정에서 애플리케이션이 종료되는 문제가 남아있습니다. 배포 과정에서 새로운 Jar이 실행되기 전까진 기존 Jar를 종료시켜 놓기 때문에 서비스가 중단됩니다. 무중단 배포 방식에는 몇 가지가 있습니다.
+ AWS 블루 그린 무중단 배포
+ 도커를 이용한 웹서비스 무중단 배포

여기서 사용할 것은 엔진엑스를 이용한 무중단 배포 입니다. 엔진엑스는 웹 서버, 리버스 프록시, 캐싱, 로드 밸런싱, 미디어 스트리밍 등을 위한 오픈소스 소프트웨어입니다. 엔진엑스가 가지고 있는 여러 기능 중 리버스 프록시란 기능이 있습니다. 리버스 프록시란 엔진엑스가 외부의 요청을 받아 백엔드 서버로 요청을 전달하는 행위를 말합니다. 리버스 프록시 서버(엔진엑스)는 요청을 받아 뒷단 서버로 요청을 전달하는 역할을 하고 실제 요청에 대한 처리는 뒷단 애플리케이션 서버에서 처리하게 됩니다. 이를 통해 무중단 배포를 구축할 수 있습니다. 엔직엑스를 이용하는 이유는 가장 저렴하고 쉽기 때문입니다. 비용 지원이 많다면 번거롭게 구축할 필요 없이 AWS에서 제공하는 블루 그린 배포 방식을 선택하면 됩니다.

## 구조
![그림39](https://github.com/backtony/blog-code/blob/master/aws/img/2/2-39.PNG?raw=true)  
전체적인 구조는 위와 같습니다.  
<br><br>

![그림36](https://github.com/backtony/blog-code/blob/master/aws/img/2/2-36.PNG?raw=true)  

구조는 간단하게 리눅스 서버에 엔진엑스 1대와 스프링 부트 Jar을 2대 사용하는 방식입니다.
+ 엔진엑스는 80(http), 443(https) 포트 할당
+ 스프링 부트 1은 8081 포트 할당
+ 스프링 부트 2는 8082 포트 할당

운영 과정은 다음과 같습니다.  
1. 사용자는 서비스 주소로 접속합니다(80 또는 442 포트)
2. 엔진엑스는 사용자의 요청을 받아 현재 연결된 스프링 부트 1인 8081포트로 요청을 전달합니다. 8082 포트로 연결된 스프링 부트 2는 아직 엔진엑스와 연결된 상태가 아니므로 요청을 받지 못합니다.
3. ver 1.1 배포를 진행합니다. 배포가 진행되는 동안 엔진엑스는 스프링 부트 1을 바라보기 때문에 서비스가 중단되지 않습니다.
4. 배포가 끝나면 스프링 부트 2가 정상적으로 구동되는지 확인합니다.
5. 정상적으로 구동된다면 nginx reload 명령어를 통해 nginx가 8081 대신 8082를 바라보도록 합니다. 이 과정은 0.1초내에 완료됩니다.
6. 이후 배포과정은 8081으로 진행됩니다.

## profile 나누기
```yml
# application.yml
spring:
  config:
    activate:
      on-profile: real1
  datasource:
    url: jdbc:mysql://host주소:3306/db명
    driver-class-name: com.mysql.cj.jdbc.Driver
    username: 아이디
    password: 패스워드  

server:  
  port: 8081
---
spring:
  config:
    activate:
      on-profile: real2
  datasource:
    url: jdbc:mysql://host주소:3306/db명
    driver-class-name: com.mysql.cj.jdbc.Driver
    username: 아이디
    password: 패스워드  

server:  
  port: 8082
```
스프링 부트를 2개 돌려야 하므로 서로 다른 profile 2개를 만들어 사용합니다.

<Br>

## profile API 만들기
배포시 8081을 쓸지, 8082를 쓸지 판단하는 기준이 되는 API를 만들어야 합니다. 현재 profile을 반환하도록 만들어 줍니다.
```java
@RestController
@RequiredArgsConstructor
public class Controller {

    private final Environment env;

    @GetMapping("/profile")
    public String profile(){
        List<String> profiles = Arrays.asList(env.getActiveProfiles());
        List<String> realProfiles = Arrays.asList("real1", "real2");

        String defaultProfile = profiles.isEmpty() ? "default" : profiles.get(0);

        return profiles.stream()
                .filter(realProfiles::contains)
                .findAny()
                .orElse(defaultProfile);
    }
}
```

<br>

## Nginx 설치
EC2로 접속하여 엔진엑스를 설치합니다.
```
설치
sudo amazon-linux-extras install nginx1

시작
sudo service nginx start
```
<br>

## 보안 그룹 추가
![그림37](https://github.com/backtony/blog-code/blob/master/aws/img/2/2-37.PNG?raw=true)  
엔진엑스의 기본 포트 번호는 80이므로 해당 EC2의 보안 그룹에서 포트를 열어주도록 합니다.

<br>

## 엔진엑스와 스프링 부트 연동
엔진엑스가 현재 실행 중인 스프링 부트 프로젝트를 바라볼 수 있도록 프록시 설정을 진행해야 합니다.
```
파일 만들기
sudo vim /etc/nginx/conf.d/service-url.inc

다음을 추가
set $service_url http://127.0.0.1:8080;

설정파일 열기
sudo vim /etc/nginx/nginx.conf
```

![그림38](https://github.com/backtony/blog-code/blob/master/aws/img/2/2-38.PNG?raw=true)  
위 그림처럼 include와 location 부분에 다음 코드를 추가해줍니다.
```
include /etc/nginx/conf.d/service-url.inc;

location / {
  proxy_pass $service_url;
  proxy_set_header X-Real-IP $remote_addr; 
  proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
  proxy_set_header Host $http_host;
}
```
+ proxy_pass : 엔진엑스로 요청이 오면 해당 url로 전달합니다. 앞서 만들었던 파일을 include해서 변수로 사용합니다.
+ proxy_set_header : 실제 요청 데이터를 header의 각 항목에 할당합니다.
  - 예를 들면, 요청헤더에 X-Real-IP 값으로 요청자의 IP를 넣습니다.

```
sudo service nginx restart
```
설정을 끝내고 nginx를 재부팅 시킵니다.

<br>

## 배포 스크립트 작성
+ stop.sh : 엔진엑스와 연결되어 있지 않은 스프링 부트 종료
+ start.sh : 엔진엑스와 연결되어 있지 않은 Port로 새 버전 스프링 부트 시작
+ health.sh : start.sh로 실행시킨 프로젝트가 정상적으로 실행 중인지 체크
+ switch.sh : 엔진엑스가 바라보는 스프링 부트를 최신 버전으로 변경
+ profile.sh : 앞선 4개의 스크립트 파일에서 공용으로 사용할 로직이 담긴 파일

### appspec.yml 수정
```yml
version: 0.0
os: linux
files:
  - source:  /
    destination: /home/ec2-user/app/step/zip/
    overwrite: yes

permissions:
  - object: /
    pattern: "**"
    owner: ec2-user
    group: ec2-user

hooks:
  AfterInstall:
    - location: stop.sh # 엔진엑스와 연결되어 있지 않은 스프링 부트를 종료합니다.
      timeout: 60
      runas: ec2-user
  ApplicationStart:
    - location: start.sh # 엔진엑스와 연결되어 있지 않은 Port로 새 버전의 스프링 부트를 시작합니다.
      timeout: 60
      runas: ec2-user
  ValidateService:
    - location: health.sh # 새 스프링 부트가 정상적으로 실행됐는지 확인 합니다.
      timeout: 60
      runas: ec2-user
```
<Br>

배포를 /home/ec2-user/app/step/zip 에서 진행하고 있기 때문에 EC2 에서 디렉토리를 생성해줘야 합니다.
```
// /home/ec2-user 위치에서
mkdir -p app/step/zip
```
<br>

### profile.sh
```sh
#!/usr/bin/env bash

# bash는 return value가 안되니 *제일 마지막줄에 echo로 해서 결과 출력*후, 클라이언트에서 값을 사용한다

# 쉬고 있는 profile 찾기: real1이 사용중이면 real2가 쉬고 있고, 반대면 real1이 쉬고 있음
function find_idle_profile()
{
    RESPONSE_CODE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost/profile)

    if [ ${RESPONSE_CODE} -ge 400 ] # 400 보다 크면 (즉, 40x/50x 에러 모두 포함)
    then
        CURRENT_PROFILE=real2 # 에러 발생 시 현재 profile을 real2로 사용
    else
        CURRENT_PROFILE=$(curl -s http://localhost/profile)
    fi

    if [ ${CURRENT_PROFILE} == real1 ]
    then
      IDLE_PROFILE=real2
    else
      IDLE_PROFILE=real1
    fi

    echo "${IDLE_PROFILE}"
}

# 쉬고 있는 profile의 port 찾기
function find_idle_port()
{
    IDLE_PROFILE=$(find_idle_profile)

    if [ ${IDLE_PROFILE} == real1 ]
    then
      echo "8081"
    else
      echo "8082"
    fi
}
```
+ $(curl -s -o /dev/null -w "%{http_code}" http://localhost/profile)  
  - 현재 엔직엑스가 바라보고 있는 스프링 부트가 정상적으로 수행중인지 확인하는 과정으로 해당 주소로 요청을 보낸 후 상태 코드를 http_code에 담습니다.
  - curl : 해당 URL로 요청을 보내는 명령어입니다.
    - -s : silent란 의미로 상태진행바를 노출시키지 않는 옵션
    - -w : http status code를 찍는 옵션
  - -o /dev/null: 위 명령어가 성공했다면 아무 것도 뜨지 않고 실패시 에러 메시지가 출력됩니다. output이 필요없으므로 /dev/null로 보내는 옵션입니다. 
  - "%{http_code}" : URL 요청의 HTTP Status Code를 파싱하여 상태값을 담습니다.
+ if문: if 문안에 [] 안에 위와 같이 반드시 띄어쓰기를 해야 에러가 발생하지 않습니다.
+ then: if문 뒤에 써주어야 할 것입니다.
+ fi: if문이 끝났음을 알리는 것입니다.
+ ${IDLE_PROFILE} : 엔진엑스와 연결되지 않은 profile로 다음 배포 시 사용할 profile로 사용하기 위해 반환합니다.
+ bash 스크립트는 값을 반환하는 기능이 없으므로 echo로 결과를 출력하여 클라이언트에서 그 값을 잡아서 사용해야 합니다.

### stop.sh
```sh
#!/usr/bin/env bash

ABSPATH=$(readlink -f $0)
ABSDIR=$(dirname $ABSPATH)
source ${ABSDIR}/profile.sh

IDLE_PORT=$(find_idle_port)

echo "> $IDLE_PORT 에서 구동중인 애플리케이션 pid 확인"
IDLE_PID=$(lsof -ti tcp:${IDLE_PORT})

if [ -z ${IDLE_PID} ]
then
  echo "> 현재 구동중인 애플리케이션이 없으므로 종료하지 않습니다."
else
  echo "> kill -15 $IDLE_PID"  # Nginx에 연결되어 있지는 않지만 현재 실행 중인 jar 를 Kill 합니다.
  kill -15 ${IDLE_PID}
  sleep 5
fi
```
profile.sh에 존재하는 find_idle_port 함수를 통해서 현재 구동되고 있는 포트의 PID를 확인 한 후에 kill 하는 스크립트입니다.
+ (readlink -f $0) : 파일의 절대 경로를 알아내는 명령어입니다.
+ $(dirname $ABSPATH) : 디렉토리 경로를 출력하기 위한 명령어입니다. stop.sh가 속해있는 경로를 찾습니다.
  - dirname은 디렉토리 경로를 출력하는 명령어인데 마지막의 경로를 출력하지 않는다는 특징이 있습니다.
  - 예를 들어, direname /home/ec2-user/app/ 입력시 /home/ec2-user 까지 출력됩니다.
+ source ${ABSDIR}/profile.sh : 자바로 보면 일종의 import 구문입니다.
+ (sudo lsof -ti tcp:${IDLE_PORT}) : 현재 실행 중인 포트의 PID를 확인할 수 있습니다.
+ if [ -z ${IDLE_PID} ] : if 문안에 -z 옵션을 사용하면 해당 IDLE_PID가 null이면 true, null이 아니면 false를 반환합니다.

### start.sh
```sh
#!/usr/bin/env bash

ABSPATH=$(readlink -f $0)
ABSDIR=$(dirname $ABSPATH)
source ${ABSDIR}/profile.sh

REPOSITORY=/home/ec2-user/app/step

echo "> Build 파일 복사"
echo "> cp $REPOSITORY/zip/*.jar $REPOSITORY/"

cp $REPOSITORY/zip/*.jar $REPOSITORY/

echo "> 새 어플리케이션 배포"
JAR_NAME=$(ls -tr $REPOSITORY/*.jar | tail -n 1)   # jar 이름 꺼내오기

echo "> JAR Name: $JAR_NAME"   

echo "> $JAR_NAME 에 실행권한 추가"

chmod +x $JAR_NAME

echo "> $JAR_NAME 실행"

IDLE_PROFILE=$(find_idle_profile)

echo "> $JAR_NAME 를 profile=$IDLE_PROFILE 로 실행합니다."
nohup java -jar \
    -Dspring.profiles.active=$IDLE_PROFILE \
    $JAR_NAME > $REPOSITORY/nohup.out 2>&1 &
```
+ IDLE_PROFILE=$(find_idle_profile) : 프로파일명을 가져옵니다.

### health.sh
```sh
#!/usr/bin/env bash

ABSPATH=$(readlink -f $0)
ABSDIR=$(dirname $ABSPATH)
source ${ABSDIR}/profile.sh
source ${ABSDIR}/switch.sh

IDLE_PORT=$(find_idle_port)

echo "> Health Check Start!"
echo "> IDLE_PORT: $IDLE_PORT"
echo "> curl -s http://localhost:$IDLE_PORT/profile "
sleep 10

for RETRY_COUNT in {1..10}
do
  RESPONSE=$(curl -s http://localhost:${IDLE_PORT}/profile)
  UP_COUNT=$(echo ${RESPONSE} | grep 'real' | wc -l)  

  if [ ${UP_COUNT} -ge 1 ]
  then # $up_count >= 1 ("real" 문자열이 있는지 검증)
      echo "> Health check 성공"
      switch_proxy
      break
  else
      echo "> Health check의 응답을 알 수 없거나 혹은 실행 상태가 아닙니다."
      echo "> Health check: ${RESPONSE}"
  fi

  if [ ${RETRY_COUNT} -eq 10 ]
  then
    echo "> Health check 실패. "
    echo "> 엔진엑스에 연결하지 않고 배포를 종료합니다."
    exit 1
  fi

  echo "> Health check 연결 실패. 재시도..."
  sleep 10
done
```
echo ${RESPONSE} | grep 'real' | wc -l : wc명령어에 -l 옵션은 해당 명령어의 결과의 수를 숫자로 반환합니다.(즉, 결과가 3줄이면 3을 반환합니다.)

### switch.sh
```sh
#!/usr/bin/env bash

ABSPATH=$(readlink -f $0)
ABSDIR=$(dirname $ABSPATH)
source ${ABSDIR}/profile.sh

function switch_proxy() {
    IDLE_PORT=$(find_idle_port)

    echo "> 전환할 Port: $IDLE_PORT"
    echo "> Port 전환"
    echo "set \$service_url http://127.0.0.1:${IDLE_PORT};" | sudo tee /etc/nginx/conf.d/service-url.inc

    echo "> 엔진엑스 Reload"
    sudo service nginx reload
}
```
+ echo "set \$service_url http://127.0.0.1:${IDLE_PORT};
  - 하나의 문장을 만들어 파이프라인으로 넘겨주기 위해 echo를 사용합니다.
+ | sudo tee /etc/nginx/conf.d/service-url.inc
  - 앞에서 넘겨준 문장을 service-url.inc에 덮어 씁니다.
+ sudo service nginx reload
  - 엔진엑스 설정을 다시 불러옵니다.
  - restart는 잠시 끊기는 현상이 있지만, reload는 끊김 없이 다시 불러옵니다.
  - 다만, 중요한 설정들은 반영되지 않으므로 restart를 사용해야 합니다.
  - 여기선 외부의 설정 파일인 service-url을 다시 불러오는 거라 reload로 가능합니다.

<br>

# 6. 무중단 배포 테스트
## CodeDeploy 로그 확인
```
tail -f /opt/codedeploy-agent/deployment-root/deployment-logs/codedeploy-agent-deployments.log
```

## 스프링 부트 로그 확인
```
vim ~/app/step/nohup.out
```

## 자바 애플리케이션 실행 여부
2번 배포 진행 뒤에 다음과 같은 명령어로 자바 애플리케이션 실행 여부를 확인할 수 있습니다.
```
ps -ef | grep java
```



<Br><Br>

__참고__  
<a href="http://www.kyobobook.co.kr/product/detailViewKor.laf?ejkGb=KOR&mallGb=KOR&barcode=9788965402602" target="_blank"> 스프링 부트와 AWS로 혼자 구현하는 웹 서비스</a>   


