package net.duckycraftmc.remotehost.config;

import lombok.RequiredArgsConstructor;
import net.duckycraftmc.remotehost.api.v1.security.jwt.JWTAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final AuthenticationProvider authProvider;
    private final JWTAuthFilter jwtAuthFilter;
    private final LogoutHandler logoutHandler;

    private final String[] needAuthentication = {
            "/api/v1/discord/verify",
            "/api/v1/account/add-key",
            "/api/v1/minecraft",
            "/api/v1/minecraft/**",
    };

    private final String[] needAdmin = {
            "/api/v1/admin/**",
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .authenticationProvider(authProvider)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers(needAdmin).hasAuthority("ADMIN");
                    auth.requestMatchers(needAuthentication).authenticated();
                    auth.anyRequest().permitAll();
                })
                .logout(logout -> {
                    logout.logoutUrl("/api/v1/auth/logout");
                    logout.addLogoutHandler(logoutHandler);
                    logout.logoutSuccessHandler((request, response, authentication)
                            -> SecurityContextHolder.clearContext());
                    logout.permitAll();
                })
                .build();
    }

}
