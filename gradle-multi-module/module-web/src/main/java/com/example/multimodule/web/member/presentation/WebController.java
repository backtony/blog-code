package com.example.multimodule.web.member.presentation;

import com.example.multimodule.core.member.domain.Member;
import com.example.multimodule.web.member.application.WebFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class WebController {

    private final WebFacade webFacade;

    @PostMapping("/")
    public void saveAnyMember(){
        webFacade.saveAnyMember();
    }

    @GetMapping("/")
    public Member getNewMember(){
        return webFacade.findAnyMember();
    }
}
