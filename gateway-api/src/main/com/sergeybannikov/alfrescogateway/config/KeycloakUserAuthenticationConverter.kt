package com.sergeybannikov.alfrescogateway.config

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.oauth2.provider.token.UserAuthenticationConverter
import org.springframework.util.StringUtils
import java.util.*

class KeycloakUserAuthenticationConverter : UserAuthenticationConverter {

    private val USERNAME = "username"
    private val REALM_ACCESS = "realm_access"
    private var defaultAuthorities: Collection<GrantedAuthority>? = null

    private var userDetailsService: UserDetailsService? = null

    /**
     * Optional [UserDetailsService] to use when extracting an [Authentication] from the incoming map.
     *
     * @param userDetailsService the userDetailsService to set
     */
    fun setUserDetailsService(userDetailsService: UserDetailsService) {
        this.userDetailsService = userDetailsService
    }

    /**
     * Default value for authorities if an Authentication is being created and the input has no data for authorities.
     * Note that unless this property is set, the default Authentication created by [.extractAuthentication]
     * will be unauthenticated.
     *
     * @param defaultAuthorities the defaultAuthorities to set. Default null.
     */
    fun setDefaultAuthorities(defaultAuthorities: Array<String>) {
        this.defaultAuthorities = AuthorityUtils.commaSeparatedStringToAuthorityList(
            StringUtils
                .arrayToCommaDelimitedString(defaultAuthorities)
        )
    }

    override fun convertUserAuthentication(authentication: Authentication): Map<String, Any> {
        val response = LinkedHashMap<String, Any>()
        response[USERNAME] = authentication.name
        if (authentication.authorities != null && !authentication.authorities.isEmpty()) {
            response[UserAuthenticationConverter.AUTHORITIES] =
                AuthorityUtils.authorityListToSet(authentication.authorities)
        }
        return response
    }

    override fun extractAuthentication(map: Map<String, *>): Authentication? {
        if (map.containsKey(USERNAME)) {
            var principal: Any = map[USERNAME]!!
            var authorities = getAuthorities(map)
            if (userDetailsService != null) {
                val user = userDetailsService!!.loadUserByUsername(map[USERNAME] as String)
                authorities = user.authorities
                principal = user
            }
            return UsernamePasswordAuthenticationToken(principal, "N/A", authorities)
        }
        return null
    }

    private fun getAuthorities(map: Map<String, *>): Collection<GrantedAuthority>? {
        if (!map.containsKey(REALM_ACCESS)) {
            return defaultAuthorities
        }
        val authorities = map[REALM_ACCESS]
        if (authorities is String) {
            return AuthorityUtils.commaSeparatedStringToAuthorityList(authorities)
        }
        if (authorities is HashMap<*, *>) {
            val authoritiesList = authorities as HashMap<String, List<String>>
            return AuthorityUtils.commaSeparatedStringToAuthorityList(
                StringUtils
                    .collectionToCommaDelimitedString(authoritiesList["roles"])
            )
        }
        throw IllegalArgumentException("Authorities must be either a String or a Collection")
    }

}
