package com.querydsl.domain.repository;

import com.querydsl.config.TestQuerydslConfig;
import com.querydsl.domain.dto.TeamDto;
import com.querydsl.domain.entity.Member;
import com.querydsl.domain.entity.Team;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@Import({TeamQueryRepository.class, TestQuerydslConfig.class})
@DataJpaTest
class TeamQueryRepositoryTest {

    @Autowired TeamQueryRepository teamQueryRepository;
    @Autowired MemberRepository memberRepository;
    @Autowired TeamRepository teamRepository;

    @Test
    @DisplayName("team id로 teamDto 찾기")
    void findTeamDtoByTeamId_success() throws Exception{
        //given
        Team team = teamRepository.save(Team.builder().name("team").build());
        memberRepository.save(Member.builder().age(10).team(team).build());
        memberRepository.save(Member.builder().age(30).team(team).build());

        //when
        TeamDto teamDto = teamQueryRepository.findTeamDtoByTeamId(team.getId()).get();

        //then
        assertThat(teamDto.getAvgAge()).isEqualTo(20);
    }

}