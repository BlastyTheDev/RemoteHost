package net.duckycraftmc.remotehost.api.v1.security.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    @Query(value = "select * from `users` where username = :username", nativeQuery = true)
    Optional<User> findByUsername(String username);

    @Query(value = "select * from `users` where discord = :discord", nativeQuery = true)
    Optional<User> findByDiscord(String discord);

}
