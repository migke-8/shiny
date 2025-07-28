package migke.shiny.http;

public enum HttpMethod {
    GET("GET"),
    POST("POST"),
    PUT("PUT"),
    DELETE("DELETE"),
    PATCH("PATCH");

    private final String name;

    HttpMethod(String name) {
        this.name = name;
    }

    public String toString() {
        return this.name;
    }

}
