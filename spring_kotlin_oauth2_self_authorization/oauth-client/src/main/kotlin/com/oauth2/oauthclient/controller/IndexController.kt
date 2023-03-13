package com.oauth2.oauthclient.controller

import org.springframework.security.oauth2.client.OAuth2AuthorizedClient
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

@Controller
class IndexController {

    @GetMapping("/")
    fun index(model: Model): String {
        model.addAttribute("token", "-")
        return "index"
    }

    @GetMapping("/token")
    fun token(
        // https://godekdls.github.io/Spring%20Security/@registeredoauth2authorizedclient/
        // OAuth2AuthorizedClientArgunemtResolver에서 요청을 가로채서 유형별로 권한 부여 흐름을 실행한다.
        // OAuth2AuthorizedClientManager나 OAuth2AuthorizedClientService로 OAuth2AuthorizedClient을 얻어내는 과정을 애노테이션이 한번에 해주는 것으로 보면 된다.
        @RegisteredOAuth2AuthorizedClient("self-oauth")
        oAuth2AuthorizedClient: OAuth2AuthorizedClient,
        model: Model
    ): String {
        model.addAttribute("token", oAuth2AuthorizedClient.accessToken.tokenValue)
        return "index"
    }
}
