# Kotlin - 컬렉션


## 컬렉션의 구조와 기본
### 코틀린 컬렉션
코틀린의 컬렉션은 자바 컬렉션 구조를 확장해 구현했습니다.  
컬렉션의 종류는 List, Set, Map 등이 있으며 자바와는 다르게 __불변형(immutable)__ 과 __가변형(mutable)__ 으로 나뉩니다.  
가변형 컬렉션은 객체에 데이터를 추가하거나 변경할 수 있고 불변형 컬렉션은 데이터를 한 번 할당하면 읽기 전용이 됩니다.  
자바에서는 오로지 __가변형 컬렉션__ 만 취급되므로 자바와 상호작용하는 코드에서는 주의해야 합니다.  

컬렉션|불변형(읽기 전용)|가변형
---|---|---
List|listOf|mutableListOf, arrayListOf
Set|setOf|mutableSetOf, hashSetOf, linkedSetOf, sortedSetOf
Map|mapOf|mutableMapOf, hashMapOf, linkedMapOf, sortedMapOf

쉽게 생각하면 자료형이 앞쪽에 온다면 불변형이라고 보면 됩니다.  
변수를 선언할 때 불변형 val의 사용을 권장하듯이, 컬렉션도 되도록이면 읽기 전용인 불변형으로 선언할 것을 권장합니다.  


### 컬렉션 구조
iterable 인터페이스는 컬렉션이 연속적인 요소를 표현할 수 있도록 합니다.  
iterable를 구현한 Collection과 MutableIterable 인터페이스가 존재합니다.  
그 아래 Collection 인터페이스를 구현한 Set, List가 있습니다.  
MutableIterable 아레에는 MutableIterable과 Collection 인터페이스를 구현한 MutableCollection 인터페이스가 있습니다.  
그 아래 있는 MutableSet은 Set과 MutableCollection 인터페이스를 구현했고, MutableList는 List와 MutableCollection을 구현합니다.  
Map은 별개로 있고 MutableMap은 Map을 구현합니다.  

<br>

__Collection 인터페이스 멤버__  


|멤버|설명|
|---|---|
|size|컬렉션의 크기|
|isEmpty()|컬렉션이 비어 있으면 true|
|contains(element: E)|특정 요소가 있다면 true|
|containsAll(elements: Collection\<E>)|인자로 받아들인 컬렉션이 있다면 true|

<br>

__MutableCollection 인터페이스의 멤버 메서드__  
다른 인터페이스로 MutableIterable과 MutableCollection 인터페이스는 가변형 컬렉션을 지원하기 위해 준비된 인터페이스이며 요소를 추가하거나 제거하는 등의 기능을 수행합니다.  

|멤버 메서드|설명|
|---|---|
|add(element: E)|인자로 전달 받은 요소를 추가하고 true를 반환하며, 이미 오ㅛ소가 있거나 중복이 허용되지 않으면 false|
|remove(element: E)|인자로 전달 받은 요소를 삭제하고 true를 반환하며, 삭제하려는 요소가 없다면 false|
|addAll(elements: Collection\<E>)|컬렉션을 인자로 전달 받아 모든 요소를 추가하고 true를 반환하며, 실패하면 false|
|removeAll(elements: Collection\<E>)|컬렉션을 인자로 전달 받아 모든 요소를 삭제하고 true를 반환하며, 실패하면 false|
|retainAll(elements: Collection\<E>)|인자로 전달 받은 컬렉션의 요소만 보유한다. 성공하면 true, 싪패하면 false|
|clear()|컬렉션의 모든 요소 삭제|


<br>

## List 활용

### 불변형 List 생성하기
listOf() 함수는 불변형 List를 만들 수 있습니다.
```kotlin
public fun <T> listOf(vararg elements: T): List<T>
```
vararg는 가변 인자를 받을 수 있기 때문에 원하는 만큼 요소를 지정할 수 있습니다.  
값을 반환할 때는 List\<T>를 사용합니다.  
타입 매개변수는 \<T>는 원하는 자료형을 지정해 선언할 수 있습니다.  
사용하지 않으면 \<Any> 가 기본값이며 어떤 자료형이든 혼합할 수 있습니다.
```kotlin
var numbers: List<Int> = listOf(1,2,3,4)
```

#### 컬렉션 반복
```kotlin
fun main() {
    val fruits: List<String> = listOf("appble", "banana", "kiwi")

    for (fruit in fruits) {
        println(fruit)
    }

    for (index in fruits.indices) { // 인덱스
        println("${fruits[index]}")
    }
}
```

