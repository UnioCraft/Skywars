package me.uniodex.skywars.utils.packages.pool.properties;

import com.zaxxer.hikari.HikariConfig;

public class PropertyIsolateInternalQueries implements HikariProperty {

    private final boolean value;

    public PropertyIsolateInternalQueries(boolean value) {
        this.value = value;
    }

    @Override
    public void applyTo(HikariConfig config) {
        config.setIsolateInternalQueries(value);
    }

}
