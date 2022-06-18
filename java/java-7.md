# Java - Functional Interface와 Method Reference

## Fuction
---
```java
@FunctionalInterface
public interface Function<T, R> {
    R apply(T t);
}
```
Function 인터페이스는 apply 메서드 하나를 추상 메서드로 제공합니다.  

### 사용 방법 1: 구현 클래스 사용
```java
public class Adder implements Function<Integer, Integer> {

	@Override
	public Integer apply(Integer x) {
		return x + 10;
	}
}
---------------------------------------------------------
public static void main(String[] args) {
		Function<Integer, Integer> myAdder = new Adder();
		int result = myAdder.apply(5);
		System.out.println(result);
	}
```
인터페이스의 구현체를 선언하고 사용하는 방식입니다.

### 사용 방법 2: 람다식 사용
람다식을 사용하면 구현체를 선언하지 않고도 간편하게 사용할 수 있습니다.  
```java
public static void main(String[] args) {
		Function<Integer, Integer> myAdder = x -> x + 10;
		int result = myAdder.apply(5);
		System.out.println(result);
	}
```
제네릭으로 인해 타입을 유추할 수 있기 때문에 람다식에는 타입을 명시하고 않고 바로 변수명을 사용해도 됩니다.  
<br>

## BiFunction
---
```java
@FunctionalInterface
public interface BiFunction<T, U, R> {    
    R apply(T t, U u);
}
```
BiFunction은 파라미터를 T, U 타입의 파라미터를 받아서 R타입을 반환하는 추상메서드를 제공하는 인터페이스입니다.

### 예시
```java
public static void main(String[] args) {
		BiFunction<Integer, Integer, Integer> add = (x, y) -> x + y;
		int result = add.apply(3, 5);
		System.out.println(result);
	}
```
x, y를 받아서 덧셈 결과를 반환하는 람다식입니다.  
<br>

## Functional interface
---
Fuction와 BiFunction를 타고 들어가보면 인터페이스에 __@FunctionalInterface__ 가 붙어있습니다.  
이는 __단 하나의 abstract method(추상 메서드)만을 갖는 인터페이스__ 를 의미합니다.  
default 메서드와 static 메서드는 이미 구현되어 있으므로 해당 메서드는 존재해도 괜찮습니다.  
인자를 3개 받는 함수형 인터페이스는 선언되어 있지 않기 때문에 한 번 예시로 만들어보겠습니다.
```java
@FunctionalInterface
public interface TriFunction<T, U, V, R> {
	R apply(T t, U u, V v);
}
------------------------------------------------------------------------------
public static void main(String[] args) {
	TriFunction<Integer, Integer, Integer, Integer> addThreeNumbers =
			(x, y, z) -> x + y + z;
	int result = addThreeNumbers.apply(3, 2, 5);
	System.out.println(result);
}
```
위와 같이 간단하게 만들어두고 사용할 수 있습니다.

<br>

## Supplier
---
```java
@FunctionalInterface
public interface Supplier<T> {
    T get();
}

```
Supplier는 공급하는 인터페이스로 input 없이 리턴값만 갖는 추상 메서드를 갖고 있습니다.  


### 예시
```java
public class SupplierTest {
	public static void main(String[] args) {
		Supplier<Double> myRandomDoubleSupplier = () -> Math.random();
		printRandomDoubles(myRandomDoubleSupplier, 5);
	}
	
	public static void printRandomDoubles(Supplier<Double> randomSupplier, int count) {
		for (int i = 0; i < count; i ++) {
			System.out.println(randomSupplier.get());
		}
	}
} 
```
myStringSupplier의 get 함수가 랜덤값을 반환하도록 람다식으로 정의했습니다.  
이 함수형 인터페이스를 함수의 인자로 넘겨서 5번 호출하는 예시입니다.  
결과적으로 랜덤값이 5번 찍히게 됩니다.  
<br>

## Consumer
---
```java
@FunctionalInterface
public interface Consumer<T> {
    void accept(T t);
}
```
Consumer 인터페이스는 값을 받아서 처리하고 아무것도 리턴하지 않는 추상메서드를 갖고 있습니다.

### 예시
```java
public class ConsumerTest {
	public static void main(String[] args) {
		
		List<Integer> integerInputs = Arrays.asList(4, 2, 3);
		Consumer<Integer> myIntegerProcessor = x -> 
			System.out.println("Processing integer " + x);
		process(integerInputs, myIntegerProcessor);
	}
	
	public static <T> void process(List<T> inputs, Consumer<T> processor) {
		for (T input : inputs) {
			processor.accept(input);
		}
	}
}
```
process 함수에 인자로 리스트와 Consumer를 넣어주고 Consumer의 accept 함수를 호출해서 콘솔에 찍는 예시입니다.
<br>

