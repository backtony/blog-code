package com.oauth2.authorizationserver.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding

@ConfigurationProperties(prefix = "oauth-client")
data class OAuthClientProperties @ConstructorBinding constructor(
    val album: OAuthClient,
    val friend: OAuthClient,
    val client: OAuthClient
)

data class OAuthClient(
    val id: String,
    val clientId: String,
    val clientSecret: String
)
