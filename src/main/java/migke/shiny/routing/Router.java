package migke.shiny.routing;

import migke.shiny.http.HttpMethod;
import migke.shiny.http.HttpRequest;
import migke.shiny.http.HttpResponse;

import java.util.*;
import java.util.function.Function;

import java.util.stream.Stream;

public abstract class Router {
    private final SegmentNode root;
    private final RouteCache cache;

    private Router(SegmentNode root, int cacheSize) {
        this.root = root;
        this.cache = new RouteCache(cacheSize);
    }
    public Router(int cacheSize) {
        this.root = new SegmentNode("");
        this.cache = new RouteCache(cacheSize);
    }

    protected Optional<Function<HttpRequest, HttpResponse>> findHandler(HttpMethod method, String path) {
        var foundInCache = this.cache.get(new HttpRouteDefinition(method, path));
        System.out.println("cache: "+foundInCache.isPresent());
        if(foundInCache.isPresent()) return Optional.of(foundInCache.get().handler());
        var found = root.searchHandler(method, path);
        System.out.println("tree: "+found.isPresent());
        if(found.isPresent()) this.cache.set(HttpRoute.of(method, path, found.get()));
        return found;
    }

    public Router route(Function<SegmentNode, SegmentNode>... funcs) {
        Stream.of(funcs).forEach(f -> f.apply(root));
        return this;
    }

    @SafeVarargs
    public static Function<SegmentNode, SegmentNode> nest(Function<SegmentNode, SegmentNode>... funcs) {
        return (node) -> {
            for(var func : funcs) node = func.apply(node);
            return node;
        };
    }
    public static Function<SegmentNode, SegmentNode> path(String path) {
        return (node) -> node.add(path);
    }
    public static Function<SegmentNode, SegmentNode> path(String path, HttpRoute... routes) {
        return (node) -> {
            node = node.add(path);
            for(var route : routes) node = node.add(route);
            return node;
        };
    }

    public static HttpRoute get(Function<HttpRequest, HttpResponse> handler) {
        return HttpRoute.of(HttpMethod.GET, "", handler);
    }
    public static HttpRoute post(Function<HttpRequest, HttpResponse> handler) {
        return HttpRoute.of(HttpMethod.POST, "", handler);
    }
    public static HttpRoute put(Function<HttpRequest, HttpResponse> handler) {
        return HttpRoute.of(HttpMethod.PUT, "", handler);
    }
    public static HttpRoute patch(Function<HttpRequest, HttpResponse> handler) {
        return HttpRoute.of(HttpMethod.PATCH, "", handler);
    }
    public static HttpRoute delete(Function<HttpRequest, HttpResponse> handler) {
        return HttpRoute.of(HttpMethod.DELETE, "", handler);
    }
}
