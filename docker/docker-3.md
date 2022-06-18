# 도커 - 이미지 만들기

## 1. 도커 파일이란?
---
+ 도커 이미지를 만들기 위한 설정파일, 컨테이너가 어떻게 행동해야 하는지에 대한 설정들을 정의해주는 것

<br>

### 도커 파일 만드는 순서
1. 베이스 이미지 명시 (파일 스냅샷에 해당)
2. 추가적으로 필요한 파일을 다운 받기 위한 몇가지 명령어 명시(파일 스냅샷에 해당)
3. 컨테이너 시작시 실행 될 명령어 명시

<br>

### 베이스 이미지란?
![그림1](https://github.com/backtony/blog-code/blob/master/docker/img/3/3-1.PNG?raw=true)

+ 도커의 이미지는 여러개의 레이어로 구성되어 있고 그중에서 이미지의 기반의 되는 부분이 베이스 이미지
+ 레이어는 중간 단계의 이미지라고 생각

<br>

## 2. 도커 파일 만들기
---
기본적인 양식은 다음과 같다.
```dockerfile
# 베이스 이미지 명시, 이미지 생성시 기반이 되는 이미지 레이어
# from <이미지 이름> <태그> 형식 
# 태그 명시하지 않으면 가장 최신것으로 자동 다운
FROM baseImage

# 도커이미지가 생성되기 전에 수행할 쉘 명령어
# 추가적으로 필요한 파일들을 다운
RUN command

# 컨테이너가 시작되었을 때 실행할 실행 파일 또는 쉘 스크립트
# 해당 명령어는 dockerfile 내부에서 1회만 사용 가능
# 컨테이너 시작시 실행될 명령어 
CMD ["executable"]
```
<br>

## 3. 도커 파일로 도커 이미지 만들기
---
### 도커 파일로 어떻게 이미지가 생성되는가?
![그림2](https://github.com/backtony/blog-code/blob/master/docker/img/3/3-2.PNG?raw=true)

+ 도커 파일에 입력된 것들이 도커 클라이언트에 전달되어서 도커 서버가 인식하여야 이미지가 생성

```
docker build ./ 
docker build .
```
+ 위의 명령어는 해당 디렉토리 내에서 dockerfile을 찾아서 도커 클라이언트에게 전달
+ build 뒤의 .와 ./ 둘다 현재 디렉토리를 의미

<Br>

### 생성 과정

```dockerfile
FROM alpine

CMD ["echo", "hello"]
```
도커파일을 위와 같이 만들었다면 아래 그림과 같은 과정을 거친다.
<br>

![그림3](https://github.com/backtony/blog-code/blob/master/docker/img/3/3-3.PNG?raw=true)

1. BASE가 되는 alpine 이미지를 가져온다.
2. BASE가 되는 이미지를 통해 임시 컨테이너를 생성한다.
    - BASE의 파일 스냅샷을 임시 컨테이너로 옮긴다.
3. CMD에 작성한 시작 시 실행될 명령어도 임시 컨테이너로 옮긴다.
4. 임시 컨테이너로 새로운 이미지를 만든다.

<br>

## 4. 이미지에 이름 주기
---
![그림4](https://github.com/backtony/blog-code/blob/master/docker/img/3/3-4.PNG?raw=true)

일반적으로 이름을 줄 때 위와 같은 형식을 지켜서 이름을 준다.
```
예시
docker build -t backtony/hello:latest ./
docker run -it backtony/hello
```
<br>

## 5. 이미지 관련 명령어
---

+ docker image ls 또는 docker images : 설치되어 있는 이미지 조회
+ docker image inspect 이미지:태그 : 이미지 상세 정보 조회
+ docker rmi 이미지 : 이미지 삭제
+ docker rmi -f 이미지 : 컨테이너가 있어서 삭제가 안될 경우, 컨테이너까지 다 삭제
+ docker rmi $(docker images -q) : 이미지 전체 삭제
+ docker image push 이미지:태그 : 도커허브에 이미지 업로드






<Br><Br>

__참고__  
<a href="https://www.inflearn.com/course/%EB%94%B0%EB%9D%BC%ED%95%98%EB%A9%B0-%EB%B0%B0%EC%9A%B0%EB%8A%94-%EB%8F%84%EC%BB%A4-ci#" target="_blank"> 따라하며 배우는 도커와 CI환경</a>  

