package com.example.restdocs.unit.member;

import com.example.restdocs.member.domain.Member;
import com.example.restdocs.member.presentation.dto.MemberModificationRequest;
import com.example.restdocs.member.presentation.dto.MemberSignUpRequest;
import com.example.restdocs.member.domain.MemberStatus;
import com.example.restdocs.support.docs.DocumentLinkGenerator;
import com.example.restdocs.support.docs.RestDocsTestSupport;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.Optional;

import static com.example.restdocs.config.RestDocsConfig.field;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


class MemberControllerTest extends RestDocsTestSupport {

    @Test
    public void member_get() throws Exception {
        Member member = new Member("backtony@gmail.com", 27, MemberStatus.NORMAL);
        given(memberRepository.findById(ArgumentMatchers.any())).willReturn(Optional.of(member));

        mockMvc.perform(
                get("/api/members/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(
                        restDocs.document(
                                pathParameters(
                                        parameterWithName("id").description("Member ID")
                                ),
                                responseFields(
                                        fieldWithPath("id").description("ID"),
                                        fieldWithPath("age").description("age"),
                                        fieldWithPath("email").description("email")
                                )
                        )
                )
        ;
    }

    @Test
    public void member_page_test() throws Exception {
        Member member = new Member("backtony@gmail.com", 27, MemberStatus.NORMAL);
        PageImpl<Member> memberPage = new PageImpl<>(List.of(member), PageRequest.of(0, 10), 1);
        given(memberRepository.findAll(ArgumentMatchers.any(Pageable.class))).willReturn(memberPage);

        mockMvc.perform(
                get("/api/members")
                        .param("size", "10")
                        .param("page", "0")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(
                        restDocs.document(
                                requestParameters(
                                        parameterWithName("size").optional().description("size"),
                                        parameterWithName("page").optional().description("page")
                                )
                        )
                )
        ;
    }


    @Test
    public void member_create() throws Exception {
        MemberSignUpRequest dto = MemberSignUpRequest.builder()
                .age(1)
                .email("hhh@naver.com")
                .status(MemberStatus.BAN)
                .build();

        mockMvc.perform(
                post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson(dto)))
                .andExpect(status().isOk())
                .andDo(
                        restDocs.document(
                                requestFields(
                                        fieldWithPath("age").description("age").attributes(field("constraints", "길이 10 이하")),
                                        fieldWithPath("email").description("email").attributes(field("constraints", "길이 30 이하")),
                                        fieldWithPath("status").description(DocumentLinkGenerator.generateLinkCode(DocumentLinkGenerator.DocUrl.MEMBER_STATUS))
                                )
                        )
                )
        ;
    }

    @Test
    public void member_modify() throws Exception {
        // given
        MemberModificationRequest dto = MemberModificationRequest.builder().age(1).build();
        Member member = new Member("backtony@gmail.com", 27, MemberStatus.NORMAL);
        given(memberRepository.findById(ArgumentMatchers.any())).willReturn(Optional.of(member));

        mockMvc.perform(
                patch("/api/members/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson(dto)))
                .andExpect(status().isOk())
                .andDo(
                        restDocs.document(
                                pathParameters(
                                        parameterWithName("id").description("Member ID")
                                ),
                                requestFields(
                                        fieldWithPath("age").description("age")
                                )
                        )
                )
        ;
    }

}