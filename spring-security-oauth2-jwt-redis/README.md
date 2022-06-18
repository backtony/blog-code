## spring security with OAuth2, JWT, Redis 

+ 회원가입 - OAuth2 - 카카오, 구글, 네이버
    + 핵심 로직은 CustomOauth2Service.class 참고하세요.
    + 요청은 /oauth2/authorization/google 또는 naver 또는 kakao가 기본값이지만 SecurityConfig.class에서 baseUri를 "/api/oauth2/authorization" 으로 설정했으므로 /api/oauth2/authorization/google , naver, kakao로 요청을 보내야 합니다.
+ 로그인에서 JWT통신을 받고 이후부터는 JWT토큰으로 통신합니다.  
    + AccessToken, RefreshToken 제공합니다.
    + JWT 토큰은 TokenAuthenticationFilter 에서 처리합니다.
    + 소셜 로그인은 CustomOauth2Service.class 에서 처리되고 LoginSuccessHandler 를 통해 response 값에 JWT 토큰이 담겨서 프론트로 응답합니다.
+ OAuth2에서 제공하는 서비스에서 OAuth2User를 반환해야 하는데 UserPrincipal.class에서 이를 구현하여 사용합니다.
+ Logout시 JWT accessToken과 refreshToken을 받아서 redis에 해당 토큰의 남은 시간을 TTL로 세팅하고 저장합니다.
    + JWT 로그인 처리를 진행하는 TokenAuthenticationFilter.class 에서 해당 토큰으로 로그인 시도하는 요청을 막습니다.
+ 인증 실패 시 RestAuthenticationEntryPoint.class 에서 처리합니다.
+ 인가 실패 시 RestAccessDeniedHandler.class 에서 처리합니다.
+ TokenAuthenticationFilter.class 에서 터지는 예외는 TokenAuthenticationErrorFilter에서 처리합니다.

실행 흐름
1. 클라이언트에서 소셜 로그인 선택하여 로그인 요청
2. 서버에서 요청에 맞는 소셜 로그인 URL 제공
3. 사용자는 scope에 정보 동의를 허용하고 로그인
4. 각 소셜 oauth는 oauth2를 등록할 때 작성했던 redirect_url로 응답을 리다이렉트
5. 스프링 oauth2 시큐리티를 사용 시 해당 컨트롤러를 자동으로 만들어 주는데 이는 resources/application-oauth.yml의 redirect-url을 확인
6. DefaultOAuth2UserService를 상속해서 구현한 customOauth2Service가 호출되여 로직 동작하는데 여기서 회원가입을 시킨다.
7. 성공 시 securityConfig에 oauth2Login에 등록한 LoginSuccessHandler를 통해 JWT 토큰을 응답값에 넣어 반환한다.

<br>

+ 모든 코드에 대한 테스트 코드가 작성되어 있습니다.
+ Spring rest docs로 문서화되어 있습니다. 
    - build 후 build/docs/asciidoc/index.html에서 확인 가능합니다.
    - 문서화 방법은 [여기](https://backtony.github.io/spring/2021-10-15-spring-test-3/)을 참고하세요.


<Br>

+ 고유 로그인까지 포함한 코드는 [여기](https://github.com/backtony/spring-study/tree/master/spring-security-oauth2-jwt-redis-with-native)를 참고하세요.
    
     
