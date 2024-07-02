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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final JWTService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authManager;

    @PostMapping("/login")
    public String login(@RequestBody LoginRequest loginRequest, HttpServletRequest request, HttpServletResponse response) {
        authManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
        User user = userRepository.findByUsername(loginRequest.getUsername()).orElseThrow();
        return sendNewToken(user);
    }

    @PostMapping("/signup")
    public String signup(@RequestBody SignupRequest signupRequest, HttpServletRequest request, HttpServletResponse response) {
        if (userRepository.findByUsername(signupRequest.getUsername()).isPresent()) {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            return null;
        }
        User user = User.builder()
                .username(signupRequest.getUsername())
                .password(passwordEncoder.encode(signupRequest.getPassword()))
                .discord(signupRequest.getDiscord())
                .discordVerified(false)
                .tier(AccountTier.UNVERIFIED)
                .build();
        response.setStatus(HttpServletResponse.SC_CREATED);
        return sendNewToken(userRepository.save(user));
    }

    private String sendNewToken(User user) {
        return jwtService.createToken(user);
    }

}
