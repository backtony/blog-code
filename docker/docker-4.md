# 도커 - COPY, WORKING DIRECTORY, VOLUME

## 1. COPY
---
```dockerfile
FROM node:10

WORKDIR /usr/src/app

COPY package.json ./

RUN npm install

COPY ./ ./

CMD ["node", "server.js"]
```
+ 말 그대로 로컬에 있는 파일을 도커 컨테이너에 복사하는 것
+ ./ ./ : 현재 디렉토리에 있는 모든 파일을 컨테이너의 ./로 복사
    - WORDIR 설정시 ./로 명시해도 WORKDIR로 복사되는데 아래서 다시 설명함
+ node에 관한 이야기
    - package.json은 의존성을 명시하는 곳이므로 대부분 변경되지 않음
    - package.json을 따로 copy하지 않고 한번에 ./ ./ 로 하면 계속 불필요하게 종속성을 다운받음
    - 둘이 분리해주면 종속성을 변경하지 않는 이상 캐시를 이용해 빠르게 빌드    



## 2. Working Directory
---
+ 이미지안에서 어플리케이션 소스 코드를 갖고 있을 디렉토리를 의미
+ 이 디렉토리가 어플리케이션에 working 다랙토리가 된다.

<br>

### Working Directory를 설정하는 이유
+ 베이스 이미지에 이미 같은 폴더명 혹은 파일명이 있을 가능성
+ 모든 파일이 한 디렉토리에 있는 경우 정리 X
+ 따라서 모든 어플리케이션을 위한 소스들은 Working Directory를 따로 만들어 보관


```dockerfile
FROM node:10

WORKDIR /usr/src/app

COPY package.json ./

RUN npm install

COPY ./ ./

CMD ["node", "server.js"]
```
+ WORKDIR 설정시 컨테이너 접근하면 워크 디렉토리로 접근한다.
+ 카피도 ./ 로 카피하지만 워킹 디렉토리를 설정했다면 워킹 디렉토리로 카피된다.

<br>

## 3. Volume
---
![그림1](https://github.com/backtony/blog-code/blob/master/docker/img/4/4-1.PNG?raw=true)

+ 복사 : 로컬에 있는 것을 그대로 복사해와서 별개로 사용
+ Volume : 로컬에 있는 파일을 계속 인지하면서 변화를 반영

<br>

### Volume 사용해서 애플리케이션 실행
```dockerfile
FROM node:10

WORKDIR /usr/src/app

COPY package.json ./

RUN npm install

COPY ./ ./

CMD ["node", "server.js"]
```

```
docker run -d -p 5000:8080 -v /usr/src/app/node_modules -v $(pwd):/usr/src/app backtony/nodejs
```
+ 경로가 usr로 시작하는 이유는 dockerfile의 WORKDIR에 명시해줬기 때문
+ 첫 -v는 매핑시키지 않은 것인데 이것은 매핑하지 말라는 의미
+ 뒤의 -v는 pwd로 현재 디렉토리와 컨테이너 디렉토리를 :로 구분하여 매핑
+ -d 는 백그라운드 모드


<Br><Br>

__참고__  
<a href="https://www.inflearn.com/course/%EB%94%B0%EB%9D%BC%ED%95%98%EB%A9%B0-%EB%B0%B0%EC%9A%B0%EB%8A%94-%EB%8F%84%EC%BB%A4-ci#" target="_blank"> 따라하며 배우는 도커와 CI환경</a>  

