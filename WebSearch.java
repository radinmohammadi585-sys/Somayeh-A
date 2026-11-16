package com.infinity.somayeh;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.regex.*;

/** Lightweight web lookup (Wikipedia + DuckDuckGo instant). Used before answering. */
public final class WebSearch {
  private WebSearch() {}

  public static String lookup(String query) {
    if (query == null) return "";
    String q = query.trim();
    if (q.isEmpty() || q.startsWith("[peer]")) return "";
    // prefer Wikipedia summary
    String wiki = wikipedia(q);
    if (wiki != null && !wiki.isBlank()) return wiki;
    String ddg = duckDuck(q);
    return ddg == null ? "" : ddg;
  }

  static String wikipedia(String q) {
    try {
      String enc = URLEncoder.encode(q, StandardCharsets.UTF_8);
      // try fa then en
      for (String lang : new String[]{"fa","en"}) {
        String url = "https://" + lang + ".wikipedia.org/api/rest_v1/page/summary/" + enc;
        HttpClient c = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(4)).build();
        HttpRequest req = HttpRequest.newBuilder(URI.create(url))
          .timeout(Duration.ofSeconds(8))
          .header("User-Agent", "InfinityAI/1.0")
          .GET().build();
        HttpResponse<String> res = c.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() == 200) {
          String extract = JsonUtil.field(res.body(), "extract");
          if (extract != null && extract.length() > 40) return extract;
        }
      }
    } catch (Exception ignored) {}
    return null;
  }

  static String duckDuck(String q) {
    try {
      String enc = URLEncoder.encode(q, StandardCharsets.UTF_8);
      String url = "https://api.duckduckgo.com/?q=" + enc + "&format=json&no_html=1&skip_disambig=1";
      HttpClient c = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(4)).build();
      HttpRequest req = HttpRequest.newBuilder(URI.create(url))
        .timeout(Duration.ofSeconds(8))
        .header("User-Agent", "InfinityAI/1.0")
        .GET().build();
      HttpResponse<String> res = c.send(req, HttpResponse.BodyHandlers.ofString());
      if (res.statusCode() == 200) {
        String abs = JsonUtil.field(res.body(), "AbstractText");
        if (abs != null && abs.length() > 20) return abs;
        String answer = JsonUtil.field(res.body(), "Answer");
        if (answer != null && !answer.isBlank()) return answer;
      }
    } catch (Exception ignored) {}
    return null;
  }
}
