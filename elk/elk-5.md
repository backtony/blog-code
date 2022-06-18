# Spring Data Elasticsearch 개념 및 문서 번역


## Elasticsearch
Elasticsearch는 Apache Lucene 기반의 Java 오픈소스 분산형 RESTful 검색 및 분석 엔진입니다.  
방대한 양의 데이터에 대해 실시간으로 저장과 검색 및 분석 등의 작업을 수행할 수 있습니다.  
특히 정형 데이터, 비정형 데이터, 지리 데이터 등 모든 타입의 데이터를 처리할 수 있는데 이는  JSON 문서(Document)로 데이터를 저장하기 때문입니다.  
Elasticsearch는 단독 검색을 위해 사용하거나, ELK(Elasticsearch & Logstash & Kibana) 스택을 기반으로 사용합니다.  

### ELK
![그림1](https://github.com/backtony/blog-code/blob/master/elk/img/5/1-1.PNG?raw=true)  
ELK에 대해 간단하게만 알아보겠습니다.
+ Filebeat
    - 로그를 생성하는 서버에 설치해 로그를 수집하여 Logstash 서버로 로그를 전송합니다.
+ Logstash
    - 로그 및 트랜잭션 데이터를 수집하여 집계 및 파싱하고 Elasticsearch로 전달합니다.
    - 정제 및 전처리를 담당합니다.
+ Elasticsearch
    - Logstash로부터 전달받은 데이터를 저장하고, 검색 및 집계 등의 기능을 제공합니다.
+ Kibana
    - 저장된 로그를 Elasticsearch의 빠른 검색을 통해 가져와 시각화 및 모니터링하는 기능을 제공합니다.

### DBMS와 Elasticsearch 용어 비교

DBMS|Elasticsearch
---|---
DBMS HA 구성(MMM, M/S)|Cluster
DBMS Instance|Node
Table|Index
Partition|Shard/ Routing
Row|Document
Column|Field
Row of columnar data|Serialized JSON document
Join|Nested or Parent/Child
SQL(DML)|QueryDSL
Index|Analyzed
Primary Key|_id
Configuration|elasticsearch.yml & settings
Schema|Mappings


Elasticsearch는 데이터를 JSON 문서(Document)로 직렬화된 자료구조를 저장하는 방식을 채택하고 있기 때문에 RDB용어를 그대로 사용하지 않습니다.  

### 역색인
![그림2](https://github.com/backtony/blog-code/blob/master/elk/img/5/1-2.PNG?raw=true)  
일반적으로 책의 맨 앞쪽에 있는 목차가 Index라면 책의 후반부에 키워드마다 어느 페이지에 있는지 적어놓은 것이 역색인입니다.  
역색인은 각 Document에 등장하는 모든 고유한 단어들을 리스트업하고, 해당 단어들이 등장하는 Document들을 식별합니다.  


### 아키텍처
![그림3](https://github.com/backtony/blog-code/blob/master/elk/img/5/1-3.PNG?raw=true)  


### 구성요소
![그림4](https://github.com/backtony/blog-code/blob/master/elk/img/5/1-4.PNG?raw=true)  
Elastic 구성요소는 각각은 집합 관계를 갖습니다.  
예를 들어, document의 집합이 shard가 되는 식입니다.  
+ Cluster
    - 최소 하나 이상의 노드로 이뤄진 노드들의 집합
    - 서로 다른 클러스터는 데이터의 접근 및 교환을 할 수 없는 독립적인 시스템
+ Node
    - Elasticsearch를 구성하는 하나의 단위 프로세스
    - Elasticsearch 자체라고 봐도 무방합니다.
+ Index    
    - 분산된 Shard에 저장된 문서들의 논리적 집합으로 테이블 개념입니다.
    - ES에서는 하나의 인덱스에 여러 개의 샤드가 있고 이 샤드 안에 여러 개의 도큐먼트를 담아서 보관합니다. 그리고 샤드는 각 노드에 분산되어 저장됩니다.
    - RDB에서는 하위 정보를 다른 테이블로 관리하고 읽을 때 join을 했지만 elasticsearch에서는 도큐먼트 개념을 사용하여 모든 데이터를 하나의 도큐먼트로 관리합니다.

```json
// document 샘플
{
    "상품" : "사과",
    "가격" : "10000",
    // 하위 개념을 하나의 doc로 관리합니다.
    "배송업체" : {
      "업체이름" : "빠르게배달",
      "전화번호" : "010-0000-0000"
    }
}
```
이런 도큐먼트들이 Index라는 논리적인 테이블 안에 저장되어 있습니다.  

<br><br>

![그림5](https://github.com/backtony/blog-code/blob/master/elk/img/5/1-5.PNG?raw=true)  

+ Shard
    - 인덱스의 도큐먼트를 분산 저장하는 저장소입니다.
    - 인덱스는 도큐먼트를 모아 놓은 집합인데 샤드의 개수에 따라 도큐먼트를 분산해서 저장합니다.
    - 그림은 1개의 노드의 인덱스에서 샤드 2개를 가지고 있으며 각 샤드에 도큐먼트를 분산해서 저장한 상태의 그림입니다.

![그림6](https://github.com/backtony/blog-code/blob/master/elk/img/5/1-6.PNG?raw=true)  

만약 노드가 추가된다면 샤드(초록색)가 각 노드로 분배되어 저장됩니다.  
처음에 생성된 샤드를 Primary Shard라고 합니다.  

<br><br>

![그림7](https://github.com/backtony/blog-code/blob/master/elk/img/5/1-7.PNG?raw=true)  

Primary Shard의 복제본을 Replica shard라고 하고 서로 다른 노드에 저장됩니다.  
결과적으로 총 샤드의 개수는 5개에서 10개로 증가합니다.  
이로써 하나의 노드가 죽어도 데이터가 유실되지 않게 되어 가용성과 무결성을 보장합니다.  
만약 Primary 샤드가 유실된 경우 남아있던 복제본이 Primary shard로 승격되고 다른 노드에 새로 복제본을 생성합니다.  


## Spring Data Elasticsearch 개념 잡기
Spring Data Elasticsearch 프로젝트는 Elasticsearch 검색 엔진을 사용하는 솔루션 개발을 도와주는 모듈입니다.  
Spring Data JPA가 Repository 인터페이스에 정의한 메서드 이름을 분석해서 JPQL을 자동으로 생성 및 실행해주는 것처럼, Spring Data Elasticsearch 또한 Repository 인터페이스에 메서드를 정의함으로써 쿼리를 표현할 수 있습니다.  
spring data elasticsearch가 어떤 기능을 제공하는지 알아봅시다.  
[spring data elasticsearch 문서](https://docs.spring.io/spring-data/elasticsearch/docs/current/reference/html/#reference) 번역을 참고했습니다.

### Elasticsearch Clients
Spring Data Elasticsearch는 단일 Elasticsearch 노드 또는 클러스터에 연결된 Elasticsearch 클라이언트에서 작동합니다.  
Elasticsearch Client를 사용하여 클러스터 작업을 수행할 수 있지만 Spring Data Elasticsearch를 사용하는 애플리케이션은 일반적으로 상위 레벨의 Elasticsearch Operations 및 Elasticsearch Repositories를 사용합니다.

#### High Level REST Client
Transport Client는 7버전부터 Deprecated 되었고 7버전 이후부터는 High Level REST Client를 사용합니다.  
High Level REST Client는 Elasticsearch의 기본 클라이언트로 요청을 수락하고 응답 객체를 반환합니다.

```java
@Configuration
public class RestClientConfig extends AbstractElasticsearchConfiguration {

    @Override
    @Bean
    public RestHighLevelClient elasticsearchClient() {
        // 클러스터 주소를 제공하기 위해 builder를 사용한다. 디폴트 HttpHeaders나 사용가능한 SSL로 셋한다.        
        final ClientConfiguration clientConfiguration = ClientConfiguration.builder()  
            .connectedTo("localhost:9200")
            .build();
        // RestHighLevelClient를 만든다.
        return RestClients.create(clientConfiguration).rest();                         
    }
}

// ...

  @Autowired
  RestHighLevelClient highLevelClient;

  // LowLevelRest() 메소드로 RestClient를 얻는 것도 가능하다.
  RestClient lowLevelClient = highLevelClient.lowLevelClient();                        

// ...

IndexRequest request = new IndexRequest("spring-data", "elasticsearch", randomID())
  .source(singletonMap("feature", "high-level-rest-client"))
  .setRefreshPolicy(IMMEDIATE);

IndexResponse response = highLevelClient.index(request);
```

### Elasticsearch Object Mapping
Elasticsearch Object Mapping는 __Java 오브젝트(도메인 엔티티)를 ES와 뒷단에 저장된 JSON 형식에 매핑__ 하는 과정입니다.  
4.0 이전에는 Jackson 기반 변환을 사용했지만 4.0부터는 더이상 사용하지 않고 Meta Object Mapping만 사용되며 MappingElasticsearchConverter가 사용됩니다.  

#### Meta Model Object Mapping
Meta Model 기반의 접근방식은 ES에서 읽기/쓰기를 위해 도메인의 타입 정보를 사용합니다.  
이를 통해서 도메인 타입 매칭을 위한 converter 인스턴스를 등록할 수 있습니다.  
MappingElasticsearchconverter는 메타데이터를 사용하여 도큐먼트로의 오브젝트 매핑을 수행합니다.  
메타데이터는 애노테이션이 달린 도메인 엔티티의 property에서 가져오고 제공하는 애노테이션은 다음과 같습니다.

+ @Document : ES에 매핑할 클래스임을 나타내고 클래스 레벨에 적용합니다. 다음과 같은 속성을 제공합니다.
    - indexName : 이 엔티티를 저장할 인덱스 이름
    - createIndex : repository 부트스트래핑에 인덱스를 부여할지 여부, default = true
    - versionType : 버전 관리, default= EXTERNAL
+ @Id : ID 목적으로 사용되는 필드 표기
+ @Transient : 기본적으로 모든 필드는 저장 또는 검색 시 도큐먼트에 매핑되는데 이 주석이 포함된 필드는 제외
+ @PersistenceConstructor : 데이터베이스에서 개체를 인스턴스화할 때 사용할 생성자(보호된 패키지도 포함). 생성자 argument는 검색된 문서의 키 값에 이름에 의해 매핑
+ @Field : 필드의 속성 정의, 대부분의 속성이 Elasticsearch Mapping 정의에 매핑
    - name : ES 문서에 나타낼 필드 이름, 설정되지 않을 경우 Java 필드 이름 그대로 사용
    - type : 필드 유형 정의, [유형 참조 문서](https://www.elastic.co/guide/en/elasticsearch/reference/current/mapping-types.html)
    - format 및 pattern : Date 타입의 필드의 경우 반드시 format을 정의해야 한다.
    - store : 원래 필드 값이 ES에 저장되야 하는지 판단하는 플래그, default = false
    - 커스텀 analyzer와 normalizer를 지정하여 커스텀마이징하는 analyzer, searchAnalyzer, Normalizer
+ @GeoPoint : 필드가 geo_point 데이터 형식임을 표기, 만약 필드가 GeoPoint 클래스의 인스턴스인 경우 생략 가능


> TemporalAccessor에서 파생되는 속성의 FieldType.Date 타입은 @Field 어노테이션이 있어야하며 이 타입에는 커스텀 converter를 등록해야 합니다.  
Elasticsearch 7의 변동으로 인해 사용자 정의 날짜 형식을 사용하는 경우, 연도에 yyyy 대신 uuuu를 사용해야 합니다.

<Br>

Field의 type에 관해 자세히 알아봅시다.  

데이터 타입| 설명
---|---
text|전문 검색이 필요한 데이터로 텍스트 분석기가 텍스트를 작은 단위로 분리
keyword|정렬이나 집계에 사용되는 텍스트 데이터로 분석을 하지 않고 원문 통째로 인덱싱
date|날짜/시간 데이터
btye, short, integer, long|btye : 부호있는 8비트 데이터 <br>short : 부호 있는 16비트 데이터 <br>integer : 부호 있는 32비트 데이터<br>long : 부호 있는 64비트 데이터
scaled_float, half_float, double, float|scaled_float : float 데이터에 특정 값을 곱해서 정수형으로 바꾼 데이터, 정확도는 떨어지나 필요에 따라 집계 등에 효율적으로 사용<br>half_float : 16비트 부동 소수점 실수 데이터<br>double : 32비트 부동소수점 실수 데이터<br>float : 64비트 부통소수점 실수 데이터
boolean|참/거짓 데이터로 true/false 값
ip|ipv4, ipv6 타입 ip주소
geo-point, geo-shape|geo-point : 위도, 경도 값<br>geo-shape : 하나의 위치 포인트가 아닌 임의의 지형
integer_range, long_range, float_range, double_range, ip_range, date_rage|범위를 설정할 수 있는 데이터로 최소, 최대값을 통해 입력
object|계층 구조를 갖는 형태로 필드 안에 다른 필드들이 들어갈 수 있다.
nested|배열형 객체를 저장한다. 객체를 따로 인덱싱하여 객체가 하나로 합쳐지는 것을 막고 배열 내부의 객체에 쿼리로 접근 가능
join|부모/자식 관계를 표현할 수 있다.

```java
public class MemberDocument {

    @Id
    @Field(type = FieldType.Keyword)
    private Long id;

    @Field(type = FieldType.Text)
    private String name;
}
```
실제로 사용할 때는 위와 같이 애노테이션으로 매핑하는 방법과 json을 통해 따로 매핑관련 정의를 작성한 뒤 @Setting 애노테이션을 클래스에 부착하고 json 파일 경로로 매핑하는 방식이 있습니다.  
간단한 경우라면 애노테이션을 사용하는 것이 편리하지만, 복잡해질 경우 json 파일을 따로 만들어 매핑하는 것이 좋습니다.  
json 파일을 매핑하더라도 date타입의 경우에는 앞서 설명한 것 처럼 @Field 애노테이션이 있어야 합니다.  
아무것도 매핑하지 않는다면 ES가 자동으로 데이터 타입에 맞춰 인덱스를 아래와 같이 매핑해줍니다.  


원본 소스 데이터 타입|다이내믹 매핑으로 변환된 데이터 타입
null|필드를 추가하지 않음
boolean|boolean
float|float
integer|long
object|object
string|string 데이터 형태에 따라 data, text, keyword

<br>

__텍스트 타입과 키워드 타입 설명 및 예시__  
+ 텍스트 타입
  - 문장이나 여러 단어가 나열된 문자열
  - 기본적으로 집계나 정렬을 지원하지 않습니다.
  - 텍스트 타입으로 지정된 문자열은 분석기에 의해 토큰으로 분리되고, 토큰들이 인덱싱 되는데 이를 역인덱싱이라고 하고 역인덱싱 된 토큰들을 용어(term)이라고 합니다.
+ 키워드 타입
  - 카테고리, 사람 이름, 브랜드 등 규칙성이 있거나 유의미한 값들의 집합
  - 텍스트 타입과 달리 분석기를 거치지 않고 문자열 전체가 하나의 용어로 인덱싱
  - 데이터 부분 일치 검색은 어렵지만 완전 일치 검색을 위해 사용할 수 있으며 집계, 정렬에 사용 가능
+ 멀티 필드
  - 단일 필드 입력에 대해 여러 하위 필드를 정의하는 기능으로 fields라는 매핑 파라미터가 사용됩니다.
  - fields는 하나의 필드를 여러 용도로 사용할 수 있게 만들어줍니다.
  - 예를 들면, 문자열의 경우 전문 검색이 필요하면서도 정렬도 필요한 경우가 있는데 여기에 텍스트 타입과 키워드 타입을 같이 적용하는 방식입니다.

```json
// 인덱싱 해놓고 http://localhost:9200/인덱스이름?format=json&pretty 에 접속해보면
"name" : {
  "type" : "text",
  "fields" : {
    "keyword" : {
      "type" : "keyword",
    }
  }
}
```
name 필드가 text 텍스트 타입이면서도 fields로 인해 키워드 타입으로도 동시에 사용되고 있는 것을 확인할 수 있습니다.  

#### 분석기
ES는 전문 검색을 지원하기 위해 역인덱싱 기술을 사용합니다.  
전문 검색은 장문의 문자열에서 부분 검색을 수행하는 것이며, 역인덱싱은 장문의 문자열을 분석해 작은 단위로 쪼개어 인덱싱하는 기술입니다.  
역인덱싱을 이용한 전문 검색에서 양질의 결과를 얻기 위해서는 문자열을 나누는 기준이 중요하며, 이를 지원하기 위해 ES는 캐릭터 필터, 토크나이저, 토큰 필터로 구성되어 있는 분석기 모듈을 제공합니다.

> 캐릭터 필터(여러개가능) -> 토크나이저 -> 토큰 필터(여러개가능)

분석기에는 반드시 하나의 토크나이저가 포함돼야 하며, 캐릭터 필터와 토큰 필터는 옵션으로 없거나 혹은 복수로 여러 개를 함께 사용해도 됩니다.  
+ 캐릭터 필터 : 입력받은 문자열을 변경하거나 불필요한 문자들을 제거
+ 토크나이저 : 문자열을 토큰으로 분리하고 분리할 때 토큰의 순서나 시작, 끝 위치도 기록
+ 토큰 필터 : 분리된 토큰들의 대소문자 구분, 형태소 분석 등의 필터 작업을 수행

<br>

__cf) 토큰과 용어__  
앞으로 토큰과 용어라는 단어가 많이 나오게 되는데 잠깐 정리하고 넘어갑시다.  
Cute Dog라는 문자열이 분석기에 들어온다고 해봅시다. Cute Dog는 캐릭터 필터에 의해 불필요한 문자들이 제거되고 토크나이저에 의해 Cute, Dog로 분리됩니다. 이때 잘린 단위를 토큰이라고 합니다. 이런 토큰들이 복수의 토큰 필터를 거쳐 정제되고 최종으로 역인덱스에 저장되는데 이때 상태의 토큰을 용어(Term)이라고 합니다.  
정리하면, 토큰 분석기 내부에 일시적으로 존재하는 상태를 토큰이라고 하고 인덱싱되어 있는 단위 또는 검색에 사용되는 단위를 모두 용어(Term)이라고 합니다.  

##### 대표적인 분석기

분석기|설명
---|---
standard|특별한 설정이 없으면 ES가 기본적으로 사용하는 분석기입니다. <br>영문법을 기준으로 한 스탠다드 토크나이저와 소문자 변경 필터, 스톱 필터가 포함되어 있습니다.
simple|문자만 토큰화합니다.<br>공백, 숫자, 하이픈, 작은따옴표 같은 문자는 토큰화되지 않습니다.
whitespace|공백을 기준으로 구분하여 토큰화합니다.
stop|simple 분석기와 비슷하지만 스톱 필터가 포함되어있습니다.

##### 대표적인 토크나이저

토크나이저|설명
standard|스탠다드 분석기가 사용하는 토크나이저로 특별한 설정이 없으면 기본 설정으로 사용됩니다.<br>쉼표, 점 같은 기호를 제거하며 텍스트 기반으로 토큰화합니다. 
lowercase|텍스트 기반으로 토큰화하며 모든 문자를 소문자로 변경해 토큰화합니다.
ngram|원문으로부터 N개의 연속된 글자 단위를 모두 토큰화합니다.<br>예를 들어 '엘라스틱서치'라는 원문을 2gram으로 토큰화 한다면 [엘라,라스,스틱,틱서,서치]와 같이 연속된 두 글자를 모두 추출합니다.<br>모든 조합을 얻어낼 수 있기 때문에 정밀한 부분 검색에 강점이 있지만, 토크나이징을 수행한 N개 이하의 글자 수로는 검색이 불가능하며 모든 조합을 추출하기에 저장공간을 많이 사용한다는 단점이 있습니다.
uax_url_email|스탠다드 분석기와 비슷하지만 URL이나 이메일을 토큰화하는 데 강점이 있습니다.

##### 캐릭터 필터
캐릭터 필터는 토크나이저 전에 위치하여 문자들을 전처리하는 역할을 합니다. HTML 문법을 제거/변경하거나 특정 문자가 왔을 때 다른 문자로 대체하는 일들을 합니다.  
예를 들면, HTML에서 공백문자(&nbsp)가 오면 공백으로 바꿔주는 작업 등을 수행합니다.  
ES에서 제공하는 분석기는 대부분 캐릭터 필터가 포함되어 있지 않기 때문에 사용하기 위해서는 커스텀 분석기를 만들어 사용하는 것이 좋습니다.  

##### 대표적인 토큰 필터
토큰 필터는 토크나이저에 의해 토큰화되어 있는 문자들에 필터를 적용할 때 사용합니다.

필터|설명
---|---
lowercase|모든 문자를 소문자로 변환합니다. 반대로는 uppercase가 있습니다.
stemmer|영어 문법을 분석하는 필터입니다.<br>영어 기반이라 한글은 동작하지 않습니다. 한글의 경우 아리랑, 노리 같은 오픈소스가 있습니다.
stop|기본 필터에서 제거하지 못하는 특정한 단어를 제거할 수 있습니다.<br>주로 불용어 a, the, you 같은 단어를 제거하는데 사용되는데 영어 기반이라 한글은 동작하지 않습니다.



##### 커스텀 분석기 만들기
customer_analyer라는 인덱스를 하나 만들고 해당 인덱스는 my_analyer라는 커스텀 분석기를 갖도록 만들어 봅시다.  
키바나 콘솔에서 진행하면 됩니다.
```json
PUT customer_analyzer
{
  "settings": {
    "analysis": {      
      "analyzer": {
        "my_analyer": {
          "type": "custom",
          "char_filter": [],
          "tokenizer": "standard",
          "filter": ["lowercase","my_stopwords"]
        }
      },
      "filter": {
        "my_stopwords":{
          "type": "stop",
          "stopwords": ["lions"]
        }
      }
    }
  }
}
```
customer_analyer라는 인덱스를 만들고 인덱스 설정(settings)에 analysis 파라미터를 추가하고 analysis 파라미터 밑에 필터(filter)와 분석기(analyzer)를 만들었습니다.  
분석기 이름은 my_analyzer이고 타입을 custom으로 지정하면 커스텀 분석기로 인식합니다.  
분석기에는 반드시 하나의 토크나이저가 들어가야 하므로 기본인 standard를 사용했고 캐릭터 필터(char_filter)는 사용하지 않았습니다.  
토큰 필터는 2개를 사용했는데 소문자 변경(lowercase)와 사용자가 만든 필터(my_stopwords)를 등록했습니다.  
사용자가 만든 필터는 analysis 파라미터 밑에 필터(filter)에서 원하는 이름(my_stopwords)을 지정하고 타입과 타입에 맞는 설정을 해주고 사용하면 됩니다.  
내장되어 있는 스톱(stop)필터에 사용자가 지정한 불용어를 추가했습니다.  
여기서는 lions라는 단어를 불용어로 인식하도록해 lions 단어가 나오면 불용어로 처리되도록 했습니다.  

<Br>

```json
GET customer_analyzer/_analyze
{
  "analyzer": "my_analyer",
  "text": "Cats Lions Dogs"
}

// 결과
{
  "tokens" : [
    {
      "token" : "cats",
      "start_offset" : 0,
      "end_offset" : 4,
      "type" : "<ALPHANUM>",
      "position" : 0
    },
    {
      "token" : "dogs",
      "start_offset" : 11,
      "end_offset" : 15,
      "type" : "<ALPHANUM>",
      "position" : 2
    }
  ]
}
```
하나씩 따라가보면 먼저 스탠다드 토크나이저에 의해 Cats, Lions, Dogs로 토큰화 되고 소문자 변경 필터에 의해 cats, lions, dogs로 변경됩니다. 마지막으로 사용자 필터에 의해 lions가 불용어 처리되어 제거됩니다.  
만들어준 커스텀 분석기(my_analyzer)는 다른 인덱스에서는 동작하지 않고 customer_analyer 인덱스 내에서만 유효합니다.  

##### 필터 적용 순서
커스텀 분석기에서 필터를 여러 개 사용한다면 필터의 순서에도 주의해야 합니다.  
필터 배열의 첫 번째 순서부터 필터가 적용되므로 이를 인지하지 못하면 원치않는 결과가 나오기도 합니다.  
앞선 예시에서 필터 순서를 바꾸게 되면 입력값이 Lions인데 불용어 처리는 소문자 lions이므로 Lions가 걸러지지 않게 됩니다.


##### 적용
커스텀 필터를 만들어서 자바 코드 내에서 적용하고 싶다면 다음과 같이 합니다.
```java
@Document(indexName = "lecture")
@Setting(settingPath = "elasticsearch/lecture-settings.json")
@Mapping(mappingPath = "elasticsearch/lecture-mappings.json")
public class LectureDocument {

    @Id
    private String id;

    private Long lectureId;

    private String imageUrl;
    
    ... 생략
}
```
+ @Setting : 분석기를 매핑합니다.
+ @Mapping : 타입을 매핑합니다.

__lecture_mappings.json__
```json
{
  "properties": {
    "lectureId" : {"type" : "integer"},
    "imageUrl" : {"type" : "text"},
    "title" : {
      "type" : "text",
      "analyzer": "my_analyzer"
    },
    "description" : {
      "type" : "text",
      "analyzer": "my_analyzer"
    },
    "finishedProductText" : {
      "type" : "text",
      "analyzer": "my_analyzer"
    },

    // ... 생략
  }
}
```
<br>

__lecture_settings.json__
```json
{
  "analysis": {
    "analyzer": {
      "my_analyzer": {
        "type": "custom",
        "tokenizer": "nori_tokenizer_mixed_dict",
        "filter": "my_posfilter"
      }
    },
    "tokenizer": {
      "nori_tokenizer_mixed_dict": {
        "type": "nori_tokenizer",
        "decompound_mode": "mixed",
        "discard_punctuation": "false"
      }
    },
    "filter": {
      "my_posfilter": {
        "type": "nori_part_of_speech",
        "stoptags": [          
          // 생략
        ]
      }
    }
  }
}
```

#### Mapping Rules
##### Type Hints
매핑은 서버로 전송 된 도큐먼트에 포함된 Type hints를 사용하여 일반적인 타입 매핑을 수행합니다.  
type hints는 도큐먼트에서 _class 속성으로 표기되며 각 aggregate root에 대해서 작성됩니다.
```java
public class Person {              

  @Id 
  String id;
  String firstname;
  String lastname;
}

{
  "_class" : "com.example.Person", //기본적으로 도메인 타입의 클래스 이름이 type hint로 사용된다.
  "id" : "cb7bef",
  "firstname" : "Sarah",
  "lastname" : "Connor"
}
```
사용자 지정 정보로 type hints를 구성하고 싶으면 @TypeAlias를 사용하면 됩니다.
```java
@TypeAlias("human")                
public class Person {

  @Id String id;
  // ...
}

{
  "_class" : "human",              
  "id" : ...
}
```

##### Geospatial Types
Point, GeoPoint 같은 Geospatial Type은 lat/lon 타입으로 변환됩니다.
```java
public class Address {

  String city, street;
  Point location;
}

{
  "city" : "Los Angeles",
  "street" : "2800 East Observatory Road",
  "location" : { "lat" : 34.118347, "lon" : -118.3026284 }
}
```

##### Maps
Map 내의 값에 대해서는 type hint와 커스텀 변환에 있어서 aggregate root와 동일한 매핑 규칙이 적용됩니다.  
그러나 Map key는 ES에 의해 처리되기 위해선 String이여야 합니다.
```java
public class Person {

  // ...

  Map<String, Address> knownLocations;

}

{
  // ...

  "knownLocations" : {
    "arrivedAt" : {
       "city" : "Los Angeles",
       "street" : "2800 East Observatory Road",
       "location" : { "lat" : 34.118347, "lon" : -118.3026284 }
     }
  }
}
```

### Elasticsearch Operations
Spring Data Elasticsearch는 Elasticsearch 색인에 대해 호출 가능한 연산을 정의하기 위해 여러 인터페이스를 사용합니다.
+ IndexOperations
    - 인덱스 생성 또는 삭제와 같은 인덱스 수준에서 작업을 정의합니다.
+ DocumentOperations
    - ID에 따라 엔티티를 저장, 업데이트 및 검색하는 작업을 정의합니다.
+ SearchOperations
    - 쿼리를 사용하여 여러 엔티티를 검색하는 작업을 정의합니다.
+ ElasticSearchOperations
    - DocuementOperations와 SearchOperations 인터페이스를 결합합니다.


인터페이스의 기본 구현체는 다음을 제공합니다.
+ 인덱스 관리 기능
+ 도메인 타입에 대한 읽기/쓰기 매핑 지원
+ 풍부한 쿼리와 criteria(기준) API
+ 리소스 관리와 예외 반환

__ElasticsearchOperations.java__
```java
public interface ElasticsearchOperations extends DocumentOperations, SearchOperations {
    //...
}
```
Elasticsearch 관련 작업을 수행할 때 주로 ElasticsearchOperations 인터페이스의 구현체를 사용하게 됩니다.  


#### ElasticsearchRestTemplate
ElasticsearchTemplate은 사용은 버전 4.0에서 더 이상 사용되지 않고 ElasticsearchRestTemplate를 사용합니다.  
ElasticsearchRestTemplate는 High Level REST Client를 사용해 ElasticsearchOperations 인터페이스를 구현한 것입니다.  
```java
@Configuration
public class RestClientConfig extends AbstractElasticsearchConfiguration {
  @Override
  public RestHighLevelClient elasticsearchClient() {       
    // High Level REST Client를 셋팅한다.
    return RestClients.create(ClientConfiguration.localhost()).rest();
  }

  // no special bean creation needed    
  // 베이스 클래스인 AbstractElasticsearchConfiguration는 이미 elasticsearchTemplate 빈을 제공한다.
}
```

__예시__  
Spring REST 컨트롤러에서 주입된 ElasticsearchOperations 인스턴스를 사용하는 방법입니다.
```java
@RequestMapping("/")
public class TestController {

  private  ElasticsearchOperations elasticsearchOperations;

  // 생성자를 이용하여 ElasticsearchOperations 빈을 자동 주입한다.
  public TestController(ElasticsearchOperations elasticsearchOperations) { 
    this.elasticsearchOperations = elasticsearchOperations;
  }

  // ES 클러스터에 엔티티를 저장한다.
  @PostMapping("/person")
  public String save(@RequestBody Person person) {                         

    IndexQuery indexQuery = new IndexQueryBuilder()
      .withId(person.getId().toString())
      .withObject(person)
      .build();
    String documentId = elasticsearchOperations.index(indexQuery);
    return documentId;
  }

  // id를 사용하는 쿼리로 엔티티를 검색한다.
  @GetMapping("/person/{id}")
  public Person findById(@PathVariable("id")  Long id) {                   
    Person person = elasticsearchOperations
      .queryForObject(GetQuery.getById(id.toString()), Person.class);
    return person;
  }
}
```

### Search Result Types
DocumentOperations 인터페이스의 메서드를 이용해 도큐먼트를 검색하면 찾아진 엔티티만을 반환합니다.  
SearchOperations 인터페이스의 메서드로 검색한 경우엔 각 엔티티에 대한 추가적인 정보가 더해집니다. 이를테면 찾은 엔티티의 score나 sortValues 같은 것들 입니다.  
이런 정보를 반환하기 위해서 각 엔티티는 추가 정보를 포함하는 SearchHit 오브젝트로 래핑됩니다.  
이러한 SearchHit 오브젝트는 maxScore나 요청된 집계와 같은 전체 검색에 대한 정보를 포함하고 있는 SearchHits 오브젝트에서 반환됩니다.  
다음과 같은 클래스와 인터페이스를 사용할 수 있습니다.  
<Br>

__SearchHit\<T>__  
다음과 같은 정보를 포함합니다.
+ Id
+ Score
+ Sort Values
+ Highlight fields
+ inner hits
+ The retrieved entity of type\<T>

<Br>

__SearchHits\<T>__  
다음의 정보를 포함합니다.
+ Number of total hits
+ Total hits relation
+ Maximum score
+ A list of SearchHit\<T> objects
+ Returned aggregations
+ Returned suggest results

<br>

__SearchPage\<T>__  
SearchHits\<T> 요소에 포함되며 레파지토리 메소드를 사용할 때 페이징 엑세스에 사용되는 spring data Page를 정의합니다.  

<br>

__SearchScrollHits\<T>__  
ElasticsearchRestTemplate의 낮은 수준의 scroll API 함수로 반환되며 Elasticsearch scroll ID로 SearchHits\<T>를 풍부하게 합니다.  

<br>

__SearchHitsIterator\<T>__  
SearchOperations 인터페이스의 스트리밍 함수에 의해 반환되는 Iterator  

### Elasticsearch Repositories
Elasticsearch 모듈은 문자열 쿼리, 기본 검색 쿼리, 기준 기반 쿼리 또는 메서드 이름에서 파생된 모든 기본 쿼리 구축 기능을 지원합니다.

#### method names query
```java
interface BookRepository extends Repository<Book, String> {
    // 일반적인 네이밍 쿼리 사용 가능
    List<Book> findByNameAndPrice(String name, Integer price);
}

// 위 메서드는 Elasticsearch json 쿼리로 번역됩니다.
{
    "query": {
        "bool" : {
            "must" : [
                { "query_string" : { "query" : "?", "fields" : [ "name" ] } },
                { "query_string" : { "query" : "?", "fields" : [ "price" ] } }
            ]
        }
    }
}
```
네이밍 쿼리에 지원하는 키워드는 [공식 문서](https://docs.spring.io/spring-data/elasticsearch/docs/current/reference/html/#elasticsearch.query-methods.criterions)를 참고 바랍니다.

#### Method return type
다음과 같은 리턴 타입을 지원합니다.
+ List\<T>
+ Stream\<T>
+ SearchHits\<T>
+ List\<SearchHit\<T>\>
+ Stream\<SearchHit\<T>\>
+ SearchPage\<T>

#### @Query와 NativeSearchQuery
복잡한 쿼리의 경우 @Query 애노테이션을 사용해 직접 Elasticsearch JSON 쿼리를 작성해야 합니다.
```java
interface BookRepository extends ElasticsearchRepository<Book, String> {
    @Query("{\"match\": {\"name\": {\"query\": \"?0\"}}}")
    Page<Book> findByName(String name,Pageable pageable);
}
```
"query" element의 값으로 대치되어 Easticsearch로 전송됩니다.  
예를 들어 함수를 매개 변수 John과 함께 호출할 경우 다음과 같은 쿼리 본문을 생성합니다.
```json
{
  "query": {
    "match": {
      "name": {
        "query": "John"
      }
    }
  }
}
```
<br>

더 복잡한 쿼리를 사용해야 하는 경우 NativeSearchQuery를 사용해야 합니다.  
관련 정보는 [공식 문서](https://docs.spring.io/spring-data/elasticsearch/docs/current/reference/html/#elasticsearch.operations.nativesearchquery)를 참고하세요.  


### Annotation Based Configuration
Spring Data Elasticsearch repository를 사용하기 위해서는 JavaConfig의 애노테이션을 통해 활성화 시켜야 합니다.
```java
@EnableElasticsearchRepositories(basePackageClasses = {정의한elasticsearchrepository.class})
@Configuration
public class ElasticSearchConfig {}
```



<Br><Br>

__참고__  
<a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl.html" target="_blank"> elastic search 공식 문서</a>   
<a href="https://docs.spring.io/spring-data/elasticsearch/docs/current/reference/html/#reference" target="_blank"> spring data elasticsearch 공식 문서</a>   



