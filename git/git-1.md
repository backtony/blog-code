# Git 사용법 정리

## 1. Set up
---
#### git 명령어 형식
```
git 명령어 옵션 
```
<br>

#### 모든 config 설정 정보 보기
```
git config --list

global config 보기
git config --global --list
```
<br>

#### 설정 정보 텍스트에디터로 열기
```
git config -e
git config --global -e 
```
<br>

#### 글로벌 에디터로 vscode 설정하기
```
파일이 열린 상태에도 터미널에서 다른 명령어 수행 가능
git config --global core.editor "code"

열어진 파일이 종료되기 이전에는 다른 명령어 수행 불가능
git config --global core.editor "code --wait"
```
<br>

#### 사용자 설정
```
git config --global user.name 사용자이름
git config --global user.email 사용자이메일

등록 됬는지 확인
git config user.name
git config user.email


특정 프로젝트에서만 바꾸고 싶다면 해당 프로젝트로 이동해서 수정
git config user.name 사용자이름
git config user.email 이메일
```
<br>

#### 줄바꿈 문자열 설정
+ 운영체제마다 에디터에서 줄바꿈을 할 때 들어가는 문자열이 다름
    - 윈도우 : \r\n
    - 맥 : \n
    - \r 을 carriage-return 이라고 함
+ 서로 다르기 때문에 이 문제를 해결하는 방식
    - 윈도우에서 git에 저장할 때 \r을 제거해주고 가져올 때는 \r을 붙여줌
    - 맥에서 git에 저장할 때 \r을 제거해주고 가져올 때는 그냥 가져옴, 실수로 \r이 붙을 수도 있으므로 제거해주는 것

```
윈도우
git config --global core.autocrlf true

맥
git config --global core.autocrlf input
```
<br>


## 2. Basic
---
#### 깃 초기화
```
git init
```
git 폴더 안에 init을 했다면 .git이라는 숨겨진 폴더가 만들어지고 그 안에는 git 내부 구현사항들이 저장되어 있다.  
기본적으로 master branch가 생긴다
<br>

#### 깃 폴더 열기와 삭제
```
윈도우
start .git

맥
open .git

git 삭제
rm -rf .git
```
<br>

#### alias 설정
git status 같이 자주 사용하는 것들을 단축해서 사용하기 위한 목적
```
git config --global alias.단축명령어 실제명령어

예시
git config --global alias.st status

git status -> git st 로 사용 가능
```
<br>

#### 명령어의 옵션 확인하기
```
git 명령어 --h

// 얘시
git config --h
```
<br>

#### 스테이징 하기
```
git add 파일
git add 파일 파일 파일 ...
git add *.txt  // txt 확장자 파일 모두 스테이징
git add * // 지워진 파일을 제외하고 나머지 모두 스테이징, 삭제된 파일이 있으면 삭제는 무시하고 나머지 스테이징
git add . // 모든 파일 스테이징, 삭제된 파일이 있다면 스테이징에도 삭제 처리
```
<br>

#### 세심하게 스테이징 하기
```
git add p

여러 옵션들이 나올텐데 ?를 엔터치면 옵션에 대한 설명이 나온다.
y를 누르면 add 
n를 누르면 스테이징에 올리지 않음
q 나가기
```
변경 내용을 하나하나 확인하면서 스테이징에 올릴 수 있다.  
하나의 파일 안에서도 각각 단위를 나눠서 보여주고 이것을 스테이징에 올릴지 말지 결정할 수 있다.  



<Br>

#### 스테이징에서 working directory로 옮기기
```
git rm --cached 파일

// 예시
git rm --cached a.txt
git rm --cached *
```
<br>

#### 추가하고 싶지 않다면 ignore
```
.gitignore 라는 파일에 *.log를 출력한다는 의미
echo *.log > .gitignore // 없으면 만들고 있으면 덮어씌움
echo log.log >> .gitignore // 다음 행에 추가


.gitignore 파일에 작성하는 것들의 의미
*.a : .a 파일 전부 무시
!lib.a : .a파일을 모두 무시하더라도 lib.a는 무시하지 않음
build/ : build 디렉토리 무시
/TODO : 현재 디렉토리에서 TODO 파일만 무시, 서브디렉토리 안에 있는 TODO는 무시되지 않음
doc/*.txt : doc디렉토리 안에 있는 .txt 파일 모두 무시, doc의 안에 서브 안에 .txt는 무시되지 않음
doc/**/*.pdf : doc 디렉토리 안에 있는 모든 .pdf 무시
```
.gitignore 안에 있는 git에서 더이상 추가, 추적하고 싶지 않는 파일들을 적어놓을 수 있다.

<br>

### .gitignore에 추가해도 git에서 변화를 계속 감지할 경우
```
git rm -r --cached .
git add .
git commit -m "Fix untracked files"
```

<br>

### 현재 상태 확인 status
```
git status  // 상세
git status -s // 간단하게
```
-s 로 간단하게 보면 앞에 알파벳으로 상태가 표기된다.
+ A : 스테이징된 상태
+ ?? : 트레킹 되지 않은 working directory에 있는 상태
+ AM : 스테이징되어 있는데 working directory에 수정된 내용이 있는 상태

<br>

### 파일 비교 diff
c.txt 에 hello가 적혀있고 스테이징에 올린 상태에서 c.txt에 add를 추가했다고 가정해보자. 이때 git status -s 해보면 AM으로 working directory에서 변경되었다는 상태는 확인할 수 있지만 정확히 무엇이 수정되었는지는 알지 못한다.
```
git diff
```
아무런 옵션이 없으면 working directory를 기준으로 비교해서 볼 수 있다. git diff 해보면 아래같은 화면이 나온다.
```
diff --git a/c.txt b/c.txt
index a0565..df64646546 00644
--- a/c.txt
+++ b/c.txt
@@ -1 +1,2 @@
 hello world!
+add
(END)
```
+ a라는 것은 이전 버전을, b는 현재 버전을 의미한다. 여기서 이전 버전이란 working directory에 있는 파일이라면 이전에 commit된 버전을 가리키거나 또는 스테이징에 있는 것을 의미한다. 현재 가정 상태는 커밋한게 없으니 스테이징 상태가 a가 된다.  
+ index 는 깃 내부적으로 파일을 참고할 때 사용하는 것
+ --- a/c.txt 는 이전 버전을 의미
+ +++ b/c.txt 는 지금 현재 수정된 버전
+ -1 : 마이너스는 이전 파일을( a/c.txt) 의미, -1은 이전 파일은 1번째 줄을 보라는 뜻
+ +1,2 : 플러스는 현재 파일을( b/c.txt) 의미, 1,2 -> 첫번째줄에서 두번째줄까지 확인하라는 뜻 
+ +는 추가된 것을 의미, hello world!라는 기존 상태에서 +add를 보아 add가 추가되었구나를 확인
+ -add 가 있었다면 줄이 삭제되었다는 것을 의미

