package me.uniodex.skywars.utils.packages.pool.properties;

import com.zaxxer.hikari.HikariConfig;

public class PropertyLeakDetectionThreshold implements HikariProperty {

    private final long value;

    public PropertyLeakDetectionThreshold(long value) {
        this.value = value;
    }

    @Override
    public void applyTo(HikariConfig config) {
        config.setLeakDetectionThreshold(value);
    }

}
