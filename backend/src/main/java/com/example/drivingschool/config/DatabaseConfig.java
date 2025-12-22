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

    @Value("${DATABASE_URL:}")
    private String databaseUrl;

    @Bean
    @Primary
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        
        if (databaseUrl != null && !databaseUrl.isEmpty() && !databaseUrl.startsWith("${")) {
            try {
                // Handle postgresql:// or postgres:// URLs
                String url = databaseUrl;
                if (url.startsWith("postgres://")) {
                    url = url.replace("postgres://", "postgresql://");
                }
                
                URI dbUri = new URI(url);
                String[] userInfo = dbUri.getUserInfo().split(":");
                String username = userInfo[0];
                String password = userInfo.length > 1 ? userInfo[1] : "";
                
                // Build JDBC URL - include port if specified, otherwise use default 5432
                int port = dbUri.getPort();
                String dbUrl;
                if (port > 0) {
                    dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ':' + port + dbUri.getPath();
                } else {
                    dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ":5432" + dbUri.getPath();
                }
                
                System.out.println("Parsed DATABASE_URL - Host: " + dbUri.getHost() + ", Database: " + dbUri.getPath());
                
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
                System.err.println("Failed to parse DATABASE_URL: " + e.getMessage());
                System.err.println("DATABASE_URL value: " + databaseUrl);
                e.printStackTrace();
                throw new RuntimeException("Failed to configure database from DATABASE_URL", e);
            }
        } else {
            System.err.println("DATABASE_URL is empty or not set. Value: " + databaseUrl);
            throw new RuntimeException("DATABASE_URL environment variable is required for render profile");
        }
        
        return new HikariDataSource(config);
    }
}
