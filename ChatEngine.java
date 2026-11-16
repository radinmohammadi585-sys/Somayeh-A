package com.infinity.somayeh;
public final class ChatEngine {
  public static String reply(String rawBody) {
    String message = JsonUtil.field(rawBody, "message"); if (message.isEmpty()) message = JsonUtil.field(rawBody, "text");
    String lang = LanguageService.detect(message);
    String web = WebSearch.lookup(message);
    String apiKey = Config.requireKey("LLM_API_KEY"); if (apiKey.isEmpty()) apiKey = Config.requireKey("OPENAI_API_KEY");
    String reply;
    if (!apiKey.isEmpty()) {
      String prompt = (web == null || web.isBlank()) ? message : ("اطلاعات وب:\n" + web + "\n\nسوال کاربر:\n" + message);
      String r = LlmClient.complete(apiKey, prompt, lang);
      reply = (r == null || r.isBlank()) ? local(message, lang, web) : r;
    } else reply = local(message, lang, web);
    String bridge = JsonUtil.field(rawBody, "bridge");
    if ("1".equals(bridge) || "true".equalsIgnoreCase(bridge)) {
      String peerAns = PeerBridge.consultPeers(message);
      if (peerAns != null && !peerAns.isBlank()) reply = reply + "\n\n— پاسخ هم‌تیمی‌ها —\n" + peerAns;
    }
    return "{\"ok\":true,\"lang\":\""+lang+"\",\"rtl\":"+LanguageService.isRtl(lang)+",\"reply\":\""+JsonUtil.esc(reply)+"\"}";
  }
  static String local(String message, String lang, String web) {
    String extra = (web == null || web.isBlank()) ? "" : ("

[وب] " + web);
    if ("fa".equals(lang)) return "[پاسخ محلی] " + message + extra + " — برای مدل ابری LLM_API_KEY را تنظیم کنید.";
    return "[local] " + message + extra + " — Set LLM_API_KEY for cloud model.";
  }
}
