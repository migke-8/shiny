package migke.shiny.http;

import java.util.Map;

public record HttpRequest(
        HttpMethod method,
        String url,
        Map<String, String> headers,
        String body,
        Map<String, String> params
) {
    public static HttpRequest addParam(HttpRequest request, String name, String value) {
        request.params.put(name, value);
        return request;
    }
}