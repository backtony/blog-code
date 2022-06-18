# Kotlin - 프로퍼티와 초기화


## 프로퍼티의 접근
자바의 경우 보통 Getter와 Setter를 통해 프로퍼티를 컨트롤하게 됩니다.  
하지만 이 경우 프로퍼티가 많아지면 이에 대한 코드도 많아지게 되고 결국 lombok을 사용해 의존하게 됩니다.  
코틀린에서는 각 프로퍼티에 게터와 세터가 자동으로 만들어지는 기능을 제공합니다.  

### 코틀린 게터와 세터
자바에서 필드라고 부르는 것과 달리 클래스 변수의 선언 부분을 코틀린에서는 __프로퍼티__ 라고 부릅니다.  
이유는 게터와 세터가 전부 자동으로 만들어지기 때문입니다.  
```kotlin
class User(val Id:Int, var name: String, var age:Int)

fun main() {
    val user = User(1,"backtony",27)

    val name = user.name // 게터에 의한 값 획득
    user.age = 28 // 세터에 의한 값 지정
}
```
객체를 생성하고 점(.)표기법으로 프로퍼티에 접근합니다.  
user.name은 프로퍼티에 직접 접근하는 것처럼 보이나 코틀린 내부적으로 접근 메서드가 내장되어 있습니다.  
실제로는 getName()과 같은 코틀린 내부 게터 메서드를 통해 접근하는 것입니다.  
값을 변경하는 것도 내장된 setAge 같은 세터 메서드를 사용하는 것입니다.  
__val로 선언한 것은 불변이기 때문에 읽기만 가능합니다.__  

<br>

### 게터와 세터 직접 지정하기
```kotlin
var 프로퍼티명[: 자료형] [= 프로퍼티 초기화]
    [get() {게터 본문}]
    [set(value) {세터 본문}]
val 프로퍼티명[: 자료형] [= 프로퍼티 초기화]
    [get() {게터 본문}]
```
__var의 경우 게터와 세터 둘다 만들 수 있지만 val의 경우는 게터만 가능합니다.__  
```kotlin
class User(_id:Int, _name: String, _age:Int){
    val id: Int = _id
        get() = field
    
    var name: String = _name
        get() = field
        set(value) {
            field = value
        }
}
```
+ value : 세터의 매개변수로 외부로부터 값을 가져옵니다.
+ field : 프로퍼티를 참조하는 변수

value는 세터의 매개변수를 가리키며 외부로 값을 전달받습니다.  
user.name = "backtony"라고 한다면 "backtony"가 value에 해당합니다.  
field는 프로퍼티를 참조하는 변수로 보조 필드라고 합니다.  
보조 필드를 사용하는 이유는 게터와 세터 안에서 get() = name과 같이 사용하면 프로퍼티의 get이 다시 호출되는 무한 재귀 호출에 빠지기 때문에 field라는 보조 필드를 따로 두어 field를 사용합니다.  

### 게터, 세터 커스텀하기
단순히 값을 반환하거나 변경하는 목적이라면 게터와 세터를 명시하지 않고 그냥 바로 사용하면 됩니다.  
하지만 다른 작업이 필요하면 게터와 세터를 만들어주고 그 안에 로직을 작성할 수 있습니다. 
```kotlin
class User(_id:Int, _name: String, _age:Int){
    val id: Int = _id
        get() = field

    var name: String = _name
        get() = field
        private set(value) { // 가시성 지시자로 외부에서 호출하지 못하도록 제한
            println("the name was changed") // 세터 중간에 로직 삽입
            field = value
        }
}
```
<br>

프로퍼티는 기본적으로 오버라이딩할 수 없는 final 형태로 선언됩니다.  
만일 오버라이딩이 가능하게 하려면 open 키워드로 열어줘야 합니다.  
```kotlin
open class User(){

    open val name: String = "backtony"
        get() {            
            return field
        }

}

class Student() : User() {
    override val name: String = "tony"
        get(){
            return field // field를 리턴하지 않으면 backing field가 없다고 에러가 납니다.
        }
}
```

<br>

