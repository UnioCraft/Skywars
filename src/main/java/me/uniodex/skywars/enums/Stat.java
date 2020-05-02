package me.uniodex.skywars.enums;

public enum Stat {
    wins(1), playedGames(2), kills(3), deaths(4), projectilesHit(5), projectilesLaunched(6), blocksPlaced(7), blocksBroken(8), itemsEnchanted(9), itemsCrafted(10), fishesCaught(11), playTime(12);

    public int id;

    Stat(int id) {
        this.id = id;
    }

    public static Stat getByName(String name) {
        for (Stat s : Stat.values()) if (s.name().equals(name)) return s;
        return null;
    }
}
