package com.example.async.user.presentation;

import com.example.async.user.application.CallService;
import com.example.async.user.application.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserRestController {

    private final UserService userService;
    private final CallService callService;

    @GetMapping("/hello")
    public void hello(){
        userService.hello();
    }

    @GetMapping("/event")
    public void event(){
        userService.event();
    }

    @GetMapping("/voidCall")
    public void voidCall(){
        callService.voidCall();
    }

    @GetMapping("/futureCall")
    public void futureCall(){
        callService.futureCall();
    }

    @GetMapping("/listenCall")
    public void listenCall(){
        callService.listenableFutureCall();
    }

    @GetMapping("/completableCall")
    public void completableCall(){
        callService.completableFutureCall();
    }
}
