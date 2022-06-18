

## Java vs Python
---
+ Python
    - 인터프리터 언어로 한 줄씩 컴파일링 된다.
    - 데이터 타입이 동적으로 입력된다.
    - 문법 자체가 자바에 비해 가독성이 좋다.(print만 봐도 알 수 있다.)

+ Java
    - 컴파일링 언어로 한 번에 컴파일링 된다.
    - 정적인 데이터 타입 명시가 필요하다.
    - JVM으로 실행돼서 OS에 관계없이 동작한다.(운영체제 독립성)

## Java vs C
+ C
    - 컴파일 후 링크과정이 존재한다.
    - 개발자가 직접 메모리 관리를 한다.
+ Java 
    - 별도의 링크과정 없이 런타임에 필요한 클래스들이 JVM에 링크되어 클래스 로더가 동적으로 필요한 클래스 파일을 로딩한다.  
    - 메모리 관리를 GC가 대신한다.
    - 운영체제 독립성

## Kotlin 장점
+ 컴파일 타임에 null값에 대한 잘못된 접근을 감지 -> null 안전성
+ 간결한 코드 -> data class와 같이 보일러플레이트 코드를 줄일 수 있다.
+ 자바 호환성



<br>

## Java 장단점
---
+ 장점
    + 운영체제에 독립적
        - JVM에서 동작하기 때문에 플랫폼에 종속적이지 않다.
    + 객체지향 언어
        - 캡슐화(데이터들이 공개되지 않고 메서드로 외부로 공개), 상속, 추상화, 다형성 등을 지원하여 객체 지향 프로그래밍이 가능
    + 동적 로딩을 지원
        - 애플리케이션이 실행될 때 모든 객체가 생성되지 않고, 각 객체가 필요한 시점에 클래스를 동적 로딩해서 생성된다. 또한 유지보수 시 해당 클래스만 수정하면 되기 때문에 전체 애플리케이션을 다시 컴파일할 필요가 없다. 따라서 유지보수가 쉽고 빠르다.
+ 단점
    + 비교적 느림
        - 한번의 컴파일링으로 실행 가능한 기계어가 만들어지지 않고 JVM에 의해 기계어로 번역되고 실행되는 과정을 거치기 때문에 조금 느리다.
    

JVM은 몇 가지 옵션을 통해 최적화 방법을 제공한다.
+ Xmx, Xms 를 통해 힙 크기를 조정하여 가비지 컬렉션의 횟수를 조정한다. 모니터링 및 성능테스트 작업을 통해 최적의 값을 찾아야 한다.


<Br>

## OOP(객체 지향 프로그래밍) 특징
객체 지향 프로그래밍이란 __프로그램 구현에 필요한 객체를 파악하고 객체들 간의 상호작용을 통해 프로그램을 만드는 것__ 을 말한다.

+ 캡슐화
    - 정보 은닉 : 필요 없는 정보는 외부에서 접근하지 못하도록 제한
    - 높은 응집도, 낮은 결합도로 유연함과 유지보수성 증가
+ 추상화
    - 사물들의 공통적인 특징을 파악해서 하나의 개념(집합)으로 다루는 것    
    - 목적과 관련이 없는 부분을 제거하여 필요한 부분만을 표현하기 위한 개념
+ 상속
    - 기존 상위클래스에 근거하여 새롭게 클래스와 행위를 정의할 수 있게 도와주는 개념
+ 다형성
    - __형태가 같은데 다른 기능을 하는 것을 의미__
    - 오버라이딩, 오버로딩

단점으로는 처리속도가 상대적으로 느리고 객체가 커질수록 설계시 많은 시간과 노력이 필요하다.

<br>

## OOP 5대 원칙 SOLID
+ S : 단일 책임 원칙(Single Responsible Principle)
    - 객체는 단 하나의 책임만을 가져야한다.
    - 어떤 변화에 의해 클래스를 변경해야 하는 이유는 오직 하나뿐이어야 한다.
+ O : 개방 폐쇄 원칙(Open Closed Principle)
    - 기존 코드를 변경하지 않으면서 기능을 추가할 수 있도록 설계되어야 한다.
+ L : 리스코프 치환 원칙
    - 자식 클래스는 최소한 자신의 부모 클래스에서 가능한 행위는 수행할 수 있어야한다.
+ I : 인터페이스 분리 원칙
    - 특정 클라이언트를 위한 인터페이스 여러 개가 범용 인터페이스 하나보다 낫다.
    - 인터페이스가 명확해지고, 대체 가능성이 높아진다.
+ D : 의존관계 역전 원칙 (Dependency Inversion Principle)
    - 의존 관계를 맺을 때 변화하기 쉬운 것 또는 자주 변화하는 것보다는 변화하기 어려운 것, 거의 변화가 없는 것에 의존하라는 것이다.
    - 쉽게 이야기하면, 구현 클래스에 의존하지 말고, 인터페이스에 의존하라는 뜻

<br>

## 객체 지향 프로그래밍 vs 절차 지향 프로그래밍
+ 절차 지향 프로그래밍
    - __실행하고자 하는 절차를 정하고, 순차적으로 프로그래밍 하는 방식으로 빠르다.__
    - __엄격하게 순서가 정해져 있기에 비효율적이고 유지보수가 어렵다.__
    - 목적을 달성하기 위한 일의 흐름에 중점을 둔다.
+ 객체 지향 프로그래밍
    - __구현해야할 객체들 사이의 상호작용을 프로그래밍하는 방식으로 상속, 다형성, 추상화, 캡슐화를 통해 코드의 재사용성을 높이지만 느리다.__
    - __사람의 사고와 가장 비슷하게 프로그래밍 하기 위한 방식__


<br>

## 클래스, 객체, 인스턴스
+ 클래스
    - 객체를 만들어 내기 위한 설계도, 틀
+ 인스턴스
    - 소프트웨어 세계에 구현된 구체적인 실체
    - 객체를 실체화하면 인스턴스라고 부른다.
    - __메모리에 할당되어 실제 사용될 때 인스턴스__ 라고 한다.
+ 객체
    - __소프트웨어 세계에 구현할 대상__
    - __모든 인스턴스를 대표하는 포괄적 의미__
    - 클래스에 선언된 모양 그대로 생성된 실체
    

클래스는 객체를 위한 설계도, 틀이고, 인스턴스는 클래스를 보고 메모리가 할당되어 만들어진 실체이고, 객체는 인스턴스를 포괄하여 부르는 소프트웨어 세계에서 구현할 대상 의미인 것 같다.  

## JVM(Java Virtual Machine)
+ 자바 프로그램을 실행하는 역할
    - 컴파일러를 통해 바이트 코드로 변환된 파일을 JVM에 로딩하여 실행
+ Class Loader : JVM 내(Runtime Data Area)로 Class 파일을 로드하고 링크
+ Execution Engine : 메모리(Runtime Data Area)에 적재된 클래스들을 기계어로 변경해 실행
+ Garbage Collector : 힙 메모리에서 참조되지 않는 개체들 제거
+ Runtime Data Area : 자바 프로그램을 실행할 때, 데이터를 저장

### 실행과정
1. JVM은 OS로부터 메모리(Runtime Data Area)를 할당 받음
2. 컴파일러(javac)가 소스코드(.java)를 읽어 바이트 코드(.class)로 변환
3. Class Loader를 통해 Class파일을 JVM내 Runtime Data Area로 로딩
4. 로딩된 Class 파일을 Execution Engine을 통해 해석 및 실행

### JVM 메모리(Runtime Data Area) 구조

