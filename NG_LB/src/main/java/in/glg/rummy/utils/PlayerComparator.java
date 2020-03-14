package in.glg.rummy.utils;

/**
 * Created by GridLogic on 31/8/17.
 */

import java.util.Comparator;

import in.glg.rummy.models.GamePlayer;

public class PlayerComparator implements Comparator<GamePlayer> {
    public int compare(GamePlayer p1, GamePlayer p2) {
        int place1 = Integer.valueOf(p1.getPlace()).intValue();
        int place2 = Integer.valueOf(p2.getPlace()).intValue();
        if (place1 == place2) {
            return 0;
        }
        if (place1 > place2) {
            return 1;
        }
        return -1;
    }
}