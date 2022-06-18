# Kotlin - 프로그램의 흐름 제어


## 조건문
### if 문
```kotlin
if (조건식) {
    ...  
} else if (조건식) {
    ...
} else {
    ...
}
```
다른 언어의 if문과 다를게 없습니다.  
다른점이 있다면 if문을 하나의 표현식으로서 변수에 할당해 같은 기능을 수행하도록 할 수 있습니다.
```kotlin
val max = if(a > b) a else b

// 표현식이 길어지면 중괄호로 감싸야 합니다.
val max = if (a > b) {
    println("a 선택")
    a // 마지막 식인 a가 반환되어 max에 할당
} else {
    println("b 선택")
    b // 마지막 식인 b가 반환되어 max에 할당
}
```
<br>

간단하게 성적 판별 예시를 하나 보겠습니다.  
```kotlin
fun main() {
    val score = readLine()!!.toDouble() // 사용자 입력
    var grade: Char = 'F'

    if (score>= 90)
        grade = 'A'
    else if (score in 80.0..89.9) // 80 <= score <= 89.9
        grade = 'B'
    else
        grade = 'C'

    println(grade)
}
```

### when문
```kotlin
when (인자) {
    인자에 일치하는 값 혹은 표현식 -> 수행할 문장
    인자에 일치하는 범위 -> 수행할 문장
    ...
    else -> 수행할 문장
}
```
when은 자바의 switch문과 유사하지만 더 유연한 문법을 제공합니다.  
when 블록의 안을 보면 화살표 왼쪽에는 조건을 나타내고 오른쪽을 수행할 문장을 사용합니다.  

```kotlin
fun main() {
    var x = 1
    // .. 표현을 in문을 통해 사용 가능
    when(x) {        
        1 -> println("x == 1")
        2 -> println("x == 2")
        3,4 -> println("x is 3 or 4")
        in 5..10 -> println("x in 5..10")
        !in 5..10 -> println("x !in 5..10")
        else -> println("어떠한 범위에도 없다.")
    }
    
    // is도 사용 가능
    var str = "hello"
    when(str) {
        is String -> println("문자열이다.")
        else -> println("문자열이 아니다.")
    }

    // 인자 없이도 조건이나 표현식을 직접 만들어서 사용
    when {
        x ==1 -> println("x == 1")
        x == 2 -> println("x == 2")
        x in 5..10 -> println("x in 5..10")
        x !in 5..10 -> println("x !in 5..10")
        else -> println("어떠한 범위에도 없다.")
    } 
}
```
<br>

when 문의 인자로서 Any를 사용하게 되면 다양한 자료형의 인자를 받을 수 있습니다.
```kotlin
fun main() {
    case("hello")
}

fun case(obj: Any) {
    when(obj){
        1 -> println(" is one")
        "hello" -> println(" is hello")
        !is String -> println(" is not String")
    }
}
```

<br>

## 반복문

### for 문
```kotlin
for (요소 변수 in 컬렉션 또는 범위) { 반복할 본문}
```
코틀린의 for문은 자바보다는 파이썬의 for문과 비슷합니다.  
```kotlin
fun main() {
    for (x in 1..5)
        println(x)

    for (x in 1..5 step 2) // 2칸씩 이동
        println(x)
    
    for (x in 5..1) // 아무것도 출력되지 않음
        println(x)
    
    for (x in 5 downTo 1) // 줄어들기
        println(x)

    for (x in 5 downTo 1 step 2) // 2칸씩 줄어들기
        println(x)    
}
```

### while 문
```kotlin
while (조건식) {
    본문
}
```
```kotlin
do {
    본문
} while (조건식)
```
while문은 자바와 다를게 없기에 넘어가겠습니다.

<br>

## 흐름의 중단과 반환

+ 흐름 제어문
    - return : 함수에서 결과값을 반환하거나 지정된 라벨로 이동합니다.    
    - break : for문이나 while문의 조건식에 상관없이 반복문을 끝낸다.
    - continue : for문이나 while문의 본문을 모두 수행하지 않고 다시 조건식으로 넘어간다.
+ 예외 처리문
    - try catch : try 블록의 본문을 수행하는 도중 예외가 발생하면 catch 블록의 본문을 실행한다.
    - try catch finally : 예외가 발생해도 finally 블록 본문은 실행한다.


### return 문
```kotlin
fun add(a: Int, b: Int): Int {
    return a+b
    println("여기는 실행되지 않습니다.")
}

fun welcome() { // Unit 반환 생략
    println("어서오세요")
}
```
Unit과 return을 생략할 경우 코틀린 컴파일러는 Unit을 반환하는 것으로 가정합니다.  

<br>

__cf) 람다식에서 return, break, continue 사용 가능 여부__  
람다식에서 return은 라벨 표기와 함께 사용해야만 사용할 수 있고 break, continue문은 아직 지원되지 않습니다.  
<br>

