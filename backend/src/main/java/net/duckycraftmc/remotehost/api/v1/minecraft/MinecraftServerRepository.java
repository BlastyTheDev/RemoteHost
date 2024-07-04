package net.duckycraftmc.remotehost.api.v1.minecraft;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MinecraftServerRepository extends JpaRepository<MinecraftServer, Integer> {

    // TODO: This is vulnerable to SQL injection. This should be dealt with before production
    @Query(value = "select * from `minecraft_servers` where name = :name", nativeQuery = true)
    Optional<MinecraftServer> findByName(String name);

    @Query(value = "select * from `minecraft_servers` where owner_id = :ownerId", nativeQuery = true)
    List<MinecraftServer> findAllByOwnerId(Integer ownerId);

}
