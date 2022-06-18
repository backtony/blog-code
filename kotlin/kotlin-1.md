# Kotlin - 기본 문법


## 변수와 자료형

### 선언과 추론
변수는 val, var 키워드를 사용하여 선언할 수 있습니다.  
+ val 
    - 선언 시 최초로 지정한 변수의 값으로 초기화됩니다. 
    - __자바의 final__ 과 같습니다.
+ var
    - 최초로 지정한 변수의 초기값이 있더라도 수정이 가능합니다.
    - 자바의 일반 변수와 같습니다.

```kotlin
// 선언키워드 변수명: 자료형 = 값
val username: String = "backtony"

// 자료형을 지정하지 않아도 컴파일러가 대입 값을 보고 자료형을 추론
var username = "backtony"

// 초기값을 지정하지 않을 경우에는 반드시 자료형을 입력해줘야 합니다.
var username: String
```

### 자료형
코틀린의 자료형은 참조형 자료형을 사용합니다.  
보통 프로그래밍 언어는 자료형으로 기본형과 참조형을 구분합니다.  
예를 들어 자바같은 경우에는 int, long 등 기본형과 String, Date와 같은 참조형을 모두 사용하지만 __코틀린에서는 참조형만 사용합니다.__  
__참조형으로 선언한 변수는 성능 최적화를 위해 코틀린 컴파일러에서 다시 기본형으로 대체됩니다.__  
따라서 코틀린에서는 참조형을 기본형으로 고려하는 등의 최적화를 신경 쓰지 않아도 됩니다.  

#### 정수 자료형
+ Long
+ Int
+ Short
+ Byte

```kotlin
val num1 = 127 // Int형 추론
val num2 = 127L // Long형으로 추론
val num3: Byte = 127 // 명시적 Byte형
val num4: Short = 127 // 명시적 Short형
```

#### 부호가 없는 정수 자료형
+ ULong
+ UInt
+ UShort
+ UByte

```kotlin
val num1: UInt = 123u
val num2: UShort = 123u
val num3: ULong = 123uL
val num4: UByte = 123u

// 참고 숫자가 길어지면 언더바로 자리값 구분
val num = 1_000_000
```

#### 실수 자료형
+ Double
+ Float

```kotlin
val num1 = 3.14 // Double형 추론
val num2 = 3.14F // F에 의해 Float형 추론
```

#### 최댓값, 최솟값
+ MAX_VALUE : 최대값
+ MIN_VALUE : 최소값

```kotlin
println(Int.MAX_VALUE)
println(Int.MIN_VALUE)
```
각 자료형에서 최대값, 최소값을 얻을 수 있습니다.

#### 문자 자료형
+ Char

```kotlin
val ch = 'c' // Char 추론
cal ch: Char

val ch = 'A'
println(ch + 1) // B

val ch: Char = 65 // 숫자로 선언하는 것은 오류


val code: Int = 65
val ch2: Char = code.toChar() // code에 해당하는 문자열 할당
```

#### 문자열 자료형
```kotlin
var str1: String = "hello"

// $ 기호를 사용한 문자열 출력
var str2 = "a is ${str1}"
println(str2) // a is hello

var a = 1
var b = "${a+1}"
println(b) // 2

// $를 사용하고 싶다면 이스케이프 문자를 사용
var c = "\$is dollar sign"


// 작성한 그대로 대입하기 위해서는 """를 사용합니다.
var str = """
        안녕하세요
        backtony입니다.
    """.trimIndent()

println(str)
//안녕하세요
//backtony입니다.
```

<br>

## 자료형 검사 및 변환

### 널 허용 ?
__코틀린은 변수에 기본적으로 null을 허용하지 않기 때문에 NPE 예외에 대해 고려할 필요가 없습니다.__  
하지만 ?를 붙이면 null을 허용할 수 있습니다.
```kotlin
var str1 = null // 오류 null을 허용하지 않음
var str2: String? = null
```

