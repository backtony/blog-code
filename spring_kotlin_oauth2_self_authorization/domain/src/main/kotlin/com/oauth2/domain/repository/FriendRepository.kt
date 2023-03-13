package com.oauth2.domain.repository

import com.oauth2.domain.entity.Friend
import org.springframework.data.jpa.repository.JpaRepository

interface FriendRepository : JpaRepository<Friend, Long>
