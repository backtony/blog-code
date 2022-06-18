# Spring Security - 정리

## 1. 아키텍처 정리
---
![그림1](https://github.com/backtony/blog-code/blob/master/spring/img/security/1/1-1.PNG?raw=true)

1. 서블릿 컨테이너에 요청이 들어오면 DeligatingFilterProxy가 요청을 받는다. DeligatingFilterProxy는 스프링 부트를 사용하면 자동으로 등록된다. 사용하지 않을 경우 따로 설정 필요
2. DeligatingFilterProxy는 특정한 빈 이름으로 요청의 필터처리를 FilterChainProxy로 위임한다.
3. FilterChainProxy는 여러 필터들을 체인형태로 가지고 있다. 이런 체인은 WebSecurity와 HttpSecurity를 사용해서 만들어진다. 사용자가 WebSecurityConfigureAdapter을 상속받아 시큐리티 설정을 하는 것이 결국 WebSecurity를 만드는 것이고 WebSecurity를 바탕으로 필터 체인을 만드는 것이다. 
4. 필터들이 사용하는 주요한 객체들이 있다.
    
    - 인증
        - AuthenticationManager을 인터페이스로 사용하는데 구현체로 ProviderManager을 사용한다.
        - ProviderManager는 다른 여러 AuthenticationProvider를 사용해서 인증을 처리하는데 그중 하나가 DaoAuthenticationProvider이다.
        - DaoAuthenticationProvider는 UserDetailsService라는 DAO 인터페이스를 사용해서 데이터에서 읽어온 유저 정보와 사용자가 입력한 정보가 일치하는지 확인하여 인증 처리한다.
        - 인증이 성공하면 UserDetails 타입으로 반환하게 되고 UsernamePasswordAuthenticationFilter가 인증된 Authentication 객체( = UsernamePasswordAuthenticationToken) 를 SecurityContextHolder에 넣어준다.
        - 그리고 정보는 세션에 저장된다.
    - 인가
        - 앞선 필터들을 지나 FilterSecurityInterceptor가 AccessDecisionManger를 사용해서 현재 인증되어 있는 Authentication이 접근하려는 리소스에 접근이 가능한지 인가를 처리한다.
        - 기본적으로 AffirmativeBased 전략을 사용하는데 이는 하나의 Voter라도 허용하면 허용하는 것이다.
        - AffirmativeBased를 사용하는 WebExpressionVoter은 SecurityExpressionHandler를 사용해서 expression을 처리하는데 계층형 권한 구조를 위해서는 이 핸들러를 커스텀하면 된다.

<br>

## 2. Form Login 구조 인증처리
---
1. 사용자가 아이디(principal)와 패스워드(credentials)를 입력하면 UsernamePasswordAuthenticationToken을 만든다.
2. UsernamePasswordAuthenticationFilter 가 요청을 잡고 ProviderManager(AuthenticationManager의 구현체) -> DaoAuthenticationProvider -> DB에서 가져오도록 커스텀한 UserDetailsService를 통해 DB에서 해당 아이디를 조회해 정보를 가져온다.
3. ProviderManager(AuthenticationManager의 구현체)는 UserDetailsService를 통해 받은 유저정보와 로그인 시도시 받았던 아이디, 패스워드를 비교한다.
    - 참고로 AuthenticationManager는 authenticate 메서드 하나만 가지고 있고 인자로 authentication 객체를 받는데 이건 로그인 시도시 입력했던 아이디, 패스워드가 들어있다.
4. 사용자가 입력한 정보와 일치한다면 ProviderManager는 userDetailsService에서 리턴한 그 객체를 principle로 담아 authentication(토큰) 객체를 만들어 반환한다. 이때는 principal에 아이디가 아니라 UserDetails 타입의 객체가 들어간다.(해당 Account에 대한 정보가 들어있음)
5. UsernamePasswordAuthenticationFilter는 이 객체를 SecurityContextHolder에 Authentication을 넣어준다.

<br>


## 3. 기본적 설정 
---
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    // 필터를 적용하고 싶지 않은 요청들
    @Override
    public void configure(WebSecurity web) throws Exception {
        // static 자원 필터 제외
        web.ignoring().requestMatchers(PathRequest.toStaticResources().atCommonLocations());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // 시큐리티 설정 하는 작업
    }    
}
```
스프링 시큐리티을 사용하기 위해서는 일단 기본적으로 dependency를 추가하면 기본 초기 세팅으로 설정이 다 되어있다. 그럼 사용자는 WebSecurityConfigurerAdapter를 상속받아서 원하는 설정대로 세팅하면 된다.  
<br>

## 4. PasswordEncoder
---
기본적으로 패스워드를 저장할 때 문자열 그대로 저장하면 보안에 취약하기 때문에 인코딩을 해줘야 한다. 이 기능은 스프링 시큐리티가 제공한다.
```java
@Configuration
public class AppConfig {

