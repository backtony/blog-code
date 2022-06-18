package com.example.userservice.presentation;

import com.example.userservice.application.UserFacade;
import com.example.userservice.domain.service.dto.response.UserInfoResponseDto;
import com.example.userservice.presentation.dto.UserDtoMapper;
import com.example.userservice.presentation.dto.request.UserSaveRequest;
import com.example.userservice.presentation.dto.response.UserInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserRestController {

    private final UserFacade userFacade;
    private final UserDtoMapper userDtoMapper;

    @PostMapping
    public ResponseEntity<UserInfoResponse> createUser(@RequestBody UserSaveRequest userSaveRequest){
        UserInfoResponseDto userInfoResponseDto = userFacade.createUser(userDtoMapper.from(userSaveRequest));
        UserInfoResponse userInfoResponse = userDtoMapper.from(userInfoResponseDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(userInfoResponse);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserInfoResponse> getUser(@PathVariable("userId") String userId) {
        UserInfoResponse userInfoResponse = userDtoMapper.from(userFacade.getUserInfo(userId));
        return ResponseEntity.ok(userInfoResponse);
    }
}