<br>

```
git diff --staged
```
--staged 옵션을 붙여주면 스테이징을 기준으로 비교하게 된다. 
```
diff --git a/b.txt b/b.txt
new file mode 100644
index 0000000..12a8798
--- /dev/null
+++ b/b.txt
@@ -0,0 +1 @@
+hello world!
```
이전에는 아무것도 없었는데 현재 스테이징 된 것에는 +hello world!가 추가되었다.
<br>

### 누가 코딩했는지 알아내기
```
// 파일의 부분별로 작성자 확인하기
git blame 파일명

// 특정 부분 지정해서 작성자 확인하기
// 괄호는 구분하기 쉽게 작성한 것임 -> 실제는 필요 없음
git blame -L (시작줄) (끝줄, 또는 줄수) (파일명)
```
위 명령어를 사용하기 보다는 gitlens 플러그인을 설치해서 사용하면 마우스를 갖다대면 바로 확인할 수 있다.

<br>

#### 오류가 발생한 시점 찾아내기
```
// 이진 탐색 시작을 알린다.
git bisect start

// 현재 커밋에서 오류가 발생한다면 
git bisect bad

// 어느 시점 이후로 오류가 발생했다고 예상되는 시점으로 이동
git checkout 해당커밋해시

// 해당 커밋에서는 오류가 발생하지 않았다면
git bisect good

// 이후부터는 git이 이진탐색으로 커밋을 checkout 시킨다.
// 옮겨진 위치마다 돌려보고 git bisect good, bad를 입력하다 보면
// 최종적으로 오류가 처음 발생하는 시점을 찾아준다.
```

<br>

### 커밋하기
```
스테이징 상태에 있는 것 메시지와 함께 커밋하기
git commit -m "메시지"

추적중인 working directory와 스테이징에 있는 것 모두를 메시지와 함께 커밋하기
git commit -am "메시지"
```
<br>

### 현재 커밋의 변경사항 확인하기
```
git commit -v
```

<br>

### 삭제
```
rm c.txt 

git rm c.txt
```
rm 으로 지우면 스테이징에 포함되지 않으므로 따로 add를 해줘야 한다. 하지만 git rm을 사용하면 바로 스테이징에 적용된다.
<br>

### 파일명 수정
```
mv c.txt o.txt

git mv c.txt o.txt
```
mv로 파일명을 수정하면 스테이징에 포함되지 않으므로 따로 add해줘야 한다. 하지만 git mv를 사용하면 바로 스테이징에 적용된다.

<br>

### restore
```
git restore --staged 파일명
```
staging area에서 working directory로 옮긴다.  
staged 옵션을 빼면 working directory에서도 제거한다.(아애 원래대로 되돌린다는 뜻)


<Br>

### log 활용법
```
git log
git log -p // 각 커밋마다의 변경사항 함께 보기
git log --oneline // 로그 한줄로 보기
git log --oneline --graph --all
git log -3 // 최근 커밋 3개만 보기
git log --oneline -3 // 원라인으로 최근 3개 커밋만 보기
git log --author="backtony" // 작성자가 backtony인것만 보기
git log --before="2020-09-08" // 날짜 이전의 커밋로그 보기
git log --grep="project" // 커밋메시지 중 project가 포함된 커밋만 보기
git log -S "about" // 커밋중 소스코드 컨텐츠 안에서 about이라는 코드가 변경사항에 있는 커밋 찾기
git log -S "about" -p // 커밋중 소스코드 컨텐츠 안에서 about이라는 코드가 변경사항에 있는 커밋 자세히 보기
git log about.txt // about.txt 파일에 대한 커밋 로그 보기
git log -p about.txt // about 파일에 대한 커밋 로그 자세히 보기
git log HEAD~1 // HEAD의 이전 커밋부터 로그 보기
git log HEAD~2 // HEAD의 2번째 전 커밋부터 로그 보기
git show 해시코드 // 커밋에 해당하는 정확한 내용 확인, 해시코드 적당히 붙여넣으면 된다. 전부를 복붙하지 않아도됨
git show 해시코드:파일명 // 커밋에 여러 내용이 있을 때 특정 파일에 대한 정보만 확인
git diff 해시코드 해시코드  // 두가지 커밋에 대한 비교
git diff 해시코드 해시코드 파일 // 두가지 커밋에서 해당 파일에 대한 비교
```
+ 로그가 찍히면 위에 있을 수록 최신의 커밋 log이고 아래로 갈 수록 오래된 것이다. 
+ 아무런 옵션 없이 사용하면 commit 아이디, 작성자, 시간, 메시지까지 확인할 수 있다.
+ p옵션 : 수정된 파일 내용까지 확인 (diff 로 보는 것과 마찬가지로 볼 수 있음)
+ oneline : 한줄로 간단하게 확인, 해쉬코드 앞자리 문자열과 커밋 메시지, HEAD 확인 가능
+ graph : 그래프로 전체 브렌치 정보 보기

<br>

### HEAD 와 checkout
```
git checkout 해쉬코드
```
+ HEAD는 현재 속한 브랜치의 가장 최신 커밋을 가리킨다. 
    - HEAD~1 은 지금 헤드의 이전 버전을, HEAD~2는 헤드 2번째 전의 버전을 가리킨다.
    - HEAD는 현재 속한 브랜치의 가장 최신 커밋을 가리키는데 checkout으로 HEAD를 이동시킬 수 있다?
    - 이 말은 HEAD를 이동시키면 이는 익명의 하나의 브랜치를 만들어서 이동하고 있다는 의미이다.
    - 즉, HEAD~1로 이동하면 HEAD의 1개 전 커밋으로 이동해서 해당 커밋에서 갈라진 아직 이름이 지어지지 않은 브랜치에 위치해 있다는 뜻이다.
    - 그 상태에서 브랜치에 이름을 주고 작업을 진행하고 싶다면 'git switch -c 브랜치명' 을 주면 된다.