#### 람다식에서 return 사용하기
앞선 포스팅에서 보았듯이 람다식에서는 return문을 사용할 수 없고 인라인 된 람다식에서만 특별하게 return문을 사용할 수 있었습니다.  
기본 람다식에서도 return문을 사용할 순 있는데 이때는 라벨과 함께 사용해야 합니다.  
라벨이란 코드에서 특정한 위치를 임시로 표기한 것으로 @기호화 이름을 붙여서 사용합니다.  
인라인으로 선언된 함수에서 람다식을 매개변수로 사용하면 람다식에서 return을 사용할 수 있습니다.  
```kotlin
fun main() {
    retFunc()
}

inline fun inlineLambda(a: Int, b: Int, out: (Int, Int) -> Unit) {
    out(a, b)
}

fun retFunc() {
    println("start of retFunc")
    inlineLambda(13, 3) { a, b ->
        val result = a + b
        if (result > 10) return // inlineLambda가 인라인 함수이므로 리턴문 사용 가능
        println("result : $result")
    }
    println("end of retFunc")
}
```


#### 람다식에서 라벨과 함께 return 사용하기
```kotlin
람다식 함수 이름 라벨 이름@ {
    ...
    return@라벨 이름
}
```
앞선 예제와 같지만 inline을 제거하고 return 문을 라벨과 같이 사용해 보겠습니다.
```kotlin
fun main() {
    retFunc()
}

fun inlineLambda(a: Int, b: Int, out: (Int, Int) -> Unit) { // inline 제거
    out(a, b)
}

fun retFunc() {
    println("start of retFunc")
    inlineLambda(13, 3) lit@{ a, b -> // 람다식을 라벨로 지정
        val result = a + b
        if (result > 10) return@lit // 해당 람다식을 종료
        println("result : $result")
    } // 람다식 리턴시 여기로 이동
    println("end of retFunc")
}
```

#### 암묵적 라벨
람다식 표현식 블록에 직접 라벨을 쓰는 것이 아닌 람다식 명칭을 그대로 라벨처럼 사용할 수 있는데 이것을 암묵적 라벨이라고 합니다.  
```kotlin
fun main() {
    retFunc()
}

fun inlineLambda(a: Int, b: Int, out: (Int, Int) -> Unit) {
    out(a, b)
}

fun retFunc() {
    println("start of retFunc")
    inlineLambda(13, 3) { a, b -> // 
        val result = a + b
        if (result > 10) return@inlineLambda // 함수 이름을 라벨로 사용
        println("result : $result")
    } // 람다식 리턴시 여기로 이동
    println("end of retFunc")
}
```

#### 익명 함수를 사용한 반환
람다식 대신 익명 함수를 사용할 경우에는 라벨 없이 가까운 익명 함수 자체가 반환되므로 return을 바로 사용해도 됩니다.
```kotlin
fun main() {
    retFunc()
}

fun inlineLambda(a: Int, b: Int, out: (Int, Int) -> Unit) {
    out(a, b)
}

fun retFunc() {
    println("start of retFunc")
    inlineLambda(13, 3, fun(a,b) {
        val result = a + b
        if (result > 10) return // 익명 함수 리턴
        println("result : $result")
    })  // 리턴시 여기로 이동
    println("end of retFunc")
}
```

<br>

```kotlin
val getMessage = lambda@ { num: Int ->
    if(num !in 1..100){
        return@lambda "error" // getMessage에 error 대입
    }
    "success" // 기본은 가장 마지막 식이 리턴    
}
```
람다식을 사용하면 return문을 사용할 수 없고 마지막 식이 리턴되기 때문에 사실상 return문이 2개인데 가독성이 떨어집니다.  
따라서 return과 같이 명시적으로 반환해야 할 것이 여러 개라면 람다식보다는 익명 함수를 사용하는 것이 가독성이 좋습니다.
```kotlin
val getMessage = fun(num: Int): String {
    if(num !in 1..100){
        return "error"
    }
    return "success"
}
```

#### 람다식과 익명 함수를 함수에 할당할 때 주의할 점
람다식은 특정 함수에 할당할 때 주의하여 사용해야 합니다.  
```kotlin
fun greet() = {println("Hello")}

greet() // 아무것도 출력되지 않음
greet()() // Hello 찍힘
```
할당 연산자(=) 에 의해 람다식 자체가 greet() 함수에 할당된 것이기 때문에 greet()가 가진 함수를 사용하려면 ()를 한번 더 사용해야 합니다.  
```kotlin
fun greet() = fun() {println("Hello")}
```
함수가 할당됨을 명시적으로 표현하려면 익명 함수를 사용하는 것이 더 가독성이 좋을 수 있습니다.  

### break문과 continue문
break와 continue의 사용법은 java와 다를게 없으므로 라벨과 함께 사용하는 부분만 살펴봅시다.  
```kotlin
fun labelBreak() {
    first@ for (i in 1..5){
        second@ for (j in 1..5){
            if (j == 3) break@first
        }
    } // break시 first라벨에 해당하는 for문 바깥으로 이동한다.
}
```
```kotlin
fun labelContinue() {
    first@ for (i in 1..5){
        second@ for (j in 1..5){
            if (j == 3) continue@first
        }
    } // continue시 first라벨에 해당하는 for문의 continue가 진행된다.
}
```

### 예외 처리
```kotlin
try {
    예외 발생 가능성 문장
    // throw Exception("message")
} catch (e: 예외 처리 클래스){
    예외 처리
} finally {
    반드시 실행되어야 하는 문장
}
```
예외처리 블록은 자바와 다를게 없습니다.  

