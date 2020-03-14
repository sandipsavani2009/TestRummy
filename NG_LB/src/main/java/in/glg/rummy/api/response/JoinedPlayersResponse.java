package in.glg.rummy.api.response;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;

import java.util.List;

import in.glg.rummy.models.JoinedPlayers;


/**
 * Created by GridLogic on 12/12/17.
 */

public class JoinedPlayersResponse extends BaseResponse
{
    @Attribute(name = "data", required = false)
    private String data;

    @Attribute(name = "tournament_id", required = false)
    private String tournament_id;

    @ElementList(name = "leader_board", required = false)
    private List<JoinedPlayers> joinedPlayers;

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public List<JoinedPlayers> getJoinedPlayers() {
        return joinedPlayers;
    }

    public void setJoinedPlayers(List<JoinedPlayers> joinedPlayers) {
        this.joinedPlayers = joinedPlayers;
    }

    public String getTournament_id() {
        return tournament_id;
    }

    public void setTournament_id(String tournament_id) {
        this.tournament_id = tournament_id;
    }

    @Override
    public int getErrorMessage() {
        return 0;
    }
}
