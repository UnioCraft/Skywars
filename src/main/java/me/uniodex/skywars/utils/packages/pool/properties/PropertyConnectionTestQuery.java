package me.uniodex.skywars.utils.packages.pool.properties;

import com.zaxxer.hikari.HikariConfig;

public class PropertyConnectionTestQuery implements HikariProperty {

    private final String value;

    public PropertyConnectionTestQuery(String value) {
        this.value = value;
    }

    @Override
    public void applyTo(HikariConfig config) {
        config.setConnectionTestQuery(value);
    }

}
