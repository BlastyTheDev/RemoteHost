package net.duckycraftmc.remotehost.api.v1.minecraft;

import net.duckycraftmc.remotehost.api.v1.security.user.User;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Objects;

public class ConsoleWebSocketHandler extends TextWebSocketHandler {

    @Override
    protected void handleTextMessage(@NotNull WebSocketSession session, @NotNull TextMessage message) throws Exception {
        User user = (User) session.getPrincipal();
        System.out.println("Received message: " + message.getPayload() + " from " + Objects.requireNonNull(user).getUsername());
    }

}
