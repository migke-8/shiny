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
        var segment = cutRight(leftCut);
        if(path.equals(leftCut) || leftCut.isEmpty())
            return switch (this) {
                case ParameterNode n -> n.getHandler(method, segment);
                default -> this.getHandler(method);
            };
        if(leftCut.equals(segment))
            return switch (this) {
                case ParameterNode n -> n.searchHandler(method, path, segment);
                default -> this.searchHandler(method, path);
            };
        var staticNode = this.children.get(segment);
        if(staticNode != null) {
            var result = staticNode.searchHandler(method, leftCut);
            if(result.isPresent()) return result;
        }
        var paramNode = this.paramNode;
        if(paramNode != null) {
            var result = paramNode.searchHandler(method, leftCut, segment);
            if(result.isPresent()) return result;
        }
        var wildCardNode = this.wildCardNode;
        if(wildCardNode != null) {
            var result = wildCardNode.searchHandler(method, leftCut);
            if(result.isPresent()) return result;
        }
        return Optional.empty();

        /*return switch (node) {
            case ParameterNode n -> n.searchHandler(method, leftCut, segment);
            case null -> {
                var hasEndedSearch = leftCut.isEmpty() || leftCut.equals(path);
                yield hasEndedSearch ? this.getHandler(method) : Optional.empty();
            }
            default -> {
                var matchesStatic = segment.equals(node.segment);
                var matchesWildCard = node.segment.equals("**");
                if(matchesStatic){
                    var staticMatch = node.searchHandler(method, leftCut);
                    if(staticMatch.isPresent()) yield staticMatch;
                }
                if(matchesWildCard) {
                    var wildCardMatch = node.searchHandler(method, leftCut);
                    if(wildCardMatch.isPresent()) yield wildCardMatch;
                }
                yield Optional.empty();
            }
        };*/
    }

    public SegmentNode add(String path) {
        var currentNode = this;
        var leftCut = cutLeft(path);
        var segment = "";

        while (!segment.equals(leftCut)) {
            segment = cutRight(leftCut);
            leftCut = cutLeft(leftCut);
            System.out.println(currentNode.segment + ": " + segment);
            if ((!segment.equals("")) && !segment.equals("/")) {
                var node = new SegmentNode(segment);
                if (segment.charAt(0) == '{' && segment.charAt(segment.length() - 1) == '}') {
                    var oldParamNode = currentNode.paramNode;
                    if(oldParamNode != null) node = oldParamNode;
                    else node = new ParameterNode(segment.substring(1, segment.length() - 1));
                    currentNode.paramNode = (ParameterNode) node;
                }
                else if (segment.equals("**")) {
                    var oldWildCardNode = currentNode.wildCardNode;
                    if (oldWildCardNode != null) node = oldWildCardNode;
                    currentNode.wildCardNode = node;
                }
                else {
                    var oldStaticNode = currentNode.children.get(segment);
                    if(oldStaticNode != null) node = oldStaticNode;
                    currentNode.children.put(segment, node);
                }
                currentNode = node;
            }
        }
        return currentNode;
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