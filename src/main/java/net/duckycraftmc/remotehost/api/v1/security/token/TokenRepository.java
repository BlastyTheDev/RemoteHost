package net.duckycraftmc.remotehost.api.v1.security.token;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, String> {

    @Query(value = "select * from `tokens` where value = :value", nativeQuery = true)
    Optional<Token> findByValue(String value);

}
