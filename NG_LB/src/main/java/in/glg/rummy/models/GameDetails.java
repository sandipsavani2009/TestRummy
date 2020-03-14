package in.glg.rummy.models;

import org.simpleframework.xml.Attribute;

public class GameDetails {
    @Attribute(name = "game_id", required = false)
    private String gameId;
    @Attribute(name = "prizemoney", required = false)
    private String prizeMoney;

    public String getGameId() {
        return this.gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public String getPrizeMoney() {
        return this.prizeMoney;
    }

    public void setPrizeMoney(String prizeMoney) {
        this.prizeMoney = prizeMoney;
    }
}
