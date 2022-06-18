# Spring Data Elasticsearch 연동 및 테스트 작성하기

## Docker-compose로 Elasticsearch 설치
```sh
# compose 파일 생성
vi es.yml
```
```yml
version: '3.7'
services:
  es:
    image: docker.elastic.co/elasticsearch/elasticsearch:7.15.2
    container_name: es
    environment:
      - node.name=single-node
      - cluster.name=backtony
      - discovery.type=single-node
    ports:
      - 9200:9200
      - 9300:9300
    networks:
      - es-bridge

  kibana:
    container_name: kibana
    image: docker.elastic.co/kibana/kibana:7.15.2
    environment:
      SERVER_NAME: kibana
      # Elasticsearch 기본 호스트는 http://elasticsearch:9200 이다. 
      # 현재 docker-compose 파일에 Elasticsearch 서비스 명은 es로 설정되어있다.
      ELASTICSEARCH_HOSTS: http://es:9200
    ports:
      - 5601:5601
    # Elasticsearch Start Dependency
    depends_on:
      - es
    networks:
      - es-bridge

networks:
  es-bridge:
    driver: bridge
```
```sh
# 실행, 데몬으로 띄우려면 맨 뒤에 -d를 붙여준다.
# 기본 실행 도커파일은 docker-compose.yml인데 es.yml로 만들었으므로 지정해주기 위해서 -f 옵션을 사용
docker-compose -f es.yml up

# 죽이기
docker-compose -f es.yml down
```
9200번 포트는 HTTP 클라이언트와 통신에 사용되며, 9300번 포트는 노드들간 통신 시에 사용됩니다.  
Elastic Stack은 버전정보에 민감하고 버전이 굉장히 빠르게 업데이트되기 때문에 버전을 잘 맞춰야합니다.  
이에 관해서는 [문서](https://docs.spring.io/spring-data/elasticsearch/docs/current/reference/html/#new-features)에서 제공하는 버전 호환 유무를 확인해야 합니다.  
가장 최신 버전에서 7.15.2버전을 지원하므로 이에 맞춰 이미지를 선택해줬습니다.  
<Br>

간단하게 구성하고 컨테이너를 내리고 올릴 때 이전 데이터를 사용하지 않고 다시 올리기 위해 Volume매핑 등 많은 부분을 제거했습니다.  
좀 더 명확한 설치 방법을 찾고 싶으시다면 [docker-compose-github](https://github.com/deviantony/docker-elk) 혹은 [공식 문서](https://www.elastic.co/guide/en/elastic-stack-get-started/current/get-started-stack-docker.html)를 참고하세요.  


<br>

__cf) nori 형태소 분석기 추가해서 띄위기__  
이 경우에는 Dockerfile을 따로 만들어주고 es.yml에 이미지로 넣어줘야 합니다.  
es.yml과 같은 위치에서 시작해봅시다.
```sh
# dockerfile 생성
vi Dockerfile

# 작성
ARG ELK_VERSION
FROM docker.elastic.co/elasticsearch/elasticsearch:${ELK_VERSION}
RUN elasticsearch-plugin install analysis-nori
```
<br>

es.yml 파일 수정하기
```yml
version: '3.7'
services:
  es:
    build:    
      # 도커파일의 위치 알려주기
      context: .
      # 인자 넣어주기
      args:
        ELK_VERSION: 7.15.2
    container_name: es
    environment:
      - node.name=single-node
      - cluster.name=backtony
      - discovery.type=single-node
    ports:
      - 9200:9200
      - 9300:9300
    networks:
      - es-bridge

  kibana:
    container_name: kibana
    image: docker.elastic.co/kibana/kibana:7.15.2
    environment:
      SERVER_NAME: kibana
      # Elasticsearch 기본 호스트는 http://elasticsearch:9200 이다. 
      # 현재 docker-compose 파일에 Elasticsearch 서비스 명은 es로 설정되어있다.
      ELASTICSEARCH_HOSTS: http://es:9200
    ports:
      - 5601:5601
    # Elasticsearch Start Dependency
    depends_on:
      - es
    networks:
      - es-bridge

networks:
  es-bridge:
    driver: bridge
```
이렇게 작성해주고 실행은 똑같이 하면 됩니다.  
만약 nori가 잘 설치되었는지 궁금하다면 키바나 콘솔에서 아래와 같이 수행해서 결과가 잘 나오면 잘 설치된 것입니다.
```sh
POST _analyze
{
  "tokenizer": "nori_tokenizer",
  "text": "대한민국은 민주공화국이다."
}
```



## 코드 작성

__build.gradle__
```groovy
plugins {
    id 'org.springframework.boot' version '2.6.3'
    id 'io.spring.dependency-management' version '1.0.11.RELEASE'
    id 'java'
}

group = 'com.example'
version = '0.0.1-SNAPSHOT'
sourceCompatibility = '11'

configurations {
    compileOnly {
        extendsFrom annotationProcessor
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-data-elasticsearch'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-web'
    compileOnly 'org.projectlombok:lombok'
    runtimeOnly 'com.h2database:h2'
    annotationProcessor 'org.projectlombok:lombok'
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
}

tasks.named('test') {
    useJUnitPlatform()
}
```
<br>

__AbstractElasticsearchConfiguration.java__
```java
public abstract class AbstractElasticsearchConfiguration extends ElasticsearchConfigurationSupport {

	@Bean
	public abstract RestHighLevelClient elasticsearchClient();

	@Bean(name = { "elasticsearchOperations", "elasticsearchTemplate" })
	public ElasticsearchOperations elasticsearchOperations(ElasticsearchConverter elasticsearchConverter,
			RestHighLevelClient elasticsearchClient) {

		ElasticsearchRestTemplate template = new ElasticsearchRestTemplate(elasticsearchClient, elasticsearchConverter);
		template.setRefreshPolicy(refreshPolicy());

		return template;
	}
}
```
앞선 포스팅에서 Elasticsearch 관련 작업을 수행할 때 주로 ElasticsearchOperations 인터페이스의 구현체를 사용하게 된다고 했습니다.  
AbstractElasticsearchConfiguration 클래스를 보면 ElasticsearchOperations을 Bean으로 등록하고 있는 것을 볼 수 있습니다.  
여기서 elasticsearchClient 추상 메서드로 등록되어 있으니 상속받아 구현하여 빈으로 등록해주면 되겠습니다.  
<br>

__ElasticSearchConfig.java__
```java
@Configuration
@EnableElasticsearchRepositories // elasticsearch repository 허용
public class ElasticSearchConfig extends AbstractElasticsearchConfiguration {

    @Override
    public RestHighLevelClient elasticsearchClient() {
        ClientConfiguration clientConfiguration = ClientConfiguration.builder()
                .connectedTo("localhost:9200")
                .build();
        return RestClients.create(clientConfiguration).rest();
    }
}
```
Client는 기본적으로 High Level REST Client를 사용합니다.  
위 코드는 앞선 포스팅에서 보았듯이 공식문서에서 제공하는 코드랑 똑같이 작성해주면 됩니다.  
localhost:9200에 떠있는 ES와 연결하겠다는 의미입니다. 이제 ElasticsearchClient를 사용할 수 있게 되었습니다.  
하지만 JPA 대신 추상화된 Spring-data-JPA를 사용하듯이 실제로 사용할 때는 ElasticsearchOperations 혹은 ElasticsearchRepositry를 사용합니다.   
<Br>

__Member.java, MemberDocument.java__
```java
@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String nickname;

    private int age;

    @Enumerated(EnumType.STRING)
    private Status status;

    @ManyToOne
    @JoinColumn(name = "zone_id")
    private Zone zone;

    private String description;

    public static Member from (MemberSaveRequest memberSaveRequest){
        return Member.builder()
                .name(memberSaveRequest.getName())
                .nickname(memberSaveRequest.getNickname())
                .age(memberSaveRequest.getAge())
                .status(Status.WAIT)
                .zone(Zone.builder().id(memberSaveRequest.getZoneId()).build())
                .description(memberSaveRequest.getDescription())
                .build();
    }
}
```
```java
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Document(indexName = "member")
@Mapping(mappingPath = "elastic/member-mapping.json")
@Setting(settingPath = "elastic/member-setting.json")
public class MemberDocument {

    @Id
    private Long id;

    private String name;

    private String nickname;

    private int age;

    private Status status;

    private Zone zone;

    private String description;

    @Field(type = FieldType.Date, format = {date_hour_minute_second_millis, epoch_millis})
    private LocalDateTime createdAt;

    public static MemberDocument from(Member member){
        return MemberDocument.builder()
                .id(member.getId())
                .name(member.getName())
                .nickname(member.getNickname())
                .age(member.getAge())
                .status(member.getStatus())
                .zone(member.getZone())
                .description(member.getDescription())
                .createdAt(member.getCreatedAt())
                .build();
    }

}
```
Member 엔티티와 ES에 매핑할 MemberDocument 클래스를 따로 만들었습니다.  
Member 클래스 하나에 Entity 매핑과 ES 매핑 모두 할 수 있지만 이렇게 되면 JPA Repository와 ES Repository를 사용할 때 문제가 생깁니다.  
이를 해결하기 위해서는 @EnableJpaRepository와 @EnableElasticsearchRepositorie의 속성을 사용해 어떤 것은 JPA에만 어떤 것은 ES에만 적용되도록 별도의 세팅이 필요합니다.  
그래도 하나의 엔티티에서 설정하고 싶으신 분은 [여기](https://tecoble.techcourse.co.kr/post/2021-10-19-elasticsearch/)를 참고 바랍니다.  
이렇게 해결할 경우, 당연히 새로운 Repository를 추가할 때마다 하드코딩으로 관리해줘야 하기 때문에 OCP를 위반하게 됩니다.  
따라서 저는 분리해서 관리하는게 낫다고 봅니다.  


<Br>

저는 보통 Entity를 설계할 때 연관관계는 모두 fetch=Lazy를 두고 사용하는 편인데 여기서는 테스트를 위해 Lazy로 두지 않았습니다.  
__Lazy로 두면 프록시로 땡겨오게 되는데 프록시로 땡겨온 상태에서 ES에 저장하게 되면 해당 데이터가 들어가지 않기 때문에 주의해야 합니다.__  


<br>

__cf) @PersistenceConstructor__  
앞선 포스팅에서 Spring data elasticsearch 공식 문서에 해당 애노테이션에 관한 내용이 있어서 잠깐 집고 넘어갑시다.  
공식 문서는 해당 애노테이션은 조회 결과를 객체로 복원할 때 사용하는 생성자로 명시하는 것이라 설명하고 있습니다.  
이에 관해 조금 더 찾아보니 Spring Data에서 엔티티 객체를 생성하는 알고리즘은 3가지라고 합니다.  
1. 기본 생성자가 존재한다면 다른 생성자가 존재하더라도 기본 생성자를 사용합니다.
2. 매개변수가 존재하는 생성자가 하나만 존재한다면 해당 생성자를 사용합니다.
3. 매개변수가 존재하는 생성자가 여러 개 있다면 @PersistenceConstructor 애노테이션이 적용된 생성자를 사용합니다.  

StackOverflow를 참고하니 Spring data JPA의 경우 @PersistenceConstructor가 적용되지 않는다고 합니다.  
본론으로 돌아가자면, 기본 생성자 없이 파라미터가 존재하는 생성자가 여럿 존재할 때 객체 복원 시 사용할 생성자를 정해줄 때 사용하는 용도로 보면 됩니다.  
여기서는 딱히 필요해보이지 않아서 사용하지 않았습니다.  

<br>

ES에 데이터 타입을 매핑하는 방식은 2가지가 있습니다.
+ 간단한 경우 : @Field 사용
+ 복잡한 경우 : @Setting, @Mapping 사용
  - @Setting : 분석기를 매핑합니다.
  - @Mapping : 타입을 매핑합니다.

```java
public class MemberDocument {

    @Id
    @Field(type = FieldType.Keyword)
    private Long id;

    @Field(type = FieldType.Text)
    private String name;
}
```
간단한 경우에는 위와 같이 매핑해서 바로 사용하면 됩니다.  
<br><br>

![그림7](https://github.com/backtony/blog-code/blob/master/elk/img/6/2-7.PNG?raw=true)  
복잡한 경우에는 resource 부분에 json 파일을 만들어 정의하고 애노테이션으로 path를 정해서 사용해야 합니다.  

__member-setting.json__  
노리 분석기 정의합니다.
```json
{
  "analysis": {
    "analyzer": {
      "korean": {
        "type": "nori"
      }
    }
  }
}
```
<br>

__member-setting.json__  
데이터 타입을 정의하고 nori 분석기를 사용할 곳에는 분석기도 등록해줍니다.  
```json
{
  "properties" : {
    "age" : {"type" : "keyword"},
    "id" : {"type" : "keyword"},
    "name" : {"type" : "keyword"},
    "nickname" : {"type" : "text"},
    "status" : {"type" : "keyword"},
    "zone" : {
      "properties" : {
        "id" : {"type" : "long"},
        "mainZone" : {
          "type" : "text",
          "fields" : {
            "keyword" : {"type" : "keyword","ignore_above" : 256}
          }
        },
        "subZone" : {
          "type" : "text",
          "fields" : {
            "keyword" : {"type" : "keyword","ignore_above" : 256}
          }
        }
      }
    },
    "description" : {
      "type" : "text",
      "analyzer" : "korean"
    },
    "createdAt" : {
      "type" : "date",
      "format": "uuuu-MM-dd'T'HH:mm:ss.SSS||epoch_millis"
    }
  }
}
```

<br>

__MemberSearchRepository.java__
```java
ppublic interface MemberSearchRepository extends ElasticsearchRepository<MemberDocument,Long> {

    List<MemberDocument> findByAge(int age);

    List<MemberDocument> findByNickname(String nickname, Pageable pageable);
}

```
Spring Data JPA에서 사용자 정의 Repository 인터페이스를 정의할 때 JpaRepository 인터페이스를 확장한 것처럼, ElasticsearchRepository 인터페이스를 확장해 정의하면 됩니다.  
간단하게 네이밍 쿼리만 몇 개 넣어주도록 합니다.  
<Br>

__MemberSearchQueryRepository.java__
```java
@Repository
@RequiredArgsConstructor
public class MemberSearchQueryRepository {

    private final ElasticsearchOperations operations;

    public List<MemberDocument> findByCondition(SearchCondition searchCondition, Pageable pageable) {
        CriteriaQuery query = createConditionCriteriaQuery(searchCondition).setPageable(pageable);

        SearchHits<MemberDocument> search = operations.search(query, MemberDocument.class);
        return search.stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());
    }

    private CriteriaQuery createConditionCriteriaQuery(SearchCondition searchCondition) {
        CriteriaQuery query = new CriteriaQuery(new Criteria());

        if (searchCondition == null)
            return query;

        if (searchCondition.getId() != null)
            query.addCriteria(Criteria.where("id").is(searchCondition.getId()));

        if(searchCondition.getAge() > 0)
            query.addCriteria(Criteria.where("age").is(searchCondition.getAge()));

        if(StringUtils.hasText(searchCondition.getName()))
            query.addCriteria(Criteria.where("name").is(searchCondition.getName()));

        if(StringUtils.hasText(searchCondition.getNickname()))
            query.addCriteria(Criteria.where("nickname").is(searchCondition.getNickname()));

        if(searchCondition.getZoneId() != null)
            query.addCriteria(Criteria.where("zone.id").is(searchCondition.getZoneId()));

        if(searchCondition.getStatus() != null)
            query.addCriteria(Criteria.where("status").is(searchCondition.getStatus()));

        return query;
    }
}
```
복잡한 쿼리의 경우 Querydsl를 사용할 때처럼 분리해서 리포지토리를 만들고 사용합니다.  
검색 조건 SearchCondition를 받아서 이에 맞게 동적 쿼리를 만들었습니다.  
Criteria 사용 방법은 [공식 문서](https://docs.spring.io/spring-data/elasticsearch/docs/current/reference/html/#elasticsearch.operations.criteriaquery)를 참고하세요.  
ES에 관해서 지식이 부족하기 때문에 더 나은 동적 쿼리 작성방법이 있을 것 같은데 혹시 아시는 분은 댓글 부탁드립니다.  

<Br>

__MemberService.java__
```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final MemberSearchRepository memberSearchRepository;
    private final MemberSearchQueryRepository memberSearchQueryRepository;

    @Transactional
    public void saveAllMember(MemberSaveAllRequest memberSaveAllRequest) {
        List<Member> memberList =
                memberSaveAllRequest.getMemberSaveRequestList().stream().map(Member::from).collect(Collectors.toList());
        memberRepository.saveAll(memberList);
    }

    @Transactional
    public void saveAllMemberDocuments() {
        List<MemberDocument> memberDocumentList
                = memberRepository.findAll().stream().map(MemberDocument::from).collect(Collectors.toList());
        memberSearchRepository.saveAll(memberDocumentList);
    }


    public List<MemberResponse> findByNickname(String nickname, Pageable pageable) {
        return memberSearchRepository.findByNickname(nickname, pageable)
                .stream()
                .map(MemberResponse::from)
                .collect(Collectors.toList());
    }

    public List<MemberResponse> findByAge(int age){
        return memberSearchRepository.findByAge(age)
                .stream()
                .map(MemberResponse::from)
                .collect(Collectors.toList());
    }

    public List<MemberResponse> searchByCondition(SearchCondition searchCondition, Pageable pageable) {
        return memberSearchQueryRepository.findByCondition(searchCondition, pageable)
                .stream()
                .map(MemberResponse::from)
                .collect(Collectors.toList());
    }
}
```
만들어준 Repository 메서드를 모두 테스트해보기 위해 Service 코드는 Repository에서 데이터를 꺼내와서 DTO로 변환시켜서 컨트롤러로 반환해 주도록 단순하게 만들었습니다.  
SaveAllMember 메서드는 DB에 저장하는 메서드이고 SaveAllMemberDocuments 메서드는 DB에 데이터를 꺼내서 Document로 변환해서 ES에 저장하는 메서드 입니다.  
__앞서 설명했지만 ES에 데이터를 저장할 때는 Lazy로 프록시로 땡겨온 경우 데이터가 ES에 반영되지 않기 때문에 프록시가 아니고 진짜 데이터가 들어있어야 한다는 것을 명심해야 합니다.__  

<br>

__MemberController.java__  
```java
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class MemberController {

    private final MemberService memberService;


    @PostMapping("/members")
    public ResponseEntity<Void> saveAll(@RequestBody MemberSaveAllRequest memberSaveAllRequest){
        memberService.saveAll(memberSaveAllRequest);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/memberDocuments")
    public ResponseEntity<Void> saveMemberDocuments(){
        memberService.saveMemberDocuments();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/members/age")
    public ResponseEntity<List<MemberResponse>> searchByName(@RequestParam int age){
        return ResponseEntity.ok(memberService.findByAge(age));
    }


    @GetMapping("/members/nickname")
    public ResponseEntity<List<MemberResponse>> searchByNickname(@RequestParam String nickname, Pageable pageable){
        return ResponseEntity.ok(memberService.findByNickname(nickname,pageable));
    }


    @GetMapping("/members")
    public ResponseEntity<List<MemberResponse>> searchByName(SearchCondition searchCondition, Pageable pageable){
        return ResponseEntity.ok(memberService.searchByCondition(searchCondition,pageable));
    }
}
```
이에 맞게 컨트롤러도 만들어줍니다.  


## 실행시키고 확인해보기
Postman을 사용할 수 있겠지만 계속 데이터를 입력해야하는 불편함이 있어서 IntelliJ에서 제공하는 .http를 사용하겠습니다.  
![그림1](https://github.com/backtony/blog-code/blob/master/elk/img/6/2-1.PNG?raw=true)  
<br>

__Member.json__
```json
{
  "memberSaveRequestList": [
    {
      "name": "홍길동1",
      "nickname": "hongil1",
      "age": 27,
      "zoneId": 1,
      "description": "안녕하세요 안녕하세요 안녕하세요 안녕하세요 안녕하세요 안녕하세요 안녕하세요 안녕하세요 안녕하세요 홍길도입니다. 만나서 반갑습니다."
    },
    {
      "name": "홍길동1",
      "nickname": "hongil1",
      "age": 27,
      "zoneId": 1,
      "description": "안녕하세요 안녕하세요 안녕하세요 안녕하세요 안녕하세요 안녕하세요 안녕하세요 홍길도입니다. 만나서 반갑습니다."
    },
    {
      "name": "홍길동1",
      "nickname": "hongil1",
      "age": 27,
      "zoneId": 1,
      "description": "안녕하세요 안녕하세요 안녕하세요 안녕하세요 안녕하세요 홍길도입니다. 만나서 반갑습니다."
    },
    {
      "name": "홍길동2",
      "nickname": "hongil2",
      "age": 27,
      "zoneId": 1,
      "description": "안녕하세요 안녕하세요 홍길도입니다. 만나서 반갑습니다."
    },
    {
      "name": "홍길동2",
      "nickname": "hongil2",
      "age": 27,
      "zoneId": 1,
      "description": "안녕하세요 홍길도입니다. 만나서 반갑습니다."
    },
    {
      "name": "홍길동3",
      "nickname": "hongil3",
      "age": 27,
      "zoneId": 1,
      "description": "안녕하세요 홍길도입니다. 만나서 반갑습니다."
    },
    {
      "name": "홍길동4",
      "nickname": "hongil4",
      "age": 27,
      "zoneId": 1,
      "description": "안녕하세요 홍길도입니다. 만나서 반갑습니다."
    },
    {
      "name": "홍길동4",
      "nickname": "hongil4",
      "age": 27,
      "zoneId": 1,
      "description": "안녕하세요 홍길도입니다. 만나서 반갑습니다."
    },
    {
      "name": "홍길동4",
      "nickname": "hongil4",
      "age": 27,
      "zoneId": 1,
      "description": "안녕하세요 홍길도입니다. 만나서 반갑습니다."
    },
    {
      "name": "홍길동4",
      "nickname": "hongil4",
      "age": 27,
      "zoneId": 1,
      "description": "안녕하세요 홍길도입니다. 만나서 반갑습니다."
    },
    {
      "name": "홍길동4",
      "nickname": "hongil4",
      "age": 27,
      "zoneId": 1,
      "description": "안녕하세요 홍길도입니다. 만나서 반갑습니다."
    },
    {
      "name": "홍길동4",
      "nickname": "hongil4",
      "age": 27,
      "zoneId": 1,
      "description": "안녕하세요"
    }
  ]
}
```
<br>

__member.http__
```json
# 엔티티 저장
POST http://localhost:8080/api/members
Content-Type: application/json

< ./Member.json

###

# 엔티티 document로 전환해서 ES에 저장
POST http://localhost:8080/api/memberDocuments

###

# 닉네임 검색
GET http://localhost:8080/api/members/nickname?nickname=hongil1&size=10

###

# 나이 검색
GET http://localhost:8080/api/members/age?age=27&size=10

###

# 조건 검색
GET http://localhost:8080/api/members?id=1&name=홍길동1&nickname=hongil1&age=27&status=WAIT&zoneId=1&size=10

###

# 일부 조건만 넣어 검색
GET http://localhost:8080/api/members?nickname=hongil1&age=27&size=10
```
\# 3개는 각 요청을 구분해주는 역할을 합니다.  
< 를 통해서 json 파일을 지정하여 전송할 수 있습니다.  
이제 spring을 띄우고 맨 위에 2개의 POST 요청을 차례로 보냅니다.

<br>

![그림2](https://github.com/backtony/blog-code/blob/master/elk/img/6/2-2.PNG?raw=true)  

localhost:5601로 들어가면 키바나가 열립니다.  
왼족의 Discover를 클릭하고 create index pattern을 클릭해줍니다.  

<Br><Br>

![그림3](https://github.com/backtony/blog-code/blob/master/elk/img/6/2-3.PNG?raw=true)  
오른쪽에 member가 index로 등록되어 있는 것이 보입니다.  
코드상에서 @Document(indexName = "member") 해두었던 것이 들어가 있는 것입니다.  
왼쪽에 member*를 적어주고 화면 맨 아래에 create index pattern을 클릭해줍니다.  

<Br><Br>

![그림4](https://github.com/backtony/blog-code/blob/master/elk/img/6/2-4.PNG?raw=true)  

그리고 다시 왼쪽 탭에서 discover를 클릭해주면 나오는 페이지에 데이터가 잘 적재되어있는 것을 확인할 수 있습니다.  

<Br><br>

![그림5](https://github.com/backtony/blog-code/blob/master/elk/img/6/2-5.PNG?raw=true)  

이제 만들어둔 GET 요청들을 보내면서 확인해보면 잘 나오는 것을 확인할 수 있습니다.  
```
http://localhost:9200/member?format=json&pretty
```
위 url로 접속해보면 인덱싱이 잘 되어있는지 확인할 수 있습니다.  


## Elasticsearch TestContainer
테스트 코드를 짜기 위해서는 기존 ES에 테스트 데이터를 넣을 순 없으니 다른 TEST용 ES가 필요합니다.  
Embedded를 지원하지 않을까 찾아봤는데 이에 대한 [답변](https://discuss.elastic.co/t/in-memory-testing-with-resthighlevelclient/106196/5)을 찾을 수 있었습니다.  
결론은 지원하지 않습니다. 따라서 TestContainer를 사용해 봅시다.  
```
testImplementation "org.testcontainers:elasticsearch:1.16.3"
```
의존성을 추가해줍니다.  
<br><br>

![그림6](https://github.com/backtony/blog-code/blob/master/elk/img/6/2-6.PNG?raw=true)  
테스트 디렉토리에 위와 같은 구조로 만들었습니다.  
<br>

__ElasticTestContainer.java__
```java
@TestConfiguration
// ES 관련 리포지토리 등록
@EnableElasticsearchRepositories(basePackageClasses = {MemberSearchRepository.class, MemberSearchQueryRepository.class})
public class ElasticTestContainer extends AbstractElasticsearchConfiguration{

    private static final String ELASTICSEARCH_VERSION = "7.15.2";
    private static final DockerImageName ELASTICSEARCH_IMAGE =
            DockerImageName
                    .parse("docker.elastic.co/elasticsearch/elasticsearch")
                    .withTag(ELASTICSEARCH_VERSION);
    private static final ElasticsearchContainer container;

    // testContainer 띄우기
    static {
        container = new ElasticsearchContainer(ELASTICSEARCH_IMAGE);
        container.start();
    }

    // 띄운 컨테이너로 ESCilent 재정의
    @Override
    public RestHighLevelClient elasticsearchClient() {
        ClientConfiguration clientConfiguration = ClientConfiguration.builder()
                .connectedTo(container.getHttpHostAddress())
                .build();
        return RestClients.create(clientConfiguration).rest();
    }
}
```
+ @TestConfiguration
  - 기존에 정의했던 Configuration을 커스터마이징 하고 싶을 때 사용합니다.
  - 자신이 속한 테스트가 실행될 때 정의된 빈을 생성하여 등록해줍니다.

<Br>

__MemberSearchQueryRepositoryTest.java__
```java

@Import(ElasticTestContainer.class)
// 전부 들어올릴꺼면 classes 옵션 사용하지 않아도 된다.
@SpringBootTest(classes = MemberSearchRepository.class)
class MemberSearchQueryRepositoryTest {


    @Autowired
    MemberSearchRepository memberSearchRepository;


    @Test
    void test() throws Exception{
        //given
        MemberDocument memberDocument = MemberDocument.from(
                Member.builder()
                .id(1L)
                .name("choi")
                .nickname("backtony")
                .age(27)
                .status(Status.WAIT)
                .zone(Zone.builder().id(1L).mainZone("경기도").subZone("안양시").build())
                .build());

        //when
        memberSearchRepository.save(memberDocument);

        //then
        MemberDocument result = memberSearchRepository.findById(1L).get();
        assertThat(result.getId()).isEqualTo(memberDocument.getId());
    }
}
```
+ @SpringBootTest
  - 일반적으로 통합 테스트를 할 때 사용하는 애노테이션
  - classes 속성을 사용하지 않을 경우 애플리케이션 상에 정의된 모든 빈을 생성합니다.
  - classes 속성을 사용할 경우, 빈을 생성할 클래스들을 지정할 수 있습니다.
  - classes 속성에 @Configuration 애노테이션을 사용하는 클래스가 있다면 내부에 @Bean 애노테이션을 통해서 생성되는 빈 모두 등록됩니다.
+ @Import
  - classes 속성을 이용하여 특정 클래스만 지정했을 경우 TestConfiguration은 감지되지 않습니다.
  - 그런 경우 classes 속성에 직접 해당 클래스를 추가해서 해결할 수 있지만 더 좋은 방식이 @Import 입니다.
  - @Omport를 통해 직접 사용할 TestConfiguration 클래스를 명시할 수 있으며 특정 테스트 클래스의 내부 클래스가 이닌 별도 클래스로 분리하여 여러 테스트에서 공유할 수 있습니다.


기본적인 ES testContainer을 띄우고 싶다면 위와 같은 방식으로 하면 됩니다.  
하지만 지금 프로젝트에는 Nori 분석기를 사용하고 있기 때문에 ES testContainer에 Nori 분석기도 설치해줘야 합니다. 간단하게 Container를 만들 때 command를 추가해주면 될 것 같지만 ElasticsearchContainer를 이용해서는 해당 이슈를 해결할 수 없습니다.  
이슈를 해결하기 위해 [Creating images on-the-fly](https://www.testcontainers.org/features/creating_images/)를 사용해봅시다.  
기본의 ElasticsearchContainer를 사용하지 않고 상위 클래스인 GenericContainer를 사용해야 합니다.  
따라서 ES container의존성이 필요 없습니다. ES Container 의존성에서 GenericContainer를 사용할 수 있기 때문에 그대로 의존성을 유지하고 사용해도 되지만 저는 제거하고 jupiter 의존성으로 사용하겠습니다.
```groovy
// 제거
//testImplementation "org.testcontainers:elasticsearch:1.16.3"

// 등록
testImplementation("org.testcontainers:junit-jupiter:1.16.3")
```

<br>

__ElasticTestContainer.java__  
```java
@TestConfiguration
@EnableElasticsearchRepositories(basePackageClasses = {MemberSearchRepository.class, MemberSearchQueryRepository.class})
public class ElasticTestContainer extends AbstractElasticsearchConfiguration{


    private static final GenericContainer container;

    static {
        container = new GenericContainer(
                new ImageFromDockerfile()
                    .withDockerfileFromBuilder(builder -> {
                        builder
                                // ES 이미지 가져오기
                                .from("docker.elastic.co/elasticsearch/elasticsearch:7.15.2")
                                // nori 분석기 설치
                                .run("bin/elasticsearch-plugin install analysis-nori")
                                .build();
                    })
        ).withExposedPorts(9200,9300)
        .withEnv("discovery.type","single-node");

        container.start();
    }

    @Override
    public RestHighLevelClient elasticsearchClient() {
        // ElasticearchContainer에서 제공해주던 httpHostAddress를 사용할수 없기 때문에
        // 직접 꺼내서 만들어줘야 합니다.
        String hostAddress = new StringBuilder()
                .append(container.getHost())
                .append(":")
                .append(container.getMappedPort(9200))
                .toString();

        ClientConfiguration clientConfiguration = ClientConfiguration.builder()
                .connectedTo(hostAddress)
                .build();
        return RestClients.create(clientConfiguration).rest();
    }
}
```
GenericContainer를 사용하기 때문에 ElasticsearchContainer에서 자동으로 설정해주던 것들을 추가로 세팅해줘야 합니다.  
+ withExposedPorts : 기본 포트를 설정해줍니다.
+ withEnv : ES가 싱글 노드로 돌아가도록 설정해줍니다.

<Br>

![그림8](https://github.com/backtony/blog-code/blob/master/elk/img/6/2-8.PNG?raw=true)  

이렇게 세팅해주고 돌리면 정상적으로 nori 분석기가 설치되고 돌아가는 것을 확인할 수 있습니다.  




<Br><Br>

__참고__  
<a href="https://tecoble.techcourse.co.kr/post/2021-10-19-elasticsearch/" target="_blank"> Spring Data Elasticsearch 설정 및 검색 기능 구현</a>   
<a href="https://docs.spring.io/spring-data/elasticsearch/docs/current/reference/html/#reference" target="_blank"> spring data elasticsearch 공식 문서</a>   
<a href="https://veluxer62.github.io/tutorials/spring-data-elasticsearch-test-with-test-container/" target="_blank"> sTestcontainers를 이용한 한글 형태소 검색 테스트 환경 구축하기</a>   



