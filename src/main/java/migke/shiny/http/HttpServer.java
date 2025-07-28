package migke.shiny.http;

import java.util.function.Function;

public interface HttpServer {
    HttpServer setHandler(Function<HttpRequest, HttpResponse> handler);
    HttpServer listen(int port);
}
