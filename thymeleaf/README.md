# Spring MVC - 타임리프
  

## 1. 타임리프를 사용하는 이유
---
순수 HTML 파일을 웹 브라우저에서 열어도 내용 확인 가능하며, 서버를 통해 뷰 템플릿을 거치면 동적으로 변경된 결과 확인 가능하기 때문에 사용합니다. 이렇게 순수 HTML을 그대로 유지하면서 뷰 템플릿도 사용할 수 있는 타임리프의 특징을 네츄럴 템플릿 이라 합니다.
<br>

## 2. 타임리프 핵심
---
th:xxx 가 붙은 부분은 서버사이드에서 렌더링 되고, 기존의 것을 대체합니다. th:xxx 이 없으면 기존 html 속성이 적용됩니다.
<br>

## 3. 문법
---
### 사용 선언
```html
<html xmlns:th="http://www.thymeleaf.org">
```
맨 위에 있는 html 태그 안에 thymeleaf를 사용하겠다고 적어줘야 합니다.
<br>

### 단순 출력하기 th:text
```html
<ul>
  <li th:text="${data}">th:text 사용 </li>
  <li>컨텐츠 안에서 직접 출력하기 = [[${data}]]</li>
</ul>

<!-- 실제 출력 화면 -->
Hello Spring
컨텐츠 안에서 직접 출력하기 = Hello Spring
```
컨트롤러에서 model에 data키로 Hello Spring을 넣었다고 가정합니다.  
첫 번째 줄의 데이터는 th:text 문장을 data가 대체해버립니다.  
두 번째 줄의 [[]] 문법을 사용하면 그 안에 바로 데이터가 출력됩니다.

### 단순 출력하기 th:utext
웹 브라우저는 < 를 HTML의 시작 태그로 인식합니다. 따라서 < 를 태그의 시작이 아니라 문자로 표현할 방법이 필요한데, 이것을 __HTML 엔티티__ 라고 합니다.  
HTML에서 사용하는 특수문자를 HTML 엔티티로 변경하는 것을 __이스케이프__ 라고 합니다.  
기본적으로 타임리프가 제공하는 th:text와 [[]]는 기본적으로 이스케이프를 제공합니다.  
따라서 model에 <b\></b\> 와 같은 태그를 넣으면 태그가 적용되지 않고 <b\></b\>가 그대로 출력됩니다..  
이스케이프를 사용하지 않고 태그를 적용시키는 방식은 __th:utext와 [()]__ 입니다.  
```html
<ul>
    <li>th:text = <span th:text="${data}"></span></li>
    <li>th:utext = <span th:utext="${data}"></span></li>
</ul>

<h1><span th:inline="none">[[...]] vs [(...)]</span></h1>
<ul>
    <li><span th:inline="none">[[...]] = </span>[[${data}]]</li>
    <li><span th:inline="none">[(...)] = </span>[(${data})]</li>
</ul>

<!-- 실제 출력 화면 -->
th:text = Hello <b>Spring</b>
th:utext = Hello Spring <!-- spring 글씨가 굵게 출력됨-->

[[...]] = Hello <b>Spring</b>
[(...)] = Hello Spring <!-- spring 글씨가 굵게 출력됨-->
```
컨트롤러에서 model에 data키로 Hello <b\>Spring</b\> 을 넣었다고 가정합니다.  

### 변수 - SpringEL
타임리프에서 변수를 사용할 때는 변수 표현식을 사용합니다.
```java
@GetMapping("/variable")
public String variable(Model model){
    User userA = new User("userA", 10);
    User userB = new User("userB", 20);

    List<User> list = new ArrayList<>();
    list.add(userA);
    list.add(userB);

    HashMap<String, User> map = new HashMap<>();
    map.put("userA",userA);
    map.put("userB",userB);

    model.addAttribute("user",userA);
    model.addAttribute("users",list);
    model.addAttribute("userMap",map);

    return "basic/variable";
}
```
자바 코드에서 위와 같이 모델에 담아서 html로 넘겨줍니다.
<Br>

```html
<ul>Object
    <li>${user.username} =    <span th:text="${user.username}"></span></li>
    <li>${user['username']} = <span th:text="${user['username']}"></span></li>
    <li>${user.getUsername()} = <span th:text="${user.getUsername()}"></span></li>
</ul>
<ul>List
    <li>${users[0].username}    = <span th:text="${users[0].username}"></span></li>
    <li>${users[0]['username']} = <span th:text="${users[0]['username']}"></span></li>
    <li>${users[0].getUsername()} = <span th:text="${users[0].getUsername()}"></span></li>
</ul>
<ul>Map
    <li>${userMap['userA'].username} =  <span th:text="${userMap['userA'].username}"></span></li>
    <li>${userMap['userA']['username']} = <span th:text="${userMap['userA']['username']}"></span></li>
    <li>${userMap['userA'].getUsername()} = <span th:text="${userMap['userA'].getUsername()}"></span></li>
</ul>


<div th:with="first=${users[0]}">
<p>처음 사람의 이름은 <span th:text="${first.username}"></span></p>
</div>

<!-- 실제 출력 화면 -->
Object
+ ${user.username} = userA
+ ${user['username']} = userA
+ ${user.getUsername()} = userA
List
+ ${users[0].username} = userA
+ ${users[0]['username']} = userA
+ ${users[0].getUsername()} = userA
Map
+ ${userMap['userA'].username} = userA
+ ${userMap['userA']['username']} = userA
+ ${userMap['userA'].getUsername()} = userA

처음 사람의 이름은 userA
```
위와 같이 접근해서 data를 꺼낼 수 있다.  
th:with는 지역 변수를 선언해서 사용할 수 있도록 하는 방식이다.  
위 코드에서는 first에 userA를 담아두고 사용했다.

