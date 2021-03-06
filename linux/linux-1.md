# 리눅스 명령어 기초


## 리눅스와 쉘
---
### ctrl + a
라인 맨 앞으로 커서를 이동시킵니다.

### ctrl + e
라인 맨 뒤로 커서를 이동시킵니다.

### ctrl + c
인터럽트 시그널을 보내 실행 중인 프로세스를 중단합니다.

### ctrl + r
입력한 명령어들의 history를 검색합니다.  
```
(reverse-i-search)`mkdir': mkdir hello
```
ctrl + r를 입력하고 mkdir를 입력해보면 이전에 mkdir로 입력했던 명령어가 보입니다.  
엔터를 누르면 명령어가 수행되고 다른 mkdir로 수행했던 명령어를 확인하고 싶다면 다시 ctrl + r를 입력합니다.  

### 방향키
방향키를 위, 아래 키를 누르면 최근에 입력했던 명령어를 확인할 수 있습니다.

### 도움말
man과 help 옵션은 도움말의 역할을 합니다.
```sh
man 궁금한명령어
궁금한명령어 --help

# 예시
man ls
ls --help
```

<br>

## 디렉토리와 파일 통계 명령어
---

### cd
cd는 Change Directory의 약자로 디렉토리를 이동하는데 사용합니다.
```
cd 디렉토리명
```
디렉토리명이 생각이 안나면 tab을 두 번 클릭하면 현재 위치에 있는 디렉토리들을 보여줍니다.  
디렉토리명이 어느 정도 기억난다면 어느 정도 입력해주고 tab를 누르면 자동완성됩니다.  

```sh
# 최상위 디렉토리로 이동
cd /

# 현재보다 상위 디렉토리로 이동
cd ..

# 바로 직전의 디렉토리로 이동
cd -
```

### pwd
pwd는 Print Working Directory의 약자로 현재 위치를 확인하는데 사용합니다.
```
pwd
```

### ls
현 디렉토리의 파일 목록을 출력합니다.
```sh
# 현재 디렉토리의 파일명만 가로로 표기됩니다.
ls

# 세로로 표기하기 l이 아니고 숫자 1임을 주의
ls -1

# 숨김파일까지 확인하기
ls -al

# 사람이 읽기 편한 용량을 포함해서 확인하기 -> h옵션은 주로 사람이 보기 편하게 하기 위한 옵션
ls -alh

# 시간순으로 정렬해서 보기
ls -alt

# 시간 역순으로 정렬해서 보기
ls -altr
```

### df
마운트된 모든 장치에 대한 현재 디스크 공간 통계를 출력합니다.
```sh
# 보통 h 옵션을 붙여서 사람이 읽기 편하게 표기합니다.
df -h

# 디스크 타입을 표기합니다.
df -t

# i 노드 확인
df -i
```

### mkdir
디렉토리를 생성합니다.
```sh
mkdir 디렉토리명
```

### rmdir
디렉토리를 삭제하는데 비어있는 디렉토리만 삭제가 가능합니다.
```sh
rmdir 디렉토리명
```

### state
지정한 파일의 파일통계를 출력합니다.
```
state 파일명
```
보통 접근, 수정, 변경과 같은 시각 정보를 확인하기 위해 사용합니다.

<br>

## 파일 관련 명령어
---

### touch
지정한 이름의 비어있는 파일을 생성하거나 파일이 있는 경우 타임스탬프를 업데이트합니다.  
보통 전자를 위해 많이 사용합니다.  
```sh
touch 파일명
```

### cat
지정한 파일의 내용을 출력합니다.
```sh
cat 파일명
```

### head
지정한 파일의 1라인부터 지정한 수 만큼의 라인까지 출력합니다.(기본값은 10)
```sh
head 파일명

# 위에서 2줄만 보기
head -n 2 testfile.txt
```

### tail
지정한 파일의 마지막 라인부터 지정한 수 만큼의 라인을 출력합니다.(기본값은 10)
```sh
tail 파일명

# 아래서 2줄만 보기
tail -n 2 testfile.txt

# 파일 실시간으로 열어두고 확인하기 - 보통 로그 확인할 때 사용
tail -f 파일명
```

### cp
지정한 파일을 지정한 위치와 이름으로 복사합니다.
```sh
# 복사할파일팻흐 다음에 이름 생략이 원본 파일과 같습니다.
cp 원본파일패스/이름 복사할파일패스/이름