    @Bean
    PasswordEncoder passwordEncoder(){
        // bcrypt라는 해시 함수를 이용해 패스워드를 암호화하는 클래스
        return new BCryptPasswordEncoder(); // 스프링 자체 제공 
    }
}
```
위의 빈을 추가해주고 서비스 계층에서 회원을 저장할 때 저장하기 전에 빈으로 등록한 passwordEncoder를 주입받고 passwordEncoder.encode(패스워드)로 인코딩 하고 저장하면 된다.  
<br>

## 5. DB에서 사용자 가져오도록 커스텀하기 - UserDetailsService
---
스프링 시큐리티에서 UserDetailsService 인터페이스는 DAO를 가지고 인증정보를 읽어와 인증하는 역할을 하고 실제로는 username을 받아와서 username에 해당하는 유저 정보를 DB에서 가져와 UserDetails 타입으로 변환해서 반환하는게 하는 일이다. 추가적인 시큐리티 설정 없이 UserDetailsService을 상속받아서 빈으로 등록해놓으면 자동으로 이 service를 찾아서 동작한다.
```java
@Service
public class AccountService implements UserDetailsService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AccountService(AccountRepository accountRepository, PasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = accountRepository.findByUsername(username); // DB에서 username(id)로 정보 꺼내오기

        // 해당 유저가 db에 없으면 예외 처리
        if(account == null){
            throw new UsernameNotFoundException(username);
        }

        /*
        내가 가지고 있는 정보는 Account 타입이므로 변환해서 반환해야한다.

        바꾸는데 Spring에서 편리하게 userdetails 클래스를 만들도록
        User라는 클래스를 제공한다
        return User.builder()
               .username(account.getUsername())
               .password(account.getPassword())
               .roles(account.getRole())
               .build();
        */

        // 컨트롤러에서 @AuthenticationPrincipal를 사용해서 SecurityContextHolder에 있는
        // Principle을 사용하기 위해서는 아래의 방식을 사용해야 한다.
        // 하나의 클래스 따로 만들기 -> 자세한 설명은 아래서
        return new UserAccount(account);
    }

// 사이를 엮기위한 클래스 만들기
// User 타입을 상속 받아서 사용
public class UserAccount extends User {
    // principle에서 나중에 account 꺼내서 사용하기 위해서 account 프로퍼티 생성
    private Account account;

    public UserAccount(Account account) {
        super(account.getUsername(), account.getPassword(), List.of(new SimpleGrantedAuthority("ROLE_"+account.getRole())));
        this.account=account;
    }

    public Account getAccount() {
        return account;
    }
}

// 실제 컨트롤러에서 사용
public String index(Model model, @AuthenticationPrincipal UserAccount userAccount){
        if(userAccount == null){
            model.addAttribute("message","hello spring security");
        } else{
            model.addAttribute("message","hello ," + userAccount.getAccount().getUsername());
        }
        return "index";
    }
