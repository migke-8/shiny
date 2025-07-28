package migke.shiny.http;

import java.util.HashMap;
import java.util.Map;

public record HttpResponse(int status, String body, Map<String, String> headers) {
    public static HttpResponse Ok(String body) {
        return new HttpResponse(200, body, new HashMap<>());
    }
    public HttpResponse withHeader(String key, String value) {
        headers.put(key, value);
        return this;
    }
}