#### emptyList() 함수
```kotlin
fun main() {
    val emptyList: List<String> = emptyList<String>()    
}
```
빈 리스트는 emptyList로 선언할 수 있습니다.

#### listOfNotNull() 함수
null을 제외한 요소만 반환해 List를 구성할 수 있습니다.  
```kotlin
val listOfNotNull:List<Int> = listOfNotNull(1, 2, null) // null을 제외한 요소만 반환해 리스트로 구성
```

#### 리스트 주요 메서드

멤버 메서드|설명
---|---
get(index: Int)|특정 인덱스를 인자로 받아 해당 요소를 반환
indexOf(element: E)|인자로 받은 요소가 첫 번째로 나타나는 인덱스를 반환하며, 없으면 -1
lastIndexOf(element: E)|인자로 받은 요소가 마지막으로 나타나는 인덱스를 반환하고, 없으면 -1
listIterator()|목록에 있는 iterator를 반환
subList(fromIndex: Int, toIndex: Int)|특정 인덱스의 from과 to 범위에 있는 요소 목록을 반환

### 가변형 List 생성
```kotlin
public fun <T> arrayListOf(vararg elements: T): ArrayList<T>
```
가변형 List를 생성하고 자바의 ArrayList와 같습니다.  
```kotlin
fun main() {
    val arr: ArrayList<String> = arrayListOf<String>("hello", "kotlin")
    arr.add("java")
    arr.remove("java")
}
```
<Br>

mutableListOf를 통해서도 가변형 List를 생성할 수 있습니다.  
```kotlin
val mutableList: MutableList<String> = mutableListOf<String>("hello", "world")
```
<Br>

기존의 불변형 List를 가변형으로 변경하려면 toMutableList()를 사용할 수 있습니다.  
이렇게 하면 기존의 List는 그대로 두고 새로운 공간을 만들어 냅니다.  
```kotlin
val names: List<String> = listOf("one", "two")
val toMutableList = names.toMutableList()
```
<br>

### List와 배열의 차이
List는 Array와 비슷하지만 Array 클래스에 의해 생성되는 배열 객체는 내부 구조상 고정된 크기의 메모리를 가지고 있습니다.  
코틀린의 List\<T>와 MutableList\<T>는 인터페이스로 설계되어 있고 이것을 하위에서 특정한 자료구조로 구현합니다.  
따라서 해당 자료구조에 따라 성능이 달라집니다.  
예를 들면 List\<T> 인터페이스로부터 구현한 LisnkedList\<T>와 ArrayList\<T>는 특정 자료구조를 가지는 클래스이며 성능도 다 다릅니다.  
따라서 다음과 같이 객체를 만들 수 있습니다.
```kotlin
val list1: List<Int> = LinkedList<Int>()
val list2: List<Int> = ArrayList<Int>()
```

## Set과 Map 활용

### Set
Set는 정해진 순서가 없는 요소들의 집합을 나타내는 컬렉션으로 동일한 요소를 중복해서 가질 수 없습니다.

#### Set 생성하기
Set은 SetOf()를 이용해 불변형 Set을 생성하고, mutableSetOf()를 이용해 가변형 Set을 생성핧 수 있습니다.
```kotlin
val set = setOf<Int>(1, 2, 3) // 불변형
val set2 = mutableSetOf<Int>(1, 2, 3) // 가변형
```

#### Set의 여러 가지 자료구조
hashSetOf()는 자바의 HashSet 형태의 해시 Set을 만듭니다.
```kotlin
val set:HashSet<Int> = hashSetOf(1, 2, 3, 4)
```
<Br>

sortedSetOf() 함수는 자바의 TreeSet 컬렉션을 정렬된 상태로 반환합니다.  
```kotlin
val set: TreeSet<Int> = sortedSetOf(1, 2, 3, 4)
```
<Br>

linkedSetOf() 함수는 자바의 LinkedHashSet 자료형을 반환하는 함수입니다.  
```kotlin
val set: LinkedHashSet<Int> = linkedSetOf(1,2,3,4)
```

### Map
Map 컬렉션은 자바의 Map을 사용하고 있습니다.  
키는 중복될 수 없지만 값은 중복을 허용합니다.  

#### Map 생성하기
mapOf()는 불변형 Map을 만들 수 있습니다.
```kotlin
val map: Map<키 자료형, 값 자료형> = mapOf(키 to 값, [,...])

// 예시
val map: Map<Int, String> = mapOf(11 to "java", 12 to "kotlin")
map.get(11)
map[12] // get 과 동일
map.keys // 모든 키
```
<br>

