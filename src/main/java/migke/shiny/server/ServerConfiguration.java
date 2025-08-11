package migke.shiny.server;

import java.util.function.Function;
import migke.shiny.http.HttpRequest;
import migke.shiny.http.HttpResponse;

public record
ServerConfiguration(int threads, int timeoutMilliseconds, long maxContentLength,
                    Function<HttpRequest, HttpResponse> errorHandler) {
  public ServerConfiguration withThreads(int threads) {
    return new ServerConfiguration(threads, timeoutMilliseconds,
                                   maxContentLength, errorHandler);
  }

  public ServerConfiguration withTimeoutMilliseconds(int timeoutMilliseconds) {
    return new ServerConfiguration(threads, timeoutMilliseconds,
                                   maxContentLength, errorHandler);
  }
  public ServerConfiguration withMaxContentLength(int maxContentLength) {
    return new ServerConfiguration(threads, timeoutMilliseconds,
                                   maxContentLength, errorHandler);
  }

  public ServerConfiguration withErrorHandler(
      Function<HttpRequest, HttpResponse> errorHandler) {
    return new ServerConfiguration(threads, timeoutMilliseconds,
                                   maxContentLength, errorHandler);
  }
}
