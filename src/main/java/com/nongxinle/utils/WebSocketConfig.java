package com.nongxinle.utils;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
//@EnableWebMvc
@EnableWebSocket
public class WebSocketConfig  implements WebSocketConfigurer {
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry webSocketHandlerRegistry) {
        webSocketHandlerRegistry.addHandler(myhandler(),"/webSocketIMServer");
    }

    @Bean
    public WebSocketEndPoint myhandler() {
        return new WebSocketEndPoint();
    }

    @Bean
    public HandshakeInterceptor myInterceptors() {
        return new HandshakeInterceptor();
    }

}