+ checkout : 원하는 버전(커밋)이나 브랜치로 이동할 수 있다.

```
// 2단계 뒤로
git checkout HEAD~~ 
// 1단계 뒤로
git checkout HEAD~
// 5단계 뒤로 이동
git checkout HEAD~5

// 방금 이동한 작업을 취소시킨다.
// ctrl+z 한것 처럼 방금 전 위치로 이동함
git checkout -
```


<br>

### 로그 이쁘게 만들기
```java
git log --pretty=format:"원하는대로 만들기"
```
<br>

### 깃 태그
깃 히스토리에 커밋이 많아지고 로그가 길어지면 특정 부분으로 돌아가기 어렵다. 특정한 커밋 부분에 북마크 해두는 것이 깃 태그다. 이를 이용해서 원하는 부분으로 쉽게 돌아갈 수 있다. 태그는 보통 릴리즈 버전으로 표기한다.
```
versionMajor.minor.fix 순서
v2.0.0
```
+ major : 전체적인 업데이트 발생으로 올라가는 번호
+ mojor : 조금의 기능 업데이트
+ fix : 성능 개선, 오류 수정

```
git tag 태그명 // 현재 마지막 커밋에 태그 달기
git tag 태그명 해시코드 // 특정 커밋에 태그 달기
git tag 태그명 해시코드 -a // 편집기가 열리면서 태그 메시지 입력 가능
git tag 태그명 해시코드 -m "release note...." // 태그에 대한 메시지 바로 입력

git show 태그명 // 누가 만들었는지, 언제, 메시지 정보 까지 커밋 정보 확인 가능
git tag // 만들어진 모든 태그 보기
git tag -l "v1.0.*" // 특정 명이 포함된 태그 보기
git tag -d 태그명 // 따움표 없어야함, 태그 삭제

git checkout 태그명 // HEAD를 태그로 이동
git checkout -b 브렌치명 태그명 // 태그로 checkout 하면서 브렌치 만들기

git push origin 태그명 // 내가 만든 태그를 서버에도 업로드
git push origin --tags // 만든 모든 태그 서버에 업로드
git push origin --delete 태그명 // 서버에 업로드 된 태그 삭제
```
<br>


## 3. 브랜치
---
git에서 따로 지정하지 않는 이상 master branch를 사용한다. 별도로 branch를 만들지 않으면 master 한 줄기에서 계속 커밋이 진행된다. master branch에는 기능에 문제 없이 정확하게 검증된 제품에 포함되어도 되는 내용들만 포함되어 있다. 만약 fA라는 기능을 개발한다면 master branch에서 개발하는게 아니라 fA라는 브랜치를 새로 만들어서 그곳에서 커밋을 해나간다. 이렇게 fA 개발이 완료되면 이 커밋들을 master branch로 merge 한다. 이후에 fA 브랜치는 더 이상 필요 없으므로 삭제한다. 대부분은 fA 브랜치에서 작업한 커밋들이 전부 master branch에 merge될 필요가 없기 때문에 fA 브랜치의 커밋을 하나로 만든다음에 master branch로 merge한다.
```
git branch // local에서 현재 갖고 있는 브랜치 확인
git branch --all // 서버에 있는 모든 브랜치 확인
git branch -v // 브랜치와 가장 최신 커밋 같이 확인
git branch 브랜치명 // 새로운 브랜치 생성, 만들어지기만 하고 현재 head는 유지
git switch 브랜치명 // head를 브랜치로 이동
git switch -C 브랜치명 // 브랜치 만들면서 바로 이동
git checkout 브랜치명 // 원하는 브랜치로 이동
git checkout 해시코드 // 해당하는 커밋으로 이동, head가 해당 커밋으로 이동, 폴더 파일들이 해당 버전으로 변함
git checkout -b 브랜치명 // 원하는 브랜치 만들고 그 브랜치로 이동, checkout과 switch가 겹치는게 많으므로 아무거나 사용
git branch --merge // 현재 브랜치에 merge된 브랜치 확인, 현재 브랜치에서 생성되고 더 파생되지 않은 것도 포함
git branch --no-merged // 현재 브랜치에 파생되서 다른 커밋이 있는 것들 중 merge되지 않은 것들 
git merge 브랜치명 // 현재 브랜치와 merge
git branch -d 브랜치명 // 브랜치 삭제
git push origin --delete 브랜치명 // 원격 저장소에도 해당 브랜치 삭제
git branch -m 브랜치명 변경브랜치명 // 브랜치명 변경
git log master..브랜치명 // 마스터와 브랜치 사이에 있는 커밋만 확인
git hist master..브랜치명 // 앞서 alias 했던 hist사용
git diff master..test // 서로 브랜치 사이에 변경 코드 확인
```
<br>

#### fast-forward merge
master에서 새로운 브랜치를 생성해서 새로운 커밋으로 작업을 이어나가다가 끝내서 master와 병합하려고 할 때, master에서는 여전히 아무런 작업 없이 그대로 라면 굳이 브랜치와 master를 병합할 때 새로운 커밋을 만드는 것이 아니라 master의 HEAD를 브랜치의 HEAD로 옮겨버리고 branch를 삭제해버리는 방식이 fast forward merge이다. fast forward는 히스토리에 merge되었다는 사실이 남지 않는다. 
```
git merge 브랜치명 // 마스터와 브랜치가 merge되어 master의 HEAD가 브랜치쪽으로 이동
git branch -d 브랜치명 // 브랜치 삭제
```
최신 master 에서 파생된 브랜치에서 커밋이 발생하고 master에서 커밋이 없었다면 merge를 하면 바로 fast forward merge가 되는 것이다.  
<Br>

fast forward를 사용하지 않고 병합 커밋을 만들어서 merge하려면 아래와 같이 하면 된다.
```
git merge --no-ff 브렌치명 // fast forword하지 않고 기존 master에 merge
git branch -d feature-c
```

<br>

