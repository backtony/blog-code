# TCP / IP

## OSI 모델 vs TCP/IP 모델

![그림6](https://github.com/backtony/blog-code/blob/master/interview/tcpip/img/OSI-6.PNG?raw=true)  

+ 현대의 인터넷이 TCP/IP 모델을 따르고 있는 이유는 OSI 모델이 시장점유율에서 졌기 때문이다.
+ OSI 모델의 Application, Presentation, Session 계층이 TCP/IP 모델에서는 Application으로 합쳐졌다고 보면 된다.

각 계층별로 상세히 알아보기 전에 간단히만 우선적으로 알아보자.  
+ Application Layer
    - 응용 프로세스를 직접 사용하여 직접적인 응용 서비스를 수행하는 계층
    - 말이 어렵지만, SMTP, HTTP, FTP, Talnet 과 같은 프로토콜이 속한 계층
+ Presentation Layer
    - 데이터의 변환, 데이터의 압축, 암호화가 이뤄지는 계층
    - 서로 다른 통신 기기 간에 다른 인코딩을 사용할 수 있기 때문에 이 계층에서 데이터 변환이 이뤄짐
+ Session Layer
    - 세션을 열고 닫고를 제공하는 계층
    - 세션 도커를 통해 체크포인트라는 것으로 동기화를 시켜 세션 복구도 지원
    - 다운로드 중에 끊겼다가 다시 해당 부분부터 다운 가능한 것이 이것 때문
+ Transport Layer
    - 세그멘테이션, 흐름제어, 오류제어를 제공
    - 세그멘티에션
        -  상위 계층 데이터를 받아서 세그먼트 단위로 나누는 작업
    - 흐름제어
        - 데이터 전송량이 다른 기기에서 흐름을 제어하는데 사용
        - 받는 쪽은 10Mbps를 받을 수 있는데 보내는 쪽에서 50Mbps로 보내면 전송량을 낮춰달라고 보내는 방식, 이 반대도 마찬가지
    - 오류제어
        - 보낸 데이터가 정확히 오류 손실이 없는지, 오류가 있다면 다시 보내주는 작업
+ Network Layer
    - IP, 라우터 방식가 속한 계층으로 데이터 전송을 담당
    - 호스트에 IP 번호를 부여하고, 도착지까지 최적의 경로를 찾아줌
+ Data Link Layer
    - 네트워크 계층은 서로 다른 두 네트워크간의 전송을 담당한다면, 데이터 링크 계층은 동일한 네트워크 내에서 전송을 담당
    - 오류제어
        - 데이터 링크 계층의 데이터 단위를 Frame이라고 하는데 여기서 특정 프레임이 오류가 발생하면 그냥 버림
        - Transport 계층에서는 버리지 않고 다시 보내주는 오류 복구까지 함
+ Physical Layer
    - 비트 단위신호들을 전기신호로 변환하는 역할



<br>

## 1계층 - Physical Layer
---
![그림1](https://github.com/backtony/blog-code/blob/master/interview/tcpip/img/OSI-1.PNG?raw=true)  
+ __물리적으로(전선으로) 연결된 두 대의 컴퓨터가 0과 1의 나열을 주고 받을 수 있게 해주는 모듈__
+ 송신 컴퓨터에서는 0과 1의 나열을 아날로그 신호로 바꾸어 전선으로 흘려보낸다.(encoding)
+ 수신 컴퓨터에서는 아날로그 신호가 들어오면 0과 1의 나열로 해석한다.(decoding)
+ 1 계층 모듈은 PHY칩에 하드웨어적으로 구현되어있다.

<br>

## 2계층 - Data Link Layer
---
![그림2](https://github.com/backtony/blog-code/blob/master/interview/tcpip/img/OSI-2.PNG?raw=true)  
+ __같은 네트워크에 있는 여러 대의 컴퓨터들이 데이터를 주고 받기 위해서 필요한 모듈__
+ Framing은 데이터 링크 레이어에 속하는 작업들 중 하나로 데이터를 보내기 전에 구분자들을 붙여서 보내는 작업
+ 랜카드에 하드웨어적으로 구현되어있다.

__동작 과정__  
1. 송신 컴퓨터의 2계층 encoder에 데이터가 인풋으로 들어가 Framing을 통해 구분자가 붙은 데이터가 아웃풋으로 나온다.
2. 아웃풋은 1계층 encoder에 인풋으로 들어가 아날로그 신호로 변조되어 아웃풋으로 나온다.
3. 수신 컴퓨터는 아날로그 신호를 1계층 디코더의 인풋으로 받아 해석한 신호의 데이터를 아웃풋으로 보낸다.
4. 해석된 데이터는 2계층 디코더의 인풋으로 들어가 data라는 데이터가 아웃풋으로 나온다.

<br>

## 3계층 - Network Layer 
---
![그림3](https://github.com/backtony/blog-code/blob/master/interview/tcpip/img/OSI-3.PNG?raw=true)  
+ 수많은 네트워크들의 연결로 이루어지는 inter-network 속에서 어딘가에 있는 목적지 컴퓨터로 데이터를 전송하기 위해, __IP 주소를 이용해 길을 찾고(routing) 자신 다음의 라우터에게 데이터를 넘겨주는 (forwarding) 모듈__
+ 운영체제의 커널에 소프트웨어적으로 구현되어 있다.

<br>

![그림4](https://github.com/backtony/blog-code/blob/master/interview/tcpip/img/OSI-4.PNG?raw=true)  
1. 데이터가 3계층 인코더 인풋으로 들어가 IP주소가 붙어서 아웃풋으로 나온다.(객체의 새로운 필드에 주소를 담는 형식)
2. 2계층 인코더의 인풋으로 들어가 구분자가 붙어 아웃풋으로 나온다.
3. 1계층 인코더의 인풋으로 들어가 아날로그 신호로 바뀌어 아웃풋으로 나온다.
4. 1계층 디코더에서 인풋으로 아날로그 신호를 받아 해석한 데이터가 아웃풋으로 나온다.
5. 2계층 디코더의 인풋으로 들어가 구분자를 제거한 데이터가 아웃풋으로 나온다.
6. 3계층 디코더의 인풋으로 들어가 IP주소가 제거된 데이터가 아웃풋으로 나온다.
7. 3계층에서는 이 IP 주소를 가지고 어디로 보낼지 길을 찾고 다음 라우터에게 넘겨주는 역할을 하는 것이다.
8. IP주소를 통해 어디로 보내야할지 정했다면 데이터를 다시 보내기 위한 작업을 해야한다.
9. 데이터를 3계층 인코더의 인풋으로 넣고 IP 주소가 붙은 아웃풋이 나온다.
10. 2계층 인코더의 인풋으로 들어가 구분자가 붙은 아웃풋이 나온다.
11. 1계층 인코더의 인풋으로 들어가 아날로그 신호로 변조되어 다른 라우터로 보낸다.

<Br>

## 4계층 - Transport Layer
---
+ __포트 번호를 사용하여 도착지 컴퓨터의 최종 도착지인 프로세스까지 데이터가 도돨하게 하는 모듈__
+ 포트 번호는 하나의 컴퓨터에서 동시에 실행되고 있는 프로세스(실행 중인 프로그램)들이 서로 겹치지 않게 가져야하는 정수 값
+ 송신자는 데이터를 보낼 때 데이터를 받을 수신자 컴퓨터에 있는 프로세스의 포트 번호를 붙여서 보낸다.
+ 운영체제의 커널에 소프트웨어적으로 구현되어 있다.

<br>

![그림5](https://github.com/backtony/blog-code/blob/master/interview/tcpip/img/OSI-5.PNG?raw=true)  

1. 데이터가 4계층 인코더에 인풋으로 들어가 포트 번호가 붙어서 아웃풋으로 나온다.
2. 3계층 인코더에 인풋으로 들어가 ip주소가 붙어서 아웃풋으로 나온다.
3. 2계층 인코더에 인풋으로 들어가 구분자가 붙어서 아웃풋으로 나온다.
4. 1계층 인코더에 인풋으로 들어가 아날로그 신호로 변조되어 아웃풋으로 나온다.
5. 1계층 디코더에 인풋으로 들어가 아날로그 신호를 해석한 데이터가 아웃풋으로 나온다.
6. 2계층 디코더에 인풋으로 들어가 구분자가 제거된 데이터가 아웃풋으로 나온다.
7. 3계층 디코더에 인풋으로 들어가 ip주소를 제거한 데이터가 아웃풋으로 나온다.
8. 4계층 디코어에 인풋으로 들어가 포트번호를 제거한 데이터가 아웃풋으로 나온다.

<br>

## 5계층 - Application Layer
---
+ 대표적으로 Application Layer의 프로토콜로 HTTP가 있다.
+ TCP/IP 소켓 프로그래밍(네트워크 프로그래밍) : 운영체제의 Transport Layer에서 제공하는 API를 활용해서 통신 가능한 프로그램을 만드는 것
+ TCP/IP 소켓 프로그래밍을 통해서 누구나 자신만의 Application Layer의 인코더와 디코더를 만들어, 즉, 자신만의 Application Layer 프로토콜을 만들어 사용할 수 있다.

<br>

__대표적인 Application Layer HTTP프로토콜 방식__  
![그림7](https://github.com/backtony/blog-code/blob/master/interview/tcpip/img/OSI-7.PNG?raw=true)  

1. 데이터가 5계층 HTTP 인코더에 인풋으로 들어가서 Status Code, Header 등등 여러가지가 붙어서 아웃풋이 나온다.
2. 4계층 인코더의 인풋으로 들어가 포트번호가 붙어서 아웃풋으로 나온다.
3. 이전과 마찬가지로 계층을 통과해 아날로그 신호로 변조되어 나온다.
4. 수신측 컴퓨터에서 아날로그 신호를 받아 디코더를 통해 쭉 4계층까지 올라가 HTTP 정보와 데이터가 아웃풋으로 나온다.
5. 5계층 HTTP 디코더에 인풋으로 들어가 데이터가 아웃풋으로 나온다.

<Br>

## 계층 정리
![그림8](https://github.com/backtony/blog-code/blob/master/interview/tcpip/img/OSI-8.PNG?raw=true)  
1. Application Layer : HTTPS 프로토콜을 사용하여 데이터를 감싸고 Transport Layer로 내린다.
2. Transport Layer : TCP인지 UDP인지에 대한 정보, 출발지와 도착지에 대한 포트정보를 헤더에 넣고 캡슐화한다. 이 결과물을 __세그먼트__ 라고 한다.
3. Network Layer : 출발지와 도착지에 대한 IP정보를 헤더로 만들어 붙인다. 이 결과물을 __패킷__ 이라고 한다.
4. Data Link Layer : 출발지의 Mac 주소와 가장 가까운 라우터의 맥주소를 붙인다. 이 결과물을 __Frame__ 이라고 한다.
    - 도착지에 대한 맥주소를 넣지 않는 이유는 출발지는 처음에 도착지에 대한 맥주소를 알지 못한다.
    - DHCP와 ARP()를 통해서 라우터의 IP를 받고 맥주소로 변환한 후에 헤더에 넣는다.
    - 앞쪽에 Trailer가 붙게 되는데 이건 오류제어를 위한 정보가 담긴다. 
5. Physical Layer : 전기신호로 변환하여 전송한다.

## 전송 과정
![그림9](https://github.com/backtony/blog-code/blob/master/interview/tcpip/img/OSI-9.PNG?raw=true)  
1. A에서 스위치로 데이터를 보낸다.
    - 스위치는 데이터링크 계층의 대표적인 하드웨어
2. 스위치는 decapsulation를 한번해서 헤더에 대한 정보를 확인하고 해당 주소(라우터)로 데이터를 보냅니다.
3. 라우터 decapsulation 두 번 해서 도착지에 대한 IP주소를 확인하고 라우팅 테이블을 통해 라우팅 시킨 후 B의 맥주소를 파악하여 Layer2에 대한 헤더를 업데이트 한다.(B에 대한 맥주소로 업데이트 시킨다.)
    - 라우터는 네트워크 계층의 하드웨어
4. 라우터에서 스위치로 보내고 스위치는 B로 보내게 된다.







<br><Br>

__참고__  

[[10분 테코톡] 🔮 히히의 OSI 7 Layer](https://www.youtube.com/watch?v=1pfTxp25MA8&t=33s)  
[[10분 테코톡] 👍 파즈의 OSI 7 Layer](https://www.youtube.com/watch?v=Fl_PSiIwtEo&list=PLo0ta52hn1uHQ5iQ3hAeRoMUeLJFIeRew&index=5)  
[[10분 테코톡] 🔮 수리의 TCP/IP](https://www.youtube.com/watch?v=BEK354TRgZ8&list=PLo0ta52hn1uHQ5iQ3hAeRoMUeLJFIeRew&index=5)  

