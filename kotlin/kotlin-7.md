# Kotlin - 제네릭과 배열


## 제네릭 다루기
제네릭은 클래스 내부에서 사용할 자료형을 나중에 인스턴스를 생성할 때 확정합니다.  
제네릭을 사용하면 객체의 자료형을 컴파일할 때 체크하기 때문에 객체 자료형의 안전성을 높이고 형 변환의 번거로움이 줄어듭니다.  
제네릭에서는 다음과 같은 형식 매개변수를 사용합니다.
+ E : 요소
+ K : 키
+ N : 숫자
+ T : 타입(형식)
+ V : 값
+ S, U, V etc. : 두 번째, 세 번째, 네번 째 형식

### 제네릭 일반적 사용법
#### 제네릭 클래스
```kotlin
class MyClass<T> { // 매개변수 받기

    var myProp: T // 오류 !! 

    fun myMethod(a: T){ // 메서드의 매개변수 자료형에 사용

    }
}
```
__타입 제네릭을 프로퍼티에 사용하는 경우 클래스 내부에서는 사용할 수 없습니다.__  
자료형이 특정되지 못하므로 인스턴스를 생성할 수 없기 때문입니다.  
그 대신 주 생성자나 부 생성자에 타입 매개변수를 지정하면 사용할 수 있습니다.
```kotlin
class MyClass<T>(val myProp: T) {} // 주 생성자 이용

class MyClass<T> {
    val myProp: T

    constructor(myProp: T){ // 부 생성자 이용
        this.myProp = myProp
    }
}
```

#### 자료형 변환
일반적으로 상위 클래스와 하위 클래스의 선언 형태에 따라 클래스의 자료형을 변환할 수 있지만 제네릭의 경우 가변성을 지정하지 않으면 상,하위 클래스가 지정되어도 자료형이 변환되지 않습니다.
```kotlin
open class Parent

class Child: Parent()

class Cup<T>

fun main() {
    val obj1: Parent = Child() // Child 클래스는 Parent의 하위 클래스이므로 가능
    val obj2: Child = Parent() // 오류 자료형 불일치

    val obj3: Cup<Parent> = Cup<Child>() // 오류 자료형 불일치
    val obj4: Cup<Child> = Cup<Parent>() // 오류 자료형 불일치

    val obj5 = Cup<Child>() 
    val obj6 = obj5 // 자료형 일치
}
```

#### 타입 매개변수의 null 제어
```kotlin
class GenericNull<T> {
    fun test(arg1: T, arg2: T){ // 기본적으로 null이 허용되는 타입 매개변수
        println(arg1?.equals(arg2))
    }
}

fun main() {
    val obj = GenericNull<String> () // non-null로 선언되어 널을 허용하지 않음
    val obj2 = GenericNull<Int?>() // null이 가능한 형식으로 선언
    
}
```
기본적으로 타입 매개변수 T는 null을 허용합니다.  
따라서 Int?와 같이 null을 허용하는 타입을 넘길 수 있습니다.  
만약 타입 매개변수에 null을 허용하지 않는 타입만 오도록 하고자 한다면 Any 타입을 사용합니다.
```kotlin
class GenericNull<T: Any> {}
```

#### 제네릭 메서드
타입 매개변수를 받는 메서드를 제네릭 메서드라고 합니다.  
해당 메서드 앞쪽에 \<T> 같이 타입 매개변수를 지정합니다.  
```kotlin
fun <타입 매개변수[,...]> 함수 이름(매개변수: <매개변수 자료형>[, ...]): <반환 자료형>

// 예시
fun <T> test(arg: T): T? {...}
fun <K,V> test(key: K, value: V): Unit {...}
```

