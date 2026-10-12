package uk.co.devfoundry.library.config;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Turns DATABASE_URL (sqlite:///library.db or postgresql://user:pass@host:5432/db) into Spring properties.
 * This is the only place that knows the two URL forms.
 */
public record DatabaseUrl(String kind, String jdbcUrl, String username, String password) {

    public static final String DEFAULT = "sqlite:///library.db";
    public static final String SQLITE = "sqlite";
    public static final String POSTGRESQL = "postgresql";

    public static DatabaseUrl parse(String url) {
        if (url == null || url.isBlank()) {
            url = DEFAULT;
        }
        if (url.startsWith("sqlite:")) {
            return sqlite(url);
        }
        if (url.startsWith("postgresql://") || url.startsWith("postgres://")) {
            return postgresql(url);
        }
        throw new IllegalArgumentException(
                "DATABASE_URL must start with sqlite:/// or postgresql://, got " + url);
    }

    // sqlite:///library.db is a relative path, sqlite:////data/library.db an absolute one,
    // and sqlite:// on its own an in-memory database (used by the unit tests).
    private static DatabaseUrl sqlite(String url) {
        String path;
        if (url.equals("sqlite://") || url.equals("sqlite:///:memory:")) {
            path = ":memory:";
        } else if (url.startsWith("sqlite:///")) {
            path = url.substring("sqlite:///".length());
        } else {
            throw new IllegalArgumentException("a SQLite DATABASE_URL looks like sqlite:///library.db, got " + url);
        }
        return new DatabaseUrl(SQLITE, "jdbc:sqlite:" + path, null, null);
    }

    private static DatabaseUrl postgresql(String url) {
        URI uri = URI.create(url);
        String username = null;
        String password = null;
        if (uri.getRawUserInfo() != null) {
            String[] parts = uri.getRawUserInfo().split(":", 2);
            username = decode(parts[0]);
            password = parts.length > 1 ? decode(parts[1]) : null;
        }
        int port = uri.getPort() == -1 ? 5432 : uri.getPort();
        String jdbcUrl = "jdbc:postgresql://" + uri.getHost() + ":" + port + uri.getRawPath();
        if (uri.getRawQuery() != null) {
            jdbcUrl += "?" + uri.getRawQuery();
        }
        return new DatabaseUrl(POSTGRESQL, jdbcUrl, username, password);
    }

    private static String decode(String text) {
        return URLDecoder.decode(text, StandardCharsets.UTF_8);
    }

    /** The Spring Boot properties for this database. */
    public Map<String, Object> springProperties() {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("library.database", kind);
        properties.put("spring.datasource.url", jdbcUrl);
        properties.put("spring.flyway.locations", "classpath:db/migration/" + kind);
        if (username != null) {
            properties.put("spring.datasource.username", username);
        }
        if (password != null) {
            properties.put("spring.datasource.password", password);
        }
        if (kind.equals(SQLITE)) {
            properties.put("spring.datasource.driver-class-name", "org.sqlite.JDBC");
            properties.put("spring.jpa.database-platform", "org.hibernate.community.dialect.SQLiteDialect");
            // SQLite allows one writer at a time, and an in-memory database lives as long as its connection.
            properties.put("spring.datasource.hikari.maximum-pool-size", "1");
            properties.put("spring.datasource.hikari.max-lifetime", "0");
            // Enforce foreign keys and store dates as YYYY-MM-DD text, as in db/schema.sql.
            properties.put("spring.datasource.hikari.data-source-properties.foreign_keys", "true");
            properties.put("spring.datasource.hikari.data-source-properties.date_class", "TEXT");
            properties.put("spring.datasource.hikari.data-source-properties.date_string_format", "yyyy-MM-dd");
        }
        return properties;
    }
}
