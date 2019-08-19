package com.sergeybannikov.alfrescogateway.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "app.target")
data class TargetServiceProperties (
     var host: String = "localhost",
     var port: Int = 8080,
     var timeout: Long = 600,
     var userHeader: String = "X-Alfresco-Remote-User"
)
