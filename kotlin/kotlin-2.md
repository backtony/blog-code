# Kotlin - 함수와 함수형 프로그래밍

## 함수 선언하고 호출
### 함수 구조
```kotlin
// fun 함수이름 (인자: 타입, 인자: 타입): 반환타입 {바디 리턴}
fun sum(a: Int, b: Int): Int {
    var sum = a + b
    return sum
}
```
<br>

중괄호 안에 바디부가 한 줄이라면 중괄호와 리턴문을 생략할 수 있습니다.
```kotlin
fun sum(a: Int, b: Int): Int  = a + b
```
<br>

더한 값이 Int인 것은 컴파일러가 추론할 수 있기 때문에 반환타입도 생략이 가능합니다.
```kotlin
fun sum(a: Int, b: Int) = a + b
```
<Br>

__인자와 매개변수의 차이__  
함수의 인자와 매개변수를 혼동하기 쉬운데 이는 명확히 구분할 수 있는 개념입니다.  
함수를 선언할 때는 매개변수라고 부르고 함수를 호출할 때는 인자라고 부릅니다.  
즉, sum 함수를 선언하는 부분의 a: Int, b: Int는 매개변수이고 sum(3,2)처럼 할수를 호출할 때 3, 2는 인자가 됩니다.  
이 인자는 함수 선언 부분에 있는 a와 b로 복사되어 전달됩니다.  


### 반환값이 없는 함수
자바에서는 Void로 반환타입이 없음을 표현합니다.  
코틀린에서는 이를 Unit으로 표현하지만 완벽하게 Void를 대체하는 것은 아닙니다.  
자바에서 void는 정말로 아무것도 반환하지 않는 것을 의미하지만 Unit이라는 특수한 객체를 반환합니다.
```kotlin
fun printSum(a: Int, b: Int): Unit {
    print("sum of a + b : ${a+b})
}
```
<br>

반환타입이 Unit인 경우 반환타입을 생략할 수 있습니다.
```kotlin
fun printSum(a: Int, b: Int) {
    print("sum of a + b : ${a+b})
}
```

### 함수 인자 디폴트
```kotlin
fun add(name: String, email: String = "defualt")
```
코틀린의 매개변수에는 디폴트 값을 줄 수 있습니다.  
따라서 호출하는 쪽에서 위의 함수를 호출하는데 name값만 넘기면 email 값은 자동으로 선언해둔 default값으로 들어갑니다.  

### 매개 변수 이름으로 호출
```kotlin
fun nameParam(x: Int = 100, y: Int = 200, z: Int){
    println(x+y+z)
}

fun main() {
    nameParam(x = 200, z = 100) // y는 디폴트값 사용
    nameParam(z = 100) // x, y는 디폴트값 사용
}
```
코틀린에서는 함수의 매개변수의 이름과 함께 호출하는 기능을 제공합니다.  

### 가변인자 
가변인자는 인자의 개수가 변한다는 의미입니다.  
가변 인자를 사용하면 함수는 하나만 정의해 놓고 여러 개의 인자를 받을 수 있습니다.  
함수를 선언할 때 매개변수 왼쪽에 vararg 키워드를 붙이면 됩니다.
```kotlin
fun normalVarargs(vararg counts: Int){
    for (num in counts){
        print(num)
    }
}

fun main() {
    normalVarargs(1,2,3,4) // 인자 4개 구성
    normalVarargs(1,2,3) // 인자 3개 구성
}
```
가변인자를 사용하게 되면 __배열__ 로 들어가게 됩니다.  
즉, 타입 값을 Int로 명시해주었으니 들어간 인자값들은 Int형 배열에 담기게 됩니다.  


## 함수형 프로그래밍
함수형 프로그래밍은 순수 함수를 작성하여 프로그램의 부작용을 줄이는 프로그래밍 기법입니다.  
함수형 프로그래밍에서는 람다식과 고차 함수를 사용합니다.  
+ 함수형 프로그래밍의 정의와 특징
    - 순수 함수를 사용해야 한다.
    - 람다식을 사용할 수 있다.
    - 고차 함수를 사용할 수 있다.

하나씩 살펴봅시다.

### 순수 함수
함수가 같은 인자에 대해서 항상 같은 결과를 반환하면 __부작용이 없는 함수__ 라고 합니다.  
__부작용이 없는 함수가 함수 외부의 어떤 상태도 바꾸지 않는다면 순수 함수__ 라고 부릅니다.  
이런 특성 때문에 순수 함수는 스레드에 사용해도 안전하고 코드를 테스트하기 쉽다는 장점도 있습니다.  
+ 순수함수 조건
    - 같은 인자에 대해서 항상 같은 값을 반환
    - 함수 외부의 어떤 상태도 바꾸지 않는다.

