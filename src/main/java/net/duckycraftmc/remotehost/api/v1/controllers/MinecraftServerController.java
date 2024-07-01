package net.duckycraftmc.remotehost.api.v1.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import net.duckycraftmc.remotehost.api.v1.minecraft.CreateRequest;
import net.duckycraftmc.remotehost.api.v1.minecraft.MinecraftServer;
import net.duckycraftmc.remotehost.api.v1.minecraft.MinecraftServerRepository;
import net.duckycraftmc.remotehost.api.v1.minecraft.MinecraftServerType;
import net.duckycraftmc.remotehost.api.v1.security.user.AccountTier;
import net.duckycraftmc.remotehost.api.v1.security.user.User;
import net.duckycraftmc.remotehost.api.v1.security.user.UserRepository;
import net.duckycraftmc.remotehost.api.v1.security.user.quotas.UsageQuota;
import net.duckycraftmc.remotehost.util.DownloadServerJar;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/api/v1/minecraft")
@RequiredArgsConstructor
public class MinecraftServerController {

    private final UserRepository userRepository;
    private final MinecraftServerRepository serverRepository;

    @PostMapping("/create")
    public MinecraftServer createServer(@RequestBody CreateRequest createRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, InterruptedException {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (user.getTier() == AccountTier.UNVERIFIED) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }

        // check usage quotas
        // not final; subject to change in the future
        UsageQuota quota = UsageQuota.getInstance().getQuotas(user.getTier());
        
        int userServerCount = serverRepository.findAllByOwnerId(user.getId()).size();
        AccountTier userTier = user.getTier();
        if (userTier != AccountTier.ADMIN) {
            if (userServerCount > quota.MAX_SERVERS()) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return null;
            }
            if (createRequest.getRam() > quota.MAX_RAM()) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return null;
            }
            boolean isPluginServer = createRequest.getType().equalsIgnoreCase(MinecraftServerType.PAPER.toString())
                    || createRequest.getType().equalsIgnoreCase(MinecraftServerType.SPIGOT.toString())
                    || createRequest.getType().equalsIgnoreCase(MinecraftServerType.PURPUR.toString());
            boolean isModdedServer = createRequest.getType().equalsIgnoreCase(MinecraftServerType.FABRIC.toString())
                    || createRequest.getType().equalsIgnoreCase(MinecraftServerType.FORGE.toString());
            if (isPluginServer && !quota.CAN_USE_PLUGIN_SERVERS()) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return null;
            }
            if (isModdedServer && !quota.CAN_USE_MODDED_SERVERS()) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return null;
            }
        }

        // create server
        MinecraftServer requestedServer = MinecraftServer.builder()
                .name(createRequest.getName())
                .ram(createRequest.getRam())
                .serverType(MinecraftServerType.valueOf(createRequest.getType().toUpperCase()))
                .ownerId(user.getId())
                .build();
        MinecraftServer server = serverRepository.save(requestedServer);

        File serverDir = new File("servers/" + server.getId());
        if (!serverDir.exists())
            serverDir.mkdir();

        switch (server.getServerType()) {
            case PAPER -> DownloadServerJar.downloadFromPaperAPI(serverDir.toPath(), "papermc", createRequest.getVersion(), createRequest.getBuild());
            case PURPUR -> DownloadServerJar.downloadPurpur(serverDir.toPath(), createRequest.getVersion(), createRequest.getBuild());
        }

        return server;
    }

}
