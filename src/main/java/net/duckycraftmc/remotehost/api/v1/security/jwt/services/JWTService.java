package net.duckycraftmc.remotehost.api.v1.security.jwt.services;

import com.auth0.jwt.JWT;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import net.duckycraftmc.remotehost.api.v1.security.token.Token;
import net.duckycraftmc.remotehost.api.v1.security.token.TokenRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.sql.Date;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class JWTService {

    // For debug and development ONLY
    // Use environment variables for production
    private static final String KEY = "790dbe2ba4c602fc6d293a5eaad7b0e68e201898c742e512970755d3fa568dbe";

    private final TokenRepository tokenRepository;

    private Key getSigningKey() {
        byte[] bytes = Decoders.BASE64.decode(KEY);
        return Keys.hmacShaKeyFor(bytes);
    }

    public String createToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        // expire in 30 days (last number in multiplication is number of days in the future)
        Date expiration = new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 30);
        String createdToken = Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(expiration)
                .signWith(getSigningKey())
                .compact();
        tokenRepository.save(Token.builder()
                .value(createdToken)
                .build());
        return createdToken;
    }

    public String createToken(UserDetails userDetails) {
        return createToken(Map.of(), userDetails);
    }

    public String getSubject(String jwt) {
        return JWT.decode(jwt).getSubject();
    }

    public boolean isTokenExpired(String jwt) {
        return JWT.decode(jwt).getExpiresAt().before(new Date(System.currentTimeMillis()));
    }

    // valid if not expired, subject matches user, and token is in database (known)
    public boolean isTokenValid(String jwt, UserDetails userDetails) {
        return !isTokenExpired(jwt) && getSubject(jwt).equals(userDetails.getUsername())
                && tokenRepository.findByValue(jwt).isPresent();
    }

}
