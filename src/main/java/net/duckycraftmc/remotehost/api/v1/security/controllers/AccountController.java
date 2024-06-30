package net.duckycraftmc.remotehost.api.v1.security.controllers;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import net.duckycraftmc.remotehost.api.v1.security.key.AccountKey;
import net.duckycraftmc.remotehost.api.v1.security.key.AccountKeyRepository;
import net.duckycraftmc.remotehost.api.v1.security.user.AccountTier;
import net.duckycraftmc.remotehost.api.v1.security.user.User;
import net.duckycraftmc.remotehost.api.v1.security.user.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/account")
@RequiredArgsConstructor
public class AccountController {

    private final AccountKeyRepository keyRepository;
    private final UserRepository userRepository;

    // account privilege key
    @PostMapping("/add-key")
    public void addKey(@RequestBody String key, HttpServletResponse response) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (user.getTier() != AccountTier.UNVERIFIED) {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            return;
        }
        if (!user.getDiscordVerified()) {
            response.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
            return;
        }
        Optional<AccountKey> keyOptional = keyRepository.findByValue(key);
        if (keyOptional.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        AccountTier tierToAssignUser = keyOptional.get().getTier();
        user.setTier(tierToAssignUser);
        userRepository.save(user);
        keyOptional.get().setUsed(true);
        keyOptional.get().setUserId(user.getId());
        keyRepository.save(keyOptional.get());
    }

}
