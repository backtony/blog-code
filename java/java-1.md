# Java - 제네릭

## 1. 제네릭
---
+ __클래스나 메서드에서 사용할 내부 데이터 타입을 외부에서 지정하는 기법__
+ 타입을 파라미터화 해서 컴파일 시 구체적인 타입이 결정되도록 하는 것
+ 타입 캐스팅으로 인한 자료형에 대한 검증은 컴파일 시 이뤄지지 않아 런타임에 오류가 발생할 수 있지만 제네릭 사용시 __컴파일 타임에 강한 타입 체크 가능__
+ __불필요한 타입 변환 제거__

```java
List list = new ArrayList();
list.add("hello"); // object 타입으로 저장
String str = (String) list.get(0);

List<String> list = new ArrayList(); // 스트링 타입으로 지정
list.add("hello"); // 스트링 타입으로 저장
String str = list.get(0);
``` 
add 시점에 String 타입으로 저장되는게 아니라 Object 타입으로 저장된다. 따라서 반환할 때도 Object 타입으로 리턴하기 때문에 String 타입으로 강제 타입 변환이 필요하다. 제네릭을 사용하면 강제 타입 과정을 줄일 수 있다.  
<br>

## 2. 제네릭 타입
---
```java
public class 클래스명<T>{...}
public interface 인터페이스명<T>{...}
```
+ 타입을 파라미터로 가지는 클래스와 인터페이스
+ 일반적으로 대문자 알파벳 한문자로 표현
    - E : Element를 의미하며 컬렉션에서 요소임을 나타냄
    - T : Type을 의미
    - V : Value를 의미
    - K : Key를 의미
    - N : Number를 의미
    - R : Result를 의미
+ 코드에서 타입 파라미터 자리에 구체적 타입을 지정

```java
// 해당 클래스에서 제네릭 T를 사용할 것임을 명시
public class Box<T>{
    private T t;
    public T get() {return t;}
    public void set(T t){ this.t = t;}
}

// T에 구체적 타입 지정
Box<String> box = new Box<>();


// 제네릭 클래스가 아닌 메서드만 제네릭으로 사용하기 - 제네릭 메서드
class Name{
    public <T> void printClassName(T t){...}
}

// 클래스에서 제네릭 T를 사용할 것을 명시했으나, 제네릭 메서드에서는 다른 T를 사용하고 싶을 경우
public Name<T>{
    // 메서드 앞에 다른 T를 사용하겠다고 명시 -> 이를 제네릭 메서드라고 함
    public <T> void printClassName(T t){..}
}    

Name<String> name = new Name<>(); // 클래스의 제네릭 타입은 String 이지만
name.printClassName(3.14); // 메서드에 타입을 Double로 넣었으므로 반환값은 double
name.printClassName(1); // 메서드에 타입을 Integer 로 넣었으므로 반환값은 Integer
```
제네릭 클래스가 아닌데 제네릭 메서드를 사용할 경우에는 반환타입 앞에 <T\> 와 같이 명시하면 된다.  
제네릭 클래스와 제네릭 메서드에 각각 제네릭 타입 매개변수가 정해져있다면 메서드에서는 메서드 타입으로 들어온 것을 따라간다.  

<br>

## 3. 멀티 타입 파라미터
---
```java
class <K,V,...> {...}
interface <K,V,...> {...}

// 예시
public class Product<T,M>{
    private T kind;
    private M model;
}

product<Tv,String> product = new Product<>();
```
두 개 이상의 타입 파라미터를 사용해서 선언할 수 있다.
<Br>

## 4. 제네릭 메서드
---
```java
public <타입파라미터,...> 리턴타입 메서드명(매개변수,...) {...}

// 예시
// 타입 파라미터에 T를 사용할 것이라고 앞에 기술해준 것이라 보면 된다.
public<T> box<T> boxing(T t) {...}

// 함수 호출
// 타입파라미터로 T를 추정한다.
Box<Integer> box = boxing(100); // T를 Integer로 추정 
```
+ __매개변수 타입과 리턴 타입으로 타입 파라미터를 갖는 메서드__
+ 제너릭 메서드 선언 방식
    - 리턴 타입 앞에 < > 기호를 추가하고 타입 파라미터 기술
    - 기술한 타입 파라미터는 리턴타입과 매개 변수에 사용 가능


