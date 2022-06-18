# Spring Mapper - Mapstruct 사용하기




## MapStruct란?

복잡한 애플리케이션을 여러 개의 계층으로 나누어 개발하는 것은 각 계층의 관심 측면만을 전문적으로 다룬다는 점에서 의미가 있습니다.  
이때 각 Layer 마다 사용하고 전달되는 객체는 적절한 컨버팅 로직을 통해서 격리되고 변환되어야 합니다.  
특정 Layer에서 사용되는 객체가 다른 Layer로 전달되는 과정에서 적절한 컨버팅이 없다면 Layer 간의 의존 관계가 깨질 수 있기 때문입니다.  
Layer 간의 객체 변환 로직은 개발자가 일일이 직접 구현해도 되지만 직접 구현하게 될 경우 다음과 같은 문제가 발생합니다.
+ 반복적이고 불필요한 코드 증가
+ 단순한 실수로 인한 개발 생산성 저하

이를 위해 많이 사용하는 매핑 라이브러리에는 ModelMapper가 있는데 최근에는 MapStruct라는 라이브러리가 다음과 같은 이유로 많이 사용됩니다.  

+ 동작 방식 차이
    - ModelMapper는 리플렉션 기반으로 동작하여 실제 매핑 로직을 쉽게 파악하기 어렵습니다.
    - MapStruct는 __코드 생성 방식__ 으로 동작하기 때문에 생성된 코드를 통해 매핑 로직을 쉽게 파악할 수 있습니다.
    - MapStruct는 컴파일 타임에 매핑 오류를 인지하고 설정에 따라 빌드 시 에러를 던질 수 있습니다.
+ 성능적 측면
    - MapStruct는 코드 생성 방식이기 때문에 ModelMapper보다 훨씬 더 좋다고 알려져 있습니다.


