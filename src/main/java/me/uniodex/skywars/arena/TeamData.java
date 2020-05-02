package me.uniodex.skywars.arena;

import me.uniodex.skywars.player.SWOnlinePlayer;
import org.bukkit.Location;

import java.util.HashMap;

public class TeamData {
    private Location spawnpoint;
    private HashMap<Location, Integer> largeCage;
    private HashMap<Location, Integer> smallCage;
    private boolean isReserved = false;
    private SWOnlinePlayer teamLeader;

    public TeamData(Location spawnpoint) {
        this.spawnpoint = spawnpoint;
        loadSmallCage();
        loadLargeCage();
        this.isReserved = false;
        this.teamLeader = null;
    }

    public TeamData(Location spawnpoint, SWOnlinePlayer teamLeader) {
        this.spawnpoint = spawnpoint;
        loadSmallCage();
        loadLargeCage();
        this.isReserved = true;
        this.teamLeader = teamLeader;
    }

    public SWOnlinePlayer getTeamLeader() {
        return this.teamLeader;
    }

    public void setTeamLeader(SWOnlinePlayer player) {
        this.teamLeader = player;
    }

    public Boolean isTeamReserved() {
        return this.isReserved;
    }

    public void setTeamReserved(boolean reserved) {
        this.isReserved = reserved;
    }

    public HashMap<Location, Integer> getSmallCage() {
        return this.smallCage;
    }

    public HashMap<Location, Integer> getLargeCage() {
        return this.largeCage;
    }

    public Location getSpawnpoint() {
        return spawnpoint;
    }

    private void loadSmallCage() {
        /*
         * 0 = ceiling
         * 1 = ceilingBorder
         * 2 = higherMiddle
         * 3 = higherMiddleBorder
         * 4 = middle
         * 5 = middleBorder
         * 6 = lowerMiddle
         * 7 = lowerMiddleBorder
         * 8 = floorBorder
         * 9 = floor
         */
        this.smallCage = new HashMap<Location, Integer>();
        final Location spawnpoint = this.spawnpoint.clone();
        // ceiling
        this.smallCage.put(spawnpoint.clone().add(0.0, +3.0, 0.0), 0);
        // ceilingBorder
        this.smallCage.put(spawnpoint.clone().add(+1.0, +3.0, +1.0), 1);
        this.smallCage.put(spawnpoint.clone().add(0.0, +3.0, +1.0), 1);
        this.smallCage.put(spawnpoint.clone().add(-1.0, +3.0, +1.0), 1);
        this.smallCage.put(spawnpoint.clone().add(+1.0, +3.0, 0.0), 1);
        this.smallCage.put(spawnpoint.clone().add(-1.0, +3.0, 0.0), 1);
        this.smallCage.put(spawnpoint.clone().add(+1.0, +3.0, -1.0), 1);
        this.smallCage.put(spawnpoint.clone().add(0.0, +3.0, -1.0), 1);
        this.smallCage.put(spawnpoint.clone().add(-1.0, +3.0, -1.0), 1);
        // higherMiddle
        this.smallCage.put(spawnpoint.clone().add(0.0, +2.0, +1.0), 2);
        this.smallCage.put(spawnpoint.clone().add(+1.0, +2.0, 0.0), 2);
        this.smallCage.put(spawnpoint.clone().add(-1.0, +2.0, 0.0), 2);
        this.smallCage.put(spawnpoint.clone().add(0.0, +2.0, -1.0), 2);
        //higherMiddleBorder
        this.smallCage.put(spawnpoint.clone().add(+1.0, +2.0, +1.0), 3);
        this.smallCage.put(spawnpoint.clone().add(-1.0, +2.0, +1.0), 3);
        this.smallCage.put(spawnpoint.clone().add(+1.0, +2.0, -1.0), 3);
        this.smallCage.put(spawnpoint.clone().add(-1.0, +2.0, -1.0), 3);
        // Middle
        this.smallCage.put(spawnpoint.clone().add(0.0, +1.0, +1.0), 4);
        this.smallCage.put(spawnpoint.clone().add(+1.0, +1.0, 0.0), 4);
        this.smallCage.put(spawnpoint.clone().add(-1.0, +1.0, 0.0), 4);
        this.smallCage.put(spawnpoint.clone().add(0.0, +1.0, -1.0), 4);
        // middleBorder
        this.smallCage.put(spawnpoint.clone().add(+1.0, +1.0, +1.0), 5);
        this.smallCage.put(spawnpoint.clone().add(-1.0, +1.0, +1.0), 5);
        this.smallCage.put(spawnpoint.clone().add(+1.0, +1.0, -1.0), 5);
        this.smallCage.put(spawnpoint.clone().add(-1.0, +1.0, -1.0), 5);
        // lowerMiddle
        this.smallCage.put(spawnpoint.clone().add(0.0, 0.0, +1.0), 6);
        this.smallCage.put(spawnpoint.clone().add(+1.0, 0.0, 0.0), 6);
        this.smallCage.put(spawnpoint.clone().add(-1.0, 0.0, 0.0), 6);
        this.smallCage.put(spawnpoint.clone().add(0.0, 0.0, -1.0), 6);
        // lowerMiddleBorder
        this.smallCage.put(spawnpoint.clone().add(+1.0, 0.0, +1.0), 7);
        this.smallCage.put(spawnpoint.clone().add(-1.0, 0.0, +1.0), 7);
        this.smallCage.put(spawnpoint.clone().add(+1.0, 0.0, -1.0), 7);
        this.smallCage.put(spawnpoint.clone().add(-1.0, 0.0, -1.0), 7);
        // floorBorder
        this.smallCage.put(spawnpoint.clone().add(+1.0, -1.0, +1.0), 8);
        this.smallCage.put(spawnpoint.clone().add(0.0, -1.0, +1.0), 8);
        this.smallCage.put(spawnpoint.clone().add(-1.0, -1.0, +1.0), 8);
        this.smallCage.put(spawnpoint.clone().add(+1.0, -1.0, 0.0), 8);
        this.smallCage.put(spawnpoint.clone().add(-1.0, -1.0, 0.0), 8);
        this.smallCage.put(spawnpoint.clone().add(+1.0, -1.0, -1.0), 8);
        this.smallCage.put(spawnpoint.clone().add(0.0, -1.0, -1.0), 8);
        this.smallCage.put(spawnpoint.clone().add(-1.0, -1.0, -1.0), 8);
        // floor
        this.smallCage.put(spawnpoint.clone().add(0.0, -1.0, 0.0), 9);
    }

