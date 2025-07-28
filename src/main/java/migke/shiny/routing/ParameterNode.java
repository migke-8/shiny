package migke.shiny.routing;

import migke.shiny.http.HttpMethod;
import migke.shiny.http.HttpRequest;
import migke.shiny.http.HttpResponse;

import java.util.Optional;
import java.util.function.Function;

public class ParameterNode extends SegmentNode {
    public ParameterNode(String segment) {
        super(segment);
    }
    public Optional<Function<HttpRequest, HttpResponse>> searchHandler(HttpMethod method, String path, String value) {
        if(path.replace("/", "").equals(value))
            return this.getHandler(method, value);
        return wrapHandler(super.searchHandler(method, path), value);
        /*var handler = super.searchHandler(method, path);
        if(handler.isEmpty()) return this.getHandler(method, value);
        if(!path.replace("/", "").equals(value)) return this.wrapHandler(handler, value);
        return Optional.empty();*/
    }
    public Optional<Function<HttpRequest, HttpResponse>> getHandler(HttpMethod method, String value) {
        return this.wrapHandler(this.getHandler(method), value);
    }
    private Optional<Function<HttpRequest, HttpResponse>> wrapHandler(Optional<Function<HttpRequest, HttpResponse>> handler, String value) {
        return handler.map(f -> (req) -> f.apply(HttpRequest.addParam(req, this.segment, value)));
    }
}
