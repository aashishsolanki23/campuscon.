package com.campuscon.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Simple monitoring configuration for CampusCon
 * Performs periodic health checks and logs the results
 * Compatible with existing dependencies
 */
@Configuration
@EnableScheduling
@Slf4j
public class MonitoringConfig {

    private final DataSource dataSource;

    @Autowired(required = false)
    public MonitoringConfig(DataSource dataSource) {
        this.dataSource = dataSource;
        log.info("CampusCon monitoring configuration initialized");
    }

    /**
     * Perform a basic database health check every 5 minutes
     * Only active in production mode
     */
    @Scheduled(fixedRate = 5, timeUnit = TimeUnit.MINUTES)
    @Profile("production")
    public void checkDatabaseHealth() {
        if (dataSource != null) {
            try {
                JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
                jdbcTemplate.queryForObject("SELECT 1", Integer.class);
                log.info("Database health check: OK");
            } catch (Exception e) {
                log.error("Database health check failed: {}", e.getMessage());
            }
        }
    }

    /**
     * Log system resource usage every 15 minutes
     * Active in all environments but with different frequencies
     */
    @Scheduled(fixedRateString = "${monitoring.resource.check.rate:900000}")
    public void logResourceUsage() {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory() / (1024 * 1024);
        long totalMemory = runtime.totalMemory() / (1024 * 1024);
        long freeMemory = runtime.freeMemory() / (1024 * 1024);
        long usedMemory = totalMemory - freeMemory;
        
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("usedMemoryMB", usedMemory);
        metrics.put("maxMemoryMB", maxMemory);
        metrics.put("memoryUsagePercent", (usedMemory * 100) / maxMemory);
        metrics.put("availableProcessors", runtime.availableProcessors());
        
        log.info("CampusCon system resources: {}", metrics);
    }
}
