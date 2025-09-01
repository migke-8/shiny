package migke.shiny;

import java.util.HashMap;
import java.util.function.Function;

import migke.shiny.exceptions.client.InvalidRequestException;
import migke.shiny.http.HttpResponse;
import migke.shiny.http.status.ClientErrorStatusCode;
import migke.shiny.routing.Router;
import migke.shiny.routing.SegmentNode;
import migke.shiny.server.ServerConfiguration;
import migke.shiny.server.servers.jetty.JettyServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Shiny extends Router {
	private static final Logger logger = LoggerFactory.getLogger(Shiny.class);
	private static final long MAX_CONTENT_LENGTH = 10 * 1024 * 1024; // 10MB
	private final ShinyConfiguration config;

	public Shiny(ShinyConfiguration config) {
		super(config.cacheSize());
		this.config = config;
		this.config.backend().configure(config.configuration()).setHandler((req) -> {
			var method = req.method();
			var url = req.url().replaceFirst("(http|https)://", "");
			var path = url.substring(url.indexOf('/'));
			return this.findHandler(method, path).orElseThrow(() -> {
				var message = "could not find route with path: " + path + ", and method: " + method;
				Shiny.logger.error(message);
				return new InvalidRequestException(message, ClientErrorStatusCode.NOT_FOUND);
			}).apply(req);
		});
	}

	public static Shiny create() {
		return Shiny.create(Shiny.config());
	}

	public static Shiny create(ShinyConfiguration config) {
		Shiny.logger.info("Creating Shiny instance using config: {}", config);
		return new Shiny(config);
	}

	public static ServerConfiguration serverConfig() {
		return new ServerConfiguration(4, 40 * 1000, MAX_CONTENT_LENGTH, httpRequest -> {
			var body = httpRequest.exception().isPresent() ? "Error: " + httpRequest.exception().get().getMessage()
					: "An internal server error has occurred.";
			var statusCode = switch (httpRequest.exception().get()) {
				case null -> 500;
				default -> httpRequest.exception().get().statusCode;
			};
			return new HttpResponse(statusCode, body, new HashMap<>(), new HashMap<>());
		});
	}

	public static ShinyConfiguration config() {
		return new ShinyConfiguration(new JettyServer(), Shiny.serverConfig(), 8);
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
