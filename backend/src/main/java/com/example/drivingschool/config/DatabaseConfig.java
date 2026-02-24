package com.example.drivingschool.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
@ConditionalOnProperty(name = "USE_POSTGRES", havingValue = "true")
public class DatabaseConfig {

    /** Internal URL (from Render when DB is linked); host may be short name that does not resolve in some setups. */
    @Value("${DATABASE_URL:}")
    private String databaseUrl;

    /** External URL (from Render Connect → External). Use this if you get UnknownHostException for the DB host. */
    @Value("${EXTERNAL_DATABASE_URL:}")
    private String externalDatabaseUrl;

    /** Optional: use DB_URL + DB_USER + DB_PASSWORD instead of DATABASE_URL (no URL parsing). */
    @Value("${DB_URL:}")
    private String dbUrl;
    @Value("${DB_USER:}")
    private String dbUser;
    @Value("${DB_PASSWORD:}")
    private String dbPassword;

    @Bean
    @Primary
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(60000);
        config.setInitializationFailTimeout(-1);  // don't block startup; pool connects on first use
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        config.setDriverClassName("org.postgresql.Driver");

        // Path 1: DB_URL + DB_USER + DB_PASSWORD (no parsing; use if DATABASE_URL gives EOF)
        if (dbUrl != null && !dbUrl.isEmpty() && !dbUrl.startsWith("${")) {
            String jdbcUrl = dbUrl.startsWith("jdbc:") ? dbUrl : "jdbc:postgresql://" + dbUrl;
            if (!jdbcUrl.contains("ssl=")) jdbcUrl += (jdbcUrl.contains("?") ? "&" : "?") + "ssl=true&sslmode=require";
            config.setJdbcUrl(jdbcUrl);
            config.setUsername(dbUser != null ? dbUser : "");
            config.setPassword(dbPassword != null ? dbPassword : "");
            System.out.println("Using DB_URL (Host from env). SSL added if missing.");
            return new HikariDataSource(config);
        }

        // Path 2: EXTERNAL_DATABASE_URL or DATABASE_URL (parsed)
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
                // Do not set username/password - they are in the JDBC URL
                
                return new HikariDataSource(config);
                
            } catch (URISyntaxException | ArrayIndexOutOfBoundsException e) {
                System.err.println("Failed to parse database URL: " + e.getMessage());
                System.err.println("URL value: " + urlToUse);
                e.printStackTrace();
                throw new RuntimeException("Failed to configure database from DATABASE_URL/EXTERNAL_DATABASE_URL", e);
            }
        } else {
            System.err.println("No DB config: set DATABASE_URL, EXTERNAL_DATABASE_URL, or DB_URL+DB_USER+DB_PASSWORD");
            throw new RuntimeException("DATABASE_URL, EXTERNAL_DATABASE_URL, or DB_URL+DB_USER+DB_PASSWORD is required for render profile");
        }
    }
}
