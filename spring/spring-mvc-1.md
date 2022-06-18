# Spring MVC - 기능 정리

## MVC패턴
---
![그림0](https://github.com/backtony/blog-code/blob/master/spring/img/mvc/1/1-0.PNG?raw=true)  
하나의 서블릿이나 JSP만으로 비즈니스 로직과 뷰 렌더링까지 모두 처리하게 되면 너무 많은 역할을 하게 되고, 결과적으로 유지보수가 어려워진다.  
Model View Controller(MVC) 패턴은 과거에 서블릿이나 JSP만으로 처리하던 것을 컨트롤러, 뷰로 영역을 나눈 것을 의미한다.  
+ 컨트롤러
  - HTTP 요청을 받아서 파라미터를 검증하고, 비즈니스 로직을 수행한다.
  - 뷰에 전달할 결과 데이터를 조회해서 모델에 담는다.
+ 모델
  - 뷰에 출력할 데이터를 담아둔다.
  - 뷰가 필요한 데이터를 모두 모델에 담아서 전달해주는 덕분에 뷰는 비즈니스 로직이나 데이터 접근을 몰라도 되고, 화면을 렌더링 하는 일에만 집중할 수 있다.
+ 뷰
  - 모델에 담겨있는 데이터를 사용해서 화면을 그리는 역할만 한다.
  - HTML을 생성하는 부분을 말한다.

<br>


## 1. 요청 매핑
---
### RequestMapping
```java
@RestController
@SLF4J
public class MappingController { 
 @RequestMapping("/hello-basic")
 public String helloBasic() {
 log.info("helloBasic");
 return "ok";
 }
}
```
URL로 호출이 오면 해당 메서드가 실행된다. @RequestMapping 애노테이션의 경우, 모든 HTTP 메서드를 허용한다. 이런 메서드를 다음과 같이 설정해줄 수가 있다.
```java
@RequestMapping(value = "/hello-basic", method = method=RequestMethod.GET)
```
옵션을 2개 이상 넣어주게 되면 url 앞에 value를 붙여야한다. 따라서 너무 길어지게 되니 이것을 하나의 애노테이션으로 제공해준다.
+ @GetMapping
+ @PostMapping
+ @PutMapping
+ @DeleteMapping
+ @PatchMapping

대부분의 속성을 배열로 제공하므로 다중 설정이 가능하다. URL을 {}를 이용해 여러 개를 받을 수도 있다.
```java
@RequestMapping({"/hello-basic","/hello-go"})
```
<br>

### PathVariable
```java
// 단일 사용
@GetMapping("/mapping/{userId}")
@SLF4J
public String mappingPath(@PathVariable("userId") String data) {
 log.info("mappingPath userId={}", data);
 return "ok";
}


// 다중 사용 + 변수명 일치로 파라미터 생략
@GetMapping("/mapping/users/{userId}/orders/{orderId}")
@SLF4J
public String mappingPath(@PathVariable String userId, @PathVariable Long
orderId) {
 log.info("mappingPath userId={}, orderId={}", userId, orderId);
 return "ok";
}
```
/mapping/userA 로 들어오면 userId키에 userA가 value로 들어가게 된다. 그리고 그 value값은 data에도 들어간다. 만약 변수명이 키이름과 같다면 @PathVariable의 파라미터를 생략해도 된다.
<br>

### 특정 조건 매핑
거의 사용하지 않는다.

#### 파라미터 조건 매핑
```java
/**
     * 파라미터로 추가 매핑
     * params="mode",
     * params="!mode"
     * params="mode=debug"
     * params="mode!=debug" (! = )
     * params = {"mode=debug","data=good"}
     */
@GetMapping(value = "/mapping-param", params = "mode=debug")
```
url에서 파라미터로 mode=debug로만 들어와 호출된다. /mapping-param?mode=debug로 들어와야만 한다는 것이다.

<br>

#### 헤더 조건 매핑
```java
/**
 * 특정 헤더로 추가 매핑
 * headers="mode",
 * headers="!mode"
 * headers="mode=debug"
 * headers="mode!=debug" (! = )
 */
@GetMapping(value = "/mapping-header", headers = "mode=debug")
```
헤더에 조건에 맞는 값이 있어야 가능하게 한다.
<br>

#### 미디어 타입 조건 매핑 - Content-Type
```java
/**
 * Content-Type 헤더 기반 추가 매핑 Media Type
 * consumes="application/json"
 * consumes="!application/json"
 * consumes="application/*"
 * consumes="*\/*"
 * MediaType.APPLICATION_JSON_VALUE
 */
@PostMapping(value = "/mapping-consume", consumes = "application/json")
```
Content-Type 헤더의 값이 조건과 맞을때만 가능하게 한다.
<br>

#### 미디어 타입 조건 매핑 - Accept
```java
/**
 * Accept 헤더 기반 Media Type
 * produces = "text/html"
 * produces = "!text/html"
 * produces = "text/*"
 * produces = "*\/*"
 */
@PostMapping(value = "/mapping-produce", produces = "text/html")
```
Accept는 HTTP요청에서 응답을 받을 때 받을 수 있는 형태이다. 요청할 때 Accept가 특정조건일 경우에만 가능하게 한다.  
<br>

미디어 타입 조건 매핑에서 consumes와 produces로 미디어 타입을 값으로 줘야한다. 위에서는 직접 넣어줬지만 스프링에서 정해놓은게 있다.
```java
produces = MediaType.TEXT_HTML_VALUE
consumes = MediaType.APPLICATION_JSON_VALUE
```
따라서 이 코드처럼 정해놓은 식으로 사용하는게 좋다. MediaType은 여러 개가 있으니 스프링으로 골라서 써야한다.
<br>

__cf) 공통되는 URL__  
컨트롤러 애노테이션이 붙은 클래스의 메서드들에서 공통되는 URL이 있다면 클래스에 @RequestMapping으로 공통되는 URL을 뽑아서 사용할 수 있다.
```java
@RestController
@RequestMapping("/hello")
public class MappingController { 
 @RequestMapping("/basic") // hello/basic 으로 연결된다.
 public String helloBasic() { 
 return "ok";
 }
}
```
<Br>

## 2. HTTP 요청 - 기본, 헤더 조회
---
애노테이션 기반의 스프링 컨트롤러는 다양한 파라미터를 지원한다.
```java
@RequestMapping("/headers")
public String headers(HttpServletRequest request,
                      HttpServletResponse response,
                      HttpMethod httpMethod,
                      Locale locale, // 가장 우선순위가 높은 것이 지정됨
                      // 헤더 전부 받기
                      // MultiValueMap은 하나의 키에 여러 값을 받을 수 있다
                      // 꺼내면 list로 나온다
                      @RequestHeader MultiValueMap<String, String> headerMap,
                      // 헤더 하나만 받기
                      @RequestHeader("host") String host,
                      // value는 쿠키 이름, requeired 기본값은 true, false는 없어도 된다은거
                      @CookieValue(value="myCookie",required = false) String cookie
                      ){

}
```
+ HttpMethod : HTTP 메서드 조회
+ Locale : 언어 정보 조회, 가장 우선순위 높은 것으로 받아진다.
+ @RequestHeader : 헤더 정보 받기, MultiValueMap을 사용하면 헤더의 모든 정보를 받을 수 있다. MultiValueMape은 하나의 키 값에 여러 값을 받을 수 있으며 꺼낼 때는 List로 꺼낸다. 파라미터로 헤더의 특정해주면 해당 헤더의 값만 받을 수 있다.
+ @CookieValue : 쿠키 정보 조회, value는 쿠키 이름, required는 없어도 되냐 안되냐로 기본값은 true로 false를 주면 없어도 된다는 뜻

<br>

## 3. HTTP 요청 파라미터 - @RequestParam
---
파라미터 이름으로 바인딩 한다.
```java
@ResponseBody
@SLF4J
@RequestMapping("/request-param-v2")
public String requestParamV2(
 @RequestParam("username") String memberName,
 @RequestParam("age") int memberAge) {
 log.info("username={}, age={}", memberName, memberAge);
 return "ok";
}
```
?username=hello&age=20 으로 들어오면 @RequestParam의 파라미터이름에 맞게 세팅된다. 즉, username키의 value에는 hello가 들어가고 memberName에도 hello가 들어간다.  
<br>

### 생략
만약 파라미터 이름이 변수 이름과 같다면 파라미터를 생략해도 된다.
```java
// 생략 전
@RequestParam("username") String username;

// 생략후
@RequestParam String username;
```
<br>

실제 들어오는 value값이 String, int, Integer 등의 단순 타입이면 @RequestParam 도 생략 가능하다. 하지만 웬만하면 @RequestParam 은 남겨서 명시적으로 개발자에게 알려주도록 하자.
```java
// url로 넘겨주는 값의 타입이 단순 타입이고 파라미터 이름과 변수 이름이 같은 경우
// 전부 생략
public String requestParamV2(String username);
```

<br>

### 필수여부와 기본값
```java
@RequestMapping("/request-param-default")
public String requestParamDefault(
 @RequestParam(required = true, defaultValue = "guest") String username,
 @RequestParam(required = false, defaultValue = "-1") int age) {
 log.info("username={}, age={}", username, age);
 return "ok";
}
```
+ required : 필수여부를 결정한다. 기본값은 true로 항상 값이 넘어와야 한다. false로 두면 값이 없어도 된다.
  - true여도 만약 파라미터에 이름만 있고 값을 빈칸으로 넣어주면 통과된다. ?username= 이렇게 두면 통과된다. 따라서 주의해야 한다.
  - false에서 값을 주지 않았을 때, 만약 변수의 타입이 기본형(primivitve)라면 500예외가 발생한다. 기본형에는 null이 들어가지 못하기 때문이다.
+ defaultValue : 값이 빈문자 "" 또는 값이 들어오지 않았을 경우 defaultValue로 값을 채워준다. defaultValue를 사용하면 required는 사실상 의미가 없다. 빈문자가 들어왔을 때도 defaultValue 값으로 처리한다.

### 파라미터 한번에 받기
```java
@RequestMapping("/request-param-map")
public String requestParamMap(@RequestParam Map<String, Object> paramMap) {}
```
변수의 타입을 Map, MultiValueMap 으로 한다면 한 번에 전부를 받을 수 있다. 파라미터의 값이 1개가 확실하면 Map을, 확실하지 않다면 MultiValueMap을 사용한다. 하지만 파라미터의 값은 거의 한 개만 쓴다. 애매하게 2개를 넣진 않는다.  

<br>

