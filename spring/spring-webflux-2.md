# Spring WebFlux - 리액터 타입, 리포지토리, 테스트

## 리액터 타입
리액티브 스트림은 수요 조절에 기반하고 있습니다. 프로젝트 리액터는 핵심 타입인 Flux\<T>를 사용하여 수요 조절을 구현합니다.  
```java
class KitchenService {
    Flux<Dish> getDishes(){
        return Flux.just(
            new Dish("dish1"),
            new Dish("dish2"),
            new Dish("dish3")
        )
    }
}
```
서빙 점원은 손님에게 가져다줄 Dish 객체를 달라고 KitchenService에 요철할 수 있습니다. 코드에 나온 세 가지 요루가 모두 완성된 후에 받을 수도 있지만, Flux\<Dish> 객체로 바로 받을 수도 있습니다. Flux\<Dish> 안에 포함된 요리는 아직 완성되지 않았지만 머지 않아 언젠간 완성될 것입니다.  
<br>

요리가 완성되면 서빙 점원은 행동에 나설 수 있습니다. 즉 요리 완성에 대한 반응 행동, __리액트__ 라고 할 수 있습니다. 리액터는 __논블로킹__ 방식으로 동작하기 때문에, 주방에서 요리가 완성될 때까지 서빙 점원(서버 스레드)이 다른 일을 못한 채 계속 기다리게 하지 않습니다.  
<br>

프로젝트 리액터는 풍부한 프로그래밍 모델을 제공합니다. 함수형 프로그래밍에서 수행하는 변환 뿐만 아니라 onNext(), onError, onComplete 시그널처럼 리액티브 스트림 생명주기에 연결 지을 수도 있습니다.  
```java
Flux<Dish> doingMyJob() {
    return kitchen.getDishes()
        .doOnNext(dish -> System.out.println("Thank you for " + dish + "!"))
        .doOnError(error -> System.out.println("So sorry about" + error.getMessage()))
        .doOnComplete(() -> System.out.println("Thanks for all your hard work!"))
        .map(Dish::diliver);
}
```
+ doOnNext : 리액티브 스트림의 OnNext 시그널을 받으면 동작합니다.
+ doOnError : onError 시그널을 받으면 처리합니다.
+ doOnComplete : onComplete 시그널을 받으면 처리합니다.

위 시그널은 여러 개를 반복해서 사용할 수 있으나 하나의 블록 안에서 처리하는 것이 좋습니다.
```java
Flux<Dish> doingMyJob() {
    return kitchen.getDishes()
        // bad
        .doOnNext(dish -> System.out.println("Thank you for " + dish + "!"))
        .doOnNext(dish -> System.out.println("Thank you for " + dish + "!"))
        // good
        .doOnNext(dish -> {
            System.out.println("Thank you for " + dish + "!");
            System.out.println("Thank you for " + dish + "!");
        });
        .doOnError(error -> System.out.println("So sorry about" + error.getMessage()))
        .doOnComplete(() -> System.out.println("Thanks for all your hard work!"))
        .map(Dish::diliver);
}
```

<br>

프로젝트 리액터에서는 필요한 모든 흐름과 모든 핸들러를 정의할 수 있지만 __구독__ 하기 전까지는 실제로 아무런 연산도 일어나지 않습니다. Optional의 Lazy Evalutation과 비슷하다고 보면 됩니다. 
```java
server.doingMyJob().subscribe(
    dish -> System.out.println("Cosuming" + dish),
    throwable -> System.err.println(throwable);
)
```
server.doingMyJob 메서드는 Flux\<Dish>를 반환하지만 아직 아무런 동작도 일어나지 않습니다. 그저 전달된 결과를 담는 플레이스홀더일 뿐입니다. 하지만 subscribe 메서드로 호출되면서 그때부터 무언가 동작하기 시작합니다.
<br>

```java
@Service
public class KitchenService {

    // 요리 스트림 생성
	Flux<Dish> getDishes() {
		return Flux.<Dish> generate(sink -> sink.next(randomDish())) //
				.delayElements(Duration.ofMillis(250));
	}

    // 요리 무작위 선택
	private Dish randomDish() {
		return menu.get(picker.nextInt(menu.size()));
	}

	private List<Dish> menu = Arrays.asList( 
			new Dish("Sesame chicken"), 
			new Dish("Lo mein noodles, plain"), 
			new Dish("Sweet & sour beef"));

	private Random picker = new Random();
}
```
앞선 코드에서는 Flux.just를 사용해서 고정적인 목록의 요리만 만들어주고 끝났지만 이번에는 Flux.generate 메서드를 사용해서 요리를 연속적으로 만들어 제공합니다. 제공 속도를 조절하기 위해 delayElement를 사용하여 0.25초 간격으로 요리를 제공합니다.  
generate 인자로 람다식이 들어가 있는데 sink는 randomDish로 제공되는 객체를 둘러싸는 Flux핸들로서 Flux에 포함될 원소를 동적으로 발행할 수 있는 기능을 제공합니다. 계속 찍어낸다고 보면 됩니다.  

## 스프링 데이터 리포지토리
리액티브 프로그래밍을 사용하려면 모든 과정이 리액티브여야 합니다. 웹 컨트롤러를 리액티브 방식으로 동작하게 만들고 서비스 계층도 리액티브 방식으로 동작하게 만들었는데, 블로킹 방식으로 연결되는 데이터베이스를 호출하면 리액티브는 무너집니다. 블로킹 방식으로 데이터베이스를 호출한 스레드는 응답을 받을 때까지 다른 작업을 하지 못한 채 기다려야 합니다. __리액터 기반 애플리케이션은 많은 수의 스레드를 가지고 있지 않으므로__ 데이터베이스 호출 후 블로킹 되는 스레드가 많아지면 스레드가 모두 고갈돼서 결국 전체 애플리케이션이 데이터베이스로부터 결과를 기다리면서 아무런 일도 할 수 없는 상태가 되버립니다.  
<Br>

스프링 데이터 리포지토리에서 다음과 같은 DB에서 리액티브 패러다임을 제공하고 있습니다
+ 몽고디비
+ 레디스
+ 아파치 카산드라
+ 엘라스틱 서치
+ 네오포제이
+ 카우치베이스

선택의 폭이 아주 넓지는 않습니다. 실무에서 사용 중인 80%의 데이터베이스는 관계형 데이터베이스입니다. 하지만 앞의 목록에는 관계형 데이터베이스가 포함돼 있지 않습니다. 가장 대표적으로 관계형 DB를 사용할 때 사용하는 기술은 다음과 같습니다.
+ JPA
+ JDBC
+ Jdbi
+ JOOQ

