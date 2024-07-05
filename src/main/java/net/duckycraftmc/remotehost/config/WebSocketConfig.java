package net.duckycraftmc.remotehost.config;

import lombok.RequiredArgsConstructor;
import net.duckycraftmc.remotehost.api.v1.minecraft.ConsoleWebSocketHandler;
import net.duckycraftmc.remotehost.api.v1.security.user.User;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import java.util.HashMap;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final HashMap<String, User> authenticatedSessionIds;

    @Override
    public void registerWebSocketHandlers(@NotNull WebSocketHandlerRegistry registry) {
        registry.addHandler(consoleWebSocketHandler(), "/api/v1/ws/minecraft/console").setAllowedOrigins("*");
    }

    @Bean
    public ConsoleWebSocketHandler consoleWebSocketHandler() {
        return new ConsoleWebSocketHandler(authenticatedSessionIds);
    }

}
