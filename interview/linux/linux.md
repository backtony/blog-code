 
# 리눅스 메모리 관리

## 메모리가 관리되는 방법
---
![그림1](https://github.com/backtony/blog-code/blob/master/interview/linux/img/linux-1.PNG?raw=true)  
메모리는 주소 덩어리로, 주소로 인덱싱하는 커다란 배열입니다.  
컴퓨터가 부팅되면 텅텅 비어있던 메모리에 운영체제나 사용자 프로그램이 배열의 원소처럼 채워지면서 CPU를 점유할 기회를 노립니다.  
CPU가 메모리에 채워진 프로그램 속 코드를 곧장 읽으면 좋겠지만 CPU를 코드를 읽지 못합니다. 숫자로 바꿔줘야 합니다.  
<br><Br>

![그림2](https://github.com/backtony/blog-code/blob/master/interview/linux/img/linux-2.PNG?raw=true)  
소스 코드를 숫자로 바꿔주는 것이 컴파일러고, 컴파일러가 동작하는 과정에서 코드들의 논리 주소를 결정합니다.  
<br><Br>

![그림3](https://github.com/backtony/blog-code/blob/master/interview/linux/img/linux-3.PNG?raw=true)  
각각 프로그램마다 다른 논리 주소를 갖는 것이 아니라, 중복되는 논리 주소를 갖고 있습니다.  
그래서 논리 주소를 가상 주소라고도 부릅니다.  
모두 같은 주소를 사용한다면 메모리에서 어떻게 이를 구분할까요?

<br><Br>

![그림4](https://github.com/backtony/blog-code/blob/master/interview/linux/img/linux-4.PNG?raw=true)  
논리 주소 앞에 하나의 주소값이 더 추가되면서 프로그램마다 독립적인 주소가 생기고 이를 물리 주소라고 합니다.  
그렇다면 논리 주소에 추가적으로 주소를 붙여서 물리 주소를 만들 필요 없이 심볼릭 주소에서 곧장 물리 주소로 만들면 되지 않을까 생각할 수 있습니다.  
이유는 CPU가 논리 주소만을 읽기 때문입니다.  
CPU는 현재 활동 중인 프로세스안의 내부 주소만 알면 되지 어떤 프로세스인지는 알 필요가 없습니다.  
CPU는 논리 주소만으로 물리 메모리에 올라와있는 프로세스들의 정보를 읽는데 어떤 프로세스인지도 모르는데 정보를 읽는게 어떻게 가능할까요?  
운영체제가 도와준다고 생각할 수 있지만, 운영체제도 메모리에 올라와 동작하는 프로세스 중 하나일 뿐입니다.  
똑같이 CPU에게 논리 주소로 정보를 읽히는 입장합니다.  
즉, 소프트웨어적으로는 물리 주소를 찾을 수 있는 방법이 없습니다.  
그래서 하드웨어적인 도움이 필요합니다.  
그 도움을 주는 것이 MMU(Memory Management Unit)입니다.  
<br><br>

![그림5](https://github.com/backtony/blog-code/blob/master/interview/linux/img/linux-5.PNG?raw=true)  

MMU는 프로그램의 시작 주소를 갖는 Base register, 마지막 주소를 갖는 Limit register, 간단한 산술 연산기로 이뤄져 있습니다.  
CPU를 사용중인 프로세스가 요청하는 논리 주소에 Base register를 더해서 물리 주소로 변환시켜서 완성된 물리 주소로 메모리에 프로세스가 가진 정보를 정확하게 읽어올 수 있게 됩니다.  
이렇게 동작하기 전에 선행 동작으로, Limit register에 들어있는 마지막 주소로 현재 요청하는 논리 주소가 올바른지 확인하는 작업을 합니다.  
만약 Limit register를 넘어가는 주소를 요청하게 되면 해당 프로세스를 멈추고 CPU권한을 운영체제에 넘깁니다.  
운영체제는 이 프로세스가 왜 멈췄는지 살펴보고, 악의적이었다면 바로 응징을 합니다.  

<br><Br>


![그림6](https://github.com/backtony/blog-code/blob/master/interview/linux/img/linux-6.PNG?raw=true)  
메모리에 프로세스들이 차례대로 채워지고 MMU를 통해 고정된 주소를 한번씩 더하면서 물리 메모리를 참조하니 메모리 사용이 간단해보이지만, 실제로는 메모리에 프로세스들이 딱 맞춰서 채워지지 않습니다.  
<br><Br>

![그림7](https://github.com/backtony/blog-code/blob/master/interview/linux/img/linux-7.PNG?raw=true)  
프로세스들이 들어갔따 나가면서 그림처럼 빈 공간이 생기고, 어느 시점에는 빈 공간을 합치면 들어갈 수 있지만, 빈 공간이 연결되어 있지 않아서 프로세스가 들어가지 못하는 상황이 생깁니다.  
<br><Br>

![그림8](https://github.com/backtony/blog-code/blob/master/interview/linux/img/linux-8.PNG?raw=true)  
메모리가 꽉차서 들어갈 수 없는 상태에서 수강 신청 같이 급하게 필요한 경우라면 당장 불필요한 강의 영상을 내리고 수강 신청을 메모리에 올립니다.  
프로세스를 일시적으로 메모리에서 하드디스크에 있는 swap 공간으로 내쫓는 것이 swapping 기법입니다.  
하지만 Swapping 기법이 만능은 아닙니다.  
Swapping할 프로세스를 고르는 것도 일이고, 우선순위를 판단하는 것도 일입니다.  
또한, 하드디스크까지 전체 프로세스를 옮기는데 상대적으로 많은 시간이 소요됩니다.  
이에 대한 해결책이 여러가지 제시되었지만, 결국에는 메모리 공간을 일정하게 잘라두고 그에 맞춰 프로그램을 조금씩 잘라서 올리자는 결론에 이릅니다.  
<br><Br>

![그림9](https://github.com/backtony/blog-code/blob/master/interview/linux/img/linux-9.PNG?raw=true)  
프로그램을 조금씩 잘라서 올리기 위해 물리 메모리를 동일한 크기로 잘랐습니다.  
이 공간들을 Frame이라고 부릅니다.  
그리고 프로그램들을 Frame과 동일한 크기로 자르고 잘린 것 하나를 Page라고 부릅니다.  
여기서 당장 프로그램이 동작하는데 필요한 최소한의 Page들만 메모리에 올리고 나머지는 Swap 공간에 저장해둡니다.  
이것이 Paging 기법입니다.  
<br><Br>

![그림10](https://github.com/backtony/blog-code/blob/master/interview/linux/img/linux-10.PNG?raw=true)  
Paging 기법 덕분에 메모리에 낭비되는 구멍은 거의 없어졌지만, 한 프로그램의 페이지가 여기저기 분포되면서 순서도 보장할 수 없게 되어 MMU의 계산이 복잡해지게 됩니다.  
그렇다면 순서도 보장않되고 복잡해진 페이지들을 어떻게 조회할까요?
<br><Br>

![그림11](https://github.com/backtony/blog-code/blob/master/interview/linux/img/linux-11.PNG?raw=true)  
논리, 물리 주소 변환을 위한 별도의 페이지 테이블을 사용합니다.  
페이지 테이블 때문에 MMU 레지스터의 이름과 용도도 달라지게 됩니다.  
이전에 프로세스의 시작 주소를 더해주던 Base Register는 페이지 테이블의 시작 주소를 더해주는 Page Table Base Register로 변경되었고, 프로세스의 마지막 주소를 검증하던 Limig register는 Page Table의 크기를 검증하는 Page Table Length Register로 변경되었습니다.  
페이지 테이블에는 물리 메모리에 있는지 Swap공간에 있는지 빨리 검사하기 위해 Valid 비트도 추가되었습니다.

<br><Br>

![그림12](https://github.com/backtony/blog-code/blob/master/interview/linux/img/linux-12.PNG?raw=true)  
CPU가 MMU에게 논리적인 주소로 요청하게 되면 계산이 끝난 값으로 페이지 테이블을 참조해서 찾아낸 Frame 주소로 이동해 해당 페이지의 주소를 읽어냅니다.  
그렇다면 이 페이지 테이블은 어디에 저장될까요?  
페이지 테이블은 메모리에 저장됩니다.  
우선 페이지 테이블의 행 개수는 해당 프로세스를 일정한 간격으로 나눈 수입니다.  
프로세스마다 다르겠지만 페이지 테이블의 행이 100만개가 넘기는 경우가 대다수인데 이것이 프로세스마다 한개씩 존재합니다.  
메모리 공간을 효율적으로 사용하기 위해 페이징 기법을 적용했는데 페이징 테이블이 공간을 사용하고 있습니다.  
<br><Br>

![그림13](https://github.com/backtony/blog-code/blob/master/interview/linux/img/linux-13.PNG?raw=true)  
공간을 최대한 아껴보고자, 프로세스들끼리 공통적으로 사용하는 부분은 메모리에 한개씩만 올리고 프로세스뜰이 나눠쓰게 만들고 그것을 Shared Page라고 합니다.  
이는 절때 수정되면 안되므로 Read Only 권한이 부여되고, 별도의 탐색 없이 쉽게 찾을 수 있도록 서로 동일한 논리주소에 위치합니다.  
Read Only 권한을 표시하기 위해서 페이지 테이블에 Auth 비트가 추가적으로 생깁니다.  
이로써 페이지 테이블을 메모리에 저장한 만큼의 공간을 다시 확보했지만 이번에는 속도가 발복을 잡습니다.  
페이지 테이블을 메모리에 위치하고 페이지도 메모리에 위치합니다.  
CPU가 정보를 요청할 때마다 페이지 테이블에 접근하고 좌표를 받아서 다시 메모리에 접근해서 데이터를 가져오면서 결국 메모리에 2번씩 접근해야 합니다.  
<br><br>

![그림14](https://github.com/backtony/blog-code/blob/master/interview/linux/img/linux-14.PNG?raw=true)  
이를 해결하기 위해 추가적인 하드웨어 TLB(Translation Look-aside Buffers)의 도움을 받습니다.  
페이지 테이블을 보기전에 한번 확인하는 캐시 메모리입니다.  
CPU가 논리 주소로 정보를 요청하면 페이지 테이블에 접근하기전 우선 TLB부터 확인합니다.  
TLB에 매칭된 주소가 있으면 TLB에 있는 Frame 주소로 변환해서 바로 메모리에서 데이터를 가져옵니다.  
TLB에 없다면 어쩔수없이 2번 메모리에 접근하게 됩니다.  
대부분의 프로세스는 한번 참조했던 곳을 다시 참조할 가능성이 매우 높으므로 TLB의 성공 확률이 높아서 거의 1번의 메모리 접근으로 끝나게 됩니다.  

<br><Br>

정리해보자면,
+ 현대 메모리는 페이징을 베이스로한 기법을 채택
+ 하드디스크를 Swap 공간으로 활용하여 잉여 페이지들을 보관
+ 논리 주소를 물리 주소로 변환하기 위해서 MMU, TLB 같은 하드웨어들의 지원을 받아 Page Table을 확인하고 메모리를 참조

<br>

## 리눅스가 메모리를 관리하는 방법
---
앞서 이야기한 페이징 기법에서 운영체제는 2가지 일을 하고 있습니다.
### 가상 메모리로 사용자 프로세스 속이기
![그림15](https://github.com/backtony/blog-code/blob/master/interview/linux/img/linux-15.PNG?raw=true)  
CPU를 점유하고 있는 프로세스는 자신이 온전하게 전부 메모리에 올라와 있다고 생각합니다.

<br><BR>

![그림16](https://github.com/backtony/blog-code/blob/master/interview/linux/img/linux-16.PNG?raw=true)  
하지만 실제로는 동작에 필요한 부분만 물리 메모리, 나머지는 swap 공간에 저장되어 있습니다.

<Br><Br>

![그림17](https://github.com/backtony/blog-code/blob/master/interview/linux/img/linux-17.PNG?raw=true)  
물리 메모리 공간과 Swap 공간을 합쳐서 만들어낸 가짜 메모리를 가상 메모리라고 합니다.  
페이징 기법 중 CPU를 통해서 요구하던 논리 주소가 사실 가상 메모리 상의 주소 가상 주소였던 것입니다.

### 하드디스크의 입출력(I/O) 장치 관리
![그림18](https://github.com/backtony/blog-code/blob/master/interview/linux/img/linux-18.PNG?raw=true)  
주소를 변환하고 메모리에서 페이지를 찾아내는 것은 사용자 프로세스와 하드웨어에서 진행하지만, 하드디스크 같이 입출력 저장장치를 건드리는 것은 운영체제 관할입니다.  
즉, Swap 공간에서 페이지를 꺼내려면 운영체제의 도움이 필요합니다.  
프로세스가 CPU를 점유하고 한참 작업을 이어나가던 도중에 TLB에 메모리에 없는 페이지를 요구합니다.  
메모리에 페이지가 없다는 것을 알아차린 MMU가 프로세스를 일시정지 시킵니다.  
운영체제가 CPU를 점유하고 왜 프로세스가 멈췄는지 체크합니다.  
만약 이상한 주소를 요청했다면 바로 차단하고, 아니라면 운영체제가 하드디스크의 swap공간에서 페이지를 메모리로 가져오고 TLB에 주소를 등록과 페이지 테이블에도 Valid 비트와 함께 업데이트 합니다.  
그리고 운영체제는 CPU를 내려놓고 다시 빠집니다.  
그런데 Swap 공간에서 페이지를 가져오기 까지 시간이 매우 길기 때문에 중간에 다른 프로세스에게 CPU가 넘어갈 수 있습니다.  
이런 경우 해당 프로세스는 대기 큐에 들어가서 다시 자기 차례를 기다립니다.  
이런 경우가 아니라면 운영체제가 CPU를 내려놓으면 기존 프로세스가 CPU를 차지하고 명령수행을 실패한 지점부터 다시 동작을 수행하게 됩니다.  
즉, Page Fault가 발생하면 CPU가 다른 프로세스로 넘어갈만큼 많은 시간이 소모되는 것을 알 수 있습니다.  
따라서, Page Fault 확률이 곧 성능이 됩니다.  
하지만, 컴퓨터 프로그램의 특성상 중복된 내용 참조가 많아서 Page Fault 확률이 낮고 대부분 TLB를 참조하면서 빠르게 작업이 진행됩니다.  
<br><Br>

![그림19](https://github.com/backtony/blog-code/blob/master/interview/linux/img/linux-19.PNG?raw=true)  
물리 메모리에 프레임이 가득차게 된다면, 메모리를 차지한 페이지 하나를 내쫓아야 합니다.  
이 행위를 Page Replacement라고 하고 어떤 페이지를 교체할지는 운영체제가 결정합니다.  
쫓아낼 페이지를 선정하는 방법에는 LRU, 마지막으로 참조된 시점이 가장 오래된 페이지를 찾아내는 알고리즘이 적합해 보입니다.  
<Br><br>

![그림20](https://github.com/backtony/blog-code/blob/master/interview/linux/img/linux-20.PNG?raw=true)  
하지만 실제 운영체제에서는 LRU 방식이 사용되지 않습니다.  
이유는 메모리에 데이터가 이미 존재한 경우, Page Fault가 나지 않기 때문에 운영체제가 개입하지 않기 때문입니다.  
따라서 운영체제는 자신이 관리했던 Page Fault만을 기억하게 되고, 다른 페이지들은 언제 접근되었고 몇번이나 사용되었는지는 알 수 없습니다.  
즉, LRU에 필요한 정보를 절반만 알고 있게 되어 LRU를 사용할 수 없습니다.  
<Br><Br>

![그림21](https://github.com/backtony/blog-code/blob/master/interview/linux/img/linux-21.PNG?raw=true)  
대신에 LRU 계열인 Clock Algorithm을 사용합니다.  
메모리에 올라와 있는 모든 페이지마다 1개의 reference bit를 갖게 합니다.  
초기에는 모두 0이고 CPU를 점유하고 있는 프로세스로부터 참조되면 bit가 1로 올라갑니다.  
이 상태에서 페이지 교환이 이뤄질 경우, 한쪽 방향으로 페이지 테이블을 참조하기 시작합니다.  
참조하는 과정에서 1비트를 만나면 0으로 바꾸고, 0비트를 만나면 그것이 교환의 대상이 됩니다.  
가장 오래되지 않은 페이지를 잡아낼 수는 없지만, 가장 최근에 참조된 페이지는 피할 수 있게 되는 것입니다.  
이 reference bit은 페이지 테이블에 추가됩니다.  
<br><Br>

Clock 알고리즘으로 선택한 페이지를 쫓아내야하는데 이도 함부로 쫓아낼 수는 없습니다.  
CPU를 점유한 프로세스로부터 참조되는 동안 변경사항이 있는지 확인해야 합니다.  
변경사항이 없다면 바로 쫓아내고, 변경사항이 있다면 하드디스크에도 변경된 내용을 반영합니다.  
그럼 변경사항은 어떻게 감지할까요?  
![그림22](https://github.com/backtony/blog-code/blob/master/interview/linux/img/linux-22.PNG?raw=true)  
페이지 테이블에 dirty 비트가 하나 더 추가됩니다.  
즉, 하드디스크에 변경사항을 반영하고, 반영되었으니 페이지 테이블의 Dirty 비트를 수정하는 것도 운영체제가 수행합니다.

### Trashing(쓰레싱)
![그림23](https://github.com/backtony/blog-code/blob/master/interview/linux/img/linux-23.PNG?raw=true)  
Page Fault, Page Replacement가 발생하면서 다양한 프로세스가 메모리에 올라오면 메모리의 유효공간은 줄어들고 CPU의 가동시간이 올라가면서 자원을 최대한 활용하는 상태에 이릅니다.  
하지만 시간이 흐르면 CPU 사용률이 떨어지게 되는데 이는 메모리에 프로세스가 많아지면서 프로세스당 물리 메모리를 사용할 수 있는 프레임의 개수가 줄어들어 페이지가 물리 메모리에 적게 올라온 프로세스는 명령을 조금만 수행해도 Page Fault가 발생하여 Page Replacement를 진행하게 되기 때문입니다.  
Page Replacement로 Swap 공간에서 페이지를 가져오기까지 상대적으로 오랜 시간이 걸리기 때문에 그동안 다른 프로세스가 CPU를 넘겨받지만 그 프로세스도 곧 Page Replacement를 진행하게 됩니다.  
결과적으로 모든 프로세스들이 페이지를 교체하느라 바쁜 반면에, CPU는 할일이 없어서 쉬게 되는데 CPU가 놀고있는 것을 발견한 운영체제는 더 많은 프로세스를 메모리에 올리면서 악순환이 반복됩니다.  
이 현상을 Trashing이라고 합니다.  
Trashing을 해소하기 위해 운영체제는 Working Set 알고리즘과 Page Fault Frequency 알고리즘을 사용합니다.  
<br><Br>

![그림24](https://github.com/backtony/blog-code/blob/master/interview/linux/img/linux-24.PNG?raw=true)  
Working Set 알고리즘은 대부분의 프로세스가 일정한 페이지만 집중적으로 참조한다는 성격을 이용해서 특정 시간동안 참조되는 페이지 개수를 파악하여 그 페이지 개수만큼 프레임이 확보되면 그때 페이지들을 메모리에 올리는 알고리즘입니다.  
Page Replacement 활동을 진행할 때도 프로세스마다 Working Set 단위로 페이지를 쫓아냅니다.  
<br><Br>

![그림25](https://github.com/backtony/blog-code/blob/master/interview/linux/img/linux-25.PNG?raw=true)  
Page Fault Frequency 알고리즘은 Page Fault 퍼센트의 상한과 하한을 두고 상한을 넘으면 지급하는 프레임 개수를 늘리고, 하한을 넘으면 지급 프레임 개수를 줄입니다.  
이도 남는 프레임이 없으면 프로세스 단위로 페이지를 쫓아냅니다.  



<br>

## 메모리 고갈 상황과 CPU 사용률을 체크하는 이유
---
### 메모리가 고갈되면 어떤 상황이 발생할까요?
프로세스들의 Swap이 활발해지면서 CPU 사용률이 하락하게 됩니다.  
운영체제는 CPU 사용률이 하락한 것을 보고 프로세스를 추가하게 되어 Trashing 현상이 발생합니다.  
Trashing 현상이 해결되지 않을 경우 Out Of Memory 상태로 판단되어 중요도가 낮은 프로세스를 찾아 강제로 종료하게 됩니다.

### CPU 사용률을 계속 체크해야하는 이유
특정 시점만 체크한 경우 CPU 사용률이 높아보일 수 있습니다.  
하지만 연속적으로 체크하게 되면 CPU 사용률이 급격하게 떨어지는 구간을 발견할 가능성이 높아집니다.  
이때 메모리 적재량을 함께 체크하면 Trashing의 발생 유무도 확인할 수 있게 됩니다.  
따라서 Trashing이 발견되었다면 서버자원을 추가적으로 배치하는 등 해결방안을 마련할 수 있습니다.



  

 
<Br><Br>

__참고__  
<a href="https://www.youtube.com/watch?v=qxmdX449z1U&list=PLo0ta52hn1uHQ5iQ3hAeRoMUeLJFIeRew" target="_blank"> [10분 테코톡] 🤷‍♂️ 현구막의 리눅스 메모리 관리</a>   







 