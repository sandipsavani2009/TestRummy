package in.glg.rummy.api.response;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;

import java.util.List;

import in.glg.rummy.models.Tournament;

/**
 * Created by GridLogic on 1/12/17.
 */

public class TournamentsDataResponse extends BaseResponse
{
    @Attribute(name = "data", required = false)
    private String data;

    @ElementList(name = "tournaments", required = false)
    private List<Tournament> tournaments;

    public List<Tournament> getTournaments() {
        return tournaments;
    }

    public void setTournaments(List<Tournament> tournaments) {
        this.tournaments = tournaments;
    }

    public String getData() {
        return this.data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public int getErrorMessage() {
        return 0;
    }
}
