package com.springsecurity.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.springsecurity.authentication.application.AuthCommandUseCase;
import com.springsecurity.authentication.presentation.AuthRestController;
import com.springsecurity.config.SecurityTestConfig;
import com.springsecurity.support.restdocs.DocsController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@Import(SecurityTestConfig.class)
@WebMvcTest({
        AuthRestController.class,
        DocsController.class
})
public abstract class ControllerTest {

    protected MockMvc mockMvc;

    @Autowired protected ObjectMapper objectMapper;

    @MockBean protected AuthCommandUseCase authCommandUseCase;

    protected String createJson(Object dto) throws JsonProcessingException {
        return objectMapper.writeValueAsString(dto);
    }
}
