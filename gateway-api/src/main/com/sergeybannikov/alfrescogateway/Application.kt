package com.sergeybannikov.alfrescogateway

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cloud.netflix.zuul.EnableZuulProxy
import org.springframework.context.annotation.Configuration


@Configuration
//@EnableAutoConfiguration //(exclude={MultipartAutoConfiguration.class})
@EnableZuulProxy
@SpringBootApplication(scanBasePackages = ["com.sergeybannikov.alfrescogateway"])
open class Application

fun main(args: Array<String>) {
	SpringApplication.run(Application::class.java, *args)
}