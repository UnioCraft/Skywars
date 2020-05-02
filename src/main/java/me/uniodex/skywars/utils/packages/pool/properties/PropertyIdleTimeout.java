package me.uniodex.skywars.utils.packages.pool.properties;

import com.zaxxer.hikari.HikariConfig;

public class PropertyIdleTimeout implements HikariProperty {

    private final long value;

    public PropertyIdleTimeout(long value) {
        this.value = value;
    }

    @Override
    public void applyTo(HikariConfig config) {
        config.setIdleTimeout(value);
    }

}
