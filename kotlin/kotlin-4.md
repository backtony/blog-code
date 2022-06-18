# Kotlin - 클래스와 객체


## 클래스와 객체의 정의
```kotlin
class Bird { // 클래스 정의
    
    // 프로퍼티(속성)
    var name: String = "myBird"
    var wing: Int = 2
    
    // 메서드(함수)
    fun fly() = println("fly wing: $wing")
}

fun main() {
    var coco = Bird() // 클래스의 생성자를 통한 객체 생성
    coco.wing = 3 // 프로퍼티 값 할당
}
```
자바와 그다지 다를게 없습니다.  
다른게 있다면 생성자가 조금 다른데 코틀린에서는 New 없이 클래스 이름으로 바로 생성합니다.  


<br>

## 생성자
```kotlin
class 클래스명 constructor(필요한 매개변수) { // 주 생성자 

    ...
    constructor(필요한 매개 변수) { // 부 생성자의 위치

    }
}
```
코틀린의 생성자에는 주 생성자와 부 생성자로 나뉩니다.  
부 생성자는 필요에 따라 여러 개를 정의할 수 있습니다.  

### 부 생성자
```kotlin
class Bird { // 클래스 정의

    // 프로퍼티(속성)
    var name: String
    var wing: Int
    var beak: String
    var color: String

    // 부 생성자 1
    constructor(name: String, wing: Int, beak: String, color: String) {
        this.name = name
        this.wing = wing
        this.beak = beak
        this.color = color
    }
    
    // 부 생성자 2
    constructor(name: String, wing: Int) {
        this.name = name
        this.wing = wing
        this.beak = "beak"
        this.color = "blue"
    }
}

fun main() {
    var coco = Bird("myBird", 2, "short", "blue") // 부 생성자 1 호출
    var coco = Bird("myBird", 2) // 부 생성자 2 호출
}
```
부생성자는 자바의 생성자와 비슷합니다.  


### 주 생성자
주 생성자는 클래스 이름과 함께 생성자 정의를 이용할 수 있는 기법입니다.  
주 생성자는 클래스 이름과 블록 시작 부분 사이에 선언합니다.
```kotlin
class Bird(var name: String, var wing: Int, var beak: String, var color: String) {
}

fun main() {
    var coco = Bird("myBird", 2, "short", "blue")
}
```
__주 생성자의 매개변수에 var, val을 사용해서 매개변수를 선언하면 해당 프로퍼티가 본문에 자동으로 선언됩니다.__  
따라서 전에 부 생성자가 있을 때처럼 본문에 프로퍼티를 명시할 필요가 없습니다.  

### 초기화 블록을 가진 주 생성자
객체를 생성할 때 변수 초기화 이외의 코드를 수행하도록 하려면 초기화 블록을 이용합니다.  
```kotlin
class Bird(var name: String, var wing: Int, var beak: String, var color: String) {
    init {
        println(" 초기화 시작 ")
        println("my name is $name and beak is $beak")
        println(" 초기화 종료 ")
    }
}

fun main() {
    var coco = Bird("myBird", 2, "short", "blue")
}
```

### 기본값 지정
```kotlin
class Bird(var name: String = "myBird", var wing: Int = 2, var beak: String, var color: String) {
    init {
        println(" 초기화 시작 ")
        println("my name is $name and beak is $beak")
        println(" 초기화 종료 ")
    }
}

fun main() {
    var coco = Bird(beak = "beak",color = "blue")
}
```
주 생성자에 default값을 명시할 수도 있습니다.


<br>

