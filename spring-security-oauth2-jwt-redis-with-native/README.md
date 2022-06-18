## spring security with OAuth2, JWT, Redis, native login

+ 회원가입
  + OAuth2 - 카카오, 구글, 네이버
    + 핵심 로직은 CustomOauth2Service.class 참고하세요.
    + 요청은 /oauth2/authorization/google 또는 naver 또는 kakao가 기본값이지만 SecurityConfig.class에서 baseUri를 "/api/oauth2/authorization" 으로 설정했으므로 /api/oauth2/authorization/google , naver, kakao로 요청을 보내야 합니다.
  + 일반 회원
    + AuthRestController.class의 sign-up 요청을 참고하세요.
    + 로그인
      + SecurityConfig.class에 LoginFilter 빈 등록 내용을 참고하세요.
        + /api/v1/login 으로 로그인 요청을 보내면 됩니다.
      + 커스텀 로직은 CustomUserDetailsService.class 참고하세요.
+ 로그인 과정은 백엔드 서버와 JWT 토큰으로 통신
  + AccessToken, RefreshToken 제공
  + JWT 토큰은 TokenAuthenticationFilter 에서 처리
  + 일반 로그인은 LoginFilter.class, 소셜 로그인은 CustomOauth2Service.class 에서 처리되고 둘다 LoginSuccessHandler 를 통해 response 값에 JWT 토큰이 담겨서 프론트로 응답합니다.
+ 일반 security와 OAuth2에서 제공하는 각각의 서비스에서는 UserDetails와 OAuth2User를 반환해야 하는데 MemberPrincipal.class에서 이를 구현하여 하나로 통일하여 사용합니다.
+ Logout시 JWT accessToken과 refreshToken을 받아서 redis에 해당 토큰의 남은 시간을 TTL로 세팅하고 저장
  + JWT 로그인 처리를 진행하는 TokenAuthenticationFilter.class 에서 해당 토큰으로 로그인 시도하는 요청을 막습니다.
+ 인증 실패 시 RestAuthenticationEntryPoint.class 에서 처리합니다.
+ 인가 실패 시 RestAccessDeniedHandler.class 에서 처리합니다.
+ LoginFilter.class 에서 터지는 예외는 LoginFailureHandler.class 에서 처리합니다.
+ TokenAuthenticationFilter.class 에서 터지는 예외는 TokenAuthenticationErrorFilter에서 처리합니다.
  + LoginFilter에서 터지는 예외 처리와 TokenAuthenticationFilter에서 터지는 예외 처리를 한 번에 LoginFailureHandler로 처리하려고 했으나 TokenAuthenticationFilter는 OncePerRequestFilter를 상속받고 있어서 failureHandler을 설정할 수가 없어 TokenAuthenticationFilter 앞에 에러를 처리하는 필터를 추가해서 해결하였습니다.
  
<br>

+ 모든 코드에 대한 테스트 코드가 작성되어 있습니다.
+ Spring rest docs로 문서화되어 있습니다.
  - build 후 build/docs/asciidoc/index.html에서 확인 가능합니다.
  - 문서화 방법은 [여기](https://backtony.github.io/spring/2021-10-15-spring-test-3/)을 참고하세요.

<br>

+ OAuth2 소셜 로그인만 사용하는 코드는 [여기](https://github.com/backtony/spring-study/tree/master/spring-security-oauth2-jwt-redis)를 참고하세요.