# 해당 디렉토리에서 수행합니다.
cp 이름 복사할이름

cp -rfp 원본파일패스/이름 복사할파일패스/이름
```
+ -r : recursive를 의미하며 하위 디렉토리도 포함해서 복사를 의미합니다. 옵션을 사용하지 않고 하위 디렉토리가 있는 디렉토리를 복사할 경우 에러가 발생합니다.
+ -f : force로 대상 패스에 같은 이름이 있더라도 덮어씌워버립니다.
+ -p : permission(권한)을 그대로 복사하여 부여합니다.

### mv
지정한 파일을 지정한 위치로 이동시킵니다.
```sh
mv 원본파일패스/이름 복사할파일패스/이름

# 파일이름을 바꿀때도 사용합니다.
mv 이름 변경할이름
```

### rename
지정한 규칙에 따라 여러 개의 파일 이름을 변경합니다.
```sh
rename 변경전파일명 변경후파밀명 대상파일

# test1 test2 test3 test4 파일이 있다고 가정했을때
rename test test0 test?
# 결과 -> test01  test02  test03  test04
```

### rm
지정한 파일을 삭제합니다.
```sh
# 하나 삭제
rm 파일명

# 여러개 삭제
rm 파일명 파일명 파일명

# 파일 강제 삭제
rm -f 파일명

# 디렉토리를 삭제할 경우 -r 옵션으로 recursive 내부 디렉토리까지 다 삭제해야합니다.
rm -rf 디렉토리명
```

### less
상하로 커서 이동이 가능하게 파일을 읽습니다.
```sh
# 방향키로 위아래로 파일을 읽고 q를 입력하면 빠져나옵니다.
less 파일명
```

### ln
지정한 파일에 대한 심볼릭링크나 하드링크를 생성합니다.
```sh
ln 옵션 링크의원본파일패스/이름 링크파일패스/이름

# hello.txt에 하드링크하여 hardlink.txt를 만듭니다.
ln hello.txt hardlink.txt

# 심볼릭 링크로 만들기
ln -s hello.txt hardlink.txt
```
+ 옵션을 사용하지 않으면 하드링크 파일을 만들어줍니다.
  - 하드링크는 물리적으로 두 개의 파일이 각각 다른 이름으로 하나의 원본 파일을 보고 있다고 보면 됩니다.  
  - 위에서 hello.txt를 제거해도 hardlink.txt에는 내용이 그대로 남아 있어 hardlink.txt로 접근하여 데이터를 읽을 수 있습니다.
+ -s 옵션을 사용하면 심볼릭 링크 파일을 생성합니다.  
  - 심볼릭 링크는 윈도우의 바로가기 폴더와 같습니다. 자주 사용하는 특정 파일이나 폴더를 바탕화면에 두고 빠르게 접근하기 위해 사용하는데 그것과 같은 맥락입니다.  
  - 원본 파일을 가리키고 있는 것이기에 원본 파일이 제거되면 접근할 수 없게 되고 심볼릭 파일은 여전히 남아있습니다.  

<br>

__cf 보충설명__  
![그림1](https://github.com/backtony/blog-code/blob/master/linux/img/1/1-1.PNG?raw=true)  
리눅스의 파일구조는 위와 같이 생겼습니다.  
Entry가 Name에 inode주소를 갖고 있고 inode가 실질적인 파일의 메타 데이터들을 갖고 있습니다.  
<br>

![그림2](https://github.com/backtony/blog-code/blob/master/linux/img/1/1-2.PNG?raw=true)  
hard link는 같은 inode를 참조하는 것입니다.  
즉, 이름만 다르지 같은 파일이 2개 생기고 하나의 데이터를 바라보는 것과 같습니다.  
심볼릭 링크는 Filename을 가리키기 때문에 Filename이 삭제되면 원본 파일에 접근할 수 없습니다.  


### paste
두 개의 파일 내용을 가로로 붙여줍니다.
```sh
# txt1 파일 내용
aaa
bbb
ccc

# txt2 파일 내용
111
222
333

