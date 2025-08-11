package migke.shiny;

import migke.shiny.server.HttpServer;
import migke.shiny.server.ServerConfiguration;

public record ShinyConfiguration(HttpServer backend, ServerConfiguration configuration){
    public ShinyConfiguration withBackend(HttpServer backend) {
        return new ShinyConfiguration(backend, configuration);
    }
    public ShinyConfiguration withConfiguration(ServerConfiguration configuration) {
        return new ShinyConfiguration(backend, configuration);
    }
}
