package migke.shiny;

import migke.shiny.server.HttpServer;
import migke.shiny.server.ServerConfiguration;

public record ShinyConfiguration(HttpServer backend, ServerConfiguration configuration){ }
