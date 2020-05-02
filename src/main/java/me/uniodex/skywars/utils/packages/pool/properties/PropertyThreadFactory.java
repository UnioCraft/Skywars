package me.uniodex.skywars.utils.packages.pool.properties;

import com.zaxxer.hikari.HikariConfig;

import java.util.concurrent.ThreadFactory;

public class PropertyThreadFactory implements HikariProperty {

    private final ThreadFactory value;

    public PropertyThreadFactory(ThreadFactory value) {
        this.value = value;
    }

    @Override
    public void applyTo(HikariConfig config) {
        config.setThreadFactory(value);
    }

}