```kotlin
fun sum(a: Int, b: Int): Int {
    return a+b
}
```
위 함수는 같은 인자에 대해서 항상 같은 반환값을 뱉어내고 함수 안에서 함수 외부의 어떤 변수 상태도 바꾸지 않기 때문에 순수 함수입니다.  


### 람다식
람다식은 다른 함수의 인자로 넘기는 함수, 함수의 결과값을 반환하는 함수, 변수에 저장하는 함수를 말합니다.  
람다식은 뒤에서 자세히 다룹니다.

### 일급 객체
함수형 프로그래밍에서는 함수를 일급 객체로 생각합니다.  
람다식 역시 일급 객체의 특징을 갖고 있습니다.
+ 일급 객체 특징
    - 일급 객체는 함수의 인자로 전달할 수 있다.
    - 일급 객체는 함수의 반환값에 사용할 수 있다.
    - 일급 객체는 변수에 담을 수 있다.

만약 함수가 일급 객체면 일급 함수라고 부릅니다.  
일급 함수에 이름이 없는 경우 람다식 함수 또는 람다식이라고 합니다.  
__즉, 람다식은 일급 객체의 특징을 가진 이름 없는 함수입니다.__  

### 고차 함수
__고차 함수란 다른 함수를 인자로 사용하거나 함수를 결과값으로 반환하는 함수__ 를 말합니다.  
즉, 일급 객체 또는 일급 함수를 서로 주고받을 수 있는 함수가 고차 함수입니다.  
```kotlin
fun main() {
    println(highFunc({x, y -> x+y}, 10, 20)) // 람다식 함수를 인자로 넘김
}

fun highFunc(sum: (Int, Int) -> Int, a: Int, b: Int): Int = sum(a, b) 
```
highFunc 함수는 sum이라는 함수형 매개변수를 받을 수 있기에 main에서는 람다식으로 넘겼습니다.  
일급 함수인 람다식을 인자로 사용하고 있기 때문에 고차 함수에 해당합니다.  

<br>

## 고차 함수와 람다식

### 일반 함수를 인자나 반환값으로 사용하는 고차함수
고차 함수는 인자나 반환값으로 함수를 사용합니다.  
```kotlin
fun main() {
    val res = mul(sum(3, 3), 3) // 인자로 함수를 사용 -> 고차함수
}
fun sum(a: Int, b: Int ) = a+b
fun mul(a: Int, b: Int ) = a*b
```
```kotlin
fun main() {
    println(funFunc())
}

fun funFunc(): Int {
    return sum(2,2) // 함수의 반환 값으로 함수를 사용 -> 고차함수
}

fun sum(a: Int, b: Int ) = a+b
```

### 람다식을 인자나 반환값으로 사용하는 고차함수
람다식의 기본 형태는 다음과 같습니다.
```kotlin
          // 람다식의 자료형 선언                   // 람다식의 매개변수 
// 변수명: (인자 자료형, 인자 자료형) -> 반환 자료형 = {매개변수명: 자료형, 매개변수명: 자료형 -> 람다식 처리 내용}
val multi: (Int, Int) -> Int = {x: Int, y: Int -> x * y}
```
람다식의 자료형 선언은 람다식 매개변수에 자료형이 명시된 경우 생략이 가능합니다.  
반면에 선언 자료형이 명시되어 있다면 람다식의 매개변수는 생략이 가능합니다.  
```kotlin
val multi: (Int, Int) -> Int = {x: Int, y: Int -> x * y} // 정석
val multi = {x: Int, y: Int -> x * y} // 선언 자료형 생략
val multi: (Int, Int) -> Int = {x, y -> x * y} // 람다식 매개변수 자료형 생략
```
<br>


변수에 할당하는 람다식 함수는 다음과 같습니다. 
```kotlin
fun main() {
    val result: Int
    val multi = { x: Int, y: Int -> x * y }// 일반 변수에 람다식 할당
    result = multi(10, 20) // 람다식이 할당된 변수는 함수처럼 사용 가능
}
```
<Br>

만약 람다식의 처리 내용이 2줄 이상이라면 마지막 표현식이 반환값이 됩니다.
```kotlin
val multi = { x: Int, y: Int -> 
    println("x + y")
    x * y // 마지막 표현식이 반환
} 
```
<br>

