package com.lovelycatv.crystalframework.auth.service.impl

import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.client.userinfo.ReactiveOAuth2UserService
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono

class QQOAuth2UserService(
    webClientBuilder: WebClient.Builder
) : ReactiveOAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private val webClient = webClientBuilder.build()

    override fun loadUser(userRequest: OAuth2UserRequest): Mono<OAuth2User> {
        val accessToken = userRequest.accessToken.tokenValue
        val clientId = userRequest.clientRegistration.clientId

        return webClient.get()
            .uri("https://graph.qq.com/oauth2.0/me?access_token=$accessToken")
            .retrieve()
            .bodyToMono<String>()
            .flatMap { body ->
                val openId = Regex("\"openid\":\"(.*?)\"")
                    .find(body)
                    ?.groupValues?.get(1)
                    ?: return@flatMap Mono.error(RuntimeException("QQ openid 获取失败"))

                webClient.get()
                    .uri {
                        it.scheme("https")
                            .host("graph.qq.com")
                            .path("/user/get_user_info")
                            .queryParam("access_token", accessToken)
                            .queryParam("openid", openId)
                            .queryParam("oauth_consumer_key", clientId)
                            .build()
                    }
                    .retrieve()
                    .bodyToMono(Map::class.java)
                    .map { userInfo ->

                        val attributes = HashMap<String, Any>()
                        attributes.putAll(userInfo as Map<String, Any>)
                        attributes["openid"] = openId

                        DefaultOAuth2User(
                            listOf(SimpleGrantedAuthority("ROLE_USER")),
                            attributes,
                            "openid"
                        )
                    }
            }
    }
}