## 상속과 다형성
### 상속
코틀린은 open 키워드 없이 기본으로 클래스를 선언하면 상속할 수 없는 기본 클래스가 됩니다.  
```kotlin
open class 부모클래스이름 { // 묵시적으로 Any로부터 상속됩니다.
    ...
}

class 자식클래스이름 : 부모클래스이름() { // 부모 클래스로부터 상속된 최종 클래스로 파생 불가능
    ...
}
```
코틀린의 모든 클래스는 묵시적으로 Any로부터 상속받습니다.  
즉, 아무런 표기가 없더라도 모든 클래스는 Any를 최상위 클래스로 가집니다.  
클래스 상속은 콜론(:) 기호를 사용합니다.  
```kotlin
open class Bird(var name: String, var wing: Int, var beak: String, var color: String) {    
}

// 주 생성자를 사용하는 상속
class Lark(name: String, wing: Int, beak:String, color:String) : Bird(name, wing, beak, color) {
    fun singSong() = println("hello song")
}

// 부 생성자를 사용하는 상속
class Parrot : Bird {
    val language: String

    constructor(name: String, wing: Int, beak: String, color: String, language: String) : super(
        name,
        wing,
        beak,
        color
    ) {
        this.language = language
    }
}
```

### 다형성

#### 오버로딩
```kotlin
fun add(x: Int, y: Int): Int {
    return x+y
}

fun add(x: Double, y: Double): Double {
    return x+y
}
```
오버로딩은 동일한 클래스 안에서 같은 이름의 메서드가 매개변수만 달리해서 여러 번 정의될 수 있는 개념입니다.  
반환값은 동일하거나 달라질 수 있습니다.

#### 오버라이딩
코틀린에서는 기반 클래스의 내용을 파생 클래스가 오버라이딩하기 위해 기반 클래스에서는 open 키워드, 파생 클래스에서는 override 키워드를 각각 사용합니다.  
코틀린에서는 메서드 뿐만 아니라 프로퍼티도 오버라이딩 할 수 있습니다.  
```kotlin
open class Bird {
    open fun sing(){} // override를 허용하기 위해서는 메서드 앞에 open
}

class Lark() : Bird() {
    override fun sing() {
        // 재정의
    }
}
```
기반 클래스에서 메서드의 오버라이딩을 허용하기 위해서는 open 키워드를 메서드 앞에 붙여줘야 합니다.  
<br>


만약 하위 클래스에서 더이상 재정의되는 것을 막고자 한다면 final 키워드를 사용합니다.  
```kotlin
class Lark() : Bird() {
    // final 키워드로 더이상 하위 클래스에서 재정의되는 것을 막음
    final override fun sing() {        
    }
}
```

<br>

## super와 this의 참조
+ supser.프로퍼티이름 : 상위 클래스의 프로퍼티 참조
+ super.메서드이름() : 상위 클래스의 메서드 참조
+ super() : 상위 클래스의 생성자 참조
+ this.프로퍼티이름 : 현재 클래스의 프로퍼티 참조
+ this.메서드이름() : 현재 클래스의 메서드 참조
+ this() : 현재 클래스의 생성자 참조

### super로 상위 객체 참조하기
```kotlin
open class Bird {
    open fun sing(){} // override를 허용하기 위해서는 메서드 앞에 open
}

class Lark() : Bird() {
    override fun sing() {
        super.sing() // 상위 클래스의 sing 먼저 수행
        // 추가적인 로직
    }
}
```

### this 현재 객체 참조
```kotlin
open class Person {
    constructor(firstName: String) {
        
    }
    
    constructor(firstName: String, age: Int){ // 3
        
    }
}

class Developer: Person {
    constructor(firstName: String): this(firstName,10){ // 1
        
    }
    constructor(firstName: String, age: Int): super(firstName,age){ // 2
        
    }
}

fun main() {
    val sean = Developer("sean")
}
```
main에서 Developer를 생성하면 Developer의 1번 부 생성자가 호출되고 this를 통해서 2번째 부 생성자를 호출합니다.  
2번째 부 생성자는 부모 클래스의 2번째 부 생성자를 호출하게 됩니다.  
상속을 통해서 클래스를 만드는 경우에는 상위 클래스의 생성자가 있다면 반드시 하위 클래스에서 호출해야 합니다.  
따라서 생성자 코드를 실행하기 전에 현재 클래스를 가리키는 this나 상위 클래스를 가리키는 super를 사용해 위임하여 다른 생성자를 처리할 수 있게 됩니다.