가장 널리 사용되는 것은 JDBC와 JPA인데 JPA와 JDBC는 블로킹 API입니다. 트랜잭션을 시작하는 메시지를 전송하고, 쿼리를 포함하는 메시지를 전송하고, 결과가 나올 때 클라이언트에게 스트리밍해주는 개념 자체가 없습니다. 모든 데이터베이스 호출은 응답을 받을 때까지 블로킹되어 기다려야 합니다. JDBC나 JPA를 감싸서 리액티브 스트림 계층에서 사용할 수 있게 해주는 반쪽짜리 솔루션도 있긴 하지만, 이런 솔루션은 일반적으로 숨겨진 내부 스레드 풀을 사용해서 동작합니다.  

<Br>

내부 스레드 풀을 사용하는 게 무슨 문제일까요?   
대부분 알려진 사실로는 4코어 장비면 4개의 스레드로 구성된 스레드 풀을 사용하는 게 좋다는 것입니다. 4코어 장비에 100개의 스레드를 만들어 사용하면 CPU 컨텍스트 스위칭 오버헤드가 증가하고 효율이 급격하게 떨어지게 됩니다.  
JPA같은 블로킹 API 앞에 스레드 풀을 두고 여러 스레드를 사용하는 방식은 일반적으로 포화지점에 도달하게 됩니다. 이 지점을 지나면 스레드 풀은 새 요청이 들어와도 받아서 처리할 스레드가 없으므로 스레드 풀 자체도 블로킹됩니다. 리액티브 프로그래밍에서 모든 것은 리액티브해야 하며, 일부라도 리액티브하지 않고 블로킹된다면 애플리케이션이 제대로 동작하지 않습니다.  
따라서 100% 리액티브 애플리케이션을 만들려면 데이터베이스와의 물리적 연결과 상호작용 과정에 비동기, 논블로킹 개념을 적용할 수 있는 데이터베이스 드라이버가 필요합니다.  

> 간혹 R2DBC를 들어본적이 있을텐데, R2DBC는 리액티브 스트림을 활용해서 관계형 데이터베이스에 연결할 수 있도록 설계된 명세입니다.

### 리액티브 몽고 DB 연동하기
```groovy
// 몽고 디비 리액티브 버전
implementation 'org.springframework.boot:spring-boot-starter-data-mongodb-reactive'

// 내장 몽고 DB
implementation 'de.flapdoodle.embed:de.flapdoodle.embed.mongo:3.4.5'
```
의존성을 추가해줍니다. 내장 몽고 DB는 h2같이 내장 db역할을 하는 의존성입니다.  

```yml
spring:
  data:
    mongodb:
      host: localhost
      port: 27017
      database: test
  # 버전이 올라가면서 embedded 버전을 명시해줘야하게 되었다.
  mongodb:
    embedded:
      version: 3.4.5
```
host, port, database 옵션은 작성하지 않아도 알아서 세팅되서 동작합니다. 특히, 테스트에서는 옵션을 명시하게 되면 충돌이 발생할 수 있으므로 적지 않는게 낫습니다.  

<br>

```java
public class Item {

	private @Id String id;
	private String name;
	private double price;

	private Item() {}

	Item(String name, double price) {
		this.name = name;
		this.price = price;
	}
}
```
```java
public interface ItemRepository extends ReactiveCrudRepository<Item, String>
```
JPA 리포지토리와 같은 방식으로 만들어낼 수 있습니다. 인터페이스인 ReactiveCrudRepository로부터 상속받는 메서드는 다음과 같습니다.
+ save, saveAll
+ findById, findAll, findAllById
+ existsById
+ count
+ deleteById, delete, deleteAll

JPA를 사용할 때와 거의 비슷하지만 다른점은 반환타입입니다. 모든 메서드의 반환 타입은 리액티브 타입인 Mono, Flux 둘 중 하나입니다. 따라서 Mono, Flux를 구독하고 잇다가 몽고디비가 데이터를 제공할 준비가 됐을 때 데이터를 받을 수 있게 됩니다. 그리고 일부 메서드는 리액티브 스트림의 Publisher 타입을 인자로 받을 수 있습니다.  

<br>

JPA처럼 사용한다고 하면 아래와 같이 상품을 저장하는 코드를 작성하게 됩니다.   
```java
itemRepository.save(new Item("clock", 20))
```
하지만 위 코드에는 문제가 있습니다.  
위의 save aptjemsms Mono 타입을 반환합니다. 따라서 해당 Mono 타입을 구독하기 전까지는 실제로 save 메서드는 아무일도 하지 않습니다. 일을 하게 하려면 아래와 같이 구독을 해야 합니다.
```java
itemRepository
    .save(new Item("clock", 20))
    .subscribe()
```

<br>

조금 더 복잡한 예시를 보겠습니다. 장바구니에 상품을 추가하는 로직입니다.  
```java
class Cart {
    @Id
	private String id;
	private List<CartItem> cartItems;
    // 메서드 생략
}
```
```java
class CartItem {
	private Item item;
	private int quantity;
    // 메서드 생략
}
```
```java
Mono<Cart> addToCart(String cartId, String id) { // <1>
    return this.cartRepository.findById(cartId) 
            .defaultIfEmpty(new Cart(cartId)) 
            .flatMap(cart -> cart.getCartItems().stream()  // <2>
                    .filter(cartItem -> cartItem.getItem() 
                            .getId().equals(id)) 
                    .findAny() 
                    .map(cartItem -> {
                        cartItem.increment();
                        return Mono.just(cart);
                    }) 
                    .orElseGet(() -> // <3>
                    this.itemRepository.findById(id) 
                            .map(CartItem::new) 
                            .doOnNext(cartItem -> 
                            cart.getCartItems().add(cartItem)) 
                            .map(cartItem -> cart))) // <4>
            .flatMap(this.cartRepository::save); // <5>
}
```
1. 몽고 디비에서 cart를 찾고 없으면 defaultIfEmpty 메서드를 통해 새로 생성해 반환합니다.
2. cart를 가져온 후에 가장 먼저 해야 할 일은 장바구니에 매개인자로 받은 id와 같은 아이템이 있는지 확인합니다. findAny는 Optional\<CartItem> 을 반환합니다. 같은 상품이 있다면 map 내부에서 해당 삼품의 수량만 증가시키고 장바구니를 Mono에 담아서 반환합니다.
3. 새로 장바구니에 담은 상품이 장바구니에 담겨 있지 않은 상품이라면, 몽고디비에서 해당 상품을 조회한 후 수량을 1로 지정하고(new 키워드로 생성시 1로 들어가도록 세팅해둠) CartItem에 담은 다음 cartItem을 장바구니에 추가한 후에 장바구니를 반환합니다. 이 과정은 장바구니에 동일한 상품이 없을 때만 수행되므로 하나의 람다 안에서 모두 처리합니다. 앞선 map에서 Mono로 반환하기 때문에 return cart는 자동으로 Mono에 감싸서 반환됩니다.
4. cartItem이 들어오지만 아무런 작업을 하지 않고 알고 있던 cart를 반환시켜버립니다.
5. 몽고 DB에 저장합니다.(map이 아니라 flatMap을 사용하는 이유는 아래서 설명합니다.)


