package com.oauth2.domain.repository

import com.oauth2.domain.entity.Album
import org.springframework.data.jpa.repository.JpaRepository

interface AlbumRepository: JpaRepository<Album, Long>