## 지연 초기화와 위임
__프로퍼티를 선언하면 기본적으로 모두 초기화해야 합니다.__  
하지만 객체의 정보가 나중에 나타나는 경우 객체 생성과 동시에 초기화하기 힘든 경우가 있는데 이때 지연 초기화를 사용합니다.  
보통 클래스에서 기본적으로 선언하는 프로퍼티 자료형들은 null을 가질 수 없기 때문에 생성자에서 초기화하거나 매개변수로부터 값을 초기화해야 하는 것이 규칙입니다.  
따라서 __초기화를 미루려면 lateinit와 lazy 키워드를 사용__ 해야 합니다.  

### lateinit 지연 초기화
__lateinit 키워드를 사용하면 프로퍼티에 값이 바로 할당되지 않아도 컴파일러에서 허용하게 됩니다.__  
컴파일러에게 나중에 값을 할당한다고 알려주는 것입니다.  
단, __실행할 때까지 값이 비어 있는 상태면 오류를 유발할 수 있으니 주의__ 해야 합니다.  
+ lateinit 제한
    - __var로 선언된 프로퍼티만 사용 가능__
    - __프로퍼티에 대한 게터와 세터를 사용할 수 없다.__

```kotlin
class Person {
    lateinit var name: String

    fun test() {
        if (!::name.isInitialized) { // 프로퍼티 초기화 여부 판단
            println("not initialized")
        } else {
            println("initialized")
        }
    }
}

fun main() {
    val person = Person()
    person.test()
    person.name = "backtony" // 프로퍼티 초기화(지연 초기화)
    person.test()
}
```
isInitailized는 프로퍼티가 초기화되었는지 검사하는 코틀린 표준 함수 API이고 프로퍼티 참조를 위해서 콜론 2개를 사용했습니다.  
만약 초기화하지 않은 상태에서 값을 사용하면 예외가 발생하므로 값의 할당을 잊으면 안됩니다.  

### 객체 지연 초기화
생성자를 통해 객체를 생성할 때도 lateinit을 사용해 필요한 시점에 객체를 지연 초기화할 수 있습니다.  
```kotlin
data class Person(var name:String, var age:Int)

lateinit var person: Person // 객체 생성 지연 초기화 -> lateinit이 없다면 컴파일 에러 발생

fun main() {
    person = Person("backtony",27) // 생성자 호출 시점에 초기화
}
```
person 객체는 lateinit으로 지연 초기화가 되어 초기값이 없어도 컴파일 에러가 발생하지 않습니다.  

### Lazy를 이용한 지연 초기화
lateinit을 사용할 경우 val을 불가능했고 var만 가능했습니다.  
하지만 var을 사용할 경우 언제든지 값이 변경될 가능성이 있습니다.  
따라서 __읽기 전용인 val에 지연 초기화를 하기 위해서는 lazy를 사용합니다.__  
+ 호출 시점에 by lazy 정의에 의해 블록 부분의 초기화를 진행합니다.
+ __불변의 변수 선언인 val에서만 사용 가능합니다.(읽기 전용)__
+ val이므로 값을 다시 변경할 수 없습니다.

```kotlin
class LazyTest {
    init {
        println("init block") // 2
    }

    val subject by lazy { // 6
        println("lazy subject")
        "kotlin" // lazy에 의한 반환값
    }

    fun flow() {
        println("not initialized") // 4
        println("sub one : $subject") // 5 최초 접근 시점에 lazy 블록 먼저 호출 후 현재 println 출력
        println("sub two : $subject") // 7
    }
}

fun main() {
    val test = LazyTest() // 1
    test.flow() // 3
}
```
lazy 블록은 해당 프로퍼티에 접근하는 로직이 있을 때서야 subject 프로퍼티를 초기화 시켜 줍니다.  

### 객체 지연 초기화
```kotlin
class Person(val name: String, val age: Int)

fun main() {

    val person: Person by lazy { // lazy를 통한 객체 지연 초기화
        Person("backtony", 27) // lazy 객체로 변환
    }

    val personDelegate = lazy { Person("tony", 30) } // 위임 변수를 사용한 초기화

    println("${person.name}") // 이 시점에 초기화
    println("${personDelegate.value.name}") // 이 시점에 초기화 

}
```
by lazy는 객체의 위임을 나타내는 반면에 lazy는 변수에 위임된 Lazy 객체 자체를 나타내므로 value를 한 단계 거쳐 객체의 멤버에 접근해야 합니다.  
<Br>