```
컨트롤러에서 Principle 타입으로 인증 정보를 받으면 SecurityContext에 있는 정보가 아니라 사용할 것이 별로 없다. 따라서 SecurityContext에 있는 principle을 파라미터로 받아야 제대로 정보를 사용할 수 있다. 이때 사용하는 애노테이션이 @AuthenticationPrincipal 이다. @AuthenticationPrincipal는 securityContext에 있는 principle을 파라미터로 넣어준다. 하지만 principle에는 실제로는 UserDetails 타입이 들어있고 실상 우리가 사용하고자 하는 것은 account 엔티티일 것이다. 따라서 둘 사이를 엮어주는 하나의 클래스를 따로 만들 필요가 있다. 결론적으로 클래스를 하나 만들어서 위와 같이 사용하면 securityContext에는 principle에는 엮기 위해 만들어준 UserAccount가 들어가있게 되고 컨트롤러에서 사용할 때는 getaccount를 이용해서 account 엔티티를 사용할 수 있게 된다.  
그런데 userAccount에서 굳이 getAccount로 한번 더 꺼내야하기 때문에 귀찮다면 다음과 같이 애노테이션을 하나 만들어 처리한다.
```java
@Retention(RetentionPolicy.RUNTIME) // runtime시 까지 유지
@Target(ElementType.PARAMETER) // 파라미터에 사용할 것
// principle이 anonymousUser이면 null을 넣고, 아니면 principle의 account 프로퍼티를 넣어준다
@AuthenticationPrincipal(expression = "#this == 'anonymousUser' ? null : account")
public @interface CurrentUser {
}
```
애노테이션을 만들어주고 @AuthenticationPrincipal 대신 내가 만든 위 애노테이션을 사용하면 된다.  
<br>

## 6. 계층형 구조 커스텀 하기 - expressionHandler
---
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    AccountService accountService;
    
    @Override
    protected void configure(HttpSecurity http) throws Exception {        

       // 인가
        http.authorizeRequests()
                .mvcMatchers("/", "/info", "/account/**","/signup").permitAll()
                .mvcMatchers("/admin").hasRole("ADMIN")
                .mvcMatchers("/user").hasRole("USER")
                .anyRequest().authenticated()
                // 계층구조 핸들러 추가
                .expressionHandler(securityExpressionHandler());
    }

    // 인가에서는 사용자마다 권한 계층이 있을 거고 그걸 이해시켜야한다.
    // AccessDecisionManager가 그 역할을 한다.
    // 설정으로 주지 않으면 기본적으로 만든 AccessDecisionManager을 사용하는데
    // AffirmativeBased 기본 방식으로 여러 voter중 하나의 voter만 허용하면 허용하는 방식이다.
    // 그럼 기본적으로 들어가는 AccessDecisionManager을 내가 만들어서 그걸 config에 추가해주면 내가 만든대로 동작한다.

    // AccessDecisionManager는 voter를 파라미터로 받고
    // voter은 handler을 파라미터로 받는다.
    // handler는 RoleHierarchyImpl를 파라미터로 받는다.
    // RoleHierarchyImpl는 계층 구조를 담고 있다.

    // 구조를 보면 사실상 똑같은 AccessDecisionManager를 넣는다.
    // 따라서 굳이 새로 만들어줄 필요는 없고
    // 기존에 들어가던 핸들러에 추가적으로 계층설정만 해주면 된다.

    public SecurityExpressionHandler securityExpressionHandler (){

        // 핸들러에 들어갈 계층구조
        RoleHierarchyImpl roleHierarchy = new RoleHierarchyImpl();
        roleHierarchy.setHierarchy("ROLE_ADMIN > ROLE_USER");

        // DefaultWebSecurityExpressionHandler가 기존에 사용하던 핸들러인데
        // 여기다가 계층 구조만 추가
        DefaultWebSecurityExpressionHandler handler = new DefaultWebSecurityExpressionHandler();
        handler.setRoleHierarchy(roleHierarchy); // 핸들러에 계층구조 담기

        return handler; // 핸들러 반환
    }
}
```
인가는 사용자마다 권한 계층이 있을 것이고 그걸 이해시키기 위해서는 따로 커스텀해야 한다. AccessDecisionManager가 그 역할을 한다. 설정을 따로 주지 않으면 기본적으로 만들어진 AccessDecisionManager을 사용하는데 AffirmativeBased 방식으로 여러 voter중 하나의 voter만 허용하면 허용하는 방식이다. 이 방식은 그대로 유지하고 계층구조만 이해시키고 싶다면 핸들러만 만들어주고 Config에서 추가해주면 된다.

<br>




