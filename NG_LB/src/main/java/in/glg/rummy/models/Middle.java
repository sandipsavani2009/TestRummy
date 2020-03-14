package in.glg.rummy.models;

import org.simpleframework.xml.ElementList;

import java.util.List;

public class Middle {
    @ElementList(data = false, inline = true, name = "player", required = false)
    private List<GamePlayer> player;

    public List<GamePlayer> getPlayer() {
        return this.player;
    }

    public void setPlayer(List<GamePlayer> player) {
        this.player = player;
    }
}