#### 제네릭과 람다식
```kotlin
fun <T> add(a: T, b: T): T {
    return a+b // 오류
}
```
타입 매개변수로 선언된 함수의 매개변수를 연산할 경우에는 자료형을 결정할 수 없기 때문에 오류가 납니다.  
하지만 람다식을 매개변수로 받으면 자료형을 결정하지 않아도 실행 시 람다식 본문을 넘겨줄 때 결정되므로 이 문제를 해결할 수 있습니다.  
```kotlin
fun <T> add (a: T, b: T, op: (T, T) -> T): T {
    return op(a,b)
}
```
람다식은 add 함수가 실행될 때 넘겨지는 인자이므로 연산식을 함수 선언부에 직접 구현하지 않고 전달하는 방식을 사용하기 때문에 타입 매개변수의 자료형을 특정하지 않아도 실행이 가능합니다.

### 자료형 제한하기
콜론을 사용해 자료형을 제한합니다.  
```kotlin
class Clac<T: Number> {}
fun <T: Number> add(a: T, b: T, op:(T,T) -> T): T {}
```
<br>

타입 매개변수의 자료형을 제한할 때 하나가 아닌 여러 개의 조건에 맞춰 제한하고자 할 때가 있습니다.  
예를 들어 InterfaceA와 InterfaceB를 모두 구현하는 클래스만 허용하려고 한다면 타입 매개변수의 사용 범위를 지정하는 where 키워드를 사용할 수 있습니다.  
```kotlin
// 클래스
class HandlerA: InterfaceA, InterfaceB
class HandlerB : InterfaceA

class ClassA<T> where T:InterfaceA, T: InterfaceB // T는 반드시 InterfaceA,B를 모두 구현한 것이여야만 한다.

fun main() {
    val obj1 = ClassA<HandlerA>() // 허용
    val obj2 = ClassA<HandlerB>() // 오류 발생
}

// 메서드
fun <T> myMax(a: T, b: T): T where T:Number, T:Comparable<T> {}
```

### 상,하위 형식의 가변성
가변성이란 타입 매개변수가 클래스 계층에 영향을 주는 것을 말합니다.  
예를 들어 타입 A의 값이 필요한 모든 클래스에 타입 B의 값을 넣어도 아무 문제가 없다면 B는 A의 하위 타입입니다.  

#### 클래스와 자료형
일반적으로 사용하는 모든 클래스는 자료형으로 취급할 수 있습니다.  
예를 들어 Int는 클래스이기도 하고 동시에 자료형이기도 합니다.  
하지만 null을 가지는 String? 은 클래스라고 하지 않습니다.  
또한, List는 클래스이지만 List\<String>는 클래스가 아니라 자료형일 뿐입니다.  
<br>

제네릭에서는 클래스 간에 상,하위 개념이 없어 서로 무관합니다.  
따라서 상위와 하위에 따른 형식을 주려면 가변성의 3가지 특징을 이해하고 있어야 합니다.  
+ 공변성 : T1가 T의 하위 자료형이면 C\<T1>는 C\<T>의 하위 자료형이다.
    - class Box\<out T>
+ 반공변성 : T1가 T의 하위 자료형이면 C\<T>는 C\<T1>의 하위 자료형이다.
    - class Box\<in T>
+ 무변성 : C\<T>와 C\<T1>는 아무런 관계가 없다.

#### 무변성
타입 매개변수에 in, out을 명시하지 않으면 무변성으로 제네릭 클래스가 선언됩니다.  
이 경우에는 어떤 상, 하위 관계를 잘 따지더라도 같은 타입이 아니면 오류가 발생합니다.
```kotlin
class Box<T>(val size: Int)

fun main() {
    val anys: Box<Any> = Box<Int>(10) // 오류
}
```

#### 공변성
타입 매개변수의 상하 자료형 관계가 성립하고, 그 관계가 그대로 인스턴스 자료형 관계로 이어지는 경우를 공변성이라고 합니다.  
일반적인 상하 자료형 관계가 제네릭에 그대로 적용된다고 보면 됩니다.  
공변성에는 out 키워드를 사용합니다.  
```kotlin
class Box<out T>(val size: Int)

fun main() {
    val anys: Box<Any> = Box<Int>(10) // 관계 성립으로 생성 가능
}
```