람다식에 매개변수와 반환값이 없다면 다음과 같이 사용합니다.
```kotlin
val greet: () -> Unit = { print("hello")}
```
<br>

람다식을 함수의 인자로 넘길 수 있습니다.
```kotlin
fun main() {
    var result: Int
    result = highOrder({ x, y -> x + y }, 10, 20) // 람다식을 인자로 넘기기
}

fun highOrder(sum: (Int, Int) -> Int, a: Int, b: Int): Int {
    return sum(a, b) 
}
```

### 람다식과 고차 함수 호출
함수의 내용을 할당하거나 인자 혹은 반환값을 자유롭게 넘기려면 호출 방법을 이해해야 합니다.  
기본형 변수로 할당된 값은 스택에 있고 다른 함수에 인자로 전달하는 경우에는 해당 값이 복사되어 전달됩니다.  
참조형 변수로 할당된 객체는 참조 주소가 스택에 있고 객체는 힙에 있습니다.  
참조형 객체는 함수에 전달할 때는 참조된 주소가 복사되어 전달됩니다.  

<br>

코틀린에서 값에 의한 호출은 함수가 또 다른 함수의 인자로 전달될 경우 람다식 함수는 값으로 처리되어 그 즉시 함수가 수행된 후 값을 전달합니다.  
```kotlin
fun main() {
    val result = callByValue(lambda()) // 람다식이 즉시 실행되어 결과값이 인자로 들어간다.
}

fun callByValue(b: Boolean): Boolean { // true 값이 인자로 들어와서 b에 복사된다.
    println("call by value")
    return b
}

val lambda: () -> Boolean = {
    println("lambda function")
    true // 람다식은 마지막 표현식 문장의 결과가 반환
}
```
<Br>

람다식을 호출하는 것이 아니라 람다식의 이름을 인자로 전달하면 실행흐름이 약간 다릅니다.
```kotlin
fun main() {
    val result = callByName(lambda) // 람다식이 실행되지 않고 람다식 자체가 넘어간다.
}

fun callByName(b: () -> Boolean): Boolean {
    println("call by value")
    return b() // 람다식이 실제 실행되는 시점으로 true를 반환
}

val lambda: () -> Boolean = {
    println("lambda function")
    true // 람다식은 마지막 표현식 문장의 결과가 반환
}
```
<br>

람다식이 아닌 일반 함수를 또 다른 함수의 인자에서 호출하는 고차 함수를 봅시다.  
```kotlin
fun sum (x: Int, y: Int) = x + y // 일반 함수

fun funParam(a:Int, b: Int, c: (Int, Int) -> Int): Int { 
    return c(a, b)
}

funParam(3,2,sum) // 오류 -> sum이 람다식이 아닌 일반 함수이기 때문
funParam(3,2::sum)
```
sum 함수는 람다식이 아니므로 이름으로 호출할 수 없습니다.  
하지만 sum과 funParam에서 c의 인자 수와 자료형의 개수가 동일한데 이때는 2개의 콜론::을 통해 사용할 수 있습니다.
즉, 일반 함수의 경우 :: 2개의 콜론을 붙여 마치 람다식처럼 사용할 수 있습니다.
```kotlin
fun sum (x: Int, y: Int) = x + y // 일반 함수

val likeLambda = ::sum // 일반 함수를 람다식처럼 변수에 대입
likeLambda(2,2) // 사용
funParam(3,2::sum) // 인자로 함수를 받는 어떤 함수에 인자로 주기
```
<br>

```kotlin
fun main() {
    hello(::text) // 함수 참조 기호 
    hello({a,b -> text(a,b)}) // 람다식 표현, 위와 결과 동일
    hello{a,b -> text(a,b)} // 소괄호 생략, 위와 결과 동일
}

fun text(a: String, b: String) = "hi $a, $b"
fun hello(body: (String, String) -> String){
    println(body)
}
```
main에서 3번째 호출을 보면 소괄호가 생략되어 있습니다.  
코틀린에서는 함수의 매개변수 중 가장 마지막에 있는 매개변수가 함수인 경우 호출 시 앞선 인자들은 소괄호에 넣고 마지막 함수 인자는 소괄호 바깥에서 중괄호에 작성하는 것을 허용합니다.  
허용할 뿐만 아니라 권장하기 때문에 여러 매개 변수 중 함수가 있다면 함수를 가장 마지막 매개변수로 주고 호출 시에는 인자를 따로 빼서 중괄호에 넣어주도록 합시다.  

