package net.duckycraftmc.remotehost.api.v1.security.jwt.services;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import net.duckycraftmc.remotehost.api.v1.security.token.TokenRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LogoutService implements LogoutHandler {

    private final TokenRepository tokenRepository;
    private final JWTService jwtService;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        final String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer "))
            return;

        String jwt = authHeader.split(" ")[1].trim();
        tokenRepository.findByValue(jwt).ifPresent(tokenRepository::delete);
        jwtService.purgeExpiredTokens();
    }

}