__제공 메서드__  

멤버|설명
---|---
size|Map 컬렉션의 크기를 반환
keys|Set의 모든 키 반환
values|Set의 모든 값 반환
isEmpty()|비어있으면 true, 아니면 false
containsKey(key: K)|키가 있으면 true, 없으면 false
containsValue(value: K)|인자가 있다면 true, 없으면 false
get(key: K)|키에 해당하는 값을 반환하고 없으면 null

<Br>

가변형의 경우 mutableMapOf() 함수로 생성할 수 있습니다.
```kotlin
val map: MutableMap<String, String> = mutableMapOf("backtony" to "kotlin", "gildon" to "java")
```
<Br>

__제공 메서드__  
가변형의 경우 값을 추가, 수정, 삭제할 수 있는 메서드를 더 제공합니다.

멤버|설명
---|---
put(Key: K, value: V)|키와 값의 쌍을 Map에 추가합니다.
remove(Key: K)|키에 해당하는 요소를 Map에서 제거합니다.
putAll(from: Map<\out K, V>)|인자로 주어진 Map 데이터를 갱신하거나 추가합니다.
clear()|모든 요소를 지웁니다.

#### Map 기타 자료구조
자바의 HashMap, SortedMap, LinkedHashMap을 사용할 수 있습니다.
```kotlin
val hashMap: HashMap<Int, String> = hashMapOf(1 to "hello")
val sortedMap: SortedMap<Int, String> = sortedMapOf(1 to "hello")
val linkedMap: LinkedHashMap<Int, String> = linkedMapOf(1 to "hello")
```

<br>

## 컬렉션의 확장 함수

### 컬렉션의 연산
```kotlin
fun main() {
    val list1: List<String> = listOf("one","two")
    val list2: List<Int> = listOf(1,2,3)
    val map = mapOf("hi" to 1, "hello" to 2)

    println(list1+"three") // three가 추가된 리스트
    println(list2+1) // 1 요소가 추가된 리스트
    println(list2 + listOf(1,2,3)) // 두 리스트 병함
    println(list2 - 2) // 요소 제거
    println(list2 - listOf(1,2)) // 일치하는 요소 제거
    println(map + Pair("boe",3)) // 요소 추가된 map
    println(map - "hi") // 일치하는 키값 제거
    println(map + mapOf("apple" to 3)) // map 병합
    println(map - listOf("hi","hello")) // 일치하는 key값 제거
}
```
일반적인 +와 - 연산을 통해 컬렉션 요소를 더하고 뺄 수 있고 컬렉션 자체를 더하거나 뺄 수 있습니다.  

### 요소 처리와 집계
#### 순환과 최대 최소
```kotlin
fun main() {
    val list = listOf(1, 2, 3, 4, 5, 6)
    val listPair = listOf(Pair("A", 300), Pair("B", 200), Pair("C", 100))
    val map = mapOf(11 to "java", 22 to "kotlin", 33 to "C++")

    list.forEach { println(it) } // 순환
    list.forEachIndexed { idx, element -> println("${idx}, ${element}") } // 인덱스 포함 순환

    val originList = list.onEach { println(it) } // 각 요소를 람다식 대로 처리하고 사용한 컬렉션을 반환

    list.count { it % 2 == 0 } // 조건에 맞는 요소 개수 반환

    list.maxOrNull() // 최대값
    list.minOrNull() // 최소값

    map.maxByOrNull { it.key } // 키를 기준 최대값
    map.minByOrNull { it.key } // 키 기준 최소값
}
```

#### 각 요소에 정해진 식 사용하기
```kotlin
fun main() {
    val list = listOf(1, 2, 3, 4, 5, 6)

    list.fold(4) { total, next -> total + next } // 4 + 1 + 2 + ... + 6
    list.foldRight(4) { total, next -> total + next } // 4 + 6 + 5 + ... + 1 역순

    // fold와 동일하지만 초기값 없이 진행
    list.reduce { total, next -> total + next }
    list.reduceRight { total, next -> total + next }
}
```

#### 모든 요소 합산하기
```kotlin
val listPair = listOf(Pair("A", 300), Pair("B", 200), Pair("C", 100))
listPair.sumBy { it.second } // 합산 결과 반환
```

### 요소 검사
```kotlin
fun main() {
    val list = listOf(1, 2, 3, 4, 5, 6)

    list.all { it < 10 } // 모든 요소가 만족하면 true를 반환
    list.any { it < 10 } // 어떤 요소가 하나라도 만족하면 true

    list.contains(1) // 포함하는지
    list.containsAll(listOf(1, 2)) // 모두 포함하는지

    list.none() // 요소가 없으면 true
    list.none { it > 6 } // 요소가 6보다 큰게 없다면 true

    list.isEmpty() // 비었는지
    list.isNotEmpty() // 안 비었는지
}
```