## 의존성 설정
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
자세한 설정 내용은 [문서1](https://mapstruct.org/documentation/stable/reference/html/#_gradle), [문서2](https://mapstruct.org/documentation/stable/reference/html/#lombok) 을 참고하세요.  
간략하게 lombok을 같이 세팅해주는 이유는 MapStruct는 lombok에서 생성된 getter, setter 및 생성자를 활용하여 매퍼 구현을 생성하는데 사용할 수 있기 때문입니다.  

## Mapper 인터페이스 구현
### 변환에 사용할 DTO
```java
@Getter
@Setter
public class UserSaveRequest {

    private String nickname;

    private String address;

    private String name;

    private int age;
}
--------------------------------------------------------
@Getter
@Setter
public class UserSaveRequest2 {
    private String nickname2;
}
--------------------------------------------------------
@Getter
@Setter
public class UserSaveRequest3 {

    private String nickname;

    private String address;

    private String name;

    private int age;
}
--------------------------------------------------------
@Getter
@AllArgsConstructor
public class UserSaveRequestDto {
    private String nickname;

    private String address;

    private String name;

    private int age;
}
```
```java
@Getter
@Builder
public class UserInfoDto {

    private String name;
    private int age;
}
--------------------------------------------------------
@Getter
@Builder
public class UserInfo {
    private String name;
    private int age;
}
```
```java
@Getter
@Builder
public class UserResponseDto {

    private String address;

    private List<UserInfoDto> userInfoDtoList;
}
--------------------------------------------------------
@Getter
@Builder
public class UserInfoResponse {

    private String address;

    List<UserInfo> userInfoDtoList;
}
```
+ 변환과정에서 변환 전의 객체에서 값을 꺼내와야 하므로 변환 전 객체(source) 에는 Getter가 있어야 합니다.
+ 변환과정에서 변환 후의 객체에는 (target) 에는 @Builder 또는 @AllArgsConstructor 또는 @Setter가 있어야 합니다.
    - 주의할 점이 @AllArgsConstructor, @NoArgsConstructor이 같이 있을 경우 기본 생성자를 따라가므로 매핑이 안되므로 @NoArgsConstructor(access = AccessLevel.PROTECTED)로 접근 제한자를 세팅해줘야 합니다.

### Mapper 구현
```java
/**
 * @Mapper 애노테이션을 사용해서 MapStruct 클래스라는 것을 알린다.
 * 빌드하게 되면 @Mapper 인터페이스를 찾아서 XXXImpl의 형태로 구현체를 모두 만들게 된다.
 * 이때 componentModel을 spring으로 준다면 생성되는 Impl은 스프링의 싱글톤 빈으로 관리된다. -> @Component가 붙는다.
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
주석으로 상세한 설명을 적어두었습니다.  
<br>

실제로 빌드를 하게 되면 다음과 같은 코드가 generated 폴더에 생성됩니다.
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
이렇게 코드가 생성되는 방식이기 때문에 ModelMapper보다 빠르게 동작할 수 있는 것입니다.  
componentModel을 Spring으로 사용했기 때문에 @Component가 붙은 것을 확인할 수 있습니다.  


## 단위 테스트
ComponentModel을 Spring으로 설정했기에 스프링을 띄워 DI로 주입받아서 테스트할 수 있지만 간단하게 단위 테스트를 하기 위한 용도로 Spring 전체를 띄우는 건 무리가 있습니다.  
[문서](https://mapstruct.org/documentation/stable/reference/html/#retrieving-mapper) 에 따르면 DI 프레임워크를 사용하지 않는 경우 org.mapstruct.factory.Mappers클래스를 통해 Mapper 인스턴스를 검색할 수 있다고 합니다.  
따라서 getMapper() 메서드로 반환할 매퍼의 인터페이스 유형을 전달 하여 메서드를 호출하기만 하면 됩니다.  

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

    @Test
    void userSaveRequest2ToUserSaveRequestDto() {
        //given
        UserSaveRequest2 userSaveRequest2 = new UserSaveRequest2();
        userSaveRequest2.setNickname2("backtony");

        //when
        UserSaveRequestDto result = userDtoMapper.from(userSaveRequest2);

        //then
        assertThat(result.getNickname()).isEqualTo(userSaveRequest2.getNickname2());
    }

    @Test
    void userSaveRequest3ToUserSaveRequestDto() {
        //given
        UserSaveRequest3 userSaveRequest3 = new UserSaveRequest3();
        userSaveRequest3.setAddress("강남");
        userSaveRequest3.setAge(27);
        userSaveRequest3.setName("최준성");
        userSaveRequest3.setNickname("backtony");

        //when
        UserSaveRequestDto result = userDtoMapper.from(userSaveRequest3);

        //then
        assertThat(result.getAddress()).isEqualTo(userSaveRequest3.getAddress());
        assertThat(result.getAge()).isEqualTo(userSaveRequest3.getAge());
        assertThat(result.getName()).isEqualTo(userSaveRequest3.getName());
        assertThat(result.getNickname()).isEqualTo(null);
    }

    @Test
    void userResponseDtoToUserInfoResponse() {
        //given
        UserResponseDto userResponseDto = UserResponseDto.builder()
                .userInfoDtoList(List.of(UserInfoDto.builder()
                                .age(27)
                                .name("backtony1")
                                .build(),
                        UserInfoDto.builder()
                                .age(27)
                                .name("backtony2")
                                .build()))
                .address("강남")
                .build();

        //when
        UserInfoResponse result = userDtoMapper.from(userResponseDto);

        //then
        assertThat(result.getUserInfoDtoList().size()).isEqualTo(userResponseDto.getUserInfoDtoList().size());
        assertThat(result.getUserInfoDtoList().get(0).getAge()).isEqualTo(userResponseDto.getUserInfoDtoList().get(0).getAge());
        assertThat(result.getUserInfoDtoList().get(0).getName()).isEqualTo(userResponseDto.getUserInfoDtoList().get(0).getName());
        assertThat(result.getAddress()).isEqualTo(userResponseDto.getAddress());
    }

    @Test
    void userInfoDtoToUserInfoInfo() {
        //given
        UserInfoDto userInfoDto = UserInfoDto.builder()
                .age(27)
                .name("backtony1")
                .build();

        //when
        UserInfo result = userDtoMapper.from(userInfoDto);

        //then
        assertThat(result.getName()).isEqualTo(userInfoDto.getName());
        assertThat(result.getAge()).isEqualTo(userInfoDto.getAge());
    }

}
```
