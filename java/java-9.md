# Java - 함수형 프로그래밍의 응용


## Scope
---
Scope는 변수에 접근할 수 있는 범위를 의미합니다.  
함수 안에 함수가 있을 때 내부 함수에서 외부 함수에 있는 변수에 접근이 가능하지만(lexical scope) 그 반대는 불가능합니다.  
```java
public class ScopeTest {

	public static void main(String[] args) {
		Supplier<String> supplier = getStringSupplier();
		System.out.println(supplier.get());
	}

	public static Supplier<String> getStringSupplier() {
		String hello = "Hello";
		Supplier<String> supplier = () -> {
			String world = "World";
			return hello + world;
		};
		
		return supplier;
	}
}
```
hello는 supplier 안에서도 접근 가능하지만, world 변수는 supplier 바깥에서는 접근할 수 없습니다.  
일반적으로 메서드는 호출이 끝나면 메서드 안에서 선언된 변수들을 소멸됩니다.  
하지만 리턴된 supplier는 getStringSupplier에 선언된 hello 변수를 여전히 필요로 합니다.  
그렇다면 getStringSupplier를 호출하여 반환된 supplier를 호출하면 어떻게 될까요?  
정답은 hello 변수는 여전히 존재하여 문제없이 작동합니다.  
내부 함수가 존재하는 한 내부 함수가 사용한 외부 함수의 변수들 역시 계속 존재하게 되는 것입니다.  
이렇게 lexical scope를 포함하는 함수를 __Closure__ 라고 합니다.  
__이때 내부 함수가 사용한 외부 함수의 변수들은 내부 함수 선언 당시로부터 변할 수 없기 때문에 final로 선언되지 않더라도 암묵적으로 final로 취급됩니다.__  

## Curry
---
Curry는 여러 개의 매개변수를 받는 함수를 중첩된 여러 개의 함수로 쪼개어 매개 변수를 한 번에 받지 않고 여러 단계에 걸쳐 나눠 받을 수 있게 하는 기술로 Closure의 응용입니다.  

```java	
public class ScopeTest {

	public static void main(String[] args) {
		
		// 기존 BiFunction 사용 방식 -> 두 인자를 받아서 결과 반환
		BiFunction<Integer, Integer, Integer> add = (x, y) -> x + y;

		// x를 받고 y를 받고 최종적으로 x+y를 반환
		//         (1)               (2)      (3)                 (1)  (2)    (3)
		Function<Integer, Function<Integer, Integer>> curriedAdd = x -> y -> x + y;
		
		Function<Integer, Integer> addThree = curriedAdd.apply(3); // x 입력
		int result = addThree.apply(10); // y 입력 -> x+y 반환
		System.out.println(result);
	}

}
```
만약 BiFunction에서 인자 2개를 한 번에 받지 않고, x를 먼저 받고 나중에 필요할 때 y를 받아오는 식으로 쪼갤 수 있습니다.  
처음 Integer를 받고 이 함수는 function\<Integer, Integer> 을 리턴합니다.  
두번째 함수가 Integer를 마저 받아 최종적으로 리턴합니다.  
<br>

## Lazy Evaluation
---
__Lambda의 계산은 그 결과값이 필요할 때가 되어서야 계산됩니다.__  
이를 이용하여 불필요한 계산을 줄이거나 해당 코드의 실행 순서를 의도적으로 미룰 수 있습니다.  
```java
public class LazyEvaluationTest {

	public static void main(String[] args) {
		if (returnTrue() || returnFalse()) { // ---- 1
			System.out.println("true");
		}

		if (returnFalse() && returnTrue()) { // ---- 2
			System.out.println("true");
		}
		
		if (or(returnTrue(), returnFalse())) { // ---- 3
			System.out.println("true");
		}	
	}

	public static boolean returnTrue() {
		System.out.println("Returning true");
		return true;
	}
	
	public static boolean returnFalse() {
		System.out.println("Returning false");
		return false;
	}
	
	public static boolean or(boolean x, boolean y) {
		return x || y;
	}
}
```
1. 첫 번째 인지가 True이므로 returnFalse는 호출되지 않습니다.
2. 첫 번째 인자가 False이므로 returnTrue는 호출되지 않습니다.  
3. returnTrue()와 returnFalse()이 모두 호출되고 연산이 진행됩니다.

즉, 1, 2번은 최적화가 되어있는데 3번은 최적화가 되어있지 않습니다.  
이유는 or 메서드 호출 전에 값들을 모두 알고 나서야 메서드를 호출하기 때문입니다.  
만약 returnTrue, returnFalse가 굉장히 무거운 계산이었다면 당연히 한번만 연산해서 처리할 수 있다면 더 나을 것입니다.  
이는 람다의 Lazy Evaluation 을 통해 개선할 수 있습니다.  
```java
public class LazyEvaluationTest {

	public static void main(String[] args) {
	
		// 람다식은 Lazy Evalution이므로 필요할 때가 되서야 연산이 진행됩니다.	
		if (lazyOr(() -> returnTrue(), () -> returnFalse())) {
			System.out.println("true");
		}	
	}
	
	public static boolean lazyOr(Supplier<Boolean> x, Supplier<Boolean> y) {
		return x.get() || y.get();
	}

	public static boolean returnTrue() {
		System.out.println("Returning true");
		return true;
	}
	
	public static boolean returnFalse() {
		System.out.println("Returning false");
		return false;
	}
}
```
lazyOr의 인자로 Supplier 함수형 인터페이스를 받습니다.  
즉, 함수의 인자를 람다식으로 줄 수 있습니다.  
앞서 설명했듯이 람다식은 필요할 때서야 실행됩니다.  
즉, lazyOr함수를 보면 x.get()을 하는 시점에서야 람다 연산이 진행됩니다.  
x.get()이 먼저 수행되고 true를 반환했다면 ||연산은 이미 true를 반환했기 때문에 y.get()은 호출되지 않습니다.  

