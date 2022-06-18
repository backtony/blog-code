package com.example.restdocs.support.docs;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnumDocs {

    // 문서화하고 싶은 모든 enum값을 명시
    Map<String,String> Sex;
    Map<String,String> memberStatus;


}
