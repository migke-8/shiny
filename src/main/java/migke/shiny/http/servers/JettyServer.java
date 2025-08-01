package migke.shiny.http.servers;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import migke.shiny.exceptions.InvalidRequestException;
import migke.shiny.http.HttpMethod;
import migke.shiny.http.HttpRequest;
import migke.shiny.http.HttpResponse;
import migke.shiny.http.HttpServer;
import org.eclipse.jetty.http.HttpCookie;
import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.util.Callback;

public class JettyServer implements HttpServer {
    private Function<HttpRequest, HttpResponse> handler;

    @Override
    public JettyServer setHandler(Function<HttpRequest, HttpResponse> handler) {
        this.handler = handler;
        return this;
    }

    @Override
    public JettyServer listen(int port) {
        org.eclipse.jetty.server.Server server = new org.eclipse.jetty.server.Server();
        ServerConnector connector = new ServerConnector(server);

        connector.setPort(port);
        server.addConnector(connector);

        ContextHandler context = new ContextHandler(new Handler.Abstract() {
            @Override
            public boolean handle(Request request, Response response, Callback callback) throws Exception {
                try {
                    var translatedRequest = translate(request);
                    var handlerResponse = handler.apply(translatedRequest);
                    var bodyBuffer = ByteBuffer.wrap(handlerResponse.body().getBytes(StandardCharsets.UTF_8));

                    response.setStatus(handlerResponse.status());
                    handlerResponse.headers().forEach(response.getHeaders()::put);
                    handlerResponse.cookies().forEach((_, value) -> response.getHeaders().add("Set-Cookie", value.toString()));
                    response.write(true, bodyBuffer, callback);
                } catch (Exception e) {
                    callback.failed(e);
                }
                return true;
            }
        }, "/");

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

    private HttpRequest translate(Request req) {
        var method = HttpMethod.valueOf(req.getMethod());
        var url = req.getHttpURI().toString();
        var headers = req.getHeaders().stream().collect(Collectors.toMap(HttpField::getName, HttpField::getValue));
        var bodyContent = new StringBuilder();
        req.addHttpStreamWrapper(getWrapper(bodyContent));
        var body = bodyContent.toString();
        var params = new HashMap<String, String>();
        var cookiesString = req.getHeaders().get("Cookie");
        var cookies = cookiesString != null? this.parseCookies(cookiesString): new HashMap<String, String>();
        return new HttpRequest(method, url, headers, cookies, body, params);
    }

    private migke.shiny.http.HttpCookie getWrapper(HttpCookie cookie) {
        var name = cookie.getName();
        var value = cookie.getValue();
        var maxAge = Optional.of(cookie.getMaxAge());
        Optional<LocalDateTime> expires = cookie.getExpires() != null ? Optional.of(LocalDateTime.ofInstant(cookie.getExpires(), ZoneId.of("UTC"))) : Optional.empty();
        return new migke.shiny.http.HttpCookie(name, value, expires, maxAge);
    }

    private Function<HttpStream, HttpStream> getWrapper(StringBuilder builder) {
        return httpStream -> {
            var chunk = httpStream.read();
            if (chunk != null) {
                var buffer = chunk.getByteBuffer();
                if (buffer != null && buffer.hasRemaining()) {
                    var bytes = new byte[buffer.remaining()];
                    buffer.get(bytes);
                    var chunkString = new String(bytes, StandardCharsets.UTF_8);
                    builder.append(chunkString);
                }
                if (chunk.getFailure() != null) {
                    throw new InvalidRequestException(chunk.getFailure().getMessage());
                }
            }
            return httpStream;
        };
    }

    private Map<String, String> parseCookies(String string) {
        var cookies = new HashMap<String, String>();
        StringBuilder name = new StringBuilder();
        for (int i = 0; i < string.length(); i++) {
            if (!Character.isWhitespace(string.charAt(i)))
                name.append(string.charAt(i));
            if (string.charAt(i) == '=') {
                StringBuilder value = new StringBuilder();
                while (i < string.length() && string.charAt(i + 1) != ';') {
                    value.append(string.charAt(i));
                    i++;
                }
                cookies.put(name.toString(), value.toString());
                name = new StringBuilder();
            }
        }
        return cookies;
    }
}
