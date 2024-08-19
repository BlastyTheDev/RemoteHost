package net.duckycraftmc.remotehost.api.v1.minecraft;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.duckycraftmc.remotehost.api.v1.security.user.User;
import net.duckycraftmc.remotehost.api.v1.security.user.UserRepository;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.HashMap;

import static net.duckycraftmc.remotehost.util.ValidationHelper.isUserOwnerOrCoOwner;

@RequiredArgsConstructor
public class ConsoleWebSocketHandler extends TextWebSocketHandler {

    private final HashMap<String, User> authenticatedSessionIds;
    private final HashMap<WebSocketSession, User> authenticatedSessions = new HashMap<>();
    // session watching the console of a Minecraft server (key: session, value: server ID)
    @Getter
    private final HashMap<WebSocketSession, Integer> minecraftServerConsoleSessions = new HashMap<>();

    private final MinecraftServerRepository serverRepository;
    private final UserRepository userRepository;

    @Override
    public void afterConnectionEstablished(@NotNull WebSocketSession session) throws Exception {
        String cookies = String.valueOf(session.getHandshakeHeaders().get("Cookie"));

        if (cookies == null || !cookies.contains("JSESSIONID")) {
            session.close();
            return;
        }

        String jSessionId = cookies.split("; ")[0].split("=")[1].replaceAll("]", "");

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

        if (user == null)
            return;

        String msg = message.getPayload();

        // set the server console that the session receives if the user is allowed
        if (msg.startsWith("::set-server ")) {
            String[] parts = msg.split(" ");

            if (parts.length < 2)
                return;

            Integer serverId = Integer.parseInt(parts[1]);

            if (serverRepository.findById(serverId).isEmpty())
                return;

            MinecraftServer server = serverRepository.findById(serverId).get();

            if (!isUserOwnerOrCoOwner(user, server, userRepository))
                return;

            minecraftServerConsoleSessions.put(session, serverId);
        }
    }

    @Override
    public void afterConnectionClosed(@NotNull WebSocketSession session, @NotNull CloseStatus status) {
        minecraftServerConsoleSessions.remove(session);
        authenticatedSessions.remove(session);
    }

}
