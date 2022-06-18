# HTTP - 헤더 - 캐시와 조건부 요청

## 1. 검증 헤더와 조건부 요청
---
### Last-Modified
![그림1](https://github.com/backtony/blog-code/blob/master/http/img/6/6-1.PNG?raw=true)


웹 브라우저에서 GET 방식으로 /Star.jpg 라는 사진을 요청했다고 해보자. 그럼 서버에서는 바디에 사진을 담아주고 cache-control헤더에 캐시의 유효시간을 적어주고, Last-Modified헤더로 이 데이터가 마지막에 수정된 시간을 담아서 응답한다. 웹 브라우저에서는 브라우저 캐시에 응답 결과를 저장한다. 캐시의 유효 시간 내에 같은 요청을 보내면 네트워크를 탈 필요 없이 바로 브라우저 캐시에서 가져오게 되면서 매우 빠르게 사용할 수 있다.
<br>

![그림2](https://github.com/backtony/blog-code/blob/master/http/img/6/6-2.PNG?raw=true)

캐시의 유효 시간이 지난다면 같은 요청을 서베에 보내게 된다. 이때 if-modified-since 검증 헤더에 데이터의 최종 수정일을 담아서 요청을 보내게 된다.
<br>

![그림3](https://github.com/backtony/blog-code/blob/master/http/img/6/6-3.PNG?raw=true)

서버에서 이것을 확인하고 만약 데이터의 최종 수정일이 변경되지 않았다면 304 Not Modified를 응답으로 보내면서 HTTP Body를 비워서 보낸다. 응답을 받은 웹브라우저는 브라우저 캐시에서 유효 시간이 지난 캐시를 다시 응답에서 받은 유효 시간으로 갱신해서 데이터를 재사용한다. 캐시 유효시간이 지나면 네트워크를 다시 타야하지만 결과적으로 응답받아오는 용량이 매우 줄어들기 때문에 효율적이다.

#### 단점
+ 날짜 기반 로직 사용으로 같은 데이터를 수정해서 최종 데이터 결과가 같아도 날짜가 수정된다.
+ 1초 미만 단위로 캐시 조정 불가능
+ 서버에서 별도의 캐시 로직을 관리하고 싶은 경우 불가능
    - 스페이스나 주석처럼 크게 영향이 없는 변경에서는 캐시를 유지하고 싶은 경우


<br>

### ETag
캐시용 데이터에 임의의 고유한 버전 이름을 달아두어 인해 날짜 기반 로직의 단점을 보완할 수 있다. 예를 들면 파일을 해쉬 알고리즘에 넣어서 결과값을 ETag로 주는 것이다.(파일의 컨텐츠가 같으면 같은 해쉬값이 나오고 조금이라도 다르면 다른 해쉬값이 나옴) 클라이언트 입장에서는 단순하게 ETag만 보내서 같으면 유지, 다르면 다시 받기를 하게 된다. 따라서 데이터 변경 날짜가 다르더라도 데이터만 같으면 HTTP Body를 다시 채워올 필요가 없게 되고 캐시 제어 로직을 서버에서 완전히 관리하게 된다.

![그림4](https://github.com/backtony/blog-code/blob/master/http/img/6/6-4.PNG?raw=true)
![그림5](https://github.com/backtony/blog-code/blob/master/http/img/6/6-5.PNG?raw=true)

Last-Modified 방식이랑 똑같고 Last-Modified 대신 ETag가 들어갔을 뿐이다. 위 상황은 브라우저에서 그림을 요청하고 응답을 받아서 브라우저 캐시에 저장하고 사용하다가 유효시간이 지나서 If-None-Match 헤더에 ETag값을 넣어서 다시 요청을 한 상황이다. 그리고 서버에서는 ETag에 변화가 없으므로 변화가 없다는 응답을 준 것이다.

<br>

## 2. 캐시 제어 헤더
---
+ Cache-Control 캐시 지시어
    - Cache-Control: max-age 
        - 캐시 유효 시간, 초단위
    - Cache-Control: no-cache
        - 데이터는 캐시해도 되지만, 항상 원(origin) 서버에서 검증하고 사용
    - Cache-Control: no-store
        - 데이터에 민감한 정보가 있으므로 저장하면 안되고 메모리에서 사용하고 삭제
+ Expires 캐시 만료일 지정
    - 캐시 만료일을 정확한 날짜로 지정
    - 지금은 더 유연한 Cache-Control: max-age 권장되고 같이 사용되면 Expires가 무시된다.
    - GMT 시간으로 사용


<br>

## 3. 프록시 캐시
---
![그림6](https://github.com/backtony/blog-code/blob/master/http/img/6/6-6.PNG?raw=true)

프록시 캐시 서버가 없다면 미국에 있는 원 서버에 응답 받아오는데 0.5초씩 걸린다. 하지만 프록시 캐시 서버가 있다면 첫 사용자만 0.5초가 걸리고 이후에는 프록시 캐시 서버에 저장되어 있기 때문에 다음 사용자부터는 0.1초가 걸리게 된다.

+ Cache-Control 캐시 지시어
    - Cache-Control: public 
        - 응답이 public 캐시에 저장되어도 됨
    - Cache-Control: private  (기본값)
        - 응답이 해당 사용자만을 위한 것으로 private 캐시에 저장해야 함
    - 아래 두개는 있다는 정도만 알아두자.
    - Cache-Control: s-maxgage
        - 프록시 캐시에만 적용되는 max-age
    - Age: 60 (HTTP 헤더)
        - 오리진 서버에서 응답 후 프록시 캐시 내에 머문 시간(초)
    

<br>

## 4. 캐시 무효화
---
캐시를 적용하지 않아도 웹 브라우저가 임의로 캐시를 하기도 한다. 만약 이 페이지가 캐시가 되면 안되는 경우 확실하게 막아줄 방법이 필요하다.
```
Cache-Control: no-cache, no-store, must-revalidate
Pragma: no-cache 
```
캐시를 확실하게 무효화 하기 위해서는 위 코드를 다 적어줘야 한다. Pragma: no-cache는 HTTP 1.0 하위 호환의 경우 제어하는 것이다. must-revalidate 빼고는 다 위에서 설명했다.

+ Cache-Control: must-revalidate 
    - 캐시 만료후 최초 조회시 원 서버에 검증
    - 원 서버 접근 실패시 반드시 오류 발생 504 Gateway Timeout
    - 캐시 유효 시간이 남아있다면 캐시를 사용

![그림7](https://github.com/backtony/blog-code/blob/master/http/img/6/6-7.PNG?raw=true)

no-cache의 경우 원 서버에 접근할 수 없는 경우 캐시 서버 설정에 따라 프록시 캐시에서 캐시를 반환할 수도 있다. 하지만 must-revalidate는 원 서버에 접근이 불가능한 경우 반드시 오류를 발생시킨다. 따라서 위 코드를 다 적어주면 캐시를 확실하게 무효화할 수 있다.






<Br><Br>

__참고__  
<a href="https://www.inflearn.com/course/http-%EC%9B%B9-%EB%84%A4%ED%8A%B8%EC%9B%8C%ED%81%AC#" target="_blank"> 모든 개발자를 위한 HTTP 웹 기본 지식</a>   


