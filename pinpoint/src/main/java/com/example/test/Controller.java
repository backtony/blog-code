package com.example.test;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;


@RestController
@RequiredArgsConstructor
public class Controller {

    private final MemberService memberService;


    @GetMapping("/find")
    public String test(){
        memberService.find();
        return "OK";
    }

    @GetMapping("/health")
    public String health(){
        return "version 2 OK";
    }

    @GetMapping("/save")
    public String save(){
        memberService.save();
        return "OK";

    }


    @DeleteMapping("/delete")
    public String delete(){
        memberService.delete();
        return "OK";
    }
    

}
