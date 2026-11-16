package com.infinity.somayeh;

import com.sun.net.httpserver.*;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public final class RouteRegistry {

  private static final String VIDEO_MODEL =
      "lightricks/ltx-2-5-fast";

  private static final HttpClient CLIENT =
      HttpClient.newBuilder()
          .connectTimeout(Duration.ofSeconds(20))
          .build();

  public static void register(HttpServer server) {
    server.createContext("/api/video",
        ex -> media(ex, "Video_Somayeh-AI", "video"));

    server.createContext("/api/image",
        ex -> media(ex, "Image_Somayeh-AI", "image"));

    server.createContext("/api/music",
        ex -> media(ex, "Music_Somayeh-AI", "music"));
  }

  static void media(
      HttpExchange ex,
      String keyName,
      String kind
  ) throws IOException {

    if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) {
      ServerMain.json(ex, 405,
          "{\"ok\":false,\"error\":\"POST required\"}");
      return;
    }

    String body = new String(
        ex.getRequestBody().readAllBytes(),
        StandardCharsets.UTF_8
    );

    String prompt = JsonUtil.field(body, "prompt");

    if (prompt == null || prompt.isBlank()) {
      ServerMain.json(ex, 400,
          "{\"ok\":false,\"error\":\"prompt_required\"}");
      return;
    }

    if ("video".equals(kind)) {
      generateVideo(ex, keyName, prompt);
      return;
    }

    String json =
        "{\"ok\":false,\"configured\":false,"
        + "\"kind\":\"" + kind + "\","
        + "\"error\":\"Provider adapter not implemented.\"}";

    ServerMain.json(ex, 501, json);
  }

  private static void generateVideo(
      HttpExchange ex,
      String keyName,
      String prompt
  ) throws IOException {
String accountId =
    Config.requireKey("CLOUDFLARE_ACCOUNT_ID");

String token =
    Config.requireKey("Video_Somayeh-AI");

if (token.isEmpty()) {
    token = Config.requireKey("VIDEO_SOMAYEH_AI");
}
    
    if (token.isEmpty()) {
      token = Config.requireKey(keyName);
    }

    if (accountId.isEmpty() || token.isEmpty()) {
      ServerMain.json(ex, 503,
          "{\"ok\":false,\"error\":\"Cloudflare credentials missing\"}");
      return;
    }

    if (!accountId.matches("[0-9a-fA-F]{32}")) {
      ServerMain.json(ex, 400,
          "{\"ok\":false,\"error\":\"Invalid Cloudflare Account ID\"}");
      return;
    }

    String payload =
        "{\"model\":\"" + VIDEO_MODEL + "\","
        + "\"input\":{"
        + "\"prompt\":\"" + JsonUtil.esc(prompt) + "\","
        + "\"duration\":8,"
        + "\"resolution\":\"1280x720\","
        + "\"fps\":24,"
        + "\"generate_audio\":true"
        + "}}";

    String endpoint =
        "https://api.cloudflare.com/client/v4/accounts/"
        + accountId
        + "/ai/run";

    try {
      HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create(endpoint))
          .timeout(Duration.ofSeconds(240))
          .header("Authorization", "Bearer " + token)
          .header("Content-Type", "application/json")
          .POST(HttpRequest.BodyPublishers.ofString(
              payload, StandardCharsets.UTF_8))
          .build();

      HttpResponse<String> response = CLIENT.send(
          request,
          HttpResponse.BodyHandlers.ofString(
              StandardCharsets.UTF_8)
      );

      String result = response.body();

      if (result == null || result.isBlank()) {
        result = "{\"ok\":false,\"error\":\"Empty Cloudflare response\"}";
      }

      ServerMain.json(
          ex,
          response.statusCode(),
          result
      );

    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();

      ServerMain.json(ex, 502,
          "{\"ok\":false,\"error\":\"Request interrupted\"}");
      } catch (Exception e) {
      ServerMain.json(ex, 502,
          "{\"ok\":false,\"error\":\"Cloudflare request failed\","
          + "\"detail\":\""
          + JsonUtil.esc(e.getClass().getSimpleName())
          + "\"}");
    }
  }
}
