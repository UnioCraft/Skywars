package me.uniodex.skywars.utils.packages.pool.properties;

import com.zaxxer.hikari.HikariConfig;

public class PropertyConnectionTimeout implements HikariProperty {

    private final long value;

    public PropertyConnectionTimeout(long value) {
        this.value = value;
    }

    @Override
    public void applyTo(HikariConfig config) {
        config.setConnectionTimeout(value);
    }

}
