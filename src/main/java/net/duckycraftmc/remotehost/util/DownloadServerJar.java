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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class DownloadServerJar {

    public static String downloadFromPaperAPI(File parentPath, String software, String version, String build) throws IOException, InterruptedException {
        if (build == null || build.isBlank())
            build = getLatestPaperAPIBuild(software, version);

        String jarName = software + "-" + version + "-" + build + ".jar";

        // writeToFile() does not work correctly for paper downloads
        Files.copy(URI.create("https://api.papermc.io/v2/projects/" + software + "/versions/" + version + "/builds/"
                + build + "/downloads/" + software + "-" + version + "-" + build + ".jar").toURL().openStream(),
                Paths.get(parentPath.getPath() + "/" + jarName), StandardCopyOption.REPLACE_EXISTING);

        return jarName;
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

    public static String downloadPurpur(File parentPath, String version, String build) throws IOException, InterruptedException {
        if (build == null || build.isBlank())
            build = getLatestPurpurBuild(version);

        HttpClient client = HttpClient.newHttpClient();

        HttpRequest downloadRequest = HttpRequest.newBuilder()
                .uri(URI.create("https://api.purpurmc.org/v2/purpur/" + version + "/" + build + "/download"))
                .GET()
                .headers("accept", "application/json")
                .build();

        byte[] downloadBytes = client.send(downloadRequest, HttpResponse.BodyHandlers.ofByteArray()).body();
        String jarName = "purpur-" + version + "-" + build + ".jar";
        writeToFile(downloadBytes, parentPath, jarName);
        return jarName;
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

    private static void writeToFile(byte[] bytes, File parentPath, String fileName) throws IOException {
        File file = new File(parentPath, fileName);
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(bytes);
    }

}
