package me.uniodex.skywars.utils;

import com.mojang.authlib.GameProfile;
import org.bukkit.ChatColor;

import java.util.UUID;

public class Nickname {
    private final UUID uuid;
    private final ChatColor color;
    private final String name;
    private GameProfile profile;

    public Nickname(UUID uuid, String name) {
        this(uuid, ChatColor.GOLD, name);
    }

    public Nickname(UUID uuid, ChatColor color, String name) {
        this.uuid = uuid;
        this.color = color;
        this.profile = getGameProfile(uuid, uuid, name);
        this.name = name;
    }

    public static GameProfile getGameProfile(UUID player) {
        return getGameProfile(player, player, UUIDFetcher.getName(player));
    }

    public static GameProfile getGameProfile(UUID player, UUID newUuid) {
        return getGameProfile(player, newUuid, null);
    }

    public static GameProfile getGameProfile(UUID player, UUID newUuid, String newName) {
        GameProfile gp = new GameProfile(newUuid, newName);
        return gp;
    }

    public static Nickname fromString(String string) {
        String[] parts = string.split(":");

        return new Nickname(UUID.fromString(parts[0]), ChatColor.getByChar(parts[1]), UUIDFetcher.getName(UUID.fromString(parts[0])));
    }

    public UUID getUuid() {
        return this.uuid;
    }

    public ChatColor getColor() {
        return this.color;
    }

    public boolean hasProfile(UUID player) {
        return (this.profile != null) && (this.profile.getId() == player);
    }

    public GameProfile getProfile(UUID player) {
        if ((this.profile == null) || (this.profile.getId() != player)) {
            this.profile = getGameProfile(this.uuid, player);
        }
        return this.profile;
    }

    public String getName() {
        return this.name;
    }

    public String getColoredName() {
        return this.color + this.name;
    }

    public String toString() {
        return this.uuid.toString() + ":" + this.color.getChar();
    }

    public boolean equals(Object object) {
        if ((object instanceof Nickname)) {
            Nickname other = (Nickname) object;
            return other.uuid.equals(this.uuid);
        }
        return false;
    }
}