#### 반공변성
일반적인 상하 자료형 관계가 반대로 적용되는 것을 반공변성이라고 합니다.  
in 키워드를 사용합니다. 
```kotlin
class Box<in T>(val size: Int)

fun main() {
    val anys: Box<Any> = Box<Int>(10) // 자료형 불일치
    val anys: Box<Nothing> = Box<Int>(10) // 성립
}
```

#### 공변성에 따른 자료형 제한
```kotlin
open class Animal(val size:Int){
    fun feed()  = println("feeding")
}

class Box<out T: Animal>(val element: T){
    fun getAnimal(): T = element // 가능
//    fun set(new: T){ 불가능
//        
//    }
}
```
out으로 선언된 경우 타입 매개변수는 반환 자료형에서는 사용 가능하나 함수의 매개변수로는 사용할 수 없습니다.  
반대로 in의 경우에는 반환 자료형에서는 사용할 수 없고 함수의 매개변수로는 사용할 수 있습니다.  
쉽게 생각하면 out는 나가는 반환 타입에서 사용 가능하고 in은 들어오는 매개변수 타입에서 사용 합니다.  
자바와 비교해보면 out 은 Type\<? extends T> 이고 in 은 Type\<? super T> 입니다.  
또한, out의 경우 타입 매개변수를 갖는 프로퍼티는 var로 지정될 수 없고 val만 허용합니다.  
만약 var를 사용하려면 매개변수의 가시성 지정자를 private으로 설정해야 합니다.

### 자료형 프로젝션
#### 가변성의 2가지 방법
가변성 지정에는 사용 지점 변성 방식과 선언 지점 변성 방식이 있습니다.  
자바에서는 사용 지점 변성 방식만 지원하여 클래스 내부의 메소드에 제네릭을 적용할 때마다 타입의 변성을 지정합니다.  
자바에서 제네릭 클래스를 보면 각각의 메서드마다 <? extend T> 또는 <? super T>가 붙어있는 것을 볼 수 있습니다.  
코틀린에서는 이렇게 매번 별도로 가변성을 지정하는 것이 번거롭다고 판단하여 선언 지점 변성 방식을 제공합니다.  
선언 지점 변성이란 클래스를 선언하면서 클래스 자체에 가변성을 지정하는 방식으로 클래스에 in/out을 지정할 수 있습니다.  
클래스를 선언하면서 가변성을 지정하면 클래스의 공변성을 전체적으로 지정하는 것이 되기 때문에 클래스를 사용하는 장소에서는 따로 자료형을 지정해 줄 필요가 없어서 편리합니다.  

```kotlin
// 선언 지점 변성 방식
class Box<in T: Animal>(val size: int){
    ...
}
```
위의 클래스 내부에서 사용되는 모든 T는 in T가 적용됩니다.  
즉, Type\<? super T> 가 적용되며, T는 반환타입으로 사용할 수 없고 매개변수의 인자의 타입으로 사용 가능합니다.  
만약 out이라면 <? extend T>가 적용되며, T는 반환타입으로 사용할 수 있고 매개변수의 인자의 타입으로는 사용할 수 없습니다.  

<br>

```kotlin
// 사용 지점 변성 방식
class Box<T>(val item: T)

fun <T> printObj(box: Box<out Animal>){
    box.item // get 사용 가능
    box.item =  // set 사용 불가능
}
```
사용 지점 변성 방식은 사용하는 메서드에서 in, out을 정의합니다.  
out이므로 반환(get)은 사용할 수 있지만 대입(set)은 사용할 수 없습니다.  
in이라면 대입(set)은 사용할 수 있지만 반환(get)은 사용할 수 없습니다.  
<br>

이렇게 사용하고자 하는 요소의 특정 자료형에 in, out을 지정해 제한하는 것을 자료형 프로젝션이라고 합니다.  
사용할 수 있는 위치를 제한하는 이유는 자료형 안전성을 보장하기 위해서 입니다.  
즉, 이 함수에서는 out에 의한 게터만 허용하고 in에 의한 세터는 금지하겠다고 하는 것입니다.  

