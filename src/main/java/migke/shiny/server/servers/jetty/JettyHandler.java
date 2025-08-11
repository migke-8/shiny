package migke.shiny.server.servers.jetty;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import migke.shiny.exceptions.RequestException;
import migke.shiny.exceptions.client.InvalidRequestException;
import migke.shiny.exceptions.server.ProcessingFailure;
import migke.shiny.http.HttpMethod;
import migke.shiny.http.HttpRequest;
import migke.shiny.http.HttpResponse;
import migke.shiny.http.status.ClientErrorStatusCode;
import migke.shiny.http.status.ServerErrorStatusCode;
import migke.shiny.server.ServerConfiguration;
import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.io.Content;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.ErrorHandler;
import org.eclipse.jetty.util.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JettyHandler extends Handler.Abstract {
    private static final Logger logger = LoggerFactory.getLogger(JettyHandler.class);

    private final Function<HttpRequest, HttpResponse> handler;
    private final ServerConfiguration serverConfiguration;
    private final Server server;

    public JettyHandler(Server server, ServerConfiguration serverConfiguration,
                        Function<HttpRequest, HttpResponse> handler) {
        this.server = server;
        this.serverConfiguration = serverConfiguration;
        this.handler = handler;
    }

    @Override
    public boolean handle(Request request, Response response, Callback callback) throws Exception {
        try {
            var threadPool = server.getThreadPool();
            threadPool.execute(() -> {
                try {
                    this.translateRequest(request, response, callback);
                } catch (RequestException e) {
                    response.setStatus(e.statusCode);
                    callback.failed(e);
                }
            });
        } catch (Exception e) {
            logger.error("Error while handling request", e);
            callback.failed(e);
        }
        return true;
    }

    private void translateRequest(Request baseRequest, Response response, Callback callback) throws RequestException {
        var method = HttpMethod.valueOf(baseRequest.getMethod());
        var url = baseRequest.getHttpURI().toString();
        var headers = baseRequest.getHeaders().stream().collect(
                Collectors.toMap(HttpField::getName, HttpField::getValue));
        var cookiesString = baseRequest.getHeaders().get("Cookie");
        var cookies =
                cookiesString != null ? this.parseCookies(cookiesString) : new HashMap<String, String>();
        var params = new HashMap<String, String>();
        var throwable = (Throwable) baseRequest.getAttribute(ErrorHandler.ERROR_EXCEPTION);
        Optional<RequestException> exception = switch (throwable) {
            case RequestException e -> Optional.of(e);
            case null, default -> Optional.empty();
        };
        var resultingRequest = new HttpRequest(method, url, headers, cookies, "", params, exception);
        if (hasContent(baseRequest)) {
            this.addBody(resultingRequest, baseRequest, response, callback);
            return;
        }
        this.prepareResponse(resultingRequest, response, callback);
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

    private void addBody(HttpRequest resultingRequest, Request baseRequest, Response response, Callback callback) throws RequestException {
        long contentLength = baseRequest.getLength();
        if (contentLength > serverConfiguration.maxContentLength()) {
            throw new InvalidRequestException("Request body content too large, maximum: "
                    + serverConfiguration.maxContentLength() + ".", ClientErrorStatusCode.PAYLOAD_TOO_LARGE);
        }
        readNextChunk(resultingRequest, baseRequest, response, callback);
    }

    private void prepareResponse(HttpRequest httpRequest, Response response, Callback callback) {
        var httpResponse = this.handler.apply(httpRequest);
        respond(httpResponse, response, callback);
    }

    private void respond(
            HttpResponse resultResponse, Response baseResponse, Callback callback) {
        var bodyBuffer = ByteBuffer.wrap(resultResponse.body().getBytes(StandardCharsets.UTF_8));

        setSecurityHeaders(baseResponse);
        baseResponse.setStatus(resultResponse.status());
        resultResponse.headers().forEach(baseResponse.getHeaders()::put);
        resultResponse.cookies().forEach(
                (_, value) -> baseResponse.getHeaders().add("Set-Cookie", value.toString()));
        baseResponse.write(true, bodyBuffer, callback);
    }

    private void readNextChunk(HttpRequest resultingRequest, Request request, Response response, Callback callback) {
        var contentBuilder = new StringBuilder();
        request.demand(() -> {
            try {
                Content.Chunk chunk;
                while ((chunk = request.read()) != null) {
                    if (Content.Chunk.isFailure(chunk)) {
                        chunk.release();
                        throw new ProcessingFailure("Failed to read request body content.", ServerErrorStatusCode.INTERNAL_SERVER_ERROR);
                    }

                    if (chunk.hasRemaining()) {
                        ByteBuffer buffer = chunk.getByteBuffer();
                        if (contentBuilder.length() + buffer.remaining()
                                > this.serverConfiguration.maxContentLength()) {
                            chunk.release();
                            throw (
                                    new InvalidRequestException("Request body content too large, maximum: "
                                            + serverConfiguration.maxContentLength() + ".", ClientErrorStatusCode.PAYLOAD_TOO_LARGE));
                        }

                        String chunkContent = StandardCharsets.UTF_8.decode(buffer.duplicate()).toString();
                        contentBuilder.append(chunkContent);
                    }

                    boolean isLast = chunk.isLast();
                    chunk.release();

                    if (isLast) {
                        var httpRequest = HttpRequest.addBody(resultingRequest, contentBuilder.toString());
                        prepareResponse(httpRequest, response, callback);
                        return;
                    }
                }

            } catch (Exception e) {
                callback.failed(new ProcessingFailure("Failed to read request body content.", ServerErrorStatusCode.INTERNAL_SERVER_ERROR));
            }
        });
    }

    private void setSecurityHeaders(Response response) {
        response.getHeaders().put("X-Content-Type-Options", "nosniff");
        response.getHeaders().put("X-Frame-Options", "DENY");
        response.getHeaders().put("X-XSS-Protection", "1; mode=block");
        response.getHeaders().put("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
        response.getHeaders().put("Content-Security-Policy", "default-src 'self'");
    }

    private boolean hasContent(Request request) {
        return request.getLength() > 0
                || "chunked".equalsIgnoreCase(request.getHeaders().get(HttpHeader.TRANSFER_ENCODING));
    }
}
