# git - fork, pull request 협업하기


## Fork
![그림1](https://github.com/backtony/blog-code/blob/master/git/img/3/1.PNG?raw=true)  

타겟 프로젝트의 저장소를 자신의 저장소로 Fork 합니다.  

## Clone, remote

![그림2](https://github.com/backtony/blog-code/blob/master/git/img/3/2.PNG?raw=true)  

Fork로 생성한 본인 계정의 저장소에서 주소를 복사해서 자신의 로컬 저장소에 clone합니다.
```sh
git clone [복사url]

# 예시
git clone https://github.com/hiskfnxh/withme.git
```
<br>

clone한 디렉터리로 들어가서 원격 저장소를 확인합니다.
```sh
git remote -v

# 출력
origin	https://github.com/hiskfnxh/withme.git (fetch)
origin	https://github.com/hiskfnxh/withme.git (push)
```
fork한 로컬 프로젝트는 orgin으로 기본적으로 추가되어 있는 것을 확인할 수 있습니다.  
<br>

```sh
git remote add [별칭] [원본url]

# 예시
git remote add upstream https://github.com/backtony/withme.git
```
원본 프로젝트를 원격 저장소로 등록해줍니다.  
보통 위에서 흐른다고 하여 원본 저장소를 upstream 별칭으로 지정합니다.  

## branch
본격적으로 로컬에서 코드 작업을 수행하기 전에 로컬에서 작업하는 브랜치를 만들어서 진행합니다.
```sh
# 브랜치 생성 및 이동
git checkout -b develop

# 브랜치 확인
git branch
* develop
master
```

## add, commit, push
```sh
git push [remote 별칭] [브랜치명]

# 예시
git push origin develop
```
작업을 진행하고 add, commit 합니다.  

## pull request 생성
![그림3](https://github.com/backtony/blog-code/blob/master/git/img/3/3.PNG?raw=true)  

github의 fork한 프로젝트로 이동하여 develop 브랜치의 contribute를 클릭하여 pull request를 생성합니다. 
<Br>

![그림4](https://github.com/backtony/blog-code/blob/master/git/img/3/4.PNG?raw=true)  

> upstream 리포지토리, 브랜치 <- 나의 fork 리포지토리, 브랜치

를 선택하여 pull request 방향을 정해주고 comment를 적은 뒤 create pull request를 클릭합니다.

## 코드리뷰 merge
![그림5](https://github.com/backtony/blog-code/blob/master/git/img/3/5.PNG?raw=true)  

PR을 받은 원본 저장소 관리자는 코드 변경 내역을 확인하고 Merge 여부를 결정합니다.  
<br>

![그림6](https://github.com/backtony/blog-code/blob/master/git/img/3/6.PNG?raw=true)  

squash merge를 선택하게 되면 그림의 가장 상단처럼 하나의 커밋으로 들어가게 되고 create a merge commit을 선택하게 되면 origin 리포지토리에 커밋된 기록과 하나의 merge 커밋을 추가로 만들어 모든 기록이 들어가는 merge가 됩니다.  


## Merge 이후 동기화 및 branch 삭제
```sh
# master로 이동
git checkout master

# upstream 동기화
git pull [원본 저장소] [브랜치명]
git pull upstream master

# origin에 반영
git push origin master

# 로컬 브랜치 삭제
git branch -d develop
# 원격 브랜치 저장소도 삭제하고 싶은 경우
git push origin --delete [브랜치명]
```
다음 작업을 수행할 경우 이 상태에서 다시 브랜치를 만들어서 작업하고 반복하면 됩니다.

## pull request merge conflict
![그림7](https://github.com/backtony/blog-code/blob/master/git/img/3/7.PNG?raw=true)  
upstream에서 코드를 가져와 작업을 마치고 origin에 푸시하여 pull request를 날리려고 보면 위와 같은 문구가 나올 때가 있습니다.  
내가 로컬에서 작업하는 동안 누군가 upstream에 commit 했기 때문에 지금 pull request를 진행할 경우 merge conflict가 발생한다는 의미입니다.  

### 방법1 : merge 후 PR
이를 해결하기 위해서는 upstream의 코드를 가져와서 merge 한뒤 다시 pull request를 날리면 방법이 있습니다.  
```sh
### develop 브랜치에서 진행 ###

# upstream 코드 가져오기
git fetch upstream master

# merge
git merge upstream/master
```
fetch로 가져오면 우선 아래 그림과 같이 upstream/master 에서 커밋이 발생한 것을 확인할 수 있습니다.  
![그림8](https://github.com/backtony/blog-code/blob/master/git/img/3/8.PNG?raw=true)  
그리고 그것을 develop 브랜치에 merge 해주면 충돌이 해결되는 것입니다.  
만약 merge 과정에서 내가 작업했던 내용이 upstream/master에 들어온 커밋과 겹치는 부분이 발생한다면 수정해주고 스테이징 한 뒤 continue해주면 됩니다.  
```sh
# 충돌 발생 시 해결 후
git add .
git merge --continue
```
![그림9](https://github.com/backtony/blog-code/blob/master/git/img/3/9.PNG?raw=true)  

최종적으로 위와 같은 형태로 그래프가 그려지게 되고 이제 다시 origin 에 push해서 Pull request를 진행하면 됩니다.  

### rabase PR
위와 같이 해도 되지만 그래프에 분기가 생기는 현상이 발생합니다.  
분기가 싫다면 rebase 옵션을 사용하여 그래프를 간결하게 만들어 커밋 히스토리 관리에도 좋게 만들 수 있습니다.  
pull 하기 전에 우선 현재 까지 작업한 코드를 커밋합니다.  
예를 들어 봅시다.  
```
upstream : 기준 커밋 -> A
origin   :         -> B -> C
```
origin에서 upstream 기준 커밋을 기반으로 소스를 가져와 작업한다고 해봅시다.  
origin에서 B와 C 커밋 후 pull request를 보내면 upstream에 A 커밋이 생겼기 때문에 conflict가 발생합니다.  
이때 rebase를 사용한다는 것은 다음과 같습니다.
```
upstream : 기준 커밋 -> A
origin   :         -> A -> B -> C
```
upstream에 있는 커밋 A를 가져와 기준 커밋 다음으로 붙이고 origin에서 작업한 것들을 A 이후에 붙이는 것입니다.  
여기서 A 커밋과 B, C 커밋에서 서로 충돌하는 부분이 없다면 자연스럽게 rebase 되겠지만, A에서 작업한 것과 B, C에서 작업한 것이 같은 부분이라면 충돌이 발생합니다.  
이것은 실제 예시로 봅시다.  
![그림10](https://github.com/backtony/blog-code/blob/master/git/img/3/10.PNG?raw=true)  
upstream의 master을 fetch해서 가져왔더니 하나가 추가된 것이 보이고 로컬에서는 기존 분기점에서 3개의 커밋이 추가된 상황입니다.  
```sh
# fetch로 upstream 상태 확인
git fetch upstream master

# eco 브랜치에서
git pull --rebase upstream master
```
![그림11](https://github.com/backtony/blog-code/blob/master/git/img/3/11.PNG?raw=true)  
3개의 커밋에서 모두 충돌이 발생했고 1개씩 수정을 진행해야 합니다.  
가장 먼저 커밋메시지가 'local에서 새로운 작업'인 커밋의 충돌을 해결합니다.
```sh
# 충돌 수정 후
git add .
git rebase --continue
```
이렇게 되면 커밋 수정 창이 열립니다.  
![그림12](https://github.com/backtony/blog-code/blob/master/git/img/3/12.PNG?raw=true)  
여기서 'local에서 새로운 작업' 커밋의 메시지를 수정할 수 있습니다. 저는 수정없이 진행하겠습니다.  
주석 처리된 내용을 읽어보면 현재 local에서 새로운 작업 커밋을 수정하고 있고 앞으로 새로운 작업1, 새로운 작업 2를 수정해야 한다고 합니다.  
<br>

![그림13](https://github.com/backtony/blog-code/blob/master/git/img/3/13.PNG?raw=true)  
첫 번째 충돌 커밋을 수정했으므로 2/3으로 변경된 것을 볼 수 있게 이제 두 번째 커밋을 수정해줘야 합니다.  
이는 첫 번째 충돌을 해결한 것과 동일하게 진행합니다.  
<Br>

![그림14](https://github.com/backtony/blog-code/blob/master/git/img/3/14.PNG?raw=true)  
그렇게 끝까지 진행했을 때 그래프를 보면 분기없이 하나로 형성된 것을 볼 수 있습니다.  
커밋 히스토리를 깔끔하게 만들 수 있다는 장점이 있지만 커밋이 많고 커밋마다 충돌이 발생하는 경우가 많으면 해당 커밋을 전부 다 고쳐줘야 하는 문제가 발생합니다.  

### 언제 무엇을 선택?
+ 현재 브랜치에 커밋이 많고 master와 비슷한 코드를 수정하여 conflict날 가능성이 높다면 브랜치에서 merge한 후 PR를 보냅니다.
+ 내 브랜치에 커밋이 그리 많지 않고 깔끔한 커밋 히스토리를 남기고 싶다면(오픈 소스 등) rebase를 사용합니다.  


## 발생할 수 있는 다양한 상황들

### 소스 수정을 하다가 보니 새로운 브랜치가 아니라 가져온 그대로 작업하고 있었다
아직 커밋을 하지 않은 상태이므로 그냥 새로운 브랜치를 만들면 수정하고 있는 사항들도 같이 브랜치로 옮겨집니다.
```
git checkout -b [브랜치명]
```

### 소스 수정 후 커밋도 했는데 새로운 브랜치가 아니라 가져온 그대로 작업하고 있었다
현재 상태로 새로운 브랜치를 만들고, 기존 브랜치를 커밋하기 전 상태로 되돌리면 됩니다.
```
git checkout -b [새로운브랜치]
git checkout [원래 브랜치]
git reset --hard [원본 가져온 시점 해시]
```

### 새로운 브랜치를 만들어야 하는데, develop에 아직 코드 리뷰가 끝나지 않은 커밋이 포함되어 있다
원래 작업은 별도의 브랜치에서 수행하고 PR을 날려야 하는데 이를 잊고 별도의 브랜치에서 작업하지 않고 develop 브랜치에서 작업하고 바로 커밋 후 PR을 날렸을 경우 발생하는 문제입니다.  
이 경우 기존 작업의 코드 리뷰가 끝나지 않았는데, 새로운 이슈 작업을 하기 위해 develop에서 새로운 브랜치를 만들면, 코드 리뷰를 받지 않은 기존 작업의 커밋이 따라 붙게 됩니다.  
이 경우에는 해당 커밋이 생성되기 전의 시점에서 새로운 브랜치를 만들면 됩니다.  
예시를 봅시다.  
![그림15](https://github.com/backtony/blog-code/blob/master/git/img/3/15.PNG?raw=true)  
4fee951 해시코드 지점에서 브랜치를 따고 작업했어야 했는데 잊고 계속 작업했고 PR까지 날려버렸습니다. 이제 다른 작업을 해야 하는데 브랜치를 따면 리뷰가 안된 커밋이 새로운 브랜치로 딸려오는 상황입니다.  
```sh
git checkout -b [새로운브랜치명] [기반이되는지점해시코드]

# 예시
git checkout -b feat 4fee951
```
![그림16](https://github.com/backtony/blog-code/blob/master/git/img/3/16.PNG?raw=true)  
원래 브랜치를 따야하는 지점에서 새로운 브랜치를 만들어서 진행하면 됩니다.  

### 뭘 했는지 모르겠는데 이상해졌다
pull을 잘못 받거나 revert를 잘못 했거나 뭘 했는지는 모르겠는데 이상해졌다면 reset을 해주면 됩니다.  
우선 변경 내역을 확인해야 합니다.
```sh
# 내역 조회
git reflog

4fee951 (HEAD -> feat) HEAD@{0}: checkout: moving from master to feat
a155a3e (master) HEAD@{1}: checkout: moving from feat2 to master
a155a3e (master) HEAD@{2}: reset: moving to a155a3e
4fee951 (HEAD -> feat) HEAD@{3}: checkout: moving from master to feat2
a155a3e (master) HEAD@{4}: merge feat: Fast-forward
4fee951 (HEAD -> feat) HEAD@{5}: checkout: moving from feat to master
a155a3e (master) HEAD@{6}: checkout: moving from master to feat
4fee951 (HEAD -> feat) HEAD@{7}: checkout: moving from feat to master
a155a3e (master) HEAD@{8}: checkout: moving from master to feat
4fee951 (HEAD -> feat) HEAD@{9}: reset: moving to 4fee951
a155a3e (master) HEAD@{10}: checkout: moving from feat to master
a155a3e (master) HEAD@{11}: checkout: moving from master to feat
```
되돌아 가고 싶은 지점의 해시 코드를 골라 reset 해줍니다.
```sh
git reset --hard [해시코드]
```

### push 이후 실수를 확인했다
이미 public으로 나간 커밋은 reset하면 안됩니다.  
fork 떠서 origin에 올린 것이라면 로컬에서 reset 후 push force로 되돌릴 수는 있습니다.  
이때는 origin은 반드시 혼자 사용하는 것이어야만 합니다.  
```sh
git reset --hard [되돌아가고자하는해시코드]
git push --force origin [브랜치명]
```
force 옵션을 사용하면 origin에 있는 것을 무시하고 강제로 로컬에 있는 것 그대로 따라가도록 만들어 줍니다.  
<br>

하지만 upstream과 같이 다른 팀원과 같이 사용하는 public에 올라갔다면 reset을 하면 안됩니다.  
revert하여 취소하고자 하는 커밋을 취소하는 새로운 커밋을 만들어 다시 public에 올려야 합니다.  
![그림17](https://github.com/backtony/blog-code/blob/master/git/img/3/17.PNG?raw=true)  
new 2까지 public에 올라간 상황이라고 가정하고 revert 커밋을 revert 하겠습니다.
```sh
git revert [취소하고자하는커밋해시코드]

# 예시
git revert 4b72a25
```
편집창이 뜨고 새로운 커밋 메시지를 입력하면 다음과 같이 형성됩니다.  
![그림18](https://github.com/backtony/blog-code/blob/master/git/img/3/18.PNG?raw=true)  

### merge 커밋을 revert 하고 싶다
![그림19](https://github.com/backtony/blog-code/blob/master/git/img/3/19.PNG?raw=true)  

merge 한 커밋이 public에 올라갔는데 잘못되어 시스템 상에 문제를 일으키는 것으로 밝혀진 경우, 실제 문제를 수정하기에 시간이 걸리므로 우선 merge 작업 자체를 되돌려야 할 수 있습니다. 
```sh
git revert -m [남길 line] [병합 commit 해시]

# 예시
git revert -m 1 14d9f21
```
그래프 상에서 왼쪽 그래프가 1번 라인, 오른쪽 그래프가 2번 라인이 됩니다.  
revert 하면서 살려둘 line을 명시하고 병합 커밋 해시를 주면 됩니다.  

### 이미 커밋 했는데 커밋 메시지를 수정하고 싶다
```
git commit --amend
```
엄밀히 말해 기존 커밋을 수정하는 것이 아니고 새로운 커밋을 만들어 내는 것이기 때문에 이미 public에 push 되었다면 사용하면 안된다.  

### 로컬에만 있어야 하는 파일이 이미 push까지 됐다
설정 파일과 같이 로컬에만 존재해야 하는 파일이 메인 repo까지 실수로 올렸을 때가 있습니다.  
이때는 git에서는 지우면서 로컬에는 지우지 않도록 해야 합니다. 
```sh
# git에서 제거
git rm --cached [파일명]
git rm -r --cached [폴더명]

git commit -m "update secret"
```
제거하고 커밋한 뒤 다시 push하면 됩니다.  
만약 로컬에서도 지우고 싶다면 cached 옵션을 제거하면 됩니다.  

### 브랜치명을 바꾸고 싶다
```
git branch -m [old-name] [new-name]
```

### 비슷한 여러 커밋 정리하기
같은 주제로 작업한 커밋이 여러 개라면 깔끔한 히스토리 관리를 위해 커밋들을 squash 하는 것이 바람직합니다.  

![그림20](https://github.com/backtony/blog-code/blob/master/git/img/3/20.PNG?raw=true)  

```sh
git rebase -i [원하는지점 하나 전 해시코드]
git rebase -i 78ac7ed
```
rebase 하기 위해서는 원하는 시작 지점보다 하나 전의 해시코드를 입력합니다.  
![그림21](https://github.com/backtony/blog-code/blob/master/git/img/3/21.PNG?raw=true)   

기준이 되는 시작 커밋만 그대로 두고 나머지는 앞쪽을 pick 대신 s로 변경합니다.  
저장하면 새로운 커밋 메시지를 입력하는 창이 뜨고 커밋 메시지를 입력하고 완료합니다.  
![그림22](https://github.com/backtony/blog-code/blob/master/git/img/3/22.PNG?raw=true)   
커밋이 합쳐진 것을 확인할 수 있습니다.  

### squash하고 싶은데 순서가 얽혀 있다
![그림23](https://github.com/backtony/blog-code/blob/master/git/img/3/23.PNG?raw=true)   
```sh
git rebase -i 522eca1

# 수정창
pick 8c4f4a7 s4
pick 1b556ac in
pick ca41d52 s5

# 순서 변경과 설정 변경
pick 8c4f4a7 s4
s ca41d52 s5
pick 1b556ac in
```
순서를 변경해주고 pick을 s로 바꿔서 squash 해줍니다.  
이후 커밋창에 메시지를 입력해주면 다음과 같이 커밋이 형성됩니다.  
![그림24](https://github.com/backtony/blog-code/blob/master/git/img/3/24.PNG?raw=true)   


<Br><Br>

__참고__  
<a href="https://wayhome25.github.io/git/2017/07/08/git-first-pull-request-story/" target="_blank"> git 초보를 위한 풀리퀘스트(pull request) 방법</a>   
<a href="https://deepinsight.tistory.com/167?category=1001060" target="_blank"> 협업을 위한 git & GitHub</a>   
<a href="https://meetup.toast.com/posts/116" target="_blank"> GitHub 환경에서의 실전 Git 레시피</a>   
<a href="https://milooy.wordpress.com/2018/10/25/git-rebase-or-merge-commit/" target="_blank"> [Git] Pull Request를 보내기 전에, Rebase를 해야 할까요 혹은 merge commit을 만들어야 할까요?</a>   





 