<br>

위 코드에서 자바 스트림 API를 사용했는데 잠시 전통적인 반복문과 스트림 API의 차이를 비교해 봅시다.  
```java
boolean found = false;

for (CartItem cartItem : cart.getCartItems()) {
    if (cartItem.getItem().getId().equals("5")) {
        found = true;
    }
}

if (found) {
    // 수량 증가
} else {
    // 새 구매 상품 항목 추가
}
```
위 코드가 전통적인 반복문이고 앞선 코드보다 훨씬 직관적입니다. id가 5인 상품의 존재 여부를 found라는 상태로 관리하고, 모든 구매 상품을 ㄹ반복문으로 돌면서 상품 id가 5인지를 검사해서 맞으면 found 값을 true로 변경합니다.  
<br>

매우 직관적이고 쉬운데 리팩티브 프로그래밍에서는 위 방식을 권장하지 않습니다. 이유는 __side effect__ 때문입니다. 명령형 프로그래밍에서는 모든 로컬 변수에 부수 효과가 발생할 수 있습니다. 상태를 만들면 이 상태 값을 바꿀 수 있는 수많은 다른 API를 거치면서 상태가 어떻게 변경되는지 파악하기 어려워집니다. 따라서 여러 API를 거치는 중간에 어딘가 값이 잘못될 위험성도 함께 높아집니다.  
<br>

스트림 API를 사용하면 이런 단점을 극복할 수 있습니다.
```java
if (cart.getCartItems().stream()
    .anyMatch(cartItem -> cartItem.getItem().getId().equals("5"))) {
        // 수량 증가
} else {
    // 새 구매 상품 항목 추가
}
```
위 코드에서 가장 중요한 점은 found 같은 중간 상태가 없다는 것입니다. 따라서 found의 초깃값을 잘못 설정하거나, 값을 잘못 변경하는 위험이 사라집니다.  

<br>

> map과 flatMap <Br>
> + map : "이것"을 "저것"으로 바꾸는 함수형 도구<br>
> + flatMap : "이것"의 스트림을 다른 크기로 된 "저것"의 스트림으로 바꾸는 함수형 도구<Br>
> 
> 리액터의 flatMap은 Flux뿐만 아니라 Mono도 언팩하여 사용할 수 있습니다. 리액터의 연산자를 사용하면서 의도한 대로 동작하지 않을 경우 flatMap을 떠올리는 것도 좋습니다.<br>
> 앞선 예시에서 flatMap(cart -> this.cartRepository.save(cart)).thenReturn(..)에서 thenReturn은 그 앞에 수행된 연산이 무엇인지 알지 못합니다. 여기에서 flatMap을 대신해서 map을 사용하면 의도한 것과는 다르게 cart가 저장되지 않습니다. 이유는 map을 사용하면 save가 반환하는 Mono\<cart>를 한 번 더 감싸는 Mono\<Mono\<Cart>>가 반환되기 때문입니다. flatMap을 사용해야 Mono\<cart>가 반환됩니다.<Br>
> map은 동기식으로 동작하고 flatMap은 비동기식으로 동작합니다. 따라서 웬만하면 flatMap을 사용해야하고 map 연산은 연산마다 객체를 생성하기 때문에 CG에 대상이 많아지는 단점이 존재하기 때문에 너무 많은 map연산은 피해야 합니다.  


### 스프링 데이터 몽고디비 쿼리 메서드 이름 규칙
```java
public class Item{
    private @Id String id;
    private String name;
    private String description;
    private double price;
    private String distributorRegion;
    private Date releaseDate;
    private int availableUnits;
    private Point location;
    private boolean active;
}
```


쿼리 메서드|설명
---|---
findByDescription(..)|description 값이 일치하는 데이터 질의
findByNameAndDescription(..)|name 값과 description 값이 모두 일치하는 데이터 질의
findTop10ByName(..) 또는 findFirst10ByName(..)|name 값이 일치하는 첫 10개의 데이터 질의
findByNameIgnoreCase(..)|name값이 일치하는 첫 10개의 데이터 질의
findByNameAndDescriptionAllIgnoreCase(..)|name값과 description 값 모두 대소문자 구분 없이 일치하는 데이터 질의
findByNameOrderByDescriptionAsc(..)|name 값이 일치하는 데이터를 description 값 기준 오름차순으로 정렬한 데이터 질의
findByReleaseDateBefore(Date date)|releaseDate 값이 date보다 이전인 데이터 질의
findByReleaseDateAfter(Date date)|releaseDate 값이 date 이후인 데이터 질의
findByAvailableUnitsGreaterThan(int units)|availableUnits 값이 units보다 큰 데이터 질의
findByAvailableUnitsGreaterThanEqual(int units)|availableUnits 값이 units보다 크거나 같은 데이터 질의
findByAvailableUnitsLessThan(int units)|availableUnits 값이 units보다 작은 데이터 질의
findByAvailableUnitsLessThanEqual(int units)|availableUnits 값이 units보다 작거나 같은 데이터 질의
findByAvailableUnitsBetween(int from, int to)|availableUnits 값이 from과 to 사이에 있는 데이터 질의
findByAvailableUnitsIn(Collection unitss)|availableUnits 값이 unitss 컬렉션에 포함돼 있는 데이터 질의
findByAvailableUnitsNotIn(Collection unitss)|availableUnits 값이 unitss 컬렉션에 포함돼 있지 않은 데이터 질의
findByNameNotNull() 또는 findByNameIsNotNull()|name 값이 null이 아닌 데이터 질의
findByNameNull() 또는 findByNameIsNull()|name 값이 null인 데이터 질의
findByNameLike(String f)|name 값이 문자열 f를 포함하는 데이터 질의
findByNameNotLike(String f) 또는 findByNameIsNotLike(String f)|name 값이 문자열 f를 포함하지 않는 데이터 질의
findByNameStartingWith(String f)|name 값이 문자열 f로 시작하는 데이터 질의
findByNameEndingWith(String f)|name 값이 문자열 f로 끝나는 데이터 질의
findByNameNotContaining(String f)|name 값이 문자열 f를 포함하지 않는 데이터 질의
findByNameRegex(String pattern)|name 값이 pattern으로 표현되는 정규 표현식에 해당하는 데이터 질의
findByLocationNear(Point p, Distance max)|location 값이 p 지점 기준 거리 max 이내에서 가장 가까운 순서로 정렬된 데이터 질의
findByLocationNear(Point p, Distance min, Distance max)|location 값이 p지점 기준 거리 min 이상 max 이내에서 가장 가까운 순서로 정렬된 데이터 질의
findByLocationWithin(Circle c)|location 값이 원 영역 c 안에 포함돼 있는 데이터 질의
findByLocationWithin(Box b)|location 값이 직사각형 영역 b안에 포함돼 있는 데이터 질의
findByActiveIsTrue()|active 값이 true인 데이터 질의
findByActiveIsFalse()|active 값이 false인 데이터 질의
findByLocationExists(boolean e)|location 속성의 존재 여부 기준으로 데이터 질의

