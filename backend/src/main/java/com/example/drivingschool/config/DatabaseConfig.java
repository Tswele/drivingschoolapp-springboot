package com.example.drivingschool.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import java.net.URI;
import java.net.URISyntaxException;

@Configuration
@Profile("render")
public class DatabaseConfig {

    @Value("${DATABASE_URL:}")
    private String databaseUrl;

    @Bean
    @Primary
    public DataSourceProperties dataSourceProperties() {
        DataSourceProperties properties = new DataSourceProperties();
        
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
                
                // Build JDBC URL
                String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ':' + dbUri.getPort() + dbUri.getPath();
                
                System.out.println("Parsed DATABASE_URL - Host: " + dbUri.getHost() + ", Database: " + dbUri.getPath());
                
                properties.setUrl(dbUrl);
                properties.setUsername(username);
                properties.setPassword(password);
            } catch (URISyntaxException | ArrayIndexOutOfBoundsException e) {
                System.err.println("Failed to parse DATABASE_URL: " + e.getMessage());
                System.err.println("DATABASE_URL value: " + databaseUrl);
                e.printStackTrace();
            }
        } else {
            System.err.println("DATABASE_URL is empty or not set. Value: " + databaseUrl);
        }
        
        return properties;
    }
}

