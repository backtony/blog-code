# 도커 - Docker Compose

## 1. Docker Compose
---
+ 서로 다른 컨테이너끼리는 아무런 설정없이는 서로에게 접근할 수 없음
+ 멀티 컨테이너 상황에서 쉽게 네트워크를 연결시켜주기 위해서는 Docker Compose 사용

<Br>


## 2. 설정 방식
![그림1](https://github.com/backtony/blog-code/blob/master/docker/img/5/5-1.PNG?raw=true)


```yml
# docker-compose.yml

version: "3"
services:
  redis-server:
    image: "redis"
  node-app:
    build: .
    ports:
     - "5000:8080"
```
+ docker-compose는 yml 형식
+ version : 도커 컴포즈 버전
+ services : 실행하려는 컨테이너들 정의 시작부
+ redis-server : 컨테이너 이름
+ image : 컨테이너에서 사용하는 이미지
+ node-app : 컨테이너 이름
+ build : 도커파일을 읽어서 이미지를 빌드해야 하므로 도커파일의 경로 명시하는 곳, .이면 현재 디렉토리
+ ports : 포트 맵핑 -> 로컬 포트 : 컨테이너 포트

<Br>

## 3. docker compose 작동 명령어
```
docker-compose build 
docker-compose up
docker-compose up --build 
docker-compose up --no-build

docker-compose up -d

docker-compose down
```
+ build : 이미지를 빌드하기만 하며, 컨테이너를 시작하진 않음
+ up : 이미지가 존재하지 않을 경우에만 빌드하며(이미지 존재하면 빌드 생략), 컨테이너를 시작
+ up --build : 필요하지 않을 때도 강제로 이미지 빌드하며, 컨테이너 시작
+ up --no-build : 이미지 빌드 없이, 컨테이너 시작 (이미지가 없을 시 실패)
+ -d : detached 모드, 앱을 백그다룬드에서 실행, 앱에서 나오는 output 표출 X
+ down : docker compose 종료




<Br><Br>

__참고__  
<a href="https://www.inflearn.com/course/%EB%94%B0%EB%9D%BC%ED%95%98%EB%A9%B0-%EB%B0%B0%EC%9A%B0%EB%8A%94-%EB%8F%84%EC%BB%A4-ci#" target="_blank"> 따라하며 배우는 도커와 CI환경</a>  

