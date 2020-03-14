package in.glg.rummy.api.response;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;

import java.util.List;

import in.glg.rummy.models.GamePlayer;

/**
 * Created by GridLogic on 31/1/18.
 */

public class WinnerResponse extends BaseResponse
{
    @Attribute(name = "data", required = false)
    private String data;

    @Attribute(name = "tournament_id", required = false)
    private String tournament_id;

    @ElementList(name = "players", required = false)
    private List<GamePlayer> players;


    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getTournament_id() {
        return tournament_id;
    }

    public void setTournament_id(String tournament_id) {
        this.tournament_id = tournament_id;
    }

    public List<GamePlayer> getPlayers() {
        return players;
    }

    public void setPlayers(List<GamePlayer> players) {
        this.players = players;
    }

    @Override
    public int getErrorMessage() {
        return 0;
    }
}
