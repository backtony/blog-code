## 소개
JPA 쿼리 생성의 다양한 방식을 정리한다.  
<br>

+ MemberRepository.class와 MemberPagingRepository 코드만 보면 되고 아래와 같은 내용을 작성했다.
    + 네이밍 자동 완성 쿼리 방식
    + EntityGraph 방식
    + @Query 직접 사용 방식
    + @Modifying
    + 일대다 데이터 펌핑 제거
    + join fetch
    + 페이징 쿼리
+ 페이징 테스트 코드는 MemberRepositoryPagingTest에서 나머지 테스트 코드는 MemberRepositoryTest에서 확인하면 된다.  
+ HowToJpaIndex.class는 JPA 엔티티에서 인덱스 설정하는 방법 명시