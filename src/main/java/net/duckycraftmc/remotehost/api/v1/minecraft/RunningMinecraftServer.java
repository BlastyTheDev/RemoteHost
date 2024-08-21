package net.duckycraftmc.remotehost.api.v1.minecraft;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.*;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

@RequiredArgsConstructor
public class RunningMinecraftServer {

    @Getter
    private final MinecraftServer server;

    private Process process;
    private BufferedReader reader;
    private BufferedReader errorReader;
    private BufferedWriter writer;

    private final ConsoleWebSocketHandler consoleWebSocketHandler;

    public RunningMinecraftServer start() throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder("bash", "start.sh");
        pb.directory(new File("servers/" + server.getId()));
        if (process != null && process.isAlive())
            process.waitFor();
        process = pb.start();
        // for console input and output
        reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
        monitorConsole();
        return this;
    }

    private void monitorConsole() {
        // sends console output to all sessions watching this server's console
        new Thread(() -> {
            try {
                String line;
                while ((line = reader.readLine()) != null)
                    for (WebSocketSession session : consoleWebSocketHandler.getMinecraftServerConsoleSessions().keySet())
                        if (Objects.equals(consoleWebSocketHandler.getMinecraftServerConsoleSessions().get(session), server.getId()))
                            session.sendMessage(new TextMessage(line));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();
        new Thread(() -> {
            try {
                String line;
                while ((line = errorReader.readLine()) != null)
                    for (WebSocketSession session : consoleWebSocketHandler.getMinecraftServerConsoleSessions().keySet())
                        if (Objects.equals(consoleWebSocketHandler.getMinecraftServerConsoleSessions().get(session), server.getId()))
                            session.sendMessage(new TextMessage(line));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    public void sendCommand(String command) throws IOException, InterruptedException {
        if (process == null || !process.isAlive())
            return;
        if (command.equalsIgnoreCase("restart"))
            restart();
        else {
            writer.write(command + "\n");
            writer.flush();
        }
    }
    
    public boolean stop() throws IOException, InterruptedException {
        sendCommand("stop");
        process.waitFor();
        return !process.isAlive();
    }

    // may keep world locked, unable to start server again
    // avoid using the api endpoint related to this method
    public void forceStop() {
        process.destroy();
    }

    public void restart() throws IOException, InterruptedException {
        stop();
        start();
    }

    private synchronized String getListPlayers() throws IOException, InterruptedException {
        sendCommand("list");

        AtomicReference<String> line = new AtomicReference<>();
        
//        new Thread(() -> {
//            while (true) {
//                try {
//                    if (reader.readLine() == null)
//                        break;
//                    
//                    line.set(reader.readLine());
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
//                
//                if (line.get().substring(17).startsWith("There are ")) {
//                    line.set(line.get().substring(17));
//                    System.out.println(line.get());
//                }
//            }
//        }).start();

        return line.get();
    }

    public Integer getOnlinePlayers() throws IOException, InterruptedException {
        var listPlayersResult = getListPlayers();

        if (listPlayersResult == null)
            return 0;

        return Integer.parseInt(listPlayersResult.split(" ")[2].split("/")[0]);
    }

    public Integer getMaxPlayers() throws IOException, InterruptedException {
        var listPlayersResult = getListPlayers();

        if (listPlayersResult == null)
            return 0;

        return Integer.parseInt(listPlayersResult.split(" ")[2].split("/")[1]);
    }

}
