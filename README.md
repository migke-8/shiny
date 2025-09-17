# Shiny
1. [description](#Description)
1. [features](#features)
    1. [overall](#overall-features)
    1. [request handling](#request-handling)
    1. [routing](#routing-features)
1. [under the hood and why in this way](#under-the-hood)
1. [documentation](#documentation)
    1. [routing](#routing-documentation)
    1. [configuration](#configuration-documentation)
    1. [request and response objects](#request-and-response-objects-documentation)
    1. [routing documentation](#routing-documentation)
    1. [configuration documentation](#configuration-documentation)

## Description
A simple framework that uses composable building blocks to leverage
a simple but yet powerful solution to routing and request handling.
This project aims to give the tools to do a web framework main job,
while not standing at how it should be done, it follows the philosophy
of simplicity and minimalism by not implement any additional systems
on top of the _bread and butter_ of the _thin layer on top a web server_
that we all know and love.

## Features
### Overall features
> 1. asynchronous request handling
> 1. extensible
> 1. Non opinionated
> 1. composable
> 1. easy to use
> 1. easy to learn

### Request handling features
> 1. functional handler
> 2. request object with URL, body, method, cookies, etc...
> 2. request object with url, body, method, cookies, etc...
> 3. response object with status, body, headers and cookies
> 4. error handling

### Routing features
> 1. wildcard with the "**" string as a segment in route path
> 2. parameters with the "{" character as a predecessor and "}"
>    as a successor of the segment name
> 3. grouping routes with the "path" and "nest" functions

## Under the hood
This framework utilizes of a strategy of routing based on the tree data
structure and its default server is implemented with Jetty,
this decisions were made because this project focuses on being lightweight
while also being effective, ideal for self-hosting (yes, with java) and that
is its main purpose.

## Documentation
Here is a quick look at the library's API:

```java
import migke.shiny.Shiny;
import static migke.shiny.Shiny.*;
import static migke.shiny.http.HttpResponse.Ok;

class Main {
    public static void main(String[] args) {
        var app = Shiny.create()
            .route(path("/", get(req -> Ok("Hello, world!"))))
            .listen(8080);
    }
}
```

Explanation: the first thing you should notice is the _create_ method which is called from the Shiny class. It creates a bundle with two main methods _route_ and _listen_, which is the layer on top of the server that handles how to define your app.

1. ```Shiny.create()```: Instantiate the framework engine and can receive a optional configuration
1. ```Shiny.create().route(...)```: Define routes
1. ```Shiny.create().listen(int port)```: exposes the app and starts the server

### Routing documentation
To create a route you first need to understand that each one of those are just branches in a tree like structure containing some nodes that in essence contains the segments of the path name and a possible HTTP method which signals the framework internal engine where is the handler for the incoming request.

1. ```Shiny.get(Function<HttpRequest, HttpResponse>) // or another HTTP verb```: creates a route containing the specified HTTP method and handler for being attached to some node
    1. ```Shiny.path(String path, ...)```: creates a node containing the specified path name, and need to receive a node with some HTTP method and a handler to create something reachable by the user
1. ```Shiny.nest(...)```: combine multiple nodes into a single branch

### Configuration documentation
All configurations can be customized to your own needs.

1. ```Shiny.config()```: creates the default configuration used by ```Shiny.create()```, contains:
    1. ```withBackEnd(migke.shiny.server.HttpServer backEnd)```: defines the back end server
    1. ```withBackEndConfiguration(ServerConfiguration configuration)```: defines the configuration for the underlying server
    1. ```withCacheSize(int size)```: defines size of the cache
1. ```Shiny.serverConfig()```: creates the default server configuration used by ```Shiny.config()```, contains:
    1. ```withThreads(int threads)```: defines the maximum number of threads
    1. ```withTimeoutMilliseconds(int timeoutMilliseconds)```: defines the maximum amount of time that the server can wait for a single request to finish in milliseconds
    1. ```withErrorHandler(Function<HttpRequest, HttpResponse> errorHandler)```: defines a handler for error case scenarios

### Request and response objects documentation
A handler in Shiny is just a function that receives a request and returns a response, the request object is created by the framework itself and passed as argument to the function, and the response object can be created using some helper methods, but it also contains methods for delegating the responsibility to the error handler.
1. ```migke.shiny.http.HttpResponse``` static helper methods for creating responses:
    1. ```Ok(String body, [Map<String, String> headers])```
    1. ```Response(int status, [String body])```
1. ```migke.shiny.http.HttpResponse``` static helper methods for delegating the responsibility:
    1. ```Error(String message, (ClientErrorStatusCode || ServerErrorStatusCode) status)```
1. ```migke.shiny.http.HttpRequest``` instances contains: 
    1. ```HttpMethod method```
    1. ```Map<String, String> headers```
    1. ```Map<String, String> cookies```
    1. ```String body```
    1. ```Map<String, String> params```: parameters matched by the router
    1. ```Optional<RequestException> exception```
    1. ```String path```
    1. ```String header(String name)```
    1. ```String cookie(String name)```
1. ```migke.shiny.http.HttpResponse``` instances contains:
    1. ```withStatusCode(int code)```
    1.```withHeader(String key, String value)```
    1. ```withHeaders(Map<String, String> headers)```
    1. ```withCookies(Map<String, HttpCookie> cookies)```
    1. ```withBody(String body)```
    1. ```withCookie(String name, HttpCookie cookie)```

A bigger example:
```java
import migke.shiny.Shiny;

import static migke.shiny.Shiny.*;
import static migke.shiny.http.HttpResponse.Ok;
import static migke.shiny.http.HttpResponse.Error;
import static migke.shiny.http.HttpResponse.Response;
import static migke.shiny.http.status.ServerErrorStatusCode.INTERNAL_SERVER_ERROR;

import migke.shiny.ShinyConfiguration;
import migke.shiny.http.status.ServerErrorStatusCode;
import migke.shiny.server.servers.jetty.JettyServer;

class Main {
    public static void main(String[] args) {
        // OPTIONAL configuration...
        var app = Shiny.create(Main.getConfig()).route(
            // here is a simple route...
            path("/", get(req -> Ok("Hello, world!"))),
            nest(
                path(
                    "/users/{id}",
                    post(
                        req -> /* storage */.add(/* object with: */ req.param("id") /* ... */)
                    )
                ),
                path(
                    "/message",
                    get(req -> /* response */ ),
                        
                    // oops... some request exception was thrown
                    // this is a server side error but you could also call
                    // with ClientErrorStatusCode
                    post(
                        req -> Error("Internal server error", INTERNAL_SERVER_ERROR)
                    )
                )
            )
        ).listen(8080);
    }

    public static ShinyConfiguration getConfig() {
        return Shiny.config()
            // can be a custom one, if it corresponds to the
            // HttpServer interface
            .withBackend(
                new JettyServer()
            )
            .withConfiguration(
                Shiny.serverConfig()
                    // set the maximum number of threads
                    .withThreads(4)
                    .withErrorHandler((req -> {
                        var exception = req.exception().get();
                        return Response(exception.statusCode, "Error: " + exception.getMessage());
                    })
                )
            );
    }
}
```

