# Java - Stream과 Optional


## 스트림이란?
---
+ 데이터의 흐름을 만들어 주는 것입니다.
+ 컬렉션 형태로 구성된 데이터를 람다를 이용해 간결하고 직관적으로 프로세스 할 수 있게 합니다.
+ For, while 등을 이용하던 기존 loop를 대체합니다.
+ 손쉽게 병렬 처리할 수 있게 도와줍니다.
+ 일회성으로 한 번 사용하고 나면 사용할 수 없습니다.


### 스트림 생성
```java
public class CreateStreamTest {

	public static void main(String[] args) {
		Stream<String> nameStream = Stream.of("Alice", "Bob", "Charlie");
		List<String> names = nameStream.collect(Collectors.toList());
		System.out.println(names);
		
		String[] cityArray = new String[] {"San Jose", "Seoul", "Tokyo" };
		Stream<String> cityStream = Arrays.stream(cityArray);
		List<String> cityList = cityStream.collect(Collectors.toList());
		System.out.println(cityList);
		
		Set<Integer> numberSet = new HashSet<>(Arrays.asList(3, 5, 7));
		Stream<Integer> numberStream = numberSet.stream();
		List<Integer> numberList = numberStream.collect(Collectors.toList());
		System.out.println(numberList);
	}

}
```
위와 같이 3가지 방식으로 Stream을 생성할 수 있습니다.
+ Stream.of()
+ Arrays.stream()
+ Collection.stream()

주로 Collections.stream() 으로 사용합니다.  
주의해야 할 점은 Stream을 일회용이기 때문에 사용하고 난 뒤에는 다시 사용할 수 없으므로 다시 사용하고자 한다면 Stream을 다시 만들어야 합니다.

### Filter
```java
Stream<T> filter(Predicate<? super T> predicate);
```
Stream의 Filter API는 Predicate에서 true를 반환하는 데이터만 존재하는 Stream을 리턴하는 기능을 제공합니다.  

#### 예시
```java
public class FilterTest {
	public static void main(String[] args) {
		
		System.out.println(filteredNumbers);
		
		List<Integer> newFilteredNumbers = Stream.of(3, -5, 7, 10, -3)
			.filter(x -> x > 0) // 양수만 뽑아서 스트림 반환
			.collect(Collectors.toList()); // 스트림을 리스트로 변환
		System.out.println(newFilteredNumbers);
		
		User user1 = new User()
				.setId(101)
				.setName("user1")
				.setVerified(true)
				.setEmailAddress("user1@gmail.com");
		User user2 = new User()
				.setId(102)
				.setName("user2")
				.setVerified(false)
				.setEmailAddress("user1@gmail.com");
		User user3 = new User()
				.setId(103)
				.setName("user3")
				.setVerified(true)
				.setEmailAddress("user3@gmail.com");
		
		List<User> users = Arrays.asList(user1, user2, user3);
		List<User> verifiedUsers = users.stream()
			.filter(User::isVerified) // User 클래스의 isVerified 함수 Method Reference 방식으로 호출, isVerified가 true를 반환하는 User만 뽑아서 Stream으로 반환
			.collect(Collectors.toList());
		System.out.println(verifiedUsers);
	}
}
```

### Map
```java
<R> Stream<R> map(Function<? super T, ? extends R> mapper);
```
Stream의 map API는 stream 흐름에 있는 데이터들을 변형해서 변형된 결과물을 stream으로 반환해주는 기능을 제공합니다.


#### 예시
```java
public class StreamMapTest {
	public static void main(String[] args) {
		List<Integer> numberList = Arrays.asList(3, 6, -4);
		List<Integer> numberListX2 = numberList.stream()
				.map(x -> x * 2) // stream 데이터들을 2배하여 stream으로 반환
				.collect(Collectors.toList()); // stream 데이터를 리스트로 반환
		System.out.println(numberListX2);
		
		User user1 = new User()
				.setId(101)
				.setName("user1")
				.setVerified(true)
				.setEmailAddress("user1@gmail.com");
		User user2 = new User()
				.setId(102)
				.setName("user2")
				.setVerified(false)
				.setEmailAddress("user1@gmail.com");
		User user3 = new User()
				.setId(103)
				.setName("user3")
				.setVerified(true)
				.setEmailAddress("user3@gmail.com");

		List<User> users = Arrays.asList(user1, user2, user3);
		List<String> emailAddresses = users.stream()
				.map(User::getEmailAddress) // user의 emailAddress만 뽑아서 stream으로 반환
				.collect(Collectors.toList()); // stream 데이터를 리스트로 반환
		System.out.println(emailAddresses);
	}
}
```

### Stream Pipline

