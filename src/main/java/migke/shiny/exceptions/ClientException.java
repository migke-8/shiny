package migke.shiny.exceptions;

public non-sealed class ClientException extends RequestException {
    public ClientException(String message, int status) {
        super(message, status);
    }
}
