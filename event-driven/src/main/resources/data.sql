create table event_entry (
     `id` bigint not null AUTO_INCREMENT PRIMARY KEY,
     `type` varchar(255),
     `content_type` varchar(255),
     `payload` MEDIUMTEXT,
     `timestamp` datetime
);
