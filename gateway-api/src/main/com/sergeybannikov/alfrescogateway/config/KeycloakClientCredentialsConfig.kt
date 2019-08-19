package com.sergeybannikov.alfrescogateway.config

import com.google.common.collect.ImmutableList
import com.sergeybannikov.alfrescogateway.config.properties.KeycloakProperties
import com.sergeybannikov.alfrescogateway.config.properties.WebApplicationProperties
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer
import org.springframework.security.oauth2.provider.token.RemoteTokenServices
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.filter.CorsFilter
import java.util.*

@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
@EnableResourceServer
@ComponentScan(basePackages = ["com.sergeybannikov.alfrescogateway.*"])
@EnableGlobalMethodSecurity(prePostEnabled = true)
class KeycloakClientCredentialsConfig(
    private val keycloakProperties: KeycloakProperties,
    private val webApplicationProperties: WebApplicationProperties,
    private val keycloakAccessTokenConverter: KeycloakAccessTokenConverter
) : ResourceServerConfigurerAdapter() {

    @Throws(Exception::class)
    override fun configure(http: HttpSecurity) {
        if (!keycloakProperties.enabled) {
            http
                .authorizeRequests()
                .antMatchers("/**").permitAll()
                .anyRequest().permitAll()
                .and().csrf().disable()
            return
        }
        http
            .authorizeRequests()
            .antMatchers(HttpMethod.POST, "/etcd/**").permitAll()
            .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
            .antMatchers("/test/hello").permitAll()

            .antMatchers("/**").hasAnyAuthority(
                "TEST-R-APP-MyApp",
                "TEST-R-PERMISSION-Superuser"
            )
            .anyRequest().permitAll()

            .and().csrf().disable()

    }

    @Throws(Exception::class)
    override fun configure(resources: ResourceServerSecurityConfigurer) {
        resources.resourceId(keycloakProperties.clientId)
    }

    @Bean
    fun oAuth2ProtectedResourceDetails(): OAuth2ProtectedResourceDetails {
        val details = ClientCredentialsResourceDetails()
        details.setClientId(keycloakProperties.clientId)
        details.setClientSecret(keycloakProperties.clientSecret)
        details.setAccessTokenUri(keycloakProperties.accessTokenUri)
        return details
    }

    @Primary
    @Bean
    fun tokenService(): RemoteTokenServices {
        val tokenService = RemoteTokenServices()
        tokenService.setCheckTokenEndpointUrl(
            keycloakProperties.accessTokenUri + "/introspect"
        )
        tokenService.setClientId(keycloakProperties.clientId)
        tokenService.setClientSecret(keycloakProperties.clientSecret)
        tokenService.setAccessTokenConverter(keycloakAccessTokenConverter)
        return tokenService
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        configuration.allowedOrigins = webApplicationProperties.security.allowedWebOrigins
        configuration.allowedMethods = ImmutableList.of(
            "HEAD",
            "GET", "POST", "PUT", "DELETE", "PATCH"
        )
        // setAllowCredentials(true) is important, otherwise:
        // The value of the 'Access-Control-Allow-Origin' header in the response must not be the wildcard '*' when the request's credentials mode is 'include'.
        configuration.allowCredentials = true
        // setAllowedHeaders is important! Without it, OPTIONS preflight request
        // will fail with 403 Invalid CORS request
        configuration.allowedHeaders = Arrays.asList(*("DNT,X-CustomHeader,Keep-Alive,User-Agent,X-Requested-With," +
                "If-Modified-Since,Cache-Control,Content-Type,Content-Range,Range," +
                "access-control-allow-origin,Access-Control-Allow-Origin,Authorization," +
                "authorization,accept").split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        )
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }

    @Bean
    fun corsFilter(): FilterRegistrationBean<*> {
        val source = UrlBasedCorsConfigurationSource()
        val config = CorsConfiguration()
        config.allowCredentials = true
        config.allowedOrigins = webApplicationProperties.security.allowedWebOrigins
        config.addAllowedHeader("*")
        config.addAllowedMethod("*")
        source.registerCorsConfiguration("/**", config)
        val bean = FilterRegistrationBean(CorsFilter(source))
        bean.order = 0
        return bean
    }

}