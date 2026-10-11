package com.back.global.security

import com.back.global.app.AppConfig
import com.back.global.rsData.RsData
import com.back.standard.util.Ut
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
class SecurityConfig(
    private val customAuthenticationFilter: CustomAuthenticationFilter,
    private val customOAuth2LoginSuccessHandler: AuthenticationSuccessHandler,
    private val customOAuth2AuthorizationRequestResolver: CustomOAuth2AuthorizationRequestResolver
) {

    @Bean
    fun filterChain(
        http: HttpSecurity,
        customOAuth2LoginFailureHandler: CustomOAuth2LoginFailureHandler?
    ): SecurityFilterChain {

        http {
            cors {
                configurationSource = corsConfigurationSource()
            }

            authorizeHttpRequests {
                authorize(HttpMethod.GET, "/api/*/members/{id:\\d+}", permitAll)
                authorize(HttpMethod.GET, "/api/*/widgets/{githubId}", permitAll)
                authorize(HttpMethod.GET, "/api/*/widgets/{githubId}/raw", permitAll)
                authorize(HttpMethod.GET, "/api/*/reviews/latest", permitAll)
                authorize(HttpMethod.GET, "/api/*/reviews/likeCount", permitAll)
                authorize(HttpMethod.GET, "/api/*/reviews/book/{id:\\d+}", permitAll)
                authorize(HttpMethod.GET, "/api/*/reviews/member/{id:\\d+}", permitAll)
                authorize(HttpMethod.GET, "/api/*/books/{id:\\d+}", permitAll)
                authorize(HttpMethod.GET, "/api/*/books/{id:\\d+}/thumbnail", permitAll)
                authorize(HttpMethod.GET, "/api/*/books/rank", permitAll)
                authorize(HttpMethod.GET, "/api/*/books/search", permitAll)

                authorize("/api/*/members/login", permitAll)
                authorize("/api/*/members/logout", permitAll)

                authorize(HttpMethod.POST, "/api/*/members", permitAll)

                authorize(HttpMethod.PUT, "/api/*/books/{id:\\d+}", hasRole("ADMIN"))
                authorize(HttpMethod.DELETE, "/api/*/books/{id:\\d+}", hasRole("ADMIN"))
                authorize("/api/*/members/admin", hasRole("ADMIN"))
                authorize("/api/*/members/admin/**", hasRole("ADMIN"))
                authorize("/api/*/reviews/admin", hasRole("ADMIN"))
                authorize("/api/*/reviews/admin/**", hasRole("ADMIN"))

                authorize("/api/*/**", authenticated)
                authorize(anyRequest, permitAll)
            }

            headers {
                frameOptions { sameOrigin = true }
            }

            csrf { disable() }
            formLogin { disable() }
            logout { disable() }
            httpBasic { disable() }

            sessionManagement {
                sessionCreationPolicy = SessionCreationPolicy.STATELESS
            }
            oauth2Login {
                authenticationSuccessHandler = customOAuth2LoginSuccessHandler
                authenticationFailureHandler = customOAuth2LoginFailureHandler
                authorizationEndpoint {
                    authorizationRequestResolver = customOAuth2AuthorizationRequestResolver
                }
            }

            addFilterBefore<UsernamePasswordAuthenticationFilter>(
                customAuthenticationFilter
            )

            exceptionHandling {
                authenticationEntryPoint = AuthenticationEntryPoint { _, response, _ ->
                    response.contentType = "application/json;charset=UTF-8"
                    response.status = 401
                    response.writer.write(
                        Ut.json.toString(
                            RsData<Unit>("401-1", "로그인 후 이용해주세요.")
                        )
                    )
                }
                accessDeniedHandler = AccessDeniedHandler { _, response, _ ->
                    response.contentType = "application/json;charset=UTF-8"
                    response.status = 403
                    response.writer.write(
                        Ut.json.toString(
                            RsData<Unit>("403-1", "권한이 없습니다.")
                        )
                    )
                }
            }
        }

        return http.build()
    }

    @Bean
    fun corsConfigurationSource(): UrlBasedCorsConfigurationSource {


        val configuration = CorsConfiguration().apply {
            allowedOrigins = listOf(AppConfig.siteFrontUrl)
            allowedMethods = listOf("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
            allowCredentials = true
            allowedHeaders = listOf("*")
        }

        return UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/api/**", configuration)
        }
    }
}