### 정적 제네릭 메서드 주의사항
```java
public static void print(T param) {
    System.out.println(param.hello(0));
}
```
위 코드는 두 가지 오류가 있다.  
1. T타입이 무엇인지 알 수 없기 때문에 hello 메서드를 호출할 수 없다.  
2. 타입 파라미터에 대한 정의가 없다. 

정적(static) 메서드이므로 인스턴스에 관계없이 클래스에 붙어있는 메서드인데, 위 코드에서 사용하고 있는 T는 클래스에 표시하는 \<T>로 인스턴스마다 달라지는 타입 파라미터이다.  
즉, 정적 메서드에 인스턴스의 변수로 여겨지는 T를 타입 파라미터로 사용하고 있으므로 컴파일 에러가 난다.  
따라서 정적 제네릭 메서드는 아래와 같이 사용해야 한다.
```java
public static <T extends Course> void print(T param) {
    System.out.println(param.hello(0));
}
```



<br>

## 5. 제한된 타입 파라미터
---
```java
public <T extends 상위타입> 리턴타입 메서드(매개변수,...) {...}
```
+ 상속 및 구현 관계를 이용해서 타입을 제한하는 방식
+ 상위 타입 부분에는 __상위 클래스와 인터페이스를 지정__ 할 수 있다.
    - 상위 타입 자리에 클래스가 오게 되면 T에는 상위 타입 클래스나 하위 타입 클래스가 올 수 있다.
    - 상위 타입 자리에 인터페이스가 온다면 T에는 인터페이스의 구현체가 올 수 있다.
+ __실행 블록 안에서는 상위 타입의 필드와 메서드만 사용이 가능하다.__
    - 상위 타입에는 없으나 T 타입에는 존재하는 필드와 메서드는 실행 블록에서 사용할 수 없다.

<br>

## 6. 와일드카드 타입
---
+ 제한된 타입 파라미터와 마찬가지로 파라미터나 리턴타입에 제한을 걸 때 사용한다.
+ 3가지 형태
    - 제네릭타입< ? > : 제한 없음
        - 타입 파라미터를 대치하는 것으로 모든 클래스나 인터페이스 타입이 올 수 있다.
    - 제네릭타입< ? extends 상위타입> 
        - 상위 타입을 포함하여 하위 타입만 올 수 있다.
        - get한 원소는 상위타입이다. 이는 어떤 하위 타입인지 모르기 때문에 잘못된 타입 캐스팅을 막기 위함이다. 
    - 제네렉타입< ? super 하위타입> 
        - 하위타입을 포함하여 상위 타입만 올 수 있다.
        - get한 원소는 object로 나오게 된다.

```java
public class Calcu {
    public void printList(List<?> list) {
        for (Object obj : list) {
            System.out.println(obj + " ");
        }
        //list.add("hello") -> 불가능 -> ?이므로 어떤 타입일지 모르기 때문에 null을 제외한 어떤 것도 넣을 수 없음 
    }

    public int sum(List<? extends Number> list) {
        int sum = 0;
        for (Number i : list) {
            sum += i.doubleValue();
        }
        // list.add(a); ?가 무엇인지 알 수 없기 때문에 null을 제외한 아무것도 넣을 수 없음
        return sum;
    }

    public List<? super Integer> addList(List<? super Integer> list) {
        for (int i = 1; i < list.size(); i++) {
            list.add(i); // ?는 무조건 Integer를 포함하는 타입이기 때문에 Integer만 삽입 가능
        }
        return list;
    }
}
```

### 주의사항
```java
public class Main {
    public static void printList(List<Object> list) {
        list.forEach(System.out::println);
    }

    public static void main(String[] args) {
        List<Integer> numbers = Arrays.asList(1, 2, 3);
        printList(numbers); // 컴파일 에러 
    }
}
```
위 코드는 컴파일 에러가 발생한다.  
Object는 모든 클래스의 상위 타입이므로 Integer를 받을 수 있지 않느냐고 생각할 수 있지만, 그건 Object와 Interger의 관계이다.  
즉, List\<Object>와 List\<String>과는 전혀 관계 없는 이야기다.  
__따라서 List\<Object>는 List\<Integer>, List\<String>등 모든 타입 List의 상위 타입이 아니라는 점을 기억해야 한다.__

