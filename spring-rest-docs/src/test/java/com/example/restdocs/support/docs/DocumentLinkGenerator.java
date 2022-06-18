package com.example.restdocs.support.docs;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

public interface DocumentLinkGenerator {

    static String generateLinkCode(DocUrl docUrl) {
        return String.format("link:common/%s.html[%s %s,role=\"popup\"]", docUrl.pageId, docUrl.text, "코드");
    }

    static String generateText(DocUrl docUrl) {
        return String.format("%s %s", docUrl.text, "코드명");
    }

    @RequiredArgsConstructor
    enum DocUrl {
        MEMBER_STATUS("member-status", "상태"),
        MEMBER_SEX("sex","성별")
        ;

        private final String pageId;
        @Getter
        private final String text;
    }
}
