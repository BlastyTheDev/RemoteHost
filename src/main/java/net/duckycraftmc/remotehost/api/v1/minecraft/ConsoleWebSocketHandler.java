package net.duckycraftmc.remotehost.api.v1.minecraft;

import lombok.RequiredArgsConstructor;
import net.duckycraftmc.remotehost.api.v1.security.user.User;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.HashMap;

@RequiredArgsConstructor
public class ConsoleWebSocketHandler extends TextWebSocketHandler {

    private final HashMap<String, User> authenticatedSessionIds;
    private final HashMap<WebSocketSession, User> authenticatedSessions = new HashMap<>();

    @Override
    public void afterConnectionEstablished(@NotNull WebSocketSession session) throws Exception {
        String cookies = String.valueOf(session.getHandshakeHeaders().get("Cookie"));
        if (cookies == null || !cookies.contains("JSESSIONID")) {
            session.close();
            return;
        }
        String jSessionId = cookies.split(";")[1].split("=")[1].replaceAll("]", "");
        if (!authenticatedSessionIds.containsKey(jSessionId)) {
            session.close();
            return;
        }
        User user = authenticatedSessionIds.get(jSessionId);
        authenticatedSessions.put(session, user);
    }

    @Override
    protected void handleTextMessage(@NotNull WebSocketSession session, @NotNull TextMessage message) {
        User user = authenticatedSessions.get(session);
        System.out.println("Received message from " + user.getUsername() + ": " + message.getPayload());
    }

    @Override
    public void afterConnectionClosed(@NotNull WebSocketSession session, @NotNull CloseStatus status) {
        authenticatedSessions.remove(session);
    }

}
