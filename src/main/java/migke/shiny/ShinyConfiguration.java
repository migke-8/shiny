package migke.shiny;

import migke.shiny.server.HttpServer;
import migke.shiny.server.ServerConfiguration;

public record ShinyConfiguration(HttpServer backend, ServerConfiguration configuration, int cacheSize){
    public ShinyConfiguration withBackend(HttpServer backend) {
        return new ShinyConfiguration(backend, configuration, cacheSize);
    }
    public ShinyConfiguration withConfiguration(ServerConfiguration configuration) {
        return new ShinyConfiguration(backend, configuration, cacheSize);
    }
    public ShinyConfiguration withCacheSize(int cacheSize) {
        return new ShinyConfiguration(backend, configuration, cacheSize);
    }
}
