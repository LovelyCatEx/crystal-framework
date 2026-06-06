package com.lovelycatv.crystalframework.auth.event

enum class LoginMethod(val code: Int) {
    PASSWORD(0),
    OAUTH2(1);

    companion object {
        fun getByCode(code: Int): LoginMethod? {
            return entries.find { it.code == code }
        }
    }
}