lazy는 3가지 모드를 지원합니다.
+ SYNCHRONIZED : lock을 사용해 단일 스레드만 사용하는 것을 보장해줍니다.(기본값)
+ PUBLICATION : 여러 군데서 호출될 수 있으나 처음 초기화된 후 반환값을 사용합니다.
+ NONE : lock을 사용하지 않기 때문에 빠르지만 다중 스레드가 접근할 수 있어 값의 일관성을 보장할 수 없습니다.  

```kotlin
by lazy(mode = LazyThreadSafetyMode.모드이름) {...}
```
위와 같은 형태로 사용합니다.  
항상 단일 스레드에서 사용하는 것이 보장되면 NONE을 통해서 성능을 향상시킬 수 있지만 따로 동기화 기법을 사용하지 않는다면 권장하지 않습니다.  

### By 위임
__by를 사용하면 하나의 클래스가 다른 클래스에 위임하도록 선언하여 위임된 클래스가 가지는 멤버를 참조 없이 호출할 수 있게 됩니다.__  
프로퍼티 위임이란 프로퍼티의 게터, 세터를 특정 객체에게 위임하고 그 객체가 값을 읽거나 쓸 수 있도록 허용해줍니다.  
프로퍼티 위임을 하려면 위임을 받을 객체에 by 키워드를 사용하면 됩니다.  
```kotlin
<val | var | class> 프로퍼티 혹은 클래스이름: 자료형 by 위임자
```

#### 클래스 위임
```kotlin
interface Animal {
    fun eat() {}
}

class Cat : Animal {

}

val cat = Cat()

class Robot : Animal by cat
```
Animal 인터페이스를 구현하고 있는 Cat 클래스가 있다면 Animal에서 정의하고 있는 Cat의 모든 멤버를 Robot 클래스로 위임할 수 있습니다.  
즉, Robot은 Cat이 가지는 모든 Animal의 메서드를 갖게 되는데 이것을 클래스 위임이라고 합니다.  
실제로는 Cat은 Animal자료형의 private 멤버로 Robot 클래스 안에 저장되어 Cat에 구현된 모든 Animal 메서드는 정적 메서드로 생성됩니다.  
따라서 Robot 클래스를 사용할 때 Animal을 참조하지 않고 바로 eat()을 호출할 수 있습니다.  

<br>

위임을 왜 사용할까요?  
기본적으로 코틀린이 가지고 있는 표준 라이브러리는 open으로 정의되지 않은 클래스를 사용하고 있습니다.  
즉, final 형태의 클래스이므로 상속이나 직접 클래스의 확장이 어렵습니다.  
따라서 표준 라이브러리의 무분별한 상속에 따른 복잡한 문제를 방지할 수 있습니다.  
__필요한 경우에만 위임을 통해 상속과 비슷하게 해당 클래스의 모든 기능을 사용하면서 동시에 기능을 추가 확장 구현할 수 있게 만들기 위해서 위임을 사용합니다.__  
```kotlin
interface Car {
    fun go(): String
}

class VanImpl(val power: String) : Car {
    override fun go(): String = "van power $power"
}

class SportImpl(val power: String) : Car {
    override fun go(): String = " sport power $power"
}

// Car 인터페이스를 구현하는 CarModel
// go를 override 해야 하지만 인자로 받은 Car 객체에게 위임
// 해당 객체에서 구현하고 있는 go 함수를 사용하게 된다.
class CarModel(val model: String, impl: Car) : Car by impl {
    fun carInfo() {
        println("$model ${go()}") // 참조 없이 바로 접근 가능
    }
}

fun main() {
    val porche = CarModel("porche", SportImpl("300"))
    val van = CarModel("van", VanImpl("100"))

    porche.carInfo()
    van.carInfo()
}

// 출력
porche  sport power 300
van van power 100
```

### 프로퍼티 위임과 by lazy
앞서 살펴본 프로퍼티 lazy도 by lazy{...} 형식이었기에 by가 사용되어 위임된 프로퍼티가 사용되었다는 것을 알 수 있습니다.  
lazy는 사실 람다식으로 사용된 프로퍼티는 람다식에 전달되어(위임되어) 사용됩니다.  
lazy 동작과정은 다음과 같습니다.
1. lazy 람다식은 람다식을 전달받아 저장한 Lazy\<T> 인스턴스를 반환합니다.
2. 최초 프로퍼티의 게터 실행은 lazy에 넘겨진 람다식을 실행하고 결과를 기록합니다.
3. 이후 프로퍼티의 게터 실행은 이미 초기화되어 기록된 값을 반환합니다.

