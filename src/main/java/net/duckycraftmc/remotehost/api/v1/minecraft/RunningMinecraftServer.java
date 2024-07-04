package net.duckycraftmc.remotehost.api.v1.minecraft;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.*;

@RequiredArgsConstructor
public class RunningMinecraftServer {

    @Getter
    private final MinecraftServer server;

    private Process process;
    private BufferedReader reader;
    private BufferedReader errorReader;
    private BufferedWriter writer;

    public RunningMinecraftServer start() throws IOException {
        ProcessBuilder pb = new ProcessBuilder("bash", "start.sh");
        pb.directory(new File("servers/" + server.getId()));
        process = pb.start();
        // for console input and output
        reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
        monitorConsole();
        return this;
    }

    private void monitorConsole() {
        // TODO: implement streaming to client, stop using System.out.println() when streaming is ready
        new Thread(() -> {
            try {
                String line;
                while ((line = reader.readLine()) != null)
                    System.out.println(line);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();
        new Thread(() -> {
            try {
                String line;
                while ((line = errorReader.readLine()) != null)
                    System.err.println(line);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    public void sendCommand(String command) throws IOException {
        writer.write(command + "\n");
        writer.flush();
    }

    public void stop() throws IOException {
        sendCommand("stop");
    }

}
