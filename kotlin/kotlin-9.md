# Kotlin - 표준 함수와 파일 입출력


## 코틀린 표준 함수

### 클로저
클로저란 람다식으로 표현된 내부 함수에서 외부 범위에 선언된 변수에 접근할 수 있는 개념을 말합니다.  
이때 람다식 안에 있는 외부 변수는 값을 유지하기 위해 람다식이 포획(Capture)한 변수라고 합니다.  
기본적으로 함수 안에 정의된 변수는 지역 변수로 스택에 저장되어 있다가 함수가 끝나면 값이 사라집니다.  
하지만 클로저 개념에서는 포획한 변수는 참조가 유지되어 함수가 종료되어도 사라지지 않고 함수의 변수에 접근하거나 수정할 수 있게 해줍니다.  
클로저의 조건은 다음과 같습니다.
+ final 변수를 포획한 경우 변수 값을 람다식과 함께 저장한다.
+ final이 아닌 변수를 포획한 경우 변수를 특정 래퍼로 감싸서 나중에 변경하거나 읽을 수 있게 한다. 이때 래퍼에 대한 참조를 람다식과 함께 저장한다.

```kotlin
fun main() {
    val calc = Calc()
    var result = 0
    calc.addNum(2, 3) { x, y -> result = x + y } // 외부 변수 사용하는 클로저
    println(result) // 값을 유지하며 5를 출력
}

class Calc {
    fun addNum(a: Int, b: Int, add: (Int, Int) -> Unit) {
        add(a, b)
    }
}
```
addNum 함수가 호출되면서 result는 자신의 유효 범위를 벗어나 삭제되어야 하지만 클로저 개념에 의해 독립된 복사본을 가집니다.  
람다식에서 반환값은 Unit으로 반환값이 없지만 result = x+y를 통해 result에 값을 저장함으로써 포획된 변수 result에 값을 저장할 수 있습니다.  

### 코틀린 표준 라이브러리



#### apply
__수신객체 내부 프로퍼티를 변경한 다음 수신객체 자체를 반환하기 위해 사용하는 함수입니다.__  
apply 함수는 호출하는 객체 T를 이어지는 block으로 전달하고 객체 자체인 this를 반환합니다.  
apply 함수는 특정 객체를 생성하면서 함께 호출해야 하는 초기화 코드가 있는 경우 사용합니다.  
```kotlin
public inline fun <T> T.apply(block: T.() -> Unit): T { block(); return this}

// 예시
var returnObj = person.apply {
    this.name = "backtony" // this 생략 가능
    this.skills = "kotlin" // this 생략 가능
}
println(person) // Person(name=backtony, skills=kotlin)
println(returnObj) // Person(name=backtony, skills=kotlin)
```

#### also
also는 apply와 마찬가지로 수신객체 자신을 반환합니다.  
apply가 프로퍼티를 세팅 후 객체 자체를 반환하는데만 사용된다면, __also는 프로퍼티 세팅뿐만 아니라 객체에 대한 추가적인 작업(로깅, 유효성 검사 등)을 한 후 객체를 반환할 때 사용됩니다.__  
also 함수는 함수를 호출하는 객체 T를 이어지는 block에 전달하고 객체 T 자체를 반환합니다.
```kotlin
public inline fun<T> T.also(block: (T) -> Unit): T {block(this); return this}
```
아래서 나오는 let 함수는 마지막으로 수행된 코드 블록을 반환하는 반면에 also 함수는 블록 안의 코드 수행 결과와 상관없이 T인 객체 this를 반환합니다.  
```kotlin
var m = 1
m = m.also {it+3}
println(m) // 1
```

<Br>

also와 apply는 똑같아 보이지만 also 함수에서는 it을 사용해 멤버에 접근합니다.
```kotlin
person.also {it.skills = "java"} // it으로 받고 생략 불가능
person.apply {skills = "java"} // this 생략 가능
```


#### run
__apply와 똑같이 동작하지만 수신 객체를 return하지 않고, run 블록의 마지막 라인을 return합니다.__  
수신객체에 대해 특정한 동작을 수행한 후 결과값을 리턴 받아야 할 경우 사용합니다.  
run 함수는 인자가 없는 익명 함수처럼 동작하는 형태와 객체에서 호출하는 형태 2가지로 사용됩니다.  
객체 없이 run 함수를 사용하면 인자 없는 익명 함수처럼 사용할 수 있습니다.
```kotlin
public inline fun <R> run(block: () -> R): R = return block()
public inline fun <T,R> T.run(block: T.()): R = return block()

// 예시
val a = 10
var skills = "kotlin"

skills = run {
    val level = "kot level " + a
    level // 마지막 표현식이 반환
}

println(skills) // kot level 10

val returnObj = person.run {
    this.name = "backtony"
    this.skills = "kotlin"
    "success" // success를 반환
}
```