#### Three-way merge
master에서 브랜치가 파생되어 커밋된 시점에 master에서도 커밋이 발생한 경우 fast forward merge가 불가능하다. 이때는 양쪽의 변동 사항을 모두 합해서 merge commit을 만들어 master 브랜치에 커밋해야한다. 하지만 이 과정을 알아서 동작한다.
```
git merge 브랜치명
```
git merge를 사용할 경우 fast forword가 가능한 경우 fast forword로 동작하고, 불가능할 경우 따로 merge commit이 만들어진다.

<br>

#### conflict(충돌) 해결
두 가지 브랜치에서 동일한 파일 수정했을 때 merge를 하면 conflict이 발생한다. 먼저 conflict가 발생하면 vscode로 열리도록 설정해야한다. 참고로 P4MERGE라는 툴도 있다.
```
git config -global -e 

열린 vscode에서 아래와 같은 코드 추가
[merge]
	tool = vscode
[mergetool "vscode"]
	cmd = code --wait $MERGED


git merge 브랜치명 시점에 conflict 발생시 
git mergetool 
```
git mergetool 입력시 vscode가 열리는데 변경사항에 4가지 옵션이 있다.
+ Accept Current Change : 현재 브랜치의 변경 내용 받아들이기
+ Accept Incoming Change : merge 대상의 변경 내용 받아들이기
+ Accept Both change : 둘다 받기
+ Compare Changes : 다른점 비교하기

수정을 완료하고 저장한 뒤 git status를 확인해보면
```
changes to be committed:
    modified : 파일명.txt
untracked files:
    파일명.txt.orig
```
같이 나온다. 수정된 것 따로 원본은 orig처럼 따로 나오는 것이다. 이렇게 오류 해결 전 파일이 만들어지는 것을 끄는 옵션이 있다.
```
git config --global mergetool.keepBackup false
```
merge 상황에서 어떠한 이유로든 conflict 발생 등으로 인해 merge 이전으로 되돌리고 싶다면 
```
git merge --abort
```

어찌됬든 수정을 마치고 merge 작업을 진행하고 싶다면
```
// 수정한 작업 add
git add . 
// merge 계속
git merge --continue
```
rebase 충돌도 merge 충돌과 유사하다.  
merge의 경우 두 가지 브랜치를 하나의 커밋으로 몰아서 만들어 주기 때문에 충돌이 발생하면 한 번의 작업으로 끝나지만 rebase의 경우 가지를 때서 이어붙이는 것이기 때문에 충돌이 여러번 발생한다.  
즉, rebase하는 브랜치 가지에서 하나의 커밋을 master에 붙이고 또 하나의 커밋을 master에 붙이는 작업이기 때문에 각각 충돌에 대해 처리해줘야 한다.  
따라서 위에 merge 충돌 해결한 것 처럼 수정하고 add 하고 continue 하는 과정이 여러번 발생한다.(git status를 해보면 어느 것이 문제인지 확인할 수 있다.)  
또한, rebase는 master의 경우 head가 변하지 않기 때문에 rebase하고 master로 돌아와서 git merge 브랜치 해줘야 head가 이동된다.

<br>

#### rebase
master d에서 파생된 featureA브랜치에서 e와 f의 커밋이 이루어져있고 master에서 g 커밋이 이루어졌을 때, featureA의 branch를 master의 최신 버전으로 rebase하면 fast-forword가 가능하다.  
즉, 2갈래로 뻗은 상태에서 master 끝에다가 featureA브랜치를 이어 붙이게 된다. 하지만 이렇게 되는 경우 결국 커밋이 새로 업데이트되는 것이기 때문에 rebase는 브랜치를 혼자 사용할 경우에만 사용해야 한다. 포인터의 정보가 변경되면 기존의 commit이 유지되지 않고 새로운 커밋이 발생하기 때문에 다른 개발자가 동일한 브랜치에서 작업하고 있다면 나중에 merge conflict이 발생할 수 있다.
```
git checkout featureA // 리베이스할 브랜치로 이동
git rabase master // 마스터 최신 버전으로 rebase
git checkout master
git merge featureA // fast-forword merge
```
<br>

#### rebase --onto
위와 똑같은 상황에서 featureA에서 파생된 featureB라는 브랜치가 있다고 해보자. 그런데 featureA는 필요없고 featureB만 master에 merge하고 싶다면 rebase onto 옵션을 사용한다.
```
git rebase --onto master 첫파생브랜치명 첫파생에또파생브랜치명
```
master 브랜치의 최신 커밋 이후에 파생브랜치를 이어 붙이게 된다. master에서 merge하게 되면 fast forword merge가 된다. 하지만 이 역시 서버에 이미 올라가 있거나 혼자 개발하는 것이 아니라면 사용하면 안된다.
<br>

#### merge squash
```
git merge --squash 브랜치명
```
브랜치에 있는 모든 수정사항을 master(현재 브랜치)로 가져와서 스테이징 한다.  
커밋이 되는 것은 아니므로 직접 commit 해줘야 한다.  
브랜치는 삭제되지 않고 그대로 있기 때문에 squash해서 가져오고 커밋해서 필요없어졌을 때는 브랜치를 삭제해주면 된다.  

<br>

#### cherry pick
다른 브랜치에 있더라도 원하는 커밋만 가져오는 방식
```
git cherry-pick 해시코드
```
해시코드에 해당하는 커밋을 master브랜치로 가져온다.

<br>

## 4. Stash
---
working directory나 스테이징 area에 아직 commit하지 못한게 있는데 다른 작업을 해야할 때 하던 작업을 마치 스택에 잠시 저장해놓는 것이다.  
stash 하고 싶은 대상들은 __반드시 스테이징 상태__ 에 있어야 한다.  
stash 해두면 다른 브랜치나 다른 커밋 위에서도 적용할 수 있다.
```
git stash -p // git add -p 옵션처럼 stash 하고 싶은 것을 하나씩 골라서 할 수 있다.
git stash push -m "메시지" // 현재 working, 스테이징에 있는 것 stash로 보내고 비우기, 아직 트래킹 대상이 아닌 건 무시
git stash push -m "메시지" --keep-index // working, 스테이징 있는 것 stash 보내면서 현재 상태를 비우지 않고 유지, 아직 트래킹 대상이 아닌 건 무시
git stash -u // 트래킹 대상 아닌 것도 포함해서 stash
git stash list // stash 상태 확인, 맨위가 stash@{0}인데 숫자가 작을수록 가장 최근에 들어온 것
git stash show stash@{0} // 간단한 수정 내용 확인, stash@{0}는 git stash list에서 확인
git stash show stash@{0} -p // 자세한 수정 내용 확인
git stash apply // 가장 최근 것 꺼내기, stash는 그대로 유지
git stash apply stash@{0} // stash에서 해당 stash 꺼내오기, stash는 그대로 유지
git stash pop // 가장 최근 것 꺼내면서 stash에서 지우기
git stash drop // 가장 최근 stash 지우기
git stash drop stash@{0} // 해당 stash 지우기
git stash branch 브랜치명 // 새로운 브랜치를 만들고 이동하면서 가장 최근에 stash된 것을 적용시킴(pop됨)
git stash clear // stash 전부 비우기
```
<br>