### 요소 필터와 추출
```kotlin
fun main() {
    val list = listOf(1, 2, 3, 4, 5, 6)
    val listPair = listOf(Pair("A", 300), Pair("B", 200), Pair("C", 100))
    val map = mapOf(11 to "java", 22 to "kotlin", 33 to "C++")
    val mixList = listOf("hello", 1)

    list.filter { it % 2 == 0 } // 짝수만 골라내기
    list.filterNot { it % 2 == 0 } // 짝수가 아닌 것 골라내기
    list.filterNotNull() // 널 제외하기

    // 인덱스와 함께 추출
    list.filterIndexed { idx, value -> idx != 1 && value % 2 == 0 }

    // 추출후 가변형 컬렉션으로 반환
    list.filterIndexedTo(mutableListOf()) { idx, value -> idx != 1 && value % 2 == 0 }

    map.filterKeys { it != 11 } // 키 11 제외
    map.filterValues { it == "java" } // 값 java 제외

    mixList.filterIsInstance<String>() // String인 것만 골라내기

    list.slice(1..3) // 잘라내기

    list.take(2) // 앞 두 요소 반환
    list.takeLast(2) // 마지막 두 요소 반환
    list.takeWhile { it < 3 } // 조건식에 따른 반환

    list.drop(3) // 앞의 요소 3개 제외하고 반환
    list.dropWhile { it < 3 } // 3 미만을 제외하고 반환
    list.dropLastWhile { it > 3 } // 3 초과를 제외하고 반환
    
    list.component1() // 첫 번째 요소 반환
    
    list.distinct() // 중복 요소가 있을 경우 1개로 취급해서 다시 컬렉션 List로 반환
    list.intersect(listOf(1,2,3)) // 교집합 요소만 반환
}
```

### 요소 매핑
매핑에 사용하는 .map()은 주어진 컬렉션의 요소를 일괄적으로 map의 인자로 넣어준 식을 적용해 새로운 컬렉션을 만듭니다.
```kotlin
list.map { it * 2 }
list.mapIndexed { index, it -> index * it }
list.mapNotNull { it?.times(2) } // null을 제외하고 식을 적용해 새로운 컬렉션 반환

// flatmap은 각 요소에 식을 적용한 후 이것을 다시 하나로 합쳐 새로운 컬렉션을 반환
list.flatMap { listOf(it, 'A') } // [1,A,2,A,3,A...]
listOf("a,b,c", "12").flatMap { it.toList() } // [a,b,c,1,2]

// 주어진 식에 따라 요소를 그룹화해서 Map으로 반환
list.groupBy { if (it % 2 == 0) "even" else "odd" } // {odd=[1,3,5], even=[2,4,6]}
```

### 요소 처리와 검색
```kotlin
fun main() {
    val list = listOf(1, 2, 3, 4, 5, 6)
    val listPair = listOf(Pair("A", 300), Pair("B", 200), Pair("C", 100))

    list.elementAt(1) // 해당 인덱스으 요소 반환
    list.elementAtOrElse(10, { 2 * it }) // 인덱스를 벗어나는 경우 식의 결롸를 반환
    list.elementAtOrNull(10) // 인덱스를 넘어가는 경우 null 반환

    listPair.first { it.second == 200 } // first 식에 일치하는 첫 요소를 반환
    listPair.last { it.second == 200 } // last 식에 일치하는 마지막 요소 반환
    listPair.firstOrNull { it.first == "A" } // 식에 일치하는게 없다면 null 반환
    listPair.lastOrNull { it.first == "E" } // 식에 일치하는게 없다면 경우 null 반환

    list.indexOf(4) // 요소와 일치하는 첫 인덱스 반환
    list.indexOfFirst { it % 2 == 0 } // 식과 일치하는 요소의 첫 인덱스 반환, 없으면 -1
    list.lastIndexOf(5) // 요소와 일치하는 가장 마지막 인덱스 반환
    list.indexOfLast { it % 2 == 0 } // 식과 일치하는 마지막 요소의 인덱스 반환, 없으면 -1

    listPair.single { it.second == 100 } // 일치하는 요소 하나 반환, 요소가 하나 이상인 경우 예외
    listPair.singleOrNull { it.second == 200 } // 일치하는 요소가 없거나 하나 이상이면 null 반환

    list.binarySearch(3) // 이진 탐색후 인덱스 반환

    list.findLast { it > 3 } // 첫 번째 검색된 요소 반환, 없으면 null

}
```

