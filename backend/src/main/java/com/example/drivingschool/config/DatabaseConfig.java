package com.example.drivingschool.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Configuration
@Profile("render")
public class DatabaseConfig {

    /** Internal URL (from Render when DB is linked); host may be short name that does not resolve in some setups. */
    @Value("${DATABASE_URL:}")
    private String databaseUrl;

    /** External URL (from Render Connect → External). Use this if you get UnknownHostException for the DB host. */
    @Value("${EXTERNAL_DATABASE_URL:}")
    private String externalDatabaseUrl;

    @Bean
    @Primary
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        // Prefer external URL if set (resolvable hostname); otherwise use DATABASE_URL (internal)
        String urlToUse = (externalDatabaseUrl != null && !externalDatabaseUrl.isEmpty() && !externalDatabaseUrl.startsWith("${"))
            ? externalDatabaseUrl
            : databaseUrl;

        if (urlToUse != null && !urlToUse.isEmpty() && !urlToUse.startsWith("${")) {
            try {
                // Handle postgresql:// or postgres:// URLs
                String url = urlToUse;
                if (url.startsWith("postgres://")) {
                    url = url.replace("postgres://", "postgresql://");
                }
                
                URI dbUri = new URI(url);
                // Split on first ':' only so passwords containing ':' are preserved
                String userInfo = dbUri.getUserInfo();
                String username;
                String password;
                if (userInfo != null && userInfo.contains(":")) {
                    int firstColon = userInfo.indexOf(':');
                    username = userInfo.substring(0, firstColon);
                    password = userInfo.substring(firstColon + 1);
                } else {
                    username = userInfo != null ? userInfo : "";
                    password = "";
                }
                
                // Build JDBC URL with user/password in URL (avoids encoding issues with Hikari)
                int port = dbUri.getPort() > 0 ? dbUri.getPort() : 5432;
                String path = dbUri.getPath();
                if (path != null && path.startsWith("/")) path = path.substring(1);
                String dbName = (path != null && !path.isEmpty()) ? path : "postgres";
                String encUser = URLEncoder.encode(username, StandardCharsets.UTF_8);
                String encPass = URLEncoder.encode(password, StandardCharsets.UTF_8);
                String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ':' + port + "/" + dbName
                    + "?user=" + encUser + "&password=" + encPass
                    + "&ssl=true&sslmode=require"
                    + "&connectTimeout=60&socketTimeout=60";
                
                System.out.println("Parsed DB URL - Host: " + dbUri.getHost() + ", Database: " + dbUri.getPath()
                    + (urlToUse == externalDatabaseUrl ? " (EXTERNAL_DATABASE_URL)" : " (DATABASE_URL)"));
                
                config.setJdbcUrl(dbUrl);
                config.setDriverClassName("org.postgresql.Driver");
                // Do not set username/password - they are in the JDBC URL
                
                // Connection pool: longer timeouts for Render (DB may be waking from pause)
                config.setMaximumPoolSize(10);
                config.setMinimumIdle(2);
                config.setConnectionTimeout(60000);      // 60s wait for first connection
                config.setInitializationFailTimeout(120000); // 2 min before failing startup (allows retries)
                config.setIdleTimeout(600000);
                config.setMaxLifetime(1800000);
                
                // Retry connection a few times (Render free DB may be waking from pause)
                int maxAttempts = 5;
                int waitSeconds = 15;
                Exception lastException = null;
                for (int attempt = 1; attempt <= maxAttempts; attempt++) {
                    try {
                        System.out.println("DB connection attempt " + attempt + "/" + maxAttempts + "...");
                        return new HikariDataSource(config);
                    } catch (Exception e) {
                        lastException = e;
                        if (attempt < maxAttempts) {
                            System.err.println("Connection failed, retrying in " + waitSeconds + "s: " + e.getMessage());
                            try { Thread.sleep(waitSeconds * 1000L); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); break; }
                        }
                    }
                }
                throw lastException != null ? new RuntimeException("Database connection failed after " + maxAttempts + " attempts", lastException) : new RuntimeException("Database connection failed");
                
            } catch (URISyntaxException | ArrayIndexOutOfBoundsException e) {
                System.err.println("Failed to parse database URL: " + e.getMessage());
                System.err.println("URL value: " + urlToUse);
                e.printStackTrace();
                throw new RuntimeException("Failed to configure database from DATABASE_URL/EXTERNAL_DATABASE_URL", e);
            }
        } else {
            System.err.println("DATABASE_URL (and EXTERNAL_DATABASE_URL) are empty or not set.");
            throw new RuntimeException("DATABASE_URL or EXTERNAL_DATABASE_URL environment variable is required for render profile");
        }
    }
}