![그림5](https://github.com/backtony/blog-code/blob/master/interview/gc/img/gc-5.PNG?raw=true)  
+ 메서드(static) 영역
    - 클래스가 사용되면 해당 클래스의 파일(.class)을 읽어들여, 클래스에 대한 정보(바이트 코드)를 메서드 영역에 저장
    - 클래스와 인터페이스, 메서드, 필드, static 변수, final 변수 등이 저장되는 영역입니다.
+ JVM 스택 영역
    - 스레드마다 존재하여 스레드가 시작할 때마다 할당
    - 지역변수, 매개변수, 연산 중 발생하는 임시 데이터 저장
    - 메서드 호출 시마다 개별적 스택 생성
+ JVM 힙 영역
    - 런타임 시 동적으로 할당하여 사용하는 영역
    - new 연산자로 생성된 객체와 배열 저장
    - 참조가 없는 객체는 GC의 대상
+ pc register
    - 쓰레드가 현재 실행할 스택 프레임의 주소를 저장
+ Native Method Stack
    - C/C++ 등의 Low level 코드를 실행하는 스택


## 접근 제한자
+ public : 접근에 제한이 없음
+ private : 자기 자신 클래스 내에서만 접근 가능
+ default : 동일한 패키지 내에서만 접근 가능
+ protected : 동일한 패키지 내에서만 접근 가능 + 상속을 이용한 접근 가능

<br>

## String과 Char
Char은 내용물이 1개인 문자로 제한되는 반면에 String은 제한 없이 문자를 담을 수 있다.  
Char의 경우 변수 안에 직접적으로 문자를 가지고 있지만 String은 reference 타입으로 실질적인 문자가 아니라 좌표값을 가지고 있다.  
이 때문에 동등성과 동일성 비교에서 차이가 나타난다.  
Char의 경우 값이 같다면 ==(동일성) 비교를 사용할 수 있지만, String의 경우 내용이 같더라도 생성되는 좌표가 다르기 때문에 == 비교를 사용하면 다른 결과가 나오게 되고 equals를 사용해야 같은지 알 수 있다.


## 데이터 타입
+ __Value Type__
    - __기본 Primitive 타입으로 int, char 등이 있다.__
    - 기본 타입의 크기가 작고 고정적이기 때문에 __메모리 Stack 영역__ 에 저장된다.
    - 정수형 : byte, short, int, long
    - 실수형 : float, double
    - 논리형 : boolean
    - 문자형 : char
+ __참조 타입(Reference Type)__
    - __기본형을 제외하고는 모두 참조형이다.__
    - __String과 박싱 타입인 Integer 등이 있다.__
    - 참조 타입은 데이터의 크기가 가변적이고, 동적이므로 __Heap 영역__ 에서 관리된다.
    - __데이터는 Heap 영역에서 관리되지만 메모리의 주소값은 Stack 영역에 담긴다.__
    - new 키워드를 이용해 객체를 생성하여 데이터가 생성된 __주소를 참조하는 타입__     
    - String과 배열은 일반적인 참조 타입과 달리 new 없이 생성 가능하지만 참조타입이다.
    - 더이상 참조하는 변수가 없을 때 GC에 의해 삭제된다.
+ __왜 둘을 힙, 스택에 나누는가__
    - 스택은 함수 호출이 완료되면 알아서 소멸하지만 힙 영역의 경우 사용자가 직접 관리할 수 있는 메모리 영역이기 때문이다.
    - java의 경우 GC가 힙 영역의 메모리들을 관리한다.

<br>

## Call By Value와 Call By Reference
+ Call By Value(값에 의한 호출)
    - 함수 호출 시 인자로 전달되는 __변수의 값을 복사하여 함수의 인자로 전달__ 한다.
    - 따라서, __함수 안에서 인자의 값이 변경되어도, 외부의 변수의 값은 변경되지 않는다.__
    - 여기서의 Value는 말 그대로 값 자체를 의미한다. 값을 담을 수 있는 모든 타입을 의미하여 정수형, 문자열, 문자열, __주소값까지__ 모두 값이 될 수 있다.
+ Call by Reference(참조에 의한 호출)
    - __함수 호출 시 인자로 전달되는 변수의 레퍼런스를 전달한다.__
    - 따라서 __함수 안에서 인자의 값이 변경되면, 인자로 전달된 변수의 값도 함께 변경된다.__

자바는 새롭게 지역 변수(다른 주소)를 만들어서 값만 복사하고 할당한다. 따라서 자바는 Call By Value에 해당한다.  


## ==과 equals
+ ==
    - 참조 비교로 두 객체가 같은 메모리 공간을 가리키는지 확인
+ equals
    - 두 객체의 값이 같은지 내용을 비교한다.
    - 문자열의 데이터, 내용을 기반으로 비교한다.
    - __기본 타입(Primitive Type)에 대해서는 적용할 수 없다.__
    - __객체 비교시 override해서 원하는 방식으로 수정할 수 있다.__

## hashcode
__두 객체가 동일한 객체인지 비교할 때 사용하고 heap 영역에 저장된 객체의 메모리 주소를 반환합니다.__


## Wrapper class
프로그램에 따라 기본 타입의 데이터를 객체로 취급해야하는 경우가 있다.  
예를 들어, 메서드의 인수로 객체 타입만이 요구된다면, 기본 타입의 데이터를 그대로 사용할 수는 없다. 이때 기본 타입의 데이터를 먼저 객체로 변환 후 작업을 수행해야 한다.  
__래퍼 클래스(Wrapper class)는 산술 연산을 위해 정의된 클래스가 아니므로, 인스턴스에 저장된 값을 변경할 수 없다.__  

기본 타입|래퍼 클래스
---|---
byte|Byte
short|Short
int|Integer
long|Long
float|Float
double|Double
char|Character
boolean|Boolean

래퍼 클래스는 각각 타입에 해당하는 데이터를 인수로 전달받아, 해당 값을 가지는 객체로 만들어준다.  
<br>

### 박싱, 언방식
래퍼 클래스는 산술 연산을 위해 정의된 클래스가 아니므로, __인스턴스에 저장된 값을 변경할 수 없다.__  
단지, 값을 참조하기 위해 새로운 인스턴스를 생성하고, 생성된 인스턴스의 값만을 참조할 수 있다.  
기본 타입을 래퍼클래스의 인스턴스로 변환하는 과정을 __박싱__, 래퍼클래스의 인스턴스에 저장된 값을 다시 기본 타입으로 꺼내는 과정은 __언방식__ 이라고 한다.
```java
Integer num = new Integer(17); // 박싱
int n = num.intValue();        // 언박싱
System.out.println(n); // 출력 값: 17


Character ch = 'X'; // Character ch = new Character('X'); : 오토박싱
char c = ch;        // char c = ch.charValue();           : 오토언박싱
System.out.println(c); // 출력 값: X
```
JDK1.5 부터는 위와 같이 자바 컴파일러가 알아서 언방식, 박싱 작업을 해준다.  

<br>

```java
public class Wrapper03 {
    public static void main(String[] args) {
      Integer num1 = new Integer(10);
      Integer num2 = new Integer(20);
      Integer num3 = new Integer(10);

      System.out.println(num1 < num2);       // true
      System.out.println(num1 == num3);      // false
      System.out.println(num1.equals(num3)); // true
    }
}
```
래퍼 클래스를 비교할 때는 ==를 사용하면 안되고, __equals 메서드를 사용해야 한다.__  
래퍼 클래스는 객체 이므로 주소값 비교가 아니라 내부 값을 비교하는 equals를 써야한다.  

<br>



## non-static vs static
+ non-static
    - 공간적 특성
        - 객체마다 별도고 존재하고 인스턴스 변수라고 부른다.
    - 시간적 특성
        - 객체와 생명주기가 동일하다.
+ static
    - 공간적 특성
        - __클래스당 하나만 생성된다.__
        - 클래스 멤버라고 부른다.
    - 시간적 특성
        - 객체가 생기기 전에 이미 생성되어 객체를 생성하지 않아도 사용 가능하다.
        - 객체가 사라져도 사라지지 않는다.
        - 프로그램 종료시에 사라진다.
    - 공유 특성
        - __동일한 클래스의 모든 객체들에 의해 공유된다.__

## main이 static인 이유
__static 멤버는 프로그램 시작 시(클래스 로딩) 메모리에 로드되어 인스턴스를 생성하지 않아도 호출이 가능하기 때문이다.__  
+ 실행 과정
    - 코드를 실행하면 컴파일러가 .java를 .class 바이트 코드로 변환
    - 클래스 로더가 .class 파일을 메모리 영역에 로드
    - Runetime Data Area 중 Method Area(=Class area, Static area)라고 불리는 영역에 Class Variable이 저장되는데, static 변수도 여기에 포함
    - JVM은 Method Area에 로드된 main()을 싱행

## final 키워드
+ final 키워드
    - 변수, 메서드 클래스가 __변경 불가능__ 하도록 만든다.
    - 기본 타입 변수에 적용 시
        - 해당 변수의 값 변경 불가능하다.
    - 참조 변수에 적용 시
        - 참조 변수가 힙 내의 다른 객체를 가리키도록 변경할 수 없다.
    - 메서드에 적용 시
        - 해당 메서드를 오버라이드할 수 없다.(오버로딩은 가능)
    - 클래스에 적용 시        
        - 해당 클래스를 상속 받아서 사용할 수 없다.
+ finally 키워드
    - try catch 블록 뒤에서 항상 실행될 코드 블록을 정의하기 위해 사용한다.
+ finalize 메서드
    - 가비지 컬렉터가 더 이상의 참조가 존재하지 않는 객체를 메모리에서 삭제하겠다고 결정하는 순간 호출된다.

## try with resources 
자바 7 이전에는 try-catch-finally에서는 리로스의 생성을 try 구문에서 리소스의 반납은 finally 구문에서 하다보니 실수의 발생 여지가 있었다.  
자바 7 이후에는 try with resources 구문이 나오게 됬는데 try 옆 괄호 안에서 리소스를 생성해주면 따로 반납하지 않아도 리로스를 자동으로 반납해주게 된다.  
아래와 같이 작성한 것이 try with resources 구문이고, 만약 try-catch-finally를 사용했다면 finally 구문에서 scanner을 close하는 내용이 있어야 한다.
```java
public class ResourceClose {
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(new File("input.txt"))) {
            System.out.println(scanner.nextLine());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
```

## 제네릭
+ __클래스나 메서드에서 사용할 내부 데이터 타입을 외부에서 지정하는 기법__
+ __잘못된 타입이 들어올 수 있는 것을 컴파일 단계에서 방지할 수 있다.__
+ __불필요한 타입 변환을 제거할 수 있다.__

```java
class Solution {

    public <T extends Number> Double solution(List<T> arr) {
        return arr.stream()
                .map(Number::doubleValue)
                .reduce((x,y) -> x+y)
                .orElse(0.0);
    }

    public static void main(String[] args) {
        List<Integer> intArr = new ArrayList<>(List.of(1,2,3));
        List<Double> doubleArr = new ArrayList<>(List.of(1.1,2.2,3.3));

        Solution sol = new Solution();
        System.out.println(sol.solution(intArr));
        System.out.println(sol.solution(doubleArr));

    }
}
```
Number로 제한된 리스트 받아서 sum 처리하는 제네릭 함수 만들기


## 가비지 컬렉터
[JVM 가비지 컬렉터](https://velog.io/@backtony/%EA%B0%80%EB%B9%84%EC%A7%80-%EC%BB%AC%EB%A0%89%ED%84%B0)

    
    
## 직렬화와 역직렬화
+ 직렬화
    - __자바 시스템 내부에서 사용되는 객체 또는 데이터를 외부의 자바 시스템에서도 사용할 수 있도록 바이트 형태로 데이터를 변환하는 기술__
    - 조건
        - 자바 기본 타입
        - Serializable 인터페이스 상속받은 객체
    - ObjectOutputStream 객체를 이용
+ 역직렬화
    - 바이트로 변환된 데이터를 다시 객체로 변환하는 기술
    - ObjectInputStream 객체를 이용



## 오버로딩, 오버라이딩
+ 오버로딩
    - 두 메서드가 같은 이름을 갖고 있으나 인자의 수나 자료형이 다른 경우
+ 오버라이딩
    - 하위 클래스에서 상위 클래스의 메서드와 일치하는 함수를 재정의하는 것


## 추상 클래스와 인터페이스 차이
+ 추상 메서드
    - abstract 키워드와 함께 원형만 선언되고 코드는 작성하지 않은 메서드
+ 추상 클래스
    - 개념 : abstract 키워드로 선언된 클래스
        - 추상 메서드가 없어도 abstract로 선언된 클래스
        - 추상 메서드가 최소 한 개 이상을 가진 abstract 클래스
            - 추상 메서드가 존재하면 반드시 abstract 클래스로 선언되어야함        
    - 목적
        - __관련성이 높은 클래스 간의 코드를 공유하고 확장하고 싶은 목적__
+ 인터페이스
    - 개념 : default와 static 을 제외하고는 추상 메서드와 상수만을 포함하여, interface 키워드를 사용하여 선언
        - 모든 메서드는 추상 메서드로, abstract public이 생략되어 있다.
        - 상수 필드는 public static final이 생략되어 있다.
        - 다중상속이 가능하다.
    - 목적
        - __관련성이 없는 클래스들의 논리적으로 같은 기능을 자신에 맞게 구현을 강제하는데 목적__ 

정리하자면, 구조상 차이로 추상 클래스는 abstract 키워드가 붙고 추상 메서드뿐만 아니라 다른 변수, 메서드도 선언이 가능하고, 인터페이스는 추상 메서드와 상수만 선언 가능하다.(자바 8부터는 default 메서드 선언 가능)  
목적의 차이로 추상 클래스는 관련성이 높은 클래스 간의 코드 공유(재사용)와 확장을 목적으로 하고, 인터페이스는 관련성이 없는 클래스들의 같은 기능을 자신에 맞게 구현하는데 목적이 있다.  
<br>

예를 들어, 사람은 human 추상 클래스를 상속받고, 앵무새는 animal이라는 추상클래스를 상속받았다고 했을때, 사람과 앵무새에게는 talk이라는 기능이 있다고 해보자. 그럼 이 기능을 넣기 위해서 각각의 추상 클래스에 작성하자니 중복이 발생하고, 그럼 따로 추상클래스로 talk을 뽑아서 상속시키자니 클래스 상속은 단일 상속밖에 안됩니다. 이때 인터페이스로 뽑아서 구현시켜주면 되는 것이다.  
공통점으로는 둘다 __독립적으로 객체 생성은 불가능__ 하고, 추상메서드는 오버라이드 해야된다는 점이다.

## Error, Exception
![그림3](https://github.com/backtony/blog-code/blob/master/interview/interview-series/img/java/java-3.PNG?raw=true)  

목록|Error|Exception
---|---|---
패키지|java.lang.error|java.lang.exception
발생 시점|런타임에서 발생, 컴파일 시점에서 알 수 없다.|Checked Exception은 컴파일 시점에, Unchcked Exception은 런티림 시점에 알 수 있다.
복구|에러는 복구 불가능|try catch 블락을 이용하여 복구 가능
타입|모든 Error는 Unchecked Type|checked Type, Unchecked Type으로 분류
예시|OutOfMemory, StackOverFlow|아래서 설명



## Checked Exception, Unchecked Exception

구분|Checked Exception|Unchecked Exception
---|---|---
상속|RuntimeException 상속 X| RuntimeException 상속
확인 시점|컴파일 시점|런타임 시점
처리 여부|반드시 예외처리|명시적으로 하지 않아도 됨
트랜잭션 처리|예외 발생시 롤백 X|예외 발생시 롤백 해야함
종류|IOException,SQLException..|NullPointException,ArrayIndexOutOfBounds...

대부분 Checked Exception보다는 Unchecked Exception 사용을 권장한다.  
Checked Exception의 경우 사용하는 모든 곳에 throws를 남겨야하는데 이 문제는 의존성 문제를 야기한다.  
예를 들어 가장 하위에서 SQLException(Checked Exception)를 던진다고 해보자.  
그럼 상위 서비스, 컨트롤러도 SQLException을 처리하기 위해서 throws SQLException을 붙이게 된다.  
SQLException은 JDBC 기술이므로 service, controller는 JDBC에 의존하게 된다.  
결국 JDBC 기술을 다른 기술로 교체하게 되면 연결된 모든 것들을 전부 교체해야하는 문제가 생긴다.  



## Java Collections
![그림1](https://github.com/backtony/blog-code/blob/master/interview/interview-series/img/java/java-1.PNG?raw=true)  
__Set과 List는 Collection 인터페이스를 구현하고 있고 Map은 인터페이스를 구현하고 있지 않다.__
+ Map   
    - Key와 Value의 형태로 이루어진 데이터 집합
    - 순서를 보장하지 않는다.
    - Key는 중복이 허용되지 않고, Value는 중복을 허용한다.
+ Collection을 상속한 것들
    - List
        - 순서가 있는 데이터 집합
        - 데이터를 중복해서 포함시킬 수 있다.
    - Set
        - 데이터의 중복을 허용하지 않는 데이터 집합
        - 순서를 보장하지 않는다.
        - Value의 중복을 허용하지 않는다.

<br>

![그림2](https://github.com/backtony/blog-code/blob/master/interview/interview-series/img/java/java-2.PNG?raw=true)  
Collection 인터페이스 위에는 Iterable이라는 인터페이스가 있고 이 인터페이스는 iterator 라는 추상 메서드를 갖고 있다. 그래서 컬렉션에 iterator가 있는 것이다.  
Stack은 Vector을 상속받아 구현되기 때문에 동시성 때문에 잘 사용하지 않는다.  
그래서 Stack과 큐를 구현할 때는 보통 LinkedList를 사용해서 구현한다.


### Map
+ HashMap    
    - 내부 hash값에 따라 키순서가 정해지므로 특정 규칙없이 출력된다.
    - key와 value에 __null값을 허용__ 한다.
    - 비동기 처리
+ LinkedHashMap    
    - 입력 순서대로 출력된다.
    - 비동기 처리
+ TreeMap
    - 내부적으로 __레드-블랙 트리(이진 탐색 트리)__ 로 저장된다.
    - __Null값 비허용__
    - 키값이 기본적으로 __오름차순 정렬__ 되어 출력된다.
    - Compartor 구현으로 정렬 방법을 지정할 수 있다.
+ ConCurrentHashMap
    - key,value에 null값 비허용
    - __쓰기작업에서만 동기 처리__
+ HashTable
    - key,value에 null값 비허용
    - __모든 작업에 동기 처리__

<Br>

__HashMap 동작 방식__  
+ key-value 쌍의 데이터를 저장하고 key값은 중복을 허용하지 않는다.
+ 동일하지 않은 두 객체가 같은 bucket에 들어가려고 하는 경우를 Collision이라고 하는데 성능에 영향을 미치므로 어떤 Hash 함수를 사용하는 가에 성능이 결정된다.
+ 공간이 없을 때 수행 방식
    - 리스트로 넣는 방식
    - 사이즈를 늘리는 방식
        - new HashMap<>(4, 0.75f) -> 사이즈가 4이고 load factor가 0.75세팅
        - 4 * 0.75 = 3 이므로 3개의 요소가 채워지는 순간 사이즈를 2배 늘려서 8개로 만든다.



### Set
+ HashSet
    - 저장 순서를 유지하지 않는 데이터의 집합
    - Null 저장 가능
    - 해시 알고리즘을 사용하여 검색속도가 매우 빠르다.
    - 내부적으로 HashMap 인스턴스를 이용하여 요소를 저장한다.
+ LinkedHashSet
    - 저장 순서를 유지하는 HashSet
+ TreeSet
    - 데이터가 정렬된 상태로 저장되는 이진 탐색 트리의 형태로 요소를 저장한다.
    - __Null 저장 불가능__
    - __레드 블랙 트리(이진탐색트리)__ 로 구현되어 있다.
    - Compartor 구현으로 정렬 방법을 지정할 수 있다.

### List
+ ArrayList
    - 내부적으로 배열을 사용하는 자료구조로 메모리가 연속적으로 배치된다.
    - 배열과 달리 메모리 할당이 동적이다.
    - 데이터 삽입, 삭제 시 해당 데이터 이후 모든 데이터가 복사되므로 빈번한 삭제, 삽입이 일어나는 경우에는 부적합하다.
    - 검색의 경우는 인덱스의 데이터를 가져오면 되므로 빠르다.
    - __재할당 시 크기의 절반씩 증가한다.__
+ LinkedList
    - 양방향 포인터 구조로 각 노드가 데이터와 포인터를 가지고 한 줄로 연결되어 있는 방식으로 데이터를 저장하는 자료 구조
    - 데이터의 삽입, 삭제 시 해당 노드의 주소지만 바꾸면 되므로 삽입, 삭제가 빈번한 데이터에 적합하다.
    - 메모리가 불연속적이다.
    - 데이터 검색 시 처음부터 순회하므로 검색에는 부적합하다.
    - 스택, 큐, 양방향 큐를 만들기 위한 용도로 사용한다.
    - 양옆의 정보만을 갖고 있기 때문에 순차적으로 검색을 진행하여 검색 속도가 느리다.
+ Vector
    - 내부에서 자동으로 동기처리가 일어난다.
    - 성능이 좋지 않아 잘 사용하지 않는다.
    - __재할당 시 크기의 두 배로 증가한다.__
+ Stack
    - new 키워드로 직접 사용 가능
    - Vector를 상속받아 동기 처리


#### cf) 배열과 리스트는 다른 것
+ 배열(Array)
    - 메모리 상에 연속적으로 데이터를 저장하는 자료구조
    - 크기가 고정적이라 선언 시 지정한 크기를 변경할 수 없다.(Immutable)

#### cf) 자료구조 참고
+ 그래프
    - 정점과 간선들의 집합
+ 트리
    - 계층적 관계를 표현한 자료구조로, 싸이클이 없는 그래프
+ 힙
    - 최대, 최소값을 찾아내는 연산을 빠르게 하기 위해 고안된 __완전이진트리__
+ hash
    - 내부적으로 배열을 사용하여 데이터를 저장하기 때문에 빠른 검색 속도를 갖는다.
    - hash 함수로 key값이 변경되어 해당 인덱스로 빠르게 찾을 수 있다.


#### cf) Red-Black Tree
레드 블랙 트리는 Balanced Binary Search Tree로 __균형 이진 탐색 트리__ 이다.  
이진탐색트리의 경우 한쪽에 쏠릴 경우 성능이 안좋아지게 되는데 이를 균형에 맞게 극복한 것이 레드 블랙 트리이다.  
결과적으로 높이가 log(n)이 되므로 search 연산이 최악의 경우에도 log(n)을 가지게 된다.  
참고로 삽입, 삭제 모두 log(n)이 걸린다. Recoloring, Restructuring은 O(1)이 걸리나 최악의 경우 쭉 올라가는 과정이 반복되어 log(n)번 하게 되기 때문이다.
<br>

__특징__  
1. 루트노드의 색깔인 Black이다.
2. 모든 외부(리프)노드는 Black이다.
3. 모든 빨간 노드의 자식은 Black이다.
4. 모든 외부(리프)노드에서 Black Depth는 같다.
    - 리프노드에서 루트노드까지 가는 경로에서 만나는 블랙 노드의 개수는 같다.

[자세한 내용](https://zeddios.tistory.com/237)

## String, StringBuilder, StringBuffer
+ String
    - __새로운 값을 할당할 때마다 새로운 클래스에 대한 객체가 생성__
    - String에 저장되는 문자열은 private final char[] 형태이므로 변경할 수 없다.
    - String + String + String..
        - 가비지 컬렉터가 호출되기 전까지 생성된 String 객체들은 Heap에 머물기 때문에 메모리 관리에서 치명적이다.
+ StringBuilder
    - 메모리에 append하는 방식으로 클래스에 대한 객체를 생성하지 않는다.
    - 비동기 처리
+ StringBuffer
    - 메모리에 append하는 방식으로 클래스에 대한 객체를 생성하지 않는다.
    - 동기 처리

### String new와 ""의 차이
new 는 계속 새로운 객체를 생성해내는 반면에 ""의 경우는 이미 존재하는 String 값이라면 같은 래퍼런스를 참조한다.  


## Blocking vs Non-Blocking
두 가지의 차이점은 __다른 주체가 작업할 때 자신이 코드를 실행할 제어권이 있는지 없는지로 판단__ 할 수 있다.  

### Blocking
![그림1](https://github.com/backtony/blog-code/blob/master/interview/sync-async/img/block-1.PNG?raw=true)  
Blocking 자신의 작업을 진행하다가 다른 주체의 작업이 시작되면 __제어권을 다른 주체로 넘긴다.__  
따라서 자신은 제어권이 없기 때문에 실행할 수 없고, 다른 주체가 실행을 완료하고 제어권을 돌려줄 때까지 아무 작업도 할 수 없다.  

### Non-Blocking
![그림2](https://github.com/backtony/blog-code/blob/master/interview/sync-async/img/block-2.PNG?raw=true)  
Non-Blocking은 다른 주체의 작업에 __관련없이 자신이 제어권을 갖고 있다.__  
따라서, 자신은 계속 작업을 수행할 수 있다.  

## Sync vs Async
두 가지의 차이점은 __호출하는 함수가 호출되는 함수의 작업 완료 여부를 신경쓰는지 여부__ 에 차이가 있다.  

### Sync(동기)
동기는 함수 A가 B를 호출한 뒤, B의 결과값이 나오면 해당 결과값을 가지고 바로 처리하는 방식이다.

### Async(비동기)
비동기는 함수 A가 B를 호출한 뒤, B의 결과값에 큰 비중을 두지 않고 결과가 나오면 처리를 할 수도 있고 안 할 수도 있다.
 



## 리플렉션
+ __런타임 상황에서 메모리에 올라간 클래스나 메서드등의 정의를 동적으로 찾아서 조작할 수 있는 기술__
+ 컴파일 시간이 아닐 실행 시간에 동적으로 특정 클래스의 정보를 객체화를 통해 분석 및 추출해낼 수 있는 프로그래밍 기법이다.
+ 자바에서 이미 로딩이 완료된 클래스에서 또 다른 클래스를 동적으로 로딩하여 생성자, 필드, 메서드 등을 사용할 수 있는 방식이다.  
+ 장점
    - 런타임 시점에 사용할 instance를 선택하고 동작시킬 수 있는 유연한 기능을 제공한다.
+ 단점
    - __컴파일 시점이 아니라 런타임 시점에서 오류를 잡기 때문에 컴파일 시점에 확인할 수 없다.__
    - 접근 제어자로 캡슐화된 필드, 메서드에 접근 가능해지므로 기존 동작을 무시하고 깨뜨리는 행위가 가능해진다.    
    - 위와 같은 단점 때문에 피할 수 있다면 사용을 자제하는 것이 좋다.
+ 사용처
    + 런타임 시점에 다른 클래스를 동적으로 로딩하여 접근할 때 사용
    + 클래스와 멤버 필드, 메서드 등에 관한 정보를 얻어야 할 때 사용

```java
public class DoHee {
    public String name;
    public int number;
    public void setDoHee (String name, int number) {
      this.name = name;
      this.number = number;
    }
    public void setNumber(int number) {
        this.number = number;
    }
    public void sayHello(String name) {
      System.out.println("Hello, " + name);
  }
}
```
```java
import java.lang.reflect.Method;
import java.lang.reflect.Field;

public class ReflectionTest {
    public void reflectionTest() {
        try {
            Class myClass = Class.forName("DoHee");
            Method[] methods = myClass.getDeclaredMethods();

            /* 클래스 내 선언된 메서드의 목록 출력 */
            /* 출력 : public void DoHee.setDoHee(java.lang.String,int)
                     public void DoHee.setNumber(int)
                     public void DoHee.sayHello(java.lang.String) */
            for (Method method : methods) {
                System.out.println(method.toString());
            }

            /* 메서드의 매개변수와 반환 타입 확인 */
            /* 출력 : Class Name : class DoHee
                     Method Name : setDoHee
                     Return Type : void */
            Method method = methods[0];
            System.out.println("Class Name : " + method.getDeclaringClass());
            System.out.println("Method Name : " + method.getName());
            System.out.println("Return Type : " + method.getReturnType());

            /* 출력 : Param Type : class java.lang.String
                     Param Type : int */
            Class[] paramTypes = method.getParameterTypes();
            for(Class paramType : paramTypes) {
                System.out.println("Param Type : " + paramType);
            }

            /* 메서드 이름으로 호출 */
            Method sayHelloMethod = myClass.getMethod("sayHello", String.class);
            sayHelloMethod.invoke(myClass.newInstance(), new String("DoHee")); // 출력 : Hello, DoHee

            /* 다른 클래스의 멤버 필드의 값 수정 */
            Field field = myClass.getField("number");
            DoHee obj = (DoHee) myClass.newInstance();
            obj.setNumber(5);
            System.out.println("Before Number : " + field.get(obj)); // 출력 : Before Number : 5
            field.set(obj, 10);
            System.out.println("After Number : " + field.get(obj)); // 출력 : After Number : 10
        } catch (Exception e) {
            // Exception Handling
        }
    }

    public static void main(String[] args) {
        new ReflectionTest().reflectionTest();
    }
}
```

## Stream
+ java 8 에서 추가된 API
+ 컬렉션 타입의 데이터를 Stream 메서드로 내부 반복을 통해 정렬, 필터링 등이 가능
+ 특징
    - parallel 메서드 제공을 통해 __병렬처리__ 가 가능
        - 각 스레드가 개별 큐를 가지고 있으며, 놀고 있는 스레드가 있으면 일하는 스레드의 작업을 가져와 수행
    - __데이터를 변경하지 않는다(Immutable)__
        - 원본데이터로부터 데이터를 읽기만 할 뿐, 원본데이터 자체를 변경하지 않는다.
    - 작업을 내부 반복으로 처리하므로 불필요한 코딩을 줄일 수 있다.
    - 최종 연산 후 stream이 닫히므로 __일회용__ 이다.
+ 구조
    - Stream 생성
    - 중간 연산
        - __데이터를 가공하는 과정에 해당한다.__
        - 필터링 : filter, distinct
        - 변환 : map, flatMap
        - 제한 : limit, skip
        - 정렬 : sorted
        - 연산결과확인 : peek
    - 최종 연산
        - __Stream 안의 데이터를 모아 반환하는 역할을 한다.__
        - 출력 : foreach
        - 소모 : reduce
        - 검색 : findFirst, findAny
        - 검사 : anyMatch, allMatch, noneMatch
        - 통계 : count, min, max
        - 연산 : sum, savage
        - 수집 : collect
+ __중간 연산 작업은 바로 실행되는 것이 아니라 종결 처리의 실행이 필요할 때서야 비로소 중간 처리가 실행된다.(Lazy Evalutation)__
+ ParallelStream
    - 개발자가 직접 스레드 혹은 스레드 풀을 생성하거나 관리할 필요 없이 parallelStream, parallel()만 사용하면 알아서 내부적으로 common __ForkJoinPool__ 을 사용하여 작접들을 분할하고 병렬적으로 처리한다.
        - __forkJoinPool은 ExecutorService의 구현체로 각 스레드별 개별 큐를 가지고 스레드에 아무런 task가 없으면 다른 스레드의 task를 가져와 처리하여 최적화 성능을 낼 수 있다는 특징이 있다.__
    - __내부적으로 스레드 풀을 만들어서 작업을 병렬화시킨다.__
    - __중요한 점은 parallelStream 각각 스레드 풀을 만드는게 아니라 별도의 설정이 없다면 하나의 스레드 풀을 모든 parallelStream이 공유한다.__
    - ParallelStream은 중간 연산에서 순서가 보장되지 않기 때문에 중간 연산에서 순서에 관계없는 연산의 경우에만 사용한다.
    - Parallel Stream은 작업을 분할하기 위해 Spliterator의 trySplit()을 사용하게 되는데, 이 분할되는 작업의 단위가 균등하게 나누어져야 하며, 나누어지는 작업에 대한 비용이 높지 않아야 순차적 방식보다 효율적으로 이뤄질 수 있다. array, arrayList와 같이 정확한 전체 사이즈를 알 수 있는 경우에는 분할 처리가 빠르고 비용이 적게 들지만, LinkedList의 경우라면 별다른 효과를 찾기 어렵다.

__Stream 관련해서 깊은 꼬리질문을 받았고 해당 내용을 바탕으로 업데이트했다. 위 내용은 전부 알아두자.__  



## Fork Join Pool
![그림4](https://github.com/backtony/blog-code/blob/master/interview/interview-series/img/java/java-4.PNG?raw=true)  
Java7에서 새로 지원하는 fork-join 풀을 기본적으로 큰 업무를 작은 업무로 나눠 배분하여, 일을 한 후에 일을 취합하는 형태이다.  
분할 정복 알고리즘과 비슷하다고 보면 된다.  
자바에서 풀을 관리하는 ThreadPoolExecutor와 마찬가지로 ForJoinPool도 내부에 inbound queue 라는 편지함이 하나 있다.  
그걸 두고 싸우느라 시간을 낭비하는 것을 방지하기 위해 ForkJoinPool은 스레드 개별 큐를 만들었다.  
<br>


![그림5](https://github.com/backtony/blog-code/blob/master/interview/interview-series/img/java/java-5.PNG?raw=true)  
왼쪽에서 업무를 submit하면 하나의 inbound queue에 누적되고 그걸 A와 B 스레드가 가져다가 일을 처리한다.  
A와 B는 각자 큐가 있으며, 자신의 큐에 업무가 없으면 상대방의 큐에서 업무를 가져와서 처리한다.  
최대한 노는 스레드가 없게 하기 위한 알고리즘이다.  


## 람다식
+ 자바 8에서 등장
+ __메서드를 하나의 식으로 표현하는 익명 함수__
+ 인터페이스 내에 한 개의 추상 메서드만 정의되어있는 함수형(Function) 인터페이스에 사용 가능
+ 장점
    - 기존에 익명함수로 작성하던 코드를 줄일 수 있음
    - 가독성 증가
    - 병렬 프로그래밍이 용이하다.

## Optional
+ T orElse(T other)
    - 반환할 값을 그대로 받는다.
    - __무조건 인스턴스화 된다.__
+ T orElseGet(Supplier<? extends T> other)
    - Supplier로 랩핑된 값을 인자로 받는다.
    - 함수를 전달받아 바로 값을 가져오지 않고 필요할 때(Lazy) 값을 가져온다.
    - __Optional 안의 값이 null일 경우에만 함수가 실행되면서 인스턴스화 된다.__

__면접에 나왔던 질문이므로 기억해두자.__


## 자바8 추가된 내용
+ optional
+ stream
+ lambda
+ localDateTime
+ default 메서드
    - 인터페이스는 메서드 정의만 가능하고 구현은 불가능했는데 default 메서드 개념이 생기면서 인터페이스에 구현된 메서드도 추가가 가능해졌다.

__cf) 기존 date의 문제점__  
+ 불변 객체가 아님
+ 헷갈리는 월 지정(1월을 0으로 표현)
+ 일관성 없는 요일 상수 (어디서는 일요일이 0 어디서는 1)
+ Date와 Calendar 객체의 역할 분담(Date만으로 부족해서 왔다갔다 해야함)
+ 상수 필드 남용


## Java 8 -> Java 11
+ __default GC가 Paralle GC에서 G1GC로 변경__
+ strip(), stripLeading(), stripTrailing(), isBlank(), repeat(n) 과 같은 __String 클래스에 새로운 메서드 추가__
+ writeString, readString, isSameFile 과 같은 __File관련 새로운 메서드 추가__
+ 컬렉션의 toArray() 메서드를 오버로딩하는 메서드가 추가되어 원하는 타입의 배열을 선택하여 반환할 수 있게 되었다.
    - sampleList.toArray(String[]::new)
+ Predicate 인터페이스에 부정을 나타내는 not() 메서드 추가
+ __람다에서 로컬 변수 var이 사용 가능__
+ Javac를 통해 컴파일하지 않고도 바로 java 파일을 실행할 수 있게 되었다.

자바 11은 LTS 버전이므로 장기적인 지원이 보장된 버전이다.  
__면접에서 왜 11버전을 사용했는가 8버전과 차이가 뭔가에 대해 질문이 나왔다.__



## 강한 결합과 느슨한 결합
결합도는 의존성의 정도를 나타내며 __다른 모듈에 대해 얼마나 많은 정보를 알고 있는지에 대한 척도__ 이다.  
어떤 모듈이 다른 모듈에 대해 자세한 부분(구현 세부사항)까지 알고 있는 경우를 강한 결합이라고 하고, 필요한 정보(추상화 정도)만 알고 있는 경우 느슨한 결합이라고 한다.

## 디자인 패턴
+ 싱글톤 패턴
    - 하나의 객체만 생성하여 공유하는 방식
+ 팩토리 패턴
    - 객체간 의존성을 줄이기 위해 객체의 생성과 데이터 주입만 담당하는 Factory class를 정의하여 사용하는 부분에서는 생성된 객체를 가져다 사용하여 의존성을 줄이는 방식
+ 템플릿 메서드 패턴
    - 하나의 템플릿 구조를 만들어 두고 메인이 되는 로직은 추상 메서드로 만들고 상속받는 클래스가 구현하는 방식
+ 전략 패턴
    - 템플릿 메서드 패턴처럼 구조를 만들어 두어 사용하지만 메인이 되는 로직은 인터페이스를 사용하여 전략을 주입하는 방식
+ 옵저버 패턴
    - 정보 수신 시 하나의 객체가 변하면 다른 객체에 객체가 변했다는 사항을 알려주는 패턴



## 함수형 프로그래밍
함수형 프로그래밍은 거의 모든 것을 순수 함수로 나누어 문제를 해결하는 기법으로, 작은 문제를 해결하기 위한 함수를 작성하여 가독성을 높이고 유지보수를 용이하게 해준다.  
Robert C.Martin은 __함수형 프로그래밍을 대입문이 없는 프로그래밍__ 이라고 정의하였다.  
명령형 프로그래밍에서는 메소드를 호출하면 상황에 따라 내부의 값이 바뀔 수 있다. 즉, 우리가 개발한 함수 내에서 선언된 변수의 메모리에 할당된 값이 바뀌는 등의 변화가 발생할 수 있다.  
하지만 함수형 프로그래밍에서는 대입문이 없기 때문에 메모리에 한 번 할당된 값은 새로운 값으로 변할 수 없다.  

+ 순수함수 : 같은 인자에 대해서 항상 같은 값을 반환하고 외부의 어떤 상태도 바꾸지 않는 함수
+ 일급객체 : 함수의 인자로 전달할 수 있고, 함수의 반환값으로 사용할 수 있고, 변수에 담을 수 있는 객체
+ 일급함수 : 함수가 일급객체면 일급함수라고 하고 일급함수에 이름이 없다면 람다식이라고 한다.

### 특징
부수 효과가 없는 순수 함수를 __1급 객체__ 로 간주하여 파라미터로 넘기거나 반환값으로 사용할 수 있으며, 참조 투명성을 지킬 수 있다.  

+ 사이드 이팩트가 없다.
    - 함수형 프로그래밍은 함수들의 조합으로 만들어지며, 각 함수들은 인자를 받고 그에 따른 결과를 내놓을 뿐 함수 내부적으로 어떠한 상태도 가지지 않는다. 따라서 함수 내부에서 벌어지는 일에 대해서는 전혀 신경쓸 필요가 없다. 단지, 함수 호출 시 입력하는 값과 그에 대한 결과 값만이 중요할 뿐이다.
+ 동시성 프로그래밍
    - 명령형 프로그래밍에서 교착상태가 발생하는 주된 원인은 스레드 간에 공유되는 데이터나 상태 값이 변경 가능(mutable)하기 때문이다. 하지만 함수형 프로그래밍은 모든 데이터가 변경 불가능하고 함수는 부수 효과를 가지고 있지 않기 때문에 여러 스레드가 동시에 공유 데이터에 접근하더라도 해당 데이터가 변경될 수 없기 때문에 동시성 관련된 문제를 원천적으로 봉쇄한다.
+ 함수를 값처럼 쓸 수 있기 때문에 익명함수처럼 간결한 코드를 구성할 수 있다.

### 단점
+ 순수함수를 구현하기 위해서 코드의 가독성이 좋지 않을 수 있다.
+ 순수함수를 사용하는 것은 쉬울지라도 조합하는 것은 쉽지 않다.


## 멀티스레드 프로그래밍
+ 스레드 생성 방법
    - 방법 1: Thread 클래스를 상속받아서 run을 오버라이드해서 정의한다.
    - 방법 2: Runnable 인터페이스를 구현하여 Thread 생성자에 인자로 넘긴다.
    - 방법 3: Callable 인터페이스를 구현하여 FutureTask에 한번 감싸서 Thread의 인자로 넘긴다.
    - __Runnable은 Exception이 없고 리턴값도 없으나 Callable은 리턴값이 있고 Exception을 발생시킬 수 있다.__    
+ 스레드 실행 방법
    - 보통 start() 메서드를 사용해서 호출하는데 start 한다고 해서 바로 실행되는 것은 아니고 실행 대기열에 저장된 후 차례가 오면 실행된다.
    - 정확하게 말하자면, start는 새로운 스레드가 작업을 실행하는데 필요한 새로운 호출 스택을 생성해서 그곳에 run 메서드를 올려둔다. 이후 그곳에서 run 메서드를 호출하고 스레드가 별개의 작업을 수행하게 된다.

__보통 위와 같이 인터페이스를 구현해서 Thread의 인자로 넘기거나, 상속으로 구현하는 방식은 운영환경에서 프로그램 성능에 영향을 미치기 때문에 사용하지 않는다.__  
운영 시에는 __ExecutorService와 Executors를 이용해 스레드풀을 생성__ 하여 병렬처리한다.  
앞선 방식은 각기 다른 Thread를 생성해서 작업 처리하고 처리가 완료되면 Thread를 제거하는 작업을 손수 진행해야 하지만 ExecutorService 클래스를 사용하면 손쉽게 처리할 수 있다.  
ExecutorService에 Task만 지정해주면 알아서 ThreadPool을 이용해 Task를 실행하고 관리한다.  
Executors는 ExecutorService 객체를 생성하며 다음과 같은 메서드를 제공하여 스레드 풀의 개수 및 종류를 정할 수 있다.
```java
// 인자 개수만큼 고정된 스레드를 생성하는 스레드 풀
ExecutorService executor = Executors.newFixedThreadPool(int n);

// 필요할 때 필요한 만큼 스레드를 무한정 생성하고 60초간 작업이 없다면 pool에서 제거하는 스레드풀
ExecutorService executor = Executors.newCachedThreadPool();

// 스레드 1개인 ExecutorService 리턴
ExecutorService executor = Executors.newSingleThreadExecutor();

// 일정 시간 뒤에 실행되는 작업이나, 주기적으로 수행되는 작업이 있다면 사용하는 것
ExecutorService executor = Executors.newScheduledThreadPool(int n);
```
ExecutorService API는 다음과 같다.
+ 작업 할당을 위한 메서드
    - execute(runnableTask) : 리턴타입이 void로 Task의 실행 결과나 상태를 알 수 없다.
    - submit(callableTask) : task를 할당하고 Future 타입의 결과값을 받는다. 결과 리턴이 되어야 하므로 Callable 구현체를 인자로 넣는다.
    - invokeAny(callableTasks) : Task를 Collection에 넣어서 인자로 넘긴다. 실행에 성공한 Task 중 하나의 리턴값을 반환한다.
    - invokeAll(callableTasks) : Task를 Collection에 넣어서 인자로 넘긴다. 모든 Task의 리턴값을 List\<Future\<>> 형태로 반환한다.
+ 종료 메서드
    - shutdown() : 실행중인 모든 task가 수행되면 스레드풀을 종료한다.    
    - shutdownNow() : 즉시 종료시도하지만 모든 Thread가 동시에 종료되는 것을 보장하지는 않고 실행되지 않은 Task를 반환
    - awaitTermination() : 이미 수행 중인 Task가 지정된 시간동안 끝나기를 기다리고 지정된 시간 내에 끝나지 않으면 false를 리턴


### 스레드 상태
+ NEW : 생성되고 아직 start가 호출되지 않은 상태
+ RUNNABLE : 실행 중 또는 실행 가능한 상태
+ BLOCKED : 동기화 블럭에 의해서 일시정지된 상태(lock이 풀릴 때까지 기다리는 상태)
+ WAITING : 다른 스레드가 통지할 때까지 기다리는 상태
    - 메인 스레드에서 별도 스레드를 만들고 별도스레드.join()을 호출하면 메인 스레드는 별도 스레드가 끝날때 까지 해당 위치에서 대기하다가 끝난 후에야 이후 로직을 수행한다.
+ TIMED_WAITING : 주어진 시간 동안 기다리는 상태
    - 스레드를 하나 만들고 run메서드에서 sleep을 시키고 메인 스레드에서 해당 스레드의 상태를 getState로 찍어보면 TIMED_WAITING 상태로 찍힌다.
+ TERMINATED : 쓰레드의 작업이 종료된 상태

### 스레드 스케줄링
+ sleep : 주어진 시간동안 일시 정지    
+ join : 특정 스레드가 다른 스레드의 완료를 대기
+ interrupt : sleep, join에 의해 일시정지 상태인 스레드를 실행 대기상태로 전환하는 것
+ yield : 실행 중에 다른 스레드에게 양보하고 실행 대기상태가 되는 것

### 우선순위
+ setPriority(1~10) : 5가 디폴트로 실행 우선순위를 바꿀 수 있으나 항상 우선순위대로 동작하진 않는다.

### 스레드 그룹
+ 서로 관련된 스레드를 그룹으로 묶어서 다루기 위한 것으로 모든 쓰레드는 반드시 하나의 스레드 그룹에 포함되어 있어야 한다.
+ 스레드 그룹을 지정하지 않고 생성한 스레드는 main 스레드 그룹에 속한다.
+ 자신을 생성한 스레드(부모 스레드)의 그룹과 우선순위를 상속받는다.


## 동기화
멀티스레드 프로그래밍에서는 하나의 프로세스를 동시에 여러 쓰레드가 접근하게 된다.  
즉, 하나의 데이터를 공유해서 작업하게 된다.  
+ synchronized 키워드는 통해 공유 데이터에 lock을 걸어서 먼저 작업 중이던 스레드가 작업을 완전히 마칠 때까지 다른 쓰레드가 접근해도 공유 데이터가 변경되지 않도록 보호하는 기능을 제공한다.  
+ synchornized 키워드는 어떤 것을 위한 wait, notify인지 명시적으로 알기 어렵기 때문에 Lock, Condition을 사용하기도 한다.(내부적으로는 synchornized로 구현되어 있다.)
+ 스레드 로컬을 사용하면 각 스레드만의 고유 공간을 만들어서 사용할 수 있다.
+ java.util.concurrent 패키지에 동기화 컬렉션과 atomic 자료형을 제공한다.(concurrentHashMap, AtomicInteger 등)
    - atomic은 CAS알고리즘을 사용한다. (특정 메모리 위치와 주어진 위치의 value를 비교해 다르면 대체하지 않는다.)

### Lock
동기화 처리하는 블록을 Synchronized로 묶게 되면 해당 블록에는 하나의 스레드만 접근할 수 있다.    
만약 하나의 스레드가 들어가서 특정 조건이 만족될 때까지 반복 루프를 돌게 될 경우 빠져나오지 않는 상황이 발생할 수 있다.  
이런 상황에서 동기화의 효율을 높이기 위해 wait, notify를 사용한다.  
이 경우에는 안에서 wait을 호출하여 객체의 lock을 풀고 루프를 돌고 있는 스레드를 해당 객체의 waiting pool에 넣는다.  
그리고 조건을 만족시키도록 하는 로직에서 작업 후 notify를 호출하여 대기 중인 스레드 중 하나를 깨워준다.  
notify를 통해 스레드를 깨울 때는 랜덤하게 pool에 있는 것 중 하나를 깨우기 때문에 일반적으로는 notifyAll를 사용해서 전체를 깨운다.  
하지만 이렇게 로직을 짜다보면 wait과 notify가 어떤 것을 위한 것인지 알 수 없기 때문에 lock, condition이 추가되었고 이를 통해 명확하게 구분할 수 있게 되었다.  

<br>

concurrent.locks 패키지는 내부적으로 synchronized를 사용하여 구현되어 있지만 더욱 유연하고 세밀하게 처리하기 위해 사용된다.  
+ __Lock__ : 공유 자원에 한 번에 한 쓰레드만 read, write가 수행 가능하도록 제공하는 인터페이스
    - __ReentrantLock__ : Lock의 구현체로 임계 영역의 시작과 종료 지점을 직접 명시할 수 있다.
+ __ReadWriteLock__ : 공유 자원에 여러 개의 스레드가 read 가능하고, write는 한 스레드만 가능한 인터페이스
    - __ReentrantReadWriteLock__ : ReadWriteLock의 구현체

```java
class SomeClass { 
    private final ReentrantLock locker = new ReentrantLock(); 
    private final Condition lockerCondition = locker.newCondition(); 

    // lock으로 condition을 생성할 때 구분해서 만들어서 synchronized의 단점을 해결할 수 있다.
    //private Condition forCook = lock.newCondition(); // cook을 위한 lock
    //private Condition forCust = lock.newCondition(); // cust를 위한 lock


    public void SomeMethod () { 
        locker.lock(); // 쓰레드에 락을 겁니다.(동기화의 시작) 
        try { 
            동기화내용들... 
            lockerCondition.await(); //기존의 동기화에서 wait(); 
            lockerCondition.signal(); //기존의 동기화에서 notify(); 
            lockerCondition.signalAll(); //기존의 동기화에서 notifyAll(); 
        } catch (어떤예외들) { 
            해당예외처리... 
        } finally { 
            locker.unlock(); // 쓰레드의 락을 풉니다.(동기화 끝지점) 
        } 
    } 
}
```
이런 명확성에도 차이가 있지만 공정성에도 차이가 있다.  
synchronized는 특정 스레드에서 lock이 필요한 순간 release가 발생하면 대기열을 건너뛰는 새치기 같은 일일 발생한다.  
다른 스레드들에게 우선순위가 밀려 자원을 계속해서 할당받지 못하는 기아상태가 발생하게 되는데 이를 불공정하다고 한다.  
reentrantLock의 경우에는 생성자를 통해 fair/nonFair를 설정할 수 있어 경쟁이 발생했을 때 가장 오랫동안 기다린 스레드에게 lock을 제공한다.  
하지만 락을 요청하는 시간 간격이 긴 경우가 아니라면, 스레드를 공정하게 관리하는 것보다는 불공정하게 관리하는 것이 성능이 우수하기 때문에 불공정 방식이 더 많이 사용된다.  

<br>

정리하자면, synchronized는 블록구조를 사용하여 메서드 안에 임계영역의 시작과 끝이 있고 간단하다.  
Lock은 lock, unlock 메서드로 시작과 끝을 명시하기 때문에 명확하고 임계 영역을 여러 메서드에서 나눠서 작성할 수 있다.  

## DDD 구조
+ DDD란 비즈니스 도메인 별로 나눠서 설계하는 방식으로 여러 도메인들이 서로 상호작용하도록 도메인을 중심으로 설계하는 것을 말한다.
+ DDD의 핵심 목표는 모듈간의 의존성을 최소화하고 응집성은 최대화하는 것이다.
+ Presentation, application, domain, infrastructure 계층으로 구분되어 있다.
    - presentation -> application -> domain -> infrastructure
    - 하위 계층으로만 의존함으로서 구조가 복잡하여 발생할 수 있는 순환참조를 막을 수 있다.
    - 단방향으로 구성되어있기에 각 계층별 로직을 쉽게 이해할 수 있다.
    - infrastructure는 domain의 추상화를 구현하는 계층으로 DIP의 활용을 극대화할 수 있다.




## 기타
+ 모듈 : 전역변수, 함수 등을 모아둔 파일
+ 패키지 : 모듈을 디렉터리 형식으로 구조화한 라이브러리
+ 라이브러리 : 공통적인 부분을 반복 개발하는 것을 피하기 위해서 필요한 프로그램만 모아 놓은 것


