package com.sergeybannikov.alfrescogateway.config

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken
import org.springframework.security.oauth2.common.OAuth2AccessToken
import org.springframework.security.oauth2.provider.OAuth2Authentication
import org.springframework.security.oauth2.provider.OAuth2Request
import org.springframework.security.oauth2.provider.token.AccessTokenConverter
import org.springframework.security.oauth2.provider.token.UserAuthenticationConverter
import org.springframework.stereotype.Component
import java.util.*

@Component
class KeycloakAccessTokenConverter : AccessTokenConverter {

    private val userTokenConverter = KeycloakUserAuthenticationConverter()

    private val CLIENT_ID = "username"

    private val includeGrantType: Boolean = false

    override fun convertAccessToken(token: OAuth2AccessToken, authentication: OAuth2Authentication): Map<String, *> {
        val response = HashMap<String, Any>()
        val clientToken = authentication.oAuth2Request

        if (!authentication.isClientOnly) {
            response.putAll(userTokenConverter.convertUserAuthentication(authentication.userAuthentication))
        } else {
            if (clientToken.authorities !=
                null && !clientToken.authorities.isEmpty()
            ) {
                response[UserAuthenticationConverter.AUTHORITIES] =
                    AuthorityUtils.authorityListToSet(clientToken.authorities)
            }
        }

        if (token.scope != null) {
            response[AccessTokenConverter.SCOPE] = token.scope
        }
        if (token.additionalInformation.containsKey(AccessTokenConverter.JTI)) {
            response[AccessTokenConverter.JTI] = token.additionalInformation[AccessTokenConverter.JTI]!!
        }

        if (token.expiration != null) {
            response[AccessTokenConverter.EXP] = token.expiration.time / 1000
        }

        if (includeGrantType && authentication.oAuth2Request.grantType != null) {
            response[AccessTokenConverter.GRANT_TYPE] = authentication.oAuth2Request.grantType
        }

        response.putAll(token.additionalInformation)

        response[CLIENT_ID] = clientToken.clientId
        if (clientToken.resourceIds != null && !clientToken.resourceIds.isEmpty()) {
            response[AccessTokenConverter.AUD] = clientToken.resourceIds
        }
        return response
    }

    override fun extractAccessToken(value: String, map: Map<String, *>): OAuth2AccessToken {
        val token = DefaultOAuth2AccessToken(value)
        val info = HashMap<String, Any>(map)
        info.remove(AccessTokenConverter.EXP)
        info.remove(AccessTokenConverter.AUD)
        info.remove(CLIENT_ID)
        info.remove(AccessTokenConverter.SCOPE)
        if (map.containsKey(AccessTokenConverter.EXP)) {
            token.expiration = Date(map[AccessTokenConverter.EXP] as Long * 1000L)
        }
        if (map.containsKey(AccessTokenConverter.JTI)) {
            info[AccessTokenConverter.JTI] = map[AccessTokenConverter.JTI]!!
        }
        token.scope = extractScope(map)
        token.additionalInformation = info
        return token
    }

    override fun extractAuthentication(map: Map<String, *>): OAuth2Authentication {
        val parameters = HashMap<String, String>()
        val scope = extractScope(map)
        val user = userTokenConverter.extractAuthentication(map)
        val clientId = map[CLIENT_ID] as String
        parameters[CLIENT_ID] = clientId
        if (includeGrantType && map.containsKey(AccessTokenConverter.GRANT_TYPE)) {
            parameters[AccessTokenConverter.GRANT_TYPE] = map[AccessTokenConverter.GRANT_TYPE] as String
        }
        val resourceIds = LinkedHashSet(
            if (map.containsKey(AccessTokenConverter.AUD))
                getAudience(map)
            else
                emptySet()
        )

        var authorities: Collection<GrantedAuthority>? = null
        if (user == null && map.containsKey(AccessTokenConverter.AUTHORITIES)) {
            val roles = (map[AccessTokenConverter.AUTHORITIES] as Collection<String>).toTypedArray()
            authorities = AuthorityUtils.createAuthorityList(*roles)
        }
        val request = OAuth2Request(parameters, clientId, authorities, true, scope, resourceIds, null, null, null)
        return OAuth2Authentication(request, user)

    }

    private fun getAudience(map: Map<String, *>): Collection<String> {
        val auds = map[AccessTokenConverter.AUD]
        return if (auds is Collection<*>) {
            auds as Collection<String>
        } else setOf(auds as String)
    }

    private fun extractScope(map: Map<String, *>): Set<String> {
        var scope = emptySet<String>()
        if (map.containsKey(AccessTokenConverter.SCOPE)) {
            val scopeObj = map[AccessTokenConverter.SCOPE]!!
            if (String::class.java.isInstance(scopeObj)) {
                scope =
                    LinkedHashSet(Arrays.asList(*String::class.java.cast(scopeObj).split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()))
            } else if (Collection::class.java.isAssignableFrom(scopeObj.javaClass)) {
                val scopeColl = scopeObj as Collection<String>
                scope = LinkedHashSet(scopeColl)    // Preserve ordering
            }
        }
        return scope
    }

}
