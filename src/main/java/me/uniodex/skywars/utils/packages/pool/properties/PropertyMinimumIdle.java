package me.uniodex.skywars.utils.packages.pool.properties;

import com.zaxxer.hikari.HikariConfig;

public class PropertyMinimumIdle implements HikariProperty {

    private final int value;

    public PropertyMinimumIdle(int value) {
        this.value = value;
    }

    @Override
    public void applyTo(HikariConfig config) {
        config.setMinimumIdle(value);
    }

}
