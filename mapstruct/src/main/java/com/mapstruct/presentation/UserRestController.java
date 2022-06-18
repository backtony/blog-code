package com.mapstruct.presentation;

import com.mapstruct.application.UserFacade;
import com.mapstruct.domain.service.dto.request.UserSaveRequestDto;
import com.mapstruct.domain.service.dto.response.UserResponseDto;
import com.mapstruct.presentation.dto.request.UserSaveRequest;
import com.mapstruct.presentation.dto.request.UserSaveRequest2;
import com.mapstruct.presentation.dto.request.UserSaveRequest3;
import com.mapstruct.presentation.dto.response.UserInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserRestController {

    private final UserFacade userFacade;
    private final UserDtoMapper userDtoMapper;

    @PostMapping("/v1/users")
    public ResponseEntity<Void> registerUser(@RequestBody UserSaveRequest userSaveRequest){

        UserSaveRequestDto userSaveRequestDto = userDtoMapper.from(userSaveRequest);
        userFacade.registerUser(userSaveRequestDto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/v2/users")
    public ResponseEntity<Void> registerUser2(@RequestBody UserSaveRequest2 userSaveRequest2){
        UserSaveRequestDto userSaveRequestDto = userDtoMapper.from(userSaveRequest2);
        userFacade.registerUser(userSaveRequestDto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/v3/users")
    public ResponseEntity<Void> registerUser3(@RequestBody UserSaveRequest3 userSaveRequest3){
        UserSaveRequestDto userSaveRequestDto = userDtoMapper.from(userSaveRequest3);
        userFacade.registerUser(userSaveRequestDto);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/v1/users")
    public ResponseEntity<UserInfoResponse> getSameAddressUser(){
        UserResponseDto sameAddressUserInfo = userFacade.getSameAddressUserInfo();
        UserInfoResponse userInfoResponse = userDtoMapper.from(sameAddressUserInfo);
        return ResponseEntity.ok(userInfoResponse);
    }
}