### 세이프 콜 ?.
?를 사용해서 null이 허용하는 타입으로 선언한다면 변수에 할당된 값이 null일 수 있기 때문에 기본적으로 제공하는 함수를 사용할 수 없습니다.  
?. 를 사용하면 세이프하게 제공하는 함수를 사용할 수 있습니다.  
즉, __null이라면 그냥 null을 반환해버리고 null이 아니라면 함수를 호출합니다.__
```kotlin
var str1: String? = "backtony"
// 타입이 null을 허용하기 때문에 null일 수 있어 length 실행 불가능 
println("str1: $str1 length: ${str1.length}") 


var str1: String? = "backtony"
println("str1: $str1 length: ${str1?.length}") // str1: backtony length: 8

str1 = null
println("str1: $str1 length: ${str1?.length}") // str1: null length: null
```

### non-null 단정 기호 !!
코딩을 하다보면 null을 허용하는 타입일지라도 어느 순간 반드시 null이 아닌 타입이 될 때가 있습니다.  
이때는 !!. 를 사용하여 컴파일러가 null 검사를 무시하도록 만들어줍니다.  
이는 실행 중 NPE를 발생시킬 수 있으므로 주의해야 하며 권장하는 방식은 아닙니다.  
```kotlin
var str1: String? = null
println("str1: $str1 length: ${str1!!.length}") // 컴파일에는 문제가 없지만 실행 시 NPE
```

### if문으로 null 체크
앞서 ?는 null은 허용하는 타입이고 ?.를 사용하면 세이프하게 함수를 사용할 수 있다고 했습니다.  
이렇게 간단하게 사용할 수도 있지만 if문을 사용하여 체크할 수도 있습니다.
```kotlin
fun main() {
    var username: String? = "backtony"

    var len = if(username != null) username.length else -1
    println(len)
}
```
username 변수는 null을 허용하는 타입이라 함수를 사용할 수 없지만 if문에서 null이 아닌 것을 확인했기 때문에 컴파일러는 이를 확인하고 변수에서 함수를 사용할 수 있도록 해줍니다.  

### 세이프 콜(?.)과 엘비스 연산자(?:)
```kotlin
fun main() {
    var username: String? = "backtony"

    // username이 null인 경우 -1를 반환
    println("length: ${username?.length ?: -1}")
}
```
세이프 콜을 사용하면 값이 null일 경우 함수를 호출해도 null을 반환해준다고 했습니다.  
__엘비스 연산자를 사용하면 null이 아니라 다른 값을 반환해주도록 할 수 있습니다.__

<br>

## 자료형 비교 및 변환
코틀린의 자료형은 모두 참조형으로 선언되지만 컴파일을 거쳐서 최적화될 때 Int, Long, Short와 같은 자료형은 기본 자료형으로 변환됩니다.  
참조형과 기본형의 저장 방식은 서로 다르기 때문에 자료형을 비교하거나 검사할 때는 이와 같은 특징을 이해하고 있어야 합니다.  
코틀린에서는 자료형이 서로 다른 변수를 비교하거나 연산할 수 없습니다.  
따라서 연산하기 위해서는 자료형이 같도록 변환해야 합니다.  

### 자료형 변환
__자바에서는 작은 타입의 변수를 큰 타입의 변수에 대입하면 자동으로 형변환이 이뤄지지만 코틀린에서는 이를 허용하지 않습니다.__  
코틀린에서는 이를 대신해 자료형 변환 메서드를 제공합니다.
+ toByte
+ toLong
+ toShort
+ toInt
+ toFloat
+ toDouble
+ toChar

```kotlin
fun main() {
    val a: Int = 1
    val b: Double = a // 컴파일 에러
    val c: Double = a.toDouble() // 형변환 메서드 사용
    val result = 1L + 3 // Long + Int -> Long 형
}
```
__표현식에서 자료형이 서로 다른 값을 연산하면 자료형 범위가 큰 자료형으로 형변환되어 계산됩니다.__  

### 비교
자료형을 비교할 때는 단순히 값만 비교하는 방법과 참조 주소까지 비교하는 방법이 있습니다.  
+ 이중 등호(==)
    - 단순히 값만 비교할 때 사용합니다.
    - 값이 동일하면 true, 다르면 false
+ 삼중 등호(===)
    - 참조 주소를 비교합니다.
    - 참조가 동일하면 true, 다르면 false

```kotlin
fun main() {
    val a: Int = 128
    val b: Int = 128
    println(a == b) // true
    println(a === b) // true
}
```
참조형으로 선언된 a와 b는 __코틀린 컴파일러가 기본형으로 변환하여 저장__ 합니다.  
즉, 여기서 삼중 등호가 비교하는 값도 저장된 값인 128이 됩니다.  

