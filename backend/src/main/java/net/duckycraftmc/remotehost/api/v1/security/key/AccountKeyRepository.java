package net.duckycraftmc.remotehost.api.v1.security.key;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface AccountKeyRepository extends JpaRepository<AccountKey, Integer> {

    // TODO: This is vulnerable to SQL injection. This should be dealt with before production
    @Query(value = "select * from `account_keys` where value = :value", nativeQuery = true)
    Optional<AccountKey> findByValue(String value);

}