## 5. 커밋한 시점으로 돌아가기
---
history를 다시 작성하는 것은 다시 새로운 commit을 만드는 것과 동일하므로 history가 서버에 이미 push 되었는지 다른 개발자들과 협업하는 지를 우선적으로 확인하고 사용해야 한다. 즉, 이 기능은 서버에 업로드 하지 않은 시점에서만 사용해야 한다.

#### 커밋전 취소
```
git restore 파일명 // working directory에 있는 파일의 수정 내역 되돌리기, untracked file은 취소가 안됨
git restore . // working directory에 있는 파일 전체 수정내역 되돌리기
git reset --hard // working directory와 스테이징 상태에 있는 모든 수정 내역 되돌리기
git restore --staged 파일명 // 스테이징 상태 working directory로 옮기기
git restore --staged . // 스테이징 상태 전체를 working directory로 옮기기
git restore --source=해시코드 파일명 // 파일을 특정 커밋시점 상태로 초기화
git restore --source=HEAD~숫자 파일명// 파일을 HEAD 숫자이전의 커밋시점 상태로 초기화
git clean -fd // working directory 파일중에 modified가 아니라 untracked file의 경우 restore로 삭제 불가능, 이 방법을 이용해서 untracked file삭제
```
<br>

#### 커밋 메시지 수정
```
git commit --amend -m "수정메시지" // 최근 커밋 메시지 한줄로 변경
```
<br>

#### 최근 커밋에 다른 것 추가하기
```
해당 파일 수정하고 add로 추가한 다음
git commit --amend // 최근한 커밋을 현재 add 한것을 포함해서 수정
```
최근 커밋에 포함했어야 했는데 깜빡하고 포함하지 않았을 때 사용한다.  

<br>

#### reset 초기화
```
git reset HEAD~숫자 // 아래 설명과 똑같음
git reset 해시코드 // 해시코드 커밋으로 이동하고 그 이후의 커밋 삭제, 이후 커밋의 작업들은 working directory로 이동
git reset --soft HEAD~숫자 또는 해시코드// HEAD가 해당숫자로 이동하고 그 이후의 커밋은 삭제, 이후의 커밋의 작업들은 스테이징으로 이동
git reset --hard HEAD 또는 해시코드 // 위와 똑같은데 이후 커밋의 작업들을 옮기지 않고 모두 삭제 -> 딱 해당 커밋시점으로 돌아감
git reset --hard // 마지막 커밋 상태로 돌아가기 -> 마지막 커밋 이후 수정 작업하고 있는데 전부 커밋안하고 원래대로 되돌리고 싶을 때 사용
```
보통 해당 커밋 시점으로 돌아가고 싶으면 git reset --hard 해시코드 를 사용해서 돌아가고 미래로 돌아가고 싶다면 깃허브의 해시코드를 가져와서 git reset --hard 해시코드로 돌아간다.
<br>

#### reflog 원하는 시점으로 다시 돌아가기
```
git reflog // 실행했던 명령어들과 해시코드까지 확인
get reset --hard 해시코드 // 확인한 해시코드를 이용해서 그 지점으로 돌아가기
```
이것은 커밋했었어야만 그 시점으로 돌아가는 것이다. 커밋하지 않은 시점에서 실수로 git reset --hard 했다면 다른 방법을 사용해야 한다.  
이런 경우 Extenstion을 활용해야 한다. IntelliJ에는 기본적으로 local history가 포함되어 있다. 로컬에서 작업하고 있는 파일들을 시간별로, 분별로 자동으로 저장되기 때문에 잘못해서 reset 하는 경우에 local history를 이용해서 예전 버전으로 돌아갈 수 있다.  
<br>

#### 취소사항을 버전으로 남기기 revert
reset으로 언제든지 예전 커밋 시점으로 돌아갈 수 있지만 history에 예전으로 돌아갔다는 것이 남지 않는다. 예를 들어, 제품을 릴리즈 하고나서 뒤늦게 문제가 생겼을 경우, 해당하는 커밋을 완전히 제거해야 하는 경우가 있다. 
```
git revert 해시코드 // 해당 커밋에서 했던 변경 했던 모든 내용들 삭제하고 새로운 커밋 생성
git revert --no-comit 해시코드 // 바로 커밋하지 않고 취소되는 변경사항을 스테이징에 추가해준다. -> 추가 작업하고 커밋할 수 있다는 뜻이다.
```
보통 revert는 옵션 없이 사용한다. revert는 정말 그 내용만 작성해서 commit해야하기 때문에 자동으로 커밋이 생성되게 하는게 맞다.  
__이것은 새로운 커밋이 생성되므로 이미 서버에 commit된 경우 reset, rebase가 아니라 revert를 사용해야 한다. revert는 새로운 커밋을 만들어서 이미 추가된 내용을 변경하는 것이므로 history를 수정하지 않기때문에 언제든지 자유롭게 사용할 수 있다.__  
<br>

여기서 만약 revert로 취소하고자 하는 커밋의 내용이 파일을 추가하는 작업이었고, 이후 커밋에서 해당 파일을 수정하는 커밋이 있다고 가정해보자.  
이 경우에는 revert할 경우 충돌이 발생하고 직접 해결해줘야 한다.   
git status를 쳐보면 '병합하지 않은 경로'라고 문제되는 파일이 나온다.  
해당 파일을 열어서 의도에 맞게 수정해주거나, 삭제해주면 된다.  
그리고 git revert --continue 를 입력해주어 revert 작업을 마무리해주면 된다.  

