package com.security.demospringsecurityform.config;


import com.security.demospringsecurityform.account.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.expression.SecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.access.vote.AffirmativeBased;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.expression.DefaultWebSecurityExpressionHandler;
import org.springframework.security.web.access.expression.WebExpressionVoter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

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
        // 인가
        http.authorizeRequests()
                .mvcMatchers("/", "/info", "/account/**","/signup").permitAll()
                .mvcMatchers("/admin").hasRole("ADMIN")
                .mvcMatchers("/user").hasRole("USER")
                .anyRequest().authenticated()
                .expressionHandler(securityExpressionHandler());

        http.logout()
                .logoutUrl("/logout") // logout url 페이지 설정
                .logoutSuccessUrl("/"); // 로그아웃 성공시 이동할 url

        // ExceptionTranslatorFilter -> FilterSecurityInterceptor
        // FilterSecurityInterceptor 처리 중에 2가지 발생 가능
        // AuthenticationException -> 인증 자체가 안되어있음 -> AuthenticationEntryPoint를 사용해서 예외 처리
        // 해당 유저를 인증이 가능한 페이지로 보냄
        // AccessDeniedException -> 인증은 되어있는데 권한이 없다 -> AccessDeniedHanlder 기본 처리는 403 에러 메시지 보여줌
        // AuthenticationException는 딱히 커스텀할 필요가 없다. 인증이 안되면 인증하도록 페이지로 보내주기때문
        // 하지만 AccessDeniedException은 화면에 에러 페이지를 보내주므로 이건 커스텀해주는게 좋다.
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
                        // 실제는 log사용 지금은 그냥 system 찍음
                        System.out.println("username = " + "is denied to access" + httpServletRequest.getRequestURI());
                        httpServletResponse.sendRedirect("/access-denied"); // AccessDeniedException 발생시 이동하는 URI
                    }
                });


        // 굳이 메서드 체이닝을 하지 않아도 된다.
        // 인증
        http.formLogin()
                // 로그인 페이지를 설정해주는 것인데 이렇게 설정하면
                // 커스텀한 로그인 로그아웃 페이지를 사용한다고 가정하고
                // DefaultLoginPageGeneratingFilter 와 DefaultLoginOutPageGeneratingFilter 추가되지 않는다.
                .loginPage("/login")
                .permitAll(); // 로그인 페이지 permit all

        http.httpBasic();

        http.rememberMe()
                .userDetailsService(accountService) // 사용할 service세팅
                // 리멤버미 기능으로 사용할 쿠키 이름 설정
                .key("remember-me-sample"); // html에는 remember-me로 키를 줬는데 boolean으로 넘어오므로 매개변수의 이름이 달라도 상관 없다고 한다




        // http.sessionManagement() sessionManagementFilter 관련 설정




        // service 계층에서 메서드에 @Async 애노테이션을 붙이면
        // 해당 서비스는 새로운 하위 스레드를 만들어 진행하게 된다.
        // 기본적으로 SecurityContext는 ThreadLocal으로 하나의 스레드에서만 공유되는데
        // application에 @EnableAsync 애노테이션을 붙이고 여기서 다음과 같이 설정하면
        // 같은 securityContext를 사용할 수 있다.
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);


    }

    // 인가에서는 사용자마다 권한 계층이 있을 거고 그걸 이해시켜야한다.
    //  AccessDecisionManager가 그 역할을 한다.
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

        return handler; // AffirmativeBased방식에 계층이해하는 voter을 추가해서 AccessDecisionManager를 만들었다.
    }
}


/**
 *  filter 순서대로 정리
 *  필터 정리 끝나면 전체 아키텍쳐 정리랑 필터 정리해서 포스팅하자
 */

/**
 * WebAsyncManagerIntegrationFilter
  */

// mvc의  async 기능(핸들러에서 callable을 리턴할 수 있는 기능)을 사용할 때에도
// 같은 SecurityContext를 공휴하도록 도와주는 필터
// Preprocess로 securitycontext를 설정하고 postprocess로 securitycontext를 clean up 한다.

/**
 *  SecurityContextPersistenceFilter
 */

// SecurityContextRepository를 사용해서 기존의 SecurityContext를 읽어오거나 초기화한다.
// http session에서 기존에 만들어져있던 security context를 읽어오거나 없다면 비어있는 security context를 만든다.
// 기본적으로 사용한는 전략은 HTTP Seesion을 사용한다.

/**
 * HeaderWriterFilter
 */

// 응답 헤더에 시큐리티 관련된 헤더를 자동으로 추가해주는 필터
// 거의 만질일이 없는 필터

/**
 * CsrfFilter
 */

