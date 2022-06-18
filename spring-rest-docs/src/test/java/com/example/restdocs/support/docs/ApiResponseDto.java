package com.example.restdocs.support.docs;

import lombok.*;

@ToString
@Getter
@NoArgsConstructor
@Builder
// Test에서 Response 응답 값으로 사용할 객체
public class ApiResponseDto<T> {

    private T data;

    private ApiResponseDto(T data){
        this.data=data;
    }

    public static <T> ApiResponseDto<T> of(T data) {
        return new ApiResponseDto<>(data);
    }
}
