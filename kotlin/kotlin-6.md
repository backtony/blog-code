# Kotlin - 다양한 클래스와 인터페이스


## 추상 클래스와 인터페이스
추상 클래스는 abstract 키워드와 함께 선언하며 추상 클래스로부터 일반적인 객체를 생성하는 방법으로 인스턴스화 할 수 없습니다.  
추상 클래스를 상속하는 하위 클래스가 어떻게 만들어야 하는지를 나타내는 용도로 사용됩니다.  
```kotlin
abstract class 클래스명
```
추상 클래스를 설계할 때는 멤버인 프로퍼티나 메서드도 abstract로 선언될 수 있습니다.  
이때는 추상 프로퍼티, 추상 메서드라고 합니다.  
```kotlin
abstract class Vehicle(val name: String, val color: String){
    abstract var maxSpeed: Double
    var year = 2015

    abstract fun start()
    abstract fun end()
    
    fun display() = println("display")
}

// 생성자에서 maxSpeed 오버라이딩
class Car(name:String, color:String, override var maxSpeed: Double) : Vehicle(name,color){
    override fun start() {
        TODO("Not yet implemented")
    }

    override fun end() {
        TODO("Not yet implemented")
    }

}
```
추상 클래스에는 일반 프로퍼티나 메서드를 만들 수 있기 때문에 공통 프로퍼티와 메서드를 미리 만들어 둘 수 있습니다.  
만일 추상 클래스로부터 하위 클래스를 생성하지 않고 단일 인스턴스로 객체를 생성하려면 object를 사용해서 지정할 수 있습니다.  
```kotlin
abstract class Vehicle(val name: String, val color: String){
    abstract var maxSpeed: Double
    var year = 2015

    abstract fun start()
    abstract fun end()

    fun display() = println("display")
}

val Car = object: Vehicle("name","color") {
    override var maxSpeed: Double
        get() = TODO("Not yet implemented")
        set(value) {}

    override fun start() {
        TODO("Not yet implemented")
    }

    override fun end() {
        TODO("Not yet implemented")
    }

}
```
익명 객체를 지정하는 object 키워드를 사용하면 됩니다.  

### 인터페이스
인터페이스에는 abstract로 정의된 추상 메서드나 일반 메서드가 포함됩니다.  
다른 객체 지향 언어와 다르게 메서드에 구현 내용이 포함될 수 있습니다.  
```kotlin
interface 인터페이스명 [: 인터페이스명] {
    추상 프로퍼티 선언
    추상 메서드 선언

    [일반 메서드 선언{...}]
}
```
인퍼테이스는 추상 클래스와 달리 abstract를 붙여주지 않아도 기본적으로 추상 프로퍼티와 추상 메서드로 지정됩니다.  
그리고 메서드에 기본 구현부가 있으면 일반 메서드로서 기본 구현을 가집니다.(자바의 default 메서드)  
상태를 저장할 수 없기에 프로퍼티에는 기본값을 지정할 수 없습니다.  
<br>

```kotlin
interface Pet {
    var category: String // 추상 프로퍼티
    fun feeding() // 추상 메서드

    fun patting(){ // 일반 메서드로 구현부를 포함
        println("patting")
    }
}

class Cat(override var category: String): Pet{
    override fun feeding() {
        TODO("Not yet implemented")
    }
}
```


#### 게터로 구현한 프로퍼티
인터페이스에서는 프로퍼티에 값을 저장할 수 없다고 했습니다.  
단 val의 경우에는 게터를 통해 필요한 내용을 구현할 수 있습니다.  
```kotlin
interface Pet {
    val msg: String
        get() = "Hello"
}
```
val 로 선언되었더라도 초기화는 할 수 없고 게터를 통해 반환값만 지정할 수 있습니다.  
하지만 여전히 보조 필드인 field를 사용할 수 없고 var로 선언하더라도 보조 필드를 사용할 수 없기 때문에 받을 value를 저장할 수도 없습니다.  

