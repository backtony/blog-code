package com.example.jpalock.team.presentation;

import com.example.jpalock.team.application.TeamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class TeamRestController {

    private final TeamService teamService;

    /**
     * sleep 호출하고 cancel 바로 호출하면
     * sleep이 먼저 조회하고 잠자는 동안 cancel에서 상태 바꾸고 커밋해버림
     * OptimisticLockingFailureException이 발생할 것임
     * 아무 옵션도 걸지 않았기 때문에 조회만 한 것으로는 version이 올라가진 않는다.
     */

    @GetMapping("sleep/{teamId}")
    public void sleep(@PathVariable Long teamId){
        try {
            teamService.sleepAndChangeStatusPLAY(teamId);
        } catch (OptimisticLockingFailureException ex){
            log.error("{} : {}","낙관적락 예외 발생", "동작 하는 과정에서 누가 중간에 수정했다.");
        }
    }

    @GetMapping("{teamId}")
    public void cancel(@PathVariable Long teamId){
        teamService.changeStatusCANCEL(teamId);
    }

    @PostMapping
    public void save(){
        teamService.save();
    }
}
