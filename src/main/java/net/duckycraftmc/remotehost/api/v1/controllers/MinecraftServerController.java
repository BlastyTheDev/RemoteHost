package net.duckycraftmc.remotehost.api.v1.controllers;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.duckycraftmc.remotehost.api.v1.minecraft.*;
import net.duckycraftmc.remotehost.api.v1.security.user.AccountTier;
import net.duckycraftmc.remotehost.api.v1.security.user.User;
import net.duckycraftmc.remotehost.api.v1.security.user.UserRepository;
import net.duckycraftmc.remotehost.api.v1.security.user.quotas.UsageQuota;
import net.duckycraftmc.remotehost.util.DownloadServerJar;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

import static net.duckycraftmc.remotehost.util.ValidationHelper.isUserOwnerOrCoOwner;

@RestController
@RequestMapping("/api/v1/minecraft")
@RequiredArgsConstructor
public class MinecraftServerController {

    private final MinecraftServerRepository serverRepository;
    private final UserRepository userRepository;

    private final ConsoleWebSocketHandler consoleWebSocketHandler;

    @Getter
    private final List<RunningMinecraftServer> servers = new ArrayList<>();

    @GetMapping("/list-running")
    public List<MinecraftServer> listRunningServers() {
        List<MinecraftServer> running = new ArrayList<>();
        for (RunningMinecraftServer server : servers)
            running.add(server.getServer());
        return running;
    }

    @PostMapping("/create")
    public MinecraftServer createServer(@RequestBody CreateRequest createRequest, HttpServletResponse response) throws IOException, InterruptedException {
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

    private boolean isUserNotAuthenticated(MinecraftServer server, User user, HttpServletResponse response) {
        if (server == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return true;
        }
        if (!isUserOwnerOrCoOwner(user, server, userRepository)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return true;
        }
        return false;
    }

    private RunningMinecraftServer getRunningMinecraftServerCheckingAuthentication(Integer serverId, HttpServletResponse response) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        MinecraftServer server = serverRepository.findById(serverId).orElse(null);

        if (isUserNotAuthenticated(server, user, response))
            return null;

        RunningMinecraftServer runningServer = servers.stream().filter(s -> s.getServer().getId().equals(serverId)).findFirst().orElse(null);
        if (runningServer == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }

        return runningServer;
    }

    @PostMapping("/start")
    public void startServer(@RequestParam(name = "server") Integer serverId, HttpServletResponse response) throws IOException, InterruptedException {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        MinecraftServer server = serverRepository.findById(serverId).orElse(null);

        if (isUserNotAuthenticated(server, user, response))
            return;

        assert server != null;
        File propertiesFile = new File("servers/" + server.getId() + "/server.properties");
        if (propertiesFile.exists()) {
            Properties properties = new Properties();
            properties.load(new FileInputStream(propertiesFile));
            if (!Objects.equals(properties.getProperty("server-port"), String.valueOf(25565))
                    && serverId != 1) {
                int portIncrement = serverRepository.findAll().size();
                properties.setProperty("server-port", String.valueOf(25565 + portIncrement));
                properties.store(new FileOutputStream(propertiesFile), null);
            }
        }

        servers.add(new RunningMinecraftServer(server, consoleWebSocketHandler).start());
    }

    @PostMapping("/send-command")
    public void sendCommand(@RequestParam(name = "server") Integer serverId, @RequestBody String command, HttpServletResponse response) throws IOException, InterruptedException {
        var runningServer = getRunningMinecraftServerCheckingAuthentication(serverId, response);

        if (runningServer == null)
            return;

        runningServer.sendCommand(command);
    }

    @PostMapping("/stop")
    public void stopServer(@RequestParam(name = "server") Integer serverId, HttpServletResponse response) throws IOException, InterruptedException {
        var runningServer = getRunningMinecraftServerCheckingAuthentication(serverId, response);

        if (runningServer == null)
            return;

        if (!runningServer.stop())
            runningServer.forceStop();
        servers.remove(runningServer); // TODO: this line doesnt run for some reason?
    }

    @PostMapping("/restart")
    public void restartServer(@RequestParam(name = "server") Integer serverId, HttpServletResponse response) throws IOException, InterruptedException {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        MinecraftServer server = serverRepository.findById(serverId).orElse(null);
        if (server == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        if (!isUserOwnerOrCoOwner(user, server, userRepository)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        RunningMinecraftServer runningServer = servers.stream().filter(s -> s.getServer().getId().equals(serverId)).findFirst().orElse(null);
        if (runningServer == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        runningServer.restart();
    }

    // hopefully will never have to be used, admin only
    @PostMapping("/force-stop")
    public void forceStopServer(@RequestParam(name = "server") Integer serverId, HttpServletResponse response) {
        MinecraftServer server = serverRepository.findById(serverId).orElse(null);
        if (server == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        RunningMinecraftServer runningServer = servers.stream().filter(s -> s.getServer().getId().equals(serverId)).findFirst().orElse(null);
        if (runningServer == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        runningServer.forceStop();
        servers.remove(runningServer);
    }

    @GetMapping("/get-server-info")
    public MinecraftServer getServerInfo(@RequestParam(name = "server") Integer serverId, HttpServletResponse response) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        MinecraftServer server = serverRepository.findById(serverId).orElse(null);

        if (isUserNotAuthenticated(server, user, response))
            return null;

        return server;
    }

    @GetMapping("/get-owned-servers")
    public List<MinecraftServerInfo> getOwnedServers() throws IOException, InterruptedException {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        var userServers = serverRepository.findAllByOwnerId(user.getId());

        var serverInfos = new ArrayList<MinecraftServerInfo>();

        for (var server : userServers) {
            var serverInfo = new MinecraftServerInfo();
            serverInfo.setId(server.getId());
            serverInfo.setName(server.getName());
            serverInfo.setAddress(user.getUsername() + Dotenv.load().get("SERVER_ADDRESS"));
            
            boolean serverOnline = servers.stream().anyMatch(s -> s.getServer().getId().equals(server.getId()));
            RunningMinecraftServer runningServer = serverOnline
                    ? servers.stream().filter(s -> s.getServer().getId().equals(server.getId())).findFirst().orElse(null)
                    : null;
            
            serverInfo.setStatus(serverOnline && runningServer != null ? "Online" : "Offline");
            // getMaxPlayers and getOnlinePlayers block the console reading thread
            serverInfo.setMaxPlayers(/*serverOnline && runningServer != null ? runningServer.getMaxPlayers() : */0);
            serverInfo.setPlayersOnline(/*serverOnline && runningServer != null ? runningServer.getOnlinePlayers() : */0);
            
            serverInfos.add(serverInfo);
        }

        return serverInfos;
    }

    @GetMapping("/get-server-icon")
    public byte[] getServerIcon(@RequestParam(name = "server") Integer serverId, HttpServletResponse response) throws IOException {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        MinecraftServer server = serverRepository.findById(serverId).orElse(null);

        if (isUserNotAuthenticated(server, user, response))
            return null;

        assert server != null;
        File icon = new File("servers/" + server.getId() + "/server-icon.png");
        if (!icon.exists()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }

        byte[] bytes = new byte[(int) icon.length()];
        FileInputStream fis = new FileInputStream(icon);
        fis.read(bytes);
        fis.close();

        return bytes;
    }

}
