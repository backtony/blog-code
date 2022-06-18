# 쉘 스크립트 기초

## 쉘 스크립트에 필요한 문법
---
### 파이프라인(|)
```sh
# command1의 표준 출력이 command2의 표준 입력으로 연결됩니다.  
command1 [ | command2 ] ...

# command1의 표준 출력 결과와 표준 에러가 command2의 표준 입력으로 연결됩니다.
command1 [ |& command2 ] ...
```
```sh
# 예시
# a.txt 파일안에 문자열 'b'가 있다고 가정
# a.txt 파일 내용을 grep의 입력값으로 전달
cat a.txt | grep b

# cat 출력값을 grep 의 입력값으로 넘기기
cat /etc/passwd | grep "root"
```

### 세미콜론(;)
세미콜론 왼쪽의 명령이 끝난 후 이어서 세미콜론 오른쪽의 명령을 실행합니다.
```sh
pwd; ls;
```



### AND (&&)
좌측 명령/테스트 결과가 참이면 우측 명령을 실행합니다.  
좌측 명령/테스트 결과가 거짓이면 우측의 명령을 실행하지 않습니다.
```sh
# 대괄호 양쪽 사이에 공백이 필수입니다.
[ 1 == 1 ] && echo "왼쪽 조건이 참일경우 보인다"
```

### OR (||)
좌측 명령/테스트 결과가 참이면 우측 명령을 실행하지 않습니다.  
좌측 명령/테스트 결과가 거짓이면 우측 명령을 실행합니다.  
```sh
[ 1 == 2 ] || echo " 왼쪽 조건이 거짓이면 보인다."
```

### 조건 []

```sh
# a.txt파일이 존재한다고 가정
[ -e ./a.txt ] && echo "a.txt 파일이 존재합니다."
```
+ e : 파일이 존재하는가
+ f : 지정한 파일이 파일이면 참 그외에는 거짓
+ d : 지정한 파일이 디렉토리면 참 그 외에는 거짓

<br>

## CLI 편집기
---
### vim
리눅스 터미널 환경에서 사용자 인터페이스를 제공하는 편집 도구로 vi의 업그레이드 버전입니다.  

+ 일반모드
    - 이동키로 커서를 이동하거나 편집할 수 있는 모드로 각 모드로 이동하기 전의 기본 모드입니다.
+ 입력모드
    - i : 현재 커서에서 입력
    - a : 현재 커서 바로 다음에 입력
    - o : 다음 줄에 입력    
    - O : 윗줄에 입력
+ 비주얼모드
    - 일반모드에서 v, V, ctrl+v를 누른 후 범위를 지정할 수 있는 모드입니다.
    - 지정한 범위의 복사(y), 잘라내기(x), 삭제(d), 붙여넣기(p)가 가능합니다.
    - yy(한 라인 복사), dd(한 라인 지우기)
+ ex 모드
    - : 또는 / 를 입력하여 vim편집기 하단에서 명령 또는 검색을 할 수 있는 모드입니다.
    - : 입력 후 w(저장), q(종료), wq(저장 후 종료), 강제저장(w!), 강제종료(q!)
    - / 입력 후 현재 열고 있는 문서에서 찾고자 하는 문자열을 입력하면 검색됩니다. n을 누르면 다음 문자를 찾습니다.
    - : 입력 후 !명령어 를 사용하면 bash 명령어를 실행할 수 있습니다.
+ 유용한 단축키
    - shift + v + g : 전체 드래그
    - dd : 현재 커서가 위치한 곳의 한줄 삭제(p로 복구 가능), 전체 드래그하고 dd하면 다 삭제됨
    - dw : 커서가 위치한 곳부터 띄어쓰기 전까지 삭제(한 단어 삭제)
    - d$ : 해당 커서 이후 줄 끝까지 삭제
    - (N)dd : N은 숫자로 줄 개수를 의미함, N개의 행을 삭제한다.
    - $ : 커서가 있는 줄의 맨 뒤로 이동
    - ( : 현재 문장의 처음
    - ) : 현재 문장의 끝
    - u : 방금 한 명령 취소
    + yy : 한줄 복사
    + p : 현재 커서가 있는 줄 바로 아래에 붙여넣기
    + 

<br>

## bash 쉘 스크립트
---
간단하게 hello world를 찍는 스크립트를 만들어 봅시다.
```sh
vim helloworld.sh

# 파일 내용 작성

#!/bin/bash

TXT="hello world"
echo "${TXT}"

# 파일 내용 끝 

# 파일에는 실행 권한이 부여되어 있지 않은 상태인데 bash는 실행 권한이 있습니다.
bash helloworld.sh

# 혹은 권한 부여하고 실행
chmod 700 helloworld.sh
./helloworld.sh

```
쉘 스크립트를 작성할 때는 맨 처음에 bin의 풀패스를 적어주는게 필수입니다.  

### 변수
```sh
# 아래와 같이 실행 시키면
./test.sh a b c

# 아래와 같은 변수로 사용할 수 있습니다.
$1 = a, $2 = b, $3 = c

# 스크립트 안에서 변수 선언
CMD=$1

# 변수 사용
"${CMD}"
```

### if문
```sh
# [] 처음과 끝에 공백 필수
if [ 조건문 ]; then
    로직
elif [ 조건문 ]; then
    로직
else
    로직
fi

# 조건 이어 붙이기
# -a : and  // -o : or
[ 조건1 -a 조건2 -a 조건3 ]
[ 조건1 -o 조건2 -o 조건3 ]
```

