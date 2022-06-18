package com.example.elasticsearch.member.domain.search;

import com.example.elasticsearch.config.ElasticTestContainer;
import com.example.elasticsearch.member.domain.Member;
import com.example.elasticsearch.member.domain.MemberDocument;
import com.example.elasticsearch.member.domain.Status;
import com.example.elasticsearch.zone.Zone;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;


@Import(ElasticTestContainer.class)
// 전부 들어올릴꺼면 classes 옵션 사용하지 않아도 된다.
@SpringBootTest(classes = MemberSearchRepository.class)
class MemberSearchQueryRepositoryTest {


    @Autowired
    MemberSearchRepository memberSearchRepository;


    @Test
    void test() throws Exception{
        //given
        MemberDocument memberDocument = MemberDocument.from(
                Member.builder()
                .id(1L)
                .name("choi")
                .nickname("backtony")
                .age(27)
                .status(Status.WAIT)
                .zone(Zone.builder().id(1L).mainZone("경기도").subZone("안양시").build())
                .build());

        //when
        memberSearchRepository.save(memberDocument);

        //then
        MemberDocument result = memberSearchRepository.findById(1L).get();
        assertThat(result.getId()).isEqualTo(memberDocument.getId());
    }
}