package migke.shiny;

import java.util.HashMap;
import java.util.function.Function;

import migke.shiny.exceptions.RequestException;
import migke.shiny.exceptions.client.InvalidRequestException;
import migke.shiny.http.HttpResponse;
import migke.shiny.http.status.ClientErrorStatusCode;
import migke.shiny.routing.Router;
import migke.shiny.routing.SegmentNode;
import migke.shiny.server.ServerConfiguration;
import migke.shiny.server.servers.jetty.JettyServer;

public class Shiny extends Router {
    private static final long MAX_CONTENT_LENGTH = 10 * 1024 * 1024; // 10MB
    private final ShinyConfiguration config;

    public Shiny(ShinyConfiguration config) {
        super();
        this.config = config;
        this.config.backend()
                .configure(config.configuration())
                .setHandler((req) -> {
                    var method = req.method();
                    var url = req.url().replaceFirst("(http|https)://", "");
                    var path = url.substring(url.indexOf('/'));
                    return this.findHandler(method, path)
                            .orElseThrow(() -> {
                                var message = "could not find route with path: " + path +
                                        ", and method: " + method;
                                return new InvalidRequestException(message, ClientErrorStatusCode.NOT_FOUND);
                            })
                            .apply(req);
                });
    }

    public static Shiny create() {
        return Shiny.create(Shiny.config());
    }

    public static Shiny create(ShinyConfiguration config) {
        return new Shiny(config);
    }

    public static ServerConfiguration serverConfig() {
        return new ServerConfiguration(4, 40 * 1000, MAX_CONTENT_LENGTH, httpRequest -> {
            var exception = httpRequest.exception().isPresent() ? httpRequest.exception().get() : null;
            var body = exception != null ? "Error: " + exception.getMessage() : "An error has occurred.";
            var statusCode = switch (exception) {
                case RequestException e -> e.statusCode;
                case null, default -> 500;
            };
            return new HttpResponse(statusCode, body, new HashMap<>(), new HashMap<>());
        });
    }

    public static ShinyConfiguration config() {
        return new ShinyConfiguration(new JettyServer(), Shiny.serverConfig());
    }

    @SafeVarargs
    @Override
    public final Shiny route(Function<SegmentNode, SegmentNode>... funcs) {
        return (Shiny) super.route(funcs);
    }

    public Shiny listen(int port) {
        this.config.backend().listen(port);
        return this;
    }
}