<br>

### interactive rebasing
amend 옵션으로 최신 커밋의 수정사항이나 내용을 수정했었다. 그런데 최신 커밋이 아니라 그 이전의 것을 수정하고 싶다면 interactive rebasing을 사용한다. 최신이 아니라 그 이전의 것을 rebasing 하는 순간 그 수정 이후의 모든 포인터들이 업데이트 되야하기 때문에 수정 이후의 것들이 전체적인 history가 업데이트 된다. 그러므로 서버에 업데이트 되어있으면 사용 불가능하다.  
<br>

#### 커밋 메시지 수정
```
수정 대상의 이전 해시코드부터 시작해야 한다
git rebase -i 수정하고자하는것이전의해시코드
```
위 코드를 입력하면 세팅한 편집기가 열리면서 입력한 해시코드 이후 해시코드부터 순차적으로 화면에 아래와 같이 표기된다.  
```
pick fd5cee3 add main // 입력한 해시 코드 바로 이후 커밋
pick 460e9e6 cancel update
pick 0dbc785 test update
pick 13f53d9 test
pick 557fdf4 hello
pick a6de5dd add check chceck // 가장 최근 커밋
```
이렇게 나온 창 바로 아래는 키워드에 대한 설명이 표기된다.  
설명을 읽어보면 수정하고자 하는 커밋을 pick 대신 reword(또는 r)로 수정하고 저장한 뒤 편집기를 종료하면 해당 커밋에 대해 수정하는 편집창이 열린다. 거기서 커밋 메시지를 수정하고 저장하고 끄면 최종적으로 변경이 완료된다.
<br>

#### 커밋 삭제하기
```
git rebase -i 수정하고자하는것이전의해시코드
pick -> drop 으로 수정
충돌 발생시 삭제 커밋 이후 것을 적용하려면
git add .
rebase 진행
git rebase --continue
```
뜨는 창 설명을 읽어보면 알겠지만 삭제는 drop이다. 만약 drop 했는데 그 커밋에서 있던 파일을 다음 커밋에서 수정하고 있으면 충돌 오류가 난다. git status를 통해 힌트를 얻을 수 있다. 삭제 커밋에 있는 것은 삭제하고 이후 커밋의 파일로 적용하려면 add . 하고 git rebase --continue 를 해주면 해결할 수 있다.
<br>

#### 커밋 쪼개기
커밋 하나에는 하나의 작업이 있는게 좋은데 과거의 커밋 하나에 2개의 작업을 넣어서 분리하고 싶을 때 사용법
```
git rebase -i 수정하고자하는것이전의해시코드
```
창 뜨면 작업할 것을 edit로 저장하고 끈다. 그럼 HEAD가 수정할 커밋 위치로 변경되어 있다. 
```
git reset HEAD~ // 한단계 전으로 돌리고 해당 내용 working으로 옮기기
```
이 상태에서 하나씩 add. 하고 커밋해서 둘로 분리하고 
```
git rebase --continue
```
작업이 완료되면 head는 다시 master로 돌아가고 수정한 자리에 커밋이 둘로 쪼개져서 만들어진다.  
<br>

#### squash 여러 개 커밋 하나로 만들기
```
git rebase -i 수정하고자하는것이전의해시코드
```
```
pick fd5cee3 add main
pick 460e9e6 cancel update
pick 0dbc785 test update
s 5082758 update check check
```
위와 같이 표기하면 update check check와 test update 커밋이 합쳐지게 된다.  

<br>

## 6. 깃허브
---
#### 깃허브 프로젝트 내 pc에 가져오기
```
git clone 복사한주소 // 리포지토리 원격에서 받아오기
git remote // 원격서버에 관련된 정보, 기본적 서버 이름은 origin
git remote -v // origin이 정확히 어떤것을 가리키는지 확인
git remote add 만들서버이름 주소 // origin 말고 다른 원격 서버원격 저장소 설정
git remote show // 원격저장소 정보
git remote show 서버이름 // 서버의 자세한 정보
git remote rm 원격저장소명(예를들면 origin) // 연결된 원격 제거
```
<br>

#### 내 커밋 서버에 저장하기
```
git push
git push -f // 충돌이 났을때 서버 작업을 무시하고 local것으로 대체
```
<br>

#### 이미 만들어진 프로젝트 깃허브에 업로드하기
깃허브에서 새로운 리포지토리 만들어두고
```
git remote add origin 리포지토리주소
git push // main에 푸시
```

<br>

#### fetch와 pull
git에서 local과 github와 함께 연동해서 작업하는 경우 나의 local에서는 github에 있는 branch를 origin이라고 표기한다. origin/master와 master이 현재 b를 가리키고 있는 상황에서 github에서 새로운 commit이 발생하여 서버의 origin/master은 c를 가리키고 있다고 해보자.  
이때 fetch를 사용하면 서버에 있는 git history를 받아와서 나의 local git history를 업데이트 하게 된다. 그럼 local에서 b를 가리키고 있던 origin/master은 c를 가리키게 되지만 local에서 내가 현재 바라보고 있는 작업 환경 HEAD는 그대로 유지된다. 즉, github에 업데이트된 내용을 로컬에 내려받는 것은 아니고 어디까지 업데이트 됬는지 확인만 하도록 하는 것이다.  
반면에 pull의 경우 fetch + merge(옵션에 따라 rebase) 작업을 한번에 하는 것이다. github의 history를 가지고 와서 나의 local에 있는 내용을 함께 merge를 하게 된다. 따라서 origin master와 나의 master이 모두 c를 가리키게 된다.  
fetch의 경우에는 서버의 history를 업데이트해서 어떤 작업이 일어나고 있는지 확인하고 싶을 때 사용되고, 서버에 있는 내용을 받아와서 나의 local 버전도 서버와 동일하게 만들고 싶을 때 pull을 사용한다.  
fetch를 하면 아직 로컬에 받아온 것은 아니지만, git checkout origin/master 을 이용해서 해당 코드 내역을 확인할 수 있다.  
```
git fetch // 기본적으로 origin으로 동작
git fetch 서버명 // 서버명 remote를 fetch
git fetch 서버명 브랜치 // 서버의 특정한 브랜치에 해당하는 정보만 fetch

git pull // pull 하기
```
<br>

