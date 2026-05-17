package com.lovelycatv.crystalframework.auth.service.result

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonProperty

@JsonAutoDetect(
    fieldVisibility = JsonAutoDetect.Visibility.ANY,
    getterVisibility = JsonAutoDetect.Visibility.NONE,
    isGetterVisibility = JsonAutoDetect.Visibility.NONE
)
class LoginSuccessResponseData {
    @JsonProperty("token")
    var token: String? = null
    @JsonProperty("expiresIn")
    var expiresIn: Long = 0
}