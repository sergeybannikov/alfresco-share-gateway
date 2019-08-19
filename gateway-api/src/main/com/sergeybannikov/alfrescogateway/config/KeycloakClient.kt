package com.sergeybannikov.alfrescogateway.config

import com.sergeybannikov.alfrescogateway.config.properties.KeycloakProperties
import org.keycloak.OAuth2Constants
import org.keycloak.admin.client.Keycloak
import org.keycloak.admin.client.KeycloakBuilder
import org.keycloak.admin.client.resource.RealmResource
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class KeycloakClient(private val keycloakProperties: KeycloakProperties) {
    @Bean
    open fun keycloak(): Keycloak {
        return KeycloakBuilder.builder()
            .serverUrl(keycloakProperties.authUrl)
            .realm(keycloakProperties.realm)
            .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
            .clientId(keycloakProperties.clientId)
            .clientSecret(keycloakProperties.clientSecret).build()
    }

    @Bean
    open fun realmResource(keycloak: Keycloak): RealmResource {
        return keycloak.realm(keycloakProperties.realm)
    }
}