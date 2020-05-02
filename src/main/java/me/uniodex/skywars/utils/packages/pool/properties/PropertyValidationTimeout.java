package me.uniodex.skywars.utils.packages.pool.properties;

import com.zaxxer.hikari.HikariConfig;

public class PropertyValidationTimeout implements HikariProperty {

    private final long value;

    public PropertyValidationTimeout(long value) {
        this.value = value;
    }

    @Override
    public void applyTo(HikariConfig config) {
        config.setValidationTimeout(value);
    }

}