## 4. HTTP 요청 파라미터 - @ModelAttribute
---
실제 개발을 하면 요청 파라미터를 받아서 필요한 객체를 만들고 그 객체에 값을 넣어주거나 뷰에 넘겨줘야 한다.
```java
// 요청 파라미터를 받을 객체
@Data
public class HelloData {
    private String username;
    private int age;
}

@RequestMapping("/model-attribute-v2")
public String modelAttributeV1(@ModelAttribute HelloData helloData) {
 return "ok";
}
```
?username=hello&age=20 을 받으면 자동으로 helloData 객체가 생성되어 요청 파라미터의 값이 모두 들어가 있다. 스프링 MVC는 @ModelAttribute가 있으면 다음과 같이 동작한다.
1. 해당 객체 생성
2. 요청 파라미터의 이름으로 객체의 프로퍼티 찾아 프로퍼티의 setter을 호출해서 값 세팅
  + 만약 타입이 다른 값들이 들어오면 BindException 을 발생시킨다.
3. model.addAttribute() 코드를 자동으로 처리해준다.

3번에 대해 좀 더 자세히 알아보자.
```java
public String modelAttributeV1(@ModelAttribute("key") HelloData helloData) {
  // @ModelAttribute 에 파라미터를 주면 
  // 아래와 같은 코드가 자동으로 만들어진다.
  model.addAttribute("key",helloData);
 return "ok";
}

// 구분해주기 위해 클래스 명을 바꿈
public String modelAttributeV1(@ModelAttribute Item helloData) {
  // 파라미터를 주지 않으면 변수 타입의 첫글자를 소문자로한
  // 문자가 key 값이 된다.
  model.addAttribute("item",helloData);
 return "ok";
}

// 극한의 생략
// String, int, Integer 같은 단순 타입이면 @RequestParam이 적용
// 단순 타입이 아니면 ModelAttribute 애노테이션으로 적용된다.
// @ModelAttribute 생략 + 
// 파라미터 생략시 변수 타입 첫글자 소문자를 키값으로 적용
public String modelAttributeV1(HelloData helloData) {
  // 아래 코드가 생략되어있음
  //model.addAttribute("helloData",helloData);
 return "ok";
}
```
<Br>

해당 컨트롤러에 있는 모든 요청에 대해 Model에 같은 값을 넣어주는 방법도 있다.  
컨트롤러 클래스 안에 메서드를 하나 만들고 그 위에 @ModelAttribute 애노테이션을 붙이면 된다.  
이 방법은 보통 타임 리프에서 체크 박스를 만들 때 사용한다.  
```java
@ModelAttribute("regions")
public Map<String, String> regions() {
  Map<String, String> regions = new LinkedHashMap<>(); 
  regions.put("SEOUL", "서울");
  regions.put("BUSAN", "부산");
  regions.put("JEJU", "제주");
  return regions;
}
```
보통 위와 같은 경우 고정된 값을 계속 넣게 되므로 static으로 하나 만들어서 공유하여 사용하는 것이 좋다.
<br>


__cf) 프로퍼티__  
객체에 getUsername(), setUsername 메서드가 있으면 이 객체는 username이라는 프로퍼티를 가지고 있다고 표현한다. 
<br>

__cf) @Data__  
@Data는 lombok에서 제공하는 애노테이션이다. @Getter, @Setter, @ToString, @EqualsAndHashCode, @RequiredArgsConstructor 을 자동으로 적용해준다.  
@EqualsAndHashCode는 해당 클래스에 equals와 hasCode 메서드를 만들어주어 equals 비교를 가능하게 해준다.(내부값 비교)



<br>

### 생략
```java
public String modelAttributeV2(HelloData helloData) {}
```
생략이 가능하나 @RequestParam도 생략할 수 있어 혼란이 발생할 수 있다. 스프링은 생략시 다음과 같이 동작한다.
+ String, int, Integer 같은 단순 타입 -> @RequestParam 사용
+ 나머지의 경우는 @ModelAttribute 사용 (argument resolver로 지정해둔 타입 외)

<br>

## 5. 요청 메시지 - 단순 텍스트
---
GET 방식의 url 쿼리 파라미터나 HTML Form POST의 경우 @RequestParam, @ModelAttribute를 사용했지만 이 경우를 제외한 다른 모든 경우, 즉 HTTP 메시지 바디를 통해 데이터가 직접 넘어오는 경우(HTTP API)는 위의 애노테이션들을 사용할 수 없다. HTTP 메시지 바디를 가져와서 처리해야 한다.  
__HTML Form의 경우 Post로 body에 데이터가 담겨서 오는데 이는 예외적으로 @RequestParam으로 데이터를 받을 수 있다.__
```java
@PostMapping("/request-body-string-v3")
public HttpEntity<String> requestBodyStringV3(HttpEntity<String> httpEntity) {
 String messageBody = httpEntity.getBody(); // http body에 있는 내용을 타입에 맞게 변환하여 꺼냄
 return new HttpEntity<>("ok"); // 첫 파라미터는 메시지 바디이다. 메시지 바디 정보를 직접 반환
}
```
기존에 inputStream으로 받아서 copyToString으로 변환해줬던 작업을 HttpEntity로 편리하게 조회할 수 있다.
+ __HttpEntity : Http header, body 정보를 편리하게 조회, 응답에서도 사용__
  - 메시지 바디 정보를 직접 조회
  - 메시지 바디 정보를 직접 반환, 헤더 정보 포함 가능, view 조회 X

### RequestEntity, ResponseEntity
__HttpEntity를 상속받아 더 많은 기능을 제공한다.__ 
+ RequestEntity : 요청에서 사용, HttpMethod, url 정보 추가
+ ResponseEntity : 응답에서 사용, HTTP 상태 코드 설정 가능

```java
@PostMapping("/request-body-string-v3")
public HttpEntity<String> requestBodyStringV3(RequestEntity<String> httpEntity) {
 String messageBody = httpEntity.getBody(); 
 return new ResponseEntity<String>("ok",HttpStatus.CREATED); 
}
```

### @RequestBody @ResponseBody
@RequestBody 를 사용하면 위의 내용들을 다 생략할수 있다. 사실상 이 방법을 많이 사용한다고 보면 된다. 하지만 상태코드를 애노테이션으로 지정해야 하기 때문에 동적으로 상태를 바꿀 수 없다. 따라서 동적으로 상태코드를 바꿔야한다면 Entity 방법을 사용해야 한다.
```java
@ResponseStatus(HttpStatus.OK)
@ResponseBody
@PostMapping("/request-body-string-v4")
// HTTP 메시지 바디를 읽어서 바로 변수에 넣어준다.
public String requestBodyStringV4(@RequestBody String messageBody) {
 return "ok";
}
```
@ResponseBody와 @RequestBody는 하나의 짝이라고 볼 수 있다. 
+ @ResponseBody : return 을 응답 HTTP 메시지 바디에 넣어준다.
+ @RequestBody : HTTP 메시지 바디를 변수에 넣어준다.
+ @ResponseStatus : 응답의 상태를 표시해준다.

@RequestBody를 사용한 경우, 헤더 정보가 필요하다면 추가적으로 HttpEntity를 사용하거나 @RequestHeader 을 사용하면 된다.


__cf) @RestController__  
@Controller는 반환값이 String이면 뷰 이름으로 인식하고 뷰를 찾아 랜더링한다. @Restcontroller는 반환값으로 뷰를 찾는게 아니라 리턴 값을 http 메시지 바디에 바로 입력한다. @ResponseBody 와 똑같은 기능을 하는데 붙이는 위치가 다르다. @ResponseBody는 보통 메서드에 붙인다. 하지만 모든 메서드에 붙이기에는 번거롭다. 그래서 클래스에 @ResponseBody를 붙이기도 하는데 이때 @Controller와 @ResponseBody 애노테이션을 합친 것이 @RestController 이다. 
<br>

### 요청 파라미터 vs HTTP 메시지 바디
+ 요청 파라미터를 조회하는 기능 : @RequestParam, @ModelAttribute
  - GET 방식의 쿼리 파라미터, POST 방식의 HTML 폼 데이터 전송 방식
+ HTTP 메시지 바디를 직접 조회하는 기능 : @RequestBody

<br>

## 6. 요청 메시지 - JSON
---
요즈음에는 HTTP API로 요청을 하면 사실상 위에서 언급했던 단순 텍스트가 아니라 거의 전부 JSON 형식이다. 단순 텍스트가 아닌 JSON으로 요청을 보낼 때 처리하는 방법을 알아보자.  

```java
// 방법 1
@ResponseBody
@PostMapping("/request-body-json-v5")
public HelloData requestBodyJsonV5(@RequestBody HelloData data) {
 log.info("username={}, age={}", data.getUsername(), data.getAge());
 return data;
}
```
방식은 단순 텍스트로 받을 때랑 똑같다. @RequestBody 로 받으면 객체 생성해서 setter을 찾아 세팅해준다. 위처럼 반환은 data로 보내면 data를 HTTP 메시지 컨버터가 JSON 응답으로 바꿔서 응답한다.  
여기서 주의할 점이 @RequestBody는 생략하게 되면 @ModelAttribute가 생략된 것으로 인지하고 @ModelAttribute의 동작을 수행한다.  
즉, 바디부가 아니라 파라미터부를 확인해서 세팅하기 때문에 @RequestBody는 생략하면 안된다.  

```java
// 방법 2
@ResponseBody
@PostMapping("/request-body-json-v4")
public String requestBodyJsonV4(HttpEntity<HelloData> httpEntity) {
 HelloData data = httpEntity.getBody();
 log.info("username={}, age={}", data.getUsername(), data.getAge());
 return "ok";
}
```
방법 1이 제일 편한 방법이지만 방법2를 사용해도 된다. 제네릭 타입이 HelloData 이므로 getBody 메서드를 사용해 꺼내면 요청으로 받은 JSON 데이터가 세팅된 HelloData가 꺼내진다.  

<br>

## 7. 응답 - 정적 리소스, 뷰 템플릿
---
### 정적 리소스
src/main/resources 는 리소스를 보관하는 곳이고 클래스패스의 시작 경로이다. 정적 리소스의 경로는 아래와 같다.
```
src/main/resource/static
``` 
해당 경로에 hello.html 파일을 넣어두면 localhost:8080/hello.html 입력시 해당 파일을 변경 없이 그대로 서비스한다.
<br>

