package com.springsecurity.support.restdocs;

import com.springsecurity.config.RestDocsConfig;
import com.springsecurity.support.ControllerTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@Disabled
@Import(RestDocsConfig.class)
@ExtendWith(RestDocumentationExtension.class)
public abstract class RestDocsTestSupport extends ControllerTest {

    @Autowired
    protected RestDocumentationResultHandler restDocumentationResultHandler;

    @BeforeEach
    void setUp(final WebApplicationContext context,
               final RestDocumentationContextProvider provider) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(MockMvcRestDocumentation.documentationConfiguration(provider))
                .apply(springSecurity())
                .alwaysDo(MockMvcResultHandlers.print()) // 콘솔창 출력용도인것같다.
                .alwaysDo(restDocumentationResultHandler)
                .addFilters(new CharacterEncodingFilter("UTF-8", true))
                .build();
    }

    protected List<FieldDescriptor> errorDescriptors() {
        return new ArrayList<>(List.of(
                fieldWithPath("message").description("에러 메시지"),
                fieldWithPath("code").description("에러 코드"),
                fieldWithPath("errors").description("Error 값 배열 값")
        ));
    }

    public static List<FieldDescriptor> errorDescriptorIncludeErrorFields() {
        return new ArrayList<>(List.of(
                fieldWithPath("message").description("에러 메시지"),
                fieldWithPath("code").description("에러 코드"),
                fieldWithPath("errors").description("Error 값 배열 값"),
                fieldWithPath("errors[0].field").description("에러 필드명"),
                fieldWithPath("errors[0].value").description("에러 필드값"),
                fieldWithPath("errors[0].reason").description("에러 이유")
        ));
    }
}
