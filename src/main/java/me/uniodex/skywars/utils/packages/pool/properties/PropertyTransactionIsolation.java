package me.uniodex.skywars.utils.packages.pool.properties;

import com.zaxxer.hikari.HikariConfig;

public class PropertyTransactionIsolation implements HikariProperty {

    private final String value;

    public PropertyTransactionIsolation(String value) {
        this.value = value;
    }

    @Override
    public void applyTo(HikariConfig config) {
        config.setTransactionIsolation(value);
    }

}
