package com.security.demospringsecurityform.form;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
class SampleControllerTest {

    @Autowired
    MockMvc mockMvc;

    @DisplayName("form으로 로그인 가능한가")
    @Test
    void signupForm() throws Exception{
        mockMvc.perform(get("/signup"))
                .andExpect(status().isOk())
                .andDo(print())
                // 본문 내용에 csrf있는지 확인
                .andExpect(content().string(containsString("_csrf")));
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