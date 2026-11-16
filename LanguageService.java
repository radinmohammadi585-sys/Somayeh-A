package com.infinity.somayeh;
public final class LanguageService {
  public static final String[] CODES = new String[]{
    "af","am","ar","az","be","bg","bn","bs","ca","ceb","co","cs","cy","da","de","el","en","eo","es","et",
    "eu","fa","fi","fr","fy","ga","gd","gl","gu","ha","haw","he","hi","hmn","hr","ht","hu","hy","id","ig",
    "is","it","ja","jv","ka","kk","km","kn","ko","ku","ky","la","lb","lo","lt","lv","mg","mi","mk","ml",
    "mn","mr","ms","mt","my","ne","nl","no","ny","or","pa","pl","ps","pt","ro","ru","rw","sd","si","sk",
    "sl","sm","sn","so","sq","sr","st","su","sv","sw","ta","te","tg","th","tk","tl","tr","tt","ug","uk",
    "ur","uz","vi","xh","yi","yo","zh","zu","as","dv","ff","gn","iu","kl","ks","lg","ln","lu","nd","ng",
    "nr","oc","oj","om","qu","rm","rn","sa","sc","se","sg","ss","ti","tn","to","ts","tw","ve","wa","wo",
    "ak","bm","ch","cr","ee","fj","ho","hz","ik","io","kg","ki","kj","kr","kv","kw","li","mh","na","nb",
    "nn","nv","os","pi","sh","ty","za"
  };
  public static String detect(String text) {
    if (text == null || text.isBlank()) return "en";
    for (int i = 0; i < text.length(); i++) {
      char c = text.charAt(i);
      if (c >= 0x0600 && c <= 0x06FF) return "fa";
      if (c >= 0x4E00 && c <= 0x9FFF) return "zh";
      if (c >= 0x0400 && c <= 0x04FF) return "ru";
      if (c >= 0x3040 && c <= 0x30FF) return "ja";
      if (c >= 0xAC00 && c <= 0xD7AF) return "ko";
    }
    return "en";
  }
  public static boolean isRtl(String code) {
    return "fa".equals(code)||"ar".equals(code)||"ur".equals(code)||"he".equals(code)||"ps".equals(code);
  }
  public static String listJson() {
    StringBuilder sb = new StringBuilder("{\"ok\":true,\"count\":"+CODES.length+",\"codes\":[");
    for (int i=0;i<CODES.length;i++){ if(i>0)sb.append(','); sb.append('"').append(CODES[i]).append('"'); }
    sb.append("],\"note\":\"Cloud multilingual quality needs LLM/translation API keys\"}");
    return sb.toString();
  }
}
