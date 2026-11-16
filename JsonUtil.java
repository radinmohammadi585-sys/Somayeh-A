package com.infinity.somayeh;
public final class JsonUtil {
  public static String esc(String s) {
    if (s == null) return "";
    return s.replace("\\","\\\\").replace("\"","\\\"").replace("\n","\\n").replace("\r","");
  }
  public static String field(String json, String key) {
    if (json == null) return "";
    String k = "\"" + key + "\"";
    int p = json.indexOf(k); if (p < 0) return "";
    p = json.indexOf(':', p); if (p < 0) return "";
    int i = p + 1; while (i < json.length() && Character.isWhitespace(json.charAt(i))) i++;
    if (i >= json.length()) return "";
    if (json.charAt(i) == '"') {
      StringBuilder sb = new StringBuilder(); i++;
      while (i < json.length()) {
        char c = json.charAt(i++);
        if (c == '\\' && i < json.length()) { sb.append(json.charAt(i++)); continue; }
        if (c == '"') break; sb.append(c);
      }
      return sb.toString();
    }
    int j = i; while (j < json.length() && ",}]\t\n ".indexOf(json.charAt(j)) < 0) j++;
    return json.substring(i, j).trim();
  }
}
