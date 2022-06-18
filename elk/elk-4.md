# Elasticsearch - 검색 및 집계 쿼리


## 엘라스틱서치 검색
키바나 콘솔에서 ES 검색 쿼리를 실습해보도록 하겠습니다.  
설치는 [docker-compose-github](https://github.com/deviantony/docker-elk)를 통해 docker-compose로 간단하게 설치할 수 있습니다.  
문서를 보고 진행하고 싶으시다면 [공식 문서](https://www.elastic.co/guide/en/elastic-stack-get-started/current/get-started-stack-docker.html)를 참고하세요.  


### 쿼리 컨텍스트와 필터 컨텍스트
__과거의 엘라스틱서치 검색__ 은 크게 쿼리 컨텍스트와 필터 컨텍스트로 구분할 수 있습니다. 과거의 내용이므로 가볍게 보고 넘어가면 됩니다.
+ 쿼리 컨텍스트 : 유사도를 계산해 이를 기준으로 더 정확한 결과를 먼저 보여줍니다.
  - 도큐먼트에서 '엘라스틱'이 포함되어 있는지 찾을 때는 쿼리 컨텍스트를 사용해 최대한 비슷한 도큐먼트를 찾아줍니다.
+ 필터 컨텍스트 : 유사도를 계산하지 않고 일치 여부에 따른 결과만 반환합니다.  
  - 제목이 '엘라스틱'인 문서를 찾기 위해서는 필터 컨텍스트를 사용하여 제목이 '엘라스틱'인지 아닌지만 예/아니요 결과만 확인하면 됩니다.  
  - 유사도를 계산하지 않기 때문에 쿼리 컨텍스트보다 빠르고 스코어 계산 결과에 대한 업데이트를 매번 수행할 필요가 없기 때문에 캐시를 사용할 수 있습니다.

키바나의 샘플 데이터로 테스트 해봅시다.
```json
// 쿼리 컨텍스트 실행
GET kibana_sample_data_ecommerce/_search
{
  "query" :{
    "match": {
      "category": "clothing"
    }
  }
}
```
_search는 검색 쿼리를 위해 ES에서 제공하는 API입니다.  
match는 전문 검색을 위한 쿼리로, 역인덱싱된 용어를 검색할 때 사용합니다.  
위 쿼리는 kibana_sample_data_ecommerce 인덱스에 있는 category 필드의 역인덱스 테이블에 clothing 용어가 있는 도큐먼트를 찾는 쿼리입니다.  
```json
// 결과
{
  "took" : 19,
  "timed_out" : false,
  "_shards" : {
    "total" : 1,
    "successful" : 1,
    "skipped" : 0,
    "failed" : 0
  },
  "hits" : {
    "total" : {
      "value" : 3927, // 찾은 도큐먼트 수
      "relation" : "eq"
    },
    "max_score" : 0.20545526, // 가장 높은 유사도
    "hits" : [
      {
        "_index" : "kibana_sample_data_ecommerce",
        "_type" : "_doc",
        "_id" : "oTCWOX8BYp6IL7uIcDW-",
        "_score" : 0.20545526,
        "_source" : {
          "category" : [
            "Men's Clothing"
          ],          
          // 생략...
        }
      }
    ]
  }  
}
```
<br>

```json
// 필터 컨텍스트 실행
GET kibana_sample_data_ecommerce/_search
{
  "query" :{
    "bool": {
      "filter": {
        "term": {
          "day_of_week": "Friday"
        }
      }
    }
  }
}
```
필터 컨텍스트와 쿼리 컨텍스트를 구분하는 특별한 API가 있는 것은 아니며 모두 search API를 사용합니다.  
필터 컨텍스트는 논리(bool) 쿼리 내부의 filter 타입을 사용합니다.  
위 쿼리는 kibana_sample_data_ecommerce 인덱스에 있는 day_of_week 필드가 Friday인 도큐먼트를 찾아달라는 요청입니다.  
```json
// 결과
{
  "took" : 5,
  "timed_out" : false,
  "_shards" : {
    "total" : 1,
    "successful" : 1,
    "skipped" : 0,
    "failed" : 0
  },
  "hits" : {
    "total" : {
      "value" : 770, // 770개의 도큐먼트 찾음
      "relation" : "eq"
    },
    "max_score" : 0.0, // 스코어 계산 안함
    "hits" : [
      {
        "_index" : "kibana_sample_data_ecommerce",
        "_type" : "_doc",
        "_id" : "tzCWOX8BYp6IL7uIcDW_",
        "_score" : 0.0,
        "_source" : {
          "category" : [
            "Women's Shoes",
            "Women's Clothing"
          ],
          // 생략
        }
      }
    ]
  }
}  
```
앞서 과거의 엘라스틱 서치 검색이라고 했습니다.  
1.x버전에서는 용어 검색과 용터 필터처럼 쿼리 컨텍스트와 필터 컨텍스트가 명확히 구분되어 문법상 쿼리와 필터 컨텍스트를 구분할 수 있었습니다.  
하지만 논리 쿼리가 나오면서 필터 컨텍스트는 모두 논리 쿼리에 포함되었고 필터 컨텍스트를 단독으로 사용하기보다는 쿼리/필터 컨텍스트를 조합해 사용하는 방향으로 가고 있습니다.  

### 쿼리 스트링과 쿼리 DSL
엘라스틱서치에서 쿼리를 사용하는 방법는 쿼리 스트링과 쿼리 DSL 두 가지가 있습니다.  
쿼리 스트링은 한 줄 정도의 간단한 쿼리에 사용하고 쿼리 DSL은 한 줄에 넣기 힘든 복잡한 쿼리에 사용합니다.  
쿼리 DSL은 ES에서 제공하는 쿼리 전용 언어로, JSON 기반의 직관적인 언어입니다.  

#### 쿼리 스트링
쿼리 스트링은 REST API의 URI 주소에 쿼리문을 작성하는 방식으로 실행해볼 수 있어 사용하기 쉽습니다.
```
GET kibana_sample_data_ecommerce/_search?q=customer_full_name:Mary
```
kibana_sample_data_ecommerce 인덱스 중 cutomer_full_name 필드에 Mary라는 용어가 포함된 도큐먼트를 검색합니다.  
curl 같은 툴에서 한 줄로 작성할 수 있어 타이핑하기 좋습니다.  

#### 쿼리 DSL
쿼리 DSL은 REST API의 요청 본문 안에 JSON 형태로 쿼리를 작성해야 합니다.  
```json
GET kibana_sample_data_ecommerce/_search
{
  "query": {
    "match": {
      "customer_full_name": "Mary"
    }
  }
}
```
match는 전문 검색을 할 때 사용하는 쿼리입니다.  
쿼리 스트링과 똑같은 요청을 보낸 것이므로 결과값도 같습니다.  

### 유사도 스코어
ES에서는 기본적으로 BM25 알고리즘을 이용해 유사도 스코어를 계산합니다.  
유사도 스코어는 질의문과 도큐먼트의 유사도를 표현하는 값으로, 스코어가 높을수록 찾고자 하는 도큐먼트에 가깝다는 것을 의미합니다.  
알고리즘 동작 방식을 이해한다면 더 효율적으로 인덱스를 디자인할 수 있기에 어떤 식으로 계산되는지 알아보기 위해서 explain옵션을 추가해 봅시다.
```json
GET kibana_sample_data_ecommerce/_search
{
  "query": {
    "match": {
      "products.product_name": "Pants"
    }
  },
  "explain": true
}
// 결과값이 너무 길어서 첨부하지 않겠습니다.
```
결과값을 보면 hits 배열에 들어있는 히트된 도큐먼트는 모두 score 값을 갖고 있습니다.  
스코어는 도큐먼트와 쿼리 간의 연관성 수치로 값이 클수록 연관성이 높습니다.  
BM25는 TF, IDF 개념에 문서 길이를 고려한 알고리즘입니다.  

#### IDF 계산
문서 빈도는 특정 용어가 얼마나 자주 등장했는지를 의미하는 지표입니다.  
일반적으로 자주 등장하는 용어는 중요하지 않을 확률이 높습니다.(to, the, 그리고, 그러나 같은 것들)  
따라서 도큐먼트 내에서 발생 빈도가 적을수록 가중치를 높게 주는데 이를 문서 빈도의 역수(IDF)라고 합니다.  
전체 문서에서 자주 발생하는 단어일수록 중요하지 않은 단어로 인식하고 가중치를 낮춥니다.  
IDF 계산식은 explain 옵션으로 검색했을 때 결과값에 보면 계산식을 보여줍니다.  
```json
"details" : [
  {
    "value" : 8.268259,
    "description" : "score(freq=1.0), computed as boost * idf * tf from:",
    "details" : [
      {
        "value" : 2.2,
        "description" : "boost",
        "details" : [ ]
      },
      {
        "value" : 7.1974354,
        "description" : "idf, computed as log(1 + (N - n + 0.5) / (n + 0.5)) from:",
        "details" : [
          {
            "value" : 3,
            "description" : "n, number of documents containing term",
            "details" : [ ]
          },
          {
            "value" : 4675,
            "description" : "N, total number of documents with field",
            "details" : [ ]
          }
        ]
      },
    ]
  }
]
```
일부만 가져와서 봅시다.  
n은 검색했던 용어(term)가 몇 개의 도큐먼트에 있는지 알려주는 값이고 N은 인덱스의 전체 도큐먼트 수 입니다.  
4675개의 도큐먼트에서 3개의 도큐먼트가 products.product_name 필드에 Pants라는 용어를 포함하고 있다는 의미입니다.  

#### TF 계산
용어 빈도(TF)는 특정 용어가 하나의 도큐먼트에 얼마나 많이 등장했는지를 의미하는 지표입니다.  
일반적으로 특정 용어가 도큐먼트에서 많이 반복되었다면 그 용어는 도큐먼트의 주제와 연관되어 있을 확률이 높습니다.  
하나의 도큐먼트에서 특정 용어가 많이 나오면 중요한 용어로 인식하고 가중치를 높입니다.  
이 또한 explain 옵션으로 검색했을 때 식을 알려줍니다.  
```json
{
  "value" : 0.52217203,
  "description" : "tf, computed as freq / (freq + k1 * (1 - b + b * dl / avgdl)) from:",
  "details" : [
    {
      "value" : 1.0,
      "description" : "freq, occurrences of term within document",
      "details" : [ ]
    },
    {
      "value" : 1.2,
      "description" : "k1, term saturation parameter",
      "details" : [ ]
    },
    {
      "value" : 0.75,
      "description" : "b, length normalization parameter",
      "details" : [ ]
    },
    {
      "value" : 5.0,
      "description" : "dl, length of field",
      "details" : [ ]
    },
    {
      "value" : 7.3161497,
      "description" : "avgdl, average length of field",
      "details" : [ ]
    }
  ]
}
```
freq는 도큐먼트 내에서 용어가 나온 횟수를 말합니다.  
k1과 b는 알고리즘을 정규화하기 위한 가중치로 ES가 취하는 디폴트 상수입니다.  
dl은 필드 길이, avgdl은 전체 도큐먼트에서 평균 필드 길이로, dl이 작고 avgdl이 클수록 TF값이 커집니다.  
이는 짧은 글에서 찾고자 하는 용어가 포함될수록 가중치가 높다는 의미입니다.  
예를 들어보겠습니다. 해당 인덱스에서 가장 높은 score를 받은 product_name은 다음과 같습니다.
```json
// 도큐먼트에서 product_name이 배열로 되어있습니다. 그래서 한 도규먼트에 product_name이 여러개 나옵니다.
"product_name" : "Boots - tan"
"product_name" : "Casual Cuffed Pants"

// 스탠다드 분석기 사용기
[Boots, tan, Casual, Cuffed, Patns] 

// dl = 5가 되고, avgdl은 모든 도큐먼트(4675 개)의 평균 토큰 수 입니다.
```
즉, 해당 도규먼트는 다른 도큐먼트에 비해 길이가 짧으면서 검색어를 포함하고 있기 때문에 스코어 수치상 좋은 점수를 받을 수 있었던 것입니다.  
최종적으로는 TF와 IDF를 이용해 score가 계산됩니다.  

### 쿼리
ES는 검색을 위해 쿼리를 지원하는데, 크게 리프 쿼리와 복합 쿼리로 나눌 수 있습니다.  
+ 리프 쿼리 : 특정 필드에서 용어를 찾는 쿼리로, 매치(match), 용어(term), 범위(range) 쿼리 등이 있습니다.  
+ 복합 쿼리 : 쿼리를 조합해서 사용하는 쿼리로, 대표적으로 논리(bool)쿼리가 있습니다.  

#### 전문 쿼리
전문 쿼리는 전문 검색을 하기 위해 사용되며 전문 검색을 할 필드는 인덱스 매핑 시 __텍스트 타입으로 매핑__ 해야 합니다.
```json
// 인덱스 생성
PUT qindex/_doc/1
{
  "contents": "I Love Elastic Stack" 
}
```
qindex를 생성하고 contents 필드를 갖는 도큐먼트를 인덱싱합니다.  
텍스트 타입으로 매핑된 문자열은 분석기에 의해 [i, love, elastic, stack]으로 토큰화됩니다.  

```json
// 검색
GET qindex/_search
{
  "query": {
    "match": {
      "contents" : "elastic world" 
    }
  }
}
```
elastic world도 분석기에 의해 [elastic, world]로 토큰화되어 앞서 토큰화된 도큐먼트 용어들이 매칭되어 스코어를 계산하고 검색하게 됩니다.  
전문 쿼리의 종류에는 match query, match phrase query, multi-match query, query string query 등이 있습니다.


##### match query
매치 쿼리는 가장 기본이 되는 전문 쿼리로, 전체 텍스트 중에서 특정 용어나 용어들을 검색할 때 사용합니다.  
매치 쿼리를 사용하기 위해서는 검색하고 싶은 필드를 알아야 합니다.  
키바나에서 제공하는 셈플 데이터를 사용할 것인데 필드가 어떤 것들이 있는지 확인하고 싶다면 GET kibana_sample_data_ecommerce/_mapping 으로 확인할 수 있습니다.  
```json
GET kibana_sample_data_ecommerce/_search
{
  "_source": ["customer_full_name"],
  "query": {
    "match": {
      "customer_full_name": "Mary"
    }
  }
}
```
_source는 원하는 파라미터만 보여달라는 요청입니다.  
전문 쿼리의 경우 검색어도 토큰화되기 때문에 검색어 Mary는 mary로 소문자로 변환되어 토큰화됩니다.  
분석기 종류에 따라 다르지만 일반적인 분석기를 사용했다면 대문자를 소문자로 변경하게 됩니다.  
```json
GET kibana_sample_data_ecommerce/_search
{
  "_source": ["customer_full_name"],
  "query": {
    "match": {
      "customer_full_name": "Mary bailey"
    }
  }
}
```
이번에는 복수 개의 용어를 검색했습니다.  
분석기에 의해 mary, bailey로 토큰화되는데 매치 쿼리에서 용어들 간의 공백은 or로 인식합니다.  
즉, mary나 bailey가 하나라도 포함된 도큐먼트가 있다면 매칭합니다.  
```json
GET kibana_sample_data_ecommerce/_search
{
  "_source": ["customer_full_name"],
  "query": {
    "match": {
      "customer_full_name": {
        "query": "mary bailey",
        "operator": "and"
      }
    }
  }
}
```
이번에는 operator 파라미터를 변경해서 mary와 bailey가 모두 포함된 도큐먼트를 찾았습니다.  
operator 파라미터는 기본값이 or이기 때문에 명시적으로 지정하지 않으면 앞선 방식처럼 or로 동작합니다.  
mary나 bailey의 순서를 바꿔도 결국 토큰화되기 때문에 순서는 상관 없습니다.  

##### match phrase
매치 프레이즈 쿼리는 전문 쿼리의 한 종류로, 구를 검색할 때 사용합니다.  
구는 동사가 아닌 2개 이상의 단어가 연결되어 만들어지는 단어입니다.  
예를 들면, '동네 친구들' 같이 여러 단어가 모여서 뜻을 이루는 단어를 의미하고 순서도 중요합니다.  
```json
GET kibana_sample_data_ecommerce/_search
{
  "_source": ["customer_full_name"],
  "query": {
    "match_phrase": {
      "customer_full_name": "mary bailey"
    }
  }
}
```
mary bailey가 mary, bailey로 토큰화되는 것까지는 match query와 동일하지만 match phrase 쿼리는 용어의 순서까지 맞아야 합니다.  
match phrase는 검색 시 많은 리소스를 요구하기 때문에 자주 사용하는 것은 좋지 않습니다.

##### multi-match query
앞선 쿼리들은 전부 필드를 기준으로 검색했습니다. 하지만 용어나 구절이 정확히 어떤 필드에 있는지 모르는 경우가 있습니다.  
예를 들면, 구글에 검색할 때 '엘라스틱서치'를 검색한다면 어떤 필드에 있는지 정확히 알 수 없습니다.  
이럴 경우 하나의 필드가 아닌 여러 개의 필드에서 검색을 해야 합니다.  
여러 개의 필드에서 검색하기 위한 쿼리가 multi-match query이고 전문 검색에 해당합니다.  
```json
GET kibana_sample_data_ecommerce/_search
{
  "_source": ["customer_first_name","customer_last_name","customer_full_name"],
  "query": {
    "multi_match": {
      "query": "mary", 
      "fields": [
        "customer_full_name",
        "customer_first_name",
        "customer_last_name"
        ]
    }
  },
  "explain": true
}
```
multi-match query는 1개 이상의 필드에 쿼리를 요청할 수 있습니다.  
3개의 필드에 대해 mary라는 용어로 매치 쿼리를 하고 3개의 필드에서 개별 스코어를 구한 다음에 그중 가장 큰 값을 대표 스코어로 구합니다.  
대표 스코어를 선택 방식은 사용자가 결정할 수 있지만, 특별한 설정을 하지 않으면 기본으로 가장 큰 스코어를 대표 스코어로 사용합니다.  
explain 파라미터를 참으로 설정하면 개별 필드의 스코어가 어떻게 계산되었고 대표 스코어가 어떻게 선정되는지도 확인할 수 있습니다.  

```json
GET kibana_sample_data_ecommerce/_search
{
  "_source": ["customer_first_name","customer_last_name","customer_full_name"],
  "query": {
    "multi_match": {
      "query": "mary", 
      "fields": "customer_*_name"
        
    }
  },
  "explain": true
}
```
검색하려는 필드가 너무 많을 때는 필드명에 * 같은 와일드카드를 사용해 이름이 유사한 복수 필드를 사용할 수도 있습니다.  
<br>  

__필드에 가중치 두기__  
여러 개의 필드 중 특정 필드에 가중치를 두는 방법을 부스팅 기법이라고 하는데 multi-match query에서 자주 사용됩니다.  
```json
GET kibana_sample_data_ecommerce/_search
{
  "_source": ["customer_first_name","customer_last_name","customer_full_name"],
  "query": {
    "multi_match": {
      "query": "mary", 
      "fields": [
        "customer_full_name^2",
        "customer_first_name",
        "customer_last_name"
        ]
    }
  },
  "explain": true
}
```
가중치를 부여하고자 하는 특정 필드에 ^기호와 숫자를 적어주면 됩니다.  
full_name는 얻은 스코어가 2배가 높게 책정됩니다.  


#### 용어 수준 쿼리
용어 수준 쿼리는 정확히 일치하는 용어를 찾기 위해 사용되며, 인덱스 매핑 시 필드를 __키워드 타입으로 매핑__ 해야 합니다.
```json
// 인덱스 생성
PUT qindex/_doc/1
{
  "category": "tech" 
}
```
qindex를 생성하고 category 필드를 갖는 도큐먼트를 인덱싱합니다.  
category 필드는 키워드 타입으로 매핑하는데, 키워드 타입은 인덱싱 과정에서 분석기를 사용하지 않습니다.  
```json
GET qindex/_search
{
  "query": {
    "term" : {
      "category": "Tech"
    }
  } 
}
```
검색은 용어(term)쿼리를 사용하는데 Tech는 분석기를 거치지 않고 그대로 사용되어 대소문자가 일치하지 않아 일치하는 데이터가 없습니다.  
용어 수준 쿼리는 전문 쿼리와 달리 정확한 용어를 검색할 때 사용합니다.  
일반적으로 숫자, 날짜, 범주형 데이터를 정확하게 검색할 때 사용되며 관계형 데이터베이스의 where 절과 비슷한 역할을 합니다.  
용어 수준 쿼리에는 용어 쿼리(term query), 용어들 쿼리(terms query), 퍼지 쿼리(fuzzy query)등이 있습니다.  


##### term query
term query는 용어 수준 쿼리의 대표적인 쿼리입니다.  
사용 방법은 match query와 비슷하지만 match query는 전문 쿼리에 속하기 때문에 검색어인 mary bailey가 분석기에 의해 토큰화되어 mary나 bailey가 있는 경우 매칭이 됩니다.  
반면에 term query는 용어 수준 쿼리에 속하기 때문에 검색어인 mary bailey가 분석기에 의해 토큰화되지 않습니다.  
즉, mary bailey라고 정확한 용어가 있는 경우에만 매칭되며 대소문자도 일치해야 합니다.  
```json
GET kibana_sample_data_ecommerce/_search
{
  "_source": ["customer_full_name"],
  "query": {
    "term": {
      "customer_full_name": "Mary Bailey"
    }
  }
}
```
요청을 보내면 일치하는 데이터가 없습니다.  
이유는 customer_full_name 필드는 텍스트로 매핑되어 있어 mary,bailey 2개로 소문자로 변경되고 나뉘어 토큰화되어 있는데 term query는 Mary Bailey를 찾기 때문입니다.  
즉, 토큰화 되어 [mary,bailey] 로 저장되있기에 mary로 검색하거나 bailey로 검색해야 찾을 수 있습니다.
```json
GET kibana_sample_data_ecommerce/_search
{
  "_source": ["customer_full_name"],
  "query": {
    "term": {
      "customer_full_name": "Mary"
    }
  }
}
```
mary, bailey로 토큰화되어 있기 때문에 Mary는 대소문자 불일치로 역시 데이터가 없습니다.  
이제 알맞게 사용해 봅시다.  
강제하진 않지만 term query는 키워드 타입으로 매핑된 필드에서 사용해야 합니다.  
```json
// 데이터가 어떻게 맵핑되어 있는지 확인해보기
GET kibana_sample_data_ecommerce/_mapping
"customer_full_name" : {
  "type" : "text",
  "fields" : {
    "keyword" : {
      "type" : "keyword",
      "ignore_above" : 256
    }
  }
}
```
kibana sample에서는 customer_full_name 필드를 텍스트와 키워드 타입을 갖는 멀티 필드로 지정되어있기 때문에 적합하게 사용할 수 있을 것 같습니다.  
customer_full_name 필드는 텍스트 타입, customer_full_name.keyword 필드는 키워드 타입으로 지정되어있는 것을 확인할 수 있습니다.

```json
GET kibana_sample_data_ecommerce/_search
{
  "_source": ["customer_full_name"],
  "query": {
    "term": {
      "customer_full_name.keyword": "Mary Bailey"
    }
  }
}
```
텍스트 맵핑과 키워드 맵핑이 둘다 되어있는 것을 확인했기 때문에 .keyword를 사용하여 키워드 맵핑에 검색을 수행합니다.  
이제 원하는 결과값을 얻었습니다.  
<br>

__중간 정리__  
term query를 포함한 용어 수준 쿼리는 키워드 타입으로 매핑된 필드를 대상으로 주로 키워드 검색이나 범주형 데이터를 검색하는 용도로 사용합니다.  
match query를 포함한 전문 쿼리는 텍스트 타입으로 매핑된 필드를 대상으로 전문 검색에 사용합니다.  
이 두 용도를 구분해서 잘 사용해야 한다는 것을 명심합시다.  

##### terms query
terms query는 용어 수준 쿼리로 여러 용어들을 검색해줍니다.  
키워드 타입으로 매핑된 필드에 사용해야 하며, 분석기를 거치지 않기 때문에 대소문자도 신경써야 합니다.
```json
GET kibana_sample_data_ecommerce/_search
{
  "_source": ["day_of_week"],
  "query": {
    "terms": {
      "day_of_week": ["Monday","Sunday"]
    }
  }
}
```
day_of_week필드는 요일을 표현하는 범주형 필드로, 키워드 타입으로만 매핑되어있기 때문에 .keyword 없이 바로 사용가능합니다.  


#### 범위 쿼리
특저 날짜나 숫자의 범위를 지정해 범위 안에 포함된 데이터들을 검색할 때 사용합니다.  
날짜/숫자/IP 타입의 데이터는 범위 쿼리가 가능하지만 문자형, 키워드 타입의 데이터에는 범위 쿼리를 사용할 수 없습니다.  
```json
GET kibana_sample_data_flights/_search
{
  "query": {
    "range": {
      "timestamp": {
        "gte": "2022-02-26",
        "lt": "2022-02-27"
      }
    }
  }
}
```
주의할 것이 키바나를 띄워 샘플 데이터를 넣은 날짜를 기준으로 샘플 데이터 시간이 설정되기 때문에 날짜를 잘 조절해줘야 합니다.  
날짜/시간 포맷과 도큐먼트에 저장된 포맷이 맞아야 검색이 가능하므로 이점도 주의해야 합니다.  
ES는 범위 쿼리에서 네 가지 파라미터를 제공합니다.

파라미터|설명
---|---
gte|같거나 큰값
gt|큰값
lte|같거나 작은값
lt|작은값


보통 날짜 검색은 현재를 기준으로 하는 경우가 많은데 더 편리하게 검색할 수 있는 표현식이 존재합니다.
```json
GET kibana_sample_data_flights/_search
{
  "query": {
    "range": {
      "timestamp": {
        "gte": "now-1M"
      }
    }
  }
}
```
현재 시각을 기준으로 한 달 전까지의 모든 데이터를 가져옵니다.  

__날짜/시간 관련 범위 표현식__

표현식|설명
---|---
now|현재 시각(2022-02-27T15:23:33)
now+1d|현재 시각 + 1일
now+1h+30m+10s|현재 시각 + 1시간 30분 10초
2021-01-21\|\|+1M|2021-01-21 + 1달


__날짜/시간 단위 표기법__  

시간 단위|의미|시간 단위|의미|시간 단위|의미
---|---|---|---|---|---
y|연|M|월|w|주
d|일|H,h|시|m|분
s|초|

m과 M이 헷갈리는데 시분초 단위는 모두 소문자로 보면 됩니다.  

#### 범위 데이터 타입
ES는 데이터 타입 중 범위 데이터 타입이 존재합니다.  
integer_range, float_range, long_ragen, double_range, date_range, ip_range 를 제공합니다.  
테스트 샘플에는 존재하지 않기 때문에 따로 만들어서 테스트 해 봅시다.
```json
// 인덱스 생성
PUT range_test_index
{
  "mappings": {
    "properties": {
      "test_date": {
        "type": "date_range"
      }
    }
  }
}

// 도큐먼트 인덱싱
PUT range_test_index/_doc/1
{
  "test_date": {
    "gte" : "2021-01-21",
    "lt": "2021-01-25"
  }
}

// 검색
GET range_test_index/_search
{
  "query": {
    "range": {
      "test_date": {
        "gte": "2021-01-21",
        "lte": "2021-01-28",
        "relation": "within"
      }
    }
  }
}
```
range_test_index 인덱스의 test_date 필드에 해당 날짜 사이에 속한 데이터가 있는지 검색합니다.  
relation에 들어갈 수 있는 값은 다음과 같습니다.
+ intersects(기본값) : 쿼리 범위 값이 도큐먼트의 범위 데이터를 일부라도 포함하기만 하면 매칭
+ contains : 도큐먼트 범위 데이터가 쿼리 범위 값을 모두 포함해야만 매칭
+ within : 도큐먼트의 범위 데이터가 쿼리 범위 값 내에 전부 속해야 매칭


#### 논리 쿼리
논리 쿼리는 복합 쿼리로 앞선 쿼리들을 조합해서 사용할 수 있습니다.  
논리 쿼리는 4개의 타입을 지원합니다.

타입|설명
---|---
must|쿼리를 실행하여 참인 도큐먼트를 찾는다.<br>복수의 쿼리를 실행하면 AND연산을 한다.
must_not|쿼리를 실행하여 거짓인 도큐먼트를 찾는다.<br>다른 타입과 같이 사용할 경우 도큐먼트에서 제외한다.
should|단독으로 사용 시 쿼리를 실행하여 참인 도큐먼트를 찾는다.<br>복수의 쿼리를 실행하면 OR연산을 한다.<br>다른 타입과 같이 사용할 경우 스코어에만 활용된다.
filter|쿼리를 실행하여 예/아니요 형식의 필터 컨텍스트를 수행한다.

##### must
```json
GET kibana_sample_data_ecommerce/_search
{
  "_source": ["day_of_week","customer_full_name"],
  "query": {
    "bool": {
      "must": [
        {"term": {
          "day_of_week": "Sunday" 
        }},
        {"match": {
          "customer_full_name": "mary"
        }}
      ]
    }
  }
}
```
must 타입에 복수 개의 쿼리를 실행하여 AND 효과를 얻습니다.  
복수 개의 쿼리를 사용할 때는 []를 사용합니다.

##### must_not
```json
GET kibana_sample_data_ecommerce/_search
{
  "_source": ["customer_full_name"],
  "query": {
    "bool": {
      "must": [
        {"match": {
          "customer_first_name": "mary"
        }}
      ],
      "must_not": [
        {"term": {
          "customer_last_name": "bailey"
        }}
      ]
    }
  }
}
```
mary가 들어간 도큐먼트를 찾고 그 중에서 last_name이 bailey인 것을 제외합니다.  

##### should
```json
GET kibana_sample_data_ecommerce/_search
{
  "_source": ["customer_full_name","day_of_week"],
  "query": {
    "bool": {
     "should": [
       {"term": {
         "day_of_week": {
           // 앞서 했던 것 처럼 value 없이 해도 되는데 키바나에서 자동완성으로 value를 넣어줘서 이렇게도 된다고 해봤습니다.
           "value": "Sunday"
         }
       }},
       {"match": {
         "customer_full_name": "mary"
       }}
     ]
    }
  }
}
```
should 타입에 복수 개의 쿼리를 사용하면 OR의 효과가 있습니다.  
```json
GET kibana_sample_data_ecommerce/_search
{
  "_source": ["customer_full_name","day_of_week"],
  "query": {
    "bool": {
      "must": [
        {"match": {
          "customer_full_name": "mary"
        }}
      ],
      "should": [
        {"term": {
          "day_of_week": {
            "value": "Monday"
          }
        }}
      ]     
    }
  }
}
```
쿼리를 보면 must 타입과 should 타입 두 개가 사용되었습니다.  
이 경우에 should 타입은 검색에 영향을 주지 않고 must 타입만 검색 결과에 영향을 줍니다.  
그리고 should는 검색 결과에서 day_of_week 가 Monday인 도큐먼트의 우선순위를 높여주는 역할만 수행합니다.  

##### filter
must와 같은 동작을 하지만 필터 컨텍스트로 동작하기 때문에 유사도 스코어에 영향을 주지 않습니다.  
예/아니요 결과만을 제공합니다.
```json
GET kibana_sample_data_ecommerce/_search
{
  "_source": ["products.base_price"],
  "query": {
    "bool": {
      "filter": [
        {"range": {
          "products.base_price": {
            "gte": 30,
            "lte": 60
          }
        }}
      ]     
    }
  }
}
```
필터링 된 결과만 나옵니다.
```json
GET kibana_sample_data_ecommerce/_search
{
  "_source": ["day_of_week","customer_full_name"],
  "query": {
    "bool": {
      "filter": [
        {"term": {
          "day_of_week": "Sunday"
        }}
      ],
      "must": [
        {"match": {
          "customer_full_name": "mary"
        }}
      ]
    }
  }
}
```
must로 나온 결과에서 filter로 필터링된 결과만 나오게 됩니다.  

#### 패턴 검색
패턴 검색에는 와일드카드 쿼리와 정규식 쿼리가 있습니다.

##### 와일드카드 쿼리
+ \* : 공백까지 포함하여 글자 수에 상관없이 모든 문자를 매칭
+ ? : 오직 한 문자만 매칭

용어 맨 앞에 와일드카드를 사용할 경우 속도가 매우 느려지기 때문에 검색어 앞에는 사용하지 않도록 주의해야 합니다.  

```json
GET kibana_sample_data_ecommerce/_search
{
  "_source": ["customer_full_name"],
  "query": {
    "wildcard": {
      "customer_full_name.keyword": {
        "value": "M?r*"
      }
    }
  }
}
```

##### 정규식 쿼리
+ 점(.) : 하나의 문자를 의미하고 어떤 문자가 와도 상관없이 매칭
+ \+ : 기호 앞의 문자가 한 번 이상 반복되면 매칭
+ \* : 기호 앞의 문자가 0번 혹은 여러 번 반복되면 매칭(한번도 안 나와도 매칭된다는 의미)
+ ? : 기호 앞의 문자가 0번 혹은 한 번 나타나면 매칭
+ () : 그룹핑하여 반복되는 문자들을 매핑
  - (aabb)+ 는 aabb가 한 번 이상 나오면 매칭
+ [] : 문자를 클래스화하여 특정 범위의 문자들을 매칭
  - [^a]abb 맨 첫문자가 a가 아닌 다른 문자면 매칭
  - [a-z]aabb 맨 첫문자가 a부터z 사이의 문자면 매칭

```json
GET kibana_sample_data_ecommerce/_search
{
  "query": {
    "regexp": {
      "customer_first_name.keyword": "Mar."
    }
  }
}
```

## 엘라스틱서치 집계
집계는 데이터를 그룹핑하고 통계값을 얻는 기능으로 SQL의 GROUP BY와 통계 함수를 포함하는 개념입니다.  

### 집계의 요청 - 응답 형대
집계를 위한 특별한 API가 제공되는 것은 아니며, search API의 요청 본문에 aggs 파라미터를 이용하면 쿼리 결과에 대한 집계를 생성할 수 있습니다.  

```json
GET 인덱스/_search
{
  "aggs": {
    "my_aggs": {
      "agg_type": {
        ...
      }
    }
  }
}
```
aggs는 집계 요청을 하겠다는 의미이고, my_aggs는 사용자가 지정하는 집계 이름입니다.  
ES는 크게 메트릭 집계와 버킷 집계라는 두 가지 타입의 집계가 있습니다.  
메트릭 집계는 통계나 계산에 사용되고 버킷 집계는 도큐먼트를 그룹핑하는 데 사용됩니다.  
```json
{
  ...
  "hits": {
    "total": {
      ...
    }
  },
  "aggregations": {
    "my_aggs":{
      "value":
    }
  }
}
```
집계 요청에 대한 응답값은 위와 같이 나옵니다.  
aggregations는 이 응답 메시지가 집계 요청에 대한 결과임을 알려주고 my_aggs는 집계 이름으로 사용자가 지정한 이름이고 value는 집계 결과입니다.  

### 메트릭 집계
메트릭 집계는 필드의 최소, 최대, 합계, 평균, 중간값 같은 통계 결과를 보여줍니다.  

+ avg : 평균값 계산
+ min : 최솟값 계산
+ max : 최댓값 계산
+ sum : 총합 계산
+ percentiles : 백분위값 계산
+ stats : 필드의 min, max, sum, avg, count(도큐먼트 개수)를 한 번에 확인
+ cardinality : 필드의 유니크한 값 개수
+ geo-centroid : 필드 내부의 위치 정보의 중심점 계산

#### 평균값 계산
```json
GET kibana_sample_data_ecommerce/_search
{
 "size": 0,
 "aggs": {
   "stats_aggs": {
     "avg": {
       "field": "products.base_price"
     }
   }
 }
}
```
집계 이름은 stats_aggs로 했는데 아무거나 해도 무관하며, 평균 집계를 사용하기 위해서는 필드 타입이 정수나 실수 타입이어야 합니다.  
size를 0으로 주면 집계에 사용한 도큐먼트를 결과에 포함하지 않음으로써 비용을 절약할 수 있습니다.  
```json
GET kibana_sample_data_ecommerce/_search
{
 "size": 0,
 "aggs": {
   "stats_aggs": {
     "percentiles": {
       "field": "products.base_price",
       "percents": [
         25,
         50
       ]
     }
   }
 }
}
```
percenties를 사용해서 필드의 백분위값을 구하는 요청입니다.  
25퍼센트와 50퍼센트에 해당하는 값을 요청한 것입니다.

#### 유니크한 값 개수 확인하기
```json
GET kibana_sample_data_ecommerce/_search
{
 "size": 0,
 "aggs": {
   "cardi_aggs": {
     "cardinality": {
       "field": "day_of_week",
       "precision_threshold": 100
     }
   }
 }
}
```
day_of_week의 유니크한 데이터 개수를 요청합니다. 
threshold는 정확도 수치로 클수록 정확도가 올라가는 대신 시스템 리소스를 많이 소모하게 됩니다.  
결과값으로 7이 나왔는데 threshold를 7보다 작게주면 결과값이 엉뚱한 수치가 나옵니다.  
기본값은 3000이며 최대 40000까지 값을 설정할 수 있습니다.  

```json
GET kibana_sample_data_ecommerce/_search
{
 "size": 0,
 "aggs": {
   "cardi_aggs": {
     "terms": {
       "field": "day_of_week"
     }
   }
 }
}
```
terms를 사용하면 day_of_week 필드의 유니크한 값들과 해당 유니크한 값의 도큐먼트 개수도 보여줍니다.

### 검색 결과 내에서의 집계
```json
GET kibana_sample_data_ecommerce/_search
{
 "size": 0,
  "query": {"term": {
    "day_of_week": {
      "value": "Monday"
    }
  }}, 
 "aggs": {
   "query_aggs": {
     "sum": {
       "field": "products.base_price"
     }
   }
 }
}
```
day_of_week 필드값이 Monday인 도큐먼트만 일차적으로 골라내고 이 도큐먼트만을 가지고 query_aggs라는 이름으로 집계를 수행합니다.  

### 버킷 집계
메트릭 집계가 특정 필드를 기준으로 통계값을 계산하는 목적이라면, 버킷 집계는 특정 기준에 맞춰서 도큐먼트를 그룹핑하는 역할을 합니다.  
여기서 버킷은 도큐먼트가 분할되는 단위로 나뉜 각 그룹을 의미합니다.  
예를 들면, 모든 데이터에서 월요일 도큐먼트, 화요일 도큐먼트... 이렇게 그룹핑하는 것입니다.  
보통 특정 목적으로 도큐먼트를 그룹핑하고 싶을 때 버킷 집계를 사용하고 버킷으로 도큐먼트를 구분한 후에 메트릭 집계와 연계해 분석하여 사용합니다.  

__버킷 집계 종류__  

버킷 집계|설명
histogram|숫자 타입 필드를 일정 간격으로 분류
date_histogram|날짜/시간 타입 필드를 일정 날짜/시간 간격으로 분류
range|숫자 타입 필드를 사용자가 지정하는 범위 간격으로 분류
date_range|날짜/시간 타입 필드를 사용자가 지정하는 날짜/시간 간격으로 분류
terms|필드에 많이 나타나는 용어들을 기준으로 분류
significant_terms|terms 버킷과 유사하나, 모든 값을 대상으로 하지 않고 인덱스 내 전체 문서 대비 현재 검색 조건에서 통계적으로 유의미한 값들을 기준으로 뷴라
filters|각 그룹에 포함시킬 문서의 조건을 직접 지정하고 이때 조건은 일반적으로 검색에 사용되는 쿼리와 동일



#### 히스토그램 집계
```json
GET kibana_sample_data_ecommerce/_search
{
 "size": 0,
 "aggs": {
   "histogram_aggs": {
     "histogram": {
       "field": "products.base_price",
       "interval": 100
     }
   }
 }
}
```
히스토그램 집계할 대상을 정했고 간격도 정해주었습니다.  
결과값이 0.0, 100.0, ... 처럼 나오는데 옆에 doc_count로 해당 범위 내에 몇 개의 도큐먼트가 있는지 보여줍니다.  

#### 범위 집계
히스토그램 집계는 각 버킷의 범위를 동일하게 지정할 수밖에 없다는 단점이 있습니다.  
범위 집계는 각 버킷의 범위를 사용자가 지정할 수 있습니다.
```json
GET kibana_sample_data_ecommerce/_search
{
 "size": 0,
 "aggs": {
   "range_aggs": {
     "range": {
       "field": "products.base_price",
       "ranges": [
         {"from": 0, "to": 50},
         {"from": 50, "to": 100},
         {"from": 100, "to": 200}
       ]
     }
   }
 }
}
```

#### 용어 집계
용어 집계는 필드의 유니크한 값을 기준으로 버킷을 나눌 때 사용합니다.
```json
GET kibana_sample_data_ecommerce/_search
{
 "size": 0,
 "aggs": {
   "term_aggs": {
     "terms": {
       "field": "day_of_week",
       "size": 6
     }
   }
 }
}
```
day_of_week 필드의 값을 기준으로 도큐먼트 수가 많은 상위 6개의 버킷을 요청하는 쿼리입니다.  
size는 기본값이 10으로 적용됩니다.  
용어 집계에서는 다른 집계에서 없는 결과값이 있습니다.
+ doc_count_error_upper_bound : 버킷이 잠재적으로 카운트하지 못할 도큐먼트의 수
+ sum_other_doc_count : 버킷에는 있지만 size 때문에 보이지 않은 도큐먼트의 수

<br>

__용어 집계가 정확하지 않은 이유__  
용어 집계의 부정확도를 표시하는 이유는 분산 시스템의 집계 과정에서 발생하는 잠재적인 오류 가능성 때문입니다.  
ES는 샤드에 도큐먼트를 저장하고 이를 분산하는데 size 설정값과 샤드 개수 등에 의해 집계 오류가 발생할 수 있습니다.  
이유는 집계가 모든 도큐먼트를 가져와 한 번에 집계하는 것이 아니라 분산되어 있는 개별 노드단에서 먼저 집계를 하고 그 결과를 취합해 다시 집계를 하기 때문입니다.  
예를 들어 요일 데이터가 샤드1, 샤드2에 분산되어 저장되어있다고 해봅시다. size가 6이라고 했을 때 샤드 1에서는 일요일을 빼버리고 샤드2에서는 토요일을 빼버려 집계하고 이걸 취합해서 다시 집계하게 되면 정확한 값과 다른 결과가 나오게 됩니다.  
<br>

__용어 집계 정확성 높이기__  
```json
GET kibana_sample_data_ecommerce/_search
{
 "size": 0,
 "aggs": {
   "term_aggs": {
     "terms": {
       "field": "day_of_week",
       "size": 6,
       "show_term_doc_count_error": true
     }
   }
 }
}
```
show_term_doc_count_error 옵션을 주면 결과에 각 버킷마다 doc_count_error_upper_bound를 확인할 수 있습니다.  
만약 확인 결과 이상값이 나올 경우에는 이를 해결하기 위해 샤드 크기 파라미터를 늘릴 필요가 있습니다.  
용어 집계 시 shard_size 파라미터를 이용해 샤드 크기를 늘릴 수 있는데 샤드 크기는 용어 집계 과정에서 개별 샤드에서 집계를 위해 처리하는 개수를 의미합니다.  
샤드 크기를 크게 하면 정확도가 올라가는 대신 리소스 사용량이 올라가 성능이 떨어질 수 있다는 것도 기억해야 합니다.  

```json
GET kibana_sample_data_ecommerce/_search
{
 "size": 0,
 "aggs": {
   "term_aggs": {
     "terms": {
       "field": "day_of_week",
       "size": 6,
       "shard_size": 100
     }
   }
 }
}
```

### 집계 조합

#### 버킷 집계와 메트릭 집계
가장 기본적인 조합은 버킷 집계로 도큐먼트를 그룹핑한 후에 각 버킷 집계별 메트릭 집계를 사용하는 것입니다.  
```json
GET kibana_sample_data_ecommerce/_search
{
 "size": 0,
 "aggs": {
   "term_aggs": {
     "terms": {
       "field": "day_of_week",
       "size": 5
       },
       "aggs": {
         "avg_aggs": {
           "avg": {
             "field": "products.base_price"
           }
         }
       }
    }
  }
}
```
day_of_week로 버킷을 나누고 상위 버킷 5개만 가져와서 각 버킷 내부의 base_price필드의 평균값을 구합니다.  
```json
GET kibana_sample_data_ecommerce/_search
{
 "size": 0,
 "aggs": {
   "term_aggs": {
     "terms": {
       "field": "day_of_week",
       "size": 5
       },
       "aggs": {
         "avg_aggs": {
           "avg": {
             "field": "products.base_price"
           }
         },
         "sum_aggs":{
           "sum": {
             "field": "products.base_price"
           }
         }
       }
    }
  }
}
```
이렇게 복수 개의 메트릭 집계를 수행할 수도 있습니다.  

#### 서브 버킷 집계
서브 버킷은 버킷 안에서 다시 버킷 집계를 요청하는 집계입니다.  
```json
GET kibana_sample_data_ecommerce/_search
{
 "size": 0,
 "aggs": {
   "histogram_aggs":{
     "histogram": {
       "field": "products.base_price",
       "interval": 100
     },
     "aggs": {
       "term_aggs": {
         "terms": {
           "field": "day_of_week",
           "size": 2
         }
       }
     }
   }    
  }
}
```
히스토그램 집계를 사용하여 base_price 필드를 100단위로 구분하여 나눠진 버킷에서 다시 day_of_week 필드로 유니크한 값 기준으로 상위 2개의 버킷을 만듭니다.  

### 파이프라인 집계
파이프라인 집계는 이전 결과를 다음 단계에서 이용하는 파이프라인 개념을 차용합니다.  
이전 집계로 만들어진 결과를 입력으로 삼아 다시 집계하는 방식입니다.  
이 과정에는 부모 집계와 형제 집계라는 두 가지 유형이 있습니다.  
두 집계의 큰 차이점은 집계가 작성되는 위치입니다.  
부모 집계는 기존 집계 내부에서 작성하고 형제 집계는 기존 집계 외부에서 새로 작성합니다.  
<br>

__파이프라인 집계 종류__  
+ 부모 집계
  - derivative : 기존 집계의 미분
  - cumulative_sum : 기존 집계의 누적 합
+ 형제 집계
  - min_bucket : 기존 집계 중 최소값
  - max_bucket : 기존 집계 중 최댓값
  - avg_bucket : 기존 집계의 평균값
  - sum_bucket : 기존 집계의 총합
  - stat_bucket : 기존 집계의 min, max, sum, count, avg
  - percentile_bucket : 기존 집계의 백분윗값
  - moving_avg : 기존 집계의 이동 평균

#### 부모 집계
부모 집계는 이전 집계 내부에서 실행되어 결과값도 기존 집계 내부에서 나타납니다.
```json
GET kibana_sample_data_ecommerce/_search
{
 "size": 0,
 "aggs": {
   "histogram_aggs":{
     "histogram": {
       "field": "products.base_price",
       "interval": 100
     },
     "aggs": {
       "sum_aggs": {
         "sum": {
           "field": "taxful_total_price"
         }
       },
       "cum_sum":{
         "cumulative_sum": {
           "buckets_path": "sum_aggs"
         }
       }
     }
   }
    
  }
}
```
누적합을 구하는 부모 집계입니다.  
부모 집계를 사용하기 위해서는 입력으로 다른 집계가 필요한데, 여기서는 히스토그램 집계와 합계 집계를 사용합니다.  
먼저 base_price를 100 기준으로 버킷을 나누고 각 버킷에서 total_price 합을 구하는 집계를 수행합니다.  
그리고 결과를 cum_sum의 집계의 입력으로 받습니다.  
파이프라인 집계는 반드시 버킷 경로(buckets_path)를 입력해야 하는데 입력으로 사용할 집계 이름을 적으면 됩니다.  
결과값을 확인해보면 개별 버킷 내부에서 cum_sum이 보이고 값이 점점 누적되는 것을 확인할 수 있습니다.  

#### 형제 집계
형제 집계는 기존 집계 내부가 아닌 외부에서 기존 집계를 이용해 집계 작업을 합니다.  
```json
GET kibana_sample_data_ecommerce/_search
{
 "size": 0,
 "aggs": {
   "term_aggs":{
     "terms": {
       "field": "day_of_week",
       "size": 2
     },
     "aggs": {
       "sum_aggs": {
         "sum": {
           "field": "taxful_total_price"
         }
       }
     }
   },
   "sum_total_price":{
     "sum_bucket": {
       "buckets_path": "term_aggs>sum_aggs"
     }
   }    
  }
}
```
term_aggs는 용어 집계로 day_of_week 필드를 기준으로 요일별 버킷을 나누고 상위 2개의 버킷을 생성합니다.  
sum_aggs에서 base_price 필드의 총합을 구합니다.  
sum_bucket 형제 집계를 이용해 기존 버킷별 합을 구한 집계를 다시 합칩니다.  
버킷 경로를 입력할 때 >기호가 들어가는데 하위 집계 경로를 나타낼 때 사용합니다.  
즉, term_aggs 다음에 sum_aggs를 사용한 것을 입력으로 받는다는 것을 의미합니다.  
결과를 보면 부모 집계처럼 안쪽에 들어있지 않고 외부에서 결과를 보여주며 모든 버킷에서 나온 값이 합산된 것을 확인할 수 있습니다.  





<Br><Br>

__참고__  
<a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl.html" target="_blank"> elastic search 공식 문서</a>  


