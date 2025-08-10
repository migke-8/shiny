package migke.shiny.exceptions;

public sealed class RequestException extends RuntimeException permits ServerException, ClientException {
    public final int statusCode;
    public RequestException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }
}
