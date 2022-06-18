# HTTP - 인터넷 네트워크

## 1. IP (인터넷 프로토콜)
---
![그림1](https://github.com/backtony/blog-code/blob/master/http/img/1/1-1.PNG?raw=true)

+ IP 인터넷 프로토콜 역할
  - 지정한 IP 주소에 데이터 전달
  - 패킷이라는 통신 단위 데이터 전달
+ IP 패킷
  - 출발지 IP, 목적지 IP, 전송 데이터, 기타..

클라이언트에서 서버에 데이터를 전달하기 위해서 IP 패킷을 전달하고 반대로도 마찬가지 이다. 이 과정에서 지나치는 노드는 일정하지 않다.

+ IP 프로토콜의 한계
  - 비연결성 : 패킷을 받을 대상이 서비스 불능 상태 또는 대상이 없어도 패킷을 전송
  - 비신뢰성 : 패킷이 사라지거나 순서대로 오지 않을 수 있다
  - 프로그램 구분 : 같은 IP를 사용하는 서버에서 통신하는 애플리케이션이 둘 이상이면??

<BR>

## 2. TCP
---
![그림2](https://github.com/backtony/blog-code/blob/master/http/img/1/1-2.PNG?raw=true)

IP 프로토콜의 한계를 극복하기 위해서 IP 위에 TCP나 UDP를 올려서 보완해준다.  
<BR>

![그림3](https://github.com/backtony/blog-code/blob/master/http/img/1/1-3.PNG?raw=true)

프로그램이 메시지를 생성하고 SOCKET 라이브러리를 통해 TCP/IP로 3 way handshake을 실행해 서버와 연결한다. 연결을 확인한 뒤 전송 데이터에 TCP 정보를 한 번 감싼다. TCP 정보에는 출발지 PORT, 목적지 PORT, 전송 제어, 순서, 검증 정보 등이 들어있다. 그리고 IP로 감싸고 이걸 보낸다고 보면 된다. TCP를 붙여주면 다음과 같은 특징이 있다.  
+ TCP 3 way handshake
+ 데이터 전달 보증 : 클라이언트가 데이터를 전송하면 서버가 데이터를 잘 받았다고 다시 전달해준다.
+ 순서 보장 : 패킷1, 패킷2, 패킷3 순서로 보냈는데 서버에서 1,3,2 순서로 받았다면 틀린부분 3부터 다 버리고 클라이언트에게 다시 보내라고 전달한다.

![그림4](https://github.com/backtony/blog-code/blob/master/http/img/1/1-4.PNG?raw=true)

위 그림이 TCP 3 way handshake를 나타낸 그림이다. 클라이언트에서 SYN(전송 요청)을 하고 이를 받은 서버는 ACK(요청 수락)과 클라이언트에게도 SYN(접속 요청)을 보낸다. 그럼 클라이언트가 ACK(요청 수락)을 다시 서버에 보내면 서로 연결되었음을 확인할 수 있다. 그리고 나서 데이터를 전송한다.  

<BR>

## 3. UDP
---
+ 사용자 데이터그램 프로토콜
  - TCP는 이미 구축되어있어서 수정 불가능 -> UDP를 사용해 수정
  - 그냥 내가 원하는대로 만들어낼 때 사용
+ 기능이 거의 없고 IP와 같다.
  - IP + PORT + 체크섬 정도만 추가된 것
  
<BR>

## 4. PORT
---
![그림5](https://github.com/backtony/blog-code/blob/master/http/img/1/1-5.PNG?raw=true)

pc에는 1개의 ip가 할당되어있다. 근데 하나의 ip로 게임도하고 음악도 듣고 있다면 하나의 ip로 여러 패킷이 들어온다. 이때 어떤 건 음악용 패킷이고 어떤건 게임용 패킷인지 구분할 때 사용하는 것이 port이다. TCP/IP 패킷 정보에는 출발지 PORT, 도착지 PORT 정보가 담겨있기 때문에 서버쪽에서도 이것을 이용해서 데이터를 전달할 수 있게 된다.  
0 ~ 65535 PORT까지 할당할 수 있는데 0 ~ 1023 PORT 까지는 잘 알려진 포트라서 사용하지 않는 것이 좋다.

<BR>

## 5. DNS
---
DNS는 도메인 네임 시스템이다. IP는 기억하기도 어렵고 변경될 수 있다. 따라서 DNS는 도메인 명을 IP주소로 변환시켜준다고 보면 된다.  
![그림6](https://github.com/backtony/blog-code/blob/master/http/img/1/1-6.PNG?raw=true)

도메인 명만 기억하고 있다면 도메인 명을 치면 DNS 서버에서 도메인에 해당하는 IP를 응답해주고 클라이언트는 그 IP를 가지고 접속하게 되는 것이다. 이렇게 되면서 복잡한 IP를 기억하지 않아도 되고 IP가 바뀌어도 도메인 명만 기억하고 있다면 문제없다. 당연히 IP가 바뀌면 DNS 서버에서 IP를 수정해줘야하긴 한다.

<br>

## 6. URI
---
![그림7](https://github.com/backtony/blog-code/blob/master/http/img/1/1-7.PNG?raw=true)

+ Uniform : 리소스 식별하는 통일된 방식
+ Resoucre : 자원, URI로 식별할 수 있는 모든 것
+ Identifier : 다른 항목과 구분하는데 필요한 정보

URI가 URL과 URN을 포함한다고 보면 되고 URN은 거의 사용하지 않으므로 URI와 URL을 같다고 봐도 무방하다.
<BR>

### URL 문법
![그림8](https://github.com/backtony/blog-code/blob/master/http/img/1/1-8.PNG?raw=true)

+ schema
  - 주로 프로토콜 사용
    - 프로토콜 : 어떤 방식으로 자원에 접근할 것인가 하는 약속 규칙
    - ex) http, https
  - http는 80포트, https는 443포트 사용
+ userinfo
  - 사용자정보를 포함한 인증인데 거의 사용 안함
+ host
  - 도메인 명 또는 ip 주소 직접 입력
+ port
  - 접속 포트
  - 일반적으로 생략, http는 80, https는 443
+ path
  - 리소스 경로, 계층적 구조
+ query
  - key = value 형태
  - ?로 시작하고 &로 추가
  - query parameter, query string으로 불린다. 모든 데이터가 string 타입의 파라미터로 넘어오기 때문
+ fragment
  - html 내부 북마크에 사용
  - 서버에 전송하는 정보가 아님


## 7. 웹 브라우저 요청 흐름
---
![그림9](https://github.com/backtony/blog-code/blob/master/http/img/1/1-9.PNG?raw=true)

```
https://www.google.com:443/search?q=hello&hl=ko
```
1. 웹브라우저에 위와 같은 요청을 보내면 DNS를 조회해서 ip와 port정보를 가져온다. 그리고 HTTP 요청 메시지를 생성한다. 
2. SOCKET 라이브러리를 통해 TCP/IP로 3 way handshake을 실행해 서버와 연결한다.
3. 운영체제 TCP/IP 계층으로 데이터 전송을 하기 위해 데이터를 전달한다.
4. HTTP 메시지가 포함된 TCP/IP 패킷을 생성한다.
5. 패킷 정보가 인터넷으로 보내진다.
6. 서버에 요청 패킷이 도착하여 패킷 껍데기는 버리고 HTTP 메시지를 서버가 해석한다.
7. HTTP 응답 메시지를 마찬가지 방식으로 패킷을 생성하여 응답 패킷을 전달한다.
8. 응답 패킷이 도착하면 웹 브라우저가 HTML 렌더링하여 화면에 보여준다.







<Br><Br>

__참고__  
<a href="https://www.inflearn.com/course/http-%EC%9B%B9-%EB%84%A4%ED%8A%B8%EC%9B%8C%ED%81%AC#" target="_blank"> 모든 개발자를 위한 HTTP 웹 기본 지식</a>   


