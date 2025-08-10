package migke.shiny.exceptions.server;

import migke.shiny.exceptions.ServerException;
import migke.shiny.http.status.ServerErrorStatusCode;

public class ProcessingFailure extends ServerException {
    public ProcessingFailure(String message, ServerErrorStatusCode statusCode) {
        super(message, statusCode.statusCode);
    }
}