## BiConsumer
---
```java
@FunctionalInterface
public interface BiConsumer<T, U> {
    void accept(T t, U u);
}
```
BiConsumer는 2개의 인자를 받아서 처리하고 아무것도 리턴하지 않는 추상메서드를 갖고 있습니다.  

### 예시
```java
public class BiConsumerTest {
	public static void main(String[] args) {
		BiConsumer<Integer, Double> myDoubleProcessor = 
				(index, input) -> 
					System.out.println("Processing " + input + " at index " + index);
		List<Double> inputs = Arrays.asList(1.1, 2.2, 3.3);	
		process(inputs, myDoubleProcessor);
	}
	
	public static <T> void process(List<T> inputs, BiConsumer<Integer, T> processor) {
		for (int i = 0; i< inputs.size(); i++) {
			processor.accept(i, inputs.get(i));
		}
	}
}
```
process 함수에 인자로 리스트와 BiConsumer를 넣어주고 BiConsumer의 accept 함수를 호출해서 콘솔에 찍는 예시입니다.  

<br>

## Predicate
---
```java
@FunctionalInterface
public interface Predicate<T> {
    boolean test(T t);
}
```
Predicate 인터페이스는 하나의 인자를 받아서 처리하고 boolean을 반환하는 추상 메서드를 하나 갖고 있습니다.  

### 예시
```java
public class Chapter4Section4 {

	public static void main(String[] args) {
		Predicate<Integer> isPositive = x -> x > 0;
		System.out.println(isPositive.test(-10)); // false
		
		List<Integer> inputs = Arrays.asList(10, -5, 4, -2, 0, 3);
		System.out.println("Positive number: " + filter(inputs, isPositive));
		System.out.println("Non-positive number: " + filter(inputs, isPositive.negate()));
		System.out.println("Non-negative number: " 
				+ filter(inputs, isPositive.or(x -> x == 0)));
		System.out.println("Positive even numbers: " 
				+ filter(inputs, isPositive.and(x -> x % 2 == 0)));
	}
	
	public static <T> List<T> filter(List<T> inputs, Predicate<T> condition) {
		List<T> output = new ArrayList<>();
		for (T input : inputs) {
			if (condition.test(input)) {
				output.add(input);
			}
		}
		return output;
	}
}

```
filter 함수는 인풋 중에서 통과한 것들만 걸러서 반환하는 함수로 만들었습니다.  
거르는 것에 대한 판단은 두 번째 인자로 받는 Predicate가 처리합니다.  
Predicate 인터페이스에는 default 함수가 여러 가지 있는데 몇가지만 살펴보면 다음과 같습니다.  

+ default Predicate\<T> or(Predicate<? super T> other) 
    - or API는 인자로 다른 Predicate을 받아서 둘 중에 하나라도 만족하면 true를 반환하는 새로운 Predicate을 반환해줍니다.
+ default Predicate\<T> and(Predicate<? super T> other)
    - and API는 인자로 다른 Predicate을 받아서 둘다 만족해야만 true를 반환하는 새로운 Predicate을 반환해줍니다.
+ default Predicate\<T> negate()
    - negate는 현재 만든 Predicate의 정반대의 Predicate을 반환해줍니다.

위 예시에서도 보았듯이 결국에는 하나의 filter 함수를 만들었지만 여러개의 Predicate을 제공함으로써 다양한 방식으로 활용할 수 있습니다.  
<br>

## Operator
---
+ UnaryOperator\<T>
    - Function인데 인풋과 리턴타입이 같은 경우에 사용합니다.    
+ BinaryOperator\<T>
    - 인풋 타입 2개와 리턴타입이 같은 경우에 사용합니다.

<br>

## Primitive Type Functional Interface
---
java.util.function 패키지에 보면 인터페이스명에 Primitive 타입이 붙은 것들이 존재합니다.  
Function같이 제네릭 타입으로 다 처리할 수 있는데 굳이 이렇게 Primitive 타입이 붙은 것들을 제공하는 이유는 제네릭의 경우는 박싱타입을 사용해야하므로 메모리를 더 잡아먹기 때문입니다.  
따라서 메모리를 아끼면서 사용할 수 있게끔 Primitive 타입을 붙인 인터페이스를 제공합니다.  

<br>

## Comparator
---
```java
@FunctionalInterface
public interface Comparator<T> {   
    int compare(T o1, T o2);
}
```
앞서 소개한 함수형 인터페이스는 java.util.function 패키지에 있지만, Comparator는 java.util 패키지 안에 있는 함수형 인터페이스입니다.  
인수 2개를 받아서 비교하는 int를 반환하는 compare 추상 메서드를 제공합니다.  
보통 비교하여 정렬하는 과정이 필요할 때 사용합니다.
+ 음수면 o1 < o2
+ 0이면 o1 = o2
+ 양수이면 o1 > o2