paste txt1 txt2
aaa	111
bbb	222
ccc	333
```

### dd
블록 단위로 데이터셋을 정의하여 파일을 쓰고 읽습니다.
```sh
dd if=인풋파일이름 of=아웃풋파일이름 bs=바이트(크기) count=블럭을복사할횟수
```

### tar
지정한 데이터 및 디렉토리를 하나의 파일(압축)로 만듭니다.
```sh
# 압축하기
# c : create 묶어서 새로운 파일을 만들겠다
# v : verbose 명령을 실행하는 과정을 화면에 전부 출력
# z : gzip툴로 압축한다
# f : 파일이름을 명시적으로 지정한다.(가장 마지막 옵션으로 주어야함)
tar -cvzf 타르볼파일명.tgz 압축할디렉토리명/압축할파일명 # 공백 기준으로 여러 개 명시 가능

# 압축풀기
# x : extract 묶여있는 타르볼을 풀어준다.
tar -xvzf 타르볼파일명.tgz

# 압축 파일 안에 있는 것들 확인하기
# t : list의 마지막 알파벳 t를 가져온 것입니다.
tar -tf 타르볼파일명.tgz
```

### gzip
```sh
# 압축
gzip 파일/디렉토리명

# 압축해제
gunzip 압축명.gz
```

<br>

## 프로세스 관련 명령어
---

### ps
시스템에서 실행 중인 프로세스에 대한 정보를 출력합니다.
```sh
# UID(프로세스 실행한 유저명), PID(프로세스 ID), PPID(부모 프로세스ID) 를 포함하여 표시
ps -ef

# cpu, 메모리 사용량을 포함하여 표시
ps aux
```

### pstree
시스템에서 실행 중인 프로세스에 대한 정보를 트리구조로 출력합니다.

### top
프로세스 목록을 일정 시간마다 새로고침하여 화면에 출력하는 툴로 시스템 전반적인 상황을 모니터링 할 수 있습니다.  
보여지는 화면에서 숫자 1을 누르면 코어별로 확인할 수 있습니다.  

### nohup
__쉘 스크립트 파일을 데몬 형태로 실행하고 표준 출력을 지정한 파일로 리다이렉트합니다.__(기본으로는 nohup.out 파일에 써집니다.)  
데몬이라는 것은 사용자가 매번 직접 실행하는 것이 아니라 한번 실행하면 백그라운드에서 돌면서 필요한 작업을 수행하는 프로그램을 말합니다.  
간단하게 말하면, 프로그램 내용이 끝날때까지 죽지 않습니다.  
```sh
# 바로 콘솔창에 hello가 출력됩니다.
echo hello

# nohup.out 파일이 생가기고 그 안에 hello가 써집니다.
nohup echo hello
```

### kill
지정한 프로세스에 지정한 시그널을 보내 프로세스를 종료합니다.  
INT, TERM은 시그널을 사용하면 안전하게 종료합니다.  
```sh
# INT 시그널
kill -2 프로세스번호
kill -INT 프로세스번호

# TERM 시그널
kill -15 프로세스번호
kill -TERM 프로세스번호

# 강제 종료
kill -9 프로세스번호
kill -KILL 프로세스번호
```
<br>

## 네트워크 관련 명령어
---
### ifconfig
네트워크 인터페이스의 활성/비활성화 및 설정을 합니다.  

### netstat
네트워크 프로토콜의 통계와 연결상태를 출력합니다.  
```sh
# 서버가 열고있는 포트와 프로그램을 표기(udp 포함)
netstat -nltpu

# 서버가 열고있는 포트와 프로그램을 표기(udp 제외)
netstat -nltp

