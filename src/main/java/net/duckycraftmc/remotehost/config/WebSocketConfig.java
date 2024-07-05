package net.duckycraftmc.remotehost.config;

import net.duckycraftmc.remotehost.api.v1.minecraft.ConsoleWebSocketHandler;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final ConsoleWebSocketHandler consoleWebSocketHandler = new ConsoleWebSocketHandler();

    @Override
    public void registerWebSocketHandlers(@NotNull WebSocketHandlerRegistry registry) {
        registry.addHandler(consoleWebSocketHandler, "/api/v1/minecraft/console");
    }

    @Bean
    public ConsoleWebSocketHandler consoleWebSocketHandler() {
        return consoleWebSocketHandler;
    }

}