### 람다식의 매개변수
람다식의 매개변수가 없는 경우는 소괄호, 화살표를 생략할 수 있습니다.
```kotlin
fun main() {
    noParam {"hello world"} // 소괄호, 화살표 생략
}

fun noParam(out: () -> String) = println(out())
```
<br>

람다식의 매개변수가 1개인 경우에는 변수와 화살표를 생략하고 it를 사용할 수 있습니다.
```kotlin
fun main(){

    oneParam {a -> "hello world! $a"} 
    oneParam {"hello world! $it"} // 생략하고 it로 사용
}

fun oneParam(out: (String) -> String){
    println(out("oneParam")) // hello world! oneParam
}
```
<br>

람다식의 매개변수가 2개 이상인 경우에는 it을 사용할 수 없이 소괄호만 생략 가능합니다.  
만약 매개변수를 사용하고 싶지 않다면 _를 사용합니다.
```kotlin
fun main() {
    moreParam {a,b -> "hello world $a $b} 
    moreParam {_,b -> "hello world $b} // 실제 바디부에서 a를 사용하지 않는다면 
}

fun moreParam(out: (String, String) -> String) {
    println(out("oneParam", "twoParam"))
}
```
<br>

일반 매개변수와 람다식 매개변수를 같이 사용하기
```kotlin
fun main() {
    withArgs("arg1","arg2") {a,b -> "hello world! $a $b"}
}

fun withArgs(a: String, b: String, out: (String, String) -> String){
    println(out(a,b))
}
```
<br>

일반 함수에 람다식 매개변수를 2개 이상 사용할 경우에는 소괄호를 생략할 수 없습니다.
```kotlin
fun main() {
    twoLambda({a,b -> "first $a, $b}, {"second $it"})
    twoLambda({a,b -> "first $a, $b}) {"second $it"} // 마지막 함수는 소괄호 바깥에 작성해도 된다.
}

fun twoLambda(first: {String, String} -> String, second: (String) -> String){
    ...
}
```
<br>

## 고차 함수와 람다식 사례

### 동기화 처리
자바에서는 동기화를 위해 Lock과 ReentrantLock을 제공합니다.  
이를 사용해 코틀린에서 동기화 처리를 해봅시다.
```kotlin
import java.util.concurrent.locks.ReentrantLock

var sharable = 1 // 보호가 필요한 공유 자원

fun main() {
    val reLock = ReentrantLock()
    lock(reLock,::criticalFunc)     
}

fun criticalFunc(){
    sharable+=1 // 공유 자원에 접근
}

fun <T> lock(reLock: ReentrantLock, body: () -> T): T {
    reLock.lock() // 락

    // 락을 걸고 보호하는 코드를 try 블록(임계영역)에 둔다.
    try {
        return body()
    } finally { // 잠금 해제
        reLock.unlock() // 락 해제
    }
}
```
ReentrantLock 클래스의 lock 메서드에 의해 구간을 잠게 되고 shrable 변수가 다른 루틴의 방해 없이 안전하게 처리됩니다.  

<br>

## 코틀린의 다양한 함수

### 익명 함수
익명 함수는 일반 함수이지만 이름이 없는 것입니다.  
물론 람다식 함수도 이름 없이 구성할 수 있지만 이것은 일반 함수의 이름을 생략하고 사용하는 것을 말합니다.  
```kotlin
fun(x: Int, y: Int): Int = x + y // 함수의 이름이 없다.
val add = fun(x,y) = x+y  // 익명함수를 통한 add 선언
```
위 코드는 람다식 표현법과 매우 유사하며 람다식으로 표현하면 다음과 같습니다.
```kotlin
val add = {x: Int, y: Int -> x + y}
```
람다식이 훨씬 간편한데 익명함수를 사용하는 이유는 람다식에서는 return이나 break, continue처럼 제어문을 사용하기 어렵기 때문입니다.  
즉, 함수 본문 조건식에 따라 함수를 중단하고 반환해야하는 경우에는 익명 함수를 사용해야 합니다.  

