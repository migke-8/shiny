package migke.shiny.server.servers.jetty;

import java.util.function.Function;
import migke.shiny.http.HttpRequest;
import migke.shiny.http.HttpResponse;
import migke.shiny.server.HttpServer;
import migke.shiny.server.ServerConfiguration;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

public class JettyServer implements HttpServer {
  private ServerConfiguration config;
  private JettyHandler handler;
  private Server server = new Server(new QueuedThreadPool());

  public JettyServer() { this.server.setStopAtShutdown(true); }

  @Override
  public HttpServer configure(ServerConfiguration config) {
    this.config = config;
    this.configureServer(config);
    return this;
  }

  @Override
  public JettyServer setHandler(Function<HttpRequest, HttpResponse> handler) {
    this.handler = new JettyHandler(this.server, this.config, handler);
    return this;
  }

  @Override
  public JettyServer listen(int port) {
    ServerConnector connector = new ServerConnector(server);
    connector.setPort(port);
    server.addConnector(connector);
    ContextHandler context = new ContextHandler(this.handler, "/");
    server.setHandler(context);
    try {
      server.start();
    } catch (Exception e) {
      try {
        server.join();
      } catch (InterruptedException ie) {
        ie.printStackTrace();
      }
    }
    return this;
  }

  private void configureServer(ServerConfiguration config) {
    var pool = new QueuedThreadPool();
    pool.setMaxThreads(config.threads());
    pool.setMinThreads(1);
    this.server = new Server(pool);
    this.server.setErrorHandler(new JettyHandler(this.server, this.config, config.errorHandler()));
    this.server.setStopAtShutdown(true);
  }
}
