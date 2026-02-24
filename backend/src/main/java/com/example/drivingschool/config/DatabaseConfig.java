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
                String[] userInfo = dbUri.getUserInfo().split(":");
                String username = userInfo[0];
                String password = userInfo.length > 1 ? userInfo[1] : "";
                
                // Build JDBC URL - include port if specified, otherwise use default 5432
                // Add SSL parameters for Render PostgreSQL (required for external connections)
                int port = dbUri.getPort();
                String dbUrl;
                if (port > 0) {
                    dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ':' + port + dbUri.getPath() + "?ssl=true&sslmode=require";
                } else {
                    dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ":5432" + dbUri.getPath() + "?ssl=true&sslmode=require";
                }
                
                System.out.println("Parsed DB URL - Host: " + dbUri.getHost() + ", Database: " + dbUri.getPath()
                    + (urlToUse == externalDatabaseUrl ? " (EXTERNAL_DATABASE_URL)" : " (DATABASE_URL)"));
                
                config.setJdbcUrl(dbUrl);
                config.setUsername(username);
                config.setPassword(password);
                config.setDriverClassName("org.postgresql.Driver");
                
                // Connection pool settings
                config.setMaximumPoolSize(10);
                config.setMinimumIdle(2);
                config.setConnectionTimeout(30000);
                config.setIdleTimeout(600000);
                config.setMaxLifetime(1800000);
                
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
        
        return new HikariDataSource(config);
    }
}