![그림1](https://github.com/backtony/blog-code/blob/master/java/img/8/java-46-1.PNG?raw=true)  

스트림은 3부분으로 구성됩니다.  
+ Source
	- 스트림의 시작을 알려주는 부분입니다.
	- 앞서 컬렉션이나 배열 등으로 stream을 만들었던 부분을 의미합니다.
+ Intermediate Operations(중간 처리)
	- filter나 map 같이 가공하는 중간 과정을 의미합니다.
	- __여러 가지의 중간 처리를 이어붙이는 것이 가능합니다.__
+ Terminal Operation(종결 처리)
	- 지금까지는 Collect로 스트림 데이터를 리스트로 받아오는 과정만 해봤는데 이 과정을 의미합니다.


#### 예시
```java
public class StreamPiplineTest {

	public static void main(String[] args) {
		User user1 = new User()
				.setId(101)
				.setName("user1")
				.setVerified(true)
				.setEmailAddress("user1@gmail.com");
		User user2 = new User()
				.setId(102)
				.setName("user2")
				.setVerified(false)
				.setEmailAddress("user1@gmail.com");
		User user3 = new User()
				.setId(103)
				.setName("user3")
				.setVerified(true)
				.setEmailAddress("user3@gmail.com");
		List<User> users = Arrays.asList(user1, user2, user3);
		
		List<String> emails2 = users.stream()
				.filter(user -> !user.isVerified()) // 중간 과정 1
				.map(User::getEmailAddress) // 중간 과정 2
				.collect(Collectors.toList());
		System.out.println(emails2);
	}
}
```
위와 같이 중간 과정을 이어 붙여서 수행할 수 있습니다.  

### sorted
```java
Stream<T> sorted();
Stream<T> sorted(Comparator<? super T> comparator);
```
데이터가 순서대로 정렬된 Stream을 리턴하는 기능을 제공합니다.  
데이터의 종류가 기본적으로 서로 비교할 수 없다면 어떤 식으로 비교할지를 알려주는 comparator을 인자로 제공해야 합니다.  

#### 예시
```java
public class StreamSortedTest {

	public static void main(String[] args) {
		List<Integer> numbers = Arrays.asList(3, -5, 7, 4);
		List<Integer> sortedNumbers = numbers.stream()
				.sorted() // stream 데이터(숫자)를 정렬한 stream으로 반환
				.collect(Collectors.toList());
		System.out.println(sortedNumbers);
		
		User user1 = new User()
				.setId(101)
				.setName("user1")
				.setVerified(true)
				.setEmailAddress("user1@gmail.com");
		User user2 = new User()
				.setId(102)
				.setName("user2")
				.setVerified(false)
				.setEmailAddress("user1@gmail.com");
		User user3 = new User()
				.setId(103)
				.setName("user3")
				.setVerified(true)
				.setEmailAddress("user3@gmail.com");

		List<User> users = Arrays.asList(user1, user2, user3);
		List<User> sortedUsers = users.stream()
				// user는 그냥 sort할수 없으므로 comparator을 제공해야 합니다.
				// 이는 람다식으로 간단하게 제공할 수 있습니다.
				// 아래 코드는 오름차순 방식이고 내림차순을 원한다면 u1과 u2의 순서를 바꾸면 됩니다.
				.sorted((u1, u2) -> u1.getName().compareTo(u2.getName())) 
				.collect(Collectors.toList());
		System.out.println(sortedUsers);
	}
}
```
주석만으로도 설명이 충분할 것 같습니다.  

### distinct
```java
Stream<T> distinct();
```
distinct는 stream 데이터 중에서 __중복이 제거된__ stream을 반환해주는 기능을 제공합니다.  

#### 예시
```java
public class StreamDistinctTest {

	public static void main(String[] args) {
		List<Integer> numbers = Arrays.asList(3, -5, 4, -5, 2, 3);
		List<Integer> distinctNumbers = numbers.stream()
				.distinct() // 중복된 정수 제거한 스트림을 반환
				.collect(Collectors.toList());
		System.out.println(distinctNumbers);
		
		LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
		Order order1 = new Order()
				.setId(1001)
				.setStatus(OrderStatus.CREATED)
				.setCreatedByUserId(101)
				.setCreatedAt(now.minusHours(4));
		Order order2 = new Order()
				.setId(1004)
				.setStatus(OrderStatus.ERROR)
				.setCreatedByUserId(104)
				.setCreatedAt(now.minusHours(40));
		Order order3 = new Order()
				.setId(1005)
				.setStatus(OrderStatus.IN_PROGRESS)
				.setCreatedByUserId(101)
				.setCreatedAt(now.minusHours(10));
		List<Order> orders = Arrays.asList(order1, order2, order3);
		
		// 중복되지 않는 CreateByUserId만 뽑아서 정렬하기
		List<Long> createByUerIdList = orders.stream()
				.map(Order::getCreatedByUserId)
				.distinct() // Map으로 뽑은 CreateByUserId 스트림 데이터 중에서 중복을 제거한 스트림을 반환
				.sorted() // 정렬한 스트림을 반환
				.collect(Collectors.toList());
	}
}
```
주석만으로도 설명이 충분할 것 같습니다.  

### FlatMap
```java
<R> Stream<R> flatMap(Function<? super T, ? extends Stream<? extends R>> mapper);
```
Stream 안에 있는 데이터 자체도 Stream일 경우 즉, Stream 안에 Stream(중첩)된 경우 이것을 하나의 Stream으로 풀어서 반환해주는 기능을 제공합니다.  
인자로는 Map의 기능을 하도록 함수를 제공하면 되는데 보통 컬렉션이 담겨 있으면 해당 컬렉션을 스트림으로 바꾸는 작업을 인자로 넘기고 해당 컬렉션을 스트림으로 만들었으니 Stream\<Stream\<XXX>\> 형태가 된 것을 Flat이 하나의 Stream으로(Stream\<XXX>) 만들어 반환해준다고 생각하면 됩니다.  

#### 예시 1
```java
public class StreamFlatMapTest {

	public static void main(String[] args) {
		String[][] cities = new String[][] {
			{ "Seoul", "Busan" },
			{ "San Francisco", "New York" },
			{ "Madrid", "Barcelona" }
		};
		// bad
		Stream<String[]> cityStream = Arrays.stream(cities);
		Stream<Stream<String>> cityStreamStream = cityStream.map(x -> Arrays.stream(x));
		List<Stream<String>> cityStreamList = cityStreamStream.collect(Collectors.toList());
		
		// good
		Stream<String[]> cityStream2 = Arrays.stream(cities);
		Stream<String> flattenedCityStream = cityStream2.flatMap(x -> Arrays.stream(x));
		List<String> flattenedCityList = flattenedCityStream.collect(Collectors.toList());
		System.out.println(flattenedCityList);
	}
}
```
bad 예시를 보면 List안에 스트림이 들어있습니다.  
이는 map에서 Stream을 만들었기 때문인데, map은 Stream을 반환하는데 그 안에 데이터도 Stream으로 만들었기 때문입니다.  
good 예시를 보면 정상적으로 원하는 List를 뽑아냈습니다.  
flatMap에서도 bad처럼 x를 Stream으로 가공하지만 flatMap은 이 Stream들을 하나의 Stream으로 풀어낸 Stream을 반환합니다.

#### 예시 2
조금더 실용적인 예시를 보겠습니다.  
```java
public class StreamFlatMapTest {

	public static void main(String[] args) {
		
		Order order1 = new Order()
				.setId(1001)
				.setOrderLines(Arrays.asList(
						new OrderLine()
							.setId(10001)
							.setType(OrderLineType.PURCHASE)
							.setAmount(BigDecimal.valueOf(5000)),
						new OrderLine()
							.setId(10002)
							.setType(OrderLineType.PURCHASE)
							.setAmount(BigDecimal.valueOf(4000))
				));
		Order order2 = new Order()
				.setId(1002)
				.setOrderLines(Arrays.asList(
						new OrderLine()
							.setId(10003)
							.setType(OrderLineType.PURCHASE)
							.setAmount(BigDecimal.valueOf(2000)),
						new OrderLine()
							.setId(10004)
							.setType(OrderLineType.DISCOUNT)
							.setAmount(BigDecimal.valueOf(-1000))
				));
		
		List<Order> orders = Arrays.asList(order1, order2);
		List<OrderLine> mergedOrderLines = orders.stream() 	// Stream<Order>
				.map(Order::getOrderLines) // OrderLine만 뽑아서 스트림 반환 // Stream<List<OrderLine>>
				.flatMap(List::stream) 	// List<OrderLine>을 Stream으로 변경하고 flat을 통해 하나의 Stream으로 반환 // Stream<OrderLine>
				.collect(Collectors.toList());
		System.out.println(mergedOrderLines);
	}

}
```
order1과 order2는 각각 orderLines 필드를 리스트로 갖으며 리스트 안에 2개의 데이터가 들어가 있습니다.  
stream을 이용해 두 개의 리스트 안에 있는 2개의 원소 즉, 4개의 데이터를 하나의 리스트로 뽑아내는 예시입니다.

<Br>

## Optional
---
NullPointerException은 Null 상태인 오브젝트를 레퍼런스 할 때 발생합니다.  
Runtime error이기 때문에 실행 전 까지는 발생 여부를 알기 쉽지 않습니다.  
이런 문제를 해결하기 위해 등장한 것이 Optional입니다.  
__Optional은 Null일수도, 아닐 수도 있는 오브젝트를 담은 상자입니다.__  

### Optional 생성
```java
public static <T> Optional<T> of(T value)
public static<T> Optional<T> empty()
public static <T> Optional<T> ofNullable(T value)
```
+ of
	- Null이 아닌 오브젝트를 이용해 Optional을 만들 때 사용합니다.
	- 만약 Null을 넘기게 되면 에러가 납니다.
+ empty
	- 빈 Optional 상자를 만들 때 사용합니다.
+ ofNullable
	- Null을 허용하는 빈 상자라는 의미로 객체가 Null인지 아닌지 알지 못하는 오브젝트로 Optional을 만들 때 사용합니다.

### 안에 있는 값을 확인하고 꺼내는 법
```java
public boolean isPresent()
public T get()
public T orElse(T other)
public T orElseGet(Supplier<? extends T> supplier)
public <X extends Throwable> T orElseThrow(Supplier<? extends X> exceptionSupplier) throws X
```
+ isPresent
	- 안의 오브젝트가 null인지 아닌지 체크하여 해당 여부를 boolean으로 반환합니다.
+ get
	- Optional 안의 값을 추출합니다.
	- Null이라면 에러가 발생합니다.
+ orElse
	- Optional이 null이 아니라면 Optional 안의 값을 반환합니다.
	- null 이라면 other로 공급된 값을 반환합니다.
+ orElseGet
	- Optional이 null이 아니라면 Optional 안의 값을 반환합니다.
	- null이라면 supplier로 공급되는 값을 반환합니다.
+ orElseThrow
	- Optional이 null이 아니라면 Optional 안의 값을 반환합니다.
	- null이라면 exceptionSupplier로 공급되는 exception을 던집니다.


#### 예시
```java
public class OptionalNormalTest {
    public static void main(String[] args) {
        String someEmail = "some@email.com";
        String nullEmail = null;

        // Optional 생성
        Optional<String> maybeEmail = Optional.of(someEmail);
        Optional<String> maybeEmail2 = Optional.empty();
        Optional<String> maybeEmail3 = Optional.ofNullable(someEmail);
        Optional<String> maybeEmail4 = Optional.ofNullable(nullEmail);

        // Optional 안의 값 꺼내기
        String email = maybeEmail.get();
        System.out.println(email);

        // Optional 안의 값이 존재하는지 확인
        if (maybeEmail2.isPresent()) {
            System.out.println(maybeEmail2.get());
        }

        String defaultEmail = "default@email.com";
        String email3 = maybeEmail2.orElse(defaultEmail); // 안의 값이 null이면 defaultEmail를 반환
        System.out.println(email3);

        String email4 = maybeEmail2.orElseGet(() -> defaultEmail); // 안의 값이 null이면 Supplier에서 공급되는 값을 반환
        System.out.println(email4);

        // 안의 값이 null이면 예외 던짐
        String email5 = maybeEmail2.orElseThrow(() -> new RuntimeException("email not present"));
    }
}
```

### Optional 응용
```java
public void ifPresent(Consumer<? super T> action)
public <U> Optional<U> map(Function<? super T, ? extends U> mapper)
public <U> Optional<U> flatMap(Function<? super T, ? extends Optional<? extends U>> mapper)
```
+ ifPresent
	- Optional이 null이 아니라면 action을 실행합니다.
	- 인자인 action은 Consumer 함수형 인터페이스인데 __반환 타입이 void__ 이므로 사용 시 고려해야 합니다.
+ map
	- Optional안에 값이 존재한다면 mapper를 적용합니다.
	- mapper의 반환 타입에 따라서 Optional의 타입도 변경됩니다. 
+ flatMap
	- Stream의 flatMap과 비슷한 기능을 제공합니다.
	- Optional 안에 Optional이 존재한다면 이것들 하나의 Optional로 flat하게 만들어주는 기능을 제공합니다.


#### 예시
```java
public class OptionalAdvancedTest {
	public static void main(String[] args) {
		Optional<User> maybeUser = Optional.ofNullable(maybeGetUser(true));
		// Optional안에 값이 존재한다면 출력
		maybeUser.ifPresent(user -> System.out.println(user));
		
		// Optional 안에 값이 존재한다면 map으로 가공한 값이 Optional 안에 담깁니다.
		Optional<Integer> maybeId = Optional.ofNullable(maybeGetUser(true))
			.map(user -> user.getId());
		// Optional 안에 값이 존재한다면 출력
		maybeId.ifPresent(System.out::println);
		
		// Optional 안에 값이 null이기 때문에 map이 동작하지 않습니다.
		Optional<Integer> maybeId2 = Optional.ofNullable(maybeGetUser(false))
				.map(user -> user.getId());
		// Optional 안에 값이 null이기 때문에 동작하지 않습니다.
		maybeId2.ifPresent(System.out::println);
		
		// Optional 안의 값이 null이기 때문에 orElse가 동작합니다.
		String userName = Optional.ofNullable(maybeGetUser(false))
			.map(User::getName)
			.map(name -> "The name is " + name)
			.orElse("Name is empty");
		System.out.println(userName);



// 		User의 Email 반환값
//		public Optional<String> getEmailAddress() {
//			return Optional.ofNullable(emailAddress);
//		}
		
		// getEmailAddress가 Optional을 반환하기 때문에 Optional<Optional<String>>을 FlatMap이 Optional<String>으로 만들어 줍니다.
		Optional<String> maybeEmail = Optional.ofNullable(maybeGetUser(true))
			.flatMap(User::getEmailAddress);
		maybeEmail.ifPresent(System.out::println);
	}
	
	public static User maybeGetUser(boolean returnUser) {
		if (returnUser) {
			return new User()
					  .setId(1001)
					  .setName("user")
					  .setEmailAddress("user@gmail.com")
					  .setVerified(false);
		}
		return null;
	}
}
```
<br>

![그림1](https://github.com/backtony/blog-code/blob/master/java/img/8/java-46-1.PNG?raw=true)  

+ 종결 처리는 Stream 안의 데이터를 모아 반환하는 역할을 합니다.
+ 중간 처리 작업은 바로바로 실행되는 것이 아니라 종결 처리의 실행이 필요할 때서야 비로소 중간 처리가 실행됩니다.(Lazy Evaluation)


### max, min, count
```java
Optional<T> max(Comparator<? super T> comparator);
Optional<T> min(Comparator<? super T> comparator);
long count();
```
+ max
	- Stream 안의 데이터 중 최대값을 반환합니다.
	- Stream이 비어있다면 빈 Optional을 반환합니다.
+ min
	- Stream 안의 데이터 중 최소값을 반환합니다.
	- Stream이 비어있다면 빈 Optional을 반환합니다.
+ count
	- Stream 안의 데이터 개수를 반환합니다.

#### 예시
```java

public class StreamMaxMinCountTest {

	public static void main(String[] args) {
		Optional<Integer> max = Stream.of(5, 3, 6, 2, 1)
			.max(Integer::compareTo); // 최대값 추출
		System.out.println(max.get());

		User user1 = new User()
				.setId(101)
				.setName("user1")
				.setVerified(true)
				.setEmailAddress("user1@gmail.com");
		User user2 = new User()
				.setId(102)
				.setName("user2")
				.setVerified(false)
				.setEmailAddress("user1@gmail.com");
		
		List<User> users = Arrays.asList(user1, user2);

	    User firstUser = users.stream()
	    		.min((u1, u2) -> u1.getName().compareTo(u2.getName())) // 최소값 추출
	    		.get();
	    System.out.println(firstUser);
	    
	    long positiveIntegerCount = Stream.of(1, -4, 5, -3, 6)
	    	.filter(x -> x > 0) // 양수인 스트림 제공
	    	.count(); // 개수 추출
	    System.out.println("Positive integers: " + positiveIntegerCount);
	}
}
```

### All Match, Any Match
```java
boolean allMatch(Predicate<? super T> predicate);
boolean anyMatch(Predicate<? super T> predicate);
```
+ allMatch
	- Stream 안의 모든 데이터가 predicate을 만족하면 true를 반환합니다.
+ anyMatch
	- Stream 안의 데이터 중 하나라도 predicate을 만족하면 true를 반환합니다.


#### 예시
```java
public class StreamAllMatchAnyMatchTest {

	public static void main(String[] args) {
		List<Integer> numbers = Arrays.asList(3, -4, 2, 7, 9);
		// 모두 양수이면 true
		boolean allPostive = numbers.stream()
				.allMatch(number -> number > 0);
		System.out.println("Are all numbers positive: " + allPostive);

		// 하나라도 음수이면 true
		boolean anyNegative = numbers.stream()
				.anyMatch(number -> number < 0);
		System.out.println("Is any number negative: " + anyNegative);

		User user1 = new User()
				.setId(101)
				.setName("user1")
				.setVerified(true)
				.setEmailAddress("user1@gmail.com");
		User user2 = new User()
				.setId(102)
				.setName("user2")
				.setVerified(false)
				.setEmailAddress("user1@gmail.com");

		List<User> users = Arrays.asList(user1, user2);

		// 모든 유저가 이메일 인증이 되어있다면 true
		boolean areAllUserVerified = users.stream()
				.allMatch(User::isVerified);
		System.out.println(areAllUserVerified);
	}
}
```

### findFirst, findAny
```java
Optional<T> findFirst();
Optional<T> findAny();
```
+ findFirst
	- Stream 안의 첫번째 데이터를 반환합니다.
	- Stream이 비어있다면 빈 Optional을 반환합니다.
+ findAny
	- Stream 안의 아무 데이터나 리턴합니다.(매번 달라지기 때문에 순서가 필요 없을 때 사용합니다.)
	- Parallel Stream을 사용할 때 최적화를 할 수 있습니다.
	- Stream이 비어있다면 빈 Optional을 반환합니다.


#### 예시
```java
public class StreamFindFirstFindAnyTest {

	public static void main(String[] args) {
		Optional<Integer> anyNegativeInteger = Stream.of(3, 2, -5, 6)
			.filter(x -> x < 0) // 음수만 필터링
			.findAny(); // 음수 중 아무거나 반환
		System.out.println(anyNegativeInteger.get());
		
		Optional<Integer> firstPositiveInteger = Stream.of(-3, -2, -5, 6)
			.filter(x -> x > 0) // 양수만 필터링
			.findFirst(); // 첫번째를 반환
		System.out.println(firstPositiveInteger.get());
	}
}
```
![그림2](https://github.com/backtony/blog-code/blob/master/java/img/8/java-46-2.PNG?raw=true)  
```java
Optional<T> reduce(BinaryOperator<T> accumulator);
T reduce(T identity, BinaryOperator<T> accumulator);
<U> U reduce(U identity, BiFunction<U, ? super T, U> accumulator, BinaryOperator<U> combiner);
```
주어진 함수를 반복 적용해 Stream 안의 데이터를 하나의 값으로 합치는 기능을 제공합니다.  
max, min, count도 reduce의 일종입니다.  
+ Optional\<T> reduce(BinaryOperato\r<T> accumulator);
	- 주어진 accumulator를 이용해 Stream의 데이터를 합칩니다.
	- 비어있을 경우 빈 Optional을 반환합니다.
+ T reduce(T identity, BinaryOperator\<T> accumulator);	
	- 초기값에 acumulator 연산 방식으로 stream 데이터를 합칩니다.
	- 초기값이 있기 때문에 항상 반환값이 존재합니다.
+ \<U> U reduce(U identity, BiFunction\<U, ? super T, U> accumulator, BinaryOperator\<U> combiner);
	- 합치는 과정에서 타입이 바뀔 경우 사용합니다.
	- U타입의 초기값과 T타입의 스트림 안에 데이터를 합쳐서 U타입의 결과물을 반환합니다.
	- 풀어서 설명하면 identity에는 초기값, accumulator에는 identity는 T타입이고 stream는 T타입이므로 둘을 어떻게 합칠지에 대한 함수, combiner에는 stream 값들 끼리에 대한 연산 함수를 명시합니다.
	- 하지만 보통 Map을 이용해 U 타입의 데이터로 바꾸고 그 후 reduce하는 것으로 대체할 수 있기 때문에 자주 사용하지는 않습니다.



#### 예시
```java
public class StreamReduceTest {
    public static void main(String[] args) {
        List<Integer> numbers = Arrays.asList(1, 4, -2, -5, 3);
        int sum = numbers.stream()
                .reduce((x, y) -> x + y) // stream의 데이터를 모두 더해서 반환
                .get();
        System.out.println(sum);

        int min = numbers.stream()
                .reduce((x, y) -> x < y ? x : y) // stream의 데이터 중 가장 작은 데이터 반환 -> min과 동일
                .get();
        System.out.println(min);


        int product = numbers.stream()
                .reduce(1, (x, y) -> x * y); // 초기값 1을 시작으로 stream 데이터들을 곱하기 연산
        // 초기값이 존재하기 때문에 무조건 값이 존재하여 반환값이 optional이 아닙니다.
        System.out.println(product);

        List<String> numberStrList = Arrays.asList("3", "2", "5", "-4");
        int sumOfNumberStrList = numberStrList.stream()
                .map(Integer::parseInt) // 문자열 스트림 데이터를 정수로 변환시키기
                .reduce(0, (x, y) -> x + y); // 0을 시작으로 모두 더해서 반환
        System.out.println(sumOfNumberStrList);

        int sumOfNumberStrList2 = numberStrList.stream()
                // 초기값은 integer 0, stream 데이터는 String이므로 초기값과 합치는 방식 명시, stream 데이터 연산 함수 명시
                .reduce(0, (number, str) -> number + Integer.parseInt(str), (num1, num2) -> num1 + num2);
        System.out.println(sumOfNumberStrList2);


        User user1 = new User()
                .setFriendUserIds(Arrays.asList(201, 202, 203, 204));
        User user2 = new User()
                .setFriendUserIds(Arrays.asList(204, 205, 206));
        User user3 = new User()
                .setFriendUserIds(Arrays.asList(204, 205, 207));
        List<User> users = Arrays.asList(user1, user2, user3);
        int sumOfNumberOfFriends = users.stream()
                .map(User::getFriendUserIds) // FriendUserId 리스트를 뽑아내기
                .map(List::size) // 리스트의 사이즈만 뽑아내기
                .reduce(0,  (x, y) -> x + y); // 모두 더하기
        System.out.println(sumOfNumberOfFriends);

    }
}
```

### Collectors
스트림의 결과값들을 컬렉션으로 만드는 방식입니다.  

```java
public class StreamCollectorsTest {
	public static void main(String[] args) {
		List<Integer> numberList = Stream.of(3, 5, -3, 3, 4, 5)
				.collect(Collectors.toList()); // 리스트로 변환
		System.out.println(numberList);
		
		Set<Integer> numberSet = Stream.of(3, 5, -3, 3, 4, 5)
				.collect(Collectors.toSet()); // Set으로 변환
		System.out.println(numberSet);

		List<Integer> numberList2 = Stream.of(3, 5, -3, 3, 4, 5)
				// map이 우선적으로 적용되고 결과값들을 리스트로 반환
				.collect(Collectors.mapping(x -> Math.abs(x), Collectors.toList()));
		System.out.println(numberList2);

		Set<Integer> numberSet2 = Stream.of(3, 5, -3, 3, 4, 5)
				// map이 우선적으로 적용되고 결과값들을 set으로 반환
				.collect(Collectors.mapping(x -> Math.abs(x), Collectors.toSet()));
		System.out.println(numberSet2);
		
		int sum = Stream.of(3, 5, -3, 3, 4, 5)
				// Stream의 값들을 reduce 해서 반환
				.collect(Collectors.reducing(0, (x, y) -> x + y));
		System.out.println(sum);
	}
}
```
![그림3](https://github.com/backtony/blog-code/blob/master/java/img/8/java-46-3.PNG?raw=true)  
```java
public static <T, K, U>
    Collector<T, ?, Map<K,U>> toMap(Function<? super T, ? extends K> keyMapper,
                                    Function<? super T, ? extends U> valueMapper)
```
+ Stream 안의 데이터를 map의 형태로 반환해주는 collector 입니다.
+ KeyMapper는 데이터를 map의 key로 변환하는 Function을 의미합니다.
+ valueMapper는 데이터를 map의 value로 변환하는 Function을 의미합니다.

#### 예시
```java
public class StreamToMapTest {

	public static void main(String[] args) {
		Map<Integer, String> numberMap1 = Stream.of(3, 5, -4, 2, 6)
				// keyMapper는 값을 그대로 키로, valueMapper는 Number is x 값으로 저장
				.collect(Collectors.toMap(x -> x, x -> "Number is " + x));
		System.out.println(numberMap1.get(3));

		Map<Integer, String> numberMap2 = Stream.of(3, 5, -4, 2, 6)
				// x -> x 같이 간단한 경우 Function에서 identity 메서드로 이미 제공하고 있어서 이를 이용해도 됩니다.
				.collect(Collectors.toMap(Function.identity(), x -> "Number is " + x));
		System.out.println(numberMap2.get(3));

		User user1 = new User()
				.setId(101)
				.setName("user1")
				.setVerified(true)
				.setEmailAddress("user1@gmail.com");
		User user2 = new User()
				.setId(102)
				.setName("user2")
				.setVerified(false)
				.setEmailAddress("user1@gmail.com");
		
	    List<User> users = Arrays.asList(user1, user2);
	    Map<Integer, User> userIdToUserMap = users.stream()
				// 키는 userId로, value는 User 엔티티 자체를 사용
	    		.collect(Collectors.toMap(User::getId, Function.identity()));
	    System.out.println(userIdToUserMap);
	}

}
```

### groupingBy
```java
public static <T, K> Collector<T, ?, Map<K, List<T>>>
    groupingBy(Function<? super T, ? extends K> classifier)
```
+ Stream 안의 데이터에 classifier를 적용했을 때 결과값이 같은 값끼리 List로 모아서 Map의 형태로 반환해주는 Collector 입니다.
+ 이때 키는 classifier의 결과값, value는 그 결과값을 같는 데이터들 입니다.

#### 예시
```java
public class StreamGroupingByTest {
	public static void main(String[] args) {
		List<Integer> numbers = Arrays.asList(13, 2, 101, 203, 304, 402, 305, 349, 2312, 203);
		Map<Integer, List<Integer>> unitDigitMap = numbers.stream()
				// 10으로 나눈 나머지가 같은 것들끼리 List로 만들고, 키는 나머지값, value는 나머지 값이 일치하는 데이터로 만든 리스트
				.collect(Collectors.groupingBy(number -> number % 10));
		System.out.println(unitDigitMap);
		
		Map<Integer, Set<Integer>> unitDigitSet = numbers.stream()
				// value값을 리스트가 아닌 Set 컬렉션으로 만들기
				.collect(Collectors.groupingBy(number -> number % 10, Collectors.toSet()));
		System.out.println(unitDigitSet);
		
		Map<Integer, List<String>> unitDigitStrMap = numbers.stream()
				.collect(Collectors.groupingBy(number -> number % 10,
						// value값을 데이터 그대로 넣지 않고 mapping으로 가공한 뒤 해당 데이터를 리스트로 만들어 value로 저장
						Collectors.mapping(number -> "unit digit is " + number, Collectors.toList())));
		System.out.println(unitDigitStrMap.get(3));


		Order order1 = new Order()
				.setId(1001L)
				.setAmount(BigDecimal.valueOf(2000))
				.setStatus(OrderStatus.CREATED);
	    Order order2 = new Order()
	    		.setId(1002L)
	    		.setAmount(BigDecimal.valueOf(4000))
	    		.setStatus(OrderStatus.ERROR);
	    Order order3 = new Order()
	    		.setId(1003L)
	    		.setAmount(BigDecimal.valueOf(3000))
	    		.setStatus(OrderStatus.ERROR);
	    Order order4 = new Order()
	    		.setId(1004L)
	    		.setAmount(BigDecimal.valueOf(7000))
	    		.setStatus(OrderStatus.PROCESSED);
	    List<Order> orders = Arrays.asList(order1, order2, order3, order4);

		// OrderStatus를 key로 하여 데이터 묶어보기
	    Map<OrderStatus, List<Order>> orderStatusMap 
				// Default가 리스트로 묶이기 때문에 valueMapper는 생략해도 됩니다.
				= orders.stream().collect(Collectors.groupingBy(Order::getStatus));
	}
}
```

### partitioningBy
```java
public static <T>
    Collector<T, ?, Map<Boolean, List<T>>> partitioningBy(Predicate<? super T> predicate) 

public static <T, D, A>
    Collector<T, ?, Map<Boolean, D>> partitioningBy(Predicate<? super T> predicate,
                                                    Collector<? super T, A, D> downstream)
```
+ GroupingBy와 유사하지만 Function 대신 Predicate을 받아 true와 false 두 key가 존재하는 map을 반환하는 collector 입니다.
+ downstream collector을 넘겨 List 이외의 다른 형태로 map의 value를 만들 수 있습니다.

#### 예시
```java
public class StreamPartitioningByTest {
	public static void main(String[] args) {
		List<Integer> numbers = Arrays.asList(13, 2, 101, 203, 304, 402, 305, 349, 2312, 203);
		// 짝수는 true 키로 저장, 홀수는 false를 키로 저장
		Map<Boolean, List<Integer>> numberPartitions = numbers.stream()
				.collect(Collectors.partitioningBy(number -> number % 2 == 0));
		System.out.println("Even number: " + numberPartitions.get(true));
		System.out.println("Odd number: " + numberPartitions.get(false));
		
		User user1 = new User()
				.setId(101)
				.setName("user1")
				.setEmailAddress("user1@gmail.com")
				.setFriendUserIds(Arrays.asList(201, 202, 203, 204, 211, 212, 213, 214));
	    User user2 = new User()
	    		.setId(102)
	    		.setName("user2")
	    		.setEmailAddress("user2@gmail.com")
	    		.setFriendUserIds(Arrays.asList(204, 205, 206));
	   
	    List<User> users = Arrays.asList(user1, user2);
	    
	    // friendUserIds가 5개보다 많은 User는 true에, 아니면 false 키로 저장
	    Map<Boolean, List<User>> userPartitions = users.stream()
	    		.collect(Collectors.partitioningBy(user -> user.getFriendUserIds().size() > 5));
	    
	    // 활용 : true인 유저의 친구들에게 이메일 보내기 
	    for (User user: userPartitions.get(true)) {
	    	emailService.sendPlayWithFriendsEmail(user);
	    }
	}
}
```

### forEach
```java
void forEach(Consumer<? super T> action);
```
+ 제공된 action을 Stream의 각 데이터에 적용해주는 종결 처리 메서드입니다.
+ Java의 iterable 인터페이스에도 forEach가 있기 때문에 Stream의 중간 처리가 필요없다면 굳이 Stream을 만들어 사용할 필요는 없습니다.


#### 예시
```java
public class StreamForEachTest {

	public static void main(String[] args) {
		List<Integer> numbers = Arrays.asList(3, 5, 2, 1);
		// stream에 모든 데이터에 대한 처리
		numbers.stream().forEach(number -> System.out.println("The number is " + number));

		// 컬렉션에 기본적으로 forEach가 제공되기 때문에 중간처리가 필요 없다면 굳이 stream으로 만들어서 사용할 필요는 없습니다.
		numbers.forEach(number -> System.out.println("The number is " + number));


		User user1 = new User()
				.setId(101)
				.setName("user1")
				.setVerified(true)
				.setEmailAddress("user1@gmail.com");
		User user2 = new User()
				.setId(102)
				.setName("user2")
				.setVerified(false)
				.setEmailAddress("user1@gmail.com");
	    List<User> users = Arrays.asList(user1, user2);
	    
	    users.stream()
	    	.filter(user -> !user.isVerified()) // 이메일 인증 되지 않은 유저만 필터링
	    	.forEach(emailService::sendVerifyYourEmailEmail); // 필터링된 유저들에게 이메일 발송
	}
}
```

### Parallel Stream
```java
List<Integer> numbers = Arrays.asList(1,2,3);
Stream<Integer> parallelStream = numbers.parallelStream(); // 바로 병렬 스트림 만들기
Stream<Integer> parallelStream2 = numbers.stream().parallel(); // 스트림 만들고 병렬로 바꾸기
```
+ 여러 개의 스레드를 이용하여 stream 처리 과정을 병렬화합니다.
+ 중간 과정은 병렬 처리 되지만 순서가 있는 stream의 경우 종결 처리 했을 때의 결과물이 기존의 순차적 처리와 일치하도록 종결 처리과정에서 조정됩니다.
+ 예를 들어, List로 collect한다면 중간 처리 과정은 여러 스레드가 동작해서 순서가 바뀔 수 있지만 종결 처리 과정에서 순서가 조정된다는 의미입니다.
+ 장점
	- 굉장히 간단하게 병렬 처리를 사용할 수 있게 해줍니다.
	- 속도가 비약적으로 빨라질 수 있습니다.
+ 단점
	- 항상 속도가 빨라지는 것은 아닙니다.
	- 공통으로 사용하는 리소스가 있을 경우 잘못된 결과가 나오거나 아예 오류가 날 수 있습니다.(deadlock)
	- 이를 막기 위해 mutex, semaphore 등 병렬 처리 기술을 이용하면 순차 처리보다 느려질 수 있습니다.

#### 예시
```java
public class StreamParallelTest {

	public static void main(String[] args) {
		User user1 = new User()
				.setId(101)
				.setName("user1")
				.setVerified(true)
				.setEmailAddress("user1@gmail.com");
		User user2 = new User()
				.setId(102)
				.setName("user2")
				.setVerified(false)
				.setEmailAddress("user1@gmail.com");
		....
		
	    List<User> users = Arrays.asList(user1, user2, ...);
		
		// 이메일 인증 안된 유저에게 이메일 보내는 과정을 병렬 처리
	    users.stream().parallel()
	    	.filter(user -> !user.isVerified())
	    	.forEach(emailService::sendVerifyYourEmailEmail);
		
	    // user를 map으로 가공하는 작업을 병렬 처리
		// 출력되는 과정을 보면 순서가 섞여있는 것을 확인할 수 있습니다. -> 병렬처리
		// 하지만 최종 결과물은 순서가 보장됩니다.
	    List<User> processedUsers = users.parallelStream()
	    		.map(user -> {
	    			System.out.println("Capitalize user name for user " + user.getId());
	    			user.setName(user.getName().toUpperCase());
	    			return user;
	    		})
	    		.map(user -> {
	    			System.out.println("Set 'isVerified' to true for user " + user.getId());
	    			user.setVerified(true);
	    			return user;
	    		})
	    		.collect(Collectors.toList());
	    System.out.println(processedUsers);
	}
}
```

<Br><Br>

__참고__  
<a href="https://github.com/haedalfamily/fastcampus-stream" target="_blank"> Java Function Programming & Stream Lectur</a>   




