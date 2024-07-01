package net.duckycraftmc.remotehost.api.v1.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import net.duckycraftmc.remotehost.api.v1.minecraft.CreateRequest;
import net.duckycraftmc.remotehost.api.v1.minecraft.MinecraftServer;
import net.duckycraftmc.remotehost.api.v1.minecraft.MinecraftServerRepository;
import net.duckycraftmc.remotehost.api.v1.minecraft.MinecraftServerType;
import net.duckycraftmc.remotehost.api.v1.security.user.AccountTier;
import net.duckycraftmc.remotehost.api.v1.security.user.UsageQuotas;
import net.duckycraftmc.remotehost.api.v1.security.user.User;
import net.duckycraftmc.remotehost.api.v1.security.user.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/minecraft")
@RequiredArgsConstructor
public class MinecraftServerController {

    private final UserRepository userRepository;
    private final MinecraftServerRepository serverRepository;

    @PostMapping("/create")
    public MinecraftServer createServer(@RequestBody CreateRequest createRequest, HttpServletRequest request, HttpServletResponse response) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (user.getTier() == AccountTier.UNVERIFIED) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }

        // check usage quotas
        // not final; subject to change in the future
        int userServerCount = serverRepository.findAllByOwnerId(user.getId()).size();
        AccountTier userTier = user.getTier();
        if (userTier == AccountTier.BASIC && userServerCount >= UsageQuotas.Basic.MAX_SERVERS
                || userTier == AccountTier.ENHANCED && userServerCount >= UsageQuotas.Enhanced.MAX_SERVERS
                || userTier == AccountTier.ADVANCED && userServerCount >= UsageQuotas.Advanced.MAX_SERVERS
                || userTier == AccountTier.FULL && userServerCount >= UsageQuotas.Full.MAX_SERVERS) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }
        if (userTier == AccountTier.BASIC && createRequest.getRam() >= UsageQuotas.Basic.MAX_RAM
                || userTier == AccountTier.ENHANCED && createRequest.getRam() >= UsageQuotas.Enhanced.MAX_RAM
                || userTier == AccountTier.ADVANCED && createRequest.getRam() >= UsageQuotas.Advanced.MAX_RAM
                || userTier == AccountTier.FULL && createRequest.getRam() >= UsageQuotas.Full.MAX_RAM) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }

        // create server
        MinecraftServer server = MinecraftServer.builder()
                .name(createRequest.getName())
                .ram(createRequest.getRam())
                .serverType(MinecraftServerType.valueOf(createRequest.getType().toUpperCase()))
                .ownerId(user.getId())
                .build();
        return serverRepository.save(server);
    }

}
