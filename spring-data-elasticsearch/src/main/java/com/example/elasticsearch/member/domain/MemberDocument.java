package com.example.elasticsearch.member.domain;

import com.example.elasticsearch.zone.Zone;
import lombok.*;
import org.springframework.data.elasticsearch.annotations.*;

import javax.persistence.Id;
import java.time.LocalDateTime;

import static org.springframework.data.elasticsearch.annotations.DateFormat.date_hour_minute_second_millis;
import static org.springframework.data.elasticsearch.annotations.DateFormat.epoch_millis;


@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Document(indexName = "member")
@Mapping(mappingPath = "elastic/member-mapping.json")
@Setting(settingPath = "elastic/member-setting.json")
public class MemberDocument {

    @Id
    private Long id;

    private String name;

    private String nickname;

    private int age;

    private Status status;

    private Zone zone;

    private String description;

    @Field(type = FieldType.Date, format = {date_hour_minute_second_millis, epoch_millis})
    private LocalDateTime createdAt;

    public static MemberDocument from(Member member){
        return MemberDocument.builder()
                .id(member.getId())
                .name(member.getName())
                .nickname(member.getNickname())
                .age(member.getAge())
                .status(member.getStatus())
                .zone(member.getZone())
                .description(member.getDescription())
                .createdAt(member.getCreatedAt())
                .build();
    }

}