<br>

```kotlin
fun main() {
    val a: Int = 128
    val b: Int? = 128
    println(a == b) // true
    println(a === b) // false
}
```
a는 기본형으로 변환되어 스택에 128이라는 값 자체를 저장합니다.  
__b는 널을 허용하는 타입이므로 참조형으로 저장되어 b에는 128이 저장된 힙의 참조 주소가 저장됩니다.__  
따라서 삼중 등호로 비교하게 되면 false가 나옵니다.  
```kotlin
fun main() {
    val a: Int = 128
    val b = a
    println(a == b) // true
    
    val c: Int? = a
    val d: Int? = a
    val e: Int? = c
    println(c == d) // 내부값 확인으로 true
    println(c === d) // 내부값은 갖지만 참조값은 다르므로 false
    println(c === e) // 모두 같으므로 true
}
```
c와 d는 서로 다른 주소값을 가지고 내부 값만 같습니다.  
c의 주소값을 e에 대입했기 때문에 e는 결국 c와 같은 주소값을 가리키게 됩니다.  

<br>

__cf -128 ~ 127 값은 캐시에 저장__  
-128부터 127 범위에 있다면 해당 값들은 스택이 아니라 캐시에 그 값을 저장하고 변수는 캐시의 주소를 가리킵니다.  
따라서 위의 예시에서 128이 아니라 127이었다면 c와 d의 참조 주소값이 같아지게 됩니다.  
결론적으로 a,b,c,d의 삼중 등호 비교가 모두 true가 됩니다.  

### 스마트 캐스트
어떤 값이 정수일 수도 있고 실수일 수도 있다면 그때마다 자료형을 변환해도 되지만 컴파일러가 자동으로 형 변환을 하는 스마트 캐스트를 사용할 수 있습니다.  
대표적으로 스마트 캐스트가 적용되는 자료형은 Number형이 있습니다.  
Number형을 사용하면 숫자를 저장하기 위한 특수한 자료형 객체를 만듭니다.  
Number형으로 정의된 변수에는 저장되는 값에 따라 정수형이나 실수형 등으로 자료형이 변환됩니다.  

```kotlin
fun main() {
    var test: Number = 12.1 // Float형
    test = 12 // Int형
    test = 12L // Long 형
    test += 12.0f // Float 형 
}
```

### 자료형 검사
자료형 검사는 is 키워드를 사용합니다.  

```kotlin
fun main() {
    val num = 123

    if (num is Int)
        println("num is Int")
    else if (num !is Int)
        println("num is not Int")
}
```
<Br>

__is는 변수의 자료형을 검사한 다음 그 변수를 해당 자료형으로 변환하는 기능도 제공합니다.__  
Any는 코틀린의 최상위 기본 클래스로 어떤 자료형으로도 될 수 있는 특수한 자료형입니다.  
이때 is를 사용하여 자료형을 검사하면 검사한 자료형으로 스마트 캐스트 됩니다. 
```kotlin
fun main() {
    val x: Any
    x = "hello"
    if(x is String)
        print(x.length) // x가 String으로 스마트 캐스트 되었기 때문에 length 사용 가능
}
```
<br>

### as 스마트 캐스트
as를 사용해도 스마트 캐스트를 할 수 있습니다.  
as는 형 변환이 가능하지 않으면 예외를 발생시킵니다.
```kotlin
val x: String = y as String
```
위의 경우 y가 null이 아니면 String으로 형 변환되어 x에 할당되고 y가 null이면 예외가 발생합니다.
```kotlin
val x: String? = y as? String
```
null 가능성까지 고려하여 예외 발생을 피하려면 ?를 사용합니다.  


### 묵시적 변환
Any형은 자료형이 특별히 정해지지 않은 경우 사용하는 코틀린의 모든 클래스의 뿌리가 되는 클래스입니다.  
Any형은 무엇이든 될 수 있기 때문에 __필요한 자료형으로 자동 변환__ 할 수 있고 이를 묵시적 변환이라고 합니다.  
```kotlin
fun main() {
    var a: Any = 1 // a가 1로 초기화될 때 Int형이 됨
    a = 20L // Int형이 었던 a는 Long 형이 됨
    println("a type = ${a.javaClass}") // long
}
```






