package me.uniodex.skywars.utils.packages.pool.properties;

import com.zaxxer.hikari.HikariConfig;

public class PropertyDriverClassName implements HikariProperty {

    private final String value;

    public PropertyDriverClassName(String value) {
        this.value = value;
    }

    @Override
    public void applyTo(HikariConfig config) {
        config.setDriverClassName(value);
    }

}
