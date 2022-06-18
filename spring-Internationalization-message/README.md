
## 메시지
기획자가 화면에 보이는 문구가 마음에 들지 않는다고 `상품명`이라는 단어를 모두 `상품 이름`으로 고쳐달라고 하면 어떻게 해야 할까요?  
여러 화면에 보이는 상품명, 가격 수량 등 `label`에 있는 단어를 변경하려면 해당 화면 코드에 가서 모두 변경해도 되지만 화면이 많아지면 수십개의 파일을 모두 고쳐야 하는 문제가 발생합니다. 이는 해당 HTML 파일에 메시지가 하드코딩 되어 있기 때문입니다.  
이런 다양한 메시지를 한 곳에서 관리하도록 하는 기능을 메시지 기능이라고 합니다.  
예를 들어 `messages.properties` 라는 메시지 관리용 파일을 만들고
```
item=상품
item.id=상품 ID
item.itemName=상품명
```
각 HTML들은 다음과 같이 해당 데이터를 key값으로 불러서 사용합니다.

```html
<label for="itemName" th:text="#{item.itemName}"></label>
```

## 국제화
메시지에서 설명한 메시지 파일 `message.properties`를 각 나라별로 별도로 관리하면 서비스를 국제화할 수 있습니다.  
예를 들어 다음고 같이 2개의 파일을 만들어서 분류합니다.  
__message_en.proerties__
```
item=Item
item.id=Item ID
item.price=price
```
__message_ko.properties__
```
item=상품
item.id=상품 ID
item.itemNAme=상품명
```
영어를 사용하는 사람이면 `message_en.properties`를 사용하고 한국어를 사용하는 사람이면 `message_ko.properties`를 사용하게 개발하면 됩니다.  
한국에서 접근한 것인지 영어권에서 접근한 것인지 인식하는 방법은 HTTP `accept-language` 헤더 값을 사용하거나 사용자가 직접 언어를 선택하도록 하고, 쿠키 등을 사용해서 처리하면 됩니다.  
직접 개발할 수도 있겠지만, 스프링에서 기본적인 메시지와 국제화 기능을 모두 제공하고 타임리프도 스프링이 제공하는 메시지와 국제화 기능을 편리하게 통합해서 제공하고 있습니다.



## 스프링 메시지 소스 설정
### 직접 등록
메시지 관리 기능을 사용하려면 스프링이 제공하는 MessageSource를 스프링 빈으로 등록하면 되는데 MessageSource는 인터페이스입니다. 따라서 구현체인 ResourceBundleMessageSource를 스프링 빈으로 등록하면 됩니다.
```java
@Bean
public MessageSource messageSource() {
    ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
    messageSource.setBasenames("messages", "errors");
    messageSource.setDefaultEncoding("utf-8");
    return messageSource;
}
```

setBasenames에서 messages, errors를 등록하게 되면 resources 패키지에서 messages.properties, errors.properties를 읽어오게 됩니다.

하지만 스프링 부트를 사용하면 스프링 부트가 MessageSource를 자동으로 스프링 빈으로 등록하므로 직접 등록할 필요가 없습ㄴ디ㅏ.

### 스프링 부트
앞서 언급했듯이 스프링 부트를 사용하면 알아서 빈을 등록해주므로 직접 세팅할 필요가 없이 `application.yml`로 설정할 수 있습니다.
```yml
spring:
  messages:
    basename: messages
```
사실 yml을 세팅하지 않아도 기본적으로 messages가 등록됩니다. 따라서 messages_en.properties, messages_ko.properties, messages.properties 등의 파일만 등록하면 자동으로 인식됩니다.

## 간단한 기능 알아보기
resource 위치에 메시지 파일을 만들어 보고 간단한 테스트를 진행해 봅시다.

__messages.properties__  
기본값으로 Locale 정보가 들어오지 않으면 사용되는 메시지 설정 파일입니다.
```
hello=안녕
hello.name=안녕 {0}
```
대괄호는 인자값을 받아서 매핑합니다.  
<br>

__messages_en.properties__  
영어권으로 요청시 사용되는 메시지 설정 파일입니다.
```
hello=hello
hello.name=hello {0}
```

<br>

간단하게 테스트를 진행해보겠습니다.
```java
@SpringBootTest
public class MessageSourceTest {

    @Autowired
    MessageSource ms;

    @Test
    void helloMessage() {
        String result = ms.getMessage("hello", null, null);

        assertThat(result).isEqualTo("안녕");
    }

    @Test
    void notFoundMessageCode() {
        assertThatThrownBy(() -> ms.getMessage("no_code",null,null))
                .isInstanceOf(NoSuchMessageException.class);
    }

    @Test
    void notFoundMessageCodeDefaultMessage() {
        String result = ms.getMessage("no_code", null, "기본 메시지", null);

        assertThat(result).isEqualTo("기본 메시지");
    }

    @Test
    void argumentMessage() {
        String result = ms.getMessage("hello.name", new Object[]{"spring"}, null);

        assertThat(result).isEqualTo("안녕 spring");
    }

    @Test
    void defaultLang() {
        assertThat(ms.getMessage("hello",null,null)).isEqualTo("안녕");
        assertThat(ms.getMessage("hello",null, Locale.KOREA)).isEqualTo("안녕");
    }

    @Test
    void enLang() {
        assertThat(ms.getMessage("hello",null,Locale.ENGLISH)).isEqualTo("hello");
    }

}
```
getMessage의 인자가 3개일 때는 (코드, args, locale)의 인자를 받고 4개일 때는 (코드, args, defaultMessage, locale) 값을 받습니다.

