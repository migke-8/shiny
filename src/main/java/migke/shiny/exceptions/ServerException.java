package migke.shiny.exceptions;

public non-sealed class ServerException extends RequestException {
    public ServerException(String message,  int statusCode) {
        super(message, statusCode);
    }
}
