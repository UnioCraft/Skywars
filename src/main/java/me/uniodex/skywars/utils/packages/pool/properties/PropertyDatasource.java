package me.uniodex.skywars.utils.packages.pool.properties;

import com.zaxxer.hikari.HikariConfig;

import javax.sql.DataSource;

public class PropertyDatasource implements HikariProperty {

    private final DataSource value;

    public PropertyDatasource(DataSource value) {
        this.value = value;
    }

    @Override
    public void applyTo(HikariConfig config) {
        config.setDataSource(value);
    }

}
