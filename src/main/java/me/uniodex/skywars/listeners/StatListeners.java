package me.uniodex.skywars.listeners;

import me.uniodex.skywars.Skywars;
import me.uniodex.skywars.arena.Arena;
import me.uniodex.skywars.player.SWOfflinePlayer;
import me.uniodex.skywars.player.SWOnlinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.projectiles.ProjectileSource;

public class StatListeners implements Listener {

    private Skywars plugin;

    public StatListeners(Skywars plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockPlace(BlockPlaceEvent e) {
        if (e.isCancelled()) return;

        Player p = e.getPlayer();
        if (plugin.playerManager.editMode.contains(p.getName())) {
            return;
        }
        SWOnlinePlayer swPlayer = plugin.playerManager.getSWOnlinePlayer(p.getName());
        Arena arena = swPlayer.getArena();

        if (!swPlayer.canBuild()) {
            return;
        }

        if (arena.getCuboid().contains(e.getBlock().getLocation())) {
            plugin.playerManager.giveStat(swPlayer, "blocksPlaced", 1);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockBreak(BlockBreakEvent e) {
        if (e.isCancelled()) return;

        Player p = e.getPlayer();
        if (plugin.playerManager.editMode.contains(p.getName())) {
            return;
        }
        SWOnlinePlayer swPlayer = plugin.playerManager.getSWOnlinePlayer(p.getName());

        if (!swPlayer.canBuild()) {
            return;
        }

        plugin.playerManager.giveStat(swPlayer, "blocksBroken", 1);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDeath(PlayerDeathEvent e) {
        Player p = e.getEntity();
        if (plugin.playerManager.editMode.contains(p.getName())) {
            return;
        }
        SWOnlinePlayer swPlayer = plugin.playerManager.getSWOnlinePlayer(p.getName());

        if (swPlayer.canBuild()) {
            plugin.playerManager.giveStat(swPlayer, "deaths", 1);

            SWOfflinePlayer killer = null;

            if (p.getKiller() != null) {
                killer = plugin.playerManager.getSWPlayer(p.getKiller().getName());
            }

            if (killer == null && !swPlayer.getLastAttacker().isEmpty() && ((System.currentTimeMillis() - swPlayer.getLastAttackedTime()) / 1000) <= 15) {
                killer = plugin.playerManager.getSWPlayer(swPlayer.getLastAttacker());
            }

            if (killer != null && !killer.getName().equals(p.getName())) {
                awardKiller(killer, swPlayer.getSWOfflinePlayer());
            }
        }
    }

    private void awardKiller(SWOfflinePlayer killer, SWOfflinePlayer victim) {
        // Oyuncu çevrim içi ise
        if (killer.getSWOnlinePlayer() != null) {
            plugin.playerManager.giveStat(killer.getSWOnlinePlayer(), "kills", 1);
            plugin.playerManager.giveCoin(killer.getSWOnlinePlayer(), killer.getCoinsPerKill());

            Player p = killer.getPlayer().getPlayer();
            p.sendMessage(this.plugin.customization.messages.get("Player-Kill").replace("%target%", victim.getName()).replace("%coins%", String.valueOf(killer.getCoinsPerKill())));
            p.playSound(p.getLocation(), this.plugin.NOTE_PLING, 1.0f, 1.0f);
        }
        // Oyuncu çevrim içi değil ise
        else {
            plugin.playerManager.giveStat(killer, "kills", 1);
            plugin.playerManager.giveCoin(killer, killer.getCoinsPerKill());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEnchantItemEvent(EnchantItemEvent e) {
        if (e.isCancelled()) return;

        Player p = e.getEnchanter();
        if (plugin.playerManager.editMode.contains(p.getName())) {
            return;
        }
        SWOnlinePlayer swPlayer = plugin.playerManager.getSWOnlinePlayer(p.getName());

        if (!swPlayer.canBuild()) {
            return;
        }

        plugin.playerManager.giveStat(swPlayer, "itemsEnchanted", 1);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCraftItem(CraftItemEvent e) {
        if (e.isCancelled()) return;

        Player p = (Player) e.getWhoClicked();
        if (plugin.playerManager.editMode.contains(p.getName())) {
            return;
        }
        SWOnlinePlayer swPlayer = plugin.playerManager.getSWOnlinePlayer(p.getName());

        if (!swPlayer.canBuild()) {
            return;
        }

        plugin.playerManager.giveStat(swPlayer, "itemsCrafted", 1);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerFish(PlayerFishEvent e) {
        if (e.isCancelled()) return;

        Player p = e.getPlayer();
        if (plugin.playerManager.editMode.contains(p.getName())) {
            return;
        }
        SWOnlinePlayer swPlayer = plugin.playerManager.getSWOnlinePlayer(p.getName());

        if (!swPlayer.canBuild()) {
            return;
        }

        if (e.getState().equals(PlayerFishEvent.State.CAUGHT_FISH)) {
            plugin.playerManager.giveStat(swPlayer, "fishesCaught", 1);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onProjectileLaunch(ProjectileLaunchEvent e) {
        if (e.isCancelled()) return;

        Projectile pr = e.getEntity();
        if (!(pr.getShooter() instanceof Player)) {
            return;
        }

        Player p = (Player) pr.getShooter();
        if (plugin.playerManager.editMode.contains(p.getName())) {
            return;
        }
        SWOnlinePlayer swPlayer = plugin.playerManager.getSWOnlinePlayer(p.getName());

        if (!swPlayer.canBuild()) {
            return;
        }

        plugin.playerManager.giveStat(swPlayer, "projectilesLaunched", 1);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        if (e.isCancelled()) return;

        Entity entityDamager = e.getDamager();
        Entity entityVictim = e.getEntity();

        if (entityDamager instanceof Projectile && entityVictim instanceof Player) {
            ProjectileSource shooter = ((Projectile) entityDamager).getShooter();
            if (shooter instanceof Player) {
                SWOnlinePlayer swDamager = plugin.playerManager.getSWOnlinePlayer(((Player) shooter).getName());
                SWOnlinePlayer swVictim = plugin.playerManager.getSWOnlinePlayer(entityVictim.getName());

                if (swDamager.canBuild() && swVictim.canBuild()) {
                    plugin.playerManager.giveStat(swDamager, "projectilesHit", 1);
                }
            }
        }
    }
}