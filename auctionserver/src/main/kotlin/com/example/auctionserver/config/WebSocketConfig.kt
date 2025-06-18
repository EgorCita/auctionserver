package com.example.auctionserver.config

import com.example.auctionserver.repository.UserRepository
import com.example.auctionserver.security.JwtTokenProvider
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.simp.config.ChannelRegistration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.messaging.support.MessageHeaderAccessor
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer


@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig(
    private val jwtTokenProvider: JwtTokenProvider,
    private val userRepository: UserRepository
) : WebSocketMessageBrokerConfigurer {

    override fun configureMessageBroker(config: MessageBrokerRegistry) {
        config.enableSimpleBroker("/topic", "/queue")
        config.setApplicationDestinationPrefixes("/app")
        config.setUserDestinationPrefix("/user")
    }

    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        registry.addEndpoint("/ws-auction")
//            .setAllowedOrigins("http://127.0.0.1:5500", "http://localhost:5500", "http://localhost:8080", "http://127.0.0.1:8080")
            .setAllowedOriginPatterns("*")
            .withSockJS()
    }

    override fun configureClientInboundChannel(registration: ChannelRegistration) {
        registration.interceptors(object : ChannelInterceptor {
            override fun preSend(message: Message<*>, channel: MessageChannel): Message<*>? {
//                val accessor = StompHeaderAccessor.wrap(message)
                val accessor = MessageHeaderAccessor
                    .getAccessor(message, StompHeaderAccessor::class.java)

                if (accessor != null && accessor.command == StompCommand.CONNECT) {
                    val authHeader = accessor.getFirstNativeHeader("Authorization")
                    if (authHeader?.startsWith("Bearer ") == true) {
                        val token = authHeader.substring(7)
                        if (jwtTokenProvider.validateToken(token)) {
                            accessor.user = jwtTokenProvider.getAuthentication(token, userRepository)
                        }
                    }
                }

                println("accessor.command = ${accessor?.command ?: "NULL"} ; accessor = $accessor")

                return message
            }
        })
    }
}