#### 여러 인터페이스 구현
클래스 상속은 1개만 가능하지만 인터페이스 상속은 여러 개 가능합니다.  
```kotlin
interface Bird {
    fun jump() {
        println("bird jump")
    }
}

interface Horse {
    fun jump() {
        println("Horse jump")
    }
}

class Pegasus : Bird, Horse {
    override fun jump() {
        super<Horse>.jump()
    }
}
```
상속하는 인터페이스에서 이름이 같은 경우가 있다면 super<인터페이스이름>.메서드를 사용해서 구분할 수 있습니다.  

#### 인터페이스의 위임
인터페이스에서 by 위임자를 사용할 수 있습니다.  
```kotlin
interface A {
    fun funA() {}
}

interface B {
    fun funB() {}
}

class C(val a: A, val b: B) {
    
    fun funC() {
        // 위임 없이 일반 호출
        a.funA() 
        b.funB()
    }
}

// A, B 인터페이스를 구현하는데 이 구현을 a와 b에 위임한다.
class DeligatedC(a: A, b: B) : A by a, B by b {
    fun funC() {
        // 위임했으므로 바로 호출 가능
        funA()
        funB()
    }
}
```



<br>

## 데이터 클래스와 기타 클래스
### 데이터 클래스
보통 데이터 전달을 위한 객체를 DTO라고 하고 자바에서는 POJO라고 부르기도 합니다.  
DTO는 구현 로직을 갖지 않고 게터, 세터, toString, equals 등과 같은 데이터를 표현하거나 비교하는 메서드를 가져야 합니다.  
자바에서는 이를 모두 정의하려면 코드가 길어지지만 코틀린에서는 간단하게 데이터 클래스가 해결해줍니다.  
데이터 클래스는 내부적으로 다음과 같은 메서드들이 자동으로 생성됩니다.  
+ 프로퍼티를 위한 게터/세터
+ 비교를 위한 equals와 키 사용을 위한 hashCode
+ 프로퍼티를 문자열로 변환해 순서대로 보여주는 toString()
+ 객체 복사를 위한 copy 
+ 프로퍼티에 상응하는 component1() 등

```kotlin
data class Customer(var name: String, var email: String)
```
데이터 클래스는 다음 조건을 만족해야 합니다. 
+ 주 생성자는 최소한 하나의 매개변수를 가져야 한다.
+ 주 생성자의 모든 매개변수는 val, var로 지정된 프로퍼티여야 한다.
+ __데이터 클래스는 abstract, open, sealed, inner 키워드를 사용할 수 없다.__

즉, 오로지 데이터를 기술하기 위한 용도로만 사용됩니다.  
하지만 필요하다면 추가로 부 생성자나 init 블록을 넣어 데이터를 위한 간단한 로직을 포함할 수도 있습니다.  
```kotlin
data class Customer(var name: String, var email: String) {
    var job: String = "unknown"

    constructor(name: String, email: String, job: String) : this(name, email) {
        this.job = job
    }

    init {

    }
}
```
<br>

앞서 자동으로 생성되는 메서드들이 여럿 있다고 했는데 다른 부분은 쉬우니 copy만 보고 넘어가겠습니다.
copy를 사용하면 데이터 객체를 복사하되 다른 프로퍼티 값을 가지는 것만 명시하여 변경할 수 있습니다.
```kotlin
data class Customer(var name: String, var email: String)

val cus1 = Customer("backtony","backtony@gmail.com")
val cus2 = cus1.copy(name = "tony")
```


#### 객체 디스트럭처링하기
디스트럭처링 한다는 것은 객체가 가지고 있는 프로퍼티를 개별 변수로 분해하여 할당하는 것을 말합니다.  
변수를 선언할 때 소괄호를 사용해서 분해하고자 하는 객체를 지정합니다.  
```kotlin
data class Customer(var name: String, var email: String)

fun main() {
    val cus1 = Customer("backtony", "backtony@gmail.com")
    val (name, email) = cus1
    println("$name $email")

    var (_, email) = cus1 // 필요 없는 경우 언더바 사용
}
```
<br>