#### with
수신 객체에 대한 작업 후 마지막 라인을 리턴합니다.  
__run과 완전히 똑같이 동작하고 다른 점은 run은 확장함수로 사용되지만 with은 수신 객체를 파라미터로 받아 사용합니다.__    
with 함수는 인자로 받는 객체를 이어지는 block의 receiver로 전달하며 결과값을 반환합니다.  
run 함수와 거의 동일한데 run 함수의 경우 receiver가 없지만 with 함수에서는 receiver로 전달할 객체를 처리하므로 객체의 위치가 달라집니다.  
```kotlin
public inline fun <T,R> with(receiver: T, block: T.() -> R): R = receiver.block()

// 예시
val user = User("backtony","kot")
val result = with(user) {
    skills = "java"
    name = "tony"
}

println(use) // User(name=tony,skills=java)
println(result) // kotlin.Unit
```
name 아래 표현식을 주면 (가령 "Success" 같은) result에는 Success가 들어갑니다.

#### use
__use 함수를 사용하면 객체를 사용한 후 close 함수를 자동적으로 호출해 닫아 줄 수 있습니다.__  
내부 구현을 보면 예외 오류 발생 여부와 상관없이 항상 close 호출을 보장합니다.
```kotlin
public inline fun <T: Closeable?, R> T.use(block: (T) -> R): R

// 예시
printWriter(FileOutputStream("경로")).use {
    it.println("hello")
}
```
T의 제한된 자료형을 보면 Closeable?로 block은 닫힐 수 있는 객체를 지정해야 합니다.  

#### let
수신객체를 이용해 작업 한 후 마지막 줄을 리턴할 때 사용합니다.  
run이나 with과는 달리 접근할 때 it을 사용해야한다는 점만 다르고 나머지는 똑같습니다.  
하지만 실제 사용에서는 차이가 조금 있습니다.  
let은 다음과 같은 경우에 사용합니다.
+ null check 후 코드를 실행해야 하는 경우
+ nullbale한 수신 객체를 다른 타입의 변수로 변환해야하는 경우

__요약하면 nullbale한 값을 처리해야할 때는 let을 사용해야 합니다.__  
let을 이용해 null check를 하려면 ?와 함께 사용해야 합니다.  
?.let을 사용하면 let 블록은 수신객체가 null이 아닐 때만 수행됩니다.  
따라서 let block에서 it 타입은 nullable하지 않은 타입이 됩니다.  
```kotlin
public inline fun <T,R> T.let(block: (T) -> R): R { ... return block(this) }
```
let 함수는 제네릭의 확장 함수 형태로 어디에서든 적용할 수 있습니다.  
매개변수로는 람다식 형태인 block이 있고 T를 매개변수로 받아 R을 반환합니다.  
본문의 this는 T를 가리킵니다.  
이 함수를 호출한 객체를 인자로 받으므로 이를 사용하여 다른 메서드를 실행하거나 연산을 수행하는 경우 사용할 수 있습니다.  
```kotlin
var obj: String?
obj?.let { // obj가 null이 아닐경우 람다식을 수행
    ...
}

var score: Int?
score = null
score?.let { println("not null") } // null이 아닐 경우에만 람다식이 수행
var str = score.let { it.toString() } // score을 문자열로 변환해서 str에 대입, null이라면 null이 할당된다.
```
쉽게 생각하면 블록에 있는 내용 다 처리하고 마지막 코드를 반환합니다.  



## 파일 입출력
### 표준 입출력의 기본 개념
print와 println은 자바의 System.out.println()를 호출합니다.  
표준 입력의 가장 기본적인 API로 readLine() 함수가 있습니다.
```kotlin
val input = readLine()!!
println("you entered : ${input})
```
입력이 실패할 경우 null 가능성이 생기기 때문에 !! 혹은 ?를 사용해서 NPE발생 여부를 처리합니다.  
입력받은 값들은 기본적으로 String이기 때문에 readLine()!!.toInt() 형태로 호출할 수 있습니다.  
<br>

자바에서는 입출력을 위한 기본 패키지 java.io와 대폭 확장된 java.nio 패키지가 있습니다.  
코틀린에서는 자바 라이브러리를 그대로 사용할 수 있으니 알아둬야 합니다.  
둘의 차이에는 버퍼 사용에 있습니다.

