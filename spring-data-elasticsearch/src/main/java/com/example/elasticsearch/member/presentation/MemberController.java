package com.example.elasticsearch.member.presentation;

import com.example.elasticsearch.member.application.MemberService;
import com.example.elasticsearch.member.presentation.dto.MemberResponse;
import com.example.elasticsearch.member.presentation.dto.MemberSaveAllRequest;
import com.example.elasticsearch.member.presentation.dto.SearchCondition;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class MemberController {

    private final MemberService memberService;


    @PostMapping("/members")
    public ResponseEntity<Void> saveAll(@RequestBody MemberSaveAllRequest memberSaveAllRequest){
        memberService.saveAllMember(memberSaveAllRequest);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/memberDocuments")
    public ResponseEntity<Void> saveMemberDocuments(){
        memberService.saveAllMemberDocuments();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/members/age")
    public ResponseEntity<List<MemberResponse>> searchByName(@RequestParam int age){
        return ResponseEntity.ok(memberService.findByAge(age));
    }


    @GetMapping("/members/nickname")
    public ResponseEntity<List<MemberResponse>> searchByNickname(@RequestParam String nickname, Pageable pageable){
        return ResponseEntity.ok(memberService.findByNickname(nickname,pageable));
    }


    @GetMapping("/members")
    public ResponseEntity<List<MemberResponse>> searchByName(SearchCondition searchCondition, Pageable pageable){
        return ResponseEntity.ok(memberService.searchByCondition(searchCondition,pageable));
    }

    @GetMapping("/members/nickname/startwith")
    public ResponseEntity<List<MemberResponse>> findByStartWithNickname(@RequestParam String nickname, Pageable pageable){
        return ResponseEntity.ok(memberService.findByStartWithNickname(nickname,pageable));
    }

    @GetMapping("/members/matches")
    public ResponseEntity<List<MemberResponse>> findByMatchesDescription(@RequestParam String description, Pageable pageable){
        return ResponseEntity.ok(memberService.findByMatchesDescription(description,pageable));
    }

    @GetMapping("/members/contains")
    public ResponseEntity<List<MemberResponse>> findByContainsDescription(@RequestParam String description, Pageable pageable){
        return ResponseEntity.ok(memberService.findByContainsDescription(description,pageable));
    }


}