## 7. 시큐리티 테스트
---
```
<dependency>
<groupId>org.springframework.security</groupId>
<artifactId>spring-security-test</artifactId>
<scope>test</scope>
<version>${spring-security.version}</version>
</dependency>
```
위의 의존성을 추가하고 진행한다. 참고로 버전이 적혀있지 않은 경우 < version >${spring-security.version}</ version > 로 설정을 해두면 스프링 부트가 버전이 바뀌면 알아서 바뀐다.  
왜인지 모르겠지만 mockMvc를 사용할 때 static import를 찾기가 어렵다. 일단 post()를 작성하고 static import를 한 뒤에 위에서 *으로 수정해서 사용하도록 한다. 또한 mock에서 지원하는 것은 일단 ()까지 작성하고 static import하면 적용된다. 왜인지는 아직 모르겠다.
```java
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AccountControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    AccountService accountService;

    // mockmvc static import는 왜인지 잘 안나온다
    // get post의 경우 post 먼저 임포트 하고 *로 수정해서 사용하고
    // 나머지 perform에서 사용하는 메서드는 메서드를 적고 () 까지 적고 import를 해야한다

    @DisplayName("admin 페이지 테스트")
    @Test
    void admin_user() throws Exception{
        // 가상의 유저 만들어서 테스트
        mockMvc.perform(get("/admin").with(user("backtony").roles("USER")))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @DisplayName("애노테이션으로 사용하기")
    @Test
    // with으로 이어나가기 귀찮다면 애노테이션을 사용
    @WithMockUser(username = "admin",roles = "ADMIN")
    void admin_admin() throws Exception{
        mockMvc.perform(get("/admin"))
                .andDo(print())
                .andExpect(status().isOk());
    }
    
    // 가상사용자 사용이 빈번할 경우 애노테이션으로 따로 만들고 사용
    /*
    @Retention(RetentionPolicy.RUNTIME) // 런타임까지 유지, 런타임에 애노테이션 참고하기 때문
    @WithMockUser(username = "backtony",roles = "USER")
    public @interface WithUser {
    }
    */
    @DisplayName("가짜 유저가 이미 로그인 된 상태를 만들고 테스트")
    @Test
    @WithUser
    void index_user() throws Exception{
        mockMvc.perform(get("/"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @DisplayName("폼 로그인이 잘 되는지")
    @Test
    void login() throws Exception{
        // 유저 하나 만들기
        String username = "backtony";
        String password = "123";
        Account account = createUser(username, password);

        // 해당 유저로 로그인 시도
        // password에서 주의해야 할것이 인코딩 되어있으므로 get으로 가져오면 인코딩된 패스워드임
        // 따라서 저장할 때 사용할던 패스워드 사용해야함
        mockMvc.perform(formLogin().user(account.getUsername()).password(password))
                .andExpect(authenticated()); // 인증 되는지 , 인증 실패는 unauthenticated()

    }

    private Account createUser(String username, String password) {
        Account account = new Account();
        account.setUsername(username);
        account.setPassword(password);
        account.setRole("USER");
        return accountService.createNew(account);
    }

    @DisplayName("회원가입")
    @Test
    void processSignup() throws Exception{
        // param은 perform () 안에 넣는 것이다. 혼동하지 말자
        mockMvc.perform(post("/signup")
                .param("username","backtony")
                .param("password","123")
                .with(csrf())) // csrf 넣어서 post요청 보내기
                .andDo(print())
                .andExpect(status().is3xxRedirection()); // redirect 되는지 확인

    }
}
```
+ param 으로 파라미터 줄 수 있음
+ with 를 사용해서 가상의 유저를 만들어 놓고 테스트 가능
+ @WithMockUser 애노테이션으로 간소화 가능 -> 파라미터가 없다면 그냥 가상 사용자로 만들어진다.
+ 자주 사용되는 가상 유저는 애노테이션으로 만들고 사용
+ formLogin으로 폼 로그인 테스트 

<br>

## 8. ThreadLocal
---
SecurityContextHolder는 같은 쓰레드 내에서만 공유한다. 따라서 같은 쓰레드라면 해당 데이터를 메서드 매개변수로 넘겨줄 필요 없이 SecurityContextHolder에서 Authentication을 꺼내 사용할 수 있다.  
<br>

