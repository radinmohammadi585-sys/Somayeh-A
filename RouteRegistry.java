package com.infinity.somayeh;

import com.sun.net.httpserver.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;

public final class RouteRegistry {
  private static final Set<String> VIDEO_ENGINES = Set.of("fast", "quality", "web");

  public static void register(HttpServer server) {
    server.createContext("/api/video", ex -> media(ex, "Video_Somayeh-AI", "video"));
    server.createContext("/api/image", ex -> media(ex, "Image_Somayeh-AI", "image"));
    server.createContext("/api/music", ex -> media(ex, "Music_Somayeh-AI", "music"));
  }

  static void media(HttpExchange ex, String keyName, String kind) throws IOException {
    if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) {
      ServerMain.json(ex, 405, "{\"ok\":false,\"error\":\"POST required\"}");
      return;
    }

    String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    String prompt = JsonUtil.field(body, "prompt");
    String engine = JsonUtil.field(body, "engine");

    if (prompt == null || prompt.isBlank()) {
      ServerMain.json(ex, 400, "{\"ok\":false,\"error\":\"prompt_required\"}");
      return;
    }

    if ("video".equals(kind) && !VIDEO_ENGINES.contains(engine)) engine = "fast";
    if (engine == null || engine.isBlank()) engine = "fast";

    String key = Config.requireKey(keyName);

    // The UI engine choices remain available even when no provider key is configured.
    // Do not pretend a video was generated: return a clear configuration status.
    if (key.isEmpty()) {
      String engines = "video".equals(kind)
        ? "[\"fast\",\"quality\",\"web\"]"
        : "[\"fast\",\"quality\",\"web\"]";
      String json = "{\"ok\":false,\"configured\":false,\"kind\":\"" + kind
        + "\",\"engine\":\"" + JsonUtil.esc(engine)
        + "\",\"availableEngines\":" + engines
        + ",\"error\":\"Set " + keyName + " on the server. Engine selection is enabled, but generation needs a connected provider.\"}";
      ServerMain.json(ex, 503, json);
      return;
    }

    // A key alone is not enough to call a provider: each provider has its own API
    // endpoint, model ID, payload schema, and asynchronous job/result handling.
    // Return an explicit not-integrated response instead of a fake success.
    String json = "{\"ok\":false,\"configured\":true,\"providerIntegrated\":false,\"kind\":\""
      + kind + "\",\"engine\":\"" + JsonUtil.esc(engine)
      + "\",\"error\":\"Provider API adapter is not implemented yet; no media was generated.\"}";
    ServerMain.json(ex, 501, json);
  }
}
