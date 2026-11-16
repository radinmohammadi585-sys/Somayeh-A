package com.infinity.somayeh;
import com.sun.net.httpserver.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Executors;
public class ServerMain {
  public static final String APP_NAME = "Somayeh AI";
  public static final int PORT = Integer.parseInt(System.getenv().getOrDefault("PORT", "8103"));
  public static void main(String[] args) throws Exception {
    Config.load();
    HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
    server.createContext("/api/health", ServerMain::health);
    server.createContext("/api/info", ServerMain::info);
    server.createContext("/api/chat", ServerMain::chat);
    server.createContext("/api/auth/register", AuthController::register);
    server.createContext("/api/auth/login", AuthController::login);
    server.createContext("/api/languages", ServerMain::languages);
    server.createContext("/api/peers", ServerMain::peers);
    RouteRegistry.register(server);
    server.setExecutor(Executors.newCachedThreadPool());
    server.createContext("/", ServerMain::staticPage);
    server.start();
    System.out.println("[" + APP_NAME + "] http://127.0.0.1:" + PORT);
  }
  static void staticPage(HttpExchange ex) throws IOException {
    String path = ex.getRequestURI().getPath();

    if (!"GET".equalsIgnoreCase(ex.getRequestMethod())) {
        json(ex, 405, "{\"ok\":false,\"error\":\"GET required\"}");
        return;
    }

    if (!"/".equals(path) && !"/index.html".equals(path)) {
        json(ex, 404, "{\"ok\":false,\"error\":\"not_found\"}");
        return;
    }

    Path file = Path.of("Frontend", "index.html");
    if (!Files.isRegularFile(file)) {
        json(ex, 500, "{\"ok\":false,\"error\":\"frontend_file_missing\"}");
        return;
    }

    byte[] data = Files.readAllBytes(file);
    ex.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
    ex.getResponseHeaders().set("Cache-Control", "no-store");
    ex.sendResponseHeaders(200, data.length);

    try (OutputStream out = ex.getResponseBody()) {
        out.write(data);
    }
}
  static void health(HttpExchange ex) throws IOException { json(ex,200,"{\"ok\":true,\"app\":\""+APP_NAME+"\"}"); }
  static void info(HttpExchange ex) throws IOException { json(ex,200,"{\"ok\":true,\"app\":\""+APP_NAME+"\",\"theme\":\"Clean Studio\"}"); }
  static void languages(HttpExchange ex) throws IOException { json(ex,200, LanguageService.listJson()); }
  static void peers(HttpExchange ex) throws IOException { json(ex, 200, PeerBridge.peersJson()); }
  static void chat(HttpExchange ex) throws IOException {
    if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) { json(ex,405,"{\"ok\":false}"); return; }
    String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    json(ex, 200, ChatEngine.reply(body));
  }
  public static void json(HttpExchange ex, int code, String body) throws IOException {
    byte[] b = body.getBytes(StandardCharsets.UTF_8);
    ex.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
    ex.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
    ex.sendResponseHeaders(code, b.length);
    try (OutputStream os = ex.getResponseBody()) { os.write(b); }
  }
}
