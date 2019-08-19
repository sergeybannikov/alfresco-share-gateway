package com.sergeybannikov.alfrescogateway.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "app.security")
data class SecurityProperties (

     var allowedWebOrigins: MutableList<String>,
     var keycloak: KeycloakProperties
)
