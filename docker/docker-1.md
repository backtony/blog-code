# 도커 - 기본

## 1. 도커란?
---
컨테이너를 사용하여 응용프로그램을 더 쉽게 만들고 배포하고 실행할 수 있도록 설계된 도구이며, 컨테이너 기반의 오픈소스 가상화 플랫폼 생태계
<br>

## 2. 컨테이너란?
---
![그림1](https://github.com/backtony/blog-code/blob/master/docker/img/1/1-1.PNG?raw=true)

+ 컨테이너는 코드와 모든 종속성을 패키지화하여 응용 프로그램이 한 컴퓨팅 환경에서 다른 컴퓨팅 환경으로 빠르고 안정적으로 실행되도록 하는 소프트웨어의 표준 단위
+ 간단하고 편리하게 프로그램을 실행 시켜주는 것
+ 컨테이너는 컨테이너 안의 다양한 프로그램, 실행환경을 컨테이너로 추상화하고 동일한 인터페이스를 제공하여 프로그램의 배포 및 관리를 단순하게 해준다. 일반 컨테이너의 개념에서 물건을 손쉽게 운송해주는 것처럼 프로그램을 손쉽게 이동 배포 관리를 할 수 있게 해준다고 보면 된다.

<br>

### 컨테이너 이미지란?
![그림2](https://github.com/backtony/blog-code/blob/master/docker/img/1/1-2.PNG?raw=true)

+ 코드, 런타임, 시스템 도구, 시스템 라이브러리 및 설정과 같은 응용 프로그램을 실행하는 데 필요한 모든 것을 포함하는 가볍고 독립적이며 실행 가능한 소프트웨어 패키지
+ 도커 이미지를 이용해서 도커 컨테이너를 생성하고, 컨테이너가 실행되면 어플리케이션이 컨테이너 안에서 돌아간다. 따라서 컨테이너를 이미지의 인스턴스라고 보면 된다.
  - 예를 들어 카카오톡이라고 한다면 도커 이미지는 카카오톡을 실행하는데 필요한 모든 설정, 종속성들을 가지고 있고 이를 이용해서 컨테이너를 만들고 컨테이너가 실행하면 카카오톡이 컨테이너 안에서 실행된다.




<br>

## 3. 항상 도커를 사용할 때는
---
![그림3](https://github.com/backtony/blog-code/blob/master/docker/img/1/1-3.PNG?raw=true)

1. 도커 CLI에 커맨드 입력
2. 도커 서버(도커 Daemon)이 그 커맨드를 받아서 그것에 따라 이미지를 생성하든 컨테이너를 실행하든 모든 작업을 수행

<br>

## 4. 이미지 컨테이너 동작 과정
---
![그림4](https://github.com/backtony/blog-code/blob/master/docker/img/1/1-4.PNG?raw=true)

도커 이미지 안에는 시작시 실행 될 명령어와 파일의 스냅샷이 들어있다. 파일 스냅샷은 디렉토리나 파일을 카피한 것으로 보면 된다.
<br>


![그림5](https://github.com/backtony/blog-code/blob/master/docker/img/1/1-5.PNG?raw=true)

docker run kakaotalk 명령어를 입력했다면 컨테이너의 하드디스크 부분에 파일의 스냅샷이 들어가게 된다. 

<br>

![그림6](https://github.com/backtony/blog-code/blob/master/docker/img/1/1-6.PNG?raw=true)
run 명령어를 통해 컨테이너가 실행될 때 명령이 실행되어 커널을 통해 카카오톡 실행파일을 작동 시켜서 컨테이너 안에 카카오톡이 실행된다.
<br>

![그림7](https://github.com/backtony/blog-code/blob/master/docker/img/1/1-7.PNG?raw=true)






<Br><Br>

__참고__  
<a href="https://www.inflearn.com/course/%EB%94%B0%EB%9D%BC%ED%95%98%EB%A9%B0-%EB%B0%B0%EC%9A%B0%EB%8A%94-%EB%8F%84%EC%BB%A4-ci#" target="_blank"> 따라하며 배우는 도커와 CI환경</a>  

