package migke.shiny.http;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public record HttpCookie(
    String name,
    String value,
    Optional<LocalDateTime> expires,
    Optional<Long> maxAge
) {
    public static HttpCookie of(String name, String value) {
        return new HttpCookie(name, value, Optional.empty(), Optional.empty());
    }
    public String toString() {
        return String.format("%s=%s;%s%s", name, value, this.maxAgeToString(), this.expiresToString());
    }
    private String expiresToString() {
        var formatter = DateTimeFormatter.ofPattern("EEE, dd-MMM-yyyy HH:mm:ss 'GMT'");
        return this.expires.map(dateTime -> String.format(" Expires=%s;", dateTime.atZone(ZoneOffset.UTC).format(formatter))).orElse("");
    }
    private String maxAgeToString() {
        return maxAge.map(aLong -> String.format(" Max-Age: %d;", aLong)).orElse("");
    }
}