#### 스타 프로젝션
Box\<Any?> 가 되면 모든 자료형의 요소를 담을 수 있음을 의미하는 반면에 Box\<*>는 어떤 자료형이라도 들어올 수 있으나 구체적으로 자료형이 결정되고 난 후에는 __그 자료형과 하위 자료형의 요소만__ 담을 수 있도록 제한할 수 있습니다.  
```kotlin
class InOutTest<in T, out U>(t: T, u: U) {

     fun fun2(t:T){
         print(t)
     }
}

fun test(v: InOutTest<*,*>) {
    v.fun2(1) // in Nothing 취급으로 Nothing과 그 하위자료형만 허용하므로 오류 발생
}
```
in으로 정의되어 있는 타입 매개변수를 *로 받으면 in Nothing으로 간주됩니다.  
out으로 정의되어 있는 타입 매개변수를 *로 받으면 out Any?로 간주됩니다.  
따라서 *를 사용할 때 그 위치에 따라 메서드 호출이 제한될 수 있습니다.  
<br>

__cf) Nothing 클래스__  
Nothing은 코틀린의 최하위 자료형으로 아무것도 가지고 있지 않은 클래스입니다.  
최상위의 Any와는 정반대입니다. Nothing은 보통 아무것도 존재하지 않는 값을 표현할 때 사용합니다.  
예를 들어 함수의 반환 자료형이 Nothing이면 그 함수는 절대 아무것도 반환하지 않는다는 의미입니다.  
Unit은 자바의 Void와 유사하다고 했지만 Nothing은 Void와 같다고 보면 됩니다.  

#### 자료형 프로젝션 정리

종류|예|가변성|제한
---|---|---|---
out 프로젝션|Box\<out Cat>|공변성|타입 매개변수는 세터를 통해 값을 설정하는 것이 제한됩니다.
in 프로젝션|Box\<in Cat>|반공변성|타입 매개변수는 게터를 통해 값을 읽거나 반환이 제한됩니다.
스타 프로젝션|Box\<*>|모든 인스턴스는 하위 타입이 될 수 있습니다.|in과 out은 사용 방법에 따라 결정됩니다. 

### reified 자료형
```kotlin
fun <T> myGenericFun(c: Class<T>)
```
위는 일반적인 제네릭 함수입니다.  
여기서 T 자료형은 자바처럼 실행 시간에 삭제되기 때문에 T 자체에 그대로 접근할 수 없습니다.  
\<Int>처럼 결정된 제네릭 자료형이 아닌, \<T>처럼 결정되지 않은 제네릭 자료형은 컴파일 시간에는 접근 가능하나 함수 내부에서 사용하려면 위의 코드처럼 작성해야만 실행 시간에 사라지지 않고 접근할 수 있습니다.  
위 코드와 달리 매개변수로 지정하지 않고 타입 매개변수를 직접 접근하고 싶다면 reified로 타입 매개변수 T를 지정하면 실행 시간에 접근할 수 있습니다.  
```kotlin
inline fun <reified T> myGenericFun()
```
하지만 reified 자료형은 __인라인 함수에서만__ 사용할 수 있습니다.  
이 함수가 호출되면 본문 코드 내용은 호출되는 곳 어디든 복사되어 들어가게 되는데 reified T 자료형은 컴파일러가 복사해 넣을 때 실제 자료형을 알 수 있기 때문에 실행 시간에도 사용할 수 있게 됩니다.  

<br>

__cf) Class\<T>__  
자바에서는 .class 형태로 반환 받는 객체를 Class\<T>라고 합니다.  
따라서 이 클래스는 원본 클래스에 대한 많은 메타 데이터를 가지고 있습니다.  
예를 들면 패키지 이름, 메서드, 필드, 인터페이스 등을 말입니다.  
코틀린에서는 Object::class로 표현되고 KClass를 나타냅니다.  

<br>


## 배열 다루기
### 배열 생성하기
코틀린에서 배열은 Array 클래스로 표현되며 요소로 여러 가지 자료형을 혼합해서 구성할 수 있습니다.  
```kotlin
val numbers = arrayOf(1,2,3,4) // 정수형 초기화 배열
val animals = arrayOf("cat","dog") // 문자열 초기화 배열
```
기본적인 배열을 생성하기 위해서는 arrayOf(), Array() 생성자를 사용해 배열을 만듭니다.  
만일 빈 상태의 배열을 지정하는 경우 arrayOfNulls()를 사용할 수 있습니다.  
<br>

