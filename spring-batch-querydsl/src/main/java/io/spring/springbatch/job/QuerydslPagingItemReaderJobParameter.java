package io.spring.springbatch.job;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;


@Getter
@Slf4j
@NoArgsConstructor
public class QuerydslPagingItemReaderJobParameter {
    private String name;

    @Value("#{jobParameters[name]}")
    public void setName(String name) {
        System.out.println(" 언제");

        this.name = name;
    }
}
