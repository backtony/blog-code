package com.querydsl.domain.repository;


import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.domain.dto.MemberDto;
import com.querydsl.domain.repository.utils.SliceHelper;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static com.querydsl.domain.entity.QMember.member;

/**
 * Querydsl 페이징 사용법
 *
 * fetchResult와 fetchCount가 deprecated 되면서 카운트 쿼리는 따로 작성해야 한다.
 *
 * 고민되는 사항
 * Slice 방식을 결국에 new SliceImpl로 만들어 반환하고 있는데 안에 담기는 쓸모없는 데이터가 너무 많다.
 * 사실상 마지막 페이지인지 아닌지 여부와 content 데이터만 담아서 dto로 만들어서 보내는것이 더 나은 API가 아닐까 고민된다.
 */

@Repository
@RequiredArgsConstructor
public class MemberPagingRepository {

    private final JPAQueryFactory query;

    public Page<MemberDto> findPagingByName(String name, Pageable pageable) {

        // content 가져오는 쿼리
        List<MemberDto> content = query
                // dto로 가져오기
                .select(Projections.fields(MemberDto.class,
                        member.id,
                        member.age,
                        member.name
                ))
                .from(member)
                .where(nameEq(name))
                .offset(pageable.getOffset()) // 시작 위치 = page * page size
                .limit(pageable.getPageSize()) // size
                .orderBy(member.id.desc())
                .fetch();

        // 총 데이터 개수 카운트 쿼리
        JPAQuery<Long> countQuery = query.select(member.count())
                .from(member)
                .where(nameEq(name));

        return PageableExecutionUtils.getPage(content, pageable, () -> countQuery.fetchOne());
    }


    /**
     * querydsl의 경우 slice를 직접 만들어줘야 한다.
     * limit을 1개 더 가져와서 기본 사이즈 보다 더 많다면 다음 페이지가 있는 것으로 판단하면 된다.
     */
    public Slice<MemberDto> findSliceByName(String name, Pageable pageable) {

        // content 가져오는 쿼리
        List<MemberDto> content = query
                // dto로 가져오기
                .select(Projections.fields(MemberDto.class,
                        member.id,
                        member.age,
                        member.name
                ))
                .from(member)
                .where(nameEq(name))
                .offset(pageable.getOffset()) // 시작 위치 = page * page size
                .limit(pageable.getPageSize() + 1)
                .orderBy(member.id.desc())
                .fetch();

        return SliceHelper.toSlice(content, pageable);
    }

    /**
     * NoOffset 방식을 사용한 페이징 쿼리 최적화
     * Slice 방식의 경우 NoOffset으로 쿼리를 최적화할 수 있다.
     * offset이 존재하면 다 읽어놓고 필요한 것만 갖고 나머지는 버리고 사용하는 방식이다.
     * 인자로 마지막에 읽었던 member id를 받아서 where문의 조건으로 걸어 전에 읽었던 데이터는 걸러내버리고 이후부터 읽도록 한다.
     *
     * 이는 전에 읽었었던 데이터는 제외하고 검색하기 때문에 페이지 위치를 알 수 없다.
     * 따라서 Slice가 아닌 Pagination 방식에서는 사용할 수 없다.
     */

    public Slice<MemberDto> findSliceNoOffsetByName(Long lastMemberId, String name, Pageable pageable) {

        // content 가져오는 쿼리
        List<MemberDto> content = query
                // dto로 가져오기
                .select(Projections.fields(MemberDto.class,
                        member.id,
                        member.age,
                        member.name
                ))
                .from(member)
                .where(ltMemberId(lastMemberId),
                        nameEq(name))
                .limit(pageable.getPageSize() + 1)
                .orderBy(member.id.desc())
                .fetch();

        return SliceHelper.toSlice(content, pageable);
    }

    /**
     * 커버링 인덱스를 사용한 페이징쿼리 최적화
     * Pagination 방식은 커버링인덱스 방식으로 최적화할 수 있다.
     * 커버링 인덱스란 쿼리에 사용하는 모든 필드가 인덱스인 쿼리를 의미한다.
     * 커버링 인덱스를 사용해 페이징에 부합하는 PK만 빠르게 땡겨오고 해당 PK만으로 전체 데이터를 땡겨오는 방식이다.
     */

    public Page<MemberDto> findCoveringIndexPagingByName(String name, Pageable pageable) {

        // 커버링 인덱스로 대상 조회
        List<Long> ids = query
                .select(member.id)
                .from(member)
                .where(member.name.like(name + "%"))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(member.id.desc())
                .fetch();

        JPAQuery<Long> countQuery = query
                .select(member.count())
                .from(member)
                .where(member.name.like(name + "%"));

        if (CollectionUtils.isEmpty(ids)) {
            return PageableExecutionUtils.getPage(new ArrayList<MemberDto>(), pageable, () -> countQuery.fetchOne());
        }

        // 실질적인 content 가져오기
        List<MemberDto> content = query
                .select(Projections.fields(MemberDto.class,
                        member.id,
                        member.age,
                        member.name
                ))
                .from(member)
                .where(member.id.in(ids))
                .orderBy(member.id.desc())
                .fetch();

        return PageableExecutionUtils.getPage(content, pageable, () -> countQuery.fetchOne());
    }


    private BooleanExpression ltMemberId(Long lastMemberId) {
        return lastMemberId == null
                ? null
                : member.id.lt(lastMemberId);
    }


    private BooleanExpression nameEq(String name) {
        return StringUtils.hasText(name)
                ? member.name.eq(name)
                : null;
    }


}
