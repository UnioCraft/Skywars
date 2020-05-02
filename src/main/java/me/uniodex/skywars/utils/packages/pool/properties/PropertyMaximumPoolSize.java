package me.uniodex.skywars.utils.packages.pool.properties;

import com.zaxxer.hikari.HikariConfig;

public class PropertyMaximumPoolSize implements HikariProperty {

    private final int value;

    public PropertyMaximumPoolSize(int value) {
        this.value = value;
    }

    @Override
    public void applyTo(HikariConfig config) {
        config.setMaximumPoolSize(value);
    }

}
