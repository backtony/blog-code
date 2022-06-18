# Java 람다식

## 1. 람다식
---
+ y = f(x) 형태의 함수로 구성된 프로그래밍 기법
+ 데이터 포장 객체를 생성후 처리하는 것 보다 데이터를 바로 처리하는 것이 속도에 유리
+ 자바는 람다식을 함수적 인터페이스의 익명 구현 객체로 취급
    - 함수적 인터페이스 : 한 개의 메서드를 가지고 있는 인터페이스

```java
Runnable runnable = new Runnable(){
    public void run(){ ...} // 익명 구현 객체
}

Runnable runnable = () -> {...} // 람다식
// () 는 run의 ()를 의미하고, {}는 run 뒤의 중괄호를 의미
```
매개변수는 ()로 들어오고 코드 블록은 {}이다. 람다식은 인터페이스의 메서드를 구현한 익명 구현 객체가 된다.  
<br>

## 2. 람다식 기본 문법
---
```java
(타입 매개변수,..) -> {실행문;...}
(int a) -> { System.out.println(a); }

매개 타입은 런타임시 대입값에 따라 타입 자동 인식 -> 생략 가능
(a) -> { System.out.println(a); }

매개변수가 하나일 경우 () 생략 가능
a -> { System.out.println(a); }

실행문이 하나일 경우 중괄호 생략 가능
a -> System.out.println(a); 

매개변수가 없다면 괄호 생략 불가능
() -> System.out.println(a);

리턴값이 있는 경우 return 문 사용
(x,y) -> { return x+y; }

중괄호에 return 문만 있다면 리턴 생략 가능
(x,y) -> return x+y; 
```
<br>

## 3. 타겟 타입과 함수적 인터페이스
---
### 타겟 타입
```java
인터페이스 변수 = 람다식 // 인터페이스가 타겟타입
```
+ 람다식이 대입되는 인터페이스
+ 익명 구현 객체를 만들 때 사용할 인터페이스

### 함수적 인터페이스
+ 모든 인터페이스는 람다식의 타겟 타입이 될 수 없다.
    - 람다식은 하나의 메서드를 정의하기 때문
    - 하나의 추상 메서드만 선언된 인터페이스만 타겟 타입이 될 수 있음
+ 함수적 인터페이스
    - 하나의 메서드만 선언된 인터페이스
+ @FunctionalInterface 애노테이션
    - 하나의 추상 메서드만을 가지는지 컴파일러가 체크
    - 두 개 이상이면 컴파일 오류 발생
    - 없어도 되긴 함

```java
@FunctionalInterface
public interface Myfun{
    public void method();
}

매개변수가 있는 람다식
Myfun fi = (x) -> {...}
fi.method(5);

리턴값이 있는 람다식
Myfun fi = (x) -> {... return 값;}
int result = fi.method(5);
```
<br>

## 4. 클래스 멤버와 로컬 변수 사용
---
+ 클래스 멤버 사용
    - 람다식 실행 블록에는 클래스의 멤버인 필드와 메서드를 제약없이 사용할 수 있다.
    - 람다식 실행 블록 내에서 this는 람다식을 실행한 객체의 참조이다.
+ 로컬 변수 사용
    - 람다식은 함수적 인터페이스의 익명 구현 객체를 생성한다
    - 람다식에서 사용하는 외부 로컬 변수는 final 특성을 갖는다.

```java
public class Ex{
    public int out = 10; // 람다식 내부 사용으로 final 특성을 가짐

    class Inner {
        int inner =20; // 람다식 내부 사용으로 final 특성을 가짐

        void method(){
            Myfun fi = ()->{
                System.out.println(out); // 10
                System.out.println(Ex.this.out); // 10
                // Ex.this는 Ex를 가리킴

                System.out.println(inner); // 20
                System.out.println(this.inner); // 20
                // this는 Inner을 가리킴
            }
        }
    }
}
```
<br>

## 5. 메서드 참조
---
```java
(x,y) -> Math.max(x,y); // 람다식

Math::max; // 메서드 참조

IntBinaryOperator 인터페이스는 두개의 int 매개값을 받아 int값을 리턴하는 메서드를 가짐
동일한 매개값과 리턴타입을 갖는 Math클래스의 max 메서드를 참조 가능
IntBinaryOperator operator = Math::max
```

+ 메서드를 참조해서 매개변수의 정보 및 리턴타입을 알아내어 람다식에서 불필요한 매개변수를 제거하는 것이 목적
+ 메서드 참조도 람다식과 마찬가지로 인터페이스의 익명 구현 객체로 생성됨
    - 타겟 타입에서 추상 메서드의 매개변수 및 리턴 타입에 따라 메서드 참조도 달라짐

<br>

### 정적 메서드와 인스턴스 메서드 참조
```java
public class Calcu{
    public static int staticMethod(int x, int y){
        return x+y;
    }
    public int instanceMethod(int x, int y){
        return x+y;
    }
}

// 정적 메서드
IntBinaryOperator operator;
operator = Calcu::staticMethod;

// 인스턴스 메서드
Calcu a = new Calcu();
operator = a::instanceMethod;
```

+ 정적 메서드 참조
    - 클래스 :: 메서드
    - 정적 메서드는 인스턴스 생성이 필요 없기 때문
+ 인스턴스 메서드 참조
    - 참조변수 :: 메서드
    - 인스턴스 메서드는 인스턴스 생성이 필요하기 때문

<br>

### 매개변수의 메서드 참조
```java
(a,b) -> {a.instanceMethod(b)}
a클래스::instanceMethod // a클래스의 instanceMethod를 호출하여 매개값으로 b를 준다.
```
<br>

### 생성자 참조
```java
(a,b) -> {return new 클래스(a,b)}
클래스::new // 클래스의 생성자에 a,b가 매개변수로 들어간다.
```