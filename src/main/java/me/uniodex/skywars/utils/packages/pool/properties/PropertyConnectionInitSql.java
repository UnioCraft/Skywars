package me.uniodex.skywars.utils.packages.pool.properties;

import com.zaxxer.hikari.HikariConfig;

public class PropertyConnectionInitSql implements HikariProperty {

    private final String value;

    public PropertyConnectionInitSql(String value) {
        this.value = value;
    }

    @Override
    public void applyTo(HikariConfig config) {
        config.setConnectionInitSql(value);
    }

}
