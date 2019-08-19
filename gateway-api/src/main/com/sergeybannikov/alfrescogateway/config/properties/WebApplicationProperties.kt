package com.sergeybannikov.alfrescogateway.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "app")
data class WebApplicationProperties(var security: SecurityProperties)