개별적으로 프로퍼티를 가져오기 위해 componentN 메서드를 사용할 수 있습니다.
```kotlin
data class Customer(var name: String, var email: String)

fun main() {
    val cus1 = Customer("backtony", "backtony@gmail.com")
    val name = cus1.component1() // 첫 번째 프로퍼티 가져오기
    println("$name")
}
```
<br>

람다식에서도 사용 가능합니다.
```kotlin
data class Customer(var name: String, var email: String)

fun main() {
    val myLambda = {
        (name,email): Customer ->
        println("$name $email")
    }

    myLambda(Customer("name","email"))
}
```

### 내부 클래스 기법
코틀린에는 2가지 내부 클래스 기법이 있습니다. 
+ 중첩 클래스 : 클래스 안에 또 다른 클래스
+ 내부 클래스 : inner 클래스

클래스 내부에 또 다른 클래스를 설계하여 내부에 두는 이유는 독립적인 클래스로 정의하기 모호한 경우나 다른 클래스에서는 잘 사용하지 않는 내부에서만 사용하고 외부에서는 접근할 필요가 없을 때가 있기 때문입니다.  
하지만 너무 남용하면 의존성이 커지고 코드가 읽기 어려워져 주의해야 합니다.  
<br>

+ 자바 내부 클래스 종류
    - 정적 클래스 : static 키워드로 외부 클래스를 인스턴스화하지 않고 바로 사용 가능한 내부 클래스
    - 멤버 클래스 : 인스턴스 클래스로도 불리며 외부 클래스의 필드나 메서드와 연동하는 내부 클래스
    - 지역 클래스 : 초기화 블록이나 메서드 내의 블록에서만 유효한 클래스
    - 익명 클래스 : 이름 없이 일회용 객체를 인스턴스화 하면서 오버라이드 메서드를 구현하는 내부 클래스
+ 코틀린 내부 클래스와 비교(자바:코틀린)
    - 정적 클래스 : 중첩 클래스(객체 생성 없이 사용 가능)
    - 멤버 클래스 : 이너 클래스(필드나 메서드와 연동하는 내부 클래스로 inner 키워드 필요)
    - 지역 클래스 : 지역 클래스(클래스 선언이 블록 안에 있음)
    - 익명 클래스 : 익명 객체(이름이 없고 일회용 객체로 object 키워드 필요)

```kotlin
// 자바의 멤버 클래스(이너)
class A { 
    class B{

    }
}

// 코틀린 이너 클래스
class A {
    inner class B {

    }
}
```
```kotlin
// 자바 정적 클래스
class A {
    static class B
}

// 코틀린 중첩 클래스
class A {
    class B{ // 코틀린에서는 아무 키워드가 없는 클래스를 중첩 클래스로 정적 클래스처럼 사용

    }
}

// 예시
class Outer {
    // 정적 내장 클래스
    class Nested {
        fun accessOuter() {
            println(country) // 컴페니언 객체에는 바로 접근 가능
            getSomething()
        }
    }

    // 정적 변수와 정적 메서드
    companion object {
        val country = "Korea"
        fun getSomething() = {}
    }
}
```

### 실드 클래스와 열거형 클래스


#### 실드 클래스
__실드형 클래스는 미리 만들어 놓은 자료형들을 묶어서 제공하는 클래스입니다.__  
어떤 의미로는 열거형 클래스의 확장이라고 볼 수 있습니다.  
+ sealed 키워드를 사용합니다.  
+ 추상 클래스와 같기 때문에 객체를 만들 수는 없습니다.  
+ 생성자는 기본적으로 private으로 이외의 생성자는 허용하지 않습니다.  
+ 같은 파일 안에서는 상속이 가능하지만 다른 파일에서는 상속이 불가능하게 제한됩니다.  
+ 블록 안에서 선언되는 클래스는 상속이 필요한 경우 open 키워드로 선언될 수 있습니다.

