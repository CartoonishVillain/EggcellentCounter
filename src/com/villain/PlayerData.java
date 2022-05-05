package com.villain;

import java.io.IOException;

public class PlayerData {
    String UUID;
    Integer count;
    String playerName;

    public PlayerData(String string, int count, boolean connection) {
        UUID = string;
        this.count = count;
        if (connection) { //If connections are enabled, attempt to get the username from the UUID and set it as playername. Otherwise, leave it as an empty string.
            try {
                playerName = NetworkManager.makeNameRequest(UUID);
                playerName += " ";
            } catch (IOException e) {
                System.out.println("Could not get player name for " + UUID + " due to an IOException!");
                playerName = "";
            }
        } else {
            playerName = "";
        }
    }

    public void lateConnect() {
        try {
            playerName = NetworkManager.makeNameRequest(UUID);
            playerName += " ";
        } catch (IOException e) {
            System.out.println("Could not get player name for " + UUID + " due to an IOException!");
            playerName = "";
        }
    }

    public float getCount() {
        return count;
    }

    public String getUUID() {
        return UUID;
    }

    public String getPlayerName() {
        return playerName;
    }

}
