# HTTP - 일반 헤더


## 1. HTTP BODY
---
![그림1](https://github.com/backtony/blog-code/blob/master/http/img/5/5-1.PNG?raw=true)

+ 메시지 본문을 통해 표현 데이터 전달
+ 메시지 본문 = 페이로드(payload)
+ 표현은 요청이나 응답에서 전달할 실제 데이터
+ 표현 헤더는 표현 데이터를 해석할 수 있는 정보 제공

<br>

## 2. 표현 헤더
---
![그림2](https://github.com/backtony/blog-code/blob/master/http/img/5/5-2.PNG?raw=true)
![그림3](https://github.com/backtony/blog-code/blob/master/http/img/5/5-3.PNG?raw=true)

+ Content-Type : 표현 데이터의 형식 설명
    - 미디어 타입, 문자 인코딩
+ Content-Encoding : 표현 데이터 인코딩
    - 표현 데이터를 압축하기 위해 사용
    - 데이터를 전달하는 곳에서 압축 후 인코딩 헤더 추가
    - 읽은 쪽에서 인코딩 헤더의 정보로 압축 해제
    - ex) gzip(압축), deflate, identity(압축안함)
+ Content-Language : 표현 데이터의 자연 언어 표현
    - ex) ko, en, en-US
+ Content-Length : 표현 데이터의 길이
    - 바이트 단위
    - Transfer-Encoding(전송 코딩)을 사용시 사용 불가


<br>

## 3. 콘텐츠 협상
---
클라이언트가 선호하는 표현 요청을 지정해주고 요청시에 사용하는 것을 협상헤더라고 한다.
+ Accept : 클라이언트가 선호하는 미디어 타입 전달
+ Accept-Charset : 클라이언트가 선호하는 문자 인코딩
+ Accept-Encoding : 클라이언트가 선호하는 압축 인코딩
+ Accept-Language : 클라이언트가 선호하는 자연 언어

### 우선순위
```
Accept-Language: ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7

ko-KR은 q가 생략되어있으므로 1로 가장 최우선
ko;q=0.9 다음 우선순위
en-US;q=0.8 다음 우선순위 ...
```
+ 선호하는 표현 요청을 지정하고 값마다 우선순위를 지정 가능
+ Quality Value(q) 값을 사용하고 0~1 클수록 높은 우선순위를 가진다, 생략시 1

<br>

```
Accept: text/*, text/plain, text/plain;format=flowed, */*

text/plain;format=flowed 최우선
text/plain 그 다음
text/* 그 다음 
*/* 그 다음
```
+ q를 주지 않으면 구체적인 것이 우선순위가 높다. q를 주면 준대로 우선순위 동작

<br>

## 4. 전송 방식
---
+ 단순 전송 : 단순하게 전송하는 방식
+ 압축 전송 : 압축해서 전송하는 방식, Content-Encodinig 사용
+ 분할 전송 : 분할해서 전송하는 방식, Transfer-Encoding 사용
+ 범위 전송 : 범위를 지정해서 전송하는 방식, Content-Range 사용

<br>

## 5. 일반 정보 헤더
---
+ Form : 유저 에이전트의 이메일 정보
    - 일반적으로 사용안함
    - 요청에서 사용
+ Referer : 이전 웹 페이지 주소
    - 현재 요청된 페이지의 이전 웹 페이지 주소
    - A -> B 이동하는 경우 B를 요청할 때 Referer: A를 포함해서 요청
    - Referer를 사용해 유입 경로 분석 가능
    - 요청에서 사용
+ User-Agent
    - 클리이언트의 애플리케이션 정보(웹 브라우저 정보 등)
    - 통계 정보
    - 어떤 종류의 브라우저에서 장애가 발생하는지 파악 가능
    - 요청에서 사용
+ Server : 요청을 처리하는 ORIGIN 서버의 소프트웨어 정보
    - HTTP 요청을 보내면 여러 프록시 서버를 거친다. 최종적으로 도착해서 응답을 해주는 서버가 ORIGIN 서버
    - 응답에서 사용
+ Date : 메시지가 발생한 날짜와 시간
    - 응답에서 사용

<br>

## 6. 특별한 정보 헤더
---
+ Host : 요청한 호스트 정보(도메인)
    - 필수적이며 요청에서 사용
    - 하나의 서버가 여러 도메인을 처리할 때, 하나의 IP 주소에 여러 도메인이 적용되어 있을 때 구분 용도
+ Location : 페이지 리다이렉션
    - 3XX 응답 결과에 Location 헤더가 있으면 Location 위치로 자동 이동(리다이렉션)
    - 201 Create 에서 Location 값은 요청에 의해 생성된 리소스 URI
+ Allow : 허용 가능한 HTTP 메서드
    - 405 Method Not Allowed 에서 응답에 포함하여 허용 가능한 메서드를 알려줌
    - 거의 사용 안함
+ Retry-After : 유저 에이전트가 다음 요청을 하기까지 기다려야 하는 시간
    - 503 Service Unavailable : 서비스가 언제까지 불능인지 알려줄 수 있음
+ Authorization : 클라이언트 인증 정보를 서버에 전달
+ WWW-Authenticate : 리소스 접근시 필요한 인증 방법 정의
    - 리소스 접근시 필요한 인증 방법 정의로 401 Unauthorized 응답과 함께 사용

<br>

## 7. 쿠키 헤더
---
+ Set-Cookie : 서버에서 클라이언트로 쿠키 전달(응답)
+ Cookie : 클라이언트가 서버에서 받은 쿠키를 저장하고, HTTP 요청시 서버로 전달


HTTP는 무상태(Stateless) 프로토콜이므로 요청과 응답을 주고 받으면 연결이 끊어진다. 만약 로그인하고 진행해야하는 상황이라면 모든 요청에 로그인 정보를 넘겨야 하는 것이다. 아래 그림과 같이 쿠키는 모든 요청에 자동으로 포함되기 때문에 이런 문제를 해결할 수 있다.
![그림4](https://github.com/backtony/blog-code/blob/master/http/img/5/5-4.PNG?raw=true)
![그림5](https://github.com/backtony/blog-code/blob/master/http/img/5/5-5.PNG?raw=true)

```
set-cookie 기본 스펙
set-cookie: sessionId=abcde1234; expires=Sat, 26-Dec-2020 00:00:00 GMT; path=/; domain=.google.com; Secure
```

+ 사용처
    - 사용자 로그인 세션 관리
    - 광고 정보 트래킹
+ 쿠키 정보는 항상 서버에 전송
    - 네트워크 트래픽 추가 유발하므로 최소한의 정보만 사용(세션 id, 인증 토큰 등)
    - 서버에 전송하지 않고, 웹 브라우저 내부에 데이터를 저장하고 싶으면 웹 스토리지 참고
+ 생명주기
    - 세션 쿠키: 만료 날짜를 생략하면 브라우저 종료시 까지만 유지
    - 영속 쿠키: 만료 날짜를 입력하면 해당 날짜까지 유지
        - Set-Cookie: expires=Sat, 26-Dec-2020 04:39:21 GMT
        - 만료일이 되면 쿠키 삭제
        - Set-Cookie: max-age=3600 
        - 초단위 만료기간 지정, 0이나 음수를 지정하면 쿠키 삭제
    
+ 도메인
    - 명시: 명시한 문서 기준 도메인 + 서브 도메인을 포함하여 쿠키를 전송
    - ex) domain=example.org 지정해서 쿠키 생성하면 dev.example.org도 쿠키 전송
    - 생략 : 현재 문서 기준 도메인만 적용
    - ex) example.org에서 쿠키 생성하고 domain 지정 생략하면 example.org 에서만 쿠키 전송
+ 경로
    - 도메인으로 한 번 필터해주고 경로로 추가로 필터링
    - 이 경로를 포함한 하위 경로 페이지만 쿠키 전송
    - 일반적으로 path=/ 루트로 지정한다. 한 도메인 안에서 보통 쿠키를 다 전송하기를 원하기 때문
    - ex) path=/home 지정하면 /home , /home/level1 쿠키전달 가능, /hello 전달 불가능
+ 보안
    - Secure : 쿠키는 http, https 구분하지 않고 전송하는데 secure 적용시 https 경우에만 쿠키 전송
    - HttpOnly : 자바스크립트에서 쿠키 접근 불가능하게 하는 방법, HTTP 전송에만 사용
    - SamSite : 요청 도메인과 쿠키에 설정된 도메인이 같은 경우만 쿠키 전송










<Br><Br>

__참고__  
<a href="https://www.inflearn.com/course/http-%EC%9B%B9-%EB%84%A4%ED%8A%B8%EC%9B%8C%ED%81%AC#" target="_blank"> 모든 개발자를 위한 HTTP 웹 기본 지식</a>   


