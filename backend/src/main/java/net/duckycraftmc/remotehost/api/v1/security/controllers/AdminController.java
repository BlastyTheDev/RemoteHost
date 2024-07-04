package net.duckycraftmc.remotehost.api.v1.security.controllers;

import lombok.RequiredArgsConstructor;
import net.duckycraftmc.remotehost.api.v1.security.key.AccountKey;
import net.duckycraftmc.remotehost.api.v1.security.key.AccountKeyRepository;
import net.duckycraftmc.remotehost.api.v1.security.user.AccountTier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AccountKeyRepository keyRepository;

    @GetMapping("/create-key")
    public String createKey(@RequestBody String tier) {
        int leftLimit = 48; // number '0'
        int rightLimit = 122; // letter 'z'
        int keyLength = 16;
        Random random = new Random();
        String generatedString = random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97)).limit(keyLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
        // insert - every 4 characters
        String key = generatedString.substring(0, 4) + "-"
                + generatedString.substring(4, 8) + "-"
                + generatedString.substring(8, 12) + "-"
                + generatedString.substring(12, 16);
        keyRepository.save(AccountKey.builder()
                .value(key)
                .tier(AccountTier.valueOf(tier.toUpperCase()))
                .used(false)
                .build());
        return key;
    }

}