다차원 배열은 arrayOf 안에 또다시 배열을 선언하는 방법으로 중첩할 수 있습니다.
```kotlin
val arr2d = arrayOf(arrayOf(1, 2, 3), arrayOf(4, 5, 6))
```
<br>

특정 자료형으로 제한하지 않는다면 배열의 요소로 여러 가지 자료형을 혼합할 수 있습니다.
```kotlin
val mixArr = arrayOf(1,2,3,"backtony")
```
<br>

만일 배열에서 특정 자료형을 제한하려면 arrayOf\<자료형>() 형태나 자료형이름 + ArrayOf() 형태의 조합으로 나타낼 수 있습니다.  
```kotlin
val intOnlyArr = arrayOf<Int>(1,2,3)
val intOnlyArr2 = intArrayOf(1,2,3) // 자료형 이름과 ArrayOf 조합
```
자료형 이름 조합은 charArrayOf(), booleanArrayOf(), longArrayOf() 등이 있습니다.  
이런 자료형은 내부적으로 기본형의 배열을 생성하게 됩니다.  
intArrayOf의 경우 자료형은 내부적으로 int[]로 변환됩니다.  

### 배열 요소 접근하기
```kotlin
public class Array<T> {
    public inline constructor(size: Int, init: (Int) -> T)
    public operator fun get(index: Int): T
    public operator fun set(index: Int, value: T): Unit
    public val size: Int
    public operator fun iterator() : Iterator<T>
}
```
get과 set 메서드는 요소에 접근하기 위한 게터와 세터입니다.  
대괄호를 사용해도 접근할 수 있는데 이것은 연산자 오버로딩으로 정의되어 있기 때문입니다.  
```kotlin
arr.get(index) 
arr[index]

arr.set(index)
arr[index] = value

arr.set(2,7) // 인덱스 2번 요소를 7로 교체
arr[2][1] = 2 // 다차원 배열 요소 교체
```

<Br>

배열의 크기나 합계는 기본으로 제공하는 API를 사용할 수 있습니다.
```kotlin
val arr = intArrayOf(1,2,3)

arr.size // 3
arr.sum() // 6
```
<br>

Arrays에서 멤버인 toString()을 사용하면 배열의 내용을 한꺼번에 출력할 수 있습니다.
```kotlin
fun main() {
    val arr = intArrayOf(1,2,3,4,5)
    println(Arrays.toString(arr)) // [1,2,3,4,5]
    println(arr.contentToString()) // [1,2,3,4,5]

    // 다차원 배열의 경우 deepToString 사용
    val arr2 = arrayOf(intArrayOf(1, 2, 3, 4), intArrayOf(1, 2, 3, 4))
    println(Arrays.deepToString(arr2)) // [[1, 2, 3, 4], [1, 2, 3, 4]]
    println(arr2.contentDeepToString()) // [[1, 2, 3, 4], [1, 2, 3, 4]]
}
```

<br>

표현식을 통해서 배열을 생성할 수 있습니다.
```kotlin
val|var 변수이름 = Array(요소개수,초기값)
```
람다식 초기값은 init: (Int) -> T 로 정의되어있어 다음과 같이 활용할 수 있습니다.
```kotlin
fun main() {
    val arr = Array(5) { i -> i * 2 }
    println(arr.contentToString()) // [0, 2, 4, 6, 8]

    var a = Array(1000) {0} // 0으로 채워진 배열
    println(a.contentToString())
}
```

### 배열 제한하고 처리하기
배열을 일단 정의되면 크기가 고정되기 때문에 다음과 같이 새로 할당하는 방법으로 요소를 추가하거나 잘라낼 수 있습니다.
```kotlin
fun main() {
    val arr1 = intArrayOf(1,2,3,4,5)
    val arr2 = arr1.plus(6) // 6 추가한 새로운 배열 생성
    val arr3 = arr1.sliceArray(0..2) // [1,2,3] 0부터 2인덱스까지 잘라내서 새로운 배열 생성
}
```
<br>

