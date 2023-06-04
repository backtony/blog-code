CREATE TABLE Team (
    team_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    leader_id BIGINT,
    created_at DATETIME,
    modified_at DATETIME
);

CREATE TABLE Member (
    member_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    team_id BIGINT,
    created_at DATETIME,
    modified_at DATETIME
);

ALTER TABLE member
    ADD CONSTRAINT FK_member_team
        FOREIGN KEY (team_id) REFERENCES team(team_id);

ALTER TABLE team
    ADD CONSTRAINT FK_team_leader
        FOREIGN KEY (leader_id) REFERENCES member(member_id);