package com.querydsl.domain.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.domain.dto.TeamDto;
import com.querydsl.domain.repository.utils.OrderByNull;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static com.querydsl.domain.entity.QMember.member;

@Repository
@RequiredArgsConstructor
public class TeamQueryRepository {

    private final JPAQueryFactory query;


    /**
     * MySQL에서 Group By 실행시 별도의 Order By 쿼리가 없어도 Filesort(정렬 작업)이 수행된다.
     * 인덱스에 있는 컬럼들로 Group by를 한다면 이미 인덱스로 인해 컬럼들이 정렬된 상태이기 때문에 큰 문제가 되지 않으나
     * 굳이 정렬이 필요 없는 Group by에서 정렬을 다시 할 필요는 없기 때문에 이 문제를 해결해야 하는 것이 좋다.
     *
     * mysql은 자체적으로 order by null을 사용하면 filesort가 제거되는 기능을 제공하지만 querydsl에서는 제공하지 않기에 직접 구현해야 한다.
     */

    public Optional<TeamDto> findTeamDtoByTeamId(Long id){
        return Optional.ofNullable(query
                .select(Projections.fields(TeamDto.class,
                        member.team.name,
                        member.age.avg().as("avgAge")
                ))
                .from(member)
                .where(member.team.id.eq(id))
                .groupBy(member.team)
                .orderBy(OrderByNull.DEFAULT)
                .fetchOne());
    }
}
