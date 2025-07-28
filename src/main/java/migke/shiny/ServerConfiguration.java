package migke.shiny;

import migke.shiny.http.HttpServer;
import migke.shiny.http.servers.JettyServer;

public record ServerConfiguration(HttpServer backend) {
    public ServerConfiguration() {
        this(new JettyServer());
    }
    public ServerConfiguration withServer(HttpServer backend) {
        return new ServerConfiguration(backend);
    }
}
