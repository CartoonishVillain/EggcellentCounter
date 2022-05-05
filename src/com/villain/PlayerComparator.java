package com.villain;

import java.util.Comparator;

public class PlayerComparator implements Comparator<PlayerData> {
    @Override
    public int compare(PlayerData o1, PlayerData o2) {
        return o2.count.compareTo(o1.count);
    }
}
