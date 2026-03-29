package com.easystation.health.check;

import com.easystation.health.dto.HealthCheckResult;
import io.agroal.api.AgroalDataSource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 数据库健康检查
 */
@ApplicationScoped
public class DatabaseHealthCheck {

    @Inject
    AgroalDataSource dataSource;

    /**
     * 检查数据库连接
     * 
     * @return 健康检查结果
     */
    public HealthCheckResult check() {
        long startTime = System.currentTimeMillis();
        try {
            Connection connection = dataSource.getConnection();
            if (connection != null && !connection.isClosed()) {
                connection.close();
                long responseTime = System.currentTimeMillis() - startTime;
                return HealthCheckResult.up("Database", "Database connection successful", responseTime);
            } else {
                long responseTime = System.currentTimeMillis() - startTime;
                return HealthCheckResult.down("Database", "Database connection is closed", responseTime);
            }
        } catch (SQLException e) {
            long responseTime = System.currentTimeMillis() - startTime;
            return HealthCheckResult.down("Database", "Database connection failed: " + e.getMessage(), responseTime);
        }
    }
}
