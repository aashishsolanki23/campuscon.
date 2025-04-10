package com.campuscon.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    
    @Value("${spring.profiles.active:dev}")
    private String activeProfile;
    
    @Value("${cors.allowed-origins:*}")
    private String allowedOrigins;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        if ("production".equals(activeProfile)) {
            // In production, use RabbitMQ as an external message broker
            // These settings would connect to a RabbitMQ instance
            config.enableStompBrokerRelay("/topic", "/queue")
                .setRelayHost("${RABBITMQ_HOST:localhost}")
                .setRelayPort(61613)
                .setClientLogin("${RABBITMQ_USER:guest}")
                .setClientPasscode("${RABBITMQ_PASSWORD:guest}");
        } else {
            // For development, use in-memory broker
            config.enableSimpleBroker("/topic", "/queue");
        }
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // In production, we'll restrict allowed origins
        if ("production".equals(activeProfile)) {
            registry.addEndpoint("/ws")
                .setAllowedOriginPatterns(allowedOrigins.split(","))
                .withSockJS();
        } else {
            registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
        }
    }
    
    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        // Set message size limits to prevent DOS attacks (10MB limit)
        registration.setMessageSizeLimit(10 * 1024 * 1024);
        // Set buffer size limits
        registration.setSendBufferSizeLimit(512 * 1024);
        registration.setSendTimeLimit(20000);
    }
    
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // Configure channel for handling client messages with appropriate thread pool
        registration.taskExecutor()
            .corePoolSize(4)
            .maxPoolSize(10)
            .queueCapacity(25);
    }
    
    @Override
    public void configureClientOutboundChannel(ChannelRegistration registration) {
        // Configure channel for sending messages to clients with appropriate thread pool
        registration.taskExecutor()
            .corePoolSize(4)
            .maxPoolSize(10)
            .queueCapacity(25);
    }
}
