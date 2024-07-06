package net.duckycraftmc.remotehost.api.v1.security.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import net.duckycraftmc.remotehost.api.v1.security.LoginRequest;
import net.duckycraftmc.remotehost.api.v1.security.SignupRequest;
import net.duckycraftmc.remotehost.api.v1.security.jwt.services.JWTService;
import net.duckycraftmc.remotehost.api.v1.security.user.AccountTier;
import net.duckycraftmc.remotehost.api.v1.security.user.User;
import net.duckycraftmc.remotehost.api.v1.security.user.UserRepository;
import net.duckycraftmc.remotehost.discord.DiscordBot;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final JWTService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authManager;

    private final HashMap<String, User> authenticatedSessionIds;

    private final DiscordBot discordBot;

    @PostMapping("/login")
    public String login(@RequestBody LoginRequest loginRequest, HttpServletRequest request, HttpServletResponse response) {
        authManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
        User user = userRepository.findByUsername(loginRequest.getUsername()).orElseThrow();
        return sendNewToken(user, request);
    }

    @PostMapping("/signup")
    public String signup(@RequestBody SignupRequest signupRequest, HttpServletRequest request, HttpServletResponse response) {
        if (userRepository.findByUsername(signupRequest.getUsername()).isPresent()) {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            return "username unavailable";
        }
        if (!discordBot.isUserValid(signupRequest.getDiscord())) {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            return "discord unavailable";
        }
        User user = User.builder()
                .username(signupRequest.getUsername())
                .password(passwordEncoder.encode(signupRequest.getPassword()))
                .discord(signupRequest.getDiscord().toLowerCase())
                .discordVerified(false)
                .tier(AccountTier.UNVERIFIED)
                .build();
        response.setStatus(HttpServletResponse.SC_CREATED);
        return sendNewToken(userRepository.save(user), request);
    }

    private String sendNewToken(User user, HttpServletRequest request) {
        authenticatedSessionIds.put(request.getSession().getId(), user);
        return jwtService.createToken(user);
    }

}