### 뷰 템플릿
뷰 템플릿을 거쳐서 HTML이 생성되고, 뷰가 응답을 만들어서 전달한다. 일반적으로 HTML을 동적으로 생성하는 용도로 사용한다. 뷰 템플릿의 경로는 아래와 같다.
```
src/main/resources/templates
```
해당 경로에 만약 hello.html이라는 파일을 만들어 놓았다고 가정해보자.
```java
@RequestMapping("/response-view-v2")
 public String responseViewV2(Model model) {
 model.addAttribute("data", "hello!!");
 return "hello";
 }
```
위 코드에서 return hello는 뷰 템플릿 경로에 있는 hello.html을 찾아서 model에 담긴 정보로 랜더링해서 서비스한다.  
hello.html을 만들 때 Thymeleaf를 사용했다고 해보자. Thymeleaf 라이브러리를 추가했다면 스프링 부트가 자동으로 ThymeleafViewResolver와 필요한 스프링 빈들을 등록하고 다음과 같은 설정도 한다.
```java
// application.properties
spring.thymeleaf.prefix=classpath:/templates/
spring.thymeleaf.suffix=.html
```
이 설정이 기본값인데 딱히 수정할 일은 없다.  
<br>

## 8. HTTP 메시지 컨버터
---
스프링 부트의 기본 메시지 컨버터의 우선순위는 다음과 같다.
```
0 = ByteArrayHttpMessageConverter
1 = StringHttpMessageConverter
2 = MappingJackson2HttpMessageConverter
```
+ ByteArrayHttpMessageConverter : byte[] 데이터를 처리
  - 변수의 클래스 타입 : byte
  - 미디어 타입(컨텐트 타입) : * / *
+ StringHttpMessageConverter : String 문자로 데이터를 처리
  - 변수의 클래스 타입 : String
  - 미디어 타입(컨텐트 타입) : * / *
+ MappingJackson2HttpMessageConverter : json 데이터 처리
  - 변수의 클래스 타입 : 객체 또는 HashMap
  - 미디어 타입 : application/json


```java
// 예시 1
content-type: application/json // 요청의 미디어 타입

@RequestMapping
void hello(@RequetsBody String data) {}
// 0순위의 ByteArrayHttpMessageConverter가 먼저 확인
// 변수 클래스 타입이 byte가 아니므로 다음으로 이동
// StringHttpMessageConverter가 확인
// 변수의 클래스 타입이 일치하므로 StringHttpMessageConverter가 처리

// 예시 2
content-type: application/json // 요청 미디어 타입

@RequestMapping
void hello(@RequetsBody HelloData data) {}
// 0순위 1순위 모두 아님
// 변수 클래스 타입이 객체이므로
// MappingJackson2HttpMessageConverter가 처리


// 예시 3
content-type: text/html // 요청 미디어 타입
@RequestMapping
void hello(@RequetsBody HelloData data) {}
// 0,1 순위가 아님
// 2순위 json컨버터 차례에서도 클래스 타입은 맞는데 미디어 타입이 다르므로 
// 다른 컨버터가 호출됨
```
<br>

