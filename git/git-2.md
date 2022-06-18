
# git 브랜치 전략과 커밋 컨벤션

## 브랜치 전략이란?
---
브랜치 전략이란 여러 개발자가 협업하는 환경에서 git저장소를 효과적으로 활용하기 위한 work-flow를 의미합니다.  
브랜치 전략을 사용하면 브랜치의 생성, 삭제, 병합이 자유료운 git의 유연한 구조를 활용하여 다양한 방식으로 소스관리를 할 수 있습니다.  

## git-flow
![그림1](https://github.com/backtony/blog-code/blob/master/git/img/2/strategy-1.PNG?raw=true)  
5가지 브랜치를 이용해 운영하는 브랜치 전략입니다.  
항상 유지되는 2개의 메인 브랜치와 역할을 완료하면 사라지는 3개의 보조 브랜치로 구성됩니다ㅏ.
+ 메인 브랜치 : 항상 유지됩니다.
    - master : 제품으로 출시될 수 있는 브랜치
    - develop : 다음 출시 버전을 개발하는 브랜치
+ 보조 브랜치 : merge 되면 사라집니다.
    - feature : develop 브랜치에 새로 추가할 기능을 개발하는 브랜치
    - release : develop 브랜치 개발이 완료되면 출시버전을 준비하면서 QA, TEST를 위해서 사용하는 임시 브랜치
    - hotfix : master 브랜치에서 발생한 버그를 수정하는 브랜치

### 개발 프로세스
![그림2](https://github.com/backtony/blog-code/blob/master/git/img/2/strategy-2.PNG?raw=true)  
1. 개발자는 develop 브랜치로부터 본인이 개발할 기능을 위한 feature 브랜치를 만듭니다. 
2. feature 브랜치에서 기능을 만들다가, 기능이 완성되면 develop 브랜치에 merge하고 해당 feature 브랜치는 삭제합니다.

feature 브랜치를 develop 브랜치에 merge 하는 시점에 develop과 conflic되는 내용이 있다면 충돌 부분을 수정하고 add 해준 뒤 git merge \-\-continue 해주면 됩니다.  
<br>


![그림5](https://github.com/backtony/blog-code/blob/master/git/img/2/strategy-5.PNG?raw=true)  
feature 브랜치에서 작업을 하고 develop 브랜치에 merge하는 시점에 브랜치가 fast-forward 관계에 있다면 develop 입장에서는 Feature의 커밋기록들을 동일하게 가져와도 문제가 없다고 판단하여 브랜치의 참조값만 변경하여 merge commit을 생성하지 않습니다. 이런 방식으로 commit 기록들이 형성되게 되면 어떤 feature와 관련된 commit이 어디서부터 해당하게 되는지 확인하기 어렵게 됩니다.  
<br>

![그림6](https://github.com/backtony/blog-code/blob/master/git/img/2/strategy-6.PNG?raw=true)  
따라서 \-\-no-ff 옵션을 주어 fast-forward 관계에 있어서 merge commit을 생성하여 해당 브랜치가 존재하였다는 정보를 남겨주도록 하는 것이 권장됩니다. 이런 기능들은 브랜치의 존재 여부를 알려줄 뿐만 아니라 code내에 오류가 발생하였을 때 또는 개발한 기능을 제거하려고 할 때 commit 기록을 되돌리기가 쉬워진다는 장점이 있습니다. 
```
git merge --no-ff 브랜치명
```


<br>

![그림3](https://github.com/backtony/blog-code/blob/master/git/img/2/strategy-3.PNG?raw=true)  
3. 이번 배포 버전의 기능들이 develop브랜치에 모두 merge 됐다면, QA를 위해 release 브랜치를 생성합니다.
4. release 브랜치에서 오류가 발생했다면 release 브랜치 내에서 수정합니다. QA가 끝나면, 해당 버전을 배포하기 위해 master 브랜치로 merge합니다. 만약 release 브랜치에서 bugfix가 있었다면 해당 내용을 반영하기 위해 develop 브랜치에도 merge 합니다.
5. 만약 develop 브랜치에 merge 할 때 그 사이 develop 내의 추가적인 개발사항이 있어 충돌이 발생할 수 있는데 이 경우에는 충돌이 난 부분을 수정하고 add 한 뒤 git merge \-\-continue 하면 됩니다.
6. release 브랜치를 삭제합니다.

merge 할 때는 \-\-no-ff를 사용하여 기록을 그룹화하고 master로 merge 후에는 tag 명령을 통해 버전을 명시합니다.  
release 브랜치의 경우 이름은 release-* 로 보통 주게 됩니다.  
버그 수정 및 버전 번호, 빌드 날짜와 같은 메타 데이터를 준비하며 기능 개발은 금지되는 브랜치입니다.  


<br>

![그림4](https://github.com/backtony/blog-code/blob/master/git/img/2/strategy-4.PNG?raw=true)  
5. 만약 제품(master)에서 버그가 발생한다면, hotfix 브랜치를 만듭니다.  
6. hotfix 브랜치에서 버그 픽스가 끝나면, develop와 master 브랜치에 각각 merge 하고 삭제합니다.
7. master 에 merge 후에는 tag 명령을 통해 입전 버전보다 높은 버전을 명시합니다.


hotfix 브랜치 이름은 보통 hotfix-* 형태로 주게 됩니다.  


### 예시
![그림7](https://github.com/backtony/blog-code/blob/master/git/img/2/strategy-7.PNG?raw=true)  
1. master 브랜치에는 15.3 버전이 업로드 되어 있습니다.
2. develop 브랜치에는 15.4 버전 기능 출시를 위해 개발이 진행중입니다.
3. 2개의 feature 브랜치에서 그에 따른 개발을 진행중입니다.
4. 현재 버전에서 보안 문제가 발생하여 hotfix 브랜치를 생성하여 해결하고 master와 develop으로 merge 합니다.
5. 개발 기능을 완료하여 develop 브랜치에 merge 합니다.
6. release 브랜치를 생성하여 QA와 베타버전을 출시합니다.
7. 버그를 발견하면 수정 후 master와 devlop에 merge하여 최종 배포합니다.




## 커밋 메시지 컨벤션
+ feat : 새로운 기능 추가
+ fix : 버그 수정
+ hotfix : 급하게 치명적인 버그 수정
+ docs : 문서 수정
+ style : 코드 포맷팅, 세미콜론 등의 스타일 수정(코드 자체 수정 X)
+ refactor : 프로덕션 코드 리팩토링
+ test : 테스트 코드, 테스트 코드 리팩토링
+ chore : 빌드 과정 또는 보조 기능(문서 생성 기능 등) 수정
+ rename : 파일 혹은 폴더명을 수정하거나 옮기는 작업만인 경우
+ remove : 파일을 삭제하는 작업만 수행한 경우
+ comment : 필요한 주석 추가 및 변경

```
// 커밋 작성 예시
type: subject

body

footer
```
body와 footer은 필요할 때만 적어도 되는 옵션에 해당한다고 보면 됩니다.

```md
feat: 압축파일 미리보기 기능 추가

사용자의 편의를 위해 압축을 풀기 전에 다음과 같이 압축파일 미리보기를 할 수 있도록함
- 마우스 오른쪽 클릭
- 윈도우 탐색기 또는 맥 파인더의 미리보기 창

closes #125
```
closes 대신 사용할 수 있는 키워드가 여럿 존재합니다.
+ close
+ closed
+ fix
+ fixed

이외에도 여럿 존재하는데 아무거나 사용하면 됩니다.  
이슈 여러 개를 한 번에 처리할 수도 있습니다.
```
... 생략

close #1, close #12
```







 