### 주 생성자와 부 생성자 함께 사용하기
```kotlin
class Person(firstName: String, out: Unit = println("[primary constructor] Parameter")) {
    val fName = println("[Primary Constructor] Parameter")

    init {
        println("[init] Person init block")
    }

    constructor(firstName: String, age: Int, out: Unit = println("[Secondary Constructor] Parameter")) : this(firstName) {
        println("[Secondary Contstructor] Body: $firstName, $age")
    }
}

fun main() {
    var p1 = Person("kildong",30)
}

// 출력 결과
[Secondary Constructor] Parameter
[primary constructor] Parameter
[Primary Constructor] Parameter
[init] Person init block
[Secondary Contstructor] Body: kildong, 30
```
여기서는 실험을 위해 out이라는 인자에 println 값을 기본값으로 할당해 인자에 접근할 때 출력되도록 했습니다.  
1. main에서 Person을 생성합니다.
2. Person의 부 생성자가 호출되면서 인자에 접근하면서 out이 출력됩니다.
3. this에 의해 Person의 주 생성자가 호출됩니다. 인자에 접근하면서 out이 출력됩니다.
4. fName에 값이 할당됩니다.
5. init이 호출됩니다.
6. 부 생성자의 본문이 호출됩니다.

### 바깥 클래스 호출하기
특정 클래스 안에 선언된 클래스를 이너 클래스라고 합니다.  
이너 클래스에서 바깥 클래스의 상위 클래스를 호출하려면 super 키워드와 함께 @기호 옆에 바깥 클래스 이름을 작성합니다.  
```kotlin
open class Base{
    open val x: Int = 1
    open fun f() = println("base class f()")
}

class Child : Base() {
    override val x: Int = super.x + 1
    override fun f() = println("child class f()")
    
    inner class Inside {
        fun f() = println("inside class f()")
        fun test() {
            f() // 이너 클래스의 f 함수 호출
            Child().f() // 부모 클래스의 f 함수 호출
            super@Child.f() // Child의 상위 클래스 Base의 f 함수 호출
            super@Child.x // Child 상위 클래스 Base의 x에 접근
        }
    }
}

fun main() {
    val c1 = Child()
    c1.Inside().test() // Inside()로 객체 생성하고 test 메서드 호출
}
```

### 인터페이스에서 참조하기
코틀린은 자바처럼 클래스는 1개만 상속 가능하고 인터페이스는 여러 개 상속받을 수 있습니다.  
각 인터페이스의 프로퍼티나 메서드 이름이 중복될 수 있는데 이때는 앵글 브래킷을 사용해 접근하려는 클래스나 인터페이스의 이름을 정해줍니다.  
```kotlin
open class A {
    open fun f() = println("A class f()")
    fun a() = println("A class a()")
}

interface B { // 인터페이스는 기본적으로 open
    fun f() = println("B interface f()")
    fun b() = println("B interface b()")
}

class C : A(), B {
    override fun f() { // f 함수가 중복되므로 어떤 것을 사용할지 몰라서 컴파일에러가 뜨므로 재정의를 해줌
        println("C class f()")
    }

    fun test() {
        f() // 현재 클래스의 f
        b() // 인터페이스의 b
        super<A>.f() // 클래스 A의 f
        super<B>.f() // 클래스 B의 f
    }
}
```



<br>

## 정보 은닉 캡슐화

### 가시성 지시자
각 클래스나 메서드, 프로퍼티의 접근 범위를 가시성이라고 합니다.  
가시성 지시자를 통해 공개할 부분과 숨길 부분을 정해 줄 수 있습니다.  
+ private : 외부에서 접근 불가능
+ public : 어디서든 접근 가능(기본값)
+ protected : 외부에서 접근할 수 없으나 하위 상속 요소에서는 가능
+ internal : 같은 정의의 모듈 내부에서는 접근 가능

코틀린의 internal은 자바와 다르게 새롭게 정의된 이름입니다.  
internal은 프로젝트 단위의 모듈을 가리키기도 합니다.  
기존에 자바에서는 package라는 지시자에 의해 패키지 이름이 같은 경우 접근을 허용했습니다.  
코틀린에서는 패키지에 제한하지 않고 하나의 모듈 단위를 대변하는 internal을 사용합니다.  
<br>