## 9. MVC 전체 구조 이해
![그림1](https://github.com/backtony/blog-code/blob/master/spring/img/mvc/1/1-1.PNG?raw=true)

스프링 MVC는 프론트 컨트롤러 패턴으로 구현되어 있다. 스프링 MVC의 프론트 컨트롤러가 디스패치 서블릿(DispatcherServlet) 이다. DispatcherServlet도 부모 클래스에서 HttpServlet을 상속 받아서 사용하고, 서블릿으로 동작한다.(DispatcherServlet -> FrameworkServlet -> HttpServletBean -> HttpServlet)  
스프링 부트는 DispatcherServlet을 서블릿으로 자동으로 등록하면서 모든 경로 urlPatterns="/" 에 대해서 매핑한다. 더 자세한 경로가 우선순위가 높으므로 따로 등록한 서블릿도 함께 동작한다.  

### 요청 흐름 - 간단 버전
1. 핸들러 조회
  - 핸들러 매핑을 통해 요청 URL에 매핑된 핸들러(컨트롤러)를 조회한다.
2. 핸들러 어댑터 조회
  - 핸들러를 실행할 수 있는 핸들러 어댑터를 조회한다.
3. 핸들러 어댑터 실행
  - 조회한 핸들러(컨트롤러)를 인자로 핸들러 어댑터에 넘겨서 핸들러를 실행시킨다.
4. ModelAndView 반환
  - 핸들러(컨트롤러)가 로직을 수행하고 반환하는 정보로 ModelAndView로 변환해서 반환한다.
5. viewResolver 호출
  - 적절한 viewResolver를 찾고 해당 viewResolver를 호출한다.
6. View 반환
  - viewResolver는 뷰의 논리 이름을 물리 이름으로 바꾸고, 랜더링 역할을 담당하는 뷰 객체를 반환한다.
7. 뷰 랜더링
  - 뷰를 통해서 뷰를 랜더링한다.



### 요청 흐름 - 상세 버전
1. 클라이언트의 HTTP 요청
2. DispacherServlet 호출 -> HttpServlet이 제공하는 service 호출(DispacherServlet은 부모인 FrameworkServlet에서 service를 오버라이드 해두었음)
3. service를 시작으로 여러 메서드가 호출되고 DispatcherServlet.doDispatch()가 호출된다. 이제부터 doDispatch 메서드 안에 있는 메서드들의 실행
  - getHandler 메서드로 요청으로 들어온 URL을 보고 핸들러(컨트롤러)를 조회
  - getHandlerAdapter 메서드로 해당 핸들러(컨트롤러)를 처리할 수 있는 어뎁터를 조회한다. 핸들러 어댑터가 해당 핸들러(컨트롤러)를 support 한다면 핸들러 어댑터를 가져온다.
  - 핸들러 어댑터의 handle 메서드에 인자로 핸들러(컨트롤러)를 넘겨주면서 호출
  - 핸들러(컨트롤러)의 process 메서드가 실행되고 정보를 반환한다.
  - 핸들러 어댑터는 이 정보를 ModelAndView로 변환해서 반환한다.
  - DispatcherServlet에서는 viewResolver을 실행하여 뷰의 논리 이름을 물리 이름으로 바꾸고 렌더링 역할을 담당하는 뷰 객체를 반환한다.
  - 뷰를 통해 뷰를 렌더링 한다.



<br>

## 10. 요청 매핑 헨들러 어뎁터 구조
---
+ 핸들러 어댑터가 필요한 이유 : 한 프로젝트 안에서 여러가지 컨트롤러를 사용할 수 있도록, 프론트 컨트롤러가 다양한 방식의 컨트롤러를 처리할 수 있도록 하기 위함
+ 핸들러 어댑터 : 중간에서 어탭터 역할을 하는데 덕분에 다양한 종류의 컨트롤러를 호출할 수 있다.
+ 핸들러 : 컨트롤러의 이름의 더 넓은 범위, 어댑터가 있기 때문에 꼭 컨트롤러의 개념 뿐만 아니라 어떤 것이든 해당 종류의 어댑터만 있으면 다 처리 가능

<br>

### HandlerMapping, HandlerAdapter
핸들러 매핑에서 해당 컨트롤러를 찾을 수 있어야 하고 핸들러 매핑을 통해서 찾은 핸들러를 실행할 수 있는 핸들러 어댑터가 필요하다. 스프링은 이미 필요한 핸들러 매핑과 핸들러 어댑더를 대부분 구현해두었다. 핸들러 매핑, 핸들러 어댑터 모두 순서대로 찾고 만약 없으면 다음 순서로 넘어간다.
```java
// HandlerMapping
// 우선순위 = 이름 : 설명
0 = RequestMappingHandlerMapping : 애노테이션 기반의 컨트롤러인 @RequestMapping 에서 사용
1 = BeanNameUrlHandlerMapping : 스프링 빈의 이름으로 핸들러 찾기

// HandlerAdapter
0 = RequestMappingHandlerAdapter : 애노테이션 기반의 컨트롤러인 @RequestMapping 에서 사용
1 = HttpRequestHandlerAdapter : HttpRequestHandler 처리
2 = SimpleControllerHandlerAdapter : Controller 인터페이스 처리(과거에 사용하던것 현재 X)
```
<Br>

![그림2](https://github.com/backtony/blog-code/blob/master/spring/img/mvc/1/1-2.PNG?raw=true)

애노테이션 기반의 컨트롤러는 매우 다양한 파라미터를 사용한다. 이런 다양한 파라미터를 처리할 수 있는 이유는 ArgumentResolver 때문이다. 현재는 거의 대부분 애노테이션을 기반으로 개발하기 때문에 RequestMappingHandlerAdaptor을 사용하게 된다. 애노테이션 기반 컨트롤러를 처리하는 RequestMappingHandlerAdaptor(핸들러 어뎁터)는 핸들러의 정보를 확인한 뒤 ArgumentResolver을 호출해서 컨트롤러(핸들러)가 필요로 하는 파라미터 값(객체)을 처리할 수 있는지 확인한다. 처리가 가능하다면 생성하고 반환한다. 핸들러 어뎁터는 정보를 받아서 컨트롤러(핸들러)를 호출하면서 값을 넣어준다. 정확히는 HandlerMethodArgumentResolver인데 ArgumentResolver라고 부른다.  
HandlerMethodReturnValueHandler는 줄여서 ReturnValueHandler라고 부르는데 ArgumentResolver와 비슷하다. ArgumentResolver는 요청에서의 처리를 담당했다면 ReturnValueHandler는 응답값을 변환하고 처리한다.  
HTTP 메시지 컨버터는 ArgumentResolver 혹은 ReturnValueHandler가 값을 처리하는 과정에서 HTTP 메시지 바디에 있는 것의 처리를 부탁하기 위해서 호출하게 된다.
<br>

## 11. 리다이렉트
---
```java
@PostMapping("/{itemId}/edit")
public String edit(@PathVariable Long itemId)
// redirect:/
 return "redirect:/basic/items/{itemId}";
}
```
스프링은 redirect로 편리하게 리다이렉트를 지원한다. @PathVariabled 의 값을 redirect에서 사용할 수 있다.

<br>

## 12. PRG Post/Redirect/Get
---
```java
@PostMapping("/add")
    public String addItem(@ModelAttribute Item item){
        itemRepository.save(item);
        return "basic/item";
    }
```
위 코드는 아이템을 저장하고 화면에 item.html을 뿌려주는 코드이다. 여기서 Post라는 것을 주의해야한다. 웹 브라우저에서 새로고침은 마지막에 서버에 전송한 데이터를 다시 전송한다. 만약 웹브라우저에서 저장을 클릭한 상태에서 새로고침을 누르면 다시 post로 데이터가 저장으로 요청하게 되는 것이다. 즉, 중복저장되는 것이다. Post로 데이터를 보낼때는 이런 점을 항상 주의해야한다. 이것을 해결하는 방법이 리다이렉트다.  
![그림3](https://github.com/backtony/blog-code/blob/master/spring/img/mvc/1/1-3.PNG?raw=true)

앞서 http에서 공부했듯이 리다이렉트를 주면 Get방식으로 전환되기 때문에 새로고침을 하면 마지막 메서드 방식인 Get방식으로 동작하게 되어 위의 문제를 해결할 수 있다.

```java
return "redirect:/basic/items/" + item.getId();
```
위 코드에는 적절히 해결한 것 같지만 문제가 있다. URL에 변수를 더해서 사용하는 것은 URL 인코딩이 안되기 때문에 위험하다. 따라서 이를 해결할 방법이 필요하다.  
<br>

### RedirectAttributes
RedirectAttributes 를 사용하면 URL 인코딩도 해주고 PathVarible, 쿼리파라미터까지 처리해준다.
```java
@PostMapping("/add")
public String addItemV6(Item item, RedirectAttributes redirectAttributes) {
 Item savedItem = itemRepository.save(item);
 redirectAttributes.addAttribute("itemId", savedItem.getId());
 redirectAttributes.addAttribute("status", true);
 return "redirect:/basic/items/{itemId}";
}
```
redirectAttributes.addAttribute 으로 넣었던 값 중에서 return에서 {}에 같은 키값이 사용되면 해당 값으로 넣어주고, return에 사용되지 않은 나머지 값들은 리다이렉트 될 때 쿼리 파라미터로 넘겨준다. 위에서 itemId값이 3이라고 한다면 최종 URL은 /basic/items/3?status=true 이다.

<Br>

## 13. BindingResult와 Validator
---
```java
@PostMapping("/add")
public String addItemV3(@ModelAttribute Item item, BindingResult bindingResult){
  ...
}
```
컨트롤러에서 @ModelAttribute 애노테이션이 붙은 인자 __바로 뒤__ 에 BindingResult 인자를 주면 Item에 값을 넣어주면서 실패한 에러들이 bindingResult 값에 담긴다.  
그리고 논리적인 오류를 BindingResult에 추가해서 담을 수 있는데 이것을 컨트롤러에서 작성하면 컨트롤러가 너무 많을 일을 하게 된다.  
따라서 Validator 클래스를 따로 만들어서 위임한다.  

```java
@Component
public class ItemValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return Item.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        Item item = (Item) target;

        .. 생략

        // 특정 필드
        errors.rejectValue("quantity", "max", new Object[]{9999}, null);        

        // 특정 필드가 아닌 복합 룰 검증
        errors.reject("totalPriceMin", new Object[]{10000, resultPrice}, null);
    }
}
```
1. Validator를 구현시키고 @Component로 빈으로 등록한다.
2. support로 어떤 클래스를 검증할 수 있는지 정한다.
  - 위 코드에서 isAssignableFrom은 인자로 들어온 클래스가 Item과 같은 클래스거나 하위 클래스인지 확인한다.
3. validate 메서드에서는 논리적인 오류들을 작성한다.
  - rejectValue : 특정 필드 타입 에러      
  - reject : 특정 필드가 아닌 복합적인 논리적 에러

<Br>

```java
void rejectValue(@Nullable String field, String errorCode);
void rejectValue(@Nullable String field, String errorCode, String defaultMessage);
void rejectValue(@Nullable String field, String errorCode,@Nullable Object[] errorArgs, @Nullable String defaultMessage);
void reject(String errorCode);
void reject(String errorCode, String defaultMessage);
void reject(String errorCode, @Nullable Object[] errorArgs, @Nullable String defaultMessage);
```
+ field
  - 필드명
+ errorCode
  - 에러 코드
+ errorArgs
  - 에러 코드에 들어갈 인자값
+ defaultMessage
  - default 메시지

defaultMessage로 에러값을 정적으로 내려줄 수도 있고, errorCode와 errorArgs를 사용하면 동적으로 내릴 수 있다.  
동적으로 내리기 위해서는 메시지를 한곳에서 관리하기 위해 국제화에서 사용하는 방식을 사용해야 한다.  
메시지 국제화를 위해 defult로 message를 읽는 위치는 resources 위치에 messages.properties이다.  
국제화랑 같은 곳에서 에러 처리 메시지를 관리하기는 유지보수가 힘드므로 보통 따로 관리한다.  
errors.properties로 만들어서 관리해보려고 하므로 application.properties에 메시지로 읽는 위치를 추가해줘야 한다.
```
메시지 국제화의 기본 위치인 messages외에 errors위치도 추가한다는 의미
spring.messages.basename=messages,errors
```
그럼 이제 resources 위치에 errors.properties 파일을 생성하고 관리할 에러 메시지를 정의할 차례이다.  
에러 메시지 정의에도 규칙이 있다.
```
객체 오류의 경우
1순위 : code.objectname  
2순위 : code

예시
앞서 위쪽에서 item 객체에 totalPriceMin이라는 코드로 작성했다.
totalPriceMin.item=상품의 가격 * 수량의 합은 {0}원 이상이어야 합니다. 현재 값 = {1}
totalPriceMin=전체 가격은 {0}원 이상이어야 합니다. 현재 값 = {1}

필드 오류의 경우
1순위 : code.objectname.field
2순위 : code.field
3순위 : code.field type
4순위 : code

예시
앞서 위쪽에서 item 객체의 quantity 필드에 max라는 코드를 작성했다.
max.item.quantity = 아이템 수량은 최대 {0} 까지 허용합니다.
max.quantity = 수량은 최대 {0}까지 허용합니다.
max.java.lang.Integer = {0} 까지의 숫자를 허용합니다.
max = {0} 까지 허용합니다.

타입 미스매치로 스프링이 자동으로 bindingResult에 넣어주는 규칙
예) 오류 코드: typeMismatch, object name "user", field "age", field type: int 인 경우
1순위 : typeMismatch.user.age
2순위 : typeMismatch.age
3순위 : typeMismatch.int
4순위 : typeMismatch
```
이렇게 메세지로 관리해두고 BindingResult에서 사용하면 메시지는 한곳에서만 수정 및 관리를 할 수 있으므로 유지보수가 편하다.  
최종적으로 컨트롤러에 적용할 차례이다.  
```java
@Controller
@RequestMapping("/validation")
@RequiredArgsConstructor
public class ValidationItemController {

    private final ItemValidator itemValidator;

    @InitBinder
    public void init(WebDataBinder dataBinder) {
        dataBinder.addValidators(itemValidator);
    }

    @PostMapping("/add")
    public String addItemV6(@Validated @ModelAttribute Item item, BindingResult bindingResult) {
     ..
    }
}
```
빈으로 등록한 Validator을 주입받아서 @InitBinder로 validator을 등록해주면 해당 컨트롤러로 들어오는 요청에 대해 Validator를 사용할 수 있게 된다.  
사용할 인자 앞에 @Validated를 붙여주면 해당 컨트롤러에 등록된 Validator가 해당 인자에 동작하게 된다.  
추가적으로 Validator를 추가하고 싶다면 init메서드 안에서 addValidators로 여러개 추가할 수 있다.  
<br>


## 14. Bean Validation
---
정말 간단하게 필드 에러를 담기 위해서는 위에서 적용한 Validator 외에 Bean Validation을 사용하면 정말 간편하게 구현할 수 있다.  
의존성을 추가해준다.
```
implementation 'org.springframework.boot:spring-boot-starter-validation'
```
```java
@Data
public class ItemUpdateForm {

    @NotNull
    private Long id;

    @NotBlank(message = "빈값이 들어올 수 없습니다.")
    private String itemName;

    @NotNull
    @Range(min = 1000, max = 1000000)
    private Integer price;
}
```
+ @NotBlank 
  - 빈값 + 공백만 있는 경우를 허용하지 않는다.
+ @NotNull 
  - null 을 허용하지 않는다.
+ @Range(min = 1000, max = 1000000) 
  - 범위 안의 값이어야 한다
+ @Max(9999) 
  - 최대 9999까지만 허용한다.

추가적인 애노테이션은 [공식 문서](https://docs.jboss.org/hibernate/validator/6.2/reference/en-US/html_single/#validator-defineconstraints-spec)를 참고하자.  
<br>

```java
public String addItem(@Validated @ModelAttribute Item item, BindingResult bindingResult) {
...
}
```
Bean Validation을 사용할 경우에는 Validator를 생성하지 않고 컨트롤러에서 검증하고자 하는 인자에 __@Validated__ 만 붙여주면 된다.  
실제로 검증이 이루어 지는 순서는 다음과 같다.  
+ @ModelAttribute가 적용된 객체의 각각의 필드에 타입 변환 시도
  - __성공 시에만__ Bean Validation 적용
  - 실패 시 typeMismatch로 BindingResult에 FieldError 추가, Bean Validation이 적용되지 않는다.

<br>

### 메시지 생성 방식
메시지 생성 방식은 코드값이 애노테이션 이름으로 바뀐 것 외에는 기본적인 필드 에러와 일치한다.
```
@NotBlank
NotBlank.item.itemName 
NotBlank.itemName 
NotBlank.java.lang.String 
NotBlank
```
<br>

__errors.properties__
```
NotBlank={0} 공백X
```
Bean Validation의 경우 {0}은 필드명이고, {1},{2} ... 는 각 애노테이션 별로 다르다.


### 메시지 찾는 순서
1. 생성된 메시지 코드 순서대로 messageSource 에서 메시지 찾기
2. 애노테이션의 message 속성 사용 
3. 라이브러리가 제공하는 기본 값 사용


### 한계
특정 필드 에러가 아닌 복합 에러 처리의 경우, @ScriptAssert를 제공하나 제약이 많고 복잡하다.  
따라서 복합 필드의 에러의 경우, Validator를 등록해서 처리해야 한다.  


### @Validated가 @ModelAttribute와 @RequestBody에 적용될 때의 차이
+ @ModelAttribute
  - 각각의 필드 단위로 세밀하게 적용되어 특정 필드 타입이 맞지 않는 오류가 발생해도 나머지 필드는 정상 처리가 가능하다.
  - 타입이 맞지 않아도 __컨트롤러가 호출되고 정상적인 필드에 대해서는 Validator가 적용된다.__
+ @RequestBody
  - 각각의 필드 단위가 아니라 전체 객체 단위로 적용되어 전체가 정상적으로 타입이 맞아야만 Validator가 적용된다.
  - 따라서 타입이 맞지 않으면 HttpMessageConverter 단계에서 실패 예외가 발생하여 이후 단계가 진행되지 않아 __컨트롤러도 호출되지 않고 Validator도 적용할 수 없다.__


## 15. HTTP Session
---
기본적으로 로그인 과정은 서버측에서 세션을 만들어서 관리하고 세션 아이디를 쿠키에 담아서 클라이언트에 보내고 클라이언트는 매요청마다 쿠키를 서버측에 보내면서 로그인을 유지한다.  
```java
@PostMapping("/login")
public String loginV4(@Valid @ModelAttribute LoginForm form,
                      BindingResult bindingResult,
                      HttpServletRequest request) {

    // form에 해당하는 회원이 있다고 가정
    Member loginMember = loginService.login(form.getLoginId(), form.getPassword());

    // 로그인 성공 처리
    // 세션이 있으면 있는 세션 반환, 없으면 신규 세션을 생성
    HttpSession session = request.getSession();
    //세션에 로그인 회원 정보 보관
    session.setAttribute(SessionConst.LOGIN_MEMBER, loginMember.getId());

    return "redirect:" + redirectURL;
}
```
+ HttpServletRequest.getSession(true)
  - 세션이 있으면 기존 세션을 반환하고, 세션이 없으면 새로운 세션을 생성해서 반환한다.
  - default가 true이므로 true를 생략해도 된다.
+ HttpServletRequest.getSession(false)
  - 세션이 있으면 기존 세션을 반환하고, 없으면 null을 반환한다.
+ setAttribute
  - getSession으로 가져온 세션에 값을 키-밸류 쌍으로 넣는다.

세션을 가져와서(생성해서) setAttribute로 데이터를 세션에 보관하는데 하나의 세션에 여러 값을 저장할 수 있다.  
위와 같이 처리만 해주면 자동으로 response에 쿠키값으로 JSESSIONID가 담기게 된다.  
첫 응답시 리다이렉트 URL에 JSESSIONID가 붙는데 이는 브라우저가 쿠키를 지원하는지 않하는지 모르기 때문에 첫 응답시에만 적용되는 방식이다.  
보통 사용하지 않으므로 application.properties 에서 아래와 같이 사용하지 않는다고 설정해주자.
```
server.servlet.session.tracking-modes=cookie
```
<br>

```java
@PostMapping("/logout")
public String logoutV3(HttpServletRequest request) {
    HttpSession session = request.getSession(false);
    if (session != null) {
        session.invalidate();
    }
    return "redirect:/";
}
```
+ invalidate
  - 해당 세션을 제거한다.

<Br>

```java
public String homeLoginV3(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return "home";
        }
        Member loginMember = (Member)session.getAttribute("loginMember");
}
```
로그인을 유지하기 위해서는 세션에서 값을 꺼내고 확인하는 과정이 필요하다.  
세션에서 값을 꺼내기 위해서는 getAttribute로 키값을 넣어서 빼내는 과정을 @SessionAttribute 로 간소화할 수 있다.  
없으면 null이 담긴다.
```java
@GetMapping("/")
public String homeLoginV3Spring(@SessionAttribute(name = "loginMember", required = false) Member loginMember) {
}
```

### 세션 정보
```java
@GetMapping("/session-info")
public void sessionInfo(HttpServletRequest request) {
    HttpSession session = request.getSession(false);
    if (session == null) {
        return "세션이 없습니다.";
    }
    //세션 데이터 출력
    session.getAttributeNames().asIterator()
            .forEachRemaining(name -> log.info("session name={}, value={}", name, session.getAttribute(name)));

    log.info("sessionId={}", session.getId());
    log.info("getMaxInactiveInterval={}", session.getMaxInactiveInterval());
    log.info("creationTime={}", new Date(session.getCreationTime()));
    log.info("lastAccessedTime={}", new Date(session.getLastAccessedTime()));
    log.info("isNew={}", session.isNew());
}
```
+ sessionId 
  - 세션Id, JSESSIONID 의 값
+ maxInactiveInterval 
  - 세션의 유효 시간
  - 기본값 1800초, (30분)
+ creationTime 
  - 세션 생성일시
+ lastAccessedTime 
  - 세션과 연결된 사용자의 최근 서버 접근한 시간
  - 클라이언트에서 서버로 sessionId (JSESSIONID)를 요청한 경우에 갱신된다.
+ isNew 
  - 새로 생성된 세션인지 또는 클라이언트에서 서버로 sessionId(JSESSIONID)를 요청해서 조회된 세션인지 여부

세션 유효 시간의 기본값이 30분인데 해당 세션을 물고 새로운 요청을 보내게 되면 lastAccessedTime이 갱신되면서 lastAccessedTime 이후의 30분으로 갱신된다.  


### 타임아웃
세션은 사용자가 로그아웃을 호출해 session.invalidate()가 호출되는 경우에 삭제된다.  
하지만 대부분의 사용자는 로그아웃을 하지 않고 브라우저를 종료한다.  
따라서 서버는 세션을 언제 삭제해야하는지 판단하지 못하게 된다.  
결국 세션도 자원이기 때문에 10만명이 로그인하면 10만개의 세션이 생성되는 것이므로 적절한 관리가 필요하다.  
기본적으로 타임아웃값은 LastAccessedTime 이후로 timeout 시간(default 30분)이 지나면 삭제한다.  
글로벌 설정은 application.properties에서 다음과 같이 설정한다.
```
60초 
server.servlet.session.timeout=60
```
글로벌 설정은 기본적으로 분단위로 설정해야한다. 60(1분), 120(2분)  
만약 특정 세션만 설정하고 싶다면 해당 세션에서 다음과 같이 설정한다.
```java
session.setMaxInactiveInterval(1800); //1800초 = 30분
```

### 다중 서버에서 관리
위에서까지의 설명에서는 하나의 서버를 운영할때는 상관이 없으나 여러 서버에서 운영할 때는 각각 서버마다 세션 저장소를 갖기 때문에 문제가 생긴다.  
오토 스케일링과 같이 여러 서버를 띄워두고 사용하는 경우에는 보통 세션저장소를 따로 하나 만들어서 여러 서버가 한 곳을 바라보도록 만든다.  
이에 관해서는 잘 정리해놓은 곳이 있는데 [여기](https://hyuntaeknote.tistory.com/6?category=867120)를 참고하자.(4편의 시리즈로 단계절로 잘 정리가 되어있다.)

## 16. 필터
---
필터는 서블릿이 지원하고 서블릿 앞에서 동작하면서 공통 관심 사항 처리 및 부가 기능을 수행한다.  

>HTTP 요청 -> WAS -> 필터 -> 서블릿(디스패치) -> 스프링 인터셉터 -> 컨트롤러

필터를 적용하면 필터가 호출된 다음에 서블릿이 호출되기 때문에 서블릿이 호출되기 전에 부가적인 작업을 하거나 서블릿 호출을 막을 수 있다.  
예를 들면, 모든 요청에 대한 로그를 남긴다든지, 특정 url 패턴으로 들어온 요청을 막는다든지, 비 로그인 사용자에 대한 접근을 막는다든지에 대한 기능을 수행할 수 있다.  
특히, 필터는 적절한 요청이 아니라고 판단되면 서블릿 호출전에 끝낼 수 있기 때문에 로그인 여부를 체크하기에 좋다.  
스프링 시큐리티도 결국에는 필터를 활용하는 것이다.
```java
public interface Filter {

    public default void init(FilterConfig filterConfig) throws ServletException {}
   
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException;

    public default void destroy() {}
}
```
필터 인터페이스는 위와 같이 구성되어 있다.  
__필터는 서블릿 컨테이너가 싱글톤 객체로 생성하고 관리한다.__  
+ init
  - 필터 초기화 메서드
  - 서블릿 컨테이너가 생성될 때 호출
+ doFilter
  - 요청이 들어올 때마다 호출
  - 필터의 로직을 구현하는 부분
+ destroy
  - 필터 종료 메서드
  - 서블릿 컨테이너가 종료될 때 호출

<br>

### 필터 생성
로그인을 체크하는 필터를 만들어보자.
```java
@Slf4j
public class LoginCheckFilter implements Filter {

    private static final String[] whitelist = {"/", "/members/add", "/login", "/logout", "/css/*"};

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String requestURI = httpRequest.getRequestURI();

        HttpServletResponse httpResponse = (HttpServletResponse) response;

        try {
            log.info("인증 체크 필터 시작 {}", requestURI);

            if (isLoginCheckPath(requestURI)) {
                log.info("인증 체크 로직 실행 {}", requestURI);
                HttpSession session = httpRequest.getSession(false);
                if (session == null || session.getAttribute(SessionConst.LOGIN_MEMBER) == null) {

                    log.info("미인증 사용자 요청 {}", requestURI);
                    //로그인으로 redirect
                    httpResponse.sendRedirect("/login?redirectURL=" + requestURI);

                    // 이후 로직을 실행하는 chain.doFilter를 호출하지 않도록 바로 return 시켜서 접근 막기
                    return;
                }
            }

            chain.doFilter(request, response);
        } catch (Exception e) {
            throw e; //예외 로깅 가능 하지만, 톰캣까지 예외를 보내주어야 함
        } finally {
            log.info("인증 체크 필터 종료 {} ", requestURI);
        }

    }

    /**
     * 화이트 리스트의 경우 인증 체크X
     */
    private boolean isLoginCheckPath(String requestURI) {
        return !PatternMatchUtils.simpleMatch(whitelist, requestURI);
    }
}
```
간단하게 whiteList로 들어온 URL에 대해서는 넘어가고, 이외의 URL에 대해서는 세션을 확인하는 필터이다.  
+ ServletRequest, ServletResponse
  - 필터를 등록했을 때, HTTP 요청이 들어오면 doFilter 메서드가 호출된다.
  - ServletRequest, ServletResponse는 HttpServletRequest, HttpServletResponse의 부모로 HTTP 요청이 아닌 경우까지 고려한 인터페이스다.
  - 보통은 사용하지 않으므로 Http 것으로 다운 캐스팅해서 사용한다.
+ chain.doFilter(request,response)
  - 다음 필터가 있으면 다음 필터를 호출하고, 다음 필터가 없다면 서블릿을 호출한다.
  - 해당 코드가 없다면 필터도 호출하지 않고, 서블릿도 호출하지 않는다.
  - 해당 코드를 기준으로 앞쪽은 서블릿 호출전, 이후는 호출후로 나뉜다.
+ httpResponse.sendRedirect
  - 로그인이 안되어있으면 로그인 페이지로 리다이렉트 시키는 코드이다.
  - chain.doFilter를 호출하기 전에 return 되므로 이후 필터 호출과 서블릿 호출을 하지 않는다.
  - 파라미터 값으로 redirectURL을 주었는데, 로그인 컨트롤러에서 RequestParamt으로 이 값을 받아서 로그인 후 해당 페이지로 redirect 하도록 설계하면 된다.


### 필터 등록
```java
@Configuration
public class WebConfig{
    @Bean
    public FilterRegistrationBean LoginCheckFilter() {
        FilterRegistrationBean<Filter> filterRegistrationBean = new FilterRegistrationBean<>();
        filterRegistrationBean.setFilter(new LoginCheckFilter()); 
        filterRegistrationBean.setOrder(1);
        filterRegistrationBean.addUrlPatterns("/*");
        return filterRegistrationBean;
    }

    @Bean
    public FilterRegistrationBean anotherFilter() {
        FilterRegistrationBean<Filter> filterRegistrationBean = new FilterRegistrationBean<>();
        ... 생략
        filterRegistrationBean.setOrder(2);
        ... 생략
        return filterRegistrationBean;
    }
}
```
필터 등록에는 여러가지 방식이 있지만, 스프링 부트를 사용한다면 FilterRegistrationBean를 사용하면 된다.  
여러개의 필터를 등록하고 싶다면 위 코드처럼 여러개를 등록하고 순서를 다르게 주면 된다.
+ setFilter
  - 원하는 필터 지정
+ setOrder
  - 필터체인의 순서 등록
+ addUrlPatterns
  - 어떤 URL 패턴에 필터를 적용할지 지정
  - 여러 패턴 지정 가능


<br>

## 17. 인터셉터
---
인터셉터는 필터와 달리 스프링 MVC가 제공하는 기술이다.  

>HTTP 요청 -> WAS -> 필터 -> 서블릿(디스패치) -> 스프링 인터셉터 -> 컨트롤러

필터와 마찬가지로 공통 관심 사항 및 부가 기능을 담당하지만, 컨트롤러 앞에서 동작하고 필터보다 더 편리하고 정교하며 더 많은 기능을 제공한다.  
따라서 __꼭 필터를 사용해야하는 상황이 아니라면 인터셉터를 사용하는 것이 더 편리하다.__  
인터셉터도 컨트롤러의 호출 전에 동작해서 적절하지 않은 요청이라면 컨트롤러를 호출하지 않고 끝낼 수 있다.  
따라서 필터와 마찬가지로 로그인 여부를 체크하기에 좋다.  
```java
public interface HandlerInterceptor {
	
	default boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {

		return true;
	}

	default void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			@Nullable ModelAndView modelAndView) throws Exception {
	}

	default void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
			@Nullable Exception ex) throws Exception {
	}
}
```
필터의 경우 단순하게 doFilter 메서드에서 chain.doFilter를 기준으로 호출 전과 호출 후의 과정을 분리해서 사용해야 했다.  
하지만 인터셉터의 경우 호출전(preHandle), 호출 후(postHandle), 요청 완료 이후(afterCompletion)로 세분화 되어 있다.  
또한, 어떤 컨트롤러(handler)가 호출되는지, 어떤 modelAndView가 반환되는지 응답 정보도 받을 수 있다.  

![그림4](https://github.com/backtony/blog-code/blob/master/spring/img/mvc/1/1-4.PNG?raw=true)  
+ preHandle
  - 디스패처가 컨트롤러 찾고 어뎁터로 넘기기 전에 실행
  - 응답 값이 true이면 다음으로 진행
  - 응답 값이 false이면 더이상 진행하지 않고 끝낸다.
+ postHandle
  - 컨트롤러가 수행을 끝내고 어댑터가 ModelAndView를 디스패처 서블릿에 반환한 후에 실행
  - __컨트롤러에서 예외가 발생한다면 호출되지 않는다.__
+ afterCompletion
  - 뷰가 랜더링 된 이후에 수행
  - __항상 호출되므로 예외가 발생해도 작동하며 ex로 예외 정보를 받을 수 있다.__

### 인터셉터 생성
```java
@Slf4j
public class LogInterceptor implements HandlerInterceptor {

  public static final String LOG_ID = "logId";

   @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String requestURI = request.getRequestURI();
        request.setAttribute(LOG_ID,"공유"); // postHandle에서 사용하기 위한 값 저장해서 보내기

        log.info("인증 체크 인터셉터 실행 {}", requestURI);

        HttpSession session = request.getSession();

        if (session == null || session.getAttribute(SessionConst.LOGIN_MEMBER) == null) {
            log.info("미인증 사용자 요청");
            //로그인으로 redirect
            response.sendRedirect("/login?redirectURL=" + requestURI);
            return false; // 이후 과정 호출 중단
        }

        /* 참고사항 -> 해당 컨트롤러 꺼내서 사용하는 방법
        // @RequestMapping을 사용한 컨트롤러가 호출되는 경우 -> HandlerMethod
        // 컨트롤러가 아니라 /resources/static 같은 정적 리소스가 호출되는 경우 -> ResourceHttpRequestHandler
        if (handler instanceof HandlerMethod) {
            HandlerMethod hm = (HandlerMethod) handler; //호출할 컨트롤러 메서드의 모든 정보가 포함되어 있다.
        }
        */

        return true; // 다음 인터셉터 호출 또는 컨트롤러 호출
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        request.getAttribute(LOG_ID); // preHandle에서 적은 것 가져오기
        log.info("postHandle [{}]", modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {        
        if (ex != null) {
            log.error("afterCompletion error!!", ex);
        }
    }
}
```
인터셉터는 HandlerInterceptor를 구현해서 만들면 되는데 default이므로 필요한 메서드만 선택해서 만들면 된다.  
필터의 경우 chain.doFilter로 앞뒤 과정이 분류되지만 결과적으로는 하나의 메서드 안에서 동작하기 때문에 지역변수를 공유해서 사용할 수 있었다.  
하지만 인터셉터의 경우 메서드가 분리되어 있기 때문에 공유할 수 없고, 싱글톤으로 관리되기 때문에 전역변수로도 사용할 수 없다.  
따라서 이때는 request에다가 Attribute로 값을 실어 보내면 된다.  

### 인터셉터 등록
```java
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LogInterceptor()) // 인터셉터 등록
                .order(1) // 순서
                .addPathPatterns("/**") // 등록 패턴
                .excludePathPatterns("/css/**", "/*.ico", "/error"); // 제외 패턴

        // 추가 인터셉터
        registry.addInterceptor(new anotherInterceptor())
                .order(2)
                .addPathPatterns("/**") // /의 모든 하위를 포함
                .excludePathPatterns("/", "/members/add", "/login", "/logout",
                        "/css/**", "/*.ico", "/error");
    }
}
```
인터셉터의 등록의 경우, WebMvcConfigurer를 구현하고 addInterceptors를 재정의 해주면 된다.  
registry에 체이닝으로 인터셉터를 정하고 순서, url 패턴들을 입력하면 된다.  
추가적인 인터셉터를 등록하고 싶다면 다시 아래에 registry로 등록해주면 된다.  
__앞서 필터에서 등록한 URL 경로와 스프링이 제공하는 URL 경로는 완전히 다르다.__  
더욱 자세하고 상세하게 설정할 수 있는데 이는 공식문서 [여기](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/util/pattern/PathPattern.html)를 참고하자.  


### ArgumentResolver
요청 매핑 헌들러 어댑터 구조에서 컨트롤러를 호출할 때 ArgumentResolver가 컨트롤러에 인자들을 넣어주는 작업을 했다.  
이를 활용하면 로그인 회원을 찾는 과정을 편리하게 수행할 수 있다.  
<Br>

```java
@GetMapping("/")
public String homeLogin(@Login Member loginMember) {
        ...
}
```
기존 코드에서는 request에서 세션을 꺼내 Member를 찾아야 했다.  
하지만 위의 코드에서는 ArgumentResolver를 통해 @Login 애노테이션을 만들어 바로 Member를 가져올 수 있도록 했다.  
참고로 편의상 세션에 Member를 넣고 사용했지만 실제로는 세션에 최소한의 값만을 유지하는 것이 좋기 때문에 보통 Member의 id값만 넣어두는 형식으로 사용한다.  
<br>

먼저 @Login 애노테이션은 커스텀 애노테이션이므로 만들어준다.
```java
@Target(ElementType.PARAMETER) // 파라미터에만 적용
@Retention(RetentionPolicy.RUNTIME) // 리플렉션 등을 활용할 수 있도록 런타임까지 정보가 남음
public @interface Login {
}
```
<br>

이제 ArgumentResolver를 하나 만들어서 @Login 애노테이션이 붙은 파라미터의 경우 어떤 동작을 수행할지 정의해줄 차례이다.  
```java
@Slf4j
public class LoginMemberArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        log.info("supportsParameter 실행");

        boolean hasLoginAnnotation = parameter.hasParameterAnnotation(Login.class);
        boolean hasMemberType = Member.class.isAssignableFrom(parameter.getParameterType());

        return hasLoginAnnotation && hasMemberType;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {

        log.info("resolveArgument 실행");

        HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }

        return session.getAttribute(SessionConst.LOGIN_MEMBER);
    }
}
```
HandlerMethodArgumentResolver를 구현해서 supportsParameter와 resolveArgument를 재정의해주면 된다.
+ supportsParameter
  - 만드는 LoginMemberArgumentResolver가 어떤 파라미터를 지원할지 작성하는 곳이다.
  - parameter.hasParameterAnnotation
    - 파라미터가 어떤 애노테이션이 붙어있는지 확인
  - isAssignableFrom
    - 파라미터 타입체크
+ resolveArgument
  - supportsParameter 응답으로 true가 나온 경우, 실질적으로 로직을 수행하는 곳으로 해당 파라미터에 넣어줄 값을 반환하는 곳이다.
  - 세션에 있는 로그인 멤버를 가져와서 반환했다.

<br>

ArgumentResolver를 통해 어디에 적용되고 어떤 동작을 수행할 것인지 정의했다.  
이제 ArgumentResolver를 등록만 해주면 끝이다.  

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new LoginMemberArgumentResolver()); // 만든 ArgumentResolver 추가
    }
}
```


## 18. 예외 처리
---
서블릿은 다음과 같은 2가지 방식으로 예외를 처리한다.
+ Exception
  - 컨트롤러(예외발생) -> 인터셉터 -> 서블릿 -> 필터 -> WAS(여기까지 전파)
+ response.sendError(HTTP 상태 코드, 에러 메시지)
  - 컨트롤러(예외발생) -> 인터셉터 -> 서블릿 -> 필터 -> WAS(sendError 호출 기록 확인)

Exception의 경우 서버 내부 오류이므로 500이 나가고, responseError의 경우 상태 코드를 지정할 수 있다.  
WAS는 예외나 sendError을 받으면 오류 페이지 정보를 확인하고 지정된 오류 페이지를 출력하기 위해 다시 요청을 보낸다.  
즉, 다시 WAS -> 필터 -> 서블릿 -> 인터셉터 -> 오류 컨트롤러 형태로 요청을 새로 보내게 된다.  
이 과정에서 필터와 인터셉터를 다시 타는 것은 매우 비효율적이다.  
따라서 에러 페이지를 내리기 전에 우선적으로 필터와 인터셉터에 대해서 문제를 해결해야 한다.  
이에 대한 해결책은 필터와 인터셉터가 각각 다르다.
+ 필터
  - 특별한 설정을 할 필요가 없다.
  - 서블릿은 이런 문제를 해결하기 위해 DispatcherType를 추가 정보를 제공하는데 클라이언트가 요청하면 REQUEST, 에러가 전달될 때는 ERROR로 들어온다. 기본적으로 필터는 REQUEST가 디폴트 값이기 때문에 클라이언트의 요청만 필터링을 한다. 따라서 추가적인 작업을 할 필요가 없다.
+ 인터셉터
  - excludePathPatterns를 사용한다.
  - 앞서 인터셉터를 등록하는 과정에서 인터셉터를 타지 않을 URL를 excludePathPatterns에 명시해줬다. 그 곳에 에러 컨트롤러의 url을 명시해주면 된다.


### 에러 페이지 처리
스프링에서는 예외 처리 페이지를 제공하는 컨트롤러인 BasicErrorController 제공한다.  
/error 라는 경로로 기본 오류 페이지가 설정되어 있고 뷰 선택 우선순위에 따라 들어오는 에러 페이지를 보여준다.  
우선순위는 다음과 같다.
1. 뷰 탬플릿
  - resources/templates/error/500.html  -> 구체적인 것 우선
  - resources/templates/error/5xx.html
2. 정적 리소스
  - resources/static/error/400.html -> 구체적인 것 우선
  - resources/static/error/4xx.html
3. 적용 대상이 없을 때
  - resources/templates/error.html


![그림5](https://github.com/backtony/blog-code/blob/master/spring/img/mvc/1/1-5.PNG?raw=true)  
스프링 부트는 기본적으로 3가지 ExceptionResolver를 제공한다.  
ExceptionResolver는 컨트롤러 밖으로 예외가 던져진 경우 예외를 해결하고, 동작을 새로 정의할 수 있는 방법을 제공한다.  
즉, Exception이 터진 것을 WAS로 가기 전에 변경할 수 있는 것이다.  
+ __1순위 ExceptionHandlerExceptionResolver -> 대부분 이것으로 해결__
+ 2순위 ResponseStatusExceptionResolver
+ 3순위 DefaultHandlerExceptionResolver

가장 간단한 2,3순위부터 보고 마지막으로 1순위를 알아보자.

#### ResponseStatusExceptionResolver
```java
@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "잘못된 요청 오류") 
public class BadRequestException extends RuntimeException {
}
```
기본적으로 Exception이 터져서 WAS로 예외가 나가면 500에러 처리가 된다.  
하지만 sendError처럼 에러 상태를 조정하고 싶을 때 해당 Exception 클래스에 @ResponseStatus를 붙여주면 Exception이 sendError로 변환되서 나간다.  
reason은 메시지 국제화에서 사용하는 MessageSource에서 찾는 기능도 제공한다.  
messages.properties에 만약 error.bad라고 명시해뒀다면 reason 속성에 error.bad라고 명시하면 사용된다.  
<br>

위와 달리 개발자가 직접 변경할 수 없는 예외의 경우에는 ResponseStatusException를 사용한다.
```java
@GetMapping("/api/response-status-ex2")
public String responseStatusEx2() {
    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "error.bad", new IllegalArgumentException());
}
```
결과적으로 동작 방식은 똑같다.

#### DefaultHandlerExceptionResolver
DefaultHandlerExceptionResolver는 스프링 내부에서 발생하는 스프링 예외를 해결한다.  
대표적으로 파라미터 바인딩 시점에 타입이 맞지 않으면 내부에서 TypeMismatchException이 발생하고 별다른 처리를 하지 않으면 그대로 WAS에 넘어가 500에러로 처리된다.  
하지만 바인딩 에러는 대부분 HTTP 요청 정보를 잘못해서 발생하는 문제이므로 DefaultHandlerExceptionResolver가 상태코드를 400으로 변경해서 내린다.  
이 방식도 결국에는 sendError로 변경해서 보내는 방식이다.


#### ExceptionHandlerExceptionResolver
```java
@Data
@AllArgsConstructor
public class ErrorResult {
    private String code;
    private String message;
}

@Slf4j
@RestControllerAdvice(basePackages = "hello.exception.api")
public class ExControllerAdvice {

    @ExceptionHandler
    public ResponseEntity<ErrorResult> userExHandler(UserException e) {
        log.error("[exceptionHandler] ex", e);
        ErrorResult errorResult = new ErrorResult("USER-EX", e.getMessage());
        return new ResponseEntity(errorResult, HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler
    public ErrorResult exHandler(Exception e) {
        log.error("[exceptionHandler] ex", e);
        return new ErrorResult("EX", "내부 오류");
    }
}
```
가장 많이 사용되는 방식으로 위에 두 방식과 달리, sendError로 변경하여 보내 결과적으로 다시 요청을 타도록 하는 방식이 아니라, 바로 응답값을 변환해서 내린다.  
클래스에는 @RestControllerAdvice를, 각각의 메서드에는 @ExceptionHandler를 붙여주고 인자에는 처리하고자 하는 Exception을 받아주면 된다.  
@RestControllerAdvice(또는 @ControllerAdvice)는 대상으로 지정한 여러 컨트롤러에 @ExceptionHandler, @InitBinder 기능을 부여해주는 역할을 한다.  
메서드의 반환값들을 보면 ResponseEntity로 반환하면서 상태값을 명시해줘도 되고, 객체를 반환하면서 @ResponseStatus를 사용해서 상태값을 명시해줘도 된다.  
참고로 @RestControllerAdvice의 속성을 명시하지 않으면 모든 컨트롤러를 대상으로 적용되는데 속성을 이용해 대상을 명시할 수 있다.  
```java
// 특정 애노테이션이 붙은 컨트롤러만 적용
@ControllerAdvice(annotations = RestController.class)
public class ExampleAdvice1 {}

// 패키지 안에 있는 컨트롤러만 적용
@ControllerAdvice("org.example.controllers")
public class ExampleAdvice2 {}

// 특정 타입의 컨트롤러만 적용
@ControllerAdvice(assignableTypes = {ControllerInterface.class, AbstractController.class})
public class ExampleAdvice3 {}
```

<Br>

## 19. 타입 컨버터
---
### 컨버터
요청이 들어올 때 모든 값들은 String(문자열)로 들어오는데 @RequestParam, @ModelAttribute, @PathVariable등 값을 받아올 때는 String이 아닌 다른 타입으로 받아온다.  
이는 스프링이 중간에 타입변환기를 사용해서 타입을 변환해주었기 때문에 개발자는 타입을 직접 변환하지 않고 편리하게 원하는 타입으로 바로 받을 수 있는 것이다.  
```java
// Converter 패키지 주의!! 여러개가 나옴
package org.springframework.core.convert.converter;

public interface Converter<S, T> {
      T convert(S source);
}
```
스프링은 수많은 컨버터를 제공하지만 확장이 가능한 컨버터 인터페이스를 제공한다.  
개발자는 스프링에 추가적인 타입 변환이 필요하면 이 컨버터 인터페이스를 구현해서 등록하면 된다.  
<br>

ipPort 클래스를 만들어서 String 값으로 들어오면 ipPort로 변환하고, ipPort로 들어오면 String값으로 변환하는 컨버터를 만들어보자.
```java
@Getter
// lombok이 제공하는 것으로 equals와 hascode메서드를 생성해주어 모든 필드가 같다면 equals값이 참이된다.
@EqualsAndHashCode 
public class IpPort {

    private String ip;
    private int port;

    public IpPort(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }
}

@Slf4j
// 제네릭은 <입력타입,변환타입>
public class IpPortToStringConverter implements Converter<IpPort, String> {
    @Override
    public String convert(IpPort source) {
        log.info("convert source={}", source);
        //IpPort 객체 -> "127.0.0.1:8080"
        return source.getIp() + ":" + source.getPort();
    }
}

@Slf4j
public class StringToIpPortConverter implements Converter<String, IpPort> {

    @Override
    public IpPort convert(String source) {
        log.info("convert source={}", source);
        //"127.0.0.1:8080" -> IpPort 객체
        String[] split = source.split(":");
        String ip = split[0];
        int port = Integer.parseInt(split[1]);
        return new IpPort(ip, port);
    }
}
```
이렇게 양쪽으로 변환하는 컨버터를 만들어줬는데 일일이 호출해서 컨버터를 사용하는 것은 매우 불편하다.  
이를 위해 스프링은 ConversionService를 제공한다.  
ConversionService는 컨버터를 모아두고 묶어서 편리하게 사용할 수 있는 기능을 제공한다.  

<br>

```java
public interface ConversionService {

	boolean canConvert(@Nullable Class<?> sourceType, Class<?> targetType);
	
	boolean canConvert(@Nullable TypeDescriptor sourceType, TypeDescriptor targetType);

	@Nullable
	<T> T convert(@Nullable Object source, Class<T> targetType);

	@Nullable
	Object convert(@Nullable Object source, @Nullable TypeDescriptor sourceType, TypeDescriptor targetType);
}

public interface ConverterRegistry {
	void addConverter(Converter<?, ?> converter);

	<S, T> void addConverter(Class<S> sourceType, Class<T> targetType, Converter<? super S, ? extends T> converter);

	void addConverter(GenericConverter converter);

	void addConverterFactory(ConverterFactory<?, ?> factory);

	void removeConvertible(Class<?> sourceType, Class<?> targetType);

}
```
ConversionService 인터페이스는 단순하게 컨버팅이 가능한지, 컨버팅 기능을 제공한다.  
ConverterRegistry 인터페이스는 컨버터를 등록하는 기능을 한다.  
ISP 원칙(클라이언트가 자신이 이용하지 않는 메서드에는 의존하지 않아야 한다.)를 잘 지킨 것으로 보면 된다.  
따라서 컨버터를 등록하고 사용하기 위해서는 ConverterRegistry와 ConversionService를 구현한 구현체 DefaultConversionService를 사용한다.  
```java
// 등록 -> 추가적으로 여러 컨버터들 등록 가능
DefaultConversionService conversionService = new DefaultConversionService();
conversionService.addConverter(new IntegerToStringConverter());
conversionService.addConverter(new IpPortToStringConverter());

//사용
IpPort ipPort = conversionService.convert("127.0.0.1:8080", IpPort.class);
assertThat(ipPort).isEqualTo(new IpPort("127.0.0.1", 8080));

String ipPortString = conversionService.convert(new IpPort("127.0.0.1", 8080), String.class);
assertThat(ipPortString).isEqualTo("127.0.0.1:8080");
```
위와 같이 등록해서 사용하면 된다.  
Test용으로 작성했지만, 실제로 사용하게 되면 빈으로 등록해두고 사용하는 곳에서 주입받아서 사용하면 된다.  
<br>

위와 같이 등록해도 스프링에 등록된 것은 아니라 @RequestParam 같은 곳에서는 동작하지 않는다.  
스프링은 내부에서 ConversionService를 제공하는데 WebMvcConfigurer가 제공하는 addFormatter()를 사용해서 컨버터를 스프링에 등록해주면 된다.  
```java
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new StringToIntegerConverter());
        registry.addConverter(new IntegerToStringConverter());
    }
}
```
주의해야할 점이 스프링은 이미 수많은 컨버터를 제공하는데 이렇게 등록하게 되면 등록한 것이 우선순위를 가지기 때문에 주의해서 등록해야 한다.  
실질적으로는 이렇게 등록하는 경우는 드물고, 필요한 경우 앞서 DefaultConversionService을 이용해 사용하곤 한다.  

### Formatter
Converter는 문자를 객체로 변환하고 객체를 문자로 변환하는 등의 입출력 타입에 제한이 없는 범용적인 타입 변환 기능을 제공한다.  
범용적이기 때문에 객체를 특정한 포멧에 맞추어 문자로 출력하거나 그 반대의 기능을 할 수 없는데 이런 기능을 하는 것이 Formatter이다.  
Locale 정보도 사용할 수 있다.  
예를 들면, 1000을 문자로 바꿀때, 1,000 으로 변환하거나 그 반대의 기능은 Formatter를 이용한다.  
```java
public interface Printer<T> {
    String print(T object, Locale locale);
}
public interface Parser<T> {
  T parse(String text, Locale locale) throws ParseException;
}
public interface Formatter<T> extends Printer<T>, Parser<T> {}
```
Formatter 인터페이스는 Parser와 Printer 인터페이스를 상속받고 있다.
+ Parse
  - 문자를 객체로 변환
+ print
  - 객체를 문자로 변환


즉, 직접 만들고 싶다면 Formatter를 구현하면 된다.
```java
@Slf4j
public class MyNumberFormatter implements Formatter<Number> {