배열에서는 유용한 메서드를 몇 가지 제공합니다.
```kotlin
import java.util.*

fun main() {
    val arr = intArrayOf(1,2,3,4,5)

    println(arr.first()) // 1
    println(arr.last()) // 5
    println(arr.indexOf(1)) // 0 요소 1의 인덱스 출력
    println(arr.average()) // 3.0 평균
    println(arr.count()) // 5 요소 개수
    
    arr.reverse() // 역순으로 바꾸기
    arr.sum() // 요소 합산
    arr.fill(0) // 요소 채우기
    arr.contains(2) // 요소 들어있는지 확인
    // 파이썬 처럼 4 in arr 과 같은 중위 표현번 가능
    
    val arr2 = arr.reversedArray() // 역순 배열 생성
    println(Arrays.toString(arr2)) // [5,4,3,2,1]
    
}
```
<br>

일단 자료형이 지정된 배열은 다른 자료형으로 변환할 수 없으나 Any 자료형으로 만들어진 배열은 기존 자료형을 다른 자료형으로 지정할 수 있습니다.
```kotlin
fun main() {
    val array = Array<Any>(10) { 0 }    
    array[0] = "hello"
    array[1] = 1.1
}
```
처음에는 0으로 채워진 배열 10개가 전부 정수형이었으나 할당한 값에 따라 요소의 자료형이 변환됩니다.  
이렇게 Any를 사용하면 한 번에 기본적인 초기화를 하고 나중에는 원하는 자료형으로 요소를 초기화할 수 있으므로 편리합니다.  
<br>

반복문을 사용한 배열 순환 말고도 forEach()와 forEachIndexed를 사용해 요소를 순환할 수 있습니다.  
```kotlin
import java.util.*

fun main() {
    val array = Array<Any>(10, { 0 })

    array.forEach { element -> println("$element") }

    array.forEachIndexed { idx, element -> println("$idx, $element") }
}
```
forEachIndexed는 인덱스까지 같이 처리할 수 있습니다.  

### 배열 정렬하기
```kotlin
import java.util.*

fun main() {
    val arr = intArrayOf(1, 6, 2, 5, 3, 8, 5, 0, 1)
    
    // 기존 배열 정렬
    arr.sort(1, 4) // 원본 배열에 대한 정렬 startindex, endindex, endIndex는 포함 안됨
    arr.sortDescending()
 
    // 정렬된 새로운 배열 생성
    val sortedArray = arr.sortedArray() // 오름차순 배열 반환
    val sortedArrayDescending = arr.sortedArrayDescending() // 내림차순 배열 반환
 

    // 배열 -> 리스트로 변환하여 정렬
    val sorted = arr.sorted()
    val sortedDescending = arr.sortedDescending()

    // 특정 표현식에 따른 정렬
    val items = arrayOf<String>("Dog", "Cat")
    items.sortBy { item -> item.length }
}
```
<Br>

2개 이상의 변수로 정렬을 해야하는 경우는 sortWith를 사용합니다.
```kotlin
data class Product(val name: String, val price: Double)

fun main() {
    val products = arrayOf(Product("a", 870.00),
        Product("b", 810.00),
        Product("c", 820.00),
        Product("d", 830.00),
        Product("e", 840.00),
        Product("f", 850.00)
    )
    products.sortWith(
        Comparator<Product> { p1, p2 ->
            val compareTo = p1.price.compareTo(p2.price)
            if (compareTo == 0) {
                return@Comparator p1.name.compareTo(p2.name)
            }
            compareTo
        }
    )
}
```
sortWith를 compareBy와 함께 사용하면 더 간단하게 할 수 있습니다.
```kotlin
data class Product(val name: String, val price: Double)

fun main() {
    val products = arrayOf(Product("a", 870.00),
        Product("b", 810.00),
        Product("c", 820.00),
        Product("d", 830.00),
        Product("e", 840.00),
        Product("f", 850.00)
    )
    products.sortWith(
        compareBy({it.name} , {it.price})
    )
}
```

