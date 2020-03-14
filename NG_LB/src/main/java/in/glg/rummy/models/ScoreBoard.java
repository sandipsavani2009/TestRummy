package in.glg.rummy.models;

import org.simpleframework.xml.ElementList;

import java.util.List;

public class ScoreBoard {
    @ElementList(data = false, inline = true, name = "game", required = false)
    private List<Game> game;

    public List<Game> getGame() {
        return this.game;
    }

    public void setGame(List<Game> game) {
        this.game = game;
    }
}
