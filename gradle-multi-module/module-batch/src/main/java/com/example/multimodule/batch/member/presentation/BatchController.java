package com.example.multimodule.batch.member.presentation;

import com.example.multimodule.batch.member.application.BatchFacade;
import com.example.multimodule.core.member.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class BatchController {

    private final BatchFacade batchFacade;

    @PostMapping("/")
    public void saveAnyMember(){
        batchFacade.saveAnyMember();
    }

    @GetMapping("/")
    public Member getNewMember(){
        return batchFacade.findAnyMember();
    }
}
