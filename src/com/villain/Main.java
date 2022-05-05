package com.villain;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Main {

    static boolean attemptConnections;
    static HashMap<String, Integer> pool = new HashMap<>();
    static ArrayList<PlayerData> playerData = new ArrayList<>();


    public static void main(String[] args) throws IOException {
        //Prompt the user for if we should contact mojang servers or not.
        enableNetworking();

        setUpPool();

        selectWinners();

        writeOutput();
    }

    public static void enableNetworking() {
        System.out.println("Would you like to connect to Mojang's API to grab player names?");
        System.out.println("I would confirm you have actual UUIDs before doing this. Otherwise it'll be ineffective.");
        System.out.println("Also do note that some rate limiting could maybe occur if this is run to many times!");
        System.out.println("Would you like to use this functionality? (Y/N)");
        Scanner scanner = new Scanner(System.in);
        //Loop until the state of connections is set properly
        boolean done = false;
        while (!done) {
            String input = scanner.nextLine();

            switch (input.toLowerCase()) {
                case "y":
                case "yes":
                case "ye":
                case "true":
                    attemptConnections = true;
                    done = true;
                    break;
                case "n":
                case "nah":
                case "no":
                case "false":
                    attemptConnections = false;
                    done = true;
                    break;
                default:
                    System.out.println("Would you like to use this functionality? (Y/N)");
                    break;
            }
        }
    }

    public static void setUpPool() throws IOException {
        //Open data.csv if present
        FileInputStream in;
        try {
            in = new FileInputStream("data.csv");
        } catch (FileNotFoundException e) {
            System.out.println("data.csv not found!");
            return;
        }

        //Prepare a line by line reader.
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));

        //Calibration phase. Read the first line of the csv to get bearings
        String line = null;
        try {
            line = reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Find the columns needed to work, and mark them as indices.
        int uuidIndex = -1;
        String[] tokens = line.split(","); //split first line into the columns
        int counter = 0;
        for (String content : tokens) {
            String truecontent = content.replaceAll("[-+^.:,\"]", ""); //some formats do weird stuff with extra characters.
            //"player" refers to a UUID or username based on example data. Code here could be edited to match the file, or the file could be edited to match the code.
            if (truecontent.equalsIgnoreCase("player")) {
                uuidIndex = counter; //if the player column is found, mark its index for future imports
                break;
            }
            counter++;
        }

        int normalIndex = -1; //the general process listed above for the players is followed for the normal count as well.
        counter = 0;
        for (String content : tokens) {
            String truecontent = content.replaceAll("[-+^.:,\"]", "");
            if (truecontent.equalsIgnoreCase("normal")) { //"normal" is again, entered here as a reference to example data.
                normalIndex = counter;
                break;
            }
            counter++;
        }

        int rareIndex = -1; //the general process listed above for the players is followed for the rare count as well.
        counter = 0;
        for (String content : tokens) {
            String truecontent = content.replaceAll("[-+^.:,\"]", "");
            if (truecontent.equalsIgnoreCase("rare")) { //"rare" is again, entered here as a reference to example data.
                rareIndex = counter;
                break;
            }
            counter++;
        }

        int unluckyIndex = -1; //the general process listed above for the players is followed for the unlucky count as well.
        counter = 0;
        for (String content : tokens) {
            String truecontent = content.replaceAll("[-+^.:,\"]", "");
            if (truecontent.equalsIgnoreCase("unlucky")) { //"unlucky" is again, entered here as a reference to example data.
                unluckyIndex = counter;
                break;
            }
            counter++;
        }

        if (uuidIndex == -1 || normalIndex == -1 || rareIndex == -1 || unluckyIndex == -1) { // if either index is missing, stop.
            System.out.println("CSV Index table does not contain necessary values! Need a player, normal, rare, and unlucky column");
            return;
        }

        while ((line = reader.readLine()) != null) { //Go through each line of the data.csv file, indexes in hand, and add each entry to the map.
            tokens = line.split(",");
            if (!tokens[0].equals("")) {
                String normalString = tokens[normalIndex].replaceAll("[-+^.:,\"]", "");
                String rareString = tokens[rareIndex].replaceAll("[-+^.:,\"]", "");
                String unluckyString = tokens[unluckyIndex].replaceAll("[-+^.:,\"]", "");
                String uuidString = tokens[uuidIndex].replaceAll("[-+^.:,\"]", "");

                int normalCount = Integer.parseInt(normalString);
                int rareCount = Integer.parseInt(rareString);
                int unluckyCount = Integer.parseInt(unluckyString);
                int eggCount = 0;
                if (pool.containsKey(uuidString)) {
                    eggCount = pool.get(uuidString);
                    pool.remove(uuidString);
                }

                eggCount += normalCount;
                eggCount += rareCount;
                eggCount += unluckyCount;

                pool.put(uuidString, eggCount);
            }
        }

        in.close(); //close input.
        reader.close();
    }


    public static void selectWinners() {
        for (Map.Entry<String, Integer> entry : pool.entrySet()) {
            playerData.add(new PlayerData(entry.getKey(), entry.getValue(), false));
        }

        playerData.sort(new PlayerComparator());
        ArrayList<PlayerData> topPlayerData = new ArrayList<>();
        //top 3 players
        topPlayerData.add(playerData.get(0)); topPlayerData.add(playerData.get(1)); topPlayerData.add(playerData.get(2));
        playerData.clear();
        playerData.addAll(topPlayerData);

        if(attemptConnections) {
            for (PlayerData data : playerData) {
                data.lateConnect();
            }
        }
    }

    public static void writeOutput() throws IOException {
        //Create a writer for output file.
        BufferedWriter out;
        try {
            out = new BufferedWriter(new FileWriter("output.txt"));
        } catch (FileNotFoundException e) {
            System.out.println("output.txt not found! Considering this file is to be made, how did you even do this?");
            return;
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("IO Exception on output");
            return;
        }

        int index = 0;
        for (PlayerData winner : playerData) { //For each winner, match the index of the winner to the index of the associated prize.
            String EggString = index+1 + ". " + winner.getPlayerName() + winner.getUUID() + " Eggs Collected: " + winner.getCount();
            out.write(EggString + "\n");
            System.out.println(EggString);
            index++;
        }

        out.close();
    }
}