#### local과 서버에서 동일한 파일 수정으로 충돌
서버와 local에서 동일한 add.txt 파일을 수정한 상태에서 git pull하면 충돌이 발생한다. pull은 서버의 history를 가지고와 local에 있는 내용을 merge하는 것이다. 
```
git pull // 충돌 발생
git add . // 변경 내용 수정하고 스테이징
git merge -- continue // 병합 다시 진행
최종적으로 local과 서버가 merge된 새로운 커밋 생성
```
이렇게 되면 서버와 local이 갈라지고 그게 merge된게 새로운 커밋으로 생성된다. 너무 지저분해진다. 따라서 rebase사용해서 그래프를 한 줄로 처리한다.
```
// 일단은 아래처럼 pull하면 충돌 발생
git pull --rebase // 서버에 있는 commit을 가져와서 그 위에 local에서 만든 것을 올리기
git add . // 변경내용 수정하고 스테이징
git rebase --continue 
```
서버에 있는 커밋을 그대로 가지고 와서 기존에 로컬에서 커밋되었던 것만 rebase 했기 때문에 해당 commit만 새로운 커밋으로 생성되고 나머지 서버에서 가져온 커밋은 그대로 유지된다.  
__보통 협업할 때 rebase를 사용하지 말라고 하는데 그것은 rebase에서 push하지 말라는 의미고 pull할 때는 rebase를 해도 된다.__
<br>

#### 강제로 local에 맞추기
```
git push --force
```
github에 있는 것에 따르지 않고 강제로 local에 있는 상태로 덮어씌운다.
<br>

#### 깃허브와 local간의 차이가 있을 때 pull
+ 서로 다른 파일에 차이가 있다. -> pull이 문제 없이 알아서 합쳐주고 커밋을 만들어준다.
    - 기본적으로 merge가 발생하기 때문에 local 갈래와 remote 갈래를 따로 만들고 하나로 합치는 커밋을 만든다.
    - 즉, 합치는 과정에서 커밋 하나가 더 생긴다.
    - 이걸 피하고 깨끗하게 유지하고 싶다면 git pull --rebase를 사용하면 된다.
    - 이는 remote 내용을 가져와서 그 위에다가 local 내용을 붙인다.    
+ 서로 같은 파일에 수정 -> pull 하면 문제 발생
    - 문제가 발생한 파일을 알려줄텐데 해당 파일을 보면 서로의 차이가 적혀있다.
    - 수동으로 수정하고 git add. 후  git merge --continue 한다.
    - 애초에 merge로 하지 않고 --rebase로 pull 받았다면 수정 후 add . 후 git rebase --continue 하면 된다.

<br>

#### 원격에 브랜치 올리기
test 브랜치를 만들고 원격에 올리고 싶다면 다음과 같이 한다.
```
방법 1 origin에 test 브랜치 푸시
git push origin test

방법 2 안내사항에는 다음과 같이 나온다
git push --set-upstream origin test

방법 3 위 옵션은 아래와 같이 축약이 가능하다.
git push -u origin test
```
위 방식의 차이는 upstream을 사용하면 이후에는 계속 해당 원격을 따라가기 때문에 1번 방식처럼 origin test를 계속 붙이지 않고 git push 만으로 업로드 가능하다.

<br>

#### 원격 브랜치 목록 같이 보기
```
기본적으로 local 브랜치 목록만 확인 가능
git branch

원격에 있는 브랜치 목록까지 같이 확인 가능
git branch -a
```
<br>

#### 원격에만 있는 브랜치 로컬로 가져오기
```
git switch -t remote명/브랜치명
git swtich -t origin/hello
```

<Br>

#### 브랜치 활용하여 협업하기 pull request
+ pull reqeust : 깃허브에서 merge하는 요청을 말한다.
    - 예를 들면, 깃허브에서 브랜치가 main과 test가 있을 때 두 개를 하나로 merge 하는 작업이라고 보면 된다.

일단 협업할 리포지토리에는 기본적으로 프로젝트 구조가 있을 것이다. 예를 들면 spring의 처음 시작 프로젝트같은 것들 말이다.  
1. clone으로 local로 가져온다.
2. 같은 브랜치에서 작업하면 다른 사람과 꼬일 수 있으니 바로 브랜치를 만들고 해당 브랜치로 이동한다.
    - git branch 브랜치명 // 브랜치 만들기
    - git checkout 브랜치명 // 브랜치로 이동
3. 작업을 시작한다.
4. 작업 완료 후 push 한다
    - git push origin 내가작업한브랜치명
5. github의 리포지토리를 보면 compare & pull request가 떠있을 것이다. 클릭한다.
6. base(합병 몸통)와 compare(합병할 것)을 정하고 어떤 내용인지 적어준 뒤 create pull request 클릭
7. merge pull request -> confirm merge 하면 완료 -> 브랜치 지우고 싶으면 바로 뜨는 delete branch 클릭

<br>

#### Fork로 협업하기
1. 프로젝트 리포지토리 Fork -> 해당 리포지토리를 복제해서 내 계정 리포지토리로 복제에서 가져오게 된다.
2. clone으로 local로 가져오기
3. 작업 시작후 push -> 내 리포지토리에 push 되어 있을 것임
4. 내 리포지토리에서 pull requests 클릭 new pull request -> create pull request -> 제목 내용 적기 -> create pull request
5. 프로젝트 리포지토리에서 pull requests 클릭 -> 요청온 pull reqeust 클릭하고 변경사항 확인하고  merge pull request 하면 본 프로젝트에서도 반영 완료



