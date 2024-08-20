package net.duckycraftmc.remotehost.api.v1.minecraft;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.duckycraftmc.remotehost.api.v1.security.jwt.services.JWTService;
import net.duckycraftmc.remotehost.api.v1.security.user.User;
import net.duckycraftmc.remotehost.api.v1.security.user.UserRepository;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.HashMap;

import static net.duckycraftmc.remotehost.util.ValidationHelper.isUserOwnerOrCoOwner;

@RequiredArgsConstructor
public class ConsoleWebSocketHandler extends TextWebSocketHandler {

    @Getter
    private final HashMap<WebSocketSession, Integer> minecraftServerConsoleSessions = new HashMap<>();

    private final JWTService jwtService;

    private final MinecraftServerRepository serverRepository;
    private final UserRepository userRepository;

    @Override
    protected void handleTextMessage(@NotNull WebSocketSession session, @NotNull TextMessage message) throws IOException {
        var cookies = session.getHandshakeHeaders().get("Cookie");

        if (cookies == null) {
            session.close();
            return;
        }

        var tokenCookie = cookies.getFirst().split("; ");
        String jwt = tokenCookie[1].split("=")[1];

        if (jwt == null) {
            session.close();
            return;
        }

        var user = userRepository.findByUsername(jwtService.getSubject(jwt)).orElse(null);

        if (user == null) {
            session.close();
            return;
        }

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
            
            session.sendMessage(new TextMessage("::set-server " + server.getName()));
        }
    }

    @Override
    public void afterConnectionClosed(@NotNull WebSocketSession session, @NotNull CloseStatus status) {
        minecraftServerConsoleSessions.remove(session);
    }

}
