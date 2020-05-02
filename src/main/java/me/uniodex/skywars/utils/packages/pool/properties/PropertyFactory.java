package me.uniodex.skywars.utils.packages.pool.properties;

import javax.sql.DataSource;
import java.util.concurrent.ThreadFactory;

public final class PropertyFactory {

    private PropertyFactory() {
        throw new UnsupportedOperationException();
    }

    public static HikariProperty allowPoolSuspension(boolean value) {
        return new PropertyAllowPoolSuspension(value);
    }

    public static HikariProperty autoCommit(boolean value) {
        return new PropertyAutoCommit(value);
    }

    public static HikariProperty catalog(String value) {
        return new PropertyCatalog(value);
    }

    public static HikariProperty connectionInitSql(String value) {
        return new PropertyConnectionInitSql(value);
    }

    public static HikariProperty connectionTestQuery(String value) {
        return new PropertyConnectionTestQuery(value);
    }

    public static HikariProperty connectionTimeout(long value) {
        return new PropertyConnectionTimeout(value);
    }

    public static HikariProperty datasource(DataSource value) {
        return new PropertyDatasource(value);
    }

    public static HikariProperty driverClassName(String value) {
        return new PropertyDriverClassName(value);
    }

    public static HikariProperty healthCheckRegistry(Object value) {
        return new PropertyHealthCheckRegistry(value);
    }

    public static HikariProperty idleTimeout(long value) {
        return new PropertyIdleTimeout(value);
    }

    public static HikariProperty initializationFailFast(boolean value) {
        return new PropertyInitializationFailFast(value);
    }

    public static HikariProperty isolateInternalQueries(boolean value) {
        return new PropertyIsolateInternalQueries(value);
    }

    public static HikariProperty leakDetectionThreshold(long value) {
        return new PropertyLeakDetectionThreshold(value);
    }

    public static HikariProperty maximumPoolSize(int value) {
        return new PropertyMaximumPoolSize(value);
    }

    public static HikariProperty maxLifetime(long value) {
        return new PropertyMaxLifetime(value);
    }

    public static HikariProperty metricRegistry(Object value) {
        return new PropertyMetricRegistry(value);
    }

    public static HikariProperty minimumIdle(int value) {
        return new PropertyMinimumIdle(value);
    }

    public static HikariProperty readOnly(boolean value) {
        return new PropertyReadOnly(value);
    }

    public static HikariProperty registerMbeans(boolean value) {
        return new PropertyRegisterMbeans(value);
    }

    public static HikariProperty threadFactory(ThreadFactory value) {
        return new PropertyThreadFactory(value);
    }

    public static HikariProperty transactionIsolation(String value) {
        return new PropertyTransactionIsolation(value);
    }

    public static HikariProperty validationTimeout(long value) {
        return new PropertyValidationTimeout(value);
    }

}