<br>

이 내용은 Stream에서도 적용됩니다.
```java
public class LazyEvaluationTest {

	public static void main(String[] args) {
		Stream<Integer> integerStream = Stream.of(3, -2, 5, 8, -3, 10)
				.filter(x -> x > 0)
				.peek(x -> System.out.println("peeking " + x)) // 잠깐 consume 하는 용도 peek
				.filter(x -> x % 2 ==0);
		System.out.println("Before collect");
		
		List<Integer> integers = integerStream.collect(Collectors.toList());
		System.out.println("After collect: " + integers);
	}
}
```
일반적인 생각으로 코드를 위에서 아래로 읽으면 peeking이 다 찍히고 그다음 Before collect가 찍힐 것 같지만 그렇지 않습니다.  
Stream은 종결 처리가 되기 직전까지 작업을 미루다가 종결 처리가 되는 시점에 작업을 수행합니다.  
따라서 Before collect가 가장 먼저 찍히고 peeking이 찍히고 After collect가 찍힙니다.
<br>

## Function Composition
---
Function Composition은 __여러 개의 함수를 합쳐 하나의 새로운 함수를 만드는 것을 의미__ 합니다.  
```java
@FunctionalInterface
public interface Function<T, R> {
	...
	default <V> Function<V, R> compose(Function<? super V, ? extends T> before)
	default <V> Function<T, V> andThen(Function<? super R, ? extends V> after)
	...
}
```
+ compose
	- 인자로 넘어온 Function을 먼저 실행하고 그 다음 자기 자신을 실행하도록 합성합니다.
	- 일반적인 인지 순서와 거꾸로 되기 때문에 자연스럽게 읽히기 위해서 보통 andThen을 사용합니다.
+ andThen
	- 자신을 먼저 실행하고 인자로 넘어온 Function을 실행하도록 합성합니다.

### 예시 1
```java
public class FunctionCompositionTest {
	public static void main(String[] args) {
		Function<Integer, Integer> multiplyByTwo = x -> 2 * x;
		Function<Integer, Integer> addTen = x -> x + 10;

		Function<Integer, Integer> composedFunction = multiplyByTwo.andThen(addTen);
		System.out.println(composedFunction.apply(3)); // 16
	}
}
```

### 예시2
주문서의 주문 내역을 전부 더하고 그 금액에 세금을 붙여서 최종 금액을 만드는 예시를 만들어 보겠습니다.
```java

public class FunctionCompositionTest {
	public static void main(String[] args) {
		// 주문서 생성
		Order unprocessedOrder = new Order()
				.setId(1001L)
				.setOrderLines(Arrays.asList(
						new OrderLine().setAmount(BigDecimal.valueOf(1000)),
						new OrderLine().setAmount(BigDecimal.valueOf(2000))));
		
		// 주문 내역 전부 더하는 Function함수와 Tax를 붙이는 Function함수를 리스트로 반환
		List<Function<Order, Order>> priceProcessors = getPriceProcessors(unprocessedOrder);
		
		// 두 Function 함수를 andThen으로 엮어주기 -> 주문 내역을 전부 더하고 Tax를 적용한다.
		Function<Order, Order> mergedPriceProcessors = priceProcessors.stream()
				// 초기값으로는 order를 받아서 order를 바로 Function이 생성되고 이 뒤로 andThen으로 Function이 붙습니다.			
				.reduce(Function.identity(), Function::andThen);
		// 즉 이런 형태입니다.
		// Function<Order, Order> identity = Function.identity();
		// Function<Order, Order> objectOrderFunction
		//  		    = identity.andThen(new OrderLineAggregationPriceProcessor()).andThen(new TaxPriceProcessor(new BigDecimal(100)));
		
		// order를 넘기면 쭉 연산되서 적용된 Order가 나옵니다.
		Order processedOrder = mergedPriceProcessors.apply(unprocessedOrder);
		System.out.println(processedOrder);
	}
	
	public static List<Function<Order, Order>> getPriceProcessors(Order order) {
		return Arrays.asList(new OrderLineAggregationPriceProcessor(), 
				new TaxPriceProcessor(new BigDecimal("9.375")));
	}
}
------------------------------------------------------------------------------------------------------------------
public class OrderLineAggregationPriceProcessor implements Function<Order, Order> {

	@Override
	public Order apply(Order order) {
		return order.setAmount(order.getOrderLines().stream()
				.map(OrderLine::getAmount)
				.reduce(BigDecimal.ZERO, BigDecimal::add));
	}
}
------------------------------------------------------------------------------------------------------------------
public class TaxPriceProcessor implements Function<Order, Order>{

	private final BigDecimal taxRate;
	
	public TaxPriceProcessor(BigDecimal taxRate) {
		this.taxRate = taxRate;
	}
	
	@Override
	public Order apply(Order order) {
		return order.setAmount(order.getAmount()
				.multiply(taxRate.divide(new BigDecimal(100)).add(BigDecimal.ONE)));
	}

}
```


<Br><Br>

__참고__  
<a href="https://github.com/haedalfamily/fastcampus-stream" target="_blank"> Java Function Programming & Stream Lectur</a>   




