# HTTP - 기본

## 1. 클라이언트 서버 구조
---
![그림1](https://github.com/backtony/blog-code/blob/master/http/img/2/2-1.PNG?raw=true)

+ Request Response 구조
+ 클라이언트는 서버에 요청을 보내고 응답을 대기
+ 서버가 요청에 대한 결과를 만들어서 응답

<br>

## 2. 무상태와 상태 유지
---
### 상태 유지
![그림2](https://github.com/backtony/blog-code/blob/master/http/img/2/2-2.PNG?raw=true)

상태 유지(Stateful)에서는 말그대로 상태가 유지된다. 예를 들어 노트북을 구매하는데 수량, 결제수단을 정해야 한다고 하자. 클라이언트는 노트북 2개를 카드로 결제한다는 정보를 따로따로 보냈다고 하자. 즉, 노트북을 산다는 정보, 2개를 산다는 정보, 카드로 결제한다는 정보를 따로따로 나눠서 서버에 보낸 것이다. 노트북을 산다는 정보가 들어왔으면 서버는 그것을 기억하고 있다가 2개를 산다는 정보가 들어왔을 때 서버는 이 클라이언트가 노트북을 산다는 것을 기억하고 있기 때문에 노트북을 2개 산다는 것으로 인지한다. 이러기 위해서는 클라이언트는 계속 같은 서버와만 통신해야 한다. 근데 카드로 결제한다는 정보가 오기 전에 서버 1이 죽어버리면 다른 서버와 통신해야하는데 그럼 정보를 처음부터 다시 다 줘야하는 것이다.  
위와 같은 단점도 있고 만약 하나의 서버에 클라이언트 1,2,3 이 요청한다고 해보자. 서버는 1,2,3과 연결을 맺고 있다. 즉, 클라이언트가 놀고 있어도 계속 서버는 연결을 유지해야 한다는 것이다. 이렇게 서버 자원을 계속 소모하고 있는 것도 단점이다.

<br>

### 무상태
![그림3](https://github.com/backtony/blog-code/blob/master/http/img/2/2-3.PNG?raw=true)

무상태(Stateless)는 말그대로 상태를 유지하지 않는다. 우리가 사용하는 HTTP는 기본이 연결을 유지하지 않는 모델이다. 상태유지에서 설명했던 예시를 그대로 사용해보자. 무상태에서는 클라이언트가 애초에 서버로 정보를 한 번에 보낸다. 노트북 2개를 카드로 결제하겠다는 정보를 한 번에 보낸다는 말이다. 그래서 서버1이 죽어도 상관이 없다. 주던 정보를 똑같이 다른 서버에 주면 되기 때문이다. 따라서 무상태는 응답 서버를 쉽게 바꿀 수 있기에 무한한 서버를 증설이 가능해진다는 장점이 있다.  
위에서 설명한 상태 유지의 두 번째 예시에서 무상태라면 서버 연결을 유지하지 않기 때문에 최소한의 자원을 사용할 수 있다는 장점이 있다. 그런데 연결을 하고 끊고를 계속하다보니 TCP/IP 연결을 새로 맺어야 한다. 3 way handshake 시간이 추가가 되는 것이다. 이런 한계를 극복해서 지금은 HTTP 지속 연결(persistent connections)로 문제를 해결했다. 이전에는 연결 -> 요청 -> html 응답 -> 종료, 연결 -> 요청 -> js 응답 -> 종료, 연결 -> 요청 -> 이미지 응답 -> 종료 이렇게 다 따로 놀았다면 현재는 persistent connections로 연결 -> 요청/html 응답 -> 요청/js 응답 -> 요청/이미지 응답 -> 종료 으로 한 번에 다 처리하고 끊어버린다.  
모든 것을 무상태로 설계하면 좋겠지만 실무에서는 모든 것을 무상태로 설계할 수 없는 경우들이 존재한다. 따라서 무상태 설계를 최대로, 상태 유지는 최소로 사용해야 한다.  


<br>


## 3. HTTP 메시지
---
![그림4](https://github.com/backtony/blog-code/blob/master/http/img/2/2-4.PNG?raw=true)

HTTP의 기본적인 메시지 구조는 오른쪽과 같고 요청과 응답의 경우 기본 구조는 같지만 내용이 약간씩 다르다. 공백 라인은 무조건 한줄 띄어줘야하는 부분이라고 보면 된다.
<br>

### 시작 라인
#### 요청 메시지
```
start-line = request-line
아래는 기본 스펙
request-line = method SP(공백) request-target SP HTTP-version CRLF(엔터)

ex)
GET/search?1=hello&hl=ko HTTTP/1.1
```
HTTP 메서드, 요청대상, HTTP 버전 순이다.
+ HTTP 메서드
    - 종류 : GET, POST, PUT, DELETE...
    - 서버가 수행해야 할 동작 지정
+ 요청 대상
    - 절대경로 / 로 시작하는 경로
+ HTTP 버전

#### 응답 메시지
```
start-line = status-line
status-line = HTTP-version SP status-code SP reason-phrase CRLF

ex)
HTTP/1.1 200 OK
```
HTTP 버전, 상태코드, 이유 문구 순이다.
+ HTTP 버전
+ HTTP 상태 코드
    - 요청 성공, 실패를 나타냄
    - 200 : 성공 
    - 400 : 클라이언트 요청 오류 
    - 500 : 서버 내부 오류
+ 이유 문구 : 사람이 이해할 수 있는 짧은 상태 코드 설명

<BR>

### HTTP 헤더
```
header-field = field-name ":" OWS field-value OWS (OWS:띄어쓰기 허용)

요청 헤더 예시
HOST: www.google.com

응답 헤더 예시
content-Type: text ....
content-length: ....
```
+ HTTP 전송에 필요한 모든 부가정보
+ 위 스펙을 보면 필드네임과 :는 붙여서 사용해야하고 OWS부분은 띄어쓰기를 허용한다는 뜻이다.

<BR>

### HTTP 메시지 바디
+ 실제 전송할 데이터
+ HTML 문서, 이미지, 영상 등등




<Br><Br>

__참고__  
<a href="https://www.inflearn.com/course/http-%EC%9B%B9-%EB%84%A4%ED%8A%B8%EC%9B%8C%ED%81%AC#" target="_blank"> 모든 개발자를 위한 HTTP 웹 기본 지식</a>   


