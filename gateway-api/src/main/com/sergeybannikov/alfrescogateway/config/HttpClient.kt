package com.sergeybannikov.alfrescogateway.config

import com.sergeybannikov.alfrescogateway.config.properties.TargetServiceProperties
import okhttp3.OkHttpClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.TimeUnit

@Configuration
class HttpClient(private val serviceProperties: TargetServiceProperties) {
    @Bean
    fun okhttpClient() = OkHttpClient.Builder()
        .connectTimeout(serviceProperties.timeout, TimeUnit.SECONDS)
        .readTimeout(serviceProperties.timeout, TimeUnit.SECONDS)
        .writeTimeout(serviceProperties.timeout, TimeUnit.SECONDS)
        .build()
}