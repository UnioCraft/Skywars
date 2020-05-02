package me.uniodex.skywars.utils.packages.pool.properties;

import com.zaxxer.hikari.HikariConfig;

public class PropertyRegisterMbeans implements HikariProperty {

    private final boolean value;

    public PropertyRegisterMbeans(boolean value) {
        this.value = value;
    }

    @Override
    public void applyTo(HikariConfig config) {
        config.setRegisterMbeans(value);
    }

}
