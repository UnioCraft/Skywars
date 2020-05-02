package me.uniodex.skywars.utils.packages.pool;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.uniodex.skywars.utils.packages.pool.properties.HikariProperty;
import me.uniodex.skywars.utils.packages.pool.properties.PropertyFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("deprecation")
public class Pool {

    private final PoolCredentialPackage credentials;
    private final PoolDriver driver;
    private final List<HikariProperty> properties = new ArrayList<>();
    private HikariDataSource dataSource;
    private String url;

    public Pool(PoolCredentialPackage credentials) {
        this(credentials, PoolDriver.MYSQL);
    }

    public Pool(PoolCredentialPackage credentials, PoolDriver driver) {
        this.credentials = credentials;
        this.driver = driver;
    }

    public void build() {
        if (url == null && driver == null) {
            throw new IllegalStateException("Please set a URL / Driver!");
        }
        HikariConfig config = new HikariConfig();
        if (url != null) {
            config.setJdbcUrl(url);
        } else {
            config.setDataSourceClassName(driver.getClassName());
        }
        config.setUsername(credentials.getUsername());
        config.setPassword(credentials.getPassword());
        for (HikariProperty property : properties) {
            property.applyTo(config);
        }
        config.setPoolName("SkywarsPool");
        System.out.println("URL: " + config.getJdbcUrl());
        dataSource = new HikariDataSource(config);
    }

    public Connection getConnection() throws SQLException {
        validate();
        return dataSource.getConnection();
    }

    public HikariDataSource getDataSource() {
        return dataSource;
    }

    public boolean isClosed() {
        validate();
        return dataSource.isClosed();
    }

    public void close() {
        validate();
        dataSource.close();
    }

    public void suspend() {
        validate();
        dataSource.suspendPool();
    }

    public void resume() {
        validate();
        dataSource.resumePool();
    }

    private void validate() {
        if (dataSource == null) {
            throw new IllegalStateException("Please call build() before running pool operations!");
        }
    }

    public Pool withProperty(HikariProperty property) {
        properties.add(property);
        return this;
    }

    public Pool withProperties(HikariProperty... properties) {
        this.properties.addAll(Arrays.asList(properties));
        return this;
    }

    public Pool withUrl(String url) {
        this.url = url;
        return this;
    }

    public Pool withMysqlUrl(String hostname, String database) {
        withUrl(String.format("jdbc:mysql://%s:%d/%s", hostname, 3306, database));
        return this;
    }

    public Pool withMax(int max) {
        return withProperty(PropertyFactory.maximumPoolSize(max));
    }

    public Pool withMin(int min) {
        return withProperty(PropertyFactory.minimumIdle(min));
    }

}
