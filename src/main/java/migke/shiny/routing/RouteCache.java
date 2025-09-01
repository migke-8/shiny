package migke.shiny.routing;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class RouteCache {
    private final Map<HttpRouteDefinition, HttpRoute> map;
    private HttpRoute[] routes;
    private int counter;
    private int addIndex;
    private int size;

    public RouteCache(int size) {
        this.routes = new HttpRoute[size];
        this.map = new HashMap<>(size);
        this.size = routes.length;
    }

    public void set(HttpRoute route) {
        if(this.canAdd(route)) this.add(route);
        this.safelyIncrementCounter();
        if(this.canClear()) clear();
    }

    public Optional<HttpRoute> get(HttpRouteDefinition definition) {
        return Optional.ofNullable(map.get(definition));
    }

    private void clear() {
        this.map.remove(this.routes[addIndex].definition());
        this.routes[addIndex] = null;
    }

    private boolean canClear() {
        return this.counter == 0;
    }

    private void add(HttpRoute route) {
        this.routes[this.addIndex] = route;
        this.map.put(route.definition(), route);
        this.addIndex = (this.addIndex + 1) % this.size;
    }

    private boolean canAdd(HttpRoute route) {
        var isSpaceAvailable = routes[addIndex]== null;
        var doesExist = map.containsKey(route.definition());
        return isSpaceAvailable && !doesExist;
    }
    private void safelyIncrementCounter() {
        this.counter = (this.counter + 1) % this.size;
    }
}