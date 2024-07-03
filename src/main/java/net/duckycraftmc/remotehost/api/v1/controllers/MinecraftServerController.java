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
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.util.Objects;

@RestController
@RequestMapping("/api/v1/minecraft")
@RequiredArgsConstructor
public class MinecraftServerController {

    private final MinecraftServerRepository serverRepository;
    private final UserRepository userRepository;

    @PostMapping("/create")
    public MinecraftServer createServer(@RequestBody CreateRequest createRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, InterruptedException {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (user.getTier() == AccountTier.UNVERIFIED) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }

        if (serverRepository.findByName(createRequest.getName()).isPresent()) {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
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
            serverDir.mkdirs();

        String serverJarName = "";

        switch (server.getServerType()) {
            case PAPER ->
                    serverJarName = DownloadServerJar.downloadFromPaperAPI(serverDir, "paper", createRequest.getVersion(), createRequest.getBuild());
            case PURPUR ->
                    serverJarName = DownloadServerJar.downloadPurpur(serverDir, createRequest.getVersion(), createRequest.getBuild());
        }

        File startScript = new File(serverDir, "start.sh");
        startScript.createNewFile();

        String startScriptContent = "#!/bin/bash\n" +
                "java -Xmx" + server.getRam() + "M -jar " + serverJarName + " nogui";

        FileOutputStream fos = new FileOutputStream(startScript);
        fos.write(startScriptContent.getBytes());

        return server;
    }

    @RequestMapping("/start")
    public void startServer(@RequestParam(name = "server") Integer serverId, HttpServletRequest request, HttpServletResponse response) throws IOException {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        MinecraftServer server = serverRepository.findById(serverId).orElse(null);
        if (server == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        if (!Objects.equals(server.getOwnerId(), user.getId()) && !server.getCoOwners(userRepository).contains(user)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        ProcessBuilder pb = new ProcessBuilder("bash", "start.sh");
        pb.directory(new File("servers/" + serverId));
        Process process = pb.start();

        // debug, makes sure program stays running
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        new Thread(() -> {
            String line;
            try {
                while ((line = reader.readLine()) != null)
                    System.out.println(line);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();
        new Thread(() -> {
            String line;
            try {
                while ((line = errorReader.readLine()) != null)
                    System.err.println(line);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

}
