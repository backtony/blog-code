package com.jpa.domain.repository;

import com.jpa.domain.entity.Member;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member,Long> {

    // 네이밍 자동완성 쿼리
    List<Member> findByAge(int age);

    // @Entitygraph는 left outer join으로 fetch join
    // 기본값은 명시한 필드만 EAGER, 나머지는 전부 LAZY
    // type = EntityGraph.EntityGraphType.LOAD 옵션을 추가하면 명시한 필드는 EAGER, 나머지는 엔티티에 세팅해준 대로 기본 FETCH 전략 수행
    @EntityGraph(attributePaths = {"team","zone"})
    Optional<Member> findByName(String name);

    // 직접 쿼리
    @Query("select m from Member m where m.name =:name and m.age =:age")
    Optional<Member> findByNameAndAge(@Param("name") String name, @Param("age") int age);

    // 직접 쿼리 : fetch join
    @Query("select m from Member m left join fetch m.team where m.name =:name")
    Optional<Member> findWithTeamByName(@Param("name") String name);

    // 직접 쿼리 : fetch join - 일대다 데이터 펌핑 제거
    @Query("select distinct m from Member m left join fetch m.memberCategories where m.name =:name")
    Optional<Member> findWithMemberCategoryByName(@Param("name") String name);

    // 직접 쿼리 : 수정 쿼리
    // 수정 쿼리는 영속성 컨텍스트를 무시하고 바로 DB로 전달하기 때문에 DB와 영속성 컨텍스트의 불일치가 생기기 때문에
    // 영속성 컨텍스트를 비워주는 옵션
    @Modifying(clearAutomatically = true)
    @Query("update Member m set m.age =:age where m.name =:name")
    void updateNameByAge(@Param("name") String name, @Param("age")int age);



}
