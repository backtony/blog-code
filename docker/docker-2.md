# 도커 - 기본적인 명령어 

## 1. 이미지 내부 파일 시스템 구조
---
![그림1](https://github.com/backtony/blog-code/blob/master/docker/img/2/2-1.PNG?raw=true)

```
docker run alpine ls
```
원래는 apline을 실행해야 하는데 시작 명령어를 무시하고 ls로 동작한다. 컨테이너의 현재 디렉토리의 파일 리스트를 보여준다.
<br>

## 2. 컨테이너 나열하기
---
![그림2](https://github.com/backtony/blog-code/blob/master/docker/img/2/2-2.PNG?raw=true)

+ 현재 실행중인 컨테이너를 보여준다.

<br>

![그림3](https://github.com/backtony/blog-code/blob/master/docker/img/2/2-3.PNG?raw=true)

+ CONTAINER ID : 컨테이너의 고유한 아이디 해쉬값
+ IMAGE : 컨테이너 생성시 사용한 도커 이미지
+ COMMAND : 컨테이너 시작시 실행될 명령어
+ CREATED : 컨테이너가 생성된 시간
+ STATUS : 컨테이너의 상태
+ PORT : 컨테이너가 개방한 포트와 호스트에 연결한 포트
+ NAMES : 컨테이너 고유 이름
    - 컨테이너 생성시 --name 옵션으로 주지 않으면 도커 엔진이 임의로 형용사와 명사 조합해서 설정
    - 중복 불가능, rename 명령어로 수정 가능


```
docker ps -a 
```
모든 컨테이너 나열
<br>

## 3. 도커 컨테이너의 생명주기
---
![그림4](https://github.com/backtony/blog-code/blob/master/docker/img/2/2-4.PNG?raw=true)

```
docker run 이미지이름 = docker create 이미지이름 + docker start 컨테이너아이디/이름
```
+ create은 컨테이너를 만들고 하드디스크에 스냅샷을 넣기
+ start는 실행 명령어가 컨테이너에 들어가서 실행
+ run 은 create와 start를 함께 하는 작업

<br>

### stop과 kill
```
docker kill 컨테이너아이디/이름
docker stop 컨테이너아이디/이름
```
+ stop과 kill 모두 실행중인 컨테이너를 중지
+ stop은 하던 작업을 완료하고 컨테이너 중지
+ kill은 하던 작업을 바로 중지하고 컨테이너 중지

<Br>

### 컨테이너 삭제
```
docker rm 아이디/이름
docker rm `docker ps -a -q`  // 모든 컨테이너 삭제, 백틱
```
+ 컨테이너를 중지해야만 삭제가 가능

<br>

```
docker rmi 이미지id
```
+ 도커 이미지 삭제

<br>

```
docker system prune
```
+ 한번에 컨테이너, 이미지, 네트워크 모두 삭제
+ 도커를 쓰지 않을때 모두 정리하고 싶을 때 사용
+ 하지만 실행중인 컨테이너에는 영향 X

<br>

## 4. 실행 중인 컨테이너에 명령 전달
---
```
docker exec 컨테이너아이디

예시
docker exec 컨테이너아이디 ls

docker exec -it 컨테이너아이디 명령어
```
+ -it 는 interactive의 i와 terminal의 t를 합친 것
+ -it 옵션을 붙이지 않으면 해당 컨테이너에서 명령을 수행하고 바로 컨테이너 밖으로 나와버려서 추가적인 작업이 불가능
+ -it 옵션을 붙이면 해당 컨테이너에서 명령을 수행하고 그대로 컨테이너 안에 있기에 추가적인 작업이 가능
<br>

## 5. 실행 중인 컨테이너에서 터미널 사용
---
실행중인 컨테이너에 명령어를 전달할 때마다 
```
docker exec -it 컨테이너아이디 명령어 
```
위와 같은 코드를 계속 작성해야한다면 매우 불편하다. 즉, 명령어 앞에 부분을 반복적으로 계속 사용해야한다는 뜻이다. 이런 경우 컨테이너 안에 쉘이나 터미널 환경으로 접속하면 이런 불편함을 없앨 수 있다.
<br>

![그림5](https://github.com/backtony/blog-code/blob/master/docker/img/2/2-5.PNG?raw=true)

+ 윈도우에서는 powershell, 맥에서는 zsh, bash를 사용하는데 보편적으로 어디서나 사용이 가능한 것은 sh
    - 예를 들면, docker exec -it 컨테이너ID sh 를 입력하면 이제는 앞에 중복적인 코드 작성 없이 명령어만 입력하면 해당 컨테이너에서 바로 명령어가 동작
    - 일반적인 나가는 방법은 control + c인데, 여기서는 control + d를 사용








<Br><Br>

__참고__  
<a href="https://www.inflearn.com/course/%EB%94%B0%EB%9D%BC%ED%95%98%EB%A9%B0-%EB%B0%B0%EC%9A%B0%EB%8A%94-%EB%8F%84%EC%BB%A4-ci#" target="_blank"> 따라하며 배우는 도커와 CI환경</a>  

