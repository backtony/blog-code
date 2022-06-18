package com.querydsl.domain.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.domain.entity.Member;
import com.querydsl.domain.entity.QMember;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

import static com.querydsl.domain.entity.QMember.member;
import static com.querydsl.domain.entity.QZone.zone;

/**
 * 일반적인 Querydsl 사용법
 */

@Repository
@RequiredArgsConstructor
public class MemberQueryRepository {

    private final JPAQueryFactory query;

    public Optional<Member> findById(Long id){
        return Optional.ofNullable(query.selectFrom(member)
                .where(idEq(id))
                .fetchOne());
    }

    public List<Member> findByNameAndAge(String name, int age){
        return query
                    .select(member)
                    .from(member)
                    .where(nameEq(name), ageEq(age))
                    .fetch();
    }

    public List<Member> findByBetweenAge(int startAge, int endAge){
        return query
                .select(member)
                .from(member)
                //.where(member.age.notIn(startAge,endAge))
                .where(member.age.between(startAge,endAge))
                .fetch();
    }


    public List<Member> findByStartWithName(String name){
        return query
                .select(member)
                .from(member)
//                .where(member.name.contains(name))
//                .where(member.name.like(name+"%"))
                .where(member.name.startsWith(name))
                .fetch();
    }

    public List<Member> findByNameOrderByAgeDesc(String name){
        return query
                .select(member)
                .from(member)
                .where(nameEq(name))
                // null값은 마지막으로 보내기
                .orderBy(member.age.desc().nullsLast())
                .fetch();
    }

    public boolean exist (Long id){
        Integer result = query
                .selectOne()
                .from(member)
                .where(idEq(id))
                .fetchFirst();
        return result != null;
    }

    public Optional<Member> findWithZoneById(Long id){
        return Optional.ofNullable(
                query.select(member)
                .from(member)
                .where(idEq(id))
//               leftJoin하면 leftJoin해서 쿼리가 나가지만 Lazy라 프록시가 들어있어 결국에 사용할 때 쿼리가 나간다
//              .leftJoin(member.zone, zone).
                .leftJoin(member.zone, zone).fetchJoin()
                .fetchOne()
        );
    }


    // 억지스러운 쿼리지만 서브쿼리 사용법을 위해 작성
    public List<Member> subQuery(int age){

        // 바깥 쿼리와 내부 쿼리는 다른 Q를 사용
        QMember memberSub = new QMember("memberSub");

        return query
                .selectFrom(member)
                .where(member.age.in(
                        // 서브 쿼리 -> static import로 축약 가능
                        JPAExpressions
                                .select(memberSub.age)
                                .from(memberSub)
                                .where(memberSub.age.goe(age))
                ))
                .fetch();
    }

    /**
     * 벌크연산은 영속성 컨텍스트를 거치지 않고 바로 DB로 날라간다. -> execute 사용 연산
     * 즉, 영속성 컨텍스트와 DB와의 데이터 불일치가 발생한다.
     * 따라서 벌크연산을 사용한 이후에는 em.flush와 clear를 수행한 후 이후 로직을 진행해야한다.
     *
     * JPA에서는 @Modifying(clearAutomatically = true) 옵션이 있는데 querydsl은 없어서 직접 flush clear 해줘야함
     */
    public long updateName(Long id, String name){
        return query
                .update(member)
                .set(member.name, name)
                .where(idEq(id))
                .execute();
    }

    private BooleanExpression idEq(Long id) {
        return id == null
                ? null
                : member.id.eq(id);
    }

    private BooleanExpression ageEq(int age) {
        return age<=0
                ? null
                : member.age.eq(age);
    }

    private BooleanExpression nameEq(String name) {
        return StringUtils.hasText(name)
                ? member.name.eq(name)
                : null;
    }
}