위는 deleteBy 메서드도 동일하게 작성할 수 있습니다. 몽고디비 기준으로 작성됐지만 대부분 JPA 아파치 카산드라 등 다른 데이터 스토어에서도 사용할 수 있습니다.  

### 쿼리문 자동 생성 메서드로 충분하지 않을 때

```java
@Query("{'name' : ?0, 'age' :?1}")
Flux<Item> findItemsForCustomerMonthlyReport(String name, int age);

@Query(sort = "{'age' : -1}")
Flux<Item> findSortedStuffForWeeklyReport();
```
@Query 애노테이션이 붙어 있는 메서드는 리포지토리 메서드 이름 규칙에 의해 자동으로 생성되는 쿼리문 대신 @Query 내용으로 개발자가 직접 명시한 쿼리문을 사용합니다.  

### Example 쿼리
쿼리 자동 생성과 직접 쿼리 작성을 사용하면 대부분을 처리할 수 있습니다. 하지만 필터링 기능을 추가한다면 이야기가 다릅니다. 분명 네이밍 규칙을 사용해서 필터링의 대부분 쿼리를 작성할 수는 있으나 필터링 요구사항이 추가될 때마다 메서드 이름은 점점 복잡해지고 해당 쿼리를 사용하는 서비스는 if, else로 복잡해지게 됩니다.  
이에 대한 대안이 Example 쿼리입니다. 쿼리를 사용해 여러 조건을 조립해서 스프링 데이터에 전달하면, 스프링 데이터는 필요한 쿼리문을 만들어줍니다. 그래서 조건이 추가될 때마다 계속 복잡해지는 코드를 유지 관리할 필요가 없습니다.  
```java
public interface ReactiveQueryByExampleExecutor<T> {
    <S extends T> Mono<S> findOne(Example<S> var1);

    <S extends T> Flux<S> findAll(Example<S> var1);

    <S extends T> Flux<S> findAll(Example<S> var1, Sort var2);

    <S extends T> Mono<Long> count(Example<S> var1);

    <S extends T> Mono<Boolean> exists(Example<S> var1);
}
```  
ReactiveQueryByExampleExecutor 인터페이스는 example 타입의 파라미터를 인자로 받아서 검색을 수행하고 하나 또는 그 이상의 T 타입 값을 반환합니다. 정렬 옵션도 줄 수 있고, 검색 결과 개수를 세거나 데이터 존배 여부를 반환하는 메서드도 있습니다.  

```java
public interface ItemByExampleRepository extends ReactiveQueryByExampleExecutor<Item> {}
```
ReactiveQueryByExampleExecutor를 상속아 구현하면 됩니다.  
name, description 필드에 대한 부분 일치, 대/소문자 무관한 검색과 and/or 사용을 모두 포함한 example 쿼리는 다음과 같습니다.
```java
Flux<Item> searchByExample(String name, String description, boolean useAnd) {
    Item item = new Item(name, description, 0.0); // <1>

    ExampleMatcher matcher = (useAnd // <2>
            ? ExampleMatcher.matchingAll() //
            : ExampleMatcher.matchingAny()) //
                    .withStringMatcher(StringMatcher.CONTAINING) // <3>
                    .withIgnoreCase() // <4>
                    .withIgnorePaths("price"); // <5>

    Example<Item> probe = Example.of(item, matcher); // <6>

    return repository.findAll(probe); // <7>
}
```
1. 검색어를 받아 새 item 객체를 생성합니다. price 값은 null일 수 없기에 0.0을 입력합니다.
2. 사용자가 선택한 boolean값인 useAnd 값에 따라 3항 연산자로 분기해서 ExampleMatcher를 생성합니다.
3. StringMatcher.CONTAINING을 사용해서 부분 일치 검색을 수행합니다. 외에도 다양한 옵션이 있습니다.
4. 대소문자 구분 X
5. ExampleMatcher는 기본적으로 null 필드를 무시하지만, 기본 타입인 double에는 null이 올 수 없으므로 price 필드가 무시되도록 명시적으로 지정합니다.
6. item 객체와 matcher를 함께 Example.of로 감싸서 Example을 생성합니다.
7. 쿼리를 실행합니다.

> 스프링 데이터 몽고디비가 도메인 객체를 몽고디비 도큐먼트로 저장할 때, _class라는 애트리뷰트가 포함됩니다. _class에는 개발자가 작성한 코드와 데이터베이스에 저장된 내용을 변환하는 데 필요한 메타 데이터가 담겨있습니다.<br>
> 스프링 데이터 몽고디비는 Example 쿼리를 엄격한 타입방식으로 구현해서 _class 필드 정보가 합치되는 몽고디비 도큐먼트에 대해서만 Example쿼리가 적용됩니다. 이 타입 검사를 위회해서 모든 컬렉션에 대해 쿼리를 수행하려면 ExampleMatcher 대신 UntypedExampleMatcher를 사용해야 합니다.  


