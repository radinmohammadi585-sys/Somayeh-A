package com.infinity.somayeh;
import com.sun.net.httpserver.HttpExchange;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
public final class AuthController {
  private static final Map<String,String> USERS = Collections.synchronizedMap(new HashMap<>());
  private static final Map<String,String> TOKENS = Collections.synchronizedMap(new HashMap<>());
  public static void register(HttpExchange ex) throws IOException {
    String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    String u = JsonUtil.field(body, "username"); String p = JsonUtil.field(body, "password");
    if (u.length() < 3 || p.length() < 4) { ServerMain.json(ex,400,"{\"ok\":false,\"error\":\"short\"}"); return; }
    if (USERS.containsKey(u)) { ServerMain.json(ex,400,"{\"ok\":false,\"error\":\"exists\"}"); return; }
    USERS.put(u, hash(p));
    ServerMain.json(ex,200,"{\"ok\":true,\"username\":\""+JsonUtil.esc(u)+"\"}");
  }
  public static void login(HttpExchange ex) throws IOException {
    String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    String u = JsonUtil.field(body, "username"); String p = JsonUtil.field(body, "password");
    String h = USERS.get(u);
    if (h == null || !h.equals(hash(p))) { ServerMain.json(ex,401,"{\"ok\":false,\"error\":\"invalid\"}"); return; }
    String token = UUID.randomUUID().toString().replace("-","");
    TOKENS.put(token, u);
    ServerMain.json(ex,200,"{\"ok\":true,\"token\":\""+token+"\",\"username\":\""+JsonUtil.esc(u)+"\"}");
  }
  static String hash(String s) {
    try {
      byte[] dig = MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));
      StringBuilder sb = new StringBuilder(); for (byte b: dig) sb.append(String.format("%02x", b)); return sb.toString();
    } catch (Exception e) { return s; }
  }
}