### 인라인 함수
보통 함수는 호출되었을 때 다른 코드로 분기해야 하기 때문에 내부적으로 기존 내용을 저장했다가 다시 돌아올 때 복구하는 작업에 프로세스와 메모리를 사용해야 하는 비용이 듭니다.   
인라인 함수는 이 함수가 호출되는 곳에 함수 본문의 내용을 모두 복사해 넣어 함수의 분기 없이 처리되기 때문에 코드의 성능을 높일 수 있습니다.  
인라인 함수는 코드가 복사되어 들어가기 때문에 내용은 짧게 작성하는 것이 좋습니다.  
인라인 함수는 람다식 매개변수를 가지고 있는 함수에서 동작합니다.  
```kotlin
fun main() {
    shortFunc(1) { println("$it") } // 실제 디컴파일해보면 함수 바디부가 전부 이 위치에 다 복사되어 있다.
}

inline fun shortFunc(a: Int, out: (Int) -> Unit){
    println("before calling out")
    out(a)
    println("after calling out")
}
```
<br>

#### 인라인 함수 제한하기
인라인 함수의 매개변수로 사용한 람다식의 코드가 너무 길거나 인라인 함수 본문 자체가 너무 길면 컴파일러가 성능 경고를 해주고 인라인 함수의 호출이 너무 많으면 코드 양이 늘어나 오히려 좋지 않은 경우가 있습니다.  
이를 해결하기 위해 일부 람다식은 인라인 하지 않도록 하는 키워드는 noinline입니다.
```kotlin
inline fun sub(out1:() ->Unit, noinline out2:() -> Unit){    
}
```
noinline이 있는 람다식은 인라인으로 처리되지 않고 분기되어 호출됩니다.  
```kotlin
fun main() {
    shortFunc(1) { println("$it") }
}

inline fun shortFunc(a: Int, noinline out: (Int) -> Unit){
    println("before calling out")
    out(a)
    println("after calling out")
}
```
위와 같이 호출하게 되면 shortFunc 함수는 인라인 되지만 out를 호출하는 부분은 인라인 되지 않고 함수를 호출하게 됩니다.  

#### 인라인 함수와 비지역 반환
코틀린에서는 익명 함수를 종료하기 위해서 return 문을 사용할 수 있습니다.  
이때 특정 반환값 없이 return만 사용해야 합니다.  
기본적으로 람다식에는 return문을 허용하지 않지만 인라인 함수에서 사용한 람다식의 경우는 return을 사용할 수 있습니다.  
```kotlin
fun main() {
    shortFunc(1) {
        println("hello $it")
        return
    }
    println("hi") // 호출 안됨
}

inline fun shortFunc(a: Int, out: (Int) -> Unit){
    println("before calling out")
    out(a)
    println("after calling out") // 호출 안됨
}
```
이때 shortFunc과 out 함수는 인라인 되므로 결국 return은 메인 함수에서 return 하는 형식이 되버리고 이를 비지역 반환이라고 합니다.  
만약 shortFunc가 inline이 아니었다면 return문은 람다식 본문에 사용할 수 없으므로 return문을 허용할 수 없다는 오류가 발생합니다.  
또한, out을 직접 호출해 사용하지 않고 다른 함수에 중첩하면 실행 문맥이 달라지므로 이때도 return을 사용할 수 없습니다.  
이때 비지역 반환을 금지하는 방법이 있습니다.  
```kotlin
fun main() {
    shortFunc(1) {
        println("hello $it")
        // return 리턴 사용 불가능
    }
    println("hi")
}

inline fun shortFunc(a: Int, crossinline out: (Int) -> Unit){
    println("before calling out")
    nestedFunc { out(a) }   // 람다식을 다른 곳으로 넘긴다. 
    println("after calling out")
}

fun nestedFunc(body: () -> Unit) {
    body()
}
```
crossinline 키워드는 비지역 반환을 금지해야 하는 람다식에 사용합니다.  
위와 같이 inline 함수에서 람다식을 다른 곳으로 넘기는 경우 해당 람다식에서는 return문을 사용할 수 없습니다.  
이를 컴파일 시점에 잡아주도록 하기 위해서 crossinline 키워드를 사용합니다.  

### 확장 함수
클래스에는 다양한 함수들이 정의되어 있는데 이것을 클래스의 멤버 메서드라고 합니다.  
기존 멤버 메서드는 아니지만 기존 클래스에 내가 원하는 함수를 더 포함시켜 확장하고 싶을 때 코틀린에서는 대상에 함수를 더 추가하는 확장 함수 개념을 제공합니다.  
```kotlin
fun 확장대상.함수이름(매개변수, ...): 반환값 {
    ...
    return 값
}
```
<br>