### 컬렉션 분리와 병합
```kotlin
fun main() {
    val list = listOf(1, 2, 3, 4, 5, 6)
    val list2 = listOf(1,2)

    list.union(list2) // 중복 요소는 하나만 넣고 병합
    list.plus(list2) // 중복 요소 포함해서 병합 +와 같음

    // 식에 따라 컬렉션을 2개로 분리해 Pair로 반환
    list.partition { it % 2 == 0 }  // ([2,4,6],[1,3,5)

    // 2개의 컬렉션에서 동일한 인덱스끼리 Pair를 만들어 반환
    list.zip(listOf(1, 2)) // [(1,1),(2,2)]
}
```

### 정렬
```kotlin
fun main() {
    val list = listOf(1, 2, 3, 4, 5, 6)

    list.reversed() // 뒤집힌 컬렉션 반환
    list.sorted() // 정렬된 컬렉션 반환
    list.sortedDescending() // 내림차순 컬렉션 반환
    list.sortedBy { it % 3 } // 비교식에 의해 오름차순 정렬된 컬렉션 반환
    list.sortedBy { it % 3 } // 비교식에 의해 내림차순 정렬된 컬렉션 반환
}
```

<br>

## 시퀀스 활용
코틀린의 시퀀스는 순차적인 컬렉션으로 요소의 크기를 특정하지 않고, 나중에 결정할 수 있는 특수한 컬렉션입니다.  
예를 들어 특정 파일에서 줄 단위로 읽어서 요소를 만들 때 해당 파일의 끝을 모르면 줄이 언제 끝날지 알 수 없는 경우가 있는데 이럴 때 사용할 수 있습니다.  
따라서 시퀀스는 처리 중에는 계산하고 있지 않다가 toList(), count() 같은 최종 연산에 의해 결정됩니다.  

### 요소 값 생성
특정 값을 생성하기 위해 generateSequence()를 사용합니다.  
이때 시드 인수를 주게 되고 그것에 의해 시작 요소 값이 결정됩니다.  
```kotlin
fun main() {
    // 시드 값 1을 시작으로 1씩 증가하는 시퀀스
    val nums: Sequence<Int> = generateSequence(1) { it + 1 }

    // take를 사용해 원하는 요소 개수만큼 획득하고 toList를 사용해 List 컬렉션으로 반환
    nums.take(10).toList() //[1,2,3,4,5,6,7,8,9,10]
    
    // map 연산 추가
    val squares = generateSequence(1) { it + 1 }.map { it * it }
    squares.take(10).toList() // [1,4,9,16,25,36,49,...100]
    
    // filter 연산 추가
    val oddSquares = squares.filter { it % 2 != 0 }
    oddSquares.take(5).toList() // [1,9,25,49,81]
}
```

### 요소 값 가져오기
중간 연산 결과 없이 한 번에 끝까지 연산한 후 결과를 반환하려면 asSequence() 를 사용할 수 있습니다.  
특히 filter, map을 메서드 체이닝해서 사용할 경우 순차적 연산이기 때문에 시간이 많이 걸릴 수 있지만 asSequence()를 사용하면 __병렬 처리__ 되기 때문에 성능이 좋아집니다.  
```kotlin
fun main() {
    val list = listOf(1, 2, 3, 4, 5)

    val listSequence = list.asSequence()
        .map { it * it }
        .filter { it % 2 == 0 }
        .toList()

    println(listSequence)
}
```
메서드 체이닝으로 map과 filter만으로는 결과를 도출할 수 없고 최종 마지막에 List를 만들기 때문에 빠르게 동작합니다.  
만약 asSequence를 사용하지 않고 바로 list.map .. 이런식으로 사용하게 된다면 map을 하고 새로운 List를 만들고 이걸 다시 filter 해서 새로운 리스트를 만들기 때문에 성능이 떨어지게 됩니다.  
다만 작은 컬렉션에는 시퀀스를 사용하지 않는 것이 좋습니다.  
filter 등은 인라인 함수로 설계되어 있는데, 시퀀스를 사용하게 되면 람다식을 저장하는 객체로 표현되기 때문에 인라인되지 않아 작은 컬렉션에는 오히려 좋지 않습니다.  
또한 한 번 계산된 내용은 메모리에 저장하기 때문에 시퀀스 자체를 인자로 넘기는 형태는 사용하지 않는 것이 좋습니다.  

