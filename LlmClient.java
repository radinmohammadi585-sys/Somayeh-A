package com.infinity.somayeh;
import java.net.URI; import java.net.http.*; import java.time.Duration;
public final class LlmClient {
  public static String complete(String apiKey, String message, String lang) {
    try {
      String base = Config.get("LLM_BASE_URL", "https://api.openai.com/v1");
      String model = Config.get("LLM_MODEL", "gpt-4o-mini");
      String body = "{\"model\":\""+model+"\",\"messages\":[{\"role\":\"user\",\"content\":\""+JsonUtil.esc(message)+"\"}]}";
      HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(12)).build();
      HttpRequest req = HttpRequest.newBuilder()
        .uri(URI.create(base.endsWith("/") ? base + "chat/completions" : base + "/chat/completions"))
        .timeout(Duration.ofSeconds(60))
        .header("Authorization", "Bearer " + apiKey)
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(body)).build();
      HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
      if (res.statusCode() >= 200 && res.statusCode() < 300) return JsonUtil.field(res.body(), "content");
    } catch (Exception ignored) {}
    return null;
  }
}