### 배열 필터링하기
```kotlin
fun main() {
    val arr = arrayOf(1, -2, -3, 4, 5)
    val filteredArr = arr.filter { e -> e > 0 } // [1,4,5]

    // 메서드 체이닝
    val fruit = arrayOf("banana", "avocado", "apple", "kiwi")    
    fruit.sortedBy { it }
        .filter { it.startsWith("a") }
        .map { it.toUpperCase() }
        .forEach { println(it) }

    
    // price가 가장 작은 product 가져오기
    products.minBy { it.price }

    // 가장 큰 product 가져오기
    products.maxBy { it.price }
}
```

### 배열 평탄화하기
다차원 배열을 단일 배열로 만들 수 있는 flatten 메서드를 제공합니다.  
```kotlin
fun main() {
    val arr1 = arrayOf(1, 2, 3)
    val arr2 = arrayOf("one", "two", "three")

    val simpleArr = arrayOf(arr1, arr2)
    val flatten = simpleArr.flatten()
    println(flatten) // [1,2,3,one,tow,three]
}
```


<br>


## 문자열 다루기

### 문자열 기본 처리
문자열은 연속된 문자의 배열과 같습니다.  
또한 문자열은 불변값으로 생성되기 때문에 참조되고 있는 메모리가 변경될 수 없습니다.  
새로운 값을 할당하려고 한다면 기존 메모리 이외에 새로운 문자열을 위한 메모리를 만들어 할당해야 합니다.  
또한, 하나의 요소에 새 값을 할당할 수 없습니다.
```kotlin
fun main() {
    val hello: String = "hello world"
    println(hello[0]) // h
    
    hello[0]= 'k' // 하나의 요소에 새 값 할당 불가능 -> 오류
    var s = "abac"
    s = "tex" // 새로운 메모리 공간이 생성

}
```

### 문자열 추출
문자열을 추출할 수 있는 기능을 제공합니다.
```kotlin
val hello: String = "hello world"
val substring = hello.substring(0..2) // hel
```

### 문자열 비교
```kotlin
fun main() {
    val s1 = "hello"
    var s2 = "Hello"

    println(s1.compareTo(s2))
    println(s1.compareTo(s2, true)) // 대소문자 무시

}
```
s1과 s2가 같다면 0, s1이 s2보다 작으면 양수, 그렇지 않으면 음수를 반환합니다.  

### StringBuilder
StringBuilder를 사용하면 문자열이 사용할 공간을 더 크게 잡을 수 있기 때문에 요소를 변경할 때 이 부분이 사용되어 특정 단어를 변경할 수 있게 됩니다.  
단, 기존의 문자열보다는 처리 속도가 조금 느리고, 만일 단어를 변경하지 않고 그대로 사용하면 임시 공간인 메모리를 조금 더 사용하게 되므로 낭비된다는 단점이 있습니다.  
따라서 문자열이 자주 변경되는 경우에 사용하면 좋습니다.  
```kotlin
fun main() {
    val sb = StringBuilder("hello")
    sb.append("world") // 생성된 버퍼를 사용하므로 + 연산자를 이용해 새로운 객체를 만들어 처리하는 것보다 좋다.
    sb.insert(10,"backtony") // 인덱스 10번부터 추가
    sb.delete(5,10) // 5부터 10번 전까지 삭제 
}
```

### 문자열 자르기
```kotlin
fun main() {
    var deli = "welcome to kotlin"
    val split = deli.split(" ")
    println(split) // [welcome, to, kotlin]
}
```

### 문자열을 정수로 변환하기
```kotlin
fun main() {
    try {
        var number: Int = "123".toInt()
    } catch (e: NumberFormatException){
        println(e.printStackTrace())
    }
}
```
만약 숫자가 아닌 경우 예외가 발생하므로 try-catch 블록으로 처리해야 합니다.  
만약 숫자가 아닌 문자가 포함되었을 때 null을 반환 받고자 한다면 toInt 대신 __toIntOrNull__ 을 사용합니다.