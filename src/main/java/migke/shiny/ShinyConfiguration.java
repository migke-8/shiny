package migke.shiny;

import migke.shiny.server.HttpServer;
import migke.shiny.server.ServerConfiguration;

public record ShinyConfiguration(HttpServer backEnd, ServerConfiguration backEndConfiguration, int cacheSize) {
  public ShinyConfiguration withBackEnd(HttpServer backEnd) {
    return new ShinyConfiguration(backEnd, backEndConfiguration, cacheSize);
  }

  public ShinyConfiguration withBackEndConfiguration(ServerConfiguration configuration) {
    return new ShinyConfiguration(backEnd, configuration, cacheSize);
  }

  public ShinyConfiguration withCacheSize(int cacheSize) {
    return new ShinyConfiguration(backEnd, backEndConfiguration, cacheSize);
  }
}
