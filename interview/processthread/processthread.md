# Process 와 Thread

## 1. 프로그램과 프로세스
---
![그림1](https://github.com/backtony/blog-code/blob/master/interview/processthread/img/process-thread-1.PNG?raw=true)  
피자와 피자 레시피를 비유로 들면 이와 같습니다.  
피자 레시피 = 코드가 구현되있는 파일 = 프로그램  
피자 레시피가 피자가 되는 것처럼 코드 파일(프로그램)도 실행되어 사용할 수 있는 무언가!!가 되어야 합니다.  
그 무언가가 __프로세스__ 입니다.  

### 프로그램 -> 프로세스
![그림2](https://github.com/backtony/blog-code/blob/master/interview/processthread/img/process-thread-2.PNG?raw=true)  
프로그램이 프로세스가 되면서 총 2가지 일이 발생합니다.  
프로세스가 필요로 하는 재료들이 메모리에 올라가야 합니다.  
메모리는 Code, Data, Heap, Stack 으로 총 4가지 영역으로 구성되어 있습니다.  
또한, 해당 프로세스에 대한 정보를 담고 있는 Process Control Block(PCB)가 프로세스 생성 시 함께 만들어 집니다.  
<br>

## 2. Process & Thread
---
![그림3](https://github.com/backtony/blog-code/blob/master/interview/processthread/img/process-thread-3.PNG?raw=true)  

우리는 대부분 위 그림과 같이 여러 가지의 프로세스를 동시에 사용합니다. 하지만 원래 한 프로세스가 실행되기 위해서 CPU를 점유하고 있으면 다른 프로세스는 실행상태에 있을 수 없습니다.  
노래 듣다가 코딩을 하기 위해서 인텔리제이를 키면 노래가 꺼지게 되는 것입니다. 그래서 __다수의 프로세스를 동시에 실행하기 위해 여러 개의 프로세스를 시분할__ 로, 즉 짧은 텀을 반복하면서 전환해서 실행을 시키도록 합니다.  
<Br><br>

![그림4](https://github.com/backtony/blog-code/blob/master/interview/processthread/img/process-thread-4.PNG?raw=true)  
위 그림을 화살표 방향대로 보시면 처음에는 PCB_1이 실행상태로 CPU에 적재되고, PCB_2가 실행상태로 되기 위해서는 PCB_1이 다시 준비 상태로 내려가게 됩니다. 이런 일련의 과정을 매우 짧은 텀으로 반복하게 되는 것입니다.  
이러한 행위를 __컨텍스트 스위칭__ 이라고 합니다.  
두 개의 프로세스의 컨텍스트 스위칭을 봤는데도 매우 힘든 작업인 것을 알 수 있습니다.  
<br>

### Thread
그래서 등장하는게 경량화된 프로세스 버전인 __스레드__ 입니다.  
![그림5](https://github.com/backtony/blog-code/blob/master/interview/processthread/img/process-thread-5.PNG?raw=true)  

왜 스레드가 경량화된 프로세스냐 하면, 하나의 프로세스 안에 다수의 스레드가 있다면 __공유되는 자원__ 이 있기 때문입니다.  
스레드는 프로세스의 메모리 구조에서 Code, Data, Heap영역을 공통된 자원으로 사용하고 각 스레드는 Stack 부분만을 따로 갖습니다.  
공유되는 자원이 있기 때문에 이전처럼 컨텍스트 스위칭이 일어날 때 캐싱 적중률이 올라갑니다.  
즉, 모조리 다 빼고 다시 다 넣을 필요가 없다는 겁니다.  
<Br>

### Multi-process 
![그림6](https://github.com/backtony/blog-code/blob/master/interview/processthread/img/process-thread-6.PNG?raw=true)  
__하나의 프로그램을 여러개의 프로세스로 구성하여 각 프로세스가 병렬적으로 작업을 수행하는 것을 의미합니다.__  
한 애플리케이션에서 여러 사용자가 로그인을 요청하는 상황이 있다고 가정해 봅니다.  
한 프로세스는 매번 하나의 로그인만 처리할 수 있기 때문에 동시에 처리할 수 없습니다. 그래서 부모 프로세스가 fork() 하여 자식 프로세스를 여러 개 만들어 일을 처리하도록 합니다.  
이때 자식 프로세스는 부모와 __별개의 메모리 영역을 확보__ 하게 됩니다.  
<Br>

### Multi-thread

![그림7](https://github.com/backtony/blog-code/blob/master/interview/processthread/img/process-thread-7.PNG?raw=true)  
__하나의 프로세스에 여러 스레드로 자원을 공유하며 작업을 나누어 수행하는 것을 의미합니다.__  
__스레드는 한 프로세스 내에서 구분지어진 실행 단위입니다.__  
만약 프로세스가 다수의 스레드로 구분되어있지 않다면 단일 스레드 하나로 프로세스가 실행됩니다.  
이때 실행 단위는 프로세스 그 자체(= 해당 프로세스의 하나밖에 없는 스레드 하나)가 됩니다.  
프로세스 내에서 분리해서 여러 스레드로 나뉘어서 실행 단위가 나뉘어지면 Multi-thread가 됩니다.  
예시로, 인텔리제이를 사용하면서 테스트도 돌리면서 소스코드를 수정해야 한다면 한 애플리케이션에 대한 작업의 단위가 나뉘게 됩니다. 이때 각각의 스레드가 각 작업을 담당하게 되는 것입니다.  
<br><Br>

정리를 하자면 다음과 같습니다.
+ Multi-process
    - 각 프로세스는 독립적이기에 하나의 프로세스가 비정상 종료되더라고 다른 프로세스에 영향 X
    - IPC를 사용한 통신
    - 자원 소모적, 개별 메모리 차지
    - 컨텍스트 스위칭 비용이 큼
    - 동기화 작업이 필요하지 않음
    
+ Multi-thread
    - 스레드끼리 긴밀하게 연결되어 있음
    - 공유된 자원으로 통신 비용 절감
    - 공유된 자원으로 메모리가 효율적
    - 컨텍스트 스위칭 비용이 적음
    - 공유 자원 관리가 필요함
    - 동기화 작업이 필요
    - 하나의 쓰레드가 비정상 종료될 경우, 다른 쓰레드도 종료될 가능성이 있음

<br>

## 3. Multi-core
---
![그림8](https://github.com/backtony/blog-code/blob/master/interview/processthread/img/process-thread-8.PNG?raw=true)  

싱글 코어를 가진 CPU가 실행 단위를 처리할 때는 동시에 여러 가지가 진행되기 위해서 빠른 텀으로 전환되면서 실행된다고 앞서 설명했습니다. 이 개념이 __동시성__ 입니다.  
빠르게 여러 실행 단위를 번갈아 실행하면서 동시에 일어난 것처럼 보이게 하는 것입니다.  
하지만 멀티 코어는 __병렬처리__ 합니다.  
물리적으로 둘 이상의 코어를 사용해서 동시에 하나 이상의 프로세스(혹은 스레드가)한꺼번에 진행되게 하는 것입니다.

<br>

## 4. 정리
---
+ 프로세스는 프로세서에 의해 동작하고 있는 프로그램입니다.
    - 프로세스가 동작한다는 것은 프로세스의 특정 스레드가 실행 중이라는 의미로 그 특정 스레드는 프로세스가 가진 데이터를 참조합니다.
    - 따라서 __스레드 단위 작업을 지원하기 위한 자원 할당 단위__ 라고 말하기도 합니다.
+ 스레드는 한 프로세스 내에서 나뉘어진 하나 이상의 실행 단위이다.
+ 애플리케이션에 대한 작업을 동시에 하기 위해서는 2가지 처리 방식(멀티 프로세스, 멀티 스레드)이 존재한다.
+ 동시에 실행되는 것 처럼 보이기 위해서 실행 단위는 시분할로 cpu를 점유하여 컨텍스트 스위칭을 한다.
+ 멀티 프로세스는 독립적인 메모리를 갖고 있지만, 멀티 스레드는 자원을 공유한다.
+ 멀티 코어는 하드웨어 측면에서 실행 단위를 병렬적으로 처리할 수 있도록 여러 프로세서가 있는 것이다.





<br><Br>

__참고__  
<a href="https://www.youtube.com/watch?v=1grtWKqTn50&list=PLo0ta52hn1uHQ5iQ3hAeRoMUeLJFIeRew" target="_blank"> [10분 테코톡] 🌷 코다의 Process vs Thread</a>