### 평문형 연산
몽고디비 쿼리를 보통 문장 같은 형식으로 사용할 수 있는 평문형 연산이 있습니다.  
스프링 데이터 몽고디비에서는 FluentMongoOperations의 리액티브 버전인 ReactiveFluentMongoOperations를 통해 평문형 연산 기능을 사용할 수 있습니다.
```java
Flux<Item> searchByFluentExample(String name, String description) {
    return fluentOperations.query(Item.class)
        .matching(query(where("TV tray").is(name).and("smurf").is(description)))
        .all();
}
```
평문형 연산에서는 비어 있는 필드나 부분 일치 기능은 사용할 수 없습니다. 하지만 Example을 같이 사용하면 더 많은 기능을 사용할 수 있습니다.
```java
Flux<Item> searchByFluentExample(String name, String description, boolean useAnd) {
    Item item = new Item(name, description, 0.0);

    ExampleMatcher matcher = (useAnd 
            ? ExampleMatcher.matchingAll() 
            : ExampleMatcher.matchingAny()) 
                    .withStringMatcher(StringMatcher.CONTAINING) 
                    .withIgnoreCase() 
                    .withIgnorePaths("price");

    return fluentOperations.query(Item.class) 
            .matching(query(byExample(Example.of(item, matcher)))) 
            .all();
}
```

### 트레이드 오프

쿼리 방식|장점|단점
---|---|---
표준 CRUD|미리 정의돼 있음<br>소스 코드로 작성돼 있음<br>리액터 타입을 포함해 다양한 반환타입<br>데이터 스토어 간 호환성|1개 또는 전부에만 사용 가능<br>도메인 객체별로 별도의 인터페이스 작성 필요
메서드 이름 기반 쿼리|직관적<br>쿼리 자동 생성<br>리액터 타입을 포함한 다양한 반환타입<br>여러 데이터 스토어 모두 지원|도메인 객체마다 리포지토리 작성<br>여러 필드와 조건이 포함된 복잡한 쿼리에 사용하면 메서드 이름이 길어짐
Example 쿼리|쿼리 자동 생성<br>모든 쿼리 조건을 미리 알 수 없을 때 유용<br>Jpa,Redis에서도 사용 가능|도메인 객체마다 리포지토리 작성 필요
MongoOperations|데이터 스토어에 특화된 기능까지 모두 사용 가능<br>도메인 객체마다 별도의 인터페이스 작성 불필요|데이터 스토어에 종속적
@Query 사용|몽고QL 사용 가능<br>긴 메서드 이름 불필요<br>모든 데이터 스토어에서 사용 가능|데이터 스토어에 종속적
평문형 API|직관적<br>도메인 객체마다 별도의 인터페이스 작성 불필요|데이터 스토어에 종속적<br>JPA, 레디스에서 사용할 수는 있지만 호환은 안됨


## 스프링 부트 개발자 도구

### 개발자 도구
```groovy
implementation 'org.springframework.boot:spring-boot-devtools'
```
devtools 의존성은 다음과 같은 기능을 제공합니다.
+ 애플리케이션 재시작과 리로드 자동화
+ 환경설정 정보 기본값 제공
+ 자동설정 변경사항 로깅
+ 정적 자원 제외
+ 라이브 리로드 지원

devtools는 애플리케이션 시작 방법을 몰래 훔쳐보는데, java -jar 명령이나 클라우드 서비스 제공자가 사용하는 특별한 클래스로더를 통해 애플리케이션이 실행되면 상용 실행이라 판단하여 개발자 도구 기능을 활성화 하지 않지만 IDE에서 실행되거나 spring-boot:run 명령으로 실행되면 개발 모드라고 판단하여 개발자 도구 모든 기능을 활성화합니다.  
재시작은 IDE에서 저장 명령이나 빌드 프로젝트(Rebuild) 명령일 수 있습니다. 

#### 정적 자원 제외
대부분의 웹 기술에서는 정적 자원(static resource) 변경 내용은 재부팅 없이도 서버에 반영할 수 있스므로, 정적 자원 변경은 애플리케이션 재시작을 유발하지 않습니다. 변경사항이 재시작을 유발하지 않게 하는 경로를 변경하려면 다음과 같이 설정하면 됩니다.
```yml
spring:
  devtools:
    restart:
      exclude: static/**,public/**
```

#### 개발 모드에서 캐시 비활성화
스프링 부트와 통합되는 많은 컴포넌트는 다양한 캐시 수단을 가지고 있는데 어떤 템플릿 엔진의 경우에는 컴파일된 템플릿을 캐시하기도 합니다. 이는 상용 운영환경에서는 편리하지만 변경사항을 계속 확인해야 하는 개발 과정에서는 불편함을 가중시킵니다. 설정으로 비활성화할 수 있지만 개발/운영 환경마다 비활성화/활성화 하는 것은 번거로운 일이 됩니다. 이를 devtools가 해결해줍니다. IDE로 실행할 경우 개발 모드가 실행되기 때문에 여러 가지 환경설정 정보가 기본으로 정해진 값으로 지정되는데 IDE에서 DevToolsPropertyDefaultsPostProcessor을 열어보면 영향을 주는 모든 속성을 확인할 수 있습니다.  


#### 부가적 웹 활동 로깅
```yml
logging:
    level:
        web: DEBUG
```
위 설정을 하면 web 로깅 그룹에 대한 로깅을 활성화할 수 있습니다. 상세한 로그 덕에 어떤 HTTP 요청이 들어왔는지, 무슨 컨트롤러 메서드가 실행됐는지, 요청 처리 완료 후 최종 상태는 어떤지 등을 쉽게 확인할 수 있습니다.


#### 라이브 리로드
devtools에는 라이브 리로드 서버가 내장돼 있습니다. 따라서 서버가 재시작됐을 때 브라우저의 새로고침 버튼을 자동으로 눌러줍니다.  

### 리액터 개발자 도구
앞선 개발자 도구는 모두 스프링 부트 관련 기능입니다. 이제 리액터용 개발자 도구를 봅시다.  

#### 리액터 플로우 디버깅
리액터 플로우 중에 무언가 잘못됬다면 여러 스레드에 걸쳐 수행될 수 있기에 스택 트레이스를 통해 쉽게 확인할 수 없습니다. 리액터는 나중에 구독에 의해 실행되는 작업 흐름을 조립하는 비동기, 논블로킹 연산을 사용합니다.  
<br>

애플리케이션에서 리액터로 작성하는 일련의 연산은 앞으로 어떤 작업이 수행될지 기록해놓은 조리법이라고 생각할 수 있습니다. 스프링 레퍼런스 문서에서는 이를 조립이라고 부르며, 구체적으로는 람다 함수나 메서드 레퍼런스를 사용해서 작성한 명령 객체를 합쳐놓은 것이라고 볼 수 있습니다. 조리법에 포함된 모든 연산은 하나의 스레드에서 처리될 수도 있습니다.  
<br>

