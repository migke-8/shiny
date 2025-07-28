package migke.shiny;

import migke.shiny.exceptions.InvalidRequestException;
import migke.shiny.routing.Router;
import migke.shiny.routing.SegmentNode;

import java.util.function.Function;

public class Shiny extends Router {
    private final ServerConfiguration config;

    public Shiny(ServerConfiguration config) {
        super();
        this.config = config;
        this.config.backend().setHandler((req) -> {
            var method = req.method();
            var url = req.url().replaceFirst("(http|https)://", "");
            var path = url.substring(url.indexOf('/'));
            return this.findHandler(method, path)
                    .orElseThrow(() -> {
                        var message = "could not find route with path: " + path + ", and method: " + method;
                        return new InvalidRequestException(message);
                    })
                    .apply(req);
        });
    }

    public static Shiny create() {
        return Shiny.create(Shiny.config());
    }
    public static Shiny create(ServerConfiguration config) {
        return new Shiny(config);
    }
    public static ServerConfiguration config() {
        return new ServerConfiguration();
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
