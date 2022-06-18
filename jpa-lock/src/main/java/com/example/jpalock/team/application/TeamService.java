package com.example.jpalock.team.application;

import com.example.jpalock.team.domain.Team;
import com.example.jpalock.team.domain.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;

    @Transactional
    public void sleepAndChangeStatusPLAY(Long teamId){
        Team team = teamRepository.findById(teamId).orElseThrow(() -> new RuntimeException("team not found"));
        try {
            Thread.sleep(10_000);
            team.play();
        } catch (Exception e){
        }
    }

    @Transactional
    public void changeStatusCANCEL(Long teamId){
        Team team = teamRepository.findById(teamId).orElseThrow(() -> new RuntimeException("team not found"));
        team.cancel();
    }

    @Transactional
    public void save() {
        teamRepository.save(new Team("team"));
    }

}