예를 들면 코틀린의 String 클래스에 내가 만든 함수를 추가하고 싶다면 Any에 확장 함수를 추가하면 됩니다.  
```kotlin
fun main() {
    val source = "hello world"
    val target = "backtony"
    println(source.getLongString(target)) // hello world
}

fun String.getLongString(target: String): String = if (this.length>target.length) this else target
```
만약 확장하려는 대상에 동일한 이름의 메서드가 존재한다면 확장 함수보다 멤버 메서드가 우선으로 호출됩니다.  

### 중위 함수
중위 표현법은 클래스의 멤버를 호출할 때 사용하는 .(점)을 생략하고 함수 이름 뒤에 소괄호를 붙이지 않아 직관적인 이름을 사용할 수 있는 표현법입니다.
+ 조건    
    - 멤버 메서드 또는 확장 함수여야 한다.
    - 하나의 매개변수를 가져야 한다.
    - __infix 키워드__ 를 사용하여 정의한다.

```kotlin
fun main() {
    // 일반 표현법
    //val multi = 3.multiply(10)
    
    // 중위 표현법
    val multi = 3 multiply 10
}

infix fun Int.multiply(x: Int): Int{
    return this* x
}
```

### 꼬리 재귀 함수
재귀함수는 다음과 같은 조건을 만족하도록 설계하지 않을 경우 스택 오버플로가 발생합니다.
+ 무한 호출에 빠지지 않기 위해 탈출 조건을 만든다.
+ 스택 영역을 이용하므로 호출 횟수를 무리하게 많이 지정해 연산하지 않는다.
+ 코드를 복잡하지 않게 한다.

코틀린에서는 꼬리 재귀 함수를 통해 스택 오버플로 현상을 해결할 수 있습니다.  
이것은 스택에 계속 쌓이는 방식이 아닌 꼬리를 무는 형태로 반복합니다.  
이때 코틀린 고유의 tailrec 키워드를 사용합니다.  
<br>

아래 코드는 기본적인 팩토리얼 함수로 스택 오버플로 발생 가능성이 있습니다.
```kotlin
fun main() {
    val number = 4
    val result: Long

    result = factorial(number)
    println(result)
}

fun factorial(number: Int): Long {
    return if (number == 1) number.toLong() else number * factorial(number - 1)
}
```
<br>

일반적인 재귀에서는 재귀 함수가 먼저 호출되고 계산되지만 꼬리 재귀에서는 계산을 먼저하고 재귀 함수가 호출됩니다.  
즉, 함수가 계산을 먼저 할 수 있도록 수정해야 합니다.  
조금 더 쉽게 말하자면, 꼬리 재귀 함수에는 어떤 연산도 붙으면 안되고 꼬리 재귀 함수 자체만 사용되어야 합니다.
```kotlin
fun main() {
    val number = 4
    val result: Long

    result = factorial(number)
    println(result)
}

tailrec fun factorial(number: Int, run: Int = 1): Long {
    return if (number == 1) run.toLong() else factorial(number - 1, number * run)
}
```
꼬리 재귀를 사용하면 팩토리얼 값을 그때그때 계산하므로 스택 메모리를 낭비하지 않아도 됩니다.  
<Br>

피보나치 수열 입니다.
```kotlin
import java.math.BigInteger

fun main() {
    val number = 4
    val result: BigInteger

    result = fibo(100, BigInteger("0"), BigInteger("1"))
    println(result)
}

tailrec fun fibo(n: Int, a: BigInteger, b:BigInteger): BigInteger {
    return if (n==0) a else fibo(n-1,b,a+b)
}
```

<br>

## 함수와 변수의 범위
```kotlin
fun main() { // 최상위 함수
    fun secondFunc(a: Int) {...} // 지역함수

    userFunc(4) // 사용자 함수 사용 - 선언부 위치에 상관없이 사용 가능
    secondFunc(2) // 지역 함수 사용 - 선언부가 먼저 나와야만 사용 가능!!
}

fun userFunc(counts: Int) { // 사용자 정의 최상위 함수
}
```
코틀린에서는 파일을 만들고 main 함수나 사용자 함수를 만들 수 있습니다.  
이것을 최상위 함수라고 하고 함수 안에 또 다른 함수가 선언되어 있는 경우 이를 지역 함수라고 합니다.  
최상위 함수는 어느 곳에서나 사용할 수 있고 지역함수는 자신을 감싼 함수 안에서만 사용이 가능합니다.  
지역함수의 사용은 선언 순서에 영향을 받기 때문에 사용하기 전에 먼저 선언을 하고 그 아래쪽에서 사용해야 합니다.  

