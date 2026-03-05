package com.lovelycatv.crystalframework.auth.service.impl

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.DefaultReactiveOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService
import org.springframework.security.oauth2.client.userinfo.ReactiveOAuth2UserService
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@Component
class CustomOAuth2UserService(
    webClientBuilder: WebClient.Builder
) : ReactiveOAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private val defaultService = DefaultReactiveOAuth2UserService()
    private val qqService = QQOAuth2UserService(webClientBuilder)

    override fun loadUser(userRequest: OAuth2UserRequest): Mono<OAuth2User> {
        val registrationId = userRequest.clientRegistration.registrationId

        return when (registrationId) {
            "oicq" -> qqService.loadUser(userRequest)
            else -> defaultService.loadUser(userRequest)
        }
    }
}