__by lazy에 의한 지연 초기화는 스레드에 좀 더 안정적으로 프로퍼티를 사용할 수 있습니다.__  
예를 들어 프로그램 시작 시 큰 객체가 있다면 초기화할 때 모든 내용을 시작 시간에 할당해야 하므로 느려질 수 밖에 없습니다.  
이것을 필요에 따라 해당 객체를 접근하는 시점에 초기화하면 시작할 때마다 프로퍼티를 생성하느라 소비되는 시간을 줄일 수 있습니다.  

### observable 함수와 vetoable 함수의 위임
observable과 vetoable 함수는 코틀린 표준 위임 구현중 하나 입니다.  
이 둘을 사용하려면 코틀린 패키지의 Delegates를 임포트해야 합니다.  
프로퍼티를 위임하는 object인 Delegates로부터 사용할 수 있는 위임자인 observable 함수는 프로퍼티를 감시하고 있다가 특정 코드의 로직에서 변경이 일어날 때 호출되어 처리됩니다.  
__특정 변경 이벤트에 따라 호출되므로 콜백__ 이라고도 합니다.  
vetoable 함수는 observable 함수와 비슷하지만 반환값에 따라 프로퍼티 변경을 허용하거나 취소할 수 있다는 점이 다릅니다.  
이 두 위임을 생성하기 위해서는 매개변수에 기본값을 지정해야 합니다.  

#### observable 함수 사용 방법
observable 함수는 프로퍼티를 감시하고 있다가 변경이 일어날 때 호출되어 처리됩니다.  
```kotlin
import kotlin.properties.Delegates

class User {
    var name: String by Delegates.observable("NoName") { // 프로퍼티 위임
        prop, old, new -> // 람다식 매개변수로 프로퍼티, 기존 값, 새로운 값 지정
        println("$prop = $old -> $name") // 이 부분은 이벤트가 발생할 때만 실행
    }
}

fun main() {
    val user = User()
    user.name = "backtony" // name 프로퍼티에 변경 발생 -> observable 호출
    user.name = "tony" // name 프로퍼티에 변경 발생 -> observable 호출
}

// 출력 결과
var User.name: kotlin.String = NoName -> backtony
var User.name: kotlin.String = backtony -> tony
```

#### vetoable 함수 사용 방법
```kotlin
import kotlin.properties.Delegates

fun main() {
    var max: Int by Delegates.vetoable(0){ // 초기값 0
        prop, old, new ->
        new > old  // 할당 조건식으로 조건에 맞지 않는다면 거부권 행사
    }

    println(max) // 0
    max = 10
    println(max) // 10

    max = 5 // 조건에 맞지 않으므로 5를 할당하는 것을 무시
    println(max) // 10
}
```
vetoable은 할당 조건식을 통해 조건에 맞지 않으면 실행을 무시합니다.  

<Br>

vetoable은 컬렉션과 같이 큰 데이터를 다룰 때 유용합니다.
```kotlin
import kotlin.properties.Delegates

var data: List<Any> by Delegates.vetoable(listOf()) {
    prop, old, new ->
    notifyDataSetChange() // 어떤 함수 호출
    old != new // 리스트 값에 변화가 있다면 교체
}
```
old와 new 값이 다르다면 true를 반환하여 기존 값이 새 값으로 교체됩니다.  
반면에 false라면 기존 값이 교체되지 않기 때문에 불필요한 실행 비용을 낮출 수 있습니다.  


<br>

## 정적 변수와 컴패니언 객체
정적 변수나 메서드를 사용하면 프로그램 실행 시 메모리를 고정적으로 가지게 되어 따로 인스턴스화 할 필요 없이 사용할 수 있습니다.  
독립적으로 값을 가지고 있기 때문에 어떤 객체라도 동일한 참조값을 가지고 있어 해당 클래스의 상태에 관계 없이 접근할 수 있습니다.  
따라서 모든 객체에 의해 공유되는 효과를 가집니다.  

### 컴패니언 객체 사용하기
코틀린에서는 정적 변수를 사용할 때 static 키워드가 없는 대신 컴패니언 객체를 제공합니다.  
```kotlin
class Person {
    var id: Int = 0
    var name: String = "backtony"
    companion object {
        var language: String = "Korean"
        fun work() {
            println("working...")
        }
    }
}

fun main() {
    println(Person.language) // Korean
    Person.language = "English" // 정적 변수에 접근
    Person.work() // 정적 메서드 접근
}
```
컴패니언 객체는 실제 객체의 싱글톤으로 정의됩니다.  
<br>

