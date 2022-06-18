package com.jpa.domain.repository;

import com.jpa.domain.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;


/**
 * 현재 테스트 코드에서는 Pageable를 생성할 때 order 조건을 넣고 있는데
 * 쿼리가 복잡해지면 안 먹을 수가 있다.
 * 따라서 웬만하면 Pageable에 order 조건을 넣기보다는 페이징 쿼리 자체에 Order by 조건을 넣는 것이 낫다.
 *
 * 여기서는 컨트롤러와 서비스 단을 구현하지 않았기에 추가적인 설명을 하자면
 * 현재 쿼리는 엔티티를 반환한다. 따라서 컨트롤러에서 반환할 때는 엔티티가 아닌 dto로 변환해서 반환해야 한다.
 * 이는 간단하게 map을 이용해서 dto로 변환해서 반환해야 한다.
 */
public interface MemberPagingRepository extends JpaRepository<Member,Long> {

    // 검색된 전체 데이터 건수를 조회하는 count 쿼리가 추가적으로 호출된다.
    Page<Member> findByName(String name, Pageable pageable);

    // 카운트 쿼리의 경우 기존 쿼리에 count만 붙어서 나가기 때문에 성능상 문제가 발생할 수 있다.
    // 간단한 경우에는 문제가 없으나 복잡한 쿼리의 경우 여러번의 join이 발생하게 되는데 사실상 count쿼리는 join이 필요없는 경우가 많다.
    // 이를 최적화하자
    @Query(value = "select m from Member m left join m.team t",
            countQuery = "select count(m) from Member  m")
    Page<Member> findFastByName(String name, Pageable pageable);

    // Slice 방식은 앱에서 보면 더보기 방식이다.
    // 카운트 쿼리를 실행하지 않고 limit를 1개 추가적으로 조회해서 다음 페이지가 있는지만을 확인해준다.
    // 따라서 총 개수와 총 페이지 개수는 모른다.
    Slice<Member> findSliceByName(String name, Pageable pageable);

    // 추가적인 count 쿼리가 호출되지 않는다.
    List<Member> findByAge(int age, Pageable pageable);
}
