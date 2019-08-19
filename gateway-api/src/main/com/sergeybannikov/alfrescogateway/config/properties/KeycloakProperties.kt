package com.sergeybannikov.alfrescogateway.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "app.security.keycloak")
class KeycloakProperties {

    lateinit var clientId: String
    lateinit var clientSecret: String
    lateinit var test: String
    lateinit var url: String

    val urlStripped:String
        get() = url.replace("[\\\\\\/]$".toRegex(), "")

    lateinit var realm: String
    var enabled: Boolean = true

    val accessTokenUri: String
        get() = (urlStripped
                + "/auth/realms/" + realm + "/protocol/openid-connect/token")

    val userAuthorizationUri: String
        get() = (urlStripped
                + "/auth/realms/" + realm + "/protocol/openid-connect/auth")

    val authUrl: String
        get() = urlStripped + "/auth"


}