package com.example.elasticsearch.member.domain.search;

import com.example.elasticsearch.member.domain.MemberDocument;
import com.example.elasticsearch.member.presentation.dto.SearchCondition;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;


@Repository
@RequiredArgsConstructor
public class MemberSearchQueryRepository {

    private final ElasticsearchOperations operations;

    public List<MemberDocument> findByCondition(SearchCondition searchCondition, Pageable pageable) {
        CriteriaQuery query = createConditionCriteriaQuery(searchCondition).setPageable(pageable);

        SearchHits<MemberDocument> search = operations.search(query, MemberDocument.class);
        return search.stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());
    }

    private CriteriaQuery createConditionCriteriaQuery(SearchCondition searchCondition) {
        CriteriaQuery query = new CriteriaQuery(new Criteria());

        if (searchCondition == null)
            return query;

        if (searchCondition.getId() != null)
            query.addCriteria(Criteria.where("id").is(searchCondition.getId()));

        if(searchCondition.getAge() > 0)
            query.addCriteria(Criteria.where("age").is(searchCondition.getAge()));

        if(StringUtils.hasText(searchCondition.getName()))
            query.addCriteria(Criteria.where("name").is(searchCondition.getName()));

        if(StringUtils.hasText(searchCondition.getNickname()))
            query.addCriteria(Criteria.where("nickname").is(searchCondition.getNickname()));

        if(searchCondition.getZoneId() != null)
            query.addCriteria(Criteria.where("zone.id").is(searchCondition.getZoneId()));

        if(searchCondition.getStatus() != null)
            query.addCriteria(Criteria.where("status").is(searchCondition.getStatus()));

        return query;
    }

    public List<MemberDocument> findByStartWithNickname(String nickname, Pageable pageable) {
        Criteria criteria = Criteria.where("nickname").startsWith(nickname);
        Query query = new CriteriaQuery(criteria).setPageable(pageable);
        SearchHits<MemberDocument> search = operations.search(query, MemberDocument.class);
        return search.stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());
    }

    /**
     * 일반적으로 원하는 description이 매칭되는 것들에 맞게 score 계산해서 찾아준다.
     */
    public List<MemberDocument> findByMatchesDescription(String description, Pageable pageable) {
        Criteria criteria = Criteria.where("description").matches(description);
        Query query = new CriteriaQuery(criteria).setPageable(pageable);
        SearchHits<MemberDocument> search = operations.search(query, MemberDocument.class);
        return search.stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());
    }

    /**
     * 형태소 분석기 nori를 사용할 경우 주의해야한다.
     * MemberDocument의 description이 text타입이기 때문에 노리 분석기가 토큰화 시키는데
     * member.json 파일에 보면 안녕하세요 ~~ 이런식으로 되어있는데 토큰화 될 때 안녕, 하, 시, 어요 이렇게 토큰화된다.
     * 따라서 요청 description에 "안녕하세요"로 들어올 경우 쿼리가 *안녕하세요*로 나가기 때문에 찾을 수 없게 된다.
     * 따라서 Contains를 사용할 경우 노리 분석기가 어떻게 동작하는지 잘 인지하고 사용해야 한다.
     *  contains는 앞쪽에 *가 붙어서 쿼리 성능을 급격히 저하시키기 때문에 웬만하면 사용하지 않는 것이 좋다.
     *
     *  nori 형태소가 어떻게 토큰화 하여 저장하는지는 키바나 콘솔에서 아래와 같이 검색하보면 어떤 식으로 토큰화 되는지 확인 가능
     *  {
     *   "tokenizer": "nori_tokenizer",
     *   "text": "원하는 내용"
     * }
     *
     *
     */
    public List<MemberDocument> findByContainsDescription(String description, Pageable pageable) {
        Criteria criteria = Criteria.where("description").contains(description);
        Query query = new CriteriaQuery(criteria).setPageable(pageable);
        SearchHits<MemberDocument> search = operations.search(query, MemberDocument.class);
        return search.stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());
    }




}
