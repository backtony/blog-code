package com.security.demospringsecurityform.account;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AccountControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    AccountService accountService;

    // mockmvc static import는 왜인지 잘 안나온다
    // get post의 경우 post 먼저 임포트 하고 *로 수정해서 사용하고
    // 나머지 perform에서 사용하는 메서드는 메서드를 적고 () 까지 적고 import를 해야한다

    @DisplayName("기본 화면 확인")
    @Test
    void index_anonymous() throws Exception{
        // 좀더 명시적으로 .with(anonymous())를 사용해도 된다.
        mockMvc.perform(get("/"))
                .andDo(print())
                .andExpect(status().isOk());
    }



    @DisplayName("admin 페이지 테스트")
    @Test
    void admin_user() throws Exception{
        mockMvc.perform(get("/admin").with(user("backtony").roles("USER")))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @DisplayName("애노테이션으로 사용하기")
    @Test
    @WithMockUser(username = "admin",roles = "ADMIN")
    void admin_admin() throws Exception{
        mockMvc.perform(get("/admin"))
                .andDo(print())
                .andExpect(status().isOk());
    }
    // 해당 애노테이션이 중복되면 그냥 애노테이션을 따로 만들자.
    // 애노테이션 만들고 사용할 애노테이션 붙여넣고
    // 런타임에도 사용하느 retension을 runtime까지 붙여주면 끝
    @DisplayName("가짜 유저가 이미 로그인 된 상태를 만들고 테스트")
    @Test
    @WithUser
    void index_user() throws Exception{

        // spring security test가 제공하는 user mockmvc것을 사용
        // user() 한다음 static import하면 나온다
        // 이미 해당 유저가 이미 로그인 한 상태라고 가정하고 테스트 진행
        mockMvc.perform(get("/"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @DisplayName("폼 로그인이 잘 되는지")
    @Test
    @Transactional
    void login() throws Exception{
        // 유저 하나 만들기
        String username = "backtony";
        String password = "123";
        Account account = createUser(username, password);

        // 해당 유저로 로그인 시도
        // password에서 주의해야 할것이 인코딩 되어있으므로 get으로 가져오면 인코딩된 패스워드임
        // 따라서 저장할 때 사용할던 패스워드 사용해야함
        mockMvc.perform(formLogin().user(account.getUsername()).password(password))
                .andExpect(authenticated()); // 인증 되는지

    }

    @DisplayName("폼 로그인 실패")
    @Test
    @Transactional
    void login_fail() throws Exception{
        // 유저 하나 만들기
        String username = "backtony";
        String password = "123";
        Account account = createUser(username, password);

        // 해당 유저로 로그인 시도
        // password에서 주의해야 할것이 인코딩 되어있으므로 get으로 가져오면 인코딩된 패스워드임
        // 따라서 저장할 때 사용할던 패스워드 사용해야함
        mockMvc.perform(formLogin().user(account.getUsername()).password("12345"))
                .andExpect(unauthenticated()); // 인증 되는지

    }

    private Account createUser(String username, String password) {
        Account account = new Account();
        account.setUsername(username);
        account.setPassword(password);
        account.setRole("USER");
        return accountService.createNew(account);

    }


}