    private void loadLargeCage() {
        /*
         * 0 = ceiling
         * 1 = ceilingBorder
         * 2 = higherMiddle
         * 3 = higherMiddleBorder
         * 4 = middle
         * 5 = middleBorder
         * 6 = lowerMiddle
         * 7 = lowerMiddleBorder
         * 8 = floorBorder
         * 9 = floor
         */
        this.largeCage = new HashMap<Location, Integer>();
        final Location spawnpoint = this.spawnpoint.clone();
        // ceiling
        this.largeCage.put(spawnpoint.clone().add(0.0, +3.0, 0.0), 0);
        this.largeCage.put(spawnpoint.clone().add(0.0, +3.0, +1.0), 0);
        this.largeCage.put(spawnpoint.clone().add(0.0, +3.0, -1.0), 0);
        this.largeCage.put(spawnpoint.clone().add(-1.0, +3.0, 0.0), 0);
        this.largeCage.put(spawnpoint.clone().add(-1.0, +3.0, -1.0), 0);
        this.largeCage.put(spawnpoint.clone().add(-1.0, +3.0, +1.0), 0);
        this.largeCage.put(spawnpoint.clone().add(+1.0, +3.0, +1.0), 0);
        this.largeCage.put(spawnpoint.clone().add(+1.0, +3.0, -1.0), 0);
        this.largeCage.put(spawnpoint.clone().add(+1.0, +3.0, 0.0), 0);
        // ceilingBorder
        this.largeCage.put(spawnpoint.clone().add(2.0, +3.0, 2.0), 1);
        this.largeCage.put(spawnpoint.clone().add(2.0, +3.0, 1.0), 1);
        this.largeCage.put(spawnpoint.clone().add(2.0, +3.0, 0.0), 1);
        this.largeCage.put(spawnpoint.clone().add(2.0, +3.0, -1.0), 1);
        this.largeCage.put(spawnpoint.clone().add(2.0, +3.0, -2.0), 1);
        this.largeCage.put(spawnpoint.clone().add(-2.0, +3.0, 2.0), 1);
        this.largeCage.put(spawnpoint.clone().add(-2.0, +3.0, 1.0), 1);
        this.largeCage.put(spawnpoint.clone().add(-2.0, +3.0, 0.0), 1);
        this.largeCage.put(spawnpoint.clone().add(-2.0, +3.0, -1.0), 1);
        this.largeCage.put(spawnpoint.clone().add(-2.0, +3.0, -2.0), 1);
        this.largeCage.put(spawnpoint.clone().add(+1.0, +3.0, +2.0), 1);
        this.largeCage.put(spawnpoint.clone().add(+0.0, +3.0, +2.0), 1);
        this.largeCage.put(spawnpoint.clone().add(-1.0, +3.0, +2.0), 1);
        this.largeCage.put(spawnpoint.clone().add(+1.0, +3.0, -2.0), 1);
        this.largeCage.put(spawnpoint.clone().add(+0.0, +3.0, -2.0), 1);
        this.largeCage.put(spawnpoint.clone().add(-1.0, +3.0, -2.0), 1);
        // higherMiddle
        this.largeCage.put(spawnpoint.clone().add(2.0, +2.0, 2.0), 2);
        this.largeCage.put(spawnpoint.clone().add(2.0, +2.0, 1.0), 2);
        this.largeCage.put(spawnpoint.clone().add(2.0, +2.0, 0.0), 2);
        this.largeCage.put(spawnpoint.clone().add(2.0, +2.0, -1.0), 2);
        this.largeCage.put(spawnpoint.clone().add(2.0, +2.0, -2.0), 2);
        this.largeCage.put(spawnpoint.clone().add(-2.0, +2.0, +2.0), 2);
        this.largeCage.put(spawnpoint.clone().add(-2.0, +2.0, +1.0), 2);
        this.largeCage.put(spawnpoint.clone().add(-2.0, +2.0, +0.0), 2);
        this.largeCage.put(spawnpoint.clone().add(-2.0, +2.0, -1.0), 2);
        this.largeCage.put(spawnpoint.clone().add(-2.0, +2.0, -2.0), 2);
        this.largeCage.put(spawnpoint.clone().add(+1.0, +2.0, +2.0), 2);
        this.largeCage.put(spawnpoint.clone().add(+0.0, +2.0, +2.0), 2);
        this.largeCage.put(spawnpoint.clone().add(-1.0, +2.0, +2.0), 2);
        this.largeCage.put(spawnpoint.clone().add(+1.0, +2.0, -2.0), 2);
        this.largeCage.put(spawnpoint.clone().add(+0.0, +2.0, -2.0), 2);
        this.largeCage.put(spawnpoint.clone().add(-1.0, +2.0, -2.0), 2);
        //higherMiddleBorder
        //3
        // Middle
        this.largeCage.put(spawnpoint.clone().add(2.0, +1.0, +2.0), 4);
        this.largeCage.put(spawnpoint.clone().add(2.0, +1.0, +1.0), 4);
        this.largeCage.put(spawnpoint.clone().add(2.0, +1.0, +0.0), 4);
        this.largeCage.put(spawnpoint.clone().add(2.0, +1.0, -1.0), 4);
        this.largeCage.put(spawnpoint.clone().add(2.0, +1.0, -2.0), 4);
        this.largeCage.put(spawnpoint.clone().add(-2.0, +1.0, +2.0), 4);
        this.largeCage.put(spawnpoint.clone().add(-2.0, +1.0, +1.0), 4);
        this.largeCage.put(spawnpoint.clone().add(-2.0, +1.0, +0.0), 4);
        this.largeCage.put(spawnpoint.clone().add(-2.0, +1.0, -1.0), 4);
        this.largeCage.put(spawnpoint.clone().add(-2.0, +1.0, -2.0), 4);
        this.largeCage.put(spawnpoint.clone().add(+1.0, +1.0, +2.0), 4);
        this.largeCage.put(spawnpoint.clone().add(+0.0, +1.0, +2.0), 4);
        this.largeCage.put(spawnpoint.clone().add(-1.0, +1.0, +2.0), 4);
        this.largeCage.put(spawnpoint.clone().add(+1.0, +1.0, -2.0), 4);
        this.largeCage.put(spawnpoint.clone().add(+0.0, +1.0, -2.0), 4);
        this.largeCage.put(spawnpoint.clone().add(-1.0, +1.0, -2.0), 4);
        // middleBorder
        //5
        // lowerMiddle
        this.largeCage.put(spawnpoint.clone().add(2.0, 0.0, +2.0), 6);
        this.largeCage.put(spawnpoint.clone().add(2.0, 0.0, +1.0), 6);
        this.largeCage.put(spawnpoint.clone().add(2.0, 0.0, +0.0), 6);
        this.largeCage.put(spawnpoint.clone().add(2.0, 0.0, -1.0), 6);
        this.largeCage.put(spawnpoint.clone().add(2.0, 0.0, -2.0), 6);
        this.largeCage.put(spawnpoint.clone().add(-2.0, 0.0, +2.0), 6);
        this.largeCage.put(spawnpoint.clone().add(-2.0, 0.0, +1.0), 6);
        this.largeCage.put(spawnpoint.clone().add(-2.0, 0.0, +0.0), 6);
        this.largeCage.put(spawnpoint.clone().add(-2.0, 0.0, -1.0), 6);
        this.largeCage.put(spawnpoint.clone().add(-2.0, 0.0, -2.0), 6);
        this.largeCage.put(spawnpoint.clone().add(+1.0, 0.0, +2.0), 6);
        this.largeCage.put(spawnpoint.clone().add(0.0, 0.0, +2.0), 6);
        this.largeCage.put(spawnpoint.clone().add(-1.0, 0.0, +2.0), 6);
        this.largeCage.put(spawnpoint.clone().add(+1.0, 0.0, -2.0), 6);
        this.largeCage.put(spawnpoint.clone().add(0.0, 0.0, -2.0), 6);
        this.largeCage.put(spawnpoint.clone().add(-1.0, 0.0, -2.0), 6);
        // lowerMiddleBorder
        // 7
        // floorBorder
        this.largeCage.put(spawnpoint.clone().add(2.0, -1.0, 2.0), 8);
        this.largeCage.put(spawnpoint.clone().add(2.0, -1.0, 1.0), 8);
        this.largeCage.put(spawnpoint.clone().add(2.0, -1.0, 0.0), 8);
        this.largeCage.put(spawnpoint.clone().add(2.0, -1.0, -1.0), 8);
        this.largeCage.put(spawnpoint.clone().add(2.0, -1.0, -2.0), 8);
        this.largeCage.put(spawnpoint.clone().add(-2.0, -1.0, 2.0), 8);
        this.largeCage.put(spawnpoint.clone().add(-2.0, -1.0, 1.0), 8);
        this.largeCage.put(spawnpoint.clone().add(-2.0, -1.0, 0.0), 8);
        this.largeCage.put(spawnpoint.clone().add(-2.0, -1.0, -1.0), 8);
        this.largeCage.put(spawnpoint.clone().add(-2.0, -1.0, -2.0), 8);
        this.largeCage.put(spawnpoint.clone().add(+1.0, -1.0, +2.0), 8);
        this.largeCage.put(spawnpoint.clone().add(+0.0, -1.0, +2.0), 8);
        this.largeCage.put(spawnpoint.clone().add(-1.0, -1.0, +2.0), 8);
        this.largeCage.put(spawnpoint.clone().add(+1.0, -1.0, -2.0), 8);
        this.largeCage.put(spawnpoint.clone().add(+0.0, -1.0, -2.0), 8);
        this.largeCage.put(spawnpoint.clone().add(-1.0, -1.0, -2.0), 8);
        // floor
        this.largeCage.put(spawnpoint.clone().add(0.0, -1.0, 0.0), 9);
        this.largeCage.put(spawnpoint.clone().add(0.0, -1.0, +1.0), 9);
        this.largeCage.put(spawnpoint.clone().add(0.0, -1.0, -1.0), 9);
        this.largeCage.put(spawnpoint.clone().add(-1.0, -1.0, 0.0), 9);
        this.largeCage.put(spawnpoint.clone().add(-1.0, -1.0, -1.0), 9);
        this.largeCage.put(spawnpoint.clone().add(-1.0, -1.0, +1.0), 9);
        this.largeCage.put(spawnpoint.clone().add(+1.0, -1.0, +1.0), 9);
        this.largeCage.put(spawnpoint.clone().add(+1.0, -1.0, -1.0), 9);
        this.largeCage.put(spawnpoint.clone().add(+1.0, -1.0, 0.0), 9);
    }
}
