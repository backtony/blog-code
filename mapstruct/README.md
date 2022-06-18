## MapStruct 사용 예시
+ 복잡한 애플리케이션을 여러 개의 계층으로 나누어 개발하는 것은 각 계층의 관심 측면만을 전문적으로 다룬다는 점에서 의미가 있다.
    + 이때 각 Layer 마다 사용하고 전달되는 객체는 적절한 컨버팅 로직을 통해서 격리되고 변환되어야 한다.
    + 특정 Layer에서 사용되는 객체가 다른 Layer로 전달되는 과정에서 적절한 컨버팅이 없다면 Layer 간의 의존 관계가 깨질 수 있다.
    

+ Layer 간의 객체 변환 로직은 개발자가 일일이 직접 구현해도 되지만 
    + 반복적이고 불필요한 코드가 많아지게 되고
    + 단순한 실수로 인한 개발 생산성이 떨어지게 된다.
    
    
+ 이를 위해 많이 사용하는 매핑 라이브러리에는 ModelMapper가 있는데 최근에는 MapStruct라는 라이브러리도 많이 사용된다.
    + 동작 방식 차이
        + ModelMapper는 리플렉션 기반으로 동작하여 실제 매핑 로직을 쉽게 파악하기 어렵다.
        + MapStruct는 코드 생성 방식으로 동작하기 때문에 생성된 코드를 통해 매핑 로직을 쉽게 파악할 수 있다.
        + MapStruct는 컴파일 타임에 매핑 오류를 인지하고 설정에 따라 빌드 시 에러를 던질 수 있다.
    + 성능적 측면
        + MapStruct는 코드 생성 방식이기 때문에 ModelMapper보다 훨씬 더 좋다고 알려져 있다.
    

### build.gradle
```groovy
implementation 'org.mapstruct:mapstruct:1.4.2.Final'
annotationProcessor "org.mapstruct:mapstruct-processor:1.4.2.Final"
annotationProcessor(
        'org.projectlombok:lombok',
        'org.projectlombok:lombok-mapstruct-binding:0.1.0',
        'org.mapstruct:mapstruct-processor:1.4.2.Final'        
)

// 테스트에서 사용할 경우
//testAnnotationProcessor "org.mapstruct:mapstruct-processor:${mapstructVersion}"
```


### 구현 사항
```java
/**
 * @Mapper 애노테이션을 사용해서 MapStruct 클래스라는 것을 알린다.
 * 빌드하게 되면 @Mapper 인터페이스를 찾아서 XXXImpl의 형태로 구현체를 모두 만들게 된다.
 * 이때 componentModel을 spring으로 준다면 생성되는 Impl은 스프링의 싱글톤 빈으로 관리된다. -> @Component가 붙는다.
 *
 *
 * 변환과정에서 꺼내오는 객체(source) 에는 Getter가 있어야하고
 * 변환해서 저장하고자 하는 객체(target) 에는 Builder 혹은 모든 필드를 담을 수 있는 생성자가 있어야 한다.
 */

@Mapper(
        componentModel = "spring", // 빌드 시 구현체 만들고 빈으로 등록
        injectionStrategy = InjectionStrategy.CONSTRUCTOR, // 생성자 주입 전략
        unmappedTargetPolicy = ReportingPolicy.ERROR // 일치하지 않는 필드가 있으면 빌드 시 에러
)
public interface UserDtoMapper {

    UserSaveRequestDto from(UserSaveRequest userSaveRequest);

    // 조건이 여러 개라면 @Mappings로 묶어서 안에 Mapping을 세팅해준다.
    @Mappings({
            @Mapping(source = "nickname2", target = "nickname"), // 변수명이 다를 경우 매핑 설정

            // source에는 있지만 target에는 없는 경우 target쪽 매핑 무시
            @Mapping(target = "address", ignore = true),
            @Mapping(target = "name", ignore = true),
            @Mapping(target = "age", ignore = true)
    })
    UserSaveRequestDto from(UserSaveRequest2 userSaveRequest);

    // 매핑을 원하지 않는 source, target 필드
    @Mapping(source = "nickname", target = "nickname", ignore = true)
    UserSaveRequestDto from(UserSaveRequest3 userSaveRequest);

    /**
     * 객체 안에 객체가 있는 경우 해당 객체도 변환하는 매퍼를 등록해야 한다.
     */
    UserInfoResponse from(UserResponseDto userResponseDto);
    UserInfo from(UserInfoDto userInfoDto);
}
```
<br>

<br>

실제로 빌드를 하게 되면 다음과 같은 코드가 generated 폴더에 생성된다.  
```java
@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2022-05-08T13:01:47+0900",
    comments = "version: 1.4.2.Final, compiler: javac, environment: Java 11.0.11 (AdoptOpenJDK)"
)
@Component
public class UserDtoMapperImpl implements UserDtoMapper {

    @Override
    public UserSaveRequestDto from(UserSaveRequest userSaveRequest) {
        if ( userSaveRequest == null ) {
            return null;
        }

        String nickname = null;
        String address = null;
        String name = null;
        int age = 0;

        nickname = userSaveRequest.getNickname();
        address = userSaveRequest.getAddress();
        name = userSaveRequest.getName();
        age = userSaveRequest.getAge();

        UserSaveRequestDto userSaveRequestDto = new UserSaveRequestDto( nickname, address, name, age );

        return userSaveRequestDto;
    }
    // ... 생략
}
```
이렇게 코드가 생성되는 방식이기 때문에 ModelMapper보다 빠르게 동작할 수 있는 것이다.  
componentModel을 Spring으로 사용했기 때문에 @Component가 붙은 것을 확인할 수 있다.


### 단위 테스트
```java
public class UserDtoMapperTest {

    UserDtoMapper userDtoMapper = Mappers.getMapper(UserDtoMapper.class);

    @Test
    void userSaveRequestToUserSaveRequestDto() {
        //given
        UserSaveRequest userSaveRequest = new UserSaveRequest();
        userSaveRequest.setAddress("강남");
        userSaveRequest.setAge(27);
        userSaveRequest.setName("최준성");
        userSaveRequest.setNickname("backtony");

        //when
        UserSaveRequestDto result = userDtoMapper.from(userSaveRequest);

        //then
        assertThat(result.getAddress()).isEqualTo(userSaveRequest.getAddress());
        assertThat(result.getAge()).isEqualTo(userSaveRequest.getAge());
        assertThat(result.getName()).isEqualTo(userSaveRequest.getName());
        assertThat(result.getNickname()).isEqualTo(userSaveRequest.getNickname());
    }
}
```
ComponentModel을 Spring으로 설정했기에 스프링을 띄워 DI로 주입받아서 테스트할 수 있지만 간단하게 단위 테스트를 하기 위한 용도로 Spring 전체를 띄우는 건 무리가 있다.  
[문서](https://mapstruct.org/documentation/stable/reference/html/#retrieving-mapper) 에 따르면 DI 프레임워크를 사용하지 않는 경우 org.mapstruct.factory.Mappers클래스를 통해 Mapper 인스턴스를 검색할 수 있다고 한다.  
따라서 getMapper() 메서드로 반환할 매퍼의 인터페이스 유형을 전달 하여 메서드를 호출하기만 하면 된다.  
다른 변환 테스트 코드는 UserDtoMapperTest.class 에서 확인할 수 있다.

