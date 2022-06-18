package com.springsecurity.user.presentation;

import com.springsecurity.common.LoginUser;
import com.springsecurity.user.domain.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    /**
     *  LoginUser 애노테이션이 정상적으로 동작하는지 확인하기 위한 테스트 용도
     *  지금은 딱히 테스트할 컨트롤러가 없어서 작성했지만 다른 로직들이 추가되면 지워도록 합시다.
     */
    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<User> getUserByUsingLoginUserAnnotation(@LoginUser User user){
        return ResponseEntity.ok(user);
    }
}
