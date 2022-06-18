package com.springsecurity.unit.authentication.presentation;

import com.springsecurity.authentication.application.dto.AuthResponseDto;
import com.springsecurity.authentication.presentation.dto.SignUpRequest;
import com.springsecurity.config.security.dto.LoginRequest;
import com.springsecurity.support.restdocs.RestDocsTestSupport;
import com.springsecurity.user.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.Optional;

import static com.springsecurity.support.UserGivenHelper.givenLocalUser;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.payload.JsonFieldType.STRING;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
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
                        responseAuthResponse()
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

    @Test
    void 회원가입_성공() throws Exception{
        //given
        SignUpRequest signUpRequest = SignUpRequest.builder()
                .name("backtony")
                .email("backtony@gmail.com")
                .password("password")
                .build();

        //when then
        mockMvc.perform(RestDocumentationRequestBuilders.post(URL+"sign-up")
                .content(createJson(signUpRequest))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(restDocumentationResultHandler.document(
                        requestFields(
                                fieldWithPath("name").description("실명"),
                                fieldWithPath("email").description("이메일"),
                                fieldWithPath("password").description("비밀번호")
                        )
                ))
        ;
    }

    @Test
    void 이름_없이_회원가입_시도_실패() throws Exception{
        //given
        SignUpRequest signUpRequest = SignUpRequest.builder()
                .email("backtony@gmail.com")
                .password("password")
                .build();

        //when then
        mockMvc.perform(RestDocumentationRequestBuilders.post(URL+"sign-up")
                .content(createJson(signUpRequest))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
        ;
    }

    @Test
    void 패스워드_없이_회원가입_시도_실패() throws Exception{
        //given
        SignUpRequest signUpRequest = SignUpRequest.builder()
                .name("backtony")
                .email("backtony@gmail.com")
                .build();

        //when then
        mockMvc.perform(RestDocumentationRequestBuilders.post(URL+"sign-up")
                .content(createJson(signUpRequest))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
        ;
    }

    @Test
    void 이메일_없이_회원가입_시도_실패() throws Exception{
        //given
        SignUpRequest signUpRequest = SignUpRequest.builder()
                .password("password")
                .name("backtony")
                .build();

        //when then
        mockMvc.perform(RestDocumentationRequestBuilders.post(URL+"sign-up")
                .content(createJson(signUpRequest))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
        ;
    }

    @Test
    void 이메일이_아닌_형식으로_회원가입_시도_실패() throws Exception{
        //given
        SignUpRequest signUpRequest = SignUpRequest.builder()
                .password("password")
                .email("backtony")
                .name("backtony")
                .build();

        //when then
        mockMvc.perform(RestDocumentationRequestBuilders.post(URL+"sign-up")
                .content(createJson(signUpRequest))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
        ;
    }

    @Test
    void 서비스_고유_로그인_성공() throws Exception{
        // given
        String password = "password";
        User user = givenLocalUser(passwordEncoder,password);
        LoginRequest loginRequest = new LoginRequest(user.getEmail(), password);

        given(userRepository.findByEmailAndAuthProvider(any(),any())).willReturn(Optional.of(user));
        given(passwordEncoder.matches(any(),any())).willReturn(true);
        given(tokenProvider.createAccessToken(any())).willReturn("accessToken");
        given(tokenProvider.createRefreshToken(any())).willReturn("refreshToken");

        // when then docs
        mockMvc.perform(RestDocumentationRequestBuilders.post(URL+"login")
                .content(createJson(loginRequest))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(restDocumentationResultHandler.document(
                        requestFields(
                                fieldWithPath("email").description("이메일"),
                                fieldWithPath("password").description("비밀번호")
                        ),
                        responseAuthResponse()
                ))
        ;
    }

    @Test
    void 서비스_고유_로그인_실패() throws Exception{
        // given
        User user = givenLocalUser(passwordEncoder,"password");
        LoginRequest loginRequest = new LoginRequest(user.getEmail(), user.getPassword());

        // when then docs
        mockMvc.perform(RestDocumentationRequestBuilders.post(URL+"login")
                .content(createJson(loginRequest))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andDo(restDocumentationResultHandler.document(
                        requestFields(
                                fieldWithPath("email").description("이메일"),
                                fieldWithPath("password").description("비밀번호")
                        ),
                        responseFields(
                                errorDescriptors()
                        )
                ))
        ;
    }

    private ResponseFieldsSnippet responseAuthResponse() {
        return responseFields(
                fieldWithPath("tokenType").type(STRING).description("토큰 타입"),
                fieldWithPath("accessToken").type(STRING).description("JWT Access Token"),
                fieldWithPath("refreshToken").type(STRING).description("JWT Refresh Token")
        );
    }
}
