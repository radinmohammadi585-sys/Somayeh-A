import java.net.URI; import java.net.http.*;
public class SmokeTest {
  public static void main(String[] args) throws Exception {
    var c = HttpClient.newHttpClient();
    var res = c.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:8103/api/health")).GET().build(), HttpResponse.BodyHandlers.ofString());
    System.out.println(res.statusCode()+" "+res.body());
  }
}