```kotlin
// 방법 1
sealed class Result {
    open class Success(val message: String): Result()
    class Error(val code: Int, val message: String): Result()
}

class Status: Result() // 같은 파일 내에서는 상속 가능
class Inside: Result.Success("status") // open으로 열어놨으므로 내부 클래스 상속 가능
class ErrorText: Result.Error(1,"hello") // 에러 발생 -> open으로 안 열었으므로 상속 불가능
```
```kotlin
// 방법 2 -> 블록으로 감싸지 않고 상속으로 처리
sealed class Result

open class Success(val meessage: String) : Result() // 상속
class Error(val code: Int, val message: String) : Result() // 상속

class Status : Result()
class Inside : Success("status")
```
<Br>

실드 클래스를 사용하는 이유는 특정 객체 자료형에 따라 when 문과 is에 의해 선택적으로 실행할 수 있기 때문입니다.  
```kotlin
sealed class Result

open class Success(val meessage: String) : Result()
class Error(val code: Int, val message: String) : Result()

class Status : Result()

fun main() {
    val result = Success("Good")
    val msg = eval(result)
}

fun eval(result: Result): String = when (result) {
    is Status -> "in progress"
    is Success -> result.meessage
    is Error -> result.message
}
```
__when 문에서 모든 경우의 수가 열거되었으므로 else문이 필요 없습니다.__  
이것을 이너 클래스나 중첩 클래스로 구현하려고 하면 모든 경우의 수를 컴파일러가 판단할 수 없어 else문을 가져야 합니다.  
하지만 실드 클래스를 사용하면 필요한 경우의 수를 직접 지정할 수 있습니다.  

### 열거형 클래스
열거형 클래스는 여러 개의 상수를 선언하고 열거된 값을 조건에 따라 선택할 수 있는 특수한 클래스 입니다.  
열거형 클래스는 enum 키워드와 함께 선언할 수 있고 자료형이 동일한 상수를 나열할 수 있습니다.
```kotlin
enum class 클래스 이름[(생성자)] {
    상수1[(값)], 상수2([값]), 상수3[(값)]
    [; 프로퍼티 혹은 메서드]
}
```
```kotlin
enum class Direction {
    N, E, S, W
}

enum class DayOfWeek(val num: Int) {
    MONDAY(1), TUESDAY(2), WEDNESDAY(3), THURSDAY(4), FRIDAY(5), SATURDAY(6), SUNDAY(7)

    fun isWeekDay() {
        when (num) {
            in 1..5 -> true
            else -> false
        }
    }
}

fun main() {
    val day = DayOfWeek.SATURDAY
    when (day.num) {
        in 1..5 -> println("weekday")
        else -> println("weekend")
    }

    day.isWeekDay()
}
```

### 애노테이션 클래스
애노테이션은 코드에 부가 정보를 추가하는 역할을 합니다.  
```kotlin
annotation class 애노테이션이름

// 예시
// 선언
annotation class Fancy

// 적용
@Fancy class MyClass{...}
```
애노테이션은 몇 가지 속성을 제공합니다.
+ @Target : 애노테이션이 저정되어 사용할 종류(클래스, 함수, 프로퍼티 등) 정의
    - AnnotationTarget.CLASS, FUNCTION, VALUE_PARAMETER, EXPRESSION
+ @Retention : 애노테이션을 컴파일된 클래스 파일에 저장할 것인지 실행 시간에 반영할 것인지 결정
    - AnnotationRetention.SOURCE : 컴파일 시간에 애노테이션 제거
    - AnnotationRetention.BINARY : 클래스 파일에 포함되지만 리플렉션에 의해 나타나지 않음
    - AnnotationRetention.RUNTIME : 클래스파일에 저장되고 리플렉션에 의해 나타남
+ @Repeatable : 애노테이션을 같은 요소에 여러 번 사용 가능하게 할지를 결정
+ @MustBeDocumented : 애노테이션이 API 일부분으로 문서화하기 위해 사용

