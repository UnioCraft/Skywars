package me.uniodex.skywars.utils.packages.pool.properties;

import com.zaxxer.hikari.HikariConfig;

public class PropertyMaxLifetime implements HikariProperty {

    private final long value;

    public PropertyMaxLifetime(long value) {
        this.value = value;
    }

    @Override
    public void applyTo(HikariConfig config) {
        config.setMaxLifetime(value);
    }

}
