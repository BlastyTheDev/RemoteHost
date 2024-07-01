package net.duckycraftmc.remotehost.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;

public class DownloadServerJar {

    public static void downloadFromPaperAPI(Path path, String software, String version, String build) throws IOException, InterruptedException {
        if (build == null || build.isBlank())
            build = getLatestPaperAPIBuild(software, version);

        HttpClient client = HttpClient.newHttpClient();

        HttpRequest downloadRequest = HttpRequest.newBuilder()
                .uri(URI.create("https://api.papermc.io/v2/projects/" + software + "/versions/" + version + "/builds/"
                        + build + "/downloads/" + software + "-" + version + "-" + build + ".jar"))
                .GET()
                .headers("accept", "application/json")
                .build();

        byte[] downloadBytes = client.send(downloadRequest, HttpResponse.BodyHandlers.ofString()).body().getBytes();
        writeToFile(downloadBytes, path + software + "-" + version + "-" + build + ".jar");
    }

    private static String getLatestPaperAPIBuild(String software, String version) throws IOException, InterruptedException {
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
        return buildNumbers[buildNumbers.length - 1];
    }

    public static void downloadPurpur(Path path, String version, String build) throws IOException, InterruptedException {
        if (build.isBlank())
            build = getLatestPurpurBuild(version);

        HttpClient client = HttpClient.newHttpClient();

        HttpRequest downloadRequest = HttpRequest.newBuilder()
                .uri(URI.create("https://api.purpurmc.org/v2/purpur/" + version + "/" + build + "/download"))
                .GET()
                .headers("accept", "application/json")
                .build();

        byte[] downloadBytes = client.send(downloadRequest, HttpResponse.BodyHandlers.ofByteArray()).body();
        writeToFile(downloadBytes, path + "purpur-" + version + "-" + build + ".jar");
    }

    private static String getLatestPurpurBuild(String version) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        ObjectMapper mapper = new ObjectMapper();
        HttpRequest versionsRequest = HttpRequest.newBuilder()
                .uri(URI.create("https://api.purpurmc.org/v2/purpur/" + version))
                .GET()
                .headers("accept", "*/*")
                .build();
        HttpResponse<String> versionsResponse = client.send(versionsRequest, HttpResponse.BodyHandlers.ofString());
        JsonNode buildsNode = mapper.readValue(versionsResponse.body(), JsonNode.class);
        return buildsNode.get("builds").get("latest").asText();
    }

    private static void writeToFile(byte[] bytes, String pathname) throws IOException {
        File file = new File(pathname);
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(bytes);
    }

}