__cf) 코틀린에서 자바의 package 지시자를 사용하지 않는 이유__  
자바에서 package로 지정된 경우 접근 요소가 패키지 내부에 있다면 접근할 수 있습니다.  
하지만 프로젝트 단위 묶음의 .jar 파일이 달라져도 패키지 이름이 동일하다면 다른 .jar에서도 접근할 수 있었기 때문에 보안 문제가 발생할 수 있었습니다.  
코틀린에서는 이것을 막고자 기존 package를 버리고 internal로 프로젝트의 같은 모듈(빌드된 하나의 묶음)이 아니면 외부에서 접근할 수 없도록 했습니다.  
즉, 모듈이 다른 .jar 파일에서는 internal로 선언된 요소에 접근할 수 없다는 뜻입니다.  

<br>

## 클래스와 클래스의 관계

### 연관 관계
연관 관계란 2개의 서로 분리된 클래스가 연결을 가지는 것입니다.  
단방향 혹은 양방향으로 연결될 수 있습니다.  
핵심은 두 요소가 서로 다른 생명주기를 가지고 있다는 점입니다.  

```kotlin
class Patient(val name: String) {
    fun doctorList(d: Doctor) {
        println("$name ${d.name}")
    }
}

class Doctor(val name: String) {
    fun patientList(p: Patient) {
        println("$name $p.name")
    }
}

fun main() {
    val doc1 = Doctor("backtony") // 객체가 따로 생성됨
    val patient2 = Patient("tony") // 객체가 따로 생성됨
    doc1.patientList(patient2)
    patient2.doctorList(doc1)
}
```
Doctor과 Patient 클래스의 객체는 따로 생성되며 서로 독립적인 생명주기를 갖고 있습니다.  
위 코드에서는 두 클래스가 서로의 객체를 참조하고 있으므로 양방향 참조를 가집니다.  
단방향이든 양방향이든 각각의 객체의 생명주기에 영향을 주지 않을 때는 연관 관계라고 합니다.  

### 의존 관계
한 클래스가 다른 클래스에 의존되어 있어 영향을 주는 경우 의존 관계라고 합니다.  
예를 들어 Doctor 클래스를 생성하려고 하는데 먼저 Patient 객체가 필요한 경우 Doctor는 Patient 객체에 의존하는 관계가 됩니다.  
```kotlin
class Patient(val name: String, var id: Int) {
}

class Doctor(val name: String, val p: Patient) {
    var customerId: Int = p.id
}
```
Doctor 클래스는 주 생성자에서 Patient를 매개변수로 받아야 하므로 Patient 객체가 먼저 생성되어 있어야 합니다.  

### 집합 관계
집합 관계는 연관 관계와 거의 동일하지만 특정 객체를 소유한다는 개념이 추가됩니다.  
```kotlin
class Pond(val name: String, val members: MutableList<Duck>){
    
    constructor(name:String): this(name, mutableListOf<Duck>())
    
}

class Duck(val name: String){
    
}

fun main() {
    val pond = Pond("myPond")
    val duck1 = Duck("duck1")
    val duck2 = Duck("duck2")
    
    pond.members.add(duck1)
    pond.members.add(duck2)
}
```

### 구성 관계
구성 관계는 집합 관계와 거의 동일하지만 특정 클래스가 어느 한 클래스의 부분이 되는 것입니다.  
구성품으로 지정된 클래스는 생명주기가 소유자 클래스에 의존되어 있어 소유자 클래스가 제거되면 구성 클래스도 같이 제거됩니다.  
```kotlin
class Car(val name: String, val power: String){
    private var engine = Engine(power) 
}

class Engine(power: String) {

}
```
Engine 클래스는 Car 클래스의 생명주기에 의존적입니다.  
car 객체를 생성함과 동시에 Engine 클래스의 객체도 생성됩니다.  