## 웹 애플리케이션에 메시지 적용하기
우선 message.properties에 더 많은 메시지를 추가했습니다.
```
hello=안녕
hello.name=안녕 {0}

label.item=상품
label.item.id=상품 ID
label.item.itemName=상품명
label.item.price=가격
label.item.quantity=수량

page.items=상품 목록
page.item=상품 상세
page.addItem=상품 등록
page.updateItem=상품 수정

button.save=저장
button.cancel=취소
```
타임리프의 메시지 표현식 `#{...}` 을 사용하면 스프링의 메시지를 편리하게 조회할 수 있습니다.  
예를 들어 방금 등록한 상품이라는 이름을 조회하려면 `#{label.item}` 이라고 하면 됩니다.
```html
<!-- 랜더링 전 -->
<div th:text="#{label.item}"></h2>

<!-- 랜더링 후 -->
<div>상품</h2>
```
```html
<!DOCTYPE HTML>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="utf-8">
    <link th:href="@{/css/bootstrap.min.css}"
          href="../css/bootstrap.min.css" rel="stylesheet">
    <style>
        .container {
            max-width: 560px;
        }
    </style>
</head>
<body>

<div class="container">

    <div class="py-5 text-center">
        <h2 th:text="#{page.addItem}">상품 등록</h2>
    </div>

    <form action="item.html" th:action th:object="${item}" method="post">
        <div>
            <label for="itemName" th:text="#{label.item.itemName}">상품명</label>
            <input type="text" id="itemName" th:field="*{itemName}" class="form-control" placeholder="이름을 입력하세요">
        </div>
        <div>
            <label for="price" th:text="#{label.item.price}">가격</label>
            <input type="text" id="price" th:field="*{price}" class="form-control" placeholder="가격을 입력하세요">
        </div>
        <div>
            <label for="quantity" th:text="#{label.item.quantity}">수량</label>
            <input type="text" id="quantity" th:field="*{quantity}" class="form-control" placeholder="수량을 입력하세요">
        </div>

        <hr class="my-4">

        <div class="row">
            <div class="col">
                <button class="w-100 btn btn-primary btn-lg" type="submit" th:text="#{button.save}">저</button>
            </div>
            <div class="col">
                <button class="w-100 btn btn-secondary btn-lg"
                        onclick="location.href='items.html'"
                        th:onclick="|location.href='@{/message/items}'|"
                        type="button" th:text="#{button.cancel}">취소</button>
            </div>
        </div>

    </form>

</div> <!-- /container -->
</body>
</html>
```
코드를 보면 곳곳에 th:text로 두고 #을 통해 messages.properties의 코드를 사용하는 것을 확인할 수 있습니다.  
파라미터는 다음과 같이 사용할 수 있습니다.
```html
hello.name=안녕 {0}
<p th:text="#{hello.name(${item.itemName})}"></p>
```

## 웹 애플리케이션 국제화 적용하기
message_en.properties 파일에 몇가지 더 추가해줍니다.
```
hello=hello
hello.name=hello {0}

label.item=Item
label.item.id=Item ID
label.item.itemName=Item Name
label.item.price=price
label.item.quantity=quantity

page.items=Item List
page.item=Item Detail
page.addItem=Item Add
page.updateItem=Item Update

button.save=Save
button.cancel=Cancel
```
이렇게만 하면 세팅은 끝납니다. 크롬에서 언어 세팅을 영어로 정렬하고 요청을 주면 messages_en.properties로 세팅해서 값을 내립니다.


## 스프링의 국제화 메시지 선택
앞서 테스트 코드에서 보았듯이 메시지 기능은 Locale 정보를 알아야 언어를 선택할 수 있습니다.  
결국 스프링도 Local 정보를 알아야 언어를 선택할 수 있는데 스프링은 언어 선택시 기본으로 `Accept-Language` 헤더의 값을 사용합니다.  
스프링은 Locale 선택 방식을 변경할 수 있도록 LocalResolver라는 인터페이스를 제공하는데 스프링 부트는 기본으로 Accept-Language를 활용하는 AcceptHeaderLocaleResolver를 사용합니다.  
만약 Local 선택 방식을 변경하려면 LocaleResolver의 구현체를 변경해서 쿠키나 세션 기반의 Locale 선택 기능을 사용할 수 있습니다.  
예를 들어 고객이 직접 Locale을 선택하도록 하는 것입니다. 관련해서는 LocalResolver를 검색하면 많은 예제가 나오니 그 자료들을 참고 바랍니다.






<br><Br><br>

---

__참고__  

[스프링 MVC 2편 - 백엔드 웹 개발 활용 기술](https://www.inflearn.com/course/%EC%8A%A4%ED%94%84%EB%A7%81-mvc-2#)









