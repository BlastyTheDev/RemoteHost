package net.duckycraftmc.remotehost.servers.minecraft.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class DownloadServerJar {

    public static void downloadFromPaperAPI(String software, String version, int build) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();

        HttpRequest downloadRequest = HttpRequest.newBuilder()
                .uri(URI.create("https://api.papermc.io/v2/projects/" + software + "/versions/" + version + "/builds/"
                        + build + "/downloads/" + software + "-" + version + "-" + build + ".jar"))
                .GET()
                .headers("accept", "application/json")
                .build();

        byte[] downloadBytes = client.send(downloadRequest, HttpResponse.BodyHandlers.ofString()).body().getBytes();
        writeToFile(downloadBytes, software + "-" + version + "-" + build + ".jar");
    }

    public static void downloadFromPaperAPI(String software, String version) throws IOException, InterruptedException {
        downloadFromPaperAPI(software, version, getLatestPaperAPIBuild(software, version));
    }

    private static int getLatestPaperAPIBuild(String software, String version) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        ObjectMapper mapper = new ObjectMapper();
        HttpRequest versionsRequest = HttpRequest.newBuilder()
                .uri(URI.create("https://api.papermc.io/v2/projects/" + software + "/versions/" + version))
                .GET()
                .headers("accept", "application/json")
                .build();
        HttpResponse<String> versionsResponse = client.send(versionsRequest, HttpResponse.BodyHandlers.ofString());
        JsonNode buildsNode = mapper.readValue(versionsResponse.body(), JsonNode.class);
        String rawBuildNumbers = buildsNode.get("builds").toString();
        String[] buildNumbers = rawBuildNumbers.substring(1, rawBuildNumbers.length() - 1).split(",");
        return Integer.parseInt(buildNumbers[buildNumbers.length - 1]);
    }

    private static void writeToFile(byte[] bytes, String pathname) throws IOException {
        File file = new File(pathname);
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(bytes);
    }

}
