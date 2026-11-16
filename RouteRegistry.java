package com.infinity.somayeh;
import com.sun.net.httpserver.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
public final class RouteRegistry {
  public static void register(HttpServer server) {
    server.createContext("/api/video", ex -> media(ex, "Video_Somayeh-AI", "video"));
    server.createContext("/api/image", ex -> media(ex, "Image_Somayeh-AI", "image"));
    server.createContext("/api/music", ex -> media(ex, "Music_Somayeh-AI", "music"));
  }
  static void media(HttpExchange ex, String keyName, String kind) throws IOException {
    String key = Config.requireKey(keyName);
    if (key.isEmpty()) {
      ServerMain.json(ex,200,"{\"ok\":false,\"configured\":false,\"kind\":\""+kind+"\",\"error\":\"Set "+keyName+"\"}");
      return;
    }
    String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    String prompt = JsonUtil.field(body, "prompt");
    ServerMain.json(ex,200,"{\"ok\":true,\"configured\":true,\"kind\":\""+kind+"\",\"prompt\":\""+JsonUtil.esc(prompt)+"\",\"note\":\"key loaded server-side\"}");
  }
}