숫자 비교문|설명|문자 비교문|설명|파일 비교문|설명
---|---|---|---|---|---
A -eq B|A와 B가 같은가|A=B|문자 A, B가 같은가|-d file|파일이 존재하고 디렉토리인가
A -ge B|A가 B보다 크거나 같은가|A != B|문자 A, B가 다른가|-e file|파일이 존재하는가
A -gt B|A가 B보다 큰가|A < B|문자열 A가 B보다 작은가|-f file|파일이 존재하고 파일인가
A -le B|A가 B보다 작거나 같은가|A > B|문자열 A가 B보다 큰가|-r file|파일이 존재하고 읽을 수 있는가
A -lt B|A가 B보다 작은가|-n A|A의 문자열 길이가 0보다 큰가|-s file|파일이 존재하고 비어있지 않은가
A -ne B|A와 B가 같지 않은가|-z A|A문자열의 길이가 0인가|-w file|파일이 존재하고 쓰기가 가능한가


### Case문
```sh
case 변수 in
    패턴1)
        로직;;
    패턴2)
        로직;;
    *)
        디폴트;;
esac
```

### for 문
```sh
# for문
for 변수 in 변수에 넣을 데이터
do
    데이터가 끝날 때까지 반복해서 실행할 명령어
done

# 예시1 - 값 직접 주기
for SVR in cent1 cent2 cent3
do
    echo "${SVR}"
done

# 예시2 - seq 사용해서 1부터 3까지 값 주기
for NUM in $(seq 1 3)
do
    echo "${NUM}"
done

# 예시3 - 파일 cat해서 값 주기
for NUM in $(cat numberlist)
do
    echo "${NUM}"
done
```

### while 문
```sh
# while문
while 조건문
do
    조건이 참인 동안 반복해서 실행할 명령어
done

# 예시 1
NUM=1

while [ "${NUM}" -le 3 ]
do
    echo "cent${NUM}"
    NUM=$(( ${NUM} + 1 ))
done
```

### 함수
```sh
function 함수명 {
    명령어
}

# 예시 1
function line {
    echo "=============================="
}

# 사용
line

# 예시 2
function plus {
    echo "$1 + $2 = "
    # [] 안에 + 사용시 덧셈 연산
    echo $[ $1 + $2 ]
}

# 사용
plus 4 5
```
<Br>

__함수 파일 불러오기__
```sh
#!/bin/bash

# 앞서 작성한 plus가 담긴 파일 가져다 쓰기
source ./calc

plus 45 42
```

### 배열
```sh
arr=(one two three 4 5 6)
echo ${arr[0]} # one
echo ${arr[4]} # 5
echo ${arr[*]} # one two three 4 5 6


for i in $(seq 0 5)
do echo ${arr[${i}]}
done
```

### 리다이렉션
입력/출력/에러(표준 스트림)을 지정한 곳으로 바꿔줍니다.  
쉘 스크립트나 명령을 실행할 때는 입력, 출력, 에러를 0, 1, 2로 숫자로 표현합니다.  
쉘 스크립트 안에서 텍스트 파일을 활용할 때도 사용합니다.  
```sh
# txt2 파일이 없다면
ls -al txt1 txt2

# 출력화면
ls: cannot access txt2: No such file or directory
-rw-rw-r-- 1 ec2-user ec2-user 0  2월  8 14:10 txt1

# 출력은 ok파일로 오류는 ng파일로 쓰기
ls -al txt1 txt2 1> ok 2> ng


# 스크립트 예시
#!/bin/bash

# report 파일 만들고 초기화
touch report
cp -f /dev/null report

# repost 파일에 출력 append
# uptime은 부하 평균을 가져오는 명령
df -h >> report
pstree >> report
free -m >> report
uptime >> report

# 스크립트를 실행시키면 출력 결과가 report 파일에 쓰인다.

report로 쓰는 것을 한번에 처리하기
{
    df -h >> report
    pstree >> report
    free -m >> report
    uptime >> report
}>> report
```
<br>

## 대화식 쉘 스크립트
---
```sh
# p옵션만 사용하면 사용자 입력이 화면에 출력됩니다.
# sp옵션을 사용하며 사용자 입력이 화면에 보이지 않습니다.
read -sp "화면에표시할문자" 변수이름

# 예시
#!/bin/bash
read -p "아무문자나 입력: " ANY
echo "입력한 문자는 "${ANY}""
```

<Br>

## awk
---
텍스트 파일이나 명령 실행결과에서 원하는 필드를 출력할 때 사용합니다.  

```sh
cat txt
# 출력
aaa	111	zzz
bbb	222	xxx
ccc	333	yyy
ddd	444	qqq
eee	555	ddd

# 출력필드의 구분자는 공백입니다.
# 필드 번호는 1부터 시작합니다.

# 위와 같은 txt 파일이 있다고 했을 때 첫 필드만 출력하고 싶다면
# awk print $출력필드 대상
awk '{print $1}' txt

# 출력
aaa
bbb
ccc
ddd
eee

# awk 원하는문자열 print $출력필드 대상
awk '/ccc/ {print $1}' txt

# 출력
ccc
```
```sh
cat txt
aaa:111:zzz
bbb:222:xxx
ccc:333:yyy

# 구분자 변경하기
# -F구분자
awk -F: '{print $1}' txt

# 출력
aaa
bbb
ccc
```





