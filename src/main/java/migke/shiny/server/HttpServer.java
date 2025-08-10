package migke.shiny.server;

import migke.shiny.http.HttpRequest;
import migke.shiny.http.HttpResponse;

import java.util.function.Function;

public interface HttpServer {
    HttpServer configure(ServerConfiguration config);
    HttpServer setHandler(Function<HttpRequest, HttpResponse> handler);
    HttpServer listen(int port);
}
