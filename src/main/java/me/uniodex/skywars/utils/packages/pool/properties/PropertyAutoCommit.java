package me.uniodex.skywars.utils.packages.pool.properties;

import com.zaxxer.hikari.HikariConfig;

public class PropertyAutoCommit implements HikariProperty {

    private final boolean value;

    public PropertyAutoCommit(boolean value) {
        this.value = value;
    }

    @Override
    public void applyTo(HikariConfig config) {
        config.setAutoCommit(value);
    }

}