하지만 누군가가 구독하기 전까지는 아무런 일도 발생하지 않으며 조리법에 적힌 내용은 구독이 돼야 실행되기 시작해 리액터 플로우를 조립하는 데 사용된 스레드와 각 단계를 실제 수행하는 스레드가 동일하다는 보장은 없습니다.  
<br>

하지만 자바 스택 트레이스는 동일한 스레드 내에서만 이어지며, 스레드 경계를 넘지 못합니다. 이 한계를 극복하게 해 주는 것이 리액터의 Hooks.onOperatorDebug 메서드 입니다. 
```java
static class ReactorDebuggingExample {
    public static void main(String[] args) {

        Hooks.onOperatorDebug(); 

        Mono<Integer> source;
        if (new Random().nextBoolean()) {
            source = Flux.range(1, 10).elementAt(5);
        } else {
            source = Flux.just(1, 2, 3, 4).elementAt(5); 
        }
        source 
                .subscribeOn(Schedulers.parallel()) 
                .block(); 
    }
}
```
Hooks.onOperatorDebug를 호출하면 래익터가 처리 흐름 조립 시점에서의 호출부 세부정보를 수집하고 구독해서 실행되는 시점에 세부정보를 넘겨줍니다. 하지만 스택 세버정보를 스레드 경계를 넘어서 전달하는 과정은 굉장히 많은 비용이 들기 때문에 운영에서는 사용하면 안됩니다.  

#### 리액터 플로우 로깅
```java
return itemRepository.findById(id)
    .map(Item::getPrice);
```
```java
return itemRepository.findById(id)
    .map(item -> {
        log.debug("found item");
        return item.getPrice();
    });
```
첫 번째와 같이 메서드 레퍼런스를 사용한다면 간결하지만 log를 찍기 위해서는 두 번째처럼 사용해야 합니다. 짧고 가독성 좋은 코드를 장황한 코드로 바꾸면, 코드를 읽는 데 많은 비용이 듭니다. 그저 로깅 때문에 간결한 코드를 사용할 수 없다는 건 심각한 문제입니다. 리액터는 이 문제에 대한 해결법을 제시합니다.  
```java
Mono<Cart> addItemToCart(String cartId, String itemId) {
    return this.cartRepository.findById(cartId) 
            .log("foundCart") //
            .defaultIfEmpty(new Cart(cartId)) 
            .log("emptyCart") //
            .flatMap(cart -> cart.getCartItems().stream() 
                    .filter(cartItem -> cartItem.getItem() 
                            .getId().equals(itemId))
                    .findAny() 
                    .map(cartItem -> {
                        cartItem.increment();
                        return Mono.just(cart).log("newCartItem");
                    }) 
                    .orElseGet(() -> {
                        return this.itemRepository.findById(itemId) 
                                .log("fetchedItem")  //
                                .map(item -> new CartItem(item)) 
                                .log("cartItem") //
                                .map(cartItem -> {
                                    cart.getCartItems().add(cartItem);
                                    return cart;
                                }).log("addedCartItem"); //
                    }))
            .log("cartWithAnotherItem") //
            .flatMap(cart -> this.cartRepository.save(cart)) 
            .log("savedCart"); //
}
```
메서드 안에 log문이 여러 개 있는 것을 확인할 수 있는데 각기 다른 문자열을 포함하고 있습니다. 로그에는 메서드가 실행될 때 내부적으로 수행되는 일뿐만 아니라 리액티브 스트림 시그널 흐름도 모두 함께 출력됩니다. 기본적으로 INFO 수준으로 찍히고 두 번째 인자로 로그 LEVEL을 입력해줄 수 있습니다.  

#### 블록하운드를 사용한 블로킹 코드 검출
리액티브 프로그래밍을 잘 작성했더라도 누군가가 블로킹 API를 한 번 호출하면 아무 소용이 없게됩니다. 블로킹 소스가 없고 관련 설정도 적절하다는 것은 보장할 수 있는 것이 블록하운드입니다. 블록하운드는 개발자가 작성한 코드뿐만 아니라 서드파티 라이브러리에 사용된 블로킹 메서드 호출을 모두 찾아내서 알려주는 자바 에이전트입니다.  
```groovy
implementation 'io.projectreactor.tools:blockhound'
```
블록하운드 그 자체로는 아무 일도 하지 않지만 애플리케이션에 적절하게 설정되면 블로킹 메서드를 검출하고, 해당 스레드가 블로킹 메서드를 호출을 허용하는지를 검사할 수 있습니다. 스프링 부트 시작 수명주기에 블록하운드를 등록해봅시다.
```java
@SpringBootApplication
public class HackingSpringBootApplicationPlainBlockHound {

	public static void main(String[] args) {
		BlockHound.install();

		SpringApplication.run(HackingSpringBootApplicationPlainBlockHound.class, args);
	}
}
```
install 코드가 run보다 위에 있어야 블록하운드가 바이트코드를 조작할 수 있게 됩니다. 정상적으로 서버가 뜨지만 localhost:8080에 접속해보면 오류가 출력됩니다. 상세한 스택 트레이스에 블록하운드 관련 로그가 추가된 것을 확인할 수 있는데 블로킹 코드를 검출해서 보여주는 것입니다.  
<br>

블록하운드에는 여러 옵션이 있습니다. 특정 블로킹 호출은 문제로 하지 않도록 허용 리스트에 등록할 수 있고, 특정 부분을 블로킹으로 인지되도록 금지 리스트에 등록할 수도 있습니다. 
```java
@SpringBootApplication
public class HackingSpringBootApplicationBlockHoundCustomized {

	public static void main(String[] args) {
		BlockHound.builder() 
				.allowBlockingCallsInside( 
						TemplateEngine.class.getCanonicalName(), "process") // <1>
				.install(); 

		SpringApplication.run(HackingSpringBootApplicationBlockHoundCustomized.class, args);
	}
}
```
template 엔진의 process 메서드를 허용 리스트에 추가하는 코드입니다.  

