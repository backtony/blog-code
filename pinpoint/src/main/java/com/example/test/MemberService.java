package com.example.test;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {

    private final Repository repository;

    public void find() {
        List<Member> all = repository.findAll();
        System.out.println("========== find start ==========");
        for (Member member : all) {
            System.out.println("member.getName() = " + member.getName());
        }
        System.out.println("========== find end ==========");

    }

    public void save() {
        System.out.println("======= save start ==========");
        for(int i=0;i<10;i++){
            Member m1 = Member.builder()
                    .name("m"+i)
                    .build();
            repository.save(m1);
        }
        System.out.println("========== save end ==========");
    }

    public void delete() {
        System.out.println(" ========== delete start ==========");
        repository.deleteAll();
        System.out.println(" ========== delete end ==========");

    }


}
