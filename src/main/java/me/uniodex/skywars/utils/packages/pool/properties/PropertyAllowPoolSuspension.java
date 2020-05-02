package me.uniodex.skywars.utils.packages.pool.properties;

import com.zaxxer.hikari.HikariConfig;

public class PropertyAllowPoolSuspension implements HikariProperty {

    private final boolean value;

    public PropertyAllowPoolSuspension(boolean value) {
        this.value = value;
    }

    @Override
    public void applyTo(HikariConfig config) {
        config.setAllowPoolSuspension(value);
    }

}
