package com.example.demo.test.domain.repository;

import com.example.demo.test.domain.entity.Member;
import com.example.demo.test.domain.entity.Tb;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class MemberJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    public void insertMemberList(List<Member> memberList){
        jdbcTemplate.batchUpdate("insert into member (name) values (?)",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setString(1, memberList.get(i).getName());
                    }

                    @Override
                    public int getBatchSize() {
                        return memberList.size();
                    }
                });

    }

}
