package in.glg.rummy.utils;

/**
 * Created by GridLogic on 31/8/17.
 */

import java.util.Comparator;

import in.glg.rummy.models.GamePlayer;

public class GamePlayerComparator implements Comparator<GamePlayer> {
    public int compare(GamePlayer p1, GamePlayer p2) {
        int face1 = Integer.valueOf(p1.getUser_id()).intValue();
        int face2 = Integer.valueOf(p2.getUser_id()).intValue();
        if (face1 == face2) {
            return 0;
        }
        if (face1 > face2) {
            return 1;
        }
        return -1;
    }
}