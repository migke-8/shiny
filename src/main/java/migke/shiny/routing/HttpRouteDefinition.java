package migke.shiny.routing;

import migke.shiny.http.HttpMethod;

public record HttpRouteDefinition(
    HttpMethod method,
    String path
) { }
