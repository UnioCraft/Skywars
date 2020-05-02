package me.uniodex.skywars.arena;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.Iterator;
import java.util.Random;

//TODO Recode
public class Cuboid {

    public String worldName;
    private int x1, y1, z1;
    private int x2, y2, z2;

    public Cuboid(Location l1, Location l2) {
        worldName = l1.getWorld().getName();
        x1 = Math.min(l1.getBlockX(), l2.getBlockX());
        y1 = Math.min(l1.getBlockY(), l2.getBlockY());
        z1 = Math.min(l1.getBlockZ(), l2.getBlockZ());
        x2 = Math.max(l1.getBlockX(), l2.getBlockX());
        y2 = Math.max(l1.getBlockY(), l2.getBlockY());
        z2 = Math.max(l1.getBlockZ(), l2.getBlockZ());
    }

    public Cuboid(String format) {
        String[] split = format.split(", ");
        worldName = split[0];
        x1 = Integer.parseInt(split[1]);
        y1 = Integer.parseInt(split[2]);
        z1 = Integer.parseInt(split[3]);
        x2 = Integer.parseInt(split[4]);
        y2 = Integer.parseInt(split[5]);
        z2 = Integer.parseInt(split[6]);
    }

    public boolean contains(Location l) {
        return l.getWorld().getName().equals(worldName) && l.getBlockX() >= x1 && l.getBlockX() <= x2 && l.getBlockY() > y1 && l.getBlockY() < y2 && l.getBlockZ() >= z1 && l.getBlockZ() <= z2;
    }

    public int getLowerY() {
        return y1;
    }

    public int getSize() {
        return ((x2 - x1) + 1) * ((y2 - y1) + 1) * ((z2 - z1) + 1);
    }

    public Location getRandomLocation() {
        World w = Bukkit.getWorld(worldName);
        Random r = new Random();
        int rX = r.nextInt((x2 - x1) + 1), rY = r.nextInt((y2 - y1) + 1), rZ = r.nextInt((z2 - z1) + 1);
        Location l = new Location(w, x1 + rX, y1 + rY, z1 + rZ);
        return l.getBlock().getType().equals(Material.AIR) ? l : w.getHighestBlockAt(l).getLocation();
    }

    public Iterator<Block> iterator() {
        return new CuboidIterator(Bukkit.getWorld(worldName), this.x1, this.y1, this.z1, this.x2, this.y2, this.z2);
    }

    public String toString() {
        return worldName + ", " + x1 + ", " + y1 + ", " + z1 + ", " + x2 + ", " + y2 + ", " + z2;
    }

    public class CuboidIterator implements Iterator<Block> {
        private World w;
        private int baseX, baseY, baseZ;
        private int x, y, z;
        private int sizeX, sizeY, sizeZ;

        public CuboidIterator(World w, int x1, int y1, int z1, int x2, int y2, int z2) {
            this.w = w;
            this.baseX = x1;
            this.baseY = y1;
            this.baseZ = z1;
            this.sizeX = (x2 - x1) + 1;
            this.sizeY = (y2 - y1) + 1;
            this.sizeZ = (z2 - z1) + 1;
            this.x = this.y = this.z = 0;
        }

        public boolean hasNext() {
            return this.x < this.sizeX && this.y < this.sizeY && this.z < this.sizeZ;
        }

        public Block next() {
            Block b = this.w.getBlockAt(this.baseX + this.x, this.baseY + this.y, this.baseZ + this.z);
            update();
            return b;
        }

        public void update() {
            if (++x >= this.sizeX) {
                this.x = 0;
                if (++this.z >= this.sizeZ) {
                    this.z = 0;
                    ++this.y;
                }
            }
        }

        public void remove() {
        }
    }

}
