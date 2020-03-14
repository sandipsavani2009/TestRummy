package in.glg.rummy.models;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.io.Serializable;
import java.util.List;

@Root(strict = false)
public class Splitter implements Serializable {
    @ElementList(data = false, inline = true, name = "player", required = false)
    private List<GamePlayer> player;

    public List<GamePlayer> getPlayer() {
        return this.player;
    }

    public void setPlayer(List<GamePlayer> player) {
        this.player = player;
    }
}