## 스프링 부트 테스트
### 리액티브 단위 테스트
```java
@ExtendWith(MockitoExtension.class)
public class InventoryServiceTest {
    @InjectMocks InventoryService inventoryService;

    @Mock ItemRepository itemRepository;
    @Mock CartRepository cartRepository;

    @BeforeEach
    void setUp() {
        Item sampleItem = new Item("item1", "TV tray", "Alf TV tray", 19.99);
        CartItem sampleCartItem = new CartItem(sampleItem);
        Cart sampleCart = new Cart("My Cart", Collections.singletonList(sampleCartItem));

        when(cartRepository.findById(anyString())).thenReturn(Mono.empty());
        when(itemRepository.findById(anyString())).thenReturn(Mono.just(sampleItem));
        when(cartRepository.save(any(Cart.class))).thenReturn(Mono.just(sampleCart));

    }

    @Test
    void addItemToEmptyCartShouldProduceOneCartItem() { // <1>
        inventoryService.addItemToCart("My Cart", "item1") // <2>
                .as(StepVerifier::create) // <3>
                .expectNextMatches(cart -> { // <4>
                    assertThat(cart.getCartItems()).extracting(CartItem::getQuantity) // <5>
                            .containsExactlyInAnyOrder(1); 
                    assertThat(cart.getCartItems()).extracting(CartItem::getItem) 
                            .containsExactly(new Item("item1", "TV tray", "Alf TV tray", 19.99)); 

                    return true; // <6>
                }) //
                .verifyComplete(); // <7>
    }
}
```
1. 무엇을 테스트하는 메서드인지 명확하게 이름을 테스트 메서드에 사용합니다.
2. 테스트 메서드를 실행합니다.
3. 테스트 대상 메서드의 반환 타입인 Mono\<Cart>를 리액터 테스트 모듈의 정적 메서드인 StepVerifier.create()에 메서드 레퍼런스로 연결해서, 테스트 기능을 전담하는 리액터 타입 핸들러를 생성합니다.
4. expectNectMatches() 함수와 람다식을 사용하여 결과를 검증합니다.
5. 기존 assertJ 테스트와 똑같습니다.
6. expectNextMatches 메서드는 boolean을 반환해야 하므로 이 지점까지 통과했다면 true를 반환합니다.
7. 마지막 단언은 리액티브 스트림의 complete 시그널이 발생하고 리액터 플로우가 성공적으로 완료됐음을 검증합니다.

리액티브 코드를 테스트할 때 핵심은 기능만을 검사하는 게 아니라 리액티브 스트림 시그널도 함께 검사해야 합니다. 리액티브 스트림은 onSubcribe, onNext, onError, onComplete를 의미합니다. 위의 코드는 onNext, onComplete 를 모두 검사합니다.  
구독하기 전까지는 아무일도 일어나지 않는다는 점에서 테스트 코드도 누군가 구독해야 무슨 일이 일어나고 검증도 할 수 있게 됩니다. 여기 테스트에서 구독은 StepVerifier입니다. 결과값을 얻기 위해 블로킹 방식으로 기다리는 대신 리액터의 테스트 도구가 대신 구독을 하고 값을 확인할 수 있게 해주는 것입니다. 값을 검증할 수 있는 적절한 함수를 expectNextMatches에 람다식 인자로 전달해주고, verifyComplete를 호출해서 onComplete 시그널을 확인하면 의도한 대로 테스트가 동작했음이 보장됩니다.  


### 리액티브 통합 테스트
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient // <1>
public class LoadingWebSiteIntegrationTest {

    @Autowired WebTestClient client;

    @Test // <2>
    void test() {
        client.get().uri("/").exchange()
                .expectStatus().isOk() // 상태 검증
                .expectHeader().contentType(TEXT_HTML)  // 헤더 검증
                .expectBody(String.class)  // 바디 검증
                .consumeWith(exchangeResult -> {
                    assertThat(exchangeResult.getResponseBody()).contains("Welcome");
                });
    }
}
```
WebTestClient는 SpringMvc WebFlux에 추가된 rest 클라이언트 중 하나입니다. 기존 MockMvc는 동기 방식으로 동작하지만 WebTestClient는 비동기 방식으로 동작합니다. WebTestClient를 사용하기 위해서는 spring-boot-starter-webflux 의존성을 추가해야 합니다.  
1. 애플리케이션에 요청을 날리는 WebTestClient 인스턴스를 생성합니다.
2. WebTestClient를 사용해서 컨트롤러를 호출합니다. 

### 슬라이스 테스트
스프링 부트에는 다음과 같은 다양한 테스트 지원 기능이 준비돼 있습니다.
+ @AutoConfigureRestDocs
+ @DataJdbcTest
+ @DataJpaTest
+ @DataLdapTest
+ @DataMongoTest
+ @DataNeo4jTest
+ @DataRedisTest
+ @JdbcTest
+ @JooqTest
+ @JsonTest
+ @RestClientTest
+ @RestClientTest
+ @WebFluxTest
+ @WebMvcTest

위 애노테이션은 Junit 5의 @ExtendWith(SpringExtension.class) 애노테이션을 포함하고 있으므로 이를 추가하지 않아도 됩니다.  

```java
@DataMongoTest // <1>
public class MongoDbSliceTest {

	@Autowired
	ItemRepository repository; // <2>

	@Test // <3>
	void itemRepositorySavesItems() {
		Item sampleItem = new Item( //
				"name", "description", 1.99);

		repository.save(sampleItem) //
				.as(StepVerifier::create) //
				.expectNextMatches(item -> {
					assertThat(item.getId()).isNotNull();
					assertThat(item.getName()).isEqualTo("name");
					assertThat(item.getDescription()).isEqualTo("description");
					assertThat(item.getPrice()).isEqualTo(1.99);

					return true;
				}) //
				.verifyComplete();
	}
}
```
1. 스프링 부트 기능 중 스프링 데이터 몽고디비 활용에 초점을 둔 몽고디비 테스트 관련 기능을 활성화하며, @ExtendWith(SpringExtension.class)를 포함하고 있으므로 JUnit5 기능을 사용할 수 있습니다.
2. 빈을 주입받습니다.
3. ItemRepository와 리액터의 StepVerifier를 사용해서 테스트합니다.

몽고디비 슬라이스 테스트는 스프링 데이터 몽고디비 관련 모든 기능을 사용할 수 있게 하고 그 외 @Component 애노테이션이 붙어 있는 다른 빈 정의를 무시합니다.   
<br>

이번에는 슬라이스 웹플럭스 컨트롤러 테스트를 하나 해봅시다.
```java
@WebFluxTest(HomeController.class) // <1>
public class HomeControllerSliceTest {

	@Autowired 
	private WebTestClient client;

	@MockBean 
	InventoryService inventoryService;