자바에서 컴패니언 객체를 가진 코틀린의 클래스에 접근하도록 코드를 작성하려면 @JvmStatic 애노테이션을 사용해야 합니다.
```kotlin
class KCustomer {
    companion object {
        const val LEVEL = "INTERMEDIATE"
        @JvmStatic fun login() = println("login...")
    }
}
```
const는 컴파일 시간 상수입니다.  
컴파일 시간 상수란 val과 다르게 컴파일 시간에 이미 값이 할당되는 것으로 자바에서 접근하기 위해서 필요합니다.  
val은 실행 시간에 할당합니다.  
const는 Int형, Double 형과 같이 기본형으로 사용할 자료형과 String형에만 적용할 수 있습니다.  
@JvmStatic 애노테이션은 자바 소스에서 코드를 해석할 때 Companion을 생략할 수 있게 해줍니다.  
위와 같이 코딩해놓으면 자바에서는 그냥 별 다른 무리 없이 자바에서 사용하던대로 사용하면 됩니다.  
만약 애노테이션을 사용하지 않는다면 KCustomer.Companion.login() 이렇게 접근해야 합니다.  
<Br>

만일 프로퍼티를 자바에서 사용하고자 할 경우에는 @JvmField 애노테이션을 사용할 수 있습니다.
```kotlin
class KCustomer {
    companion object {
        const val LEVEL = "INTERMEDIATE"
        @JvmField val Job = KJob()
        @JvmStatic fun login() = println("login...")
    }
}

class KJob{
    var title: String = "programmer"
}
```
@JvmField 애노테이션으로 Job을 정의했기 때문에 자바에서는 KCustomer.Job.getTitle과 같은 방법으로 접근할 수 있습니다.  

### Object와 싱글톤
변경된 클래스를 만들어야 한다고 한다면 기본적으로 상위 클래스에서 하위 클래스를 새로 선언해 변경된 내용을 기술하면 됩니다.  
하지만 새로 하위 클래스를 선언하지 않고 조금 변경한 객체를 생성해서 사용하고 싶을 수 있습니다.  
__자바에서는 익명 내부 클래스를 사용해 새로운 클래스 선언을 피할 수 있는데 코틀린에서는 object 표현식이나 object 선언으로 처리합니다.__  

#### object 선언
```kotlin
object OCustomer {
    var name = "backtony"
    fun greeting() = println("hello world")

    init {
        println("init")
    }
}

fun main() {
    OCustomer.greeting()
    println(OCustomer.name)
}
```
object로 선언된 클래스의 경우 __싱글톤__ 으로 관리되고 멤버 프로퍼티와 메서드를 접근하는데 있어서 __객체 생성이 필요 없습니다.__  
object 선언 방식을 사용하면 접근 시점에 객체가 생성됩니다.  
따라서 생성자 호출을 하지 않으므로 생성자를 사용할 수 없지만 init은 사용할 수 있습니다.  
object 선언에도 클래스나 인터페이스를 상속할 수 있습니다.  
만일 자바에서 object 선언으로 생성된 인스턴스에 접근하려면 OCustomer.INSTANCE.getName() 과 같이 INSTANCE를 사용해야 합니다.

#### Object 표현식
__object 표현식은 object 선언과 달리 이름이 없으며 싱글톤이 아닙니다.__  
따라서 object 표현식이 사용될 때마다 새로운 인스턴스가 생성됩니다.  
결과적으로 이름이 없는 익명 내부 클래스로 불리는 형태를 object 표현식으로 만들 수 있습니다.  
object 표현식을 이용해 하위 클래스를 만들지 않고 클래스의 특정 메서드를 오버라이딩 해봅시다.  
```kotlin
open class Superman(){
    fun work() = println("work")
    fun talk() = println("talk")
    open fun fly() = println("fly")
}

fun main() {
    val pretendedMan = object: Superman(){ // object 표현식으로 fly 재정의 -> 익명 객체
        override fun fly() = println("i am not a superman")
    }
}
```
여기서 pretendedMan에 대입해준 익명 객체는 Superman 클래스를 상속해 fly 메서드를 오버라이딩 하고 있습니다.  
결국 하위 클래스를 만들지 않고도 fly 메서드를 오버라이딩 한 것입니다.  