    @Override
    public Number parse(String text, Locale locale) throws ParseException {
        log.info("text={}, locale={}", text, locale);
        //"1,000" -> 1000
        NumberFormat format = NumberFormat.getInstance(locale);
        return format.parse(text);
    }

    @Override
    public String print(Number object, Locale locale) {
        log.info("object={}, locale={}", object, locale);
        return NumberFormat.getInstance(locale).format(object);
    }
}
```
Number는 Integer, Long과 같은 숫자 타입의 부모 클래스이다.  
나라마다 숫자에 쉼표를 찍는 위치가 다른데 locale 정보를 통해 나라마다 다르게 찍을 수 있다.  
컨버터와 마찬가지로 일일이 생성해서 쓰기 번거로우므로 컨버전 서비스에 등록해서 사용한다.  
컨버터를 사용할 때는 DefaultConversionService를 사용했지만, 포맷터를 추가적으로 지원하는 컨버전 서비스는 DefaultFormattingConversionService이다.  
DefaultFormattingConversionService는 컨버터도 지원하므로 만약 포맷터와 컨버터를 둘다 사용한다면 DefaultFormattingConversionService를 사용하는 것이 좋은 선택이다.
```java
public class FormattingConversionServiceTest {

    @Test
    void formattingConversionService() {
        DefaultFormattingConversionService conversionService = new DefaultFormattingConversionService();
        //컨버터 등록
        conversionService.addConverter(new StringToIpPortConverter());
        conversionService.addConverter(new IpPortToStringConverter());
        //포멧터 등록
        conversionService.addFormatter(new MyNumberFormatter());

        //컨버터 사용
        IpPort ipPort = conversionService.convert("127.0.0.1:8080", IpPort.class);
        assertThat(ipPort).isEqualTo(new IpPort("127.0.0.1", 8080));
        //포멧터 사용
        assertThat(conversionService.convert(1000, String.class)).isEqualTo("1,000");
        assertThat(conversionService.convert("1,000", Long.class)).isEqualTo(1000L);
    }
}
```
스프링에 등록하기 위해서는 컨버터랑 똑같이 하면 된다.
```java
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new StringToIpPortConverter());
        registry.addConverter(new IpPortToStringConverter());

        // 기존 코드에 추가
        registry.addFormatter(new MyNumberFormatter());
    }
}
```
<br><br>

사실 스프링에서 이미 수많은 포맷터를 이미 기본으로 제공하기 때문에 특별한 경우가 아니라면 직접 만들어 사용할 일이 거의 없다.  
가장 많이 사용하는 2가지는 다음과 같다.  
+ @NumberFormat
  - 숫자 관련 형식 지정 포맷터 사용
+ @DateTimeFormat
  - 날짜 관련 형식 지정 포맷터 사용

```java
class Form {
    @NumberFormat(pattern = "###,###")
    private Integer number;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime localDateTime;
}
```
위와 같이 지정해놓으면 위와 같은 형식의 문자열을 받아서 객체로 만들고, 객체를 위와 같은 형식의 문자열로 변환해준다.  
<br><Br>


정리하자면, 
+ Converter는 범용 변환기
+ Formatter는 문자에 특화(객체 <-> 문자) + 현지화(Local)
  - Converter의 특화 버전
+ 이미 수많은 컨버터, 포멧터를 제공하고 있기 때문에 특별한 경우가 아니라면 만들일은 거의 없다. 이런 방식으로 만들 수 있구나 정도 알아두고, 기본적으로 제공하는 NumberFormat, DateTimeFormat는 유용하니 알아두자.

<br>

## 20. 파일 업로드
---
파일 업로드에 관한 내용은 코드로 보는게 이해도 편하고 코드 자체도 길기 때문에 깃허브 코드 [여기](https://github.com/backtony/blog-code/tree/master/fileUpload)를 참고하자.  
스프링에서 파일 데이터는 MultipartFile 를 이용해 쉽게 받을 수 있다.  
매우 큰 파일을 업로드하게 둘수 없으므로 파일의 사이즈를 application.properties에서 제한할 수 있다.  
```
spring.servlet.multipart.max-file-size=1MB
spring.servlet.multipart.max-request-size=10MB
```
+ max-file-size
  - 파일 하나의 최대 사이즈
  - 기본값 1MB
+ max-request-size
  - 멀티파트 요청 하나에 여러 파일을 업로드할 수 있는데 그 전체의 용량 합
  - 기본값 10MB

사이즈를 넘으면 SizeLimitExceededException예외가 발생한다.

<br>

파일 데이터는 multipart/form-data 방식으로 전송되기 되므로 @RequestBody를 사용할 수 없다.  
일반적인 방식으로는 @RequestParam, @ModelAttribute, @RequestPart를 이용해 받을 수 있다.  
파라미터로 넘어오는게 아닌데 @RequestParam가 받을 수 있는 이유는 HTML form 형식으로 오는 데이터는 예외적으로 @RequestParam으로 받을 수 있기 때문이다.  
```java
public ResponseEntity<> file(@RequestPart MultipartFile files){}
public ResponseEntity<> file(@RequestParam MultipartFile file){}
public ResponseEntity<> file(@ModelAttribute MultipartFile file){}
// 생략시 modelAttribute 적용
public ResponseEntity<> file(MultipartFile file){}
```
3개 중 어느 애노테이션을 사용해도 상관 없다.  
<Br>

```java
@Data
public class ItemForm {
    private Long itemId;
    private String itemName;
    private List<MultipartFile> imageFiles;
    private MultipartFile attachFile;
}
```
만약 위와 같이 Form으로 여러 데이터를 한번에 받는다면 @ModelAttribute를 사용하면 된다.  

<br>

REST API에서 설계할 때는, 이미지와 데이터를 함께 보내게될 경우 문제가 있다.  
API 전송시에는 보통 content/type이 application/json이라 multipart/form-data 형식이 아니기 때문이다.  
해결 방법은 2가지가 있다.
1. 분리 요청
  - 이미지만 먼저 서버로 요청하고 서버는 이를 저장한 뒤 id(또는 파일명)을 클라이언트에 전송한다. 
  - 클라이언트는 이미즈의 id와 전송할 데이터를 application/json으로 요청
2. base64 인코딩
  - 이미지를 base64 인코딩해서 application/json으로 전송할 데이터와 함께 전송

API 통신에서는 @RequestPart를 사용하도록 한다.  
base64로 인코딩한 경우, 아래와 같이 받으면 된다.
```java
public ResponseEntity<Void> createReview(@RequestPart("request") CreateReviewRequest request,
                                          @RequestPart(value = "file") MultipartFile file)
```
<br>

참고로 html 파일을 이런 식으로 구성하면 된다.
```html
<form th:action method="post" enctype="multipart/form-data">
    <ul>
        <li>상품명 <input type="text" name="itemName"></li>
        <li>첨부파일<input type="file" name="attachFile" ></li>
        <li>이미지 파일들<input type="file" multiple="multiple" name="imageFiles" ></li>
    </ul>
    <input type="submit"/>
</form>
```
multiple은 여러 파일을 선택할 수 있는 방식이다.




<Br><Br>

__참고__  
<a href="https://www.inflearn.com/course/%EC%8A%A4%ED%94%84%EB%A7%81-mvc-1#" target="_blank"> 스프링 MVC 1편</a>   
<a href="https://www.inflearn.com/course/%EC%8A%A4%ED%94%84%EB%A7%81-mvc-2" target="_blank"> 스프링 MVC 2편</a>   