# 현재 네트워크 전체 상태 보기
netstat -tanu
```
+ n : 정의한 이름이나 도메인 이름을 빼고 ip나 포트번호를 숫자로 보여주는 옵션
  - 예를 들어 n을 안 붙이면 22번 포트는 ssh로 표기되고 n을 붙이면 22로 표기됩니다.
+ l : listen하고 있는 상태의 소켓을 보여줍니다.
  - 서버는 항상 특정 포트를 열어두고 유저의 접속을 기다리고 있는데 그것은 listen 한다고 표현합니다.
+ t : tcp
+ p : 프로그램 이름
+ u : udp

### ss
네트워크 소켓의 통계와 연결 상태를 출력합니다.  
옵션은 netstat와 동일합니다.

### iptables
패킷 필터링 도구로 패킷의 출입을 제한하는 방화벽 구성이나 NAT구성에 사용합니다.  
```sh
# n : 이름 대신 숫자로 보여주는 옵션 (위에서 설명했음)
# L : List의 약자
iptables -nL
```
출력값을 확인해보면 3개로 나눠져 있습니다.
+ Chain INPUT : 외부에서 서버로 들어오는 통신에 적용할 룰
+ Chain FORWARD : 서버를 경유해서 통과하는 통신에 적용하는 룰
+ Chain OUTPUT : 서버에서 외부로 나가는 통신에 적용하는 룰


### ufw
iptables의 제어를 쉽게 하기 위한 도구입니다.

### ping
서버와 서버 사이에 통신이 되는지 사용하기 위한 명령어입니다.
```sh
ping 서버ip 서버도메인
ping 서버도메인

# c는 count로 5번 보내는 것입니다.
ping -c 5 서버ip or 서버도메인
```

### wget
웹서버로부터 컨텐츠를 가져오는 도구입니다.
```sh
wget 다운로드주소
```

### curl
다양한 프로토콜을 사용하여 데이터를 전송하게 해주는 도구입니다.
```sh
                                          접속할 url
curl -Lkso /dev/null -w "%{http_code}\n" https://gmail.com
```
+ L : 리다이렉트 링크를 따라가는 옵션
+ k : HTTPS의 인증을 무시하는 옵션
+ s : silent 모드로 curl 실행하면 통계값이 나오는데 통계값을 출력하지 말라는 옵션
+ o : output 파일을 지정하는 옵션
  - 위에서 사용한 dev/null은 출력파일을 만들지 않겠다는 의미
+ w : output format을 정하는 옵션

### route
네트워크의 경로 정보(라우팅 테이블)의 출력, 변경하는 도구입니다.
<Br>

## 검색/탐색 관련 명령어
---
### find
지정한 파일명 또는 정규표현식을 이용하여 파일을 검색합니다.
```sh
# 찾기시작할 패스를 지정하지 않으면 현재 디렉토리를 기준으로 합니다
# 그 하위 디렉토리를 모두 검색 범위로 포함합니다.
find 옵션 찾기시작할패스 익스프레션 패턴

# 예시
find ./ -name testfile.t?t

# -print는 전체 출력
find /etc -print
```
익스프레션으로는 name, type, perm, empty 등이 있습니다.  

### which
환경변수 PATH에 등록된 디렉토리에 있는 명령어를 찾아주는 도구입니다.  
```sh
which ls
which cd
```

### grep
텍스트 검색 기능을 가진 도구입니다.  
파일이나 표준 입력을 검색하여 지정한 정규 표현식과 맞는 줄을 출력합니다.  
```sh
grep 옵션 "찾을문자열" 파일명

