package net.duckycraftmc.remotehost.servers.minecraft.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class DownloadServerJar {

    public static void downloadLatestPaper(String version) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        ObjectMapper mapper = new ObjectMapper();

        // get latest build version
        HttpRequest versionsRequest = HttpRequest.newBuilder()
                .uri(URI.create("https://api.papermc.io/v2/projects/paper/versions/" + version))
                .GET()
                .headers("accept", "application/json")
                .build();
        HttpResponse<String> versionsResponse = client.send(versionsRequest, HttpResponse.BodyHandlers.ofString());
        JsonNode buildsNode = mapper.readValue(versionsResponse.body(), JsonNode.class);
        String rawBuildNumbers = buildsNode.get("builds").toString();
        String[] buildNumbers = rawBuildNumbers.substring(1, rawBuildNumbers.length() - 1).split(",");
        int latestBuild = Integer.parseInt(buildNumbers[buildNumbers.length - 1]);

        // download latest build
        HttpRequest downloadRequest = HttpRequest.newBuilder()
                .uri(URI.create("https://api.papermc.io/v2/projects/paper/versions/" + version + "/builds/"
                        + latestBuild + "/downloads/paper-" + version + "-" + latestBuild + ".jar"))
                .GET()
                .build();
    }

}