### 와일드 카드를 사용할 때와 사용하지 않을 때의 차이
---
```java
public <E extends People> void printList(List<E> list1, List<E> list2) {
    ...
}
```
와일드 카드를 사용하지 않으면 위와 같이 파라미터에 직접 extends로 제한을 줄 수 없다.  
또한, 두 개의 파라미터 List에는 같은 타입이 와야만 한다.
```java
public void printList(List<? extends Number> list, List<? extends String> list2) {
    ...
}
```
반면에 와일드 카드를 사용하면 파라미터에 직접 extends를 사용할 수 있고, 파라미터 마다 다른 타입을 받을 수 있다.

<br>

## 7. 제네릭 타입 상속과 구현
---
+ 제네릭 타입을 부모 클래스로 사용할 경우
    - 타입 파라미터는 자식 클래스에도 기술해야 한다.
    - 추가적인 타입 파라미터를 가질 수 있다.
+ 제네릭 인터페이스를 구현할 경우
    - 타입 파라미터는 구현 클래스에도 기술해야 한다.

```java
public class ChildProduct<T,M> extends Product<T,M> {...}
public class ChildProduct<T,M,C> extends Product<T,M> {...}

public class StorageImpl<T> implements Product<T> {...}
```

<br>

## 8. 제네릭을 사용할 수 없는 경우
---
### 제네릭을 배열을 생성하지 못하는 이유
#### 배열은 공변, 제네릭은 불공변
우선적으로 제네릭과 배열의 차이점을 이해해야 한다.  
배열의 경우 Sub가 Super의 하위 타입일때 Sub[]는 Super[]의 하위 타입이 된다.  
이런 경우를 __공변__ 하다고 한다.  
반면 제네릭 타입의 경우 앞서 언급했듯이 Sub가 Super의 하위 타입이더라도 ArrayList\<Sub>는 ArrayList\<Super>의 하위 타입이 아니다.  
이런 경우를 __불공변__ 하다고 한다.  
```java
Object[] objects = new String[1]; // 배열은 공변하므로 String[]은 Object[]의 하위 타입이므로 컴파일 가능
objects[0] = 1;
```
위 코드는 컴파일 시점에는 문제가 없다.  
배열은 공변하기 때문에 컴파일 시점에는 objects는 Object 타입의 배열이므로 Integer를 할당할 수 있다.  
하지만 런타임 시점에서 문제가 발생한다.  
런타임 시점에는 objects는 String 타입의 배열로 변환되기 때문이다.  
<br>

```java
ArrayList<Object> objectList = new ArrayList<String>(); // 제네릭 타입은 불공변하므로 컴파일 불가능
```
제네릭은 불공변하다.  
즉 ArrayList\<String> 는 ArrayList\<Object>의 하위 타입이 아니다.  
따라서 컴파일 자체가 불가능하다.

#### 배열은 런타임에 실체화, 제네릭 타입은 런타임에 소거
```java
Object[] objects = new String[1];
```
배열은 런타임에 타입이 실체화되기 때문에 objects는 런타임에 String[]가 된다.  
<br>

```java
// 컴파일 타임(실제 작성한 코드)
ArrayList<String> stringList = new ArrayList<String>();
ArrayList<Integer> integerList = new ArrayList<Integer>();

// 런타임(제네릭 타입은 런타임에 소거되므로 구분이 불가능하다)
ArrayList stringList = new ArrayList();
ArrayList integerList = new ArrayList();
```
제네릭 타입은 런타임에 소거되어 런타임 시점에는 타입이 소거된 ArrayList만 남게 된다.  
<br>

