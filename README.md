# Shiny
A simple framework that uses composable building blocks to leverage
a simple but yet powerful solution to routing and request handling.
This project aims to give the tools to do a web framework main job,
while not standing at how it should be done, it follows the philosophy
of simplicity and minimalism by not implement any additional systems
on top of the _bread and butter_ of the _thin layer on top a web server_
that we all know and love.

This framework utilizes of a strategy of routing based on the tree data
structure and its default server is implemented with Jetty,
this decisions were made because this project focuses on being lightweight
while also being effective, ideal for self-hosting (yes, with java) and that
is its main purpose.

Here is a quick look at the library's API:

```java
import migke.shiny.Shiny;
import migke.shiny.Shiny.*;
import migke.shiny.ShinyConfiguration;
import migke.shiny.http.HttpResponse;
import migke.shiny.http.status.ServerErrorStatusCode;
import migke.shiny.server.servers.jetty.JettyServer;

import java.util.HashMap;

class Main {
    public static void main(String[] args) {
        // optional configuration...
        var app = Shiny.create(Main.getConfig()).route(
            // here is a simple route...
            path("/", get(req -> HttpResponse.Ok("Hello, world!"))),
            nest(
                path(
                    "/users/{id}", 
                    post(
                        req -> /* storage */.add(/* object with: */ req.param("id") /* ... */)
                    )
                ),
                path(
                    "/message",
                    get(req -> /* some response for the route with path /users/{id}/message and method GET */),
                    // oops... some request exception was thrown
                    // this is a server side error but you alo could call with ClientErrorStatusCode
                    post(req -> HttpResponse.Throw("Internal server error", ServerErrorStatusCode.INTERNAL_SERVER_ERROR))
                )
            )
        );
        
        app.listen(8080);
    }

    public static ShinyConfiguration getConfig() {
        return Shiny.config().withBackend(
            // can be a custom one, since it corresponds to the HttpServer interface
            new JettyServer()
        )
        .withConfiguration(
            Shiny.serverConfig()
                // set the maximum number of threads
                .withThreads(4)
                .withErrorHandler((req -> {
                    var exception = req.exception().get();
                    return HttpResponse.res(exception.statusCode, "Error: " + exception.getMessage());
                })
            )
        );
    }
}
```
# Request handling features
> 1. functional handler
> 2. Request object with url, body, method, cookies, etc...
> 3. response object with status, body, headers and cookies
# Routing fetures
> 1. wildcard with the "**" string as a segment in route path
> 2. parameters with the "{" character as a predecessor and "}"
>    as a successor of the segment name
> 3. grouping routes with the "path" and "nest" functions
