package com.infinity.somayeh;

import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.*;

/** Cross-project bridge: ask sibling Infinity services. */
public final class PeerBridge {
  public static final class Peer {
    public final String id; public final String name; public final int port;
    public Peer(String id, String name, int port) { this.id=id; this.name=name; this.port=port; }
  }

  private static final List<Peer> PEERS = List.of(
    new Peer("ai1", "Infinity AI 1", 8101),
    new Peer("ai2", "Infinity AI 2", 8102),
    new Peer("somayeh", "Somayeh AI", 8103),
    new Peer("ghp", "GHP AI", 8104),
    new Peer("mpn", "MPN AI", 8105)
  );

  public static String selfId() {
    return Config.get("PEER_ID", "somayeh");
  }

  public static List<Peer> others() {
    String me = selfId();
    List<Peer> out = new ArrayList<>();
    for (Peer p : PEERS) if (!p.id.equals(me)) out.add(p);
    return out;
  }

  /** Ask one peer /api/chat. Returns reply text or null. */
  public static String askPeer(Peer peer, String message) {
    try {
      String host = Config.get("PEER_HOST", "127.0.0.1");
      String url = "http://" + host + ":" + peer.port + "/api/chat";
      String body = "{\"message\":\"" + JsonUtil.esc(message) + "\",\"from_peer\":\"" + selfId() + "\"}";
      HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
      HttpRequest req = HttpRequest.newBuilder(URI.create(url))
        .timeout(Duration.ofSeconds(8))
        .header("Content-Type", "application/json")
        .header("X-Infinity-Peer", selfId())
        .POST(HttpRequest.BodyPublishers.ofString(body))
        .build();
      HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
      if (res.statusCode() >= 200 && res.statusCode() < 300) {
        String reply = JsonUtil.field(res.body(), "reply");
        if (reply != null && !reply.isBlank()) return reply;
      }
    } catch (Exception ignored) {}
    return null;
  }

  /** Fan-out question to other live peers; collect short answers. */
  public static String consultPeers(String message) {
    if (message == null || message.isBlank()) return "";
    // avoid bridge loops
    if (message.startsWith("[peer]")) return "";
    StringBuilder sb = new StringBuilder();
    for (Peer p : others()) {
      String r = askPeer(p, "[peer] " + message);
      if (r != null && !r.isBlank()) {
        if (sb.length() > 0) sb.append("\n");
        sb.append("• ").append(p.name).append(": ").append(r);
      }
    }
    return sb.toString();
  }

  public static String peersJson() {
    StringBuilder sb = new StringBuilder("{\"ok\":true,\"self\":\"" + selfId() + "\",\"peers\":[");
    boolean first = true;
    for (Peer p : PEERS) {
      if (!first) sb.append(',');
      first = false;
      boolean live = false;
      try {
        String host = Config.get("PEER_HOST", "127.0.0.1");
        HttpClient c = HttpClient.newBuilder().connectTimeout(Duration.ofMillis(400)).build();
        HttpRequest req = HttpRequest.newBuilder(URI.create("http://" + host + ":" + p.port + "/api/health")).GET().build();
        HttpResponse<String> res = c.send(req, HttpResponse.BodyHandlers.ofString());
        live = res.statusCode() == 200;
      } catch (Exception ignored) {}
      sb.append("{\"id\":\"").append(p.id).append("\",\"name\":\"").append(p.name)
        .append("\",\"port\":").append(p.port).append(",\"live\":").append(live).append("}");
    }
    sb.append("]}");
    return sb.toString();
  }
}