// CSRF 어택 방지 필터
// 인증된 유저의 계정을 사용해 악의적인 변경 요청을 만들어 내는 기법
// 의도한 사용자만 리소스를 변경할 수 있도록 허용하는 필터 csrf 토큰을 사용하여 방지

/**
 * LogoutFilter
 */

// 로그아웃을 처리하는 필터
// /logout url에서 나오는 화면은 DefaultLogoutPageGeneratingFilter가 하는 것이고
// logout을 버튼을 클릭해야 LogoutFilter가 처리한다. logout은 post요청으로 나간다.

/**
 * UsernamePasswordAuthenticationFilte
 */

// 폼 로그인을 처리하는 인증 필터
// 사용자가 폼에 입력한 username, password로 Authentcation 토큰을 만들고
// AuthenticationManager를 사용하여 인증 시도
// AuthenticationManaget(ProviderManager)는 여러 AuthenticationProvider을 사용하여 인증 시도
// 그 중에서 DaoAuthenticationProvider는 UserDetailsService를 사용하여 UserDetails정보를 가져와
// 사용자가 입력한 password와 비교

/**
 *  DefaultLoginPageGeneratingFilter와 DefaultLogoutPageGeneratingFilter
 */
// 기본 로그인, 로그아웃 폼 페이지를 생성해주는 필터

/**
 * BasicAuthenticationFilter
 */

// 요청 헤더에 username와 password를 실어 보내면 브라우저 또는 서버가 그 값을 읽어서 인증하는 방식
// 예) Authorization: Basic QWxhZGRpbjpPcGVuU2VzYW1l (keesun:123 을 BASE 64)
// 보통 브라우저 기반 요청이 클라이언트의 요청을 처리할 때 자주 사용
// 보안에 취약하기 때문에 반드시 https 사용 권장


/**
 * requestCacheAwareFilter
 */

// 현재 요청과 관련있는 캐시된 요청이 있는지 찾아서 적용하는 필터
// 캐시된 요청이 없다면 현재 요청 처리
// 캐시된 요청이 있다면 해당 캐시된 요청 처리
// 예를 들면 dashboard에 요청했는데 로그인을 해야했고 로그인하면 원래 요청했던 dashboard로 이동할 수 있게됨
// 이때 쓰이는것
// 캐시는 세션에 저장해놓고 쓴다


/**
 * SecurityContextHolderAwareRequestFilter
 */

// 시큐리티 관련 서블릿 API를 구현해주는 필터

/**
 * AnonymousAuthenticationFilter
 */

// 현재 SecutiryContext에 Authentication이 null이면 익명 Authentication을 만들어서 넣어주고
// null이 아니면 아무일도 하지 않는다.
// 기본적으로 Authentication에 Principal은 anonymousUser가 기본값

/**
 * SessionManagementFilter
 */

// 세션 변조 방지 전략 : seesionFixation
// 인증하면 세션을 바꿔버림
// migrateSession 서블릿 3.0- 컨테이너 사용시 기본값 -> 인증시 새로운 세션으로 바꿈
// changeSessionId 서블릿 3.1+ 컨테이너 사용시 기본값 -> 세션 아이디를 바꿈
// invalidSessionUrl : 유효하지 않은 세션을 리다이렉트 시킬 url 설정 -> 로그아웃 했을 때 그 기존 세션은 invalid되므로 어디로 보낼지 정하는것
// 동시성 제어 : maximumSessions - 추가 로그인을 막을지 여부 설정(기본설정 false)
    // 예를 들면 크롬, ex, 파이어폭스 여러 개로 로그인
    // 하나로 설정하고 싶으면 maximumSession(1)
    // expiredUrl 로 만약 다음 세션이 들어오면 이전 세션 사용하던 것은 어디 url로 보낼지 설정 가능

// 세션 생성 전략 sessionCreatePolicy

/**
 * exceptionTranslationFilter
 */


//
//인증, 인가 에러 처리를 담당하는 필터
//       AuthenticationException 발생시  AuthenticationEntryPoint로 예외 처리
//       AccessDeniedException 발생시  AccessDeniedHandler로 예외 처리

/**
 * FilterSecurityInterceptor
 */

// HTTP 리소스 시큐리티 처리를 담당하는 필터, AccessDecisionManager를 사용하여 인가를 처리한다.


/**
 *  RememberMeAuthenticationFilter
 */

// 추가할 경우
// SecurityContextHolderAwareRequestFilter 다음에 들어간다
//  rememberauthenticationtoken이 있으면 그걸로 인증하고 securitycontextholder에 해당 authentication을 넣어준다
// 세션이 사라지거나 만료가 되더라도 쿠키 또는 DB를 사용하여 저장된 토큰 기반으로 인증을 지원하는 필터


/**
 * 커스텀 필터 추가하기
 */

// 필터 만들고
// securityconfig에서 http.addFilter 를 사용하면 된다.

