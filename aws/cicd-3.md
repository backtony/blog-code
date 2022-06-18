# Spring, Docker, Jenkins, Blue Green 무중단 배포, VPC, AutoScaling, Load Balancer, S3, CloudWatch, RDS를 활용한 CI/CD 구축하기

# 최종 구조

![그림93](https://github.com/backtony/blog-code/blob/master/aws/img/3/3-93.PNG?raw=true)  

# 1. VPC 구성하기
---
## VPC 란?
아마존 공식 설명에 의하면, Amazon Virtual Private Cloud(VPC)를 사용하면 AWS 클라우드에서 논리적으로 격리된 공간을 프로비저닝 하여 고객이 정의하는 가상 네트워크에서 AWS 리소스를 시작할 수 있습니다. IP 주소 범위 선택, 서브넷 생성, 라우팅 테이블 및 네트워크 게이트웨이 구성 등 가상 네트워킹 환경을 완벽하게 제어할 수 있습니다. VPC에서 IPv4와 IPv6를 모두 사용하여 리소스와 애플리케이션에 안전하고 쉽게 액세스 할 수 있습니다.  
설명만 읽으면 이해하기 난해한 감이 있습니다. 간단하게 설명해보자면, VPC라는 기능은 __논리적으로 격리된 공간__ 을 제공하는데 이곳에 사용자가 원하는 EC2, RDS 등을 배치할 수 있습니다. 다시말해, 우리가 하나의 격리된 공간을 만들고 그곳에 리소스들을 배치하여 격리된 네트워크를 만들어 다른 사람들이 접근하는 것을 불가능하게 하는 것입니다.
<br>

## VPC 구성하기
![그림1](https://github.com/backtony/blog-code/blob/master/aws/img/3/3-1.PNG?raw=true)  
AWS 홈페이지에서 VPC를 검색하고 오른쪽 상단에 VPC 생성을 클릭합니다. VPC 이름을 입력하고 IPv4 CIDR 블록을 10.0.0.0/16 으로 입력합니다.  
<br><br>

![그림2](https://github.com/backtony/blog-code/blob/master/aws/img/3/3-2.PNG?raw=true)  
왼쪽 탭에서 서브넷을 클릭하고 오른쪽 상단에 서브넷 생성을 클릭합니다. vpc는 방금 위에서 만들었던 vpc를 선택하고 외부 인터넷과 연결이 가능하게 만들 public용 서브넷을 구성합니다. 가용 영역은 2a로 지정해줍니다.
<br><br>

![그림3](https://github.com/backtony/blog-code/blob/master/aws/img/3/3-3.PNG?raw=true)  
가용 영역이 2c인 외부 인터넷과 연결이 가능하게 할 public 용 서브넷을 만듭니다.
<br><br>

![그림4](https://github.com/backtony/blog-code/blob/master/aws/img/3/3-4.PNG?raw=true)  
가용 영역이 2a인 외부 인터넷과 연결하지 않을 private용 서브넷을 만듭니다.
<br><br>

![그림5](https://github.com/backtony/blog-code/blob/master/aws/img/3/3-5.PNG?raw=true)  
가용 영역이 2c인 외부 인터넷과 연결하지 않을 private용 서브넷을 만듭니다.
<br><br>

__cf) Ip__  
IP는 32비트로 구성되어 있습니다. 일반적으로 보는 0.0.0.0 은 IPv4 주소로 32비트 2진수를 10진수로 표현한 것입니다. 즉, 하나의 0이 255까지 가능한 것으로 0자리 하나당 8비트 2진수가 10진수로 변한 것이라고 보시면 됩니다.  

__cf) Prefix표기__  
Prefix 표기란 서브넷 마스크 맨 앞의 비트부터 1의 개수를 표기하는 방식을 말합니다. 예를 들어 서브넷 마스크가 255.255.255.0인 경우 2진수로 보면 맨 앞의 비트부터 1이 24개 있으므로 /24로 표기한 것입니다.  

__cf) 네트워크 영역, 호스트 영역 범위__  
10.0.0.0/24 로 예를 들면, 앞에 24비트 10.0.0을 네트워크 영역 뒤에 마지막 0을 호스트 영역이라고 합니다. 앞의 24비트는 네트워크 영역으로 같다면 같은 네트워크 대역입니다. 뒤쪽의 8비트는 호스트 영역 범위인데 해당 10.0.0 네트워크에 호스트 영역으로 2^8개의 범위, 즉 2^8개의 Ip를 범위를 할당한다는 것입니다. 여기서 8은 32비트 - 24비트로 나온 것입니다.  

__cf) VPC 사설 아이피 대역__  
VPC의 최대 크기는 16 즉, 2^(32 - 16) = 65536개의 IP를 사용가능하고 VPC에서 사용하는 사설아이피 대역은 다음과 같습니다.
+ Class A: 10.0.0.0 ~ 10.255.255.255 (8bit)
+ Class B: 172.16.0.0 ~ 172.31.255.255 (12bit)
+ Class C: 192.168.0.0 ~ 192.168.255.255 (16bit)  



<a href="https://akasai.space/aws/about_vpc/" target="_blank"> VPC란?</a>   
<a href="https://m.blog.naver.com/PostView.naver?isHttpsRedirect=true&blogId=hatesunny&logNo=220790654612" target="_blank"> IP 주소 및 서브넷마스크</a>   
<a href="https://limkydev.tistory.com/166?category=954021" target="_blank"> [Network] 서브넷마스크(Subnet Mask)란?</a>   

<Br>

## 인터넷 게이트웨이 연결하기
만들어준 VPC가 외부 인터넷과 통신이 가능하도록 하기 위해서 이를 가능케 해주는 인터넷 게이트웨이를 만들고 VPC와 연결하는 작업을 합니다.

![그림6](https://github.com/backtony/blog-code/blob/master/aws/img/3/3-6.PNG?raw=true)  
왼쪽 탭에서 인터넷 게이트웨이를 클릭하고 오른쪽 상단에 인터넷 게이트웨이 생성을 클릭합니다. 이름을 정해주고 하단의 생성을 클릭합니다.
<br><br>

![그림7](https://github.com/backtony/blog-code/blob/master/aws/img/3/3-7.PNG?raw=true)  
만든 인터넷 게이트웨이를 선택하고 작업 -> VPC 연결을 클릭합니다.
<br><br>

![그림8](https://github.com/backtony/blog-code/blob/master/aws/img/3/3-8.PNG?raw=true)  
앞서 만든 vpc를 선택하고 연결합니다.
<br><br>

![그림9](https://github.com/backtony/blog-code/blob/master/aws/img/3/3-9.PNG?raw=true)  
왼쪽 탭에서 라우팅 테이블을 클릭합니다. VPC를 만들면 기본적으로 라우팅 테이블이 1개 생성됩니다. 하나를 선택해서 하단 세부 정보에 VPC를 보시면 어떤 VPC에 의해 생성된 라우팅 테이블인지 확인할 수 있습니다. 기본적으로 생성된 것을 public용 라우팅 테이블로 만들고 private 라우팅 테이블 2개는 따로 생성하겠습니다. 먼저 알기 쉽도록 Name을 수정해 줍니다. 저는 gjgs-pub로 수정했습니다.  

__cf) 라우팅 테이블__  
라우팅 테이블이란 패킷이 목적지, 목적지까지의 거리와 가는 방법 등을 명시하고 있는 테이블입니다. 
<br><br>

![그림10](https://github.com/backtony/blog-code/blob/master/aws/img/3/3-10.PNG?raw=true)  
하단에 라우팅을 클릭하고 라우팅 편집을 클릭합니다.  

__cf) 라우팅__  
라우팅이란 한 네트워크에서 다른 네트워크로 패킷을 이동시키는 과정과 네트워크 안의 호스트에게 패킷들을 전달하는 과정을 말합니다.
<br><br>

![그림11](https://github.com/backtony/blog-code/blob/master/aws/img/3/3-11.PNG?raw=true)  
0.0.0.0/0 을 입력하고 대상은 인터넷 게이트웨이를 클릭하고 좀 전에 만든 인터넷 게이트웨이를 선택하여 저장합니다. 참고로 0.0.0.0/0은 모든 트래픽에 대해서 라고 보면 되고 10.0.0.0/16의 대상이 local인 것은 비활성화되어 있다는 것입니다.
<br><br>

![그림12](https://github.com/backtony/blog-code/blob/master/aws/img/3/3-12.PNG?raw=true)  
라우팅 테이블로 트래픽에 대한 목적지를 정해줬으니 이제는 해당 라우팅 테이블로 들어오는 서브넷을 지정해줄 차례입니다. 서브넷 연결 탭에서 서브넷 연결 편집을 클릭합니다.
<br><br>

![그림13](https://github.com/backtony/blog-code/blob/master/aws/img/3/3-13.PNG?raw=true)  
public 용 라우팅 테이블이므로 public 용 서브넷 2개를 연결시켜줍니다. private 용 서브넷은 NAT Gateway를 구성하고 같이 진행하겠습니다.
<br>

## NAT Gateway 구성하기
NAT Gateway는 VPC의 private subnet 리소스가 인터넷으로 트래픽이 통할 수 있도록 만들어주는 서비스 입니다. NAT Gateway는 2a용, 2c용으로 2개를 만들 것입니다. NAT Gateway는 할당된 서브넷에 대한 기능을 주로 하기 때문에 2a서브넷에 NAT Gateway 한개 2c서브넷에 NAT Gateway를 하나씩 올리고 각 NAT Gateway에 라우팅 테이블을 설정해주겠습니다.


![그림14](https://github.com/backtony/blog-code/blob/master/aws/img/3/3-14.PNG?raw=true)  
왼쪽 탭에서 NAT 게이트웨이를 클릭하고 오른쪽 상단에 NAT 게이트웨이 생성을 클릭합니다. 이름을 정해주고 서브넷에 public 용 2a를 선택합니다. 인터넷으로 나갈때는 공인 IP를 타고 나가기 때문에 탄력적 IP를 할당해주고 생성해줍니다.
<br><br>

![그림15](https://github.com/backtony/blog-code/blob/master/aws/img/3/3-15.PNG?raw=true)  
다시 한번 public 용 2c를 선택하고 탄력적 IP를 할당한 뒤 생성해줍니다.
<br><br>

![그림16](https://github.com/backtony/blog-code/blob/master/aws/img/3/3-16.PNG?raw=true)  
이제 라우팅 테이블을 만들 차례입니다. public 용은 2a, 2c의 서브넷에서 오는 트래픽을 하나의 게이트웨이에 연결해주면 되기 때문에 라우팅 테이블이 1개만 필요했지만, private의 경우 각각의 NAT Gateway에 연결시켜줘야 하므로 라우팅 테이블이 2개가 필요합니다. 이렇게 2개로 분리해서 생성하는 이유는 특정 가용 영역에 장애가 났을 경우, 다른 쪽에서 서비스가 가능하도록 하기 위해서 입니다. 먼저 2a용 라우팅 테이블을 만들겠습니다.
<br><br>

![그림17](https://github.com/backtony/blog-code/blob/master/aws/img/3/3-17.PNG?raw=true)  
서브넷 연결로 pri1-2a를 연결해 줍니다.
<br><br>

![그림18](https://github.com/backtony/blog-code/blob/master/aws/img/3/3-18.PNG?raw=true)  
이번엔 어디로 보낼지 정해줄 차례입니다. 라우팅 편집을 클릭하고 0.0.0.0/0에 대하여 NAT 게이트웨이를 선택해주고 2a용을 클릭하고 저장해줍니다. 이와 마찬가지로 2c용 라우팅 테이블을 만들고 설정해주면 됩니다.
<br>

# 2. Bastion
---
## Bastion 이란?
베스천(Bastion) 은 보루, 요새라는 뜻으로 중세 시대에 영주나 왕이 살고 있는 중요한 기지인 성을 둘러싸고 있는 방어막을 의미합니다. 컴퓨터 보안에서도 이런 의미를 가져와서 보호된 네트워크에 접근하기 위해 유일하게 외부에 노출되는 호스트를 Bastion 호스트라고 정의하고 있습니다.  
외부에서 접근 불가능한 Private Subnet에 접근하기 위해 Public Subnet에 Bastion Host를 두어 Bastion Host를 통해 Private Subnet에 접근한다고 보면 됩니다. 조금 더 풀어서 설명하면, Private Subnet에 있는 EC2는 외부와 소통을 못하니 외부와 소통 가능한 EC2를 만들어 두고 이 EC2에서 Private subnet에 있는 EC2로 ssh 접속이 가능하게 만들어 두는 것입니다. 여기서 소통 가능한 EC2를 Bastion이라고 합니다. 일종의 게이트 역할을 수행하는 것입니다.  
<br>

## Bastion을 사용하는 이유
Bastion Host를 통해 접근하게 된다면 Bastion Host에서 특정 IP만 접근 가능하도록 설정할 수 있습니다. 'Private Subnet에 있는 인스턴트들도 보안 설정을 통해 해주면 되지 않을까' 라고 생각할 수 있지만 한 곳에서 모든 접근을 관리함으로 인해 각 인스턴스마다 하나씩 관리하는 것보다 훨씬 수월하고 모든 접근에 대한 로그 또한 한곳에서 관리가 가능합니다.

## Bastion 구성하기
### EC2 생성
![그림19](https://github.com/backtony/blog-code/blob/master/aws/img/3/3-19.PNG?raw=true)  
AWS에서 EC2를 생성합니다. 연습용이니 프리티어를 선택해주시고 3단계에서 전에 만든 VPC를 선택합니다. 서브넷은 pub1-2a를 선택해주시고 퍼블릭 IP 자동 할당을 활성화해줍니다.
<br><br>

![그림20](https://github.com/backtony/blog-code/blob/master/aws/img/3/3-20.PNG?raw=true)  
보안은 ssh만 열어주도록 하고 만들어 줍니다. 만든 이후 저는 추가적으로 접속을 편하게 하기 위해 탄력적 IP를 할당했습니다.
<br>

### EC2 초기 설정
#### EC2 hostname 변경
현재 EC2에 접속하시면 아래 사진과 IP만으로 표시되고 있습니다. 여러 서버를 관리 중일 경우 IP만으로 어떤 서비스의 서버인지 확인하기 어렵습니다.
![그림21](https://github.com/backtony/blog-code/blob/master/aws/img/3/1-21.P?raw=trueNG)
따라서 Hostname을 변경해주겠습니다.
```
sudo hostnamectl set-hostname 원하는이름

예시
sudo hostnamectl set-hostname bastion
```
변경해주었다면 sudo reboot 를 이용해 EC2를 재부팅 시키고 조금 뒤에 다시 접속해보면 아래 사진과 같이 deploy로 변경된 것을 확인할 수 있습니다.
![그림22](https://github.com/backtony/blog-code/blob/master/aws/img/3/1-22.P?raw=trueNG)


#### EC2 시간 변경
EC2 서버의 기본 타임존은 UTC입니다. 한국시간으로 변경시켜주겠습니다.
```
sudo rm /etc/localtime
sudo ln -s /usr/share/zoneinfo/Asia/Seoul /etc/localtime
```
위 명령어를 입력한 뒤에 date를 입력하면 시간이 변경된 것을 확인할 수 있습니다.

#### MySQL 설치
```
sudo yum install https://dev.mysql.com/get/mysql80-community-release-el7-3.noarch.rpm

sudo yum install mysql-community-server -y

시작
sudo systemctl start mysqld

상태 확인
sudo systemctl status mysqld
```

#### 부팅 시 MySQL 시작하기
```
sudo vi /etc/rc.d/rc.local

맨 아래 줄에 추가
sudo systemctl start mysqld

권한 부여
sudo chmod +x /etc/rc.d/rc.local

재부팅
sudo reboot
```
기본적인 Bastion 세팅은 여기까지만 진행하고 RDS와 운영 EC2를 구성하고 접속을 진행하겠습니다.
<br>

# 3. RDS - MySQL
---
## RDS 생성하기
![그림21](https://github.com/backtony/blog-code/blob/master/aws/img/3/3-21.PNG?raw=true)  
RDS 만들기 앞서 RDS에 적용할 보안그룹을 만들어 줍니다. AWS에서 EC2를 검색하고 왼쪽 탭의 아래쪽에 보안 그룹을 클릭하고 오른쪽 상단에 보안 그룹 생성을 클릭합니다. 적당한 이름과 설명을 적어주시고 VPC는 전에 만든 VPC를 선택해줍니다. 인바운드 규칙으로 MySQL을 선택하시고 열어줍니다. 내부용으로 RDS를 만들 것이기 때문에 만들어준 VPC인 10.0.0.0/16으로 해도 되고 전체로 열어줘도 상관 없습니다.
<br><Br>

![그림22](https://github.com/backtony/blog-code/blob/master/aws/img/3/3-22.PNG?raw=true)  
AWS 홈페이지에서 RDS를 검색하고 데이터베이스 생성을 클릭합니다. MySQL을 선택하고 연습용 이므로 프리티어로 선택합니다.
<br><Br>

![그림23](https://github.com/backtony/blog-code/blob/master/aws/img/3/3-23.PNG?raw=true)  
식별자와 계정 정보를 적어줍니다.
<br><Br>

![그림24](https://github.com/backtony/blog-code/blob/master/aws/img/3/3-24.PNG?raw=true)  
RDS는 완전 관리형 DB서비스이므로 이 설정을 통해 이중화 구성을 사용할 수 있습니다. 대기 인스턴스 생성을 선택하면 이중화 구성이 가능하고 장애가 났을 경우 대기 인스턴스 RDS가 바로 메인으로 사용됩니다. 프리티어의 경우는 지원하지 않으므로 선택하지 않고 넘어갑니다. 앞선 구조 그림과 같이 설계하려면 템플릿을 프리티어가 아닌 프로덕션이나 개발/테스트를 선택하시고 이 옵션을 체크해주면 됩니다.
<br><Br>


![그림25](https://github.com/backtony/blog-code/blob/master/aws/img/3/3-25.PNG?raw=true)  
vpc는 만들어둔 vpc를 선택, 퍼블릭 엑세스 아니요를 선택합니다. VPC는 앞서 RDS용으로 만든 것을 선택하시고 가용 영역은 2a로 선택해줍니다. 퍼블릭 액세스는 RDS를 생성했을 때 방화벽, 보안을 허용해주면 접속을 가능하케 해주겠느냐는 옵션으로 보통 private의 경우 No를 선택합니다.
<br><Br>

![그림26](https://github.com/backtony/blog-code/blob/master/aws/img/3/3-26.PNG?raw=true)  

db 이름을 주고 생성해줍니다. 파라미터 그룹 설정은 [여기](https://velog.io/@backtony/Spring-TravisCI-CodeDeploy-Nginx-%ED%99%9C%EC%9A%A9%ED%95%9C-CICD#%EC%9A%B4%EC%98%81%ED%99%98%EA%B2%BD%EC%97%90-%EB%A7%9E%EB%8A%94-%ED%8C%8C%EB%9D%BC%EB%AF%B8%ED%84%B0-%EC%84%A4%EC%A0%95%ED%95%98%EA%B8%B0)를 참고하여 진행하시면 됩니다.
<br>

## Bastion에서 RDS 접속해보기
```
Bastion EC2에 접속한 뒤
mysql -u <user> -p -h <rdsEndPoint>

예시
mysql -u soma1218 -p -h gjgs-vpc-mysql-8-0-23.ap-northeast-2.rds.amazonaws.com
```


## 로컬 MySQL Workbench로 RDS 접속해보기
![그림27](https://github.com/backtony/blog-code/blob/master/aws/img/3/3-27.PNG?raw=true)  
+ SSH Hostname : Bastion IP 입력
+ SSH Username : ec2-user
+ SSH Key File : Bastion 접속 pem 키
+ MySQL Hostname : RDS EndPoint
+ MySQL Service Port : 3306
+ Username : RDS 계정
+ password : RDS 패스워드

Workbench를 이용하면 Bastion EC2로 SSH로 접속한 뒤 RDS에 접근할 수 있습니다.

<br>

# 4. Load Balancer
---
Auto Scaling을 구성하기 앞서 먼저 Auto Scaling에서 사용할 Load Balancer을 만들겠습니다.

## Load Balancer란?
Load Balancer는 애플리케이션 트래픽을 Amazon EC2 인스턴스, 컨테이너, IP 주소, Lambda 함수, 가상 어플라이언스와 같은 여러 대상에 자동으로 분산시켜 안정적인 AWS서버 환경을 운용하는데에 도움을 주는 서비스입니다. 간단하게 말하면, 여러 대의 EC2가 있는 상황에서 수 많은 요청이 들어올 때 이 요청을 분산시켜 각각의 EC2에게 전달해주는 서비스입니다.  

## Load Balancer용 보안 그룹 구성하기
![그림40](https://github.com/backtony/blog-code/blob/master/aws/img/3/3-40.PNG?raw=true)  
보안 그룹 탭에서 위와 같이 Load Balancer에서 사용할 보안 그룹을 만들어 줍니다. http와 https를 열어주었습니다.
<br>

## 대상 그룹 만들기
Load Balancer의 대상을 만드는 작업입니다.
![그림41](https://github.com/backtony/blog-code/blob/master/aws/img/3/3-41.PNG?raw=true)  
왼쪽 탭에서 대상 그룹을 클릭하고 오른쪽 상단에 대상 그룹 만들기를 클릭합니다. 타켓을 Instances로 선택하고 타겟 그룹 이름을 명시합니다. 받는 포트는 80으로 열어주고 만들었던 vpc를 선택해줍니다.

<br><br>

![그림42](https://github.com/backtony/blog-code/blob/master/aws/img/3/3-42.PNG?raw=true)  
Health check 작업입니다. 저는 프로젝트의 /health 요청에 대해 컨트롤러를 만들어서 단순히 health ok 정도로 200 응답을 보내도록 만들어 놓았습니다. 각 항목은 다음과 같습니다.
+ Healthy threshold : 연속으로 몇 번 정상 응답을 해야만 정상 상태로 볼 것인지 지정하는 항목
+ Unhealthy threshold : 연속으로 몇 번 비정상 응답을 해야만 정상 상태로 볼 것인지 지정하는 항목
+ Timeout : 타임아웃 시간으로 응답이 몇 초 이내로 오지 않을 경우 비정상 응답으로 판단할지 지정하는 항목
+ Interval : 몇 초 간격으로 인스턴스의 상태를 물어볼 것인지 지정하는 항목
+ Success Codes : 어떤 HTTP 응답 코드를 줬을 경우 정상 상태로 판단할 것인지 지정하는 항목

이후에 Register targets를 정하는 페이지가 나오는데 Auto Scaling 그룹을 지정해줄 것이기 때문에 아무것도 선택하지 않고 Create target group을 해줍니다.
<br>

## Load Balancer 구성하기
![그림43](https://github.com/backtony/blog-code/blob/master/aws/img/3/3-43.PNG?raw=true)  
왼쪽 탭에서 로드 밸런서를 선택하고 오른쪽 상단에 Load Balancer 생성을 클릭합니다. Application Load Balancer를 선택합니다.
<Br><br>

![그림44](https://github.com/backtony/blog-code/blob/master/aws/img/3/3-44.PNG?raw=true)  
이름을 정해주시고 Internet-facing을 선택해줍니다. 
+ Internet-facing : 인터넷에서 트래픽을 받아 로드 밸런서에 등록된 타겟 그룹에 트래픽을 분산 시켜주는 옵션
+ Internal : private 공간에서의 로드 밸런싱만 하는 옵션

<Br><br>

![그림45](https://github.com/backtony/blog-code/blob/master/aws/img/3/3-45.PNG?raw=true)  
vpc를 선택해주고 mapping에는 pub1-2a와 pub2-2c를 선택합니다. 이렇게 해야 로드 밸랜서가 퍼블릭 공간에서 트래픽을 받아서 통신할 수 있기 때문입니다. 
<Br><br>

![그림46](https://github.com/backtony/blog-code/blob/master/aws/img/3/3-46.PNG?raw=true)  
앞서 만든 보안 그룹을 선택하고 타겟 그룹도 앞서 만든 타켓 그룹으로 선택해주고 최종적으로 로드 밸런서를 만들어 줍니다.
<Br>


# 5. Auto Scaling
---
## Auto Scaling 이란?
Auto Scaling은 서버의 과부하, 장애 등과 같이 서비스 불능 상황 발생시 자동으로 서버를 복제하여 서버 대수를 늘려주는 작업을 해주는 AWS 서비스입니다.


## EC2 AMI란?
AMI란 Amazon Machine Images의 약자로 EC2 인스턴스를 그대로 저장해서 재사용 할 수 있도록 만든 것입니다. 현재 서버의 하드웨어, 소프트웨어 설정, 어플리케이션 등 모든것을 그대로 사용가능합니다. Auto Scaling에서 사용할 AMI를 만들겠습니다. 우선 AMI 를 만들 EC2를 만들어야 합니다. EC2에는 Docker와 배포를 위한 CodeDeploy agent를 설치할 것입니다. CodeDeploy로 배포 가능한 인스턴스에는 조건이 있습니다.
+ 올바른 권한을 가진 역할이 필요하다.
+ CodeDeploy Agent가 설치돼 있어야 한다.
+ 애플리케이션이 배포될 경로에 이미 저장돼 있는 파일이 없어야 한다.


CodeDeploy를 설치하기 위해서는 S3에 해당 EC2를 생성할 때 
AMI를 가지고 Auto Scaling 그룹을 만들 때 VPC를 설정하고, 시작 템플릿을 만들 때 보안 그룹을 설정하기 때문에 AMI를 만들기 위해 만드는 EC2의 경우 VPC와 보안그룹의 경우 신경 쓸 필요 없이 아무거나 선택해도 되지만, CodeDeploy Agent와 CloudWatch Agent를 설치하기 위해서는 해당 EC2에 역할이 필요합니다. 기본적인 EC2를 생성한 상태를 가정하고 진행하겠습니다.

## EC2 인스턴스 역할, 인스턴스 프로파일 생성하기
필요한 권한들을 모은 정책을 생성하고 해당 정책을 적용한 역할을 생성하겠습니다. 인스턴스 프로파일은 따로 생성하지 않아도 역할을 생성할 때 자동으로 생성됩니다.
![그림28](https://github.com/backtony/blog-code/blob/master/aws/img/3/3-28.PNG?raw=true)  
AWS에서 IAM을 검색하고 왼쪽 탬에서 정책을 선택하고 오른쪽 상단에 정책 생성을 클릭합니다. 시각적 편집기 옆에 JSON을 클릭하고 아래 코드를 입력해주고 쭉 진행해서 정책을 생성해줍니다.
```
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Action": [
                "s3:Get*",
                "s3:List*"
            ],
            "Effect": "Allow",
            "Resource": "*"
        }
    ]
}
```
EC2 인스턴스에 CodeDeploy Agent 를 설치하기 위해서나 설치된 CodeDeploy Agent가 깃허브나 S3에서 업로드된 파일을 가져오기 위해서나 S3의 읽기 권한이 필요합니다.
<br><br>

![그림29](https://github.com/backtony/blog-code/blob/master/aws/img/3/3-29.PNG?raw=true)  
만든 정책으로 역할을 만들 차례입니다. 왼쪽 탭에서 역할을 클릭하고 오른쪽 상단에 역할 만들기를 클릭합니다. EC2를 선택합니다.
<br><br>

![그림30](https://github.com/backtony/blog-code/blob/master/aws/img/3/3-30.PNG?raw=true)  
위에서 방금 만든 정책과 __CloudWatchAgentServerPolicy__ 정책을 추가한 뒤 쭉 만들어 주면 됩니다.
<br><br>

![그림31](https://github.com/backtony/blog-code/blob/master/aws/img/3/3-31.PNG?raw=true)  
만들어둔 AMI 용 인스턴스에 오른쪽 클릭을 통해 보안 -> IAM 역할 수정을 클릭합니다.
<br><br>

![그림32](https://github.com/backtony/blog-code/blob/master/aws/img/3/3-32.PNG?raw=true)  
방금 만든 역할을 선택해줍니다.
<br>


## AMI용 EC2 세팅
앞서 진행했던 세팅과 마찬가지로 시간은 한국 시간으로, hostname은 알기 쉽도록 설정해줍니다.
### Docker 설치
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

### 부팅 시 Docker 시작
```
sudo vi /etc/rc.d/rc.local

맨 아래 줄에 추가
sudo service docker start

권한 부여
sudo chmod +x /etc/rc.d/rc.local

재부팅
sudo reboot
```

### CodeDeploy 에이전트 설치하기
EC2에 접속하여 CodeDeploy의 요청을 받을 수 있도록 에이전트를 설치해주도록 합니다.
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

### CloudWatch Agent 설치하기
```
# 설치 스크립트 다운
wget https://s3.amazonaws.com/amazoncloudwatch-agent/linux/amd64/latest/AmazonCloudWatchAgent.zip

# 압축 해제 및 zip 파일 제거
unzip AmazonCloudWatchAgent.zip -d AmazonCloudWatchAgent
rm AmazonCloudWatchAgent.zip

# 폴더로 이동
cd AmazonCloudWatchAgent

# 설치
sudo ./install.sh

# 설치 마법사 실행
sudo /opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-config-wizard

=============================================================
= Welcome to the AWS CloudWatch Agent Configuration Manager =
=============================================================
# 운영 체제 선택
On which OS are you planning to use the agent?
1. linux
2. windows
3. darwin
default choice: [1]:
1

# EC2 인스턴스 설치 or 자체 서버 설치
Trying to fetch the default region based on ec2 metadata...
Are you using EC2 or On-Premises hosts?
1. EC2
2. On-Premises
default choice: [1]:
1

# agent run 유저 선택
Which user are you planning to run the agent?
1. root
2. cwagent
3. others
default choice: [1]:
1

# StatsD 데몬 실행 여부
# 사용자 지정 지표를 쉽게 기록할 수 있게 하는 프로토콜
Do you want to turn on StatsD daemon?
1. yes
2. no
default choice: [1]:
2

# CollectD dammon 실행 여부
# StatD와 마찬가지로 사용자 지표 쉽게 기록할 수 있게 하는 프로토콜
Do you want to monitor metrics from CollectD?
1. yes
2. no
default choice: [1]:
2

# CPU, 메모리와 같은 호스트의 지표를 기록하고 싶은지 여부
Do you want to monitor any host metrics? e.g. CPU, memory, etc.
1. yes
2. no
default choice: [1]:
1

# CPU 코어별 기록 여부 -> 추가 요금 발생
Do you want to monitor cpu metrics per core? Additional CloudWatch charges may apply.
1. yes
2. no
default choice: [1]:
2

# 기록 시 가능한 모든 EC2 자원을 기록하고 싶은지 여부
Do you want to add ec2 dimensions (ImageId, InstanceId, InstanceType, AutoScalingGroupName) into all of your metrics if the info is available?
1. yes
2. no
default choice: [1]:
1

# 지표를 기록하는 주기 지정
# 짧을 수록 더 많으 비용 지불
Would you like to collect your metrics at high resolution (sub-minute resolution)? This enables sub-minute resolution for all metrics, but you can customize for specific metrics in the output json file.
1. 1s
2. 10s
3. 30s
4. 60s
default choice: [4]:
4

# 어떤 지표들을 기록할지 지정, 이후에 설명
Which default metrics config do you want?
1. Basic
2. Standard
3. Advanced
4. None
default choice: [1]:
2

# 현재 선택 사항 확인
Current config as follows:
{
	"agent": {
		"metrics_collection_interval": 60,
		"run_as_user": "root"
	},
	"metrics": {
		"append_dimensions": {
			"AutoScalingGroupName": "${aws:AutoScalingGroupName}",
			"ImageId": "${aws:ImageId}",
			"InstanceId": "${aws:InstanceId}",
			"InstanceType": "${aws:InstanceType}"
		},
		"metrics_collected": {
			"cpu": {
				"measurement": [
					"cpu_usage_idle",
					"cpu_usage_iowait",
					"cpu_usage_user",
					"cpu_usage_system"
				],
				"metrics_collection_interval": 60,
				"totalcpu": false
			},
			"disk": {
				"measurement": [
					"used_percent",
					"inodes_free"
				],
				"metrics_collection_interval": 60,
				"resources": [
					"*"
				]
			},
			"diskio": {
				"measurement": [
					"io_time"
				],
				"metrics_collection_interval": 60,
				"resources": [
					"*"
				]
			},
			"mem": {
				"measurement": [
					"mem_used_percent"
				],
				"metrics_collection_interval": 60
			},
			"swap": {
				"measurement": [
					"swap_used_percent"
				],
				"metrics_collection_interval": 60
			}
		}
	}
}
Are you satisfied with the above config? Note: it can be manually customized after the wizard completes to add additional items.
1. yes
2. no
default choice: [1]:
1

# CloudWatch Logs 설정 파일이 있는지 여부
Do you have any existing CloudWatch Log Agent (http://docs.aws.amazon.com/AmazonCloudWatch/latest/logs/AgentReference.html) configuration file to import for migration?
1. yes
2. no
default choice: [2]:
2

# 모니터링 하고 싶은 로그 파일이 있는지 여부
Do you want to monitor any log files?
1. yes
2. no
default choice: [1]:
2

# 설정 값 다시 확인
Saved config file to /opt/aws/amazon-cloudwatch-agent/bin/config.json successfully.
Current config as follows:
{
	"agent": {
		"metrics_collection_interval": 60,
		"run_as_user": "root"
	},
	"metrics": {
		"append_dimensions": {
			"AutoScalingGroupName": "${aws:AutoScalingGroupName}",
			"ImageId": "${aws:ImageId}",
			"InstanceId": "${aws:InstanceId}",
			"InstanceType": "${aws:InstanceType}"
		},
		"metrics_collected": {
			"cpu": {
				"measurement": [
					"cpu_usage_idle",
					"cpu_usage_iowait",
					"cpu_usage_user",
					"cpu_usage_system"
				],
				"metrics_collection_interval": 60,
				"totalcpu": false
			},
			"disk": {
				"measurement": [
					"used_percent",
					"inodes_free"
				],
				"metrics_collection_interval": 60,
				"resources": [
					"*"
				]
			},
			"diskio": {
				"measurement": [
					"io_time"
				],
				"metrics_collection_interval": 60,
				"resources": [
					"*"
				]
			},
			"mem": {
				"measurement": [
					"mem_used_percent"
				],
				"metrics_collection_interval": 60
			},
			"swap": {
				"measurement": [
					"swap_used_percent"
				],
				"metrics_collection_interval": 60
			}
		}
	}
}
Please check the above content of the config.
The config file is also located at /opt/aws/amazon-cloudwatch-agent/bin/config.json.
Edit it manually if needed.

# SSSM parameter store의 설정 여부
Do you want to store the config in the SSM parameter store?
1. yes
2. no
default choice: [1]:
2

# 종료
Program exits now.

# 설치된 에이전트 실행, file 뒤의 경로는 방금 생성한 cloudWatch 에이전트 설정 파일의 경로
sudo /opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-ctl -a fetch-config -m ec2 -c file:/opt/aws/amazon-cloudwatch-agent/bin/config.json -s

# 실행 확인
sudo /opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-ctl -m ec2 -a status
```
status가 running으로 나오면 성공적으로 실행이 된 것입니다. CloudWatch 에이전트는 서비스로 자동으로 등록되어 종료되기 이전에 성공적으로 실행되었다면 시스템이 시작될 때 자동으로 실행됩니다.  
<br>

__cf) CloudWatch 정의 지표 목록__  
+ Basic : 메모리 사용량 + Swap 사용량
+ Standard : Basic 내용 + CPU 사용량 + Disk 사용량
+ Advanced : Standard 내용 + Diskio + Netstat

<Br>

### CloudWatch logs 설정
spring boot에서 생긴 error 로그 파일을 CloudWatch Agent가 인식하고 CloudWatch로 보내도록 하는 설정을 하겠습니다.
![그림94](https://github.com/backtony/blog-code/blob/master/aws/img/3/3-94.PNG?raw=true)  
AWS 홈페이지에서 CloudWatch를 검색하고 왼쪽 탭에서 로그 그룹을 클릭합니다. 로그 그룹을 생성하고 그룹 명을 기억해 둡니다.
<br><br>

다시 EC2로 돌아와서 진행합니다.
```sh
sudo vi /opt/aws/amazon-cloudwatch-agent/bin/config.json

# metrics와 같은 위치에 작성합니다.
# spring log는 logs/error.log 위치에 생성할 예정입니다.
{
    "logs": {
            "logs_collected": {
                    "files": {
                            "collect_list": [
                                    {
                                            "file_path": "/home/ec2-user/logs/error.log",
                                            "log_group_name": "spring-logs",
                                            "log_stream_name": "{instance_id}"
                                    }
                            ]
                    }
            }
    },
    "metrics" : {
        ....
    }
}

# CloudWatch 정지
sudo /opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-ctl -m ec2 -a stop

# 업데이트하고 에이전트 시작
sudo /opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-ctl -a fetch-config -m ec2 -c file:/opt/aws/amazon-cloudwatch-agent/bin/config.json -s

# CloudWatch 상태 확인
sudo /opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-ctl -m ec2 -a status

# 제일 첫 줄에 status가 stopped이면 다음 명령어 입력해서 start 시키기
sudo /opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-ctl -m ec2 -a start
```
<br>

__cf) CloudWatch Agent 주요 명령어__  
```sh
# fetch-config를 통해 config.json 파일의 내용으로 설정 파일을 업데이트하고 시작
sudo /opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-ctl -a fetch-config -m ec2 -c file:/opt/aws/amazon-cloudwatch-agent/bin/config.json -s

# CloudWatch 에이전트 서비스 시작 명령어
sudo /opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-ctl -m ec2 -a start

# CloudWatch 에이전트 중지 명령어
sudo /opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-ctl -m ec2 -a stop

# CloutWatch 에이전트 상태 조회 명령어
sudo /opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-ctl -m ec2 -a status

# CloudWatch 설정 파일 위치
# wizard로 생성한 파일 위치
/opt/aws/amazon-cloudwatch-agent/bin/config.json

# 실제로 CloudWatch 에이전트가 사용하는 설정 파일 위치
/opt/aws/amazon-cloudwatch-agent/stc/amazon-cloudwatch-agent.json

# CloudWatch 에이전트의 로그 파일 위치
/opt/aws/amazon-cloudwatch-agent/logs/amazon-cloudwatch-agent.log
```

<br><br>

![그림77](https://github.com/backtony/blog-code/blob/master/aws/img/3/3-77.PNG?raw=true)  
AWS 홈페이지에서 CloudWatch 를 검색하고 왼쪽 탭에서 지표를 보면 CWAgent라는 이름의 네임스페이스가 사용자 지정 네임스페이스에 추가돼 있음을 확인할 수 있습니다. CloudWatch 기능에 대해서는 제일 마지막부분에 다시 다루겠습니다.

<br>

## AMI 만들기
![그림33](https://github.com/backtony/blog-code/blob/master/aws/img/3/3-33.PNG?raw=true)  
AMI용 EC2에 세팅을 끝냈으니 이제 AMI를 만들 차례입니다. 해당 EC2에 오른쪽 클릭 -> 이미지 및 템플릿 -> 이미지 생성을 클릭합니다.
<br><Br>

![그림34](https://github.com/backtony/blog-code/blob/master/aws/img/3/3-34.PNG?raw=true)  
이름과 설명을 지정해주고 만들어 줍니다. 참고로 이미지를 만들 때 해당 EC2를 종료하고 만들게 되는데 재부팅 안 함 옵션을 선택해주면 재부팅하지 않고 이미지를 생성합니다.
<br>

## AMI 용 보안 그룹 만들기
![그림35](https://github.com/backtony/blog-code/blob/master/aws/img/3/3-35.PNG?raw=true)  
AWS 홈페이지에서 EC2를 검색하고 왼쪽 탭에서 보안 그룹을 클릭 후 오른쪽 상단에 보안 그룹 생성을 클릭합니다. vpc는 전에 만든 vpc로 선택해주고 인바운드 규칙으로 ssh, http, https를 포함시켜 만들어 줍니다.
<br>


## 시작 템플릿에서 사용할 instace-init.sh 만들기
시작 템플릿은 Auto Scaling 할 때 만들어지는 EC2를 정의하는데 사용하는 템플릿입니다. 시작 템플릿을 만드는 과정에서 EC2들이 만들어지고 수행할 동작을 정의하는 Userdata가 있습니다. 그곳에 사용할 instace-init.sh 파일을 만들겠습니다.
![그림48](https://github.com/backtony/blog-code/blob/master/aws/img/3/3-48.PNG?raw=true)  
AWS 홈페이지에서 S3를 검색하고 오른쪽에 버킷 만들기를 클릭합니다. 버킷 이름만 정해주고 그대로 만들어 줍니다.
<br><Br>

![그림49](https://github.com/backtony/blog-code/blob/master/aws/img/3/3-49.PNG?raw=true)  
일단 업로드 할 instance-init.sh 파일을 로컬에 만듭니다. 내용은 다음과 같습니다.
```sh
export DOCKER_ID='id'
export DOCKER_PASSWORD='password'

# 도커 로그인
echo ${DOCKER_PASSWORD} | docker login -u ${DOCKER_ID} --password-stdin

# 가동중인 gjgs 도커 중단 및 삭제
sudo docker ps -a -q --filter "name=gjgs" | grep -q . && docker stop gjgs && docker rm gjgs | true

# 기존 이미지 삭제
sudo docker rmi soma1218/gjgs

# 도커허브 이미지 pull
sudo docker pull soma1218/gjgs

# 도커 run
docker run -d -p 80:8080 -e TZ=Asia/Seoul -v /home/ec2-user/logs:/logs --name gjgs soma1218/gjgs:latest

# 사용하지 않는 불필요한 이미지 삭제 -> 현재 컨테이너가 물고 있는 이미지는 삭제 안됨
docker rmi -f $(docker images -f "dangling=true" -q) || true
```
만들어진 버킷을 클릭하고 만든 파일을 업로드 한 뒤 S3 URI 복사를 해두고 다른 곳에 적어둡니다. 시작 템플릿 만들기에서 사용합니다.
<br>

## 시작 템플릿 만들기
![그림36](https://github.com/backtony/blog-code/blob/master/aws/img/3/3-36.PNG?raw=true)  
AWS 홈페이지에서 EC2를 검색하고 왼쪽 탭에서 시작 템플릿을 클릭하고 오른쪽 상단에 시작 템플릿 생성을 클릭합니다. 이름과 설명을 입력하고 Auto Scaling 지침을 클릭해줍니다. Auto Scaling에서 사용하는 템플릿을 설정하는데 도움이 되도록 알려주는 설명입니다.
<br><br>

![그림37](https://github.com/backtony/blog-code/blob/master/aws/img/3/3-37.PNG?raw=true)  
좀 전에 만든 AMI를 선택하고 인스턴스는 프리 티어로 사용 가능한 인스턴스로 설정해줍니다. 키 페어도 선택해줍니다.
<br><br>

![그림38](https://github.com/backtony/blog-code/blob/master/aws/img/3/3-38.PNG?raw=true)  
보안 그룹은 좀 전에 만든 보안 그룹으로 선택해 줍니다.
<br><br>

![그림39](https://github.com/backtony/blog-code/blob/master/aws/img/3/3-39.PNG?raw=true)  
고급 세부 정보에서 IAM 인스턴스 프로파일로 AMI용 EC2에 적용해줬던 인스턴스 프로파일을 선택해줍니다. 인스턴스 프로파일은 역할을 만드는 과정에서 자동으로 생성되었을 것입니다. 
<br><br>

![그림47](https://github.com/backtony/blog-code/blob/master/aws/img/3/3-47.PNG?raw=true)  
Auto Scaling으로 AMI기반으로 만들어지고 수행할 내용들을 적는 곳입니다.
```
#!/bin/bash

cd /home/ec2-user
aws s3 cp s3://instace-init.sh/instance-init.sh . --region ap-northeast-2
chmod +x instance-init.sh
sh instance-init.sh
shred -uv instance-init.sh
```
1. ec2-user 디렉토리로 이동합니다.
2. s3에서 instance-init.sh 파일을 받아옵니다.
3. 해당 파일에 권한을 주고 실행합니다.
4. 해당 파일을 삭제합니다.

<br>



## Auto Scaling 그룹 만들기
![그림50](https://github.com/backtony/blog-code/blob/master/aws/img/3/3-50.PNG?raw=true)  
AWS 홈페이지에서 EC2를 검색하고 왼쪽 탭 하단의 Auto Scaling 그룹을 클릭합니다. 그리고 오른쪽 상단에 Auto Scaling 그룹 생성을 클릭합니다. 그룹 이름을 정해주시고 전에 만들어준 시작 템플릿을 선택합니다.
<BR><br>

![그림51](https://github.com/backtony/blog-code/blob/master/aws/img/3/3-51.PNG?raw=true)  
vpc도 전에 만든 것을 선택해주고 서브넷은 pri1-2a, pri2-2c를 선택해줍니다. Auto Scaling으로 생성되는 EC2를 private 서브넷으로 배치시키는 것입니다.
<BR><br>


![그림52](https://github.com/backtony/blog-code/blob/master/aws/img/3/3-52.PNG?raw=true)  
기존 로드 밸런서에 연결을 선택하고 전에 만든 로드 밸런서를 선택합니다. 이렇게 하면 로드 밸런서의 target 그룹에서 선택하지 않았던 register target이 Auto Scaling그룹으로 선택됩니다. 상태확인에 ELB를 클릭해주시고 적당한 유예 시간도 설정해줍니다. CloudWatch 도 체크해줍니다.
<BR><br>

![그림53](https://github.com/backtony/blog-code/blob/master/aws/img/3/3-53.PNG?raw=true)  
Auto Scaling의 그룹 크기와 조정 정책을 정해줍니다. 내용은 다음과 같습니다.
+ 원하는 용량, 목표 용량 : 그룹 내 목표로 하는 인스턴스 수입니다. 사용자가 임의로 지정할 수도 있고 자동 조정 정책에 맞게 이 값이 바뀔 수 있습니다. 이 값이 변경되면 Auto Scaling 그룹에서는 현재 정상 상태인 인스턴스 수가 그 값과 같아질 때까지 인스턴스를 생성하거나 줄입니다.
+ 최소/최대 용량 : 이 그룹 내 최소/최대로 유지할 인스턴스 수입니다. 예를 들어 최소가 2이면 자동 조정 정책이나 사람에 의해 인스턴스 수가 2개 미만으로 줄일 수 없습니다.
+ 조정 정책 : 자동으로 인스턴스를 늘리고 줄이기 위한 정책들을 표시합니다. 기본적인 설정으로는 CPU 사용량, 네트워크 사용량으로 처리할 수 있습니다. CloudWatch를 통한 추가 설정을 하면 메모리 사용량, 디스크 사용량, 외부 지표 등 다양한 경우에 대해서도 인스턴스 수를 조절할 수 있습니다.
<BR><br>

![그림54](https://github.com/backtony/blog-code/blob/master/aws/img/3/3-54.PNG?raw=true)  
태그를 추가하면 생성되는 EC2에 태그를 줄 수 있습니다. 이렇게 최종적으로 만들어 주면 현재 인스턴스에 EC2가 2개 추가된 것을 확인할 수 있습니다.
<BR>

# 6. CodeDeploy 
---
## CodeDeploy 역할 만들기
CodeDeploy의 역할에는 기본적인 CodeDeploy 역할에 Auto Scaling으로 인한 EC2에 관련된 정책을 추가해줘야합니다.
![그림57](https://github.com/backtony/blog-code/blob/master/aws/img/3/3-57.PNG?raw=true)  
AWS 홈페이지에서 IAM을 검색하고 왼쪽 탭에서 역할을 클릭한 뒤 오른쪽 상단에 역할 만들기를 클릭합니다. 그리고 CodeDeploy를 선택학 하단 사용 사례 선택에도 CodeDeploy를 선택하고 다음으로 넘깁니다.
<br><Br>

![그림58](https://github.com/backtony/blog-code/blob/master/aws/img/3/3-58.PNG?raw=true)  
정책을 확인하고 넘깁니다.
<br><Br>

![그림59](https://github.com/backtony/blog-code/blob/master/aws/img/3/3-59.PNG?raw=true)  
태그와 역할 이름을 알아보기 쉬운 것으로 정하고 만들어 줍니다.
<br><Br>

![그림60](https://github.com/backtony/blog-code/blob/master/aws/img/3/3-60.PNG?raw=true)  
왼쪽 탭에서 정책을 클릭하고 오른쪽 상단에 정책 생성을 클릭합니다. 상단에 시각적 편집기 옆에 있는 JSON을 클릭하고 다음을 입력해줍니다.
```
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Sid": "VisualEditor0",
            "Effect": "Allow",
            "Action": [
                "iam:PassRole",
                "ec2:CreateTags",
                "ec2:RunInstances"
            ],
            "Resource": "*"
        }
    ]
}
```
<br>

![그림61](https://github.com/backtony/blog-code/blob/master/aws/img/3/3-61.PNG?raw=true)  
왼쪽 탭에서 다시 역할을 클릭하고 방금 전에 만들었던 code deploy 역할이 들어있는 역할을 클릭해줍니다.
<br><Br>

![그림62](https://github.com/backtony/blog-code/blob/master/aws/img/3/3-62.PNG?raw=true)  
정책 연결을 클릭합니다.
<br><Br>

![그림63](https://github.com/backtony/blog-code/blob/master/aws/img/3/3-63.PNG?raw=true)  
좀 전에 만든 정책을 검색해서 연결해줍니다.
<br>

## CodeDeploy 애플리케이션 생성

![그림64](https://github.com/backtony/blog-code/blob/master/aws/img/3/3-64.PNG?raw=true)  
AWS 홈페이지에서 CodeDeploy를 검색하고 왼쪽 탭에서 애플리케이션을 클릭하고 오른쪽 상단에 애플리케이션 생성을 클릭합니다. 애플리케이션 이름을 정하고 생성해줍니다.
<br><br>

![그림65](https://github.com/backtony/blog-code/blob/master/aws/img/3/3-65.PNG?raw=true)  
배포 그룹 생성을 클릭해줍니다.
<br><br>

![그림66](https://github.com/backtony/blog-code/blob/master/aws/img/3/3-66.PNG?raw=true)  
배포그룹 이름을 정해주고 좀 전에 만들어줬던 역할을 추가해줍니다. 블루/그린 배포 방식을 선택합니다.
<br><br>

![그림67](https://github.com/backtony/blog-code/blob/master/aws/img/3/3-67.PNG?raw=true)  
Auto Scaling 그룹을 선택해줍니다. 배포 성공한 후에 원본 인스턴스를 종료하는 시간을 테스트를 빨리 하기 위해서 5분으로 설정했습니다.실제 환경에서는 상황에 맞게 설정하면 될 거 같습니다. 배포는 절반씩 진행하기 위해 HalfAtTime을 선택했습니다.
<br><br>

![그림68](https://github.com/backtony/blog-code/blob/master/aws/img/3/3-68.PNG?raw=true)  
대상 그룹은 Auto Scaling 그룹을 선택해주시고 최종적으로 생성하시면 됩니다.
<br>

# 7. Jenkins
---
## Jenkins EC2 만들기
![그림55](https://github.com/backtony/blog-code/blob/master/aws/img/3/3-55.PNG?raw=true)  
jenkins EC2는 Gitlab, GitHub 등에서 hook 요청을 받아야 하기 때문에 외부 인터넷과 통신이 가능한 pub2에 위치시키고 퍼블릭 ip 할당을 황성화 해줍니다.
<br><Br>

![그림56](https://github.com/backtony/blog-code/blob/master/aws/img/3/3-56.PNG?raw=true)  
ssh는 전체를 열고 8080포트는 제 ip와 hook으로 들어오는 ip만 열어두었습니다. 
<br><Br>

이후의 Jenkins EC2 세팅은 [여기](https://velog.io/@backtony/Spring-Jenkins-Docker-DockerHub-GitHub-%ED%99%9C%EC%9A%A9%ED%95%9C-CICD#6-jenkins-ec2-%EC%84%B8%ED%8C%85%ED%95%98%EA%B8%B0) 포스팅의 Jenkins EC2 세팅하기부터 SSH Servers 세팅만 제외하고 똑같이 진행하시면 됩니다. 링크를 건 포스팅에서는 ssh를 통해 배포를 진행하고 있고 지금 포스팅에서는 CodeDeploy로 배포를 진행할 것이기 때문에 CodeDeploy 세팅을 진행할 차례입니다.
<Br>

## Jenkins CodeDeploy 세팅
![그림69](https://github.com/backtony/blog-code/blob/master/aws/img/3/3-69.PNG?raw=true)  
Jenkins 플러그인 관리에서 AWS CodeDeploy를 설치합니다.
<br><Br>

![그림70](https://github.com/backtony/blog-code/blob/master/aws/img/3/3-70.PNG?raw=true)  
작성해놓은 프로젝트로 들어가서 Build 부분을 수정합니다. 전에 포스팅과 프로젝트 명이 다르나 구조는 똑같습니다.
```sh
docker build -t soma1218/gjgs:1.0 -f Dockerfile.dev .
echo $PASSWORD | docker login -u $USERNAME --password-stdin
docker push soma1218/gjgs:1.0
docker rmi soma1218/gjgs:1.0
mkdir deploy
cp scripts/*.sh deploy/
cp appspec.yml deploy/
```
deploy 디렉토리를 만들고 script와 appspec을 deploy 디렉토리로 옮기는 작업이 추가되었습니다.
<Br>

![그림71](https://github.com/backtony/blog-code/blob/master/aws/img/3/3-71.PNG?raw=true)  
빌드 후 조치 추가에서 Deploy an application to AWS CodeDeploy를 선택합니다. 저는 이미 만들어서 회색으로 표기되고 있습니다.
<br><br>

![그림72](https://github.com/backtony/blog-code/blob/master/aws/img/3/3-72.PNG?raw=true)  
+ AWS CodeDeploy Application Name : AWS 에서 만든 CodeDeploy 애플리케이션 이름
+ AWS CodeDeploy Deployment Group : CodeDeploy에 연결한 배포 그룹명
+ AWS CodeDeploy Deployment Config : 배포 방식 설정
+ AWS Region : AWS 리전 이름
+ S3 Bucket : 버킷명
+ S3 Prefix : 버킷 내 폴더명
+ Subdirectory : Jenkins 서버에서 업로드할 파일이 있는 디렉토리 지정
+ include files : upload할 파일 입력 (deploy 내의 모든 파일 -> **)

<br>

![그림73](https://github.com/backtony/blog-code/blob/master/aws/img/3/3-73.PNG?raw=true)  
Jenkins에서 AWS에 접근하기 위한 키를 입력해야하므로 AWS 홈페이지로 가서 사용자를 만들어야합니다. AWS 홈페이지에서 IAM을 검색하고 왼쪽 탭에서 사용자를 클릭합니다. 오른쪽 상단에 사용자 추가를 클릭합니다. 이름을 정하고 프로그래밍 방식 액세스를 선택합니다.
<br><Br>

![그림74](https://github.com/backtony/blog-code/blob/master/aws/img/3/3-74.PNG?raw=true)  
상단의 기존 정책 직접 연결을 클릭하고 AWSCodeDeployFullAccess와 AmazonS3FullAccess를 추가해서 사용자를 만들어 줍니다. 최종적으로 생성하면 나오는 액세스키와 비밀키를 잘 보관해둡니다.
<br><Br>

![그림75](https://github.com/backtony/blog-code/blob/master/aws/img/3/3-75.PNG?raw=true)  
다시 Jenkins 설정 화면으로 돌아와서 발급받은 액세스 키와 비밀키를 입력해줍니다.
<br><br>

![그림76](https://github.com/backtony/blog-code/blob/master/aws/img/3/3-76.PNG?raw=true)  
모든 구축이 끝났습니다. 이제 푸시를 한 뒤 AWS 홈페이지에서 CodeDeploy의 배포 상태를 확인해보면 정상적으로 작동하는 것을 확인할 수 있습니다.

<Br>

# 8. CloudWatch
---
## CloudWatch란
AWS CloudWatch는 AWS에서 제공하는 AWS 내 자원과 애플리케이션에 대한 모니터링 및 관리 서비스입니다. 서비스가 실행되면서 발생하는 모든 로그와 지표 정보들을 수집해서 한눈에 볼 수 있도록 시각화하는 모니터링 서비스의 역할을 합니다. 그리고 이렇게 수집된 값들을 이용해 자동화된 작업을 수행할 수 있게 해주는 관리 서브스의 역할도 합니다.  
CloudWatch는 대시보드, 이벤트, 경보, 로그, 지표로 구성돼 있습니다. 여기서 지표는 언제 어떤 항목의 값이 무엇이었는지를 기록한 값으로 AWs 내의 대부분 서비스 이력은 이 지표로 기록됩니다. 별도로 설정하지 않아도 서비스를 사용하기만 하면 AWS에서 자동으로 기록해줍니다. 기본적으로 제공하는 지표 외에도 사용자가 지정한 지표들을 직접 기록할 수도 있습니다. 지표는 다음과 같은 항목으로 구성되어 있습니다.
+ 네임스페이스 : 비슷한 지표들을 모아두기 위해 사용하는 네임스페이스입니다. 예를 들어 Auto Sacling 그룹, 애플리케이션 로드 밸런서 같은 항목들이 네임스페이스가 될 수 있습니다.
+ 이름 : 지표의 이름입니다. 인스턴스의 수, CPU 사용률 같은 항목이 이름이 될 수 있습니다.
+ 차원 : 지표를 더욱 쉽게 분류하기 위한 값입니다. 키/값 쌍을 최대 10개를 등록할 수 있으며 같은 차원으로 표시된 지표만 따로 모아서 볼 수 있습니다. 예를 들어 Auto Scaling 그룹 내 존재하는 모든 인스턴스의 CPU 지표를 종합해서 확인하고 싶다면 인스턴스의 CPU 지표를 기록할 때 Auto Scaling 그룹의 이름으로 차원을 지정해서 기록하면 됩니다. 하나의 지표에 여러 종류의 차원으로 보고 싶다면 같은 지표를 여러번 기록하면 됩니다.
+ 시간 : 지표로 기록한 값이 발생한 시간입니다.
+ 값 : 지표로 기록할 값입니다.
+ 단위 : 지표로 기록할 값의 단위입니다.

<br>

기본적으로 제공되는 지표들 중 많이 사용되는 지표들은 다음과 같습니다.
+ CPU 사용량
+ 디스크 사용량
+ 로드 밸런서의 응답 시간, 총 요청 수, 5XX 대 HTTP 응답 수
+ 네트워크 트래픽
+ DB 레플리카 랙 시간
+ 캐시 hit/miss 비율

<br>

## CloudWatch 대시보드
CloutWatch는 지표들을 한눈에 보여주기 위해 대시보드 기능을 제공합니다. 월별 최대 50개의 지표를 제공하는 대시보드 3개가 프리 티어로 제공됩니다. 
![그림78](https://github.com/backtony/blog-code/blob/master/aws/img/3/3-78.PNG?raw=true)  
AWS 홈페이지에서 CloudWatch를 검색하고 왼쪽 탭에서 대시보드를 클릭하고 바로 우측에 대시보드 생성을 클릭합니다. 이름을 정하고 위젯 유형을 선택합니다
<br><Br>

![그림79](https://github.com/backtony/blog-code/blob/master/aws/img/3/3-79.PNG?raw=true)  
지표를 선택해줍니다.
<br><Br>

![그림80](https://github.com/backtony/blog-code/blob/master/aws/img/3/3-80.PNG?raw=true)  
CWAgent를 선택해줍니다.
<br><Br>

![그림81](https://github.com/backtony/blog-code/blob/master/aws/img/3/3-81.PNG?raw=true)  
오른쪽 하단에 있는 ImageId,InstanceId, InstanceType 차원을 클릭해줍니다.
<br><Br>

![그림82](https://github.com/backtony/blog-code/blob/master/aws/img/3/3-82.PNG?raw=true)  
항목중 오른쪽 끝에 지표 이름이 mem-used_percent인 값을 추가하고 위젯을 생성해줍니다.
<br><Br>

![그림83](https://github.com/backtony/blog-code/blob/master/aws/img/3/3-83.PNG?raw=true)  
위젯이 생성된 것을 확인할 수 있습니다. 추가적으로 위젯을 추가하고 싶다면 상단에 위젯 추가버튼을 누르고 방금 했던 것처럼 원하는 지표들을 추가할 수 있습니다. 대시보드는 수정하상이 일어나도 상단에 저장버튼을 클릭하지 않으면 저장되지 않으므로 반드시 작업이 끝나면 저장을 클릭해 줘야 합니다. 
<br><Br>

![그림84](https://github.com/backtony/blog-code/blob/master/aws/img/3/3-84.PNG?raw=true)  
위젯의 X축 값을 보면 현지 시간이 아닌것을 확인할 수 있습니다. 현지 시간으로 변경하기 위해서는 우측 상단에 사용자 지정을 클릭하고 시간을 현지 시간대로 변경하면 됩니다.
<br><Br>

![그림80](https://github.com/backtony/blog-code/blob/master/aws/img/3/3-80.PNG?raw=true)  
로드 밸런서의 지표도 추가할 수 있습니다. 위젯 추가를 위와 같은 방식으로 진행하고 지표에서 ApplicationELB를 선택해줍니다.
<br><Br>

![그림85](https://github.com/backtony/blog-code/blob/master/aws/img/3/3-85.PNG?raw=true)  
우측에 AppELB별 지표를 선택해줍니다. 그리고 나오는 페이지에서 원하는 로드밸런서의 HTTPCode_ELB_5XX_Count와 RequestCount 지표를 선택하고 위젯을 생성해줍니다.
<br><Br>

![그림86](https://github.com/backtony/blog-code/blob/master/aws/img/3/3-86.PNG?raw=true)  
대시보드 화면에 위젯이 추가된 것을 확인할 수 있습니다.
<Br>

## CloudWatch 경보
AWS에서는 중요한 지표들에 대해 조건들을 미리 만들어두고 사람 대신 모니터링할 수 있는 경보 기능을 제공합니다. 경보는 기간과 값을 이용해 조건을 걸어두어 생성할 수 있습니다. 조건을 기준으로 정상과 경보 상태를 왔다갔다 하게 되는데 평상시에는 경보가 정상 상태로 존재하다가 문제가 되는 조건을 넘는 경우 경보 상태로 변경됩니다. 시간이 지나 정상 조건을 만족하는 경우 다시 정상 상태로 돌아옵니다.  
CloudWatch 경보는 단순히 상태만 변경되는 것이 아니라 상태 변경에 따른 작업들을 자동으로 진행할 수도 있습니다. 상태가 변경되면 이메일, 문자 알림을 줄 수도 있고 AWS내 리소스들을 알아서 조작할 수도 있습니다. 예를들면 Auto Scaling 기능도 CloudWatch의 경보를 이용해서 만들어진 것입니다. Auto Scaling 그룹의 조정 정책을 만들 때 세워둔 기준들은 CloudWatch 경보로 등록되어 경보 상태가 정상에서 경보로 변경되면 Auto Scaling 그룹의 목표 용량의 값을 현재 +-1 로 변경하는 작업을 수행하게 되는 것입니다.  
<br>

## 이메일 알림주기
로드 밸런서가 받는 요청에 대해 경보 1개를 생성해서 경보 상태로 변경되는 경우와 다시 정상 상태로 변경되는 경우에 이메일을 받도록 설정해보겠습니다.

![그림87](https://github.com/backtony/blog-code/blob/master/aws/img/3/3-87.PNG?raw=true)  
AWS 홈페이지에서 CloudWatch를 검색하고 왼쪽 탭에서 경보를 클릭하고 오른쪽 상단에 경보 생성을 클릭합니다. 지표 선택 -> 하단에 AWS 네임스페이스에 ApplicationELB 선택 -> AppELB별 지표 선택 -> 사용하고 있는 로드 밸런서의 RequestCount 지표 선택 -> 통계를 합계로 변경 -> 기간 1분으로 설정
<br><br>

![그림88](https://github.com/backtony/blog-code/blob/master/aws/img/3/3-88.PNG?raw=true)  
선택한 지표에 대해 경보의 임계값을 상세히 지정할 수 있는 화면입니다. 요청량에 대한 경보를 생성하고 있으니 요청량의 특정 값을 임곗값으로 정할 수 있습니다. 임곘값을 한 번 넘었다고 상태가 바로 변경되는 것이 아니라 일정 기간을 두고 그 기간 동안 임곗값을 넘긴 경우에만 경보 상태로 변경되도록 설정할 수도 있습니다. 가장 하단에 보시면 데이터 포인트로 2번 이상 넘긴 경우에만 상태를 변경하도록 설정했습니다.
<br><br>

![그림89](https://github.com/backtony/blog-code/blob/master/aws/img/3/3-89.PNG?raw=true)  
경보 상태일 경우 알림을 보낼 방식을 생성해줍니다. 다 작성하시면 하단에 주제 생성을 클릭해줍니다.
<br><br>

![그림90](https://github.com/backtony/blog-code/blob/master/aws/img/3/3-90.PNG?raw=true)  
정상 상태로 돌아올 경우 알림을 보낼 방식을 생성해줍니다. 다 작성하시면 하단에 주제 생성을 클릭해줍니다. 주제를 생성하면 이메일로 subscribe 요청이 올텐데 허용을 해줘야합니다.
<br><br>

![그림91](https://github.com/backtony/blog-code/blob/master/aws/img/3/3-91.PNG?raw=true)  
만들고 나면 이렇게 생성된 것을 확인할 수 있습니다. 
<br><br>

![그림92](https://github.com/backtony/blog-code/blob/master/aws/img/3/3-92.PNG?raw=true)  
경보를 생성한 로드 밸런서에 1분 간격으로 5회 이상씩 2번 보내주면 경보 상태로 변경되고 이메일을 받게 됩니다.

<br>

## CloudWatch Logs
CloudWatch Logs는 CloudWatch 기능 중 로그를 관리하는 기능입니다. CloudWatch Agent가 로그도 모니터링해서 CloudWatch Logs로 전송하는 역할도 합니다. 로그는 영구적으로 보존할 수도 있으며 만료 시간을 지정핵서 자동으로 삭제되게 처리할 수도 있습니다. 용량 단위로 요금이 책정되기 때문에 적당한 만료 시간을 두는 것이 좋습니다. CloudWatch Logs는 크게 세 항목으로 구성되어 있습니다.
+ 로그 이벤트 : 로그를 기록하는 애플리케이션이나 자원에서 로그를 기록한 줄의 모음입니다. 독립적인 이벤트로 볼 수 있는 로그줄의 묶음이기 때문에 로그 파일에서 한 줄이 될 수도 있고 여러 줄이 될 수도 있습니다. 예를 들어, 애플리케이션 서버 로그에서 클라이언트 요청의 시작부터 응답을 줄 때까지 쌓인 여러 줄의 로그를 하나의 이벤트로 볼 수 있습니다.
+ 로그 스트림 : 동일한 소스에서 기록된 로그 이벤트들을 시간순으로 모아둔 스트림입니다. 예를 들어 특정 인스턴스에서 발생한 서버 호출 로그들이 하나의 스트림이 됩니다.
+ 로그 그룹 : 여러 로그 스트림을 하나로 모아둔 곳입니다. 예를 들어 여러 인스턴스에서 발생한 서버 호출 로그들을 모아둔 그룹이 존재하게 됩니다.

<br>

### Spring Log 설정하기
```yml
# application.yml
logging:
  file:
    name: logs/error.log
  pattern:
    file: "[%d{yy-MM-dd HH:mm:ss}][%-5level][%logger.%method:line%line] - %msg%n"
  level:
    root: error
  logback:
    rollingpolicy:
      max-file-size: 50MB
      max-history: 14
      total-size-cap: 1GB
```
/logs 위치에 error.log로 로그가 생성됩니다.  
배포에서 사용하는 deploy.sh에서 아래와 옵션으로 도커를 실행하도록 합니다. -v 으로 로그 위치를 마운트해줘야합니다.
```sh
docker run -d -p 80:8080 -e TZ=Asia/Seoul -v /home/ec2-user/logs:/logs --name gjgs soma1218/gjgs:latest
```
<Br><br>

![그림95](https://github.com/backtony/blog-code/blob/master/aws/img/3/3-95.PNG?raw=true)  
이제 에러 로그가 찍히는 로직에 요청을 보내보면 CloudWatch로 가는 것을 확인할 수 있습니다.
<br><br>


# cf) Bastion에서 EC2 접속하기
Bastion EC2에서 운영용 EC2로 접속하기 위해서는 pem키가 필요합니다. 따라서 pem키를 Bastion EC2에 옮겨줍니다.
```
로컬에서 pem키 
vim pem키

예시
vim ./.ssh/example.pem

출력으로 나오는 키 복사 후 Bastion EC2로 접속한 뒤 pem 키 파일 만들기
sudo vim pem키

예시 
sudo vim example.pem

복사한 키를 붙여넣고 esc -> :wq 로 저장

운영 EC2 접속하기
ssh -i pem키 ec2-user@EC2의IP

예시
ssh -i example.pem ec2-user@14.225.~~

// 참고 //
permission error가 뜰 경우
sudo chmod 700 ./.ssh
sudo chmod 600 ./.ssh/authorized_keys
```
