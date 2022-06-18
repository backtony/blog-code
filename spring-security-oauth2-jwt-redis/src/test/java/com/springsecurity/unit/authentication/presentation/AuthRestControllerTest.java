package com.springsecurity.unit.authentication.presentation;

import com.springsecurity.authentication.application.dto.AuthResponseDto;
import com.springsecurity.support.restdocs.RestDocsTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.security.test.context.support.WithMockUser;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.payload.JsonFieldType.STRING;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AuthRestControllerTest extends RestDocsTestSupport {

    private final String URL = "/api/v1/";
    private final String REFRESH_HEADER = "refreshToken";
    private final String ACCESS_TOKEN = "Bearer accessToken";
    private final String REFRESH_TOKEN = "Bearer refreshToken";

    @Test
    @WithMockUser(roles = "USER")
    void 토큰_재발급() throws Exception{
        //given
        AuthResponseDto authResponseDto = AuthResponseDto.of("accessToken", "refreshToken");

        given(authCommandUseCase.reissue(anyString()))
                .willReturn(authResponseDto);

        //when then docs
        mockMvc.perform(RestDocumentationRequestBuilders.post(URL+"reissue")
                .header(HttpHeaders.AUTHORIZATION,REFRESH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(restDocumentationResultHandler.document(
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer 타입 Refresh Token")
                        ),
                        responseFields(
                                fieldWithPath("tokenType").type(STRING).description("토큰 타입"),
                                fieldWithPath("accessToken").type(STRING).description("JWT Access Token"),
                                fieldWithPath("refreshToken").type(STRING).description("JWT Refresh Token")
                        )
                ));
    }

    @Test
    void 유효하지_않은_토큰으로_재발급_시도() throws Exception{
        // when then docs
        mockMvc.perform(RestDocumentationRequestBuilders.post(URL+"reissue")
                .header(HttpHeaders.AUTHORIZATION,REFRESH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andDo(restDocumentationResultHandler.document(
                        responseFields(
                            errorDescriptors()
                        )
                ));
    }

    @Test
    void Authorization_헤더_없이_토큰_재발급_시도() throws Exception{
        // when then docs
        mockMvc.perform(RestDocumentationRequestBuilders.post(URL+"reissue")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andDo(restDocumentationResultHandler.document(
                        responseFields(
                                errorDescriptors()
                        )
                ));
    }


    @Test
    @WithMockUser(roles = "USER")
    void 로그아웃() throws Exception{

        // when then
        mockMvc.perform(RestDocumentationRequestBuilders.post(URL+"logout")
                .header(HttpHeaders.AUTHORIZATION,ACCESS_TOKEN)
                .header(REFRESH_HEADER,REFRESH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(restDocumentationResultHandler.document(
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer 타입 Access Token"),
                                headerWithName(REFRESH_HEADER).description("Bearer 타입 refresh Token")
                        )
                ))
        ;
    }

    @Test
    void 유효하지_않은_토큰으로_로그아웃_시도() throws Exception{
        // when then
        mockMvc.perform(RestDocumentationRequestBuilders.post(URL+"logout")
                .header(HttpHeaders.AUTHORIZATION,ACCESS_TOKEN)
                .header(REFRESH_HEADER,REFRESH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andDo(restDocumentationResultHandler.document(
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer 타입 Access Token"),
                                headerWithName(REFRESH_HEADER).description("Bearer 타입 refresh Token")
                        ),
                        responseFields(
                                errorDescriptors()
                        )
                ))
        ;
    }

    @Test
    void Authorization헤더_없이_로그아웃_시도() throws Exception{
        // when then
        mockMvc.perform(RestDocumentationRequestBuilders.post(URL+"logout")
                .header(REFRESH_HEADER,REFRESH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
        ;
    }

    @Test
    void refreshToken헤더_없이_로그아웃_시도() throws Exception{
        // when then
        mockMvc.perform(RestDocumentationRequestBuilders.post(URL+"logout")
                .header(HttpHeaders.AUTHORIZATION,ACCESS_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
        ;
    }

}