## 7. 간략 실습
### Branch
![그림1](https://github.com/backtony/blog-code/blob/master/git/img/1/dream-1.PNG?raw=true)  
```
git branch step1
git checkout step1
```
메인에서 브랜치를 만들고 체크아웃합니다.  
git log를 입력해보면 HEAD가 step1으로 이동한 것을 확인할 수 있습니다.  
HEAD는 현재 내가 보고 있는 곳을 의미합니다.  
아직까지는 master와 step1는 같은 로그를 가리키고 있습니다.
<br><br>


![그림2](https://github.com/backtony/blog-code/blob/master/git/img/1/dream-2.PNG?raw=true)  
```
vim hello.txt
git add .
git commit -m "hello"
```
step1 브랜치에서 파일을 하나 만들고 커밋을 하고 git log를 입력해보면 로그가 분리되어 2개로 생성된 것을 확인할 수 있습니다.  
step1은 방금 커밋한 것을 가리키고, master는 이전의 것을 가리키고 있습니다.

### Merge
![그림3](https://github.com/backtony/blog-code/blob/master/git/img/1/dream-3.PNG?raw=true)  
```
git checkout master
git branch bugfix
git checkout bugfix
vim bugfix.txt
git add .
git commit -m "bugfix"
```
Branch 실습 상태에서 마스터로 이동한 뒤 bugfix 브랜치를 만듭니다.  
bugfix.txt 파일을 만들고 add하여 commit 합니다.  
그럼 위와 같은 형태로 만들어지게 됩니다.  
이제 step1과 bugfix 브랜치를 합치는 작업을 해보겠습니다.  
![그림4](https://github.com/backtony/blog-code/blob/master/git/img/1/dream-4.PNG?raw=true)  
```
git checkout step1
git merge bugfix
```
step1 브랜치에서 bugfix 브랜치를 머지하면 새로운 커밋이 생성되고 step1은 새로운 커밋을 가리킵니다.  
bugfix는 여전히 자신의 기존 커밋을 가리키고 있으므로 bugfix도 커밋 4를 가리키도록 해봅시다.
![그림5](https://github.com/backtony/blog-code/blob/master/git/img/1/dream-5.PNG?raw=true)  
```
git checkout bugfix
git merge step1
```
이때는 새로운 커밋이 생성되지 않고 bugfix 브랜치가 커밋4를 가리키게 됩니다.  
![그림6](https://github.com/backtony/blog-code/blob/master/git/img/1/dream-6.PNG?raw=true)  
이렇게 되면 머지한 작업들이 히스토리에 남게 됩니다.  
어떤 면에서는 확인하기 편할 수도 있지만 많아지면 확인하기 불편할 수도 있습니다.  

### Rebase
![그림3](https://github.com/backtony/blog-code/blob/master/git/img/1/dream-3.PNG?raw=true)  
다시 이 상태로 돌아와서 Rebase 작업을 해보겠습니다.
```
git rebase step1
```
![그림7](https://github.com/backtony/blog-code/blob/master/git/img/1/dream-7.PNG?raw=true)  
rebase하면 기존 커밋3이 커밋1과 연결을 끊고 커밋 2뒤로 붙게됩니다.  
step1은 여전히 커밋 2를 가리키고 있기 때문에 커밋3으로 옮겨주도록 하겠습니다.
```
git checkout step1
git rebase bugfix
```
bugfix가 step1의 부모쪽에 있었기 때문에 단순히 step1이 가리키는 커밋의 위치만 이동하게 됩니다.  
![그림8](https://github.com/backtony/blog-code/blob/master/git/img/1/dream-8.PNG?raw=true)  
이때는 새로 히스토리가 생성되지 않기 때문에 깔끔한 히스토리를 볼 수 있습니다.


### revert
reset의 경우 히스토리 자체를 고쳐쓰는 것이기 때문에 함께 사용하는 환경에서는 사용할 수 없습니다.  
![그림9](https://github.com/backtony/blog-code/blob/master/git/img/1/dream-9.PNG?raw=true)  
커밋2를 했는데 만약 커밋2가 잘못된 커밋이었다면 revert를 사용해서 되돌려야합니다.   
<BR><Br>

![그림10](https://github.com/backtony/blog-code/blob/master/git/img/1/dream-10.PNG?raw=true)  
```
git revert HEAD
```
이때 되돌리는 것은 해당 커밋작업을 되돌리는 새로운 커밋을 생성하는 것입니다.  
즉, 커밋1이 새로운 커밋으로 생성됩니다.

### Cherry-pick
![그림3](https://github.com/backtony/blog-code/blob/master/git/img/1/dream-3.PNG?raw=true)  
다시 이상태로 돌아와 커밋2를 cherry-pick으로 가져와보겠습니다
```
git cherry-pick step1
```
이렇게 되면 커밋 2가 복사되어 커밋 3뒤에 붙게 되면서 새로운 커밋이 만들어집니다.



<Br>

## 8. 커밋 관리하기
---
### 널리 사용되는 커밋 메시지 작성 방식
```
type: subject

body (optional)
...
...
...

footer (optional)
```
__예시__
```
feat: 압축파일 미리보기 기능 추가

사용자의 편의를 위해 압축을 풀기 전에
다음과 같이 압축파일 미리보기를 할 수 있도록 함
 - 마우스 오른쪽 클릭
 - 윈도우 탐색기 또는 맥 파인더의 미리보기 창

Closes #125
```
__타입__  

타입|설명
---|---
feat|새로운 기능 추가
fix|버그 수정
docs|문서 수정
style|공백, 세미콜론 등 스타일 수정
refactor|코드 리팩토링
perf|성능 개선
test|테스트 추가
chore|빌드 과정 또는 보조 기능(문서 생성기능 등) 수정


+ subject
    - 커밋의 작업 내용 간략히 설명
+ body
    - 길게 설명할 필요가 있을 시 작성
+ fotter
    - breaking point가 있을 때
    - 특정 이슈에 대한 해결 작업일 때

보통은 type과 subject만 적는다고 한다.
<Br>

## 9. submodules
---
하나의 프로젝트에서 여러 개의 모듈을 다룰 때 사용한다.  
예를 들어 github상에 리포지토리가 2개로 구분되어 있고 하나를 메인으로 하나를 서브모듈로 사용하고자 한다고 가정해보자.  
로컬에는 메인만 가지고 있다고 했을 때 아래와 같이 명령어를 입력한다.
```
git submodule add (submodule의 GitHub 레포지토리 주소) (하위폴더명, 없을 시 생략)
```
로컬 프로젝트 폴더 내 submodule로 가져온 내용과 .gitmodules 파일이 생기게 된다.  
이제부터는 master 쪽에서 커밋하고 푸시하면 master쪽으로가고 submodule로 이동해서 커밋하고 푸시하면 서브 모듈쪽 리포지토리로 푸시가 된다.  
만약 submodule 안에 있는 내용을 수정하고 master에서 푸시하면 master에는 올라가지 않는다.  
즉, submodule 안에 있는 깃과 master 에 있는 깃은 서로 독립적이다.
