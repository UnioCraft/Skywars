package me.uniodex.skywars.party;

import de.simonsator.partyandfriends.spigot.api.party.PlayerParty;
import me.uniodex.skywars.Skywars;

import java.util.ArrayList;

public class SWPartyManager {

    @SuppressWarnings("unused")
    private Skywars plugin;

    private ArrayList<PlayerParty> parties = new ArrayList<PlayerParty>();

    public SWPartyManager(Skywars plugin) {
        this.plugin = plugin;
    }

    public ArrayList<PlayerParty> getParties() {
        return parties;
    }
}