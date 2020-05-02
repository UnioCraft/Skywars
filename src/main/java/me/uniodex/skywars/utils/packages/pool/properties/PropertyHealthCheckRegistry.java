package me.uniodex.skywars.utils.packages.pool.properties;

import com.zaxxer.hikari.HikariConfig;

public class PropertyHealthCheckRegistry implements HikariProperty {

    private final Object value;

    public PropertyHealthCheckRegistry(Object value) {
        this.value = value;
    }

    @Override
    public void applyTo(HikariConfig config) {
        config.setHealthCheckRegistry(value);
    }

}