구분|java.io|java.nio
---|---|---
입출력|스트림 방식|채널 방식
버퍼 방식|넌버퍼|버퍼
비동기 지원|지원 안함(블로킹)|지원함(넌블로킹)

입축력 구분으로는 발생한 데이터를 물 흐르듯 바로 전송시키는 스트림 방식과 여러 개의 수로를 사용해 병목 현상을 줄이는 채널 방식이 있습니다.  
버퍼는 송/수신 사이에 임시적으로 사용하는 공간이 있는지에 따라 결정됩니다.  
공간이 있는 버퍼 방식은 좀 더 유연한 처리가 가능합니다.  
비동기 지원 여부로 구분하면 java.io는 블로킹 방식으로 단순하게 구성이 가능하고 java.nio는 넌블로킹을 지원해 입출력 동작의 멈춤 없이 또 다른 작업을 할 수 있는 비동기 처리를 지원합니다.  


#### 스트림과 채널
스트림은 데이터가 흘러가는 방향성에 따라 입력 스트림(inputStream)과 출력 스트림(outputStream)으로 구분됩니다.  
데이터를 읽고 저장하는 양방향성을 가지는 작업을 할 때, 예를 들어 FileInputStream과 FileOutputStream으로 두 작업을 별도로 지정해야 합니다.  
채널 방식은 양방향으로 입력과 출력이 모두 가능하기 때문에 입출력을 별도로 지정하지 않아도 됩니다.  
예를 들어 파일을 처리하기 위해 FileChannel을 생성하면 입력과 출력을 동시에 사용할 수 있게 되는 것입니다.  

#### 버퍼와 넌버퍼 방식
스트림 방식에서는 1바이트를 쓰면 입력 스트림이 1바이트를 읽습니다.  
버퍼를 사용해 다수의 데이터를 읽는 것보다 상당히 느리게 동작합니다.  
그래서 io 방식에서는 버퍼와 병합해 사용하는 BufferInputStream과 BufferOutputStream을 제공해 사용하기도 합니다.  
nio 패키지에서는 기본적으로 버퍼를 사용하는 입출력을 하기 때문에 데이터를 일일이 읽는 것보다 더 나은 성능을 보여줍니다.  

#### 블로킹과 넌블로킹
프로그램에서 쓰려고하는데 공간이 없으면 공간이 비워질 때까지 기다리게 됩니다. 읽는 것도 마찬가지 입니다.  
따라서 공간이 비워지거나 채워지기 전까지는 읽고 쓸수가 없기 때문에 호출한 코드에서 계속 멈춰있는 것을 블로킹이라고 합니다.  
하지만 메인 코드의 흐름을 방해하지 않도록 입출력 작업 시 스레드나 비동기 루틴에 맡겨 별개의 흐름으로 작업하게 되는 것을 넌블로킹이라고 합니다.  
따라서 쓰거나 읽지 못해도 스스로 빠져나와 다른 작업을 진행할 수 있습니다. 다만 코드가 복잡해집니다.  

### 파일 쓰기
Files 클래스는 java.nio.file에 속해 있으며 파일을 조작하기 위한 static 메서드로 구성되어 있습니다.
```kotlin
fun main() {
    val path = "경로"
    val text = "hello world"
    
    try {
        Files.write(Paths.get(path), text.toByteArray(),StandardOpenOption.CREATE)
    } catch (e:IOException){
        
    }
}
```
Files 클래스의 write를 사용해 경로에 지정된 파일을 생성하고 내용을 씁니다.  
+ StandardOpenOption : 파일 생성 옵션
    - READ : 파일을 읽기용으로 연다.
    - WRITE : 파일을 쓰기용으로 연다.
    - APPEND : 파일이 존재하면 마지막에 추가한다.
    - CREATE : 파일이 없으면 새 파일을 생성한다.

#### PrintWriter
PrintWriter는 기본적인 printWriter() 외에도 print, println, printf, writer 처럼 파일에 출력을 하는 메서드를 제공하고 있어 기존에 콘솔에 출력하듯이 바이트 단위로 파일에 쓸 수 있습니다.  
```kotlin
fun main() {
    val outString: String = "안녕하세요"
    val path = "경로"
    
    val file = File(path)
    val printWriter = PrintWriter(file)
    
    printWriter.println(outString) // 파일에 출력    
    printWriter.close()
    
    // 축약하기 -> use로 자동 close
    File(path).printWriter().use { it.println(outString) }
}
```

