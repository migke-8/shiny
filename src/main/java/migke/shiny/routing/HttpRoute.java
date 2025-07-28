package migke.shiny.routing;

import migke.shiny.http.HttpMethod;
import migke.shiny.http.HttpRequest;
import migke.shiny.http.HttpResponse;

import java.util.function.Function;

public record HttpRoute(HttpMethod method, String path, Function<HttpRequest, HttpResponse> handler) {
}