## 9. FilterChainProxy
---
![그림2](https://github.com/backtony/blog-code/blob/master/spring/img/security/1/1-2.PNG?raw=true)

1. WebAsyncManagerIntergrationFilter
2. SecurityContextPersistenceFilter
3. HeaderWriterFilter
4. CsrfFilter
5. LogoutFilter
6. UsernamePasswordAuthenticationFilter
7. DefaultLoginPageGeneratingFilter
8. DefaultLogoutPageGeneratingFilter
9. BasicAuthenticationFilter
10. RequestCacheAwareFtiler
11. SecurityContextHolderAwareReqeustFilter
12. AnonymouseAuthenticationFilter
13. SessionManagementFilter
14. ExeptionTranslationFilter
15. FilterSecurityInterceptor

FilterChainProxy는 15가지의 필터를 호출한다. 위의 15개의 필터 전에 DelegatingFilterProxy 부터 살펴보자.  

### DelegatingFilterProxy
![그림3](https://github.com/backtony/blog-code/blob/master/spring/img/security/1/1-3.PNG?raw=true)

+ 일반적인 서블릿 필터
+ 서블릿 필터 처리를 스프링에 들어있는 빈으로 위임할 때 사용하는 서블릿 필터
+ 타켓 빈 이름을 설정
+ FilterChainProxy는 보통 springSecurityFilterChain 이라는 이름의 빈으로 등록

<br>

### 필터 정리
+ WebAsyncManagerIntegrationFilter : 스프링 MVC의 Async 기능(핸들러에서 Callable을 리턴할 수 있는 기능)을 사용할 때에도 SecurityContext를 공유하도록 도와주는 필터.
+ SecurityContextPersistenceFilter : SecurityContextRepository를 사용해서 기존의 SecurityContext를 읽어오거나 초기화 한다. http session에 기존에 만들어져 있던 securityContext를 읽어오거나 없다면 비어있는 security context를 만든다.
+ HeaderWriterFilter : 응답 헤더에 시큐리티 관련 헤더를 추가해주는 필터
+ CsrfFilter : 인증된 유저의 계정을 사용해 악의적인 변경 요청을 만들어 내는 기법인 CSRF 어택을 방지하는 필터
+ LogoutFilter : 로그아웃 처리하는 필터
+ UsernamePasswordAuthenticationFilter : 폼 로그인을 처리하는 인증 필터 
+ DefaultLoginPageGeneratingFilter : 기본 로그인 페이지를 생성해주는 필터
+ DefaultLogoutPageGeneratingFilter : 기본 로그아웃 페이지를 생성해주는 필터
+ BasicAuthenticationFilter : 요청 헤더에 username와 password를 실어 보내면 브라우저 또는 서버가 그 값을 읽어서 인증하는 방식으로 브라우저 기반 요청이 클라이언트의 요청을 처리할 때 자주 사용하지만 보안에 취약해 HTTPS 사용을 권장
+ RequestCacheAwareFilter : 현재 요청과 관련 있는 캐시된 요청이 있는지 찾아서 적용하는 필터로 캐시된 요청이 없다면 현재 요청을 처리하고 있다면 해당 캐시된 요청을 처리한다.
+ SecurityContextHolderAwareRequestFilter : 시큐리티 관련 서블릿 API를 구현해주는 필터
+ AnonymousAuthenticationFilter : 현재 SecurityContext에 Authentication이 null이면 “익명 Authentication”을 만들어 넣어주고,
null이 아니면 아무일도 하지 않는다.
+ SessionManagementFilter : 세션 관리 필터
    - 세션 변조 방지 전략 설정 : sessionFixation
        - 인증하면 세션을 바꾸는 것
        - migrateSession 서블릿 3.0- 컨테이너 사용시 기본값 -> 인증시 새로운 세션으로 바꿈
        - changeSessionId 서블릿 3.1+ 컨테이너 사용시 기본값 -> 세션 아이디를 바꿈
    - invalidSessionUrl : 유효하지 않은 세션을 리다이렉트 시킬 url 설정 -> 로그아웃 했을 때 그 기존 세션은 invalid되므로 어디로 보낼지 정하는것
    - 동시성 제어 : maximumSessions - 추가 로그인을 막을지 여부 설정(기본설정 false)
        - 예를 들면 크롬, ex, 파이어폭스 여러 개로 로그인
        - 하나로 설정하고 싶으면 maximumSession(1)
        - expiredUrl 로 만약 다음 세션이 들어오면 이전 세션 사용하던 것은 어디 url로 보낼지 설정 가능
+ ExceptionTranslationFilter : 인증, 인가 에러 처리를 담당하는 필터
    - AuthenticationEntryPoint
    - AccessDeniedHandler
+ FilterSecurityInterceptor : HTTP 리소스 시큐리티 처리를 담당하는 필터. AccessDecisionManager를 사용하여 인가를 처리한다. 
    - FilterSecurityInterceptor 처리 중에 2가지 발생 가능
        - AuthenticationException -> 익명사용자라 인증 자체가 안되어있음 -> AuthenticationEntryPoint를 사용해서 예외 처리 -> 해당 유저를 인증이 가능한 페이지로 보냄
        - AccessDeniedException -> 인증은 되어있는데 권한이 없다 -> AccessDeniedHanlder에게 위임 -> 기본 처리는 403 에러 메시지 보여줌
    - AuthenticationException는 유저를 인증 가능 페이지로 보내니 커스텀이 필요 없지만 AccessDeniedException는 에러 페이지를 보여주므로 커스텀이 필요
    - 참고로 UsernamePasswordAuthenticationFilter에서 로그인시 발생한 실패는 이쪽에서 처리하는게 아니라 UsernamePasswordAuthenticationFilter에서 세션에 에러를 담아두고 그 에러메시지를 기반으로 DefaultLoginPageGeneratingFilter가 해당 에러와 같이 화면을 보여준다.
+ RememberMeAuthenticationFilter : 세션이 사라지거나 만료가 되더라도 쿠키 또는 DB를 사용하여 저장된 토큰 기반으로 인증을 지원하는 필터
    - 토큰 기반 인증 필터로 추가하면 SecurityContextHolderAwareRequestFilter 다음 순서로 들어간다. 
    - rememberauthenticationtoken이 있으면 그걸로 인증하고 securitycontextholder에 해당 authentication을 넣어준다.

<br>

## 10. SecurityConfig
---
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    AccountService accountService;

    // 필터를 적용하고 싶지 않은 요청들
    @Override
    public void configure(WebSecurity web) throws Exception {
        // static 자원 필터 제외
        web.ignoring().requestMatchers(PathRequest.toStaticResources().atCommonLocations());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // 모든 작업을 굳이 굳이 메서드 체이닝을 하지 않아도 된다. 아래처럼 http를 나눠서 사용하는게 가독성이 좋다.

        // 인가
        http.authorizeRequests()
                .mvcMatchers("/", "/info", "/account/**","/signup").permitAll()
                .mvcMatchers("/admin").hasRole("ADMIN")
                .mvcMatchers("/user").hasRole("USER")
                .anyRequest().authenticated()
                .expressionHandler(securityExpressionHandler()); // 계층구조 추가

        http.logout()
                .logoutUrl("/logout") // logout url 페이지 설정
                .logoutSuccessUrl("/"); // 로그아웃 성공시 이동할 url

        // AuthenticationException은 커스텀 할 필요가 없지만
        // AccessDeniedException은 화면에 에러 페이지를 보내주므로 이건 커스텀해주는게 좋다.
        //http.exceptionHandling()
        //        .accessDeniedPage("/access-denied"); // denied되었을 때 보여줄 페이지
        // 위와 같이 코딩하면 서버단에서는 사실 누가 계속 나쁜일을 시도하는지 확인이 안됨
        // 서버단에 로그를 남기는 것이 좋은 선택
        // 이때는 handler을 만들어 넣어주는게 좋다.

        http.exceptionHandling()
                // 별도의 클래스를 만들어 빈으로 등록해서 사용하는게 좋으나 지금은 그냥 여기다 씀
                .accessDeniedHandler(new AccessDeniedHandler() {
                    @Override
                    public void handle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AccessDeniedException e) throws IOException, ServletException {
                        UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                        String username = principal.getUsername();
                        // 실제는 log사용해야하지만 지금은 그냥 system 찍음
                        System.out.println("username = " + "is denied to access" + httpServletRequest.getRequestURI());
                        httpServletResponse.sendRedirect("/access-denied"); // AccessDeniedException 발생시 이동하는 URI
                    }
                });


        
        // 인증
        http.formLogin()
                // 로그인 페이지를 설정해주는 것인데 아래와 같이 로그인 페이지 url을 설정하면
                // 커스텀한 로그인 로그아웃 페이지를 사용한다고 가정하고
                // DefaultLoginPageGeneratingFilter 와 
                // DefaultLoginOutPageGeneratingFilter가 추가되지 않는다.
                .loginPage("/login")
                .permitAll(); // 로그인 페이지 permit all

        http.httpBasic();

        http.rememberMe()
                .userDetailsService(accountService) // 사용할 service세팅
                // 리멤버미 기능으로 사용할 쿠키 이름 설정
                .key("remember-me-sample"); // html에는 remember-me로 키를 줬는데 boolean으로 넘어오므로 매개변수의 이름이 달라도 상관 없다고 한다
}
```



<Br><Br>

__참고__  
<a href="https://www.inflearn.com/course/%EB%B0%B1%EA%B8%B0%EC%84%A0-%EC%8A%A4%ED%94%84%EB%A7%81-%EC%8B%9C%ED%81%90%EB%A6%AC%ED%8B%B0" target="_blank"> 스프링 시큐리티</a>   