<br>

애노테이션은 클래스 앞, 메서드 앞, 프로퍼티 앞, 반환할 때는 값 앞에 표기하고 소괄호로 값과 애노테이션을 한번에 감싸서 사용할 수 있습니다.
```kotlin
@Fancy class MyClass{
    @Fancy fun myMethod(@Fancy myProperty: Int): Int {
        return (@Fancy 1)
    }
}
```
<Br>

만약 생성자에 애노테이션을 붙이고 싶다면 constructor를 명시해야 합니다.
```kotlin
class Foo @Fancy constructor(myDependency : MyDependency)
```
<Br>

게터와 세터에도 적용할 수 있습니다.
```kotlin
class Foo {
    var x: MyDependency? = null
        @Fancy set
}
```
<Br>

애노테이션에 매개변수를 지정하고자 한다면 생성자를 통해 지정합니다.
```kotlin
annotation class Special(val why: String) // 애노테이션 정의

@Special("example") class Foo {} // 애노테이션 사용하는 곳에서 속성값으로 넘긴다.
```
매개변수로 사용될 수 있는 자료형은 다음과 같습니다.
+ 자바의 기본형과 연동되는 자료형(Int, Long 등)
+ 문자열
+ 열거형
+ 기타 애노테이션
+ 클래스(클래스 이름::class)
+ 위의 목록을 가지는 배열

<br>

애노테이션이 또 다른 애노테이션을 가지고 있을 때는 @ 기호를 사용하지 않아도 됩니다.
```kotlin
annotation class ReplaceWith(val expression: String) // 첫 번째 애노테이션 클래스 정의

annotation class Deprecated( // 두 번째 애노테이션 클래스 정의
    val message: String,
    val replaceWith: ReplaceWith = ReplaceWith("") // @ 없이 정의
)

// 사용
@Deprecated("....", ReplaceWith("....)) // @ 없이 사용
```
<br>

애노테이션의 인자로 특정 클래스가 필요하면 코틀린의 KClass를 사용해야 합니다.  
그러면 코틀린 컴파일러가 자동적으로 자바 클래스로 변환합니다.  
이후에 자바 코드에서도 애노테이션 인자를 사용할 수 있게 됩니다.  
```kotlin
annotation class Ann(val arg1: KClass<*>, val arg2: KClass<..>)

@Ann(String::class, Int::class) class MyClass
```
<br>

## 연산자 오버로딩
연산자 오버로딩이란 operator 키워드를 사용해 클래스의 다형성의 한 경우로 플러스와 같은 연산자에 여러 가지 다른 의미의 작동을 부여할 수 있습니다.  
연산자를 사용하면 관련 멤버 메서드를 호출하는 것과 같습니다.
```kotlin
val a = 5
val b = 10
print(a+b) // a.plus(b)와 동일
```
<br>

코틀린의 표준 라이브러리를 살펴보면 다음과 같습니다.
```kotlin
operator fun plus(other: Byte) : Int
operator fun plus(other: Short) : Int
operator fun plus(other: Int) : Int
operator fun plus(other: Long) : Int
...
```
operator 키워드를 사용해 plus() 함수가 다양한 자료형으로 선언되어 있는 것을 확인할 수 있습니다.  
<br>

클래스를 하나 만들고 + 연산을 오버로딩 해봅시다.
```kotlin
class Point(var x: Int = 0, var y: Int = 10) {
    operator fun plus(p: Point): Point {
        return Point(x + p.x, y + p.y)
    }
}

fun main() {
    val p1 = Point(1, 2)
    val p2 = Point(3, 4)

    var point = Point()
    point = p1 + p2
    println("${point.x} , ${point.y}") // 4 6
    
}
```

### 연산자 종류
+ plus : 덧셈
+ minus : 뺄셈
+ times : 곱셈
+ div : 나눗셈
+ rem : 나머지 연산
+ rangeTo : .. 범위 연산


