# 소개
+ kotlin spring을 사용한 oauth2 demo
  + resource server
    + album-resource-server 프로젝트
      + friend-resource-server로 데이터를 가져오는 client 역할도 겸임
      + authorization server로부터 client credential 방식으로 토큰을 발급받아서 friend 쪽과 통신
    + friend-resource-server 프로젝트
  + authorization server
    + authorization-server 프로젝트
      + social이 아닌 self 구축
  + oauth2 client 
    + oauth-client 프로젝트
  + domain
    + 도메인 모듈

127.0.0.1:8080 서버에 접속해서 login하고 토큰을 발급받아서 resource server에 요청시 발급받은 토큰을 헤더에 추가해서 사용하면 된다.(localhost를 사용하면 리다이렉트 시에 막히므로 127.0.0.1로 접근해야 한다.)  

학습 용도로 oauth2 client의 ui로 token을 발급받아서 resource 서버로 요청하는 시나리오로 구성했지만, 보통 실제 운영환경에서는 server to server의 인증 방식으로는 뒤에서 authorization server로 토큰을 발급받아서 resource 서버로 요청하는 방식으로 사용된다.(album-resource-server에서 friend-resource-server로 요청하는 방식을 보면 이해할 수 있다.)


# oauth 운영환경에서의 주의사항(inmemory -> jdbc)
oauth-client의 OAuth2AuthorizedClient는 OAuth2AuthorizedClientService에 의해 저장되고,  authorization-server의 OAuth2Authorization는 OAuth2AuthorizationService에 의해 저장된다.  

OAuth2AuthorizedClient와 OAuth2Authorization는 서로 대응되는 개념이다. OAuth2AuthorizedClient는 클라이언트가 인가를 받았다는 개념이었고 OAuth2Authorization는 인가서버가 클라이언트에게 권한을 부여했다는 의미다.

기본적으로 두 service는 inMemory 구현체가 default 빈으로 생성된다. 이는 운영환경에서는 jdbc 구현체로 변경이 필요하다. 관련 내용은 [여기](https://youtu.be/-YbqW-pqt3w?t=1091)를 참고하라.  
요약하면 사용자의 요청마다 쌓이게 되는데 inmemory의 경우 OOM이 발생할 수 있다.  

server to server(client credential)로만 사용할 경우, 구현체의 흐름을 따라가다 보면 익명사용자로 처리되어서 계속 덮어씌워지기 때문에 inmemory를 사용해도 운영상 상관이 없다.

server to server 라도 authorization_code 방식을 사용한다면 InMemoryOAuth2AuthorizationService의 save메서드에서 인가 전에 initializedAuthorizations가 처음 code를 저장하면서 인메모리에 여럿이 쌓이게 되므로 이경우에는 jdbc를 사용해야 한다.

처리흐름을 상세히 보면, 
OAuth2AuthorizedClientRepository가 OAuth2AuthorizedClientService 에게 OAuth2AuthorizedClient 의 저장, 조회, 삭제 처리를 위임한다.(OAuth2AuthorizedClient 관리 역할을 service에게 위임한다.) OAuth2AuthorizedClientRepository의 구현체는 AuthenticatedPrincipalOAuth2AuthorizedClientRepository와 HttpSessionOAuth2AuthorizedClientRepository이 있는데 아무런 설정이 없다면 전자가 사용된다. 익명 anonymous 사용자에 대해서는 authorizedClientService가 아니라 anonymousAuthorizedClientRepository에 위임하게 되고 이는 세션에서 처리된다. server to server의 경우 익명 토큰, 혹은 토큰이라도 authentication의 isAuthenticated가 false이므로 anonymousAuthorizedClientRepository를 타게 된다. 익명 토큰으로 넣으면 authorizedClient.getClientRegistration().getRegistrationId()가 키값으로 덮어지기 때문에 운영상에서 inmemory를 사용해도 문제가 없다.

## 결론
+ OAuth2AuthorizedClientService, OAuth2AuthorizationService
  + 운영환경
    + server to server + client credential 방식
      + 인메모리를 사용해도 문제 없다.
    + 이외 운영 환경
      + jdbc 빈으로 따로 등록해줘야 한다.

인가서버에는 권한 동의 내역을 저장하는 OAuth2AuthorizationConsentService가 존재하는데 이도 위와 같이 이외 운영 환경에서는 jdbc를 사용해야 한다. 


# 개념 정리
+ [[spring oauth2] Spring Security Fundamentals](https://github.com/backtony/blog-code/issues/28)
+ [[spring oauth2] OAuth 2.0 용어 이해와 권한부여 유형](https://github.com/backtony/blog-code/issues/29)
+ [[spring oauth2] OAuth 2.0 Open ID Connect](https://github.com/backtony/blog-code/issues/30)
+ [[spring oauth2] Spring Security Oauth2, clientRegistration](https://github.com/backtony/blog-code/issues/31)
+ [[spring oauth2] OAuth 2.0 Client - oauth2Login](https://github.com/backtony/blog-code/issues/32)
+ [[spring oauth2] OAuth 2.0 Client - oauth2Client](https://github.com/backtony/blog-code/issues/33)
+ [[spring oauth2] OAuth 2.0 Resource Server](https://github.com/backtony/blog-code/issues/35)
+ [[spring oauth2] OAuth 2.0 Authorization Server](https://github.com/backtony/blog-code/issues/36)
+ [토리맘 oauth2](https://godekdls.github.io/Spring%20Security/oauth2/)
+ [baeldung oauth authorization server](https://www.baeldung.com/spring-security-oauth-auth-server)
