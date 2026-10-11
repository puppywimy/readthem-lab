package com.back.global.app

import com.back.standard.util.Ut
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.client.RestTemplate
import tools.jackson.databind.ObjectMapper

@Configuration
class AppConfig(
    @Value("\${custom.site.cookieDomain}") cookieDomain: String,
    @Value("\${custom.site.cookieSecure}") cookieSecure: Boolean,
    @Value("\${custom.site.frontUrl}") siteFrontUrl: String,
    @Value("\${custom.site.backUrl}") siteBackUrl: String,
) {
    init {
        _cookieDomain = cookieDomain
        _cookieSecure = cookieSecure
        _siteFrontUrl = siteFrontUrl
        _siteBackUrl = siteBackUrl
    }

    companion object {
        lateinit var objectMapper: ObjectMapper
            private set

        private lateinit var _cookieDomain: String
        private var _cookieSecure: Boolean = false
        private lateinit var _siteFrontUrl: String
        private lateinit var _siteBackUrl: String

        val cookieDomain by lazy { _cookieDomain }
        val cookieSecure by lazy { _cookieSecure }
        val siteFrontUrl by lazy { _siteFrontUrl }
        val siteBackUrl by lazy { _siteBackUrl }
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun restTemplate(): RestTemplate = RestTemplate()

    @Autowired
    fun setObjectMapper(objectMapper: ObjectMapper) {
        AppConfig.objectMapper = objectMapper
    }

    @PostConstruct
    fun postConstruct() {
        Ut.json.objectMapper = objectMapper
    }
}