#### BufferedWriter
BufferWriter는 버퍼를 사용해 데이터를 메모리 영역에 두었다가 파일에 쓰는 좀 더 효율적인 파일 쓰기를 지원합니다.  
```kotlin
File(path).bufferedWriter().use { it.write(outString)}
```

#### writeText
writeText는 코틀린에서 확장해 감싼 메서드입니다.  
감싼 메서드란 기존의 존재하는 메서드를 또 다른 메서드로 감싼 후 기능을 더 추가해 편리하게 사용할 수 있게 한 메서드 입니다.  
```kotlin
val file = File(path)
file.writeText(outString)
file.appendText("hello")
```
writeText를 사용하고 appendText에서 문자열을 파일에 추가할 수 있습니다.  
writeText를 타고 들어가보면 안쪽에는 결국 FileOutputStream을 사용하고 있으며 표준 함수 user를 이용해 write가 사용되고 있는 것을 알 수 있습니다.  
그러므로 use에 의해 close가 호출되어 따로 close를 하지 않아도 됩니다.  
한편 null인 내용을 파일에 쓰는 경우 printWriter는 null을 파일에 쓸 수 있지만 bufferedWrtier는 NPE가 발생할 수 있습니다.  
writerText를 사용하는 경우에는 자료형 불일치가 발생할 수 있으니 주의하도록 합니다.  

#### FileWriter
```kotlin
fun main() {
    val path = "경로"
    val outputString = "hello"
    val writer = FileWriter(path,true) // 경로, append 여부
    try {
        writer.write(outputString)
    } catch (e: Exception){
        
    } finally {
        writer.close()
    }

    // 출약
    FileWriter(path,true).use { it.write(outString)}
}
```


### 파일 읽기

#### FileReader
```kotlin
fun main() {
    val path = "경로"
    val outputString = "hello"
    val writer = FileWriter(path, true) // 경로, append 여부
    try {
        val read = FileReader(path)
        println(read.readText())
    } catch (e: Exception) {

    }
}
```
FileReader로 부터 선언된 read의 readText 멤버 메서드를 통해 읽어옵니다.  
readText는 내부적으로 StringWriter를 호출해 텍스트를 메모리로 가져온 후 그 내용을 반환합니다.  

#### 자바 파일 읽기 코틀린으로 변경하기
```java
bufferedReader reader = null;
try {
    reader = new BufferedReader(
        new InputStreamReader(getAssets().open("datafile.json"),"UTF-8"));
    
    String mLine;
    while ((mLine = reader.readLine()) != null){
        // 읽은 줄 처리
    }    
} catch (IOException e){
    // 예외 처리 로그
} finally {
    if (reader != null) {
        try {
            reader.close();
        } catch(IOException e){
            // 예외 처리 로그
        }
    }
}
```
자바에서 파일 입력을 위해 보통 많이 볼 수 있는 코드입니다.  
핵심은 BufferedReader의 인자로 전달된 InputStreamReader를 통해 datafile.json 파일을 UTF-8 인코딩 방식으로 열고 readLine을 통해 읽어들입니다.  
모든 것이 정상적으로 끝나면 finally 블록에서 close를 호출해 안전하게 파일을 닫습니다.  
<br>

```kotlin
fun main() {
    val path = "경로"
    val file = File(path)
    val inputStream: InputStream = file.inputStream()
    val text = inputStream.bufferedReader().use { it.readText() }
    println(text)
}
```
코틀린으로 바꾸면 간단하게 처리가 가능합니다.  
자바의 InputStream 클래스에는 bufferedReader()라는 멤버 메서드가 없지만 코틀린 라이브러리에서 확장 함수 기법으로 InputStream에 추가되었습니다.  
만약 줄단위로 처리하고 싶다면 use 대신 userLines 를 사용하면 됩니다.  
```kotlin
// 컬렉션을 사용하고 BufferedReader 생략해서 파일 직접 사용하는 방식
val lineList = mutableListOf<String>()
File(path).useLines {lines -> lines.forEach { lineList.add(it)}}
lineList.forEach{ println("> " + it)}
```

#### copyTo
copyTo는 파일에 대한 복사 작업을 처리합니다.  
```kotlin
public fun File.copyTo(target: File, overwrite: Boolean = false, bufferSize: Int = DEFAULT_BUFFER_SIZE)
```
목적지인 target에 파일을 버퍼 크기만큼 한 번에 복사합니다.  
이때 기존에 파일이 존재하면 덮어쓸지 결정하기 위해 overwrite 매개변수를 통해 결정합니다.  
bufferSize 매개변수는 버퍼 크기를 결정합니다.  
이 둘은 기본값이 있어 생략해도 됩니다.