### 예시
```java
public class ComparatorTest {

	public static void main(String[] args) {
		List<User> users = new ArrayList<>();
		users.add(new User(3, "Alice"));
		users.add(new User(1, "Charlie"));
		users.add(new User(5, "Bob"));
		System.out.println(users);
		
		Comparator<User> idComparator = (u1, u2) -> Integer.compare(u1.getId(),u2.getId());
		Collections.sort(users, idComparator);
		System.out.println(users);
		
		Collections.sort(users, (u1, u2) -> u1.getName().compareTo(u2.getName()));
		System.out.println(users);
	}

}
```
Collections.sort의 두 번째 인자 또는 Arrays.sort의 두 번째 인자로 넣어서 사용할 수 있습니다.  
박싱타입의 compare를 사용하면 둘을 비교해서 첫 번째 인자가 크면 양수, 작으면 음수, 같으면 0을 반환해줍니다.  
오름차순으로 정렬하고 싶다면 순서를 바꿔주면 됩니다.  
<br>

## Method Reference
---
+ 기존에 이미 선언되어있는 메서드를 지정하고 싶을 때 사용합니다.
+ :: 오퍼레이터를 사용합니다.
+ 생략이 많기 때문에 사용할 메서드의 매개변수의 타입과 리턴 타입을 미리 숙지해야 합니다.

### 4가지 사용 케이스
+ className::staticMethodName 
	- 클래스의 static method를 지정할 때 사용합니다.
+ objectName::instanceMethodName
	- 선언 된 객체의 instance method를 지정할 때 사용합니다.

```java
public class MethodReferenceTest {
	public static int calculate(int x, int y, BiFunction<Integer, Integer, Integer> operator) {
		return operator.apply(x, y);
	}
	
	public static int multiply(int x, int y) {
		return x * y;
	}
	
	public int subtract(int x, int y) {
		return x - y;
	}
	
	public void myMethod() {
		System.out.println(calculate(10, 3, this::subtract));
	}
	
	
	public static void main(String[] args) {
		int a = Integer.parseInt("15");
		Function<String, Integer> str2int = Integer::parseInt; // 첫 번째 방식
		System.out.println(str2int.apply("20"));
		
		String str = "hello";
		boolean b = str.equals("world");
		Predicate<String> equalsToHello = str::equals; // 두 번째 방식
		System.out.println(equalsToHello.test("world"));
		
		System.out.println(calculate(8, 2, (x, y) -> x + y)); // BiFunction 람다식 이용하기
		System.out.println(calculate(8, 2, MethodReferenceTest::multiply)); // BiFunction method Reference 첫 번째 방식 이용하기
		
		MethodReferenceTest instance = new MethodReferenceTest();
		System.out.println(calculate(8, 2, instance::subtract)); // BiFunction method Reference 두 번째 방식 이용하기
		instance.myMethod();
	}

}
```

<br>

+ className::instanceMethodName
	- 객체의 instance method를 지정할 때 사용합니다.  


```java
public class MethodReferenceTest {

	public static void main(String[] args) {
		Function<String, Integer> strLength = String::length; // 세 번째 방식 -> 앞선 두 번째 방식은 인스턴스::메서드 지만 이것은 클래스타입::메서드
		int length = strLength.apply("hello world");
		System.out.println(length);
		
		BiPredicate<String, String> strEquals = String::equals; // 세 번째 방식 클래스타입:: 메서드
		boolean helloEqualsWorld = strEquals.test("hello", "world");
		System.out.println(strEquals.test("hello", "hello"));
		
		List<User> users = new ArrayList<>();
		users.add(new User(3, "Alice"));
		users.add(new User(1, "Charlie"));
		users.add(new User(5, "Bob"));
		
		printUserField(users, User::getName);
	}

	public static void printUserField(List<User> users, Function<User, Object> getter) {
		for (User user : users) {
			System.out.println(getter.apply(user));
		}
	}
}
```

<br>

+ className::new
	- 클래스의 constructor를 지정할 때 사용합니다.

```java
public class MethodReferenceTest {

	public static void main(String[] args) {		
		BiFunction<Integer, String, User> userCreator = User::new; // 4번째 방식 사용
		User backtony = userCreator.apply(1, "backtony");
	}
}

----------------------------------------------------------------------------
// 이런식으로도 사용할 수 있습니다.
Map<String, BiFunction<String, String, Car>> carTypeToConstructorMap = new HashMap<>();
carTypeToConstructorMap.put("sedan", Sedan::new);
carTypeToConstructorMap.put("suv", Suv::new);
carTypeToConstructorMap.put("van", Van::new);
```




<Br><Br>

__참고__  
<a href="https://github.com/haedalfamily/fastcampus-stream" target="_blank"> Java Function Programming & Stream Lectur</a>   


