package me.uniodex.skywars.managers;

import com.boydti.fawe.bukkit.wrapper.AsyncWorld;
import com.boydti.fawe.util.TaskManager;
import me.uniodex.skywars.Skywars;
import me.uniodex.skywars.arena.Arena;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;

import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.logging.Level;

public class WorldManager {

    private Skywars plugin;

    public WorldManager(Skywars plugin) {
        this.plugin = plugin;
    }

    public void rollbackWorld(String worldName, Arena worldsArena) {
        File serverDir = Bukkit.getWorldContainer();
        File backupWorldDir = new File(plugin.getDataFolder().getAbsolutePath() + File.separator + "arenas" + File.separator + worldName + File.separator + "world");
        File arenaDir = new File(plugin.getDataFolder().getAbsolutePath() + File.separator + "arenas" + File.separator + worldName);
        File worldDir = new File(serverDir.getAbsolutePath() + File.separator + worldName);
        World world = Bukkit.getWorld(worldName);

        if (world != null) {
            if (world.getPlayers().size() > 0) {
                for (Player wp : world.getPlayers()) {
                    wp.teleport(plugin.lobbyLocation);
                }
            }
            if (!Bukkit.getServer().unloadWorld(world, false)) {
                Bukkit.getLogger().log(Level.SEVERE, "Dünya yenilenirken bir sorun oluştu! Hata Kodu: A1");
            }
        }

        try {
            FileUtils.deleteDirectory(worldDir);
            FileUtils.copyDirectory(backupWorldDir, new File(arenaDir.getAbsolutePath() + File.separator + worldName));
            FileUtils.moveDirectoryToDirectory(new File(arenaDir.getAbsolutePath() + File.separator + worldName), serverDir, false);
            if (Bukkit.getWorld(worldName) == null) {
                TaskManager.IMP.async(new Runnable() {
                    @Override
                    public void run() {
                        AsyncWorld world = AsyncWorld.create(new WorldCreator(worldName).generator(new ChunkGenerator() {
                            public byte[][] generateBlockSections(World world, Random random, int chunkX, int chunkZ, ChunkGenerator.BiomeGrid biomeGrid) {
                                return new byte[world.getMaxHeight() / 16][];
                            }
                        }));
                        world.commit();
                        Bukkit.getScheduler().runTask(plugin, new Runnable() {
                            @Override
                            public void run() {
                                worldsArena.initPart2();
                            }
                        });
                    }
                });
            } else {
                Bukkit.getLogger().log(Level.SEVERE, "Dünya sıfırlanırken bir sorun oluştu! Hata Kodu: A2");
            }
        } catch (IOException e) {
            e.printStackTrace();
            Bukkit.getLogger().log(Level.SEVERE, "Dünya sıfırlanırken bir sorun oluştu! Hata Kodu: A3");
        }
    }
}
