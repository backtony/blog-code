package com.example.demo.test.presentation;

import com.example.demo.test.domain.entity.Board;
import com.example.demo.test.domain.entity.Member;
import com.example.demo.test.domain.repository.BoardRepository;
import com.example.demo.test.domain.entity.Tb;
import com.example.demo.test.domain.repository.MemberJdbcRepository;
import com.example.demo.test.domain.repository.TbRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class Controller {

    private final BoardRepository boardRepository;
    private final TbRepository tbRepository;
    private final MemberJdbcRepository memberJdbcRepository;

    @PostMapping("/board")
    public void test(){
        List<Board> ls = new ArrayList<>();

        for (int i=0; i<100;i++){
            Board title = Board.builder().title("title").build();
            ls.add(title);
        }
        boardRepository.saveAll(ls);
    }

    @PostMapping("/tb")
    public void testTb(){
        List<Tb> ls = new ArrayList<>();

        for (int i=0; i<100;i++){
            Tb title = Tb.builder().title("title").build();
            ls.add(title);
        }
        tbRepository.saveAll(ls);
    }

    @PostMapping("/member")
    public void testMember(){
        List<Member> ls = new ArrayList<>();

        for (int i=0; i<100;i++){
            Member member = Member.builder().name("name").build();
            ls.add(member);
        }
        memberJdbcRepository.insertMemberList(ls);
    }
}