그렇다면 제네릭 배열이 가능하다면 어떤 문제가 발생하는지 확인해보자.
```java
// 실제론 컴파일 에러가 발생한다.
ArrayList<String>[] stringLists = new ArrayList<String>[1]; // 제네릭 배열을 생성. 런타임시에는 제네릭 타입은 소거되므로 ArrayList[]가 된다.

ArrayList<Integer> intList = Arrays.asList(1);              // 타입 소거로 인해 런타임시 ArrayList가 된다.
Object[] objects = stringLists;                             // 배열은 공변성을 가지므로 Object[]는 ArrayList[]가 될 수 있다.
objects[0] = intList;                                       // intList또한 ArrayList이므로 배열의 요소가 될 수 있다. 
String s = stringLists[0].get(0)  
```
애초에 위 코드는 컴파일 에러가 발생한다.  
하지만 만약 동작한다고 했을 때도 결국 런타임 에러가 발생한다.  
마지막 행헤서 String으로 꺼내고 있는데 실제로 값은 Integer이기 때문이다.  
정리하자면, __제네릭은 런타임 시에 소거되기 때문에 만약 제네릭 배열이 가능하다면 타입의 안전성을 보장할 수 없게 되는 것이다.__  
따라서 제네릭 타입의 목적은 타입의 안정성을 보장하기 위함이므로 제네릭 배열 자체를 금지하는 것이다.  
<br>


굳이 사용하고 싶다면 아래와 같이 Object 배열로 생성한 뒤에 제네릭으로 타입 캐스팅해서 사용할 수 있다.  
```java
public class Course<T> {

    private String name;
    private T[] students;

    public Course(String name,int capacity) {
        this.name = name;
        this.students = (T[]) (new Object[capacity]);
    }
}
```

### Static 변수에 사용 불가능
Static 변수는 생성되는 인스턴스에 무관하게 클래스에 붙어서 공유되어 사용된다.  
따라서 생성되는 인스턴스에 따라 타입이 바뀐다는 개념자체가 말이 안된다.  
그러므로 static 변수에는 제네릭을 사용할 수 없다.  




<br>

## 9. Type Erasure(타입 소거)
---
제네릭의 Type Erasure(타입 소거)는 컴파일 시 타입 체크를 해서 타입이 안 맞는 것을 잡아낸 후 컴파일 에러를 발생시키고 문제없이 컴파일 됐다면 런타임 중에는 타입 정보를 전부 버리는 것이다.  
위쪽에서 제네릭 배열을 선언할 수 없는 이유를 설명할 때 잠깐 설명했었는데 다른 예시를 들어 보자.  
```java
public class Person<T> {
    private T name;

    public Person(T name) {
        this.name = name;
    }

    public T getName() {
        return name;
    }
}
----------------------------
public class Main {
    public static void main(String[] args) {
        Person<String> person = new Person<>("backtony");
        String name = person.getName();
    }
}
```
위와 같은 코드를 돌리면 런타임 시점에는 아래와 같이 바뀐다.  
```java
public class Person {
    private Object name;

    public Person(Object name) {
        this.name = name;
    }

    public Object getName() {
        return name;
    }
}
-----------------------------------
public class Main {
    public static void main(String[] args) {
        Person person = new Person("backtony");
        String name = (String) person.getName();
    }
}
```
런타임 시점에는 제한이 걸려있지 않다면 제네릭이 전부 사라지고 그 자리에 Object가 들어간다.  
만약 제한을 걸어뒀다면 아래와 같이 바뀐다.
```java
public class Person<T extends Team> {
    private T name;

    public Person(T name) {
        this.name = name;
    }

    public T getName() {
        return name;
    }
}
------------------
public class Person {
    private Team name;

    public Person(Team name) {
        this.name = name;
    }

    public Team getName() {
        return name;
    }
}
```
이렇게 제네릭이 소거되는 이유는 호환성 때문이다.  
제네릭은 JDK 5부터 도입되었기에 기존의 코드를 모두 수용하면서 제네릭을 사용하는 새로운 코드와의 호환성을 유지해야 했다.  
따라서 런타임 시점에 제네릭을 소거하는 방식으로 진행된 것이다.  






<Br><Br>

__참고__  
<a href="https://pompitzz.github.io/blog/Java/whyCantCreateGenericsArray.html#%E1%84%8C%E1%85%A6%E1%84%82%E1%85%A6%E1%84%85%E1%85%B5%E1%86%A8%E1%84%80%E1%85%AA-%E1%84%87%E1%85%A2%E1%84%8B%E1%85%A7%E1%86%AF%E1%84%8B%E1%85%B4-%E1%84%8E%E1%85%A1%E1%84%8B%E1%85%B5%E1%84%8C%E1%85%A5%E1%86%B7" target="_blank"> JAVA 제네릭 배열을 생성하지 못하는 이유</a>   



