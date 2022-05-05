package com.villain;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.net.URL;
import java.util.Scanner;

public class NetworkManager {
    public static String makeNameRequest(String uuid) throws IOException {
        URL url = new URL("https://sessionserver.mojang.com/session/minecraft/profile/"+uuid); //generate a url with the UUID to point to the Mojang API
        HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setConnectTimeout(5000);
        con.setReadTimeout(5000);
        con.connect();

        int responseCode = con.getResponseCode();
        if(responseCode != 200) { //Stop if response code isn't 200.
            System.out.println("Could not grab name for " + uuid + ". Response code: " + responseCode);
            return "";
        }

        String line;
        Scanner scanner = new Scanner(url.openStream());

        while(scanner.hasNext()) {
            line = scanner.nextLine();
            if(line.contains("name")) { //Find the line containing the name.
                String[] tokens = line.split("\""); //Split by quotation
                if(tokens.length == 5) { //If the length is 5, the expected length
                    con.disconnect();
                    return tokens[3]; //then the name should be in index 3, and we return it.
                }
            }
        }
        throw new IOException(); //Throw an IO Exception if we haven't returned by now.
    }
}
