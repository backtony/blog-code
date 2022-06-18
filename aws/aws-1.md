# AWS - Route 53 도메인 등록, 로드밸런서 연결, SSL/TLS 인증서 설치

# 1. 도메인
---
클라이언트가 요청을 보내는 서버마다 고유 IP 주소를 가지고 있으나 이는 친화적이지 않습니다. 예를 들어, 웹 사이트의 주소가 24.243.xx.xx 의 ip 주소를 갖고 있다고 가정한다면, 사용자들이 이를 외워서 접속하기는 매우 어렵기 때문에 사용자들이 외울 수 있는 naver.com과 같은 도메인 주소가 필요합니다. 또한, 도메인 주소가 없다면 서버의 IP주소가 변경되는 경우 기존 사용자들이 접속할 수 없게 됩니다. 따라서 운영 서버라면 도메인 주소는 반드시 필요합니다.
<br>

# 2. 동작 방식
---
![그림1](https://github.com/backtony/blog-code/blob/master/aws/img/aws/1/9-1.PNG?raw=true)  

1. 웹 브라우저 주소창에 test.com을 입력하고 엔터를 칩니다.
2. 웹 브라우저에서 가까운 DNS 서버에 test.com이라는 도메인의 실제 ip주소를 알고 있는지 물어봅니다.
3. 해당 DNS서버가 모른다면 그다음 DNS서버에게 물어봅니다.
4. test.com의 실제 ip주소를 알고 있는 DNS서버를 만나면 해당 서버에서 ip주소인 xx.xx.xx.xx를 알려줍니다.
5. 웹 브라우저에서 xx.xx.xx.xx 라는 주소로 페이지 조회 요청을 보냅니다.

DNS 서버는 도메인과 그 도메인에 연결된 IP주소들을 관리하는 서버입니다.  
도메인을 등록하기 위해서는 도메인 네임 등록 대행자에 돈을 내고 사용 가능한 도메인을 사야합니다. 도메인을 산 뒤 원하는 ip주소를 도메인에 연결을 요청하면 도메인 네임 등록 대행자는 DNS 서버들에 도메인과 IP주소를 등록합니다. 도메인 등록 대행자는 모든 도메인의 정보가 담긴 데이터베이스 같은 도메인 네임 레지스트리에 사용자 대신 도메인을 등록하는 역할을 합니다. 등록 시 같은 도메인이 여러 사람들에게 중복되어 발급되지 않도록 보장하는 역할도 합니다.
<Br>

# 3. Route 53을 이용한 도메인 등록
---
![그림2](https://github.com/backtony/blog-code/blob/master/aws/img/aws/1/9-2.PNG?raw=true)  
AWS에서 Route 53을 검색하고 맨 오른쪽에 도메인 등록을 클릭합니다.
<Br><br>

![그림3](https://github.com/backtony/blog-code/blob/master/aws/img/aws/1/9-3.PNG?raw=true)  
원하는 도메인을 장바구니에 추가하고 우측 하단에 계속을 클릭합니다. 그리고 추가적인 정보들을 적어줍니다.  
최종적으로 만들어 주면 도메인 등록까지 시간이 조금 걸립니다.
<Br><br>

![그림4](https://github.com/backtony/blog-code/blob/master/aws/img/aws/1/9-4.PNG?raw=true)  
등록이 완료되면 왼쪽 등록된 도메인 탭에서 확인할 수 있습니다.
<br>

# 4. 로드 밸런서에 도메인 등록
---
도메인을 로드 밸런서에 연결하면 도메인 주소로 로드 밸런서에서 관리하고 있는 서버들에 접속할 수 있게 됩니다.

![그림5](https://github.com/backtony/blog-code/blob/master/aws/img/aws/1/9-5.PNG?raw=true)  
왼쪽 탭에서 호스트 영역을 클릭하고 만든 도메인 이름을 클릭합니다.
<br><br>

![그림6](https://github.com/backtony/blog-code/blob/master/aws/img/aws/1/9-6.PNG?raw=true)  
우측에 레코드 생성을 클릭합니다.
<br><br>

![그림7](https://github.com/backtony/blog-code/blob/master/aws/img/aws/1/9-7.PNG?raw=true)  
레코드 유형은 A - Ipv4를 선택하고 우측 상단에 별칭바를 클릭해서 활성화 시켜줍니다. Application/Classic Load Balancer에 대한 별칭을 선택하시고, 서울을 선택합니다. 그다음 원하는 자신의 로드 밸런서를 선택하고 레코드를 생성해줍니다.
<br><Br>

![그림8](https://github.com/backtony/blog-code/blob/master/aws/img/aws/1/9-8.PNG?raw=true)  
저는 프로젝트를 띄워놓고 요청을 보내 테스트를 진행했는데 잘 처리되는 것을 확인할 수 있었습니다.  
<br>

# 5. SSL/TLS, HTTPS
---
## SSL/ TLS 인증서, HTTPS 동작 방식
HTTPS(HTTP secure)는 HTTP 프로토콜에 SSL/ TLS 암호화 프로토콜을 이용해 전송되는 데이터를 암호화하는 과정을 추가한 것입니다.  
HTTPS는 기밀성, 무결성, 인증이라는 세 가지 목적을 달성하기 위해 사용합니다. 우리가 사용하고 있는 클라이언트에서 서버까지 요청이 이동할 때 공유기, 인터넷 서비스 제공자 등 수많은 곳을 거쳐 가기 때문에 중간에 제 3자가 해당 내용을 얼마든지 가로채거나 변조할 수 있습니다. 중간에 내용을 가로채가더라도 내용을 읽지 못하게 암호화하는 것이 기밀성이고, 내용을 변조해서 중간자 공격을 못 하게 하는 것은 무결성입니다. 그리고 클라이언트가 통신하고 있는 서버의 신원을 확인할 수 있는 것이 인증입니다.  
따라서 HTTPS 프로토콜은 모든 페이지에 대해서 사용하는 것이 권장됩니다.

![그림9](https://github.com/backtony/blog-code/blob/master/aws/img/aws/1/9-9.PNG?raw=true)  
1. 서버 관리자가 인증서 발급 기관을 통해 서비스하는 도메인에 대한 인증서를 발급 받는다.
2. 발급받은 인증서를 클라이언트와 통신하는 서버 인스턴스의 웹 서버에 설치한다.
3. 이용자가 웹 브라우저 같은 클라이언트에서 https:// 로 시작하는 주소로 접속을 시도한다.
4. 클라이언트는 사용자가 입력한 주소에 해당하는 서버에 SSL/TLS로 암호화한 통신을 하고 싶다고 요청하면서 사용할 수 있는 SSL/TLS 버전 목록과 암호화 알고리즘을 전달한다.
5. 서버는 클라이언트가 전달한 SSL/TLS 버전과 암호화 알고리즘 중 선호하는 것을 고르고 서버에 설치된 인증서를 함께 응답한다. 서버에서 전달하는 이 인증서는 공개 암호화키를 포함하고 있다.
6. 클라이언트는 서버의 인증서가 신뢰할 수 있는 곳으로부터 서명된 것인지 확인하고 대칭 암호화 키로 사용할 키를 생성해서 서버에서 받은 공개 암호화키로 암호화해서 전달한다.
7. 서버와 클라이언트는 이 대칭 암호화 키를 이용해 암복호화가 잘 되는지 테스트를 진행한다.
8. 그다음부터 서버와 클라이언트는 서로 데이터를 주고받을 때 대칭 암호화 키를 이용해 암호화해서 전달한다.

<br>

참고로 http로 요청을 보내도 자동으로 https로 요청이 되는 경우 nginx와 같은 웹 서버에서 https로 리다이렉트하도록 설정해 놓았기 때문입니다.

<Br>

## SSL/TLS 인증서 설치
SSL/TLS 인증서는 HTTPS 통신을 위한 것이기 때문에 클라이언트와 직접 통신하는 서버에 설치돼 있어야 합니다. 로드 밸런서를 이용한 다중 서버 아키텍처에서는 클라이언트와 직접 통신하는 것은 로드 밸런스 서버입니다. 따라서 로드 밸런서의 역할을 하는 서버 인스턴스에 있는 웹 서버에 인증서를 설치애햐 합니다.  
AWS에서는 AWS 콘솔에서 간편하게 클릭만으로 로드 밸런서에 인증서를 추가할 수 있습니다.  
<br><Br>

![그림10](https://github.com/backtony/blog-code/blob/master/aws/img/aws/1/9-10.PNG?raw=true)  
AWS 서비스에서 Certificate Manager를 검색하고 인증서 프로지버닝을 선택합니다.  
<br><Br>

![그림11](https://github.com/backtony/blog-code/blob/master/aws/img/aws/1/9-11.PNG?raw=true)  
공인 인증서 요청을 선택하고 하단에 인증서 요청을 클릭합니다.  
<br><br>

![그림12](https://github.com/backtony/blog-code/blob/master/aws/img/aws/1/9-12.PNG?raw=true)  
앞서 만들어준 도메인을 적어줍니다.
<br><br>

![그림13](https://github.com/backtony/blog-code/blob/master/aws/img/aws/1/9-13.PNG?raw=true)  
DNS 검증을 클릭하고 태그도 적어주시면 이런 화면까지 옵니다. 확인 및 요청을 클릭해줍니다.
<br><br>

![그림14](https://github.com/backtony/blog-code/blob/master/aws/img/aws/1/9-14.PNG?raw=true)  
요청이 완료되면 해당 도메인에 다음과 같은 이름과 값으로 CNAME 기록을 추가하라고 안내 화면을 보여줍니다. 만약 도메인 등록을 AWS가 아닌 외부 업체를 통해서 했다면 해당 업체 사이트에서 저 레코드를 추가해도 되고, AWS Route53을 통해 등록한 경우에는 하단의 Route53에서 레코드 생성 버튼을 클릭하면 자동으로 레코드가 생성됩니다.  
<br><br>

![그림15](https://github.com/backtony/blog-code/blob/master/aws/img/aws/1/9-15.PNG?raw=true)  
레코드를 성공적으로 등록하면 최대 30분 내 AWS에서 레코드가 등록된 것을 확인하고 신정한 인증서의 상태를 발급 완료로 변경해줍니다.
<br><br>

![그림16](https://github.com/backtony/blog-code/blob/master/aws/img/aws/1/9-16.PNG?raw=true)  
이제 발급한 인증서를 로드 밸런서에 등록하기 위해 ec2 - 로드 밸런서 탭에서 로드 밸런서를 선택하고 하단에 리스너를 클릭하고 리스너 추가를 클릭합니다. 
<br><br>

![그림17](https://github.com/backtony/blog-code/blob/master/aws/img/aws/1/9-17.PNG?raw=true)  
HTTPS 프로토콜을 선택하고 전달 대상에서 대상 그룹을 지정합니다. 그리고 인증서로 앞에서 추가한 인증서를 선택하고 우측 상단에 리스너 추가를 클릭해줍니다.
<br><br>

![그림18](https://github.com/backtony/blog-code/blob/master/aws/img/aws/1/9-18.PNG?raw=true)  
완료되면 이렇게 추가된 것을 확인할 수 있습니다.
<br><Br>

![그림19](https://github.com/backtony/blog-code/blob/master/aws/img/aws/1/9-19.PNG?raw=true)  
Postman으로 요청이 성공적으로 가는 것을 확인했습니다.
<br><Br>