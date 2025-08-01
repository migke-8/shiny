package migke.shiny.http;

import java.util.HashMap;
import java.util.Map;

public record HttpResponse(int status, String body, Map<String, String> headers, Map<String, HttpCookie> cookies) {
    public static HttpResponse Ok(String body) {
        return new HttpResponse(200, body, new HashMap<>(), new HashMap<>());
    }

    public static HttpResponse create(int status) {
        return new HttpResponse(status, "", new HashMap<>(), new HashMap<>());
    }
    public static HttpResponse create(int status, String body) {
        return new HttpResponse(status, body, new HashMap<>(), new HashMap<>());
    }

    public HttpResponse withHeader(String key, String value) {
        headers.put(key, value);
        return new HttpResponse(this.status, body, new HashMap<>(this.headers), new HashMap<>(this.cookies));
    }
    public HttpResponse withHeaders(Map<String, String> headers) {
        this.headers.putAll(headers);
        return new HttpResponse(this.status, body, new HashMap<>(this.headers), new HashMap<>(this.cookies));
    }
    public HttpResponse withCookies(Map<String, HttpCookie> cookies) {
        this.cookies.putAll(cookies);
        return new HttpResponse(this.status, body, new HashMap<>(this.headers), new HashMap<>(this.cookies));
    }
    public HttpResponse withBody(String body) {
        return new HttpResponse(this.status, body, new HashMap<>(this.headers), new HashMap<>(this.cookies));
    }
    public HttpResponse withCookie(String name, HttpCookie cookie) {
        this.cookies.put(name, cookie);
        return new HttpResponse(this.status, this.body, new HashMap<>(this.headers), new HashMap<>(this.cookies));
    }
}