	@Test
	void homePage() {
		when(inventoryService.getInventory()).thenReturn(Flux.just( //
				new Item("id1", "name1", "desc1", 1.99),
				new Item("id2", "name2", "desc2", 9.99) 
		));
		when(inventoryService.getCart("My Cart")) //
				.thenReturn(Mono.just(new Cart("My Cart")));

		client.get().uri("/").exchange() 
				.expectStatus().isOk() 
				.expectBody(String.class) 
				.consumeWith(exchangeResult -> {
					assertThat( 
							exchangeResult.getResponseBody()).contains("action=\"/add/id1\"");
					assertThat( 
							exchangeResult.getResponseBody()).contains("action=\"/add/id2\"");
				});
	}
}
```
1. @WebFluxTest(HomeController.class)는 이 테스트 케이스가 HomeController에 국한된 스프링 웹플럭스 슬라이스 테스트를 사용하도록 설정합니다.



### 블록하운드 단위 테스트

```groovy
testImplementation 'io.projectreactor.tools:blockhound'
```
앞서 언급했듯이 블록하운드는 테스트에서만 사용하는 것이 좋습니다.  
```java
class BlockHoundUnitTest {
	@Test
	void threadSleepIsABlockingCall() {
		Mono.delay(Duration.ofSeconds(1)) 
				.flatMap(tick -> {
					try {
						Thread.sleep(10); 
						return Mono.just(true);
					} catch (InterruptedException e) {
						return Mono.error(e);
					}
				}) //
				.as(StepVerifier::create) 
				.verifyComplete();
//				.verifyErrorMatches(throwable -> {
//					assertThat(throwable.getMessage()) //
//							.contains("Blocking call! java.lang.Thread.sleep");
//					return true;
//				});
    }
}
```
위 코드는 스레드 sleep으로 스레드를 멈추게 하는 블로킹 코드가 들어가 있습니다. 따라서 실행 시 오류가 납니다. 이처럼 블록하운드가 테스트 케이스에도 연동되어 블로킹 코드를 검출해내는 것을 알 수 있습니다. 주석 코드는 테스트를 통과시키는 방법입니다.  
블록하운드는 여러 가지를 검출하지만 주요한 것은 다음과 같습니다.
+ Thread#sleep
+ 여러 가지 Socket 및 네트워크 연산
+ 파일 접근 메소드 일부


<Br>

이제 실제 발생 사례를 봅시다.
```java
Mono<Cart> addItemToCart(String cartId, String itemId) {
    Cart myCart = this.cartRepository.findById(cartId) 
            .defaultIfEmpty(new Cart(cartId)) 
            .block(); // 블로킹

    return myCart.getCartItems().stream() 
            .filter(cartItem -> cartItem.getItem().getId().equals(itemId)) 
            .findAny() 
            .map(cartItem -> {
                cartItem.increment();
                return Mono.just(myCart);
            }) 
            .orElseGet(() -> this.itemRepository.findById(itemId) 
                    .map(item -> new CartItem(item)) 
                    .map(cartItem -> {
                        myCart.getCartItems().add(cartItem);
                        return myCart;
                    })) 
            .flatMap(cart -> this.cartRepository.save(cart));
}
```
앞서 계속 사용하던 예제와 같으나 중간에 block 메서드를 사용하여 값을 얻을 때까지 블로킹 방식으로 기다리는 코드입니다. 여기서 블로킹 지점을 테스트 코드로 찾아봅시다.  
```java
@ExtendWith(SpringExtension.class) // <1>
public class BlockHoundIntegrationTest {

	AltInventoryService inventoryService; 

	@MockBean ItemRepository itemRepository; 
	@MockBean CartRepository cartRepository;

	@BeforeEach
	void setUp() {
		Item sampleItem = new Item("item1", "TV tray", "Alf TV tray", 19.99);
		CartItem sampleCartItem = new CartItem(sampleItem);
		Cart sampleCart = new Cart("My Cart", Collections.singletonList(sampleCartItem));

		when(cartRepository.findById(anyString())) 
				.thenReturn(Mono.<Cart> empty().hide()); // <2>

		when(itemRepository.findById(anyString())).thenReturn(Mono.just(sampleItem));
		when(cartRepository.save(any(Cart.class))).thenReturn(Mono.just(sampleCart));

		inventoryService = new AltInventoryService(itemRepository, cartRepository);
	}

	@Test
	void blockHoundShouldTrapBlockingCall() { 
		Mono.delay(Duration.ofSeconds(1)) // <3>
				.flatMap(tick -> inventoryService.addItemToCart("My Cart", "item1")) // <4>
				.as(StepVerifier::create) // <5>
				.verifyErrorSatisfies(throwable -> { // <6>
					assertThat(throwable).hasMessageContaining( //
							"block()/blockFirst()/blockLast() are blocking");
				});
	}
}
```
1. 스프링 부트의 @MockBean 애노테이션을 사용하려면 JUnit 5의 확장인 SpringExtension을 등록해야 합니다.
2. 비어 있는 결과를 리액터로부터 감춥니다. Mono.empty()는 MonoEmpty 클래스의 싱글턴 객체를 반환합니다. 리액터는 이런 인스턴스를 감지하고 런타임에서 최적화하면서 block 호출을 없애버립니다. 따라서 블로킹 호출이 알아서 제거되는 문제를 hide를 통해 해결합니다.
3. 블로킹되지 않는다는 것을 블록하운드로 검증하려면 리액터 스레드 안에서 실행돼야 합니다. 따라서 Mono.delay를 사용해 후속 작업이 리액터 스레드 안에서 실행되도록 만듭니다.
4. tick 이벤트가 발생하면 테스트할 메서드를 실행합니다.
5. addItemCart 메서드가 반환하는 Mono를 리액터 StepVerifier로 전환합니다.
6. 블로킹 호출이 있으면 예외가 발생하여, 이 예외를 단언문으로 검증합니다.

여기서는 명시적인 블로킹 호출을 포함하고 있으므로 예외가 발생하고, 예외가 발생할 것을 예상하여 verifyErrorSatisfies를 호출해서 발생한 예외릐 메시지를 단언하는 테스트를 작성했지만, 일반적으로는 테스트 케이스는 블로킹 코드가 없다는 것을 검증하는 것이 목적이고, 실행 중 오류 없이 완료될 것을 예상하므로 verifyComplete를 호출해야 합니다. 즉, 블로킹 코드가 없음을 예상하는 상황을 가정하고 블로킹 코드가 포함돼 있다면 테스트 케이스는 실패하게 됩니다.  




<Br><Br>

__참고__  
<a href="https://github.com/onlybooks/spring-boot-reactive" target="_blank"> 스프링 부트 실전 활용 마스터</a>   
<a href="https://www.youtube.com/watch?v=I0zMm6wIbRI" target="_blank"> [[NHN FORWARD 2020] 내가 만든 WebFlux가 느렸던 이유</a>   











