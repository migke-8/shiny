package migke.shiny.routing;

import migke.shiny.http.HttpMethod;
import migke.shiny.http.HttpRequest;
import migke.shiny.http.HttpResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class SegmentNode {
    public final Map<String, SegmentNode> children;
    public final String segment;
    public final Map<HttpMethod, Function<HttpRequest, HttpResponse>> handlers;
    private ParameterNode paramNode;
    private SegmentNode wildCardNode;

    private SegmentNode(String segment, Map<String, SegmentNode> children, Map<HttpMethod, Function<HttpRequest, HttpResponse>> handlers) {
        this.segment = segment;
        this.children = children;
        this.handlers = handlers;
    }

    public SegmentNode(String segment) {
        this.segment = segment;
        this.children = new HashMap<>();
        this.handlers = new HashMap<>();
    }

    public Optional<Function<HttpRequest, HttpResponse>> searchHandler(HttpMethod method, String path) {
        var leftCut = cutLeft(path);
        if(leftCut.isEmpty()) return Optional.empty();
        var segment = cutRight(leftCut);
        var staticNode = this.children.get(segment);
        if(staticNode != null) {
            if(leftCut.equals(segment)) return staticNode.getHandler(method);
            var result = staticNode.searchHandler(method, leftCut);
            if(result.isPresent()) return result;
        }
        var paramNode = this.paramNode;
        if(paramNode != null) {
            if(leftCut.equals(segment)) return paramNode.getHandler(method, segment);
            var result = paramNode.searchHandler(method, leftCut, segment);
            if(result.isPresent()) return result;
        }
        var wildCardNode = this.wildCardNode;
        if(wildCardNode != null) {
            if(leftCut.equals(segment)) return wildCardNode.getHandler(method);
            var result = wildCardNode.searchHandler(method, leftCut);
            if(result.isPresent()) return result;
        }
        return Optional.empty();
    }

    public SegmentNode add(String path) {
        var leftCut = cutLeft(path);
        var segment = cutRight(leftCut);
        System.out.println(segment);
        if(segment.equals(path.replace("/", ""))) return this;
        if (segment.charAt(0) == '{' && segment.charAt(segment.length() - 1) == '}') {
            this.paramNode = this.paramNode != null ? this.paramNode : new ParameterNode(segment.substring(1, segment.length() - 1));
            return this.paramNode.add(leftCut);
        }
        else if(segment.equals("**")) {
            this.wildCardNode = this.wildCardNode != null ? this.wildCardNode : new SegmentNode(segment);
            return this.wildCardNode.add(leftCut);
        }
        var oldNode = this.children.get(segment);
        var node = oldNode != null ? oldNode : new SegmentNode(segment);
        this.children.put(segment, node);
        return node.add(leftCut);
    }

    public SegmentNode add(HttpRoute route) {
        var node = this.add(route.path());
        System.out.println(route.method() + " " + node.segment);
        node.handlers.put(route.method(), route.handler());
        return node;
    }

    public SegmentNode createCopy() {
        return new SegmentNode(segment, new HashMap<>(children), new HashMap<>(handlers));
    }

    protected Optional<Function<HttpRequest, HttpResponse>> getHandler(HttpMethod method) {
        return Optional.ofNullable(handlers.get(method));
    }

    protected String cutLeft(String url) {
        var index = url.indexOf('/');
        return index == -1 ? url : url.substring(index + 1);
    }

    protected String cutRight(String url) {
        var index = url.indexOf("/");
        return index == -1 ? url : url.substring(0, index);
    }

    private SegmentNode getCorrectNode(String segment) {
        if (segment.replace("/", "").isEmpty()) return null;
        var node = this;
        var child = node.children.get(segment);
        var paramNode = node.paramNode;
        var wildCardNode = node.wildCardNode;
        if (child != null)
            return child;
        else if (paramNode != null)
            return paramNode;
        else return wildCardNode;
    }
}