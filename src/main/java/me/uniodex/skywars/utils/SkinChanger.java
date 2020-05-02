package me.uniodex.skywars.utils;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.*;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Multimap;
import com.mojang.authlib.GameProfile;
import me.uniodex.skywars.Skywars;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("deprecation")
public class SkinChanger {
    protected static final String TEXTURES_KEY = "textures";
    private static Map<UUID, Nickname> nicks = new HashMap<UUID, Nickname>();
    private static ProtocolManager protocolManager;
    private static Method fillProfilePropertiesMethod;
    private static Object sessionServiceObject;
    private static LoadingCache<String, Collection<WrappedSignedProperty>> cachedTextures;

    static {
        protocolManager = ProtocolLibrary.getProtocolManager();
        SkinChanger.listenToPlayerInfo();
        cachedTextures = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.HOURS).build(new CacheLoader<String, Collection<WrappedSignedProperty>>() {

            public Collection<WrappedSignedProperty> load(String name) throws ReflectiveOperationException {
                WrappedGameProfile profile;
                Player player = Bukkit.getPlayer(name);
                if (player != null) {
                    profile = WrappedGameProfile.fromPlayer(player);
                } else {
                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(name);
                    profile = WrappedGameProfile.fromOfflinePlayer(offlinePlayer);
                    profile = SkinChanger.fillProfileProperties(profile);
                }
                return profile.getProperties().get("textures");
            }
        });
    }

    public static void nick(final Player player, final Nickname nick) {
        final GameProfile profile = nick.getProfile(player.getUniqueId());
        if (profile == null) {
            return;
        }
        final List<Player> show = despawnPlayer(player);
        nicks.remove(player.getUniqueId());
        SkinChanger.nicks.put(player.getUniqueId(), nick);
        spawnPlayer(player, nick.getColoredName());
        for (final Player online : show) {
            online.showPlayer(player);
        }
        sendRespawnPacket(player);
    }

    public static void unNick(final Player player) {
        if (!isNicked(player)) {
            throw new IllegalStateException("Player " + player.getName() + " is not nicked!");
        }
        final List<Player> show = despawnPlayer(player);
        SkinChanger.nicks.remove(player.getUniqueId());
        spawnPlayer(player, player.getName());
        for (final Player online : show) {
            online.showPlayer(player);
        }
        sendRespawnPacket(player);
    }

    private static void listenToPlayerInfo() {
        SkinChanger.protocolManager.addPacketListener(new PacketAdapter(Skywars.instance, PacketType.Play.Server.PLAYER_INFO) {
            public void onPacketSending(final PacketEvent e) {
                try {
                    if (!e.getPacket().getPlayerInfoAction().read(0).equals(EnumWrappers.PlayerInfoAction.ADD_PLAYER)) {
                        return;
                    }
                    final List<PlayerInfoData> playerInfoDataList = new ArrayList<PlayerInfoData>();
                    for (final PlayerInfoData data : e.getPacket().getPlayerInfoDataLists().read(0)) {
                        WrappedGameProfile profile = data.getProfile();
                        final UUID uuid = data.getProfile().getUUID();
                        if (!SkinChanger.isNicked(uuid)) {
                            playerInfoDataList.add(data);
                        } else {
                            final Nickname nick = SkinChanger.getNickname(uuid);
                            String tag;
                            tag = nick.getColoredName();
                            final String texturesName = nick.getName();
                            final Collection<WrappedSignedProperty> textures = SkinChanger.getTextures(texturesName);
                            if (tag == null) {
                                tag = profile.getName();
                            }
                            if (tag.length() > 16) {
                                tag = tag.substring(0, 15);
                            }
                            profile = profile.withName(tag);
                            final Multimap<String, WrappedSignedProperty> properties = profile.getProperties();
                            properties.removeAll("textures");
                            properties.putAll("textures", textures);
                            playerInfoDataList.add(new PlayerInfoData(profile, data.getPing(), data.getGameMode(), data.getDisplayName()));
                        }
                    }
                    e.getPacket().getPlayerInfoDataLists().write(0, playerInfoDataList);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    protected static Collection<WrappedSignedProperty> getTextures(final String name) {
        try {
            return SkinChanger.cachedTextures.get(name);
        } catch (ExecutionException exception) {
            System.err.println("Unable to load textures for " + name + "!");
            return null;
        }
    }

    private static WrappedGameProfile fillProfileProperties(final WrappedGameProfile profile) throws ReflectiveOperationException {
        if (SkinChanger.fillProfilePropertiesMethod == null) {
            final Server server = Bukkit.getServer();
            final Object minecraftServerObject = server.getClass().getDeclaredMethod("getServer", new Class[0]).invoke(server);
            for (final Method method : minecraftServerObject.getClass().getMethods()) {
                if (method.getReturnType().getSimpleName().equals("MinecraftSessionService")) {
                    SkinChanger.sessionServiceObject = method.invoke(minecraftServerObject);
                    break;
                }
            }
            for (final Method method : SkinChanger.sessionServiceObject.getClass().getMethods()) {
                if (method.getName().equals("fillProfileProperties")) {
                    SkinChanger.fillProfilePropertiesMethod = method;
                    break;
                }
            }
        }
        return WrappedGameProfile.fromHandle(SkinChanger.fillProfilePropertiesMethod.invoke(SkinChanger.sessionServiceObject, profile.getHandle(), true));
    }

    public static boolean isNicked(final Player player) {
        return isNicked(player.getUniqueId());
    }

    public static boolean isNicked(final UUID player) {
        return SkinChanger.nicks.containsKey(player);
    }

    public static Nickname getNickname(final UUID player) {
        return SkinChanger.nicks.get(player);
    }

    private static void sendRespawnPacket(final Player player) {
        final Location loc = player.getLocation().clone();
        final boolean allowFlight = player.getAllowFlight();
        final boolean flying = player.isFlying();
        final int slot = player.getInventory().getHeldItemSlot();
        final PacketContainer packet = SkinChanger.protocolManager.createPacket(PacketType.Play.Server.RESPAWN);
        packet.getIntegers().write(0, player.getWorld().getEnvironment().getId());
        packet.getDifficulties().write(0, EnumWrappers.Difficulty.valueOf(player.getWorld().getDifficulty().toString()));
        packet.getGameModes().write(0, EnumWrappers.NativeGameMode.fromBukkit(player.getGameMode()));
        packet.getWorldTypeModifier().write(0, player.getWorld().getWorldType());
        try {
            SkinChanger.protocolManager.sendServerPacket(player, packet);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        player.teleport(loc);
        player.setAllowFlight(allowFlight);
        player.setFlying(flying);
        player.updateInventory();
        player.getInventory().setHeldItemSlot(slot);
    }

    private static void spawnPlayer(final Player player, final String displayName) {
        final PacketContainer packet = SkinChanger.protocolManager.createPacket(PacketType.Play.Server.PLAYER_INFO);
        packet.getPlayerInfoAction().write(0, EnumWrappers.PlayerInfoAction.ADD_PLAYER);
        final WrappedGameProfile wrappedProfile = new WrappedGameProfile(player.getUniqueId(), player.getName());
        final EnumWrappers.NativeGameMode nativeGameMode = EnumWrappers.NativeGameMode.fromBukkit(player.getGameMode());
        packet.getPlayerInfoDataLists().write(0, Collections.singletonList(new PlayerInfoData(wrappedProfile, 20, nativeGameMode, WrappedChatComponent.fromText(displayName))));
        for (final Player online : Bukkit.getOnlinePlayers()) {
            if (!online.canSee(player)) {
                continue;
            }
            try {
                SkinChanger.protocolManager.sendServerPacket(online, packet);
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    private static List<Player> despawnPlayer(final Player player) {
        final List<Player> show = new ArrayList<Player>();
        final PacketContainer packet = SkinChanger.protocolManager.createPacket(PacketType.Play.Server.PLAYER_INFO);
        packet.getPlayerInfoAction().write(0, EnumWrappers.PlayerInfoAction.REMOVE_PLAYER);
        final WrappedGameProfile profile = new WrappedGameProfile(player.getUniqueId(), null);
        packet.getPlayerInfoDataLists().write(0, Collections.singletonList(new PlayerInfoData(profile, 0, EnumWrappers.NativeGameMode.SURVIVAL, WrappedChatComponent.fromText(""))));
        for (final Player online : Bukkit.getOnlinePlayers()) {
            if (online.canSee(player)) {
                show.add(online);
                online.hidePlayer(player);
            }
            try {
                SkinChanger.protocolManager.sendServerPacket(online, packet);
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return show;
    }
}
