package migke.shiny.exceptions.client;

import migke.shiny.exceptions.ClientException;
import migke.shiny.http.status.ClientErrorStatusCode;

public class InvalidRequestException extends ClientException {
    public InvalidRequestException(String message, ClientErrorStatusCode statusCode) {
        super(message, statusCode.statusCode);
    }
}