### 기본 객체들
타임리프틑 기본 객체들을 제공합니다.  
+ ${#request}
+ ${#response}
+ ${#session} 
+ ${#servletContext} 
+ ${#locale}

그런데 #request 는 HttpServletRequest 객체가 그대로 제공되기 때문에 데이터를 조회하려면 request.getParameter("data") 처럼 불편하게 접근해야 합니다.  
이를 간편하게 하기 위한 편의 객체도 제공합니다.  
+ HTTP 요청 파라미터 접근 - param
    - 요청에서 쿼리파라미터로 paramData=hello를 줬다면 ${param.paramData} 로 꺼내 사용할 수 있습니다.
+ HTTP 세션 접근 - session
    - session에 seesionData로 값이 담겨있다면 ${session.sessionData} 로 꺼내 사용할 수 있습니다.
+ 스프링 빈 접근 - @
    - helloBean이라는 빈이 있고 그 안에 hello 메서드가 있다면 ${@helloBean.hello('Spring!')} 로 메서드 반환값을 사용할 수 있습니다.

```html
<ul>
    <li>request = <span th:text="${#request}"></span></li>
    <li>response = <span th:text="${#response}"></span></li>
    <li>session = <span th:text="${#session}"></span></li>
    <li>servletContext = <span th:text="${#servletContext}"></span></li>
    <li>locale = <span th:text="${#locale}"></span></li>
</ul>

<h1>편의 객체</h1>
<ul>
    <li>Request Parameter = <span th:text="${param.paramData}"></span></li>
    <li>session = <span th:text="${session.sessionData}"></span></li>
</ul>

<!-- 실제 출력 화면 -->
request = org.apache.catalina.connector.RequestFacade@7e2b84d1
response = org.apache.catalina.connector.ResponseFacade@47e652a6
session = org.apache.catalina.session.StandardSessionFacade@4ef9b74b
servletContext = org.apache.catalina.core.ApplicationContextFacade@7546035e
locale = ko_KR

편의 객체
Request Parameter = hello
session = Hello Session
spring bean = Spring!
```

### 유틸리티 객체와 날짜
타임리프는 문자, 숫자, 날짜, URI 등을 편리하게 다루는 다양한 유틸리티 객체들을 제공합니다. 
+ #message : 메시지, 국제화 처리
+ #uris : URI 이스케이프 지원
+ #dates : java.util.Date 서식 지원 
+ #calendars : java.util.Calendar 서식 지원 
+ #temporals : 자바8 날짜 서식 지원
+ #numbers : 숫자 서식 지원
+ #strings : 문자 관련 편의 기능
+ #objects : 객체 관련 기능 제공
+ #bools : boolean 관련 기능 제공
+ #arrays : 배열 관련 기능 제공
+ #lists , #sets , #maps : 컬렉션 관련 기능 제공 
+ #ids : 아이디 처리 관련 기능 제공, 뒤에서 설명

이에 관한 사용 방법으로는 [공식문서](https://www.thymeleaf.org/doc/tutorials/3.0/usingthymeleaf.html#appendix-b-expression- utility-objects)를 참고바랍니다.  

문서에 java 8 시간에 대한 부분만 예시가 없어서 이 부분만 예시를 작성하겠습니다.  
model.addAttribute("localDateTime", LocalDateTime.now()); 이렇게 해주고 아래와 같이 사용합니다.  
```html
<ul>
    <li>default = <span th:text="${localDateTime}"></span></li>
    <li>yyyy-MM-dd HH:mm:ss = <span th:text="${#temporals.format(localDateTime, 'yyyy-MM-dd HH:mm:ss')}"></span></li>
</ul>

<h1>LocalDateTime - Utils</h1>
<ul>
    <li>${#temporals.day(localDateTime)} = <span th:text="${#temporals.day(localDateTime)}"></span></li>
    <li>${#temporals.month(localDateTime)} = <span th:text="${#temporals.month(localDateTime)}"></span></li>
    <li>${#temporals.monthName(localDateTime)} = <span th:text="${#temporals.monthName(localDateTime)}"></span></li>
    <li>${#temporals.monthNameShort(localDateTime)} = <span th:text="${#temporals.monthNameShort(localDateTime)}"></span></li>
    <li>${#temporals.year(localDateTime)} = <span th:text="${#temporals.year(localDateTime)}"></span></li>
    <li>${#temporals.dayOfWeek(localDateTime)} = <span th:text="${#temporals.dayOfWeek(localDateTime)}"></span></li>
    <li>${#temporals.dayOfWeekName(localDateTime)} = <span th:text="${#temporals.dayOfWeekName(localDateTime)}"></span></li>
    <li>${#temporals.dayOfWeekNameShort(localDateTime)} = <span th:text="${#temporals.dayOfWeekNameShort(localDateTime)}"></span></li>
    <li>${#temporals.hour(localDateTime)} = <span th:text="${#temporals.hour(localDateTime)}"></span></li>
    <li>${#temporals.minute(localDateTime)} = <span th:text="${#temporals.minute(localDateTime)}"></span></li>
    <li>${#temporals.second(localDateTime)} = <span th:text="${#temporals.second(localDateTime)}"></span></li>
    <li>${#temporals.nanosecond(localDateTime)} = <span th:text="${#temporals.nanosecond(localDateTime)}"></span></li>
</ul>

<!-- 실제 출력 화면 -->
LocalDateTime
+ default = 2021-12-03T15:18:21.249854
+ yyyy-MM-dd HH:mm:ss = 2021-12-03 15:18:21
LocalDateTime - Utils
+ ${#temporals.day(localDateTime)} = 3
+ ${#temporals.month(localDateTime)} = 12
+ ${#temporals.monthName(localDateTime)} = 12월
+ ${#temporals.monthNameShort(localDateTime)} = 12월
+ ${#temporals.year(localDateTime)} = 2021
+ ${#temporals.dayOfWeek(localDateTime)} = 5
+ ${#temporals.dayOfWeekName(localDateTime)} = 금요일
+ ${#temporals.dayOfWeekNameShort(localDateTime)} = 금
+ ${#temporals.hour(localDateTime)} = 15
+ ${#temporals.minute(localDateTime)} = 18
+ ${#temporals.second(localDateTime)} = 21
+ ${#temporals.nanosecond(localDateTime)} = 249854000
```


### 리터럴
리터럴은 소스 코드상에서 고정된 값을 말하는 용어입니다.  
+ 문자 리터럴 : 'hello'
+ 숫자 리터럴 : 10
+ 불린 리터럴 : true, false
+ null 리터럴 : null

타임리프에서 문자 리터럴 항상 ' 작은 따옴표로 감싸야 합니다.  
하지만 항상 작은 따옴표를 감싸는 것은 불편하므로 공백 없이 쭉 이어진다면 하나의 의미있는 토큰으로 인지해서 작은 따옴표를 생략할 수 있습니다.
```html
<!-- 원래는 작은 따옴표로 감싸야함-->
<span th:text="'hello'">

<!-- 붙어있으면 생략 가능-->
<span th:text="hello">

<!-- 붙어있지 않으므로 작은 따옴표 필수-->
<span th:text="'hello world'">

<!-- 덧셈 연산 가능-->
<span th:text="'hello' + ' world'">

<!-- 리터럴 대체 문법 ||-->
<span th:text="|hello ${data}|"></span>
```
리터럴 대체문법으로 || 안에 문자열을 작성하면 하나의 의미있는 토큰으로 인식하여 작은 따옴표를 사용하지 않아도 됩니다.


### URL 링크 @{...}
```java
model.addAttribute("param1", "data1");
model.addAttribute("param2", "data2");
```
모델에 위와 같이 넘겨주면 html은 아래 처럼 사용합니다.

```html
<ul>
    <li><a th:href="@{/hello}">basic url</a></li>
    <li><a th:href="@{/hello(param1=${param1}, param2=${param2})}">hello query param</a></li>
    <li><a th:href="@{/hello/{param1}/{param2}(param1=${param1}, param2=${param2})}">path variable</a></li>
    <li><a th:href="@{/hello/{param1}(param1=${param1}, param2=${param2})}">path variable + query parameter</a></li>
</ul>

<!--굳이 파람을 가져올 필요가 없다면 'test' 처럼 직접 넣어도 됩니다.-->
th:href="@{/basic/items/{itemId}(itemId=${item.id}, query='test')}"

<!-- 리터럴 대체 문법 응용 -->
th:href="@{|/basic/items/${item.id}|}"

<!-- 리터럴 사용하겠다고 먼저 || 를 사용하고 그 안에서 url 경로식 있으면 그 안에 다시 || 써줘야합니다. -->
th:onclick="|location.href='@{|/basic/items/${item.id}/edit|}'|"
```
첫 번째는 단순 /hello 가 url로 입력됩니다.  
두 번째는 쿼리파라미터로 ?param1=data&param2=data2가 들어갑니다.  
세 번째는 /hello/data1/data2 로 path variable로 들어갑니다.  
네 번째는 /hello/data1?param2=data2 로 들어갑니다.  
즉, ()안에 명시된 데이터들이 맵핑되는데 path variable에 일치하는게 있다면 path variable로 들어가고 매칭되는게 없다면 쿼리 파라미터로 들어갑니다.


### 연산

```html
<li>10 + 2 = <span th:text="10 + 2"></span></li>
<li>10 % 2 == 0 = <span th:text="10 % 2 == 0"></span></li>
<li>1 gt 10 = <span th:text="1 gt 10"></span></li>
<li>1 >= 10 = <span th:text="1 >= 10"></span></li>
<li>1 ge 10 = <span th:text="1 ge 10"></span></li>
<li>1 == 10 = <span th:text="1 == 10"></span></li>
<li>1 != 10 = <span th:text="1 != 10"></span></li>
<li>(10 % 2 == 0)? '짝수':'홀수' = <span th:text="(10 % 2 == 0)? '짝수':'홀수'"></span></li>
<li>${data}?: '데이터가 없습니다.' = <span th:text="${data}?: '데이터가 없습니다.'"></span></li>
<li>${nullData}?: '데이터가 없습니다.' = <span th:text="${nullData}?: '데이터가 없습니다.'"></span></li>
<li>${data}?: _ = <span th:text="${data}?: _">데이터가 없습니다.</span></li>
<li>${nullData}?: _ = <span th:text="${nullData}?: _">데이터가 없습니다.</span></li>
```
자바 연산과 별 다를게 없습니다.  
다른 점은 Elvic 연산자와 No-Operation 연산자(_) 입니다.  
Elvic는 조건식의 편의버전으로 맨 아래서 3번째 줄을 보면 data가 존재하면 data를 그대로 출력하고 없으면 데이터가 없습니다를 출력합니다.  
No-Operation은 _ 가 선택되면 마치 타임리프가 실행되지 않은 것처럼 기존의 HTML을 그대로 사용합니다. 맨 마지막줄을 보면 nullData의 데이터가 없다면 _가 선택되면서 기존 데이터인 '데이터가 없습니다' 가 출력됩니다.


### 속성 값 설정
타임리프는 주로 HTML 태그에 th:* 속성을 지정하는 방식으로 동작합니다.  
th:* 로 속성을 적용하면 기존 속성을 대체하고 기존 속성이 없다면 새로 해당 속성으로 만들어 줍니다.
```html
<!-- name 태그를 th:name의 태그로 대체해서 랜더링-->
<input type="text" name="mock" th:name="userA" />

<!-- class 태그 뒤쪽에 large를 붙여서 랜더링 -> text large -->
<input type="text" class="text" th:classappend="large" /><br/>

<!-- html은 check 속성이 있으면 true, false에 관계없이 그냥 체크박스에 체크를 해버림 -->
<input type="checkbox" name="active" checked="false" /><br/>

<!-- 그에 반해 타임리프는 값이 적용된다. -->
<input type="checkbox" name="active" th:checked="false" /><br/>
```

### 반복문 th:each
```html
<tr th:each="user : ${users}">
    <td th:text="${user.username}">username</td>
    <td th:text="${user.age}">0</td>
</tr>

<tr th:each="user, userStat : ${users}">
    <td th:text="${userStat.count}">username</td>
    <td th:text="${user.username}">username</td>
    <td th:text="${user.age}">0</td>
    <td>
      index = <span th:text="${userStat.index}"></span>
      count = <span th:text="${userStat.count}"></span>
      size = <span th:text="${userStat.size}"></span>
      even? = <span th:text="${userStat.even}"></span>
      odd? = <span th:text="${userStat.odd}"></span>
      first? = <span th:text="${userStat.first}"></span>
      last? = <span th:text="${userStat.last}"></span>
      current = <span th:text="${userStat.current}"></span>
    </td>
</tr>
```
model에 users로 유저 리스트를 넘기면 향상된 for문같이 사용할 수 있습니다.  
each 문에서 추가적인 변수를 주게 되면 반복 상태에 대한 값을 가져올 수 있습니다.  
하지만 이 변수는 생략이 가능합니다. 위에서 변수는 user로 받고 있는데 여기다가 Stat을 붙여서 userStat을 사용하면 위에 코드처럼 명시적으로 주지 않아도 사용 가능합니다.  
+ index : 0부터 시작하는 값
+ count : 1부터 시작하는 값
+ size : 전체 사이즈
+ even , odd : count를 기준으로 홀수, 짝수 여부( boolean )
+ first , last :처음, 마지막 여부( boolean )
+ current : 현재 객체

### 조건문
```html
 <tr th:each="user, userStat : ${users}">
    <td th:text="${userStat.count}">1</td>
    <td th:text="${user.username}">username</td>
    <td>
        <span th:text="${user.age}">0</span>
        <span th:text="'미성년자'" th:if="${user.age lt 20}"></span>
        <span th:text="'미성년자'" th:unless="${user.age ge 20}"></span>
    </td>
</tr>
<tr th:each="user, userStat : ${users}">
    <td th:text="${userStat.count}">1</td>
    <td th:text="${user.username}">username</td>
    <td th:switch="${user.age}">
        <span th:case="10">10살</span>
        <span th:case="20">20살</span>
        <span th:case="*">기타</span>
    </td>
</tr>
```
특별히 설명할 것도 없이 그냥 java if문, switch문이랑 똑같습니다.

### 주석
```html
html 표준 주석
<!-- -->

타임리프 파서 주석
<!--/* */-->
```
표준 주석은 타임리프가 랜더링하지 않고 그대로 남겨둡니다.  
타임리프 파서 주석은 랜더링 시 주석 부분을 제거합니다.


### 블록 th:block
block은 HTML 태그가 아닌 타임리프의 자체 태그입니다.  
each로 해결하기 어려운 경우에 보통 사용합니다.
```html
<th:block th:each="user : ${users}">
    <div>
        사용자 이름1 <span th:text="${user.username}"></span>
        사용자 나이1 <span th:text="${user.age}"></span>
    </div>
    <div>
        요약 <span th:text="${user.username} + ' / ' + ${user.age}"></span>
    </div>
</th:block>
```
div 태그 2개씩을 반복으로 돌리고 싶을다고 했을 때 위 처럼 사용합니다.  
div에 each를 넣으면 div 한개만 반복되므로 두개를 반복으로 돌리고 싶을 때 block으로 해결할 수 있습니다.


### 자바스크립트 인라인
```html
<!-- 자바스크립트 인라인 사용 전 -->
<script>

    var username = [[${user.username}]];    

</script>
```
th의 inline 속성을 사용하지 않으면 문자열 부분에서 에러가 납니다.  
문자열의 경우 "" 쌍따옴표가 필요한데 랜더링 후에는 쌍다옴표가 들어가지 않고 문자만 들어가기 때문입니다.  
이를 방지하기 위해 inline 속성이 사용됩니다.

<br>

```html
<!-- 자바스크립트 인라인 사용 후 -->
<script th:inline="javascript">
    var username = [[${user.username}]];
    var age = [[${user.age}]];

    //자바스크립트 내추럴 템플릿
    var username2 = /*[[${user.username}]]*/ "test username";

    //객체
    var user = [[${user}]];
</script>
```
inline을 사용하면 문자열의 경우 "" 쌍따옴표가 들어갑니다.  
그냥 HTML을 열었을 때는 varname2의 경우 코드 그대로 드러나는데, 랜더링 이후에는 test username이 사라지고 주석 안에 있는 것으로 랜더링 되어 처리됩니다.  
타임리프는 인라인 기능을 사용하면 객체를 JSON으로 자동으로 변환해줍니다.  


<br>

each문도 사용이 가능합니다.
```html
<script th:inline="javascript">
      [# th:each="user, stat : ${users}"]
      var user[[${stat.count}]] = [[${user}]];
      [/]
</script>

실제 동작결과
var user1 = {"username":"userA","age":10};
var user2 = {"username":"userB","age":20};
var user3 = {"username":"userC","age":30};
```



### 템플릿 조각
웹 페이지를 개발할 때는 공통 영역이 많이 있습니다.  
이런 공통 부분의 코드를 복사해서 사용한다면 변경시 여러 페이지를 다 수정해야하므로 비효율적입니다.  
타임리프는 이런 문제를 해결하기 위해 템플릿 조각과 레이아웃 기능을 지원합니다.  
```java
@GetMapping("/fragment")
public String template() {
    return "template/fragment/fragmentMain";
}
```
자바에서 위와 같이 호출을 합니다.
<br>

__footer.html__
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">

<body>

<footer th:fragment="copy">
    푸터 자리 입니다.
</footer>

<footer th:fragment="copyParam (param1, param2)">
    <p>파라미터 자리 입니다.</p>
    <p th:text="${param1}"></p>
    <p th:text="${param2}"></p>
</footer>

</body>

</html>
```
위 파일은 resources/templates/template/fragment/footer.html 파일입니다.  
이 파일을 조각이라고 지칭하고 fragmentMain에서는 이 조각을 가져다가 사용할 것입니다.  
<br>


__fragmentMain.html__
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>Title</title>
</head>
<body>
<h1>부분 포함</h1>
<h2>부분 포함 insert</h2>
<div th:insert="~{template/fragment/footer :: copy}"></div>

<h2>부분 포함 replace</h2>
<div th:replace="~{template/fragment/footer :: copy}"></div>

<h2>부분 포함 단순 표현식</h2>
<div th:replace="template/fragment/footer :: copy"></div>

<h1>파라미터 사용</h1>
<div th:replace="~{template/fragment/footer :: copyParam ('데이터1', '데이터2')}"></div>
</body>
</html>
```
fragmentMain.html은 위와 같이 구성되어 있습니다.  
여기서 봐야할 부분은 insert, replace속성입니다.  
~{}을 보면 어떤 경로에 있는 것을 불러오고 있는 것을 확인할 수 있습니다. 단순한 경로의 경우 부분포함 단순 표현식처럼  ~{}를 생략할 수 있습니다.  
위에서 작성한 footer.html파일의 경로를 불러왔고 :: 뒤에는 fragment="copy"인 copy를 지칭합니다.  
<br>

insert는 랜더링 시 다음과 같습니다.
```html
<h2>부분 포함 insert</h2> 
<div>
<footer>
푸터 자리 입니다.
</footer>
</div>
```
div 태그 안에 그대로 불러온 태그와 내용을 가져다 넣습니다.

<br>

replace는 랜더링 시 다음과 같습니다.
```html
<h2>부분 포함 replace</h2>
<footer>
푸터 자리 입니다. 
</footer>
```
div 태그를 불러온 태그로 대체합니다.
<Br>

파라미터를 넣을 수 있는데 파라미터를 넣으면 다음과 같이 파라미터 값이 활용되어 랜더링 됩니다.
```html
<h1>파라미터 사용</h1> 
<footer>
<p>파라미터 자리 입니다.</p> 
<p>데이터1</p> 
<p>데이터2</p>
</footer>
```

### 템플릿 레이아웃 1
위에서는 일부 코드 조각을 가지고 와서 사용했다면, 이번에는 개념을 더 확장해서 코드 조각을 레이아웃에 넘겨서 사용하는 방식입니다.  
head에 공통으로 사용하는 css, javascript 같은 정보들이 있는데 이를 한 곳에 모아두고, 공통으로 사용하지만 어떤 부분에서는 정보들을 추가해서 사용하고 싶을 때 이 방법을 사용합니다.
```java
@GetMapping("/layout")
public String layout() {
    return "template/layout/layoutMain";
}
```
<Br>

__base.html__  
```html
<html xmlns:th="http://www.thymeleaf.org">
<head th:fragment="common_header(title,links)">

    <title th:replace="${title}">레이아웃 타이틀</title>

    <!-- 공통 -->
    <link rel="stylesheet" type="text/css" media="all" th:href="@{/css/awesomeapp.css}">
    <link rel="shortcut icon" th:href="@{/images/favicon.ico}">
    <script type="text/javascript" th:src="@{/sh/scripts/codebase.js}"></script>

    <!-- 추가 -->
    <th:block th:replace="${links}" />

</head>
```
/resources/templates/template/layout/ 위치에 base.html 파일을 만들었습니다.  
이제 웹 페이지에서 만드는 대부분의 레이아웃은 이 base.html을 가지고 만든다고 가정하겠습니다.
<br>

__layoutMain.html__
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head th:replace="template/layout/base :: common_header(~{::title},~{::link})">
    <title>메인 타이틀</title>
    <link rel="stylesheet" th:href="@{/css/bootstrap.min.css}">
    <link rel="stylesheet" th:href="@{/themes/smoothness/jquery-ui.css}">
</head>
<body>
메인 컨텐츠
</body>
</html>
```
th:replace 속성을 이용해 base.html에 있는 th:fragment="common_header" 이 붙어있는 head 태그로 교체합니다.  
항상 같은 레이아웃을 사용하겠다고 base.html을 뽑아놨지만 모든 페이지가 같지는 않을 것입니다.  
replace태그 가장 오른쪽에 보면 title과 link를 넘기는 것을 확인할 수 있습니다.  
이는 replace 태그 아래있는 title 태그와 link 태그 2개를 인자로 넘기는 것과 같습니다. 태그 자체를 통채로 넘깁니다.  
그럼 base.html 쪽으로 돌아가서 title로 들어간 것은 ${title}로 들어가 해당 title 태그를 넘어온 title태그로 통채로 바꿉니다.  
인자로 들어간 link도 base.html 가장 하단에 있는 ${links}로 들어가 해당 태그를 넘어온 태그로 통채로 바꿔버립니다.  
결과적으로 랜더링 되면 아래와 같이 나타납니다.
```html
<!DOCTYPE html>
<html>
<head>
<title>메인 타이틀</title> 

<!-- 공통 -->
  <link rel="stylesheet" type="text/css" media="all" href="/css/awesomeapp.css">
  <link rel="shortcut icon" href="/images/favicon.ico">
  <script type="text/javascript" src="/sh/scripts/codebase.js"></script>

<!-- 추가 -->
<link rel="stylesheet" href="/css/bootstrap.min.css">
<link rel="stylesheet" href="/themes/smoothness/jquery-ui.css">
</head> 
<body> 
메인 컨텐츠 
</body> 
</html>
```

정리하자면, common_header(~{::title},~{::link}) 이 부분이 핵심입니다.  
+ ::title 은 현재 페이지의 title 태그들을 전달합니다.
+ ::link 는 현재 페이지의 link 태그들을 전달합니다.


### 템플릿 레이아웃 2
앞선 레이아웃 1에서는 헤더 정도만 바꾸는 정도로 적용했습니다.  
하지만 이를 html 전체에 적용할 수도 있습니다.  
예를 들어, 페이지가 100개가 있는데 모든 레이아웃이 전부 똑같고 메인 컨텐츠 내용만 다르다면 이 방식을 사용할 수 있습니다.
<br>

__layoutFile.html__  
```html
<!DOCTYPE html>
<html th:fragment="layout (title, content)" xmlns:th="http://www.thymeleaf.org">
<head>
    <title th:replace="${title}">레이아웃 타이틀</title>
</head>
<body>
<h1>레이아웃 H1</h1>
<div th:replace="${content}">
    <p>레이아웃 컨텐츠</p>
</div>
<footer>
    레이아웃 푸터
</footer>
</body>
</html>
```
<Br>

__layoutExtendMain.html__
```html
<!DOCTYPE html>
<html th:replace="~{template/layoutExtend/layoutFile :: layout(~{::title}, ~{::section})}"
      xmlns:th="http://www.thymeleaf.org">
<head>
    <title>메인 페이지 타이틀</title>
</head>
<body>
<section>
    <p>메인 페이지 컨텐츠</p>
    <div>메인 페이지 포함 내용</div>
</section>
</body>
</html>
```
replace 속성이 html에 붙어있어 html 전체를 layoutFile로 교체하게 되는데 이때, title태그와 section 태그를 넘기고 있습니다.  
layoutFile.html 파일을 보면 레이아웃1과 마찬가지로 ${title}에 담겨 해당 태그 자체를 교체하고 넘긴 section은 content에 담겨서 해당 태그 자체를 교체시켜버립니다.  
즉, 아래와 같이 랜더링됩니다.  
```html
<!DOCTYPE html>
<html>
<head>
    <title>메인 페이지 타이틀</title>
</head>
<body>
<h1>레이아웃 H1</h1>
<section>
    <p>메인 페이지 컨텐츠</p>
    <div>메인 페이지 포함 내용</div>
</section>
<footer>
    레이아웃 푸터
</footer>
</body>
</html>
```

<br>

## 4. 스프링 통합
---
### 입력 폼 처리
컨트롤러에서 model에 아이템을 넣어서 뷰로 보낸다고 가정했을 때, HTML에 작성되어 있는 코드를 보면 id, name, value 값을 명시해줘야 했습니다.
```html
<form action="item.html" th:action method="post">
        <div>
            <label for="itemName">상품명</label>
            <input type="text" id="itemName" name="itemName" class="form-control" placeholder="이름을 입력하세요">
        </div>        

        ... 생략

    </form>
```
<br>

하지만 th:object와 th:field 기능을 사용하면 id, name, value값을 한 번에 처리할 수 있습니다.
```html
<form action="item.html" th:action th:object="${item}" method="post">
        <div>
            <label for="itemName">상품명</label>
            <input type="text" id="itemName" th:field="*{itemName}" class="form-control" placeholder="이름을 입력하세요"> 
        </div>
        <div>
            <label for="price">가격</label>
            <input type="text" id="price" th:field="*{price}" class="form-control" placeholder="가격을 입력하세요">
        </div> 
        <div>
            <label for="quantity">수량</label>
            <input type="text" id="quantity" th:field="*{quantity}" class="form-control" placeholder="수량을 입력하세요"> 
        </div>
```
th:object에는 form에서 사용할 객체를 지정합니다. Model로 넘어온 것을 지정해주면 됩니다.  
*{itemName} 는 ${item.itemName}와 같은 의미인데, 앞서 object로 item임을 명시해줬기 때문에 *를 사용하면 item을 생략할 수 있습니다.  
결과적으로 id, name, value값을 생략하고 th:field로만 처리해도 됩니다.  
itemName으로 예시를 들면, id="itemName" name="itemName" value="모델에 담긴 itemName값"이 자동으로 들어갑니다.  


### 단일 체크 박스
```html
<div class="form-check">
    <input type="checkbox" id="open" name="open" class="form-check-input"> 
    <label for="open" class="form-check-label">판매 오픈</label>
</div>
```
기본적으로 체크 박스는 체크가 되어 있으면 위의 경우에는 open=on으로 on이라는 값이 넘어옵니다.  
스프링은 on이라는 문자열을 boolean이 받게 되면 자동으로 true 타입으로 변환해줍니다.  
하지만 체크 박스가 체크되지 않은 경우에는 클라이언트는 값 자체를 보내지 않기 때문에 null값이 들어가게 됩니다.  
이에 대해서는 로직에 따라 문제가 발생할 수 있습니다.  
<br>

```html
<div class="form-check">
    <input type="checkbox" id="open" name="open" class="form-check-input"> 
    <input type="hidden" name="_open" value="on"/> <!-- 히든 필드 추가 -->
    <label for="open" class="form-check-label">판매 오픈</label>    
</div>
```
스프링 MVC는 이를 방지하기 위해 약간의 트릭을 제공하는데 기존 체크 박스 name에 _언더바를 붙여서 히든필드로 보내게 되면 값이 들어오지 않았을 경우, false로 매핑해줍니다.  
둘다 값이 들어오면 히든필드는 버립니다.  
하지만 이렇게 일일이 코드를 작성하는 것은 매우 번거롭습니다.  
<br>

```html
<div class="form-check">
    <input type="checkbox" id="open" th:field="*{open}" class="form-check-input">
    <label for="open" class="form-check-label">판매 오픈</label>    
</div>
```
앞에서 object로 명시해줬다고 가정하고 작성한 코드입니다.  
체크 박스를 th:field로 작성할 경우, id, name, value 는 기본으로 들어가고 히든필드까지 자동으로 넣어줍니다.  
추가적으로 체크 박스는 checked 속성이 있으면 체크 표시가 된 상태로 화면에 표기가 되는데, th:field에서 open 값이 true면 chekced 속성도 자동으로 만들어서 넣어줍니다.



### 멀티 체크 박스
```html
<div>
    <div>등록 지역</div>
    <div th:each="region : ${regions}" class="form-check form-check-inline">
        <input type="checkbox" th:field="*{regions}" th:value="${region.key}" class="form-check-input">
        <label th:for="${#ids.prev('regions')}"
                th:text="${region.value}" class="form-check-label">서울</label>
    </div>
</div>

<!-- 실제 랜더링 결과 -->
<div class="form-check form-check-inline">
    <input type="checkbox" value="SEOUL" class="form-check-input" id="regions1" name="regions">
    <input type="hidden" name="_regions" value="on"/>
    <label for="regions1" class="form-check-label">서울</label>
</div>

.. 여러개 나오지만 생략
```
model에 regions 키값으로 linkedHashMap으로 넘겼고, 위쪽 form에서 object로 item을 잡아줬다고 가정하고 진행합니다.  
여러 개의 체크 박스를 만들 것이므로 th:each로 regions를 for문으로 돌려줍니다.  
사용하고 있는 $는 현재 for문에서 가져온 region이고 *는 앞서 object로 명시한 item의 regions입니다.  
each문을 사용하면 id, name, value값이 기본적으로 생성된다고 했으나, th:value로 값이 명시되어 있으니 명시한 값으로 들어갑니다.  
하지만 id값의 경우 each문으로 인해 생성되는 체크 박스마다 다른 id값이 들어가야 합니다.  
이에 대해서 타임피르는 각각마다 번호를 붙여서 regions1, regions2 이렇게 자동으로 붙여줍니다.  
보통 체크 박스를 보면 체그 박스를 누르지 않고, 해당 체크 박스의 텍스트를 눌러도 체크 표시가 되는데 이를 위해서는 텍스트가 체크 박스의 id값을 알아야 합니다.  
이는 #ids.prev를 사용해서 바로 앞의 th가 생성해준 id를 가져올 수 있습니다.  



### 라디오 버튼
멀티 체크 박스는 여러 개가 선택이 가능했지만 라디오 버튼의 경우 딱 한개만 선택 가능합니다.  
```html
<div th:each="type : ${itemTypes}" class="form-check form-check-inline">
    <input type="radio" th:field="*{itemType}" th:value="${type.name()}" class="form-check-input">
    <label th:for="${#ids.prev('itemType')}" th:text="${type.description}" class="form-check-label"> Book </label>
</div>
```
위에서 설명한 체크 박스랑 다를게 없습니다.  
다른 점이 있다면 라디오 버튼의 고유한 특성인데, 라디오 버튼은 한번 선택되면 다른 것으로 변경은 가능하나 아애 선택을 안하는 것을 불가능 하기 때문에 히든 필드는 생성되지 않습니다.  

### 셀렉트 박스
```html
<div>배송 방식</div>
<select th:field="*{deliveryCode}" class="form-select">
    <option value="">==배송 방식 선택==</option>
    <option th:each="deliveryCode : ${deliveryCodes}" th:value="${deliveryCode.code}"
            th:text="${deliveryCode.displayName}">FAST</option>
</select>
```
셀렉트 박스는 박스를 누르면 아래로 쭉 길게 나와서 선택하는 박스입니다.  
다른 방식과 크게 차이가 없습니다.  

### BindingResult 사용하기
타임리프는 BindingResult를 사용해서 검증 오류를 표현할 수 있는 기능을 제공합니다.  
```html
<form action="item.html" th:action th:object="${item}" method="post">

    <div th:if="${#fields.hasGlobalErrors()}">
        <p class="field-error" th:each="err : ${#fields.globalErrors()}" th:text="${err}">글로벌 오류 메시지</p>
    </div>

    <div>
        <label for="itemName" th:text="#{label.item.itemName}">상품명</label>
        <input type="text" id="itemName" th:field="*{itemName}"
                th:errorclass="field-error" class="form-control" placeholder="이름을 입력하세요">
        <div class="field-error" th:errors="*{itemName}">
            상품명 오류
        </div>
    </div>
     <div>
        <label for="price" th:text="#{label.item.price}">가격</label>
        <input type="text" id="price" th:field="*{price}"
                th:errorclass="field-error" class="form-control" placeholder="가격을 입력하세요">
        <div class="field-error" th:errors="*{price}">
            가격 오류
        </div>
    </div>

    ... 생략
```

+ #fields 
    - #fields 로 BindingResult 가 제공하는 검증 오류에 접근할 수 있습니다.
    - globalError은 ObjectError을 의미합니다.
+ th:errors     
    - 마치 if문처럼 BindingResult에 th:errors 값으로 된 필드 오류가 있으면 해당 태그를 출력합니다.
+ th:errorclass 
    - 마치 if문처럼 BindingResult에 th:field 값으로 된 필드 오류가 있으면 class 정보를 추가해줍니다.

### 컨버터 적용하기
```html
<li>${number}: <span th:text="${number}" ></span></li>
<li>${{number}}: <span th:text="${{number}}" ></span></li>
<li>${ipPort}: <span th:text="${ipPort}" ></span></li>
<li>${{ipPort}}: <span th:text="${{ipPort}}" ></span></li>
th:field <input type="text" th:field="*{ipPort}"><br/>
```
+ ${...} : 변수 표현식
+ ${\{...}} 컨버전 서비스 적용
    - 자동으로 컨버전 서비스를 사용해 변환된 결과를 출력해준다.  
    - ipPort가 클래스이고 ipPort를 String으로 변환하는 컨버터가 등록되어 있고 model에 ipPort 객체가 담겨 넘어온다고 가정하면, 변수 표현식에서는 toString으로 값이 찍힐 것이고 컨버전 서비스가 적용된 곳에는 컨버터가 동작해 String으로 값이 찍힙니다.
+ th:field
    - id, name을 출력하는 등 다양한 기능을 제공하는데 여기에 컨버전 서비스도 함께 적용된다.





<Br><Br>

__참고__  
<a href="https://www.inflearn.com/course/%EC%8A%A4%ED%94%84%EB%A7%81-mvc-2" target="_blank"> 스프링 MVC 2편</a>   



