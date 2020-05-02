package me.uniodex.skywars.enums;

public enum ArenaState {
    WAITING("Waiting", true), STARTING("Starting", true), INGAME("In-game", false), HELL("Hell", false), ENDING("Ending", false), ROLLBACKING("Rollbacking", false), QUEUED("Queued", false), DISABLED("Disabled", false);

    private boolean available;
    private String customizedValue;

    ArenaState(String customizedValue, boolean available) {
        this.customizedValue = customizedValue;
        this.available = available;
    }

    public boolean isAvailable() {
        return available;
    }

    public String getStateName() {
        return customizedValue;
    }

    public void setStateName(String name) {
        this.customizedValue = name;
    }
}
