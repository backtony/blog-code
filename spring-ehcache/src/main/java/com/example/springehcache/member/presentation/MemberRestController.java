package com.example.springehcache.member.presentation;

import com.example.springehcache.member.application.MemberService;
import com.example.springehcache.member.presentation.dto.MemberInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class MemberRestController {

    private final MemberService memberService;

    @PostMapping
    public void save() {
        memberService.saveMember();
    }

    @GetMapping("/{memberId}")
    public ResponseEntity<MemberInfoResponse> get(@PathVariable Long memberId) {
        return ResponseEntity.ok(MemberInfoResponse.from(memberService.getMember(memberId)));
    }


    @PatchMapping("/{memberId}")
    public void modifyName(@PathVariable Long memberId, String name) {
        memberService.updateMemberName(memberId, name);
    }
}
