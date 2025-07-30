package migke.shiny.http;

import java.util.Map;
import java.util.Optional;

public record HttpRequest(
        HttpMethod method,
        String url,
        Map<String, String> headers,
        Map<String, String> cookies,
        String body,
        Map<String, String> params
) {
    public static HttpRequest addParam(HttpRequest request, String name, String value) {
        request.params.put(name, value);
        return request;
    }
    public Optional<String> param(String name) {
        return Optional.ofNullable(params.get(name));
    }
    public String path() {
        return url.substring(url.indexOf('/'));
    }
    public String header(String name) {
        return  headers.get(name);
    }
    public Optional<String> cookie(String name) {
        return Optional.ofNullable(cookies.get(name));
    }
}