# i는 대소문자를 구분하지 않는 옵션
grep -i "error" /var/log/*

# i 하위 디렉토리까지 검색하기
grep -ir "error" /var/log/*

# c 특정 문자열이 몇개 들어가있는지 확인 옵션

# v 특정 문자를 제외하기 위한 옵션
grep -v "grep" /var/log/*
```

### history
명령어를 수행한 목록을 출력/조작합니다.  
각 유저의 home에 .bash_historay에 실행했던 명령어가 저장됩니다.
<Br>

## I/O 관련 명령어
---
### echo
문자열을 출력합니다.
```sh
echo hello
```

### redirection
입력/출력/에러(표준 스트림)을 지정한 곳으로 바꿔줍니다.  
쉘 스크립트나 명령을 실행할 때는 입력, 출력, 에러를 0, 1, 2로 숫자로 표현합니다.  

#### 출력 redirection
```sh
명령 리다이렉션기호 파일명
```
+ \> : 매번 명령을 실행할 때마다 파일 내용을 초기화해서 덮어씌우는 명령
+ \>\> : 기존 파일 내용의 아래쪽에 추가합니다.
+ \>\| : noclobber 옵션 설정 시 덮어쓰기 시도 시 에러가 발생하는데 이 명령어를 사용하면 무시하고 덮어쓰기 합니다.
+ \>& : >와 동일하지만 대상 파일 대신 대상 파일 디스크립터를 지정합니다. 보통 표준 출력과 표준 에러를 한꺼번에 출력하고 싶을 때 사용합니다.
+ &> : 바로 위 옵션은 조금 복잡한데 이 방법을 사용하면 더 간단하게 사용할 수 있다.

```sh
# a출력을 a라는 파일에 집어넣는다.
echo a > a

# a의 파일내용은 b로 바뀐다.
echo b > a

# a파일 내용에 아래쪽에 c가 추가
echo c >> a

# 2(표준에러)를 1(정상출력, standard output)으로 리다이렉션(보내라)
# ls 커멘드의 결과를 result에 저장하는데 에러 출력을 정상 출력으로 보내라.
ls > result 2>&1

# 에러가 result에 저장된다.
ls /아무거나 > result 2>&1

# 위의 옵션은 너무 불편하다
# &> 하면 한번에 처리 가능
# 표준 에러든 출력이든 result로 보낸다.
ls &> result
```

#### 입력 redirection
```sh
# 파일 내용이 지정된 스트림(n)으로 리다이렉션
[n] < 파일명

# 예시
# wc는 사용자 입력을 받아서 라인수, 단어수, character수를 출력합니다.
# wc의 입력을 파일로 준다.
wc < 파일명
```

#### Here documents
```sh
[command] << [-]DELIM
```
+ 프로그램의 표준 입력으로 multi-line string을 전달합니다.
+ 코드 블록의 내용이 임시 파일로 저장됐다가 프로그램의 표준 입력으로 리다이렉션 됩니다.
+ DELIM은 다른 단어로 변경 가능합니다(EOF, END 등등)
+ -를 붙여서 사용시 라인 앞쪽의 tab 문자가 제거됩니다.


```sh
# hello world줄부터 how are you 줄까지 입력으로 들어간다.
# DELIM은 아무 단어나 상관없이 처음과 끝을 일치시켜주면 된다.
wc << DELIM
> hello world
> hi
> how are you?
> DELIM
```
```sh
# cat 은 뒤에 아무것도 안주면 입력을 기다린다.
# cat의 출력은 hellotext에 쓰는데 입력을 <<로 받아서 쓴다.
cat > hellotext << eof
hello world
bye
eof
```

#### Here strings
```sh
[command] <<< word
```
Here document의 한줄 버전입니다.
```sh
cat > txt1 <<< "helolo world"
# 하지만 이것 대신 보통 echo를 사용한다.
echo "hello world" > txt1
```

<br>

## 기타 명령어
---
### date
현재의 날짜와 시간을 출력합니다.
```sh
# 하루 전을 표시
# week month hour minute second 도 가능
date -d '-1 day'

# 출력 형식 변경
# 연-월-일 시:분:초
date "+%Y-%m-%d %H:%M:%S"

# 연월일
date "+%Y%m%d"
```

### seq
지정한 규칙으로 숫자열을 출력하는 도구입니다.
```sh
# 1부터 10까지 출력
seq 1 10

# 가장 큰 자리수에 맞춰서 앞쪽에 0을 붙인다.
seq -w 1 10

# 전부 2자리수에 앞에 0을 채운다.
seq -f %0자리수g

# 1부터 10까지 중에서 2자리수로 다채우면서 앞에 0을 붙인다.
seq -f %02g 1 10
```


### more
한 화면씩 지정한 파일의 내용을 출력하는 도구입니다.
```sh
more 파일명
# q입력시 종료, 엔터 한칸씩 내려가기, 엔터 한페이씩 내려가기
```


### watch
지정한 명령어를 지정한 시간(초)마다 재실행하여 화면에 출력해줍니다. 기본은 2초입니다.

### crontab
리눅스의 잡 스케줄러의 내용을 출력하거나 편집할 수 있는 도구입니다.
```sh
# l은 리스트로, 등록한 crontab을 보여준다.
crontab -l

# e는 edit로, crontab을 편집할 수 있게 한다.
crontab -e

# crontab에 등록한 모든것을 지워버린다.
crontab -r
```
<br>

## apt 패키지 매니저
---
```sh
sudo apt 옵션 패키지명
```
+ install : 패키지를 설지합니다.
+ reinstall : 패키지를 재설치합니다.
+ remove : 패키지를 제거합니다.
+ autoremove : 사용하지 않는 패키지를 자동으로 전부 지웁니다.
+ upgrade : 패키지를 설치/업그레이드해 시스템을 업그레이드합니다.

<Br>

## 리눅스 디렉토리 계층
---
+ / : 루트 디렉토리로 모든 디렉토리의 최상위 부모
+ /bin : 모든 사용자가 사용할 수 있는 여러 가지 실행 파일의 위치
+ /sbin : 시스템 관리자 권한으로 실행해야 하는 실행 파일 위치
+ /etc : 여러 가지 설정 파일
+ /lib : 공유 라이브러리 디렉토리
+ /home : 사용자들의 홈 디렉토리
+ /mnt : 일시적으로 파일 시스템에 마운트하는 경우 사용하는 디렉토리
+ /proc, /sys : 시스템 정보를 설정/조회할 수 있는 디렉토리
+ /tmp : 임시 디렉토리
+ /usr : 사용자가 추가한 실행 파일, 라이브러리 등의 소프트웨어를 저장
+ /dev : 디바이스 드라이버가 사용하는 디바이스 파일 디렉토리

<br>

## 사용자와 그룹
---
### chmod
#### 8진 표기법
파일이나 디렉토리의 모드(접근권한)을 변경하는 도구입니다.  
ls -al 을 입력해보면 각 행의 맨 앞쪽에 다음과 같이 적혀있습니다.
```sh
# 권한      # 소유자   # 그룹
drwx------ ec2-user ec2-user 29  2월  7 08:22 .ssh
```
맨앞 d는 디렉토리를 의미합니다. 이후에는 rwx가 반복되어 명시되어있습니다.  
rwx는 각각 쓰기, 읽기, 실행의 권한을 의미합니다.  
rwx가 3번씩 반복되는데 첫 번째 rwx는 파일 소유자가 갖는 권한입니다.  
그다음 나오는 문자열 rwx 2개는 순서대로 파일 소유자, 소유자의 그룹명입니다.  
![그림3](https://github.com/backtony/blog-code/blob/master/linux/img/1/1-3.PNG?raw=true)  

<br><Br>

![그림4](https://github.com/backtony/blog-code/blob/master/linux/img/1/1-4.PNG?raw=true)  
```sh
# 소유자와 그룹은 읽기와 쓰기가 가능하고 그밖에는 읽기만 가능
rw- rw- r--
6   6   4 -> 664
chmod 664 파일명
```
<br>

#### 의미 표기법
```sh
chmod [ugoa[+/-]rwx] <dir>

u = user
g = group
o = others
a = all

+ : 권한 부여
- : 권한 제거

r : read
w : write
x : exe

# 예시
# 그룹과 기외에 read,exe 권한 부여 ,, 만약 ugoa를 명시하지 않으면 전체에 적용
chmod go+rx <dir>
```


### chown
파일의 소유권을 바꾸기 위한 도구입니다.
```sh
chown 소유자명:그룹명 소유자이전할파일혹은디렉토리이름
```

### sudo
root 사용자의 보안 권한을 이용하여 명령 또는 프로그램을 실행하는 도구입니다.  

### who
현재 시스템에 로그인한 사용자 목록을 출력합니다.  
w만 입력하면 더 상세하게 출력합니다.
```sh
who
w
```

### 사용자 추가
```sh
# 둘다 사용해도 무관한데 adduser가 더 편하다
adduser
useradd

# 사용자 추가
# 기본적으로 이름으로 그룹명이 지정된다.
sudo adduser 이름

# 패스워드 입력
New password:

# 이후 부가적인 정보 상관없으면 계속 엔터
# 완료되면 cat /etc/passwd 를 보면 맨 아래 계정이 추가된 것을 확인할 수 있다.

# 로그인 시도 2가지 방법
sudo login 이름
su - 이름
```

### 사용자 제거
```sh
sudo deluser 이름 옵션

# 사용자의 처음 디렉토리 제거
--remove-home

# 사용자의 모든 파일 제거
--remove-all-files

# 삭제하깆 전에 백업
--backup-to <DIR>
```


### 그룹 추가
```sh
# 그룹 생성
sudo addgroup 그룹명

# 계정 생성 + 그룹지정
sudo adduser 이름 --ingroup 그룹명
```