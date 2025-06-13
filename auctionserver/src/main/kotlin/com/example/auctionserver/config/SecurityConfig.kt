package com.example.auctionserver.config

import com.example.auctionserver.security.JwtTokenProvider
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.HttpStatusEntryPoint
import org.springframework.stereotype.Component
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter

@Configuration
@EnableWebSecurity
class SecurityConfig {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .cors { it.configurationSource(corsConfigurationSource()) }
            .csrf { it.disable() }
            .authorizeHttpRequests {
                it.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    .requestMatchers("/api/auth/**").permitAll()
                    .requestMatchers("/ws-auction/**").permitAll()
                    .requestMatchers("/error").permitAll()
                    .anyRequest().authenticated()
            }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .exceptionHandling { exceptions ->
              // Кастомизация ошибки 401
              exceptions.authenticationEntryPoint { req, res, ex ->
                res.contentType = "application/json"
                res.status = HttpStatus.UNAUTHORIZED.value()
                res.writer.write("""{"error": "Unauthorized"}""")
              }
            }
//            }.addFilterBefore(RequestLoggingFilter(), BasicAuthenticationFilter::class.java)

        return http.build()
    }
//@Bean
//fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
//    http.cors { it.configurationSource(corsConfigurationSource()) }
//        .csrf { it.disable() }
//        .authorizeHttpRequests { it.anyRequest().permitAll() }
//    return http.build()
//}

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration().apply {
            allowedOrigins = listOf(
                "http://127.0.0.1:5500",
                "http://localhost:5500",
                "http://localhost:8080",
                "http://127.0.0.1:8080"
            )
            allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
            allowedHeaders = listOf("*")
            allowCredentials = true
            maxAge = 3600
        }

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }

    @Component
    class RequestLoggingFilter : OncePerRequestFilter() {
        override fun doFilterInternal(
            request: HttpServletRequest,
            response: HttpServletResponse,
            filterChain: FilterChain
        ) {
            println("\n=== Request Debug ===")
            println("Method: ${request.method}")
            println("URL: ${request.requestURI}")
            println("Content-Type: ${request.contentType}")
            println("=== End Request Debug ===\n")

            try {
                println("Before chain: ${request.requestURI}")
                filterChain.doFilter(request, response)
                println("After chain: ${request.requestURI}")
            } catch (e: Exception) {
                println("Filter error: ${e.javaClass.simpleName} - ${e.message}")
                throw e
            }
        }
    }

//    @Component
//    class JwtAuthenticationFilter(
//        private val jwtTokenProvider: JwtTokenProvider
//    ) : OncePerRequestFilter() {
//
//        override fun doFilterInternal(
//            request: HttpServletRequest,
//            response: HttpServletResponse,
//            filterChain: FilterChain
//        ) {
//            try {
//                val token = extractToken(request)
//                if (token != null && jwtTokenProvider.validateToken(token)) {
//                    val auth = jwtTokenProvider.getAuthentication(token)
//                    SecurityContextHolder.getContext().authentication = auth
//                }
//            } catch (ex: Exception) {
//                logger.error("Failed to set authentication", ex)
//            }
//            filterChain.doFilter(request, response)
//        }
//
//        private fun extractToken(request: HttpServletRequest): String? {
//            val header = request.getHeader("Authorization")
//            return if (header?.startsWith("Bearer ") == true) {
//                header.substring(7)
//            } else null
//        }
//    }

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()
}