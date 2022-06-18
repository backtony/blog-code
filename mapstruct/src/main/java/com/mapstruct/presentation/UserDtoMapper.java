package com.mapstruct.presentation;

import com.mapstruct.domain.service.dto.request.UserSaveRequestDto;
import com.mapstruct.domain.service.dto.response.UserInfoDto;
import com.mapstruct.domain.service.dto.response.UserResponseDto;
import com.mapstruct.presentation.dto.request.UserSaveRequest;
import com.mapstruct.presentation.dto.request.UserSaveRequest2;
import com.mapstruct.presentation.dto.request.UserSaveRequest3;
import com.mapstruct.presentation.dto.response.UserInfo;
import com.mapstruct.presentation.dto.response.UserInfoResponse;
import org.mapstruct.*;

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
        componentModel = "spring", // 빌드 시 만들어진 구현체를 빈으로 등록
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
